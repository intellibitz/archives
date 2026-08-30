/**
 * BLUKIT VIEWMODEL: BLUETOOTH ORCHESTRATOR
 *
 * The central intelligence hub for Blukit's reactive UDF (Unidirectional Data Flow) architecture.
 * Coordinates between hardware radio states, secure P2P engines, and local room storage.
 */
package cc.thevar.blukit.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cc.thevar.blukit.data.local.MessageStore
import cc.thevar.blukit.data.repository.IdentityRepository
import cc.thevar.blukit.data.system.RadioStateManager
import cc.thevar.blukit.data.system.SpreadPermissionManager
import cc.thevar.blukit.domain.logic.IntelligenceManager
import cc.thevar.blukit.domain.model.ConnectionStatus
import cc.thevar.blukit.domain.model.MeshMessage
import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.domain.model.MeshRoom
import cc.thevar.blukit.domain.model.RoomEvent
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
    private val messageStore: MessageStore,
    private val connectivityUseCase: ConnectivityUseCase,
    private val intelligenceManager: IntelligenceManager,
) : ViewModel() {

    private val _selectedDevices = MutableStateFlow<Set<String>>(emptySet())

    private val _currentChainId = MutableStateFlow(MeshRoom.ID_GLOBAL)
    /** The currently focused room context. */
    @Suppress("unused")
    val currentChainId = _currentChainId.asStateFlow()

    /** Public rooms discovered in the local air, excluding current focus. */
    val discoveredCrowds = p2pController.discoveredRooms
        .filter { it.id != _currentChainId.value }
        .shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000))

    /** MESSAGE CANVAS: Reactive flow of high-resonance messages for the spatial header. */
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val highResonancePulses: StateFlow<List<MeshMessage>> = _currentChainId
        .flatMapLatest { groupId ->
            messageStore.getHighResonanceMessages(groupId)
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
                    msgs.filter { it.type == MeshMessage.TYPE_RITUAL_PUSH }.forEach { ritualMsg ->
                        try {
                            val event = kotlinx.serialization.json.Json.decodeFromString<RoomEvent>(ritualMsg.content)
                            ritualMsg.groupId?.let { gid -> addSchedule(gid, event) }
                        } catch (e: Exception) {
                            Log.e("BluetoothViewModel", "Failed to decode ritual push: ${e.message}")
                        }
                    }
                }
            }
            .launchIn(viewModelScope)

        // --- Room Awakening Protocol ---
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

        // --- Room Events ---
        viewModelScope.launch {
            while (true) {
                checkRoomEvents()
                delay(1.minutes)
            }
        }

        // --- Storage Pruning & History Decay ---
        viewModelScope.launch {
            while (true) {
                messageStore.pruneMedia(thresholdMs = 90.days.inWholeMilliseconds) 
                messageStore.autoArchiveRooms() 
                delay(1.days)
            }
        }

        // --- Secure Room Self-Healing ---
        @OptIn(FlowPreview::class)
        combine(
            p2pController.connectedGroups,
            p2pController.scannedDevices,
        ) { connected, scanned -> connected to scanned }
            .debounce(2.seconds)
            .onEach { (connected, scanned) ->
                val currentRoom = messageStore.getGroup(_currentChainId.value)
                if (currentRoom?.scope == MeshRoom.SCOPE_PRIVATE) {
                    val missingMembers = currentRoom.memberIds - connected - repository.getDeviceId()
                    missingMembers.forEach { memberId ->
                        scanned.find { (it.id == memberId) || (it.persistentId == memberId) }?.let { device ->
                            Log.i("BluetoothViewModel", "Auto-Reconnect: Room needs resonance with $memberId")
                            connectToDevice(device)
                        }
                    }
                }
            }.launchIn(viewModelScope)
    }

    /** Evaluates active schedules to awaken Rooms or trigger Smart Reminders. */
    private fun checkRoomEvents() {
        val now = Calendar.getInstance()
        val day = now[Calendar.DAY_OF_WEEK]
        val hour = now[Calendar.HOUR_OF_DAY]
        val minute = now[Calendar.MINUTE]

        val scheduledRooms = messageStore.groups.value.filter { it.schedules.isNotEmpty() }
        scheduledRooms.forEach { room ->
            room.schedules.forEach { s ->
                val isActive = (s.dayOfWeek == day) &&
                        ((hour > s.startHour) || ((hour == s.startHour) && (minute >= s.startMinute))) &&
                        ((hour < s.endHour) || ((hour == s.endHour) && (minute <= s.endMinute)))

                if (isActive && room.isArchived) {
                    restoreFromVault(room.id)
                    Log.i("BluetoothViewModel", "Room Awakening: ${room.name}")
                }
                
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

    /** Automatically broadcasts local SILENCE messages when the mesh radio becomes available. */
    private suspend fun promoteSilenceToShout() {
        val myId = repository.getDeviceId()
        val messages = p2pController.messages.value
        val silentMessages = messages.filter { 
            it.senderId == myId && it.messageScope == MeshMessage.MESSAGE_SILENCE 
        }
        
        silentMessages.forEach { msg ->
            p2pController.broadcastMessage(msg.content, MeshMessage.MESSAGE_SHOUT, msg.messageId, groupId = MeshRoom.ID_GLOBAL, groupName = "HOME")
        }
    }

    // --- State Reducers ---

    private val activityState: Flow<MeshActivity> = combine(
        p2pController.isDiscovering,
        p2pController.isAdvertising,
        p2pController.messages,
        p2pController.errors,
    ) { isDiscovering, isAdvertising, messages, error ->
        val now = System.currentTimeMillis()
        val recentMessages = messages.count { (now - it.timestamp) < 300000 } 
        val intensity = (recentMessages / 20f).coerceAtMost(1f)

        MeshActivity(
            isDiscovering = isDiscovering,
            isAdvertising = isAdvertising,
            energyIntensity = intensity,
            uiError = error?.toUiError(),
        )
    }

    private val crowdState: Flow<NearbyPeers> = combine(
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
        
        NearbyPeers(
            scannedDevices = scanned,
            selectedDevices = selected,
            pulsedPeers = pulsed,
            blockedUsers = blocked,
            incomingRadioRequests = incoming,
            outgoingRadioRequests = outgoing,
        )
    }

    private val sessionDataState: Flow<MeshSession> = combine(
        p2pController.connectedGroups,
        p2pController.messages,
        messageStore.activeGroups,
        messageStore.archivedGroups,
        p2pController.syncProgress,
    ) { ties, messages, groups, archivedGroups, syncProgress ->
        MeshSession(
            connectedTies = ties,
            messages = messages,
            groups = groups,
            archivedGroups = archivedGroups,
            syncProgress = syncProgress,
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
                val peer = crowd.scannedDevices.find { it.id in session.connectedTies }
                    ?: P2PDevice(id = session.connectedTies.firstOrNull() ?: "", name = "?", emoji = "👤")
                RadioConnectionState.Connected(peer)
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

    /** Orchestrates context formation for public rooms or private groups. */
    fun startGroupPulse(name: String, members: Set<String>? = null, scope: Int = MeshRoom.SCOPE_PRIVATE, templateId: String? = null): String {
        if (name.isBlank()) return ""
        
        val targetMembers = members ?: _selectedDevices.value
        if (scope != MeshRoom.SCOPE_PUBLIC && targetMembers.isEmpty()) {
            Log.w("BluetoothViewModel", "Cannot start private group with empty member set")
            return ""
        }

        val isPublicAir = scope == MeshRoom.SCOPE_PUBLIC
        val currentGroup = state.value.session.groups.find { it.id == _currentChainId.value }
        val groupId = MeshRoom.generateId(name, scope, currentGroup)
        val parentId = _currentChainId.value
        
        viewModelScope.launch {
            val existing = messageStore.getGroup(groupId)
            if (isPublicAir) {
                if (existing == null) {
                    val template = cc.thevar.blukit.domain.model.RoomTemplates.ALL.find { it.id == templateId }
                    val newRoom = MeshRoom(
                        id = groupId, 
                        name = name, 
                        scope = MeshRoom.SCOPE_PUBLIC,
                        parentId = parentId,
                        templateId = templateId,
                        ownerId = repository.getDeviceId()
                    )
                    messageStore.insertGroup(newRoom)
                    
                    template?.defaultChannels?.forEach { channelName ->
                        val chainId = MeshRoom.generateId(channelName, MeshRoom.SCOPE_PRIVATE, newRoom)
                        messageStore.insertGroup(
                            MeshRoom(
                                id = chainId,
                                name = channelName,
                                scope = MeshRoom.SCOPE_PRIVATE,
                                parentId = groupId,
                            )
                        )
                    }
                } else {
                    messageStore.updateRoomLastMessage(groupId, System.currentTimeMillis())
                }
            } else {
                p2pController.startGroupRoom(name, targetMembers, scope, groupId = groupId, parentId = parentId)
                delay(100.milliseconds) 
                messageStore.getGroup(groupId)?.let { tie ->
                    messageStore.insertGroup(tie.copy(parentId = parentId, ownerId = repository.getDeviceId()))
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

    /** Propagates a message based on current context. */
    fun sendMessage(message: String, groupId: String? = null) {
        if (message.isBlank()) return
        viewModelScope.launch {
            val targetGid = groupId ?: _currentChainId.value
            p2pController.sendGroupMessage(message, targetGid)
        }
    }

    /** Shares media over the mesh. WiFi is prioritized if available. */
    fun spreadFile(uri: android.net.Uri, messageScope: Int = MeshMessage.MESSAGE_SILENCE) {
        viewModelScope.launch {
            val activeChainId = _currentChainId.value
            val activeChain = messageStore.getGroup(activeChainId)

            when (messageScope) {
                MeshMessage.MESSAGE_SILENCE -> {
                    p2pController.sendFile(uri, null, MeshMessage.MESSAGE_SILENCE, groupId = MeshRoom.ID_SILENCE, groupName = "SILENCE")
                }
                MeshMessage.MESSAGE_SHOUT -> {
                    p2pController.sendFile(uri, null, MeshMessage.MESSAGE_SHOUT, groupId = activeChainId, groupName = activeChain?.name)
                }
                MeshMessage.MESSAGE_WHISPER -> {
                    val targets = state.value.crowd.selectedDevices.ifEmpty { state.value.session.connectedTies }
                    if (targets.isNotEmpty()) {
                        targets.forEach { targetId ->
                            p2pController.sendFile(uri, targetId, messageScope, groupId = activeChainId, groupName = activeChain?.name)
                        }
                    } else {
                        p2pController.sendFile(uri, null, messageScope, groupId = activeChainId, groupName = activeChain?.name)
                    }
                }
            }
        }
    }

    fun vaultGroup(groupId: String, isVaulted: Boolean) = messageStore.vaultGroup(groupId, isVaulted)

    fun seniorVaultGroup(groupId: String, isSeniorVault: Boolean) = messageStore.seniorVaultGroup(groupId, isSeniorVault)

    fun updateNote(groupId: String, content: String, messageId: String?, version: Int) {
        viewModelScope.launch { p2pController.sendNoteUpdate(groupId, content, messageId, version) }
    }

    fun initiateHistorySync(deviceId: String, sinceTimestamp: Long? = null) {
        p2pController.initiateHistorySync(deviceId, sinceTimestamp)
    }

    fun restoreFromVault(groupId: String) = messageStore.restoreFromVault(groupId)

    /** Transitions the UI focus to a specific room context. */
    fun enterChain(chainId: String) {
        _currentChainId.value = chainId
    }

    /** Terminates all active mesh links. */
    fun disconnect() {
        p2pController.closeConnection()
    }

    /** Removes a member from a private group. */
    fun removeMemberFromGroup(groupId: String, memberId: String) {
        viewModelScope.launch {
            val group = messageStore.getGroup(groupId) ?: return@launch
            val newMembers = group.memberIds - memberId
            p2pController.updateGroupMembers(groupId, newMembers)
            messageStore.insertGroup(group.copy(memberIds = newMembers))
        }
    }

    /** Adds a new member to an existing private group. */
    fun addMemberToGroup(groupId: String, memberId: String) {
        viewModelScope.launch {
            val group = messageStore.getGroup(groupId) ?: return@launch
            val newMembers = group.memberIds + memberId
            p2pController.updateGroupMembers(groupId, newMembers)
            messageStore.insertGroup(group.copy(memberIds = newMembers))
        }
    }

    /** Assigns a functional role to a member within a room context. */
    fun assignRole(groupId: String, memberId: String, role: String) {
        viewModelScope.launch {
            val group = messageStore.getGroup(groupId) ?: return@launch
            val newUserRoles = group.userRoles + (memberId to role)
            messageStore.insertGroup(group.copy(userRoles = newUserRoles))
            Log.i("BluetoothViewModel", "Ritual Role: $role assigned to $memberId in $groupId")
        }
    }

    /** Triggers a swarm consensus vote to adjust message resonance. */
    fun castVote(messageId: String, weight: Int) {
        viewModelScope.launch {
            intelligenceManager.castConsensusVote(messageId, _currentChainId.value, weight)
        }
    }

    fun addSchedule(groupId: String, event: RoomEvent) {
        viewModelScope.launch { messageStore.addRoomSchedule(groupId, event) }
    }

    /** Propagates a RoomEvent to all members of a Group. */
    fun pushRitual(groupId: String, event: RoomEvent) {
        viewModelScope.launch {
            val content = kotlinx.serialization.json.Json.encodeToString(event)
            val members = messageStore.getGroup(groupId)?.allMemberIds ?: emptySet()
            members.forEach { memberId ->
                p2pController.sendMessage(
                    content = content,
                    receiverId = memberId,
                    messageScope = MeshMessage.MESSAGE_WHISPER,
                    groupId = groupId
                )
            }
        }
    }

    override fun onCleared() { p2pController.release() }
}
