/**
 * BLUKIT CORE DOMAIN: MESH ROOM
 *
 * A shared container for family chats and campus rooms.
 * Orchestrates group scaling and partition strategies for decentralized efficiency.
 */
package cc.thevar.blukit.domain.model

import kotlinx.serialization.Serializable

/**
 * Data model for a Mesh Room (Public) or Private Channel.
 *
 * @property id Unique identifier, often deterministic for public Rooms (room_NAME).
 * @property name Human-readable title of the room.
 * @property scope Scoping level: PUBLIC (Open Room), PRIVATE (Family/Channel), or LOCAL (Device).
 * @property isArchived True if the room hasn't messaged in 30 days (Sunk Message).
 * @property partitionThreshold Threshold for member partitioning to maintain radio efficiency.
 * @property memberSections Partitioned buckets of members for large-scale Rooms (>500 members).
 */
@Serializable
data class MeshRoom(
    val id: String,
    val name: String,
    val memberIds: Set<String> = emptySet(),
    val scope: Int = SCOPE_PUBLIC,
    val lastMessageTimestamp: Long = System.currentTimeMillis(),
    val isPersistent: Boolean = true,
    val isArchived: Boolean = false,
    val parentId: String? = null,
    val isMeta: Boolean = true,
    val pinnedMessageIds: Set<String> = emptySet(),
    val projectionEmoji: String? = null,
    val isVaulted: Boolean = false,
    val isSeniorVault: Boolean = false,
    val isPinned: Boolean = false, // For priority family/home rooms
    val vaultTimestamp: Long? = null,
    val schedules: List<RoomEvent> = emptyList(),
    val partitionThreshold: Int = 100,
    val memberSections: Map<String, Set<String>> = emptyMap(),
    val connections: List<RoomConnection> = emptyList(),
    val templateId: String? = null,
    val ownerId: String? = null,
    val userRoles: Map<String, String> = emptyMap(), // Map of userId to role name
) {
    /**
     * Resolves all unique member IDs across flat list and partitioned sections.
     * Ensuring a unified view for identity resolution.
     */
    val allMemberIds: Set<String> get() = memberIds + memberSections.values.asSequence().flatten().toSet()

    /** True if this is the root collective room that all users are pre-joined to. */
    val isDefaultRoom: Boolean get() = id == ID_GLOBAL

    companion object {
        // --- Scoping Levels (Social Aliases) ---
        const val SCOPE_PUBLIC = 0
        const val SCOPE_PRIVATE = 1
        const val SCOPE_LOCAL = 2

        const val TYPE_OPEN_ROOM = SCOPE_PUBLIC
        const val TYPE_PRIVATE_CHANNEL = SCOPE_PRIVATE

        // --- Root Identity Identifiers ---
        const val ID_GLOBAL = "global_room"
        const val ID_SILENCE = "silence_room"

        /** The duration after which an inactive room is auto-archived. */
        const val ARCHIVE_THRESHOLD_MS = 30L * 24 * 60 * 60 * 1000 // 30 Days

        /**
         * The absolute maximum number of members in a flat set before partitioning is required.
         * Optimization: prevents O(N) lookup issues during rapid radio broadcasts.
         */
        const val MAX_MEMBERS_PER_SECTION = 500

        /**
         * Generates a deterministic ID for public rooms or a UUID for private ones.
         */
        fun generateId(name: String, scope: Int, parentGroup: MeshRoom? = null): String {
            val normalized = name.uppercase().trim()
            return if (scope == SCOPE_PUBLIC) {
                if (normalized == "GLOBAL" || normalized == "OPEN MESH") {
                    ID_GLOBAL
                } else {
                    // Recursive path generation for Child Rooms
                    if (parentGroup?.scope == SCOPE_PUBLIC && parentGroup.id != ID_GLOBAL) {
                        "${parentGroup.id}_${normalized.replace(" ", "_")}"
                    } else {
                        "room_${normalized.replace(" ", "_")}"
                    }
                }
            } else {
                // Private Channels are always unique anchors
                java.util.UUID.randomUUID().toString()
            }
        }
    }
}
