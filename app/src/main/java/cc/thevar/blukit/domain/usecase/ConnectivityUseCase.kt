package cc.thevar.blukit.domain.usecase

import android.util.Log
import cc.thevar.blukit.data.system.RadioStateManager
import cc.thevar.blukit.data.system.SpreadPermissionManager
import cc.thevar.blukit.domain.model.ConnectionStatus
import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.network.p2p.P2PController
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
    private val p2pController: P2PController,
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
                    Log.d("BlukitP2P", "USECASE: Harmony achieved. Awakening the radios.")
                    p2pController.startDiscovery()
                    p2pController.startAdvertising()
                } else {
                    Log.w("BlukitP2P", "USECASE: Harmony lost. Radios are still.")
                    p2pController.stopDiscovery()
                    p2pController.stopAdvertising()
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
    fun connectToDevice(device: P2PDevice, currentlyConnectedRadios: Set<String>) {
        if (currentlyConnectedRadios.contains(device.id)) return
        
        if (p2pController.isNearbyConnected(device.id)) {
             Log.d("BlukitP2P", "USECASE: Nearby connected but no radio. Requesting Radio.")
             p2pController.requestRadio(device)
             return
        }

        _manualConnectionStatus.value = ConnectionStatus.Connecting
        p2pController.connectToDevice(device)
            .onEach { status ->
                when (status) {
                    is ConnectionStatus.Connected -> {
                        Log.d("BlukitP2P", "USECASE: Connected level Nearby. Requesting Radio.")
                        _manualConnectionStatus.value = null
                        p2pController.requestRadio(device)
                    }
                    is ConnectionStatus.Error -> {
                        Log.e("BlukitP2P", "USECASE: Connection error: ${status.message}")
                        _manualConnectionStatus.value = status
                    }
                    else -> {
                        _manualConnectionStatus.value = status
                    }
                }
            }.launchIn(scope)
    }

    fun disconnect() {
        p2pController.closeConnection()
        _manualConnectionStatus.value = null
    }

    fun clearManualStatus() {
        _manualConnectionStatus.value = null
    }
}
