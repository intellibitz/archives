package cc.thevar.blukit.data.repository

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class IdentityRepositoryTest {

    private lateinit var repository: IdentityRepository

    @Before
    fun setUp() {
        repository = IdentityRepository(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun `test default identity values`() = runTest {
        repository.nickname.test {
            assertEquals(null, awaitItem()) // Default is null for "Watch first" flow
        }
        repository.emojiAvatar.test {
            assertEquals("🟦", awaitItem())
        }
    }

    @Test
    fun `test saving and resetting identity`() = runTest {
        repository.saveNickname("NewName")
        repository.nickname.test {
            assertEquals("NewName", awaitItem())
        }
        
        repository.logout()
        repository.nickname.test {
            assertEquals(null, awaitItem())
        }
    }

    @Test
    fun `test stealth mode toggle`() = runTest {
        repository.stealthMode.test {
            assertFalse(awaitItem())
        }
        
        repository.toggleStealth(true)
        repository.stealthMode.test {
            assertTrue(awaitItem())
        }
    }

    @Test
    fun `test device id generation and persistence`() = runTest {
        val id1 = repository.getDeviceId()
        assertTrue(id1.isNotEmpty())
        
        val id2 = repository.getDeviceId()
        assertEquals(id1, id2)
    }

    @Test
    fun `test blocking users`() = runTest {
        repository.blockedUsers.test {
            assertTrue(awaitItem().isEmpty())
        }
        
        repository.blockUser("user1")
        repository.blockedUsers.test {
            assertTrue(awaitItem().contains("user1"))
        }
        
        repository.blockUser("user2")
        repository.blockedUsers.test {
            val blocked = awaitItem()
            assertTrue(blocked.contains("user1"))
            assertTrue(blocked.contains("user2"))
        }
    }
}
