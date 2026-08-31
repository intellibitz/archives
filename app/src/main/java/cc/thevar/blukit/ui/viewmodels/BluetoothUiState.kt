package cc.thevar.blukit.ui.viewmodels

import cc.thevar.blukit.domain.model.Echo
import cc.thevar.blukit.domain.model.Source
import cc.thevar.blukit.domain.model.Sphere

/**
 * Represents the UI state for resonance within the field.
 */
sealed interface RadioConnectionState {
    data object Disconnected : RadioConnectionState
    data object Scanning : RadioConnectionState
    data object Connecting : RadioConnectionState
    data class Connected(val device: Source) : RadioConnectionState
    data class Error(val message: String) : RadioConnectionState
}

/**
 * Hardware status for resonance radios.
 */
data class HardwareHarmony(
    val isBluetoothEnabled: Boolean = false,
    val isLocationEnabled: Boolean = false,
    val isWifiEnabled: Boolean = false,
    val permissionsGranted: Boolean = false,
)

/**
 * Active resonance operations and errors.
 */
data class MeshActivity(
    val isDiscovering: Boolean = false,
    val isAdvertising: Boolean = false,
    val energyIntensity: Float = 0f,
    val uiError: cc.thevar.blukit.ui.UiError? = null
)

/**
 * Nearby Sources and social relationships.
 */
data class NearbyPeers(
    val scannedDevices: List<Source> = emptyList(),
    val selectedDevices: Set<String> = emptySet(),
    val pulsedPeers: Set<String> = emptySet(),
    val blockedUsers: Set<String> = emptySet(),
    val incomingRadioRequests: Set<Source> = emptySet(),
    val outgoingRadioRequests: Set<Source> = emptySet()
)

/**
 * Active resonance session, Spheres, and The Ledger.
 */
data class MeshSession(
    val connectionState: RadioConnectionState = RadioConnectionState.Disconnected,
    val connectedTies: Set<String> = emptySet(),
    val messages: List<Echo> = emptyList(),
    val groups: List<Sphere> = emptyList(),
    val archivedGroups: List<Sphere> = emptyList(),
    val syncProgress: Float? = null
)

data class BluetoothUiState(
    val harmony: HardwareHarmony = HardwareHarmony(),
    val activity: MeshActivity = MeshActivity(),
    val crowd: NearbyPeers = NearbyPeers(),
    val session: MeshSession = MeshSession()
)
