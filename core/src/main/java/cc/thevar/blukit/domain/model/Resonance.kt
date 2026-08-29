/**
 * BLUKIT CORE DOMAIN: RESONANCE
 *
 * A shared container for pulses. Represents both public Crowds and private Chains.
 * Orchestrates group scaling and partition strategies for decentralized efficiency.
 */
package cc.thevar.blukit.domain.model

import kotlinx.serialization.Serializable

/**
 * Data model for a Crowd or Chain context.
 *
 * @property id Unique identifier, often deterministic for public Crowds (crowd_NAME).
 * @property name Human-readable title of the frequency.
 * @property scope Scoping level: PUBLIC (Crowd), PRIVATE (Chain), or LOCAL (Device).
 * @property isArchived True if the frequency has hasn't pulsed in 30 days (Sunk Pulse).
 * @property partitionThreshold Threshold for member partitioning to maintain radio efficiency.
 * @property memberSections Partitioned buckets of members for large-scale Crowds (>500 members).
 */
@Serializable
data class Resonance(
    val id: String,
    val name: String,
    val memberIds: Set<String> = emptySet(),
    val scope: Int = SCOPE_PUBLIC,
    val lastPulseTimestamp: Long = System.currentTimeMillis(),
    val isPersistent: Boolean = true,
    val isArchived: Boolean = false,
    val parentId: String? = null,
    val isMeta: Boolean = true,
    val pinnedPulseIds: Set<String> = emptySet(),
    val projectionEmoji: String? = null,
    val isVaulted: Boolean = false,
    val isSeniorVault: Boolean = false,
    val vaultTimestamp: Long? = null,
    val schedules: List<CrowdSchedule> = emptyList(),
    val partitionThreshold: Int = 100,
    val memberSections: Map<String, Set<String>> = emptyMap(),
    val connections: List<CrowdConnection> = emptyList(),
    val templateId: String? = null,
    val ownerId: String? = null,
    val userRoles: Map<String, String> = emptyMap(), // Map of userId to role name
) {
    /**
     * Resolves all unique member IDs across flat list and partitioned sections.
     * Ensuring a unified view for identity resolution.
     */
    val allMemberIds: Set<String> get() = memberIds + memberSections.values.asSequence().flatten().toSet()

    /** True if this is the root collective crowd that all users are pre-joined to. */
    val isDefaultCrowd: Boolean get() = id == ID_CROWD

    companion object {
        // --- Scoping Levels ---
        const val SCOPE_PUBLIC = 0
        const val SCOPE_PRIVATE = 1
        const val SCOPE_LOCAL = 2

        // --- Root Identity Identifiers ---
        const val ID_CROWD = "crowd_chain"
        const val ID_SILENCE = "silence_chain"

        /** The duration after which an inactive frequency is auto-archived (Sunk Pulse). */
        const val ARCHIVE_THRESHOLD_MS = 30L * 24 * 60 * 60 * 1000 // 30 Days

        /**
         * The absolute maximum number of members in a flat set before partitioning is required.
         * Optimization: prevents O(N) lookup issues during rapid radio broadcasts.
         */
        const val MAX_MEMBERS_PER_SECTION = 500

        /**
         * Generates a deterministic ID for public frequencies or a UUID for private ones.
         * Deterministic naming (crowd_NAME) allows cross-device resonance discovery.
         */
        fun generateId(name: String, scope: Int, parentGroup: Resonance? = null): String {
            val normalized = name.uppercase().trim()
            return if (scope == SCOPE_PUBLIC) {
                if (normalized == "CROWD" || normalized == "THE CROWD") {
                    ID_CROWD
                } else {
                    // Recursive path generation for Child Crowds
                    if (parentGroup?.scope == SCOPE_PUBLIC && parentGroup.id != ID_CROWD) {
                        "${parentGroup.id}_${normalized.replace(" ", "_")}"
                    } else {
                        "crowd_${normalized.replace(" ", "_")}"
                    }
                }
            } else {
                // Private Chains are always unique anchors
                java.util.UUID.randomUUID().toString()
            }
        }
    }
}
