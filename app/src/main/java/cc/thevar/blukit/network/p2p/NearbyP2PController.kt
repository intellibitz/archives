/**
 * BLUKIT NETWORK: NEARBY P2P CONTROLLER
 *
 * The primary mesh engine leveraging Google Nearby Connections.
 * Orchestrates high-speed P2P_CLUSTER networking with ECDH-encrypted pulses.
 * 
 * Features:
 * - 100% Offline: Zero cloud dependency.
 * - Pulse Aggregation: Batching low-priority pulses for radio efficiency.
 * - Mesh Relay: 3-hop ephemeral relaying for extended range.
 * - Intelligence Support: Carrier for decentralized Crowd AI and swarm votes.
 * - Differential Sync: Rapid history bridging using WiFi/BLE composite radios.
 */
package cc.thevar.blukit.network.p2p

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import cc.thevar.blukit.data.crypto.CryptoManager
import cc.thevar.blukit.data.local.PulseStore
import cc.thevar.blukit.data.repository.IdentityRepository
import cc.thevar.blukit.data.system.HapticManager
import cc.thevar.blukit.domain.model.ConnectionStatus
import cc.thevar.blukit.domain.model.MessagePayload
import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.domain.model.Resonance
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionOptions
import com.google.android.gms.nearby.connection.ConnectionResolution
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.json.Json
import java.io.File
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
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
 * Concrete implementation of P2PController using Google's Nearby Connections API.
 */
