package cc.thevar.blukit.domain.model

data class P2PDevice(
    val id: String,
    val name: String?,
    val emoji: String = "👤",
    val signalStrength: Int = 0, // RSSI value in dBm (-100 to 0)
    val isConnected: Boolean = false,
    val isConnecting: Boolean = false,
    val isLinkPending: Boolean = false
) {
    /**
     * Returns a human-readable proximity label based on signal strength.
     * - Strong signal (>-50 dBm): "Very Close"
     * - Medium signal (-50 to -80 dBm): "Close" / "Moderate"  
     * - Weak signal (<-80 dBm): "Far"
     */
    val proximityLabel: String
        get() = when {
            signalStrength > -40 -> "Very Close"
            signalStrength > -60 -> "Close"
            signalStrength > -75 -> "Moderate" 
            signalStrength > -85 -> "Far"
            else -> "Very Far"
        }

    /**
     * Returns a normalized proximity factor (0.0 to 1.0) for radar positioning.
     */
    val proximityFactor: Float
        get() = kotlin.math.max(0f, kotlin.math.min(1f, 
            (signalStrength + 90f) / 50f
        ))
}
