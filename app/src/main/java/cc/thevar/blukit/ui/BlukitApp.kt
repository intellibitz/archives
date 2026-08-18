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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
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
    val manager = remember { (context.applicationContext as BlukitApplication).spreadPermissionManager }
    var allGranted by remember { mutableStateOf(manager.checkAllGranted()) }
    var shouldShowRationale by remember { mutableStateOf(permissions.any { (context as? Activity)?.shouldShowRequestPermissionRationale(it) == true }) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        allGranted = manager.checkAllGranted()
        shouldShowRationale = permissions.any { (context as? Activity)?.shouldShowRequestPermissionRationale(it) == true }
        (context.applicationContext as? BlukitApplication)?.spreadPermissionManager?.refresh()
    }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            allGranted = manager.checkAllGranted()
            shouldShowRationale = permissions.any { (context as? Activity)?.shouldShowRequestPermissionRationale(it) == true }
            (context.applicationContext as? BlukitApplication)?.spreadPermissionManager?.refresh()
        }
    }

    return remember(allGranted, shouldShowRationale) {
        object : SpreadPermissionsState {
            override val allPermissionsGranted: Boolean = allGranted
            override val shouldShowRationale: Boolean = shouldShowRationale
            override fun launchMultiplePermissionRequest() { launcher.launch(permissions.toTypedArray()) }
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
    val viewModel: MainViewModel = viewModel(factory = object : ViewModelProvider.Factory { override fun <T : ViewModel> create(modelClass: Class<T>): T = MainViewModel(repository, vibeStore) as T })
    val bluetoothViewModel: BluetoothViewModel = viewModel(factory = object : ViewModelProvider.Factory { override fun <T : ViewModel> create(modelClass: Class<T>): T = BluetoothViewModel(p2pController, radioStateManager, repository, permissionManager, vibeStore) as T })
    val supremePowerViewModel: SupremePowerViewModel = viewModel(factory = object : ViewModelProvider.Factory { override fun <T : ViewModel> create(modelClass: Class<T>): T = SupremePowerViewModel(supremePowerManager) as T })
    
    val nickname by viewModel.nickname.collectAsStateWithLifecycle(initialValue = null)
    val emoji by viewModel.emojiAvatar.collectAsStateWithLifecycle(initialValue = "👤")
    val isStealthMode by viewModel.isStealthMode.collectAsStateWithLifecycle(initialValue = false)
    val lowPowerMode by viewModel.lowPowerMode.collectAsStateWithLifecycle(initialValue = false)
    val bluetoothState by bluetoothViewModel.state.collectAsStateWithLifecycle()
    val report by supremePowerViewModel.report.collectAsStateWithLifecycle()
    val energySurge by bluetoothViewModel.energySurge.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(bluetoothState.uiError) { bluetoothState.uiError?.let { snackbarHostState.showSnackbar(it.message.uppercase()) } }

    val permissionState = rememberSpreadPermissionsState(permissions = permissionManager.requiredPermissions)
    val isPermanentlyDenied = !permissionState.allPermissionsGranted && !permissionState.shouldShowRationale

    val initialRoute = Route.Blukit
    val backStack = rememberNavBackStack(initialRoute)
    val currentRoute = backStack.lastOrNull()

    val hubRotation by rememberInfiniteTransition(label = "HubScan").animateFloat(initialValue = 0f, targetValue = 360f, animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing)), label = "Scan")

    var selectedStudentForMenu by remember { mutableStateOf<P2PDevice?>(null) }

    LaunchedEffect(currentRoute, bluetoothState.vibedPeers) {
        if (currentRoute is Route.Focus && bluetoothState.vibedPeers.isEmpty()) {
            if (backStack.size > 1) backStack.removeLastOrNull() else backStack.add(Route.Blukit)
        }
    }

    val listDetailSceneStrategy = rememberListDetailSceneStrategy<NavKey>()
    val personaFocusRequester = remember { FocusRequester() }
    val isLocationMandatory = Build.VERSION.SDK_INT < Build.VERSION_CODES.S
    var messageText by remember { mutableStateOf("") }
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current

    val roarsCount = remember(bluetoothState.messages) { bluetoothState.messages.count { it.receiverId.isNullOrBlank() } }
    val vibesCount = remember(bluetoothState.messages) { bluetoothState.messages.count { !it.receiverId.isNullOrBlank() } }

    Box(modifier = modifier.fillMaxSize()) {
        FullLighthouseScan(rotation = hubRotation, lowPowerMode = lowPowerMode)

        Column(modifier = Modifier.fillMaxSize()) {
            NavDisplay(backStack = backStack, onBack = { backStack.removeLastOrNull() }, sceneStrategy = listDetailSceneStrategy, modifier = Modifier.weight(1f).fillMaxWidth()) { key ->
                when (key) {
                    Route.Blukit -> NavEntry(key) {
                        RipplesScreen(
                            state = bluetoothState,
                            localDeviceId = viewModel.deviceId.collectAsStateWithLifecycle(initialValue = "").value,
                            localNickname = nickname ?: "?",
                            localEmoji = emoji,
                            energySurge = energySurge,
                            lowPowerMode = lowPowerMode,
                            vibedPeers = bluetoothState.vibedPeers,
                            onStartScan = bluetoothViewModel::startScan,
                            onStopScan = bluetoothViewModel::stopScan,
                            onDeviceClick = { device ->
                                if (bluetoothState.selectedDevices.isEmpty()) {
                                    device.persistentId?.let { pid -> viewModel.toggleVibePeer(pid) } ?: viewModel.toggleVibePeer(device.id)
                                } else {
                                    bluetoothViewModel.toggleDeviceSelection(device.id)
                                }
                            },
                            onDeviceLongClick = { selectedStudentForMenu = it },
                            onBroadcastMessage = bluetoothViewModel::roar
                        )
                    }
                    Route.Focus -> NavEntry(key) {
                        RipplesScreen(
                            state = bluetoothState,
                            localDeviceId = viewModel.deviceId.collectAsStateWithLifecycle(initialValue = "").value,
                            localNickname = nickname ?: "?",
                            localEmoji = emoji,
                            energySurge = energySurge,
                            vibedPeers = bluetoothState.vibedPeers,
                            isFilterMode = true,
                            lowPowerMode = lowPowerMode,
                            onStartScan = bluetoothViewModel::startScan,
                            onStopScan = bluetoothViewModel::stopScan,
                            onDeviceClick = { device ->
                                if (bluetoothState.selectedDevices.isEmpty()) {
                                    device.persistentId?.let { pid -> viewModel.toggleVibePeer(pid) } ?: viewModel.toggleVibePeer(device.id)
                                } else {
                                    bluetoothViewModel.toggleDeviceSelection(device.id)
                                }
                            },
                            onDeviceLongClick = { selectedStudentForMenu = it },
                            onBroadcastMessage = bluetoothViewModel::roar
                        )
                    }
                    Route.Vibes -> NavEntry(key) { ConversationsScreen(state = bluetoothState, isGroupType = true, onVibeClick = { backStack.add(Route.VibeDetail(it.id)) }) }
                    Route.SideVibes -> NavEntry(key) { ConversationsScreen(state = bluetoothState, isGroupType = false, onVibeClick = { backStack.add(Route.VibeDetail(it.id)) }) }
                    Route.Chat -> NavEntry(key) { 
                        TieScreen(
                            state = bluetoothState, 
                            localDeviceId = viewModel.deviceId.collectAsStateWithLifecycle(initialValue = "").value, 
                            localEmoji = emoji, 
                            localNickname = nickname ?: "?", 
                            onNicknameChange = viewModel::saveNickname, 
                            groupId = bluetoothState.groups.find { it.memberIds.contains(bluetoothState.connectedVibe?.id) }?.id, 
                            onDisconnect = bluetoothViewModel::disconnect, 
                            onNavigateBack = { backStack.removeLastOrNull() }, 
                            onSendMessage = bluetoothViewModel::sendMessage, 
                            onStartSideVibe = { peerId -> 
                                val gid = bluetoothViewModel.startGroupVibe("SIDE VIBE", setOf(peerId), isTie = false)
                                backStack.add(Route.VibeDetail(gid)) 
                            }, 
                            onToggleFocus = { device -> 
                                device.persistentId?.let { pid -> viewModel.toggleVibePeer(pid) } ?: viewModel.toggleVibePeer(device.id) 
                            }, 
                            onBlockUser = viewModel::blockUser, 
                            onEnterPip = onEnterPip
                        ) 
                    }
                    is Route.VibeDetail -> NavEntry(key) { 
                        TieScreen(
                            state = bluetoothState, 
                            localDeviceId = viewModel.deviceId.collectAsStateWithLifecycle(initialValue = "").value, 
                            localEmoji = emoji, 
                            localNickname = nickname ?: "?", 
                            onNicknameChange = viewModel::saveNickname, 
                            groupId = key.groupId, 
                            onDisconnect = bluetoothViewModel::disconnect, 
                            onNavigateBack = { backStack.removeLastOrNull() }, 
                            onSendMessage = bluetoothViewModel::sendMessage, 
                            onStartSideVibe = { peerId -> 
                                val gid = bluetoothViewModel.startGroupVibe("SIDE VIBE", setOf(peerId), isTie = false)
                                backStack.add(Route.VibeDetail(gid)) 
                            }, 
                            onToggleFocus = { device -> 
                                device.persistentId?.let { pid -> viewModel.toggleVibePeer(pid) } ?: viewModel.toggleVibePeer(device.id) 
                            }, 
                            onBlockUser = viewModel::blockUser, 
                            onEnterPip = onEnterPip
                        ) 
                    }
                    else -> NavEntry(key) { Text("Unknown") }
                }
            }

            BlukitHub(
                currentRoute = (currentRoute as? Route) ?: initialRoute,
                nickname = nickname ?: "?", emoji = emoji,
                isBluetoothEnabled = bluetoothState.isBluetoothEnabled, isLocationEnabled = bluetoothState.isLocationEnabled, isWifiEnabled = bluetoothState.isWifiEnabled,
                isLocationMandatory = isLocationMandatory, permissionsGranted = permissionState.allPermissionsGranted, isPermanentlyDenied = isPermanentlyDenied,
                onSaveNickname = viewModel::saveNickname, personaFocusRequester = personaFocusRequester, messageText = messageText,
                onMessageChange = { messageText = it },
                onSend = { if (messageText.isNotBlank()) { bluetoothViewModel.roar(messageText, currentRoute is Route.Vibes || currentRoute is Route.SideVibes); messageText = ""; focusManager.clearFocus() } },
                vibeCount = if (currentRoute is Route.Vibes) vibesCount else roarsCount,
                energySurge = energySurge, hubRotation = hubRotation, userCount = report.userCount, linksCount = report.connectedLinksCount,
                roarsCount = roarsCount, vibesCount = vibesCount, lowPowerMode = lowPowerMode, isStealthMode = isStealthMode,
                incomingLinkRequests = bluetoothState.incomingLinkRequests, selectedDevices = bluetoothState.selectedDevices, scannedDevices = bluetoothState.scannedDevices,
                connectedLinks = bluetoothState.connectedLinks, vibedPeers = bluetoothState.vibedPeers, messages = bluetoothState.messages,
                onNavigate = { route -> if (currentRoute != route) backStack.add(route) },
                onDeviceClick = { device ->
                    if (bluetoothState.selectedDevices.isEmpty()) {
                        if (currentRoute is Route.Vibes || currentRoute is Route.SideVibes) {
                            val group = bluetoothState.groups.find { it.memberIds.contains(device.id) || it.memberIds.contains(device.persistentId) }
                            if (group != null) backStack.add(Route.VibeDetail(group.id))
                            else { device.persistentId?.let { pid -> viewModel.toggleVibePeer(pid) } ?: viewModel.toggleVibePeer(device.id) }
                        } else { device.persistentId?.let { pid -> viewModel.toggleVibePeer(pid) } ?: viewModel.toggleVibePeer(device.id) }
                    } else { bluetoothViewModel.toggleDeviceSelection(device.id) }
                },
                onDeviceLongClick = { selectedStudentForMenu = it },
                onAwakenBluetooth = { context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS)) },
                onAwakenLocation = { context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) },
                onAwakenWifi = { context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS)) },
                onGrantPermissions = { permissionState.launchMultiplePermissionRequest() },
                onOpenSettings = { context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply { data = Uri.fromParts("package", context.packageName, null) }) },
                onToggleStealth = viewModel::toggleStealth, onToggleLowPower = viewModel::toggleLowPowerMode, onClearHistory = viewModel::clearChatHistory,
                onLogout = viewModel::logout, onAcceptLink = bluetoothViewModel::acceptLink,
                onStartSideVibe = { val members = bluetoothState.selectedDevices; if (members.all { it in bluetoothState.connectedLinks }) { val gid = bluetoothViewModel.startGroupVibe("SIDE VIBE", members, isTie = false); backStack.add(Route.VibeDetail(gid)) } },
                onStartTie = { val gid = bluetoothViewModel.startGroupVibe("VIBE", bluetoothState.selectedDevices, isTie = true); backStack.add(Route.VibeDetail(gid)) },
                onClearSelection = bluetoothViewModel::clearSelection
            )
        }

        val isRadiosOff = !bluetoothState.isBluetoothEnabled || !bluetoothState.isLocationEnabled

        if (isRadiosOff && permissionState.shouldShowRationale && !permissionState.allPermissionsGranted) {
            AlertDialog(
                onDismissRequest = { },
                containerColor = Color.Black,
                titleContentColor = StealthPrimary,
                textContentColor = Color.White.copy(alpha = 0.7f),
                title = { Text("MAKE PEOPLE VIBE", fontWeight = FontWeight.Black) },
                text = { Text("BLUKIT USES LOCAL RADIOS TO SYNC THE VIBES.", fontSize = 12.sp) },
                confirmButton = {
                    TextButton(onClick = { permissionState.launchMultiplePermissionRequest() }) {
                        Text("GRANT", color = StealthPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        if (selectedStudentForMenu != null) {
            StudentOptionsMenu(
                device = selectedStudentForMenu!!,
                isVibed = (selectedStudentForMenu!!.persistentId ?: selectedStudentForMenu!!.id) in bluetoothState.vibedPeers,
                isTied = selectedStudentForMenu!!.id in bluetoothState.connectedLinks,
                onFocus = {
                    val device = selectedStudentForMenu!!
                    device.persistentId?.let { pid -> viewModel.toggleVibePeer(pid) } ?: viewModel.toggleVibePeer(device.id)
                    selectedStudentForMenu = null
                },
                onVibe = {
                    val device = selectedStudentForMenu!!
                    if (device.id !in bluetoothState.connectedLinks) bluetoothViewModel.connectToDevice(device)
                    selectedStudentForMenu = null
                },
                onDismiss = { selectedStudentForMenu = null }
            )
        }
    }
}

@Composable
fun BlukitHub(
    currentRoute: Route,
    nickname: String,
    emoji: String,
    isBluetoothEnabled: Boolean,
    isLocationEnabled: Boolean,
    isWifiEnabled: Boolean,
    isLocationMandatory: Boolean,
    permissionsGranted: Boolean,
    isPermanentlyDenied: Boolean,
    onSaveNickname: (String) -> Unit,
    personaFocusRequester: FocusRequester,
    messageText: String,
    onMessageChange: (String) -> Unit,
    onSend: () -> Unit,
    vibeCount: Int,
    energySurge: Float,
    hubRotation: Float,
    userCount: Int,
    linksCount: Int,
    roarsCount: Int,
    vibesCount: Int,
    lowPowerMode: Boolean,
    isStealthMode: Boolean,
    incomingLinkRequests: Set<P2PDevice>,
    selectedDevices: Set<String>,
    scannedDevices: List<P2PDevice>,
    connectedLinks: Set<String>,
    vibedPeers: Set<String>,
    messages: List<cc.thevar.blukit.domain.model.MessagePayload>,
    onNavigate: (Route) -> Unit,
    onDeviceClick: (P2PDevice) -> Unit,
    onDeviceLongClick: (P2PDevice) -> Unit,
    onAwakenBluetooth: () -> Unit,
    onAwakenLocation: () -> Unit,
    onAwakenWifi: () -> Unit,
    onGrantPermissions: () -> Unit,
    onOpenSettings: () -> Unit,
    onToggleStealth: (Boolean) -> Unit,
    onToggleLowPower: (Boolean) -> Unit,
    onClearHistory: () -> Unit,
    onLogout: () -> Unit,
    onAcceptLink: (P2PDevice) -> Unit,
    onStartSideVibe: () -> Unit,
    onStartTie: () -> Unit,
    onClearSelection: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth().zIndex(1f), horizontalAlignment = Alignment.CenterHorizontally) {
        AnimatedVisibility(visible = selectedDevices.isNotEmpty(), enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
            Row(modifier = Modifier.padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onStartSideVibe, colors = ButtonDefaults.buttonColors(containerColor = StealthPrimary, contentColor = Color.Black), shape = RoundedCornerShape(12.dp)) { Text("START SIDE VIBE", fontWeight = FontWeight.Black, fontSize = 10.sp) }
                Button(onClick = onStartTie, colors = ButtonDefaults.buttonColors(containerColor = StealthRose, contentColor = Color.White), shape = RoundedCornerShape(12.dp)) { Text("START VIBE", fontWeight = FontWeight.Black, fontSize = 10.sp) }
                IconButton(onClick = onClearSelection, modifier = Modifier.background(Color.White.copy(alpha = 0.1f), CircleShape)) { Icon(Icons.Rounded.Close, tint = Color.White, contentDescription = "Cancel") }
            }
        }

        Surface(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp), color = Color.Black.copy(alpha = 0.95f), shape = RoundedCornerShape(32.dp), border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)), tonalElevation = 12.dp) {
            Column(modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp)) {
                val cloudDevices = if (currentRoute is Route.Focus) scannedDevices.filter { it.persistentId in vibedPeers || it.id in vibedPeers } else scannedDevices
                AnimatedVisibility(visible = currentRoute is Route.Blukit || currentRoute is Route.Focus || currentRoute is Route.Vibes || currentRoute is Route.SideVibes) {
                    UnifiedPersonaCloud(devices = cloudDevices, vibedPeers = vibedPeers, connectedLinks = connectedLinks, activeBubbles = messages.map { BubbleData(it.senderId, it.content, it.timestamp, it.messageId, !it.receiverId.isNullOrBlank()) }, onDeviceClick = onDeviceClick, onDeviceLongClick = onDeviceLongClick)
                }
                Spacer(modifier = Modifier.height(4.dp))
                AnimatedVisibility(visible = currentRoute is Route.Blukit || currentRoute is Route.Vibes) {
                    BlukitInput(nickname = nickname, emoji = emoji, airIsStill = !isBluetoothEnabled || (isLocationMandatory && !isLocationEnabled) || !permissionsGranted, onNicknameChange = onSaveNickname, personaFocusRequester = personaFocusRequester, value = messageText, onValueChange = onMessageChange, onSend = onSend, vibeCount = vibeCount, placeholder = "SPREAD VIBES…", modifier = Modifier.fillMaxWidth())
                }
                Spacer(modifier = Modifier.height(8.dp))
                UnifiedBlukitBadge(energy = energySurge, rotation = hubRotation, userCount = userCount, linksCount = linksCount, roarsCount = roarsCount, vibesCount = vibesCount, lowPowerMode = lowPowerMode, permissionsGranted = permissionsGranted, isPermanentlyDenied = isPermanentlyDenied, isStealthMode = isStealthMode, incomingLinkRequests = incomingLinkRequests, isBluetoothEnabled = isBluetoothEnabled, isLocationEnabled = isLocationEnabled, isWifiEnabled = isWifiEnabled, currentRoute = currentRoute, onNavigate = onNavigate, onAwakenBluetooth = onAwakenBluetooth, onAwakenLocation = onAwakenLocation, onAwakenWifi = onAwakenWifi, onGrantPermissions = onGrantPermissions, onOpenSettings = onOpenSettings, onToggleStealth = onToggleStealth, onToggleLowPower = onToggleLowPower, onClearHistory = onClearHistory, onLogout = onLogout, onAcceptLink = onAcceptLink)
            }
        }
    }
}

