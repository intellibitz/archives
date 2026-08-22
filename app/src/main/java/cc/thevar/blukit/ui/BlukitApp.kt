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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.domain.model.VibeGroup
import cc.thevar.blukit.domain.model.MessagePayload
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import cc.thevar.blukit.ui.navigation.Route
import cc.thevar.blukit.ui.screens.*
import cc.thevar.blukit.ui.theme.StealthAmber
import cc.thevar.blukit.ui.theme.StealthPrimary
import cc.thevar.blukit.ui.theme.StealthRose
import cc.thevar.blukit.ui.viewmodels.BluetoothViewModel
import cc.thevar.blukit.ui.viewmodels.MainViewModel
import cc.thevar.blukit.ui.viewmodels.SupremePowerViewModel
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import cc.thevar.blukit.R

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalFoundationApi::class)
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

    var showManageDialog by remember { mutableStateOf(false) }
    var isSearchMode by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let { bluetoothViewModel.spreadFile(it) }
        }
    )

    val personaCoordinates = remember { mutableStateMapOf<String, PersonaConnectionPoints>() }
    
    LaunchedEffect(currentRoute) {
        personaCoordinates.clear()
    }
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
            val connectionAlpha by rememberInfiniteTransition(label = "LinePulse").animateFloat(
                initialValue = 0.4f, targetValue = 0.8f,
                animationSpec = infiniteRepeatable(tween(2500, easing = LinearEasing), RepeatMode.Reverse),
                label = "Pulse"
            )

            Canvas(modifier = Modifier.fillMaxSize().zIndex(5f)) {
                personaCoordinates.forEach { (id, points) ->
                    val isVibed = id in bluetoothState.crowd.vibedPeers
                    val device = bluetoothState.crowd.scannedDevices.find { it.persistentId == id || it.id == id }
                    val isTied = device?.isConnected == true
                    val isPrivateMode = currentRoute is Route.Vibes || currentRoute is Route.VibeDetail
                    val isRelevant = if (isPrivateMode) { id == "YOU" || isTied || isVibed || bluetoothState.crowd.incomingLinkRequests.any { it.persistentId == id || it.id == id } } else { true }
                    if (!isRelevant) return@forEach
                    val isFocused = id == "YOU" || isVibed || isTied
                    if (isNoiseFilterActive && !isFocused) return@forEach
                    val color = if (id == "YOU") StealthPrimary else if (isTied) StealthRose else StealthPrimary
                    val alphaMultiplier = if (isNoiseFilterActive) 1.2f else 1f
                    val baseAlpha = if (id == "YOU") 0.35f else 0.25f
                    if (points.uph != null && points.ticker != null) {
                        val y = (points.uph.y + points.ticker.y) / 2f
                        drawLine(color = color.copy(alpha = baseAlpha * alphaMultiplier * connectionAlpha), start = Offset(points.ticker.x + 20f, y), end = Offset(points.uph.x - 20f, y), strokeWidth = 1.dp.toPx(), pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(5f, 15f), 0f))
                    }
                }
            }

            Column(modifier = Modifier.fillMaxSize()) {
                val topTitle = when { currentRoute is Route.Blukit -> "PUBLIC VIBES"; currentRoute is Route.VibeDetail -> { val group = bluetoothState.session.groups.find { it.id == currentRoute.groupId }; group?.name ?: "VIBE" }; else -> "BLUKIT" }
                val topIcon = when { currentRoute is Route.Blukit -> Icons.Rounded.Groups; currentRoute is Route.VibeDetail -> { val group = bluetoothState.session.groups.find { it.id == currentRoute.groupId }; if (group?.type == VibeGroup.TYPE_TIE) Icons.Rounded.Flare else Icons.Rounded.Hearing }; else -> Icons.Rounded.Hub }

                BlukitHarmonyTopBar(
                    title = topTitle, 
                    icon = topIcon,
                    currentRoute = (currentRoute as? Route) ?: initialRoute,
                    onNavigate = { route -> 
                        if (currentRoute == route && route is Route.Blukit) { 
                            viewModel.clearVibedPeers()
                            isNoiseFilterActive = false
                        } else if (currentRoute != route) { 
                            focusManager.clearFocus() 
                            backStack.add(route) 
                        } 
                    },
                    userNickname = nickname ?: "?",
                    userEmoji = emoji,
                    onUserNicknameChange = viewModel::saveNickname,
                    userFocusRequester = personaFocusRequester,
                    isBluetoothOff = !bluetoothState.harmony.isBluetoothEnabled,
                    isLocationOff = !bluetoothState.harmony.isLocationEnabled,
                    isWifiOff = !bluetoothState.harmony.isWifiEnabled,
                    isPermissionMissing = !permissionState.allPermissionsGranted,
                    isPermanentlyDenied = isPermanentlyDenied,
                    userCount = report.userCount,
                    isStealthMode = isStealthMode,
                    lowPowerMode = lowPowerMode,
                    airIsStill = airIsStill,
                    onToggleStealth = viewModel::toggleStealth,
                    onToggleLowPower = viewModel::toggleLowPowerMode,
                    onAwakenBluetooth = { context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS)) },
                    onAwakenLocation = { context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) },
                    onAwakenWifi = { context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS)) },
                    onGrantPermissions = { permissionState.launchMultiplePermissionRequest() },
                    onOpenSettings = { context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply { data = Uri.fromParts("package", context.packageName, null); addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }) },
                    onClearHistory = viewModel::clearChatHistory,
                    onBack = if (currentRoute is Route.VibeDetail || isSearchMode) { { if (isSearchMode) { isSearchMode = false; searchText = "" } else backStack.removeLastOrNull() } } else null,
                    onManage = { showManageDialog = true },
                    onSearch = if (currentRoute is Route.Blukit) { { isSearchMode = !isSearchMode } } else null
                )

                if (isSearchMode) {
                    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
                        BlukitInput(airIsStill = false, value = searchText, onValueChange = { searchText = it }, onSend = { focusManager.clearFocus() }, modifier = Modifier.height(48.dp), decoratorText = "SEARCH VIBES", placeholder = "TYPE NICKNAME OR VIBE...")
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    NavDisplay(backStack = backStack, onBack = { backStack.removeLastOrNull() }, sceneStrategy = listDetailSceneStrategy, modifier = Modifier.fillMaxSize()) { key ->
                        when (key) {
                            Route.Blukit -> NavEntry(key) { RipplesScreen(state = bluetoothState, localDeviceId = viewModel.deviceId.collectAsStateWithLifecycle(initialValue = "").value, localNickname = nickname ?: "?", localEmoji = emoji, energySurge = energySurge, lowPowerMode = lowPowerMode, vibedPeers = bluetoothState.crowd.vibedPeers, noiseFilterEnabled = isNoiseFilterActive, onStartScan = bluetoothViewModel::startScan, onStopScan = bluetoothViewModel::stopScan, onDeviceClick = { device -> if (bluetoothState.crowd.selectedDevices.isNotEmpty()) { bluetoothViewModel.toggleDeviceSelection(device.id) } else { val id = device.persistentId ?: device.id; viewModel.toggleVibePeer(id); isNoiseFilterActive = true } }, onDeviceLongClick = { selectedPersonaForMenu = it }, onBroadcastMessage = bluetoothViewModel::spreadVibe, onDeleteVibe = viewModel::deleteVibe, onBlockUser = viewModel::blockUser, onUnblockUser = viewModel::unblockUser, onWhisper = { device -> val id = device.persistentId ?: device.id; val gid = bluetoothViewModel.startGroupVibe("WHISPER", setOf(id), isTie = false); backStack.add(Route.VibeDetail(gid)) }, onToggleSelection = bluetoothViewModel::toggleDeviceSelection, onAcceptLink = bluetoothViewModel::acceptLink, onDenyLink = bluetoothViewModel::denyLink, onDisconnect = bluetoothViewModel::disconnect, onIdentifyUser = { highlightedUserId = it }, onClearFocus = { viewModel.clearVibedPeers(); isNoiseFilterActive = false }, hasSidebar = false, searchText = searchText) }
                            is Route.VibeDetail -> NavEntry(key) { TieScreen(state = bluetoothState, localDeviceId = viewModel.deviceId.collectAsStateWithLifecycle(initialValue = "").value, localEmoji = emoji, groupId = key.groupId, onDisconnect = bluetoothViewModel::disconnect, onSendMessage = bluetoothViewModel::sendMessage, onStartSideVibe = { peerId -> val gid = bluetoothViewModel.startGroupVibe("SIDE VIBE", setOf(peerId), isTie = false); backStack.add(Route.VibeDetail(gid)) }, onToggleFocus = { device -> val id = device.persistentId ?: device.id; viewModel.toggleVibePeer(id) }, onDeviceLongClick = { selectedPersonaForMenu = it }, onBlockUser = viewModel::blockUser, onAddMember = bluetoothViewModel::addMemberToGroup, onRemoveMember = bluetoothViewModel::removeMemberFromGroup, showMemberManagement = showManageDialog, onDismissManagement = { showManageDialog = false }, onEnterPip = onEnterPip, onAttachFile = { filePickerLauncher.launch("*/*") }) }
                            else -> NavEntry(key) { Text("Unknown") }
                        }
                    }
                }

                BlukitVibeHub(
                    currentRoute = (currentRoute as? Route) ?: initialRoute,
                    messageText = messageText,
                    onMessageChange = { messageText = it },
                    onSend = { if (messageText.isNotBlank()) { if (airIsStill) { showAirIsStillDialog = true } else { bluetoothViewModel.spreadVibe(messageText, MessagePayload.VIBE_LOCAL); messageText = ""; focusManager.clearFocus() } } },
                    vibeCount = allVibesCount + secureVibesCount,
                    airIsStill = airIsStill,
                    incomingLinkRequests = bluetoothState.crowd.incomingLinkRequests, 
                    selectedDevices = bluetoothState.crowd.selectedDevices,
                    vibedPeers = bluetoothState.crowd.vibedPeers,
                    groups = bluetoothState.session.groups,
                    onAcceptLink = bluetoothViewModel::acceptLink, 
                    onDenyLink = bluetoothViewModel::denyLink,
                    onStartSideVibe = { val members = bluetoothState.crowd.selectedDevices; if (members.all { it in bluetoothState.session.connectedLinks }) { val gid = bluetoothViewModel.startGroupVibe("WHISPER", members, isTie = false); backStack.add(Route.VibeDetail(gid)) } },
                    onStartTie = { val gid = bluetoothViewModel.startGroupVibe("VIBE", bluetoothState.crowd.selectedDevices, isTie = true); backStack.add(Route.VibeDetail(gid)) },
                    onClearSelection = bluetoothViewModel::clearSelection,
                    onAttachFile = { filePickerLauncher.launch("*/*") }
                )
            }

            if (selectedPersonaForMenu != null) {
                val menuDevice = selectedPersonaForMenu!!
                val menuId = menuDevice.persistentId ?: menuDevice.id
                PersonaOptionsMenu(device = menuDevice, isTied = menuDevice.id in bluetoothState.session.connectedLinks, isBlocked = menuId in bluetoothState.crowd.blockedUsers, isSelected = menuDevice.id in bluetoothState.crowd.selectedDevices, isRequesting = bluetoothState.crowd.incomingLinkRequests.any { it.id == menuDevice.id }, onVibe = { bluetoothViewModel.requestWhisper(menuDevice) }, onAccept = { bluetoothViewModel.acceptLink(menuDevice) }, onDeny = { bluetoothViewModel.denyLink(menuDevice) }, onDisconnect = { bluetoothViewModel.disconnect() }, onSelect = { bluetoothViewModel.toggleDeviceSelection(menuDevice.id) }, onIdentify = { highlightedUserId = menuId }, onBlock = { viewModel.blockUser(menuId) }, onUnblock = { viewModel.unblockUser(menuId) }, onDismiss = { selectedPersonaForMenu = null })
            }

            if (showAirIsStillDialog) {
                ConfirmationDialog(title = "AIR IS STILL", text = "BLUKIT RADIOS ARE SILENT. AWAKEN BLUETOOTH OR GRANT PERMISSIONS TO SPREAD VIBES.", onConfirm = { showAirIsStillDialog = false; permissionState.launchMultiplePermissionRequest() }, onDismiss = { showAirIsStillDialog = false })
            }
        }
    }
}

