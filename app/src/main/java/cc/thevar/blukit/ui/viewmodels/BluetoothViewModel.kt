package cc.thevar.blukit.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cc.thevar.blukit.network.p2p.P2PController
import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.domain.model.ConnectionStatus
import cc.thevar.blukit.data.system.RadioStateManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.launch

/**
 * ViewModel responsible for managing connectivity within The Air and UI states.
 * Coordinates between the P2PController and RadioStateManager to provide
 * a reactive stream of Bluetooth and Vibing Air statuses.
 */
class BluetoothViewModel(
    private val p2pController: P2PController,
    private val radioStateManager: RadioStateManager,
) : ViewModel() {

    private val _manualConnectionState = MutableStateFlow<AirConnectionState?>(null)
    private val _energySurge = MutableStateFlow(0f)
    val energySurge = _energySurge.asStateFlow()

    init {
        // Observe messages to trigger energy surges
        p2pController.messages
            .onEach { if (it.isNotEmpty()) triggerEnergySurge() }
            .launchIn(viewModelScope)
    }

    private fun triggerEnergySurge() {
        viewModelScope.launch {
            _energySurge.value = 1f
            delay(100.milliseconds)
            _energySurge.value = 0f
        }
    }

    val state: StateFlow<BluetoothUiState> = combine(
        p2pController.scannedDevices,
        radioStateManager.radioStates,
        p2pController.connectedTies,
        p2pController.incomingTieRequests,
        p2pController.isConnected,
        p2pController.isDiscovering,
        p2pController.isAdvertising,
        p2pController.errors,
        p2pController.messages,
        _manualConnectionState
    ) { args: Array<Any?> ->
        val scannedDevices = args[0] as List<P2PDevice>
        val radioStates = args[1] as cc.thevar.blukit.data.system.RadioStates
        val connectedTies = args[2] as Set<String>
        val incomingTieRequests = args[3] as Set<P2PDevice>
        val isConnected = args[4] as Boolean
        val isDiscovering = args[5] as Boolean
        val isAdvertising = args[6] as Boolean
        val error = args[7] as String
        val messages = args[8] as List<cc.thevar.blukit.domain.model.MessagePayload>
        val manualState = args[9] as? AirConnectionState

        val connectionState = when {
            manualState != null -> manualState
            error.isNotEmpty() -> AirConnectionState.Error(error)
            isConnected -> {
                val vibe = scannedDevices.find { it.id in connectedTies }
                    ?: P2PDevice(id = connectedTies.firstOrNull() ?: "", name = "vibe", emoji = "🎭")
                AirConnectionState.Connected(vibe)
            }
            isDiscovering || isAdvertising -> AirConnectionState.Scanning
            else -> AirConnectionState.Disconnected
        }

        BluetoothUiState(
            scannedDevices = scannedDevices,
            connectionState = connectionState,
            connectedTies = connectedTies,
            incomingTieRequests = incomingTieRequests,
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
        _manualConnectionState.value = AirConnectionState.Connecting
        p2pController.connectToDevice(device)
            .onEach { status ->
                when (status) {
                    is ConnectionStatus.Connected -> {
                        _manualConnectionState.value = null
                        p2pController.requestTie(device)
                    }
                    is ConnectionStatus.Error -> _manualConnectionState.value = AirConnectionState.Error(status.message)
                    else -> {}
                }
            }.launchIn(viewModelScope)
    }

    fun acceptTie(device: P2PDevice) {
        p2pController.acceptTie(device)
    }

    fun denyTie(device: P2PDevice) {
        p2pController.denyTie(device)
    }

    fun sendMessage(message: String) {
        viewModelScope.launch {
            val vibeId = state.value.connectedVibe?.id
            p2pController.sendMessage(message, vibeId)
        }
    }

    fun broadcastMessage(message: String) {
        viewModelScope.launch {
            p2pController.broadcastMessage(message)
        }
    }

    fun disconnect() {
        p2pController.closeConnection()
        _manualConnectionState.value = AirConnectionState.Disconnected
    }

    override fun onCleared() {
        p2pController.release()
    }
}