@Composable
fun UnifiedBlukitBadge(
    energy: Float, rotation: Float, userCount: Int, linksCount: Int, roarsCount: Int, vibesCount: Int, lowPowerMode: Boolean, permissionsGranted: Boolean, isPermanentlyDenied: Boolean, isStealthMode: Boolean,
    incomingLinkRequests: Set<P2PDevice>, isBluetoothEnabled: Boolean, isLocationEnabled: Boolean, isWifiEnabled: Boolean, currentRoute: Route, onNavigate: (Route) -> Unit,
    onAwakenBluetooth: () -> Unit, onAwakenLocation: () -> Unit, onAwakenWifi: () -> Unit, onGrantPermissions: () -> Unit, onOpenSettings: () -> Unit, onToggleStealth: (Boolean) -> Unit, onToggleLowPower: (Boolean) -> Unit,
    onClearHistory: () -> Unit, onLogout: () -> Unit, onAcceptLink: (P2PDevice) -> Unit, modifier: Modifier = Modifier
) {
    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    val isLocationMandatory = Build.VERSION.SDK_INT < Build.VERSION_CODES.S
    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                VisualEnergyPicker(currentRoute = currentRoute, userCount = userCount, linksCount = linksCount, vibeCount = vibesCount, energy = energy, rotation = rotation, lowPowerMode = lowPowerMode, onNavigate = onNavigate)
            }
            if (incomingLinkRequests.isNotEmpty()) {
                Spacer(modifier = Modifier.width(12.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "VIBE REQUEST", style = MaterialTheme.typography.labelSmall.copy(fontSize = 5.sp, fontWeight = FontWeight.Black, color = StealthPrimary, letterSpacing = 0.5.sp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "JOIN", modifier = Modifier.testTag("AcceptLinkButton").clickable { onAcceptLink(incomingLinkRequests.first()) }, color = StealthPrimary, fontWeight = FontWeight.Black, fontSize = 7.sp)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        EnergyBarContent(isBluetoothOff = !isBluetoothEnabled, isLocationOff = isLocationMandatory && !isLocationEnabled, isWifiOff = !isWifiEnabled, isPermissionMissing = !permissionsGranted, isStealthMode = isStealthMode, lowPowerMode = lowPowerMode, onToggleStealth = onToggleStealth, onToggleLowPower = onToggleLowPower, onAwakenBluetooth = onAwakenBluetooth, onAwakenLocation = onAwakenLocation, onAwakenWifi = onAwakenWifi, userCount = userCount, vibeCount = roarsCount + vibesCount, isPermanentlyDenied = isPermanentlyDenied, onGrantPermissions = onGrantPermissions, onOpenSettings = onOpenSettings, onClearHistory = { showClearHistoryDialog = true }, onResetProfile = { showLogoutDialog = true })
    }
    if (showClearHistoryDialog) { ConfirmationDialog(title = "CLEAR VIBES?", text = "THIS WILL PERMANENTLY REMOVE YOUR SHARED HISTORY.", onConfirm = { onClearHistory(); showClearHistoryDialog = false }, onDismiss = { showClearHistoryDialog = false }) }
    if (showLogoutDialog) { ConfirmationDialog(title = "RESET PROFILE?", text = "THIS WILL DELETE YOUR LOCAL BLUKIT IDENTITY.", onConfirm = { onLogout(); showLogoutDialog = false }, onDismiss = { showLogoutDialog = false }) }
}

@Composable
private fun VisualEnergyPicker(currentRoute: Route, userCount: Int, linksCount: Int, vibeCount: Int, energy: Float, rotation: Float, lowPowerMode: Boolean, onNavigate: (Route) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color.White.copy(alpha = 0.05f)).padding(2.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        val isAll = currentRoute is Route.Blukit
        Surface(onClick = { onNavigate(Route.Blukit) }, shape = RoundedCornerShape(12.dp), color = if (isAll) Color.White.copy(alpha = 0.12f) else Color.Transparent, modifier = Modifier.height(56.dp).weight(1.5f)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Icon(imageVector = Icons.Rounded.Groups, contentDescription = null, tint = if (isAll) StealthPrimary else Color.White.copy(alpha = 0.3f), modifier = Modifier.size(20.dp))
                Text("ALL", fontSize = 10.sp, fontWeight = FontWeight.Black, color = if (isAll) StealthPrimary else Color.White.copy(alpha = 0.3f), letterSpacing = 1.sp)
            }
        }
        val isFocus = currentRoute is Route.Focus
        Surface(onClick = { onNavigate(Route.Focus) }, shape = RoundedCornerShape(12.dp), color = if (isFocus) Color.White.copy(alpha = 0.08f) else Color.Transparent, modifier = Modifier.height(56.dp).weight(1f)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Icon(imageVector = Icons.Rounded.FilterCenterFocus, contentDescription = null, tint = if (isFocus) StealthPrimary else Color.White.copy(alpha = 0.2f), modifier = Modifier.size(16.dp))
                Text("FOCUS", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = if (isFocus) StealthPrimary else Color.White.copy(alpha = 0.2f))
            }
        }
        Box(modifier = Modifier.width(1.dp).height(32.dp).background(Color.White.copy(alpha = 0.1f)))
        val isVibes = currentRoute is Route.Vibes
        Surface(onClick = { onNavigate(Route.Vibes) }, shape = RoundedCornerShape(12.dp), color = if (isVibes) Color.White.copy(alpha = 0.12f) else Color.Transparent, modifier = Modifier.height(56.dp).weight(1.5f)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Icon(imageVector = Icons.Rounded.Flare, contentDescription = null, tint = if (isVibes) StealthRose else Color.White.copy(alpha = 0.3f), modifier = Modifier.size(20.dp))
                Text("VIBES", fontSize = 10.sp, fontWeight = FontWeight.Black, color = if (isVibes) StealthRose else Color.White.copy(alpha = 0.3f), letterSpacing = 1.sp)
            }
        }
        val isSide = currentRoute is Route.SideVibes
        Surface(onClick = { onNavigate(Route.SideVibes) }, shape = RoundedCornerShape(12.dp), color = if (isSide) Color.White.copy(alpha = 0.08f) else Color.Transparent, modifier = Modifier.height(56.dp).weight(1f)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Icon(imageVector = Icons.Rounded.AutoAwesome, contentDescription = null, tint = if (isSide) StealthRose else Color.White.copy(alpha = 0.2f), modifier = Modifier.size(16.dp))
                Text("1-1", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = if (isSide) StealthRose else Color.White.copy(alpha = 0.2f))
            }
        }
    }
}

