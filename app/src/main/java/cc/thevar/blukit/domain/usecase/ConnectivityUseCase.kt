package cc.thevar.blukit.domain.usecase

import android.util.Log
import cc.thevar.blukit.data.system.RadioStateManager
import cc.thevar.blukit.data.system.SpreadPermissionManager
import cc.thevar.blukit.domain.model.ConnectionStatus
import cc.thevar.blukit.domain.model.Source
import cc.thevar.blukit.network.p2p.ConnectionController
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
    private val connectionController: ConnectionController,
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
                    Log.d("BlukitConnection", "USECASE: Harmony achieved. Awakening the connection.")
                    connectionController.startDiscovery()
                    connectionController.startAdvertising()
                } else {
                    Log.w("BlukitConnection", "USECASE: Harmony lost. Connection is still.")
                    connectionController.stopDiscovery()
                    connectionController.stopAdvertising()
                }
            }
            .launchIn(scope)
    }

    /**
     * Executes the multi-stage connection flow:
     * 1. Check if already connected.
     * 2. Check if Nearby connected (if so, request Group).
     * 3. If not connected, initiate Nearby connection, then request Group.
     */
    fun connectToSource(device: Source, currentlyConnectedRadios: Set<String>) {
        if (currentlyConnectedRadios.contains(device.id)) return
        
        if (connectionController.isNearbyConnected(device.id)) {
             Log.d("BlukitConnection", "USECASE: Nearby connected but no connection. Requesting Connection.")
             connectionController.requestRadio(device)
             return
        }

        _manualConnectionStatus.value = ConnectionStatus.Connecting
        connectionController.connectToDevice(device)
            .onEach { status ->
                when (status) {
                    is ConnectionStatus.Connected -> {
                        Log.d("BlukitConnection", "USECASE: Connected level Nearby. Requesting Connection.")
                        _manualConnectionStatus.value = null
                        connectionController.requestRadio(device)
                    }
                    is ConnectionStatus.Error -> {
                        Log.e("BlukitConnection", "USECASE: Connection error: ${status.message}")
                        _manualConnectionStatus.value = status
                    }
                    else -> {
                        _manualConnectionStatus.value = status
                    }
                }
            }.launchIn(scope)
    }
}
