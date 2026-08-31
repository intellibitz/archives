/**
 * BLUKIT NETWORK: NEARBY RESONANCE CONTROLLER
 *
 * High-performance resonance engine using Google Nearby Connections.
 * Implements a fully decentralized, hardware-encrypted resonance protocol.
 */
package cc.thevar.blukit.network.p2p

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import cc.thevar.blukit.data.crypto.CryptoManager
import cc.thevar.blukit.data.local.EchoLedger
import cc.thevar.blukit.data.repository.IdentityRepository
import cc.thevar.blukit.data.system.HapticManager
import cc.thevar.blukit.data.system.RadioStateManager
import cc.thevar.blukit.domain.model.ConnectionStatus
import cc.thevar.blukit.domain.model.Echo
import cc.thevar.blukit.domain.model.Source
import cc.thevar.blukit.domain.model.Sphere
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionOptions
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.json.Json
import java.io.File
import java.util.Collections
import java.util.LinkedList
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import javax.crypto.SecretKey
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * The primary resonance engine utilizing Google's Nearby Connections API.
 */
class NearbyResonanceController(
    private val context: Context,
    private val repository: IdentityRepository,
    private val echoLedger: EchoLedger,
    private val hapticManager: HapticManager,
    private val radioStateManager: RadioStateManager,
    private val cryptoManager: CryptoManager,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
) : ResonanceController {

    private val tag = "ResonanceController"
    private val serviceId = "cc.thevar.blukit.RESONANCE_SERVICE"
    private val connectionsClient: ConnectionsClient = Nearby.getConnectionsClient(context)
    private val internalScope = CoroutineScope(SupervisorJob() + mainDispatcher)

    // --- ResonanceController State Implementation ---
    private val _scannedSources = MutableStateFlow<List<Source>>(emptyList())
    override val scannedDevices = _scannedSources.asStateFlow()

    private val _isConnected = MutableStateFlow(value = false)
    override val isConnected = _isConnected.asStateFlow()

    private val _connectedSpheres = MutableStateFlow<Set<String>>(emptySet())
    override val connectedGroups = _connectedSpheres.asStateFlow()

    private val _incomingRadioRequests = MutableStateFlow<Set<Source>>(emptySet())
    override val incomingRadioRequests = _incomingRadioRequests.asStateFlow()

    private val _outgoingRadioRequests = MutableStateFlow<Set<Source>>(emptySet())
    override val outgoingRadioRequests = _outgoingRadioRequests.asStateFlow()

    private val _isSensing = MutableStateFlow(false)
    override val isDiscovering = _isSensing.asStateFlow()

    private val _isAdvertising = MutableStateFlow(false)
    override val isAdvertising = _isAdvertising.asStateFlow()

    private val _resonanceErrors = MutableStateFlow<ResonanceError?>(null)
    override val errors = _resonanceErrors.asStateFlow()

    private val _discoveredSpheres = MutableSharedFlow<Sphere>(extraBufferCapacity = 5)
    override val discoveredRooms = _discoveredSpheres.asSharedFlow()

    override val messages: StateFlow<List<Echo>> = echoLedger.echoes
    override val syncProgress: StateFlow<Float?> get() = _syncProgress.asStateFlow()
    private val _syncProgress = MutableStateFlow<Float?>(null)

    // --- Private Resonance State ---
    private val activeConnections = Collections.synchronizedSet(mutableSetOf<String>())
    private val pendingRadioRequests = Collections.synchronizedSet(mutableSetOf<String>())
    private val messageKeys = ConcurrentHashMap<String, SecretKey>()
    private val messageIdHistory = Collections.synchronizedList(LinkedList<String>())
    private val incomingFiles = ConcurrentHashMap<Long, Echo>()
    private val aggregateBuffer = ConcurrentHashMap<String, MutableList<Echo>>()
    private val outgoingQueues = ConcurrentHashMap<String, kotlinx.coroutines.channels.Channel<Payload>>()
    private val _connectionUpdates = MutableSharedFlow<Pair<String, ConnectionStatus>>(extraBufferCapacity = 20)

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            when (payload.type) {
                Payload.Type.BYTES -> handleReceivedBytes(endpointId, payload.asBytes()!!)
                Payload.Type.FILE -> {
                    incomingFiles[payload.id] = Echo(
                        messageId = "pending_${payload.id}",
                        senderId = endpointId,
                        senderName = "PENDING",
                        content = "",
                        timestamp = System.currentTimeMillis(),
                    )
                }
                else -> Log.w(tag, "Unsupported payload type received")
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            if (update.status == PayloadTransferUpdate.Status.SUCCESS) {
                val payloadId = update.payloadId
                Log.d(tag, "Payload SUCCESS: $payloadId")
                incomingFiles[payloadId]?.let { finalizeFileEcho(payloadId, it) }
            }
        }
    }

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            connectionsClient.acceptConnection(endpointId, payloadCallback)
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            if (result.status.isSuccess) {
                activeConnections.add(endpointId)
                internalScope.launch { _connectionUpdates.emit(endpointId to ConnectionStatus.Connected) }
                sendHandshake(endpointId)
            } else {
                internalScope.launch { _connectionUpdates.emit(endpointId to ConnectionStatus.Error(result.status.statusMessage ?: "Link Refused")) }
                pendingRadioRequests.remove(endpointId)
            }
        }

        override fun onDisconnected(endpointId: String) {
            activeConnections.remove(endpointId)
            messageKeys.remove(endpointId)
            pendingRadioRequests.remove(endpointId)
            _connectedSpheres.update { it - endpointId }
            if (activeConnections.isEmpty()) _isConnected.value = false
            updateScannedSources()
            internalScope.launch { _connectionUpdates.emit(endpointId to ConnectionStatus.ConnectionLost()) }
        }
    }

    private val discoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            val name = info.endpointName
            val parts = name.split("|")
            if (parts.size < 3) return
            
            val messageDeviceId = parts[2]
            val myDeviceId = repository.getDeviceId()
            val peerMedium = if (parts.size >= 4) { when (parts[3]) { "W" -> Source.ResonanceMedium.WIFI; "B" -> Source.ResonanceMedium.BLUETOOTH; else -> Source.ResonanceMedium.LOCATION } } else Source.ResonanceMedium.LOCATION
            val peerMessageCount = parts.getOrNull(4)?.toIntOrNull() ?: 0
            val peerIsLowPower = parts.getOrNull(5) == "P"
            val newSource = Source(id = endpointId, name = parts[1].ifBlank { "?" }, emoji = parts[0], persistentId = messageDeviceId, medium = peerMedium, messageCount = peerMessageCount, isLowPower = peerIsLowPower)
            _scannedSources.update { current -> current.filter { d -> d.id != endpointId } + newSource }
            
            if ((myDeviceId < messageDeviceId) && !activeConnections.contains(endpointId)) {
                val localName = "${repository.emojiAvatar.value}|${repository.getCurrentNickname()}|$myDeviceId|${getRadioFlag()}"
                val options = ConnectionOptions.Builder().build()
                connectionsClient.requestConnection(localName, endpointId, connectionLifecycleCallback, options)
            }
        }
        override fun onEndpointLost(endpointId: String) { _scannedSources.update { current -> current.filter { d -> d.id != endpointId } } }
    }

    override fun startDiscovery() {
        if (_isSensing.value) return
        _isSensing.value = true

        internalScope.launch(ioDispatcher) {
            while (_isSensing.value) {
                val peerCount = _scannedSources.value.size
                val scanDuration = if (peerCount > 10) 10.seconds else 30.seconds
                val idleDuration = if (peerCount > 10) 30.seconds else 5.seconds

                val options = DiscoveryOptions.Builder().setStrategy(getStrategy()).build()
                connectionsClient.startDiscovery(serviceId, discoveryCallback, options)
                    .addOnFailureListener { e ->
                        Log.e(tag, "Sensing failed to start: ${e.message}")
                        _resonanceErrors.value = ResonanceError.SensingError(e.message ?: "Failed to start sensing")
                    }

                delay(scanDuration)
                if (_isSensing.value) {
                    connectionsClient.stopDiscovery()
                    delay(idleDuration)
                }
            }
        }
    }

    override fun stopDiscovery() {
        _isSensing.value = false
        connectionsClient.stopDiscovery()
    }

    override fun startAdvertising() {
        if (_isAdvertising.value) return
        _isAdvertising.value = true
        internalScope.launch(ioDispatcher) {
            val options = AdvertisingOptions.Builder().setStrategy(getStrategy()).build()
            
            while (_isAdvertising.value) {
                val name = "${repository.emojiAvatar.value}|${repository.getCurrentNickname()}|${repository.getDeviceId()}|${getRadioFlag()}"
                
                connectionsClient.startAdvertising(name, serviceId, connectionLifecycleCallback, options)
                    .addOnFailureListener { e -> 
                        Log.e(tag, "Advertising failure: ${e.message}. Retrying in 5s...")
                        _resonanceErrors.value = ResonanceError.AdvertisingError(e.message ?: "Failed to start advertising")
                    }
                
                delay(1.minutes) 
                if (_isAdvertising.value) connectionsClient.stopAdvertising()
            }
        }
    }

    override fun stopAdvertising() {
        connectionsClient.stopAdvertising()
        _isAdvertising.value = false
    }

    override fun connectToDevice(device: Source): SharedFlow<ConnectionStatus> {
        val flow = MutableSharedFlow<ConnectionStatus>(replay = 1)
        internalScope.launch(ioDispatcher) {
            flow.emit(ConnectionStatus.Connecting)
            internalScope.launch { _connectionUpdates.filter { it.first == device.id }.collect { flow.emit(it.second) } }
            val name = "${repository.emojiAvatar.value}|${repository.getCurrentNickname()}|${repository.getDeviceId()}"
            val options = ConnectionOptions.Builder().build()
            connectionsClient.requestConnection(name, device.id, connectionLifecycleCallback, options)
                .addOnFailureListener { e -> flow.tryEmit(ConnectionStatus.Error(e.message ?: "Fail")) }
        }
        return flow.asSharedFlow()
    }

    override fun isNearbyConnected(endpointId: String): Boolean = activeConnections.contains(endpointId)

    override fun requestRadio(device: Source) {
        pendingRadioRequests.add(device.id)
        _outgoingRadioRequests.update { it + device }
        updateScannedSources()
        internalScope.launch(ioDispatcher) {
            sendMessageInternal(device.id, Echo(messageId = UUID.randomUUID().toString(), senderId = repository.getDeviceId(), senderName = repository.getCurrentNickname(), senderEmoji = repository.emojiAvatar.value, content = "RADIO_REQUEST", timestamp = System.currentTimeMillis(), type = Echo.TYPE_GROUP_REQUEST))
        }
    }

    override fun acceptRadio(device: Source) {
        _incomingRadioRequests.update { it - device }
        pendingRadioRequests.remove(device.id)
        _connectedSpheres.update { it + device.id }
        _isConnected.value = true
        _outgoingRadioRequests.update { current ->
            current.asSequence().filter { it.id != device.id }.toSet()
        }
        updateScannedSources()
        internalScope.launch(ioDispatcher) {
            sendMessageInternal(device.id, Echo(messageId = UUID.randomUUID().toString(), senderId = repository.getDeviceId(), senderName = repository.getCurrentNickname(), senderEmoji = repository.emojiAvatar.value, content = "RADIO_ACCEPT", timestamp = System.currentTimeMillis(), type = Echo.TYPE_GROUP_ACCEPT))
        }
    }

    override fun denyRadio(device: Source) {
        pendingRadioRequests.remove(device.id)
        _incomingRadioRequests.update { it - device }
        _outgoingRadioRequests.update { current ->
            current.asSequence().filter { it.id != device.id }.toSet()
        }
        updateScannedSources()
    }

    override fun joinRoom(groupId: String) {
        echoLedger.joinSphere(groupId, repository.getDeviceId())
    }

    private fun sendMessageInternal(endpointId: String, payload: Echo) {
        messageKeys[endpointId]?.let { key -> queueEcho(endpointId, Payload.fromBytes(cryptoManager.encrypt(Json.encodeToString(Echo.serializer(), payload).toByteArray(), key))) }
    }

    private suspend fun getEchoKeyWithRetry(id: String): SecretKey? {
        var a = 0
        while ((messageKeys[id] == null) && (a < 50)) {
            delay(20.milliseconds)
            a++
        }
        return messageKeys[id]
    }

    private fun syncEchoHistory(endpointId: String) {
        internalScope.launch(ioDispatcher) {
            val key = getEchoKeyWithRetry(endpointId) ?: return@launch
            val latestEchoId = echoLedger.getLatestEchoId()
            val request = Echo(
                messageId = UUID.randomUUID().toString(),
                senderId = repository.getDeviceId(),
                senderName = "SYNC_BOOTSTRAP",
                content = latestEchoId ?: "0",
                timestamp = System.currentTimeMillis(),
                type = Echo.TYPE_RESYNC_REQUEST
            )
            sendMessageInternal(endpointId, request, key)
            Log.i(tag, "Differential Sync: Bootstrapping resonance with $endpointId from $latestEchoId")
        }
    }

    private fun backupRecentRecords(endpointId: String) {
        internalScope.launch(ioDispatcher) {
            val key = getEchoKeyWithRetry(endpointId) ?: return@launch
            val recentRecords = echoLedger.echoes.value
                .filter { it.type == Echo.TYPE_MEMORY || it.type == Echo.TYPE_IMAGE }
                .takeLast(10)
            
            if (recentRecords.isEmpty()) return@launch
            
            val backupPayload = Echo(
                messageId = UUID.randomUUID().toString(),
                senderId = repository.getDeviceId(),
                senderName = "BACKUP_ORCHESTRATOR",
                content = Json.encodeToString(recentRecords),
                timestamp = System.currentTimeMillis(),
                type = Echo.TYPE_RESYNC_CHUNK,
            )
            sendMessageInternal(endpointId, backupPayload, key)
            Log.i(tag, "Black Box: Resonance backup emitted to $endpointId")
        }
    }

    override suspend fun sendMessage(content: String, receiverId: String?, messageScope: Int, messageId: String?, groupId: String?, groupName: String?, type: Int): Echo? {
        val sphere = groupId?.let { echoLedger.getSphere(it) }
        
        groupId?.let { gid ->
            if (!echoLedger.isMember(gid, repository.getDeviceId())) {
                Log.w(tag, "Resonance Denied: Source not a member of $gid")
                return null
            }
        }

        val payload = Echo(
            messageId = messageId ?: UUID.randomUUID().toString(), 
            senderId = repository.getDeviceId(), 
            senderName = repository.getCurrentNickname(), 
            senderEmoji = repository.emojiAvatar.value, 
            receiverId = receiverId, 
            groupId = groupId, 
            groupName = groupName, 
            content = content, 
            timestamp = System.currentTimeMillis(), 
            messageScope = messageScope,
            type = type,
            hopCount = 0,
            anchoredPublicSphereId = sphere?.anchoredPublicSphereId
        )

        if (!payload.isPriority && receiverId == null && activeConnections.size > 5) {
            groupId?.let { gid ->
                aggregateBuffer.getOrPut(gid) { mutableListOf() }.add(payload)
                return payload
            }
        }

        return dispatchEcho(payload)
    }

    private suspend fun dispatchEcho(payload: Echo): Echo? {
        val bytes = Json.encodeToString(Echo.serializer(), payload).toByteArray()
        val targetRid = payload.receiverId
        try {
            if (targetRid != null) { 
                getEchoKeyWithRetry(targetRid)?.let { key ->
                    queueEcho(targetRid, Payload.fromBytes(cryptoManager.encrypt(bytes, key))) 
                } 
            } else { 
                val targets = if (activeConnections.size > 20) {
                    activeConnections.shuffled().take(10) 
                } else {
                    activeConnections
                }
                
                targets.forEach { target -> 
                    internalScope.launch(ioDispatcher) { 
                        messageKeys[target]?.let { key -> 
                            try { queueEcho(target, Payload.fromBytes(cryptoManager.encrypt(bytes, key))) } catch (_: Exception) {} 
                        } 
                    } 
                } 
            }
            synchronized(messageIdHistory) {
                messageIdHistory.add(payload.messageId)
                if (messageIdHistory.size > 500) messageIdHistory.removeAt(0)
            }
            echoLedger.upsertEcho(payload)
            return payload
        } catch (_: Exception) {
            return null
        }
    }

    override suspend fun broadcastMessage(content: String, messageScope: Int, messageId: String?, groupId: String?, groupName: String?, type: Int): Echo? = 
        sendMessage(content, null, messageScope, messageId, groupId, groupName, type)

    override suspend fun broadcastIdentityUpdate(oldName: String): Echo {
        val payload = Echo(messageId = UUID.randomUUID().toString(), senderId = repository.getDeviceId(), senderName = repository.getCurrentNickname(), senderEmoji = repository.emojiAvatar.value, content = oldName, timestamp = System.currentTimeMillis(), type = Echo.TYPE_IDENTITY_UPDATE, messageScope = Echo.MESSAGE_SHOUT)
        val bytes = Json.encodeToString(Echo.serializer(), payload).toByteArray()
        activeConnections.forEach { target -> internalScope.launch(ioDispatcher) { messageKeys[target]?.let { key -> try { queueEcho(target, Payload.fromBytes(cryptoManager.encrypt(bytes, key))) } catch (_: Exception) {} } } }
        return payload
    }

    override suspend fun sendFile(fileUri: Uri, receiverId: String?, messageScope: Int, groupId: String?, groupName: String?): Echo? {
        val fileName = getFileName(fileUri)
        val fileSize = getFileSize(fileUri)
        val mimeType = context.contentResolver.getType(fileUri)
        val messageId = UUID.randomUUID().toString()
        return try {
            val pfd = context.contentResolver.openFileDescriptor(fileUri, "r") ?: return null
            val filePayload = Payload.fromFile(pfd)
            val payload = Echo(messageId = messageId, senderId = repository.getDeviceId(), senderName = repository.getCurrentNickname(), senderEmoji = repository.emojiAvatar.value, receiverId = receiverId, groupId = groupId, groupName = groupName, content = if (mimeType?.startsWith("image/") == true) fileUri.toString() else "[FILE] $fileName", timestamp = System.currentTimeMillis(), type = if (mimeType?.startsWith("image/") == true) Echo.TYPE_IMAGE else Echo.TYPE_FILE, messageScope = messageScope, fileId = filePayload.id, fileName = fileName, fileSize = fileSize, mimeType = mimeType)
            internalScope.launch(ioDispatcher) {
                if (receiverId != null) { sendMessageInternal(receiverId, payload); connectionsClient.sendPayload(receiverId, filePayload) }
                else { activeConnections.forEach { target -> internalScope.launch { sendMessageInternal(target, payload); connectionsClient.sendPayload(target, filePayload) } } }
            }
            echoLedger.upsertEcho(payload)
            payload
        } catch (_: Exception) {
            null
        }
    }

    private fun getFileName(uri: Uri): String? {
        var name: String? = null
        if (uri.scheme == "content") { context.contentResolver.query(uri, null, null, null, null)?.use { cursor -> if (cursor.moveToFirst()) name = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)) } }
        return name ?: uri.path?.let { File(it).name }
    }

    private fun getFileSize(uri: Uri): Long? {
        var size: Long? = null
        if (uri.scheme == "content") { context.contentResolver.query(uri, null, null, null, null)?.use { cursor -> if (cursor.moveToFirst()) size = cursor.getLong(cursor.getColumnIndexOrThrow(OpenableColumns.SIZE)) } }
        return size
    }

    override suspend fun sendGroupMessage(content: String, groupId: String): Echo? {
        val sphere = echoLedger.getSphere(groupId) ?: return null
        val payload = Echo(messageId = UUID.randomUUID().toString(), senderId = repository.getDeviceId(), senderName = repository.getCurrentNickname(), senderEmoji = repository.emojiAvatar.value, groupId = groupId, content = content, timestamp = System.currentTimeMillis(), messageScope = Echo.MESSAGE_WHISPER)
        val bytes = Json.encodeToString(Echo.serializer(), payload).toByteArray()
        return try {
            sphere.allMemberIds.forEach { memberId -> internalScope.launch(ioDispatcher) { messageKeys[memberId]?.let { key -> try { queueEcho(memberId, Payload.fromBytes(cryptoManager.encrypt(bytes, key))) } catch (_: Exception) {} } } }
            activeConnections.filter { it !in sphere.allMemberIds }.forEach { target -> internalScope.launch(ioDispatcher) { messageKeys[target]?.let { key -> try { queueEcho(target, Payload.fromBytes(cryptoManager.encrypt(bytes, key))) } catch (_: Exception) {} } } }
            synchronized(messageIdHistory) { messageIdHistory.add(payload.messageId); if (messageIdHistory.size > 500) messageIdHistory.removeAt(0) }
            echoLedger.upsertEcho(payload); echoLedger.updateSphereLastEcho(groupId, payload.timestamp)
            payload
        } catch (_: Exception) { null }
    }

    override suspend fun sendNoteUpdate(groupId: String, content: String, messageId: String?, version: Int): Echo? {
        val sphere = echoLedger.getSphere(groupId) ?: return null
        val payload = Echo(
            messageId = messageId ?: UUID.randomUUID().toString(),
            senderId = repository.getDeviceId(),
            senderName = repository.getCurrentNickname(),
            senderEmoji = repository.emojiAvatar.value,
            groupId = groupId,
            content = content,
            timestamp = System.currentTimeMillis(),
            type = Echo.TYPE_NOTE_UPDATE,
            noteVersion = version,
            messageScope = Echo.MESSAGE_WHISPER,
        )
        val bytes = Json.encodeToString(Echo.serializer(), payload).toByteArray()
        return try {
            sphere.allMemberIds.forEach { memberId -> internalScope.launch(ioDispatcher) { messageKeys[memberId]?.let { key -> try { queueEcho(memberId, Payload.fromBytes(cryptoManager.encrypt(bytes, key))) } catch (_: Exception) {} } } }
            activeConnections.filter { it !in sphere.allMemberIds }.forEach { target -> internalScope.launch(ioDispatcher) { messageKeys[target]?.let { key -> try { queueEcho(target, Payload.fromBytes(cryptoManager.encrypt(bytes, key))) } catch (_: Exception) {} } } }
            synchronized(messageIdHistory) { messageIdHistory.add(payload.messageId); if (messageIdHistory.size > 500) messageIdHistory.removeAt(0) }
            echoLedger.upsertEcho(payload); echoLedger.updateSphereLastEcho(groupId, payload.timestamp)
            payload
        } catch (_: Exception) { null }
    }

    override fun startGroupRoom(name: String, members: Set<String>, type: Int, groupId: String?, parentId: String?, anchoredPublicSphereId: String?): String {
        val gid = groupId ?: Sphere.generateId(name, type)
        internalScope.launch(ioDispatcher) { 
            echoLedger.insertSphere(
                Sphere(
                    id = gid,
                    name = name,
                    memberIds = members + repository.getDeviceId(),
                    scope = type,
                    parentId = parentId,
                    ownerId = repository.getDeviceId(),
                    anchoredPublicSphereId = anchoredPublicSphereId
                )
            ) 
            
            // If this is an anchored group, advertise it to the public sphere
            if (anchoredPublicSphereId != null) {
                broadcastAnchorAdvertisement(gid, name, anchoredPublicSphereId)
            }
        }
        return gid
    }

    private suspend fun broadcastAnchorAdvertisement(anchoredGid: String, name: String, anchorPublicGid: String) {
        val advertisement = Echo(
            messageId = UUID.randomUUID().toString(),
            senderId = repository.getDeviceId(),
            senderName = repository.getCurrentNickname(),
            groupId = anchoredGid,
            groupName = name,
            content = "PRIVATE SPHERE AVAILABLE",
            timestamp = System.currentTimeMillis(),
            type = Echo.TYPE_ANCHOR_ADVERTISEMENT,
            messageScope = Echo.MESSAGE_SHOUT,
            anchoredPublicSphereId = anchorPublicGid
        )
        dispatchEcho(advertisement)
    }

    override fun updateGroupMembers(groupId: String, memberIds: Set<String>) {
        internalScope.launch(ioDispatcher) { echoLedger.updateSphereMembers(groupId, memberIds) }
    }

    override fun updateGroupScope(groupId: String, scope: Int) {
        internalScope.launch(ioDispatcher) { echoLedger.updateSphereScope(groupId, scope) }
    }

    override fun initiateHistorySync(endpointId: String, sinceTimestamp: Long?) {
        internalScope.launch(ioDispatcher) {
            val key = getEchoKeyWithRetry(endpointId) ?: return@launch
            val request = Echo(
                messageId = UUID.randomUUID().toString(),
                senderId = repository.getDeviceId(),
                senderName = "SYNC_REQUEST",
                content = sinceTimestamp?.toString() ?: "0",
                timestamp = System.currentTimeMillis(),
                type = Echo.TYPE_RESYNC_REQUEST
            )
            sendMessageInternal(endpointId, request, key)
        }
    }

    private fun sendMessageInternal(endpointId: String, payload: Echo, key: SecretKey) {
        try {
            val bytes = Json.encodeToString(Echo.serializer(), payload).toByteArray()
            queueEcho(endpointId, Payload.fromBytes(cryptoManager.encrypt(bytes, key)))
        } catch (_: Exception) {
            Log.e(tag, "Failed to send Echo to $endpointId")
        }
    }

    override fun closeConnection() {
        activeConnections.forEach { connectionsClient.disconnectFromEndpoint(it) }
        activeConnections.clear()
        messageKeys.clear()
        _isConnected.value = false
        _connectedSpheres.value = emptySet()
    }

    override fun release() {
        stopDiscovery()
        stopAdvertising()
        closeConnection()
        internalScope.cancel()
    }

    private fun handleReceivedBytes(endpointId: String, bytes: ByteArray) {
        if (isHandshakePayload(bytes)) {
            handleHandshake(endpointId, bytes)
        } else {
            messageKeys[endpointId]?.let { key ->
                try {
                    val decrypted = cryptoManager.decrypt(bytes, key)
                    val payload = Json.decodeFromString<Echo>(decrypted.decodeToString())
                    handleIncomingPayload(endpointId, payload, key)
                } catch (_: Exception) {
                    Log.e(tag, "Decryption or parsing failure")
                }
            }
        }
    }

    private fun isHandshakePayload(bytes: ByteArray): Boolean = bytes.isNotEmpty() && (bytes[0] == 0x01.toByte())

    private fun handleHandshake(endpointId: String, bytes: ByteArray) {
        try {
            val peerPublicKeyBytes = bytes.copyOfRange(1, bytes.size)
            val keyFactory = java.security.KeyFactory.getInstance("EC")
            val peerPublicKey = keyFactory.generatePublic(java.security.spec.X509EncodedKeySpec(peerPublicKeyBytes))

            val secretKey = cryptoManager.deriveSharedSecret(peerPublicKey)
            messageKeys[endpointId] = secretKey
            Log.i(tag, "SECURE: Resonance established with $endpointId")
            
            syncEchoHistory(endpointId)
            backupRecentRecords(endpointId)
        } catch (_: Exception) {
            Log.e(tag, "Handshake failed")
        }
    }

    private fun handleIncomingPayload(endpointId: String, payload: Echo, secretKey: SecretKey) {
        when (payload.type) {
            Echo.TYPE_ACK -> handleAck(payload)
            Echo.TYPE_IDENTITY_UPDATE -> handleIdentityUpdate(endpointId, payload, secretKey)
            Echo.TYPE_GROUP_REQUEST -> handleGroupRequest(endpointId, payload)
            Echo.TYPE_GROUP_ACCEPT -> handleGroupAccept(endpointId)
            Echo.TYPE_RESYNC_REQUEST -> handleSyncRequest(endpointId, payload, secretKey)
            Echo.TYPE_RESYNC_CHUNK -> handleSyncChunk(payload)
            Echo.TYPE_RESYNC_COMPLETE -> handleSyncComplete(endpointId, payload)
            Echo.TYPE_ANCHOR_ADVERTISEMENT -> handleAnchorAdvertisement(payload)
            else -> handleEcho(endpointId, payload, secretKey)
        }
    }

    private fun handleAck(payload: Echo) {
        internalScope.launch(ioDispatcher) {
            echoLedger.updateEchoStatus(payload.messageId, Echo.STATUS_DELIVERED)
        }
    }

    private fun handleIdentityUpdate(endpointId: String, payload: Echo, secretKey: SecretKey) {
        _scannedSources.update { current -> current.map { if ((it.persistentId == payload.senderId) || (it.id == endpointId)) it.copy(name = payload.senderName, emoji = payload.senderEmoji ?: it.emoji) else it } }
        saveIncomingEcho(payload)
        sendAck(endpointId, payload.messageId, payload.senderId, secretKey)
        relayEcho(endpointId, payload)
    }

    private fun handleGroupRequest(endpointId: String, payload: Echo) {
        val source = _scannedSources.value.find { it.id == endpointId } ?: Source(id = endpointId, name = payload.senderName, emoji = payload.senderEmoji ?: "👤")
        _incomingRadioRequests.update { it + source }
        hapticManager.triggerMessage(HapticManager.MessageType.CONNECTION)
    }

    private fun handleGroupAccept(endpointId: String) {
        pendingRadioRequests.remove(endpointId)
        _outgoingRadioRequests.update { current ->
            current.asSequence().filter { it.id != endpointId }.toSet()
        }
        _connectedSpheres.update { it + endpointId }
        _isConnected.value = true
        updateScannedSources()
    }

    private fun handleSyncRequest(endpointId: String, payload: Echo, secretKey: SecretKey) {
        val sinceId = payload.content
        val allLocalEchoes = echoLedger.echoes.value
        val sinceTimestamp = if (sinceId == "0") 0L else {
            allLocalEchoes.find { it.messageId == sinceId }?.timestamp ?: 0L
        }
        
        val historyToSync = echoLedger.getRawEchoesSince(sinceTimestamp)
        
        internalScope.launch(ioDispatcher) {
            _syncProgress.value = 0.1f
            historyToSync.chunked(5).forEachIndexed { index, chunk ->
                val chunkPayload = Echo(
                    messageId = UUID.randomUUID().toString(),
                    senderId = repository.getDeviceId(),
                    senderName = "SYNC_CHUNK",
                    content = Json.encodeToString(chunk.map { it.decodeToString() }),
                    timestamp = System.currentTimeMillis(),
                    type = Echo.TYPE_RESYNC_CHUNK,
                )
                sendMessageInternal(endpointId, chunkPayload, secretKey)
                _syncProgress.value = 0.1f + ((index.toFloat() / (historyToSync.size / 5f + 1)) * 0.8f)
            }
            
            val complete = Echo(
                messageId = UUID.randomUUID().toString(),
                senderId = repository.getDeviceId(),
                senderName = "SYNC",
                content = "COMPLETE",
                timestamp = System.currentTimeMillis(),
                type = Echo.TYPE_RESYNC_COMPLETE,
            )
            sendMessageInternal(endpointId, complete, secretKey)
            _syncProgress.value = 1.0f
            delay(1.seconds)
            _syncProgress.value = null
        }
    }

    private fun handleSyncChunk(payload: Echo) {
        try {
            val encryptedMessages = Json.decodeFromString<List<String>>(payload.content)
            encryptedMessages.forEach { encryptedStr ->
                try {
                    val encryptedBytes = encryptedStr.toByteArray()
                    val decrypted = cryptoManager.decryptLocal(encryptedBytes)
                    val echo = Json.decodeFromString<Echo>(decrypted.decodeToString())
                    if (isNewEcho(echo.messageId)) {
                        echoLedger.upsertEcho(echo)
                    }
                } catch (_: Exception) {}
            }
        } catch (_: Exception) {
            Log.e(tag, "Failed to parse sync chunk")
        }
    }

    private fun handleSyncComplete(endpointId: String, payload: Echo) {
        _syncProgress.value = 1.0f
        internalScope.launch {
            delay(1.seconds)
            _syncProgress.value = null
        }
    }

    private fun handleAnchorAdvertisement(payload: Echo) {
        val anchoredGid = payload.groupId ?: return
        val anchorPublicGid = payload.anchoredPublicSphereId ?: return
        val gName = payload.groupName ?: "ANCHORED SPHERE"
        
        if (echoLedger.getSphere(anchoredGid) == null) {
            echoLedger.insertSphere(
                Sphere(
                    id = anchoredGid,
                    name = gName,
                    scope = Sphere.SCOPE_PRIVATE,
                    anchoredPublicSphereId = anchorPublicGid,
                    isMeta = true
                )
            )
            Log.i(tag, "AIR Discovery: Anchored sphere $gName found in $anchorPublicGid")
        }
    }

    private fun handleEcho(endpointId: String, payload: Echo, secretKey: SecretKey) {
        if (isSpam(payload)) return
        sendAck(endpointId, payload.messageId, payload.senderId, secretKey)
        if (payload.receiverId.isNullOrEmpty()) relayEcho(endpointId, payload)
        saveIncomingEcho(payload)
        
        val gid = payload.groupId
        val gName = payload.groupName
        if ((gid != null) && (gName != null)) {
            val existing = echoLedger.getSphere(gid)
            if (existing == null) {
                val scope = when (payload.messageScope) {
                    Echo.MESSAGE_SHOUT -> Sphere.SCOPE_PUBLIC
                    Echo.MESSAGE_SILENCE -> Sphere.SCOPE_LOCAL
                    else -> Sphere.SCOPE_PRIVATE
                }
                echoLedger.insertSphere(
                    Sphere(
                        id = gid,
                        name = gName,
                        scope = scope,
                        parentId = Sphere.ID_GLOBAL,
                        anchoredPublicSphereId = payload.anchoredPublicSphereId
                    )
                )
            }
        }
        hapticManager.triggerMessage(HapticManager.MessageType.MESSAGE)
    }

    private fun saveIncomingEcho(payload: Echo) {
        if (isNewEcho(payload.messageId)) {
            echoLedger.upsertEcho(payload)
        } else {
            echoLedger.incrementAnchoredCount(payload.messageId)
        }
    }

    private fun sendAck(endpointId: String, messageId: String, receiverId: String, secretKey: SecretKey) {
        val ack = Echo(
            messageId = messageId,
            senderId = repository.getDeviceId(),
            senderName = "ACK",
            content = "",
            timestamp = System.currentTimeMillis(),
            type = Echo.TYPE_ACK,
            receiverId = receiverId
        )
        sendMessageInternal(endpointId, ack, secretKey)
    }

    private fun relayEcho(sourceEndpointId: String, payload: Echo) {
        if (payload.hopCount >= 3) return
        
        internalScope.launch(ioDispatcher) {
            val myId = repository.getDeviceId()
            if (payload.senderId == myId) return@launch
            
            val relayedEcho = payload.copy(hopCount = payload.hopCount + 1)
            val json = Json.encodeToString(Echo.serializer(), relayedEcho)
            val bytes = json.encodeToByteArray()
            
            activeConnections.forEach { endpointId ->
                if (endpointId != sourceEndpointId) {
                    messageKeys[endpointId]?.let { key ->
                        try {
                            queueEcho(endpointId, Payload.fromBytes(cryptoManager.encrypt(bytes, key)))
                        } catch (_: Exception) {
                            Log.e(tag, "Relay fail to $endpointId")
                        }
                    }
                }
            }
        }
    }

    private fun isNewEcho(messageId: String): Boolean = synchronized(messageIdHistory) {
        if (messageIdHistory.contains(messageId)) false else {
            messageIdHistory.add(messageId)
            if (messageIdHistory.size > 500) messageIdHistory.removeAt(0)
            true
        }
    }

    private fun isSpam(payload: Echo): Boolean {
        val now = System.currentTimeMillis()
        val recentFromSender = echoLedger.echoes.value.count { it.senderId == payload.senderId && (now - it.timestamp) < 10000 }
        return recentFromSender > 5
    }

    private fun sendHandshake(endpointId: String) {
        val localPublicKeyBytes = cryptoManager.getLocalKeyPair().public.encoded
        val handshakePayload = byteArrayOf(0x01.toByte()) + localPublicKeyBytes
        connectionsClient.sendPayload(endpointId, Payload.fromBytes(handshakePayload))
    }

    private fun queueEcho(endpointId: String, payload: Payload) {
        val queue = outgoingQueues[endpointId] ?: run {
            val newQueue = kotlinx.coroutines.channels.Channel<Payload>(kotlinx.coroutines.channels.Channel.UNLIMITED)
            val existing = outgoingQueues.putIfAbsent(endpointId, newQueue)
            if (existing == null) {
                processQueueForEndpoint(endpointId, newQueue)
                newQueue
            } else {
                existing
            }
        }
        queue.trySend(payload)
    }

    private fun processQueueForEndpoint(endpointId: String, queue: kotlinx.coroutines.channels.Channel<Payload>) {
        internalScope.launch(ioDispatcher) {
            try {
                for (payload in queue) {
                    try {
                        suspendCancellableCoroutine<Unit> { continuation ->
                            connectionsClient.sendPayload(endpointId, payload)
                                .addOnSuccessListener { continuation.resume(Unit) }
                                .addOnFailureListener { _ -> 
                                    Log.e(tag, "Failed to send payload to $endpointId")
                                    if (continuation.isActive) continuation.resume(Unit)
                                }
                        }
                    } catch (_: Exception) {
                        Log.e(tag, "Failed to send payload to $endpointId")
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                Log.e(tag, "Queue iterator failed for $endpointId")
            }
        }
    }

    private fun updateScannedSources() {
        _scannedSources.update { current ->
            current.map { source ->
                source.copy(
                    isConnected = source.id in activeConnections,
                    isGroupPending = source.id in pendingRadioRequests
                )
            }
        }
    }

    private fun getStrategy(): Strategy = Strategy.P2P_CLUSTER

    private fun getRadioFlag(): String {
        val b = if (radioStateManager.radioStates.value.isBluetoothEnabled) "B" else ""
        val w = if (radioStateManager.radioStates.value.isWifiEnabled) "W" else ""
        val p = if (repository.lowPowerMode.value) "P" else ""
        return "$b$w$p"
    }

    private fun finalizeFileEcho(payloadId: Long, partial: Echo) {
        echoLedger.upsertEcho(partial.copy(messageId = "file_$payloadId", content = "FILE_RECEIVED"))
    }
}