@Composable
private fun EnergyBarContent(isBluetoothOff: Boolean, isLocationOff: Boolean, isWifiOff: Boolean, isPermissionMissing: Boolean, isStealthMode: Boolean, lowPowerMode: Boolean, onToggleStealth: (Boolean) -> Unit, onToggleLowPower: (Boolean) -> Unit, onAwakenBluetooth: () -> Unit, onAwakenLocation: () -> Unit, onAwakenWifi: () -> Unit, userCount: Int, vibeCount: Int, isPermanentlyDenied: Boolean, onGrantPermissions: () -> Unit, onOpenSettings: () -> Unit, onClearHistory: () -> Unit, onResetProfile: () -> Unit) {
    val pulseAlpha by rememberInfiniteTransition(label = "AlertPulse").animateFloat(initialValue = 0.6f, targetValue = 1f, animationSpec = infiniteRepeatable(tween(1000, easing = LinearEasing), RepeatMode.Reverse), label = "Alpha")
    var pendingHint by remember { mutableStateOf<Pair<String, () -> Unit>?>(null) }
    val isWeak = userCount == 0 && !isBluetoothOff && !isLocationOff
    val isStill = isBluetoothOff || isLocationOff
    val barBorderColor = when { isStill -> Color.Red.copy(alpha = 0.4f); isPermissionMissing -> Color(0xFFF4511E).copy(alpha = 0.4f); else -> Color.White.copy(alpha = 0.05f) }
    if (pendingHint != null) { val (text, action) = pendingHint!!; AlertDialog(onDismissRequest = { pendingHint = null }, containerColor = Color.Black, titleContentColor = StealthPrimary, textContentColor = Color.White.copy(alpha = 0.8f), title = { Text("ACTION REQUIRED", fontWeight = FontWeight.Black) }, text = { Text(text.uppercase(), fontSize = 12.sp, fontWeight = FontWeight.Bold) }, confirmButton = { TextButton(onClick = { action.invoke(); pendingHint = null }) { Text("PROCEED", color = StealthPrimary, fontWeight = FontWeight.Black) } }, dismissButton = { TextButton(onClick = { pendingHint = null }) { Text("CANCEL", color = Color.White.copy(alpha = 0.4f)) } }) }
    Surface(color = if (isStill) Color.Red.copy(alpha = 0.1f) else if (isPermissionMissing) Color(0xFFF4511E).copy(alpha = 0.1f) else Color.White.copy(alpha = 0.03f), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, barBorderColor)) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) { StatusIcon(icon = Icons.Rounded.Bluetooth, isOn = !isBluetoothOff, isWeak = isWeak, isPermissionMissing = isPermissionMissing, onClick = onAwakenBluetooth); StatusIcon(icon = Icons.Rounded.Wifi, isOn = !isWifiOff, isWeak = isWeak, isPermissionMissing = isPermissionMissing, onClick = onAwakenWifi); StatusIcon(icon = Icons.Rounded.LocationOn, isOn = !isLocationOff, isWeak = isWeak, isPermissionMissing = isPermissionMissing, onClick = onAwakenLocation) }
                if (isStill || isPermissionMissing) { val actionData = when { isStill -> { val hint = if (isBluetoothOff) "PLEASE TURN ON YOUR BLUETOOTH IN SYSTEM SETTINGS." else "PLEASE TURN ON YOUR LOCATION IN SYSTEM SETTINGS."; val act = if (isBluetoothOff) onAwakenBluetooth else onAwakenLocation; Triple("AWAKEN", hint, act) }; isPermissionMissing -> { val hint = if (isPermanentlyDenied) "ANDROID HAS BLOCKED THE DIALOG. PLEASE GO TO PERMISSIONS AND MANUALLY ALLOW 'NEARBY DEVICES' AND 'LOCATION'." else "PLEASE TAP 'ALLOW' ON THE SYSTEM DIALOG TO LET BLUKIT SCAN FOR NEARBY PEOPLE."; val label = if (isPermanentlyDenied) "SETTINGS" else "GRANT"; val act = if (isPermanentlyDenied) onOpenSettings else onGrantPermissions; Triple(label, hint, act) }; else -> null }; Text(text = "MAKE PEOPLE VIBE", modifier = Modifier.testTag("EnergyRequiredLabel"), style = MaterialTheme.typography.labelSmall.copy(fontSize = 6.sp, fontWeight = FontWeight.Black, color = Color.White.copy(alpha = 0.8f))); if (actionData != null) { Surface(onClick = { pendingHint = actionData.second to actionData.third }, color = Color.White, shape = RoundedCornerShape(4.dp), modifier = Modifier.graphicsLayer { alpha = pulseAlpha }) { Text(text = actionData.first, style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.sp, fontWeight = FontWeight.Black, color = Color.Red), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)) } } }
            }
            Spacer(modifier = Modifier.weight(1f))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                EnvironmentToggle(label = "DARK MODE", checked = isStealthMode, onCheckedChange = onToggleStealth)
                EnvironmentToggle(label = "LOW BATTERY MODE", checked = lowPowerMode, onCheckedChange = onToggleLowPower)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = 0.05f)).clickable { onClearHistory() }.padding(horizontal = 8.dp, vertical = 4.dp)) { Text(text = vibeCount.toString(), style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color.White.copy(alpha = 0.3f))); Icon(imageVector = Icons.Rounded.DeleteSweep, contentDescription = "Clear Vibes", tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(14.dp)) }
                    Box(modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = 0.05f)).clickable { onResetProfile() }.padding(horizontal = 8.dp, vertical = 4.dp), contentAlignment = Alignment.Center) { Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) { Text(text = "RESET", style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.sp, fontWeight = FontWeight.Black, color = Color.White.copy(alpha = 0.25f))); Icon(imageVector = Icons.Rounded.RestartAlt, contentDescription = "Reset Profile", tint = Color.White.copy(alpha = 0.25f), modifier = Modifier.size(14.dp)) } }
                }
            }
        }
    }
}

