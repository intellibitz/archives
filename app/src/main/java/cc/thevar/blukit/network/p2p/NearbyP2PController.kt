package cc.thevar.blukit.network.p2p

import android.content.Context
import android.util.Log
import cc.thevar.blukit.R
import cc.thevar.blukit.data.repository.IdentityRepository
import cc.thevar.blukit.data.repository.ContactRepository
import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.domain.model.MessagePayload
import cc.thevar.blukit.domain.model.ConnectionStatus
import cc.thevar.blukit.data.local.dao.MessageDao
import cc.thevar.blukit.data.local.dao.PeerDao
import cc.thevar.blukit.data.local.entities.ContactEntity
import cc.thevar.blukit.data.local.entities.PeerEntity
import cc.thevar.blukit.data.local.entities.toBluetoothPayload
import cc.thevar.blukit.data.local.entities.toMessageEntity
import cc.thevar.blukit.data.crypto.CryptoManager
import cc.thevar.blukit.data.system.HapticManager
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.crypto.SecretKey
import kotlin.time.Duration.Companion.seconds

/**
 * Supreme Senior Android Expert Implementation:
 * Hardened P2P Controller Enforcing Blukit Commandments.
 * Focuses on Bluetooth-first discovery and silent optional radio failures.
 */
class NearbyP2PController(
    private val context: Context,
    private val repository: IdentityRepository,
    private val contactRepository: ContactRepository,
    private val messageDao: MessageDao,
    private val peerDao: PeerDao,
    private val hapticManager: HapticManager,
    private val cryptoManager: CryptoManager = CryptoManager(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : P2PController {

    private val TAG = "BlukitP2P"
    private val connectionsClient = Nearby.getConnectionsClient(context)
    // P2P_STAR: Bluetooth LE for discovery + WiFi Direct/USB for data (more reliable than P2P_CLUSTER on Android 14+)
    private val strategy = Strategy.P2P_STAR
    private val serviceId = "cc.thevar.blukit.P2P"

    private val internalScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _scannedDevices = MutableStateFlow<List<P2PDevice>>(emptyList())
    override val scannedDevices = _scannedDevices.asStateFlow()

    private val _isConnected = MutableStateFlow(value = false)
    override val isConnected = _isConnected.asStateFlow()

    private val _connectedPeers = MutableStateFlow<Set<String>>(emptySet())
    override val connectedPeers = _connectedPeers.asStateFlow()

    private val _isDiscovering = MutableStateFlow(value = false)
    override val isDiscovering = _isDiscovering.asStateFlow()

    private val _errors = MutableSharedFlow<String>()
    override val errors = _errors.asSharedFlow()

    override val messages: StateFlow<List<MessagePayload>> = messageDao.getAllMessages()
        .map { entities -> entities.map { it.toBluetoothPayload() } }
        .stateIn(
            scope = internalScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val activeConnections = Collections.synchronizedSet(mutableSetOf<String>())
    private val peerKeys = Collections.synchronizedMap(mutableMapOf<String, SecretKey>())
    private val messageIdHistory = Collections.synchronizedList(LinkedList<String>())

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            // Accept all incoming connections automatically (mesh relay)
            connectionsClient.acceptConnection(endpointId, payloadCallback)
                .addOnSuccessListener {
                    Log.d(TAG, "Accepted connection from $endpointId (${info.endpointName})")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Connection acceptance failed for $endpointId: ${e.message}")
                }
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            if (result.status.isSuccess) {
                activeConnections.add(endpointId)
                _connectedPeers.update { it + endpointId }
                _isConnected.value = true
                Log.d(TAG, "Connected successfully to $endpointId")
                
                // Secure the channel immediately
                sendHandshake(endpointId)
                
                val device = _scannedDevices.value.find { it.id == endpointId }
                device?.let { 
                    internalScope.launch(ioDispatcher) {
                        contactRepository.saveContact(
                            ContactEntity(
                                contactId = it.id,
                                name = it.name ?: context.getString(R.string.discovery_unknown_device),
                                bluetoothAddress = "nearby://$endpointId",
                                lastSeen = System.currentTimeMillis(),
                                avatarUri = it.emoji ?: "👤"
                            )
                        )
                    }
                }
                // Power 2: Sync autonomous mesh history upon connection
                syncMeshHistory(endpointId)
            } else {
                Log.w(TAG, "Connection failed for $endpointId: ${result.status.statusMessage}")
            }
        }

        override fun onDisconnected(endpointId: String) {
            activeConnections.remove(endpointId)
            _connectedPeers.update { it - endpointId }
            peerKeys.remove(endpointId)
            if (activeConnections.isEmpty()) {
                _isConnected.value = false
            }
            Log.d(TAG, "Disconnected from $endpointId")
        }
    }

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            payload.asBytes()?.let { bytes ->
                if (isHandshakePayload(bytes)) {
                    handleHandshake(endpointId, bytes)
                } else {
                    handleMessage(endpointId, bytes)
                }
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {}
    }

    private fun isHandshakePayload(bytes: ByteArray): Boolean = bytes.isNotEmpty() && bytes[0] == 0x01.toByte()

    private fun sendHandshake(endpointId: String) {
        val publicKeyBytes = cryptoManager.getLocalKeyPair().public.encoded
        val handshakePayload = byteArrayOf(0x01.toByte()) + publicKeyBytes
        connectionsClient.sendPayload(endpointId, Payload.fromBytes(handshakePayload))
            .addOnFailureListener { e -> Log.e(TAG, "Failed to send handshake to $endpointId", e) }
    }

    private fun handleHandshake(endpointId: String, bytes: ByteArray) {
        try {
            val publicKeyEncoded = bytes.copyOfRange(1, bytes.size)
            val keyFactory = KeyFactory.getInstance("EC")
            val peerPublicKey = keyFactory.generatePublic(X509EncodedKeySpec(publicKeyEncoded))
            val sharedSecret = cryptoManager.deriveSharedSecret(peerPublicKey)
            peerKeys[endpointId] = sharedSecret
            
            internalScope.launch(ioDispatcher) {
                peerDao.insertPeer(
                    PeerEntity(
                        endpointId = endpointId,
                        name = null,
                        publicKey = Base64.getEncoder().encodeToString(publicKeyEncoded),
                        lastSeen = System.currentTimeMillis()
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Handshake failed", e)
        }
    }

    private fun handleMessage(endpointId: String, bytes: ByteArray) {
        val secretKey = peerKeys[endpointId] ?: run {
            Log.w(TAG, "Received message from $endpointId but no secret key found")
            return
        }
        try {
            val decryptedBytes = cryptoManager.decrypt(bytes, secretKey)
            val payloadJson = decryptedBytes.decodeToString()
            val messagePayload = Json.decodeFromString<MessagePayload>(payloadJson)
            
            if (messagePayload.type == MessagePayload.TYPE_ACK) {
                internalScope.launch(ioDispatcher) {
                    messageDao.updateMessageStatus(messagePayload.messageId, MessagePayload.STATUS_DELIVERED)
                }
                return
            }

            if (!isNewMessage(messagePayload.messageId)) return

            // Send delivery ACK back to sender
            internalScope.launch(ioDispatcher) {
                val ack = MessagePayload(
                    messageId = messagePayload.messageId,
                    senderId = repository.getDeviceId(),
                    senderName = "",
                    content = "",
                    timestamp = System.currentTimeMillis(),
                    type = MessagePayload.TYPE_ACK,
                    receiverId = messagePayload.senderId
                )
                val ackJson = Json.encodeToString(MessagePayload.serializer(), ack)
                cryptoManager.encrypt(ackJson.toByteArray(), secretKey).let { encryptedAck ->
                    connectionsClient.sendPayload(endpointId, Payload.fromBytes(encryptedAck))
                }
            }

            // Mesh Forwarding: If broadcast, send to all other connected peers
            if (messagePayload.receiverId.isNullOrEmpty()) {
                internalScope.launch(ioDispatcher) {
                    val currentDeviceId = repository.getDeviceId()
                    // Don't forward our own messages if they somehow looped back
                    if (messagePayload.senderId != currentDeviceId) {
                        activeConnections.filter { it != endpointId }.forEach { targetEndpointId ->
                            peerKeys[targetEndpointId]?.let { key ->
                                try {
                                    val encrypted = cryptoManager.encrypt(decryptedBytes, key)
                                    connectionsClient.sendPayload(targetEndpointId, Payload.fromBytes(encrypted))
                                } catch (e: Exception) {
                                    Log.e(TAG, "Failed to forward mesh message to $targetEndpointId", e)
                                }
                            }
                        }
                    }
                }
            }

            internalScope.launch(ioDispatcher) {
                val blocked = repository.blockedUsers.first()
                if (messagePayload.senderId !in blocked) {
                    messageDao.insertMessage(messagePayload.toMessageEntity(isFromLocalUser = false))
                    hapticManager.triggerMessageAlert()
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Decryption failed or message invalid from $endpointId")
        }
    }

    override fun startDiscovery() {
        if (_isDiscovering.value) return
        stopDiscovery()
        _isDiscovering.value = true
        
        // Retry up to 3 times with 1s delay (handles Google Play Services init delay)
        var attempts = 0
        val maxAttempts = 3
        val delayMs = 1000L

        internalScope.launch(ioDispatcher) {
            while (attempts < maxAttempts && _isDiscovering.value) {
                attempts++
                try {
                    val options = DiscoveryOptions.Builder().setStrategy(strategy).build()
                    connectionsClient.startDiscovery(
                        serviceId,
                        object : EndpointDiscoveryCallback() {
                            override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
                                Log.i(TAG, "Endpoint found: $endpointId (${info.endpointName})")
                                _scannedDevices.update { devices ->
                                    val parts = info.endpointName.split("|", limit = 2)
                                    val emoji = if (parts.size == 2) parts[0] else "👤"
                                    val name = if (parts.size == 2) parts[1] else info.endpointName
                                    val newDevice = P2PDevice(id = endpointId, name = name, emoji = emoji, signalStrength = -50)
                                    if (devices.any { it.id == endpointId }) devices else devices + newDevice
                                }
                            }

                            override fun onEndpointLost(endpointId: String) {
                                _scannedDevices.update { devices -> devices.filter { it.id != endpointId } }
                            }
                        },
                        options
                    ).addOnSuccessListener {
                        Log.i(TAG, "Discovery started successfully")
                    }.addOnFailureListener { e ->
                        if (attempts < maxAttempts) {
                            Log.w(TAG, "Discovery attempt $attempts failed, retrying in ${delayMs}ms: ${e.message}")
                            internalScope.launch { kotlinx.coroutines.delay(delayMs); startDiscovery() }
                        } else {
                            _isDiscovering.value = false
                            handleNearbyError(e, "Discovery")
                        }
                    }
                } catch (ex: Exception) {
                    if (attempts < maxAttempts) {
                        Log.w(TAG, "Discovery exception $attempts: ${ex.message}")
                        internalScope.launch { kotlinx.coroutines.delay(delayMs); startDiscovery() }
                    } else {
                        _isDiscovering.value = false
                        handleNearbyError(ex, "Discovery")
                    }
                }
            }
        }
    }

    override fun stopDiscovery() {
        connectionsClient.stopDiscovery()
        _isDiscovering.value = false
        _scannedDevices.value = emptyList()
    }

    private var isAdvertising = false
    override fun startAdvertising() {
        if (isAdvertising) return
        stopAdvertising()
        
        // Retry up to 3 times with 1s delay
        var attempts = 0
        val maxAttempts = 3
        val delayMs = 1000L

        internalScope.launch(ioDispatcher) {
            while (!isAdvertising && attempts < maxAttempts) {
                attempts++
                try {
                    val options = AdvertisingOptions.Builder().setStrategy(strategy).build()
                    val nickname = repository.getNickname() ?: context.getString(R.string.anonymous)
                    val emoji = repository.emojiAvatar.first()
                    val endpointName = "$emoji|$nickname"
                    isAdvertising = true
                    
                    connectionsClient.startAdvertising(endpointName, serviceId, connectionLifecycleCallback, options)
                        .addOnSuccessListener {
                            Log.i(TAG, "Advertising started: $endpointName (attempt $attempts)")
                        }
                        .addOnFailureListener { e ->
                            isAdvertising = false
                            if (attempts < maxAttempts) {
                                Log.w(TAG, "Advertising attempt $attempts failed, retrying: ${e.message}")
                                internalScope.launch { kotlinx.coroutines.delay(delayMs); startAdvertising() }
                            } else {
                                handleNearbyError(e, "Advertising")
                            }
                        }
                } catch (ex: Exception) {
                    isAdvertising = false
                    if (attempts < maxAttempts) {
                        Log.w(TAG, "Advertising exception $attempts: ${ex.message}")
                        internalScope.launch { kotlinx.coroutines.delay(delayMs); startAdvertising() }
                    } else {
                        handleNearbyError(ex, "Advertising")
                    }
                }
            }
        }
    }

    override fun stopAdvertising() {
        connectionsClient.stopAdvertising()
        isAdvertising = false
    }

    override fun connectToDevice(device: P2PDevice): SharedFlow<ConnectionStatus> {
        val progress = MutableSharedFlow<ConnectionStatus>(replay = 1)
        internalScope.launch(ioDispatcher) {
            try {
                progress.emit(ConnectionStatus.Connecting)
                val nickname = repository.getNickname() ?: context.getString(R.string.anonymous)
                val emoji = repository.emojiAvatar.first()
                val localName = "$emoji|$nickname"
                
                withTimeout(15.seconds) {
                    connectionsClient.requestConnection(localName, device.id, connectionLifecycleCallback)
                        .addOnFailureListener { e ->
                            handleNearbyError(e, "ConnectionRequest")
                            progress.tryEmit(ConnectionStatus.Error(e.message ?: "Unknown"))
                        }
                }
            } catch (e: Exception) {
                progress.emit(ConnectionStatus.Error(e.message ?: "Unknown"))
            }
        }
        return progress.asSharedFlow()
    }

    private suspend fun getPeerKeyWithRetry(endpointId: String): SecretKey? {
        var attempts = 0
        while (peerKeys[endpointId] == null && attempts < 30) { // Wait up to 3 seconds
            delay(100)
            attempts++
        }
        return peerKeys[endpointId]
    }

    private fun syncMeshHistory(endpointId: String) {
        internalScope.launch(ioDispatcher) {
            val key = getPeerKeyWithRetry(endpointId) ?: run {
                Log.w(TAG, "Sync failed: No key for $endpointId after waiting")
                return@launch
            }
            
            val history = messageDao.getAllMessages().first()
                .filter { it.receiverId.isNullOrEmpty() }
                .takeLast(20) // Sync last 20 messages for mesh context

            history.forEach { entity ->
                try {
                    val payloadObj = entity.toBluetoothPayload()
                    val payloadJson = Json.encodeToString(MessagePayload.serializer(), payloadObj)
                    val encrypted = cryptoManager.encrypt(payloadJson.toByteArray(), key)
                    connectionsClient.sendPayload(endpointId, Payload.fromBytes(encrypted))
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to sync message ${entity.messageId} to $endpointId", e)
                }
            }
        }
    }

    override suspend fun sendMessage(content: String, receiverId: String?): MessagePayload? {
        val nickname = repository.getNickname() ?: context.getString(R.string.anonymous)
        val emoji = repository.emojiAvatar.first()
        val senderId = repository.getDeviceId()
        val payloadObj = MessagePayload(
            messageId = UUID.randomUUID().toString(),
            senderId = senderId,
            senderName = nickname,
            senderEmoji = emoji,
            receiverId = receiverId,
            content = content,
            timestamp = System.currentTimeMillis()
        )

        val payloadJson = Json.encodeToString(MessagePayload.serializer(), payloadObj)
        val dataBytes = payloadJson.toByteArray()

        try {
            if (receiverId != null) {
                val key = getPeerKeyWithRetry(receiverId) ?: return null
                val encrypted = cryptoManager.encrypt(dataBytes, key)
                connectionsClient.sendPayload(receiverId, Payload.fromBytes(encrypted))
                Log.d(TAG, "Sent private payload to $receiverId")
            } else {
                if (activeConnections.isEmpty()) {
                    Log.w(TAG, "No active connections to broadcast to")
                }
                activeConnections.forEach { endpointId ->
                    internalScope.launch(ioDispatcher) {
                        val key = getPeerKeyWithRetry(endpointId)
                        if (key != null) {
                            try {
                                val encrypted = cryptoManager.encrypt(dataBytes, key)
                                connectionsClient.sendPayload(endpointId, Payload.fromBytes(encrypted))
                                Log.d(TAG, "Sent broadcast payload to $endpointId")
                            } catch (e: Exception) {
                                Log.e(TAG, "Encryption/Send error for $endpointId", e)
                            }
                        } else {
                            Log.w(TAG, "Skipping broadcast to $endpointId: No key available")
                        }
                    }
                }
            }
            synchronized(messageIdHistory) {
                messageIdHistory.add(payloadObj.messageId)
                if (messageIdHistory.size > 100) messageIdHistory.removeAt(0)
            }
            
            messageDao.insertMessage(payloadObj.toMessageEntity(isFromLocalUser = true))
            return payloadObj
        } catch (e: Exception) {
            Log.e(TAG, "Send failed", e)
            return null
        }
    }

    override suspend fun broadcastMessage(content: String): MessagePayload? = sendMessage(content, null)

    private fun isNewMessage(messageId: String): Boolean {
        synchronized(messageIdHistory) {
            if (messageIdHistory.contains(messageId)) return false
            messageIdHistory.add(messageId)
            if (messageIdHistory.size > 100) messageIdHistory.removeAt(0)
            return true
        }
    }

    /**
     * Handles errors from Nearby Connections API by enforcing Blukit Commandments.
     * Silent for WiFi/Location related errors.
     */
    private fun handleNearbyError(e: Exception, context: String) {
        if (e is com.google.android.gms.common.api.ApiException) {
            when (e.statusCode) {
                8003, // STATUS_ALREADY_CONNECTED_TO_ENDPOINT
                8012, // STATUS_ENDPOINT_IO_ERROR (Collision/Race)
                8029, // MISSING_PERMISSION_NEARBY_WIFI_DEVICES
                8032, // MISSING_PERMISSION_ACCESS_WIFI_STATE
                8035, // WIFI_DISABLED
                8025, // LOCATION_DISABLED
                8030  // BLUETOOTH_DISABLED (Handled by UI warning)
                -> {
                    Log.i(TAG, "$context: Silent/Expected nearby error: ${e.statusCode}")
                    return 
                }
            }
        }
        Log.e(TAG, "$context failure", e)
        emitError(e.message ?: "Operation failed")
    }

    private fun emitError(msg: String) {
        internalScope.launch { _errors.emit(msg) }
    }

    override fun closeConnection() {
        connectionsClient.stopAllEndpoints()
        activeConnections.clear()
        peerKeys.clear()
        _isConnected.value = false
    }

    override fun release() {
        stopDiscovery()
        stopAdvertising()
        closeConnection()
    }
}
