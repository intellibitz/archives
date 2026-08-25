/**
 * BLUKIT CORE DOMAIN: SUPREME POWER REPORT
 *
 * A global summary of the mesh's health and local activity.
 * Surfaces metrics from the Mesh Relay and Discovery Radar.
 */
package cc.thevar.blukit.domain.power

/**
 * Status report for global mesh energy.
 *
 * @property userCount Number of active peers currently in radio orbit.
 * @property harmony A normalized metric (0-1) representing mesh stability and sync density.
 * @property aiInsight Contextual advice generated from local pulse history.
 * @property lowPowerMode True if hardware radio throttling is active to preserve battery.
 */
data class SupremePowerReport(
    val userCount: Int = 0,
    val connectedTiesCount: Int = 0,
    val totalMessages: Int = 0,
    val harmony: Float = 0f, // 0.0 to 1.0
    val aiInsight: String = "SEARCHING THE AIR...",
    val currentBreeze: String? = null,
    val lowPowerMode: Boolean = false,
    val suggestedAirs: List<String> = emptyList(),
    val lastLocation: String? = null,
)
