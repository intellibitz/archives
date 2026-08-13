package cc.thevar.blukit.domain.power

data class SupremePowerReport(
    val userCount: Int = 0,
    val connectedPeerCount: Int = 0,
    val totalMessages: Int = 0,
    val meshHealth: Float = 0f, // 0.0 to 1.0
    val trafficDensity: String = "IDLE",
    val aiInsight: String = "Feeling for the vibes...",
    val signalStability: String = "STABLE"
)
