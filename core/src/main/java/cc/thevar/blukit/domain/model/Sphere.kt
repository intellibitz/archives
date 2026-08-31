/**
 * BLUKIT CORE DOMAIN: SPHERE
 *
 * A shared container for existence records.
 * Orchestrates group resonance and partition strategies for decentralized efficiency.
 */
package cc.thevar.blukit.domain.model

import kotlinx.serialization.Serializable

/**
 * Data model for a Sphere (Public) or Private Channel.
 *
 * @property id Unique identifier, often deterministic for public Spheres (sphere_NAME).
 * @property name Human-readable title of the sphere.
 * @property scope Scoping level: PUBLIC (Open Sphere), PRIVATE (Family/Channel), or LOCAL (Device).
 * @property isArchived True if the sphere hasn't resonated in 30 days.
 * @property partitionThreshold Threshold for member partitioning to maintain resonance efficiency.
 * @property memberSections Partitioned buckets of members for large-scale Spheres (>500 members).
 */
@Serializable
data class Sphere(
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
    val isPinned: Boolean = false, // For priority family/home spheres
    val vaultTimestamp: Long? = null,
    val schedules: List<SphereEvent> = emptyList(),
    val partitionThreshold: Int = 100,
    val memberSections: Map<String, Set<String>> = emptyMap(),
    val connections: List<RoomConnection> = emptyList(), // Keeping RoomConnection for now or rename to SphereConnection later if exists
    val templateId: String? = null,
    val ownerId: String? = null,
    val userRoles: Map<String, String> = emptyMap(), // Map of userId to role name
    val anchoredPublicSphereId: String? = null, // ID of the public sphere this private one is anchored to
    val encryptedGroupKey: String? = null, // Group shared secret for multi-user private resonance
) {
    /**
     * Resolves all unique member IDs across flat list and partitioned sections.
     * Ensuring a unified view for identity resolution.
     */
    val allMemberIds: Set<String> get() = memberIds + memberSections.values.asSequence().flatten().toSet()

    /** True if this is the root collective sphere that all Sources are pre-joined to. */
    val isDefaultSphere: Boolean get() = id == ID_GLOBAL

    /** True if this group is anchored to a public resonance context. */
    val isAnchored: Boolean get() = anchoredPublicSphereId != null

    companion object {
        // --- Scoping Levels (Resonance Aliases) ---
        const val SCOPE_PUBLIC = 0
        const val SCOPE_PRIVATE = 1
        const val SCOPE_LOCAL = 2

        const val TYPE_OPEN_SPHERE = SCOPE_PUBLIC
        const val TYPE_PRIVATE_CHANNEL = SCOPE_PRIVATE

        // --- Root Identity Identifiers ---
        const val ID_GLOBAL = "global_sphere"
        const val ID_SILENCE = "silence_sphere"

        /** The duration after which an inactive sphere is auto-archived. */
        const val ARCHIVE_THRESHOLD_MS = 30L * 24 * 60 * 60 * 1000 // 30 Days

        /**
         * The absolute maximum number of members in a flat set before partitioning is required.
         */
        const val MAX_MEMBERS_PER_SECTION = 500

        /**
         * Generates a deterministic ID for public spheres or a UUID for private ones.
         */
        fun generateId(name: String, scope: Int, parentGroup: Sphere? = null): String {
            val normalized = name.uppercase().trim()
            return if (scope == SCOPE_PUBLIC) {
                if (normalized == "GLOBAL" || normalized == "OPEN MESH") {
                    ID_GLOBAL
                } else {
                    // Recursive path generation for Child Spheres
                    if (parentGroup?.scope == SCOPE_PUBLIC && parentGroup.id != ID_GLOBAL) {
                        "${parentGroup.id}_${normalized.replace(" ", "_")}"
                    } else {
                        "sphere_${normalized.replace(" ", "_")}"
                    }
                }
            } else {
                // Private Channels are always unique anchors
                java.util.UUID.randomUUID().toString()
            }
        }
    }
}
