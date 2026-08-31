/**
 * BLUKIT ECHO FIELD
 *
 * The granular view of a single Echo interaction.
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
import cc.thevar.blukit.domain.model.Echo
import cc.thevar.blukit.domain.model.Source
import cc.thevar.blukit.domain.model.Sphere
import cc.thevar.blukit.ui.theme.*
import cc.thevar.blukit.ui.viewmodels.BluetoothUiState
import cc.thevar.blukit.ui.navigation.Route

/**
 * ECHO FIELD: Granular Echo detail.
 */
@Composable
fun EchoField(
    state: BluetoothUiState,
    localDeviceId: String,
    messageId: String,
    onNavigateToPulse: (String) -> Unit = {},
    onNavigateToLiveFeed: () -> Unit = {},
    messageText: String = "",
    onMessageChange: (String) -> Unit = {},
    onSend: () -> Unit = {},
    isSearchActive: Boolean = false,
    onSearchToggle: (() -> Unit)? = null,
    onAttachFile: () -> Unit = {},
    onInputFocusChange: (Boolean) -> Unit = {},
    breadcrumbTrail: List<String> = emptyList(),
    onCrumbClick: (Int) -> Unit = {},
    userNickname: String = "",
    activeCrowds: List<Sphere> = emptyList(),
    onShowTimeline: () -> Unit = {},
    onResetProfile: () -> Unit = {},
    onTitleClick: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null,
    header: @Composable () -> Unit,
) {
    val rootEcho = remember(messageId, state.session.messages) {
        state.session.messages.find { it.messageId == messageId }
    }

    val childEchoes = remember(state.session.messages, messageId) {
        state.session.messages.asSequence().filter { it.parentMessageId == messageId }
            .sortedBy { it.timestamp }.toList()
    }

    val themeColor = if (rootEcho?.messageScope == Echo.MESSAGE_WHISPER) StealthRose else StealthPrimary

    BlukitFieldScaffold(
        themeColor = themeColor,
        glowIntensityTarget = 0.9f,
        header = header,
        entries = {
            Column(modifier = Modifier.fillMaxSize()) {
                IdentityStage(
                    title = "ECHO",
                    breadcrumbTrail = breadcrumbTrail,
                    onCrumbClick = onCrumbClick,
                    activeRooms = activeCrowds,
                    onShowTimeline = onShowTimeline,
                    onResetProfile = onResetProfile,
                    onBack = onBack,
                    themeColor = themeColor,
                    userCount = childEchoes.size,
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
                                    text = if (isSearchActive) "SEARCH" else "SOURCES",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = (if (isSearchActive) StealthAmber else themeColor).copy(alpha = StealthAlphaHigh),
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                )

                rootEcho?.let {
                    Surface(
                        color = StealthSurface,
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, themeColor.copy(alpha = StealthAlphaMedium))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            val headerLabel = when (it.type) {
                                Echo.TYPE_FILE -> "Shared File"
                                Echo.TYPE_NOTE_UPDATE -> "Shared Record"
                                else -> "Synthesis"
                            }
                            Text(
                                text = headerLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = themeColor
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            if (it.type == Echo.TYPE_FILE) {
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
                                text = "Resonated by ${it.senderName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.4f)
                            )
                        }
                    }
                }

                ResonanceTicker(
                    state = state,
                    resonanceList = childEchoes.map { echo -> 
                        val source = Source(id = echo.senderId, name = echo.senderName, emoji = echo.senderEmoji ?: "👤", medium = Source.ResonanceMedium.BLUETOOTH)
                        source to echo 
                    },
                    echoCounts = emptyMap(),
                    localDeviceId = localDeviceId,
                    localNickname = userNickname,
                    pulsedPeers = emptySet(),
                    isGrouped = false,
                    onEchoClick = { onNavigateToPulse(it) },
                    onSourceClick = {  },
                    onSourceLongClick = {  },
                    modifier = Modifier.weight(1f),
                    themeColor = themeColor
                )
            }

            EchoHub(
                currentRoute = Route.EchoField(messageId),
                messageText = messageText,
                onMessageChange = onMessageChange,
                onSend = onSend,
                messageCount = childEchoes.size,
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

            if (childEchoes.isEmpty()) {
                BlukitTip(
                    text = "No resonance detected. Echo to start the ledger.",
                    themeColor = themeColor,
                    onDismiss = {  },
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        }
    )
}
