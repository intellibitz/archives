package cc.thevar.blukit.data.crypto

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class CryptoManagerTest {
    private fun generateAesKey(): SecretKey {
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(256)
        return keyGen.generateKey()
    }

    @Test
    fun `encrypt and decrypt round‑trip returns original data`() {
        val manager = CryptoManager()
        val secretKey = generateAesKey()
        val original = "Blukit test payload".toByteArray(Charsets.UTF_8)

        val encrypted = manager.encrypt(original, secretKey)
        // First byte stores IV length; AES/GCM default IV is 12 bytes
        assertEquals(12, encrypted[0].toInt() and 0xFF)

        val decrypted = manager.decrypt(encrypted, secretKey)
        assertArrayEquals(original, decrypted)
    }
}
