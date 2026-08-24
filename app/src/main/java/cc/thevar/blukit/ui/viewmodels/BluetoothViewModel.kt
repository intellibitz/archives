package cc.thevar.blukit.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cc.thevar.blukit.network.p2p.P2PController
import cc.thevar.blukit.domain.model.MessagePayload
import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.domain.model.VibeGroup
import cc.thevar.blukit.domain.model.ConnectionStatus
import cc.thevar.blukit.data.system.RadioStateManager
import cc.thevar.blukit.data.system.SpreadPermissionManager
import cc.thevar.blukit.data.repository.IdentityRepository
import cc.thevar.blukit.data.local.VibeStore
import cc.thevar.blukit.domain.usecase.ConnectivityUseCase
import cc.thevar.blukit.ui.toUiError
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * ViewModel responsible for managing connectivity within The Air and UI states.
 * Coordinates between the P2PController and RadioStateManager to provide
 * a reactive stream of Bluetooth and Vibing Air statuses.
 */
class BluetoothViewModel(
    private val p2pController: P2PController,
    private val radioStateManager: RadioStateManager,
    private val repository: IdentityRepository,
    private val permissionManager: SpreadPermissionManager,
    private val vibeStore: VibeStore,
    private val connectivityUseCase: ConnectivityUseCase,
) : ViewModel() {

    private val _selectedDevices = MutableStateFlow<Set<String>>(emptySet())
    private val _energySurge = MutableStateFlow(0f)
    val energySurge = _energySurge.asStateFlow()

    private val _currentTieId = MutableStateFlow(VibeGroup.ID_AIR)
    val currentTieId = _currentTieId.asStateFlow()

    val discoveredAirs = p2pController.discoveredAirs
        .filter { it.id != _currentTieId.value }
        .shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000))

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

    init {
        // Observe messages to trigger energy surges
        p2pController.messages
            .onEach { if (it.isNotEmpty()) triggerEnergySurge() }
            .launchIn(viewModelScope)

        // AIR AWAKENING: Automatically promote silence to shout when radios engage
        combine(
            radioStateManager.radioStates,
            permissionManager.permissionsGranted
        ) { radios, granted -> 
            radios.isBluetoothEnabled && granted 
        }
        .distinctUntilChanged()
        .filter { it } 
        .onEach { promoteSilenceToShout() }
        .launchIn(viewModelScope)

        // RITUAL SENTIENCE: Check schedules every minute
        viewModelScope.launch {
            while (true) {
                checkAirSchedules()
                delay(60 * 1000)
            }
        }

        // STORAGE PRUNING: Daily cleanup
        viewModelScope.launch {
            while (true) {
                vibeStore.pruneMedia(thresholdMs = 90L * 24 * 60 * 60 * 1000) // 90 days
                delay(24L * 60 * 60 * 1000)
            }
        }
    }

    private fun checkAirSchedules() {
        val now = java.util.Calendar.getInstance()
        val day = now.get(java.util.Calendar.DAY_OF_WEEK)
        val hour = now.get(java.util.Calendar.HOUR_OF_DAY)
        val minute = now.get(java.util.Calendar.MINUTE)

        val scheduledAirs = vibeStore.groups.value.filter { it.schedules.isNotEmpty() }
        scheduledAirs.forEach { air ->
            val isActive = air.schedules.any { s ->
                s.dayOfWeek == day && 
                (hour > s.startHour || (hour == s.startHour && minute >= s.startMinute)) &&
                (hour < s.endHour || (hour == s.endHour && minute <= s.endMinute))
            }
            if (isActive && air.isArchived) {
                restoreFromVault(air.id)
                Log.i("BluetoothViewModel", "Ritual Awakening: ${air.name}")
            }
        }
    }

    private suspend fun promoteSilenceToShout() {
        val myId = repository.getDeviceId()
        val messages = p2pController.messages.value
        val silentVibes = messages.filter { 
            it.senderId == myId && it.vibeType == MessagePayload.VIBE_SILENCE 
        }
        
        silentVibes.forEach { vibe ->
            // Promote to THE AIR or last active tie? Default to THE AIR.
            p2pController.broadcastMessage(vibe.content, MessagePayload.VIBE_SHOUT, vibe.messageId, groupId = VibeGroup.ID_AIR, groupName = "THE AIR")
        }
    }

    private fun triggerEnergySurge() {
        viewModelScope.launch {
            _energySurge.value = 1f
            delay(100.milliseconds)
            _energySurge.value = 0f
        }
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
    ) { flows: Array<out Any?> ->
        val scanned = flows[0] as List<P2PDevice>
        val selected = flows[1] as Set<String>
        val vibed = flows[2] as Set<String>
        val blocked = flows[3] as Set<String>
        val incoming = flows[4] as Set<P2PDevice>
        val outgoing = flows[5] as Set<P2PDevice>
        
        MeshCrowd(
            scannedDevices = scanned,
            selectedDevices = selected,
            vibedPeers = vibed,
            blockedUsers = blocked,
            incomingLinkRequests = incoming,
            outgoingLinkRequests = outgoing
        )
    }

    private val sessionDataState: Flow<VibeSession> = combine(
        p2pController.connectedLinks,
        p2pController.messages,
        vibeStore.activeGroups,
        vibeStore.archivedGroups
    ) { links, messages, groups, archivedGroups ->
        VibeSession(
            connectedLinks = links,
            messages = messages,
            groups = groups,
            archivedGroups = archivedGroups
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
        permissionManager.refresh()
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

    fun startGroupVibe(name: String, members: Set<String>? = null, scope: Int = VibeGroup.SCOPE_PRIVATE): String {
        val targetMembers = members ?: _selectedDevices.value
        val normalized = name.uppercase().trim()
        val isPublicAir = scope == VibeGroup.SCOPE_PUBLIC
        
        val groupId = if (isPublicAir) {
            if (normalized == "AIR" || normalized == "THE AIR") VibeGroup.ID_AIR else "air_${normalized.replace(" ", "_")}"
        } else {
            UUID.randomUUID().toString()
        }

        val parentAirId = if (scope == VibeGroup.SCOPE_PRIVATE) _currentTieId.value else null
        
        viewModelScope.launch {
            val existing = vibeStore.getGroup(groupId)
            if (isPublicAir) {
                // PUBLIC AIR: Join existing or create a shared entry. No one owns it.
                if (existing == null) {
                    vibeStore.insertGroup(VibeGroup(id = groupId, name = name, scope = VibeGroup.SCOPE_PUBLIC))
                } else {
                    vibeStore.updateGroupLastVibe(groupId, System.currentTimeMillis())
                }
            } else {
                // PRIVATE TIE: Anchored to a parent Air.
                p2pController.startGroupVibe(name, targetMembers, scope)
                vibeStore.getGroup(groupId)?.let { tie ->
                    vibeStore.insertGroup(tie.copy(parentAirId = parentAirId))
                }
            }
        }
        
        if (members == null) _selectedDevices.value = emptySet()
        return groupId
    }

    fun enterTie(groupId: String) {
        _currentTieId.value = groupId
    }

    fun addMemberToGroup(groupId: String, deviceId: String) {
        val group = state.value.session.groups.find { it.id == groupId } ?: return
        p2pController.updateGroupMembers(groupId, group.memberIds + deviceId)
    }

    fun updateGroupScope(groupId: String, scope: Int) {
        p2pController.updateGroupScope(groupId, scope)
    }

    fun removeMemberFromGroup(groupId: String, deviceId: String) {
        val group = state.value.session.groups.find { it.id == groupId } ?: return
        if (deviceId == repository.getDeviceId()) return
        p2pController.updateGroupMembers(groupId, group.memberIds - deviceId)
    }

    fun denyLink(device: P2PDevice) {
        p2pController.denyLink(device)
    }

    fun broadcastIdentityUpdate(oldName: String) {
        viewModelScope.launch {
            p2pController.broadcastIdentityUpdate(oldName)
        }
    }

    fun sendMessage(message: String, groupId: String? = null) {
        viewModelScope.launch {
            val targetGid = groupId ?: _currentTieId.value
            p2pController.sendGroupMessage(message, targetGid)
        }
    }

    fun spreadVibe(message: String) {
        viewModelScope.launch {
            val myId = repository.getDeviceId()
            val activeTieId = _currentTieId.value
            val activeTie = vibeStore.getGroup(activeTieId)

            val existingLocalVibe: MessagePayload? = state.value.session.messages.findLast { 
                it.senderId == myId && it.content == message && it.vibeType == MessagePayload.VIBE_SILENCE 
            }
            
            val isAirActive = state.value.harmony.isBluetoothEnabled && state.value.harmony.permissionsGranted
            val isTieContext = activeTie?.scope == VibeGroup.SCOPE_PRIVATE
            
            val effectiveVibeType = when {
                !isAirActive -> MessagePayload.VIBE_SILENCE
                isTieContext -> MessagePayload.VIBE_WHISPER
                else -> MessagePayload.VIBE_SHOUT
            }

            when (effectiveVibeType) {
                MessagePayload.VIBE_SILENCE -> {
                    p2pController.sendMessage(message, null, MessagePayload.VIBE_SILENCE, existingLocalVibe?.messageId, groupId = VibeGroup.ID_SILENCE, groupName = "SILENCE")
                }
                MessagePayload.VIBE_SHOUT -> {
                    p2pController.broadcastMessage(message, MessagePayload.VIBE_SHOUT, existingLocalVibe?.messageId, groupId = activeTieId, groupName = activeTie?.name)
                }
                MessagePayload.VIBE_WHISPER -> {
                    p2pController.sendGroupMessage(message, activeTieId)
                }
            }
        }
    }

    fun spreadFile(uri: android.net.Uri, vibeType: Int = MessagePayload.VIBE_SILENCE) {
        viewModelScope.launch {
            val activeTieId = _currentTieId.value
            val activeTie = vibeStore.getGroup(activeTieId)

            when (vibeType) {
                MessagePayload.VIBE_SILENCE -> {
                    p2pController.sendFile(uri, null, MessagePayload.VIBE_SILENCE, groupId = VibeGroup.ID_SILENCE, groupName = "SILENCE")
                }
                MessagePayload.VIBE_SHOUT -> {
                    p2pController.sendFile(uri, null, MessagePayload.VIBE_SHOUT, groupId = activeTieId, groupName = activeTie?.name)
                }
                MessagePayload.VIBE_WHISPER -> {
                    val targets = state.value.crowd.selectedDevices.ifEmpty { state.value.session.connectedLinks }
                    if (targets.isNotEmpty()) {
                        targets.forEach { targetId ->
                            p2pController.sendFile(uri, targetId, vibeType, groupId = activeTieId, groupName = activeTie?.name)
                        }
                    } else {
                        p2pController.sendFile(uri, null, vibeType, groupId = activeTieId, groupName = activeTie?.name)
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

    fun initiateHistorySync(deviceId: String) {
        p2pController.initiateHistorySync(deviceId)
    }

    fun restoreFromVault(groupId: String) {
        vibeStore.restoreFromVault(groupId)
    }

    fun pinVibe(groupId: String, messageId: String) {
        viewModelScope.launch { vibeStore.pinVibe(groupId, messageId) }
    }

    fun unpinVibe(groupId: String, messageId: String) {
        viewModelScope.launch { vibeStore.unpinVibe(groupId, messageId) }
    }

    fun updateProjection(groupId: String, emoji: String?) {
        viewModelScope.launch { vibeStore.updateGroupProjection(groupId, emoji) }
    }

    fun addSchedule(groupId: String, schedule: cc.thevar.blukit.domain.model.AirSchedule) {
        viewModelScope.launch { vibeStore.addAirSchedule(groupId, schedule) }
    }

    override fun onCleared() {
        p2pController.release()
    }
}