@Composable
private fun FullLighthouseScan(rotation: Float, lowPowerMode: Boolean) { 
    if (lowPowerMode && rotation % 10 > 2) return
    Canvas(modifier = Modifier.fillMaxSize()) { 
        val center = Offset(56.dp.toPx(), size.height - 64.dp.toPx())
        rotate(rotation, pivot = center) { 
            val scanBrush = Brush.sweepGradient(0.0f to StealthPrimary.copy(alpha = if (lowPowerMode) 0.05f else 0.15f), 0.1f to Color.Transparent, center = center)
            drawCircle(brush = scanBrush, radius = size.maxDimension, center = center) 
        } 
    } 
}

interface SpreadPermissionsState {
    val allPermissionsGranted: Boolean
    val essentialPermissionsGranted: Boolean
    val shouldShowRationale: Boolean
    fun launchMultiplePermissionRequest()
}

@Composable
fun rememberSpreadPermissionsState(permissions: List<String>): SpreadPermissionsState {
    val context = LocalContext.current
    var allGranted by remember { mutableStateOf(permissions.all { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }) }
    var essentialGranted by remember { mutableStateOf(permissions.all { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results -> allGranted = results.values.all { it }; essentialGranted = results.filter { it.key != Manifest.permission.BLUETOOTH_ADVERTISE && it.key != Manifest.permission.BLUETOOTH_SCAN && it.key != Manifest.permission.BLUETOOTH_CONNECT }.values.all { it } }
    return object : SpreadPermissionsState {
        override val allPermissionsGranted: Boolean get() = allGranted
        override val essentialPermissionsGranted: Boolean get() = essentialGranted
        override val shouldShowRationale: Boolean get() = (context as? Activity)?.let { act -> permissions.any { p -> androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale(act, p) } } ?: false
        override fun launchMultiplePermissionRequest() { launcher.launch(permissions.toTypedArray()) }
    }
}
