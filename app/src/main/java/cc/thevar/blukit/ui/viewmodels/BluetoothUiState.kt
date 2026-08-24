package cc.thevar.blukit.ui.viewmodels

import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.domain.model.MessagePayload
import cc.thevar.blukit.domain.model.VibeGroup

/**
 * Represents the UI state for connectivity within The mesh.
 * Uses a sealed interface to model a robust state machine for the Radio lifecycle.
 */
sealed interface RadioConnectionState {
    data object Disconnected : RadioConnectionState
    data object Scanning : RadioConnectionState
    data object Connecting : RadioConnectionState
    data class Connected(val device: P2PDevice) : RadioConnectionState
    data class Error(val message: String) : RadioConnectionState
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
 * Host Activity: Current network actions and errors.
 */
data class HostActivity(
    val isDiscovering: Boolean = false,
    val isAdvertising: Boolean = false,
    val energyIntensity: Float = 0f,
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
    val incomingRadioRequests: Set<P2PDevice> = emptySet(),
    val outgoingRadioRequests: Set<P2PDevice> = emptySet()
)

/**
 * Vibe Session: Active connections, groups, and message history.
 */
data class VibeSession(
    val connectionState: RadioConnectionState = RadioConnectionState.Disconnected,
    val connectedRadios: Set<String> = emptySet(),
    val messages: List<MessagePayload> = emptyList(),
    val groups: List<VibeGroup> = emptyList(),
    val archivedGroups: List<VibeGroup> = emptyList(),
    val syncProgress: Float? = null
)

data class BluetoothUiState(
    val harmony: HardwareHarmony = HardwareHarmony(),
    val activity: HostActivity = HostActivity(),
    val crowd: MeshCrowd = MeshCrowd(),
    val session: VibeSession = VibeSession()
) {
    // Helper properties for backward compatibility or convenience in UI logic
    val isConnected: Boolean get() = session.connectionState is RadioConnectionState.Connected
    val isConnecting: Boolean get() = session.connectionState is RadioConnectionState.Connecting
    val connectedVibe: P2PDevice? get() = (session.connectionState as? RadioConnectionState.Connected)?.device
}
