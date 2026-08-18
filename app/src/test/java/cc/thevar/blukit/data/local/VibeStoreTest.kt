package cc.thevar.blukit.data.local

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import cc.thevar.blukit.data.crypto.CryptoManager
import cc.thevar.blukit.domain.model.MessagePayload
import cc.thevar.blukit.domain.model.VibeGroup
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34]) // Limit SDK for Robolectric compatibility
class VibeStoreTest {

    private lateinit var context: Context
    private lateinit var cryptoManager: CryptoManager
    private lateinit var vibeStore: VibeStore
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        cryptoManager = mockk()
        
        // Mock encryption/decryption as passthrough for simplicity in store tests
        every { cryptoManager.encryptLocal(any()) } answers { it.invocation.args[0] as ByteArray }
        every { cryptoManager.decryptLocal(any()) } answers { it.invocation.args[0] as ByteArray }
        
        vibeStore = VibeStore(context, cryptoManager, testDispatcher)
    }

    @Test
    fun `insert and retrieve messages`() = runTest {
        val message = MessagePayload(
            messageId = "msg-1",
            senderId = "user-1",
            senderName = "Alice",
            content = "Hello",
            timestamp = System.currentTimeMillis()
        )

        vibeStore.insertMessage(message)
        
        val messages = vibeStore.messages.first()
        assertEquals(1, messages.size)
        assertEquals(message, messages[0])
    }

    @Test
    fun `insert and retrieve groups`() = runTest {
        val group = VibeGroup(
            id = "group-1",
            name = "Project Vibe",
            memberIds = setOf("user-1", "user-2"),
            type = VibeGroup.TYPE_TIE
        )

        vibeStore.insertGroup(group)
        
        val groups = vibeStore.groups.first()
        assertEquals(1, groups.size)
        assertEquals(group, groups[0])
    }

    @Test
    fun `compact messages removes old vibes and ephemeral groups`() = runTest {
        val now = System.currentTimeMillis()
        val oldMessage = MessagePayload(
            messageId = "old",
            senderId = "user-1",
            senderName = "Bob",
            content = "Old Vibe",
            timestamp = now - 24 * 3600 * 1000 // 24 hours ago
        )
        val newMessage = MessagePayload(
            messageId = "new",
            senderId = "user-1",
            senderName = "Bob",
            content = "New Vibe",
            timestamp = now
        )
        
        val ephemeralGroup = VibeGroup(
            id = "ephemeral",
            name = "Side",
            memberIds = setOf("1"),
            type = VibeGroup.TYPE_SIDE,
            lastVibeTimestamp = now - 24 * 3600 * 1000
        )
        
        val persistentGroup = VibeGroup(
            id = "persistent",
            name = "Tie",
            memberIds = setOf("1"),
            type = VibeGroup.TYPE_TIE,
            isPersistent = true,
            lastVibeTimestamp = now - 24 * 3600 * 1000
        )

        vibeStore.insertMessage(oldMessage)
        vibeStore.insertMessage(newMessage)
        vibeStore.insertGroup(ephemeralGroup)
        vibeStore.insertGroup(persistentGroup)
        
        vibeStore.compactMessages()
        
        val messages = vibeStore.messages.first()
        assertEquals(1, messages.size)
        assertEquals("new", messages[0].messageId)
        
        val groups = vibeStore.groups.first()
        assertEquals(1, groups.size)
        assertEquals("persistent", groups[0].id)
    }
}
