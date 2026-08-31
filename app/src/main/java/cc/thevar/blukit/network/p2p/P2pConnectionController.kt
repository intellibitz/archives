/**
 * BLUKIT NETWORK: P2P CONNECTION CONTROLLER
 *
 * High-performance connection engine using Google Nearby Connections.
 * Orchestrates low-level sensing and advertising while delegating session
 * security to [SecureSessionManager] and handshaking to [HandshakeProtocol].
 * Implements a fully decentralized, hardware-encrypted connection protocol.
 */
package cc.thevar.blukit.network.p2p

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import cc.thevar.blukit.data.crypto.CryptoManager
import cc.thevar.blukit.data.local.MessageRepository
import cc.thevar.blukit.data.repository.IdentityRepository
import cc.thevar.blukit.data.system.HapticManager
import cc.thevar.blukit.data.system.RadioStateManager
import cc.thevar.blukit.domain.model.ConnectionStatus
import cc.thevar.blukit.domain.model.Message
import cc.thevar.blukit.domain.model.Source
import cc.thevar.blukit.domain.model.Group
import cc.thevar.blukit.domain.protocol.HandshakeProtocol
import cc.thevar.blukit.domain.security.SecureSessionManager
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
 * The primary connection engine utilizing Google's Nearby Connections API.
 */
