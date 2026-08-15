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
 * Implements Elliptic Curve Diffie-Hellman (ECDH) on SecP256r1 for secure key exchange
 * and AES-256-GCM for authenticated payload confidentiality.
 * Key derivation follows HKDF (RFC 5869) to ensure high entropy for session keys.
 */
class CryptoManager {

    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
        load(null)
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
     * Uses HKDF (HMAC-based Extract-and-Expand Key Derivation Function) for security hardening.
     */
    fun deriveSharedSecret(vibePublicKey: PublicKey): SecretKey {
        val keyAgreement = KeyAgreement.getInstance("ECDH")
        keyAgreement.init(getLocalKeyPair().private)
        keyAgreement.doPhase(vibePublicKey, true)
        val sharedSecret = keyAgreement.generateSecret()
        
        // HKDF Implementation (RFC 5869) using HMAC-SHA256
        val salt = "blukit_p2p_salt".toByteArray()
        val info = "blukit_aes_256_gcm_key".toByteArray()
        
        val prk = hmacSha256(salt, sharedSecret)
        val derivedKey = hmacSha256(prk, info + 0x01.toByte()).copyOf(32) // Expand to 256 bits
        
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

    companion object {
        private const val KEY_ALIAS_EC = "blukit_ec_identity"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
    }
}
