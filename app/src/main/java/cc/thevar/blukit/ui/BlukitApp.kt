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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import cc.thevar.blukit.ui.viewmodels.*
import cc.thevar.blukit.ui.viewmodels.ViewModelFactory

@Composable
fun rememberSpreadPermissionsState(permissions: List<String>): SpreadPermissionsState {
    val context = LocalContext.current
    var allGranted by remember {
        mutableStateOf(permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        })
    }
    var shouldShowRationale by remember {
        mutableStateOf(permissions.any {
            (context as? Activity)?.shouldShowRequestPermissionRationale(it) == true
        })
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        allGranted = result.values.all { it }
        shouldShowRationale = permissions.any {
            (context as? Activity)?.shouldShowRequestPermissionRationale(it) == true
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
                return BluetoothViewModel(p2pController, radioStateManager) as T
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
    val emojiAvatar by viewModel.emojiAvatar.collectAsStateWithLifecycle(initialValue = "👤")
    val isStealthMode by viewModel.isStealthMode.collectAsStateWithLifecycle(initialValue = false)
    val lowPowerMode by viewModel.lowPowerMode.collectAsStateWithLifecycle(initialValue = false)
    val bluetoothState by bluetoothViewModel.state.collectAsStateWithLifecycle()
    val report by supremePowerViewModel.report.collectAsStateWithLifecycle()
    val energySurge by bluetoothViewModel.energySurge.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(bluetoothState.uiError) {
        bluetoothState.uiError?.let { error ->
            val message = when (error) {
                is UiError.SecureChannelFailed -> "ROAR FAIL: ${error.message.uppercase()}"
                else -> error.message.uppercase()
            }
            snackbarHostState.showSnackbar(
                message = message,
                duration = if (error.isCritical) SnackbarDuration.Long else SnackbarDuration.Short
            )
        }
    }

    // Global Permission State for the Magic Bar
    val permissions = buildList {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            add(Manifest.permission.BLUETOOTH_SCAN)
            add(Manifest.permission.BLUETOOTH_ADVERTISE)
            add(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.NEARBY_WIFI_DEVICES)
        }
    }
    val permissionState = rememberSpreadPermissionsState(permissions = permissions)
    
    val isPermanentlyDenied = !permissionState.allPermissionsGranted && !permissionState.shouldShowRationale

    val initialRoute = Route.Crowd
    val backStack = rememberNavBackStack(initialRoute)
    val currentRoute = backStack.lastOrNull()

    val listDetailSceneStrategy = rememberListDetailSceneStrategy<NavKey>()

    Box(modifier = modifier.fillMaxSize()) {
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
                        localEmoji = emojiAvatar,
                        energySurge = energySurge,
                        onStartScan = bluetoothViewModel::startScan,
                        onStopScan = bluetoothViewModel::stopScan,
                        onDeviceClick = { device ->
                            if (device.id !in bluetoothState.connectedLinks) {
                                bluetoothViewModel.connectToDevice(device)
                            } else {
                                backStack.add(Route.Chat)
                            }
                        },
                        onBroadcastMessage = bluetoothViewModel::roar
                    )
                }
                Route.Chat -> NavEntry(key, metadata = ListDetailSceneStrategy.detailPane()) {
                    TieScreen(
                        state = bluetoothState,
                        localDeviceId = viewModel.deviceId.collectAsStateWithLifecycle(initialValue = "").value,
                        localEmoji = emojiAvatar,
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
                        localEmoji = emojiAvatar,
                        energySurge = energySurge,
                        onlyTies = true,
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

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
        )

        val globalSubtitle = when {
            bluetoothState.connectedLinks.isNotEmpty() -> "PROXIMITY: ${bluetoothState.connectedLinks.size}"
            bluetoothState.connectionState is AirConnectionState.Scanning -> "VIBES PROXIMITY…"
            bluetoothState.connectionState is AirConnectionState.Connecting -> "ENERGY PROXIMITY…"
            else -> "SPREAD VIBES"
        }

        UnifiedBlukitBadge(
            subtitle = globalSubtitle,
            energy = energySurge,
            userCount = report.userCount,
            linksCount = report.connectedLinksCount,
            aiInsight = report.aiInsight,
            currentBreeze = report.currentBreeze,
            isBluetoothEnabled = bluetoothState.isBluetoothEnabled,
            isLocationEnabled = bluetoothState.isLocationEnabled,
            permissionsGranted = permissionState.allPermissionsGranted,
            isPermanentlyDenied = isPermanentlyDenied,
            isStealthMode = isStealthMode,
            lowPowerMode = lowPowerMode,
            currentRoute = (currentRoute as? Route) ?: initialRoute,
            nickname = nickname ?: "vibe",
            incomingLinkRequests = bluetoothState.incomingLinkRequests,
            onNavigate = { route ->
                if (currentRoute != route) {
                    backStack.add(route)
                }
            },
            onAwakenBluetooth = { context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS)) },
            onAwakenLocation = { context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) },
            onGrantPermissions = { permissionState.launchMultiplePermissionRequest() },
            onOpenSettings = {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            },
            onSaveNickname = viewModel::saveNickname,
            onToggleStealth = viewModel::toggleStealth,
            onToggleLowPower = viewModel::toggleLowPowerMode,
            onClearHistory = viewModel::clearChatHistory,
            onLogout = viewModel::logout,
            onAcceptLink = bluetoothViewModel::acceptLink,
            onDenyLink = bluetoothViewModel::denyLink,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        )

        if (permissionState.shouldShowRationale && !permissionState.allPermissionsGranted) {
            AlertDialog(
                onDismissRequest = { /* Require choice */ },
                containerColor = Color.Black,
                titleContentColor = StealthPrimary,
                textContentColor = Color.White.copy(alpha = 0.7f),
                title = { Text("BLUKIT ENERGY", fontWeight = FontWeight.Black) },
                text = { Text("SPREAD VIBES. HEAR THE CROWD ROAR. PROXIMITY ENERGY.", fontSize = 12.sp) },
                confirmButton = {
                    TextButton(onClick = { permissionState.launchMultiplePermissionRequest() }) {
                        Text("VIBES", color = StealthPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}

@Composable
fun UnifiedBlukitBadge(
    subtitle: String,
    energy: Float,
    userCount: Int,
    linksCount: Int,
    aiInsight: String,
    currentBreeze: String?,
    isBluetoothEnabled: Boolean,
    isLocationEnabled: Boolean,
    permissionsGranted: Boolean,
    isPermanentlyDenied: Boolean,
    isStealthMode: Boolean,
    lowPowerMode: Boolean,
    currentRoute: Route,
    nickname: String,
    incomingLinkRequests: Set<P2PDevice>,
    onNavigate: (Route) -> Unit,
    onAwakenBluetooth: () -> Unit,
    onAwakenLocation: () -> Unit,
    onGrantPermissions: () -> Unit,
    onOpenSettings: () -> Unit,
    onSaveNickname: (String) -> Unit,
    onToggleStealth: (Boolean) -> Unit,
    onToggleLowPower: (Boolean) -> Unit,
    onClearHistory: () -> Unit,
    onLogout: () -> Unit,
    onAcceptLink: (P2PDevice) -> Unit,
    onDenyLink: (P2PDevice) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    
    val isLocationMandatory = Build.VERSION.SDK_INT < Build.VERSION_CODES.S
    val airIsStill = !isBluetoothEnabled || (isLocationMandatory && !isLocationEnabled) || !permissionsGranted
    val hasBreeze = !currentBreeze.isNullOrBlank()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color.Black.copy(alpha = 0.8f))
                .border(
                    width = 0.5.dp, 
                    color = (if (airIsStill) StealthAmber else if (hasBreeze) StealthPrimary else StealthPrimary).copy(alpha = 0.3f), 
                    shape = RoundedCornerShape(24.dp)
                )
                .testTag("BlukitBadge")
                .clickable { expanded = !expanded }
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Left: Smart Branding & Dynamic Status - Center Vibe Heartbeat moved here
                BlukitHeartbeat(energy = energy, modifier = Modifier.padding(end = 4.dp))

                Spacer(modifier = Modifier.width(8.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    // Dynamic Status Bar (Top)
                    Text(
                        text = (if (airIsStill) "VIBES STILL" 
                                else if (!currentBreeze.isNullOrBlank()) currentBreeze 
                                else if (incomingLinkRequests.isNotEmpty()) "VIBE PROXIMITY"
                                else subtitle).uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Black,
                            color = (if (airIsStill) StealthAmber 
                                    else if (!currentBreeze.isNullOrBlank() || incomingLinkRequests.isNotEmpty()) StealthPrimary 
                                    else Color.White).copy(alpha = 0.6f),
                            letterSpacing = 0.5.sp
                        )
                    )
                    
                    // Static Branding (Bottom)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "BLUKIT",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp,
                                color = if (airIsStill) StealthAmber else StealthPrimary
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(modifier = Modifier.size(1.dp, 10.dp).background(Color.White.copy(alpha = 0.2f)))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SPREAD VIBES",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 7.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = Color.White.copy(alpha = 0.4f)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(0.1f))

                // Right: Contextual Animated Icon
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        val infiniteTransition = rememberInfiniteTransition(label = "IconPulse")
                        val pulseScale by infiniteTransition.animateFloat(
                            initialValue = 0.9f,
                            targetValue = 1.2f,
                            animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse),
                            label = "Pulse"
                        )
                        
                        Box(
                            modifier = Modifier
                                .size(24.dp * pulseScale)
                                .background(
                                    Brush.radialGradient(
                                        listOf((if (airIsStill) StealthAmber else StealthPrimary).copy(alpha = 0.15f), Color.Transparent)
                                    ),
                                    CircleShape
                                )
                        )
                        
                        Icon(
                            imageVector = Icons.Rounded.AccountCircle,
                            contentDescription = null,
                            tint = if (airIsStill) StealthAmber else StealthPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = if (currentRoute is Route.Vibes) "ROAR" else "VIBES",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 6.sp,
                            fontWeight = FontWeight.Black,
                            color = (if (airIsStill) StealthAmber else StealthPrimary).copy(alpha = 0.6f),
                            letterSpacing = 1.sp
                        )
                    )
                }
            }

            AnimatedVisibility(
                visible = airIsStill || expanded || hasBreeze || incomingLinkRequests.isNotEmpty(),
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    if (airIsStill || hasBreeze || incomingLinkRequests.isNotEmpty()) {
                        val requests = incomingLinkRequests
                        Spacer(modifier = Modifier.height(16.dp))
                        MagicBarContent(
                            isBluetoothOff = !isBluetoothEnabled,
                            isLocationOff = isLocationMandatory && !isLocationEnabled,
                            isPermissionMissing = !permissionsGranted,
                            isPermanentlyDenied = isPermanentlyDenied,
                            currentBreeze = currentBreeze,
                            incomingRequests = requests,
                            onAcceptLink = onAcceptLink,
                            onDenyLink = onDenyLink,
                            onAwakenBluetooth = onAwakenBluetooth,
                            onAwakenLocation = onAwakenLocation,
                            onGrantPermissions = onGrantPermissions,
                            onOpenSettings = onOpenSettings
                        )
                    }

                    if (expanded) {
                        Spacer(modifier = Modifier.height(24.dp))

                        // Integrated Vibe Identity (Minimalist, no Emojis)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                                .padding(16.dp)
                        ) {
                            var localNickname by remember(nickname) { mutableStateOf(nickname) }
                            TextField(
                                value = localNickname,
                                onValueChange = { 
                                    localNickname = it
                                    onSaveNickname(it.ifBlank { "vibe" })
                                },
                                modifier = Modifier.weight(1f).testTag("IdentityVibeInput"),
                                label = { Text("BLUKIT VIBE", fontSize = 8.sp, color = StealthPrimary.copy(alpha = 0.5f)) },
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White, fontWeight = FontWeight.Bold),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = StealthPrimary,
                                    unfocusedIndicatorColor = StealthPrimary.copy(alpha = 0.2f)
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Column(modifier = Modifier.testTag("IntelSection")) {
                            IntelRow(
                                label = "CROWD", 
                                value = userCount.toString(),
                                icon = Icons.Rounded.AccountCircle,
                                modifier = Modifier.testTag("VibesStat"),
                                onClick = { 
                                    onNavigate(Route.Crowd)
                                    expanded = false 
                                }
                            )
                            IntelRow(
                                label = "VIBES", 
                                value = linksCount.toString(),
                                icon = Icons.Rounded.AccountCircle,
                                modifier = Modifier.testTag("TiesStat"),
                                onClick = { 
                                    onNavigate(Route.Vibes)
                                    expanded = false
                                }
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        // Settings Sections (Einstein Minimalist)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                                .padding(12.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "ENERGY GLOW",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "PROXIMITY ENERGY",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = StealthPrimary.copy(alpha = 0.5f),
                                    fontSize = 7.sp
                                )
                            }
                            Switch(
                                checked = isStealthMode,
                                onCheckedChange = onToggleStealth,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = StealthPrimary,
                                    checkedTrackColor = StealthPrimary.copy(alpha = 0.5f)
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                                .padding(12.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "BLUKIT ENERGY",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "VIBE PROXIMITY",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = StealthPrimary.copy(alpha = 0.5f),
                                    fontSize = 7.sp
                                )
                            }
                            Switch(
                                checked = lowPowerMode,
                                onCheckedChange = onToggleLowPower,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = StealthPrimary,
                                    checkedTrackColor = StealthPrimary.copy(alpha = 0.5f)
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        var showStillnessLocal by remember { mutableStateOf(false) }
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                                .clickable { showStillnessLocal = !showStillnessLocal }
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (showStillnessLocal) "▼ ROAR" else "▶ ROAR",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (showStillnessLocal) StealthPrimary else StealthPrimary.copy(alpha = 0.4f),
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                            }

                            if (showStillnessLocal) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = { showClearHistoryDialog = true },
                                        modifier = Modifier.weight(1f),
                                        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.2f)),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White.copy(alpha = 0.7f)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("CLEAR VIBES", fontSize = 7.sp, fontWeight = FontWeight.Bold)
                                    }
                                    OutlinedButton(
                                        onClick = { showLogoutDialog = true },
                                        modifier = Modifier.weight(1f),
                                        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.2f)),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White.copy(alpha = 0.7f)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("RESET BLUKIT", fontSize = 7.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = StealthPrimary.copy(alpha = 0.1f)
                        )
                        
                        Text(
                            text = aiInsight.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(lineHeight = 14.sp),
                            color = Color.White.copy(alpha = 0.8f),
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    }

    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            containerColor = Color.Black,
            titleContentColor = StealthPrimary,
            textContentColor = Color.White.copy(alpha = 0.7f),
            title = { Text("CLEAR VIBES?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black) },
            text = { Text("CLEAR VIBE ENERGY.", fontSize = 12.sp) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearHistory()
                        showClearHistoryDialog = false
                    }
                ) {
                    Text("CLEAR", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text("CANCEL", color = Color.White.copy(alpha = 0.4f))
                }
            }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = Color.Black,
            titleContentColor = StealthPrimary,
            textContentColor = Color.White.copy(alpha = 0.7f),
            title = { Text("NEW BLUKIT?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black) },
            text = { Text("RESET BLUKIT VIBES.", fontSize = 12.sp) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onLogout()
                        showLogoutDialog = false
                    }
                ) {
                    Text("RESET", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("CANCEL", color = Color.White.copy(alpha = 0.4f))
                }
            }
        )
    }
}

