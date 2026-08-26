/**
 * BLUKIT VIEWMODEL: BLUETOOTH ORCHESTRATOR
 *
 * The central intelligence hub for Blukit's reactive UDF (Unidirectional Data Flow) architecture.
 * Coordinates between hardware radio states, secure P2P engines, and local resonance storage.
 * 
 * Responsibilities:
 * - Hardware Harmony: Monitoring Bluetooth/Location availability and permissions.
 * - Crowd AI Orchestration: Integrating ambient intelligence for resonance synthesis.
 * - Crowd Awakening: Automatically promoting local pulses to the mesh when radios engage.
 * - Ritual Sentience: Executing scheduled context activations and smart reminders.
 * - Storage Pruning: Orchestrating the "Pulse Decay" protocol for media and history.
 * - Reactive State: Aggregating multiple data sources into a unified `BluetoothUiState`.
 */
package cc.thevar.blukit.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cc.thevar.blukit.data.local.PulseStore
import cc.thevar.blukit.data.repository.IdentityRepository
import cc.thevar.blukit.data.system.RadioStateManager
import cc.thevar.blukit.data.system.SpreadPermissionManager
import cc.thevar.blukit.domain.logic.IntelligenceManager
import cc.thevar.blukit.domain.model.ConnectionStatus
import cc.thevar.blukit.domain.model.MessagePayload
import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.domain.model.Resonance
import cc.thevar.blukit.domain.usecase.ConnectivityUseCase
import cc.thevar.blukit.network.p2p.P2PController
import cc.thevar.blukit.ui.toUiError
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * Manages mesh connectivity, group orchestration, and interaction hub states.
 */
