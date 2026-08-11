package cc.thevar.blukit.ui.viewmodels

import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.domain.model.MessagePayload

data class BluetoothUiState(
    val scannedDevices: List<P2PDevice> = emptyList(),
    val isConnected: Boolean = false,
    val isConnecting: Boolean = false,
    val errorMessage: String? = null,
    val messages: List<MessagePayload> = emptyList(),
    val isBluetoothEnabled: Boolean = false,
    val isLocationEnabled: Boolean = false
)
