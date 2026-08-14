package cc.thevar.blukit.ui

import android.Manifest
import androidx.compose.foundation.Image
import cc.thevar.blukit.domain.model.P2PDevice
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.Canvas
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import cc.thevar.blukit.R
import cc.thevar.blukit.data.power.SupremePowerManager
import cc.thevar.blukit.data.repository.IdentityRepository
import cc.thevar.blukit.data.system.RadioStateManager
import cc.thevar.blukit.network.p2p.P2PController
import cc.thevar.blukit.ui.navigation.Route
import cc.thevar.blukit.ui.screens.ContactsScreen
import cc.thevar.blukit.ui.screens.RipplesScreen
import cc.thevar.blukit.ui.screens.TieScreen
import cc.thevar.blukit.ui.theme.StealthAmber
import cc.thevar.blukit.ui.theme.StealthPrimary
import cc.thevar.blukit.ui.theme.StealthRose
import cc.thevar.blukit.ui.viewmodels.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalPermissionsApi::class)
@Composable
fun BlukitApp(
    repository: IdentityRepository,
    contactRepository: cc.thevar.blukit.data.repository.ContactRepository,
    messageDao: cc.thevar.blukit.data.local.dao.MessageDao,
    radioStateManager: RadioStateManager,
    p2pController: P2PController,
    supremePowerManager: SupremePowerManager,
    onEnterPip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: MainViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                MainViewModel(repository, messageDao)
            }
        }
    )

    val contactsViewModel: ContactsViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                ContactsViewModel(contactRepository)
            }
        }
    )
    
    val bluetoothViewModel: BluetoothViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                BluetoothViewModel(p2pController, radioStateManager)
            }
        }
    )

    val supremePowerViewModel: SupremePowerViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                SupremePowerViewModel(supremePowerManager)
            }
        }
    )
    
    val nickname by viewModel.nickname.collectAsStateWithLifecycle(initialValue = null)
    val emojiAvatar by viewModel.emojiAvatar.collectAsStateWithLifecycle(initialValue = "👤")
    val isStealthMode by viewModel.isStealthMode.collectAsStateWithLifecycle(initialValue = false)
    val deviceId by viewModel.deviceId.collectAsStateWithLifecycle(initialValue = "")
    val bluetoothState by bluetoothViewModel.state.collectAsStateWithLifecycle()
    val supremeReport by supremePowerViewModel.report.collectAsStateWithLifecycle()
    val energySurge by bluetoothViewModel.energySurge.collectAsStateWithLifecycle()

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
    val permissionState = rememberMultiplePermissionsState(permissions = permissions)
    
    val initialRoute = Route.Shout
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
                Route.Shout -> NavEntry(key, metadata = ListDetailSceneStrategy.listPane()) {
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
                        onBroadcastMessage = bluetoothViewModel::broadcastMessage
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
                Route.Ties -> NavEntry(key) {
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
                        onBroadcastMessage = bluetoothViewModel::broadcastMessage
                    )
                }
                else -> NavEntry(key) { Text("Unknown") }
            }
        }


        val globalSubtitle = when {
            bluetoothState.connectedLinks.isNotEmpty() -> "${bluetoothState.connectedLinks.size} TIED TOGETHER"
            bluetoothState.connectionState is AirConnectionState.Scanning -> "FEELING THE VIBES..."
            bluetoothState.connectionState is AirConnectionState.Connecting -> "BRIDGING THE DISTANCE..."
            else -> "FEEL THE VIBES"
        }

        UnifiedBlukitBadge(
            subtitle = globalSubtitle,
            report = supremeReport,
            isBluetoothEnabled = bluetoothState.isBluetoothEnabled,
            isLocationEnabled = bluetoothState.isLocationEnabled,
            permissionsGranted = permissionState.allPermissionsGranted,
            isStealthMode = isStealthMode,
            currentRoute = (currentRoute as? Route) ?: initialRoute,
            emojiAvatar = emojiAvatar,
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
            onSaveNickname = viewModel::saveNickname,
            onToggleStealth = viewModel::toggleStealth,
            onClearHistory = viewModel::clearChatHistory,
            onLogout = viewModel::logout,
            onAcceptLink = bluetoothViewModel::acceptLink,
            onDenyLink = bluetoothViewModel::denyLink,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        )
    }
}

