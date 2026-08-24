package cc.thevar.blukit.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.thevar.blukit.domain.model.MessagePayload
import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.domain.model.VibeGroup
import cc.thevar.blukit.ui.viewmodels.BluetoothUiState
import cc.thevar.blukit.ui.theme.StealthPrimary
import cc.thevar.blukit.ui.theme.StealthRose
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * CROWD FIELD: Public frequency view. 
 * Lists Ties and Shouts within a specific Crowd container.
 * Features collaborative Crowd Canvas for pinned vibes and spectral tips.
 */
@Composable
fun CrowdField(
    state: BluetoothUiState,
    localDeviceId: String,
    crowdId: String?,
    onDisconnect: () -> Unit,
    onSendMessage: (String, String) -> Unit,
    crowdIsStill: Boolean = false,
    // Hub Callbacks
    messageText: String = "",
    onMessageChange: (String) -> Unit = {},
    onSend: () -> Unit = {},
    onSearchToggle: (() -> Unit)? = null,
    onAcceptRadio: (P2PDevice) -> Unit = {},
    onDenyRadio: (P2PDevice) -> Unit = {},
    onStartSideVibe: () -> Unit = {},
    onStartChain: () -> Unit = {},
    onClearSelection: () -> Unit = {},
    onShowPrivacy: () -> Unit = {},
    onNavigateToGroup: (String) -> Unit = {},
    onNavigateToVibe: (String) -> Unit = {},
    externalFocusedId: String? = null
) {
    var showTip by remember { mutableStateOf(true) }
    var localFocusedId by remember(externalFocusedId) { mutableStateOf(externalFocusedId) }

    val crowd = remember(crowdId, state.session.groups) {
        state.session.groups.find { it.id == crowdId }
    }

    val childGroups = remember(state.session.groups, crowdId) {
        state.session.groups.filter { it.parentId == crowdId }
    }

    val childCrowds = childGroups.filter { it.scope == VibeGroup.SCOPE_PUBLIC }
    val childTies = childGroups.filter { it.scope != VibeGroup.SCOPE_PUBLIC }

    val vibesData = remember(state.session.messages, crowdId, localDeviceId, localFocusedId) {
        if (crowdId == null) {
            Triple(emptyList<MessagePayload>(), emptyMap<String, Int>(), false)
        } else {
            val baseVibes = state.session.messages.filter { it.groupId == crowdId && it.parentMessageId == null }
            val counts = baseVibes.groupBy { it.senderId }.mapValues { it.value.size }
            val sorted = baseVibes.sortedBy { it.timestamp }
            Triple(sorted, counts, localFocusedId != null)
        }
    }

    val (chatVibes, vibeCounts, isVibeFocused) = vibesData
    val sdf = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    BlukitFieldScaffold(
        themeColor = StealthPrimary,
        glowIntensityTarget = 0.6f,
        floatingContent = {
            AnimatedVisibility(
                visible = showTip && chatVibes.isEmpty() && childGroups.isEmpty(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                BlukitTip(
                    text = "THE CROWD IS SILENT. CREATE A TIE OR SPREAD A VIBE TO START.",
                    onDismiss = { showTip = false }
                )
            }
        },
        fieldContent = {
            Column(modifier = Modifier.fillMaxSize().padding(top = 80.dp)) {
                // Meta Sections
                if (childCrowds.isNotEmpty() || childTies.isNotEmpty()) {
                    Text(
                        text = "EVENT & LINKS", 
                        style = MaterialTheme.typography.labelSmall, 
                        color = StealthPrimary, 
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    
                    LazyColumn(modifier = Modifier.weight(0.4f)) {
                        items(childCrowds) { nestedCrowd ->
                            PluralPulseSummary(
                                title = nestedCrowd.name,
                                subtitle = "NESTED CROWD",
                                icon = Icons.Rounded.Grain,
                                themeColor = StealthPrimary,
                                count = nestedCrowd.memberIds.size,
                                lastUpdate = sdf.format(Date(nestedCrowd.lastVibeTimestamp)),
                                onClick = { onNavigateToGroup(nestedCrowd.id) }
                            )
                        }
                        items(childTies) { tie ->
                            PluralPulseSummary(
                                title = tie.name,
                                subtitle = if (tie.scope == VibeGroup.SCOPE_PRIVATE) "CHAIN" else "LOCAL CHAIN",
                                icon = if (tie.scope == VibeGroup.SCOPE_PRIVATE) Icons.Rounded.Hearing else Icons.Rounded.CellTower,
                                themeColor = if (tie.scope == VibeGroup.SCOPE_PRIVATE) StealthRose else StealthPrimary,
                                count = tie.memberIds.size,
                                lastUpdate = sdf.format(Date(tie.lastVibeTimestamp)),
                                onClick = { onNavigateToGroup(tie.id) }
                            )
                        }
                    }
                }

                // Vibes
                Text(
                    text = "VIBES", 
                    style = MaterialTheme.typography.labelSmall, 
                    color = StealthPrimary, 
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )

                LazyColumn(modifier = Modifier.weight(0.6f)) {
                    items(chatVibes) { vibe ->
                        if (vibe.isMeta) {
                            PluralPulseSummary(
                                title = vibe.content.take(20),
                                subtitle = "PLURAL VIBE",
                                icon = Icons.Rounded.BubbleChart,
                                themeColor = StealthPrimary,
                                count = state.session.messages.count { it.parentMessageId == vibe.messageId },
                                lastUpdate = sdf.format(Date(vibe.timestamp)),
                                onClick = { onNavigateToVibe(vibe.messageId) }
                            )
                        } else {
                            AnimatedVibeItem(
                                msg = vibe,
                                isSelected = false,
                                senderDevice = null,
                                vibeCount = 0,
                                isVibed = false,
                                isMe = vibe.senderId == localDeviceId,
                                isGrouped = false,
                                isMutual = false,
                                vibeGroup = crowd,
                                rowId = vibe.messageId,
                                onVibeClick = { /* Handle Unit Click */ },
                                onDeviceLongClick = { },
                                onDelete = { }
                            )
                        }
                    }
                }
            }
            
            // Background Canvas for Field
            RipplesField(
                state = state,
                localDeviceId = localDeviceId,
                activeBubbles = emptyList(),
                vibedPeers = emptySet(),
                drawBackground = false,
                drawNodes = false, // Simplified for Meta view
                onDeviceClick = { },
                onStartScan = { }
            )
        },
        tickerContent = {
            VibingVibesTicker(
                state = state,
                energyList = chatVibes.map { msg -> 
                    val dev = P2PDevice(id = msg.senderId, name = msg.senderName, emoji = msg.senderEmoji ?: "👤", medium = P2PDevice.ConnectionMedium.BLUETOOTH)
                    dev to msg 
                },
                vibeCounts = vibeCounts,
                localDeviceId = localDeviceId,
                vibedPeers = emptySet(),
                isGrouped = false,
                onVibeClick = { onNavigateToVibe(it) },
                onDeviceLongClick = { },
                onDeleteVibe = { },
                modifier = Modifier.fillMaxSize()
            )
        },
        inputContent = {
            BlukitVibeHub(
                currentRoute = cc.thevar.blukit.ui.navigation.Route.GroupField(crowdId ?: ""),
                messageText = messageText,
                onMessageChange = onMessageChange,
                onSend = onSend,
                vibeCount = state.session.messages.size,
                crowdIsStill = crowdIsStill,
                incomingRadioRequests = state.crowd.incomingRadioRequests,
                selectedDevices = state.crowd.selectedDevices,
                vibedPeers = emptySet(),
                groups = state.session.groups,
                onAcceptRadio = onAcceptRadio,
                onDenyRadio = onDenyRadio,
                onStartSideVibe = onStartSideVibe,
                onStartChain = onStartChain,
                onClearSelection = onClearSelection,
                onAttachFile = { }, // Handle via parent if needed
                onSearchToggle = onSearchToggle,
                onShowPrivacy = onShowPrivacy,
                modifier = Modifier.fillMaxWidth()
            )
        }
    )
}
