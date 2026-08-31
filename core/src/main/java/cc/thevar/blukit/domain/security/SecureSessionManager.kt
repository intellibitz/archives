package cc.thevar.blukit.domain.security

import cc.thevar.blukit.data.crypto.CryptoManager
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import java.util.concurrent.ConcurrentHashMap
import javax.crypto.SecretKey

/**
 * Manages active cryptographic sessions for peer connections.
 * Decouples session lifecycle from the network controller.
 */
class SecureSessionManager(
    private val cryptoManager: CryptoManager
) {
    private val sessionKeys = ConcurrentHashMap<String, SecretKey>()

    /**
     * Completes a handshake by deriving a shared secret from the peer's public key.
     */
    fun establishSession(endpointId: String, peerPublicKeyBytes: ByteArray): SecretKey? {
        return try {
            val keyFactory = KeyFactory.getInstance("EC")
            val peerPublicKey = keyFactory.generatePublic(X509EncodedKeySpec(peerPublicKeyBytes))
            val secretKey = cryptoManager.deriveSharedSecret(peerPublicKey)
            sessionKeys[endpointId] = secretKey
            secretKey
        } catch (e: Exception) {
            null
        }
    }

    fun getSessionKey(endpointId: String): SecretKey? = sessionKeys[endpointId]

    fun terminateSession(endpointId: String) {
        sessionKeys.remove(endpointId)
    }

    fun clearAll() {
        sessionKeys.clear()
    }

    /**
     * Encrypts data for a specific session.
     */
    fun encryptForSession(endpointId: String, data: ByteArray): ByteArray? {
        val key = sessionKeys[endpointId] ?: return null
        return cryptoManager.encrypt(data, key)
    }

    /**
     * Decrypts data from a specific session.
     */
    fun decryptFromSession(endpointId: String, encryptedData: ByteArray): ByteArray? {
        val key = sessionKeys[endpointId] ?: return null
        return try {
            cryptoManager.decrypt(encryptedData, key)
        } catch (e: Exception) {
            null
        }
    }
}
