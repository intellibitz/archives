/**
 * BLUKIT UI: MAIN APP ENTRY (PHASE 2 - CLEAN)
 */
package cc.thevar.blukit.ui

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cc.thevar.blukit.data.system.SpreadPermissionManager
import cc.thevar.blukit.domain.model.Source
import cc.thevar.blukit.ui.components.*
import cc.thevar.blukit.ui.navigation.BlukitNavGraph
import cc.thevar.blukit.ui.navigation.Route
import cc.thevar.blukit.ui.screens.*
import cc.thevar.blukit.ui.viewmodels.*
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun BlukitApp(
    modifier: Modifier = Modifier,
) {
    val viewModel: MainViewModel = koinViewModel()
    val connectionViewModel: ConnectionViewModel = koinViewModel()
    val harmonyViewModel: HarmonyViewModel = koinViewModel()
    val navViewModel: NavigationViewModel = koinViewModel()
    
    val nickname by viewModel.nickname.collectAsStateWithLifecycle(initialValue = null)
    val emojiAvatar by viewModel.emojiAvatar.collectAsStateWithLifecycle(initialValue = "👤")
    val connectionState by connectionViewModel.state.collectAsStateWithLifecycle()
    val harmonyReport by harmonyViewModel.report.collectAsStateWithLifecycle()
    val currentRoute by navViewModel.currentRoute.collectAsStateWithLifecycle()

    // Initialize backstack once nickname is known
    LaunchedEffect(nickname) {
        if (nickname == null) {
            navViewModel.initBackStack(Route.Onboarding)
        } else {
            navViewModel.initBackStack(Route.Nearby)
        }
    }

    val personaCoordinates = remember { mutableStateMapOf<String, PersonaConnectionPoints>() }
    val activeMessageId = remember { mutableStateOf<String?>(null) }

    CompositionLocalProvider(
        LocalPersonaCoordinates provides personaCoordinates,
        LocalActiveMessageId provides activeMessageId,
        LocalUserEmoji provides emojiAvatar
    ) {
        currentRoute?.let { route ->
            BlukitAppContent(
                modifier = modifier,
                nickname = nickname,
                connectionState = connectionState,
                harmonyReport = harmonyReport,
                currentRoute = route,
                onNavigate = { navViewModel.navigate(it, resetStack = true) },
                onLogout = { viewModel.logout() },
                onResetProfile = { viewModel.resetProfile() },
                onBack = if (navViewModel.backStack.size > 1) { { navViewModel.popBackStack() } } else null,
                navViewModel = navViewModel,
                connectionViewModel = connectionViewModel,
                mainViewModel = viewModel,
                harmonyViewModel = harmonyViewModel
            )
        }
    }
}

