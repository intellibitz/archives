package cc.thevar.blukit.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class VibeGroup(
    val id: String,
    val name: String,
    val memberIds: Set<String> = emptySet(),
    val scope: Int = SCOPE_PUBLIC,
    val lastVibeTimestamp: Long = System.currentTimeMillis(),
    val isPersistent: Boolean = true,
    val isArchived: Boolean = false,
    val parentId: String? = null,
    val isMeta: Boolean = true,
    val pinnedVibeIds: Set<String> = emptySet(),
    val projectionEmoji: String? = null,
    val isVaulted: Boolean = false,
    val isSeniorVault: Boolean = false,
    val vaultTimestamp: Long? = null,
    val schedules: List<CrowdSchedule> = emptyList(),
    val partitionThreshold: Int = 100,
    val memberSections: Map<String, Set<String>> = emptyMap(),
    val connections: List<CrowdConnection> = emptyList(),
    val templateId: String? = null,
    val userRoles: Map<String, String> = emptyMap() // Map of userId to role name
) {
    /**
     * Resolves all unique member IDs across flat list and sections.
     */
    val allMemberIds: Set<String> get() = memberIds + memberSections.values.flatten().toSet()

    companion object {
        const val SCOPE_PUBLIC = 0
        const val SCOPE_PRIVATE = 1
        const val SCOPE_LOCAL = 2

        const val ID_CROWD = "crowd_chain"
        const val ID_SILENCE = "silence_chain"

        const val ARCHIVE_THRESHOLD_MS = 30L * 24 * 60 * 60 * 1000 // 30 Days

        /**
         * The absolute maximum number of members in a flat set before partitioning is required.
         */
        const val MAX_MEMBERS_PER_SECTION = 500

        /**
         * The default threshold for triggering a new member partition.
         */
        const val DEFAULT_PARTITION_THRESHOLD = 100

        fun generateId(name: String, scope: Int, parentGroup: VibeGroup? = null): String {
            val normalized = name.uppercase().trim()
            return if (scope == SCOPE_PUBLIC) {
                if (normalized == "CROWD" || normalized == "THE CROWD") {
                    ID_CROWD
                } else {
                    if (parentGroup?.scope == SCOPE_PUBLIC && parentGroup.id != ID_CROWD) {
                        "${parentGroup.id}_${normalized.replace(" ", "_")}"
                    } else {
                        "crowd_${normalized.replace(" ", "_")}"
                    }
                }
            } else {
                java.util.UUID.randomUUID().toString()
            }
        }
    }
}
