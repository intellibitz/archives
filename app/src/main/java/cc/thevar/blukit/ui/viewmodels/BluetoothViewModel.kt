package cc.thevar.blukit.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cc.thevar.blukit.network.p2p.P2PController
import cc.thevar.blukit.domain.model.MessagePayload
import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.domain.model.VibeGroup
import cc.thevar.blukit.domain.model.ConnectionStatus
import cc.thevar.blukit.data.system.RadioStateManager
import cc.thevar.blukit.ui.toUiError
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
    private val vibeStore: cc.thevar.blukit.data.local.VibeStore,
    private val connectivityUseCase: cc.thevar.blukit.domain.usecase.ConnectivityUseCase,
) : ViewModel() {

    private val _selectedDevices = MutableStateFlow<Set<String>>(emptySet())
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

    private val harmonyState: Flow<HardwareHarmony> = combine(
        radioStateManager.radioStates,
        permissionManager.permissionsGranted
    ) { radioStates, permissionsGranted ->
        HardwareHarmony(
            isBluetoothEnabled = radioStates.isBluetoothEnabled,
            isLocationEnabled = radioStates.isLocationEnabled,
            isWifiEnabled = radioStates.isWifiEnabled,
            permissionsGranted = permissionsGranted
        )
    }

    private val activityState: Flow<AirActivity> = combine(
        p2pController.isDiscovering,
        p2pController.isAdvertising,
        p2pController.errors
    ) { isDiscovering, isAdvertising, error ->
        AirActivity(
            isDiscovering = isDiscovering,
            isAdvertising = isAdvertising,
            uiError = error?.toUiError()
        )
    }

    private val crowdState: Flow<MeshCrowd> = combine(
        p2pController.scannedDevices,
        _selectedDevices,
        repository.vibedPeers,
        repository.blockedUsers,
        p2pController.incomingLinkRequests,
        p2pController.outgoingLinkRequests
    ) { Array ->
        val scanned = Array[0] as List<P2PDevice>
        val selected = Array[1] as Set<String>
        val vibed = Array[2] as Set<String>
        val blocked = Array[3] as Set<String>
        val incoming = Array[4] as Set<P2PDevice>
        val outgoing = Array[5] as Set<P2PDevice>
        
        MeshCrowd(
            scannedDevices = scanned,
            selectedDevices = selected,
            vibedPeers = vibed,
            blockedUsers = blocked,
            incomingLinkRequests = incoming,
            outgoingLinkRequests = outgoing
        )
    }

    private val sessionDataState: Flow<ResonanceSession> = combine(
        p2pController.connectedLinks,
        p2pController.messages,
        vibeStore.groups
    ) { links, messages, groups ->
        ResonanceSession(
            connectedLinks = links,
            messages = messages,
            groups = groups
        )
    }

    val state: StateFlow<BluetoothUiState> = combine(
        harmonyState,
        activityState,
        crowdState,
        sessionDataState,
        combine(
            p2pController.isConnected,
            connectivityUseCase.manualConnectionStatus,
            p2pController.errors
        ) { isConnected, manualStatus, rawError -> Triple(isConnected, manualStatus, rawError) }
    ) { harmony, activity, crowd, session, sessionExtras ->
        val (isConnected, manualStatus, rawError) = sessionExtras

        val manualConnectionState = when (manualStatus) {
            ConnectionStatus.Connecting -> AirConnectionState.Connecting
            is ConnectionStatus.Error -> AirConnectionState.Error(manualStatus.message)
            else -> null
        }

        android.util.Log.d("BlukitUI", "STATE: vibedPeers=${crowd.vibedPeers.size}, isConnected=$isConnected")

        val connectionState = when {
            manualConnectionState != null -> manualConnectionState
            activity.uiError != null -> AirConnectionState.Error(activity.uiError.message)
            isConnected -> {
                val vibe = crowd.scannedDevices.find { it.id in session.connectedLinks }
                    ?: P2PDevice(id = session.connectedLinks.firstOrNull() ?: "", name = "?", emoji = "👤")
                AirConnectionState.Connected(vibe)
            }
            activity.isDiscovering || activity.isAdvertising -> AirConnectionState.Scanning
            else -> AirConnectionState.Disconnected
        }

        BluetoothUiState(
            harmony = harmony,
            activity = activity,
            crowd = crowd,
            session = session.copy(connectionState = connectionState)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BluetoothUiState())

    fun startScan() {
        refreshRadios()
        if (state.value.harmony.isBluetoothEnabled && state.value.harmony.permissionsGranted) {
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
        connectivityUseCase.clearManualStatus()
    }

    fun startAdvertising() {
        p2pController.startAdvertising()
    }

    fun connectToDevice(device: P2PDevice) {
        connectivityUseCase.connectToDevice(device, state.value.session.connectedLinks)
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

    fun addMemberToGroup(groupId: String, deviceId: String) {
        val group = state.value.session.groups.find { it.id == groupId } ?: return
        p2pController.updateGroupMembers(groupId, group.memberIds + deviceId)
    }

    fun removeMemberFromGroup(groupId: String, deviceId: String) {
        val group = state.value.session.groups.find { it.id == groupId } ?: return
        if (deviceId == repository.getDeviceId()) return // Can't remove self this way (use delete group)
        p2pController.updateGroupMembers(groupId, group.memberIds - deviceId)
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

    fun spreadVibe(message: String, vibeType: Int = MessagePayload.VIBE_LOCAL) {
        viewModelScope.launch {
            when (vibeType) {
                MessagePayload.VIBE_LOCAL -> {
                    // Just store locally in the message list via p2pController or repo
                    // If p2pController manages messages, we might need a 'storeLocal' method
                    p2pController.sendMessage(message, null, MessagePayload.VIBE_LOCAL)
                }
                MessagePayload.VIBE_PUBLIC -> {
                    p2pController.broadcastMessage(message, MessagePayload.VIBE_PUBLIC)
                }
                MessagePayload.VIBE_TIE -> {
                    val targets = state.value.crowd.selectedDevices.ifEmpty { state.value.session.connectedLinks }
                    if (targets.isNotEmpty()) {
                        targets.forEach { targetId ->
                            p2pController.sendMessage(message, targetId, MessagePayload.VIBE_TIE)
                        }
                    } else {
                        p2pController.sendMessage(message, null, MessagePayload.VIBE_TIE)
                    }
                }
            }
        }
    }

    fun disconnect() {
        connectivityUseCase.disconnect()
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
