package cc.thevar.blukit.network.p2p

import android.content.Context
import android.util.Log
import cc.thevar.blukit.R
import cc.thevar.blukit.data.crypto.CryptoManager
import cc.thevar.blukit.data.local.dao.MessageDao
import cc.thevar.blukit.data.local.dao.PeerDao
import cc.thevar.blukit.data.local.entities.ContactEntity
import cc.thevar.blukit.data.local.entities.PeerEntity
import cc.thevar.blukit.data.local.entities.toBluetoothPayload
import cc.thevar.blukit.data.local.entities.toMessageEntity
import cc.thevar.blukit.data.repository.ContactRepository
import cc.thevar.blukit.data.repository.IdentityRepository
import cc.thevar.blukit.data.system.HapticManager
import cc.thevar.blukit.domain.model.ConnectionStatus
import cc.thevar.blukit.domain.model.MessagePayload
import cc.thevar.blukit.domain.model.P2PDevice
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
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
 * Supreme Senior Android Expert Implementation:
 * Hardened Mesh Controller.
 * Focused on high-reliability Bluetooth-first mesh connectivity.
 */
class NearbyP2PController(
    private val context: Context,
    private val repository: IdentityRepository,
    private val contactRepository: ContactRepository,
    private val messageDao: MessageDao,
    private val peerDao: PeerDao,
    private val hapticManager: HapticManager,
    private val cryptoManager: CryptoManager = CryptoManager(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : P2PController {

    private val tag = "BlukitP2P"
    private val connectionsClient = Nearby.getConnectionsClient(context)
    private val strategy = Strategy.P2P_CLUSTER
    private val serviceId = "cc.thevar.blukit.MESH_ID"

    private val internalScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _scannedDevices = MutableStateFlow<List<P2PDevice>>(emptyList())
    override val scannedDevices = _scannedDevices.asStateFlow()

    private val _isConnected = MutableStateFlow(value = false)
    override val isConnected = _isConnected.asStateFlow()

    private val _connectedPeers = MutableStateFlow<Set<String>>(emptySet())
    override val connectedPeers = _connectedPeers.asStateFlow()

    private val _isDiscovering = MutableStateFlow(value = false)
    override val isDiscovering = _isDiscovering.asStateFlow()

    private val _isAdvertising = MutableStateFlow(value = false)
    override val isAdvertising = _isAdvertising.asStateFlow()

    private val _errors = MutableStateFlow("")
    override val errors = _errors.asStateFlow()

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

    // Sequential Payload Queue
    private sealed class OutgoingPayload {
        data class Single(val endpointId: String, val payload: Payload) : OutgoingPayload()
    }
    private val outgoingQueue = kotlinx.coroutines.channels.Channel<OutgoingPayload>(kotlinx.coroutines.channels.Channel.UNLIMITED)

    init {
        processOutgoingQueue()
        observeIdentityChanges()
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

    private fun processOutgoingQueue() {
        internalScope.launch(ioDispatcher) {
            for (item in outgoingQueue) {
                try {
                    when (item) {
                        is OutgoingPayload.Single -> {
                            suspendCancellableCoroutine<Unit> { continuation ->
                                connectionsClient.sendPayload(item.endpointId, item.payload)
                                    .addOnCompleteListener { 
                                        continuation.resume(Unit)
                                    }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(tag, "QUEUE FAIL: ${e.message}")
                }
            }
        }
    }

    private fun queuePayload(endpointId: String, payload: Payload) {
        outgoingQueue.trySend(OutgoingPayload.Single(endpointId, payload))
    }

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            Log.i(tag, "INIT: $endpointId (${info.endpointName})")
            connectionsClient.acceptConnection(endpointId, payloadCallback)
                .addOnSuccessListener { Log.i(tag, "ACCEPTED: $endpointId") }
                .addOnFailureListener { e -> Log.e(tag, "ACCEPT FAIL: ${e.message}") }
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            if (result.status.isSuccess) {
                Log.i(tag, "LINKED: $endpointId")
                activeConnections.add(endpointId)
                _connectedPeers.update { it + endpointId }
                _isConnected.value = true
                hapticManager.triggerVibe(HapticManager.VibeType.CONNECTION)
                sendHandshake(endpointId)
                syncMeshHistory(endpointId)
            } else {
                Log.w(tag, "LINK FAIL $endpointId: ${result.status.statusMessage}")
                handleNearbyError(Exception(result.status.statusMessage), "LINK")
            }
        }

        override fun onDisconnected(endpointId: String) {
            Log.i(tag, "UNLINKED: $endpointId")
            activeConnections.remove(endpointId)
            _connectedPeers.update { it - endpointId }
            peerKeys.remove(endpointId)
            if (activeConnections.isEmpty()) _isConnected.value = false
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
        queuePayload(endpointId, Payload.fromBytes(handshakePayload))
    }

    private fun handleHandshake(endpointId: String, bytes: ByteArray) {
        try {
            val publicKeyEncoded = bytes.copyOfRange(1, bytes.size)
            val keyFactory = KeyFactory.getInstance("EC")
            val peerPublicKey = keyFactory.generatePublic(X509EncodedKeySpec(publicKeyEncoded))
            val sharedSecret = cryptoManager.deriveSharedSecret(peerPublicKey)
            peerKeys[endpointId] = sharedSecret
            Log.i(tag, "SECURE: Channel ready for $endpointId")
        } catch (e: Exception) { Log.e(tag, "SECURE FAIL: ${e.message}") }
    }

    private fun handleMessage(endpointId: String, bytes: ByteArray) {
        val secretKey = peerKeys[endpointId] ?: return
        try {
            val decryptedBytes = cryptoManager.decrypt(bytes, secretKey)
            val payload = Json.decodeFromString<MessagePayload>(decryptedBytes.decodeToString())
            
            if (payload.type == MessagePayload.TYPE_ACK) {
                internalScope.launch(ioDispatcher) { messageDao.updateMessageStatus(payload.messageId, MessagePayload.STATUS_DELIVERED) }
                return
            }

            if (!isNewMessage(payload.messageId)) return

            // ACK
            internalScope.launch(ioDispatcher) {
                val ack = MessagePayload(
                    messageId = payload.messageId,
                    senderId = repository.getDeviceId(),
                    senderName = "", content = "", timestamp = System.currentTimeMillis(),
                    type = MessagePayload.TYPE_ACK, receiverId = payload.senderId
                )
                val encAck = cryptoManager.encrypt(Json.encodeToString(MessagePayload.serializer(), ack).toByteArray(), secretKey)
                queuePayload(endpointId, Payload.fromBytes(encAck))
            }

            // Relay
            if (payload.receiverId.isNullOrEmpty()) {
                internalScope.launch(ioDispatcher) {
                    val myId = repository.getDeviceId()
                    if (payload.senderId != myId) {
                        activeConnections.filter { it != endpointId }.forEach { target ->
                            peerKeys[target]?.let { key ->
                                try { queuePayload(target, Payload.fromBytes(cryptoManager.encrypt(decryptedBytes, key))) } catch (e: Exception) {}
                            }
                        }
                    }
                }
            }

            internalScope.launch(ioDispatcher) {
                if (payload.senderId !in repository.blockedUsers.value) {
                    messageDao.insertMessage(payload.toMessageEntity(isFromLocalUser = false))
                    hapticManager.triggerVibe(HapticManager.VibeType.MESSAGE)
                }
            }
        } catch (e: Exception) {}
    }

    override fun startDiscovery() {
        Log.i(tag, "EXEC: startDiscovery() - Bluetooth Primary, Wi-Fi Optional")
        if (_isDiscovering.value) return
        _isDiscovering.value = true
        internalScope.launch(ioDispatcher) {
            // Hardened: Ensure strategy is Cluster for best Bluetooth mesh performance
            val options = DiscoveryOptions.Builder()
                .setStrategy(strategy)
                .build()
            
            connectionsClient.startDiscovery(serviceId, object : EndpointDiscoveryCallback() {
                override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
                    Log.i(tag, "PEER FOUND: $endpointId (${info.endpointName})")
                    val parts = info.endpointName.split("|", limit = 3)
                    if (parts.size < 3) return
                    val peerDeviceId = parts[2]
                    val myDeviceId = repository.getDeviceId()

                    val newDevice = P2PDevice(id = endpointId, name = parts[1], emoji = parts[0])
                    _scannedDevices.update { it.filter { d -> d.id != endpointId } + newDevice }
                    
                    if (myDeviceId < peerDeviceId && !activeConnections.contains(endpointId)) {
                        Log.i(tag, "MESH: Requesting $endpointId")
                        val localName = "${repository.emojiAvatar.value}|${repository.getCurrentNickname()}|$myDeviceId"
                        connectionsClient.requestConnection(localName, endpointId, connectionLifecycleCallback)
                            .addOnFailureListener { e -> Log.w(tag, "REQ FAIL: ${e.message}") }
                    }
                }
                override fun onEndpointLost(endpointId: String) {
                    _scannedDevices.update { it.filter { d -> d.id != endpointId } }
                }
            }, options).addOnSuccessListener { Log.i(tag, "DISCOVERY START SUCCESS") }
              .addOnFailureListener { e -> _isDiscovering.value = false; Log.e(tag, "DISCOVERY FAIL: ${e.message}") }
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
                .setStrategy(strategy)
                .build()
            val name = "${repository.emojiAvatar.value}|${repository.getCurrentNickname()}|${repository.getDeviceId()}"
            connectionsClient.startAdvertising(name, serviceId, connectionLifecycleCallback, options)
                .addOnSuccessListener { Log.i(tag, "ADVERTISING START SUCCESS") }
                .addOnFailureListener { e -> _isAdvertising.value = false; Log.e(tag, "ADVERTISING FAIL: ${e.message}") }
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

    private suspend fun getPeerKeyWithRetry(id: String): SecretKey? {
        var a = 0
        while (peerKeys[id] == null && a < 30) { delay(100); a++ }
        return peerKeys[id]
    }

    private fun syncMeshHistory(endpointId: String) {
        internalScope.launch(ioDispatcher) {
            val key = getPeerKeyWithRetry(endpointId) ?: return@launch
            messageDao.getAllMessages().first().filter { it.receiverId.isNullOrBlank() }.takeLast(10).forEach { entity ->
                try {
                    val json = Json.encodeToString(MessagePayload.serializer(), entity.toBluetoothPayload())
                    queuePayload(endpointId, Payload.fromBytes(cryptoManager.encrypt(json.toByteArray(), key)))
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
                val key = getPeerKeyWithRetry(receiverId) ?: return null
                queuePayload(receiverId, Payload.fromBytes(cryptoManager.encrypt(bytes, key)))
            } else {
                if (activeConnections.isEmpty()) Log.w(tag, "MESH EMPTY: Cannot Shout")
                activeConnections.forEach { target ->
                    internalScope.launch(ioDispatcher) {
                        peerKeys[target]?.let { key ->
                            try { queuePayload(target, Payload.fromBytes(cryptoManager.encrypt(bytes, key))) } catch (e: Exception) {}
                        }
                    }
                }
            }
            synchronized(messageIdHistory) { messageIdHistory.add(payload.messageId); if (messageIdHistory.size > 100) messageIdHistory.removeAt(0) }
            messageDao.insertMessage(payload.toMessageEntity(isFromLocalUser = true))
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

    private fun handleNearbyError(e: Exception, context: String) {
        if (e is ApiException && e.statusCode in setOf(
                8001, 8002, 8003, 8011, 8012, 8029, 8032, 8035, 8025, 8030
            )
        ) return
        internalScope.launch { _errors.emit(e.message ?: "Mesh Error") }
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
        outgoingQueue.close()
        internalScope.cancel()
    }
}
