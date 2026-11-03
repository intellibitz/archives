/**
 * BLUKIT NETWORK: CONNECTION CONTROLLER
 *
 * The fundamental contract for all peer-to-peer connection engines in the mesh.
 * Defines the operational primitives for Nearby sensing, advertising, and secure Message propagation.
 */
package cc.thevar.blukit.network.p2p

import cc.thevar.blukit.domain.model.ConnectionStatus
import cc.thevar.blukit.domain.model.Message
import cc.thevar.blukit.domain.model.Source
import cc.thevar.blukit.domain.model.Group
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Categorized Connection Errors.
 */
sealed class ConnectionError(val message: String) {
    class NearbyError(msg: String) : ConnectionError(msg)
    class AdvertisingError(msg: String) : ConnectionError(msg)
    class LinkError(msg: String) : ConnectionError(msg)
    class EncryptionError(msg: String) : ConnectionError(msg)
}

/**
 * Connection Controller interface — orchestrates Source sensing and encrypted Messaging.
 */
interface ConnectionController {
    /** Real-time list of detected Sources in range. */
    val scannedDevices: StateFlow<List<Source>>
    /** True if at least one secure connection link is active. */
    val isConnected: StateFlow<Boolean>
    /** Set of persistent identifiers for currently connected Groups. */
    val connectedGroups: StateFlow<Set<String>>
    /** Pending incoming requests to form a private Group. */
    val incomingRadioRequests: StateFlow<Set<Source>>
    /** Pending outgoing requests waiting for peer acceptance. */
    val outgoingRadioRequests: StateFlow<Set<Source>>
    /** Active status of the nearby sensing radio. */
    val isDiscovering: StateFlow<Boolean>
    /** Active status of the advertising radio. */
    val isAdvertising: StateFlow<Boolean>
    /** The last encountered connection or encryption error. */
    val errors: StateFlow<ConnectionError?>
    /** The unified life stream of Messages extracted from the mesh. */
    val messages: StateFlow<List<Message>>
    /** Emissions for newly sensed Groups. */
    val discoveredGroups: SharedFlow<Group>
    /** Progress of the differential history sync. */
    val syncProgress: StateFlow<Float?>

    /** Activates the nearby sensing radio. */
    fun startDiscovery()
    /** Deactivates sensing. */
    fun stopDiscovery()
    
    /** Begins projecting the user's Persona. */
    fun startAdvertising()
    /** Stops the Persona projection. */
    fun stopAdvertising()

    /**
     * Initiate connection with a sensed Source.
     */
    fun connectToDevice(device: Source): SharedFlow<ConnectionStatus>
    
    /** Requests a private Group formation with a peer. */
    fun requestRadio(device: Source)
    /** Checks if a low-level endpoint is currently connected. */
    fun isNearbyConnected(endpointId: String): Boolean
    /** Accepts an incoming request. */
    fun acceptRadio(device: Source)
    /** Rejects an incoming request. */
    fun denyRadio(device: Source)

    /**
     * Enters a discoverable Group.
     */
    fun joinGroup(groupId: String)

    /**
     * Sends an encrypted message.
     * @return The generated Message metadata.
     */
    suspend fun sendMessage(content: String, receiverId: String? = null, messageScope: Int = Message.MESSAGE_SHOUT, messageId: String? = null, groupId: String? = null, groupName: String? = null, type: Int = Message.TYPE_TEXT): Message?

    /**
     * Broadcasts a Message to all available peers.
     */
    suspend fun broadcastMessage(content: String, messageScope: Int = Message.MESSAGE_SHOUT, messageId: String? = null, groupId: String? = null, groupName: String? = null, type: Int = Message.TYPE_TEXT): Message?

    /** Sends a message scoped to a specific Group. */
    suspend fun sendGroupMessage(content: String, groupId: String): Message?

    /** Propagates a LWW-versioned note update. */
    suspend fun sendNoteUpdate(groupId: String, content: String, messageId: String?, version: Int): Message?

    /** Shares a local file over high-speed connection. */
    suspend fun sendFile(fileUri: android.net.Uri, receiverId: String? = null, messageScope: Int = Message.MESSAGE_SHOUT, groupId: String? = null, groupName: String? = null): Message?

    /** Broadcasts a change in the user's Persona. */
    suspend fun broadcastIdentityUpdate(oldName: String): Message

    /** Initializes a new Group context. */
    fun startGroupRoom(name: String, members: Set<String>, type: Int = Group.SCOPE_PUBLIC, groupId: String? = null, parentId: String? = null, anchoredPublicGroupId: String? = null): String

    /** Updates the membership of a private Group. */
    fun updateGroupMembers(groupId: String, memberIds: Set<String>)

    /** Dynamically shifts the scoping of a frequency. */
    fun updateGroupScope(groupId: String, scope: Int)

    /** Triggers a differential sync. */
    fun initiateHistorySync(endpointId: String, sinceTimestamp: Long? = null)

    /** Sever all active connection links. */
    fun closeConnection()
    /** Full system cleanup. */
    fun release()
}
