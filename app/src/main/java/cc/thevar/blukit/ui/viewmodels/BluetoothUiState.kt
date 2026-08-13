package cc.thevar.blukit.ui.viewmodels

import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.domain.model.MessagePayload

/**
 * Supreme Senior Expert Implementation:
 * Typed State Machine for Mesh Connections.
 */
sealed interface MeshConnectionState {
    data object Disconnected : MeshConnectionState
    data object Scanning : MeshConnectionState
    data object Connecting : MeshConnectionState
    data class Connected(val device: P2PDevice) : MeshConnectionState
    data class Error(val message: String) : MeshConnectionState
}

data class BluetoothUiState(
    val scannedDevices: List<P2PDevice> = emptyList(),
    val connectionState: MeshConnectionState = MeshConnectionState.Disconnected,
    val isDiscovering: Boolean = false,
    val isAdvertising: Boolean = false,
    val connectedPeers: Set<String> = emptySet(),
    val errorMessage: String? = null,
    val messages: List<MessagePayload> = emptyList(),
    val isBluetoothEnabled: Boolean = false,
    val isLocationEnabled: Boolean = false
) {
    // Helper properties for legacy UI compatibility or convenience
    val isConnected: Boolean get() = connectionState is MeshConnectionState.Connected
    val isConnecting: Boolean get() = connectionState is MeshConnectionState.Connecting
    val connectedPeer: P2PDevice? get() = (connectionState as? MeshConnectionState.Connected)?.device
}