class P2pConnectionController(
    private val context: Context,
    private val repository: IdentityRepository,
    private val messageLedger: MessageRepository,
    private val hapticManager: HapticManager,
    private val radioStateManager: RadioStateManager,
    private val cryptoManager: CryptoManager,
    private val sessionManager: SecureSessionManager,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
) : ConnectionController {

    private val tag = "ConnectionController"
    private val serviceId = "cc.thevar.blukit.CONNECTION_SERVICE"
    private val connectionsClient: ConnectionsClient = Nearby.getConnectionsClient(context)
    private val internalScope = CoroutineScope(SupervisorJob() + mainDispatcher)

    // --- ConnectionController State Implementation ---
    private val _scannedSources = MutableStateFlow<List<Source>>(emptyList())
    override val scannedDevices = _scannedSources.asStateFlow()

    private val _isConnected = MutableStateFlow(value = false)
    override val isConnected = _isConnected.asStateFlow()

    private val _connectedGroups = MutableStateFlow<Set<String>>(emptySet())
    override val connectedGroups = _connectedGroups.asStateFlow()

    private val _incomingRadioRequests = MutableStateFlow<Set<Source>>(emptySet())
    override val incomingRadioRequests = _incomingRadioRequests.asStateFlow()

    private val _outgoingRadioRequests = MutableStateFlow<Set<Source>>(emptySet())
    override val outgoingRadioRequests = _outgoingRadioRequests.asStateFlow()

    private val _isNearbyNearby = MutableStateFlow(false)
    override val isDiscovering = _isNearbyNearby.asStateFlow()

    private val _isAdvertising = MutableStateFlow(false)
    override val isAdvertising = _isAdvertising.asStateFlow()

    private val _connectionErrors = MutableStateFlow<ConnectionError?>(null)
    override val errors = _connectionErrors.asStateFlow()

    private val _discoveredGroups = MutableSharedFlow<Group>(extraBufferCapacity = 5)
    override val discoveredGroups = _discoveredGroups.asSharedFlow()

    override val messages: StateFlow<List<Message>> = messageLedger.messages
    override val syncProgress: StateFlow<Float?> get() = _syncProgress.asStateFlow()
    private val _syncProgress = MutableStateFlow<Float?>(null)

    // --- Private Connection State ---
    private val activeConnections = Collections.synchronizedSet(mutableSetOf<String>())
    private val pendingRadioRequests = Collections.synchronizedSet(mutableSetOf<String>())
    private val messageIdHistory = Collections.synchronizedList(LinkedList<String>())
    private val incomingFiles = ConcurrentHashMap<Long, Message>()
    private val aggregateBuffer = ConcurrentHashMap<String, MutableList<Message>>()
    private val outgoingQueues = ConcurrentHashMap<String, kotlinx.coroutines.channels.Channel<Payload>>()
    private val _connectionUpdates = MutableSharedFlow<Pair<String, ConnectionStatus>>(extraBufferCapacity = 20)

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            when (payload.type) {
                Payload.Type.BYTES -> handleReceivedBytes(endpointId, payload.asBytes()!!)
                Payload.Type.FILE -> {
                    incomingFiles[payload.id] = Message(
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
                incomingFiles[payloadId]?.let { finalizeFileMessage(payloadId, it) }
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
            sessionManager.terminateSession(endpointId)
            pendingRadioRequests.remove(endpointId)
            _connectedGroups.update { it - endpointId }
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
            val peerMedium = if (parts.size >= 4) { when (parts[3]) { "W" -> Source.ConnectionMedium.WIFI; "B" -> Source.ConnectionMedium.BLUETOOTH; else -> Source.ConnectionMedium.LOCATION } } else Source.ConnectionMedium.LOCATION
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
        if (_isNearbyNearby.value) return
        _isNearbyNearby.value = true

        internalScope.launch(ioDispatcher) {
            while (_isNearbyNearby.value) {
                val peerCount = _scannedSources.value.size
                val scanDuration = if (peerCount > 10) 10.seconds else 30.seconds
                val idleDuration = if (peerCount > 10) 30.seconds else 5.seconds

                val options = DiscoveryOptions.Builder().setStrategy(getStrategy()).build()
                connectionsClient.startDiscovery(serviceId, discoveryCallback, options)
                    .addOnFailureListener { e ->
                        Log.e(tag, "Nearby sensing failed to start: ${e.message}")
                        _connectionErrors.value = ConnectionError.NearbyError(e.message ?: "Failed to start nearby sensing")
                    }

                delay(scanDuration)
                if (_isNearbyNearby.value) {
                    connectionsClient.stopDiscovery()
                    delay(idleDuration)
                }
            }
        }
    }

    override fun stopDiscovery() {
        _isNearbyNearby.value = false
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
                        _connectionErrors.value = ConnectionError.AdvertisingError(e.message ?: "Failed to start advertising")
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
            sendMessageInternal(device.id, Message(messageId = UUID.randomUUID().toString(), senderId = repository.getDeviceId(), senderName = repository.getCurrentNickname(), senderEmoji = repository.emojiAvatar.value, content = "RADIO_REQUEST", timestamp = System.currentTimeMillis(), type = Message.TYPE_GROUP_REQUEST))
        }
    }

    override fun acceptRadio(device: Source) {
        _incomingRadioRequests.update { it - device }
        pendingRadioRequests.remove(device.id)
        _connectedGroups.update { it + device.id }
        _isConnected.value = true
        _outgoingRadioRequests.update { current ->
            current.asSequence().filter { it.id != device.id }.toSet()
        }
        updateScannedSources()
        internalScope.launch(ioDispatcher) {
            sendMessageInternal(device.id, Message(messageId = UUID.randomUUID().toString(), senderId = repository.getDeviceId(), senderName = repository.getCurrentNickname(), senderEmoji = repository.emojiAvatar.value, content = "RADIO_ACCEPT", timestamp = System.currentTimeMillis(), type = Message.TYPE_GROUP_ACCEPT))
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

    override fun joinGroup(groupId: String) {
        messageLedger.joinGroup(groupId, repository.getDeviceId())
    }

    private fun sendMessageInternal(endpointId: String, payload: Message) {
        val bytes = Json.encodeToString(Message.serializer(), payload).toByteArray()
        sessionManager.encryptForSession(endpointId, bytes)?.let { encrypted ->
            queueMessage(endpointId, Payload.fromBytes(encrypted))
        }
    }

    private suspend fun getMessageKeyWithRetry(id: String): SecretKey? {
        var a = 0
        while ((sessionManager.getSessionKey(id) == null) && (a < 50)) {
            delay(20.milliseconds)
            a++
        }
        return sessionManager.getSessionKey(id)
    }

    private fun syncMessageHistory(endpointId: String) {
        internalScope.launch(ioDispatcher) {
            val key = getMessageKeyWithRetry(endpointId) ?: return@launch
            val latestMessageId = messageLedger.getLatestMessageId()
            val request = Message(
                messageId = UUID.randomUUID().toString(),
                senderId = repository.getDeviceId(),
                senderName = "SYNC_BOOTSTRAP",
                content = latestMessageId ?: "0",
                timestamp = System.currentTimeMillis(),
                type = Message.TYPE_RESYNC_REQUEST
            )
            sendMessageInternal(endpointId, request, key)
            Log.i(tag, "Differential Sync: Bootstrapping connection with $endpointId from $latestMessageId")
        }
    }

    private fun backupRecentRecords(endpointId: String) {
        internalScope.launch(ioDispatcher) {
            val key = getMessageKeyWithRetry(endpointId) ?: return@launch
            val recentRecords = messageLedger.messages.value
                .filter { it.type == Message.TYPE_MEMORY || it.type == Message.TYPE_IMAGE }
                .takeLast(10)
            
            if (recentRecords.isEmpty()) return@launch
            
            val backupPayload = Message(
                messageId = UUID.randomUUID().toString(),
                senderId = repository.getDeviceId(),
                senderName = "BACKUP_ORCHESTRATOR",
                content = Json.encodeToString(recentRecords),
                timestamp = System.currentTimeMillis(),
                type = Message.TYPE_RESYNC_CHUNK,
            )
            sendMessageInternal(endpointId, backupPayload, key)
            Log.i(tag, "Black Box: Connection backup emitted to $endpointId")
        }
    }

    override suspend fun sendMessage(content: String, receiverId: String?, messageScope: Int, messageId: String?, groupId: String?, groupName: String?, type: Int): Message? {
        val group = groupId?.let { messageLedger.getGroup(it) }
        
        groupId?.let { gid ->
            if (!messageLedger.isMember(gid, repository.getDeviceId())) {
                Log.w(tag, "Connection Denied: Source not a member of $gid")
                return null
            }
        }

        val payload = Message(
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
            anchoredPublicGroupId = group?.anchoredPublicGroupId
        )

        if (!payload.isPriority && receiverId == null && activeConnections.size > 5) {
            groupId?.let { gid ->
                aggregateBuffer.getOrPut(gid) { mutableListOf() }.add(payload)
                return payload
            }
        }

        return dispatchMessage(payload)
    }

    private suspend fun dispatchMessage(payload: Message): Message? {
        val bytes = Json.encodeToString(Message.serializer(), payload).toByteArray()
        val targetRid = payload.receiverId
        try {
            if (targetRid != null) { 
                sessionManager.encryptForSession(targetRid, bytes)?.let { encrypted ->
                    queueMessage(targetRid, Payload.fromBytes(encrypted)) 
                } 
            } else { 
                val targets = if (activeConnections.size > 20) {
                    activeConnections.shuffled().take(10) 
                } else {
                    activeConnections
                }
                
                targets.forEach { target -> 
                    internalScope.launch(ioDispatcher) { 
                        sessionManager.encryptForSession(target, bytes)?.let { encrypted ->
                            try { queueMessage(target, Payload.fromBytes(encrypted)) } catch (_: Exception) {} 
                        } 
                    } 
                } 
            }
            synchronized(messageIdHistory) {
                messageIdHistory.add(payload.messageId)
                if (messageIdHistory.size > 500) messageIdHistory.removeAt(0)
            }
            messageLedger.upsertMessage(payload)
            return payload
        } catch (_: Exception) {
            return null
        }
    }

    override suspend fun broadcastMessage(content: String, messageScope: Int, messageId: String?, groupId: String?, groupName: String?, type: Int): Message? = 
        sendMessage(content, null, messageScope, messageId, groupId, groupName, type)

    override suspend fun broadcastIdentityUpdate(oldName: String): Message {
        val payload = Message(messageId = UUID.randomUUID().toString(), senderId = repository.getDeviceId(), senderName = repository.getCurrentNickname(), senderEmoji = repository.emojiAvatar.value, content = oldName, timestamp = System.currentTimeMillis(), type = Message.TYPE_IDENTITY_UPDATE, messageScope = Message.MESSAGE_SHOUT)
        val bytes = Json.encodeToString(Message.serializer(), payload).toByteArray()
        activeConnections.forEach { target -> 
            internalScope.launch(ioDispatcher) { 
                sessionManager.encryptForSession(target, bytes)?.let { encrypted ->
                    try { queueMessage(target, Payload.fromBytes(encrypted)) } catch (_: Exception) {} 
                } 
            } 
        }
        return payload
    }

    override suspend fun sendFile(fileUri: Uri, receiverId: String?, messageScope: Int, groupId: String?, groupName: String?): Message? {
        val fileName = getFileName(fileUri)
        val fileSize = getFileSize(fileUri)
        val mimeType = context.contentResolver.getType(fileUri)
        val messageId = UUID.randomUUID().toString()
        return try {
            val pfd = context.contentResolver.openFileDescriptor(fileUri, "r") ?: return null
            val filePayload = Payload.fromFile(pfd)
            val payload = Message(messageId = messageId, senderId = repository.getDeviceId(), senderName = repository.getCurrentNickname(), senderEmoji = repository.emojiAvatar.value, receiverId = receiverId, groupId = groupId, groupName = groupName, content = if (mimeType?.startsWith("image/") == true) fileUri.toString() else "[FILE] $fileName", timestamp = System.currentTimeMillis(), type = if (mimeType?.startsWith("image/") == true) Message.TYPE_IMAGE else Message.TYPE_FILE, messageScope = messageScope, fileId = filePayload.id, fileName = fileName, fileSize = fileSize, mimeType = mimeType)
            internalScope.launch(ioDispatcher) {
                if (receiverId != null) { sendMessageInternal(receiverId, payload); connectionsClient.sendPayload(receiverId, filePayload) }
                else { activeConnections.forEach { target -> internalScope.launch { sendMessageInternal(target, payload); connectionsClient.sendPayload(target, filePayload) } } }
            }
            messageLedger.upsertMessage(payload)
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

    override suspend fun sendGroupMessage(content: String, groupId: String): Message? {
        val group = messageLedger.getGroup(groupId) ?: return null
        val payload = Message(messageId = UUID.randomUUID().toString(), senderId = repository.getDeviceId(), senderName = repository.getCurrentNickname(), senderEmoji = repository.emojiAvatar.value, groupId = groupId, content = content, timestamp = System.currentTimeMillis(), messageScope = Message.MESSAGE_WHISPER)
        val bytes = Json.encodeToString(Message.serializer(), payload).toByteArray()
        return try {
            group.allMemberIds.forEach { memberId -> 
                internalScope.launch(ioDispatcher) { 
                    sessionManager.encryptForSession(memberId, bytes)?.let { encrypted ->
                        try { queueMessage(memberId, Payload.fromBytes(encrypted)) } catch (_: Exception) {} 
                    } 
                } 
            }
            activeConnections.filter { it !in group.allMemberIds }.forEach { target -> 
                internalScope.launch(ioDispatcher) { 
                    sessionManager.encryptForSession(target, bytes)?.let { encrypted ->
                        try { queueMessage(target, Payload.fromBytes(encrypted)) } catch (_: Exception) {} 
                    } 
                } 
            }
            synchronized(messageIdHistory) { messageIdHistory.add(payload.messageId); if (messageIdHistory.size > 500) messageIdHistory.removeAt(0) }
            messageLedger.upsertMessage(payload); messageLedger.updateGroupLastMessage(groupId, payload.timestamp)
            payload
        } catch (_: Exception) { null }
    }

    override suspend fun sendNoteUpdate(groupId: String, content: String, messageId: String?, version: Int): Message? {
        val group = messageLedger.getGroup(groupId) ?: return null
        val payload = Message(
            messageId = messageId ?: UUID.randomUUID().toString(),
            senderId = repository.getDeviceId(),
            senderName = repository.getCurrentNickname(),
            senderEmoji = repository.emojiAvatar.value,
            groupId = groupId,
            content = content,
            timestamp = System.currentTimeMillis(),
            type = Message.TYPE_NOTE_UPDATE,
            noteVersion = version,
            messageScope = Message.MESSAGE_WHISPER,
        )
        val bytes = Json.encodeToString(Message.serializer(), payload).toByteArray()
        return try {
            group.allMemberIds.forEach { memberId -> 
                internalScope.launch(ioDispatcher) { 
                    sessionManager.encryptForSession(memberId, bytes)?.let { encrypted ->
                        try { queueMessage(memberId, Payload.fromBytes(encrypted)) } catch (_: Exception) {} 
                    } 
                } 
            }
            activeConnections.filter { it !in group.allMemberIds }.forEach { target -> 
                internalScope.launch(ioDispatcher) { 
                    sessionManager.encryptForSession(target, bytes)?.let { encrypted ->
                        try { queueMessage(target, Payload.fromBytes(encrypted)) } catch (_: Exception) {} 
                    } 
                } 
            }
            synchronized(messageIdHistory) { messageIdHistory.add(payload.messageId); if (messageIdHistory.size > 500) messageIdHistory.removeAt(0) }
            messageLedger.upsertMessage(payload); messageLedger.updateGroupLastMessage(groupId, payload.timestamp)
            payload
        } catch (_: Exception) { null }
    }

    override fun startGroupRoom(name: String, members: Set<String>, type: Int, groupId: String?, parentId: String?, anchoredPublicGroupId: String?): String {
        val gid = groupId ?: Group.generateId(name, type)
        internalScope.launch(ioDispatcher) { 
            messageLedger.insertGroup(
                Group(
                    id = gid,
                    name = name,
                    memberIds = members + repository.getDeviceId(),
                    scope = type,
                    parentId = parentId,
                    ownerId = repository.getDeviceId(),
                    anchoredPublicGroupId = anchoredPublicGroupId
                )
            ) 
            
            // If this is an anchored group, advertise it to the public group
            if (anchoredPublicGroupId != null) {
                broadcastAnchorAdvertisement(gid, name, anchoredPublicGroupId)
            }
        }
        return gid
    }

    private suspend fun broadcastAnchorAdvertisement(anchoredGid: String, name: String, anchorPublicGid: String) {
        val advertisement = Message(
            messageId = UUID.randomUUID().toString(),
            senderId = repository.getDeviceId(),
            senderName = repository.getCurrentNickname(),
            groupId = anchoredGid,
            groupName = name,
            content = "PRIVATE GROUP AVAILABLE",
            timestamp = System.currentTimeMillis(),
            type = Message.TYPE_ANCHOR_ADVERTISEMENT,
            messageScope = Message.MESSAGE_SHOUT,
            anchoredPublicGroupId = anchorPublicGid
        )
        dispatchMessage(advertisement)
    }

    override fun updateGroupMembers(groupId: String, memberIds: Set<String>) {
        internalScope.launch(ioDispatcher) { messageLedger.updateGroupMembers(groupId, memberIds) }
    }

    override fun updateGroupScope(groupId: String, scope: Int) {
        internalScope.launch(ioDispatcher) { messageLedger.updateGroupScope(groupId, scope) }
    }

    override fun initiateHistorySync(endpointId: String, sinceTimestamp: Long?) {
        internalScope.launch(ioDispatcher) {
            val key = getMessageKeyWithRetry(endpointId) ?: return@launch
            val request = Message(
                messageId = UUID.randomUUID().toString(),
                senderId = repository.getDeviceId(),
                senderName = "SYNC_REQUEST",
                content = sinceTimestamp?.toString() ?: "0",
                timestamp = System.currentTimeMillis(),
                type = Message.TYPE_RESYNC_REQUEST
            )
            sendMessageInternal(endpointId, request, key)
        }
    }

    private fun sendMessageInternal(endpointId: String, payload: Message, key: SecretKey) {
        try {
            val bytes = Json.encodeToString(Message.serializer(), payload).toByteArray()
            queueMessage(endpointId, Payload.fromBytes(cryptoManager.encrypt(bytes, key)))
        } catch (_: Exception) {
            Log.e(tag, "Failed to send Message to $endpointId")
        }
    }

    override fun closeConnection() {
        activeConnections.forEach { connectionsClient.disconnectFromEndpoint(it) }
        activeConnections.clear()
        sessionManager.clearAll()
        _isConnected.value = false
        _connectedGroups.value = emptySet()
    }

    override fun release() {
        stopDiscovery()
        stopAdvertising()
        closeConnection()
        internalScope.cancel()
    }

    private fun handleReceivedBytes(endpointId: String, bytes: ByteArray) {
        if (HandshakeProtocol.isHandshake(bytes)) {
            handleHandshake(endpointId, bytes)
        } else {
            sessionManager.decryptFromSession(endpointId, bytes)?.let { decrypted ->
                try {
                    val payload = Json.decodeFromString<Message>(decrypted.decodeToString())
                    sessionManager.getSessionKey(endpointId)?.let { key ->
                        handleIncomingPayload(endpointId, payload, key)
                    }
                } catch (_: Exception) {
                    Log.e(tag, "Parsing failure")
                }
            } ?: Log.e(tag, "Decryption failure")
        }
    }

    private fun handleHandshake(endpointId: String, bytes: ByteArray) {
        val peerPublicKeyBytes = HandshakeProtocol.parseHandshake(bytes) ?: return
        val secretKey = sessionManager.establishSession(endpointId, peerPublicKeyBytes)
        if (secretKey != null) {
            Log.i(tag, "SECURE: Connection established with $endpointId")
            syncMessageHistory(endpointId)
            backupRecentRecords(endpointId)
        } else {
            Log.e(tag, "Handshake failed")
        }
    }

    private fun handleIncomingPayload(endpointId: String, payload: Message, secretKey: SecretKey) {
        when (payload.type) {
            Message.TYPE_ACK -> handleAck(payload)
            Message.TYPE_IDENTITY_UPDATE -> handleIdentityUpdate(endpointId, payload, secretKey)
            Message.TYPE_GROUP_REQUEST -> handleGroupRequest(endpointId, payload)
            Message.TYPE_GROUP_ACCEPT -> handleGroupAccept(endpointId)
            Message.TYPE_RESYNC_REQUEST -> handleSyncRequest(endpointId, payload, secretKey)
            Message.TYPE_RESYNC_CHUNK -> handleSyncChunk(payload)
            Message.TYPE_RESYNC_COMPLETE -> handleSyncComplete(endpointId, payload)
            Message.TYPE_ANCHOR_ADVERTISEMENT -> handleAnchorAdvertisement(payload)
            else -> handleMessage(endpointId, payload, secretKey)
        }
    }

    private fun handleAck(payload: Message) {
        internalScope.launch(ioDispatcher) {
            messageLedger.updateMessageStatus(payload.messageId, Message.STATUS_DELIVERED)
        }
    }

    private fun handleIdentityUpdate(endpointId: String, payload: Message, secretKey: SecretKey) {
        _scannedSources.update { current -> current.map { if ((it.persistentId == payload.senderId) || (it.id == endpointId)) it.copy(name = payload.senderName, emoji = payload.senderEmoji ?: it.emoji) else it } }
        saveIncomingMessage(payload)
        sendAck(endpointId, payload.messageId, payload.senderId, secretKey)
        relayMessage(endpointId, payload)
    }

    private fun handleGroupRequest(endpointId: String, payload: Message) {
        val source = _scannedSources.value.find { it.id == endpointId } ?: Source(id = endpointId, name = payload.senderName, emoji = payload.senderEmoji ?: "👤")
        _incomingRadioRequests.update { it + source }
        hapticManager.triggerMessage(HapticManager.MessageType.CONNECTION)
    }

    private fun handleGroupAccept(endpointId: String) {
        pendingRadioRequests.remove(endpointId)
        _outgoingRadioRequests.update { current ->
            current.asSequence().filter { it.id != endpointId }.toSet()
        }
        _connectedGroups.update { it + endpointId }
        _isConnected.value = true
        updateScannedSources()
    }

    private fun handleSyncRequest(endpointId: String, payload: Message, secretKey: SecretKey) {
        val sinceId = payload.content
        val allLocalMessages = messageLedger.messages.value
        val sinceTimestamp = if (sinceId == "0") 0L else {
            allLocalMessages.find { it.messageId == sinceId }?.timestamp ?: 0L
        }
        
        internalScope.launch(ioDispatcher) {
            val historyToSync = messageLedger.getRawMessagesSince(sinceTimestamp)
            _syncProgress.value = 0.1f
            historyToSync.chunked(5).forEachIndexed { index, chunk ->
                val chunkPayload = Message(
                    messageId = UUID.randomUUID().toString(),
                    senderId = repository.getDeviceId(),
                    senderName = "SYNC_CHUNK",
                    content = Json.encodeToString(chunk.map { it.decodeToString() }),
                    timestamp = System.currentTimeMillis(),
                    type = Message.TYPE_RESYNC_CHUNK,
                )
                sendMessageInternal(endpointId, chunkPayload, secretKey)
                _syncProgress.value = 0.1f + ((index.toFloat() / (historyToSync.size / 5f + 1)) * 0.8f)
            }
            
            val complete = Message(
                messageId = UUID.randomUUID().toString(),
                senderId = repository.getDeviceId(),
                senderName = "SYNC",
                content = "COMPLETE",
                timestamp = System.currentTimeMillis(),
                type = Message.TYPE_RESYNC_COMPLETE,
            )
            sendMessageInternal(endpointId, complete, secretKey)
            _syncProgress.value = 1.0f
            delay(1.seconds)
            _syncProgress.value = null
        }
    }

    private fun handleSyncChunk(payload: Message) {
        try {
            val encryptedMessages = Json.decodeFromString<List<String>>(payload.content)
            encryptedMessages.forEach { encryptedStr ->
                try {
                    val encryptedBytes = encryptedStr.toByteArray()
                    val decrypted = cryptoManager.decryptLocal(encryptedBytes)
                    val message = Json.decodeFromString<Message>(decrypted.decodeToString())
                    if (isNewMessage(message.messageId)) {
                        messageLedger.upsertMessage(message)
                    }
                } catch (_: Exception) {}
            }
        } catch (_: Exception) {
            Log.e(tag, "Failed to parse sync chunk")
        }
    }

    private fun handleSyncComplete(endpointId: String, payload: Message) {
        _syncProgress.value = 1.0f
        internalScope.launch {
            delay(1.seconds)
            _syncProgress.value = null
        }
    }

    private fun handleAnchorAdvertisement(payload: Message) {
        val anchoredGid = payload.groupId ?: return
        val anchorPublicGid = payload.anchoredPublicGroupId ?: return
        val gName = payload.groupName ?: "ANCHORED GROUP"
        
        internalScope.launch(ioDispatcher) {
            if (messageLedger.getGroup(anchoredGid) == null) {
                messageLedger.insertGroup(
                    Group(
                        id = anchoredGid,
                        name = gName,
                        scope = Group.SCOPE_PRIVATE,
                        anchoredPublicGroupId = anchorPublicGid,
                        isMeta = true
                    )
                )
                Log.i(tag, "Assistant Discovery: Anchored group $gName found in $anchorPublicGid")
            }
        }
    }

    private fun handleMessage(endpointId: String, payload: Message, secretKey: SecretKey) {
        if (isSpam(payload)) return
        sendAck(endpointId, payload.messageId, payload.senderId, secretKey)
        if (payload.receiverId.isNullOrEmpty()) relayMessage(endpointId, payload)
        saveIncomingMessage(payload)
        
        val gid = payload.groupId
        val gName = payload.groupName
        if ((gid != null) && (gName != null)) {
            internalScope.launch(ioDispatcher) {
                val existing = messageLedger.getGroup(gid)
                if (existing == null) {
                    val scope = when (payload.messageScope) {
                        Message.MESSAGE_SHOUT -> Group.SCOPE_PUBLIC
                        Message.MESSAGE_SILENCE -> Group.SCOPE_LOCAL
                        else -> Group.SCOPE_PRIVATE
                    }
                    messageLedger.insertGroup(
                        Group(
                            id = gid,
                            name = gName,
                            scope = scope,
                            parentId = Group.ID_GLOBAL,
                            anchoredPublicGroupId = payload.anchoredPublicGroupId
                        )
                    )
                }
            }
        }
        hapticManager.triggerMessage(HapticManager.MessageType.MESSAGE)
    }

    private fun saveIncomingMessage(payload: Message) {
        if (isNewMessage(payload.messageId)) {
            messageLedger.upsertMessage(payload)
        } else {
            messageLedger.incrementAnchoredCount(payload.messageId)
        }
    }

    private fun sendAck(endpointId: String, messageId: String, receiverId: String, secretKey: SecretKey) {
        val ack = Message(
            messageId = messageId,
            senderId = repository.getDeviceId(),
            senderName = "ACK",
            content = "",
            timestamp = System.currentTimeMillis(),
            type = Message.TYPE_ACK,
            receiverId = receiverId
        )
        sendMessageInternal(endpointId, ack, secretKey)
    }

    private fun relayMessage(sourceEndpointId: String, payload: Message) {
        if (payload.hopCount >= 3) return
        
        internalScope.launch(ioDispatcher) {
            val myId = repository.getDeviceId()
            if (payload.senderId == myId) return@launch
            
            val relayedMessage = payload.copy(hopCount = payload.hopCount + 1)
            val json = Json.encodeToString(Message.serializer(), relayedMessage)
            val bytes = json.encodeToByteArray()
            
            activeConnections.forEach { endpointId ->
                if (endpointId != sourceEndpointId) {
                    sessionManager.encryptForSession(endpointId, bytes)?.let { encrypted ->
                        try {
                            queueMessage(endpointId, Payload.fromBytes(encrypted))
                        } catch (_: Exception) {
                            Log.e(tag, "Relay fail to $endpointId")
                        }
                    }
                }
            }
        }
    }

    private fun isNewMessage(messageId: String): Boolean = synchronized(messageIdHistory) {
        if (messageIdHistory.contains(messageId)) false else {
            messageIdHistory.add(messageId)
            if (messageIdHistory.size > 500) messageIdHistory.removeAt(0)
            true
        }
    }

    private fun isSpam(payload: Message): Boolean {
        val now = System.currentTimeMillis()
        val recentFromSender = messageLedger.messages.value.count { it.senderId == payload.senderId && (now - it.timestamp) < 10000 }
        return recentFromSender > 5
    }

    private fun sendHandshake(endpointId: String) {
        val handshakePayload = HandshakeProtocol.createHandshake(cryptoManager.getLocalKeyPair())
        connectionsClient.sendPayload(endpointId, Payload.fromBytes(handshakePayload))
    }

    private fun queueMessage(endpointId: String, payload: Payload) {
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

    private fun finalizeFileMessage(payloadId: Long, partial: Message) {
        messageLedger.upsertMessage(partial.copy(messageId = "file_$payloadId", content = "FILE_RECEIVED"))
    }
}
