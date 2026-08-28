package cc.thevar.blukit.ui.viewmodels

import cc.thevar.blukit.domain.model.MessagePayload
import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.domain.model.Resonance

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
    val permissionsGranted: Boolean = false,
)

/**
 * Event Activity: Current network actions and errors.
 */
data class EventActivity(
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
    val pulsedPeers: Set<String> = emptySet(),
    val blockedUsers: Set<String> = emptySet(),
    val incomingRadioRequests: Set<P2PDevice> = emptySet(),
    val outgoingRadioRequests: Set<P2PDevice> = emptySet()
)

/**
 * Pulse Session: Active connections, resonances, and message history.
 */
data class PulseSession(
    val connectionState: RadioConnectionState = RadioConnectionState.Disconnected,
    val connectedTies: Set<String> = emptySet(),
    val messages: List<MessagePayload> = emptyList(),
    val groups: List<Resonance> = emptyList(),
    val archivedGroups: List<Resonance> = emptyList(),
    val syncProgress: Float? = null
)

data class BluetoothUiState(
    val harmony: HardwareHarmony = HardwareHarmony(),
    val activity: EventActivity = EventActivity(),
    val crowd: MeshCrowd = MeshCrowd(),
    val session: PulseSession = PulseSession()
)
