package cc.thevar.blukit.domain.power

/**
 * A snapshot of the local Connection Field's health and atmosphere.
 */
data class HarmonyReport(
    val userCount: Int = 0,
    val connectedTiesCount: Int = 0,
    val totalMessages: Int = 0,
    val harmony: Float = 0f,
    val synthesis: String = "NEARBY ASSISTANT...",
    val trendLabel: String? = null,
    val currentBreeze: String? = null,
    val lowPowerMode: Boolean = false,
    val suggestedGroups: List<String> = emptyList(),
    val lastLocation: String? = null
)