@Composable
private fun BlukitAppContent(
    modifier: Modifier = Modifier,
    nickname: String?,
    connectionState: ConnectionUiState,
    harmonyReport: cc.thevar.blukit.domain.power.HarmonyReport,
    currentRoute: Route,
    onNavigate: (Route) -> Unit,
    onLogout: () -> Unit,
    onResetProfile: () -> Unit,
    onBack: (() -> Unit)?,
    navViewModel: NavigationViewModel,
    connectionViewModel: ConnectionViewModel,
    mainViewModel: MainViewModel,
    harmonyViewModel: HarmonyViewModel
) {
    val context = LocalContext.current
    val permissionManager: SpreadPermissionManager = koinInject()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var sourceForOptions by remember { mutableStateOf<Source?>(null) }
    var activeRipple by remember { mutableStateOf<Pair<String, Boolean>?>(null) }

    // Error handling
    LaunchedEffect(connectionState.activity.uiError) { 
        connectionState.activity.uiError?.let { snackbarHostState.showSnackbar(it.message.uppercase()) } 
    }

    // Radio refresh
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                connectionViewModel.refreshRadios()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Message ripple
    LaunchedEffect(Unit) {
        connectionViewModel.messageTrigger.collect { trigger -> activeRipple = trigger }
    }

    val permissionState = rememberSpreadPermissionsState(
        allPermissions = permissionManager.requiredPermissions,
        essentialPermissions = permissionManager.essentialPermissions,
    )
    val isPermanentlyDenied = !permissionState.allPermissionsGranted && !permissionState.shouldShowRationale

    val currentTitle = when (currentRoute) {
        is Route.Onboarding -> "IDENTITY"
        is Route.Nearby -> "NEARBY"
        is Route.GroupField -> connectionState.session.groups.find { it.id == currentRoute.roomId }?.name ?: "GROUP"
        is Route.LiveFeed -> "LIVE FEED"
        is Route.Timeline -> "HISTORY"
        is Route.MessageField -> "CHAT"
    }

    BlukitScaffold(
        currentRoute = currentRoute,
        title = currentTitle,
        nickname = nickname,
        syncProgress = connectionState.session.syncProgress,
        snackbarHostState = snackbarHostState,
        onNavigate = onNavigate,
        onLogout = onLogout,
        onResetProfile = onResetProfile,
        onBack = onBack,
        connectionStatus = harmonyReport.synthesis,
        trend = harmonyReport.trendLabel,
        isBluetoothEnabled = connectionState.harmony.isBluetoothEnabled,
        isWifiEnabled = connectionState.harmony.isWifiEnabled,
        onAwakenBluetooth = { connectionViewModel.refreshRadios() },
        onAwakenWifi = { /* WiFi logic */ }
    ) { innerPadding ->
        Box(modifier = modifier.padding(innerPadding)) {
            // Only show permission requirement if not onboarding
            if (currentRoute !is Route.Onboarding && nickname != null && !permissionState.essentialPermissionsGranted) {
                PermissionRequiredScreen(
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
            } else {
                BlukitNavGraph(
                    navViewModel = navViewModel,
                    connectionViewModel = connectionViewModel,
                    mainViewModel = mainViewModel,
                    harmonyViewModel = harmonyViewModel,
                    breadcrumbTrail = navViewModel.getBreadcrumbTrail(
                        sessionGroups = connectionState.session.groups,
                        scannedDevices = connectionState.crowd.scannedDevices
                    ),
                    onCrumbClick = { navViewModel.navigateToCrumb(it) },
                    onSourceLongClick = { sourceForOptions = it }
                )
            }
        }
    }

    sourceForOptions?.let { source ->
        SourceOptionsMenuWrapper(
            source = source,
            connectionState = connectionState,
            currentRoute = currentRoute,
            onNavigate = { navViewModel.navigate(it) },
            onDismiss = { sourceForOptions = null },
            connectionViewModel = connectionViewModel
        )
    }

    activeRipple?.let { (_, isPrivate) ->
        MessageRippleEffect(
            isPrivate = isPrivate,
            onFinished = { activeRipple = null }
        )
    }
}

@Composable
private fun PermissionRequiredScreen(
    isPermanentlyDenied: Boolean,
    onGrantClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        PermissionRequiredField(
            isPermanentlyDenied = isPermanentlyDenied,
            onGrantClick = onGrantClick
        )
    }
}

@Composable
private fun SourceOptionsMenuWrapper(
    source: Source,
    connectionState: ConnectionUiState,
    currentRoute: Route,
    onNavigate: (Route) -> Unit,
    onDismiss: () -> Unit,
    connectionViewModel: ConnectionViewModel
) {
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
        onMessage = { connectionViewModel.requestWhisper(source) { gid -> onNavigate(Route.GroupField(gid)) }; onDismiss() },
        onAccept = { connectionViewModel.acceptRadio(source); onDismiss() },
        onDeny = { connectionViewModel.denyRadio(source); onDismiss() },
        onDisconnect = { onDismiss() },
        onSelect = { connectionViewModel.toggleSourceSelection(source.id); onDismiss() },
        onIdentify = { onDismiss() },
        onBlock = { connectionViewModel.blockUser(source.persistentId ?: source.id); onDismiss() },
        onUnblock = { connectionViewModel.unblockUser(source.persistentId ?: source.id); onDismiss() },
        onSync = { groupId?.let { connectionViewModel.initiateHistorySync(it) }; onDismiss() },
        onAddToGroup = { gid: String -> connectionViewModel.addMemberToGroup(gid, source.id); onDismiss() },
        onRemoveFromGroup = { gid: String -> connectionViewModel.removeMemberFromGroup(gid, source.id); onDismiss() },
        onDismiss = onDismiss
    )
}
