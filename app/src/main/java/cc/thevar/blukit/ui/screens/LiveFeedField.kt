package cc.thevar.blukit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import cc.thevar.blukit.domain.model.Message
import cc.thevar.blukit.domain.model.Source
import cc.thevar.blukit.ui.theme.*
import cc.thevar.blukit.ui.components.*

import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.flow.Flow

/**
 * LIVE FEED FIELD: A real-time stream of every Message in the field using Paging.
 */
@Composable
fun LiveFeedField(
    messagesFlow: Flow<PagingData<Message>>,
    sources: List<Source>,
    onBack: () -> Unit,
    onMessageClick: (String) -> Unit
) {
    val messages = messagesFlow.collectAsLazyPagingItems()
    Box(modifier = Modifier.fillMaxSize().background(StealthBlack)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp, top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items(
                count = messages.itemCount,
                key = messages.itemKey { it.messageId }
            ) { index ->
                val message = messages[index]
                if (message != null) {
                    val source = sources.find { it.id == message.senderId || it.persistentId == message.senderId }
                    MessageItem(
                        message = message,
                        isSelected = false,
                        senderSource = source,
                        replyCount = 0,
                        isPulsed = false,
                        isMe = false,
                        isGrouped = false,
                        isMutual = false,
                        rowId = "live_${message.messageId}",
                        onMessageClick = { onMessageClick(message.messageId) },
                        onSourceLongClick = {}
                    )
                }
            }
        }
    }
}