@Composable
private fun MagicBarContent(
    isBluetoothOff: Boolean,
    isLocationOff: Boolean,
    isPermissionMissing: Boolean,
    isPermanentlyDenied: Boolean,
    currentBreeze: String?,
    incomingRequests: Set<P2PDevice>,
    onAcceptLink: (P2PDevice) -> Unit,
    onDenyLink: (P2PDevice) -> Unit,
    onAwakenBluetooth: () -> Unit,
    onAwakenLocation: () -> Unit,
    onGrantPermissions: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "AwakenPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Alpha"
    )

    val isStill = isBluetoothOff || isLocationOff || isPermissionMissing
    val hasRequests = incomingRequests.isNotEmpty()
    val barColor = if (isStill) StealthAmber else StealthPrimary

    Surface(
        color = barColor.copy(alpha = 0.05f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(0.5.dp, barColor.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when {
                        isPermissionMissing -> "BLUKIT ENERGY"
                        isBluetoothOff && isLocationOff -> "VIBES STILL"
                        isBluetoothOff -> "VIBES STILL"
                        isLocationOff -> "VIBES STILL"
                        hasRequests -> "PROXIMITY"
                        !currentBreeze.isNullOrBlank() -> "ENERGY"
                        else -> "VIBES ROAR"
                    },
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    ),
                    color = barColor.copy(alpha = pulseAlpha),
                    modifier = Modifier.weight(1f)
                )

                if (isStill) {
                    val action = when {
                        isPermissionMissing -> if (isPermanentlyDenied) onOpenSettings else onGrantPermissions
                        isBluetoothOff -> onAwakenBluetooth
                        isLocationOff -> onAwakenLocation
                        else -> null
                    }

                    if (action != null) {
                        Text(
                            text = if (isPermissionMissing && isPermanentlyDenied) "OPEN SETTINGS" else "AWAKEN",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 8.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.Black
                            ),
                            modifier = Modifier
                                .graphicsLayer { alpha = pulseAlpha }
                                .clip(RoundedCornerShape(4.dp))
                                .background(StealthAmber)
                                .clickable { action() }
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            
            val description = when {
                hasRequests -> "${(incomingRequests.first().name ?: "VIBE").uppercase()} PROXIMITY."
                isPermissionMissing -> "SPREAD VIBES. HEAR THE CROWD ROAR. PROXIMITY ENERGY."
                isBluetoothOff -> "VIBES STILL. ENERGY REQUIRED."
                isLocationOff -> "VIBES STILL. ENERGY REQUIRED."
                !currentBreeze.isNullOrBlank() -> currentBreeze
                else -> null
            }

            if (description != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 10.sp
                        ),
                        color = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.weight(1f)
                    )
                    
                    if (hasRequests) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                "VIBE",
                                color = StealthPrimary,
                                fontWeight = FontWeight.Black,
                                fontSize = 8.sp,
                                modifier = Modifier
                                    .testTag("AcceptLinkButton")
                                    .clickable { onAcceptLink(incomingRequests.first()) }
                            )
                            Text(
                                "IGNORE",
                                color = Color.White.copy(alpha = 0.3f),
                                fontWeight = FontWeight.Bold,
                                fontSize = 8.sp,
                                modifier = Modifier.clickable { onDenyLink(incomingRequests.first()) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IntelRow(
    label: String, 
    value: String,
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    textIcon: String? = null,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = StealthPrimary.copy(alpha = 0.4f),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            } else if (textIcon != null) {
                Text(
                    text = textIcon,
                    fontSize = 12.sp,
                    modifier = Modifier.alpha(0.6f)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = label, 
                style = MaterialTheme.typography.labelSmall, 
                color = Color.White.copy(alpha = 0.4f)
            )
        }
        Text(
            text = value, 
            style = MaterialTheme.typography.labelSmall, 
            color = StealthPrimary, 
            fontWeight = FontWeight.Bold
        )
    }
}
