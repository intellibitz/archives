/**
 * BLUKIT CORE DOMAIN: SOURCE
 *
 * The atomic identity of a peer in the mesh.
 * Represents a physical hardware anchor within the Connection Field.
 */
package cc.thevar.blukit.domain.model

/**
 * Data representation of a discovered or connected Source.
 *
 * @property id The unique hardware or session identifier for this Source.
 * @property name The display name set by the Source (Persona).
 * @property emoji The visual identity projecting this Source on the Nearby Radar.
 * @property persistentId A stable identifier for cross-session recognition.
 * @property signalStrength The raw RSSI value in dBm.
 * @property isConnected True if a secure connection link is currently active.
 * @property medium The primary connection technology used.
 */
data class Source(
    val id: String,
    val name: String?,
    val emoji: String = "👤",
    val persistentId: String? = null,
    val signalStrength: Int = 0, // RSSI value in dBm (-100 to 0)
    val isConnected: Boolean = false,
    val isConnecting: Boolean = false,
    val isGroupPending: Boolean = false,
    val medium: ConnectionMedium = ConnectionMedium.LOCATION,
    val messageCount: Int = 0,
    val isLowPower: Boolean = false,
    val distanceMm: Int = -1, // WiFi RTT distance if available
    val statusLabel: String? = null, // Temporary status or Synthesis projection
    val avatarPath: String? = null, // Local path to the profile picture
) {
    /**
     * The physical connection medium used for the mesh connection.
     */
    enum class ConnectionMedium {
        /** Low-energy, short-range discovery and heartbeats. */
        BLUETOOTH,
        /** High-speed data and media synchronization. */
        WIFI,
        /** Spatial proximity inferred from location providers. */
        LOCATION
    }

    /**
     * Returns a normalized proximity factor (0.0 to 1.0) for radar positioning.
     */
    val proximityFactor: Float
        get() =
            kotlin.math.max(
                0f,
                kotlin.math.min(
                    1f,
                    (signalStrength + 90f) / 50f,
                ),
            )
}
