package cc.thevar.blukit.data.networking

import android.content.Context
import cc.thevar.blukit.R
import cc.thevar.blukit.data.IdentityRepository
import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.domain.model.MessagePayload
import cc.thevar.blukit.domain.model.ConnectionStatus
import cc.thevar.blukit.data.local.dao.MessageDao
import cc.thevar.blukit.data.local.entities.toBluetoothPayload
import cc.thevar.blukit.data.local.entities.toMessageEntity
import cc.thevar.blukit.data.security.CryptoManager
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.util.*

class NearbyP2PController(
    private val context: Context,
    private val repository: IdentityRepository,
    private val messageDao: MessageDao,
) : P2PController {

    private val connectionsClient = Nearby.getConnectionsClient(context)
    private val strategy = Strategy.P2P_CLUSTER
    private val serviceId = "cc.thevar.blukit.P2P"

    private val _scannedDevices = MutableStateFlow<List<P2PDevice>>(emptyList())
    override val scannedDevices = _scannedDevices.asStateFlow()

    private val _isConnected = MutableStateFlow(value = false)
    override val isConnected = _isConnected.asStateFlow()

    private val _errors = MutableSharedFlow<String>()
    override val errors = _errors.asSharedFlow()

    override val messages: StateFlow<List<MessagePayload>> = messageDao.getAllMessages()
        .map { entities -> entities.map { it.toBluetoothPayload() } }
        .stateIn(
            scope = CoroutineScope(Dispatchers.IO),
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val cryptoManager = CryptoManager()
    private val activeConnections = mutableSetOf<String>()

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            connectionsClient.acceptConnection(endpointId, payloadCallback)
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            if (result.status.isSuccess) {
                activeConnections.add(endpointId)
                _isConnected.value = true
            } else {
                CoroutineScope(Dispatchers.IO).launch {
                    val errorMsg = context.getString(
                        R.string.error_connection_failed,
                        result.status.statusMessage ?: context.getString(R.string.error_unknown)
                    )
                    _errors.emit(errorMsg)
                }
            }
        }

        override fun onDisconnected(endpointId: String) {
            activeConnections.remove(endpointId)
            if (activeConnections.isEmpty()) {
                _isConnected.value = false
            }
        }
    }

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            payload.asBytes()?.let { bytes ->
                val decryptedBytes = try {
                    cryptoManager.decrypt(bytes)
                } catch (e: Exception) {
                    e.printStackTrace()
                    return@let
                }
                val payloadJson = decryptedBytes.decodeToString()
                val bluetoothPayload = Json.decodeFromString<MessagePayload>(payloadJson)
                
                CoroutineScope(Dispatchers.IO).launch {
                    val blocked = repository.blockedUsers.first()
                    if (bluetoothPayload.senderId !in blocked) {
                        messageDao.insertMessage(bluetoothPayload.toMessageEntity(isFromLocalUser = false))
                    }
                }
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {}
    }

    override fun startDiscovery() {
        val options = DiscoveryOptions.Builder().setStrategy(strategy).build()
        connectionsClient.startDiscovery(
            serviceId,
            object : EndpointDiscoveryCallback() {
                override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
                    _scannedDevices.update { devices ->
                        val newDevice = P2PDevice(info.endpointName, endpointId)
                        if (newDevice in devices) devices else devices + newDevice
                    }
                }

                override fun onEndpointLost(endpointId: String) {
                    _scannedDevices.update { devices ->
                        devices.filter { it.address != endpointId }
                    }
                }
            },
            options
        )
    }

    override fun stopDiscovery() {
        connectionsClient.stopDiscovery()
    }

    override fun startAdvertising() {
        val options = AdvertisingOptions.Builder().setStrategy(strategy).build()
        CoroutineScope(Dispatchers.IO).launch {
            val nickname = repository.getNickname() ?: context.getString(R.string.anonymous)
            connectionsClient.startAdvertising(nickname, serviceId, connectionLifecycleCallback, options)
        }
    }

    override fun stopAdvertising() {
        connectionsClient.stopAdvertising()
    }

    override fun connectToDevice(device: P2PDevice): SharedFlow<ConnectionStatus> {
        CoroutineScope(Dispatchers.IO).launch {
            val nickname = repository.getNickname() ?: context.getString(R.string.anonymous)
            connectionsClient.requestConnection(nickname, device.address, connectionLifecycleCallback)
        }
        return MutableSharedFlow() 
    }

    override suspend fun sendMessage(content: String, receiverId: String?): MessagePayload {
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
        val encryptedBytes = cryptoManager.encrypt(payloadJson.toByteArray())
        val payload = Payload.fromBytes(encryptedBytes)

        if (receiverId != null) {
            connectionsClient.sendPayload(receiverId, payload)
        } else {
            activeConnections.forEach { endpointId ->
                connectionsClient.sendPayload(endpointId, payload)
            }
        }

        messageDao.insertMessage(payloadObj.toMessageEntity(isFromLocalUser = true))
        return payloadObj
    }

    override fun closeConnection() {
        connectionsClient.stopAllEndpoints()
        activeConnections.clear()
        _isConnected.value = false
    }

    override fun release() {
        stopDiscovery()
        stopAdvertising()
        closeConnection()
    }
}
