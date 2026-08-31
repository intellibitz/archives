package cc.thevar.blukit.ui

import cc.thevar.blukit.network.p2p.ResonanceError

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
fun ResonanceError.toUiError(): UiError = when (this) {
    is ResonanceError.EncryptionError -> UiError.SecureChannelFailed("Security breach: $message")
    is ResonanceError.SensingError, is ResonanceError.AdvertisingError -> UiError.RadiosStill("The air waves are quiet: $message")
    is ResonanceError.ConnectionError -> UiError.ProximityError("Could not bridge tie: $message")
}
