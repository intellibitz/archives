/**
 * BLUKIT NETWORK: RESONANCE CONTROLLER
 *
 * The fundamental contract for all peer-to-peer resonance engines in the mesh.
 * Defines the operational primitives for Sensing, advertising, and secure Echo propagation.
 */
package cc.thevar.blukit.network.p2p

import cc.thevar.blukit.domain.model.ConnectionStatus
import cc.thevar.blukit.domain.model.Echo
import cc.thevar.blukit.domain.model.Source
import cc.thevar.blukit.domain.model.Sphere
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Categorized Resonance Errors.
 */
sealed class ResonanceError(val message: String) {
    class SensingError(msg: String) : ResonanceError(msg)
    class AdvertisingError(msg: String) : ResonanceError(msg)
    class ConnectionError(msg: String) : ResonanceError(msg)
    class EncryptionError(msg: String) : ResonanceError(msg)
}

/**
 * Resonance Controller interface — orchestrates Source sensing and encrypted Echoing.
 */
interface ResonanceController {
    /** Real-time list of detected Sources in range. */
    val scannedDevices: StateFlow<List<Source>>
    /** True if at least one secure resonance link is active. */
    val isConnected: StateFlow<Boolean>
    /** Set of persistent identifiers for currently connected Spheres. */
    val connectedGroups: StateFlow<Set<String>>
    /** Pending incoming requests to form a private Sphere. */
    val incomingRadioRequests: StateFlow<Set<Source>>
    /** Pending outgoing requests waiting for peer acceptance. */
    val outgoingRadioRequests: StateFlow<Set<Source>>
    /** Active status of the sensing radio. */
    val isDiscovering: StateFlow<Boolean>
    /** Active status of the advertising radio. */
    val isAdvertising: StateFlow<Boolean>
    /** The last encountered resonance or encryption error. */
    val errors: StateFlow<ResonanceError?>
    /** The unified life stream of Echoes extracted from the mesh. */
    val messages: StateFlow<List<Echo>>
    /** Emissions for newly sensed Spheres. */
    val discoveredRooms: SharedFlow<Sphere>
    /** Progress of the differential history sync. */
    val syncProgress: StateFlow<Float?>

    /** Activates the sensing radio. */
    fun startDiscovery()
    /** Deactivates sensing. */
    fun stopDiscovery()
    
    /** Begins projecting the user's Persona. */
    fun startAdvertising()
    /** Stops the Persona projection. */
    fun stopAdvertising()

    /**
     * Initiate resonance with a sensed Source.
     */
    fun connectToDevice(device: Source): SharedFlow<ConnectionStatus>
    
    /** Requests a private Sphere formation with a peer. */
    fun requestRadio(device: Source)
    /** Checks if a low-level endpoint is currently connected. */
    fun isNearbyConnected(endpointId: String): Boolean
    /** Accepts an incoming request. */
    fun acceptRadio(device: Source)
    /** Rejects an incoming request. */
    fun denyRadio(device: Source)

    /**
     * Enters a discoverable Sphere.
     */
    fun joinRoom(groupId: String)

    /**
     * Echoes an encrypted message.
     * @return The generated Echo metadata.
     */
    suspend fun sendMessage(content: String, receiverId: String? = null, messageScope: Int = Echo.MESSAGE_SHOUT, messageId: String? = null, groupId: String? = null, groupName: String? = null, type: Int = Echo.TYPE_TEXT): Echo?

    /**
     * Broadcasts an Echo to all available peers.
     */
    suspend fun broadcastMessage(content: String, messageScope: Int = Echo.MESSAGE_SHOUT, messageId: String? = null, groupId: String? = null, groupName: String? = null, type: Int = Echo.TYPE_TEXT): Echo?

    /** Echoes a message scoped to a specific Sphere. */
    suspend fun sendGroupMessage(content: String, groupId: String): Echo?

    /** Propagates a LWW-versioned note update. */
    suspend fun sendNoteUpdate(groupId: String, content: String, messageId: String?, version: Int): Echo?

    /** Shares a local file over high-speed resonance. */
    suspend fun sendFile(fileUri: android.net.Uri, receiverId: String? = null, messageScope: Int = Echo.MESSAGE_SHOUT, groupId: String? = null, groupName: String? = null): Echo?

    /** Broadcasts a change in the user's Persona. */
    suspend fun broadcastIdentityUpdate(oldName: String): Echo

    /** Initializes a new Sphere context. */
    fun startGroupRoom(name: String, members: Set<String>, type: Int = Sphere.SCOPE_PUBLIC, groupId: String? = null, parentId: String? = null, anchoredPublicSphereId: String? = null): String

    /** Updates the membership of a private Sphere. */
    fun updateGroupMembers(groupId: String, memberIds: Set<String>)

    /** Dynamically shifts the scoping of a frequency. */
    fun updateGroupScope(groupId: String, scope: Int)

    /** Triggers a differential sync. */
    fun initiateHistorySync(endpointId: String, sinceTimestamp: Long? = null)

    /** Sever all active resonance links. */
    fun closeConnection()
    /** Full system cleanup. */
    fun release()
}