@Composable
private fun EnvironmentToggle(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(text = label, fontSize = 6.sp, fontWeight = FontWeight.Black, color = if(checked) StealthPrimary else Color.White.copy(alpha = 0.3f), letterSpacing = 1.sp); Switch(checked = checked, onCheckedChange = onCheckedChange, modifier = Modifier.scale(0.55f).height(20.dp), colors = SwitchDefaults.colors(checkedThumbColor = StealthPrimary, checkedTrackColor = StealthPrimary.copy(alpha = 0.5f))) } }

@Composable
private fun ConfirmationDialog(title: String, text: String, onConfirm: () -> Unit, onDismiss: () -> Unit) { AlertDialog(onDismissRequest = onDismiss, containerColor = Color.Black, titleContentColor = StealthPrimary, textContentColor = Color.White.copy(alpha = 0.7f), title = { Text(title, fontWeight = FontWeight.Black) }, text = { Text(text, fontSize = 12.sp) }, confirmButton = { TextButton(onClick = onConfirm) { Text("PROCEED", color = Color.Red, fontWeight = FontWeight.Bold) } }, dismissButton = { TextButton(onClick = { onDismiss() }) { Text("CANCEL", color = Color.White.copy(alpha = 0.4f)) } }) }

@Composable
private fun StatusIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, isOn: Boolean, isWeak: Boolean, isPermissionMissing: Boolean, onClick: () -> Unit) { val tint = when { !isOn -> Color.Red; isPermissionMissing -> Color(0xFFF4511E); isWeak -> Color.Yellow; else -> Color.Green }; Icon(imageVector = icon, contentDescription = null, tint = tint.copy(alpha = 0.8f), modifier = Modifier.size(20.dp).clickable { onClick() }) }

@Composable
private fun FullLighthouseScan(rotation: Float, lowPowerMode: Boolean) { if (lowPowerMode && rotation % 10 > 2) return; Canvas(modifier = Modifier.fillMaxSize()) { val center = Offset(56.dp.toPx(), size.height - 64.dp.toPx()); rotate(rotation, pivot = center) { val scanBrush = Brush.sweepGradient(0.0f to StealthPrimary.copy(alpha = if (lowPowerMode) 0.05f else 0.15f), 0.1f to Color.Transparent, center = center); drawCircle(brush = scanBrush, radius = size.maxDimension, center = center) } } }
