package cc.thevar.blukit.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import cc.thevar.blukit.BlukitApplication
import cc.thevar.blukit.R
import cc.thevar.blukit.data.local.VibeStore
import cc.thevar.blukit.data.power.SupremePowerManager
import cc.thevar.blukit.data.repository.ContactRepository
import cc.thevar.blukit.data.repository.IdentityRepository
import cc.thevar.blukit.data.system.RadioStateManager
import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.network.p2p.P2PController
import cc.thevar.blukit.network.p2p.P2PError
import cc.thevar.blukit.ui.navigation.Route
import cc.thevar.blukit.ui.screens.BlukitHeartbeat
import cc.thevar.blukit.ui.screens.RipplesScreen
import cc.thevar.blukit.ui.screens.TieScreen
import cc.thevar.blukit.ui.theme.StealthAmber
import cc.thevar.blukit.ui.theme.StealthPrimary
import cc.thevar.blukit.ui.theme.StealthRose
import cc.thevar.blukit.ui.viewmodels.*
import cc.thevar.blukit.ui.viewmodels.ViewModelFactory
import cc.thevar.blukit.ui.screens.BlukitInput

@Composable
fun rememberSpreadPermissionsState(permissions: List<String>): SpreadPermissionsState {
    val context = LocalContext.current
    val manager = remember { (context.applicationContext as BlukitApplication).spreadPermissionManager }
    
    var allGranted by remember {
        mutableStateOf(manager.checkAllGranted())
    }
    var shouldShowRationale by remember {
        mutableStateOf(permissions.any {
            (context as? Activity)?.shouldShowRequestPermissionRationale(it) == true
        })
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        allGranted = manager.checkAllGranted()
        shouldShowRationale = permissions.any {
            (context as? Activity)?.shouldShowRequestPermissionRationale(it) == true
        }
        // Hardened: Sync with global manager
        (context.applicationContext as? BlukitApplication)?.spreadPermissionManager?.refresh()
    }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            allGranted = manager.checkAllGranted()
            shouldShowRationale = permissions.any {
                (context as? Activity)?.shouldShowRequestPermissionRationale(it) == true
            }
            // Hardened: Sync with the global manager so the VM reacts
            (context.applicationContext as? BlukitApplication)?.spreadPermissionManager?.refresh()
        }
    }

    return remember(allGranted, shouldShowRationale) {
        object : SpreadPermissionsState {
            override val allPermissionsGranted: Boolean = allGranted
            override val shouldShowRationale: Boolean = shouldShowRationale
            override fun launchMultiplePermissionRequest() {
                launcher.launch(permissions.toTypedArray())
            }
        }
    }
}

