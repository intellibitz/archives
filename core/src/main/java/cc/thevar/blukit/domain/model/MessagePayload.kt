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
    val groupName: String? = null,
    val content: String,
    val timestamp: Long,
    val type: Int = TYPE_TEXT,
    val vibeType: Int = VIBE_PUBLIC,
    val status: Int = STATUS_SENT,
    val hopCount: Int = 0,
    val noteVersion: Int = 0,
    val fileId: Long? = null,
    val fileName: String? = null,
    val fileSize: Long? = null,
    val mimeType: String? = null
) {
    companion object {
        const val TYPE_TEXT = 1
        const val TYPE_IMAGE = 2
        const val TYPE_ACK = 3
        const val TYPE_LINK_REQUEST = 4
        const val TYPE_LINK_ACCEPT = 5
        const val TYPE_LINK_DENY = 6
        const val TYPE_FILE = 7
        const val TYPE_IDENTITY_UPDATE = 8
        const val TYPE_RESYNC_REQUEST = 9
        const val TYPE_RESYNC_CHUNK = 10
        const val TYPE_RESYNC_COMPLETE = 11
        const val TYPE_NOTE_UPDATE = 12

        const val VIBE_SHOUT = 0
        const val VIBE_WHISPER = 1
        const val VIBE_SILENCE = 3

        const val VIBE_PUBLIC = VIBE_SHOUT
        const val VIBE_LOCAL = VIBE_SILENCE
        const val VIBE_PRIVATE = VIBE_WHISPER

        const val STATUS_PENDING = 0
        const val STATUS_SENT = 1
        const val STATUS_DELIVERED = 2
    }
}
