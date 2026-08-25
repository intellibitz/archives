/**
 * BLUKIT SECURITY: CRYPTO MANAGER
 *
 * Manages End-to-End Encryption (E2EE) for pulses in The Air.
 * Implements a hardware-backed security protocol to ensure absolute local privacy.
 * 
 * Cryptographic Specs:
 * 1. **Key Agreement (ECDH)**: NIST P-256 (secp256r1) for established shared secrets.
 * 2. **Key Derivation (HKDF)**: RFC 5869 to derive high-entropy 256-bit AES keys.
 * 3. **Authenticated Encryption (AES-GCM)**: Galois/Counter Mode for confidentiality and integrity.
 * 4. **Hardware Storage**: Android KeyStore (TEE/StrongBox) for identity and local storage keys.
 */
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
 * Orchestrates cryptographic operations for the mesh.
 */
class CryptoManager(
    keyStoreProvider: String = "AndroidKeyStore"
) {

    private val keyStore = try {
        KeyStore.getInstance(keyStoreProvider).apply { load(null) }
    } catch (_: Exception) {
        // Fallback for non-Android environments (unit tests)
        KeyStore.getInstance(KeyStore.getDefaultType()).apply { load(null) }
    }

    /**
     * Retrieves or generates the hardware-backed EC identity key pair.
     * This key pair acts as the user's permanent cryptographic anchor.
     */
    fun getLocalKeyPair(): KeyPair {
        val entry = keyStore.getEntry(KEY_ALIAS_EC, null) as? KeyStore.PrivateKeyEntry
        return if (entry != null) {
            KeyPair(keyStore.getCertificate(KEY_ALIAS_EC).publicKey, entry.privateKey)
        } else {
            generateECKeyPair()
        }
    }

    /** Generates a new P-256 key pair in the Secure Element / TEE. */
    private fun generateECKeyPair(): KeyPair {
        val kpg = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_EC, "AndroidKeyStore"
        )
        
        val purposes = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            KeyProperties.PURPOSE_AGREE_KEY
        } else {
            // Fallback: Use PURPOSE_SIGN for key generation on older devices
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
     * Derives a shared AES-256 key using ECDH and HKDF.
     * Ensures that session keys are high-entropy and context-bound.
     */
    fun deriveSharedSecret(pulsePublicKey: PublicKey): SecretKey {
        val keyAgreement = KeyAgreement.getInstance("ECDH")
        keyAgreement.init(getLocalKeyPair().private)
        keyAgreement.doPhase(pulsePublicKey, true)
        val sharedSecret = keyAgreement.generateSecret()
        
        // HKDF Implementation (RFC 5869)
        val salt = "blukit_pulse_bridge_salt_v1".toByteArray()
        val info = "blukit_aes_256_gcm_session_v1".toByteArray()
        
        val prk = hmacSha256(salt, sharedSecret)
        val derivedKey = hmacSha256(prk, info + 0x01.toByte()).copyOf(32) // 256-bit
        
        return SecretKeySpec(derivedKey, "AES")
    }

    private fun hmacSha256(key: ByteArray, data: ByteArray): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(key, "HmacSHA256"))
        return mac.doFinal(data)
    }

    /**
     * Encrypts a pulse using AES-256-GCM.
     * Output format: [IV Length (1b)] [IV] [Encrypted Data + 16b GCM Tag]
     */
    fun encrypt(data: ByteArray, secretKey: SecretKey): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val iv = cipher.iv
        val encrypted = cipher.doFinal(data)
        
        val result = ByteArray(1 + iv.size + encrypted.size)
        result[0] = iv.size.toByte()
        iv.copyInto(result, 1)
        encrypted.copyInto(result, 1 + iv.size)
        
        return result
    }

    /** Decrypts a pulse using AES-256-GCM and validates the authentication tag. */
    fun decrypt(encryptedData: ByteArray, secretKey: SecretKey): ByteArray {
        val ivLen = encryptedData[0].toInt() and 0xFF
        val ivPart = encryptedData.copyOfRange(1, 1 + ivLen)
        val encryptedPart = encryptedData.copyOfRange(1 + ivLen, encryptedData.size)
        
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, ivPart))
        
        return cipher.doFinal(encryptedPart)
    }

    /** Encrypts data for local binary logs using the internal storage key. */
    fun encryptLocal(data: ByteArray): ByteArray {
        val secretKey = getLocalStoreKey()
        return encrypt(data, secretKey)
    }

    /** Decrypts data from local binary logs. */
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
