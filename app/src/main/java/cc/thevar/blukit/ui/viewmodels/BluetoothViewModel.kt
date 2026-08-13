package cc.thevar.blukit.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cc.thevar.blukit.network.p2p.P2PController
import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.domain.model.ConnectionStatus
import cc.thevar.blukit.data.system.RadioStateManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Supreme Senior Android Expert Implementation:
 * Bluetooth ViewModel with Reactive UI State and nearby discovery control.
 */
class BluetoothViewModel(
    private val p2pController: P2PController,
    private val radioStateManager: RadioStateManager,
) : ViewModel() {

    private val _manualConnectionState = MutableStateFlow<MeshConnectionState?>(null)

    val state: StateFlow<BluetoothUiState> = combine(
        p2pController.scannedDevices,
        radioStateManager.radioStates,
        p2pController.connectedPeers,
        p2pController.isConnected,
        p2pController.isDiscovering,
        p2pController.isAdvertising,
        p2pController.errors,
        p2pController.messages,
        _manualConnectionState
    ) { args: Array<Any?> ->
        val scannedDevices = args[0] as List<P2PDevice>
        val radioStates = args[1] as cc.thevar.blukit.data.system.RadioStates
        val connectedPeers = args[2] as Set<String>
        val isConnected = args[3] as Boolean
        val isDiscovering = args[4] as Boolean
        val isAdvertising = args[5] as Boolean
        val error = args[6] as String
        val messages = args[7] as List<cc.thevar.blukit.domain.model.MessagePayload>
        val manualState = args[8] as? MeshConnectionState

        val connectionState = when {
            manualState != null -> manualState
            error.isNotEmpty() -> MeshConnectionState.Error(error)
            isConnected -> {
                val peer = scannedDevices.find { it.id in connectedPeers }
                    ?: P2PDevice(id = connectedPeers.firstOrNull() ?: "", name = "vibe", emoji = "🎭")
                MeshConnectionState.Connected(peer)
            }
            isDiscovering || isAdvertising -> MeshConnectionState.Scanning
            else -> MeshConnectionState.Disconnected
        }

        BluetoothUiState(
            scannedDevices = scannedDevices,
            connectionState = connectionState,
            connectedPeers = connectedPeers,
            isBluetoothEnabled = radioStates.isBluetoothEnabled,
            isLocationEnabled = radioStates.isLocationEnabled,
            isDiscovering = isDiscovering,
            isAdvertising = isAdvertising,
            messages = messages,
            errorMessage = error.takeIf { it.isNotEmpty() }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BluetoothUiState())

    fun startScan() {
        refreshRadios()
        p2pController.startDiscovery()
        p2pController.startAdvertising()
    }

    fun refreshRadios() {
        radioStateManager.triggerRefresh()
    }

    fun stopScan() {
        p2pController.stopDiscovery()
        p2pController.stopAdvertising()
        _manualConnectionState.value = null
    }

    fun startAdvertising() {
        p2pController.startAdvertising()
    }

    fun connectToDevice(device: P2PDevice) {
        _manualConnectionState.value = MeshConnectionState.Connecting
        p2pController.connectToDevice(device)
            .onEach { status ->
                when (status) {
                    is ConnectionStatus.Connected -> _manualConnectionState.value = null 
                    is ConnectionStatus.Error -> _manualConnectionState.value = MeshConnectionState.Error(status.message)
                    else -> {}
                }
            }.launchIn(viewModelScope)
    }

    fun sendMessage(message: String) {
        viewModelScope.launch {
            val peerId = state.value.connectedPeer?.id
            p2pController.sendMessage(message, peerId)
        }
    }

    fun broadcastMessage(message: String) {
        viewModelScope.launch {
            p2pController.broadcastMessage(message)
        }
    }

    fun disconnect() {
        p2pController.closeConnection()
        _manualConnectionState.value = MeshConnectionState.Disconnected
    }

    override fun onCleared() {
        p2pController.release()
    }
}
