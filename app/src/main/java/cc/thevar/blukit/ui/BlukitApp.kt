package cc.thevar.blukit.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.BorderStroke
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import cc.thevar.blukit.BlukitApplication
import cc.thevar.blukit.R
import cc.thevar.blukit.data.local.VibeStore
import cc.thevar.blukit.data.power.SupremePowerManager
import cc.thevar.blukit.data.repository.IdentityRepository
import cc.thevar.blukit.data.system.RadioStateManager
import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.network.p2p.P2PController
import cc.thevar.blukit.domain.model.VibeGroup
import cc.thevar.blukit.ui.navigation.Route
import cc.thevar.blukit.ui.screens.*
import cc.thevar.blukit.ui.theme.StealthAmber
import cc.thevar.blukit.ui.theme.StealthPrimary
import cc.thevar.blukit.ui.theme.StealthRose
import cc.thevar.blukit.ui.viewmodels.*

@Composable
fun rememberSpreadPermissionsState(permissions: List<String>): SpreadPermissionsState {
    val context = LocalContext.current
    val manager: cc.thevar.blukit.data.system.SpreadPermissionManager = koinInject()
    var allGranted by remember { mutableStateOf(manager.checkAllGranted()) }
    var essentialGranted by remember { mutableStateOf(manager.checkEssentialGranted()) }
    var shouldShowRationale by remember { mutableStateOf(permissions.any { (context as? Activity)?.shouldShowRequestPermissionRationale(it) == true }) }
    
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        allGranted = manager.checkAllGranted()
        essentialGranted = manager.checkEssentialGranted()
        shouldShowRationale = permissions.any { (context as? Activity)?.shouldShowRequestPermissionRationale(it) == true }
        manager.refresh()
    }
    
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            allGranted = manager.checkAllGranted()
            essentialGranted = manager.checkEssentialGranted()
            shouldShowRationale = permissions.any { (context as? Activity)?.shouldShowRequestPermissionRationale(it) == true }
            manager.refresh()
        }
    }
    
    return remember(allGranted, essentialGranted, shouldShowRationale) {
        object : SpreadPermissionsState {
            override val allPermissionsGranted: Boolean = allGranted
            override val essentialPermissionsGranted: Boolean = essentialGranted
            override val shouldShowRationale: Boolean = shouldShowRationale
            override fun launchMultiplePermissionRequest() { launcher.launch(permissions.toTypedArray()) }
        }
    }
}

