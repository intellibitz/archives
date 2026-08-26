/**
 * BLUKIT CORE DOMAIN: MESSAGE PAYLOAD
 *
 * The atomic unit of energy (Pulse) in the mesh.
 * Contains all necessary metadata for offline propagation and decentralized orchestration.
 */
package cc.thevar.blukit.domain.model

import kotlinx.serialization.Serializable

/**
 * Data packet representing a single pulse resonance.
 * 
 * @property messageId Unique UUID for deduplication across mesh hops.
 * @property senderId Hardware ID of the originating peer.
 * @property content The core energy of the pulse (Text, JSON, or media reference).
 * @property timestamp Epoch time of creation for chronological ordering (Timeline Field).
 * @property pulseType Scoping: SHOUT (Public), WHISPER (Private), or SILENCE (Local).
 * @property noteVersion LWW (Last-Write-Wins) version for conflict-free shared note updates.
 * @property hopCount Tracking ephemeral hops for Mesh Relay (ephemeral hops).
 */
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
    val pulseType: Int = PULSE_PUBLIC,
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
    val resonanceWeight: Int = 0, // Consensus weight for Crowd AI
) {
    companion object {
        // --- Core Pulse Types ---
        const val TYPE_TEXT = 1
        const val TYPE_IMAGE = 2
        const val TYPE_ACK = 3
        const val TYPE_TIE_REQUEST = 4
        const val TYPE_TIE_ACCEPT = 5
        const val TYPE_TIE_DENY = 6
        const val TYPE_FILE = 7
        const val TYPE_IDENTITY_UPDATE = 8
        
        // --- Resync & Mesh Protocol ---
        const val TYPE_RESYNC_REQUEST = 9
        const val TYPE_RESYNC_CHUNK = 10
        const val TYPE_RESYNC_COMPLETE = 11
        
        // --- Shared Interaction Logic ---
        const val TYPE_NOTE_UPDATE = 12
        const val TYPE_RITUAL_PUSH = 13
        const val TYPE_ASSIGNMENT_TASK = 14
        const val TYPE_CALENDAR_EVENT = 15
        const val TYPE_MEMORY = 16

        // --- Crowd AI & Intelligence ---
        const val TYPE_AI_SUMMARY = 17
        const val TYPE_CONSENSUS_VOTE = 18
        const val TYPE_CROWD_INSIGHT = 19

        // --- Task Status (CRDT) ---
        const val TASK_PENDING = 0
        const val TASK_IN_PROGRESS = 1
        const val TASK_COMPLETED = 2

        // --- Scoping Lexicon ---
        const val PULSE_SHOUT = 0
        const val PULSE_WHISPER = 1
        const val PULSE_SILENCE = 3

        const val PULSE_PUBLIC = PULSE_SHOUT
        const val PULSE_LOCAL = PULSE_SILENCE
        const val PULSE_PRIVATE = PULSE_WHISPER

        // --- Propagation Status ---
        const val STATUS_PENDING = 0
        const val STATUS_SENT = 1
        const val STATUS_DELIVERED = 2
    }
}
