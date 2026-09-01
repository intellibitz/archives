/**
 * BLUKIT NEARBY FIELD
 *
 * The root entry point of the connection field (Landing).
 * Provides a view of all nearby Groups and Sources.
 */
package cc.thevar.blukit.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cc.thevar.blukit.domain.model.Message
import cc.thevar.blukit.domain.model.Source
import cc.thevar.blukit.domain.model.Group
import cc.thevar.blukit.ui.theme.*
import cc.thevar.blukit.ui.components.ConnectionNearbyView
import cc.thevar.blukit.ui.viewmodels.ConnectionUiState
import cc.thevar.blukit.ui.viewmodels.ConnectionViewModel
import cc.thevar.blukit.ui.navigation.Route
import cc.thevar.blukit.ui.components.*
import cc.thevar.blukit.ui.components.*
import org.koin.androidx.compose.koinViewModel

import androidx.compose.ui.res.stringResource
import cc.thevar.blukit.R

/**
 * THE NEARBY FIELD: The master feed for finding Groups and Sources.
 */
@Composable
fun NearbyField(
    state: ConnectionUiState,
    localDeviceId: String,
    header: @Composable () -> Unit,
    viewModel: ConnectionViewModel = koinViewModel(),
    pulsedPeers: Set<String> = emptySet(),
    onIdentifyUser: (String) -> Unit = {},
    breadcrumbTrail: List<String> = emptyList(),
    onCrumbClick: (Int) -> Unit = {},
    userNickname: String = "",
    onShowTimeline: () -> Unit = {},
    onResetProfile: () -> Unit = {},
    onTitleClick: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null,
    harmonyReport: cc.thevar.blukit.domain.power.HarmonyReport? = null,
    onSearchToggle: (() -> Unit)? = null,
    isSearchActive: Boolean = false,
    onCreatePublicRoom: ((String, String?) -> Unit)? = null,
    onNavigateToGroup: (String) -> Unit = {},
    onNavigateToMessage: (String) -> Unit = {},
    onSourceLongClick: (Source) -> Unit = {},
    onAcceptRadio: (Source) -> Unit = {},
    onDenyRadio: (Source) -> Unit = {},
    onRestoreCrowd: (String) -> Unit = {},
    onNavigateToLiveFeed: () -> Unit = {},
    onStartWhisper: () -> Unit = {},
    onStartSubGroup: () -> Unit = {},
    onClearSelection: () -> Unit = {},
    showGroupSetup: Boolean = false,
    onShowGroupSetup: () -> Unit = {},
    onDismissGroupSetup: () -> Unit = {},
) {
    val connectionList by viewModel.connectionList.collectAsStateWithLifecycle()

    var groupNameProposal by remember { mutableStateOf("") }
    var showVault by remember { mutableStateOf(false) }
    var messageText by remember { mutableStateOf("") }


    BlukitFieldScaffold(
        header = header,
        entries = {
            Column(modifier = Modifier.fillMaxSize()) {
                if (state.crowd.scannedDevices.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            TickerSectionHeader(title = stringResource(R.string.nearby_groups_header).uppercase(), color = StealthPrimary)
                        }

                        
                        val publicGroups = state.session.groups.filter { it.scope == Group.SCOPE_PUBLIC }
                        items(publicGroups) { group ->
                            GroupSummary(
                                title = group.name,
                                subtitle = "Public Group",
                                icon = Icons.Rounded.Public,
                                themeColor = StealthPrimary,
                                count = group.memberIds.size,
                                lastUpdate = "Active",
                                onClick = { onNavigateToGroup(group.id) }
                            )
                        }

                        item {
                            TickerSectionHeader(title = "PEOPLE NEARBY", color = StealthRose)
                        }

                        items(state.crowd.scannedDevices) { source ->
                            GroupSummary(
                                title = source.name ?: "Unknown",
                                subtitle = "Available to connect",
                                icon = Icons.Rounded.Person,
                                themeColor = StealthRose,
                                count = -1,
                                lastUpdate = "Nearby",
                                onClick = { onNavigateToMessage(source.id) }
                            )
                        }
                    }
                } else {
                    ConnectionNearbyView(onSignalPresence = { onShowGroupSetup() }, modifier = Modifier.weight(1f))
                }

                MessageHub(
                    currentRoute = Route.Nearby,
                    messageText = messageText,
                    onMessageChange = { messageText = it },
                    onSend = { }, 
                    messageCount = 0,
                    incomingRadioRequests = state.crowd.incomingRadioRequests,
                    selectedDevices = state.crowd.selectedDevices,
                    onAcceptRadio = onAcceptRadio,
                    onDenyRadio = onDenyRadio,
                    onStartWhisper = onStartWhisper,
                    onStartSubGroup = onStartSubGroup, 
                    onClearSelection = onClearSelection,
                    onAttachFile = { },
                    isSearchMode = isSearchActive,
                    onSearchToggle = onSearchToggle,
                    onCreatePublicRoom = onCreatePublicRoom,
                    onFocusChange = { },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )

    if (showGroupSetup) {
        GroupSetup(
            onNameChange = { groupNameProposal = it },
            onDone = { templateId ->
                onCreatePublicRoom?.invoke(groupNameProposal, templateId)
                onDismissGroupSetup()
            },
            onDismiss = onDismissGroupSetup,
            nearbyGroups = state.session.groups.filter { it.scope == Group.SCOPE_PUBLIC && it.id != Group.ID_GLOBAL },
            onJoinGroup = { gid -> onNavigateToGroup(gid); onDismissGroupSetup() }
        )
    }

    if (showVault) {
        SunkRecordVault(archivedGroups = state.session.archivedGroups, onRestore = onRestoreCrowd, onDismiss = { showVault = false })
    }
}
