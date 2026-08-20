package cc.thevar.blukit.domain.usecase

import android.util.Log
import cc.thevar.blukit.data.system.RadioStateManager
import cc.thevar.blukit.data.system.SpreadPermissionManager
import cc.thevar.blukit.domain.model.ConnectionStatus
import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.network.p2p.P2PController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*

/**
 * ORCHESTRATOR OF THE AIR.
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
                    Log.d("BlukitP2P", "USECASE: Harmony achieved. Awakening the air.")
                    p2pController.startDiscovery()
                    p2pController.startAdvertising()
                } else {
                    Log.w("BlukitP2P", "USECASE: Harmony lost. Air is still.")
                    p2pController.stopDiscovery()
                    p2pController.stopAdvertising()
                }
            }
            .launchIn(scope)
    }

    /**
     * Executes the multi-stage connection flow:
     * 1. Check if already linked.
     * 2. Check if Nearby connected (if so, request Link/Tie).
     * 3. If not connected, initiate Nearby connection, then request Link/Tie.
     */
    fun connectToDevice(device: P2PDevice, currentlyConnectedLinks: Set<String>) {
        if (currentlyConnectedLinks.contains(device.id)) return
        
        if (p2pController.isNearbyConnected(device.id)) {
             Log.d("BlukitP2P", "USECASE: Nearby connected but not linked. Requesting Link.")
             p2pController.requestLink(device)
             return
        }

        _manualConnectionStatus.value = ConnectionStatus.Connecting
        p2pController.connectToDevice(device)
            .onEach { status ->
                when (status) {
                    is ConnectionStatus.Connected -> {
                        Log.d("BlukitP2P", "USECASE: Connected level Nearby. Requesting Link.")
                        _manualConnectionStatus.value = null
                        p2pController.requestLink(device)
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
