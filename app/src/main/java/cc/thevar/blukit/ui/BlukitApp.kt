/**
 * BLUKIT UI: MAIN APP ENTRY
 */
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
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import cc.thevar.blukit.domain.model.MeshMessage
import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.domain.model.MeshRoom
import cc.thevar.blukit.ui.navigation.Route
import cc.thevar.blukit.ui.screens.*
import cc.thevar.blukit.ui.theme.*
import cc.thevar.blukit.ui.viewmodels.BluetoothViewModel
import cc.thevar.blukit.ui.viewmodels.MainViewModel
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalFoundationApi::class)
@Composable
fun BlukitApp(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val permissionManager: cc.thevar.blukit.data.system.SpreadPermissionManager = koinInject()
    
    val viewModel: MainViewModel = koinViewModel()
    val bluetoothViewModel: BluetoothViewModel = koinViewModel()
    val supremePowerViewModel: cc.thevar.blukit.ui.viewmodels.SupremePowerViewModel = koinViewModel()
    
    val nickname by viewModel.nickname.collectAsStateWithLifecycle(initialValue = null)
    val emoji by viewModel.emojiAvatar.collectAsStateWithLifecycle(initialValue = "👤")
    val isStealthMode by viewModel.isStealthMode.collectAsStateWithLifecycle(initialValue = false)
    val lowPowerMode by viewModel.lowPowerMode.collectAsStateWithLifecycle(initialValue = false)
    val bluetoothState by bluetoothViewModel.state.collectAsStateWithLifecycle()
    val highResonancePulses by bluetoothViewModel.highResonancePulses.collectAsStateWithLifecycle()
    val supremeReport by supremePowerViewModel.report.collectAsStateWithLifecycle()

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

    val initialRoute = Route.Discovery
    val backStack = remember { mutableStateListOf<Route>(initialRoute) }
    val currentRoute = backStack.lastOrNull()

    var focusedChainId by remember { mutableStateOf<String?>(null) }
    
    val breadcrumbTrail = remember(backStack.size, bluetoothState, focusedChainId) {
        val trail = mutableListOf<String>()
        backStack.forEach { route ->
            when (route) {
                is Route.Discovery -> trail.add("DISCOVERY")
                is Route.RoomField -> {
                    val group = bluetoothState.session.groups.find { it.id == route.roomId }
                    if (group != null) {
                        group.parentId?.let { pid ->
                            val parent = bluetoothState.session.groups.find { it.id == pid }
                            val parentName = parent?.name ?: "HOME"
                            if (trail.lastOrNull() != parentName) trail.add(parentName)
                        }
                        trail.add(group.name)
                    } else {
                        trail.add("ROOM")
                    }
                }
                else -> trail.add("BLUKIT")
            }
        }
        
        if ((focusedChainId != null) && (currentRoute is Route.RoomField)) {
            val device = bluetoothState.crowd.scannedDevices.find { (it.persistentId == focusedChainId) || (it.id == focusedChainId) }
            trail.add(device?.name ?: "Peer")
        }
        trail.distinct() 
    }

    val onCrumbClick: (Int) -> Unit = { index ->
        val trailSize = breadcrumbTrail.size
        if (focusedChainId != null && index == trailSize - 1) {
            // Already there
        } else if (focusedChainId != null && index == trailSize - 2) {
            focusedChainId = null
        } else {
            if (index < backStack.size) {
                while (backStack.size > index + 1) {
                    backStack.removeLastOrNull()
                }
                focusedChainId = null
            }
        }
    }

    val hubRotation by rememberInfiniteTransition(label = "HubScan").animateFloat(initialValue = 0f, targetValue = 360f, animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing)), label = "Scan")
    var selectedPersonaForMenu by remember { mutableStateOf<P2PDevice?>(null) }
    var isNoiseFilterActive by remember { mutableStateOf(value = false) }

    val listDetailSceneStrategy = rememberListDetailSceneStrategy<Route>()
    val isLocationMandatory = Build.VERSION.SDK_INT < Build.VERSION_CODES.S
    val locationPermissionGranted = remember(permissionState.allPermissionsGranted) {
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }
    
    val crowdIsStill = (!bluetoothState.harmony.isBluetoothEnabled || 
                     !permissionState.essentialPermissionsGranted ||
                     (isLocationMandatory && (!bluetoothState.harmony.isLocationEnabled || !locationPermissionGranted))) &&
                     permissionManager.requiredPermissions.isNotEmpty()

    var messageText by remember { mutableStateOf("") }
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current

    var onboardingStep by remember { mutableIntStateOf(0) }
    
    var showManageDialog by remember { mutableStateOf(value = false) }
    var isSearchMode by remember { mutableStateOf(value = false) }
    var isInputFocused by remember { mutableStateOf(value = false) }
    var showAirGhost by remember { mutableStateOf(value = false) }
    var showPrivacyProtocol by remember { mutableStateOf(false) }
    var showOnboarding by remember { mutableStateOf(false) }
    
    LaunchedEffect(nickname, crowdIsStill) {
        if ((nickname == null || nickname == "?" || nickname == "" || nickname == "SET NAME") && !crowdIsStill) {
            showOnboarding = true
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        uri?.let { bluetoothViewModel.spreadFile(it) }
    }

    val personaCoordinates = remember { mutableStateMapOf<String, PersonaConnectionPoints>() }
    
    LaunchedEffect(currentRoute) {
        personaCoordinates.clear()
        if (currentRoute is Route.Discovery) {
            bluetoothViewModel.enterChain(MeshRoom.ID_GLOBAL)
        }
    }
    var highlightedUserId by remember { mutableStateOf<String?>(null) }
    var showAirIsStillDialog by remember { mutableStateOf(false) }
    var hasNudgedStillAir by remember { mutableStateOf(false) }
    
    LaunchedEffect(crowdIsStill) {
        if (!hasNudgedStillAir && crowdIsStill) {
            delay(1.seconds)
            showAirIsStillDialog = true
            hasNudgedStillAir = true
        }
    }
    
    LaunchedEffect(highlightedUserId) {
        if (highlightedUserId != null) {
            delay(3.seconds)
            highlightedUserId = null
        }
    }

    val activePulseId = remember { mutableStateOf<String?>(null) }
    var discoveredCrowd by remember { mutableStateOf<MeshRoom?>(null) }

    LaunchedEffect(Unit) {
        bluetoothViewModel.discoveredCrowds.collect { room ->
            discoveredCrowd = room
        }
    }

    CompositionLocalProvider(
        LocalPersonaCoordinates provides personaCoordinates,
        LocalActivePulseId provides activePulseId,
        LocalUserEmoji provides emoji
    ) {
        Box(modifier = modifier.fillMaxSize()) {
            FullLighthouseScan(rotation = hubRotation, lowPowerMode = lowPowerMode)
            
            bluetoothState.session.syncProgress?.let { progress ->
                Box(modifier = Modifier.fillMaxSize().zIndex(100f).background(StealthBlack.copy(alpha = 0.6f)), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Syncing mesh history...", 
                            style = MaterialTheme.typography.titleMedium,
                            color = StealthAmber
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.width(200.dp).height(2.dp).clip(RoundedCornerShape(1.dp)),
                            color = StealthAmber,
                            trackColor = Color.White.copy(alpha = 0.1f)
                        )
                    }
                }
            }
            
            val connectionAlpha by rememberInfiniteTransition(label = "LinePulse").animateFloat(
                initialValue = 0.4f, targetValue = 0.8f,
                animationSpec = infiniteRepeatable(tween(2500, easing = LinearEasing), RepeatMode.Reverse),
                label = "Pulse"
            )

            Canvas(modifier = Modifier.fillMaxSize().zIndex(5f)) {
                personaCoordinates.forEach { (id, points) ->
                    val isPulsed = id in bluetoothState.crowd.pulsedPeers
                    val device = bluetoothState.crowd.scannedDevices.find { it.persistentId == id || it.id == id }
                    val isTied = device?.isConnected == true
                    val isPrivateMode = currentRoute is Route.RoomField
                    val isRelevant = if (isPrivateMode) { id == "YOU" || isTied || isPulsed || bluetoothState.crowd.incomingRadioRequests.any { it.persistentId == id || it.id == id } } else { true }
                    if (!isRelevant) return@forEach
                    val isFocused = id == "YOU" || isPulsed || isTied
                    if (isNoiseFilterActive && !isFocused) return@forEach
                    val color = if (id == "YOU") StealthPrimary else if (isTied) StealthRose else StealthPrimary
                    val alphaMultiplier = if (isNoiseFilterActive) 1.2f else 1f
                    val baseAlpha = if (id == "YOU") 0.35f else 0.25f
                    
                    if (points.uph != null && points.ticker != null) {
                        val y = (points.uph.y + points.ticker.y) / 2f
                        drawLine(color = color.copy(alpha = baseAlpha * alphaMultiplier * connectionAlpha), start = Offset(points.ticker.x + 20f, y), end = Offset(points.uph.x - 20f, y), strokeWidth = 1.dp.toPx(), pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(5f, 15f), 0f))
                    }
                }

                val hubPoint = personaCoordinates["YOU"]?.uph
                val ghostPoint = personaCoordinates["ONBOARDING"]?.field
                if (hubPoint != null && ghostPoint != null) {
                    val pulseAlpha = (0.3f + 0.5f * connectionAlpha)
                    drawLine(color = StealthAmber.copy(alpha = pulseAlpha), start = hubPoint, end = ghostPoint, strokeWidth = 2.dp.toPx(), pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 20f), connectionAlpha * 30f))
                }

                val pulseGhostPoint = personaCoordinates["GHOST_PULSE"]?.field
                if (pulseGhostPoint != null) {
                    personaCoordinates.forEach { (_, points) ->
                        val sourcePoint = points.pulse
                        if (sourcePoint != null) {
                            val pulseAlpha = (0.4f + 0.6f * connectionAlpha)
                            drawLine(color = StealthPrimary.copy(alpha = pulseAlpha), start = sourcePoint, end = pulseGhostPoint, strokeWidth = 2.dp.toPx(), pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(15f, 15f), connectionAlpha * 40f))
                        }
                    }
                }
            }

            val topBarHeader: @Composable () -> Unit = {
                val themeColor = if (currentRoute is Route.RoomField) StealthRose else StealthPrimary

                BlukitHeader(
                    themeColor = themeColor,
                    onAwakenBluetooth = { context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS)) },
                    onAwakenWifi = { context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS)) },
                    onGrantPermissions = { permissionState.launchMultiplePermissionRequest() },
                    onOpenSettings = { context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply { data = Uri.fromParts("package", context.packageName, null); addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }) },
                    onShowPrivacy = { showPrivacyProtocol = true },
                    isBluetoothOff = !bluetoothState.harmony.isBluetoothEnabled,
                    isWifiOff = !bluetoothState.harmony.isWifiEnabled,
                    isPermissionMissing = !permissionState.essentialPermissionsGranted,
                    isPermanentlyDenied = isPermanentlyDenied,
                    meshInsights = supremeReport?.aiInsight ?: bluetoothState.session.messages.findLast { it.type == MeshMessage.TYPE_AI_SUMMARY }?.content
                )
            }

            Box(modifier = Modifier.fillMaxSize().zIndex(10f)) {
                NavDisplay(
                    backStack = backStack,
                    onBack = { backStack.removeLastOrNull() },
                    sceneStrategies = listOf(listDetailSceneStrategy),
                    modifier = Modifier.fillMaxSize(),
                    entryProvider = { key ->
                        when (key) {
                            Route.Discovery -> NavEntry(key) { 
                                DiscoveryField(
                                    state = bluetoothState, 
                                    localDeviceId = viewModel.deviceId.collectAsStateWithLifecycle(initialValue = "").value, 
                                    header = topBarHeader,
                                    pulsedPeers = bluetoothState.crowd.pulsedPeers, 
                                    onIdentifyUser = { highlightedUserId = it },
                                    breadcrumbTrail = breadcrumbTrail,
                                    onCrumbClick = onCrumbClick,
                                    userNickname = nickname ?: "?",
                                    onShowTimeline = { backStack.add(Route.Timeline) },
                                    onResetProfile = { viewModel.resetProfile() },
                                    supremeReport = supremeReport,
                                    onBack = if (isSearchMode || showAirGhost) { 
                                        { 
                                            isSearchMode = false
                                            showAirGhost = false
                                            messageText = "" 
                                        } 
                                    } else null,
                                    onTitleClick = { showAirGhost = true; isSearchMode = false; messageText = "" },
                                    onNavigateToGroup = { gid ->
                                        backStack.add(Route.RoomField(gid))
                                        bluetoothViewModel.enterChain(gid)
                                    },
                                    messageText = messageText,
                                    onMessageChange = { messageText = it },
                                    onSearchToggle = { isSearchMode = !isSearchMode; showAirGhost = false; messageText = "" },
                                    onCreatePublicRoom = { name, templateId ->
                                        val gid = bluetoothViewModel.startGroupPulse(name, scope = MeshRoom.SCOPE_PUBLIC, templateId = templateId)
                                        backStack.add(Route.RoomField(gid))
                                        bluetoothViewModel.enterChain(gid)
                                        isSearchMode = false
                                        messageText = ""
                                    },
                                    onAcceptRadio = bluetoothViewModel::acceptRadio, 
                                    onDenyRadio = bluetoothViewModel::denyRadio,
                                    onNavigateToPulse = { backStack.add(Route.MessageField(it)) },
                                    onRestoreCrowd = bluetoothViewModel::restoreFromVault,
                                    onNavigateToLiveFeed = { backStack.add(Route.LiveFeed) },
                                    isSearchActive = isSearchMode,
                                    showAirGhost = showAirGhost,
                                    onShowAirGhost = { showAirGhost = true },
                                    onDismissAirGhost = { showAirGhost = false },
                                ) 
                            }
                            is Route.RoomField -> NavEntry(key) { 
                                val group = bluetoothState.session.groups.find { it.id == key.roomId }
                                if (group?.scope == MeshRoom.SCOPE_PUBLIC) {
                                    RoomField(
                                        state = bluetoothState, 
                                        localDeviceId = viewModel.deviceId.collectAsStateWithLifecycle(initialValue = "").value, 
                                        header = topBarHeader,
                                        roomId = key.roomId, 
                                        highResonanceMessages = highResonancePulses,
                                        onVote = bluetoothViewModel::castVote,
                                        isSearchActive = isSearchMode,
                                        onSearchToggle = { isSearchMode = !isSearchMode; messageText = "" },
                                        onNavigateToPulse = { vid ->
                                            backStack.add(Route.MessageField(vid))
                                        },
                                        breadcrumbTrail = breadcrumbTrail,
                                        onCrumbClick = onCrumbClick,
                                        userNickname = nickname ?: "?",
                                        onShowTimeline = { backStack.add(Route.Timeline) },
                                        onResetProfile = { viewModel.resetProfile() },
                                        onBack = { backStack.removeLastOrNull(); focusedChainId = null },
                                        onTitleClick = null,
                                        messageText = messageText,
                                        onMessageChange = { messageText = it },
                                        onSend = { 
                                            if (messageText.isNotBlank()) { 
                                                bluetoothViewModel.sendMessage(messageText, key.roomId)
                                                messageText = ""
                                                focusManager.clearFocus() 
                                            } 
                                        },
                                        onAcceptRadio = bluetoothViewModel::acceptRadio, 
                                        onDenyRadio = bluetoothViewModel::denyRadio,
                                        onNavigateToLiveFeed = { backStack.add(Route.LiveFeed) },
                                        onStartSidePulse = { val members = bluetoothState.crowd.selectedDevices; if (members.all { it in bluetoothState.session.connectedTies }) { val gid = bluetoothViewModel.startGroupPulse("CHAT", members); backStack.add(Route.RoomField(gid)); bluetoothViewModel.enterChain(gid) } },
                                        onClearSelection = bluetoothViewModel::clearSelection,
                                        onInputFocusChange = { isInputFocused = it },
                                        isStealthMode = isStealthMode,
                                        lowPowerMode = lowPowerMode,
                                        onToggleStealth = viewModel::toggleStealth,
                                        onToggleLowPower = viewModel::toggleLowPowerMode
                                    )
                                } else {
                                    ChannelField(
                                        state = bluetoothState, 
                                        localDeviceId = viewModel.deviceId.collectAsStateWithLifecycle(initialValue = "").value, 
                                        header = topBarHeader,
                                        roomId = key.roomId, 
                                        onRemoveMember = bluetoothViewModel::removeMemberFromGroup, 
                                        onVaultGroup = bluetoothViewModel::vaultGroup,
                                        onSeniorVaultGroup = bluetoothViewModel::seniorVaultGroup,
                                        onAssignRole = bluetoothViewModel::assignRole,
                                        onUpdateNote = bluetoothViewModel::updateNote,
                                        onPushRitual = bluetoothViewModel::pushRitual,
                                        onSendMessage = bluetoothViewModel::sendMessage,
                                        showMemberManagement = showManageDialog, 
                                        onShowManagement = { showManageDialog = true }, 
                                        onDismissManagement = { showManageDialog = false }, 
                                        onNavigateToGroup = { gid ->
                                            backStack.add(Route.RoomField(gid))
                                            bluetoothViewModel.enterChain(gid)
                                        },
                                        onNavigateToPulse = { vid ->
                                            backStack.add(Route.MessageField(vid))
                                        },
                                        breadcrumbTrail = breadcrumbTrail,
                                        onCrumbClick = onCrumbClick,
                                        userNickname = nickname ?: "?",
                                        onShowTimeline = { backStack.add(Route.Timeline) },
                                        onResetProfile = { viewModel.resetProfile() },
                                        onBack = { backStack.removeLastOrNull(); focusedChainId = null },
                                        onTitleClick = null,
                                        messageText = messageText,
                                        onMessageChange = { messageText = it },
                                        onSend = { 
                                            if (messageText.isNotBlank()) { 
                                                bluetoothViewModel.sendMessage(messageText, key.roomId)
                                                messageText = ""
                                                focusManager.clearFocus() 
                                            } 
                                        },
                                        onSearchToggle = { isSearchMode = !isSearchMode; messageText = "" },
                                        onAcceptRadio = bluetoothViewModel::acceptRadio, 
                                        onDenyRadio = bluetoothViewModel::denyRadio,
                                        onNavigateToLiveFeed = { backStack.add(Route.LiveFeed) },
                                        onStartSidePulse = { val members = bluetoothState.crowd.selectedDevices; if (members.all { it in bluetoothState.session.connectedTies }) { val gid = bluetoothViewModel.startGroupPulse("CHAT", members); backStack.add(Route.RoomField(gid)); bluetoothViewModel.enterChain(gid) } },
                                        onClearSelection = bluetoothViewModel::clearSelection,
                                        onInputFocusChange = { isInputFocused = it },
                                        isStealthMode = isStealthMode,
                                        lowPowerMode = lowPowerMode,
                                        onToggleStealth = viewModel::toggleStealth,
                                        onToggleLowPower = viewModel::toggleLowPowerMode
                                    ) 
                                }
                            }
                            is Route.MessageField -> NavEntry(key) {
                                MessageField(
                                    state = bluetoothState,
                                    localDeviceId = viewModel.deviceId.collectAsStateWithLifecycle(initialValue = "").value,
                                    header = topBarHeader,
                                    messageId = key.messageId,
                                    onNavigateToPulse = { vid -> backStack.add(Route.MessageField(vid)) },
                                    onNavigateToLiveFeed = { backStack.add(Route.LiveFeed) },
                                    breadcrumbTrail = breadcrumbTrail,
                                    onCrumbClick = onCrumbClick,
                                    userNickname = nickname ?: "?",
                                    onShowTimeline = { backStack.add(Route.Timeline) },
                                    onResetProfile = { viewModel.resetProfile() },
                                    onBack = { backStack.removeLastOrNull() },
                                    onTitleClick = null,
                                    messageText = messageText,
                                    onMessageChange = { messageText = it },
                                    onSend = {
                                        if (messageText.isNotBlank()) {
                                            messageText = ""
                                            focusManager.clearFocus()
                                        }
                                    },
                                    onSearchToggle = { isSearchMode = !isSearchMode; messageText = "" },
                                    onAttachFile = { filePickerLauncher.launch("*/*") },
                                    onInputFocusChange = { isInputFocused = it }
                                )
                            }
                            Route.Timeline -> NavEntry(key) {
                                TimelineField(
                                    messages = bluetoothState.session.messages,
                                    onBack = { backStack.removeLastOrNull() }
                                )
                            }
                            Route.LiveFeed -> NavEntry(key) {
                                LiveFeedField(
                                    messages = bluetoothState.session.messages,
                                    peers = bluetoothState.crowd.scannedDevices,
                                    onBack = { backStack.removeLastOrNull() },
                                    onNavigateToPulse = { backStack.add(Route.MessageField(it)) }
                                )
                            }
                        }
                    }
                )
            }

            if (selectedPersonaForMenu != null) {
                val menuDevice = selectedPersonaForMenu!!
                val menuId = menuDevice.persistentId ?: menuDevice.id
                PeerOptionsMenu(
                    device = menuDevice, 
                    isTied = menuDevice.id in bluetoothState.session.connectedTies, 
                    isBlocked = menuId in bluetoothState.crowd.blockedUsers, 
                    isRequesting = bluetoothState.crowd.incomingRadioRequests.any { it.id == menuDevice.id }, 
                    activeGroupId = (currentRoute as? Route.RoomField)?.roomId, 
                    isAlreadyInActiveGroup = (bluetoothState.session.groups.find { it.id == (currentRoute as? Route.RoomField)?.roomId }?.memberIds?.contains(menuId) == true), 
                    onPulse = { bluetoothViewModel.requestWhisper(menuDevice); selectedPersonaForMenu = null }, 
                    onAccept = { bluetoothViewModel.acceptRadio(menuDevice); selectedPersonaForMenu = null }, 
                    onDeny = { bluetoothViewModel.denyRadio(menuDevice); selectedPersonaForMenu = null }, 
                    onDisconnect = { bluetoothViewModel.disconnect(); selectedPersonaForMenu = null }, 
                    onSelect = { bluetoothViewModel.toggleDeviceSelection(menuDevice.id); selectedPersonaForMenu = null }, 
                    onIdentify = { highlightedUserId = menuId; selectedPersonaForMenu = null }, 
                    onBlock = { viewModel.blockUser(menuId); selectedPersonaForMenu = null }, 
                    onUnblock = { viewModel.unblockUser(menuId); selectedPersonaForMenu = null }, 
                    onSync = { bluetoothViewModel.initiateHistorySync(menuDevice.id); selectedPersonaForMenu = null }, 
                    onAddToGroup = { gid -> bluetoothViewModel.addMemberToGroup(gid, menuDevice.id); selectedPersonaForMenu = null }, 
                    onRemoveFromGroup = { gid -> bluetoothViewModel.removeMemberFromGroup(gid, menuDevice.id); selectedPersonaForMenu = null }, 
                    onDismiss = { selectedPersonaForMenu = null }
                )
            }

            if (showAirIsStillDialog) {
                ConfirmationDialog(
                    title = "Spectrum Silent", 
                    text = "Radios are silent. Awaken Bluetooth or grant permissions to dive into the mesh.", 
                    onConfirm = { 
                        showAirIsStillDialog = false
                        if (!permissionState.essentialPermissionsGranted) permissionState.launchMultiplePermissionRequest() 
                        else if (!bluetoothState.harmony.isBluetoothEnabled) context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }) 
                        else if (isLocationMandatory && (!bluetoothState.harmony.isLocationEnabled || !locationPermissionGranted)) context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }) 
                    }, 
                    onDismiss = { showAirIsStillDialog = false }
                )
            }

            if (showPrivacyProtocol) {
                val themeColor = if (currentRoute is Route.RoomField) StealthRose else StealthPrimary
                BlukitAlert(
                    title = "Mesh Protocol",
                    text = "Blukit is anonymous-first. 100% offline P2P. Messages stay on your device until you choose to emit them.",
                    confirmLabel = "RESPECT",
                    themeColor = themeColor,
                    onConfirm = { showPrivacyProtocol = false },
                    onDismiss = { showPrivacyProtocol = false }
                )
            }

            if (showOnboarding) {
                Box(modifier = Modifier.fillMaxSize().zIndex(200f)) {
                    WelcomeGhost(
                        nickname = nickname ?: "",
                        emoji = emoji,
                        onNicknameChange = { newName ->
                            viewModel.saveNickname(newName)
                        },
                        onDone = { 
                            showOnboarding = false 
                            onboardingStep = 1 
                            val currentName = nickname ?: "?"
                            if (currentName != "?" && currentName != "SET NAME") {
                                bluetoothViewModel.broadcastIdentityUpdate(currentName)
                            }
                        },
                        onDismiss = { showOnboarding = false }
                    )
                }
            }

            if (onboardingStep in 1..3 && !showOnboarding) {
                val tipText = when (onboardingStep) {
                    1 -> "This app works without the internet. It uses your phone's radio to talk to people in the same house."
                    2 -> "ROOMS are like different areas of the house. See who is in each room on the radar."
                    else -> "Switch to LIVE FEED to see a continuous stream of everything happening nearby."
                }
                
                Box(modifier = Modifier.fillMaxSize().padding(top = 120.dp).zIndex(150f), contentAlignment = Alignment.TopCenter) {
                    BlukitTip(
                        text = tipText,
                        onDismiss = { onboardingStep++ },
                        themeColor = if (onboardingStep == 3) StealthRose else StealthAmber
                    )
                }
            }
        }
    }
}