interface SpreadPermissionsState {
    val allPermissionsGranted: Boolean
    val essentialPermissionsGranted: Boolean
    val shouldShowRationale: Boolean
    fun launchMultiplePermissionRequest()
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun BlukitApp(
    onEnterPip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val permissionManager: cc.thevar.blukit.data.system.SpreadPermissionManager = koinInject()
    
    val viewModel: MainViewModel = koinViewModel()
    val bluetoothViewModel: BluetoothViewModel = koinViewModel()
    val supremePowerViewModel: SupremePowerViewModel = koinViewModel()
    
    val nickname by viewModel.nickname.collectAsStateWithLifecycle(initialValue = null)
    val emoji by viewModel.emojiAvatar.collectAsStateWithLifecycle(initialValue = "👤")
    val isStealthMode by viewModel.isStealthMode.collectAsStateWithLifecycle(initialValue = false)
    val lowPowerMode by viewModel.lowPowerMode.collectAsStateWithLifecycle(initialValue = false)
    val bluetoothState by bluetoothViewModel.state.collectAsStateWithLifecycle()
    val report by supremePowerViewModel.report.collectAsStateWithLifecycle()
    val energySurge by bluetoothViewModel.energySurge.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(bluetoothState.activity.uiError) { bluetoothState.activity.uiError?.let { snackbarHostState.showSnackbar(it.message.uppercase()) } }

    val permissionState = rememberSpreadPermissionsState(permissions = permissionManager.requiredPermissions)
    val isPermanentlyDenied = !permissionState.allPermissionsGranted && !permissionState.shouldShowRationale

    val initialRoute = Route.Blukit
    val backStack = rememberNavBackStack(initialRoute)
    val currentRoute = backStack.lastOrNull()

    val hubRotation by rememberInfiniteTransition(label = "HubScan").animateFloat(initialValue = 0f, targetValue = 360f, animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing)), label = "Scan")
    var selectedPersonaForMenu by remember { mutableStateOf<P2PDevice?>(null) }
    var isNoiseFilterActive by remember { mutableStateOf(false) }

    val listDetailSceneStrategy = rememberListDetailSceneStrategy<NavKey>()
    val personaFocusRequester = remember { FocusRequester() }
    val isLocationMandatory = Build.VERSION.SDK_INT < Build.VERSION_CODES.S
    val locationPermissionGranted = remember(permissionState.allPermissionsGranted) {
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }
    
    val airIsStill = !bluetoothState.harmony.isBluetoothEnabled || 
                     !permissionState.essentialPermissionsGranted ||
                     (isLocationMandatory && (!bluetoothState.harmony.isLocationEnabled || !locationPermissionGranted))

    var messageText by remember { mutableStateOf("") }
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    val allVibesCount = remember(bluetoothState.session.messages) { bluetoothState.session.messages.count { it.receiverId.isNullOrBlank() } }
    val secureVibesCount = remember(bluetoothState.session.messages) { bluetoothState.session.messages.count { !it.receiverId.isNullOrBlank() } }

    val config = LocalConfiguration.current
    val isLandscape = config.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    var showManageDialog by remember { mutableStateOf(false) }

    val personaCoordinates = remember { mutableStateMapOf<String, PersonaConnectionPoints>() }
    var highlightedUserId by remember { mutableStateOf<String?>(null) }
    var showAirIsStillDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(highlightedUserId) {
        if (highlightedUserId != null) {
            delay(3000)
            highlightedUserId = null
        }
    }

    CompositionLocalProvider(LocalPersonaCoordinates provides personaCoordinates) {
        Box(modifier = modifier.fillMaxSize()) {
            FullLighthouseScan(rotation = hubRotation, lowPowerMode = lowPowerMode)
            
            // Visual Connections Layer
            Canvas(modifier = Modifier.fillMaxSize().zIndex(5f)) {
                personaCoordinates.forEach { (id, points) ->
                    val isFocused = id == "YOU" || id in bluetoothState.crowd.vibedPeers || id in bluetoothState.session.connectedLinks
                    if (isNoiseFilterActive && !isFocused) return@forEach // Skip non-focused lines in filter mode
                    
                    val color = if (id == "YOU") StealthPrimary else if (id in bluetoothState.session.connectedLinks) StealthRose else StealthPrimary.copy(alpha = 0.4f)
                    val alphaMultiplier = if (isNoiseFilterActive) 1f else 0.6f
                    
                    // UPH -> Field
                    if (points.uph != null && points.field != null) {
                        drawLine(
                            color = color.copy(alpha = 0.15f * alphaMultiplier),
                            start = points.uph + Offset(26.dp.toPx(), 26.dp.toPx()),
                            end = points.field + Offset(32.dp.toPx(), 32.dp.toPx()),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )
                    }
                    
                    // Field -> Ticker
                    if (points.field != null && points.ticker != null) {
                        drawLine(
                            color = color.copy(alpha = 0.1f * alphaMultiplier),
                            start = points.field + Offset(32.dp.toPx(), 32.dp.toPx()),
                            end = points.ticker + Offset(20.dp.toPx(), 20.dp.toPx()),
                            strokeWidth = 0.5.dp.toPx()
                        )
                    }
                }
            }

            Column(modifier = Modifier.fillMaxSize()) {
                var focusedSenderId by remember { mutableStateOf<String?>(null) }
                
                val topTitle = when {
                    focusedSenderId != null -> {
                        val device = bluetoothState.crowd.scannedDevices.find { it.id == focusedSenderId || it.persistentId == focusedSenderId }
                        device?.name?.uppercase() ?: "USER"
                    }
                    currentRoute is Route.Blukit -> "ALL"
                    currentRoute is Route.Vibes -> "VIBES"
                    currentRoute is Route.VibeDetail -> {
                        val group = bluetoothState.session.groups.find { it.id == currentRoute.groupId }
                        group?.name ?: "VIBE"
                    }
                    else -> "BLUKIT"
                }
                
                val topIcon = when {
                    focusedSenderId != null -> Icons.Rounded.Person
                    currentRoute is Route.Blukit -> Icons.Rounded.Groups
                    currentRoute is Route.Vibes -> Icons.Rounded.Flare
                    currentRoute is Route.VibeDetail -> {
                        val group = bluetoothState.session.groups.find { it.id == currentRoute.groupId }
                        if (group?.type == VibeGroup.TYPE_TIE) Icons.Rounded.Flare else Icons.Rounded.Hearing
                    }
                    else -> Icons.Rounded.Hub
                }

                BlukitHarmonyTopBar(
                    title = topTitle, 
                    icon = topIcon,
                    isBluetoothOff = !bluetoothState.harmony.isBluetoothEnabled,
                    isLocationOff = !bluetoothState.harmony.isLocationEnabled,
                    isWifiOff = !bluetoothState.harmony.isWifiEnabled,
                    isPermissionMissing = !permissionState.allPermissionsGranted,
                    isPermanentlyDenied = isPermanentlyDenied,
                    userCount = report.userCount,
                    isStealthMode = isStealthMode,
                    lowPowerMode = lowPowerMode,
                    onToggleStealth = viewModel::toggleStealth,
                    onToggleLowPower = viewModel::toggleLowPowerMode,
                    onAwakenBluetooth = { context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS)) },
                    onAwakenLocation = { context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) },
                    onAwakenWifi = { context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS)) },
                    onGrantPermissions = { permissionState.launchMultiplePermissionRequest() },
                    onOpenSettings = { 
                        context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply { 
                            data = Uri.fromParts("package", context.packageName, null)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }) 
                    },
                    onBack = if (focusedSenderId != null || currentRoute is Route.VibeDetail) { 
                        { 
                            if (focusedSenderId != null) focusedSenderId = null 
                            else backStack.removeLastOrNull() 
                        } 
                    } else null,
                    onManage = if (currentRoute is Route.VibeDetail) { { showManageDialog = true } } else null
                )

                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    val cloudDevices = bluetoothState.crowd.scannedDevices
                    val showUPH = (currentRoute is Route.Blukit || currentRoute is Route.Vibes)

                    NavDisplay(backStack = backStack, onBack = { backStack.removeLastOrNull() }, sceneStrategy = listDetailSceneStrategy, modifier = Modifier.fillMaxSize()) { key ->
                        when (key) {
                            Route.Blukit -> NavEntry(key) { 
                                RipplesScreen(
                                    state = bluetoothState, 
                                    localDeviceId = viewModel.deviceId.collectAsStateWithLifecycle(initialValue = "").value, 
                                    localNickname = nickname ?: "?", 
                                    localEmoji = emoji, 
                                    energySurge = energySurge, 
                                    lowPowerMode = lowPowerMode, 
                                    vibedPeers = bluetoothState.crowd.vibedPeers, 
                                    noiseFilterEnabled = isNoiseFilterActive,
                                    onStartScan = bluetoothViewModel::startScan, 
                                    onStopScan = bluetoothViewModel::stopScan, 
                                    onDeviceClick = { device -> 
                                        if (bluetoothState.crowd.selectedDevices.isNotEmpty()) {
                                            bluetoothViewModel.toggleDeviceSelection(device.id)
                                        } else {
                                            val id = device.persistentId ?: device.id
                                            viewModel.toggleVibePeer(id)
                                        }
                                    }, 
                                    onDeviceLongClick = { selectedPersonaForMenu = it }, 
                                    onBroadcastMessage = bluetoothViewModel::spreadVibe, 
                                    onDeleteVibe = viewModel::deleteVibe, 
                                    onBlockUser = viewModel::blockUser, 
                                    onUnblockUser = viewModel::unblockUser,
                                    onWhisper = { device -> val id = device.persistentId ?: device.id; val gid = bluetoothViewModel.startGroupVibe("WHISPER", setOf(id), isTie = false); backStack.add(Route.VibeDetail(gid)) },
                                    onToggleSelection = bluetoothViewModel::toggleDeviceSelection,
                                    onAcceptLink = bluetoothViewModel::acceptLink,
                                    onDenyLink = bluetoothViewModel::denyLink,
                                    onDisconnect = bluetoothViewModel::disconnect,
                                    onIdentifyUser = { highlightedUserId = it },
                                    onClearFocus = { 
                                        viewModel.clearVibedPeers()
                                        isNoiseFilterActive = false
                                    },
                                    hasSidebar = showUPH,
                                    externalFocusedId = focusedSenderId,
                                    onFocusChange = { focusedSenderId = it }
                                ) 
                            }
                            Route.Vibes -> NavEntry(key) { 
                                ConversationsScreen(
                                    state = bluetoothState, 
                                    onVibeClick = { backStack.add(Route.VibeDetail(it.id)) }, 
                                    onDeleteGroup = bluetoothViewModel::deleteGroup,
                                    onAcceptLink = bluetoothViewModel::acceptLink,
                                    onDenyLink = bluetoothViewModel::denyLink
                                ) 
                            }
                            is Route.VibeDetail -> NavEntry(key) { 
                                TieScreen(
                                    state = bluetoothState, 
                                    localDeviceId = viewModel.deviceId.collectAsStateWithLifecycle(initialValue = "").value, 
                                    localEmoji = emoji, 
                                    groupId = key.groupId, 
                                    onDisconnect = bluetoothViewModel::disconnect, 
                                    onSendMessage = bluetoothViewModel::sendMessage, 
                                    onStartSideVibe = { peerId -> val gid = bluetoothViewModel.startGroupVibe("SIDE VIBE", setOf(peerId), isTie = false); backStack.add(Route.VibeDetail(gid)) }, 
                                    onToggleFocus = { device -> val id = device.persistentId ?: device.id; viewModel.toggleVibePeer(id) }, 
                                    onDeviceLongClick = { selectedPersonaForMenu = it },
                                    onBlockUser = viewModel::blockUser, 
                                    onAddMember = bluetoothViewModel::addMemberToGroup,
                                    onRemoveMember = bluetoothViewModel::removeMemberFromGroup,
                                    showMemberManagement = showManageDialog,
                                    onDismissManagement = { showManageDialog = false },
                                    onEnterPip = onEnterPip,
                                    externalFocusedId = focusedSenderId,
                                    onFocusChange = { focusedSenderId = it }
                                ) 
                            }
                            else -> NavEntry(key) { Text("Unknown") }
                        }
                    }

                    Box(modifier = Modifier.align(Alignment.BottomEnd).padding(end = 4.dp, bottom = 4.dp)) {
                        androidx.compose.animation.AnimatedVisibility(
                            visible = showUPH,
                            enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
                            exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
                        ) {
                            UnifiedPersonaCloud(
                                devices = cloudDevices, 
                                vibedPeers = bluetoothState.crowd.vibedPeers, 
                                connectedLinks = bluetoothState.session.connectedLinks, 
                                selectedDevices = bluetoothState.crowd.selectedDevices,
                                activeBubbles = bluetoothState.session.messages.map { BubbleData(it.senderId, it.content, it.timestamp, it.messageId, !it.receiverId.isNullOrBlank()) }, 
                                onDeviceClick = { device -> 
                                    if (bluetoothState.crowd.selectedDevices.isNotEmpty()) {
                                        bluetoothViewModel.toggleDeviceSelection(device.id)
                                    } else {
                                        val id = device.persistentId ?: device.id
                                        viewModel.toggleVibePeer(id)
                                    }
                                }, 
                                onDeviceLongClick = { selectedPersonaForMenu = it },
                                isVertical = true,
                                userNickname = nickname ?: "?",
                                userEmoji = emoji,
                                onUserNicknameChange = viewModel::saveNickname,
                                    userFocusRequester = personaFocusRequester,
                                airIsStill = airIsStill,
                                isNoiseFilterActive = isNoiseFilterActive,
                                vibedPeersCount = bluetoothState.crowd.vibedPeers.size,
                                onToggleNoiseFilter = { isNoiseFilterActive = it },
                                onClearHistory = viewModel::clearChatHistory,
                                showFilter = currentRoute is Route.Blukit,
                                currentRoute = (currentRoute as? Route) ?: initialRoute,
                                onNavigate = { route -> 
                                    if (route == Route.Blukit) isNoiseFilterActive = false
                                    if (currentRoute != route) { 
                                        focusManager.clearFocus() 
                                        backStack.add(route) 
                                    } 
                                }
                            )
                        }
                    }
                }

                BlukitHub(
                    currentRoute = (currentRoute as? Route) ?: initialRoute,
                    messageText = messageText,
                    onMessageChange = { messageText = it },
                    onSend = { 
                        if (messageText.isNotBlank()) { 
                            if (airIsStill) {
                                showAirIsStillDialog = true
                            } else {
                                bluetoothViewModel.spreadVibe(messageText, currentRoute is Route.Vibes)
                                messageText = ""
                                focusManager.clearFocus() 
                            }
                        } 
                    },
                    vibeCount = allVibesCount + secureVibesCount,
                    airIsStill = airIsStill,
                    incomingLinkRequests = bluetoothState.crowd.incomingLinkRequests, 
                    selectedDevices = bluetoothState.crowd.selectedDevices,
                    isNoiseFilterActive = isNoiseFilterActive,
                    onClearHistory = viewModel::clearChatHistory,
                    onAcceptLink = bluetoothViewModel::acceptLink, 
                    onDenyLink = bluetoothViewModel::denyLink,
                    onStartSideVibe = { 
                        val members = bluetoothState.crowd.selectedDevices
                        if (members.all { it in bluetoothState.session.connectedLinks }) { 
                            val gid = bluetoothViewModel.startGroupVibe("WHISPER", members, isTie = false)
                            backStack.add(Route.VibeDetail(gid)) 
                        } 
                    },
                    onStartTie = { 
                        val gid = bluetoothViewModel.startGroupVibe("VIBE", bluetoothState.crowd.selectedDevices, isTie = true)
                        backStack.add(Route.VibeDetail(gid)) 
                    },
                    onClearSelection = bluetoothViewModel::clearSelection
                )
            }

            if (selectedPersonaForMenu != null) {
                val menuDevice = selectedPersonaForMenu!!
                val menuId = menuDevice.persistentId ?: menuDevice.id
                
                PersonaOptionsMenu(
                    device = menuDevice,
                    isVibed = menuId in bluetoothState.crowd.vibedPeers,
                    isTied = menuDevice.id in bluetoothState.session.connectedLinks,
                    isBlocked = menuId in bluetoothState.crowd.blockedUsers,
                    isSelected = menuDevice.id in bluetoothState.crowd.selectedDevices,
                    isRequesting = bluetoothState.crowd.incomingLinkRequests.any { it.id == menuDevice.id },
                    onFocus = { viewModel.toggleVibePeer(menuId) },
                    onVibe = { bluetoothViewModel.connectToDevice(menuDevice) },
                    onAccept = { bluetoothViewModel.acceptLink(menuDevice) },
                    onDeny = { bluetoothViewModel.denyLink(menuDevice) },
                    onDisconnect = { bluetoothViewModel.disconnect() },
                    onSelect = { bluetoothViewModel.toggleDeviceSelection(menuDevice.id) },
                    onIdentify = { highlightedUserId = menuId },
                    onWhisper = { val gid = bluetoothViewModel.startGroupVibe("WHISPER", setOf(menuId), isTie = false); backStack.add(Route.VibeDetail(gid)) },
                    onBlock = { viewModel.blockUser(menuId) },
                    onUnblock = { viewModel.unblockUser(menuId) },
                    onDismiss = { selectedPersonaForMenu = null }
                )
            }

            if (showAirIsStillDialog) {
                ConfirmationDialog(
                    title = "AIR IS STILL",
                    text = "BLUKIT RADIOS ARE SILENT. AWAKEN BLUETOOTH OR GRANT PERMISSIONS TO SPREAD VIBES.",
                    onConfirm = { 
                        showAirIsStillDialog = false
                        permissionState.launchMultiplePermissionRequest()
                    },
                    onDismiss = { showAirIsStillDialog = false }
                )
            }
        }
    }
}

