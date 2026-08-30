package cc.thevar.blukit.ui.viewmodels

import cc.thevar.blukit.domain.model.MeshMessage
import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.domain.model.MeshRoom

/**
 * Represents the UI state for connectivity within the mesh.
 */
sealed interface RadioConnectionState {
    data object Disconnected : RadioConnectionState
    data object Scanning : RadioConnectionState
    data object Connecting : RadioConnectionState
    data class Connected(val device: P2PDevice) : RadioConnectionState
    data class Error(val message: String) : RadioConnectionState
}

/**
 * Hardware status for mesh radios.
 */
data class HardwareHarmony(
    val isBluetoothEnabled: Boolean = false,
    val isLocationEnabled: Boolean = false,
    val isWifiEnabled: Boolean = false,
    val permissionsGranted: Boolean = false,
)

/**
 * Active mesh operations and errors.
 */
data class MeshActivity(
    val isDiscovering: Boolean = false,
    val isAdvertising: Boolean = false,
    val energyIntensity: Float = 0f,
    val uiError: cc.thevar.blukit.ui.UiError? = null
)

/**
 * Nearby peers and social relationships.
 */
data class NearbyPeers(
    val scannedDevices: List<P2PDevice> = emptyList(),
    val selectedDevices: Set<String> = emptySet(),
    val pulsedPeers: Set<String> = emptySet(),
    val blockedUsers: Set<String> = emptySet(),
    val incomingRadioRequests: Set<P2PDevice> = emptySet(),
    val outgoingRadioRequests: Set<P2PDevice> = emptySet()
)

/**
 * Active mesh session, rooms, and history.
 */
data class MeshSession(
    val connectionState: RadioConnectionState = RadioConnectionState.Disconnected,
    val connectedTies: Set<String> = emptySet(),
    val messages: List<MeshMessage> = emptyList(),
    val groups: List<MeshRoom> = emptyList(),
    val archivedGroups: List<MeshRoom> = emptyList(),
    val syncProgress: Float? = null
)

data class BluetoothUiState(
    val harmony: HardwareHarmony = HardwareHarmony(),
    val activity: MeshActivity = MeshActivity(),
    val crowd: NearbyPeers = NearbyPeers(),
    val session: MeshSession = MeshSession()
)
