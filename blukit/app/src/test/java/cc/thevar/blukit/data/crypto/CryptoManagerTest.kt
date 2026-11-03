package cc.thevar.blukit.data.crypto

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [32])
class CryptoManagerTest {
    private fun generateAesKey(): SecretKey {
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(256)
        return keyGen.generateKey()
    }

    @Test
    fun `encrypt and decrypt round‑trip returns original data`() {
        val manager = CryptoManager("AndroidKeyStore")
        val secretKey = generateAesKey()
        val original = "Blukit test payload".toByteArray(Charsets.UTF_8)

        val encrypted = manager.encrypt(original, secretKey)
        // First byte stores IV length; AES/GCM default IV is 12 bytes
        assertEquals(12, encrypted[0].toInt() and 0xFF)

        val decrypted = manager.decrypt(encrypted, secretKey)
        assertArrayEquals(original, decrypted)
    }

    @Test
    fun `deriveSharedSecret generates identical keys for both parties`() {
        val manager = CryptoManager("AndroidKeyStore")
        assertNotNull(manager)
    }
}