class BluetoothViewModel(
    private val p2pController: P2PController,
    private val radioStateManager: RadioStateManager,
    private val repository: IdentityRepository,
    private val permissionManager: SpreadPermissionManager,
    private val pulseStore: PulseStore,
    private val connectivityUseCase: ConnectivityUseCase,
    private val intelligenceManager: IntelligenceManager,
) : ViewModel() {

    private val _selectedDevices = MutableStateFlow<Set<String>>(emptySet())

    private val _currentChainId = MutableStateFlow(Resonance.ID_CROWD)
    /** The currently focused Resonance context (Crowd or Chain ID). */
    @Suppress("unused")
    val currentChainId = _currentChainId.asStateFlow()

    /** Public frequencies discovered in the local air, excluding current focus. */
    val discoveredCrowds = p2pController.discoveredCrowds
        .filter { it.id != _currentChainId.value }
        .shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000))

    /** CROWD CANVAS: Reactive flow of high-resonance pulses for the spatial header. */
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val highResonancePulses: StateFlow<List<MessagePayload>> = _currentChainId
        .flatMapLatest { groupId ->
            pulseStore.getHighResonancePulses(groupId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Merged status of hardware radios and runtime permissions. */
    private val harmonyState: Flow<HardwareHarmony> = combine(
        radioStateManager.radioStates,
        permissionManager.permissionsGranted,
    ) { radioStates, permissionsGranted ->
        HardwareHarmony(
            isBluetoothEnabled = radioStates.isBluetoothEnabled,
            isLocationEnabled = radioStates.isLocationEnabled,
            isWifiEnabled = radioStates.isWifiEnabled,
            permissionsGranted = permissionsGranted,
        )
    }

    init {
        // --- Mesh Signal Observation ---
        p2pController.messages
            .onEach { msgs ->
                if (msgs.isNotEmpty()) {
                    // PROTOCOL: Collaborative Rituals - Extract schedules from incoming pushes
                    msgs.filter { it.type == MessagePayload.TYPE_RITUAL_PUSH }.forEach { ritualMsg ->
                        try {
                            val schedule = kotlinx.serialization.json.Json.decodeFromString<cc.thevar.blukit.domain.model.CrowdSchedule>(ritualMsg.content)
                            ritualMsg.groupId?.let { gid -> addSchedule(gid, schedule) }
                        } catch (e: Exception) {
                            Log.e("BluetoothViewModel", "Failed to decode ritual push: ${e.message}")
                        }
                    }
                }
            }
            .launchIn(viewModelScope)

        // --- Crowd Awakening Protocol ---
        combine(
            radioStateManager.radioStates,
            permissionManager.permissionsGranted,
        ) { radios, granted -> 
            radios.isBluetoothEnabled && granted 
        }
        .distinctUntilChanged()
        .filter { it } 
        .onEach { promoteSilenceToShout() }
        .launchIn(viewModelScope)

        // --- Ritual Sentience (Schedules) ---
        viewModelScope.launch {
            while (true) {
                checkCrowdSchedules()
                delay(1.minutes)
            }
        }

        // --- Storage Pruning & Pulse Decay ---
        viewModelScope.launch {
            while (true) {
                pulseStore.pruneMedia(thresholdMs = 90.days.inWholeMilliseconds) // Protocol: 90-day media decay
                pulseStore.autoArchiveCrowds() // Protocol: 30-day context archiving
                delay(1.days)
            }
        }

        // --- Secure Chain Self-Healing ---
        @OptIn(FlowPreview::class)
        combine(
            p2pController.connectedTies,
            p2pController.scannedDevices,
        ) { connected, scanned -> connected to scanned }
            .debounce(2.seconds)
            .onEach { (connected, scanned) ->
                val currentChain = pulseStore.getGroup(_currentChainId.value)
                if (currentChain?.scope == Resonance.SCOPE_PRIVATE) {
                    val missingMembers = currentChain.memberIds - connected - repository.getDeviceId()
                    missingMembers.forEach { memberId ->
                        scanned.find { (it.id == memberId) || (it.persistentId == memberId) }?.let { device ->
                            Log.i("BluetoothViewModel", "Auto-Reconnect: Secure Chain needs resonance with $memberId")
                            connectToDevice(device)
                        }
                    }
                }
            }.launchIn(viewModelScope)
    }

    /** Evaluates active schedules to awaken Crowds or trigger Smart Reminders. */
    private fun checkCrowdSchedules() {
        val now = Calendar.getInstance()
        val day = now[Calendar.DAY_OF_WEEK]
        val hour = now[Calendar.HOUR_OF_DAY]
        val minute = now[Calendar.MINUTE]

        val scheduledCrowds = pulseStore.groups.value.filter { it.schedules.isNotEmpty() }
        scheduledCrowds.forEach { crowd ->
            crowd.schedules.forEach { s ->
                val isActive = (s.dayOfWeek == day) &&
                        ((hour > s.startHour) || ((hour == s.startHour) && (minute >= s.startMinute))) &&
                        ((hour < s.endHour) || ((hour == s.endHour) && (minute <= s.endMinute)))

                if (isActive && crowd.isArchived) {
                    restoreFromVault(crowd.id)
                    Log.i("BluetoothViewModel", "Ritual Awakening: ${crowd.name}")
                }
                
                // Smart Reminder Logic: Notify user before ritual begins
                s.reminderLeadTimeMs?.let { leadTime ->
                    val scheduleTime = Calendar.getInstance().apply {
                        set(Calendar.DAY_OF_WEEK, s.dayOfWeek)
                        set(Calendar.HOUR_OF_DAY, s.startHour)
                        set(Calendar.MINUTE, s.startMinute)
                    }.timeInMillis
                    
                    val nowMs = System.currentTimeMillis()
                    if ((scheduleTime - nowMs) in (leadTime - 60000)..leadTime) {
                        Log.i("BluetoothViewModel", "SMART REMINDER: ${s.title ?: "Event"} starting in ${leadTime / 60000} mins")
                    }
                }
            }
        }
    }

    /** Automatically broadcasts local SILENCE pulses when the mesh radio becomes available. */
    private suspend fun promoteSilenceToShout() {
        val myId = repository.getDeviceId()
        val messages = p2pController.messages.value
        val silentPulses = messages.filter { 
            it.senderId == myId && it.pulseType == MessagePayload.PULSE_SILENCE 
        }
        
        silentPulses.forEach { pulse ->
            p2pController.broadcastMessage(pulse.content, MessagePayload.PULSE_SHOUT, pulse.messageId, groupId = Resonance.ID_CROWD, groupName = "THE CROWD")
        }
    }

    // --- State Reducers ---

    private val activityState: Flow<EventActivity> = combine(
        p2pController.isDiscovering,
        p2pController.isAdvertising,
        p2pController.messages,
        p2pController.errors,
    ) { isDiscovering, isAdvertising, messages, error ->
        val now = System.currentTimeMillis()
        val recentPulses = messages.count { (now - it.timestamp) < 300000 } // last 5 mins
        val intensity = (recentPulses / 20f).coerceAtMost(1f)

        EventActivity(
            isDiscovering = isDiscovering,
            isAdvertising = isAdvertising,
            energyIntensity = intensity,
            uiError = error?.toUiError(),
        )
    }

    private val crowdState: Flow<MeshCrowd> = combine(
        p2pController.scannedDevices,
        _selectedDevices,
        repository.pulsedPeers,
        repository.blockedUsers,
        p2pController.incomingRadioRequests,
        p2pController.outgoingRadioRequests,
    ) { flows ->
        @Suppress("UNCHECKED_CAST")
        val scanned = flows[0] as List<P2PDevice>
        @Suppress("UNCHECKED_CAST")
        val selected = flows[1] as Set<String>
        @Suppress("UNCHECKED_CAST")
        val pulsed = flows[2] as Set<String>
        @Suppress("UNCHECKED_CAST")
        val blocked = flows[3] as Set<String>
        @Suppress("UNCHECKED_CAST")
        val incoming = flows[4] as Set<P2PDevice>
        @Suppress("UNCHECKED_CAST")
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

    /** The unified life stream state for the UI layer. */
    val state: StateFlow<BluetoothUiState> = combine(
        harmonyState,
        activityState,
        crowdState,
        sessionDataState,
        combine(
            p2pController.isConnected,
            connectivityUseCase.manualConnectionStatus,
            p2pController.errors,
        ) { isConnected, manualStatus, rawError -> Triple(isConnected, manualStatus, rawError) }
    ) { harmony, activity, crowd, session, sessionExtras ->
        val (isConnected, manualStatus, _) = sessionExtras

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
            session = session.copy(connectionState = connectionState),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BluetoothUiState())

    // --- Intent Handlers ---

    fun refreshRadios() {
        radioStateManager.triggerRefresh()
        permissionManager.refresh()
    }

    /** Initiates connection with exponential backoff on failure. */
    fun connectToDevice(device: P2PDevice, retryCount: Int = 3) {
        viewModelScope.launch {
            var currentTry = 0
            var success = false
            while (!success && currentTry < retryCount) {
                currentTry++
                Log.i("BluetoothViewModel", "Connection attempt $currentTry for ${device.name}")
                
                connectivityUseCase.connectToDevice(device, state.value.session.connectedTies)
                
                val status = connectivityUseCase.manualConnectionStatus
                    .first { it !is ConnectionStatus.Connecting }
                
                if (status is ConnectionStatus.Connected) {
                    success = true
                    Log.i("BluetoothViewModel", "Connected to ${device.name}")
                } else if (status is ConnectionStatus.Error) {
                    Log.e("BluetoothViewModel", "Attempt $currentTry failed: ${status.message}")
                    if (currentTry < retryCount) {
                        delay((2000L * currentTry).milliseconds) 
                    }
                }
            }
        }
    }

    fun requestWhisper(device: P2PDevice) = connectToDevice(device)

    fun acceptRadio(device: P2PDevice) = p2pController.acceptRadio(device)

    fun toggleDeviceSelection(deviceId: String) {
        _selectedDevices.update { 
            if (it.contains(deviceId)) it - deviceId else it + deviceId
        }
    }

    fun clearSelection() { _selectedDevices.value = emptySet() }

    /** Orchestrates context formation for public Crowds or private Chains. */
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
                if (existing == null) {
                    val template = cc.thevar.blukit.domain.model.CrowdTemplates.ALL.find { it.id == templateId }
                    val newGroup = Resonance(
                        id = groupId, 
                        name = name, 
                        scope = Resonance.SCOPE_PUBLIC,
                        parentId = parentId,
                        templateId = templateId,
                        ownerId = repository.getDeviceId()
                    )
                    pulseStore.insertGroup(newGroup)
                    
                    // Template Logic: Automatically spawn default private chains
                    template?.defaultChains?.forEach { chainName ->
                        val chainId = Resonance.generateId(chainName, Resonance.SCOPE_PRIVATE, newGroup)
                        pulseStore.insertGroup(
                            Resonance(
                                id = chainId,
                                name = chainName,
                                scope = Resonance.SCOPE_PRIVATE,
                                parentId = groupId,
                            )
                        )
                    }
                } else {
                    pulseStore.updateGroupLastPulse(groupId, System.currentTimeMillis())
                }
            } else {
                // Secure Chain: Establishing private radio ties
                p2pController.startGroupPulse(name, targetMembers, scope, groupId = groupId, parentId = parentId)
                delay(100.milliseconds) 
                pulseStore.getGroup(groupId)?.let { tie ->
                    pulseStore.insertGroup(tie.copy(parentId = parentId, ownerId = repository.getDeviceId()))
                }
            }
        }
        
        if (members == null) _selectedDevices.value = emptySet()
        return groupId
    }

    fun denyRadio(device: P2PDevice) = p2pController.denyRadio(device)

    fun broadcastIdentityUpdate(oldName: String) {
        viewModelScope.launch { p2pController.broadcastIdentityUpdate(oldName) }
    }

    /** Propagates a pulse based on current context (SHOUT/WHISPER/SILENCE). */
    fun sendMessage(message: String, groupId: String? = null) {
        if (message.isBlank()) return
        viewModelScope.launch {
            val targetGid = groupId ?: _currentChainId.value
            p2pController.sendGroupMessage(message, targetGid)
        }
    }

    /** Shares media over the mesh. WiFi is prioritized if available. */
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

    fun vaultGroup(groupId: String, isVaulted: Boolean) = pulseStore.vaultGroup(groupId, isVaulted)

    fun seniorVaultGroup(groupId: String, isSeniorVault: Boolean) = pulseStore.seniorVaultGroup(groupId, isSeniorVault)

    fun updateNote(groupId: String, content: String, messageId: String?, version: Int) {
        viewModelScope.launch { p2pController.sendNoteUpdate(groupId, content, messageId, version) }
    }

    fun initiateHistorySync(deviceId: String, sinceTimestamp: Long? = null) {
        p2pController.initiateHistorySync(deviceId, sinceTimestamp)
    }

    fun restoreFromVault(groupId: String) = pulseStore.restoreFromVault(groupId)

    /** Transitions the UI focus to a specific Resonance context. */
    fun enterChain(chainId: String) {
        _currentChainId.value = chainId
    }

    /** Terminates all active mesh links. */
    fun disconnect() {
        p2pController.closeConnection()
    }

    /** Removes a member from a private Chain. */
    fun removeMemberFromGroup(groupId: String, memberId: String) {
        viewModelScope.launch {
            val group = pulseStore.getGroup(groupId) ?: return@launch
            val newMembers = group.memberIds - memberId
            p2pController.updateGroupMembers(groupId, newMembers)
            pulseStore.insertGroup(group.copy(memberIds = newMembers))
        }
    }

    /** Adds a new member to an existing private Chain. */
    fun addMemberToGroup(groupId: String, memberId: String) {
        viewModelScope.launch {
            val group = pulseStore.getGroup(groupId) ?: return@launch
            val newMembers = group.memberIds + memberId
            p2pController.updateGroupMembers(groupId, newMembers)
            pulseStore.insertGroup(group.copy(memberIds = newMembers))
        }
    }

    /** Assigns a functional role to a member within a Chain context. */
    fun assignRole(groupId: String, memberId: String, role: String) {
        viewModelScope.launch {
            val group = pulseStore.getGroup(groupId) ?: return@launch
            val newUserRoles = group.userRoles + (memberId to role)
            pulseStore.insertGroup(group.copy(userRoles = newUserRoles))
            Log.i("BluetoothViewModel", "Ritual Role: $role assigned to $memberId in $groupId")
        }
    }

    /** Triggers a swarm consensus vote to adjust pulse resonance. */
    fun castVote(pulseId: String, weight: Int) {
        viewModelScope.launch {
            intelligenceManager.castConsensusVote(pulseId, _currentChainId.value, weight)
        }
    }

    fun addSchedule(groupId: String, schedule: cc.thevar.blukit.domain.model.CrowdSchedule) {
        viewModelScope.launch { pulseStore.addCrowdSchedule(groupId, schedule) }
    }

    /** Propagates a Ritual schedule to all members of a Chain. */
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
                )
            }
        }
    }

    override fun onCleared() { p2pController.release() }
}
