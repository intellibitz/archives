package cc.thevar.blukit.ui.viewmodels

import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.domain.model.MessagePayload
import cc.thevar.blukit.domain.model.VibeGroup

/**
 * Represents the UI state for connectivity within The Air.
 * Uses a sealed interface to model a robust state machine for the Vibing Air lifecycle.
 */
sealed interface AirConnectionState {
    data object Disconnected : AirConnectionState
    data object Scanning : AirConnectionState
    data object Connecting : AirConnectionState
    data class Connected(val device: P2PDevice) : AirConnectionState
    data class Error(val message: String) : AirConnectionState
}

/**
 * Hardware Harmony: System and Radio availability.
 */
data class HardwareHarmony(
    val isBluetoothEnabled: Boolean = false,
    val isLocationEnabled: Boolean = false,
    val isWifiEnabled: Boolean = false,
    val permissionsGranted: Boolean = false
)

/**
 * Air Activity: Current network actions and errors.
 */
data class AirActivity(
    val isDiscovering: Boolean = false,
    val isAdvertising: Boolean = false,
    val uiError: cc.thevar.blukit.ui.UiError? = null
)

/**
 * Mesh Crowd: Nearby devices and peer relationships.
 */
data class MeshCrowd(
    val scannedDevices: List<P2PDevice> = emptyList(),
    val selectedDevices: Set<String> = emptySet(),
    val vibedPeers: Set<String> = emptySet(),
    val blockedUsers: Set<String> = emptySet(),
    val incomingLinkRequests: Set<P2PDevice> = emptySet()
)

/**
 * Resonance Session: Active connections, groups, and message history.
 */
data class ResonanceSession(
    val connectionState: AirConnectionState = AirConnectionState.Disconnected,
    val connectedLinks: Set<String> = emptySet(),
    val messages: List<MessagePayload> = emptyList(),
    val groups: List<VibeGroup> = emptyList()
)

data class BluetoothUiState(
    val harmony: HardwareHarmony = HardwareHarmony(),
    val activity: AirActivity = AirActivity(),
    val crowd: MeshCrowd = MeshCrowd(),
    val session: ResonanceSession = ResonanceSession()
) {
    // Helper properties for backward compatibility or convenience in UI logic
    val isConnected: Boolean get() = session.connectionState is AirConnectionState.Connected
    val isConnecting: Boolean get() = session.connectionState is AirConnectionState.Connecting
    val connectedVibe: P2PDevice? get() = (session.connectionState as? AirConnectionState.Connected)?.device
}
