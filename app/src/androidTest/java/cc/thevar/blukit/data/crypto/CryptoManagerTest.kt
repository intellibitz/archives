package cc.thevar.blukit.data.crypto

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import javax.crypto.spec.SecretKeySpec

@RunWith(AndroidJUnit4::class)
class CryptoManagerTest {

    private lateinit var cryptoManager: CryptoManager

    @Before
    fun setUp() {
        cryptoManager = CryptoManager()
    }

    @Test
    fun testEncryptionDecryption() {
        val secretKey = SecretKeySpec(ByteArray(32), "AES") // Dummy key for testing encrypt/decrypt logic
        val originalText = "Top Secret Blukit Message"
        val originalBytes = originalText.toByteArray()

        val encryptedBytes = cryptoManager.encrypt(originalBytes, secretKey)
        assertFalse(originalText == encryptedBytes.decodeToString())

        val decryptedBytes = cryptoManager.decrypt(encryptedBytes, secretKey)
        assertEquals(originalText, decryptedBytes.decodeToString())
    }

    @Test
    fun testECKeyPairGeneration() {
        val keyPair = cryptoManager.getLocalKeyPair()
        assertNotNull(keyPair.public)
        assertNotNull(keyPair.private)
        assertEquals("EC", keyPair.public.algorithm)
    }

    @Test
    fun testSharedSecretDerivation() {
        val managerA = CryptoManager()
        val managerB = CryptoManager() // In real test, this would be on a different device

        // This is a simplified test on a single device
        // We'll use managerA's keys to derive secrets with a hypothetical peerB
        val keyPairA = managerA.getLocalKeyPair()
        
        // Hypothetical Peer B (we'll just generate another one on the fly for testing)
        // In reality, this peer public key comes over the wire
        val keyPairB = managerA.getLocalKeyPair() 

        val secretA = managerA.deriveSharedSecret(keyPairB.public)
        assertNotNull(secretA)
        assertEquals("AES", secretA.algorithm)
    }
}
