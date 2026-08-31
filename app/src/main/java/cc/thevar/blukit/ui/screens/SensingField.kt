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
import cc.thevar.blukit.domain.model.Echo
import cc.thevar.blukit.domain.model.Source
import cc.thevar.blukit.domain.model.Sphere
import cc.thevar.blukit.ui.theme.*
import cc.thevar.blukit.ui.components.MeshSearchingView
import cc.thevar.blukit.ui.viewmodels.BluetoothUiState
import cc.thevar.blukit.ui.navigation.Route

/**
 * THE SENSING FIELD: The master feed for finding Spheres and Sources.
 */
@Composable
fun SensingField(
    state: BluetoothUiState,
    localDeviceId: String,
    header: @Composable () -> Unit,
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
    onAcceptRadio: (Source) -> Unit = {},
    onDenyRadio: (Source) -> Unit = {},
    onRestoreCrowd: (String) -> Unit = {},
    onNavigateToLiveFeed: () -> Unit = {},
    showAirGhost: Boolean = false,
    onShowAirGhost: () -> Unit = {},
    onDismissAirGhost: () -> Unit = {},
) {
    val activeSpheres = state.session.groups.filter { it.scope == Sphere.SCOPE_PUBLIC }
    
    val echoesData = remember(state.session.messages, activeSpheres) {
        val groupedBySphere = state.session.messages.groupBy { it.groupId ?: Sphere.ID_GLOBAL }
        val counts = groupedBySphere.mapValues { it.value.size }
        val filtered = groupedBySphere.map { it.value.maxBy { msg -> msg.timestamp } }
        val sorted = filtered.sortedByDescending { it.timestamp }
        Triple(sorted, counts, false)
    }

    val (_, echoCounts, _) = echoesData

    val combinedResonance = remember(activeSpheres, state.session.messages) {
        val list = mutableListOf<Pair<Source, Echo?>>()
        val pinned = activeSpheres.filter { it.isPinned }.sortedByDescending { it.lastMessageTimestamp }
        val others = activeSpheres.filter { !it.isPinned }.sortedBy { it.lastMessageTimestamp }
        val sortedSpheres = others + pinned 
        
        sortedSpheres.forEach { sphere ->
            val latestSynthesis = state.session.messages.findLast { 
                (it.groupId == sphere.id) && (it.type == Echo.TYPE_AI_SUMMARY)
            }
            val sphereMessages = state.session.messages.asSequence().filter { 
                (it.groupId == sphere.id || (sphere.id == Sphere.ID_GLOBAL && it.groupId == null)) && (it.senderId != localDeviceId)
            }.sortedBy { it.timestamp }.toList().takeLast(3)
            
            sphereMessages.forEach { echo ->
                val source = Source(id = echo.senderId, name = echo.senderName, emoji = echo.senderEmoji ?: "👤", medium = Source.ResonanceMedium.BLUETOOTH)
                list.add(source to echo)
            }
            val headSource = Source(id = sphere.id, name = if (sphere.id == Sphere.ID_GLOBAL) "Global Resonance" else sphere.name, emoji = sphere.projectionEmoji ?: "✨", medium = Source.ResonanceMedium.BLUETOOTH, statusLabel = latestSynthesis?.content)
            list.add(headSource to null)
        }
        list
    }

    var sphereNameProposal by remember { mutableStateOf("") }
    var showVault by remember { mutableStateOf(false) }
    var messageText by remember { mutableStateOf("") }

    val globalSynthesis = remember(state.session.messages) {
        state.session.messages.findLast { 
            (it.groupId == Sphere.ID_GLOBAL || it.groupId == null) && (it.type == Echo.TYPE_AI_SUMMARY) 
        }
    }

    BlukitFieldScaffold(
        header = header,
        entries = {
            Column(modifier = Modifier.fillMaxSize()) {
                IdentityStage(
                    title = "SENSING",
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
                                Text(text = if (isSearchActive) "SEARCH" else "SOURCES", style = MaterialTheme.typography.labelSmall, color = (if (isSearchActive) StealthAmber else StealthPrimary).copy(alpha = StealthAlphaHigh))
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                )

                if (state.crowd.scannedDevices.isNotEmpty()) {
                    globalSynthesis?.let { synthesis ->
                        BlukitWidget(header = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = StealthAmber, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = "Sphere Synthesis", style = MaterialTheme.typography.labelSmall, color = StealthAmber)
                                }
                            },
                            entries = { Text(text = synthesis.content, color = Color.White, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(12.dp)) },
                            themeColor = StealthAmber, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    ResonanceTicker(
                        state = state,
                        resonanceList = combinedResonance,
                        echoCounts = echoCounts,
                        localDeviceId = localDeviceId,
                        localNickname = userNickname,
                        pulsedPeers = pulsedPeers,
                        onEchoClick = { id -> if (state.session.groups.any { it.id == id }) onNavigateToGroup(id) else onNavigateToPulse(id) },
                        onSourceClick = { onNavigateToPulse(it.id) },
                        onSourceLongClick = { },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    ResonanceSensingView(onSignalPresence = { onShowAirGhost() })
                }
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
                onStartSidePulse = { }, 
                onStartChain = { }, 
                onClearSelection = { },
                onAttachFile = { },
                isSearchMode = isSearchActive,
                onSearchToggle = onSearchToggle,
                onCreatePublicRoom = onCreatePublicRoom,
                onFocusChange = { },
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
            )
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