@Composable
fun UnifiedBlukitBadge(
    subtitle: String,
    report: cc.thevar.blukit.domain.power.SupremePowerReport,
    isBluetoothEnabled: Boolean,
    isLocationEnabled: Boolean,
    permissionsGranted: Boolean,
    isStealthMode: Boolean,
    currentRoute: Route,
    emojiAvatar: String,
    nickname: String,
    incomingLinkRequests: Set<P2PDevice>,
    onNavigate: (Route) -> Unit,
    onAwakenBluetooth: () -> Unit,
    onAwakenLocation: () -> Unit,
    onGrantPermissions: () -> Unit,
    onSaveNickname: (String) -> Unit,
    onToggleStealth: (Boolean) -> Unit,
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
    val hasBreeze = !report.currentBreeze.isNullOrBlank()

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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Left: Sentient Branding & Dynamic Status
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { expanded = !expanded }
                ) {
                    // Animated B Icon (Vibes travelling through)
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(32.dp)) {
                        val infiniteTransition = rememberInfiniteTransition(label = "LogoFlow")
                        val flowOffset by infiniteTransition.animateFloat(
                            initialValue = 0f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing)),
                            label = "Flow"
                        )
                        
                        Canvas(modifier = Modifier.fillMaxSize().blur(8.dp)) {
                            drawCircle(
                                brush = Brush.sweepGradient(
                                    0.0f to StealthPrimary.copy(alpha = 0f),
                                    flowOffset to StealthPrimary.copy(alpha = 0.4f),
                                    1.0f to StealthPrimary.copy(alpha = 0f)
                                ),
                                radius = size.minDimension / 2
                            )
                        }
                        
                        Image(
                            painter = painterResource(id = R.drawable.ic_blukit_logo),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column {
                        // Dynamic Status Bar (Top)
                        Text(
                            text = (if (airIsStill) "THE VIBES ARE STILL" 
                                    else if (report.currentBreeze != null) report.currentBreeze.orEmpty() 
                                    else if (incomingLinkRequests.isNotEmpty()) "INCOMING VIBE"
                                    else subtitle).uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 7.sp,
                                fontWeight = FontWeight.Black,
                                color = (if (airIsStill) StealthAmber 
                                        else if (report.currentBreeze != null || incomingLinkRequests.isNotEmpty()) StealthPrimary 
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
                                text = "FEEL THE VIBES",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 7.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = Color.White.copy(alpha = 0.4f)
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(0.2f))

                // Right: Contextual Animated Ritual Icon
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        // Pulse Animation (Sentient Feel)
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
                            imageVector = if (currentRoute is Route.Ties) Icons.Rounded.Person else Icons.Rounded.Diversity3,
                            contentDescription = null,
                            tint = if (airIsStill) StealthAmber else StealthPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = if (currentRoute is Route.Ties) "TIE" else "VIBES",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 6.sp,
                            fontWeight = FontWeight.Black,
                            color = (if (airIsStill) StealthAmber else StealthPrimary).copy(alpha = 0.6f),
                            letterSpacing = 1.sp
                        )
                    )
                }
            }
            }

            // --- THE MAGIC BAR & INTEL ---
            AnimatedVisibility(
                visible = airIsStill || expanded || hasBreeze,
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
                            currentBreeze = report.currentBreeze,
                            incomingRequests = requests,
                            onAcceptLink = onAcceptLink,
                            onDenyLink = onDenyLink,
                            onAwakenBluetooth = onAwakenBluetooth,
                            onAwakenLocation = onAwakenLocation,
                            onGrantPermissions = onGrantPermissions
                        )
                    }

                    if (expanded) {
                        Spacer(modifier = Modifier.height(24.dp))

                        // Einstein Transformation: Integrated Vibe Identity
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                                .padding(16.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(48.dp)) {
                                Surface(
                                    shape = CircleShape,
                                    color = StealthPrimary.copy(alpha = 0.1f),
                                    border = BorderStroke(1.dp, StealthPrimary.copy(alpha = 0.3f)),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Rounded.Person, 
                                            contentDescription = null,
                                            tint = StealthPrimary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.width(16.dp))

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
                        
                        Column(modifier = Modifier.testTag("IntelSection")) {
                            IntelRow(
                                label = "VOICES", 
                                value = report.userCount.toString(),
                                icon = Icons.Rounded.Groups,
                                modifier = Modifier.testTag("VibesStat"),
                                onClick = { 
                                    onNavigate(Route.Shout)
                                    expanded = false 
                                }
                            )
                            IntelRow(
                                label = "TIES", 
                                value = report.connectedLinksCount.toString(),
                                icon = Icons.Rounded.Person,
                                modifier = Modifier.testTag("TiesStat"),
                                onClick = { 
                                    onNavigate(Route.Ties)
                                    expanded = false
                                }
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        // Einstein Transformation: Quiet Light & Stillness Integrated
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
                                    text = "QUIET LIGHT",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "GENTLE STADIUM GLOW",
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
                                    text = if (showStillnessLocal) "▼ STILLNESS" else "▶ STILLNESS",
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
                                        Text("FORGET TIES", fontSize = 7.sp, fontWeight = FontWeight.Bold)
                                    }
                                    OutlinedButton(
                                        onClick = { showLogoutDialog = true },
                                        modifier = Modifier.weight(1f),
                                        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.2f)),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White.copy(alpha = 0.7f)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("NEW IDENTITY", fontSize = 7.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = StealthPrimary.copy(alpha = 0.1f)
                        )
                        
                        Text(
                            text = report.aiInsight.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(lineHeight = 14.sp),
                            color = Color.White.copy(alpha = 0.8f),
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    }

    // --- DIALOGS MOVED TO HUB (THE BRAIN) ---
    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            containerColor = Color.Black,
            titleContentColor = StealthPrimary,
            textContentColor = Color.White.copy(alpha = 0.7f),
            title = { Text("Forget all Ties?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black) },
            text = { Text("This will permanently remove all your private ties and shared history.", fontSize = 12.sp) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearHistory()
                        showClearHistoryDialog = false
                    }
                ) {
                    Text("REMOVE", color = Color.Red, fontWeight = FontWeight.Bold)
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
            title = { Text("New Identity?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black) },
            text = { Text("This will clear your local profile and start a fresh ritual.", fontSize = 12.sp) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onLogout()
                        showLogoutDialog = false
                    }
                ) {
                    Text("START FRESH", color = Color.Red, fontWeight = FontWeight.Bold)
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
    currentBreeze: String?,
    incomingRequests: Set<P2PDevice>,
    onAcceptLink: (P2PDevice) -> Unit,
    onDenyLink: (P2PDevice) -> Unit,
    onAwakenBluetooth: () -> Unit,
    onAwakenLocation: () -> Unit,
    onGrantPermissions: () -> Unit
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
                        isPermissionMissing -> "AWAKEN PERMISSIONS"
                        isBluetoothOff && isLocationOff -> "AWAKEN RADIOS"
                        isBluetoothOff -> "AWAKEN BLUETOOTH"
                        isLocationOff -> "AWAKEN LOCATION"
                        hasRequests -> "INCOMING VIBE"
                        !currentBreeze.isNullOrBlank() -> "ATMOSPHERIC BREEZE"
                        else -> "THE VIBES ARE PURE"
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
                        isPermissionMissing -> onGrantPermissions
                        isBluetoothOff -> onAwakenBluetooth
                        isLocationOff -> onAwakenLocation
                        else -> null
                    }

                    if (action != null) {
                        Text(
                            text = "AWAKEN",
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
                isPermissionMissing -> "Blukit needs permission to feel the vibes around you."
                isBluetoothOff -> "Your Bluetooth must be awake to feel the vibes around you."
                isLocationOff -> "Location must be awake to feel nearby ripples on this device."
                hasRequests -> "${(incomingRequests.first().name ?: "Vibe").uppercase()} wants to bridge a link."
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
                    
                    if (hasRequests && !isStill) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                "ACCEPT",
                                color = StealthPrimary,
                                fontWeight = FontWeight.Black,
                                fontSize = 8.sp,
                                modifier = Modifier.clickable { onAcceptLink(incomingRequests.first()) }
                            )
                            Text(
                                "DENY",
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
