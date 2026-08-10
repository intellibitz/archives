package cc.thevar.blukit.ui.discovery

import cc.thevar.blukit.data.bluetooth.BluetoothDeviceDomain
import cc.thevar.blukit.data.bluetooth.BluetoothPayload

data class BluetoothUiState(
    val scannedDevices: List<BluetoothDeviceDomain> = emptyList(),
    val pairedDevices: List<BluetoothDeviceDomain> = emptyList(),
    val isConnected: Boolean = false,
    val isConnecting: Boolean = false,
    val isBluetoothEnabled: Boolean = true,
    val isLocationEnabled: Boolean = true,
    val errorMessage: String? = null,
    val messages: List<BluetoothPayload> = emptyList()
)