interface SpreadPermissionsState {
    val allPermissionsGranted: Boolean
    val shouldShowRationale: Boolean
    fun launchMultiplePermissionRequest()
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun BlukitApp(
    onEnterPip: () -> Unit,
    repository: IdentityRepository,
    contactRepository: ContactRepository,
    vibeStore: VibeStore,
    radioStateManager: RadioStateManager,
    p2pController: P2PController,
    supremePowerManager: SupremePowerManager,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val permissionManager = (context.applicationContext as BlukitApplication).spreadPermissionManager
    
    val viewModel: MainViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MainViewModel(repository, vibeStore) as T
            }
        }
    )
    val bluetoothViewModel: BluetoothViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return BluetoothViewModel(p2pController, radioStateManager, permissionManager) as T
            }
        }
    )
    val supremePowerViewModel: SupremePowerViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SupremePowerViewModel(supremePowerManager) as T
            }
        }
    )
    
    val nickname by viewModel.nickname.collectAsStateWithLifecycle(initialValue = null)
    val isStealthMode by viewModel.isStealthMode.collectAsStateWithLifecycle(initialValue = false)
    val lowPowerMode by viewModel.lowPowerMode.collectAsStateWithLifecycle(initialValue = false)
    val bluetoothState by bluetoothViewModel.state.collectAsStateWithLifecycle()
    val report by supremePowerViewModel.report.collectAsStateWithLifecycle()
    val energySurge by bluetoothViewModel.energySurge.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(bluetoothState.uiError) {
        bluetoothState.uiError?.let { error ->
            snackbarHostState.showSnackbar(
                message = error.message.uppercase(),
                duration = if (error.isCritical) SnackbarDuration.Long else SnackbarDuration.Short
            )
        }
    }

    val permissionState = rememberSpreadPermissionsState(permissions = permissionManager.requiredPermissions)
    val isPermanentlyDenied = !permissionState.allPermissionsGranted && !permissionState.shouldShowRationale

    val initialRoute = Route.Crowd
    val backStack = rememberNavBackStack(initialRoute)
    val currentRoute = backStack.lastOrNull()

    val hubRotation by rememberInfiniteTransition(label = "HubLighthouse").animateFloat(
        initialValue = 0f, 
        targetValue = 360f, 
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing)), 
        label = "Scan"
    )

    val listDetailSceneStrategy = rememberListDetailSceneStrategy<NavKey>()

    Box(modifier = modifier.fillMaxSize()) {
        FullLighthouseScan(rotation = hubRotation, lowPowerMode = lowPowerMode)

        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            sceneStrategy = listDetailSceneStrategy,
            modifier = Modifier.fillMaxSize()
        ) { key ->
            when (key) {
                Route.Crowd -> NavEntry(key, metadata = ListDetailSceneStrategy.listPane()) {
                    RipplesScreen(
                        state = bluetoothState,
                        localDeviceId = viewModel.deviceId.collectAsStateWithLifecycle(initialValue = "").value,
                        localEmoji = "👤",
                        energySurge = energySurge,
                        lowPowerMode = lowPowerMode,
                        onStartScan = bluetoothViewModel::startScan,
                        onStopScan = bluetoothViewModel::stopScan,
                        onDeviceClick = { device ->
                            if (device.id !in bluetoothState.connectedLinks) {
                                bluetoothViewModel.connectToDevice(device)
                            } else {
                                backStack.add(Route.Vibes)
                            }
                        },
                        onBroadcastMessage = bluetoothViewModel::roar
                    )
                }
                Route.Chat -> NavEntry(key, metadata = ListDetailSceneStrategy.detailPane()) {
                    TieScreen(
                        state = bluetoothState,
                        localDeviceId = viewModel.deviceId.collectAsStateWithLifecycle(initialValue = "").value,
                        localEmoji = "👤",
                        vibeId = bluetoothState.connectedVibe?.id,
                        vibeName = bluetoothState.connectedVibe?.name,
                        vibeEmoji = bluetoothState.connectedVibe?.emoji,
                        onDisconnect = bluetoothViewModel::disconnect,
                        onNavigateBack = { backStack.removeLastOrNull() },
                        onSendMessage = bluetoothViewModel::sendMessage,
                        onBlockUser = viewModel::blockUser,
                        onEnterPip = onEnterPip
                    )
                }
                Route.Vibes -> NavEntry(key) {
                    RipplesScreen(
                        state = bluetoothState,
                        localDeviceId = viewModel.deviceId.collectAsStateWithLifecycle(initialValue = "").value,
                        localEmoji = "👤",
                        energySurge = energySurge,
                        onlyTies = true,
                        lowPowerMode = lowPowerMode,
                        onStartScan = bluetoothViewModel::startScan,
                        onStopScan = bluetoothViewModel::stopScan,
                        onDeviceClick = { device ->
                            backStack.add(Route.Chat)
                        },
                        onBroadcastMessage = bluetoothViewModel::roar
                    )
                }
                else -> NavEntry(key) { Text("Unknown") }
            }
        }

        val roarsCount = remember(bluetoothState.messages) {
            bluetoothState.messages.count { it.receiverId.isNullOrBlank() }
        }
        val vibesCount = remember(bluetoothState.messages) {
            bluetoothState.messages.count { !it.receiverId.isNullOrBlank() }
        }

        var messageText by remember { mutableStateOf("") }
        val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
        
        Column(
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp).zIndex(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(
                visible = currentRoute is Route.Crowd || currentRoute is Route.Vibes,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                BlukitInput(
                    value = messageText,
                    onValueChange = { messageText = it },
                    onSend = {
                        if (messageText.isNotBlank()) {
                            bluetoothViewModel.roar(messageText, currentRoute is Route.Vibes)
                            messageText = ""
                            focusManager.clearFocus()
                        }
                    },
                    messageCount = if (currentRoute is Route.Vibes) vibesCount else roarsCount,
                    placeholder = "SPREAD VIBES…",
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            UnifiedBlukitBadge(
                energy = energySurge,
                rotation = hubRotation,
                userCount = report.userCount,
                linksCount = report.connectedLinksCount,
                roarsCount = roarsCount,
                vibesCount = vibesCount,
                lowPowerMode = lowPowerMode,
                permissionsGranted = permissionState.allPermissionsGranted,
                isPermanentlyDenied = isPermanentlyDenied,
                isStealthMode = isStealthMode,
                currentBreeze = report.currentBreeze,
                incomingLinkRequests = bluetoothState.incomingLinkRequests,
                isBluetoothEnabled = bluetoothState.isBluetoothEnabled,
                isLocationEnabled = bluetoothState.isLocationEnabled,
                isWifiEnabled = bluetoothState.isWifiEnabled,
                currentRoute = (currentRoute as? Route) ?: initialRoute,
                nickname = nickname ?: "UNKNOWN",
                onNavigate = { route ->
                    if (currentRoute != route) {
                        backStack.add(route)
                    }
                },
                onAwakenBluetooth = { context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS)) },
                onAwakenLocation = { context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) },
                onAwakenWifi = { context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS)) },
                onGrantPermissions = { permissionState.launchMultiplePermissionRequest() },
                onOpenSettings = {
                    context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    })
                },
                onSaveNickname = viewModel::saveNickname,
                onToggleStealth = viewModel::toggleStealth,
                onToggleLowPower = viewModel::toggleLowPowerMode,
                onClearHistory = viewModel::clearChatHistory,
                onLogout = viewModel::logout,
                onAcceptLink = bluetoothViewModel::acceptLink
            )
        }

        val isStill = !bluetoothState.isBluetoothEnabled || !bluetoothState.isLocationEnabled

        if (isStill && permissionState.shouldShowRationale && !permissionState.allPermissionsGranted) {
            AlertDialog(
                onDismissRequest = { },
                containerColor = Color.Black,
                titleContentColor = StealthPrimary,
                textContentColor = Color.White.copy(alpha = 0.7f),
                title = { Text("ENERGY REQUIRED", fontWeight = FontWeight.Black) },
                text = { Text("BLUKIT USES LOCAL RADIOS TO SYNC THE VIBE.", fontSize = 12.sp) },
                confirmButton = {
                    TextButton(onClick = { permissionState.launchMultiplePermissionRequest() }) {
                        Text("GRANT", color = StealthPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}

@Composable
fun UnifiedBlukitBadge(
    energy: Float,
    rotation: Float,
    userCount: Int,
    linksCount: Int,
    roarsCount: Int,
    vibesCount: Int,
    lowPowerMode: Boolean,
    permissionsGranted: Boolean,
    isPermanentlyDenied: Boolean,
    isStealthMode: Boolean,
    currentBreeze: String?,
    incomingLinkRequests: Set<P2PDevice>,
    isBluetoothEnabled: Boolean,
    isLocationEnabled: Boolean,
    isWifiEnabled: Boolean,
    currentRoute: Route,
    nickname: String,
    onNavigate: (Route) -> Unit,
    onAwakenBluetooth: () -> Unit,
    onAwakenLocation: () -> Unit,
    onAwakenWifi: () -> Unit,
    onGrantPermissions: () -> Unit,
    onOpenSettings: () -> Unit,
    onSaveNickname: (String) -> Unit,
    onToggleStealth: (Boolean) -> Unit,
    onToggleLowPower: (Boolean) -> Unit,
    onClearHistory: () -> Unit,
    onLogout: () -> Unit,
    onAcceptLink: (P2PDevice) -> Unit,
    modifier: Modifier = Modifier
) {
    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    
    val isLocationMandatory = Build.VERSION.SDK_INT < Build.VERSION_CODES.S
    // Hardened: The air is still if Bluetooth is OFF OR critical permissions are missing
    val airIsStill = !isBluetoothEnabled || (isLocationMandatory && !isLocationEnabled) || !permissionsGranted

    Surface(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
        color = Color.Black.copy(alpha = 0.95f),
        shape = RoundedCornerShape(32.dp),
        border = BorderStroke(
            1.dp, 
            if (airIsStill) Color.Red.copy(alpha = 0.5f) else StealthPrimary.copy(alpha = 0.2f)
        ),
        tonalElevation = 12.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            EnergyBarContent(
                isBluetoothOff = !isBluetoothEnabled,
                isLocationOff = isLocationMandatory && !isLocationEnabled,
                isWifiOff = !isWifiEnabled,
                isPermissionMissing = !permissionsGranted,
                isStealthMode = isStealthMode,
                lowPowerMode = lowPowerMode,
                onToggleStealth = onToggleStealth,
                onToggleLowPower = onToggleLowPower,
                onAwakenBluetooth = onAwakenBluetooth,
                onAwakenLocation = onAwakenLocation,
                onAwakenWifi = onAwakenWifi,
                userCount = userCount,
                vibeCount = roarsCount + vibesCount,
                isPermanentlyDenied = isPermanentlyDenied,
                onGrantPermissions = onGrantPermissions,
                onOpenSettings = onOpenSettings,
                onClearHistory = { showClearHistoryDialog = true }
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    VisualEnergyPicker(
                        currentRoute = currentRoute,
                        userCount = userCount,
                        linksCount = linksCount,
                        energy = energy,
                        rotation = rotation,
                        lowPowerMode = lowPowerMode,
                        onNavigate = onNavigate
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(horizontalAlignment = Alignment.End) {
                    val statusText = when {
                        airIsStill -> null
                        incomingLinkRequests.isNotEmpty() -> "VIBE REQUEST"
                        !currentBreeze.isNullOrBlank() -> currentBreeze
                        else -> "SYNCED"
                    }
                    
                    if (statusText != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = statusText.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 5.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if(incomingLinkRequests.isNotEmpty()) StealthPrimary else Color.White.copy(alpha = 0.4f),
                                    letterSpacing = 0.5.sp
                                )
                            )
                            if (incomingLinkRequests.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "JOIN",
                                    modifier = Modifier
                                        .testTag("AcceptLinkButton")
                                        .clickable { onAcceptLink(incomingLinkRequests.first()) },
                                    color = StealthPrimary,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 7.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                    }

                    var localNickname by remember(nickname) { mutableStateOf(nickname) }
                    
                    BasicTextField(
                        value = localNickname,
                        onValueChange = { newName ->
                            localNickname = newName
                            onSaveNickname(newName.ifBlank { "UNKNOWN" })
                        },
                        modifier = Modifier
                            .width(80.dp)
                            .focusRequester(focusRequester)
                            .testTag("IdentityVibeInput"),
                        textStyle = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = if (airIsStill) Color.Red else StealthPrimary,
                            textAlign = TextAlign.End,
                            letterSpacing = 0.5.sp
                        ),
                        singleLine = true,
                        cursorBrush = SolidColor(StealthPrimary)
                    )
                    Text(
                        text = "(YOU)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 6.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.3f)
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "RESET PROFILE",
                        modifier = Modifier.clickable { showLogoutDialog = true },
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 6.sp, 
                            fontWeight = FontWeight.Black, 
                            color = Color.White.copy(alpha = 0.2f),
                            letterSpacing = 0.5.sp
                        )
                    )
                }
            }
        }
    }

    if (showClearHistoryDialog) {
        ConfirmationDialog(
            title = "CLEAR VIBES?",
            text = "THIS WILL PERMANENTLY REMOVE YOUR SHARED HISTORY.",
            onConfirm = {
                onClearHistory()
                showClearHistoryDialog = false
            },
            onDismiss = { showClearHistoryDialog = false }
        )
    }

    if (showLogoutDialog) {
        ConfirmationDialog(
            title = "RESET PROFILE?",
            text = "THIS WILL DELETE YOUR LOCAL BLUKIT IDENTITY.",
            onConfirm = {
                onLogout()
                showLogoutDialog = false
            },
            onDismiss = { showLogoutDialog = false }
        )
    }
}

@Composable
private fun EnergyBarContent(
    isBluetoothOff: Boolean,
    isLocationOff: Boolean,
    isWifiOff: Boolean,
    isPermissionMissing: Boolean,
    isStealthMode: Boolean,
    lowPowerMode: Boolean,
    onToggleStealth: (Boolean) -> Unit,
    onToggleLowPower: (Boolean) -> Unit,
    onAwakenBluetooth: () -> Unit,
    onAwakenLocation: () -> Unit,
    onAwakenWifi: () -> Unit,
    userCount: Int,
    vibeCount: Int,
    isPermanentlyDenied: Boolean,
    onGrantPermissions: () -> Unit,
    onOpenSettings: () -> Unit,
    onClearHistory: () -> Unit
) {
    val pulseAlpha by rememberInfiniteTransition(label = "AlertPulse").animateFloat(
        initialValue = 0.6f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1000, easing = LinearEasing), RepeatMode.Reverse),
        label = "Alpha"
    )

    val isWeak = userCount == 0 && !isBluetoothOff && !isLocationOff
    val isStill = isBluetoothOff || isLocationOff
    
    Surface(
        color = if (isStill) Color.Red.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.03f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (isStill) Color.Red.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusIcon(icon = Icons.Rounded.Bluetooth, isOn = !isBluetoothOff, isWeak = isWeak, isPermissionMissing = isPermissionMissing, onClick = onAwakenBluetooth)
                    StatusIcon(icon = Icons.Rounded.Wifi, isOn = !isWifiOff, isWeak = isWeak, isPermissionMissing = isPermissionMissing, onClick = onAwakenWifi)
                    StatusIcon(icon = Icons.Rounded.LocationOn, isOn = !isLocationOff, isWeak = isWeak, isPermissionMissing = isPermissionMissing, onClick = onAwakenLocation)
                }

                if (isStill || isPermissionMissing) {
                    val action = when {
                        isStill -> if (isBluetoothOff) onAwakenBluetooth else onAwakenLocation
                        isPermissionMissing -> if (isPermanentlyDenied) onOpenSettings else onGrantPermissions
                        else -> null
                    }
                    
                    Text(
                        text = "ENERGY REQUIRED",
                        modifier = Modifier.testTag("EnergyRequiredLabel"),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 6.sp, fontWeight = FontWeight.Black, color = Color.White.copy(alpha = 0.8f))
                    )

                    if (action != null) {
                        Surface(
                            onClick = { action.invoke() },
                            color = Color.White,
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.graphicsLayer { alpha = pulseAlpha }
                        ) {
                            val btnText = if (isStill) "AWAKEN" else "GRANT"
                            Text(
                                text = btnText.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.sp, fontWeight = FontWeight.Black, color = Color.Red),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                EnvironmentToggle(label = "DARK MODE", checked = isStealthMode, onCheckedChange = onToggleStealth)
                EnvironmentToggle(label = "LOW BATTERY MODE", checked = lowPowerMode, onCheckedChange = onToggleLowPower)
                
                // Moved: Clear Vibes
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .clickable { onClearHistory() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = vibeCount.toString(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White.copy(alpha = 0.3f)
                        )
                    )
                    Icon(
                        imageVector = Icons.Rounded.DeleteSweep,
                        contentDescription = "Clear Vibes",
                        tint = Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EnvironmentToggle(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label, 
            fontSize = 6.sp, 
            fontWeight = FontWeight.Black, 
            color = if(checked) StealthPrimary else Color.White.copy(alpha = 0.3f),
            letterSpacing = 1.sp
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.scale(0.55f).height(20.dp),
            colors = SwitchDefaults.colors(checkedThumbColor = StealthPrimary, checkedTrackColor = StealthPrimary.copy(alpha = 0.5f))
        )
    }
}

@Composable
private fun VisualEnergyPicker(
    currentRoute: Route, 
    userCount: Int, 
    linksCount: Int, 
    energy: Float,
    rotation: Float,
    lowPowerMode: Boolean,
    onNavigate: (Route) -> Unit
) {
    Row(
        modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = 0.05f)).padding(2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val isCrowd = currentRoute is Route.Crowd
        
        // CROWD Tab (Replaced with Animated BLUKIT)
        Surface(
            onClick = { onNavigate(Route.Crowd) },
            shape = RoundedCornerShape(10.dp),
            color = if (isCrowd) StealthPrimary else Color.Transparent,
            modifier = Modifier.height(44.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally, 
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Box(modifier = Modifier.scale(0.5f)) {
                    BlukitHeartbeat(energy = energy, rotation = rotation, lowPowerMode = lowPowerMode)
                }
                Text(
                    text = "BLUKIT ($userCount)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 7.sp, 
                        fontWeight = FontWeight.Black, 
                        color = if (isCrowd) Color.Black else Color.White.copy(alpha = 0.4f),
                        letterSpacing = 0.5.sp
                    )
                )
            }
        }

        // FRIENDS Tab (Renamed to KNOWN)
        EnergyButton(label = "KNOWN", count = linksCount, active = !isCrowd, onClick = { onNavigate(Route.Vibes) })
    }
}

