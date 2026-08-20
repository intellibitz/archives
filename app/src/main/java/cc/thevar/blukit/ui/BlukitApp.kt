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
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
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
    var shouldShowRationale by remember { mutableStateOf(permissions.any { (context as? Activity)?.shouldShowRequestPermissionRationale(it) == true }) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        allGranted = manager.checkAllGranted()
        shouldShowRationale = permissions.any { (context as? Activity)?.shouldShowRequestPermissionRationale(it) == true }
        manager.refresh()
    }
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            allGranted = manager.checkAllGranted()
            shouldShowRationale = permissions.any { (context as? Activity)?.shouldShowRequestPermissionRationale(it) == true }
            manager.refresh()
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
    var messageText by remember { mutableStateOf("") }
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    val roarsCount = remember(bluetoothState.session.messages) { bluetoothState.session.messages.count { it.receiverId.isNullOrBlank() } }
    val mineCount = remember(bluetoothState.session.messages) { bluetoothState.session.messages.count { !it.receiverId.isNullOrBlank() } }

    val config = LocalConfiguration.current
    val isLandscape = config.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    var showManageDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        FullLighthouseScan(rotation = hubRotation, lowPowerMode = lowPowerMode)

        Column(modifier = Modifier.fillMaxSize()) {
            var focusedSenderId by remember { mutableStateOf<String?>(null) }
            
            val topTitle = when {
                focusedSenderId != null -> {
                    val device = bluetoothState.crowd.scannedDevices.find { it.id == focusedSenderId || it.persistentId == focusedSenderId }
                    device?.name?.uppercase() ?: "USER"
                }
                currentRoute is Route.Blukit -> "ALL"
                currentRoute is Route.Mine -> "MINE"
                currentRoute is Route.VibeDetail -> {
                    val group = bluetoothState.session.groups.find { it.id == currentRoute.groupId }
                    group?.name ?: "VIBE"
                }
                else -> "BLUKIT"
            }
            
            val topIcon = when {
                focusedSenderId != null -> Icons.Rounded.Person
                currentRoute is Route.Blukit -> Icons.Rounded.Groups
                currentRoute is Route.Mine -> Icons.Rounded.Flare
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
                vibeCount = roarsCount + mineCount,
                onToggleStealth = viewModel::toggleStealth,
                onToggleLowPower = viewModel::toggleLowPowerMode,
                onClearHistory = viewModel::clearChatHistory,
                onResetProfile = viewModel::logout,
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
                val showUPH = (currentRoute is Route.Blukit || currentRoute is Route.Mine)

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
                                    val id = device.persistentId ?: device.id
                                    viewModel.toggleVibePeer(id)
                                    isNoiseFilterActive = true
                                }, 
                                onDeviceLongClick = { selectedPersonaForMenu = it }, 
                                onBroadcastMessage = bluetoothViewModel::roar, 
                                onDeleteVibe = viewModel::deleteVibe, 
                                onBlockUser = viewModel::blockUser, 
                                onUnblockUser = viewModel::unblockUser,
                                onWhisper = { device -> val id = device.persistentId ?: device.id; val gid = bluetoothViewModel.startGroupVibe("WHISPER", setOf(id), isTie = false); backStack.add(Route.VibeDetail(gid)) },
                                hasSidebar = showUPH,
                                externalFocusedId = focusedSenderId,
                                onFocusChange = { focusedSenderId = it }
                            ) 
                        }
                        Route.Mine -> NavEntry(key) { 
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
                            activeBubbles = bluetoothState.session.messages.map { BubbleData(it.senderId, it.content, it.timestamp, it.messageId, !it.receiverId.isNullOrBlank()) }, 
                            onDeviceClick = { device -> 
                                if (bluetoothState.crowd.selectedDevices.isEmpty()) { 
                                    val id = device.persistentId ?: device.id
                                    viewModel.toggleVibePeer(id)
                                    if (currentRoute is Route.Blukit) {
                                        isNoiseFilterActive = true
                                    }
                                } else { 
                                    bluetoothViewModel.toggleDeviceSelection(device.id) 
                                } 
                            }, 
                            onDeviceLongClick = { selectedPersonaForMenu = it },
                            isVertical = true,
                            userNickname = nickname ?: "?",
                            userEmoji = emoji,
                            onUserNicknameChange = viewModel::saveNickname,
                            userFocusRequester = personaFocusRequester,
                            airIsStill = !bluetoothState.harmony.isBluetoothEnabled || (isLocationMandatory && !bluetoothState.harmony.isLocationEnabled) || !permissionState.allPermissionsGranted
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
                        bluetoothViewModel.roar(messageText, currentRoute is Route.Mine)
                        messageText = ""
                        focusManager.clearFocus() 
                    } 
                },
                vibeCount = roarsCount + mineCount,
                airIsStill = !bluetoothState.harmony.isBluetoothEnabled || (isLocationMandatory && !bluetoothState.harmony.isLocationEnabled) || !permissionState.allPermissionsGranted,
                incomingLinkRequests = bluetoothState.crowd.incomingLinkRequests, 
                selectedDevices = bluetoothState.crowd.selectedDevices,
                isNoiseFilterActive = isNoiseFilterActive,
                onToggleNoiseFilter = { isNoiseFilterActive = it },
                onNavigate = { route -> 
                    if (route == Route.Blukit) isNoiseFilterActive = false
                    if (currentRoute != route) { 
                        focusManager.clearFocus()
                        backStack.add(route) 
                    } 
                },
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
            PersonaOptionsMenu(
                device = selectedPersonaForMenu!!,
                isVibed = (selectedPersonaForMenu!!.persistentId ?: selectedPersonaForMenu!!.id) in bluetoothState.crowd.vibedPeers,
                isTied = selectedPersonaForMenu!!.id in bluetoothState.session.connectedLinks,
                isBlocked = (selectedPersonaForMenu!!.persistentId ?: selectedPersonaForMenu!!.id) in bluetoothState.crowd.blockedUsers,
                onFocus = { val id = selectedPersonaForMenu!!.persistentId ?: selectedPersonaForMenu!!.id; viewModel.toggleVibePeer(id); selectedPersonaForMenu = null },
                onVibe = { if (selectedPersonaForMenu!!.id !in bluetoothState.session.connectedLinks) bluetoothViewModel.connectToDevice(selectedPersonaForMenu!!); selectedPersonaForMenu = null },
                onWhisper = { val id = selectedPersonaForMenu!!.persistentId ?: selectedPersonaForMenu!!.id; val gid = bluetoothViewModel.startGroupVibe("WHISPER", setOf(id), isTie = false); backStack.add(Route.VibeDetail(gid)); selectedPersonaForMenu = null },
                onBlock = { val id = selectedPersonaForMenu!!.persistentId ?: selectedPersonaForMenu!!.id; viewModel.blockUser(id); selectedPersonaForMenu = null },
                onUnblock = { val id = selectedPersonaForMenu!!.persistentId ?: selectedPersonaForMenu!!.id; viewModel.unblockUser(id); selectedPersonaForMenu = null },
                onDismiss = { selectedPersonaForMenu = null }
            )
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
    onToggleNoiseFilter: (Boolean) -> Unit, 
    onNavigate: (Route) -> Unit, 
    onAcceptLink: (P2PDevice) -> Unit, 
    onDenyLink: (P2PDevice) -> Unit, 
    onStartSideVibe: () -> Unit, 
    onStartTie: () -> Unit, 
    onClearSelection: () -> Unit, 
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth().zIndex(1f), horizontalAlignment = Alignment.CenterHorizontally) {
        AnimatedVisibility(visible = selectedDevices.isNotEmpty(), enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
            Row(modifier = Modifier.padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onStartSideVibe, colors = ButtonDefaults.buttonColors(containerColor = StealthPrimary, contentColor = Color.Black), shape = RoundedCornerShape(12.dp)) { Text("WHISPER", fontWeight = FontWeight.Black, fontSize = 10.sp) }
                Button(onClick = onStartTie, colors = ButtonDefaults.buttonColors(containerColor = StealthRose, contentColor = Color.White), shape = RoundedCornerShape(12.dp)) { Text("START VIBE", fontWeight = FontWeight.Black, fontSize = 10.sp) }
                IconButton(onClick = onClearSelection, modifier = Modifier.background(Color.White.copy(alpha = 0.1f), CircleShape)) { Icon(Icons.Rounded.Close, tint = Color.White, contentDescription = "Cancel") }
            }
        }
        Box(modifier = Modifier.fillMaxWidth().background(Color.Black.copy(alpha = 0.96f), RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)).border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))) {
            Column(modifier = Modifier.padding(vertical = 12.dp, horizontal = 12.dp).imePadding()) {
                AnimatedVisibility(visible = currentRoute is Route.Blukit || currentRoute is Route.Mine) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BlukitInput(
                            airIsStill = airIsStill, 
                            isReadOnly = isNoiseFilterActive && currentRoute is Route.Blukit,
                            value = messageText, 
                            onValueChange = onMessageChange, 
                            onSend = onSend, 
                            vibeCount = vibeCount, 
                            modifier = Modifier.weight(1f)
                        )
                        if (currentRoute is Route.Blukit) {
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = { onToggleNoiseFilter(!isNoiseFilterActive) }, modifier = Modifier.size(48.dp).background(if (isNoiseFilterActive) StealthPrimary.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f), CircleShape).border(1.dp, if (isNoiseFilterActive) StealthPrimary else Color.Transparent, CircleShape)) {
                                Icon(imageVector = if (isNoiseFilterActive) Icons.Rounded.FilterCenterFocus else Icons.Rounded.Tune, contentDescription = "Filter", tint = if (isNoiseFilterActive) StealthPrimary else Color.White.copy(alpha = 0.4f), modifier = Modifier.size(20.dp))
                            }
                        }
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

                VisualEnergyPicker(
                    currentRoute = currentRoute, 
                    onNavigate = onNavigate
                )
                Spacer(modifier = Modifier.height(12.dp))
                val localContext = LocalContext.current
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Icon(painter = painterResource(id = R.drawable.ic_blukit_logo), contentDescription = null, tint = StealthPrimary.copy(alpha = 0.3f), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "blukit: spread vibes", fontSize = 8.sp, fontWeight = FontWeight.Black, color = StealthPrimary.copy(alpha = 0.3f), letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = 0.05f)).clickable { 
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/intellibitz/blukit/blob/main/PRIVACY_POLICY.md"))
                        localContext.startActivity(intent)
                    }.padding(horizontal = 8.dp, vertical = 2.dp), contentAlignment = Alignment.Center) {
                        Text(text = "PRIVACY", style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.sp, fontWeight = FontWeight.Black, color = Color.White.copy(alpha = 0.25f)))
                    }
                }
            }
        }
    }
}

