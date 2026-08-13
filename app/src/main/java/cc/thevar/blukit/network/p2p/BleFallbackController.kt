package cc.thevar.blukit.network.p2p

import android.bluetooth.BluetoothManager
import android.content.Context
import android.util.Log
import cc.thevar.blukit.domain.model.ConnectionStatus
import cc.thevar.blukit.domain.model.MessagePayload
import cc.thevar.blukit.domain.model.P2PDevice
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.*

/**
 * Native BLE Fallback Controller.
 * Used when Google Nearby Connections is unavailable or failing.
 * Implements standard BLE GATT advertising and scanning.
 */
class BleFallbackController(
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : P2PController {

    private val tag = "BlukitBLE"
    private val bluetoothManager = context.getSystemService(BluetoothManager::class.java)
    private val adapter = bluetoothManager?.adapter

    private val internalScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _scannedDevices = MutableStateFlow<List<P2PDevice>>(emptyList())
    override val scannedDevices = _scannedDevices.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    override val isConnected = _isConnected.asStateFlow()

    private val _connectedPeers = MutableStateFlow<Set<String>>(emptySet())
    override val connectedPeers = _connectedPeers.asStateFlow()

    private val _isDiscovering = MutableStateFlow(false)
    override val isDiscovering = _isDiscovering.asStateFlow()

    private val _isAdvertising = MutableStateFlow(false)
    override val isAdvertising = _isAdvertising.asStateFlow()

    private val _errors = MutableStateFlow("")
    override val errors = _errors.asStateFlow()

    override val messages = MutableStateFlow<List<MessagePayload>>(emptyList())

    override fun startDiscovery() {
        Log.i(tag, "BLE: startDiscovery()")
        if (adapter == null || !adapter.isEnabled) {
            _errors.value = "Bluetooth Disabled"
            return
        }
        _isDiscovering.value = true
        // TODO: Implement BLE Scanner
    }

    override fun stopDiscovery() {
        _isDiscovering.value = false
    }

    override fun startAdvertising() {
        Log.i(tag, "BLE: startAdvertising()")
        _isAdvertising.value = true
        // TODO: Implement BLE Advertiser
    }

    override fun stopAdvertising() {
        _isAdvertising.value = false
    }

    override fun connectToDevice(device: P2PDevice): SharedFlow<ConnectionStatus> {
        val flow = MutableSharedFlow<ConnectionStatus>(replay = 1)
        flow.tryEmit(ConnectionStatus.Connecting)
        // TODO: Implement GATT Connect
        return flow.asSharedFlow()
    }

    override suspend fun sendMessage(content: String, receiverId: String?): MessagePayload? {
        // TODO: Implement GATT Write
        return null
    }

    override suspend fun broadcastMessage(content: String): MessagePayload? {
        return sendMessage(content, null)
    }

    override fun closeConnection() {
        _isConnected.value = false
        _connectedPeers.value = emptySet()
    }

    override fun release() {
        stopDiscovery()
        stopAdvertising()
        closeConnection()
        internalScope.cancel()
    }
}
