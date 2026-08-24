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
    val schedules: List<AirSchedule> = emptyList()
) {
    companion object {
        const val SCOPE_PUBLIC = 0
        const val SCOPE_PRIVATE = 1
        const val SCOPE_LOCAL = 2

        const val ID_AIR = "air_tie"
        const val ID_SILENCE = "silence_tie"

        const val ARCHIVE_THRESHOLD_MS = 30L * 24 * 60 * 60 * 1000 // 30 Days

        fun generateId(name: String, scope: Int, parentGroup: VibeGroup? = null): String {
            val normalized = name.uppercase().trim()
            return if (scope == SCOPE_PUBLIC) {
                if (normalized == "AIR" || normalized == "THE AIR") {
                    ID_AIR
                } else {
                    if (parentGroup?.scope == SCOPE_PUBLIC && parentGroup.id != ID_AIR) {
                        "${parentGroup.id}_${normalized.replace(" ", "_")}"
                    } else {
                        "air_${normalized.replace(" ", "_")}"
                    }
                }
            } else {
                java.util.UUID.randomUUID().toString()
            }
        }
    }
}
