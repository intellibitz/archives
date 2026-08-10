package cc.thevar.blukit.data.bluetooth

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface BluetoothController {
    val scannedDevices: StateFlow<List<BluetoothDeviceDomain>>
    val pairedDevices: StateFlow<List<BluetoothDeviceDomain>>
    val isConnected: StateFlow<Boolean>
    val errors: SharedFlow<String>
    val messages: StateFlow<List<BluetoothPayload>>

    fun startDiscovery()
    fun stopDiscovery()

    fun startBluetoothServer(): SharedFlow<ConnectionResult>
    fun connectToDevice(device: BluetoothDeviceDomain): SharedFlow<ConnectionResult>

    suspend fun trySendMessage(message: String): BluetoothPayload?

    fun closeConnection()
    fun release()
}
