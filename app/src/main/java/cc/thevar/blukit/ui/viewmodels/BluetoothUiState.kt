package cc.thevar.blukit.ui.viewmodels

import cc.thevar.blukit.domain.model.Message
import cc.thevar.blukit.domain.model.Source
import cc.thevar.blukit.domain.model.Group

/**
 * Represents the UI state for connection within the field.
 */
sealed interface RadioConnectionState {
    data object Disconnected : RadioConnectionState
    data object Scanning : RadioConnectionState
    data object Connecting : RadioConnectionState
    data class Connected(val device: Source) : RadioConnectionState
    data class Error(val message: String) : RadioConnectionState
}

/**
 * Hardware status for connection radios.
 */
data class HardwareHarmony(
    val isBluetoothEnabled: Boolean = false,
    val isLocationEnabled: Boolean = false,
    val isWifiEnabled: Boolean = false,
    val permissionsGranted: Boolean = false,
)

/**
 * Active connection operations and errors.
 */
data class ConnectionActivity(
    val isDiscovering: Boolean = false,
    val isAdvertising: Boolean = false,
    val energyIntensity: Float = 0f,
    val uiError: cc.thevar.blukit.ui.UiError? = null
)

/**
 * Nearby Sources and social relationships.
 */
data class ConnectionPeers(
    val scannedDevices: List<Source> = emptyList(),
    val selectedDevices: Set<String> = emptySet(),
    val pulsedPeers: Set<String> = emptySet(),
    val blockedUsers: Set<String> = emptySet(),
    val incomingRadioRequests: Set<Source> = emptySet(),
    val outgoingRadioRequests: Set<Source> = emptySet()
)

/**
 * Active connection session, Groups, and The Ledger.
 */
data class ConnectionSession(
    val connectionState: RadioConnectionState = RadioConnectionState.Disconnected,
    val connectedTies: Set<String> = emptySet(),
    val groups: List<Group> = emptyList(),
    val archivedGroups: List<Group> = emptyList(),
    val syncProgress: Float? = null
)

data class ConnectionUiState(
    val harmony: HardwareHarmony = HardwareHarmony(),
    val activity: ConnectionActivity = ConnectionActivity(),
    val crowd: ConnectionPeers = ConnectionPeers(),
    val session: ConnectionSession = ConnectionSession()
)
