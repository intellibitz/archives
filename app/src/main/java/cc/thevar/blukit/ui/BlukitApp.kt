/**
 * BLUKIT UI: MAIN APP ENTRY
 */
package cc.thevar.blukit.ui

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import cc.thevar.blukit.domain.model.Echo
import cc.thevar.blukit.domain.model.Sphere
import cc.thevar.blukit.ui.navigation.Route
import cc.thevar.blukit.ui.components.EchoRippleEffect
import cc.thevar.blukit.ui.screens.*
import cc.thevar.blukit.ui.theme.*
import cc.thevar.blukit.ui.viewmodels.BluetoothViewModel
import cc.thevar.blukit.ui.viewmodels.HarmonyViewModel
import cc.thevar.blukit.ui.viewmodels.MainViewModel
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
    val bluetoothViewModel: BluetoothViewModel = koinViewModel()
    val harmonyViewModel: HarmonyViewModel = koinViewModel()
    
    val nickname by viewModel.nickname.collectAsStateWithLifecycle(initialValue = null)
    val bluetoothState by bluetoothViewModel.state.collectAsStateWithLifecycle()
    val highResonanceEchoes by bluetoothViewModel.highResonancePulses.collectAsStateWithLifecycle()
    val harmonyReport by harmonyViewModel.report.collectAsStateWithLifecycle()

    var activeRipple by remember { mutableStateOf<Pair<String, Boolean>?>(null) }
    LaunchedEffect(Unit) {
        bluetoothViewModel.resonanceTrigger.collect { trigger ->
            activeRipple = trigger
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(bluetoothState.activity.uiError) { 
        bluetoothState.activity.uiError?.let { snackbarHostState.showSnackbar(it.message.uppercase()) } 
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                bluetoothViewModel.refreshRadios()
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

    val initialRoute = Route.Sensing
    val backStack = remember { mutableStateListOf<Route>(initialRoute) }
    val currentRoute = backStack.lastOrNull()

    var focusedSphereId by remember { mutableStateOf<String?>(null) }
    
    val breadcrumbTrail = remember(backStack.size, bluetoothState, focusedSphereId) {
        val trail = mutableListOf<String>()
        backStack.forEach { route ->
            when (route) {
                is Route.Sensing -> trail.add("SENSING")
                is Route.SphereField -> {
                    val sphere = bluetoothState.session.groups.find { it.id == route.roomId }
                    if (sphere != null) {
                        sphere.parentId?.let { pid ->
                            val parent = bluetoothState.session.groups.find { it.id == pid }
                            val parentName = parent?.name ?: "HOME"
                            if (trail.lastOrNull() != parentName) trail.add(parentName)
                        }
                        trail.add(sphere.name)
                    } else {
                        trail.add("SPHERE")
                    }
                }
                else -> trail.add("BLUKIT")
            }
        }
        
        if ((focusedSphereId != null) && (currentRoute is Route.SphereField)) {
            val device = bluetoothState.crowd.scannedDevices.find { (it.persistentId == focusedSphereId) || (it.id == focusedSphereId) }
            trail.add(device?.name ?: "Source")
        }
        trail.distinct() 
    }

    val onCrumbClick: (Int) -> Unit = { index ->
        if (index < backStack.size) {
            while (backStack.size > index + 1) {
                backStack.removeLastOrNull()
            }
            focusedSphereId = null
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column {
                if (nickname == null) {
                    IdentityEchoInput(
                        onSave = { name, emoji -> 
                            viewModel.saveNickname(name)
                            viewModel.saveEmoji(emoji)
                        }
                    )
                } else {
                    ResonanceHeader(
                        themeColor = if (currentRoute is Route.SphereField) StealthRose else StealthPrimary,
                        onAwakenBluetooth = { bluetoothViewModel.refreshRadios() },
                        onAwakenWifi = { /* WiFi logic if any */ },
                        onGrantPermissions = { permissionState.launchMultiplePermissionRequest() },
                        onOpenSettings = {
                            context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            })
                        },
                        onLogout = { viewModel.logout() },
                        isBluetoothOff = !bluetoothState.harmony.isBluetoothEnabled,
                        isWifiOff = !bluetoothState.harmony.isWifiEnabled,
                        isPermissionMissing = !permissionState.essentialPermissionsGranted,
                        isPermanentlyDenied = isPermanentlyDenied,
                        resonanceStatus = harmonyReport.synthesis,
                        breeze = harmonyReport.currentBreeze,
                        trend = harmonyReport.trendLabel,
                        highResonanceMessages = highResonanceEchoes,
                        trail = breadcrumbTrail,
                        onCrumbClick = onCrumbClick
                    )
                }
                
                SyncProgressIndicator(progress = bluetoothState.session.syncProgress)
            }
        },
        bottomBar = {
            if (nickname != null) {
                ResonanceNav(
                    currentRoute = currentRoute ?: Route.Sensing,
                    onNav = { route ->
                        if (route != currentRoute) {
                            backStack.clear()
                            backStack.add(route)
                            focusedSphereId = null
                        }
                    }
                )
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
                                is Route.Sensing -> {
                                    SensingField(
                                        state = bluetoothState,
                                        localDeviceId = viewModel.deviceId.collectAsStateWithLifecycle().value,
                                        header = { },
                                        onNavigateToGroup = { gid -> backStack.add(Route.SphereField(gid)) },
                                        onNavigateToPulse = { eid -> backStack.add(Route.EchoField(eid)) },
                                        onAcceptRadio = { bluetoothViewModel.acceptRadio(it) },
                                        onDenyRadio = { bluetoothViewModel.denyRadio(it) },
                                        onCreatePublicRoom = { name, tid -> bluetoothViewModel.startSphereResonance(name, scope = Sphere.SCOPE_PUBLIC, templateId = tid) }
                                    )
                                }
                                is Route.Timeline -> {
                                    TimelineField(
                                        echoes = bluetoothState.session.messages,
                                        onBack = { backStack.removeLast() }
                                    )
                                }
                                is Route.LiveFeed -> {
                                    LiveFeedField(
                                        echoes = bluetoothState.session.messages,
                                        sources = bluetoothState.crowd.scannedDevices,
                                        onBack = { backStack.removeLast() },
                                        onEchoClick = { backStack.add(Route.EchoField(it)) }
                                    )
                                }
                                is Route.SphereField -> {
                                    val sphere = bluetoothState.session.groups.find { it.id == entryRoute.roomId }
                                    val sphereTrend = bluetoothState.session.messages.findLast { 
                                        it.groupId == entryRoute.roomId && it.type == Echo.TYPE_AI_SUMMARY 
                                    }?.trendLabel

                                    if (sphere?.scope == Sphere.SCOPE_PRIVATE) {
                                        PrivateSphereField(
                                            state = bluetoothState,
                                            localDeviceId = viewModel.deviceId.collectAsStateWithLifecycle().value,
                                            header = { },
                                            sphereId = entryRoute.roomId,
                                            onBack = { backStack.removeLast() },
                                            onNavigateToPulse = { backStack.add(Route.EchoField(it)) },
                                            onSend = { content -> bluetoothViewModel.echo(content, entryRoute.roomId) },
                                            onUpdateRecord = { gid, content, mid, v -> bluetoothViewModel.updateNote(gid, content, mid, v) },
                                            onVaultSphere = { gid, v -> bluetoothViewModel.vaultSphere(gid, v) },
                                            onSeniorVaultSphere = { gid, v -> bluetoothViewModel.seniorVaultSphere(gid, v) },
                                            trend = sphereTrend
                                        )
                                    } else {
                                        SphereField(
                                            state = bluetoothState,
                                            localDeviceId = viewModel.deviceId.collectAsStateWithLifecycle().value,
                                            header = { },
                                            sphereId = entryRoute.roomId,
                                            highResonanceMessages = highResonanceEchoes,
                                            onBack = { backStack.removeLast() },
                                            onNavigateToPulse = { backStack.add(Route.EchoField(it)) },
                                            onSend = { content -> bluetoothViewModel.echo(content, entryRoute.roomId) },
                                            trend = sphereTrend
                                        )
                                    }
                                }
                                is Route.EchoField -> {
                                    EchoField(
                                        state = bluetoothState,
                                        localDeviceId = viewModel.deviceId.collectAsStateWithLifecycle().value,
                                        messageId = entryRoute.messageId,
                                        header = { },
                                        onBack = { backStack.removeLast() },
                                        onNavigateToPulse = { backStack.add(Route.EchoField(it)) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    activeRipple?.let { (groupId, isPrivate) ->
        EchoRippleEffect(
            isPrivate = isPrivate,
            onFinished = { activeRipple = null }
        )
    }
}