@Composable
private fun VisualEnergyPicker(
    currentRoute: Route, 
    onNavigate: (Route) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth().background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(14.dp)).padding(2.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        HubTab(label = "ALL", icon = Icons.Rounded.Groups, isSelected = currentRoute is Route.Blukit, weight = 1.2f, testTag = "HubTab_ALL", onClick = { onNavigate(Route.Blukit) })
        Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.White.copy(alpha = 0.08f)))
        HubTab(label = "MINE", icon = Icons.Rounded.Flare, isSelected = currentRoute is Route.Mine, weight = 1.2f, testTag = "HubTab_MINE", onClick = { onNavigate(Route.Mine) })
    }
}

@Composable
private fun RowScope.HubTab(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, isSelected: Boolean, weight: Float, testTag: String, onClick: () -> Unit) {
    Box(modifier = Modifier.height(52.dp).weight(weight).clip(RoundedCornerShape(12.dp)).background(if (isSelected) Color.White.copy(alpha = 0.08f) else Color.Transparent).clickable { onClick() }.testTag(testTag), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(imageVector = icon, contentDescription = null, tint = if (isSelected) (if (label == "MINE") StealthRose else StealthPrimary) else Color.White.copy(alpha = 0.25f), modifier = Modifier.size(if (weight > 1.2f) 20.dp else 16.dp))
            Text(text = label, fontSize = if (weight > 1.2f) 10.sp else 7.sp, fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold, color = if (isSelected) (if (label == "MINE") StealthRose else StealthPrimary) else Color.White.copy(alpha = 0.25f), letterSpacing = if (weight > 1.2f) 1.sp else 0.sp)
        }
    }
}

@Composable
private fun FullLighthouseScan(rotation: Float, lowPowerMode: Boolean) { if (lowPowerMode && rotation % 10 > 2) return; Canvas(modifier = Modifier.fillMaxSize()) { val center = Offset(56.dp.toPx(), size.height - 64.dp.toPx()); rotate(rotation, pivot = center) { val scanBrush = Brush.sweepGradient(0.0f to StealthPrimary.copy(alpha = if (lowPowerMode) 0.05f else 0.15f), 0.1f to Color.Transparent, center = center); drawCircle(brush = scanBrush, radius = size.maxDimension, center = center) } } }
