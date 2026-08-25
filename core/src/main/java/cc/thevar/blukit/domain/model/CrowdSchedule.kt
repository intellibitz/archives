/**
 * BLUKIT CORE DOMAIN: CROWD SCHEDULE
 *
 * Defines the operational hours for a resonant frequency.
 * Used for Academic Rituals (Smart Reminders) and automatic awakening of contexts.
 */
package cc.thevar.blukit.domain.model

import kotlinx.serialization.Serializable

/**
 * Temporal metadata for a Crowd or Chain.
 * 
 * @property title The ritual name (e.g., "Lecture: Systems Arch").
 * @property dayOfWeek ISO day constant (1=Mon to 7=Sun).
 * @property startHour The hour the frequency awakens.
 * @property endHour The hour the resonance decays.
 * @property reminderLeadTimeMs How early to trigger a Smart Reminder pulse.
 */
@Serializable
data class CrowdSchedule(
    val title: String? = null,
    val dayOfWeek: Int, // 1 (Mon) to 7 (Sun)
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val locationTag: String? = null,
    val reminderLeadTimeMs: Long? = null,
)
