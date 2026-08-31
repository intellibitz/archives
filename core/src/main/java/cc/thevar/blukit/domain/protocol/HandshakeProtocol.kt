package cc.thevar.blukit.domain.protocol

import java.security.KeyPair

/**
 * Defines the binary protocol for establishing secure mesh links.
 */
object HandshakeProtocol {
    private const val HANDSHAKE_VERSION = 0x01.toByte()

    fun createHandshake(localKeyPair: KeyPair): ByteArray {
        val publicKeyBytes = localKeyPair.public.encoded
        val handshake = ByteArray(1 + publicKeyBytes.size)
        handshake[0] = HANDSHAKE_VERSION
        publicKeyBytes.copyInto(handshake, 1)
        return handshake
    }

    fun isHandshake(data: ByteArray): Boolean {
        return data.isNotEmpty() && data[0] == HANDSHAKE_VERSION
    }

    fun parseHandshake(data: ByteArray): ByteArray? {
        if (!isHandshake(data)) return null
        return data.copyOfRange(1, data.size)
    }
}
