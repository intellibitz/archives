package cc.thevar.blukit.ui

import cc.thevar.blukit.network.p2p.ConnectionError

/**
 * Represents a user-facing error within the Blukit experience.
 * Bridges the gap between low-level technical errors and human-centric feedback.
 */
sealed class UiError(val message: String) {
    data class RadiosStill(val msg: String) : UiError(msg)
    data class SecureChannelFailed(val msg: String) : UiError(msg)
    data class ProximityError(val msg: String) : UiError(msg)
}

/**
 * Mapper to convert P2P-level errors to UI-facing errors.
 */
fun ConnectionError.toUiError(): UiError = when (this) {
    is ConnectionError.EncryptionError -> UiError.SecureChannelFailed("Security error: $message")
    is ConnectionError.NearbyError, is ConnectionError.AdvertisingError -> UiError.RadiosStill("Communication offline: $message")
    is ConnectionError.LinkError -> UiError.ProximityError("Could not establish connection: $message")
}