class NearbyP2PController(
    private val context: Context,
    private val repository: IdentityRepository,
    private val pulseStore: PulseStore,
    private val hapticManager: HapticManager,
    private val radioStateManager: cc.thevar.blukit.data.system.RadioStateManager,
    private val cryptoManager: CryptoManager = CryptoManager(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
) : P2PController {

    private val tag = "BlukitP2P"
    private val connectionsClient = Nearby.getConnectionsClient(context)
    private val serviceId = "BLUKIT_PULSE"

    /** Use P2P_CLUSTER for high-density crowd interaction. */
    private fun getStrategy() = Strategy.P2P_CLUSTER
    private val internalScope = CoroutineScope(SupervisorJob() + mainDispatcher)

    // --- State Flows ---
    private val _scannedDevices = MutableStateFlow<List<P2PDevice>>(emptyList())
    override val scannedDevices = _scannedDevices.asStateFlow()

    private val _isConnected = MutableStateFlow(value = false)
    override val isConnected = _isConnected.asStateFlow()

    private val _connectedTies = MutableStateFlow<Set<String>>(emptySet())
    override val connectedTies = _connectedTies.asStateFlow()

    private val _incomingRadioRequests = MutableStateFlow<Set<P2PDevice>>(emptySet())
    override val incomingRadioRequests = _incomingRadioRequests.asStateFlow()

    private val _outgoingRadioRequests = MutableStateFlow<Set<P2PDevice>>(emptySet())
    override val outgoingRadioRequests = _outgoingRadioRequests.asStateFlow()

    private val _isDiscovering = MutableStateFlow(value = false)
    override val isDiscovering = _isDiscovering.asStateFlow()

    private val _isAdvertising = MutableStateFlow(value = false)
    override val isAdvertising = _isAdvertising.asStateFlow()

    private val _errors = MutableStateFlow<P2PError?>(null)
    override val errors = _errors.asStateFlow()

    private val _discoveredCrowds = MutableSharedFlow<Resonance>(extraBufferCapacity = 5)
    override val discoveredCrowds = _discoveredCrowds.asSharedFlow()

    private val _connectionUpdates = MutableSharedFlow<Pair<String, ConnectionStatus>>(extraBufferCapacity = 10)
    private val _syncProgress = MutableStateFlow<Float?>(null)
    override val syncProgress = _syncProgress.asStateFlow()

    override val messages: StateFlow<List<MessagePayload>> = pulseStore.getAllMessages()

    // --- Concurrent Safety Containers ---
    private val activeConnections = Collections.synchronizedSet(mutableSetOf<String>())
    private val pulseKeys = ConcurrentHashMap<String, SecretKey>()
    private val messageIdHistory = Collections.synchronizedList(LinkedList<String>())
    private val outgoingQueues = ConcurrentHashMap<String, kotlinx.coroutines.channels.Channel<Payload>>()
    private val pendingRadioRequests = Collections.synchronizedSet(mutableSetOf<String>())
    
    private val incomingFiles = Collections.synchronizedMap(mutableMapOf<Long, MessagePayload>())
    private val senderRateLimits = Collections.synchronizedMap(mutableMapOf<String, MutableList<Long>>())
    private val blockedFingerprints = Collections.synchronizedSet(mutableSetOf<String>())

    // PERFORMANCE: Pulse Aggregator for bundling low-priority SHOUTs
    private val aggregateBuffer = ConcurrentHashMap<String, MutableList<MessagePayload>>()
    @Suppress("unused")
    private val aggregateJob = internalScope.launch {
        while (isActive) {
            delay(200.milliseconds)
            flushAggregateBuffer()
        }
    }

    init {
        observeIdentityChanges()
        observeRadioChanges()
    }

    /** Restarts advertising when hardware radio states shift. */
    private fun observeRadioChanges() {
        radioStateManager.radioStates
            .drop(1)
            .onEach {
                if (_isAdvertising.value) {
                    stopAdvertising()
                    startAdvertising()
                }
            }
            .launchIn(internalScope)
    }

    /** Encodes hardware and mesh metadata into the radio flag (endpoint name). */
    private fun getRadioFlag(): String {
        val states = radioStateManager.radioStates.value
        val lowPower = repository.lowPowerMode.value
        val pulseCount = messages.value.size
        val radioChar = when {
            states.isWifiEnabled -> "W"
            states.isBluetoothEnabled -> "B"
            else -> "L"
        }
        val powerChar = if (lowPower) { "P" } else "H"
        val meshSize = activeConnections.size
        return "$radioChar|$pulseCount|$powerChar|$meshSize"
    }

    /** Restarts advertising if the user changes their Persona. */
    private fun observeIdentityChanges() {
        internalScope.launch {
            combine(repository.nicknameFlow, repository.emojiAvatar) { n, e -> n to e }
                .drop(1)
                .collect {
                    if (_isAdvertising.value) {
                        stopAdvertising()
                        startAdvertising()
                    }
                }
        }
    }

    /** Dedicated queue processor for an endpoint to ensure sequential pulse delivery. */
    private fun processQueueForEndpoint(endpointId: String, queue: kotlinx.coroutines.channels.Channel<Payload>) {
        internalScope.launch(ioDispatcher) {
            try {
                for (payload in queue) {
                    try {
                        suspendCancellableCoroutine<Unit> { continuation ->
                            connectionsClient.sendPayload(endpointId, payload)
                                .addOnCompleteListener { _ ->
                                    if (continuation.isActive) continuation.resume(Unit)
                                }
                        }
                    } catch (e: Exception) {
                        Log.e(tag, "Failed to send payload to $endpointId: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e(tag, "Queue iterator failed for $endpointId: ${e.message}")
            }
        }
    }

    private fun queuePulse(endpointId: String, payload: Payload) {
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

    // --- Lifecycle Callbacks ---
    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            // Extract peer radio metadata from the endpoint name
            val parts = info.endpointName.split("|")
            if (parts.size >= 4) {
                val peerMedium = when (parts[3]) {
                    "W" -> P2PDevice.ConnectionMedium.WIFI
                    "B" -> P2PDevice.ConnectionMedium.BLUETOOTH
                    else -> P2PDevice.ConnectionMedium.LOCATION
                }
                val peerPulseCount = parts.getOrNull(4)?.toIntOrNull() ?: 0
                val peerIsLowPower = parts.getOrNull(5) == "P"
                
                _scannedDevices.update { current ->
                    current.map { 
                        if (it.id == endpointId) it.copy(medium = peerMedium, pulseCount = peerPulseCount, isLowPower = peerIsLowPower) 
                        else it 
                    }
                }
            }
            connectionsClient.acceptConnection(endpointId, payloadCallback)
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            if (result.status.isSuccess) {
                activeConnections.add(endpointId)
                _connectionUpdates.tryEmit(endpointId to ConnectionStatus.Connected)
                hapticManager.triggerPulse(HapticManager.PulseType.CONNECTION)
                // Start hardware handshake for key exchange
                sendHandshake(endpointId)
                // Bridge history gap
                syncPulseHistory(endpointId)
            } else {
                val errorMsg = result.status.statusMessage ?: "Radio Failed"
                Log.e(tag, "Connection failed for $endpointId: $errorMsg (Status Code: ${result.status.statusCode})")
                _errors.value = P2PError.ConnectionError(errorMsg)
                _connectionUpdates.tryEmit(endpointId to ConnectionStatus.Error(errorMsg))
            }
        }

        override fun onDisconnected(endpointId: String) {
            activeConnections.remove(endpointId)
            pendingRadioRequests.remove(endpointId)
            _outgoingRadioRequests.update { current ->
            current.asSequence().filter { it.id != endpointId }.toSet()
        }
            _connectedTies.update { it - endpointId }
            pulseKeys.remove(endpointId)
            if (activeConnections.isEmpty()) _isConnected.value = false
            updateScannedDevices()
        }
    }

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            when (payload.type) {
                Payload.Type.BYTES -> {
                    payload.asBytes()?.let { bytes ->
                        // Protocol Check: Handshake (0x01) or Encrypted Message
                        if (isHandshakePayload(bytes)) handleHandshake(endpointId, bytes) else handleMessage(endpointId, bytes)
                    }
                }
                Payload.Type.FILE -> {
                    Log.d(tag, "Incoming file: ${payload.id}")
                }
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            if (update.status == PayloadTransferUpdate.Status.SUCCESS) {
                val payloadId = update.payloadId
                Log.d(tag, "Payload SUCCESS: $payloadId")
                val metadata = incomingFiles[payloadId]
                if (metadata != null) finalizeFileMessage(payloadId, metadata)
            }
        }
    }

    private fun isHandshakePayload(bytes: ByteArray): Boolean = bytes.isNotEmpty() && (bytes[0] == 0x01.toByte())

    /** Initiates ECDH handshake by sharing the local public key. */
    private fun sendHandshake(endpointId: String) {
        val publicKeyBytes = cryptoManager.getLocalKeyPair().public.encoded
        val handshakePayload = byteArrayOf(0x01.toByte()) + publicKeyBytes
        queuePulse(endpointId, Payload.fromBytes(handshakePayload))
    }

    /** Derives a shared AES secret from the peer's public key. */
    private fun handleHandshake(endpointId: String, bytes: ByteArray) {
        try {
            val publicKeyEncoded = bytes.copyOfRange(1, bytes.size)
            val pulsePublicKey = KeyFactory.getInstance("EC").generatePublic(X509EncodedKeySpec(publicKeyEncoded))
            pulseKeys[endpointId] = cryptoManager.deriveSharedSecret(pulsePublicKey)
        } catch (e: Exception) {
            Log.e(tag, "Handshake handle failed: ${e.message}")
        }
    }

    /** Core message router: decrypts, parses, and handles all pulse types. */
    private fun handleMessage(endpointId: String, bytes: ByteArray) {
        val secretKey = pulseKeys[endpointId] ?: return
        try {
            val decryptedBytes = cryptoManager.decrypt(bytes, secretKey)
            val payload = Json.decodeFromString<MessagePayload>(decryptedBytes.decodeToString())
            when (payload.type) {
                MessagePayload.TYPE_ACK -> handleAck(payload)
                MessagePayload.TYPE_TIE_REQUEST -> handleRadioRequest(endpointId, payload)
                MessagePayload.TYPE_TIE_ACCEPT -> handleRadioAccept(endpointId)
                MessagePayload.TYPE_IDENTITY_UPDATE -> {
                    if (isNewMessage(payload.messageId)) handleIdentityUpdate(endpointId, payload, secretKey)
                }
                MessagePayload.TYPE_IMAGE, MessagePayload.TYPE_FILE -> {
                    if (isNewMessage(payload.messageId)) handleFileMetadata(endpointId, payload, secretKey)
                }
                MessagePayload.TYPE_RESYNC_REQUEST -> {
                    val since = payload.content.toLongOrNull()
                    handleResyncRequest(endpointId, secretKey, since)
                }
                MessagePayload.TYPE_RESYNC_CHUNK -> handleResyncChunk(payload)
                MessagePayload.TYPE_RESYNC_COMPLETE -> {
                    Log.d(tag, "RESYNC COMPLETE from $endpointId")
                    _syncProgress.value = 1.0f
                    internalScope.launch {
                        delay(1.seconds)
                        _syncProgress.value = null
                    }
                }
                else -> {
                    // PERFORMANCE: Handle aggregated pulses (batch payloads)
                    if (payload.messageId.startsWith("aggregate_")) {
                        try {
                            val batch = Json.decodeFromString<List<MessagePayload>>(payload.content)
                            batch.forEach { p -> if (isNewMessage(p.messageId)) handleChatMessage(endpointId, p, secretKey) }
                        } catch (e: Exception) {
                            Log.e(tag, "Failed to decode aggregate: ${e.message}")
                        }
                    } else {
                        if (isNewMessage(payload.messageId)) handleChatMessage(endpointId, payload, secretKey)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Decryption or parsing failure: ${e.message}")
        }
    }

    /** Orchestrates differential history sync over high-speed radio. */
    private fun handleResyncRequest(endpointId: String, secretKey: SecretKey, sinceTimestamp: Long? = null) {
        internalScope.launch(ioDispatcher) {
            _syncProgress.value = 0.05f
            // 1. Sync Resonances first to establish context
            val allGroups = pulseStore.groups.value
            val groups = if (sinceTimestamp != null) allGroups.filter { it.lastPulseTimestamp > sinceTimestamp } else allGroups
            
            groups.chunked(5).forEachIndexed { index, chunk ->
                val chunkPayload = MessagePayload(
                    messageId = UUID.randomUUID().toString(),
                    senderId = repository.getDeviceId(),
                    senderName = "SYNC",
                    content = Json.encodeToString(chunk),
                    timestamp = System.currentTimeMillis(),
                    type = MessagePayload.TYPE_RESYNC_CHUNK,
                    status = 1, // Tag for resonances
                )
                sendMessageInternal(endpointId, chunkPayload, secretKey)
                _syncProgress.value = 0.05f + ((index.toFloat() / ((groups.size / 5) + 1).toFloat()) * 0.2f)
            }

            // 2. Sync Messages (The Life Stream)
            val allHistory = pulseStore.messages.value
            val history = if (sinceTimestamp != null) allHistory.filter { it.timestamp > sinceTimestamp } else allHistory
            
            history.chunked(10).forEachIndexed { index, chunk ->
                val chunkPayload = MessagePayload(
                    messageId = UUID.randomUUID().toString(),
                    senderId = repository.getDeviceId(),
                    senderName = "SYNC",
                    content = Json.encodeToString(chunk),
                    timestamp = System.currentTimeMillis(),
                    type = MessagePayload.TYPE_RESYNC_CHUNK,
                    status = 0, // Tag for messages
                )
                sendMessageInternal(endpointId, chunkPayload, secretKey)
                _syncProgress.value = 0.25f + ((index.toFloat() / ((history.size / 10 + 1).toFloat())) * 0.7f)
            }
            val complete = MessagePayload(
                messageId = UUID.randomUUID().toString(),
                senderId = repository.getDeviceId(),
                senderName = "SYNC",
                content = "COMPLETE",
                timestamp = System.currentTimeMillis(),
                type = MessagePayload.TYPE_RESYNC_COMPLETE,
            )
            sendMessageInternal(endpointId, complete, secretKey)
            _syncProgress.value = 1.0f
            delay(1.seconds)
            _syncProgress.value = null
        }
    }

    private fun handleResyncChunk(payload: MessagePayload) {
        try {
            _syncProgress.value = 0.5f // Indeterminate for receiver for now
            if (payload.status == 1) {
                val groups = Json.decodeFromString<List<Resonance>>(payload.content)
                internalScope.launch(ioDispatcher) {
                    groups.forEach { pulseStore.insertGroup(it) }
                }
            } else {
                val chunk = Json.decodeFromString<List<MessagePayload>>(payload.content)
                internalScope.launch(ioDispatcher) {
                    chunk.forEach { pulseStore.upsertMessage(it) }
                }
            }
        } catch (e: Exception) { }
    }

    private fun sendMessageInternal(endpointId: String, payload: MessagePayload, secretKey: SecretKey) {
        val bytes = Json.encodeToString(MessagePayload.serializer(), payload).toByteArray()
        queuePulse(endpointId, Payload.fromBytes(cryptoManager.encrypt(bytes, secretKey)))
    }

    private fun handleFileMetadata(endpointId: String, payload: MessagePayload, secretKey: SecretKey) {
        val filePayloadId = payload.fileId ?: return
        incomingFiles[filePayloadId] = payload
        saveIncomingMessage(payload.copy(status = MessagePayload.STATUS_PENDING))
        sendAck(endpointId, payload.messageId, payload.senderId, secretKey)
    }

    private fun finalizeFileMessage(payloadId: Long, metadata: MessagePayload) {
        Log.d(tag, "Finalizing file message: $payloadId (Type: ${metadata.type})")
        // File URI and persistence logic here
    }

    private fun handleAck(payload: MessagePayload) {
        internalScope.launch(ioDispatcher) { pulseStore.updateMessageStatus(payload.messageId, MessagePayload.STATUS_DELIVERED) }
    }

    private fun handleRadioRequest(endpointId: String, payload: MessagePayload) {
        val device = _scannedDevices.value.find { it.id == endpointId }
            ?: P2PDevice(endpointId, payload.senderName.ifBlank { "?" }, payload.senderEmoji ?: "👤", persistentId = payload.senderId)
        _incomingRadioRequests.update { it + device }
    }

    private fun handleRadioAccept(endpointId: String) {
        pendingRadioRequests.remove(endpointId)
        _outgoingRadioRequests.update { current ->
            current.asSequence().filter { it.id != endpointId }.toSet()
        }
        _connectedTies.update { it + endpointId }
        _isConnected.value = true
        updateScannedDevices()
    }

    private fun handleIdentityUpdate(endpointId: String, payload: MessagePayload, secretKey: SecretKey) {
        _scannedDevices.update { current -> current.map { if (it.persistentId == payload.senderId || it.id == endpointId) it.copy(name = payload.senderName, emoji = payload.senderEmoji ?: it.emoji) else it } }
        saveIncomingMessage(payload)
        sendAck(endpointId, payload.messageId, payload.senderId, secretKey)
        relayMessage(endpointId, payload)
    }

    private fun handleChatMessage(endpointId: String, payload: MessagePayload, secretKey: SecretKey) {
        if (isSpam(payload)) return
        sendAck(endpointId, payload.messageId, payload.senderId, secretKey)
        if (payload.receiverId.isNullOrEmpty()) relayMessage(endpointId, payload)
        saveIncomingMessage(payload)
    }

    private fun sendAck(endpointId: String, messageId: String, receiverId: String, secretKey: SecretKey) {
        internalScope.launch(ioDispatcher) {
            val ack = MessagePayload(messageId = messageId, senderId = repository.getDeviceId(), senderName = "", content = "", timestamp = System.currentTimeMillis(), type = MessagePayload.TYPE_ACK, receiverId = receiverId)
            queuePulse(endpointId, Payload.fromBytes(cryptoManager.encrypt(Json.encodeToString(MessagePayload.serializer(), ack).toByteArray(), secretKey)))
        }
    }

    /** 
     * Propagates a broadcast pulse to other peers.
     * Ephemeral hops limited to 3 to prevent mesh saturation.
     */
    private fun relayMessage(sourceEndpointId: String, payload: MessagePayload) {
        if (payload.hopCount >= 3) return
        internalScope.launch(ioDispatcher) {
            val myId = repository.getDeviceId()
            if (payload.senderId == myId) return@launch
            val relayedPayload = payload.copy(hopCount = payload.hopCount + 1)
            val bytes = Json.encodeToString(MessagePayload.serializer(), relayedPayload).toByteArray()
            activeConnections.filter { it != sourceEndpointId }.forEach { target ->
                pulseKeys[target]?.let { key -> try { queuePulse(target, Payload.fromBytes(cryptoManager.encrypt(bytes, key))) } catch (e: Exception) { } }
            }
        }
    }

    private fun saveIncomingMessage(payload: MessagePayload) {
        internalScope.launch(ioDispatcher) {
            if (payload.senderId !in repository.blockedUsers.value) {
                // AUTO-DISCOVER RESONANCES: Create groups from pulses if unknown
                val gid = payload.groupId
                val gName = payload.groupName
                if (gid != null && gName != null) {
                    val existing = pulseStore.getGroup(gid)
                    if (existing == null) {
                        val scope = when (payload.pulseType) {
                            MessagePayload.PULSE_SHOUT -> Resonance.SCOPE_PUBLIC
                            MessagePayload.PULSE_SILENCE -> Resonance.SCOPE_LOCAL
                            else -> Resonance.SCOPE_PRIVATE
                        }
                        val newGroup = Resonance(
                            id = gid, 
                            name = gName, 
                            scope = scope,
                            parentId = Resonance.ID_CROWD // Anchor to root Crowd if parent is unknown
                        )
                        pulseStore.insertGroup(newGroup)
                        _discoveredCrowds.tryEmit(newGroup)
                    }
                }
                pulseStore.upsertMessage(payload)
                hapticManager.triggerPulse(HapticManager.PulseType.MESSAGE)
            }
        }
    }

    override fun startDiscovery() {
        if (_isDiscovering.value) return
        _isDiscovering.value = true
        internalScope.launch(ioDispatcher) {
            val options = DiscoveryOptions.Builder().setStrategy(getStrategy()).build()
            connectionsClient.startDiscovery(serviceId, createDiscoveryCallback(), options)
                .addOnFailureListener { e -> 
                    Log.e(tag, "Discovery failed to start: ${e.message}")
                    _errors.value = P2PError.DiscoveryError(e.message ?: "Failed to start discovery")
                    _isDiscovering.value = false 
                }
            // Auto-heal logic: refresh discovery if stuck in "still air"
            while (_isDiscovering.value) {
                val isBoosted = _scannedDevices.value.count { it.isConnected } >= 3
                val scanDelay = if (isBoosted) 10000L else 30000L
                delay(scanDelay.milliseconds)
                if (_isDiscovering.value && _scannedDevices.value.isEmpty()) {
                    connectionsClient.stopDiscovery()
                    connectionsClient.startDiscovery(serviceId, createDiscoveryCallback(), options)
                }
            }
        }
    }

    private fun createDiscoveryCallback() = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            val parts = info.endpointName.split("|")
            if (parts.size < 3) return
            val pulseDeviceId = parts[2]
            val myDeviceId = repository.getDeviceId()
            val peerMedium = if (parts.size >= 4) { when (parts[3]) { "W" -> P2PDevice.ConnectionMedium.WIFI; "B" -> P2PDevice.ConnectionMedium.BLUETOOTH; else -> P2PDevice.ConnectionMedium.LOCATION } } else P2PDevice.ConnectionMedium.LOCATION
            val peerPulseCount = parts.getOrNull(4)?.toIntOrNull() ?: 0
            val peerIsLowPower = parts.getOrNull(5) == "P"
            val newDevice = P2PDevice(id = endpointId, name = parts[1].ifBlank { "?" }, emoji = parts[0], persistentId = pulseDeviceId, medium = peerMedium, pulseCount = peerPulseCount, isLowPower = peerIsLowPower)
            _scannedDevices.update { current -> current.filter { d -> d.id != endpointId } + newDevice }
            // Deterministic connection: prevent race conditions where both devices request at once
            if (myDeviceId < pulseDeviceId && !activeConnections.contains(endpointId)) {
                val localName = "${repository.emojiAvatar.value}|${repository.getCurrentNickname()}|$myDeviceId|${getRadioFlag()}"
                val options = ConnectionOptions.Builder().build()
                connectionsClient.requestConnection(localName, endpointId, connectionLifecycleCallback, options)
            }
        }
        override fun onEndpointLost(endpointId: String) { _scannedDevices.update { current -> current.filter { d -> d.id != endpointId } } }
    }

    override fun stopDiscovery() {
        connectionsClient.stopDiscovery()
        _isDiscovering.value = false
        _scannedDevices.value = emptyList()
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
                        _errors.value = P2PError.AdvertisingError(e.message ?: "Failed to start advertising")
                    }
                
                // Stability: Periodic restart to refresh radio state or name updates
                delay(1.minutes) 
                if (_isAdvertising.value) connectionsClient.stopAdvertising()
            }
        }
    }

    override fun stopAdvertising() {
        connectionsClient.stopAdvertising()
        _isAdvertising.value = false
    }

    override fun connectToDevice(device: P2PDevice): SharedFlow<ConnectionStatus> {
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

    override fun requestRadio(device: P2PDevice) {
        pendingRadioRequests.add(device.id)
        _outgoingRadioRequests.update { it + device }
        updateScannedDevices()
        internalScope.launch(ioDispatcher) {
            sendMessagePayload(device.id, MessagePayload(messageId = UUID.randomUUID().toString(), senderId = repository.getDeviceId(), senderName = repository.getCurrentNickname(), senderEmoji = repository.emojiAvatar.value, content = "RADIO_REQUEST", timestamp = System.currentTimeMillis(), type = MessagePayload.TYPE_TIE_REQUEST))
        }
    }

    override fun acceptRadio(device: P2PDevice) {
        _incomingRadioRequests.update { it - device }
        pendingRadioRequests.remove(device.id)
        _connectedTies.update { it + device.id }
        _isConnected.value = true
        _outgoingRadioRequests.update { current ->
            current.asSequence().filter { it.id != device.id }.toSet()
        }
        updateScannedDevices()
        internalScope.launch(ioDispatcher) {
            sendMessagePayload(device.id, MessagePayload(messageId = UUID.randomUUID().toString(), senderId = repository.getDeviceId(), senderName = repository.getCurrentNickname(), senderEmoji = repository.emojiAvatar.value, content = "RADIO_ACCEPT", timestamp = System.currentTimeMillis(), type = MessagePayload.TYPE_TIE_ACCEPT))
        }
    }

    override fun denyRadio(device: P2PDevice) {
        pendingRadioRequests.remove(device.id)
        _incomingRadioRequests.update { it - device }
        _outgoingRadioRequests.update { current ->
            current.asSequence().filter { it.id != device.id }.toSet()
        }
        updateScannedDevices()
    }


    private fun sendMessagePayload(endpointId: String, payload: MessagePayload) {
        pulseKeys[endpointId]?.let { key -> queuePulse(endpointId, Payload.fromBytes(cryptoManager.encrypt(Json.encodeToString(MessagePayload.serializer(), payload).toByteArray(), key))) }
    }

    private suspend fun getPulseKeyWithRetry(id: String): SecretKey? {
        var a = 0; while (pulseKeys[id] == null && a < 30) { delay(100.milliseconds); a++ }; return pulseKeys[id]
    }

    private fun syncPulseHistory(endpointId: String) {
        internalScope.launch(ioDispatcher) {
            val key = getPulseKeyWithRetry(endpointId) ?: return@launch
            val allMessages = pulseStore.getAllMessages().value
            // Only bridge recent public history
            allMessages.filter { it.receiverId.isNullOrBlank() }.takeLast(10).forEach { payload ->
                try { queuePulse(endpointId, Payload.fromBytes(cryptoManager.encrypt(Json.encodeToString(MessagePayload.serializer(), payload).toByteArray(), key))) } catch (e: Exception) {}
            }
        }
    }

    override suspend fun sendMessage(content: String, receiverId: String?, pulseType: Int, messageId: String?, groupId: String?, groupName: String?, type: Int): MessagePayload? {
        val payload = MessagePayload(
            messageId = messageId ?: UUID.randomUUID().toString(), 
            senderId = repository.getDeviceId(), 
            senderName = repository.getCurrentNickname(), 
            senderEmoji = repository.emojiAvatar.value, 
            receiverId = receiverId, 
            groupId = groupId, 
            groupName = groupName, 
            content = content, 
            timestamp = System.currentTimeMillis(), 
            pulseType = pulseType,
            type = type,
            hopCount = 0
        )

        // PERFORMANCE: Batch processing with aggregation for non-priority crowd pulses
        if (!payload.isPriority && receiverId == null && activeConnections.size > 5) {
            groupId?.let { gid ->
                aggregateBuffer.getOrPut(gid) { mutableListOf() }.add(payload)
                return payload
            }
        }

        return dispatchPulse(payload)
    }

    /** Encrypts and transmits a payload to a target or via mesh relay. */
    private suspend fun dispatchPulse(payload: MessagePayload): MessagePayload? {
        val bytes = Json.encodeToString(MessagePayload.serializer(), payload).toByteArray()
        val targetRid = payload.receiverId
        try {
            if (targetRid != null) { 
                getPulseKeyWithRetry(targetRid)?.let { key -> 
                    queuePulse(targetRid, Payload.fromBytes(cryptoManager.encrypt(bytes, key))) 
                } 
            } else { 
                // SELECTIVE BROADCASTING: Prioritize anchor nodes in large crowds to prevent broadcast storms
                val targets = if (activeConnections.size > 20) {
                    activeConnections.shuffled().take(10) 
                } else {
                    activeConnections
                }
                
                targets.forEach { target -> 
                    internalScope.launch(ioDispatcher) { 
                        pulseKeys[target]?.let { key -> 
                            try { queuePulse(target, Payload.fromBytes(cryptoManager.encrypt(bytes, key))) } catch (e: Exception) {} 
                        } 
                    } 
                } 
            }
            synchronized(messageIdHistory) {
                messageIdHistory.add(payload.messageId)
                if (messageIdHistory.size > 500) messageIdHistory.removeAt(0)
            }
            pulseStore.upsertMessage(payload)
            return payload
        } catch (e: Exception) {
            return null
        }
    }

    /** Bundles multiple pending pulses into a single high-density radio transmission. */
    private fun flushAggregateBuffer() {
        val groupsToFlush = aggregateBuffer.keys().toList()
        groupsToFlush.forEach { gid ->
            val pulses = aggregateBuffer.remove(gid) ?: return@forEach
            if (pulses.isEmpty()) return@forEach
            
            internalScope.launch(ioDispatcher) {
                val bundle = MessagePayload(
                    messageId = "aggregate_${System.currentTimeMillis()}",
                    senderId = "CROWD_SYSTEM",
                    senderName = "AGGREGATOR",
                    content = Json.encodeToString(pulses),
                    timestamp = System.currentTimeMillis(),
                    groupId = gid,
                    type = MessagePayload.TYPE_TEXT, 
                    isPriority = false
                )
                dispatchPulse(bundle)
            }
        }
    }

    override suspend fun broadcastMessage(content: String, pulseType: Int, messageId: String?, groupId: String?, groupName: String?, type: Int): MessagePayload? = 
        sendMessage(content, null, pulseType, messageId, groupId, groupName, type)

    override suspend fun broadcastIdentityUpdate(oldName: String): MessagePayload {
        val payload = MessagePayload(messageId = UUID.randomUUID().toString(), senderId = repository.getDeviceId(), senderName = repository.getCurrentNickname(), senderEmoji = repository.emojiAvatar.value, content = oldName, timestamp = System.currentTimeMillis(), type = MessagePayload.TYPE_IDENTITY_UPDATE, pulseType = MessagePayload.PULSE_SHOUT)
        val bytes = Json.encodeToString(MessagePayload.serializer(), payload).toByteArray()
        activeConnections.forEach { target -> internalScope.launch(ioDispatcher) { pulseKeys[target]?.let { key -> try { queuePulse(target, Payload.fromBytes(cryptoManager.encrypt(bytes, key))) } catch (e: Exception) {} } } }
        return payload
    }

    override suspend fun sendFile(fileUri: Uri, receiverId: String?, pulseType: Int, groupId: String?, groupName: String?): MessagePayload? {
        val fileName = getFileName(fileUri)
        val fileSize = getFileSize(fileUri)
        val mimeType = context.contentResolver.getType(fileUri)
        val messageId = UUID.randomUUID().toString()
        try {
            val pfd = context.contentResolver.openFileDescriptor(fileUri, "r") ?: return null
            val filePayload = Payload.fromFile(pfd)
            val payload = MessagePayload(messageId = messageId, senderId = repository.getDeviceId(), senderName = repository.getCurrentNickname(), senderEmoji = repository.emojiAvatar.value, receiverId = receiverId, groupId = groupId, groupName = groupName, content = if (mimeType?.startsWith("image/") == true) fileUri.toString() else "[FILE] $fileName", timestamp = System.currentTimeMillis(), type = if (mimeType?.startsWith("image/") == true) MessagePayload.TYPE_IMAGE else MessagePayload.TYPE_FILE, pulseType = pulseType, fileId = filePayload.id, fileName = fileName, fileSize = fileSize, mimeType = mimeType)
            internalScope.launch(ioDispatcher) {
                if (receiverId != null) { sendMessagePayload(receiverId, payload); connectionsClient.sendPayload(receiverId, filePayload) }
                else { activeConnections.forEach { target -> internalScope.launch { sendMessagePayload(target, payload); connectionsClient.sendPayload(target, filePayload) } } }
            }
            pulseStore.upsertMessage(payload)
            return payload
        } catch (e: Exception) {
            return null
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

    override suspend fun sendGroupMessage(content: String, groupId: String): MessagePayload? {
        val group = pulseStore.getGroup(groupId) ?: return null
        val payload = MessagePayload(messageId = UUID.randomUUID().toString(), senderId = repository.getDeviceId(), senderName = repository.getCurrentNickname(), senderEmoji = repository.emojiAvatar.value, groupId = groupId, content = content, timestamp = System.currentTimeMillis(), pulseType = MessagePayload.PULSE_WHISPER)
        val bytes = Json.encodeToString(MessagePayload.serializer(), payload).toByteArray()
        try {
            group.allMemberIds.forEach { memberId -> internalScope.launch(ioDispatcher) { pulseKeys[memberId]?.let { key -> try { queuePulse(memberId, Payload.fromBytes(cryptoManager.encrypt(bytes, key))) } catch (e: Exception) {} } } }
            activeConnections.filter { it !in group.allMemberIds }.forEach { target -> internalScope.launch(ioDispatcher) { pulseKeys[target]?.let { key -> try { queuePulse(target, Payload.fromBytes(cryptoManager.encrypt(bytes, key))) } catch (e: Exception) {} } } }
            synchronized(messageIdHistory) { messageIdHistory.add(payload.messageId); if (messageIdHistory.size > 500) messageIdHistory.removeAt(0) }
            pulseStore.upsertMessage(payload); pulseStore.updateGroupLastPulse(groupId, payload.timestamp); return payload
        } catch (e: Exception) { return null }
    }

    override suspend fun sendNoteUpdate(groupId: String, content: String, messageId: String?, version: Int): MessagePayload? {
        val group = pulseStore.getGroup(groupId) ?: return null
        val payload = MessagePayload(
            messageId = messageId ?: UUID.randomUUID().toString(),
            senderId = repository.getDeviceId(),
            senderName = repository.getCurrentNickname(),
            senderEmoji = repository.emojiAvatar.value,
            groupId = groupId,
            content = content,
            timestamp = System.currentTimeMillis(),
            type = MessagePayload.TYPE_NOTE_UPDATE,
            noteVersion = version,
            pulseType = MessagePayload.PULSE_WHISPER
        )
        val bytes = Json.encodeToString(MessagePayload.serializer(), payload).toByteArray()
        try {
            group.allMemberIds.forEach { memberId -> internalScope.launch(ioDispatcher) { pulseKeys[memberId]?.let { key -> try { queuePulse(memberId, Payload.fromBytes(cryptoManager.encrypt(bytes, key))) } catch (e: Exception) {} } } }
            activeConnections.filter { it !in group.allMemberIds }.forEach { target -> internalScope.launch(ioDispatcher) { pulseKeys[target]?.let { key -> try { queuePulse(target, Payload.fromBytes(cryptoManager.encrypt(bytes, key))) } catch (e: Exception) {} } } }
            synchronized(messageIdHistory) { messageIdHistory.add(payload.messageId); if (messageIdHistory.size > 500) messageIdHistory.removeAt(0) }
            pulseStore.upsertMessage(payload); pulseStore.updateGroupLastPulse(groupId, payload.timestamp); return payload
        } catch (e: Exception) { return null }
    }

    override fun startGroupPulse(name: String, members: Set<String>, type: Int, groupId: String?, parentId: String?): String {
        val gid = groupId ?: Resonance.generateId(name, type)
        internalScope.launch(ioDispatcher) { 
            pulseStore.insertGroup(
                Resonance(
                    id = gid,
                    name = name,
                    memberIds = members + repository.getDeviceId(),
                    scope = type,
                    parentId = parentId,
                )
            ) 
        }
        return gid
    }

    override fun updateGroupMembers(groupId: String, memberIds: Set<String>) {
        internalScope.launch(ioDispatcher) { pulseStore.updateGroupMembers(groupId, memberIds) }
    }

    override fun updateGroupScope(groupId: String, scope: Int) {
        internalScope.launch(ioDispatcher) { pulseStore.updateGroupScope(groupId, scope) }
    }

    override fun initiateHistorySync(endpointId: String, sinceTimestamp: Long?) {
        internalScope.launch(ioDispatcher) {
            val key = getPulseKeyWithRetry(endpointId) ?: return@launch
            val request = MessagePayload(
                messageId = UUID.randomUUID().toString(),
                senderId = repository.getDeviceId(),
                senderName = "SYNC",
                content = sinceTimestamp?.toString() ?: "RESYNC_REQUEST",
                timestamp = System.currentTimeMillis(),
                type = MessagePayload.TYPE_RESYNC_REQUEST
            )
            sendMessageInternal(endpointId, request, key)
        }
    }

    /** Basic spam detection based on sender rate limits. */
    private fun isSpam(payload: MessagePayload): Boolean {
        if (payload.senderId in repository.blockedUsers.value || blockedFingerprints.contains(payload.senderId)) return true
        val now = System.currentTimeMillis()
        val timestamps = senderRateLimits.getOrPut(payload.senderId) { mutableListOf() }
        timestamps.removeAll { now - it > 10000 }
        if (timestamps.size > 5) { blockedFingerprints.add(payload.senderId); return true }
        timestamps.add(now)
        return false
    }

    private fun isNewMessage(id: String): Boolean = synchronized(messageIdHistory) { if (messageIdHistory.contains(id)) false else { messageIdHistory.add(id); if (messageIdHistory.size > 100) messageIdHistory.removeAt(0); true } }

    private fun updateScannedDevices() { _scannedDevices.update { current -> current.map { device -> val tied = device.id in _connectedTies.value; val connecting = device.id in pendingRadioRequests; device.copy(isConnected = tied, isTiePending = connecting, medium = if (tied) P2PDevice.ConnectionMedium.WIFI else if (connecting || activeConnections.contains(device.id)) P2PDevice.ConnectionMedium.BLUETOOTH else P2PDevice.ConnectionMedium.LOCATION) } } }

    override fun closeConnection() {
        connectionsClient.stopAllEndpoints()
        activeConnections.clear()
        pulseKeys.clear()
        _isConnected.value = false
    }

    override fun release() { 
        stopDiscovery()
        stopAdvertising()
        closeConnection()
        outgoingQueues.values.forEach { q -> q.close() }
        outgoingQueues.clear()
        internalScope.cancel() 
    }
}
