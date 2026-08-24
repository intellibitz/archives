package cc.thevar.blukit.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class CrowdSchedule(
    val title: String? = null,
    val dayOfWeek: Int, // 1 (Mon) to 7 (Sun)
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val locationTag: String? = null,
    val reminderLeadTimeMs: Long? = null
)
