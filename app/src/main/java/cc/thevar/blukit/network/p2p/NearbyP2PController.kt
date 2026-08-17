package cc.thevar.blukit.network.p2p

import android.content.Context
import android.util.Log
import cc.thevar.blukit.R
import cc.thevar.blukit.data.crypto.CryptoManager
import cc.thevar.blukit.data.local.VibeStore
import cc.thevar.blukit.data.local.entities.ContactEntity
import cc.thevar.blukit.data.local.entities.PeerEntity
import cc.thevar.blukit.data.repository.ContactRepository
import cc.thevar.blukit.data.repository.IdentityRepository
import cc.thevar.blukit.data.system.HapticManager
import cc.thevar.blukit.domain.model.ConnectionStatus
import cc.thevar.blukit.domain.model.MessagePayload
import cc.thevar.blukit.domain.model.P2PDevice
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import android.os.Build
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.crypto.SecretKey
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Controller for the Peer-to-Peer (P2P) network within The Air using Google Nearby Connections.
 * Implements the P2P_CLUSTER strategy for high-reliability, Bluetooth-first connectivity.
 * Manages discovery, advertising, and secure relaying of vibes through The Air.
 */
class NearbyP2PController(
    private val context: Context,
    private val repository: IdentityRepository,
    private val contactRepository: ContactRepository,
    private val vibeStore: VibeStore,
    private val hapticManager: HapticManager,
    private val radioStateManager: cc.thevar.blukit.data.system.RadioStateManager,
    private val cryptoManager: CryptoManager = CryptoManager(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main
) : P2PController {

    private val tag = "BlukitP2P"
    private val connectionsClient = Nearby.getConnectionsClient(context)
    private val serviceId = "BLUKIT_VIBE"

    private fun getStrategy() = Strategy.P2P_CLUSTER

    private val internalScope = CoroutineScope(SupervisorJob() + mainDispatcher)

    private val hasRttSupport = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        context.packageManager.hasSystemFeature(android.content.pm.PackageManager.FEATURE_WIFI_RTT)
    } else false
    
    private val _scannedDevices = MutableStateFlow<List<P2PDevice>>(emptyList())
    override val scannedDevices = _scannedDevices.asStateFlow()

    private val _isConnected = MutableStateFlow(value = false)
    override val isConnected = _isConnected.asStateFlow()

    private val _connectedLinks = MutableStateFlow<Set<String>>(emptySet())
    override val connectedLinks = _connectedLinks.asStateFlow()

    private val _incomingLinkRequests = MutableStateFlow<Set<P2PDevice>>(emptySet())
    override val incomingLinkRequests = _incomingLinkRequests.asStateFlow()

    private val _isDiscovering = MutableStateFlow(value = false)
    override val isDiscovering = _isDiscovering.asStateFlow()

    private val _isAdvertising = MutableStateFlow(value = false)
    override val isAdvertising = _isAdvertising.asStateFlow()

    private val _errors = MutableStateFlow<P2PError?>(null)
    override val errors = _errors.asStateFlow()

    override val messages: StateFlow<List<MessagePayload>> = vibeStore.getAllMessages()
        .stateIn(
            scope = internalScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val activeConnections = Collections.synchronizedSet(mutableSetOf<String>())
    private val vibeKeys = Collections.synchronizedMap(mutableMapOf<String, SecretKey>())
    private val messageIdHistory = Collections.synchronizedList(LinkedList<String>())

    // Sequential Vibe Queue
    private val outgoingQueues = Collections.synchronizedMap(mutableMapOf<String, kotlinx.coroutines.channels.Channel<Payload>>())
    private val pendingLinkRequests = Collections.synchronizedSet(mutableSetOf<String>())

    init {
        observeIdentityChanges()
        observePowerChanges()
        observeRadioChanges()
    }

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

    private fun getRadioFlag(): String {
        val states = radioStateManager.radioStates.value
        val lowPower = repository.lowPowerMode.value
        val vibeCount = messages.value.size
        
        val radioChar = when {
            states.isWifiEnabled -> "W"
            states.isBluetoothEnabled -> "B"
            else -> "L"
        }
        val powerChar = if (lowPower) "P" else "H" // Power: Low (P) or High (H)
        
        // Format: FLAG|COUNT|POWER
        return "$radioChar|$vibeCount|$powerChar"
    }

    private fun observeIdentityChanges() {
        internalScope.launch {
            combine(repository.nicknameFlow, repository.emojiAvatar) { n, e -> n to e }
                .drop(1) // Avoid initial trigger
                .collect {
                    if (_isAdvertising.value) {
                        stopAdvertising()
                        startAdvertising()
                    }
                    // Discovery also carries the local name in some strategies, but primarily advertising.
                }
        }
    }

    private fun observePowerChanges() {
        internalScope.launch {
            repository.lowPowerMode
                .drop(1)
                .collect {
                    Log.i(tag, "POWER: Low Power Mode changed to $it. Adapting strategy.")
                    if (_isAdvertising.value) {
                        stopAdvertising()
                        startAdvertising()
                    }
                    if (_isDiscovering.value) {
                        stopDiscovery()
                        startDiscovery()
                    }
                }
        }
    }

    private fun processQueueForEndpoint(endpointId: String) {
        val queue = outgoingQueues.getOrPut(endpointId) {
            kotlinx.coroutines.channels.Channel(kotlinx.coroutines.channels.Channel.UNLIMITED)
        }
        
        internalScope.launch(ioDispatcher) {
            for (payload in queue) {
                try {
                    suspendCancellableCoroutine<Unit> { continuation ->
                        connectionsClient.sendPayload(endpointId, payload)
                            .addOnCompleteListener { continuation.resume(Unit) }
                    }
                } catch (e: Exception) {
                    Log.e(tag, "QUEUE FAIL ($endpointId): ${e.message}")
                    if (e is CancellationException) throw e
                }
            }
        }
    }

    private fun queueVibe(endpointId: String, payload: Payload) {
        val queue = outgoingQueues.getOrPut(endpointId) {
            processQueueForEndpoint(endpointId)
            outgoingQueues[endpointId]!!
        }
        queue.trySend(payload)
    }

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            Log.i(tag, "INIT: $endpointId (${info.endpointName})")
            
            // Extract peer state from connection info name if available
            val parts = info.endpointName.split("|")
            if (parts.size >= 4) {
                val peerMedium = when (parts[3]) {
                    "W" -> P2PDevice.ConnectionMedium.WIFI
                    "B" -> P2PDevice.ConnectionMedium.BLUETOOTH
                    else -> P2PDevice.ConnectionMedium.LOCATION
                }
                val peerVibeCount = parts.getOrNull(4)?.toIntOrNull() ?: 0
                val peerIsLowPower = parts.getOrNull(5) == "P"
                
                _scannedDevices.update { current ->
                    current.map { 
                        if (it.id == endpointId) it.copy(medium = peerMedium, vibeCount = peerVibeCount, isLowPower = peerIsLowPower) 
                        else it 
                    }
                }
            }
            
            connectionsClient.acceptConnection(endpointId, payloadCallback)
                .addOnSuccessListener { Log.i(tag, "ACCEPTED: $endpointId") }
                .addOnFailureListener { e -> Log.e(tag, "ACCEPT FAIL: ${e.message}") }
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            if (result.status.isSuccess) {
                Log.i(tag, "LINKED: $endpointId")
                activeConnections.add(endpointId)
                // Low-level linked, but not yet "Tied" by the user
                hapticManager.triggerVibe(HapticManager.VibeType.CONNECTION)
                sendHandshake(endpointId)
                syncAirHistory(endpointId)
            } else {
                Log.w(tag, "LINK FAIL $endpointId: ${result.status.statusMessage}")
                handleNearbyError(P2PError.ConnectionError(result.status.statusMessage ?: "Link Failed"))
            }
        }

        override fun onDisconnected(endpointId: String) {
            Log.i(tag, "UNLINKED: $endpointId")
            activeConnections.remove(endpointId)
            pendingLinkRequests.remove(endpointId)
            _connectedLinks.update { it - endpointId }
            vibeKeys.remove(endpointId)
            if (activeConnections.isEmpty()) _isConnected.value = false
            updateScannedDevices()
        }
    }

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            payload.asBytes()?.let { bytes ->
                if (isHandshakePayload(bytes)) handleHandshake(endpointId, bytes) else handleMessage(endpointId, bytes)
            }
        }
        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {}
    }

    private fun isHandshakePayload(bytes: ByteArray): Boolean = bytes.isNotEmpty() && (bytes[0] == 0x01.toByte())

    private fun sendHandshake(endpointId: String) {
        val publicKeyBytes = cryptoManager.getLocalKeyPair().public.encoded
        val handshakePayload = byteArrayOf(0x01.toByte()) + publicKeyBytes
        queueVibe(endpointId, Payload.fromBytes(handshakePayload))
    }

    private fun handleHandshake(endpointId: String, bytes: ByteArray) {
        try {
            val publicKeyEncoded = bytes.copyOfRange(1, bytes.size)
            val keyFactory = KeyFactory.getInstance("EC")
            val vibePublicKey = keyFactory.generatePublic(X509EncodedKeySpec(publicKeyEncoded))
            val sharedSecret = cryptoManager.deriveSharedSecret(vibePublicKey)
            vibeKeys[endpointId] = sharedSecret
            Log.i(tag, "SECURE: Channel ready for $endpointId")
        } catch (e: Exception) { Log.e(tag, "SECURE FAIL: ${e.message}") }
    }

    private fun handleMessage(endpointId: String, bytes: ByteArray) {
        val secretKey = vibeKeys[endpointId] ?: return
        try {
            val decryptedBytes = cryptoManager.decrypt(bytes, secretKey)
            val payload = Json.decodeFromString<MessagePayload>(decryptedBytes.decodeToString())

            when (payload.type) {
                MessagePayload.TYPE_ACK -> handleAck(payload)
                MessagePayload.TYPE_LINK_REQUEST -> handleLinkRequest(endpointId, payload)
                MessagePayload.TYPE_LINK_ACCEPT -> handleLinkAccept(endpointId)
                else -> {
                    if (isNewMessage(payload.messageId)) {
                        handleChatMessage(endpointId, payload, secretKey)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "MESSAGE DECRYPT/DECODE FAIL: ${e.message}")
            handleNearbyError(P2PError.EncryptionError("Failed to decrypt incoming vibe"))
        }
    }

    private fun handleAck(payload: MessagePayload) {
        internalScope.launch(ioDispatcher) {
            vibeStore.updateMessageStatus(payload.messageId, MessagePayload.STATUS_DELIVERED)
        }
    }

    private fun handleLinkRequest(endpointId: String, payload: MessagePayload) {
        val device = _scannedDevices.value.find { it.id == endpointId }
            ?: P2PDevice(endpointId, payload.senderName.ifBlank { "UNKNOWN" }, payload.senderEmoji ?: "👤")
        _incomingLinkRequests.update { it + device }
    }

    private fun handleLinkAccept(endpointId: String) {
        pendingLinkRequests.remove(endpointId)
        _connectedLinks.update { it + endpointId }
        _isConnected.value = true
        updateScannedDevices()
    }

    private fun handleChatMessage(
        endpointId: String,
        payload: MessagePayload,
        secretKey: SecretKey
    ) {
        // 1. Send ACK back immediately
        sendAck(endpointId, payload.messageId, payload.senderId, secretKey)

        // 2. Sentient Mesh Relay: Gossip protocol
        // If it's a broadcast vibe and we are in a healthy state, relay it.
        if (payload.receiverId.isNullOrEmpty()) {
            val senderDevice = _scannedDevices.value.find { it.id == endpointId }
            val isStrongVibe = (senderDevice?.signalStrength ?: -100) > -60
            
            // Heuristic: Strong vibes from the Inner Circle get prioritized relay
            if (isStrongVibe || payload.hopCount < 2) {
                relayMessage(endpointId, payload)
            }
        }

        // 3. Save to Local DB
        saveIncomingMessage(payload)
    }

    private fun sendAck(endpointId: String, messageId: String, receiverId: String, secretKey: SecretKey) {
        internalScope.launch(ioDispatcher) {
            val ack = MessagePayload(
                messageId = messageId,
                senderId = repository.getDeviceId(),
                senderName = "",
                content = "",
                timestamp = System.currentTimeMillis(),
                type = MessagePayload.TYPE_ACK,
                receiverId = receiverId
            )
            val json = Json.encodeToString(MessagePayload.serializer(), ack)
            val encAck = cryptoManager.encrypt(json.toByteArray(), secretKey)
            queueVibe(endpointId, Payload.fromBytes(encAck))
        }
    }

    private fun relayMessage(sourceEndpointId: String, payload: MessagePayload) {
        if (payload.hopCount >= 3) return // Max hops reached
        
        internalScope.launch(ioDispatcher) {
            val myId = repository.getDeviceId()
            if (payload.senderId == myId) return@launch // Don't relay our own messages

            val relayedPayload = payload.copy(hopCount = payload.hopCount + 1)
            val json = Json.encodeToString(MessagePayload.serializer(), relayedPayload)
            val bytes = json.encodeToByteArray()

            activeConnections.filter { it != sourceEndpointId }.forEach { target ->
                vibeKeys[target]?.let { key ->
                    try {
                        val reEncrypted = cryptoManager.encrypt(bytes, key)
                        queueVibe(target, Payload.fromBytes(reEncrypted))
                    } catch (e: Exception) {
                        Log.e(tag, "RELAY FAIL to $target: ${e.message}")
                    }
                }
            }
        }
    }

    private fun saveIncomingMessage(payload: MessagePayload) {
        internalScope.launch(ioDispatcher) {
            if (payload.senderId !in repository.blockedUsers.value) {
                vibeStore.insertMessage(payload)
                hapticManager.triggerVibe(HapticManager.VibeType.MESSAGE)
            }
        }
    }

    override fun startDiscovery() {
        Log.i(tag, "EXEC: startDiscovery() - Sentient Mesh Mode. RTT Support: $hasRttSupport")
        if (_isDiscovering.value) return
        _isDiscovering.value = true
        internalScope.launch(ioDispatcher) {
            val options = DiscoveryOptions.Builder()
                .setStrategy(getStrategy())
                .build()

            connectionsClient.startDiscovery(serviceId, createDiscoveryCallback(), options)
                .addOnSuccessListener { Log.i(tag, "DISCOVERY START SUCCESS") }
                .addOnFailureListener { e ->
                    _isDiscovering.value = false
                    Log.e(tag, "DISCOVERY FAIL: ${e.message}")
                    handleNearbyError(P2PError.DiscoveryError(e.message ?: "Discovery failed"))
                }
        }
    }

    private fun createDiscoveryCallback() = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            Log.i(tag, "VIBE FOUND: $endpointId (${info.endpointName}). Updating PEOPLE.")
            val parts = info.endpointName.split("|")
            if (parts.size < 3) return
            val vibeDeviceId = parts[2]
            val myDeviceId = repository.getDeviceId()

            // Sentient detection of peer state based on packed flags
            // Format: emoji|nickname|deviceId|RADIO|COUNT|POWER
            val peerMedium = if (parts.size >= 4) {
                when (parts[3]) {
                    "W" -> P2PDevice.ConnectionMedium.WIFI
                    "B" -> P2PDevice.ConnectionMedium.BLUETOOTH
                    else -> P2PDevice.ConnectionMedium.LOCATION
                }
            } else {
                P2PDevice.ConnectionMedium.LOCATION
            }
            
            val peerVibeCount = parts.getOrNull(4)?.toIntOrNull() ?: 0
            val peerIsLowPower = parts.getOrNull(5) == "P"

            val newDevice = P2PDevice(
                id = endpointId, 
                name = parts[1].ifBlank { "UNKNOWN" }, 
                emoji = parts[0],
                medium = peerMedium,
                vibeCount = peerVibeCount,
                isLowPower = peerIsLowPower
            )
            _scannedDevices.update { it.filter { d -> d.id != endpointId } + newDevice }

            if (myDeviceId < vibeDeviceId && !activeConnections.contains(endpointId)) {
                Log.i(tag, "THE AIR: Requesting $endpointId")
                val localName = "${repository.emojiAvatar.value}|${repository.getCurrentNickname()}|$myDeviceId|${getRadioFlag()}"
                connectionsClient.requestConnection(localName, endpointId, connectionLifecycleCallback)
                    .addOnFailureListener { e ->
                        Log.w(tag, "REQ FAIL: ${e.message}")
                        handleNearbyError(P2PError.ConnectionError("Failed to request connection to ${info.endpointName}"))
                    }
            }
        }

        override fun onEndpointLost(endpointId: String) {
            _scannedDevices.update { it.filter { d -> d.id != endpointId } }
        }
    }

    override fun stopDiscovery() {
        connectionsClient.stopDiscovery()
        _isDiscovering.value = false
        _scannedDevices.value = emptyList()
    }

    override fun startAdvertising() {
        Log.i(tag, "EXEC: startAdvertising() - Bluetooth Vibe")
        if (_isAdvertising.value) return
        _isAdvertising.value = true
        internalScope.launch(ioDispatcher) {
            val options = AdvertisingOptions.Builder()
                .setStrategy(getStrategy())
                .build()
            val name = "${repository.emojiAvatar.value}|${repository.getCurrentNickname()}|${repository.getDeviceId()}|${getRadioFlag()}"
            connectionsClient.startAdvertising(name, serviceId, connectionLifecycleCallback, options)
                .addOnSuccessListener { Log.i(tag, "ADVERTISING START SUCCESS") }
                .addOnFailureListener { e ->
                    _isAdvertising.value = false
                    Log.e(tag, "ADVERTISING FAIL: ${e.message}")
                    handleNearbyError(P2PError.AdvertisingError(e.message ?: "Advertising failed"))
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
            val name = "${repository.emojiAvatar.value}|${repository.getCurrentNickname()}|${repository.getDeviceId()}"
            connectionsClient.requestConnection(name, device.id, connectionLifecycleCallback)
                .addOnFailureListener { e -> flow.tryEmit(ConnectionStatus.Error(e.message ?: "Fail")) }
        }
        return flow.asSharedFlow()
    }

    override fun requestLink(device: P2PDevice) {
        pendingLinkRequests.add(device.id)
        updateScannedDevices()
        internalScope.launch(ioDispatcher) {
            val payload = MessagePayload(
                messageId = UUID.randomUUID().toString(),
                senderId = repository.getDeviceId(),
                senderName = repository.getCurrentNickname(),
                senderEmoji = repository.emojiAvatar.value,
                content = "LINK_REQUEST",
                timestamp = System.currentTimeMillis(),
                type = MessagePayload.TYPE_LINK_REQUEST
            )
            sendMessagePayload(device.id, payload)
        }
    }

    override fun acceptLink(device: P2PDevice) {
        _incomingLinkRequests.update { it - device }
        pendingLinkRequests.remove(device.id)
        _connectedLinks.update { it + device.id }
        _isConnected.value = true
        updateScannedDevices()
        internalScope.launch(ioDispatcher) {
            val payload = MessagePayload(
                messageId = UUID.randomUUID().toString(),
                senderId = repository.getDeviceId(),
                senderName = repository.getCurrentNickname(),
                senderEmoji = repository.emojiAvatar.value,
                content = "LINK_ACCEPT",
                timestamp = System.currentTimeMillis(),
                type = MessagePayload.TYPE_LINK_ACCEPT
            )
            sendMessagePayload(device.id, payload)
        }
    }

    override fun denyLink(device: P2PDevice) {
        pendingLinkRequests.remove(device.id)
        _incomingLinkRequests.update { it - device }
        updateScannedDevices()
    }

    private suspend fun sendMessagePayload(endpointId: String, payload: MessagePayload) {
        val secretKey = vibeKeys[endpointId] ?: return
        val json = Json.encodeToString(MessagePayload.serializer(), payload)
        queueVibe(endpointId, Payload.fromBytes(cryptoManager.encrypt(json.toByteArray(), secretKey)))
    }

    private suspend fun getVibeKeyWithRetry(id: String): SecretKey? {
        var a = 0
        while (vibeKeys[id] == null && a < 30) { delay(100); a++ }
        return vibeKeys[id]
    }

    private fun syncAirHistory(endpointId: String) {
        internalScope.launch(ioDispatcher) {
            val key = getVibeKeyWithRetry(endpointId) ?: return@launch
            vibeStore.getAllMessages().value.filter { it.receiverId.isNullOrBlank() }.takeLast(10).forEach { payload ->
                try {
                    val json = Json.encodeToString(MessagePayload.serializer(), payload)
                    queueVibe(endpointId, Payload.fromBytes(cryptoManager.encrypt(json.toByteArray(), key)))
                } catch (e: Exception) {}
            }
        }
    }

    override suspend fun sendMessage(content: String, receiverId: String?): MessagePayload? {
        val payload = MessagePayload(
            messageId = UUID.randomUUID().toString(),
            senderId = repository.getDeviceId(),
            senderName = repository.getCurrentNickname(),
            senderEmoji = repository.emojiAvatar.value,
            receiverId = receiverId, content = content, timestamp = System.currentTimeMillis()
        )
        val json = Json.encodeToString(MessagePayload.serializer(), payload)
        val bytes = json.toByteArray()

        try {
            if (receiverId != null) {
                val key = getVibeKeyWithRetry(receiverId) ?: return null
                queueVibe(receiverId, Payload.fromBytes(cryptoManager.encrypt(bytes, key)))
            } else {
                if (activeConnections.isEmpty()) Log.w(tag, "THE AIR IS STILL: Cannot Shout")
                activeConnections.forEach { target ->
                    internalScope.launch(ioDispatcher) {
                        vibeKeys[target]?.let { key ->
                            try { queueVibe(target, Payload.fromBytes(cryptoManager.encrypt(bytes, key))) } catch (e: Exception) {}
                        }
                    }
                }
            }
            synchronized(messageIdHistory) { messageIdHistory.add(payload.messageId); if (messageIdHistory.size > 100) messageIdHistory.removeAt(0) }
            vibeStore.insertMessage(payload)
            return payload
        } catch (e: Exception) { return null }
    }

    override suspend fun broadcastMessage(content: String): MessagePayload? = sendMessage(content, null)

    private fun isNewMessage(id: String): Boolean = synchronized(messageIdHistory) {
        if (messageIdHistory.contains(id)) false else {
            messageIdHistory.add(id)
            if (messageIdHistory.size > 100) messageIdHistory.removeAt(0)
            true
        }
    }

    private fun updateScannedDevices() {
        _scannedDevices.update { current ->
            current.map { device ->
                val tied = device.id in _connectedLinks.value
                val connecting = device.id in pendingLinkRequests
                
                // Determine medium based on connectivity state
                val medium = when {
                    tied -> P2PDevice.ConnectionMedium.WIFI
                    connecting || activeConnections.contains(device.id) -> P2PDevice.ConnectionMedium.BLUETOOTH
                    else -> P2PDevice.ConnectionMedium.LOCATION
                }
                
                device.copy(
                    isConnected = tied,
                    isLinkPending = connecting,
                    medium = medium
                )
            }
        }
    }

    private fun handleNearbyError(error: P2PError) {
        if (error is P2PError.ConnectionError && error.message.contains("8003")) return // Already connected
        internalScope.launch { _errors.emit(error) }
    }

    override fun closeConnection() {
        connectionsClient.stopAllEndpoints()
        activeConnections.clear()
        vibeKeys.clear()
        _isConnected.value = false
    }

    override fun release() {
        stopDiscovery()
        stopAdvertising()
        closeConnection()
        outgoingQueues.values.forEach { it.close() }
        outgoingQueues.clear()
        internalScope.cancel()
    }
}
