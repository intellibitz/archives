package cc.thevar.blukit.data.networking

import cc.thevar.blukit.data.bluetooth.BluetoothDeviceDomain
import cc.thevar.blukit.data.bluetooth.BluetoothPayload
import cc.thevar.blukit.data.bluetooth.ConnectionResult
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface P2PController {
    val scannedDevices: StateFlow<List<BluetoothDeviceDomain>>
    val isConnected: StateFlow<Boolean>
    val errors: SharedFlow<String>
    val messages: StateFlow<List<BluetoothPayload>>

    fun startDiscovery()
    fun stopDiscovery()
    
    fun startAdvertising()
    fun stopAdvertising()

    fun connectToDevice(device: BluetoothDeviceDomain): SharedFlow<ConnectionResult>
    suspend fun sendMessage(content: String, receiverId: String? = null): BluetoothPayload?

    fun closeConnection()
    fun release()
}
