package cc.thevar.blukit.ui.viewmodels

import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.domain.model.MessagePayload

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

data class BluetoothUiState(
    val scannedDevices: List<P2PDevice> = emptyList(),
    val connectionState: AirConnectionState = AirConnectionState.Disconnected,
    val isDiscovering: Boolean = false,
    val isAdvertising: Boolean = false,
    val connectedLinks: Set<String> = emptySet(),
    val incomingLinkRequests: Set<P2PDevice> = emptySet(),
    val errorMessage: String? = null,
    val messages: List<MessagePayload> = emptyList(),
    val isBluetoothEnabled: Boolean = false,
    val isLocationEnabled: Boolean = false
) {
    // Helper properties for legacy UI compatibility or convenience
    val isConnected: Boolean get() = connectionState is AirConnectionState.Connected
    val isConnecting: Boolean get() = connectionState is AirConnectionState.Connecting
    val connectedVibe: P2PDevice? get() = (connectionState as? AirConnectionState.Connected)?.device
}
