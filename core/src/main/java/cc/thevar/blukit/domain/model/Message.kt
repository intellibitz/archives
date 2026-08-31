/**
 * BLUKIT CORE DOMAIN: MESSAGE
 *
 * The atomic unit of communication in the mesh.
 * Contains all necessary metadata for local connection.
 */
package cc.thevar.blukit.domain.model

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Data packet representing a single Message or shared item.
 * 
 * @property messageId Unique UUID for deduplication across local connection links.
 * @property senderId Hardware ID of the originating Source.
 * @property content The core Message (Text, JSON, or file reference).
 * @property timestamp Epoch time of creation for chronological ordering.
 * @property messageScope Scoping: SHOUT (Public), WHISPER (Private), or SILENCE (Device).
 * @property noteVersion LWW (Last-Write-Wins) version for conflict-free shared items.
 * @property hopCount Tracking hops for discovery.
 */
@Entity(tableName = "messages")
@Serializable
data class Message(
    @PrimaryKey val messageId: String,
    val senderId: String,
    val senderName: String,
    val senderEmoji: String? = null,
    val receiverId: String? = null,
    val groupId: String? = null,
    val groupName: String? = null,
    val content: String,
    val trendLabel: String? = null,
    val timestamp: Long,
    val type: Int = TYPE_TEXT,
    val messageScope: Int = SCOPE_PUBLIC,
    val status: Int = STATUS_SENT,
    val parentMessageId: String? = null,
    val isMeta: Boolean = false,
    val hopCount: Int = 0,
    val noteVersion: Int = 0,
    val fileId: Long? = null,
    val fileName: String? = null,
    val fileSize: Long? = null,
    val mimeType: String? = null,
    val isPriority: Boolean = false,
    val dueDate: Long? = null,
    val assigneeId: String? = null,
    val taskStatus: Int = 0,
    val connectionWeight: Int = 0, // Social priority weight
    val anchoredCount: Int = 0, // Number of peers holding this record
    val anchoredPublicGroupId: String? = null, // Anchor reference for private sub-groups
) {
    companion object {
        // --- Core Message Types ---
        const val TYPE_TEXT = 1
        const val TYPE_IMAGE = 2
        const val TYPE_ACK = 3
        const val TYPE_GROUP_REQUEST = 4
        const val TYPE_GROUP_ACCEPT = 5
        const val TYPE_FILE = 7
        const val TYPE_IDENTITY_UPDATE = 8
        
        // --- Synchronization ---
        const val TYPE_RESYNC_REQUEST = 9
        const val TYPE_RESYNC_CHUNK = 10
        const val TYPE_RESYNC_COMPLETE = 11
        
        // --- Shared Social Items ---
        const val TYPE_NOTE_UPDATE = 12
        const val TYPE_RITUAL_PUSH = 13
        const val TYPE_ASSIGNMENT_TASK = 14
        const val TYPE_MEMORY = 16

        // --- Intelligence ---
        const val TYPE_AI_SUMMARY = 17
        const val TYPE_CONSENSUS_VOTE = 18
        const val TYPE_ANCHOR_ADVERTISEMENT = 19

        // --- Status ---
        const val TASK_PENDING = 0
        const val TASK_COMPLETED = 1
        const val TASK_BLOCKED = 2
        const val TASK_ABANDONED = 3

        // --- Social Scoping (Connection levels) ---
        const val MESSAGE_SHOUT = 0
        const val MESSAGE_WHISPER = 1
        const val MESSAGE_SILENCE = 3

        const val SCOPE_PUBLIC = MESSAGE_SHOUT
        const val SCOPE_PRIVATE = MESSAGE_WHISPER

        // --- Status ---
        const val STATUS_SENT = 1
        const val STATUS_DELIVERED = 2
    }
}
