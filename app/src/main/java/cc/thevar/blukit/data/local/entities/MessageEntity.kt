package cc.thevar.blukit.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey
    val messageId: String,
    val senderId: String,
    val senderName: String,
    val receiverId: String? = null,
    val content: String,
    val timestamp: Long,
    val type: Int, // 1 for text, 2 for image, etc.
    val isFromLocalUser: Boolean,
    val status: Int // 0: Pending, 1: Sent, 2: Delivered
)
