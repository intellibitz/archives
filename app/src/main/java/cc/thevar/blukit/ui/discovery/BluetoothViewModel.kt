package cc.thevar.blukit.ui.discovery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cc.thevar.blukit.data.networking.P2PController
import cc.thevar.blukit.data.bluetooth.BluetoothDeviceDomain
import cc.thevar.blukit.data.bluetooth.ConnectionResult
import cc.thevar.blukit.data.system.RadioStateManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class BluetoothViewModel(
    private val p2pController: P2PController,
    private val radioStateManager: RadioStateManager
) : ViewModel() {

    private val _state = MutableStateFlow(BluetoothUiState())
    val state = combine(
        p2pController.scannedDevices,
        radioStateManager.radioStates,
        _state
    ) { scannedDevices, radioStates, state ->
        state.copy(
            scannedDevices = scannedDevices,
            isBluetoothEnabled = radioStates.isBluetoothEnabled,
            isLocationEnabled = radioStates.isLocationEnabled
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BluetoothUiState())

    private var deviceConnectionJob: Job? = null

    init {
        p2pController.isConnected.onEach { isConnected ->
            _state.update { it.copy(isConnected = isConnected) }
        }.launchIn(viewModelScope)

        p2pController.errors.onEach { error ->
            _state.update { it.copy(errorMessage = error) }
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

    fun connectToDevice(device: BluetoothDeviceDomain) {
        _state.update { it.copy(isConnecting = true) }
        deviceConnectionJob = p2pController
            .connectToDevice(device)
            .listen()
    }

    fun waitForIncomingConnections() {
        _state.update { it.copy(isConnecting = true) }
        p2pController.startAdvertising()
    }

    fun sendMessage(message: String) {
        viewModelScope.launch {
            p2pController.sendMessage(message)
        }
    }

    fun disconnect() {
        deviceConnectionJob?.cancel()
        p2pController.closeConnection()
        _state.update { it.copy(isConnected = false, isConnecting = false) }
    }

    private fun Flow<ConnectionResult>.listen(): Job {
        return onEach { result ->
            when(result) {
                ConnectionResult.ConnectionEstablished -> {
                    _state.update { it.copy(
                        isConnected = true,
                        isConnecting = false,
                        errorMessage = null
                    ) }
                }
                is ConnectionResult.TransferSucceeded -> {
                    // Handled by controller updating messages state flow
                }
                is ConnectionResult.Error -> {
                    _state.update { it.copy(
                        isConnected = false,
                        isConnecting = false,
                        errorMessage = result.message
                    ) }
                }
            }
        }.catch { e ->
            p2pController.closeConnection()
            _state.update { it.copy(
                isConnected = false,
                isConnecting = false,
                errorMessage = e.message
            ) }
        }.launchIn(viewModelScope)
    }

    override fun onCleared() {
        super.onCleared()
        p2pController.release()
    }
}
