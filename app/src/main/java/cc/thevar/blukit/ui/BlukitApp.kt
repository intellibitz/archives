/**
 * BLUKIT UI: MAIN APP ENTRY
 */
package cc.thevar.blukit.ui

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Radar
import androidx.compose.material.icons.rounded.Stream
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import cc.thevar.blukit.domain.model.Message
import cc.thevar.blukit.domain.model.Source
import cc.thevar.blukit.domain.model.Group
import cc.thevar.blukit.ui.navigation.Route
import cc.thevar.blukit.ui.components.MessageRippleEffect
import cc.thevar.blukit.ui.components.BlukitToolbar
import cc.thevar.blukit.ui.components.MessageHub
import cc.thevar.blukit.ui.screens.*
import cc.thevar.blukit.ui.theme.*
import cc.thevar.blukit.ui.viewmodels.ConnectionViewModel
import cc.thevar.blukit.ui.viewmodels.HarmonyViewModel
import cc.thevar.blukit.ui.viewmodels.MainViewModel
import cc.thevar.blukit.ui.viewmodels.NavigationViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun BlukitApp(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val permissionManager: cc.thevar.blukit.data.system.SpreadPermissionManager = koinInject()
    
    val viewModel: MainViewModel = koinViewModel()
    val connectionViewModel: ConnectionViewModel = koinViewModel()
    val harmonyViewModel: HarmonyViewModel = koinViewModel()
    val navViewModel: NavigationViewModel = koinViewModel()
    
    val nickname by viewModel.nickname.collectAsStateWithLifecycle(initialValue = null)
    val emojiAvatar by viewModel.emojiAvatar.collectAsStateWithLifecycle(initialValue = "👤")
    
    val connectionState by connectionViewModel.state.collectAsStateWithLifecycle()
    val trendingMessages by connectionViewModel.trendingMessages.collectAsStateWithLifecycle()
    val harmonyReport by harmonyViewModel.report.collectAsStateWithLifecycle()

    val currentRoute by navViewModel.currentRoute.collectAsStateWithLifecycle()
    val backStack = navViewModel.backStack

    var sourceForOptions by remember { mutableStateOf<Source?>(null) }
    var showMemberManagement by remember { mutableStateOf(false) }
    var isSearchActive by remember { mutableStateOf(false) }
    
    val personaCoordinates = remember { mutableStateMapOf<String, PersonaConnectionPoints>() }
    val activeMessageId = remember { mutableStateOf<String?>(null) }

    CompositionLocalProvider(
        LocalPersonaCoordinates provides personaCoordinates,
        LocalActiveMessageId provides activeMessageId,
        LocalUserEmoji provides (emojiAvatar ?: "👤")
    ) {
        val breadcrumbTrail = navViewModel.getBreadcrumbTrail(
            sessionGroups = connectionState.session.groups,
            focusedSourceId = null,
            scannedDevices = connectionState.crowd.scannedDevices
        )

    val onCrumbClick: (Int) -> Unit = { index ->
        navViewModel.navigateToCrumb(index)
    }

    var activeRipple by remember { mutableStateOf<Pair<String, Boolean>?>(null) }
    LaunchedEffect(Unit) {
        connectionViewModel.messageTrigger.collect { trigger ->
            activeRipple = trigger
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(connectionState.activity.uiError) { 
        connectionState.activity.uiError?.let { snackbarHostState.showSnackbar(it.message.uppercase()) } 
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                connectionViewModel.refreshRadios()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val permissionState = rememberSpreadPermissionsState(
        allPermissions = permissionManager.requiredPermissions,
        essentialPermissions = permissionManager.essentialPermissions,
    )
    val isPermanentlyDenied = !permissionState.allPermissionsGranted && !permissionState.shouldShowRationale
    
    NavigationSuiteScaffold(
        navigationSuiteItems = {
            if (nickname != null) {
                listOf(
                    Triple(Route.Nearby, "Nearby", Icons.Rounded.Radar),
                    Triple(Route.LiveFeed, "Live", Icons.Rounded.Stream),
                    Triple(Route.Timeline, "Messages", Icons.Rounded.History),
                ).forEach { (route, label, icon) ->
                    item(
                        selected = currentRoute::class == route::class,
                        onClick = { navViewModel.navigate(route, resetStack = true) },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label) }
                    )
                }
            }
        },
        modifier = modifier.fillMaxSize(),
        containerColor = StealthBlack,
        contentColor = Color.White
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                Column {
                    if (nickname == null) {
                        IdentityInput(
                            onSave = { name, emoji -> 
                                viewModel.saveNickname(name)
                                viewModel.saveEmoji(emoji)
                            }
                        )
                    } else {
                        BlukitToolbar(
                            title = if (currentRoute is Route.GroupField) {
                                connectionState.session.groups.find { it.id == (currentRoute as Route.GroupField).roomId }?.name ?: "Group"
                            } else "Blukit",
                            onLogout = { viewModel.logout() },
                            onResetProfile = { viewModel.resetProfile() },
                            themeColor = if (currentRoute is Route.GroupField) StealthRose else StealthPrimary,
                            onBack = if (navViewModel.backStack.size > 1) { { navViewModel.popBackStack() } } else null,
                            connectionStatus = harmonyReport.synthesis,
                            trend = harmonyReport.trendLabel,
                            isBluetoothOff = !connectionState.harmony.isBluetoothEnabled,
                            isWifiOff = !connectionState.harmony.isWifiEnabled,
                            onAwakenBluetooth = { connectionViewModel.refreshRadios() },
                            onAwakenWifi = { /* WiFi logic */ }
                        )
                    }
                    
                    SyncProgressIndicator(progress = connectionState.session.syncProgress)
                }
            }
        ) { innerPadding ->
            if (nickname != null) {
                Box(modifier = Modifier.padding(innerPadding)) {
                    if (!permissionState.essentialPermissionsGranted) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            PermissionRequiredField(
                                isPermanentlyDenied = isPermanentlyDenied,
                                onGrantClick = {
                                    if (isPermanentlyDenied) {
                                        context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                            data = Uri.fromParts("package", context.packageName, null)
                                        })
                                    } else {
                                        permissionState.launchMultiplePermissionRequest()
                                    }
                                }
                            )
                        }
                    } else {
                        NavDisplay(
                            backStack = backStack,
                        ) { entryRoute ->
                            NavEntry(entryRoute) {
                                when (entryRoute) {
                                    is Route.Nearby -> {
                                        NearbyField(
                                            state = connectionState,
                                            localDeviceId = viewModel.deviceId.collectAsStateWithLifecycle().value,
                                            header = { },
                                            breadcrumbTrail = breadcrumbTrail,
                                            onCrumbClick = onCrumbClick,
                                            userNickname = nickname ?: "",
                                            harmonyReport = harmonyReport,
                                            onShowTimeline = { navViewModel.navigate(Route.Timeline) },
                                            onResetProfile = { connectionViewModel.resetProfile() },
                                            onNavigateToGroup = { gid -> navViewModel.navigate(Route.GroupField(gid)) },
                                            onNavigateToMessage = { eid -> navViewModel.navigate(Route.MessageField(eid)) },
                                            onSourceLongClick = { sourceForOptions = it },
                                            onAcceptRadio = { connectionViewModel.acceptRadio(it) },
                                            onDenyRadio = { connectionViewModel.denyRadio(it) },
                                            onRestoreCrowd = { connectionViewModel.restoreFromVault(it) },
                                            onNavigateToLiveFeed = { navViewModel.navigate(Route.LiveFeed) },
                                            onSearchToggle = { isSearchActive = !isSearchActive },
                                            isSearchActive = isSearchActive,
                                            onStartWhisper = { 
                                                val selectedIds = connectionState.crowd.selectedDevices
                                                val source = connectionState.crowd.scannedDevices.find { it.id in selectedIds }
                                                if (source != null) connectionViewModel.requestWhisper(source)
                                            },
                                            onStartSubGroup = { connectionViewModel.startGroupConnection("NEW GROUP") },
                                            onClearSelection = { connectionViewModel.clearSelection() },
                                            onCreatePublicRoom = { name, tid -> connectionViewModel.startGroupConnection(name, scope = Group.SCOPE_PUBLIC, templateId = tid) }
                                        )
                                    }
                                    is Route.Timeline -> {
                                        TimelineField(
                                            messages = connectionState.session.messages,
                                            onBack = { navViewModel.popBackStack() }
                                        )
                                    }
                                    is Route.LiveFeed -> {
                                        LiveFeedField(
                                            messages = connectionState.session.messages,
                                            sources = connectionState.crowd.scannedDevices,
                                            onBack = { navViewModel.popBackStack() },
                                            onMessageClick = { navViewModel.navigate(Route.MessageField(it)) }
                                        )
                                    }
                                    is Route.GroupField -> {
                                        val group = connectionState.session.groups.find { it.id == entryRoute.roomId }
                                        val groupTrend = group?.trendLabel

                                        if (group?.scope == Group.SCOPE_PRIVATE) {
                                            PrivateGroupField(
                                                state = connectionState,
                                                localDeviceId = viewModel.deviceId.collectAsStateWithLifecycle().value,
                                                header = { },
                                                groupId = entryRoute.roomId,
                                                breadcrumbTrail = breadcrumbTrail,
                                                onCrumbClick = onCrumbClick,
                                                userNickname = nickname ?: "",
                                                activeGroups = connectionState.session.groups,
                                                onShowTimeline = { navViewModel.navigate(Route.Timeline) },
                                                onResetProfile = { connectionViewModel.resetProfile() },
                                                onBack = { navViewModel.popBackStack() },
                                                onNavigateToMessage = { navViewModel.navigate(Route.MessageField(it)) },
                                                onNavigateToGroup = { gid -> navViewModel.navigate(Route.GroupField(gid)) },
                                                onSourceLongClick = { sourceForOptions = it },
                                                onSend = { content -> connectionViewModel.sendMessage(content, entryRoute.roomId) },
                                                onUpdateRecord = { gid, content, mid, v -> connectionViewModel.updateNote(gid, content, mid, v) },
                                                onVaultGroup = { gid, v -> connectionViewModel.vaultGroup(gid, v) },
                                                onSeniorVaultGroup = { gid, v -> connectionViewModel.seniorVaultGroup(gid, v) },
                                                onRemoveMember = { gid, mid -> connectionViewModel.removeMemberFromGroup(gid, mid) },
                                                onAssignRole = { gid, mid, role -> connectionViewModel.assignRole(gid, mid, role) },
                                                onPushRitual = { gid, event -> connectionViewModel.pushRitual(gid, event) },
                                                showMemberManagement = showMemberManagement,
                                                onShowManagement = { showMemberManagement = true },
                                                onDismissManagement = { showMemberManagement = false },
                                                onStartWhisper = { 
                                                    val selectedIds = connectionState.crowd.selectedDevices
                                                    val source = connectionState.crowd.scannedDevices.find { it.id in selectedIds }
                                                    if (source != null) connectionViewModel.requestWhisper(source) { gid ->
                                                        navViewModel.navigate(Route.GroupField(gid))
                                                    }
                                                },
                                                onStartSubGroup = { connectionViewModel.startGroupConnection("SUB GROUP") },
                                                onClearSelection = { connectionViewModel.clearSelection() },
                                                trend = groupTrend,
                                                isSearchActive = isSearchActive,
                                                onSearchToggle = { isSearchActive = !isSearchActive },
                                                onAcceptRadio = { connectionViewModel.acceptRadio(it) },
                                                onDenyRadio = { connectionViewModel.denyRadio(it) }
                                            )
                                        } else {
                                            GroupField(
                                                state = connectionState,
                                                localDeviceId = viewModel.deviceId.collectAsStateWithLifecycle().value,
                                                header = { },
                                                groupId = entryRoute.roomId,
                                                breadcrumbTrail = breadcrumbTrail,
                                                onCrumbClick = onCrumbClick,
                                                userNickname = nickname ?: "",
                                                onShowTimeline = { navViewModel.navigate(Route.Timeline) },
                                                onResetProfile = { connectionViewModel.resetProfile() },
                                                highConnectionMessages = trendingMessages,
                                                onBack = { navViewModel.popBackStack() },
                                                onNavigateToMessage = { navViewModel.navigate(Route.MessageField(it)) },
                                                onSourceLongClick = { sourceForOptions = it },
                                                onSend = { content -> connectionViewModel.sendMessage(content, entryRoute.roomId) },
                                                onStartWhisper = { 
                                                    val selectedIds = connectionState.crowd.selectedDevices
                                                    val source = connectionState.crowd.scannedDevices.find { it.id in selectedIds }
                                                    if (source != null) connectionViewModel.requestWhisper(source) { gid ->
                                                        navViewModel.navigate(Route.GroupField(gid))
                                                    }
                                                },
                                                onStartSubGroup = { connectionViewModel.startGroupConnection("NEW GROUP") },
                                                onClearSelection = { connectionViewModel.clearSelection() },
                                                trend = groupTrend,
                                                isSearchActive = isSearchActive,
                                                onSearchToggle = { isSearchActive = !isSearchActive },
                                                onAcceptRadio = { connectionViewModel.acceptRadio(it) },
                                                onDenyRadio = { connectionViewModel.denyRadio(it) }
                                            )
                                        }
                                    }
                                    is Route.MessageField -> {
                                        var messageText by remember { mutableStateOf("") }
                                        MessageField(
                                            state = connectionState,
                                            localDeviceId = viewModel.deviceId.collectAsStateWithLifecycle().value,
                                            messageId = entryRoute.messageId,
                                            header = { },
                                            breadcrumbTrail = breadcrumbTrail,
                                            onCrumbClick = onCrumbClick,
                                            userNickname = nickname ?: "",
                                            activeGroups = connectionState.session.groups,
                                            onShowTimeline = { navViewModel.navigate(Route.Timeline) },
                                            onResetProfile = { connectionViewModel.resetProfile() },
                                            onBack = { navViewModel.popBackStack() },
                                            onNavigateToMessage = { navViewModel.navigate(Route.MessageField(it)) },
                                            onNavigateToLiveFeed = { navViewModel.navigate(Route.LiveFeed) },
                                            messageText = messageText,
                                            onMessageChange = { messageText = it },
                                            onSend = {
                                                connectionViewModel.sendMessage(messageText, connectionState.session.messages.find { it.messageId == entryRoute.messageId }?.groupId)
                                                messageText = ""
                                            },
                                            isSearchActive = isSearchActive,
                                            onSearchToggle = { isSearchActive = !isSearchActive }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                sourceForOptions?.let { source ->
                    val groupId = (currentRoute as? Route.GroupField)?.roomId
                    val isAlreadyInGroup = if (groupId != null) {
                        val group = connectionState.session.groups.find { it.id == groupId }
                        source.id in (group?.allMemberIds ?: emptySet()) || source.persistentId in (group?.allMemberIds ?: emptySet())
                    } else false

                    SourceOptionsMenu(
                        device = source,
                        isTied = connectionState.session.groups.any { it.memberIds.contains(source.id) || it.memberIds.contains(source.persistentId) },
                        isBlocked = connectionState.crowd.blockedUsers.contains(source.persistentId ?: source.id),
                        isRequesting = connectionState.crowd.incomingRadioRequests.contains(source),
                        activeGroupId = groupId,
                        isAlreadyInActiveGroup = isAlreadyInGroup,
                        onMessage = { connectionViewModel.requestWhisper(source) { gid -> navViewModel.navigate(Route.GroupField(gid)) }; sourceForOptions = null },
                        onAccept = { connectionViewModel.acceptRadio(source); sourceForOptions = null },
                        onDeny = { connectionViewModel.denyRadio(source); sourceForOptions = null },
                        onDisconnect = { sourceForOptions = null },
                        onSelect = { connectionViewModel.toggleSourceSelection(source.id); sourceForOptions = null },
                        onIdentify = { sourceForOptions = null },
                        onBlock = { connectionViewModel.blockUser(source.persistentId ?: source.id); sourceForOptions = null },
                        onUnblock = { connectionViewModel.unblockUser(source.persistentId ?: source.id); sourceForOptions = null },
                        onSync = { groupId?.let { connectionViewModel.initiateHistorySync(it) }; sourceForOptions = null },
                        onAddToGroup = { gid: String -> connectionViewModel.addMemberToGroup(gid, source.id); sourceForOptions = null },
                        onRemoveFromGroup = { gid: String -> connectionViewModel.removeMemberFromGroup(gid, source.id); sourceForOptions = null },
                        onDismiss = { sourceForOptions = null }
                    )
                }
            }
        }
    }

    activeRipple?.let { (_, isPrivate) ->
        MessageRippleEffect(
            isPrivate = isPrivate,
            onFinished = { activeRipple = null }
        )
    }
}
}
