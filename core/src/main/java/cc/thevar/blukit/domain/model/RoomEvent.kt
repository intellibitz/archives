/**
 * BLUKIT CORE DOMAIN: ROOM EVENT
 *
 * Defines the operational hours or scheduled highlights for a room.
 * Used for Academic Rooms (Class times) and home events.
 */
package cc.thevar.blukit.domain.model

import kotlinx.serialization.Serializable

/**
 * Temporal metadata for a Room or Channel.
 * 
 * @property title The event name (e.g., "Lecture: Computer Science").
 * @property dayOfWeek ISO day constant (1=Mon to 7=Sun).
 * @property startHour The hour the event begins.
 * @property endHour The hour the event concludes.
 * @property reminderLeadTimeMs How early to trigger a reminder.
 */
@Serializable
data class RoomEvent(
    val title: String? = null,
    val dayOfWeek: Int, // 1 (Mon) to 7 (Sun)
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val locationTag: String? = null,
    val reminderLeadTimeMs: Long? = null,
)
