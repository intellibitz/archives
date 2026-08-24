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
    val parentAirId: String? = null,
    val pinnedVibeIds: Set<String> = emptySet(),
    val projectionEmoji: String? = null,
    val schedules: List<AirSchedule> = emptyList()
) {
    companion object {
        const val SCOPE_PUBLIC = 0
        const val SCOPE_PRIVATE = 1
        const val SCOPE_LOCAL = 2

        const val ID_AIR = "air_tie"
        const val ID_SILENCE = "silence_tie"

        const val ARCHIVE_THRESHOLD_MS = 30L * 24 * 60 * 60 * 1000 // 30 Days
    }
}
