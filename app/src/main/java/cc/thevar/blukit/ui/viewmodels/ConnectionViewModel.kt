/**
 * BLUKIT VIEWMODEL: CONNECTION ORCHESTRATOR
 *
 * The central intelligence hub for Blukit's reactive UDF (Unidirectional Data Flow) architecture.
 * Coordinates between hardware radio states, secure Connection engines, and Message Ledger.
 */
package cc.thevar.blukit.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cc.thevar.blukit.data.local.MessageRepository
import cc.thevar.blukit.data.repository.IdentityRepository
import cc.thevar.blukit.data.system.RadioStateManager
import cc.thevar.blukit.data.system.SpreadPermissionManager
import cc.thevar.blukit.domain.logic.AssistantManager
import cc.thevar.blukit.domain.model.ConnectionStatus
import cc.thevar.blukit.domain.model.Message
import cc.thevar.blukit.domain.model.Source
import cc.thevar.blukit.domain.model.Group
import cc.thevar.blukit.domain.model.GroupEvent
import cc.thevar.blukit.domain.usecase.ConnectivityUseCase
import cc.thevar.blukit.network.p2p.ConnectionController
import cc.thevar.blukit.ui.toUiError
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * Manages connection connectivity, Group orchestration, and interaction hub states.
 */
class ConnectionViewModel(
    private val connectionController: ConnectionController,
    private val radioStateManager: RadioStateManager,
    private val repository: IdentityRepository,
    private val permissionManager: SpreadPermissionManager,
    private val messageRepository: MessageRepository,
    private val connectivityUseCase: ConnectivityUseCase,
    private val assistantManager: AssistantManager,
    private val hapticManager: cc.thevar.blukit.data.system.HapticManager,
) : ViewModel() {

    private val _selectedSources = MutableStateFlow<Set<String>>(emptySet())

    private val _messageTrigger = kotlinx.coroutines.flow.MutableSharedFlow<Pair<String, Boolean>>()
    val messageTrigger = _messageTrigger.asSharedFlow()

    private val _currentGroupId = MutableStateFlow(Group.ID_GLOBAL)
    /** The currently focused Group context. */
    val currentGroupId = _currentGroupId.asStateFlow()

    /** Public Groups sensed in the local connection environment, excluding current focus. */
    val discoveredGroups = connectionController.discoveredGroups
        .filter { it.id != _currentGroupId.value }
        .shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000))

    /** MESSAGE CANVAS: Reactive flow of trending Messages for the header. */
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val trendingMessages: StateFlow<List<Message>> = _currentGroupId
        .flatMapLatest { groupId ->
            messageRepository.getHighConnectionMessages(groupId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
        connectionController.messages
            .onEach { msgs ->
                if (msgs.isNotEmpty()) {
                    msgs.filter { it.type == Message.TYPE_RITUAL_PUSH }.forEach { ritualMsg ->
                        try {
                            val event = kotlinx.serialization.json.Json.decodeFromString<GroupEvent>(ritualMsg.content)
                            ritualMsg.groupId?.let { gid -> addSchedule(gid, event) }
                        } catch (e: Exception) {
                            Log.e("ConnectionViewModel", "Failed to decode ritual push: ${e.message}")
                        }
                    }
                }
            }
            .launchIn(viewModelScope)

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

        viewModelScope.launch {
            while (true) {
                checkGroupEvents()
                delay(1.minutes)
            }
        }

        viewModelScope.launch {
            while (true) {
                messageRepository.pruneMedia(thresholdMs = 90.days.inWholeMilliseconds) 
                messageRepository.autoArchiveGroups() 
                delay(1.days)
            }
        }

        @OptIn(FlowPreview::class)
        combine(
            connectionController.connectedGroups,
            connectionController.scannedDevices,
        ) { connected, scanned -> connected to scanned }
            .debounce(2.seconds)
            .onEach { (connected, scanned) ->
                val currentGroup = messageRepository.getGroup(_currentGroupId.value)
                if (currentGroup?.scope == Group.SCOPE_PRIVATE) {
                    val missingMembers = currentGroup.memberIds - connected - repository.getDeviceId()
                    missingMembers.forEach { memberId ->
                        scanned.find { (it.id == memberId) || (it.persistentId == memberId) }?.let { source ->
                            Log.i("ConnectionViewModel", "Auto-Reconnect: Group needs connection with $memberId")
                            connectToSource(source)
                        }
                    }
                }
            }.launchIn(viewModelScope)
    }

    private fun checkGroupEvents() {
        val now = Calendar.getInstance()
        val day = now[Calendar.DAY_OF_WEEK]
        val hour = now[Calendar.HOUR_OF_DAY]
        val minute = now[Calendar.MINUTE]

        val scheduledGroups = messageRepository.groups.value.filter { it.schedules.isNotEmpty() }
        scheduledGroups.forEach { group ->
            group.schedules.forEach { s ->
                val isActive = (s.dayOfWeek == day) &&
                        ((hour > s.startHour) || ((hour == s.startHour) && (minute >= s.startMinute))) &&
                        ((hour < s.endHour) || ((hour == s.endHour) && (minute <= s.endMinute)))

                if (isActive && group.isArchived) {
                    restoreFromVault(group.id)
                    Log.i("ConnectionViewModel", "Group Awakening: ${group.name}")
                }
                
                s.reminderLeadTimeMs?.let { leadTime ->
                    val scheduleTime = Calendar.getInstance().apply {
                        set(Calendar.DAY_OF_WEEK, s.dayOfWeek)
                        set(Calendar.HOUR_OF_DAY, s.startHour)
                        set(Calendar.MINUTE, s.startMinute)
                    }.timeInMillis
                    
                    val nowMs = System.currentTimeMillis()
                    if ((scheduleTime - nowMs) in (leadTime - 60000)..leadTime) {
                        Log.i("ConnectionViewModel", "SMART REMINDER: ${s.title ?: "Event"} starting in ${leadTime / 60000} mins")
                    }
                }
            }
        }
    }

    private suspend fun promoteSilenceToShout() {
        val myId = repository.getDeviceId()
        val messages = connectionController.messages.value
        val silentMessages = messages.filter { 
            it.senderId == myId && it.messageScope == Message.MESSAGE_SILENCE 
        }
        
        silentMessages.forEach { msg ->
            connectionController.broadcastMessage(msg.content, Message.MESSAGE_SHOUT, msg.messageId, groupId = Group.ID_GLOBAL, groupName = "HOME")
        }
    }

    private val activityState: Flow<ConnectionActivity> = combine(
        connectionController.isDiscovering,
        connectionController.isAdvertising,
        connectionController.messages,
        connectionController.errors,
    ) { isDiscovering, isAdvertising, messages, error ->
        val now = System.currentTimeMillis()
        val recentMessages = messages.count { (now - it.timestamp) < 300000 } 
        val intensity = (recentMessages / 20f).coerceAtMost(1f)

        ConnectionActivity(
            isDiscovering = isDiscovering,
            isAdvertising = isAdvertising,
            energyIntensity = intensity,
            uiError = error?.toUiError(),
        )
    }

    private val crowdState: Flow<ConnectionPeers> = combine(
        connectionController.scannedDevices,
        _selectedSources,
        repository.pulsedPeers,
        repository.blockedUsers,
        connectionController.incomingRadioRequests,
        connectionController.outgoingRadioRequests,
    ) { flows ->
        @Suppress("UNCHECKED_CAST")
        val scanned = flows[0] as List<Source>
        @Suppress("UNCHECKED_CAST")
        val selected = flows[1] as Set<String>
        @Suppress("UNCHECKED_CAST")
        val pulsed = flows[2] as Set<String>
        @Suppress("UNCHECKED_CAST")
        val blocked = flows[3] as Set<String>
        @Suppress("UNCHECKED_CAST")
        val incoming = flows[4] as Set<Source>
        @Suppress("UNCHECKED_CAST")
        val outgoing = flows[5] as Set<Source>
        
        ConnectionPeers(
            scannedDevices = scanned,
            selectedDevices = selected,
            pulsedPeers = pulsed,
            blockedUsers = blocked,
            incomingRadioRequests = incoming,
            outgoingRadioRequests = outgoing,
        )
    }

    private val sessionDataState: Flow<ConnectionSession> = combine(
        connectionController.connectedGroups,
        connectionController.messages,
        messageRepository.activeGroups,
        messageRepository.archivedGroups,
        connectionController.syncProgress,
    ) { ties, messages, groups, archivedGroups, syncProgress ->
        ConnectionSession(
            connectedTies = ties,
            messages = messages,
            groups = groups,
            archivedGroups = archivedGroups,
            syncProgress = syncProgress,
        )
    }

    val state: StateFlow<ConnectionUiState> = combine(
        harmonyState,
        activityState,
        crowdState,
        sessionDataState,
        combine(
            connectionController.isConnected,
            connectivityUseCase.manualConnectionStatus,
            connectionController.errors,
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
                    ?: Source(id = session.connectedTies.firstOrNull() ?: "", name = "?", emoji = "👤")
                RadioConnectionState.Connected(peer)
            }
            activity.isDiscovering || activity.isAdvertising -> RadioConnectionState.Scanning
            else -> RadioConnectionState.Disconnected
        }

        ConnectionUiState(
            harmony = harmony,
            activity = activity,
            crowd = crowd,
            session = session.copy(connectionState = connectionState),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ConnectionUiState())

    /** THE CONNECTION LIST: Pre-sorted list of active Groups and People for the Nearby Field. */
    val connectionList: StateFlow<List<ConnectionItem>> = combine(
        state,
        repository.deviceId
    ) { uiState, localDeviceId ->
        val messages = uiState.session.messages
        val groups = uiState.session.groups
        
        val list = mutableListOf<ConnectionItem>()
        
        // 1. Active Groups (Sorted by last message)
        val sortedGroups = groups.filter { it.scope == Group.SCOPE_PUBLIC }
            .sortedByDescending { it.lastMessageTimestamp }
        
        sortedGroups.forEach { group ->
            val latestMessage = messages.findLast { it.groupId == group.id }
            val headSource = Source(
                id = group.id, 
                name = if (group.id == Group.ID_GLOBAL) "Public Hub" else group.name, 
                emoji = group.projectionEmoji ?: "💬", 
                medium = Source.ConnectionMedium.BLUETOOTH, 
                statusLabel = if (group.trendLabel != null) "Trending: ${group.trendLabel}" else null
            )
            list.add(ConnectionItem(headSource, latestMessage))
        }

        // 2. People Nearby (Not in groups)
        val people = uiState.crowd.scannedDevices.filter { source ->
            groups.none { it.memberIds.contains(source.id) || it.allMemberIds.contains(source.id) }
        }
        
        people.forEach { source ->
            list.add(ConnectionItem(source, null))
        }

        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun refreshRadios() {
        radioStateManager.triggerRefresh()
        permissionManager.refresh()
    }

    fun connectToSource(device: Source, retryCount: Int = 3) {
        viewModelScope.launch {
            var currentTry = 0
            var success = false
            while (!success && currentTry < retryCount) {
                currentTry++
                Log.i("ConnectionViewModel", "Connection attempt $currentTry for ${device.name}")
                
                connectivityUseCase.connectToSource(device, state.value.session.connectedTies)
                
                val status = connectivityUseCase.manualConnectionStatus
                    .first { it !is ConnectionStatus.Connecting }
                
                if (status is ConnectionStatus.Connected) {
                    success = true
                    Log.i("ConnectionViewModel", "Connected with ${device.name}")
                } else if (status is ConnectionStatus.Error) {
                    Log.e("ConnectionViewModel", "Attempt $currentTry failed: ${status.message}")
                    if (currentTry < retryCount) {
                        delay((2000L * currentTry).milliseconds) 
                    }
                }
            }
        }
    }

    fun requestWhisper(device: Source, onAnchoredCreated: ((String) -> Unit)? = null) {
        viewModelScope.launch {
            val currentGid = _currentGroupId.value
            val currentGroup = messageRepository.getGroup(currentGid)
            
            if (currentGroup?.scope == Group.SCOPE_PUBLIC && currentGid != Group.ID_SILENCE) {
                val anchoredGid = getOrCreateAnchoredGroup(
                    name = "Chat with ${device.name}", 
                    targetId = device.id, 
                    anchoredPublicId = currentGid
                )
                enterGroup(anchoredGid)
                onAnchoredCreated?.invoke(anchoredGid)
            } else {
                connectToSource(device)
            }
        }
    }

    private suspend fun getOrCreateAnchoredGroup(name: String, targetId: String, anchoredPublicId: String): String {
        val existing = messageRepository.groups.value.find { 
            it.scope == Group.SCOPE_PRIVATE && 
            it.anchoredPublicGroupId == anchoredPublicId && 
            (it.memberIds.contains(targetId) || it.allMemberIds.contains(targetId))
        }
        if (existing != null) return existing.id
        
        return startGroupConnection(
            name = name, 
            members = setOf(targetId), 
            scope = Group.SCOPE_PRIVATE, 
            anchoredPublicGroupId = anchoredPublicId
        )
    }

    fun acceptRadio(device: Source) = connectionController.acceptRadio(device)

    fun toggleSourceSelection(deviceId: String) {
        _selectedSources.update { 
            if (it.contains(deviceId)) it - deviceId else it + deviceId
        }
    }

    fun clearSelection() { _selectedSources.value = emptySet() }

    fun startGroupConnection(name: String, members: Set<String>? = null, scope: Int = Group.SCOPE_PRIVATE, templateId: String? = null, anchoredPublicGroupId: String? = null): String {
        if (name.isBlank()) return ""
        
        val targetMembers = members ?: _selectedSources.value
        if (scope != Group.SCOPE_PUBLIC && targetMembers.isEmpty()) {
            Log.w("ConnectionViewModel", "Cannot start private Group with empty Source set")
            return ""
        }

        val currentGroup = state.value.session.groups.find { it.id == _currentGroupId.value }
        val groupId = Group.generateId(name, scope, currentGroup)
        val parentId = _currentGroupId.value
        
        viewModelScope.launch {
            val existing = messageRepository.getGroup(groupId)
            if (scope == Group.SCOPE_PUBLIC) {
                if (existing == null) {
                    val template = cc.thevar.blukit.domain.model.RoomTemplates.ALL.find { it.id == templateId }
                    val newGroup = Group(
                        id = groupId, 
                        name = name, 
                        scope = Group.SCOPE_PUBLIC,
                        parentId = parentId,
                        templateId = templateId,
                        ownerId = repository.getDeviceId(),
                        anchoredPublicGroupId = anchoredPublicGroupId
                    )
                    messageRepository.insertGroup(newGroup)
                    
                    template?.defaultChannels?.forEach { channelName ->
                        val chainId = Group.generateId(channelName, Group.SCOPE_PRIVATE, newGroup)
                        messageRepository.insertGroup(
                            Group(
                                id = chainId,
                                name = channelName,
                                scope = Group.SCOPE_PRIVATE,
                                parentId = groupId,
                            )
                        )
                    }
                } else {
                    messageRepository.updateGroupLastMessage(groupId, System.currentTimeMillis())
                }
            } else {
                connectionController.startGroupRoom(name, targetMembers, scope, groupId = groupId, parentId = parentId, anchoredPublicGroupId = anchoredPublicGroupId)
                delay(100.milliseconds) 
                messageRepository.getGroup(groupId)?.let { tie ->
                    messageRepository.insertGroup(tie.copy(parentId = parentId, ownerId = repository.getDeviceId(), anchoredPublicGroupId = anchoredPublicGroupId))
                }
            }
        }
        
        if (members == null) _selectedSources.value = emptySet()
        return groupId
    }

    fun denyRadio(device: Source) = connectionController.denyRadio(device)

    fun broadcastIdentityUpdate(oldName: String) {
        viewModelScope.launch { connectionController.broadcastIdentityUpdate(oldName) }
    }

    fun sendMessage(messageContent: String, groupId: String? = null) {
        if (messageContent.isBlank()) return
        viewModelScope.launch {
            val targetGid = groupId ?: _currentGroupId.value
            val message = connectionController.sendGroupMessage(messageContent, targetGid)
            if (message != null) {
                hapticManager.triggerMessage(cc.thevar.blukit.data.system.HapticManager.MessageType.CONNECTION)
                _messageTrigger.emit(targetGid to (message.messageScope == Message.MESSAGE_WHISPER))
            }
        }
    }

    fun sendFile(uri: android.net.Uri, messageScope: Int = Message.MESSAGE_SILENCE) {
        viewModelScope.launch {
            val activeGroupId = _currentGroupId.value
            val activeGroup = messageRepository.getGroup(activeGroupId)

            when (messageScope) {
                Message.MESSAGE_SILENCE -> {
                    connectionController.sendFile(uri, null, Message.MESSAGE_SILENCE, groupId = Group.ID_SILENCE, groupName = "SILENCE")
                }
                Message.MESSAGE_SHOUT -> {
                    connectionController.sendFile(uri, null, Message.MESSAGE_SHOUT, groupId = activeGroupId, groupName = activeGroup?.name)
                }
                Message.MESSAGE_WHISPER -> {
                    val targets = state.value.crowd.selectedDevices.ifEmpty { state.value.session.connectedTies }
                    if (targets.isNotEmpty()) {
                        targets.forEach { targetId ->
                            connectionController.sendFile(uri, targetId, messageScope, groupId = activeGroupId, groupName = activeGroup?.name)
                        }
                    } else {
                        connectionController.sendFile(uri, null, messageScope, groupId = activeGroupId, groupName = activeGroup?.name)
                    }
                }
            }
        }
    }

    fun vaultGroup(groupId: String, isVaulted: Boolean) = messageRepository.vaultGroup(groupId, isVaulted)

    fun blockUser(persistentId: String) {
        viewModelScope.launch {
            repository.blockUser(persistentId)
        }
    }

    fun unblockUser(persistentId: String) {
        viewModelScope.launch {
            repository.unblockUser(persistentId)
        }
    }

    fun resetProfile() {
        viewModelScope.launch {
            repository.resetProfile()
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }

    fun seniorVaultGroup(groupId: String, isSeniorVault: Boolean) = messageRepository.seniorVaultGroup(groupId, isSeniorVault)

    fun updateNote(groupId: String, content: String, messageId: String?, version: Int) {
        viewModelScope.launch { connectionController.sendNoteUpdate(groupId, content, messageId, version) }
    }

    fun initiateHistorySync(deviceId: String, sinceTimestamp: Long? = null) {
        connectionController.initiateHistorySync(deviceId, sinceTimestamp)
    }

    fun restoreFromVault(groupId: String) = messageRepository.restoreFromVault(groupId)

    fun enterGroup(groupId: String) {
        _currentGroupId.value = groupId
    }

    fun disconnect() {
        connectionController.closeConnection()
    }

    fun removeMemberFromGroup(groupId: String, memberId: String) {
        viewModelScope.launch {
            val group = messageRepository.getGroup(groupId) ?: return@launch
            val newMembers = group.memberIds - memberId
            connectionController.updateGroupMembers(groupId, newMembers)
            messageRepository.insertGroup(group.copy(memberIds = newMembers))
        }
    }

    fun addMemberToGroup(groupId: String, memberId: String) {
        viewModelScope.launch {
            val group = messageRepository.getGroup(groupId) ?: return@launch
            val newMembers = group.memberIds + memberId
            connectionController.updateGroupMembers(groupId, newMembers)
            messageRepository.insertGroup(group.copy(memberIds = newMembers))
        }
    }

    fun assignRole(groupId: String, memberId: String, role: String) {
        viewModelScope.launch {
            val group = messageRepository.getGroup(groupId) ?: return@launch
            val newUserRoles = group.userRoles + (memberId to role)
            messageRepository.insertGroup(group.copy(userRoles = newUserRoles))
            Log.i("ConnectionViewModel", "Role: $role assigned to $memberId in $groupId")
        }
    }

    fun castVote(messageId: String, weight: Int) {
        viewModelScope.launch {
            assistantManager.castConsensusVote(messageId, _currentGroupId.value, weight)
        }
    }

    fun addSchedule(groupId: String, event: GroupEvent) {
        viewModelScope.launch { messageRepository.addGroupSchedule(groupId, event) }
    }

    fun pushRitual(groupId: String, event: GroupEvent) {
        viewModelScope.launch {
            val content = kotlinx.serialization.json.Json.encodeToString(event)
            val members = messageRepository.getGroup(groupId)?.allMemberIds ?: emptySet()
            members.forEach { memberId ->
                connectionController.sendMessage(
                    content = content,
                    receiverId = memberId,
                    messageScope = Message.MESSAGE_WHISPER,
                    groupId = groupId
                )
            }
        }
    }

    override fun onCleared() { connectionController.release() }
}
