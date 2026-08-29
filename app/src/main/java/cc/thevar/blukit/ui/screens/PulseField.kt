/**
 * BLUKIT PULSE FIELD
 *
 * The ultimate granular view of a single interaction.
 * Breaks down Resonances into constituent Pulse Units.
 */
package cc.thevar.blukit.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BubbleChart
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.thevar.blukit.domain.model.MessagePayload
import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.domain.model.Resonance
import cc.thevar.blukit.ui.components.AssignmentItem
import cc.thevar.blukit.ui.theme.StealthPrimary
import cc.thevar.blukit.ui.theme.StealthRose
import cc.thevar.blukit.ui.viewmodels.BluetoothUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * PULSE FIELD: The ultimate granular view.
 * Displays child pulses (units) and nested pulse resonance metas.
 */
/**
 * PULSE FIELD: Granular unit drill-down.
 * 
 * Architectural Pattern:
 * - Header: Harmony Top Bar.
 * - Entries: Root Pulse Header, Unit List, Local Unit Ticker, Sub-Pulse Hub.
 */
@Composable
fun PulseField(
    state: BluetoothUiState,
    localDeviceId: String,
    messageId: String,
    onNavigateToPulse: (String) -> Unit = {},
    // Hub Callbacks
    messageText: String = "",
    onMessageChange: (String) -> Unit = {},
    onSend: () -> Unit = {},
    onSearchToggle: (() -> Unit)? = null,
    onAttachFile: () -> Unit = {},
    isInputFocused: Boolean = false,
    onInputFocusChange: (Boolean) -> Unit = {},
    // Humanity Stage Props
    breadcrumbTrail: List<String> = emptyList(),
    onCrumbClick: (Int) -> Unit = {},
    userNickname: String = "",
    userEmoji: String = "",
    onUserNicknameChange: (String) -> Unit = {},
    activeCrowds: List<Resonance> = emptyList(),
    onShowTimeline: () -> Unit = {},
    onResetProfile: () -> Unit = {},
    onTitleClick: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null,
    header: @Composable () -> Unit,
) {
    val rootPulse = remember(messageId, state.session.messages) {
        state.session.messages.find { it.messageId == messageId }
    }

    val childPulses = remember(state.session.messages, messageId) {
        state.session.messages.asSequence().filter { it.parentMessageId == messageId }
            .sortedBy { it.timestamp }.toList()
    }

    val themeColor = if (rootPulse?.pulseType == MessagePayload.PULSE_PRIVATE) StealthRose else StealthPrimary
    val sdf = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    BlukitFieldScaffold(
        themeColor = themeColor,
        glowIntensityTarget = 0.9f,
        header = header,
        entries = {
            // MODULE 1: BASE CONTENT (Radar + Lists)
            Column(modifier = Modifier.fillMaxSize()) {
                RipplesField(
                    state = state,
                    activeBubbles = emptyList(),
                    pulsedPeers = emptySet(),
                    onDeviceClick = { },
                    // Humanity Stage
                    title = "PULSE",
                    breadcrumbTrail = breadcrumbTrail,
                    onCrumbClick = onCrumbClick,
                    userNickname = userNickname,
                    userEmoji = userEmoji,
                    activeCrowds = activeCrowds,
                    onShowTimeline = onShowTimeline,
                    onResetProfile = onResetProfile,
                    onTitleClick = onTitleClick,
                    onBack = onBack,
                    onNicknameChange = onUserNicknameChange,
                    isDimmed = isInputFocused,
                    themeColor = themeColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp),
                )
                rootPulse?.let {
                    Surface(
                        color = themeColor.copy(alpha = 0.1f),
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, themeColor.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "ROOT PULSE",
                                style = MaterialTheme.typography.labelSmall,
                                color = themeColor,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = it.content.uppercase(),
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }

                Text(
                    text = "PULSE UNITS", 
                    style = MaterialTheme.typography.labelSmall, 
                    color = themeColor, 
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )

                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(childPulses) { pulse ->
                        if (pulse.type == MessagePayload.TYPE_ASSIGNMENT_TASK) {
                            AssignmentItem(
                                assignment = pulse,
                                onStatusChange = { _ ->
                                    // In a real implementation, we'd call viewModel.updateTaskStatus
                                    // For now, we'll rely on the CRDT upsert logic in PulseStore
                                },
                                themeColor = themeColor,
                                modifier = Modifier.padding(horizontal = 16.dp),
                            )
                        } else if (pulse.isMeta) {
                            ResonanceSummary(
                                title = pulse.content.take(20),
                                subtitle = "RESONANCE",
                                icon = Icons.Rounded.BubbleChart,
                                themeColor = themeColor,
                                count = state.session.messages.count { it.parentMessageId == pulse.messageId },
                                lastUpdate = sdf.format(Date(pulse.timestamp)),
                                onClick = { onNavigateToPulse(pulse.messageId) },
                                showJoin = true
                            )
                        } else {
                            AnimatedPulseItem(
                                msg = pulse,
                                isSelected = false,
                                senderDevice = null,
                                pulseCount = 0,
                                isPulsed = false,
                                isMe = pulse.senderId == localDeviceId,
                                isGrouped = false,
                                isMutual = false,
                                rowId = pulse.messageId,
                                onPulseClick = { },
                                onDeviceLongClick = { }
                            )
                        }
                    }
                }
                
                // Bottom padding to avoid occlusion
                Spacer(modifier = Modifier.height(140.dp))
            }

            // MODULE 2: TICKER (Floating Overlay)
            PulsingResonanceTicker(
                state = state,
                energyList = childPulses.map { msg -> 
                    val dev = P2PDevice(id = msg.senderId, name = msg.senderName, emoji = msg.senderEmoji ?: "👤", medium = P2PDevice.ConnectionMedium.BLUETOOTH)
                    dev to msg 
                },
                pulseCounts = emptyMap(),
                localDeviceId = localDeviceId,
                localNickname = userNickname,
                pulsedPeers = emptySet(),
                isGrouped = false,
                onPulseClick = { onNavigateToPulse(it) },
                onDeviceClick = { },
                onDeviceLongClick = { },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(280.dp)
                    .padding(bottom = 100.dp) // Room for Hub
            )

            // MODULE 3: PULSE HUB (Bottom Overlay)
            BlukitPulseHub(
                currentRoute = cc.thevar.blukit.ui.navigation.Route.PulseField(messageId),
                messageText = messageText,
                onMessageChange = onMessageChange,
                onSend = onSend,
                pulseCount = childPulses.size,
                incomingRadioRequests = emptySet(),
                selectedDevices = emptySet(),
                resonances = emptyList(),
                onAcceptRadio = { },
                onDenyRadio = { },
                onStartSidePulse = { },
                onStartChain = { },
                onClearSelection = { },
                onAttachFile = onAttachFile,
                onSearchToggle = onSearchToggle,
                onFocusChange = onInputFocusChange,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            )

            // MODULE 4: FLOATING TIPS
            if (childPulses.isEmpty()) {
                BlukitTip(
                    text = "NO GRANULAR PULSES YET. ADD A UNIT TO EXPAND THE RESONANCE.",
                    themeColor = themeColor,
                    onDismiss = { },
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        }
    )
}
