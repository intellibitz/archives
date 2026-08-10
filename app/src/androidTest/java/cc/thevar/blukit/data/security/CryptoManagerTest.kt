package cc.thevar.blukit.data.security

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CryptoManagerTest {

    private lateinit var cryptoManager: CryptoManager

    @Before
    fun setUp() {
        cryptoManager = CryptoManager()
    }

    @Test
    fun testEncryptionDecryption() {
        val originalText = "Hello Blukit World!"
        val originalBytes = originalText.toByteArray()

        val encryptedBytes = cryptoManager.encrypt(originalBytes)
        assertNotEquals(originalText, encryptedBytes.decodeToString())

        val decryptedBytes = cryptoManager.decrypt(encryptedBytes)
        assertEquals(originalText, decryptedBytes.decodeToString())
    }

    @Test
    fun testEncryptionUniqueness() {
        val text = "Consistent Text"
        val bytes = text.toByteArray()

        val encrypted1 = cryptoManager.encrypt(bytes)
        val encrypted2 = cryptoManager.encrypt(bytes)

        // GCM should use different IVs for each encryption
        assertNotEquals(encrypted1.decodeToString(), encrypted2.decodeToString())
    }
}
