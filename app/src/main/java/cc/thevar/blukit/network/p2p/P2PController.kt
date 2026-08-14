package cc.thevar.blukit.network.p2p

import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.domain.model.MessagePayload
import cc.thevar.blukit.domain.model.ConnectionStatus
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * P2P Controller interface — defines the contract for device discovery, 
 * connection management, and encrypted messaging via Nearby Connections.
 */
interface P2PController {
    val scannedDevices: StateFlow<List<P2PDevice>>
    val isConnected: StateFlow<Boolean>
    val connectedLinks: StateFlow<Set<String>>
    val incomingLinkRequests: StateFlow<Set<P2PDevice>>
    val isDiscovering: StateFlow<Boolean>
    val isAdvertising: StateFlow<Boolean>
    val errors: StateFlow<String>
    val messages: StateFlow<List<MessagePayload>>

    fun startDiscovery()
    fun stopDiscovery()
    
    fun startAdvertising()
    fun stopAdvertising()

    /**
     * Initiate connection to a discovered device.
     * Returns a flow that emits ConnectionStatus updates throughout the 
     * connection lifecycle (Connecting → Connected | Error).
     */
    fun connectToDevice(device: P2PDevice): SharedFlow<ConnectionStatus>
    
    fun requestLink(device: P2PDevice)
    fun acceptLink(device: P2PDevice)
    fun denyLink(device: P2PDevice)

    suspend fun sendMessage(content: String, receiverId: String? = null): MessagePayload?

    suspend fun broadcastMessage(content: String): MessagePayload?

    fun closeConnection()
    fun release()
}
