package cc.thevar.blukit.ui

import cc.thevar.blukit.network.p2p.P2PError

/**
 * Represents a user-facing error within the Blukit experience.
 * Bridges the gap between low-level technical errors and human-centric feedback.
 */
sealed class UiError(val message: String, val isCritical: Boolean = false) {
    data class RadiosStill(val msg: String) : UiError(msg)
    data class SecureChannelFailed(val msg: String) : UiError(msg, isCritical = true)
    data class ProximityError(val msg: String) : UiError(msg)
    data class PermissionDenied(val msg: String, val permanently: Boolean = false) : UiError(msg)
    data class General(val msg: String) : UiError(msg)
}

/**
 * Mapper to convert P2P-level errors to UI-facing errors.
 */
fun P2PError.toUiError(): UiError = when (this) {
    is P2PError.EncryptionError -> UiError.SecureChannelFailed("Security breach: $message")
    is P2PError.DiscoveryError, is P2PError.AdvertisingError -> UiError.RadiosStill("The air waves are quiet: $message")
    is P2PError.ConnectionError -> UiError.ProximityError("Could not bridge tie: $message")
    else -> UiError.General(message)
}
