package cc.thevar.blukit.data.crypto

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
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
    fun testLocalEncryptionDecryption() {
        val originalText = "Sensitive Local Data"
        val originalBytes = originalText.toByteArray()

        val encryptedBytes = cryptoManager.encryptLocal(originalBytes)
        assertNotEquals(originalText, encryptedBytes.decodeToString())

        val decryptedBytes = cryptoManager.decryptLocal(encryptedBytes)
        assertEquals(originalText, decryptedBytes.decodeToString())
    }

    @Test
    fun testSharedSecretDerivation_Consistency() {
        val managerA = CryptoManager()
        val managerB = CryptoManager()

        val keyPairA = managerA.getLocalKeyPair()
        val keyPairB = managerB.getLocalKeyPair()

        // ECDH: A(pubB) == B(pubA)
        val secretA = managerA.deriveSharedSecret(keyPairB.public)
        val secretB = managerB.deriveSharedSecret(keyPairA.public)

        assertArrayEquals(secretA.encoded, secretB.encoded)
    }

    @Test(expected = Exception::class)
    fun testDecryptionWithWrongKey() {
        val key1 = SecretKeySpec(ByteArray(32) { 1 }, "AES")
        val key2 = SecretKeySpec(ByteArray(32) { 2 }, "AES")
        val data = "Hello".toByteArray()

        val encrypted = cryptoManager.encrypt(data, key1)
        cryptoManager.decrypt(encrypted, key2) // Should throw
    }
}
