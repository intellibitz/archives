package cc.thevar.blukit.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cc.thevar.blukit.network.p2p.P2PController
import cc.thevar.blukit.domain.model.MessagePayload
import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.domain.model.Resonance
import cc.thevar.blukit.domain.model.ConnectionStatus
import cc.thevar.blukit.data.system.RadioStateManager
import cc.thevar.blukit.data.system.SpreadPermissionManager
import cc.thevar.blukit.data.repository.IdentityRepository
import cc.thevar.blukit.data.local.PulseStore
import cc.thevar.blukit.domain.usecase.ConnectivityUseCase
import cc.thevar.blukit.ui.toUiError
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.launch

/**
 * ViewModel responsible for managing connectivity within The Crowd and UI states.
 * Coordinates between the P2PController and RadioStateManager to provide
 * a reactive stream of Bluetooth and Pulsing Crowd statuses.
 */

class BluetoothViewModel(
    private val p2pController: P2PController,
    private val radioStateManager: RadioStateManager,
    private val repository: IdentityRepository,
    private val permissionManager: SpreadPermissionManager,
    private val pulseStore: PulseStore,
    private val connectivityUseCase: ConnectivityUseCase,
) : ViewModel() {

    private val _selectedDevices = MutableStateFlow<Set<String>>(emptySet())
    private val _energySurge = MutableStateFlow(0f)
    val energySurge = _energySurge.asStateFlow()

    private val _currentChainId = MutableStateFlow(Resonance.ID_CROWD)
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
                pulseStore.pruneMedia(thresholdMs = 90L * 24 * 60 * 60 * 1000) // 90 days
                pulseStore.autoArchiveCrowds() // PROTOCOL: Archive after 30 days inactivity
                delay(24L * 60 * 60 * 1000)
            }
        }

        // SECURITY: Auto-Reconnect for secure Chains
        combine(
            p2pController.connectedTies,
            p2pController.scannedDevices
        ) { connected, scanned -> connected to scanned }
            .debounce(2000)
            .onEach { (connected, scanned) ->
                val currentChain = pulseStore.getGroup(_currentChainId.value)
                if (currentChain?.scope == Resonance.SCOPE_PRIVATE) {
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

        val scheduledCrowds = pulseStore.groups.value.filter { it.schedules.isNotEmpty() }
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
        val silentPulses = messages.filter { 
            it.senderId == myId && it.pulseType == MessagePayload.PULSE_SILENCE 
        }
        
        silentPulses.forEach { pulse ->
            // Promote to THE CROWD or last active tie? Default to THE CROWD.
            p2pController.broadcastMessage(pulse.content, MessagePayload.PULSE_SHOUT, pulse.messageId, groupId = Resonance.ID_CROWD, groupName = "THE CROWD")
        }
    }

    private fun triggerEnergySurge() {
        viewModelScope.launch {
            _energySurge.value = 1f
            delay(100.milliseconds)
            _energySurge.value = 0f
        }
    }

    private val activityState: Flow<EventActivity> = combine(
        p2pController.isDiscovering,
        p2pController.isAdvertising,
        p2pController.messages,
        p2pController.errors
    ) { isDiscovering, isAdvertising, messages, error ->
        val now = System.currentTimeMillis()
        val recentPulses = messages.count { (now - it.timestamp) < 300000 } // last 5 mins
        val intensity = (recentPulses / 20f).coerceAtMost(1f)

        EventActivity(
            isDiscovering = isDiscovering,
            isAdvertising = isAdvertising,
            energyIntensity = intensity,
            uiError = error?.toUiError()
        )
    }

    private val crowdState: Flow<MeshCrowd> = combine(
        p2pController.scannedDevices,
        _selectedDevices,
        repository.pulsedPeers,
        repository.blockedUsers,
        p2pController.incomingRadioRequests,
        p2pController.outgoingRadioRequests
    ) { flows: Array<out Any?> ->
        val scanned = flows[0] as List<P2PDevice>
        val selected = flows[1] as Set<String>
        val pulsed = flows[2] as Set<String>
        val blocked = flows[3] as Set<String>
        val incoming = flows[4] as Set<P2PDevice>
        val outgoing = flows[5] as Set<P2PDevice>
        
        MeshCrowd(
            scannedDevices = scanned,
            selectedDevices = selected,
            pulsedPeers = pulsed,
            blockedUsers = blocked,
            incomingRadioRequests = incoming,
            outgoingRadioRequests = outgoing
        )
    }

    private val sessionDataState: Flow<PulseSession> = combine(
        p2pController.connectedTies,
        p2pController.messages,
        pulseStore.activeGroups,
        pulseStore.archivedGroups,
        p2pController.syncProgress
    ) { ties, messages, groups, archivedGroups, syncProgress ->
        PulseSession(
            connectedTies = ties,
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
                val pulse = crowd.scannedDevices.find { it.id in session.connectedTies }
                    ?: P2PDevice(id = session.connectedTies.firstOrNull() ?: "", name = "?", emoji = "👤")
                RadioConnectionState.Connected(pulse)
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
                
                connectivityUseCase.connectToDevice(device, state.value.session.connectedTies)
                
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

    fun startGroupPulse(name: String, members: Set<String>? = null, scope: Int = Resonance.SCOPE_PRIVATE, templateId: String? = null): String {
        if (name.isBlank()) return ""
        
        val targetMembers = members ?: _selectedDevices.value
        if (scope != Resonance.SCOPE_PUBLIC && targetMembers.isEmpty()) {
            Log.w("BluetoothViewModel", "Cannot start private group with empty member set")
            return ""
        }

        val isPublicAir = scope == Resonance.SCOPE_PUBLIC
        
        val currentGroup = state.value.session.groups.find { it.id == _currentChainId.value }
        val groupId = Resonance.generateId(name, scope, currentGroup)
        val parentId = _currentChainId.value
        
        viewModelScope.launch {
            val existing = pulseStore.getGroup(groupId)
            if (isPublicAir) {
                // PUBLIC CROWD: Join existing or create a shared entry. No one owns it.
                if (existing == null) {
                    val template = cc.thevar.blukit.domain.model.CrowdTemplates.ALL.find { it.id == templateId }
                    val newGroup = Resonance(
                        id = groupId, 
                        name = name, 
                        scope = Resonance.SCOPE_PUBLIC,
                        parentId = parentId,
                        templateId = templateId
                    )
                    pulseStore.insertGroup(newGroup)
                    
                    // Automatically spawn default chains if template exists
                    template?.defaultChains?.forEach { chainName ->
                        val chainId = Resonance.generateId(chainName, Resonance.SCOPE_PRIVATE, newGroup)
                        pulseStore.insertGroup(Resonance(
                            id = chainId,
                            name = chainName,
                            scope = Resonance.SCOPE_PRIVATE,
                            parentId = groupId
                        ))
                    }
                } else {
                    pulseStore.updateGroupLastPulse(groupId, System.currentTimeMillis())
                }
            } else {
                // PRIVATE CHAIN: Anchored to a parent Crowd.
                p2pController.startGroupPulse(name, targetMembers, scope, groupId = groupId, parentId = parentId)
                delay(100) 
                pulseStore.getGroup(groupId)?.let { tie ->
                    pulseStore.insertGroup(tie.copy(parentId = parentId))
                }
            }
        }
        
        if (members == null) _selectedDevices.value = emptySet()
        return groupId
    }

    fun connectCrowds(sourceId: String, targetId: String, bridge: cc.thevar.blukit.domain.model.ConnectionBridge = cc.thevar.blukit.domain.model.ConnectionBridge.PEER_TO_PEER) {
        viewModelScope.launch {
            pulseStore.addCrowdConnection(cc.thevar.blukit.domain.model.CrowdConnection(sourceId, targetId, bridge))
        }
    }

    fun assignRole(groupId: String, userId: String, role: String) {
        viewModelScope.launch {
            pulseStore.assignUserRole(groupId, userId, role)
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

    fun spreadPulse(message: String) {
        if (message.isBlank()) return

        viewModelScope.launch {
            val myId = repository.getDeviceId()
            val activeChainId = _currentChainId.value
            val activeChain = pulseStore.getGroup(activeChainId)

            val existingLocalPulse: MessagePayload? = state.value.session.messages.findLast { 
                it.senderId == myId && it.content == message && it.pulseType == MessagePayload.PULSE_SILENCE 
            }
            
            val isRadiosActive = state.value.harmony.isBluetoothEnabled && state.value.harmony.permissionsGranted
            val isChainContext = activeChain?.scope == Resonance.SCOPE_PRIVATE
            
            when {
                !isRadiosActive -> {
                    p2pController.sendMessage(
                        message, 
                        null, 
                        MessagePayload.PULSE_SILENCE, 
                        existingLocalPulse?.messageId, 
                        groupId = Resonance.ID_SILENCE, 
                        groupName = "SILENCE"
                    )
                }
                isChainContext -> {
                    p2pController.sendGroupMessage(message, activeChainId)
                }
                else -> {
                    p2pController.broadcastMessage(
                        message, 
                        MessagePayload.PULSE_SHOUT, 
                        existingLocalPulse?.messageId, 
                        groupId = activeChainId, 
                        groupName = activeChain?.name
                    )
                }
            }
        }
    }

    fun spreadFile(uri: android.net.Uri, pulseType: Int = MessagePayload.PULSE_SILENCE) {
        viewModelScope.launch {
            val activeChainId = _currentChainId.value
            val activeChain = pulseStore.getGroup(activeChainId)

            when (pulseType) {
                MessagePayload.PULSE_SILENCE -> {
                    p2pController.sendFile(uri, null, MessagePayload.PULSE_SILENCE, groupId = Resonance.ID_SILENCE, groupName = "SILENCE")
                }
                MessagePayload.PULSE_SHOUT -> {
                    p2pController.sendFile(uri, null, MessagePayload.PULSE_SHOUT, groupId = activeChainId, groupName = activeChain?.name)
                }
                MessagePayload.PULSE_WHISPER -> {
                    val targets = state.value.crowd.selectedDevices.ifEmpty { state.value.session.connectedTies }
                    if (targets.isNotEmpty()) {
                        targets.forEach { targetId ->
                            p2pController.sendFile(uri, targetId, pulseType, groupId = activeChainId, groupName = activeChain?.name)
                        }
                    } else {
                        p2pController.sendFile(uri, null, pulseType, groupId = activeChainId, groupName = activeChain?.name)
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
            pulseStore.deleteGroup(groupId)
        }
    }

    fun vaultGroup(groupId: String, isVaulted: Boolean) {
        pulseStore.vaultGroup(groupId, isVaulted)
    }

    fun seniorVaultGroup(groupId: String, isSeniorVault: Boolean) {
        pulseStore.seniorVaultGroup(groupId, isSeniorVault)
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
        pulseStore.restoreFromVault(groupId)
    }

    fun pinPulse(groupId: String, messageId: String) {
        viewModelScope.launch { pulseStore.pinPulse(groupId, messageId) }
    }

    fun unpinPulse(groupId: String, messageId: String) {
        viewModelScope.launch { pulseStore.unpinPulse(groupId, messageId) }
    }

    fun updateProjection(groupId: String, emoji: String?) {
        viewModelScope.launch { pulseStore.updateGroupProjection(groupId, emoji) }
    }

    fun addSchedule(groupId: String, schedule: cc.thevar.blukit.domain.model.CrowdSchedule) {
        viewModelScope.launch { pulseStore.addCrowdSchedule(groupId, schedule) }
    }

    fun pushRitual(groupId: String, schedule: cc.thevar.blukit.domain.model.CrowdSchedule) {
        viewModelScope.launch {
            val content = kotlinx.serialization.json.Json.encodeToString(schedule)
            val members = pulseStore.getGroup(groupId)?.allMemberIds ?: emptySet()
            members.forEach { memberId ->
                p2pController.sendMessage(
                    content = content,
                    receiverId = memberId,
                    pulseType = MessagePayload.PULSE_WHISPER,
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
