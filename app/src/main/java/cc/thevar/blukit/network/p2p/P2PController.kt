package cc.thevar.blukit.network.p2p

import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.domain.model.MessagePayload
import cc.thevar.blukit.domain.model.ConnectionStatus
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface P2PController {
    val scannedDevices: StateFlow<List<P2PDevice>>
    val isConnected: StateFlow<Boolean>
    val errors: SharedFlow<String>
    val messages: StateFlow<List<MessagePayload>>

    fun startDiscovery()
    fun stopDiscovery()
    
    fun startAdvertising()
    fun stopAdvertising()

    fun connectToDevice(device: P2PDevice): SharedFlow<ConnectionStatus>
    suspend fun sendMessage(content: String, receiverId: String? = null): MessagePayload?

    fun closeConnection()
    fun release()
}
