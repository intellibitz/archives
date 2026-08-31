package cc.thevar.blukit.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.thevar.blukit.domain.model.Source
import cc.thevar.blukit.domain.model.Group
import cc.thevar.blukit.ui.navigation.Route
import cc.thevar.blukit.ui.components.BlukitWidget
import cc.thevar.blukit.ui.theme.*

/**
 * MESSAGE HUB: The primary interaction point at the bottom of the field.
 *
 * @param currentRoute The active navigation destination (determines theme and context).
 * @param messageText The current content of the Message input field.
 * @param onMessageChange Callback for text input updates.
 * @param onSend Triggered to release a new Message into the field.
 * @param messageCount The number of recent Messages detected in the current context.
 * @param incomingRadioRequests Set of Sources currently requesting a connection handshake.
 * @param selectedDevices Set of Source IDs selected for group actions.
 * @param onAcceptRadio Accept an incoming connection request.
 * @param onDenyRadio Decline an incoming connection request.
 * @param onStartWhisper Initiate a private one-on-one "Whisper" session.
 * @param onStartSubGroup Create a new linked Group context (Sub-Group).
 * @param onClearSelection Deselect all Sources.
 */
@Composable
fun MessageHub(
    currentRoute: Route,
    messageText: String,
    onMessageChange: (String) -> Unit,
    onSend: () -> Unit,
    messageCount: Int,
    incomingRadioRequests: Set<Source>,
    selectedDevices: Set<String>,
    onAcceptRadio: (Source) -> Unit,
    onDenyRadio: (Source) -> Unit,
    onStartWhisper: () -> Unit,
    onStartSubGroup: () -> Unit,
    onClearSelection: () -> Unit,
    modifier: Modifier = Modifier,
    groups: List<Group> = emptyList(),
    onAttachFile: () -> Unit = {},
    onSearchToggle: (() -> Unit)? = null,
    onManage: (() -> Unit)? = null,
    onNote: (() -> Unit)? = null,
    onCreatePublicRoom: ((String, String?) -> Unit)? = null,
    onTask: (() -> Unit)? = null, 
    isSearchMode: Boolean = false,
    onFocusChange: (Boolean) -> Unit = {}
) {
    val isPrivate = currentRoute is Route.GroupField || currentRoute is Route.Nearby
    val targetName = if (currentRoute is Route.GroupField) groups.find { it.id == currentRoute.roomId }?.name else null
    val themeColor = if (isPrivate) StealthRose else StealthPrimary

    BlukitWidget(
        themeColor = themeColor,
        header = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                SelectionInteractionHeader(
                    selectedCount = selectedDevices.size,
                    themeColor = themeColor,
                    onStartWhisper = onStartWhisper,
                    onStartSubGroup = onStartSubGroup,
                    onClearSelection = onClearSelection
                )
                
                SearchActionBanner(
                    isSearchMode = isSearchMode,
                    messageText = messageText,
                    onCreatePublicRoom = onCreatePublicRoom
                )

                IncomingRequestBanner(
                    requests = incomingRadioRequests.toList(),
                    onAccept = onAcceptRadio,
                    onDeny = onDenyRadio
                )
            }
        },
        entries = {
            val isMessageLocked = currentRoute is Route.Nearby
            Column(modifier = Modifier.fillMaxWidth()) {
                BlukitInput(
                    isReadOnly = false, 
                    isMessageLocked = isMessageLocked,
                    isPrivate = isPrivate, 
                    targetName = targetName, 
                    value = messageText, 
                    onValueChange = onMessageChange, 
                    onSend = onSend, 
                    onAttachFile = onAttachFile, 
                    onManage = onManage,
                    onNote = onNote,
                    onTask = onTask, 
                    messageCount = messageCount, 
                    isSearchActive = isSearchMode,
                    onSearchToggle = onSearchToggle,
                    onFocusChange = onFocusChange,
                    modifier = Modifier.fillMaxWidth().navigationBarsPadding().imePadding()
                )
            }
        },
        modifier = modifier
    )
}

/**
 * Interaction header for performing actions on selected sources.
 */
@Composable
fun SelectionInteractionHeader(
    selectedCount: Int,
    themeColor: Color,
    onStartWhisper: () -> Unit,
    onStartSubGroup: () -> Unit,
    onClearSelection: () -> Unit
) {
    AnimatedVisibility(
        visible = selectedCount > 0, 
        enter = fadeIn() + expandVertically(expandFrom = Alignment.Bottom), 
        exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Bottom)
    ) {
        Row(
            modifier = Modifier
                .padding(bottom = 12.dp)
                .background(StealthBlack.copy(alpha = 0.8f), RoundedCornerShape(20.dp))
                .border(1.dp, themeColor.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onStartWhisper, 
                colors = ButtonDefaults.buttonColors(containerColor = StealthPrimary, contentColor = Color.Black), 
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) { 
                Text("Private", style = MaterialTheme.typography.labelLarge) 
            }
            Button(
                onClick = onStartSubGroup, 
                colors = ButtonDefaults.buttonColors(containerColor = StealthRose, contentColor = Color.White), 
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) { 
                Text("New Group", style = MaterialTheme.typography.labelLarge) 
            }
            IconButton(
                onClick = onClearSelection, 
                modifier = Modifier.size(32.dp).background(Color.White.copy(alpha = 0.1f), CircleShape)
            ) { 
                Icon(Icons.Rounded.Close, tint = Color.White, contentDescription = "Cancel", modifier = Modifier.size(16.dp)) 
            }
        }
    }
}

