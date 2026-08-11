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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.crypto.SecretKey
import kotlin.time.Duration.Companion.seconds

/**
 * Supreme Senior Android Expert Implementation:
 * Hardened P2P Controller with exhaustive logging and radio management alerts.
 */
class NearbyP2PController(
    private val context: Context,
    private val repository: IdentityRepository,
    private val contactRepository: ContactRepository,
    private val messageDao: MessageDao,
    private val peerDao: PeerDao,
    private val hapticManager: HapticManager
) : P2PController {

    private val TAG = "BlukitP2P"
    private val connectionsClient = Nearby.getConnectionsClient(context)
    private val strategy = Strategy.P2P_CLUSTER
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

    private val cryptoManager = CryptoManager()
    private val activeConnections = mutableSetOf<String>()
    private val peerKeys = mutableMapOf<String, SecretKey>()
    private val messageIdHistory = Collections.synchronizedList(LinkedList<String>())

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            Log.d(TAG, "Connection initiated with $endpointId (${info.endpointName})")
            connectionsClient.acceptConnection(endpointId, payloadCallback)
                .addOnSuccessListener {
                    Log.d(TAG, "Accepted connection from $endpointId")
                    sendHandshake(endpointId)
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to accept connection from $endpointId", e)
                    emitError("Handshake failed: ${e.message}")
                }
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            if (result.status.isSuccess) {
                Log.i(TAG, "Connection SUCCESS with $endpointId")
                activeConnections.add(endpointId)
                _connectedPeers.update { it + endpointId }
                _isConnected.value = true
                
                val device = _scannedDevices.value.find { it.id == endpointId }
                device?.let { 
                    internalScope.launch(Dispatchers.IO) {
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
            } else {
                Log.e(TAG, "Connection FAILED with $endpointId status: ${result.status.statusMessage}")
                emitError(context.getString(R.string.error_connection_failed, result.status.statusMessage ?: "Unknown"))
            }
        }

        override fun onDisconnected(endpointId: String) {
            Log.w(TAG, "Disconnected from $endpointId")
            activeConnections.remove(endpointId)
            _connectedPeers.update { it - endpointId }
            peerKeys.remove(endpointId)
            if (activeConnections.isEmpty()) {
                _isConnected.value = false
            }
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

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            if (update.status == PayloadTransferUpdate.Status.SUCCESS) {
                Log.d(TAG, "Payload transfer success from $endpointId")
            }
        }
    }

    private fun isHandshakePayload(bytes: ByteArray): Boolean = bytes.isNotEmpty() && bytes[0] == 0x01.toByte()

    private fun sendHandshake(endpointId: String) {
        Log.d(TAG, "Sending E2EE handshake to $endpointId")
        val publicKeyBytes = cryptoManager.getLocalKeyPair().public.encoded
        val handshakePayload = byteArrayOf(0x01.toByte()) + publicKeyBytes
        connectionsClient.sendPayload(endpointId, Payload.fromBytes(handshakePayload))
    }

    private fun handleHandshake(endpointId: String, bytes: ByteArray) {
        Log.d(TAG, "Received E2EE handshake from $endpointId")
        try {
            val publicKeyEncoded = bytes.copyOfRange(1, bytes.size)
            val keyFactory = KeyFactory.getInstance("EC")
            val peerPublicKey = keyFactory.generatePublic(X509EncodedKeySpec(publicKeyEncoded))
            val sharedSecret = cryptoManager.deriveSharedSecret(peerPublicKey)
            peerKeys[endpointId] = sharedSecret
            Log.i(TAG, "Secure session established with $endpointId")
            
            internalScope.launch(Dispatchers.IO) {
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
            Log.e(TAG, "Handshake processing failed for $endpointId", e)
            emitError("Security handshake failed")
        }
    }

    private fun handleMessage(endpointId: String, bytes: ByteArray) {
        val secretKey = peerKeys[endpointId] ?: return
        try {
            val decryptedBytes = cryptoManager.decrypt(bytes, secretKey)
            val payloadJson = decryptedBytes.decodeToString()
            val messagePayload = Json.decodeFromString<MessagePayload>(payloadJson)
            
            if (!isNewMessage(messagePayload.messageId)) return

            internalScope.launch(Dispatchers.IO) {
                val blocked = repository.blockedUsers.first()
                if (messagePayload.senderId !in blocked) {
                    messageDao.insertMessage(messagePayload.toMessageEntity(isFromLocalUser = false))
                    hapticManager.triggerMessageAlert()
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to decrypt message from $endpointId")
        }
    }

    override fun startDiscovery() {
        if (_isDiscovering.value) return
        stopDiscovery() // Ensure clean state before starting
        Log.d(TAG, "Starting Discovery with serviceId: $serviceId")
        val options = DiscoveryOptions.Builder().setStrategy(strategy).build()
        _isDiscovering.value = true
        connectionsClient.startDiscovery(
            serviceId,
            object : EndpointDiscoveryCallback() {
                override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
                    Log.i(TAG, "Endpoint FOUND: $endpointId (${info.endpointName})")
                    _scannedDevices.update { devices ->
                        val parts = info.endpointName.split("|", limit = 2)
                        val emoji = if (parts.size == 2) parts[0] else "👤"
                        val name = if (parts.size == 2) parts[1] else info.endpointName
                        val newDevice = P2PDevice(id = endpointId, name = name, emoji = emoji, signalStrength = -50)
                        if (devices.any { it.id == endpointId }) devices else devices + newDevice
                    }
                }

                override fun onEndpointLost(endpointId: String) {
                    Log.w(TAG, "Endpoint LOST: $endpointId")
                    _scannedDevices.update { devices -> devices.filter { it.id != endpointId } }
                }
            },
            options
        ).addOnSuccessListener {
            Log.d(TAG, "Discovery successfully started")
        }.addOnFailureListener { 
            _isDiscovering.value = false
            Log.e(TAG, "Discovery failed to start", it)
            emitError("Discovery failure: ${it.message}") 
        }
    }

    override fun stopDiscovery() {
        Log.d(TAG, "Stopping Discovery")
        connectionsClient.stopDiscovery()
        _isDiscovering.value = false
        _scannedDevices.value = emptyList()
    }

    private var isAdvertising = false
    override fun startAdvertising() {
        if (isAdvertising) return
        stopAdvertising() // Ensure clean state before starting
        Log.d(TAG, "Starting Advertising with serviceId: $serviceId")
        val options = AdvertisingOptions.Builder().setStrategy(strategy).build()
        internalScope.launch(Dispatchers.IO) {
            val nickname = repository.getNickname() ?: context.getString(R.string.anonymous)
            val emoji = repository.emojiAvatar.first()
            val endpointName = "$emoji|$nickname"
            isAdvertising = true
            connectionsClient.startAdvertising(endpointName, serviceId, connectionLifecycleCallback, options)
                .addOnSuccessListener { Log.d(TAG, "Advertising successfully started as $endpointName") }
                .addOnFailureListener { 
                    isAdvertising = false
                    Log.e(TAG, "Advertising failed to start", it)
                    emitError("Advertising failure: ${it.message}") 
                }
        }
    }

    override fun stopAdvertising() {
        Log.d(TAG, "Stopping Advertising")
        connectionsClient.stopAdvertising()
        isAdvertising = false
    }

    override fun connectToDevice(device: P2PDevice): SharedFlow<ConnectionStatus> {
        Log.d(TAG, "Initiating connection to ${device.id}")
        val progress = MutableSharedFlow<ConnectionStatus>(replay = 1)
        internalScope.launch(Dispatchers.IO) {
            try {
                progress.emit(ConnectionStatus.Connecting)
                val nickname = repository.getNickname() ?: context.getString(R.string.anonymous)
                val emoji = repository.emojiAvatar.first()
                val localName = "$emoji|$nickname"
                
                withTimeout(15.seconds) {
                    connectionsClient.requestConnection(localName, device.id, connectionLifecycleCallback)
                        .addOnSuccessListener { Log.d(TAG, "Connection request sent to ${device.id}") }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Connection request failed for ${device.id}", e)
                            emitError("Connection request failed: ${e.message}")
                            progress.tryEmit(ConnectionStatus.Error(e.message ?: "Unknown"))
                        }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Connection attempt error for ${device.id}", e)
                progress.emit(ConnectionStatus.Error(e.message ?: "Unknown"))
            }
        }
        return progress.asSharedFlow()
    }

    override suspend fun sendMessage(content: String, receiverId: String?): MessagePayload? {
        val nickname = repository.getNickname() ?: context.getString(R.string.anonymous)
        val senderId = repository.getDeviceId()
        val payloadObj = MessagePayload(
            messageId = UUID.randomUUID().toString(),
            senderId = senderId,
            senderName = nickname,
            receiverId = receiverId,
            content = content,
            timestamp = System.currentTimeMillis()
        )

        val payloadJson = Json.encodeToString(MessagePayload.serializer(), payloadObj)
        val dataBytes = payloadJson.toByteArray()

        try {
            if (receiverId != null) {
                val key = peerKeys[receiverId] ?: return null
                val encrypted = cryptoManager.encrypt(dataBytes, key)
                connectionsClient.sendPayload(receiverId, Payload.fromBytes(encrypted))
            } else {
                activeConnections.forEach { endpointId ->
                    peerKeys[endpointId]?.let { key ->
                        val encrypted = cryptoManager.encrypt(dataBytes, key)
                        connectionsClient.sendPayload(endpointId, Payload.fromBytes(encrypted))
                    }
                }
            }
            messageDao.insertMessage(payloadObj.toMessageEntity(isFromLocalUser = true))
            return payloadObj
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send message", e)
            emitError("Message transmission failed")
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

    private fun emitError(msg: String) {
        internalScope.launch { _errors.emit(msg) }
    }

    override fun closeConnection() {
        Log.d(TAG, "Closing all P2P connections")
        connectionsClient.stopAllEndpoints()
        activeConnections.clear()
        peerKeys.clear()
        _isConnected.value = false
    }

    override fun release() {
        Log.d(TAG, "Releasing P2P Controller")
        stopDiscovery()
        stopAdvertising()
        closeConnection()
    }
}
