package cc.thevar.blukit.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
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
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
import cc.thevar.blukit.ui.screens.*
import cc.thevar.blukit.ui.theme.StealthPrimary
import cc.thevar.blukit.ui.viewmodels.*

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
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
    val emojiAvatar by viewModel.emojiAvatar.collectAsStateWithLifecycle(initialValue = "🎭")
    val isStealthMode by viewModel.isStealthMode.collectAsStateWithLifecycle(initialValue = false)
    val deviceId by viewModel.deviceId.collectAsStateWithLifecycle(initialValue = "")
    val bluetoothState by bluetoothViewModel.state.collectAsStateWithLifecycle()
    val supremeReport by supremePowerViewModel.report.collectAsStateWithLifecycle()
    
    val initialRoute = Route.Shout
    val backStack = rememberNavBackStack(initialRoute)
    val currentRoute = backStack.lastOrNull()

    val listDetailSceneStrategy = rememberListDetailSceneStrategy<NavKey>()

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            item(
                selected = currentRoute is Route.Shout,
                onClick = { if (currentRoute !is Route.Shout) backStack.add(Route.Shout) },
                icon = { 
                    VibesTabIcon(
                        icon = "🌬️", 
                        label = stringResource(R.string.nav_shout), 
                        selected = currentRoute is Route.Shout 
                    ) 
                },
                label = { Text(stringResource(R.string.nav_shout)) }
            )
            item(
                selected = currentRoute is Route.Ties,
                onClick = { if (currentRoute !is Route.Ties) backStack.add(Route.Ties) },
                icon = { 
                    VibesTabIcon(
                        icon = Icons.Rounded.Diversity1, 
                        label = stringResource(R.string.nav_whispers), 
                        selected = currentRoute is Route.Ties 
                    ) 
                },
                label = { Text(stringResource(R.string.nav_whispers)) }
            )
            item(
                selected = currentRoute is Route.Mask,
                onClick = { if (currentRoute !is Route.Mask) backStack.add(Route.Mask) },
                icon = { 
                    VibesTabIcon(
                        icon = emojiAvatar, 
                        label = stringResource(R.string.nav_mask), 
                        selected = currentRoute is Route.Mask 
                    ) 
                },
                label = { Text(stringResource(R.string.nav_mask)) }
            )
        },
        modifier = modifier
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            NavDisplay(
                backStack = backStack,
                onBack = { backStack.removeLastOrNull() },
                sceneStrategy = listDetailSceneStrategy,
                modifier = Modifier.fillMaxSize()
            ) { key ->
                when (key) {
                    Route.Mask -> NavEntry(key) {
                        ProfileScreen(
                            onSaveNickname = viewModel::saveNickname,
                            onSaveEmoji = { viewModel.saveEmoji(it) },
                            onToggleStealth = { viewModel.toggleStealth(it) },
                            currentNickname = nickname,
                            currentEmoji = emojiAvatar,
                            isStealthMode = isStealthMode,
                            onClearHistory = viewModel::clearChatHistory,
                            onLogout = viewModel::logout
                        )
                    }
                    Route.Shout -> NavEntry(key, metadata = ListDetailSceneStrategy.listPane()) {
                        RipplesScreen(
                            state = bluetoothState,
                            localDeviceId = deviceId,
                            localEmoji = emojiAvatar,
                            onStartScan = bluetoothViewModel::startScan,
                            onStopScan = bluetoothViewModel::stopScan,
                            onDeviceClick = { device ->
                                bluetoothViewModel.connectToDevice(device)
                                backStack.add(Route.Chat)
                            },
                            onStartServer = bluetoothViewModel::startAdvertising,
                            onBroadcastMessage = bluetoothViewModel::broadcastMessage
                        )
                    }
                    Route.Chat -> NavEntry(key, metadata = ListDetailSceneStrategy.detailPane()) {
                        TieScreen(
                            state = bluetoothState,
                            localDeviceId = deviceId,
                            localEmoji = emojiAvatar,
                            peerId = bluetoothState.connectedPeer?.id,
                            peerName = bluetoothState.connectedPeer?.name,
                            peerEmoji = bluetoothState.connectedPeer?.emoji,
                            onDisconnect = bluetoothViewModel::disconnect,
                            onNavigateBack = { backStack.removeLastOrNull() },
                            onSendMessage = bluetoothViewModel::sendMessage,
                            onBlockUser = viewModel::blockUser,
                            onEnterPip = onEnterPip
                        )
                    }
                    Route.Ties -> NavEntry(key) {
                        val contacts by contactsViewModel.allContacts.collectAsStateWithLifecycle()
                        ContactsScreen(
                            contacts = contacts,
                            onStartChat = { contact ->
                                bluetoothState.scannedDevices.find { it.id == contact.contactId }
                                    ?.let { 
                                        bluetoothViewModel.connectToDevice(it)
                                        backStack.add(Route.Chat)
                                    }
                            }
                        )
                    }
                    else -> NavEntry(key) { Text("Unknown") }
                }
            }

            val currentTitle = when (currentRoute) {
                Route.Shout -> "THE AIR"
                Route.Ties -> "YOUR TIES"
                Route.Mask -> "YOUR VIBE"
                Route.Chat -> (bluetoothState.connectedPeer?.name?.uppercase() ?: "TIE")
                else -> ""
            }

            val globalSubtitle = when {
                bluetoothState.connectedPeers.isNotEmpty() -> "${bluetoothState.connectedPeers.size} VIBING TOGETHER"
                bluetoothState.connectionState is MeshConnectionState.Scanning -> "FEELING THE AIR..."
                bluetoothState.connectionState is MeshConnectionState.Connecting -> "BRIDGING THE DISTANCE..."
                else -> "FEEL THE VIBES"
            }

            UnifiedBlukitBadge(
                title = currentTitle,
                subtitle = globalSubtitle,
                report = supremeReport,
                isDiscovering = bluetoothState.isDiscovering,
                isBluetoothEnabled = bluetoothState.isBluetoothEnabled
            )
        }
    }
}