@Composable
private fun EnergyButton(label: String, count: Int, active: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (active) StealthPrimary else Color.Transparent,
        modifier = Modifier.height(44.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 12.dp)) {
            Text(
                text = "$label ($count)",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 8.sp, 
                    fontWeight = FontWeight.Black, 
                    color = if (active) Color.Black else Color.White.copy(alpha = 0.4f),
                    letterSpacing = 0.5.sp
                )
            )
        }
    }
}

@Composable
private fun ConfirmationDialog(title: String, text: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.Black,
        titleContentColor = StealthPrimary,
        textContentColor = Color.White.copy(alpha = 0.7f),
        title = { Text(title, fontWeight = FontWeight.Black) },
        text = { Text(text, fontSize = 12.sp) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("PROCEED", color = Color.Red, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("CANCEL", color = Color.White.copy(alpha = 0.4f))
            }
        }
    )
}

@Composable
private fun StatusIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, isOn: Boolean, isWeak: Boolean, isPermissionMissing: Boolean, onClick: () -> Unit) {
    val tint = when {
        !isOn -> Color.Red
        isPermissionMissing || isWeak -> Color.Yellow
        else -> Color.Green
    }
    Icon(
        imageVector = icon, contentDescription = null, tint = tint.copy(alpha = 0.8f),
        modifier = Modifier.size(20.dp).clickable { onClick() }
    )
}

@Composable
private fun FullLighthouseScan(rotation: Float, lowPowerMode: Boolean) {
    if (lowPowerMode && rotation % 10 > 2) return
    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(56.dp.toPx(), size.height - 64.dp.toPx())
        rotate(rotation, pivot = center) {
            val scanBrush = Brush.sweepGradient(
                0.0f to StealthPrimary.copy(alpha = if (lowPowerMode) 0.05f else 0.15f), 
                0.1f to Color.Transparent, 
                center = center
            )
            drawCircle(brush = scanBrush, radius = size.maxDimension, center = center)
        }
    }
}
