/**
 * BLUKIT NETWORK: P2P CONTROLLER
 *
 * The fundamental contract for all peer-to-peer radio engines in the mesh.
 * Defines the operational primitives for discovery, advertising, and secure pulse propagation.
 */
package cc.thevar.blukit.network.p2p

import cc.thevar.blukit.domain.model.ConnectionStatus
import cc.thevar.blukit.domain.model.MessagePayload
import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.domain.model.Resonance
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Categorized P2P Errors for ergonomic UI feedback and tactical retry logic.
 */
sealed class P2PError(val message: String, val isTransient: Boolean) {
    /** Failure during the discovery scan. Usually recoverable by restarting the radio. */
    class DiscoveryError(msg: String) : P2PError(msg, true)
    /** Failure during advertising. May require a brief cooldown period. */
    class AdvertisingError(msg: String) : P2PError(msg, true)
    /** Physical link failure or handshake rejection. */
    class ConnectionError(msg: String) : P2PError(msg, true)
    /** Failure in ECDH key derivation or AES-GCM decryption. */
    class EncryptionError(msg: String) : P2PError(msg, false)
    /** Unknown or non-specific radio failure. */
    class GenericError(msg: String) : P2PError(msg, false)
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
    /** Set of persistent identifiers for currently tied peers. */
    val connectedTies: StateFlow<Set<String>>
    /** Pending incoming requests to form a private Chain. */
    val incomingRadioRequests: StateFlow<Set<P2PDevice>>
    /** Pending outgoing requests waiting for peer acceptance. */
    val outgoingRadioRequests: StateFlow<Set<P2PDevice>>
    /** Active status of the discovery radio. */
    val isDiscovering: StateFlow<Boolean>
    /** Active status of the advertising radio. */
    val isAdvertising: StateFlow<Boolean>
    /** The last encountered radio or encryption error. */
    val errors: StateFlow<P2PError?>
    /** The unified life stream of pulses extracted from the mesh. */
    val messages: StateFlow<List<MessagePayload>>
    /** Emissions for newly discovered Crowds (public frequencies). */
    val discoveredCrowds: SharedFlow<Resonance>
    /** Progress of the differential pulse sync (0.0 to 1.0). */
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
    
    /** Requests a private radio tie (Chain formation) with a peer. */
    fun requestRadio(device: P2PDevice)
    /** Checks if a low-level endpoint is currently connected. */
    fun isNearbyConnected(endpointId: String): Boolean
    /** Accepts an incoming request to form a private Chain. */
    fun acceptRadio(device: P2PDevice)
    /** Rejects an incoming request. */
    fun denyRadio(device: P2PDevice)

    /**
     * Sends an encrypted pulse to a specific peer or the local crowd.
     * @return The generated MessagePayload metadata.
     */
    suspend fun sendMessage(content: String, receiverId: String? = null, pulseType: Int = MessagePayload.PULSE_SHOUT, messageId: String? = null, groupId: String? = null, groupName: String? = null, type: Int = MessagePayload.TYPE_TEXT): MessagePayload?

    /**
     * Broadcasts a pulse to all available peers in the mesh.
     */
    suspend fun broadcastMessage(content: String, pulseType: Int = MessagePayload.PULSE_SHOUT, messageId: String? = null, groupId: String? = null, groupName: String? = null, type: Int = MessagePayload.TYPE_TEXT): MessagePayload?

    /** Sends a pulse scoped to a specific Resonance (Crowd or Chain). */
    suspend fun sendGroupMessage(content: String, groupId: String): MessagePayload?

    /** Propagates a LWW-versioned note update to a Chain. */
    suspend fun sendNoteUpdate(groupId: String, content: String, messageId: String?, version: Int): MessagePayload?

    /** Shares a local file (Image or Memory) over high-speed WiFi radio. */
    suspend fun sendFile(fileUri: android.net.Uri, receiverId: String? = null, pulseType: Int = MessagePayload.PULSE_SHOUT, groupId: String? = null, groupName: String? = null): MessagePayload?

    /** Broadcasts a change in the user's Persona (Nickname/Emoji) across the mesh. */
    suspend fun broadcastIdentityUpdate(oldName: String): MessagePayload

    /** Initializes a new Resonance context. */
    fun startGroupPulse(name: String, members: Set<String>, type: Int = Resonance.SCOPE_PUBLIC, groupId: String? = null, parentId: String? = null): String

    /** Updates the membership of a private Chain. */
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
