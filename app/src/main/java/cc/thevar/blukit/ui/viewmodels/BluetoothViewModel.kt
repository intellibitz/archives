package cc.thevar.blukit.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cc.thevar.blukit.network.p2p.P2PController
import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.domain.model.VibeGroup
import cc.thevar.blukit.domain.model.ConnectionStatus
import cc.thevar.blukit.data.system.RadioStateManager
import cc.thevar.blukit.ui.toUiError
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
    private val repository: cc.thevar.blukit.data.repository.IdentityRepository,
    private val permissionManager: cc.thevar.blukit.data.system.SpreadPermissionManager,
    private val vibeStore: cc.thevar.blukit.data.local.VibeStore
) : ViewModel() {

    private val _manualConnectionState = MutableStateFlow<AirConnectionState?>(null)
    private val _selectedDevices = MutableStateFlow<Set<String>>(emptySet())
    private val _energySurge = MutableStateFlow(0f)
    val energySurge = _energySurge.asStateFlow()

    init {
        // 1. Observe messages to trigger energy surges
        p2pController.messages
            .onEach { if (it.isNotEmpty()) triggerEnergySurge() }
            .launchIn(viewModelScope)

        // 2. THE UNBREAKABLE CORE: Sentient Radio & Permission Management
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
                    android.util.Log.d("BlukitP2P", "CORE: Harmony achieved. Awakening the air.")
                    p2pController.startDiscovery()
                    p2pController.startAdvertising()
                } else {
                    android.util.Log.w("BlukitP2P", "CORE: Harmony lost. Air is still.")
                    p2pController.stopDiscovery()
                    p2pController.stopAdvertising()
                }
            }
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
        p2pController.connectedLinks,
        p2pController.incomingLinkRequests,
        p2pController.isConnected,
        p2pController.isDiscovering,
        p2pController.isAdvertising,
        p2pController.errors,
        p2pController.messages,
        vibeStore.groups,
        _selectedDevices,
        repository.vibedPeers,
        repository.blockedUsers,
        _manualConnectionState,
        permissionManager.permissionsGranted
    ) { args: Array<Any?> ->
        val scannedDevices = args[0] as List<P2PDevice>
        val radioStates = args[1] as cc.thevar.blukit.data.system.RadioStates
        val connectedLinks = args[2] as Set<String>
        val incomingLinkRequests = args[3] as Set<P2PDevice>
        val isConnected = args[4] as Boolean
        val isDiscovering = args[5] as Boolean
        val isAdvertising = args[6] as Boolean
        val error = args[7] as? cc.thevar.blukit.network.p2p.P2PError
        val messages = args[8] as List<cc.thevar.blukit.domain.model.MessagePayload>
        val groups = args[9] as List<VibeGroup>
        val selectedDevices = args[10] as Set<String>
        val vibedPeers = args[11] as Set<String>
        val blockedUsers = args[12] as Set<String>
        val manualState = args[13] as? AirConnectionState
        val permissionsGranted = args[14] as Boolean

        android.util.Log.d("BlukitUI", "STATE: vibedPeers=${vibedPeers.size}, isConnected=$isConnected")

        val connectionState = when {
            manualState != null -> manualState
            error != null -> AirConnectionState.Error(error.message)
            isConnected -> {
                val vibe = scannedDevices.find { it.id in connectedLinks }
                    ?: P2PDevice(id = connectedLinks.firstOrNull() ?: "", name = "?", emoji = "👤")
                AirConnectionState.Connected(vibe)
            }
            isDiscovering || isAdvertising -> AirConnectionState.Scanning
            else -> AirConnectionState.Disconnected
        }

        BluetoothUiState(
            scannedDevices = scannedDevices,
            selectedDevices = selectedDevices,
            vibedPeers = vibedPeers,
            connectionState = connectionState,
            connectedLinks = connectedLinks,
            incomingLinkRequests = incomingLinkRequests,
            isBluetoothEnabled = radioStates.isBluetoothEnabled,
            isLocationEnabled = radioStates.isLocationEnabled,
            isWifiEnabled = radioStates.isWifiEnabled,
            permissionsGranted = permissionsGranted,
            isDiscovering = isDiscovering,
            isAdvertising = isAdvertising,
            messages = messages,
            groups = groups,
            blockedUsers = blockedUsers,
            uiError = error?.toUiError()
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BluetoothUiState())

    fun startScan() {
        refreshRadios()
        if (state.value.isBluetoothEnabled && state.value.permissionsGranted) {
            p2pController.startDiscovery()
            p2pController.startAdvertising()
        }
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
        if (state.value.connectedLinks.contains(device.id)) return
        
        if (p2pController.isNearbyConnected(device.id)) {
             p2pController.requestLink(device)
             return
        }

        _manualConnectionState.value = AirConnectionState.Connecting
        p2pController.connectToDevice(device)
            .onEach { status ->
                when (status) {
                    is ConnectionStatus.Connected -> {
                        android.util.Log.d("BlukitP2P", "CONNECTED level Nearby. Requesting Link (Tie).")
                        _manualConnectionState.value = null
                        p2pController.requestLink(device)
                    }
                    is ConnectionStatus.Error -> _manualConnectionState.value = AirConnectionState.Error(status.message)
                    else -> {}
                }
            }.launchIn(viewModelScope)
    }

    fun requestWhisper(device: P2PDevice) {
        // For 1-1, we use the same connection logic but could tag it differently if needed.
        // Currently, requestLink handles the UI request state.
        connectToDevice(device)
    }

    fun acceptLink(device: P2PDevice) {
        p2pController.acceptLink(device)
    }

    fun toggleDeviceSelection(deviceId: String) {
        _selectedDevices.update { 
            if (it.contains(deviceId)) it - deviceId else it + deviceId
        }
    }

    fun clearSelection() {
        _selectedDevices.value = emptySet()
    }

    fun startGroupVibe(name: String, members: Set<String>? = null, isTie: Boolean): String {
        val targetMembers = members ?: _selectedDevices.value
        val type = if (isTie) VibeGroup.TYPE_TIE else VibeGroup.TYPE_SIDE
        val groupId = p2pController.startGroupVibe(name, targetMembers, type)
        if (members == null) _selectedDevices.value = emptySet()
        return groupId
    }

    fun denyLink(device: P2PDevice) {
        p2pController.denyLink(device)
    }

    fun sendMessage(message: String, groupId: String? = null) {
        viewModelScope.launch {
            if (groupId != null) {
                p2pController.sendGroupMessage(message, groupId)
            } else {
                val vibeId = state.value.connectedVibe?.id
                p2pController.sendMessage(message, vibeId)
            }
        }
    }

    fun roar(message: String, isPrivate: Boolean) {
        viewModelScope.launch {
            if (isPrivate) {
                // Send to all connected links (Private Group Vibe)
                state.value.connectedLinks.forEach { vibeId ->
                    p2pController.sendMessage(message, vibeId)
                }
            } else {
                // Public Roar
                p2pController.broadcastMessage(message)
            }
        }
    }

    fun disconnect() {
        p2pController.closeConnection()
        _manualConnectionState.value = null
    }

    fun deleteGroup(groupId: String) {
        viewModelScope.launch {
            vibeStore.deleteGroup(groupId)
        }
    }

    override fun onCleared() {
        p2pController.release()
    }
}
