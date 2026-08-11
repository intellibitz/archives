package cc.thevar.blukit.network.p2p

import android.content.Context
import cc.thevar.blukit.R
import cc.thevar.blukit.data.repository.IdentityRepository
import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.domain.model.MessagePayload
import cc.thevar.blukit.domain.model.ConnectionStatus
import cc.thevar.blukit.data.local.dao.MessageDao
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
import kotlinx.serialization.json.Json
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.crypto.SecretKey

/**
 * Supreme Senior Android Expert Implementation:
 * P2P Controller using Google Nearby Connections with Hardware-Backed Security Handshake.
 */
class NearbyP2PController(
    private val context: Context,
    private val repository: IdentityRepository,
    private val messageDao: MessageDao,
    private val hapticManager: HapticManager
) : P2PController {

    private val connectionsClient = Nearby.getConnectionsClient(context)
    private val strategy = Strategy.P2P_CLUSTER
    private val serviceId = "cc.thevar.blukit.P2P"

    private val internalScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _scannedDevices = MutableStateFlow<List<P2PDevice>>(emptyList())
    override val scannedDevices = _scannedDevices.asStateFlow()

    private val _isConnected = MutableStateFlow(value = false)
    override val isConnected = _isConnected.asStateFlow()

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

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            connectionsClient.acceptConnection(endpointId, payloadCallback)
                .addOnSuccessListener {
                    sendHandshake(endpointId)
                }
                .addOnFailureListener { e ->
                    emitError("Handshake failed: ${e.message}")
                }
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            if (result.status.isSuccess) {
                activeConnections.add(endpointId)
                _isConnected.value = true
            } else {
                emitError(context.getString(R.string.error_connection_failed, result.status.statusMessage ?: "Unknown"))
            }
        }

        override fun onDisconnected(endpointId: String) {
            activeConnections.remove(endpointId)
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

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {}
    }

    private fun isHandshakePayload(bytes: ByteArray): Boolean = bytes.isNotEmpty() && bytes[0] == 0x01.toByte()

    private fun sendHandshake(endpointId: String) {
        val publicKeyBytes = cryptoManager.getLocalKeyPair().public.encoded
        val handshakePayload = byteArrayOf(0x01.toByte()) + publicKeyBytes
        connectionsClient.sendPayload(endpointId, Payload.fromBytes(handshakePayload))
    }

    private fun handleHandshake(endpointId: String, bytes: ByteArray) {
        try {
            val publicKeyEncoded = bytes.copyOfRange(1, bytes.size)
            val keyFactory = KeyFactory.getInstance("EC")
            val peerPublicKey = keyFactory.generatePublic(X509EncodedKeySpec(publicKeyEncoded))
            val sharedSecret = cryptoManager.deriveSharedSecret(peerPublicKey)
            peerKeys[endpointId] = sharedSecret
        } catch (e: Exception) {
            emitError("Security handshake failed")
        }
    }

    private fun handleMessage(endpointId: String, bytes: ByteArray) {
        val secretKey = peerKeys[endpointId] ?: return
        try {
            val decryptedBytes = cryptoManager.decrypt(bytes, secretKey)
            val payloadJson = decryptedBytes.decodeToString()
            val messagePayload = Json.decodeFromString<MessagePayload>(payloadJson)
            
            internalScope.launch(Dispatchers.IO) {
                val blocked = repository.blockedUsers.first()
                if (messagePayload.senderId !in blocked) {
                    messageDao.insertMessage(messagePayload.toMessageEntity(isFromLocalUser = false))
                    hapticManager.triggerMessageAlert()
                }
            }
        } catch (e: Exception) {
            // Decryption failure or blocked user
        }
    }

    override fun startDiscovery() {
        val options = DiscoveryOptions.Builder().setStrategy(strategy).build()
        _isDiscovering.value = true
        connectionsClient.startDiscovery(
            serviceId,
            object : EndpointDiscoveryCallback() {
                override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
                    _scannedDevices.update { devices ->
                        val newDevice = P2PDevice(id = endpointId, name = info.endpointName, signalStrength = 0)
                        if (devices.any { it.id == endpointId }) devices else devices + newDevice
                    }
                }

                override fun onEndpointLost(endpointId: String) {
                    _scannedDevices.update { devices -> devices.filter { it.id != endpointId } }
                }
            },
            options
        ).addOnFailureListener { 
            _isDiscovering.value = false
            emitError("Discovery failure: ${it.message}") 
        }
    }

    override fun stopDiscovery() {
        connectionsClient.stopDiscovery()
        _isDiscovering.value = false
        _scannedDevices.value = emptyList()
    }

    override fun startAdvertising() {
        val options = AdvertisingOptions.Builder().setStrategy(strategy).build()
        internalScope.launch(Dispatchers.IO) {
            val nickname = repository.getNickname() ?: context.getString(R.string.anonymous)
            connectionsClient.startAdvertising(nickname, serviceId, connectionLifecycleCallback, options)
                .addOnFailureListener { emitError("Advertising failure: ${it.message}") }
        }
    }

    override fun stopAdvertising() {
        connectionsClient.stopAdvertising()
    }

    override fun connectToDevice(device: P2PDevice): SharedFlow<ConnectionStatus> {
        internalScope.launch(Dispatchers.IO) {
            val nickname = repository.getNickname() ?: context.getString(R.string.anonymous)
            connectionsClient.requestConnection(nickname, device.id, connectionLifecycleCallback)
                .addOnFailureListener { emitError("Connection request failure: ${it.message}") }
        }
        return MutableSharedFlow() 
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
            emitError("Message send failed")
            return null
        }
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
