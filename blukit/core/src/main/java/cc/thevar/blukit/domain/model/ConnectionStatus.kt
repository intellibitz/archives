/**
 * BLUKIT CORE DOMAIN: CONNECTION STATUS
 *
 * Defines the spectral states of a mesh connection.
 * Used by the Interaction Hub to signal state changes in the unified pulse frequency.
 */
package cc.thevar.blukit.domain.model

/**
 * Sealed interface representing the lifecycle of a peer-to-peer connection.
 */
sealed interface ConnectionStatus {
    /** Negotiating ECDH keys and radio synchronization. */
    data object Connecting : ConnectionStatus
    
    /** Secure hardware-encrypted channel is established. */
    data object Connected : ConnectionStatus
    
    /** A critical failure occurred during the pulse handshake. */
    data class Error(val message: String) : ConnectionStatus
    
    /** The peer moved out of radio range or the medium was severed. */
    data class ConnectionLost(val reason: String = "Disconnected") : ConnectionStatus
}
