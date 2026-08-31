package cc.thevar.blukit.domain.usecase

import android.util.Log
import cc.thevar.blukit.data.system.RadioStateManager
import cc.thevar.blukit.data.system.SpreadPermissionManager
import cc.thevar.blukit.domain.model.ConnectionStatus
import cc.thevar.blukit.domain.model.Source
import cc.thevar.blukit.network.p2p.ResonanceController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * ORCHESTRATOR OF THE RADIOS.
 * Encapsulates the complex logic of sentient radios, permission-gated discovery,
 * and multi-stage connection handshakes.
 */
class ConnectivityUseCase(
    private val resonanceController: ResonanceController,
    private val radioStateManager: RadioStateManager,
    private val permissionManager: SpreadPermissionManager,
    private val scope: CoroutineScope
) {
    private val _manualConnectionStatus = MutableStateFlow<ConnectionStatus?>(null)
    val manualConnectionStatus: StateFlow<ConnectionStatus?> = _manualConnectionStatus.asStateFlow()

    init {
        observeHardwareHarmony()
    }

    private fun observeHardwareHarmony() {
        // Automatically start/stop based on Bluetooth availability AND Permissions
        combine(
            radioStateManager.radioStates,
            permissionManager.permissionsGranted
        ) { radios, permissionsGranted ->
            radios.isBluetoothEnabled && permissionsGranted
        }
            .distinctUntilChanged()
            .onEach { isHealthy ->
                if (isHealthy) {
                    Log.d("BlukitResonance", "USECASE: Harmony achieved. Awakening the resonance.")
                    resonanceController.startDiscovery()
                    resonanceController.startAdvertising()
                } else {
                    Log.w("BlukitResonance", "USECASE: Harmony lost. Resonance is still.")
                    resonanceController.stopDiscovery()
                    resonanceController.stopAdvertising()
                }
            }
            .launchIn(scope)
    }

    /**
     * Executes the multi-stage connection flow:
     * 1. Check if already connected.
     * 2. Check if Nearby connected (if so, request Radio).
     * 3. If not connected, initiate Nearby connection, then request Radio.
     */
    fun connectToSource(device: Source, currentlyConnectedRadios: Set<String>) {
        if (currentlyConnectedRadios.contains(device.id)) return
        
        if (resonanceController.isNearbyConnected(device.id)) {
             Log.d("BlukitResonance", "USECASE: Nearby connected but no resonance. Requesting Resonance.")
             resonanceController.requestRadio(device)
             return
        }

        _manualConnectionStatus.value = ConnectionStatus.Connecting
        resonanceController.connectToDevice(device)
            .onEach { status ->
                when (status) {
                    is ConnectionStatus.Connected -> {
                        Log.d("BlukitResonance", "USECASE: Connected level Nearby. Requesting Resonance.")
                        _manualConnectionStatus.value = null
                        resonanceController.requestRadio(device)
                    }
                    is ConnectionStatus.Error -> {
                        Log.e("BlukitResonance", "USECASE: Connection error: ${status.message}")
                        _manualConnectionStatus.value = status
                    }
                    else -> {
                        _manualConnectionStatus.value = status
                    }
                }
            }.launchIn(scope)
    }
}
