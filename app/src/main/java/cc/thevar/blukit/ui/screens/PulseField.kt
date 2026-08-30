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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.rounded.WifiTethering
import androidx.compose.material.icons.rounded.Radar
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.ui.zIndex
import cc.thevar.blukit.domain.model.MessagePayload
import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.domain.model.Resonance
import cc.thevar.blukit.ui.components.AssignmentItem
import cc.thevar.blukit.ui.theme.StealthAmber
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
    onNavigateToLiveFeed: () -> Unit = {},
    // Hub Callbacks
    messageText: String = "",
    onMessageChange: (String) -> Unit = {},
    onSend: () -> Unit = {},
    isSearchActive: Boolean = false,
    onSearchToggle: (() -> Unit)? = null,
    onAttachFile: () -> Unit = {},
    onInputFocusChange: (Boolean) -> Unit = {},
    // Humanity Stage Props
    breadcrumbTrail: List<String> = emptyList(),
    onCrumbClick: (Int) -> Unit = {},
    userNickname: String = "",
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

    BlukitFieldScaffold(
        themeColor = themeColor,
        glowIntensityTarget = 0.9f,
        header = header,
        entries = {
            // MODULE 1: BASE CONTENT (Humanity Stage + Unified Ticker/Radar)
            Column(modifier = Modifier.fillMaxSize()) {
                // Humanity Stage (Breadcrumbs)
                BlukitHumanityStage(
                    title = "MESSAGE",
                    breadcrumbTrail = breadcrumbTrail,
                    onCrumbClick = onCrumbClick,
                    activeCrowds = activeCrowds,
                    onShowTimeline = onShowTimeline,
                    onResetProfile = onResetProfile,
                    onTitleClick = onTitleClick,
                    onBack = onBack,
                    themeColor = themeColor,
                    userCount = childPulses.size,
                    onModeChange = { onNavigateToLiveFeed() },
                    trailingContent = {
                        // Tactical Radar Toggles
                        if (onSearchToggle != null) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                IconButton(
                                    onClick = onSearchToggle,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isSearchActive) Icons.Rounded.WifiTethering else Icons.Rounded.Radar,
                                        contentDescription = "Toggle Search",
                                        tint = if (isSearchActive) StealthAmber else themeColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = if (isSearchActive) "SCAN" else "RADAR",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = (if (isSearchActive) StealthAmber else themeColor).copy(alpha = 0.8f),
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
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
                                text = "ORIGINAL MESSAGE",
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

                // MODULE 2: UNIFIED RESONANCE TICKER (With integrated Radar)
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
                    modifier = Modifier.weight(1f),
                    themeColor = themeColor
                )
            }

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
                isSearchMode = isSearchActive,
                onSearchToggle = onSearchToggle,
                onFocusChange = onInputFocusChange,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            )

            // MODULE 4: FLOATING TIPS
            if (childPulses.isEmpty()) {
                BlukitTip(
                    text = "NO REPLIES DETECTED. REPLY TO START THE CONVERSATION.",
                    themeColor = themeColor,
                    onDismiss = { },
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        }
    )
}
