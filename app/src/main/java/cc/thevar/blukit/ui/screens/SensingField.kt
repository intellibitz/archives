/**
 * BLUKIT SENSING FIELD
 *
 * The root entry point of the resonance field (Landing).
 * Provides a view of all nearby Spheres and Sources.
 */
package cc.thevar.blukit.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cc.thevar.blukit.domain.model.Echo
import cc.thevar.blukit.domain.model.Source
import cc.thevar.blukit.domain.model.Sphere
import cc.thevar.blukit.ui.theme.*
import cc.thevar.blukit.ui.components.ResonanceSensingView
import cc.thevar.blukit.ui.viewmodels.BluetoothUiState
import cc.thevar.blukit.ui.viewmodels.BluetoothViewModel
import cc.thevar.blukit.ui.navigation.Route
import org.koin.androidx.compose.koinViewModel

/**
 * THE SENSING FIELD: The master feed for finding Spheres and Sources.
 */
@Composable
fun SensingField(
    state: BluetoothUiState,
    localDeviceId: String,
    header: @Composable () -> Unit,
    viewModel: BluetoothViewModel = koinViewModel(),
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
    onNavigateToPulse: (String) -> Unit = {},
    onSourceLongClick: (Source) -> Unit = {},
    onAcceptRadio: (Source) -> Unit = {},
    onDenyRadio: (Source) -> Unit = {},
    onRestoreCrowd: (String) -> Unit = {},
    onNavigateToLiveFeed: () -> Unit = {},
    onStartSidePulse: () -> Unit = {},
    onStartChain: () -> Unit = {},
    onClearSelection: () -> Unit = {},
    showAirGhost: Boolean = false,
    onShowAirGhost: () -> Unit = {},
    onDismissAirGhost: () -> Unit = {},
) {
    val resonanceList by viewModel.resonanceList.collectAsStateWithLifecycle()
    
    val echoCounts = remember(state.session.messages) {
        state.session.messages.groupBy { it.groupId ?: Sphere.ID_GLOBAL }.mapValues { it.value.size }
    }

    var sphereNameProposal by remember { mutableStateOf("") }
    var showVault by remember { mutableStateOf(false) }
    var messageText by remember { mutableStateOf("") }


    BlukitFieldScaffold(
        header = header,
        entries = {
            Column(modifier = Modifier.fillMaxSize()) {
                IdentityStage(
                    title = "NEARBY",
                    breadcrumbTrail = breadcrumbTrail,
                    onCrumbClick = onCrumbClick,
                    activeRooms = state.session.groups,
                    onShowTimeline = onShowTimeline,
                    onResetProfile = onResetProfile,
                    onBack = onBack,
                    themeColor = StealthPrimary,
                    userCount = state.crowd.scannedDevices.size,
                    isDiscovery = true,
                    onModeChange = { onNavigateToLiveFeed() },
                    trailingContent = {
                        if (onSearchToggle != null) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                IconButton(onClick = onSearchToggle, modifier = Modifier.size(28.dp)) {
                                    Icon(imageVector = if (isSearchActive) Icons.Rounded.Search else Icons.Rounded.People, contentDescription = "Toggle Search", tint = if (isSearchActive) StealthAmber else StealthPrimary, modifier = Modifier.size(20.dp))
                                }
                                Text(text = if (isSearchActive) "SEARCH" else "PEOPLE", style = MaterialTheme.typography.labelSmall, color = (if (isSearchActive) StealthAmber else StealthPrimary).copy(alpha = StealthAlphaHigh))
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                )

                if (state.crowd.scannedDevices.isNotEmpty()) {
                    ResonanceTicker(
                        state = state,
                        resonanceList = resonanceList.map { it.source to it.latestEcho },
                        echoCounts = echoCounts,
                        localDeviceId = localDeviceId,
                        localNickname = userNickname,
                        pulsedPeers = pulsedPeers,
                        onEchoClick = { id -> if (state.session.groups.any { it.id == id }) onNavigateToGroup(id) else onNavigateToPulse(id) },
                        onSourceClick = { onNavigateToPulse(it.id) },
                        onSourceLongClick = onSourceLongClick,
                        modifier = Modifier.weight(1f),
                        reverseLayout = false,
                        trend = harmonyReport?.trendLabel
                    )
                } else {
                    ResonanceSensingView(onSignalPresence = { onShowAirGhost() }, modifier = Modifier.weight(1f))
                }

                EchoHub(
                    currentRoute = Route.Sensing,
                    messageText = messageText,
                    onMessageChange = { messageText = it },
                    onSend = { }, 
                    messageCount = state.session.messages.size,
                    incomingRadioRequests = state.crowd.incomingRadioRequests,
                    selectedDevices = state.crowd.selectedDevices,
                    onAcceptRadio = onAcceptRadio,
                    onDenyRadio = onDenyRadio,
                    onStartSidePulse = onStartSidePulse,
                    onStartChain = onStartChain, 
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

    if (showAirGhost) {
        SphereRitualGhost(
            onNameChange = { sphereNameProposal = it },
            onDone = { templateId ->
                onCreatePublicRoom?.invoke(sphereNameProposal, templateId)
                onDismissAirGhost()
            },
            onDismiss = onDismissAirGhost,
            nearbyAirs = state.session.groups.filter { it.scope == Sphere.SCOPE_PUBLIC && it.id != Sphere.ID_GLOBAL },
            onJoinAir = { gid -> onNavigateToGroup(gid); onDismissAirGhost() }
        )
    }

    if (showVault) {
        SunkRecordVault(archivedSpheres = state.session.archivedGroups, onRestore = onRestoreCrowd, onDismiss = { showVault = false })
    }
}
