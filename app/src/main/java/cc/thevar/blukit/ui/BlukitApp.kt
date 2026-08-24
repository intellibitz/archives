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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
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
        essentialPermissions = permissionManager.essentialPermissions
    )
    val isPermanentlyDenied = !permissionState.allPermissionsGranted && !permissionState.shouldShowRationale

    val initialRoute = Route.Atmos
    val backStack = remember { mutableStateListOf<Route>(initialRoute) }
    val currentRoute = backStack.lastOrNull()

    var focusedTieId by remember { mutableStateOf<String?>(null) }
    
    // BREADCRUMB LOGIC: Nested Scoping (AIR > TIE > PERSONA)
    val breadcrumbTrail = remember(backStack.size, bluetoothState, focusedTieId) {
        val trail = mutableListOf<String>()
        backStack.forEach { route ->
            when (route) {
                is Route.Atmos -> trail.add("Atmos")
                is Route.GroupField -> {
                    val group = bluetoothState.session.groups.find { it.id == route.groupId }
                    if (group != null) {
                        // If it has a parent Air, add it if not already present
                        group.parentId?.let { pid ->
                            val parent = bluetoothState.session.groups.find { it.id == pid }
                            val parentName = parent?.name ?: "Air"
                            if (trail.lastOrNull() != parentName) trail.add(parentName)
                        }
                        trail.add(group.name)
                    } else {
                        trail.add("Depth")
                    }
                }
                else -> trail.add("Blukit")
            }
        }
        
        // Add Persona if focused
        if (focusedTieId != null && currentRoute is Route.GroupField) {
            val device = bluetoothState.crowd.scannedDevices.find { it.persistentId == focusedTieId || it.id == focusedTieId }
            trail.add(device?.name ?: "Persona")
        }
        trail.distinct() // Ensure no duplicates if backstack already contains parent
    }

    val onCrumbClick: (Int) -> Unit = { index ->
        val trailSize = breadcrumbTrail.size
        // If clicking a persona crumb
        if (focusedTieId != null && index == trailSize - 1) {
            // Stay here
        } else if (focusedTieId != null && index == trailSize - 2) {
            // Clicked the Tie crumb while in Persona view
            focusedTieId = null
        } else {
            // Standard backstack navigation
            // Map trail index back to backstack index (approximate)
            val routeIndex = if (focusedTieId != null) index else index
            if (routeIndex < backStack.size) {
                while (backStack.size > routeIndex + 1) {
                    backStack.removeLastOrNull()
                }
                focusedTieId = null
            }
        }
    }

    val hubRotation by rememberInfiniteTransition(label = "HubScan").animateFloat(initialValue = 0f, targetValue = 360f, animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing)), label = "Scan")
    var selectedPersonaForMenu by remember { mutableStateOf<P2PDevice?>(null) }
    var isNoiseFilterActive by remember { mutableStateOf(false) }

    val listDetailSceneStrategy = rememberListDetailSceneStrategy<Route>()
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

    var showManageDialog by remember { mutableStateOf(false) }
    var isSearchMode by remember { mutableStateOf(false) }
    var showAirGhost by remember { mutableStateOf(false) }
    var airProposalName by remember { mutableStateOf("") }
    var showPrivacyProtocol by remember { mutableStateOf(false) }
    var showOnboarding by remember(nickname, airIsStill) { 
        mutableStateOf((nickname == null || nickname == "?" || nickname == "" || nickname == "SET NAME") && !airIsStill) 
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let { bluetoothViewModel.spreadFile(it) }
        }
    )

    val personaCoordinates = remember { mutableStateMapOf<String, PersonaConnectionPoints>() }
    
    LaunchedEffect(currentRoute) {
        personaCoordinates.clear()
        if (currentRoute is Route.Atmos) {
            bluetoothViewModel.enterTie(VibeGroup.ID_AIR)
        }
    }
    var highlightedUserId by remember { mutableStateOf<String?>(null) }
    var showAirIsStillDialog by remember { mutableStateOf(false) }
    var hasNudgedStillAir by remember { mutableStateOf(false) }
    
    LaunchedEffect(airIsStill) {
        if (!hasNudgedStillAir && airIsStill) {
            delay(1000)
            showAirIsStillDialog = true
            hasNudgedStillAir = true
        }
    }
    
    LaunchedEffect(highlightedUserId) {
        if (highlightedUserId != null) {
            delay(3000)
            highlightedUserId = null
        }
    }

    val locationManager = remember { context.getSystemService(android.content.Context.LOCATION_SERVICE) as android.location.LocationManager }
    LaunchedEffect(locationPermissionGranted) {
        if (locationPermissionGranted) {
            try {
                locationManager.requestLocationUpdates(
                    android.location.LocationManager.PASSIVE_PROVIDER,
                    5000L,
                    10f,
                    object : android.location.LocationListener {
                        override fun onLocationChanged(location: android.location.Location) {
                            supremePowerViewModel.updateLocation(location)
                        }
                        @Deprecated("Deprecated in Java")
                        override fun onStatusChanged(p0: String?, p1: Int, p2: android.os.Bundle?) {}
                        override fun onProviderEnabled(p0: String) {}
                        override fun onProviderDisabled(p0: String) {}
                    }
                )
            } catch (e: SecurityException) {}
        }
    }

    val activeVibeId = remember { mutableStateOf<String?>(null) }
    var discoveredAir by remember { mutableStateOf<VibeGroup?>(null) }

    LaunchedEffect(Unit) {
        bluetoothViewModel.discoveredAirs.collect {
            discoveredAir = it
        }
    }

    CompositionLocalProvider(
        LocalPersonaCoordinates provides personaCoordinates,
        LocalActiveVibeId provides activeVibeId
    ) {
        Box(modifier = modifier.fillMaxSize()) {
            FullLighthouseScan(rotation = hubRotation, lowPowerMode = lowPowerMode)
            
            // SYNC RESONANCE
            bluetoothState.session.syncProgress?.let { progress ->
                Box(modifier = Modifier.fillMaxSize().zIndex(100f).background(Color.Black.copy(alpha = 0.4f)), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("VIBE SYNC IN PROGRESS", color = StealthAmber, fontWeight = FontWeight.Black, fontSize = 12.sp, letterSpacing = 2.sp)
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
                    val isVibed = id in bluetoothState.crowd.vibedPeers
                    val device = bluetoothState.crowd.scannedDevices.find { it.persistentId == id || it.id == id }
                    val isTied = device?.isConnected == true
                    val isPrivateMode = currentRoute is Route.Vibes || currentRoute is Route.GroupField
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

                val hubPoint = personaCoordinates["YOU"]?.uph
                val ghostPoint = personaCoordinates["ONBOARDING"]?.field
                if (hubPoint != null && ghostPoint != null) {
                    val pulseAlpha = (0.3f + 0.5f * connectionAlpha)
                    drawLine(color = StealthAmber.copy(alpha = pulseAlpha), start = hubPoint, end = ghostPoint, strokeWidth = 2.dp.toPx(), pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 20f), connectionAlpha * 30f))
                }

                val vibeGhostPoint = personaCoordinates["GHOST_VIBE"]?.field
                if (vibeGhostPoint != null) {
                    personaCoordinates.forEach { (id, points) ->
                        val sourcePoint = points.vibe
                        if (sourcePoint != null) {
                            val pulseAlpha = (0.4f + 0.6f * connectionAlpha)
                            drawLine(color = StealthPrimary.copy(alpha = pulseAlpha), start = sourcePoint, end = vibeGhostPoint, strokeWidth = 2.dp.toPx(), pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(15f, 15f), connectionAlpha * 40f))
                        }
                    }
                }
            }

            Column(modifier = Modifier.fillMaxSize()) {
                val topTitle = when { 
                    currentRoute is Route.Atmos -> "ATMOS"
                    currentRoute is Route.GroupField -> { 
                        val group = bluetoothState.session.groups.find { it.id == currentRoute.groupId }
                        group?.name ?: "DEPTH"
                    }
                    else -> "BLUKIT" 
                }
                val topIcon = when { currentRoute is Route.Atmos -> Icons.Rounded.Groups; currentRoute is Route.GroupField -> { val group = bluetoothState.session.groups.find { it.id == currentRoute.groupId }; if (group?.scope == VibeGroup.SCOPE_PRIVATE) Icons.Rounded.Hearing else if (group?.scope == VibeGroup.SCOPE_LOCAL) Icons.Rounded.CellTower else Icons.Rounded.Grain }; else -> Icons.Rounded.Hub }

                BlukitHarmonyTopBar(
                    title = topTitle, 
                    icon = topIcon,
                    currentRoute = currentRoute ?: initialRoute,
                    breadcrumbTrail = breadcrumbTrail,
                    onCrumbClick = onCrumbClick,
                    onNavigate = { route -> 
                        if (currentRoute == route && route is Route.Atmos) { 
                            viewModel.clearVibedPeers()
                            isNoiseFilterActive = false
                            focusedTieId = null
                        } else if (currentRoute != route) { 
                            focusManager.clearFocus() 
                            focusedTieId = null
                            backStack.add(route as Route) 
                        } 
                    },
                    userNickname = nickname ?: "?",
                    userEmoji = emoji,
                    onUserNicknameChange = { newName ->
                        val oldName = nickname ?: "?"
                        viewModel.saveNickname(newName)
                        if (oldName != "?" && oldName != newName && oldName != "SET NAME") {
                            bluetoothViewModel.broadcastIdentityUpdate(oldName)
                        }
                    },
                    onResetProfile = {
                        viewModel.resetProfile()
                    },
                    userFocusRequester = personaFocusRequester,
                    isBluetoothOff = !bluetoothState.harmony.isBluetoothEnabled,
                    isLocationOff = !bluetoothState.harmony.isLocationEnabled,
                    isWifiOff = !bluetoothState.harmony.isWifiEnabled,
                    isPermissionMissing = !permissionState.essentialPermissionsGranted,
                    isPermanentlyDenied = isPermanentlyDenied,
                    userCount = report.userCount,
                    isStealthMode = isStealthMode,
                    lowPowerMode = lowPowerMode,
                    airIsStill = airIsStill,
                    isLocationMandatory = isLocationMandatory,
                    activeAirs = bluetoothState.session.groups,
                    onToggleStealth = viewModel::toggleStealth,
                    onToggleLowPower = viewModel::toggleLowPowerMode,
                    onAwakenBluetooth = { context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS)) },
                    onAwakenLocation = { context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) },
                    onAwakenWifi = { context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS)) },
                    onGrantPermissions = { permissionState.launchMultiplePermissionRequest() },
                    onOpenSettings = { context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply { data = Uri.fromParts("package", context.packageName, null); addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }) },
                    onClearHistory = viewModel::clearChatHistory,
                    onShowPrivacy = { showPrivacyProtocol = true },
                    onProfileClick = { if (currentRoute is Route.Atmos) showOnboarding = true else personaFocusRequester.requestFocus() },
                    onBack = if (currentRoute is Route.GroupField || isSearchMode || showAirGhost) { 
                        { 
                            if (isSearchMode || showAirGhost) { 
                                isSearchMode = false
                                showAirGhost = false
                                messageText = "" 
                            } else if (focusedTieId != null) {
                                focusedTieId = null
                            } else {
                                backStack.removeLastOrNull() 
                            }
                        } 
                    } else null,
                    onTitleClick = if (currentRoute is Route.Atmos || currentRoute == null) {
                        { showAirGhost = true; isSearchMode = false; messageText = "" }
                    } else null
                )

                AnimatedVisibility(
                    visible = discoveredAir != null,
                    enter = slideInVertically() + fadeIn(),
                    exit = slideOutVertically() + fadeOut()
                ) {
                    discoveredAir?.let { group ->
                        AirNudge(
                            group = group,
                            onJoin = { 
                                val route = Route.GroupField(group.id)
                                backStack.add(route)
                                bluetoothViewModel.enterTie(group.id)
                                discoveredAir = null
                            },
                            onDismiss = { discoveredAir = null }
                        )
                    }
                }

                // PROACTIVE GEOSPATIAL NUDGES
                val proactiveSuggestion = report.suggestedAirs.firstOrNull()
                AnimatedVisibility(
                    visible = proactiveSuggestion != null && discoveredAir == null,
                    enter = slideInVertically() + fadeIn(),
                    exit = slideOutVertically() + fadeOut()
                ) {
                    proactiveSuggestion?.let { name ->
                        AirNudge(
                            group = VibeGroup(id = "suggested_$name", name = name, scope = VibeGroup.SCOPE_PUBLIC),
                            onJoin = { 
                                val gid = bluetoothViewModel.startGroupVibe(name, scope = VibeGroup.SCOPE_PUBLIC)
                                val route = Route.GroupField(gid)
                                backStack.add(route)
                                bluetoothViewModel.enterTie(gid)
                            },
                            onDismiss = { /* Optionally hide for a while */ }
                        )
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    NavDisplay(
                        backStack = backStack,
                        onBack = { backStack.removeLastOrNull() },
                        sceneStrategies = listOf(listDetailSceneStrategy),
                        modifier = Modifier.fillMaxSize(),
                        entryProvider = { key ->
                            when (key) {
                                Route.Atmos -> NavEntry(key) { 
                                    AtmosField(
                                        state = bluetoothState, 
                                        localDeviceId = viewModel.deviceId.collectAsStateWithLifecycle(initialValue = "").value, 
                                        localNickname = nickname ?: "?", 
                                        localEmoji = emoji, 
                                        vibedPeers = bluetoothState.crowd.vibedPeers, 
                                        noiseFilterEnabled = isNoiseFilterActive, 
                                        onStartScan = bluetoothViewModel::startScan, 
                                        onDeviceClick = { device -> if (bluetoothState.crowd.selectedDevices.isNotEmpty()) { bluetoothViewModel.toggleDeviceSelection(device.id) } else { val id = device.persistentId ?: device.id; viewModel.toggleVibePeer(id); isNoiseFilterActive = true } }, 
                                        onDeleteVibe = viewModel::deleteVibe, 
                                        onWhisper = { device -> val id = device.persistentId ?: device.id; val gid = bluetoothViewModel.startGroupVibe("WHISPER", setOf(id)); backStack.add(Route.GroupField(gid)); bluetoothViewModel.enterTie(gid) }, 
                                        onIdentifyUser = { highlightedUserId = it }, 
                                        onNicknameChange = viewModel::saveNickname, 
                                        onOnboardingDone = { showOnboarding = false },
                                        showOnboarding = showOnboarding,
                                        airIsStill = airIsStill,
                                        onNavigateToGroup = { gid ->
                                            backStack.add(Route.GroupField(gid))
                                            bluetoothViewModel.enterTie(gid)
                                        },
                                        // Hub Props
                                        messageText = messageText,
                                        onMessageChange = { messageText = it },
                                        onSend = { 
                                            if (messageText.isNotBlank() && !isSearchMode) { 
                                                bluetoothViewModel.spreadVibe(messageText)
                                                if (airIsStill) showAirIsStillDialog = true
                                                messageText = ""
                                                focusManager.clearFocus() 
                                            } 
                                        },
                                        onSearchToggle = { isSearchMode = !isSearchMode; showAirGhost = false; messageText = "" },
                                        onCreatePublicTie = { name -> 
                                            val gid = bluetoothViewModel.startGroupVibe(name, scope = VibeGroup.SCOPE_PUBLIC)
                                            backStack.add(Route.GroupField(gid))
                                            bluetoothViewModel.enterTie(gid)
                                            isSearchMode = false
                                            messageText = ""
                                        },
                                        onAcceptLink = bluetoothViewModel::acceptLink, 
                                        onDenyLink = bluetoothViewModel::denyLink,
                                        onStartSideVibe = { val members = bluetoothState.crowd.selectedDevices; if (members.all { it in bluetoothState.session.connectedLinks }) { val gid = bluetoothViewModel.startGroupVibe("WHISPER", members); backStack.add(Route.GroupField(gid)); bluetoothViewModel.enterTie(gid) } },
                                        onStartTie = { val gid = bluetoothViewModel.startGroupVibe("TIE", bluetoothState.crowd.selectedDevices); backStack.add(Route.GroupField(gid)); bluetoothViewModel.enterTie(gid) },
                                        onClearSelection = bluetoothViewModel::clearSelection,
                                        onAttachFile = { filePickerLauncher.launch("*/*") },
                                        onShowPrivacy = { showPrivacyProtocol = true },
                                        isSearchActive = isSearchMode,
                                        onRestoreAir = bluetoothViewModel::restoreFromVault
                                    ) 
                                }
                                is Route.GroupField -> NavEntry(key) { 
                                    val group = bluetoothState.session.groups.find { it.id == key.groupId }
                                    if (group?.scope == VibeGroup.SCOPE_PUBLIC) {
                                        AirField(
                                            state = bluetoothState, 
                                            localDeviceId = viewModel.deviceId.collectAsStateWithLifecycle(initialValue = "").value, 
                                            localNickname = nickname ?: "?", 
                                            localEmoji = emoji, 
                                            airId = key.groupId, 
                                            onDisconnect = bluetoothViewModel::disconnect, 
                                            onSendMessage = bluetoothViewModel::sendMessage, 
                                            onNavigateToGroup = { gid ->
                                                backStack.add(Route.GroupField(gid))
                                                bluetoothViewModel.enterTie(gid)
                                            },
                                            onNavigateToVibe = { vid ->
                                                backStack.add(Route.VibeField(vid))
                                            },
                                            // Hub Props
                                            messageText = messageText,
                                            onMessageChange = { messageText = it },
                                            onSend = { 
                                                if (messageText.isNotBlank()) { 
                                                    bluetoothViewModel.sendMessage(messageText, key.groupId)
                                                    messageText = ""
                                                    focusManager.clearFocus() 
                                                } 
                                            },
                                            onSearchToggle = { isSearchMode = !isSearchMode; messageText = "" },
                                            onAcceptLink = bluetoothViewModel::acceptLink, 
                                            onDenyLink = bluetoothViewModel::denyLink,
                                            onStartSideVibe = { val members = bluetoothState.crowd.selectedDevices; if (members.all { it in bluetoothState.session.connectedLinks }) { val gid = bluetoothViewModel.startGroupVibe("WHISPER", members); backStack.add(Route.GroupField(gid)); bluetoothViewModel.enterTie(gid) } },
                                            onStartTie = { val gid = bluetoothViewModel.startGroupVibe("TIE", bluetoothState.crowd.selectedDevices); backStack.add(Route.GroupField(gid)); bluetoothViewModel.enterTie(gid) },
                                            onClearSelection = bluetoothViewModel::clearSelection,
                                            onShowPrivacy = { showPrivacyProtocol = true }
                                        )
                                    } else {
                                        TieField(
                                            state = bluetoothState, 
                                            localDeviceId = viewModel.deviceId.collectAsStateWithLifecycle(initialValue = "").value, 
                                            localNickname = nickname ?: "?", 
                                            localEmoji = emoji, 
                                            groupId = key.groupId, 
                                            onDisconnect = bluetoothViewModel::disconnect, 
                                            onSendMessage = bluetoothViewModel::sendMessage, 
                                            onRemoveMember = bluetoothViewModel::removeMemberFromGroup, 
                                            onVaultGroup = bluetoothViewModel::vaultGroup,
                                            onSeniorVaultGroup = bluetoothViewModel::seniorVaultGroup,
                                            onUpdateNote = bluetoothViewModel::updateNote,
                                            onPushRitual = bluetoothViewModel::pushRitual,
                                            showMemberManagement = showManageDialog, 
                                            onShowManagement = { showManageDialog = true }, 
                                            onDismissManagement = { showManageDialog = false }, 
                                            onNavigateToGroup = { gid ->
                                                backStack.add(Route.GroupField(gid))
                                                bluetoothViewModel.enterTie(gid)
                                            },
                                            onNavigateToVibe = { vid ->
                                                backStack.add(Route.VibeField(vid))
                                            },
                                            // Hub Props
                                            messageText = messageText,
                                            onMessageChange = { messageText = it },
                                            onSend = { 
                                                if (messageText.isNotBlank()) { 
                                                    bluetoothViewModel.sendMessage(messageText, key.groupId)
                                                    messageText = ""
                                                    focusManager.clearFocus() 
                                                } 
                                            },
                                            onSearchToggle = { isSearchMode = !isSearchMode; messageText = "" },
                                            onAcceptLink = bluetoothViewModel::acceptLink, 
                                            onDenyLink = bluetoothViewModel::denyLink,
                                            onStartSideVibe = { val members = bluetoothState.crowd.selectedDevices; if (members.all { it in bluetoothState.session.connectedLinks }) { val gid = bluetoothViewModel.startGroupVibe("WHISPER", members); backStack.add(Route.GroupField(gid)); bluetoothViewModel.enterTie(gid) } },
                                            onClearSelection = bluetoothViewModel::clearSelection,
                                            onShowPrivacy = { showPrivacyProtocol = true }
                                        ) 
                                    }
                                }
                                is Route.VibeField -> NavEntry(key) {
                                    VibeField(
                                        state = bluetoothState,
                                        localDeviceId = viewModel.deviceId.collectAsStateWithLifecycle(initialValue = "").value,
                                        localNickname = nickname ?: "?",
                                        localEmoji = emoji,
                                        messageId = key.messageId,
                                        onSendMessage = bluetoothViewModel::sendMessage,
                                        onNavigateToVibe = { vid -> backStack.add(Route.VibeField(vid)) },
                                        // Hub Props
                                        messageText = messageText,
                                        onMessageChange = { messageText = it },
                                        onSend = {
                                            if (messageText.isNotBlank()) {
                                                // Need to handle parentMessageId in sendMessage
                                                // For now simplified
                                                messageText = ""
                                                focusManager.clearFocus()
                                            }
                                        },
                                        onSearchToggle = { isSearchMode = !isSearchMode; messageText = "" },
                                        onAttachFile = { filePickerLauncher.launch("*/*") },
                                        onShowPrivacy = { showPrivacyProtocol = true }
                                    )
                                }
                                else -> NavEntry(key) { Text("Unknown") }
                            }
                        }
                    )
                }

                if (currentRoute != null && currentRoute !is Route.GroupField && currentRoute !is Route.Atmos) {
                    BlukitVibeHub(
                        currentRoute = currentRoute ?: initialRoute,
                        messageText = messageText,
                        onMessageChange = { messageText = it },
                        onSend = { 
                            if (messageText.isNotBlank() && !isSearchMode) { 
                                bluetoothViewModel.spreadVibe(messageText)
                                if (airIsStill) showAirIsStillDialog = true
                                messageText = ""
                                focusManager.clearFocus() 
                            } 
                        },
                        vibeCount = bluetoothState.session.messages.size,
                        airIsStill = airIsStill,
                        isSearchMode = isSearchMode,
                        onSearchToggle = { isSearchMode = !isSearchMode; showAirGhost = false; messageText = "" },
                        onCreatePublicTie = { name -> 
                            val gid = bluetoothViewModel.startGroupVibe(name, scope = VibeGroup.SCOPE_PUBLIC)
                            backStack.add(Route.GroupField(gid))
                            bluetoothViewModel.enterTie(gid)
                            isSearchMode = false
                            messageText = ""
                        },
                        incomingLinkRequests = bluetoothState.crowd.incomingLinkRequests, 
                        selectedDevices = bluetoothState.crowd.selectedDevices,
                        vibedPeers = bluetoothState.crowd.vibedPeers,
                        groups = bluetoothState.session.groups,
                        onAcceptLink = bluetoothViewModel::acceptLink, 
                        onDenyLink = bluetoothViewModel::denyLink,
                        onStartSideVibe = { val members = bluetoothState.crowd.selectedDevices; if (members.all { it in bluetoothState.session.connectedLinks }) { val gid = bluetoothViewModel.startGroupVibe("WHISPER", members); backStack.add(Route.GroupField(gid)); bluetoothViewModel.enterTie(gid) } },
                        onStartTie = { val gid = bluetoothViewModel.startGroupVibe("TIE", bluetoothState.crowd.selectedDevices); backStack.add(Route.GroupField(gid)); bluetoothViewModel.enterTie(gid) },
                        onClearSelection = bluetoothViewModel::clearSelection,
                        onAttachFile = { filePickerLauncher.launch("*/*") },
                        onShowPrivacy = { showPrivacyProtocol = true }
                    )
                }
            }

            if (selectedPersonaForMenu != null) {
                val menuDevice = selectedPersonaForMenu!!
                val menuId = menuDevice.persistentId ?: menuDevice.id
                PersonaOptionsMenu(device = menuDevice, isTied = menuDevice.id in bluetoothState.session.connectedLinks, isBlocked = menuId in bluetoothState.crowd.blockedUsers, isSelected = menuDevice.id in bluetoothState.crowd.selectedDevices, isRequesting = bluetoothState.crowd.incomingLinkRequests.any { it.id == menuDevice.id }, activeGroupId = (currentRoute as? Route.GroupField)?.groupId, isAlreadyInActiveGroup = (bluetoothState.session.groups.find { it.id == (currentRoute as? Route.GroupField)?.groupId }?.memberIds?.contains(menuId) == true), onVibe = { bluetoothViewModel.requestWhisper(menuDevice); selectedPersonaForMenu = null }, onAccept = { bluetoothViewModel.acceptLink(menuDevice); selectedPersonaForMenu = null }, onDeny = { bluetoothViewModel.denyLink(menuDevice); selectedPersonaForMenu = null }, onDisconnect = { bluetoothViewModel.disconnect(); selectedPersonaForMenu = null }, onSelect = { bluetoothViewModel.toggleDeviceSelection(menuDevice.id); selectedPersonaForMenu = null }, onIdentify = { highlightedUserId = menuId; selectedPersonaForMenu = null }, onBlock = { viewModel.blockUser(menuId); selectedPersonaForMenu = null }, onUnblock = { viewModel.unblockUser(menuId); selectedPersonaForMenu = null }, onSync = { bluetoothViewModel.initiateHistorySync(menuDevice.id); selectedPersonaForMenu = null }, onAddToGroup = { gid -> bluetoothViewModel.addMemberToGroup(gid, menuDevice.id); selectedPersonaForMenu = null }, onRemoveFromGroup = { gid -> bluetoothViewModel.removeMemberFromGroup(gid, menuDevice.id); selectedPersonaForMenu = null }, onDismiss = { selectedPersonaForMenu = null })
            }

            if (showAirIsStillDialog) {
                ConfirmationDialog(title = "AIR IS STILL", text = "BLUKIT RADIOS ARE SILENT. AWAKEN BLUETOOTH OR GRANT PERMISSIONS TO SPREAD VIBES.", onConfirm = { showAirIsStillDialog = false; if (!permissionState.essentialPermissionsGranted) permissionState.launchMultiplePermissionRequest() else if (!bluetoothState.harmony.isBluetoothEnabled) context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }) else if (isLocationMandatory && (!bluetoothState.harmony.isLocationEnabled || !locationPermissionGranted)) context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }) }, onDismiss = { showAirIsStillDialog = false })
            }

            if (showPrivacyProtocol) {
                val themeColor = if (currentRoute is Route.Vibes || currentRoute is Route.GroupField) StealthRose else StealthPrimary
                BlukitAlert(
                    title = "PRIVACY PROTOCOL",
                    text = "BLUKIT IS ANONYMOUS-FIRST. 100% OFFLINE P2P. ALL VIBES STAY ON YOUR DEVICE UNTIL YOU CHOOSE TO CLEAR THEM.",
                    confirmLabel = "UNDERSTOOD",
                    onConfirm = { showPrivacyProtocol = false },
                    onDismiss = { showPrivacyProtocol = false }
                )
            }
        }
    }
}

@Composable
private fun FullLighthouseScan(rotation: Float, lowPowerMode: Boolean) { 
    if (lowPowerMode && rotation % 10 > 2) return
    
    val infiniteTransition = rememberInfiniteTransition(label = "ScanGlow")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.05f, 
        targetValue = 0.15f, 
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Reverse),
        label = "Pulse"
    )

    Canvas(modifier = Modifier.fillMaxSize()) { 
        val center = Offset(56.dp.toPx(), size.height - 64.dp.toPx())
        
        // Background Depth Glow
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
                0.0f to StealthPrimary.copy(alpha = if (lowPowerMode) 0.05f else 0.2f), 
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
