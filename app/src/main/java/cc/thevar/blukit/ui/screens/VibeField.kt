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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.thevar.blukit.domain.model.MessagePayload
import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.ui.viewmodels.BluetoothUiState
import cc.thevar.blukit.ui.theme.StealthPrimary
import cc.thevar.blukit.ui.theme.StealthRose
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * VIBE FIELD: The ultimate granular view.
 * Displays child vibes (units) and nested vibe metas.
 */
@Composable
fun VibeField(
    state: BluetoothUiState,
    localDeviceId: String,
    localNickname: String,
    localEmoji: String,
    messageId: String,
    onSendMessage: (String, String?) -> Unit,
    onNavigateToVibe: (String) -> Unit = {},
    // Hub Callbacks
    messageText: String = "",
    onMessageChange: (String) -> Unit = {},
    onSend: () -> Unit = {},
    onSearchToggle: (() -> Unit)? = null,
    onAttachFile: () -> Unit = {},
    onShowPrivacy: () -> Unit = {}
) {
    val rootVibe = remember(messageId, state.session.messages) {
        state.session.messages.find { it.messageId == messageId }
    }

    val childVibes = remember(state.session.messages, messageId) {
        state.session.messages.filter { it.parentMessageId == messageId }
            .sortedBy { it.timestamp }
    }

    val themeColor = if (rootVibe?.vibeType == MessagePayload.VIBE_PRIVATE) StealthRose else StealthPrimary
    val sdf = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    BlukitFieldScaffold(
        themeColor = themeColor,
        glowIntensityTarget = 0.9f,
        floatingContent = {
            if (childVibes.isEmpty()) {
                BlukitTip(
                    text = "NO GRANULAR VIBES YET. ADD A UNIT TO EXPAND THE PULSE.",
                    themeColor = themeColor,
                    onDismiss = { }
                )
            }
        },
        fieldContent = {
            Column(modifier = Modifier.fillMaxSize().padding(top = 80.dp)) {
                // Header (The Meta Root)
                rootVibe?.let {
                    Surface(
                        color = themeColor.copy(alpha = 0.1f),
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, themeColor.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "ROOT VIBE",
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
                    text = "VIBE UNITS", 
                    style = MaterialTheme.typography.labelSmall, 
                    color = themeColor, 
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )

                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(childVibes) { vibe ->
                        if (vibe.isMeta) {
                            MetaVibeItem(
                                title = vibe.content.take(20),
                                subtitle = "NESTED META",
                                icon = Icons.Rounded.BubbleChart,
                                themeColor = themeColor,
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
                                vibeGroup = null,
                                rowId = vibe.messageId,
                                onVibeClick = { },
                                onDeviceLongClick = { },
                                onDelete = { }
                            )
                        }
                    }
                }
            }
        },
        tickerContent = {
            // Simplified ticker for Vibe Field
            VibingVibesTicker(
                state = state,
                energyList = childVibes.map { msg -> 
                    val dev = P2PDevice(id = msg.senderId, name = msg.senderName, emoji = msg.senderEmoji ?: "👤", medium = P2PDevice.ConnectionMedium.BLUETOOTH)
                    dev to msg 
                },
                vibeCounts = emptyMap(),
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
                currentRoute = cc.thevar.blukit.ui.navigation.Route.VibeField(messageId),
                messageText = messageText,
                onMessageChange = onMessageChange,
                onSend = onSend,
                vibeCount = childVibes.size,
                airIsStill = false,
                incomingLinkRequests = emptySet(),
                selectedDevices = emptySet(),
                vibedPeers = emptySet(),
                groups = emptyList(),
                onAcceptLink = { },
                onDenyLink = { },
                onStartSideVibe = { },
                onStartTie = { },
                onClearSelection = { },
                onAttachFile = onAttachFile,
                onSearchToggle = onSearchToggle,
                onShowPrivacy = onShowPrivacy,
                modifier = Modifier.fillMaxWidth()
            )
        }
    )
}
