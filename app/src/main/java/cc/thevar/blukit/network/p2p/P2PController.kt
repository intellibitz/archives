package cc.thevar.blukit.network.p2p

import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.domain.model.MessagePayload
import cc.thevar.blukit.domain.model.ConnectionStatus
import cc.thevar.blukit.domain.model.VibeGroup
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Categorized P2P Errors for better UI feedback and retry logic.
 */
sealed class P2PError(val message: String, val isTransient: Boolean) {
    class DiscoveryError(msg: String) : P2PError(msg, true)
    class AdvertisingError(msg: String) : P2PError(msg, true)
    class ConnectionError(msg: String) : P2PError(msg, true)
    class EncryptionError(msg: String) : P2PError(msg, false)
    class GenericError(msg: String) : P2PError(msg, false)
}

/**
 * P2P Controller interface — defines the contract for device discovery, 
 * connection management, and encrypted messaging via Nearby Connections.
 */
interface P2PController {
    val scannedDevices: StateFlow<List<P2PDevice>>
    val isConnected: StateFlow<Boolean>
    val connectedLinks: StateFlow<Set<String>>
    val incomingLinkRequests: StateFlow<Set<P2PDevice>>
    val outgoingLinkRequests: StateFlow<Set<P2PDevice>>
    val isDiscovering: StateFlow<Boolean>
    val isAdvertising: StateFlow<Boolean>
    val errors: StateFlow<P2PError?>
    val messages: StateFlow<List<MessagePayload>>
    val discoveredAirs: SharedFlow<VibeGroup>

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
    fun isNearbyConnected(endpointId: String): Boolean
    fun acceptLink(device: P2PDevice)
    fun denyLink(device: P2PDevice)

    suspend fun sendMessage(content: String, receiverId: String? = null, vibeType: Int = MessagePayload.VIBE_SHOUT, messageId: String? = null, groupId: String? = null, groupName: String? = null): MessagePayload?

    suspend fun broadcastMessage(content: String, vibeType: Int = MessagePayload.VIBE_SHOUT, messageId: String? = null, groupId: String? = null, groupName: String? = null): MessagePayload?

    suspend fun sendGroupMessage(content: String, groupId: String): MessagePayload?

    suspend fun sendFile(fileUri: android.net.Uri, receiverId: String? = null, vibeType: Int = MessagePayload.VIBE_SHOUT, groupId: String? = null, groupName: String? = null): MessagePayload?

    suspend fun broadcastIdentityUpdate(oldName: String): MessagePayload?

    fun startGroupVibe(name: String, members: Set<String>, type: Int = VibeGroup.SCOPE_PUBLIC): String

    fun updateGroupMembers(groupId: String, memberIds: Set<String>)

    fun updateGroupScope(groupId: String, scope: Int)

    fun initiateHistorySync(endpointId: String)

    fun closeConnection()
    fun release()
}