@Composable
fun UnifiedBlukitBadge(
    title: String,
    subtitle: String,
    report: cc.thevar.blukit.domain.power.SupremePowerReport,
    isDiscovering: Boolean,
    isBluetoothEnabled: Boolean
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(top = 8.dp, start = 16.dp),
        contentAlignment = Alignment.TopStart
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 320.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Black.copy(alpha = 0.85f))
                .border(0.5.dp, StealthPrimary.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                .clickable { expanded = !expanded }
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "BLUKIT",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp,
                                color = StealthPrimary
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(modifier = Modifier.size(1.dp, 12.dp).background(Color.White.copy(alpha = 0.2f)))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp,
                                color = Color.White
                            )
                        )
                    }
                    Text(
                        text = subtitle.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.5f),
                            letterSpacing = 0.5.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                cc.thevar.blukit.ui.screens.StatusOverlay(
                    isDiscovering = isDiscovering,
                    isBluetoothEnabled = isBluetoothEnabled,
                    modifier = Modifier.graphicsLayer { alpha = 0.8f }
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Icon(
                    Icons.Rounded.Bolt,
                    contentDescription = null,
                    tint = StealthPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                IntelRow("HEARTS", report.userCount.toString())
                IntelRow("TIES", report.connectedPeerCount.toString())
                IntelRow("ENERGY", report.trafficDensity)
                IntelRow("FLOW", report.signalStability)
                
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = StealthPrimary.copy(alpha = 0.1f)
                )
                
                Text(
                    text = report.aiInsight.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.8f),
                    lineHeight = 14.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
private fun IntelRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.4f))
        Text(text = value, style = MaterialTheme.typography.labelSmall, color = StealthPrimary, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun VibesTabIcon(
    icon: Any, // Can be ImageVector or String (Emoji)
    label: String,
    selected: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "TabVibes")
    val vibePulse by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "VibePulse"
    )

    val glowAlpha by animateFloatAsState(
        targetValue = if (selected) 0.6f else 0f,
        animationSpec = tween(500),
        label = "Glow"
    )

    Box(contentAlignment = Alignment.Center) {
        if (selected) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(StealthPrimary.copy(alpha = glowAlpha * 0.2f), CircleShape)
                    .border(0.5.dp, StealthPrimary.copy(alpha = glowAlpha), CircleShape)
                    .graphicsLayer {
                        scaleX = vibePulse
                        scaleY = vibePulse
                    }
            )
        }
        
        when (icon) {
            is androidx.compose.ui.graphics.vector.ImageVector -> {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (selected) StealthPrimary else Color.White.copy(alpha = 0.4f),
                    modifier = Modifier.size(24.dp)
                )
            }
            is String -> {
                Text(
                    text = icon,
                    fontSize = 20.sp,
                    modifier = Modifier.graphicsLayer {
                        alpha = if (selected) 1f else 0.4f
                    }
                )
            }
        }
    }
}
