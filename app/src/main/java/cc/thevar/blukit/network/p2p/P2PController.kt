/**
 * BLUKIT NETWORK: P2P CONTROLLER
 *
 * The fundamental contract for all peer-to-peer radio engines in the mesh.
 * Defines the operational primitives for discovery, advertising, and secure pulse propagation.
 */
package cc.thevar.blukit.network.p2p

import cc.thevar.blukit.domain.model.ConnectionStatus
import cc.thevar.blukit.domain.model.MeshMessage
import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.domain.model.MeshRoom
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Categorized P2P Errors for ergonomic UI feedback and tactical retry logic.
 */
sealed class P2PError(val message: String) {
    /** Failure during the discovery scan. Usually recoverable by restarting the radio. */
    class DiscoveryError(msg: String) : P2PError(msg)
    /** Failure during advertising. May require a brief cooldown period. */
    class AdvertisingError(msg: String) : P2PError(msg)
    /** Physical link failure or handshake rejection. */
    class ConnectionError(msg: String) : P2PError(msg)
    /** Failure in ECDH key derivation or AES-GCM decryption. */
    class EncryptionError(msg: String) : P2PError(msg)
}

/**
 * P2P Controller interface — orchestrates device discovery and encrypted messaging.
 * 
 * Implementations (Nearby, BLE, Composite) must ensure that all data is hardware-encrypted
 * and that no internet connection is required for any operation.
 */
interface P2PController {
    /** Real-time list of detected Event Personas in range. */
    val scannedDevices: StateFlow<List<P2PDevice>>
    /** True if at least one secure radio link is active. */
    val isConnected: StateFlow<Boolean>
    /** Set of persistent identifiers for currently connected groups. */
    val connectedGroups: StateFlow<Set<String>>
    /** Pending incoming requests to form a private Room. */
    val incomingRadioRequests: StateFlow<Set<P2PDevice>>
    /** Pending outgoing requests waiting for peer acceptance. */
    val outgoingRadioRequests: StateFlow<Set<P2PDevice>>
    /** Active status of the discovery radio. */
    val isDiscovering: StateFlow<Boolean>
    /** Active status of the advertising radio. */
    val isAdvertising: StateFlow<Boolean>
    /** The last encountered radio or encryption error. */
    val errors: StateFlow<P2PError?>
    /** The unified life stream of messages extracted from the mesh. */
    val messages: StateFlow<List<MeshMessage>>
    /** Emissions for newly discovered Rooms (public frequencies). */
    val discoveredRooms: SharedFlow<MeshRoom>
    /** Progress of the differential history sync (0.0 to 1.0). */
    val syncProgress: StateFlow<Float?>

    /** Activates the discovery radio to search for nearby Event Personas. */
    fun startDiscovery()
    /** Deactivates discovery to preserve hardware energy. */
    fun stopDiscovery()
    
    /** Begins projecting the user's Persona into the local air. */
    fun startAdvertising()
    /** Stops the Persona projection. */
    fun stopAdvertising()

    /**
     * Initiate connection to a discovered device.
     * @return A flow of ConnectionStatus (Connecting → Connected | Error).
     */
    fun connectToDevice(device: P2PDevice): SharedFlow<ConnectionStatus>
    
    /** Requests a private radio group (Room formation) with a peer. */
    fun requestRadio(device: P2PDevice)
    /** Checks if a low-level endpoint is currently connected. */
    fun isNearbyConnected(endpointId: String): Boolean
    /** Accepts an incoming request to form a private Room. */
    fun acceptRadio(device: P2PDevice)
    /** Rejects an incoming request. */
    fun denyRadio(device: P2PDevice)

    /**
     * Joins a discoverable room to enable participation (sending messages).
     * All users are pre-joined to the default room.
     */
    fun joinRoom(groupId: String)

    /**
     * Sends an encrypted message to a specific peer or the local room.
     * @return The generated MeshMessage metadata.
     */
    suspend fun sendMessage(content: String, receiverId: String? = null, messageScope: Int = MeshMessage.MESSAGE_SHOUT, messageId: String? = null, groupId: String? = null, groupName: String? = null, type: Int = MeshMessage.TYPE_TEXT): MeshMessage?

    /**
     * Broadcasts a message to all available peers in the mesh.
     */
    suspend fun broadcastMessage(content: String, messageScope: Int = MeshMessage.MESSAGE_SHOUT, messageId: String? = null, groupId: String? = null, groupName: String? = null, type: Int = MeshMessage.TYPE_TEXT): MeshMessage?

    /** Sends a message scoped to a specific MeshRoom. */
    suspend fun sendGroupMessage(content: String, groupId: String): MeshMessage?

    /** Propagates a LWW-versioned note update to a Group. */
    suspend fun sendNoteUpdate(groupId: String, content: String, messageId: String?, version: Int): MeshMessage?

    /** Shares a local file (Image or Memory) over high-speed WiFi radio. */
    suspend fun sendFile(fileUri: android.net.Uri, receiverId: String? = null, messageScope: Int = MeshMessage.MESSAGE_SHOUT, groupId: String? = null, groupName: String? = null): MeshMessage?

    /** Broadcasts a change in the user's Persona (Nickname/Emoji) across the mesh. */
    suspend fun broadcastIdentityUpdate(oldName: String): MeshMessage

    /** Initializes a new MeshRoom context. */
    fun startGroupRoom(name: String, members: Set<String>, type: Int = MeshRoom.SCOPE_PUBLIC, groupId: String? = null, parentId: String? = null): String

    /** Updates the membership of a private Room. */
    fun updateGroupMembers(groupId: String, memberIds: Set<String>)

    /** Dynamically shifts the scoping of a frequency (e.g., WHISPER to SHOUT). */
    fun updateGroupScope(groupId: String, scope: Int)

    /** Triggers a differential sync to bridge missing history with a peer. */
    fun initiateHistorySync(endpointId: String, sinceTimestamp: Long? = null)

    /** Sever all active radio links immediately. */
    fun closeConnection()
    /** Full system cleanup for disposal. */
    fun release()
}
