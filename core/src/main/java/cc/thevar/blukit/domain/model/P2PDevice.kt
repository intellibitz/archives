/**
 * BLUKIT CORE DOMAIN: P2P DEVICE
 *
 * The atomic identity of a peer in the mesh.
 * Represents a physical hardware anchor within the Crowd Field.
 */
package cc.thevar.blukit.domain.model

/**
 * Data representation of a discovered or connected peer.
 *
 * @property id The unique hardware or session identifier for this device.
 * @property name The display name set by the user (Event Persona).
 * @property emoji The visual identity projecting this device on the Discovery Radar.
 * @property persistentId A stable identifier for cross-session recognition (e.g., from a Tie).
 * @property signalStrength The raw RSSI value in dBm, used for spatial positioning.
 * @property isConnected True if a secure radio link is currently active.
 * @property medium The primary radio technology used for this connection.
 */
data class P2PDevice(
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
    val statusLabel: String? = null, // Temporary status or AI trend projection
    val avatarPath: String? = null, // Local path to the mesh profile picture
) {
    /**
     * The physical radio medium used for the mesh connection.
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
     * 0.0 represents the center (Self/Identity Anchor), 1.0 represents the edge of discovery.
     */
    val proximityFactor: Float
        get() =
            // Normalization logic: maps RSSI -90 to -40 range to 0.0 to 1.0
            kotlin.math.max(
                0f,
                kotlin.math.min(
                    1f,
                    (signalStrength + 90f) / 50f,
                ),
            )
}