@Composable
fun BlukitHub(
    currentRoute: Route, 
    messageText: String, 
    onMessageChange: (String) -> Unit, 
    onSend: () -> Unit, 
    vibeCount: Int, 
    airIsStill: Boolean,
    incomingLinkRequests: Set<P2PDevice>, 
    selectedDevices: Set<String>, 
    isNoiseFilterActive: Boolean,
    onClearHistory: () -> Unit,
    onAcceptLink: (P2PDevice) -> Unit, 
    onDenyLink: (P2PDevice) -> Unit, 
    onStartSideVibe: () -> Unit, 
    onStartTie: () -> Unit, 
    onClearSelection: () -> Unit, 
    modifier: Modifier = Modifier
) {
    var showClearHistoryDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth().zIndex(1f), horizontalAlignment = Alignment.CenterHorizontally) {
        AnimatedVisibility(visible = selectedDevices.isNotEmpty(), enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
            Row(modifier = Modifier.padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onStartSideVibe, colors = ButtonDefaults.buttonColors(containerColor = StealthPrimary, contentColor = Color.Black), shape = RoundedCornerShape(12.dp)) { Text("WHISPER", fontWeight = FontWeight.Black, fontSize = 10.sp) }
                Button(onClick = onStartTie, colors = ButtonDefaults.buttonColors(containerColor = StealthRose, contentColor = Color.White), shape = RoundedCornerShape(12.dp)) { Text("VIBE REQUEST", fontWeight = FontWeight.Black, fontSize = 10.sp) }
                IconButton(onClick = onClearSelection, modifier = Modifier.background(Color.White.copy(alpha = 0.1f), CircleShape)) { Icon(Icons.Rounded.Close, tint = Color.White, contentDescription = "Cancel") }
            }
        }
        Box(modifier = Modifier.fillMaxWidth().background(Color.Black.copy(alpha = 0.96f), RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)).border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))) {
            Column(modifier = Modifier.padding(vertical = 12.dp, horizontal = 12.dp).imePadding()) {
                AnimatedVisibility(visible = currentRoute is Route.Blukit || currentRoute is Route.Vibes) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BlukitInput(
                            airIsStill = airIsStill, 
                            isReadOnly = false, 
                            isFilterActive = isNoiseFilterActive,
                            value = messageText, 
                            onValueChange = onMessageChange, 
                            onSend = onSend, 
                            vibeCount = vibeCount, 
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // VIBE REQUEST Row
                if (incomingLinkRequests.isNotEmpty()) {
                    val request = incomingLinkRequests.first()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .background(StealthPrimary.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                            .border(1.dp, StealthPrimary.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = request.emoji, fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "VIBE REQUEST FROM ${(request.name ?: "?").uppercase()}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = StealthPrimary, fontSize = 8.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(text = "DENY", modifier = Modifier.clickable { onDenyLink(request) }, color = Color.White.copy(alpha = 0.4f), fontWeight = FontWeight.Bold, fontSize = 9.sp)
                            Text(text = "JOIN", modifier = Modifier.testTag("AcceptLinkButton").clickable { onAcceptLink(request) }, color = StealthPrimary, fontWeight = FontWeight.Black, fontSize = 9.sp)
                        }
                    }
                }

                // Hub logo and actions removed (moved to top)
            }
        }
        
        if (showClearHistoryDialog) { ConfirmationDialog(title = "CLEAR VIBES?", text = "THIS WILL PERMANENTLY REMOVE YOUR SHARED HISTORY.", onConfirm = { onClearHistory(); showClearHistoryDialog = false }, onDismiss = { showClearHistoryDialog = false }) }
    }
}

@Composable
private fun FullLighthouseScan(rotation: Float, lowPowerMode: Boolean) { if (lowPowerMode && rotation % 10 > 2) return; Canvas(modifier = Modifier.fillMaxSize()) { val center = Offset(56.dp.toPx(), size.height - 64.dp.toPx()); rotate(rotation, pivot = center) { val scanBrush = Brush.sweepGradient(0.0f to StealthPrimary.copy(alpha = if (lowPowerMode) 0.05f else 0.15f), 0.1f to Color.Transparent, center = center); drawCircle(brush = scanBrush, radius = size.maxDimension, center = center) } } }
