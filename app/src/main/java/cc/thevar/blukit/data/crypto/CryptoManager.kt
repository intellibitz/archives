package cc.thevar.blukit.data.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PublicKey
import javax.crypto.Cipher
import javax.crypto.KeyAgreement
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Supreme Senior Architect Implementation:
 * End-to-End Encryption using ECDH (Curve SecP256r1) for key exchange
 * and AES-256-GCM for payload confidentiality.
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
        val parameterSpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS_EC,
            KeyProperties.PURPOSE_AGREE_KEY
        ).setAlgorithmParameterSpec(java.security.spec.ECGenParameterSpec("secp256r1"))
            .setUserAuthenticationRequired(false)
            .build()

        kpg.initialize(parameterSpec)
        return kpg.generateKeyPair()
    }

    /**
     * Derives a shared AES-256 key from our private key and a peer's public key using ECDH.
     */
    fun deriveSharedSecret(peerPublicKey: PublicKey): SecretKey {
        val keyAgreement = KeyAgreement.getInstance("ECDH")
        keyAgreement.init(getLocalKeyPair().private)
        keyAgreement.doPhase(peerPublicKey, true)
        val sharedSecret = keyAgreement.generateSecret()
        
        // Use SHA-256 to derive a fixed-length 256-bit AES key from the secret
        val md = java.security.MessageDigest.getInstance("SHA-256")
        val derivedKey = md.digest(sharedSecret)
        return SecretKeySpec(derivedKey, "AES")
    }

    /**
     * Encrypts data using AES-256-GCM with a specific secret key.
     */
    fun encrypt(data: ByteArray, secretKey: SecretKey): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val iv = cipher.iv
        val encrypted = cipher.doFinal(data)
        return iv + encrypted
    }

    /**
     * Decrypts data using AES-256-GCM with a specific secret key.
     */
    fun decrypt(encryptedData: ByteArray, secretKey: SecretKey): ByteArray {
        val ivPart = encryptedData.copyOfRange(0, 12)
        val encryptedPart = encryptedData.copyOfRange(12, encryptedData.size)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, ivPart))
        return cipher.doFinal(encryptedPart)
    }

    companion object {
        private const val KEY_ALIAS_EC = "blukit_ec_identity"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
    }
}
