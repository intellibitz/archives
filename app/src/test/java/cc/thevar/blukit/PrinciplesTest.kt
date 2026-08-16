package cc.thevar.blukit

import cc.thevar.blukit.data.local.VibeStore
import cc.thevar.blukit.data.repository.ContactRepository
import cc.thevar.blukit.data.repository.IdentityRepository
import cc.thevar.blukit.data.system.HapticManager
import cc.thevar.blukit.data.crypto.CryptoManager
import cc.thevar.blukit.network.p2p.NearbyP2PController
import cc.thevar.blukit.domain.model.MessagePayload
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit test suite for verifying the fundamental "Powers" and "Commandments" of Blukit.
 * Ensures the decentralized nature of vibes and strict adherence to privacy-first identity management.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = TestBlukitApplication::class)
class PrinciplesTest {

    private lateinit var repository: IdentityRepository
    private val contactRepository: ContactRepository = mockk(relaxed = true)
    private val vibeStore: VibeStore = mockk(relaxed = true)
    private val hapticManager: HapticManager = mockk(relaxed = true)
    private val radioStateManager: cc.thevar.blukit.data.system.RadioStateManager = mockk(relaxed = true)
    private val cryptoManager: CryptoManager = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var controller: NearbyP2PController

    @Before
    fun setUp() {
        mockkStatic(android.util.Log::class)
        every { android.util.Log.d(any<String>(), any<String>()) } returns 0
        every { android.util.Log.i(any<String>(), any<String>()) } returns 0
        every { android.util.Log.w(any<String>(), any<String>()) } returns 0
        every { android.util.Log.e(any<String>(), any<String>(), any<Throwable>()) } returns 0
        
        repository = mockk(relaxed = true)
        
        every { repository.nicknameFlow } returns MutableStateFlow(null)
        every { repository.emojiAvatar } returns MutableStateFlow("👤")
        every { repository.stealthMode } returns MutableStateFlow(false)
        every { repository.lowPowerMode } returns MutableStateFlow(false)
        every { repository.blockedUsers } returns MutableStateFlow(emptySet())
        every { repository.getCurrentNickname() } returns "TestUser"
        every { repository.getDeviceId() } returns "test-device-id"
        every { vibeStore.getAllMessages() } returns MutableStateFlow(emptyList())
        every { contactRepository.allContacts } returns flowOf(emptyList())
        every { radioStateManager.radioStates } returns MutableStateFlow(cc.thevar.blukit.data.system.RadioStates(true, true, true))
        
        controller = NearbyP2PController(
            io.mockk.mockk(relaxed = true), repository, contactRepository, vibeStore, hapticManager, radioStateManager, cryptoManager, testDispatcher
        )
    }

    // --- POWER TESTS ---

    @Test
    fun `Power 1 & 4 - Vibes are decentralized and broadcast to everyone in The Vibes`() = runTest {
        controller.broadcastMessage("Public Info")
        coVerify { vibeStore.insertMessage(match { it.content == "Public Info" && it.receiverId == null }) }
    }

    @Test
    fun `Power 2 - User can feel the vibes in The Vibes without any ties connected`() = runTest {
        val result = controller.broadcastMessage("Lone Shoutout")
        assertNotNull(result)
        assertEquals("Lone Shoutout", result?.content)
        coVerify { vibeStore.insertMessage(any()) }
    }

    // --- COMMANDMENT TESTS ---

    @Test
    fun `Commandment 5 - Privacy Anonymity - IdentityRepository only stores non-identifiable data`() {
        val sensitivePatterns = listOf("imei", "mac", "location_lat", "email", "phone")
        val fields = cc.thevar.blukit.data.local.entities.ContactEntity::class.java.declaredFields
        fields.forEach { field ->
            val isSensitive = sensitivePatterns.any { pattern -> field.name.lowercase().contains(pattern) }
            assertFalse("Privacy breach: Sensitive field '${field.name}' found in ContactEntity", isSensitive)
        }
    }

    @Test
    fun `Commandment 7 - Security - No passwords or usernames allowed`() {
        val classes = listOf(MessagePayload::class.java, cc.thevar.blukit.data.local.entities.ContactEntity::class.java)
        classes.forEach { clazz ->
            val hasPasswordField = clazz.declaredFields.any { it.name.lowercase().contains("password") || it.name.lowercase().contains("secret") }
            assertFalse("Security breach: Password field found in ${clazz.simpleName}", hasPasswordField)
        }
    }
}
