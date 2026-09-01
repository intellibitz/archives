/**
 * BLUKIT MESSAGE FIELD
 *
 * The granular view of a single Message interaction.
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
import cc.thevar.blukit.domain.model.Message
import cc.thevar.blukit.domain.model.Source
import cc.thevar.blukit.domain.model.Group
import cc.thevar.blukit.ui.theme.*
import cc.thevar.blukit.ui.viewmodels.ConnectionUiState
import cc.thevar.blukit.ui.navigation.Route
import cc.thevar.blukit.ui.components.*

import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow

/**
 * MESSAGE FIELD: Granular Message detail.
 */
@Composable
fun MessageField(
    state: ConnectionUiState,
    localDeviceId: String,
    messageId: String,
    rootMessage: Message?,
    childMessagesFlow: Flow<PagingData<Message>>,
    onNavigateToMessage: (String) -> Unit = {},
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
    activeGroups: List<Group> = emptyList(),
    onShowTimeline: () -> Unit = {},
    onResetProfile: () -> Unit = {},
    onTitleClick: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null,
    header: @Composable () -> Unit,
) {
    val childMessages = childMessagesFlow.collectAsLazyPagingItems()
    val themeColor = if (rootMessage?.messageScope == Message.MESSAGE_WHISPER) StealthRose else StealthPrimary

    BlukitFieldScaffold(
        themeColor = themeColor,
        glowIntensityTarget = 0.9f,
        header = header,
        entries = {
            Column(modifier = Modifier.fillMaxSize()) {
                rootMessage?.let {
                    Surface(
                        color = StealthSurface,
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, themeColor.copy(alpha = StealthAlphaMedium))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            val headerLabel = when (it.type) {
                                Message.TYPE_FILE -> "Shared File"
                                Message.TYPE_NOTE_UPDATE -> "Shared Record"
                                else -> "Synthesis"
                            }
                            Text(
                                text = headerLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = themeColor
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            if (it.type == Message.TYPE_FILE) {
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
                                text = "Connected by ${it.senderName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.4f)
                            )
                        }
                    }
                }

                PagedConnectionTicker(
                    state = state,
                    pagedMessages = childMessages,
                    messageCounts = emptyMap(),
                    localDeviceId = localDeviceId,
                    localNickname = userNickname,
                    pulsedPeers = emptySet(),
                    isGrouped = false,
                    onMessageClick = { onNavigateToMessage(it) },
                    onSourceClick = {  },
                    onSourceLongClick = {  },
                    modifier = Modifier.weight(1f),
                    themeColor = themeColor
                )
            }

            MessageHub(
                currentRoute = Route.MessageField(messageId),
                messageText = messageText,
                onMessageChange = onMessageChange,
                onSend = onSend,
                messageCount = childMessages.itemCount,
                incomingRadioRequests = emptySet(),
                selectedDevices = emptySet(),
                onAcceptRadio = { },
                onDenyRadio = { },
                onStartWhisper = {  },
                onStartSubGroup = {  },
                onClearSelection = {  },
                onAttachFile = onAttachFile,
                isSearchMode = isSearchActive,
                onSearchToggle = onSearchToggle,
                onFocusChange = onInputFocusChange,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            )

            if (childMessages.itemCount == 0) {
                BlukitTip(
                    text = "No connection detected. Send a Message to start the ledger.",
                    themeColor = themeColor,
                    onDismiss = {  },
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        }
    )
}