@Composable
private fun FullLighthouseScan(rotation: Float, lowPowerMode: Boolean) { 
    if (lowPowerMode && (rotation % 10 > 2)) return
    
    val infiniteTransition = rememberInfiniteTransition(label = "ScanGlow")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.05f, 
        targetValue = 0.15f, 
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Reverse),
        label = "Pulse"
    )

    Canvas(modifier = Modifier.fillMaxSize()) { 
        val center = Offset(56.dp.toPx(), size.height - 64.dp.toPx())
        
        drawCircle(
            brush = Brush.radialGradient(
                0.0f to StealthPrimary.copy(alpha = pulseAlpha),
                1.0f to Color.Transparent,
                center = center,
                radius = 400.dp.toPx()
            ),
            radius = 400.dp.toPx(),
            center = center
        )

        rotate(rotation, pivot = center) { 
            val scanBrush = Brush.sweepGradient(
                0.0f to StealthPrimary.copy(alpha = if (lowPowerMode) StealthAlphaLow else 0.2f), 
                0.15f to Color.Transparent, 
                center = center
            )
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
fun rememberSpreadPermissionsState(
    allPermissions: List<String>,
    essentialPermissions: List<String>
): SpreadPermissionsState {
    val context = LocalContext.current
    var allGranted by remember { mutableStateOf(allPermissions.all { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }) }
    var essentialGranted by remember { mutableStateOf(essentialPermissions.all { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }) }
    
    val checkPermissions = {
        allGranted = allPermissions.all { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }
        essentialGranted = essentialPermissions.all { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { _ -> 
        checkPermissions()
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                checkPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    return object : SpreadPermissionsState {
        override val allPermissionsGranted: Boolean get() = allGranted
        override val essentialPermissionsGranted: Boolean get() = essentialGranted
        override val shouldShowRationale: Boolean get() = (context as? Activity)?.let { act -> allPermissions.any { p -> androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale(act, p) } } ?: false
        override fun launchMultiplePermissionRequest() { launcher.launch(allPermissions.toTypedArray()) }
    }
}
