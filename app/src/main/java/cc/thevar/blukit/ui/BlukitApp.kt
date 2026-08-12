package cc.thevar.blukit.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Face
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.Radar
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import android.util.Log
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import cc.thevar.blukit.R
import cc.thevar.blukit.data.repository.IdentityRepository
import cc.thevar.blukit.data.system.RadioStateManager
import cc.thevar.blukit.network.p2p.P2PController
import cc.thevar.blukit.ui.navigation.Route
import cc.thevar.blukit.ui.screens.ChatScreen
import cc.thevar.blukit.ui.screens.LobbyScreen
import cc.thevar.blukit.ui.screens.DiscoveryScreen
import cc.thevar.blukit.ui.screens.ProfileScreen
import cc.thevar.blukit.ui.screens.ContactsScreen
import cc.thevar.blukit.ui.viewmodels.BluetoothViewModel
import cc.thevar.blukit.ui.viewmodels.MainViewModel
import cc.thevar.blukit.ui.viewmodels.ContactsViewModel

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun BlukitApp(
    repository: IdentityRepository,
    contactRepository: cc.thevar.blukit.data.repository.ContactRepository,
    messageDao: cc.thevar.blukit.data.local.dao.MessageDao,
    radioStateManager: RadioStateManager,
    p2pController: P2PController,
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
    
    val nickname by viewModel.nickname.collectAsStateWithLifecycle(initialValue = null)
    val emojiAvatar by viewModel.emojiAvatar.collectAsStateWithLifecycle(initialValue = "👤")
    val isStealthMode by viewModel.isStealthMode.collectAsStateWithLifecycle(initialValue = false)
    val deviceId by viewModel.deviceId.collectAsStateWithLifecycle(initialValue = "")
    val isUserRegistered by viewModel.isUserRegistered.collectAsStateWithLifecycle()
    val bluetoothState by bluetoothViewModel.state.collectAsStateWithLifecycle()
    
    val initialRoute = remember(isUserRegistered) {
        if (isUserRegistered) Route.Lobby else Route.Profile
    }
    
    val backStack = rememberNavBackStack(initialRoute)
    val currentRoute = backStack.lastOrNull()
    
    // Start P2P scanning once when user registers (keeps running persistently)
    var scanStarted by remember { mutableStateOf(false) }
    
    LaunchedEffect(isUserRegistered) {
        if (isUserRegistered && !scanStarted) {
            Log.d("BlukitApp", "Starting P2P scan on first registration")
            bluetoothViewModel.startScan()
            scanStarted = true
        }
    }

    // Handle auto-navigation between Lobby and Chat based on connection state
    LaunchedEffect(bluetoothState.isConnected) {
        if (bluetoothState.isConnected && currentRoute !is Route.Chat) {
            backStack.add(Route.Chat)
        } else if (!bluetoothState.isConnected && currentRoute is Route.Chat) {
            backStack.removeLastOrNull()
        }
    }

    val listDetailSceneStrategy = rememberListDetailSceneStrategy<NavKey>()

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            if (isUserRegistered) {
                item(
                    selected = currentRoute is Route.Lobby || currentRoute is Route.Chat,
                    onClick = { 
                        if (currentRoute !is Route.Lobby) {
                            backStack.add(Route.Lobby)
                        }
                    },
                    icon = { Icon(Icons.Rounded.Forum, contentDescription = stringResource(R.string.chat_stadium_lobby)) },
                    label = { Text(stringResource(R.string.chat_stadium_lobby)) }
                )
                item(
                    selected = currentRoute is Route.Discovery,
                    onClick = { 
                        if (currentRoute !is Route.Discovery) {
                            backStack.add(Route.Discovery)
                        }
                    },
                    icon = { Icon(Icons.Rounded.Radar, contentDescription = "Radar") },
                    label = { Text("Radar") }
                )
                item(
                    selected = currentRoute is Route.Contacts,
                    onClick = { 
                        if (currentRoute !is Route.Contacts) {
                            backStack.add(Route.Contacts)
                        }
                    },
                    icon = { Icon(Icons.Rounded.Face, contentDescription = stringResource(R.string.contacts_title)) },
                    label = { Text(stringResource(R.string.contacts_title)) }
                )
                item(
                    selected = currentRoute is Route.Profile,
                    onClick = { 
                        if (currentRoute !is Route.Profile) {
                            backStack.add(Route.Profile)
                        }
                    },
                    icon = { Icon(Icons.Rounded.Person, contentDescription = stringResource(R.string.profile_title)) },
                    label = { Text(stringResource(R.string.profile_title)) }
                )
            }
        },
        modifier = modifier
    ) {
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            sceneStrategy = listDetailSceneStrategy,
            modifier = Modifier.fillMaxSize()
        ) { key ->
            when (key) {
                Route.Profile -> NavEntry(key) {
                    ProfileScreen(
                        onSaveNickname = viewModel::saveNickname,
                        onSaveEmoji = { viewModel.saveEmoji(it) },
                        onToggleStealth = { viewModel.toggleStealth(it) },
                        onNavigateNext = { backStack.add(Route.Lobby) },
                        currentNickname = nickname,
                        currentEmoji = emojiAvatar,
                        isStealthMode = isStealthMode,
                        onClearHistory = viewModel::clearChatHistory,
                        onLogout = viewModel::logout
                    )
                }
                Route.Discovery -> NavEntry(
                    key = key,
                    metadata = ListDetailSceneStrategy.listPane(
                        detailPlaceholder = {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    stringResource(R.string.discovery_select_placeholder),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    )
                ) {
                    DiscoveryScreen(
                        state = bluetoothState,
                        onStartScan = bluetoothViewModel::startScan,
                        onStopScan = bluetoothViewModel::stopScan,
                        onDeviceClick = bluetoothViewModel::connectToDevice,
                        onStartServer = bluetoothViewModel::startAdvertising,
                        onNavigateToLobby = { backStack.add(Route.Lobby) }
                    )
                }
                Route.Chat -> NavEntry(
                    key = key,
                    metadata = ListDetailSceneStrategy.detailPane()
                ) {
                    ChatScreen(
                        state = bluetoothState,
                        localDeviceId = deviceId,
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
                Route.Lobby -> NavEntry(
                    key = key,
                    metadata = ListDetailSceneStrategy.detailPane()
                ) {
                    LobbyScreen(
                        state = bluetoothState,
                        localDeviceId = deviceId,
                        onAutoConnectPeer = { peerId ->
                            // Auto-connect to the discovered peer (Power 5 - whisper)
                            if (!bluetoothState.isConnected || !bluetoothState.connectedPeers.contains(peerId)) {
                                val peer = bluetoothState.scannedDevices.firstOrNull { it.id == peerId }
                                peer?.let { bluetoothViewModel.connectToDevice(it) }
                            }
                        },
                        onBroadcastMessage = bluetoothViewModel::broadcastMessage,
                        onBlockUser = viewModel::blockUser,
                        onEnterPip = onEnterPip
                    )
                }
                Route.Contacts -> NavEntry(key) {
                    val contacts by contactsViewModel.allContacts.collectAsStateWithLifecycle()
                    ContactsScreen(
                        contacts = contacts,
                        onNavigateBack = { backStack.removeLastOrNull() },
                        onStartChat = { contact ->
                            bluetoothState.scannedDevices.find { it.id == contact.contactId }
                                ?.let { bluetoothViewModel.connectToDevice(it) }
                        }
                    )
                }
                else -> NavEntry(key) {
                    Text("Unknown route")
                }
            }
        }
    }
}
