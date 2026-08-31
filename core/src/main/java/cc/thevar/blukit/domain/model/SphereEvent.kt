/**
 * BLUKIT CORE DOMAIN: SPHERE EVENT
 *
 * Defines the operational hours or scheduled highlights for a Sphere.
 */
package cc.thevar.blukit.domain.model

import kotlinx.serialization.Serializable

/**
 * Temporal metadata for a Sphere.
 * 
 * @property title The event name.
 * @property dayOfWeek ISO day constant (1=Mon to 7=Sun).
 * @property startHour The hour the event begins.
 */
@Serializable
data class SphereEvent(
    val title: String? = null,
    val dayOfWeek: Int, // 1 (Mon) to 7 (Sun)
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val locationTag: String? = null,
    val reminderLeadTimeMs: Long? = null,
)
