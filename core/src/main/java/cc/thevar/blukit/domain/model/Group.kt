/**
 * BLUKIT CORE DOMAIN: GROUP
 *
 * A shared container for existence records.
 * Orchestrates group connection and partition strategies for decentralized efficiency.
 */
package cc.thevar.blukit.domain.model

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Data model for a Group (Public) or Private Channel.
 *
 * @property id Unique identifier, often deterministic for public Groups (group_NAME).
 * @property name Human-readable title of the group.
 * @property scope Scoping level: PUBLIC (Open Group), PRIVATE (Family/Channel), or LOCAL (Device).
 * @property isArchived True if the group hasn't connected in 30 days.
 * @property partitionThreshold Threshold for member partitioning to maintain connection efficiency.
 * @property memberSections Partitioned buckets of members for large-scale Groups (>500 members).
 */
@Entity(tableName = "groups")
@Serializable
data class Group(
    @PrimaryKey val id: String,
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
    val isPinned: Boolean = false, // For priority family/home groups
    val vaultTimestamp: Long? = null,
    val schedules: List<GroupEvent> = emptyList(),
    val partitionThreshold: Int = 100,
    val memberSections: Map<String, Set<String>> = emptyMap(),
    val connections: List<RoomConnection> = emptyList(), 
    val templateId: String? = null,
    val ownerId: String? = null,
    val userRoles: Map<String, String> = emptyMap(), // Map of userId to role name
    val anchoredPublicGroupId: String? = null, // ID of the public group this private one is anchored to
    val encryptedGroupKey: String? = null, // Group shared secret for multi-user private connection
    val trendLabel: String? = null, // Silent Assistant trend detection
    val connectionSummary: String? = null, // Silent Assistant synthesis summary
    val extractedTasks: Set<String> = emptySet(), // Tasks mined by Assistant
) {
    /**
     * Resolves all unique member IDs across flat list and partitioned sections.
     * Ensuring a unified view for identity resolution.
     */
    val allMemberIds: Set<String> get() = memberIds + memberSections.values.asSequence().flatten().toSet()

    /** True if this is the root collective group that all Sources are pre-joined to. */
    val isDefaultGroup: Boolean get() = id == ID_GLOBAL

    /** True if this group is anchored to a public connection context. */
    val isAnchored: Boolean get() = anchoredPublicGroupId != null

    companion object {
        // --- Scoping Levels (Connection Aliases) ---
        const val SCOPE_PUBLIC = 0
        const val SCOPE_PRIVATE = 1
        const val SCOPE_LOCAL = 2

        const val TYPE_OPEN_GROUP = SCOPE_PUBLIC
        const val TYPE_PRIVATE_CHANNEL = SCOPE_PRIVATE

        // --- Root Identity Identifiers ---
        const val ID_GLOBAL = "global_group"
        const val ID_SILENCE = "silence_group"

        /** The duration after which an inactive group is auto-archived. */
        const val ARCHIVE_THRESHOLD_MS = 30L * 24 * 60 * 60 * 1000 // 30 Days

        /**
         * The absolute maximum number of members in a flat set before partitioning is required.
         */
        const val MAX_MEMBERS_PER_SECTION = 500

        /**
         * Generates a deterministic ID for public groups or a UUID for private ones.
         */
        fun generateId(name: String, scope: Int, parentGroup: Group? = null): String {
            val normalized = name.uppercase().trim()
            return if (scope == SCOPE_PUBLIC) {
                if (normalized == "GLOBAL" || normalized == "OPEN MESH") {
                    ID_GLOBAL
                } else {
                    // Recursive path generation for Child Groups
                    if (parentGroup?.scope == SCOPE_PUBLIC && parentGroup.id != ID_GLOBAL) {
                        "${parentGroup.id}_${normalized.replace(" ", "_")}"
                    } else {
                        "group_${normalized.replace(" ", "_")}"
                    }
                }
            } else {
                // Private Channels are always unique anchors
                java.util.UUID.randomUUID().toString()
            }
        }
    }
}
