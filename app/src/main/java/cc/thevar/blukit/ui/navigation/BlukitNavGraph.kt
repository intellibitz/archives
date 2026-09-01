package cc.thevar.blukit.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import cc.thevar.blukit.domain.model.Message
import cc.thevar.blukit.ui.screens.*
import cc.thevar.blukit.ui.viewmodels.ConnectionViewModel
import cc.thevar.blukit.ui.viewmodels.MainViewModel
import cc.thevar.blukit.ui.viewmodels.NavigationViewModel
import cc.thevar.blukit.ui.viewmodels.HarmonyViewModel
import cc.thevar.blukit.ui.theme.StealthBlack

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun BlukitNavGraph(
    navViewModel: NavigationViewModel,
    connectionViewModel: ConnectionViewModel,
    mainViewModel: MainViewModel,
    harmonyViewModel: HarmonyViewModel,
    breadcrumbTrail: List<String>,
    onCrumbClick: (Int) -> Unit,
    onSourceLongClick: (cc.thevar.blukit.domain.model.Source) -> Unit,
    modifier: Modifier = Modifier
) {
    val connectionState by connectionViewModel.state.collectAsStateWithLifecycle()
    val harmonyReport by harmonyViewModel.report.collectAsStateWithLifecycle()
    val nickname by mainViewModel.nickname.collectAsStateWithLifecycle(initialValue = null)
    val deviceId by mainViewModel.deviceId.collectAsStateWithLifecycle()
    
    var isSearchActive by remember { mutableStateOf(false) }

    val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>()

    NavDisplay(
        backStack = navViewModel.backStack,
        onBack = { navViewModel.popBackStack() },
        modifier = modifier,
        sceneStrategies = listOf(listDetailStrategy),
        entryProvider = entryProvider {
            entry<Route.Onboarding> {
                IdentityInput(
                    onSave = { name, emoji -> 
                        mainViewModel.saveNickname(name)
                        mainViewModel.saveEmoji(emoji)
                        navViewModel.navigate(Route.Nearby, resetStack = true)
                    }
                )
            }

            entry<Route.Nearby>(
                metadata = ListDetailSceneStrategy.listPane(
                    detailPlaceholder = {
                        DetailPlaceholder("Select a person or group to start chatting")
                    }
                )
            ) {
                NearbyField(
                    state = connectionState,
                    localDeviceId = deviceId,
                    header = { },
                    breadcrumbTrail = breadcrumbTrail,
                    onCrumbClick = onCrumbClick,
                    userNickname = nickname ?: "",
                    harmonyReport = harmonyReport,
                    onShowTimeline = { navViewModel.navigate(Route.Timeline) },
                    onResetProfile = { connectionViewModel.resetProfile() },
                    onNavigateToGroup = { gid -> navViewModel.navigate(Route.GroupField(gid)) },
                    onNavigateToMessage = { eid -> navViewModel.navigate(Route.MessageField(eid)) },
                    onSourceLongClick = onSourceLongClick,
                    onAcceptRadio = { connectionViewModel.acceptRadio(it) },
                    onDenyRadio = { connectionViewModel.denyRadio(it) },
                    onRestoreCrowd = { connectionViewModel.restoreFromVault(it) },
                    onNavigateToLiveFeed = { navViewModel.navigate(Route.LiveFeed) },
                    onSearchToggle = { isSearchActive = !isSearchActive },
                    isSearchActive = isSearchActive,
                    onStartWhisper = { 
                        val selectedIds = connectionState.crowd.selectedDevices
                        val source = connectionState.crowd.scannedDevices.find { it.id in selectedIds }
                        if (source != null) connectionViewModel.requestWhisper(source)
                    },
                    onStartSubGroup = { connectionViewModel.startGroupConnection("NEW GROUP") },
                    onClearSelection = { connectionViewModel.clearSelection() },
                    onCreatePublicRoom = { name, tid -> connectionViewModel.startGroupConnection(name, scope = cc.thevar.blukit.domain.model.Group.SCOPE_PUBLIC, templateId = tid) }
                )
            }
            entry<Route.Timeline> {
                TimelineField(
                    messagesFlow = connectionViewModel.timelineMessages,
                    onBack = { navViewModel.popBackStack() }
                )
            }
            entry<Route.LiveFeed> {
                LiveFeedField(
                    messagesFlow = connectionViewModel.allMessagesPaging,
                    sources = connectionState.crowd.scannedDevices,
                    onBack = { navViewModel.popBackStack() },
                    onMessageClick = { navViewModel.navigate(Route.MessageField(it)) }
                )
            }
            entry<Route.GroupField>(
                metadata = ListDetailSceneStrategy.detailPane()
            ) { route ->
                val group = connectionState.session.groups.find { it.id == route.roomId }
                val groupTrend = group?.trendLabel
                
                if (group?.scope == cc.thevar.blukit.domain.model.Group.SCOPE_PRIVATE) {
                    PrivateGroupField(
                        state = connectionState,
                        localDeviceId = deviceId,
                        groupId = route.roomId,
                        pagedMessagesFlow = connectionViewModel.pagedMessages,
                        breadcrumbTrail = breadcrumbTrail,
                        onCrumbClick = onCrumbClick,
                        userNickname = nickname ?: "",
                        activeGroups = connectionState.session.groups,
                        onShowTimeline = { navViewModel.navigate(Route.Timeline) },
                        onResetProfile = { connectionViewModel.resetProfile() },
                        onBack = { navViewModel.popBackStack() },
                        onNavigateToMessage = { navViewModel.navigate(Route.MessageField(it)) },
                        onNavigateToGroup = { gid -> navViewModel.navigate(Route.GroupField(gid)) },
                        onSourceLongClick = onSourceLongClick,
                        onSend = { content -> connectionViewModel.sendMessage(content, route.roomId) },
                        onUpdateRecord = { gid, content, mid, v -> connectionViewModel.updateNote(gid, content, mid, v) },
                        onVaultGroup = { gid, v -> connectionViewModel.vaultGroup(gid, v) },
                        onSeniorVaultGroup = { gid, v -> connectionViewModel.seniorVaultGroup(gid, v) },
                        onRemoveMember = { gid, mid -> connectionViewModel.removeMemberFromGroup(gid, mid) },
                        onAssignRole = { gid, mid, role -> connectionViewModel.assignRole(gid, mid, role) },
                        onPushRitual = { gid, event -> connectionViewModel.pushRitual(gid, event) },
                        showMemberManagement = false,
                        onShowManagement = { /* ... */ },
                        onDismissManagement = { /* ... */ },
                        onStartWhisper = { 
                            val selectedIds = connectionState.crowd.selectedDevices
                            val source = connectionState.crowd.scannedDevices.find { it.id in selectedIds }
                            if (source != null) connectionViewModel.requestWhisper(source) { gid ->
                                navViewModel.navigate(Route.GroupField(gid))
                            }
                        },
                        onStartSubGroup = { connectionViewModel.startGroupConnection("SUB GROUP") },
                        onClearSelection = { connectionViewModel.clearSelection() },
                        trend = groupTrend,
                        isSearchActive = isSearchActive,
                        onSearchToggle = { isSearchActive = !isSearchActive },
                        onAcceptRadio = { connectionViewModel.acceptRadio(it) },
                        onDenyRadio = { connectionViewModel.denyRadio(it) },
                        header = {}
                    )
                } else {
                    val trendingMessages by connectionViewModel.trendingMessages.collectAsStateWithLifecycle()
                    GroupField(
                        state = connectionState,
                        localDeviceId = deviceId,
                        header = { },
                        groupId = route.roomId,
                        pagedMessagesFlow = connectionViewModel.pagedMessages,
                        breadcrumbTrail = breadcrumbTrail,
                        onCrumbClick = onCrumbClick,
                        userNickname = nickname ?: "",
                        onShowTimeline = { navViewModel.navigate(Route.Timeline) },
                        onResetProfile = { connectionViewModel.resetProfile() },
                        highConnectionMessages = trendingMessages,
                        onBack = { navViewModel.popBackStack() },
                        onNavigateToMessage = { navViewModel.navigate(Route.MessageField(it)) },
                        onSourceLongClick = onSourceLongClick,
                        onSend = { content -> connectionViewModel.sendMessage(content, route.roomId) },
                        onStartWhisper = { 
                            val selectedIds = connectionState.crowd.selectedDevices
                            val source = connectionState.crowd.scannedDevices.find { it.id in selectedIds }
                            if (source != null) connectionViewModel.requestWhisper(source) { gid ->
                                navViewModel.navigate(Route.GroupField(gid))
                            }
                        },
                        onStartSubGroup = { connectionViewModel.startGroupConnection("NEW GROUP") },
                        onClearSelection = { connectionViewModel.clearSelection() },
                        trend = groupTrend,
                        isSearchActive = isSearchActive,
                        onSearchToggle = { isSearchActive = !isSearchActive },
                        onAcceptRadio = { connectionViewModel.acceptRadio(it) },
                        onDenyRadio = { connectionViewModel.denyRadio(it) }
                    )
                }
            }
            entry<Route.MessageField>(
                metadata = ListDetailSceneStrategy.detailPane()
            ) { route ->
                val rootMessage by produceState<Message?>(initialValue = null, route.messageId) {
                    value = connectionViewModel.getMessage(route.messageId)
                }

                var messageText by remember { mutableStateOf("") }
                MessageField(
                    state = connectionState,
                    localDeviceId = deviceId,
                    messageId = route.messageId,
                    rootMessage = rootMessage,
                    childMessagesFlow = connectionViewModel.getChildMessages(route.messageId),
                    header = { },
                    breadcrumbTrail = breadcrumbTrail,
                    onCrumbClick = onCrumbClick,
                    userNickname = nickname ?: "",
                    activeGroups = connectionState.session.groups,
                    onShowTimeline = { navViewModel.navigate(Route.Timeline) },
                    onResetProfile = { connectionViewModel.resetProfile() },
                    onBack = { navViewModel.popBackStack() },
                    onNavigateToMessage = { navViewModel.navigate(Route.MessageField(it)) },
                    onNavigateToLiveFeed = { navViewModel.navigate(Route.LiveFeed) },
                    messageText = messageText,
                    onMessageChange = { messageText = it },
                    onSend = {
                        connectionViewModel.sendMessage(messageText, route.messageId)
                        messageText = ""
                    },
                    isSearchActive = isSearchActive,
                    onSearchToggle = { isSearchActive = !isSearchActive }
                )
            }
        }
    )
}

@Composable
private fun DetailPlaceholder(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StealthBlack),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message.uppercase(),
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.2f)
        )
    }
}
