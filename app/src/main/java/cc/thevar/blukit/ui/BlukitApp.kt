package cc.thevar.blukit.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
import cc.thevar.blukit.ui.screens.DiscoveryScreen
import cc.thevar.blukit.ui.screens.RadarScreen
import cc.thevar.blukit.ui.screens.ProfileScreen
import cc.thevar.blukit.ui.viewmodels.BluetoothViewModel
import cc.thevar.blukit.ui.viewmodels.MainViewModel

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun BlukitApp(
    repository: IdentityRepository,
    radioStateManager: RadioStateManager,
    p2pController: P2PController,
    onEnterPip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: MainViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                MainViewModel(repository)
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
        if (isUserRegistered) Route.Discovery else Route.Profile
    }
    
    val backStack = rememberNavBackStack(initialRoute)
    val currentRoute = backStack.lastOrNull()
    
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
                    selected = currentRoute is Route.Discovery || currentRoute is Route.Chat,
                    onClick = { 
                        if (currentRoute !is Route.Discovery) {
                            backStack.add(Route.Discovery)
                        }
                    },
                    icon = { Icon(Icons.Rounded.Search, contentDescription = stringResource(R.string.discovery_title)) },
                    label = { Text(stringResource(R.string.discovery_title)) }
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
                        currentNickname = nickname,
                        currentEmoji = emojiAvatar,
                        isStealthMode = isStealthMode,
                        onSaveNickname = viewModel::saveNickname,
                        onSaveEmoji = { viewModel.saveEmoji(it) },
                        onToggleStealth = { viewModel.toggleStealth(it) },
                        onNavigateNext = { backStack.add(Route.Discovery) }
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
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    )
                ) {
                    RadarScreen(
                        state = bluetoothState,
                        onDeviceClick = bluetoothViewModel::connectToDevice
                    )
                }
                Route.Chat -> NavEntry(
                    key = key,
                    metadata = ListDetailSceneStrategy.detailPane()
                ) {
                    ChatScreen(
                        state = bluetoothState,
                        localDeviceId = deviceId,
                        onDisconnect = bluetoothViewModel::disconnect,
                        onSendMessage = bluetoothViewModel::sendMessage,
                        onBlockUser = viewModel::blockUser,
                        onEnterPip = onEnterPip
                    )
                }
                else -> NavEntry(key) {
                    Text("Unknown route")
                }
            }
        }
    }
}
