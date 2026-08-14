package cc.thevar.blukit.domain.power

data class SupremePowerReport(
    val userCount: Int = 0,
    val connectedLinksCount: Int = 0,
    val totalMessages: Int = 0,
    val harmony: Float = 0f, // 0.0 to 1.0
    val aiInsight: String = "Feeling for the vibes...",
    val currentBreeze: String? = null,
)
