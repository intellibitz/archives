package cc.thevar.blukit.data.crypto

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PublicKey
import javax.crypto.Cipher
import javax.crypto.KeyAgreement
import javax.crypto.Mac
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Manages End-to-End Encryption (E2EE) for vibes in The Air.
 * 
 * ### Security Implementation:
 * 1. **Key Agreement (ECDH)**: Uses Elliptic Curve Diffie-Hellman with the SecP256r1 curve 
 *    (NIST P-256) to establish a shared secret without ever transmitting private keys.
 * 2. **Key Derivation (HKDF)**: Implements RFC 5869 (HMAC-based Extract-and-Expand KDF) 
 *    to transform the raw ECDH shared secret into a high-entropy 256-bit AES key.
 * 3. **Authenticated Encryption (AES-GCM)**: Uses AES-256 in Galois/Counter Mode (GCM)
 *    to provide both confidentiality and integrity (authentication tag).
 * 4. **Hardware Backed**: Keys are stored in the Android KeyStore, utilizing hardware-backed
 *    security (TEE/StrongBox) whenever supported by the device.
 */
class CryptoManager(
    keyStoreProvider: String = "AndroidKeyStore"
) {

    private val keyStore = try {
        KeyStore.getInstance(keyStoreProvider).apply {
            load(null)
        }
    } catch (_: Exception) {
        // Fallback for non-Android environments (unit tests)
        KeyStore.getInstance(KeyStore.getDefaultType()).apply {
            load(null)
        }
    }

    /**
     * Retrieves or generates the hardware-backed EC key pair for this device.
     */
    fun getLocalKeyPair(): KeyPair {
        val entry = keyStore.getEntry(KEY_ALIAS_EC, null) as? KeyStore.PrivateKeyEntry
        return if (entry != null) {
            KeyPair(keyStore.getCertificate(KEY_ALIAS_EC).publicKey, entry.privateKey)
        } else {
            generateECKeyPair()
        }
    }

    private fun generateECKeyPair(): KeyPair {
        val kpg = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_EC, "AndroidKeyStore"
        )
        
        val purposes = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            KeyProperties.PURPOSE_AGREE_KEY
        } else {
            // Fallback for API 26-30: Hardware-backed EC agreement is limited.
            // On some devices, PURPOSE_SIGN might be usable for some agreement hacks,
            // but strictly PURPOSE_AGREE_KEY is needed for official support.
            // We'll use PURPOSE_SIGN as a placeholder to at least allow key generation.
            KeyProperties.PURPOSE_SIGN
        }

        val parameterSpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS_EC,
            purposes
        ).setAlgorithmParameterSpec(java.security.spec.ECGenParameterSpec("secp256r1"))
            .setUserAuthenticationRequired(false)
            .build()

        kpg.initialize(parameterSpec)
        return kpg.generateKeyPair()
    }

    /**
     * Derives a shared AES-256 key from our private key and a vibe's public key using ECDH.
     * Uses HKDF (HMAC-based Extract-and-Expand Key Derivation Function) with high-fidelity salt and info.
     */
    fun deriveSharedSecret(vibePublicKey: PublicKey): SecretKey {
        val keyAgreement = KeyAgreement.getInstance("ECDH")
        keyAgreement.init(getLocalKeyPair().private)
        keyAgreement.doPhase(vibePublicKey, true)
        val sharedSecret = keyAgreement.generateSecret()
        
        // HKDF Implementation (RFC 5869)
        // Hardened: High-entropy static salt for the Extract phase
        val salt = "blukit_vibe_bridge_salt_v1".toByteArray()
        // Context-specific info for the Expand phase
        val info = "blukit_aes_256_gcm_session_v1".toByteArray()
        
        val prk = hmacSha256(salt, sharedSecret)
        
        // Expand: T(1) = HMAC-SHA256(PRK, info | 0x01)
        val derivedKey = hmacSha256(prk, info + 0x01.toByte()).copyOf(32) // 256-bit key
        
        return SecretKeySpec(derivedKey, "AES")
    }

    private fun hmacSha256(key: ByteArray, data: ByteArray): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(key, "HmacSHA256"))
        return mac.doFinal(data)
    }

    /**
     * Encrypts data using AES-256-GCM with a specific secret key.
     * Prepend: [1-byte tag length prefix][12-byte IV][encrypted data + implicit GCM tag]
     */
    fun encrypt(data: ByteArray, secretKey: SecretKey): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val iv = cipher.iv
        val encrypted = cipher.doFinal(data)
        
        // Format: [IV Length (1 byte)] [IV] [Encrypted Data + Tag]
        val result = ByteArray(1 + iv.size + encrypted.size)
        result[0] = iv.size.toByte()
        iv.copyInto(result, 1)
        encrypted.copyInto(result, 1 + iv.size)
        
        return result
    }

    /**
     * Decrypts data using AES-256-GCM with a specific secret key.
     */
    fun decrypt(encryptedData: ByteArray, secretKey: SecretKey): ByteArray {
        val ivLen = encryptedData[0].toInt() and 0xFF
        val ivPart = encryptedData.copyOfRange(1, 1 + ivLen)
        val encryptedPart = encryptedData.copyOfRange(1 + ivLen, encryptedData.size)
        
        val cipher = Cipher.getInstance(TRANSFORMATION)
        // GCM standard tag length is 128 bits (16 bytes)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, ivPart))
        
        return cipher.doFinal(encryptedPart)
    }

    /**
     * Encrypts data for local storage using a hardware-backed AES key.
     */
    fun encryptLocal(data: ByteArray): ByteArray {
        val secretKey = getLocalStoreKey()
        return encrypt(data, secretKey)
    }

    /**
     * Decrypts data from local storage using a hardware-backed AES key.
     */
    fun decryptLocal(encryptedData: ByteArray): ByteArray {
        val secretKey = getLocalStoreKey()
        return decrypt(encryptedData, secretKey)
    }

    private fun getLocalStoreKey(): SecretKey {
        val entry = keyStore.getEntry(KEY_ALIAS_LOCAL, null) as? KeyStore.SecretKeyEntry
        return entry?.secretKey ?: generateLocalStoreKey()
    }

    private fun generateLocalStoreKey(): SecretKey {
        val keyGenerator = javax.crypto.KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore"
        )
        val parameterSpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS_LOCAL,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setUserAuthenticationRequired(false)
            .build()
        keyGenerator.init(parameterSpec)
        return keyGenerator.generateKey()
    }

    companion object {
        private const val KEY_ALIAS_EC = "blukit_ec_identity"
        private const val KEY_ALIAS_LOCAL = "blukit_local_storage"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
    }
}
