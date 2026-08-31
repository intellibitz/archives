/**
 * BLUKIT VIEWMODEL: RESONANCE ORCHESTRATOR
 *
 * The central intelligence hub for Blukit's reactive UDF (Unidirectional Data Flow) architecture.
 * Coordinates between hardware radio states, secure Resonance engines, and Echo Ledger.
 */
package cc.thevar.blukit.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cc.thevar.blukit.data.local.EchoLedger
import cc.thevar.blukit.data.repository.IdentityRepository
import cc.thevar.blukit.data.system.RadioStateManager
import cc.thevar.blukit.data.system.SpreadPermissionManager
import cc.thevar.blukit.domain.logic.AtmosphereManager
import cc.thevar.blukit.domain.model.ConnectionStatus
import cc.thevar.blukit.domain.model.Echo
import cc.thevar.blukit.domain.model.Source
import cc.thevar.blukit.domain.model.Sphere
import cc.thevar.blukit.domain.model.SphereEvent
import cc.thevar.blukit.domain.usecase.ConnectivityUseCase
import cc.thevar.blukit.network.p2p.ResonanceController
import cc.thevar.blukit.ui.toUiError
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
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
 * Manages resonance connectivity, Sphere orchestration, and interaction hub states.
 */
class BluetoothViewModel(
    private val resonanceController: ResonanceController,
    private val radioStateManager: RadioStateManager,
    private val repository: IdentityRepository,
    private val permissionManager: SpreadPermissionManager,
    private val echoLedger: EchoLedger,
    private val connectivityUseCase: ConnectivityUseCase,
    private val atmosphereManager: AtmosphereManager,
    private val hapticManager: cc.thevar.blukit.data.system.HapticManager,
) : ViewModel() {

    private val _selectedSources = MutableStateFlow<Set<String>>(emptySet())

    private val _resonanceTrigger = kotlinx.coroutines.flow.MutableSharedFlow<Pair<String, Boolean>>()
    val resonanceTrigger = _resonanceTrigger.asSharedFlow()

    private val _currentSphereId = MutableStateFlow(Sphere.ID_GLOBAL)
    /** The currently focused Sphere context. */
    val currentChainId = _currentSphereId.asStateFlow()

    /** Public Spheres sensed in the local air, excluding current focus. */
    val discoveredCrowds = resonanceController.discoveredRooms
        .filter { it.id != _currentSphereId.value }
        .shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000))

    /** ECHO CANVAS: Reactive flow of high-resonance Echoes for the header. */
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val highResonancePulses: StateFlow<List<Echo>> = _currentSphereId
        .flatMapLatest { groupId ->
            echoLedger.getHighResonanceEchoes(groupId)
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
        resonanceController.messages
            .onEach { msgs ->
                if (msgs.isNotEmpty()) {
                    msgs.filter { it.type == Echo.TYPE_RITUAL_PUSH }.forEach { ritualMsg ->
                        try {
                            val event = kotlinx.serialization.json.Json.decodeFromString<SphereEvent>(ritualMsg.content)
                            ritualMsg.groupId?.let { gid -> addSchedule(gid, event) }
                        } catch (e: Exception) {
                            Log.e("BluetoothViewModel", "Failed to decode ritual push: ${e.message}")
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
                checkSphereEvents()
                delay(1.minutes)
            }
        }

        viewModelScope.launch {
            while (true) {
                echoLedger.pruneMedia(thresholdMs = 90.days.inWholeMilliseconds) 
                echoLedger.autoArchiveSpheres() 
                delay(1.days)
            }
        }

        @OptIn(FlowPreview::class)
        combine(
            resonanceController.connectedGroups,
            resonanceController.scannedDevices,
        ) { connected, scanned -> connected to scanned }
            .debounce(2.seconds)
            .onEach { (connected, scanned) ->
                val currentSphere = echoLedger.getSphere(_currentSphereId.value)
                if (currentSphere?.scope == Sphere.SCOPE_PRIVATE) {
                    val missingMembers = currentSphere.memberIds - connected - repository.getDeviceId()
                    missingMembers.forEach { memberId ->
                        scanned.find { (it.id == memberId) || (it.persistentId == memberId) }?.let { source ->
                            Log.i("BluetoothViewModel", "Auto-Reconnect: Sphere needs resonance with $memberId")
                            connectToSource(source)
                        }
                    }
                }
            }.launchIn(viewModelScope)
    }

    private fun checkSphereEvents() {
        val now = Calendar.getInstance()
        val day = now[Calendar.DAY_OF_WEEK]
        val hour = now[Calendar.HOUR_OF_DAY]
        val minute = now[Calendar.MINUTE]

        val scheduledSpheres = echoLedger.spheres.value.filter { it.schedules.isNotEmpty() }
        scheduledSpheres.forEach { sphere ->
            sphere.schedules.forEach { s ->
                val isActive = (s.dayOfWeek == day) &&
                        ((hour > s.startHour) || ((hour == s.startHour) && (minute >= s.startMinute))) &&
                        ((hour < s.endHour) || ((hour == s.endHour) && (minute <= s.endMinute)))

                if (isActive && sphere.isArchived) {
                    restoreFromVault(sphere.id)
                    Log.i("BluetoothViewModel", "Sphere Awakening: ${sphere.name}")
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

    private suspend fun promoteSilenceToShout() {
        val myId = repository.getDeviceId()
        val echoes = resonanceController.messages.value
        val silentEchoes = echoes.filter { 
            it.senderId == myId && it.messageScope == Echo.MESSAGE_SILENCE 
        }
        
        silentEchoes.forEach { msg ->
            resonanceController.broadcastMessage(msg.content, Echo.MESSAGE_SHOUT, msg.messageId, groupId = Sphere.ID_GLOBAL, groupName = "HOME")
        }
    }

    private val activityState: Flow<MeshActivity> = combine(
        resonanceController.isDiscovering,
        resonanceController.isAdvertising,
        resonanceController.messages,
        resonanceController.errors,
    ) { isDiscovering, isAdvertising, echoes, error ->
        val now = System.currentTimeMillis()
        val recentEchoes = echoes.count { (now - it.timestamp) < 300000 } 
        val intensity = (recentEchoes / 20f).coerceAtMost(1f)

        MeshActivity(
            isDiscovering = isDiscovering,
            isAdvertising = isAdvertising,
            energyIntensity = intensity,
            uiError = error?.toUiError(),
        )
    }

    private val crowdState: Flow<NearbyPeers> = combine(
        resonanceController.scannedDevices,
        _selectedSources,
        repository.pulsedPeers,
        repository.blockedUsers,
        resonanceController.incomingRadioRequests,
        resonanceController.outgoingRadioRequests,
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
        resonanceController.connectedGroups,
        resonanceController.messages,
        echoLedger.activeSpheres,
        echoLedger.archivedSpheres,
        resonanceController.syncProgress,
    ) { ties, echoes, spheres, archivedSpheres, syncProgress ->
        MeshSession(
            connectedTies = ties,
            messages = echoes,
            groups = spheres,
            archivedGroups = archivedSpheres,
            syncProgress = syncProgress,
        )
    }

    val state: StateFlow<BluetoothUiState> = combine(
        harmonyState,
        activityState,
        crowdState,
        sessionDataState,
        combine(
            resonanceController.isConnected,
            connectivityUseCase.manualConnectionStatus,
            resonanceController.errors,
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

        BluetoothUiState(
            harmony = harmony,
            activity = activity,
            crowd = crowd,
            session = session.copy(connectionState = connectionState),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BluetoothUiState())

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
                Log.i("BluetoothViewModel", "Resonance attempt $currentTry for ${device.name}")
                
                connectivityUseCase.connectToSource(device, state.value.session.connectedTies)
                
                val status = connectivityUseCase.manualConnectionStatus
                    .first { it !is ConnectionStatus.Connecting }
                
                if (status is ConnectionStatus.Connected) {
                    success = true
                    Log.i("BluetoothViewModel", "Resonated with ${device.name}")
                } else if (status is ConnectionStatus.Error) {
                    Log.e("BluetoothViewModel", "Attempt $currentTry failed: ${status.message}")
                    if (currentTry < retryCount) {
                        delay((2000L * currentTry).milliseconds) 
                    }
                }
            }
        }
    }

    fun requestWhisper(device: Source) = connectToSource(device)

    fun acceptRadio(device: Source) = resonanceController.acceptRadio(device)

    fun toggleSourceSelection(deviceId: String) {
        _selectedSources.update { 
            if (it.contains(deviceId)) it - deviceId else it + deviceId
        }
    }

    fun clearSelection() { _selectedSources.value = emptySet() }

    fun startSphereResonance(name: String, members: Set<String>? = null, scope: Int = Sphere.SCOPE_PRIVATE, templateId: String? = null): String {
        if (name.isBlank()) return ""
        
        val targetMembers = members ?: _selectedSources.value
        if (scope != Sphere.SCOPE_PUBLIC && targetMembers.isEmpty()) {
            Log.w("BluetoothViewModel", "Cannot start private Sphere with empty Source set")
            return ""
        }

        val currentSphere = state.value.session.groups.find { it.id == _currentSphereId.value }
        val groupId = Sphere.generateId(name, scope, currentSphere)
        val parentId = _currentSphereId.value
        
        viewModelScope.launch {
            val existing = echoLedger.getSphere(groupId)
            if (scope == Sphere.SCOPE_PUBLIC) {
                if (existing == null) {
                    val template = cc.thevar.blukit.domain.model.RoomTemplates.ALL.find { it.id == templateId }
                    val newSphere = Sphere(
                        id = groupId, 
                        name = name, 
                        scope = Sphere.SCOPE_PUBLIC,
                        parentId = parentId,
                        templateId = templateId,
                        ownerId = repository.getDeviceId()
                    )
                    echoLedger.insertSphere(newSphere)
                    
                    template?.defaultChannels?.forEach { channelName ->
                        val chainId = Sphere.generateId(channelName, Sphere.SCOPE_PRIVATE, newSphere)
                        echoLedger.insertSphere(
                            Sphere(
                                id = chainId,
                                name = channelName,
                                scope = Sphere.SCOPE_PRIVATE,
                                parentId = groupId,
                            )
                        )
                    }
                } else {
                    echoLedger.updateSphereLastEcho(groupId, System.currentTimeMillis())
                }
            } else {
                resonanceController.startGroupRoom(name, targetMembers, scope, groupId = groupId, parentId = parentId)
                delay(100.milliseconds) 
                echoLedger.getSphere(groupId)?.let { tie ->
                    echoLedger.insertSphere(tie.copy(parentId = parentId, ownerId = repository.getDeviceId()))
                }
            }
        }
        
        if (members == null) _selectedSources.value = emptySet()
        return groupId
    }

    fun denyRadio(device: Source) = resonanceController.denyRadio(device)

    fun broadcastIdentityUpdate(oldName: String) {
        viewModelScope.launch { resonanceController.broadcastIdentityUpdate(oldName) }
    }

    fun echo(message: String, groupId: String? = null) {
        if (message.isBlank()) return
        viewModelScope.launch {
            val targetGid = groupId ?: _currentSphereId.value
            val echo = resonanceController.sendGroupMessage(message, targetGid)
            if (echo != null) {
                hapticManager.triggerMessage(cc.thevar.blukit.data.system.HapticManager.MessageType.RESONATE)
                _resonanceTrigger.emit(targetGid to (echo.messageScope == Echo.MESSAGE_WHISPER))
            }
        }
    }

    fun echoFile(uri: android.net.Uri, messageScope: Int = Echo.MESSAGE_SILENCE) {
        viewModelScope.launch {
            val activeSphereId = _currentSphereId.value
            val activeSphere = echoLedger.getSphere(activeSphereId)

            when (messageScope) {
                Echo.MESSAGE_SILENCE -> {
                    resonanceController.sendFile(uri, null, Echo.MESSAGE_SILENCE, groupId = Sphere.ID_SILENCE, groupName = "SILENCE")
                }
                Echo.MESSAGE_SHOUT -> {
                    resonanceController.sendFile(uri, null, Echo.MESSAGE_SHOUT, groupId = activeSphereId, groupName = activeSphere?.name)
                }
                Echo.MESSAGE_WHISPER -> {
                    val targets = state.value.crowd.selectedDevices.ifEmpty { state.value.session.connectedTies }
                    if (targets.isNotEmpty()) {
                        targets.forEach { targetId ->
                            resonanceController.sendFile(uri, targetId, messageScope, groupId = activeSphereId, groupName = activeSphere?.name)
                        }
                    } else {
                        resonanceController.sendFile(uri, null, messageScope, groupId = activeSphereId, groupName = activeSphere?.name)
                    }
                }
            }
        }
    }

    fun vaultSphere(groupId: String, isVaulted: Boolean) = echoLedger.vaultSphere(groupId, isVaulted)

    fun seniorVaultSphere(groupId: String, isSeniorVault: Boolean) = echoLedger.seniorVaultSphere(groupId, isSeniorVault)

    fun updateNote(groupId: String, content: String, messageId: String?, version: Int) {
        viewModelScope.launch { resonanceController.sendNoteUpdate(groupId, content, messageId, version) }
    }

    fun initiateHistorySync(deviceId: String, sinceTimestamp: Long? = null) {
        resonanceController.initiateHistorySync(deviceId, sinceTimestamp)
    }

    fun restoreFromVault(groupId: String) = echoLedger.restoreFromVault(groupId)

    fun enterSphere(chainId: String) {
        _currentSphereId.value = chainId
    }

    fun disconnect() {
        resonanceController.closeConnection()
    }

    fun removeMemberFromSphere(groupId: String, memberId: String) {
        viewModelScope.launch {
            val sphere = echoLedger.getSphere(groupId) ?: return@launch
            val newMembers = sphere.memberIds - memberId
            resonanceController.updateGroupMembers(groupId, newMembers)
            echoLedger.insertSphere(sphere.copy(memberIds = newMembers))
        }
    }

    fun addMemberToSphere(groupId: String, memberId: String) {
        viewModelScope.launch {
            val sphere = echoLedger.getSphere(groupId) ?: return@launch
            val newMembers = sphere.memberIds + memberId
            resonanceController.updateGroupMembers(groupId, newMembers)
            echoLedger.insertSphere(sphere.copy(memberIds = newMembers))
        }
    }

    fun assignRole(groupId: String, memberId: String, role: String) {
        viewModelScope.launch {
            val sphere = echoLedger.getSphere(groupId) ?: return@launch
            val newUserRoles = sphere.userRoles + (memberId to role)
            echoLedger.insertSphere(sphere.copy(userRoles = newUserRoles))
            Log.i("BluetoothViewModel", "Ritual Role: $role assigned to $memberId in $groupId")
        }
    }

    fun castVote(messageId: String, weight: Int) {
        viewModelScope.launch {
            atmosphereManager.castConsensusVote(messageId, _currentSphereId.value, weight)
        }
    }

    fun addSchedule(groupId: String, event: SphereEvent) {
        viewModelScope.launch { echoLedger.addSphereSchedule(groupId, event) }
    }

    fun pushRitual(groupId: String, event: SphereEvent) {
        viewModelScope.launch {
            val content = kotlinx.serialization.json.Json.encodeToString(event)
            val members = echoLedger.getSphere(groupId)?.allMemberIds ?: emptySet()
            members.forEach { memberId ->
                resonanceController.sendMessage(
                    content = content,
                    receiverId = memberId,
                    messageScope = Echo.MESSAGE_WHISPER,
                    groupId = groupId
                )
            }
        }
    }

    override fun onCleared() { resonanceController.release() }
}
