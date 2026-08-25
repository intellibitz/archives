package cc.thevar.blukit.domain.power

data class SupremePowerReport(
    val userCount: Int = 0,
    val connectedTiesCount: Int = 0,
    val totalMessages: Int = 0,
    val harmony: Float = 0f, // 0.0 to 1.0
    val aiInsight: String = "SEARCHING THE AIR...",
    val currentBreeze: String? = null,
    val lowPowerMode: Boolean = false,
    val suggestedAirs: List<String> = emptyList(),
    val lastLocation: String? = null
)
