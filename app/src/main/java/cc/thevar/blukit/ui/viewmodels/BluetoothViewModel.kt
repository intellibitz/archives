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
    private val radioStateManager: RadioStateManager
) : ViewModel() {

    private val _state = MutableStateFlow(BluetoothUiState())
    val state: StateFlow<BluetoothUiState> = combine(
        p2pController.scannedDevices,
        radioStateManager.radioStates,
        p2pController.connectedPeers,
        _state
    ) { scannedDevices, radioStates, connectedPeers, currentState ->
        val connectedDevice = scannedDevices.find { it.id in connectedPeers } ?: currentState.connectedPeer
        currentState.copy(
            scannedDevices = scannedDevices,
            isBluetoothEnabled = radioStates.isBluetoothEnabled,
            isLocationEnabled = radioStates.isLocationEnabled,
            connectedPeer = connectedDevice
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BluetoothUiState())

    private var connectionJob: Job? = null

    init {
        p2pController.isConnected.onEach { isConnected ->
            _state.update { it.copy(isConnected = isConnected, isConnecting = false) }
        }.launchIn(viewModelScope)

        p2pController.errors.onEach { error ->
            _state.update { it.copy(errorMessage = error, isConnecting = false) }
        }.launchIn(viewModelScope)

        p2pController.messages.onEach { messages ->
            _state.update { it.copy(messages = messages) }
        }.launchIn(viewModelScope)
    }

    fun startScan() {
        p2pController.startDiscovery()
        p2pController.startAdvertising()
    }

    fun stopScan() {
        p2pController.stopDiscovery()
        p2pController.stopAdvertising()
    }

    fun startAdvertising() {
        p2pController.startAdvertising()
    }

    fun stopAdvertising() {
        p2pController.stopAdvertising()
    }

    fun connectToDevice(device: P2PDevice) {
        _state.update { it.copy(isConnecting = true) }
        connectionJob?.cancel()
        connectionJob = p2pController.connectToDevice(device).onEach { status ->
            when (status) {
                is ConnectionStatus.Connected -> {
                    _state.update { it.copy(isConnected = true, isConnecting = false) }
                }
                is ConnectionStatus.Error -> {
                    _state.update { it.copy(isConnected = false, isConnecting = false, errorMessage = status.message) }
                }
                else -> {}
            }
        }.launchIn(viewModelScope)
    }

    fun sendMessage(message: String) {
        viewModelScope.launch {
            p2pController.sendMessage(message)
        }
    }

    fun broadcastMessage(message: String) {
        viewModelScope.launch {
            p2pController.broadcastMessage(message)
        }
    }

    fun disconnect() {
        p2pController.closeConnection()
        connectionJob?.cancel()
        _state.update { it.copy(isConnected = false, isConnecting = false) }
    }

    override fun onCleared() {
        super.onCleared()
        p2pController.release()
    }
}
