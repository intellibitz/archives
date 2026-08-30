/**
 * BLUKIT MESSAGE FIELD
 *
 * The ultimate granular view of a single interaction.
 */
package cc.thevar.blukit.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cc.thevar.blukit.domain.model.MeshMessage
import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.domain.model.MeshRoom
import cc.thevar.blukit.ui.theme.*
import cc.thevar.blukit.ui.viewmodels.BluetoothUiState
import cc.thevar.blukit.ui.navigation.Route

/**
 * MESSAGE FIELD: Granular message detail.
 */
@Composable
fun MessageField(
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
    activeCrowds: List<MeshRoom> = emptyList(),
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

    val themeColor = if (rootPulse?.messageScope == MeshMessage.SCOPE_PRIVATE) StealthRose else StealthPrimary

    BlukitFieldScaffold(
        themeColor = themeColor,
        glowIntensityTarget = 0.9f,
        header = header,
        entries = {
            Column(modifier = Modifier.fillMaxSize()) {
                IdentityStage(
                    title = "MESSAGE",
                    breadcrumbTrail = breadcrumbTrail,
                    onCrumbClick = onCrumbClick,
                    activeRooms = activeCrowds,
                    onShowTimeline = onShowTimeline,
                    onResetProfile = onResetProfile,
                    onBack = onBack,
                    themeColor = themeColor,
                    userCount = childPulses.size,
                    onModeChange = { onNavigateToLiveFeed() },
                    trailingContent = {
                        if (onSearchToggle != null) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                IconButton(
                                    onClick = onSearchToggle,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isSearchActive) Icons.Rounded.Search else Icons.Rounded.People,
                                        contentDescription = "Toggle Search",
                                        tint = if (isSearchActive) StealthAmber else themeColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = if (isSearchActive) "SEARCH" else "PEOPLE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = (if (isSearchActive) StealthAmber else themeColor).copy(alpha = StealthAlphaHigh),
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                )

                rootPulse?.let {
                    Surface(
                        color = StealthSurface,
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, themeColor.copy(alpha = StealthAlphaMedium))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            val headerLabel = when (it.type) {
                                MeshMessage.TYPE_FILE -> "Shared File"
                                MeshMessage.TYPE_NOTE_UPDATE -> "Shared Note"
                                else -> "Topic"
                            }
                            Text(
                                text = headerLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = themeColor
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            if (it.type == MeshMessage.TYPE_FILE) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.Description, contentDescription = null, tint = themeColor, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(text = it.fileName ?: "Document", style = MaterialTheme.typography.bodyLarge, color = Color.White)
                                        Text(text = "${(it.fileSize ?: 0) / 1024} KB", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f))
                                    }
                                }
                            } else {
                                Text(
                                    text = it.content, 
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.White
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Shared by ${it.senderName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.4f)
                            )
                        }
                    }
                }

                LiveMessageTicker(
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
                    onDeviceClick = {  },
                    onDeviceLongClick = {  },
                    modifier = Modifier.weight(1f),
                    themeColor = themeColor
                )
            }

            MessageHub(
                currentRoute = Route.MessageField(messageId),
                messageText = messageText,
                onMessageChange = onMessageChange,
                onSend = onSend,
                messageCount = childPulses.size,
                incomingRadioRequests = emptySet(),
                selectedDevices = emptySet(),
                onAcceptRadio = { },
                onDenyRadio = { },
                onStartSidePulse = {  },
                onStartChain = {  },
                onClearSelection = {  },
                onAttachFile = onAttachFile,
                isSearchMode = isSearchActive,
                onSearchToggle = onSearchToggle,
                onFocusChange = onInputFocusChange,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            )

            if (childPulses.isEmpty()) {
                BlukitTip(
                    text = "No replies detected. Reply to start the conversation.",
                    themeColor = themeColor,
                    onDismiss = {  },
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        }
    )
}
