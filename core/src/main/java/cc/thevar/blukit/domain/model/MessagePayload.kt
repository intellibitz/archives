package cc.thevar.blukit.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class MessagePayload(
    val messageId: String,
    val senderId: String,
    val senderName: String,
    val senderEmoji: String? = null,
    val receiverId: String? = null,
    val groupId: String? = null,
    val content: String,
    val timestamp: Long,
    val type: Int = TYPE_TEXT,
    val vibeType: Int = VIBE_PUBLIC,
    val status: Int = STATUS_SENT,
    val hopCount: Int = 0
) {
    companion object {
        const val TYPE_TEXT = 1
        const val TYPE_IMAGE = 2
        const val TYPE_ACK = 3
        const val TYPE_LINK_REQUEST = 4
        const val TYPE_LINK_ACCEPT = 5
        const val TYPE_LINK_DENY = 6

        const val VIBE_PUBLIC = 0
        const val VIBE_SIDE = 1
        const val VIBE_TIE = 2

        const val STATUS_PENDING = 0
        const val STATUS_SENT = 1
        const val STATUS_DELIVERED = 2
    }
}