/**
 * Action banner for creating a new public Group based on search query.
 */
@Composable
fun SearchActionBanner(
    isSearchMode: Boolean,
    messageText: String,
    onCreatePublicRoom: ((String, String?) -> Unit)?
) {
    val showGroupBanner = isSearchMode && messageText.isNotBlank() && onCreatePublicRoom != null
    AnimatedVisibility(
        visible = showGroupBanner, 
        enter = fadeIn() + slideInVertically { it / 2 },
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
    ) {
        Button(
            onClick = { onCreatePublicRoom?.invoke(messageText, null) },
            colors = ButtonDefaults.buttonColors(containerColor = StealthPrimary, contentColor = Color.Black),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.padding(bottom = 12.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
        ) {
            Icon(Icons.Rounded.Grain, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Start Group: $messageText", 
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

/**
 * Banner notifying the user of incoming radio connection requests.
 */
@Composable
fun IncomingRequestBanner(
    requests: List<Source>,
    onAccept: (Source) -> Unit,
    onDeny: (Source) -> Unit
) {
    if (requests.isNotEmpty()) {
        val request = requests.first()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .background(StealthPrimary.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                .padding(12.dp)
                .testTag("IncomingRequestRow"),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = request.emoji, fontSize = 20.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Incoming Connection Request", 
                    style = MaterialTheme.typography.labelSmall, 
                    color = StealthPrimary
                )
                Text(
                    text = (request.name ?: "Unknown"), 
                    style = MaterialTheme.typography.bodySmall, 
                    color = Color.White
                )
            }
            Row {
                IconButton(
                    onClick = { onDeny(request) },
                    modifier = Modifier.testTag("DenyRequestButton")
                ) { 
                    Icon(Icons.Rounded.Close, contentDescription = "Deny", tint = StealthError) 
                }
                IconButton(
                    onClick = { onAccept(request) },
                    modifier = Modifier.testTag("AcceptRequestButton")
                ) { 
                    Icon(Icons.Rounded.Check, contentDescription = "Accept", tint = StealthPrimary) 
                }
            }
        }
    }
}

/**
 * Standard text input for the social hub with specialized actions.
 */
@Composable
fun BlukitInput(
    isReadOnly: Boolean,
    isMessageLocked: Boolean,
    isPrivate: Boolean,
    targetName: String?,
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    onAttachFile: () -> Unit,
    onManage: (() -> Unit)?,
    onNote: (() -> Unit)?,
    onTask: (() -> Unit)?,
    messageCount: Int,
    isSearchActive: Boolean,
    onSearchToggle: (() -> Unit)?,
    onFocusChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = StealthSurface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onSearchToggle != null) {
                    IconButton(onClick = onSearchToggle) {
                        Icon(
                            imageVector = if (isSearchActive) Icons.Rounded.Close else Icons.Rounded.Search,
                            contentDescription = "Search",
                            tint = if (isSearchActive) StealthRose else Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
                
                Box(modifier = Modifier.weight(1f)) {
                    androidx.compose.foundation.text.BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        enabled = !isMessageLocked,
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { onFocusChange(it.isFocused) },
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White),
                        cursorBrush = androidx.compose.ui.graphics.SolidColor(if (isPrivate) StealthRose else StealthPrimary),
                        decorationBox = { innerTextField ->
                            if (value.isEmpty()) {
                                Text(
                                    text = when {
                                        isSearchActive -> "Search chat..."
                                        isMessageLocked -> "Connected to nearby people"
                                        targetName != null -> "Chat in $targetName..."
                                        else -> "Say something..."
                                    },
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.White.copy(alpha = 0.3f)
                                )
                            }
                            innerTextField()
                        }
                    )
                }

                if (value.isNotBlank() && !isReadOnly && !isMessageLocked) {
                    IconButton(
                        onClick = onSend,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (isPrivate) StealthRose else StealthPrimary,
                            contentColor = Color.Black
                        )
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = "Send")
                    }
                }
            }

            if (!isReadOnly) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onAttachFile) {
                        Icon(Icons.Rounded.Add, contentDescription = "Attach", tint = Color.White.copy(alpha = 0.6f))
                    }
                    if (onNote != null) {
                        IconButton(onClick = onNote) {
                            Icon(Icons.Rounded.EditNote, contentDescription = "Note", tint = Color.White.copy(alpha = 0.6f))
                        }
                    }
                    if (onTask != null) {
                        IconButton(onClick = onTask) {
                            Icon(Icons.Rounded.TaskAlt, contentDescription = "Task", tint = Color.White.copy(alpha = 0.6f))
                        }
                    }
                    if (onManage != null) {
                        IconButton(onClick = onManage) {
                            Icon(Icons.Rounded.Settings, contentDescription = "Manage", tint = Color.White.copy(alpha = 0.6f))
                        }
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    if (messageCount > 0) {
                        Text(
                            text = "$messageCount Messages",
                            style = MaterialTheme.typography.labelSmall,
                            color = (if (isPrivate) StealthRose else StealthPrimary).copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}
