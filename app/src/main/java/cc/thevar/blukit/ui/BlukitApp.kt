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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
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
import cc.thevar.blukit.ui.screens.BlukitInput

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

    val infiniteTransition = rememberInfiniteTransition(label = "HubLighthouse")
    val hubRotation by infiniteTransition.animateFloat(
        initialValue = 0f, 
        targetValue = 360f, 
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing)), 
        label = "Scan"
    )

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
        
        // Global Lighthouse Scan (Glows from the Hub Badge over the Field/Ticker)
        FullLighthouseScan(rotation = hubRotation, lowPowerMode = lowPowerMode)

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
        )

        val roarsCount = remember(bluetoothState.messages) {
            bluetoothState.messages.count { it.receiverId.isNullOrBlank() }
        }
        val vibesCount = remember(bluetoothState.messages) {
            bluetoothState.messages.count { !it.receiverId.isNullOrBlank() }
        }

        // Global "SEND VIBES" input field positioned above the Hub
        var messageText by remember { mutableStateOf("") }
        val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
        
        Column(
            modifier = Modifier.align(Alignment.BottomCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Feedback 15/16: Global Send Vibes field with count in corner
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
                lowPowerMode = lowPowerMode,
                permissionsGranted = permissionState.allPermissionsGranted,
                isPermanentlyDenied = isPermanentlyDenied,
                isStealthMode = isStealthMode,
                currentBreeze = report.currentBreeze,
                isBluetoothEnabled = bluetoothState.isBluetoothEnabled,
                isLocationEnabled = bluetoothState.isLocationEnabled,
                isWifiEnabled = bluetoothState.isWifiEnabled,
                currentRoute = (currentRoute as? Route) ?: initialRoute,
                nickname = nickname ?: "vibe",
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
                    context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
                },
                onSaveNickname = viewModel::saveNickname,
                onToggleStealth = viewModel::toggleStealth,
                onToggleLowPower = viewModel::toggleLowPowerMode,
                onClearHistory = viewModel::clearChatHistory,
                onLogout = viewModel::logout,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }

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
    energy: Float,
    rotation: Float,
    userCount: Int,
    linksCount: Int,
    lowPowerMode: Boolean,
    permissionsGranted: Boolean,
    isPermanentlyDenied: Boolean,
    isStealthMode: Boolean,
    currentBreeze: String?,
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
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    
    val isLocationMandatory = Build.VERSION.SDK_INT < Build.VERSION_CODES.S
    val airIsStill = !isBluetoothEnabled || (isLocationMandatory && !isLocationEnabled) || !permissionsGranted

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
                    color = (if (airIsStill) Color.Red else StealthPrimary).copy(alpha = 0.3f), 
                    shape = RoundedCornerShape(24.dp)
                )
                .testTag("BlukitBadge")
                .clickable { expanded = !expanded }
                .padding(16.dp)
        ) {
            // Feedback 14/15/16: Radios Bar at the TOP of the badge (Always visible)
            MagicBarContent(
                isBluetoothOff = !isBluetoothEnabled,
                isLocationOff = isLocationMandatory && !isLocationEnabled,
                isWifiOff = !isWifiEnabled,
                isPermissionMissing = !permissionsGranted,
                isPermanentlyDenied = isPermanentlyDenied,
                onAwakenBluetooth = onAwakenBluetooth,
                onAwakenLocation = onAwakenLocation,
                onAwakenWifi = onAwakenWifi,
                onGrantPermissions = onGrantPermissions,
                onOpenSettings = onOpenSettings
            )
            
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Left: Sentient Branding
                Column(
                    modifier = Modifier.padding(end = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    BlukitHeartbeat(energy = energy, rotation = rotation, lowPowerMode = lowPowerMode)
                    Text(
                        text = "BLUKIT",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = if (airIsStill) Color.Red else StealthPrimary
                        )
                    )
                    Text(
                        text = "CROWD ENERGY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.3f)
                        )
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))
                
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    val statusText = when {
                        airIsStill -> null
                        !currentBreeze.isNullOrBlank() -> currentBreeze
                        else -> "HEAR THE CROWD ROAR"
                    }
                    
                    if (statusText != null) {
                        Text(
                            text = statusText.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 7.sp,
                                fontWeight = FontWeight.Black,
                                color = StealthPrimary.copy(alpha = 0.8f),
                                letterSpacing = 0.5.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    // Feedback 11/15/16: CROWD / FRIENDS Picker with counts
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .padding(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        val isCrowd = currentRoute is Route.Crowd
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isCrowd) StealthPrimary else Color.Transparent)
                                .clickable { onNavigate(Route.Crowd) }
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "CROWD ($userCount)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 7.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isCrowd) Color.Black else Color.White.copy(alpha = 0.4f)
                                )
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (!isCrowd) StealthPrimary else Color.Transparent)
                                .clickable { onNavigate(Route.Vibes) }
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "FRIENDS ($linksCount)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 7.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (!isCrowd) Color.Black else Color.White.copy(alpha = 0.4f)
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(0.1f))

                // Right: User Identity (Feedback 11/16)
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = nickname.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = if (airIsStill) Color.Red else StealthPrimary,
                            letterSpacing = 1.sp
                        )
                    )
                    Text(
                        text = "(YOU)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 6.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.4f),
                            letterSpacing = 0.5.sp
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Feedback 15/16: Expansion Action (HUB)
                    val actionText = if (expanded) "CLOSE" else "HUB"
                    Surface(
                        onClick = { expanded = !expanded },
                        color = Color.White.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = actionText,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 6.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = StealthPrimary,
                                letterSpacing = 1.sp
                            )
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(24.dp))

                    // Integrated Vibe Identity
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
                            label = { Text("YOU", fontSize = 8.sp, color = StealthPrimary.copy(alpha = 0.5f)) },
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

                    // Settings Sections (Feedback 10: Clubbed Row)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Dark Mode Toggle
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Text(
                                text = "DARK MODE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = if (isStealthMode) StealthPrimary else Color.White.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Switch(
                                checked = isStealthMode,
                                onCheckedChange = onToggleStealth,
                                modifier = Modifier.scale(0.7f),
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = StealthPrimary,
                                    checkedTrackColor = StealthPrimary.copy(alpha = 0.5f)
                                )
                            )
                        }

                        // Battery Saver Toggle
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "BATTERY SAVER",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = if (lowPowerMode) StealthPrimary else Color.White.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Switch(
                                checked = lowPowerMode,
                                onCheckedChange = onToggleLowPower,
                                modifier = Modifier.scale(0.7f),
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = StealthPrimary,
                                    checkedTrackColor = StealthPrimary.copy(alpha = 0.5f)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Feedback 6: Top-level destructive buttons
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
                            Text("RESET PROFILE", fontSize = 7.sp, fontWeight = FontWeight.Bold)
                        }
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
            text = { Text("THIS WILL PERMANENTLY REMOVE YOUR SHARED HISTORY.", fontSize = 12.sp) },
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
            title = { Text("RESET PROFILE?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black) },
            text = { Text("THIS WILL DELETE YOUR LOCAL BLUKIT IDENTITY.", fontSize = 12.sp) },
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
    isWifiOff: Boolean,
    isPermissionMissing: Boolean,
    isPermanentlyDenied: Boolean,
    onAwakenBluetooth: () -> Unit,
    onAwakenLocation: () -> Unit,
    onAwakenWifi: () -> Unit,
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
    
    Surface(
        color = if (isStill) Color.Red.copy(alpha = 0.15f) else StealthPrimary.copy(alpha = 0.05f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(0.5.dp, if (isStill) Color.Red.copy(alpha = 0.5f) else StealthPrimary.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status Icons: Bluetooth (Mandatory), WiFi, Location (Optional)
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    StatusIcon(
                        icon = Icons.Rounded.Bluetooth, 
                        isOn = !isBluetoothOff, 
                        isPermissionMissing = isPermissionMissing,
                        isMandatory = true,
                        onClick = onAwakenBluetooth
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    StatusIcon(
                        icon = Icons.Rounded.Wifi, 
                        isOn = !isWifiOff, 
                        isPermissionMissing = isPermissionMissing,
                        isMandatory = false,
                        onClick = onAwakenWifi
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    StatusIcon(
                        icon = Icons.Rounded.LocationOn, 
                        isOn = !isLocationOff, 
                        isPermissionMissing = isPermissionMissing,
                        isMandatory = false,
                        onClick = onAwakenLocation
                    )
                }

                if (isStill) {
                    Text(
                        text = "RADIOS OFF",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        ),
                        modifier = Modifier.padding(end = 12.dp)
                    )

                    val action = when {
                        isPermissionMissing -> if (isPermanentlyDenied) onOpenSettings else onGrantPermissions
                        isBluetoothOff -> onAwakenBluetooth
                        isLocationOff -> onAwakenLocation
                        else -> onOpenSettings
                    }

                    if (action != null) {
                        Text(
                            text = if (isPermissionMissing && isPermanentlyDenied) "OPEN SETTINGS" else if (isPermissionMissing) "GRANT" else "TURN ON",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 8.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            ),
                            modifier = Modifier
                                .graphicsLayer { alpha = pulseAlpha }
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.White.copy(alpha = 0.2f))
                                .clickable { action() }
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isOn: Boolean,
    isPermissionMissing: Boolean,
    @Suppress("UNUSED_PARAMETER") isMandatory: Boolean,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "StatusAnim")
    
    val tint = when {
        isPermissionMissing -> Color.Yellow
        isOn -> Color.Green
        else -> Color.Red
    }

    val animScale by infiniteTransition.animateFloat(
        initialValue = if (isOn) 0.9f else 1f,
        targetValue = if (isOn) 1.1f else 1f,
        animationSpec = infiniteRepeatable(tween(1500), RepeatMode.Reverse),
        label = "Scale"
    )

    val animAlpha by infiniteTransition.animateFloat(
        initialValue = if (!isOn) 0.3f else 1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isPermissionMissing) 1000 else 500), 
            repeatMode = RepeatMode.Reverse
        ),
        label = "Alpha"
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isOn) 360f else 0f,
        animationSpec = infiniteRepeatable(tween(8000, easing = LinearEasing)),
        label = "Rotate"
    )

    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = tint.copy(alpha = if (isOn) 0.8f else animAlpha),
        modifier = Modifier
            .size(20.dp) 
            .clickable { onClick() }
            .graphicsLayer {
                scaleX = if (isOn) animScale else 1f
                scaleY = if (isOn) animScale else 1f
                rotationZ = rotation
            }
    )
}

@Composable
private fun FullLighthouseScan(rotation: Float, lowPowerMode: Boolean) {
    if (lowPowerMode && rotation % 10 > 2) return // Skip rendering 80% of frames in battery saver
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(56.dp.toPx(), size.height - 64.dp.toPx()) // Precisely aligned with the Hub Heartbeat
        val radius = if (lowPowerMode) size.minDimension / 2 else size.maxDimension
        
        rotate(rotation, pivot = center) {
            val scanBrush = Brush.sweepGradient(
                0.0f to StealthPrimary.copy(alpha = if (lowPowerMode) 0.1f else 0.3f), 
                0.05f to StealthPrimary.copy(alpha = if (lowPowerMode) 0.02f else 0.1f), 
                0.15f to Color.Transparent, 
                center = center
            )
            drawCircle(brush = scanBrush, radius = radius, center = center)
        }
    }
}

@Composable
private fun HubStat(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 6.sp,
                fontWeight = FontWeight.Bold,
                color = StealthPrimary.copy(alpha = 0.5f),
                letterSpacing = 1.sp
            )
        )
    }
}
