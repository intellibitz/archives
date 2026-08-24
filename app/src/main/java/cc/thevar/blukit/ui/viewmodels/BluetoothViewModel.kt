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

/**
 * ViewModel responsible for managing connectivity within The Crowd and UI states.
 * Coordinates between the P2PController and RadioStateManager to provide
 * a reactive stream of Bluetooth and Vibing Crowd statuses.
 */
@OptIn(kotlinx.coroutines.FlowPreview::class)
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

    private val _currentChainId = MutableStateFlow(VibeGroup.ID_CROWD)
    val currentChainId = _currentChainId.asStateFlow()

    val discoveredCrowds = p2pController.discoveredCrowds
        .filter { it.id != _currentChainId.value }
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
        // Observe messages to trigger energy surges and handle protocols
        p2pController.messages
            .onEach { msgs ->
                if (msgs.isNotEmpty()) {
                    triggerEnergySurge()
                    // PROTOCOL: Collaborative Rituals
                    msgs.filter { it.type == MessagePayload.TYPE_RITUAL_PUSH }.forEach { ritualMsg ->
                        try {
                            val schedule = kotlinx.serialization.json.Json.decodeFromString<cc.thevar.blukit.domain.model.CrowdSchedule>(ritualMsg.content)
                            ritualMsg.groupId?.let { gid -> addSchedule(gid, schedule) }
                        } catch (e: Exception) { Log.e("BluetoothViewModel", "Failed to decode ritual push") }
                    }
                }
            }
            .launchIn(viewModelScope)

        // CROWD AWAKENING: Automatically promote silence to shout when radios engage
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
                checkCrowdSchedules()
                delay(60 * 1000)
            }
        }

        // STORAGE PRUNING: Daily cleanup
        viewModelScope.launch {
            while (true) {
                vibeStore.pruneMedia(thresholdMs = 90L * 24 * 60 * 60 * 1000) // 90 days
                vibeStore.autoArchiveCrowds() // PROTOCOL: Archive after 30 days inactivity
                delay(24L * 60 * 60 * 1000)
            }
        }

        // SECURITY: Auto-Reconnect for secure Chains
        combine(
            p2pController.connectedRadios,
            p2pController.scannedDevices
        ) { connected, scanned -> connected to scanned }
            .debounce(2000)
            .onEach { (connected, scanned) ->
                val currentChain = vibeStore.getGroup(_currentChainId.value)
                if (currentChain?.scope == VibeGroup.SCOPE_PRIVATE) {
                    val missingMembers = currentChain.memberIds - connected - repository.getDeviceId()
                    missingMembers.forEach { memberId ->
                        scanned.find { it.id == memberId || it.persistentId == memberId }?.let { device ->
                            Log.i("BluetoothViewModel", "Auto-Reconnect: Secure Chain needs resonance with $memberId")
                            connectToDevice(device)
                        }
                    }
                }
            }.launchIn(viewModelScope)
    }

    private fun checkCrowdSchedules() {
        val now = java.util.Calendar.getInstance()
        val day = now.get(java.util.Calendar.DAY_OF_WEEK)
        val hour = now.get(java.util.Calendar.HOUR_OF_DAY)
        val minute = now.get(java.util.Calendar.MINUTE)

        val scheduledCrowds = vibeStore.groups.value.filter { it.schedules.isNotEmpty() }
        scheduledCrowds.forEach { crowd ->
            crowd.schedules.forEach { s ->
                val isActive = s.dayOfWeek == day && 
                    (hour > s.startHour || (hour == s.startHour && minute >= s.startMinute)) &&
                    (hour < s.endHour || (hour == s.endHour && minute <= s.endMinute))
                
                if (isActive && crowd.isArchived) {
                    restoreFromVault(crowd.id)
                    Log.i("BluetoothViewModel", "Ritual Awakening: ${crowd.name}")
                }
                
                // Smart Reminder Logic
                s.reminderLeadTimeMs?.let { leadTime ->
                    val scheduleTime = java.util.Calendar.getInstance().apply {
                        set(java.util.Calendar.DAY_OF_WEEK, s.dayOfWeek)
                        set(java.util.Calendar.HOUR_OF_DAY, s.startHour)
                        set(java.util.Calendar.MINUTE, s.startMinute)
                    }.timeInMillis
                    
                    val nowMs = System.currentTimeMillis()
                    if (scheduleTime - nowMs in (leadTime - 60000)..leadTime) {
                        Log.i("BluetoothViewModel", "SMART REMINDER: ${s.title ?: "Event"} starting in ${leadTime / 60000} mins")
                        // Trigger local notification here in a real impl
                    }
                }
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
            // Promote to THE CROWD or last active tie? Default to THE CROWD.
            p2pController.broadcastMessage(vibe.content, MessagePayload.VIBE_SHOUT, vibe.messageId, groupId = VibeGroup.ID_CROWD, groupName = "THE CROWD")
        }
    }

    private fun triggerEnergySurge() {
        viewModelScope.launch {
            _energySurge.value = 1f
            delay(100.milliseconds)
            _energySurge.value = 0f
        }
    }

    private val activityState: Flow<HostActivity> = combine(
        p2pController.isDiscovering,
        p2pController.isAdvertising,
        p2pController.messages,
        p2pController.errors
    ) { isDiscovering, isAdvertising, messages, error ->
        val now = System.currentTimeMillis()
        val recentVibes = messages.count { (now - it.timestamp) < 300000 } // last 5 mins
        val intensity = (recentVibes / 20f).coerceAtMost(1f)

        HostActivity(
            isDiscovering = isDiscovering,
            isAdvertising = isAdvertising,
            energyIntensity = intensity,
            uiError = error?.toUiError()
        )
    }

    private val crowdState: Flow<MeshCrowd> = combine(
        p2pController.scannedDevices,
        _selectedDevices,
        repository.vibedPeers,
        repository.blockedUsers,
        p2pController.incomingRadioRequests,
        p2pController.outgoingRadioRequests
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
            incomingRadioRequests = incoming,
            outgoingRadioRequests = outgoing
        )
    }

    private val sessionDataState: Flow<VibeSession> = combine(
        p2pController.connectedRadios,
        p2pController.messages,
        vibeStore.activeGroups,
        vibeStore.archivedGroups,
        p2pController.syncProgress
    ) { links, messages, groups, archivedGroups, syncProgress ->
        VibeSession(
            connectedRadios = links,
            messages = messages,
            groups = groups,
            archivedGroups = archivedGroups,
            syncProgress = syncProgress
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
            ConnectionStatus.Connecting -> RadioConnectionState.Connecting
            is ConnectionStatus.Error -> RadioConnectionState.Error(manualStatus.message)
            else -> null
        }

        val connectionState = when {
            manualConnectionState != null -> manualConnectionState
            activity.uiError != null -> RadioConnectionState.Error(activity.uiError.message)
            isConnected -> {
                val vibe = crowd.scannedDevices.find { it.id in session.connectedRadios }
                    ?: P2PDevice(id = session.connectedRadios.firstOrNull() ?: "", name = "?", emoji = "👤")
                RadioConnectionState.Connected(vibe)
            }
            activity.isDiscovering || activity.isAdvertising -> RadioConnectionState.Scanning
            else -> RadioConnectionState.Disconnected
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

    fun connectToDevice(device: P2PDevice, retryCount: Int = 3) {
        viewModelScope.launch {
            var currentTry = 0
            var success = false
            while (!success && currentTry < retryCount) {
                currentTry++
                Log.i("BluetoothViewModel", "Connection attempt $currentTry for ${device.name}")
                
                connectivityUseCase.connectToDevice(device, state.value.session.connectedRadios)
                
                // Wait for status update
                val status = connectivityUseCase.manualConnectionStatus
                    .filter { it !is ConnectionStatus.Connecting }
                    .first()
                
                if (status is ConnectionStatus.Connected) {
                    success = true
                    Log.i("BluetoothViewModel", "Connected to ${device.name} after $currentTry attempts")
                } else if (status is ConnectionStatus.Error) {
                    Log.e("BluetoothViewModel", "Attempt $currentTry failed: ${status.message}")
                    if (currentTry < retryCount) delay(2000L * currentTry) // Exponential backoff
                }
            }
        }
    }

    fun requestWhisper(device: P2PDevice) {
        connectToDevice(device)
    }

    fun acceptRadio(device: P2PDevice) {
        p2pController.acceptRadio(device)
    }

    fun toggleDeviceSelection(deviceId: String) {
        _selectedDevices.update { 
            if (it.contains(deviceId)) it - deviceId else it + deviceId
        }
    }

    fun clearSelection() {
        _selectedDevices.value = emptySet()
    }

    fun startGroupVibe(name: String, members: Set<String>? = null, scope: Int = VibeGroup.SCOPE_PRIVATE, templateId: String? = null): String {
        if (name.isBlank()) return ""
        
        val targetMembers = members ?: _selectedDevices.value
        if (scope != VibeGroup.SCOPE_PUBLIC && targetMembers.isEmpty()) {
            Log.w("BluetoothViewModel", "Cannot start private group with empty member set")
            return ""
        }

        val isPublicAir = scope == VibeGroup.SCOPE_PUBLIC
        
        val currentGroup = state.value.session.groups.find { it.id == _currentChainId.value }
        val groupId = VibeGroup.generateId(name, scope, currentGroup)
        val parentId = _currentChainId.value
        
        viewModelScope.launch {
            val existing = vibeStore.getGroup(groupId)
            if (isPublicAir) {
                // PUBLIC CROWD: Join existing or create a shared entry. No one owns it.
                if (existing == null) {
                    val template = cc.thevar.blukit.domain.model.CrowdTemplates.ALL.find { it.id == templateId }
                    val newGroup = VibeGroup(
                        id = groupId, 
                        name = name, 
                        scope = VibeGroup.SCOPE_PUBLIC,
                        parentId = parentId,
                        templateId = templateId
                    )
                    vibeStore.insertGroup(newGroup)
                    
                    // Automatically spawn default chains if template exists
                    template?.defaultChains?.forEach { chainName ->
                        val chainId = VibeGroup.generateId(chainName, VibeGroup.SCOPE_PRIVATE, newGroup)
                        vibeStore.insertGroup(VibeGroup(
                            id = chainId,
                            name = chainName,
                            scope = VibeGroup.SCOPE_PRIVATE,
                            parentId = groupId
                        ))
                    }
                } else {
                    vibeStore.updateGroupLastVibe(groupId, System.currentTimeMillis())
                }
            } else {
                // PRIVATE CHAIN: Anchored to a parent Crowd.
                p2pController.startGroupVibe(name, targetMembers, scope, groupId = groupId, parentId = parentId)
                delay(100) 
                vibeStore.getGroup(groupId)?.let { tie ->
                    vibeStore.insertGroup(tie.copy(parentId = parentId))
                }
            }
        }
        
        if (members == null) _selectedDevices.value = emptySet()
        return groupId
    }

    fun connectCrowds(sourceId: String, targetId: String, bridge: cc.thevar.blukit.domain.model.ConnectionBridge = cc.thevar.blukit.domain.model.ConnectionBridge.PEER_TO_PEER) {
        viewModelScope.launch {
            vibeStore.addCrowdConnection(cc.thevar.blukit.domain.model.CrowdConnection(sourceId, targetId, bridge))
        }
    }

    fun assignRole(groupId: String, userId: String, role: String) {
        viewModelScope.launch {
            vibeStore.assignUserRole(groupId, userId, role)
        }
    }

    fun enterChain(groupId: String) {
        _currentChainId.value = groupId
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

    fun denyRadio(device: P2PDevice) {
        p2pController.denyRadio(device)
    }

    fun broadcastIdentityUpdate(oldName: String) {
        viewModelScope.launch {
            p2pController.broadcastIdentityUpdate(oldName)
        }
    }

    fun sendMessage(message: String, groupId: String? = null) {
        if (message.isBlank()) return
        
        viewModelScope.launch {
            val targetGid = groupId ?: _currentChainId.value
            p2pController.sendGroupMessage(message, targetGid)
        }
    }

    fun spreadVibe(message: String) {
        if (message.isBlank()) return

        viewModelScope.launch {
            val myId = repository.getDeviceId()
            val activeChainId = _currentChainId.value
            val activeChain = vibeStore.getGroup(activeChainId)

            val existingLocalVibe: MessagePayload? = state.value.session.messages.findLast { 
                it.senderId == myId && it.content == message && it.vibeType == MessagePayload.VIBE_SILENCE 
            }
            
            val isRadiosActive = state.value.harmony.isBluetoothEnabled && state.value.harmony.permissionsGranted
            val isChainContext = activeChain?.scope == VibeGroup.SCOPE_PRIVATE
            
            when {
                !isRadiosActive -> {
                    p2pController.sendMessage(
                        message, 
                        null, 
                        MessagePayload.VIBE_SILENCE, 
                        existingLocalVibe?.messageId, 
                        groupId = VibeGroup.ID_SILENCE, 
                        groupName = "SILENCE"
                    )
                }
                isChainContext -> {
                    p2pController.sendGroupMessage(message, activeChainId)
                }
                else -> {
                    p2pController.broadcastMessage(
                        message, 
                        MessagePayload.VIBE_SHOUT, 
                        existingLocalVibe?.messageId, 
                        groupId = activeChainId, 
                        groupName = activeChain?.name
                    )
                }
            }
        }
    }

    fun spreadFile(uri: android.net.Uri, vibeType: Int = MessagePayload.VIBE_SILENCE) {
        viewModelScope.launch {
            val activeChainId = _currentChainId.value
            val activeChain = vibeStore.getGroup(activeChainId)

            when (vibeType) {
                MessagePayload.VIBE_SILENCE -> {
                    p2pController.sendFile(uri, null, MessagePayload.VIBE_SILENCE, groupId = VibeGroup.ID_SILENCE, groupName = "SILENCE")
                }
                MessagePayload.VIBE_SHOUT -> {
                    p2pController.sendFile(uri, null, MessagePayload.VIBE_SHOUT, groupId = activeChainId, groupName = activeChain?.name)
                }
                MessagePayload.VIBE_WHISPER -> {
                    val targets = state.value.crowd.selectedDevices.ifEmpty { state.value.session.connectedRadios }
                    if (targets.isNotEmpty()) {
                        targets.forEach { targetId ->
                            p2pController.sendFile(uri, targetId, vibeType, groupId = activeChainId, groupName = activeChain?.name)
                        }
                    } else {
                        p2pController.sendFile(uri, null, vibeType, groupId = activeChainId, groupName = activeChain?.name)
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

    fun vaultGroup(groupId: String, isVaulted: Boolean) {
        vibeStore.vaultGroup(groupId, isVaulted)
    }

    fun seniorVaultGroup(groupId: String, isSeniorVault: Boolean) {
        vibeStore.seniorVaultGroup(groupId, isSeniorVault)
    }

    fun updateNote(groupId: String, content: String, messageId: String?, version: Int) {
        viewModelScope.launch {
            p2pController.sendNoteUpdate(groupId, content, messageId, version)
        }
    }

    fun initiateHistorySync(deviceId: String, sinceTimestamp: Long? = null) {
        p2pController.initiateHistorySync(deviceId, sinceTimestamp)
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

    fun addSchedule(groupId: String, schedule: cc.thevar.blukit.domain.model.CrowdSchedule) {
        viewModelScope.launch { vibeStore.addCrowdSchedule(groupId, schedule) }
    }

    fun pushRitual(groupId: String, schedule: cc.thevar.blukit.domain.model.CrowdSchedule) {
        viewModelScope.launch {
            val content = kotlinx.serialization.json.Json.encodeToString(schedule)
            val members = vibeStore.getGroup(groupId)?.allMemberIds ?: emptySet()
            members.forEach { memberId ->
                p2pController.sendMessage(
                    content = content,
                    receiverId = memberId,
                    vibeType = MessagePayload.VIBE_WHISPER,
                    groupId = groupId
                )?.let { 
                    // Ritual push logic
                }
            }
        }
    }

    override fun onCleared() {
        p2pController.release()
    }
}
