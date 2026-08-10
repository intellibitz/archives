package cc.thevar.blukit.ui.discovery

import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.domain.model.MessagePayload

data class BluetoothUiState(
    val scannedDevices: List<P2PDevice> = emptyList(),
    val isConnected: Boolean = false,
    val isConnecting: Boolean = false,
    val isBluetoothEnabled: Boolean = true,
    val isLocationEnabled: Boolean = true,
    val errorMessage: String? = null,
    val messages: List<MessagePayload> = emptyList()
)
