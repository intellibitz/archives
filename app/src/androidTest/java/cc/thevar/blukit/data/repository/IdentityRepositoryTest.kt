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
        repository = IdentityRepositoryImpl(ApplicationProvider.getApplicationContext())
        repository.logout()
    }

    @Test
    fun testDefaultIdentityValues() = runTest {
        repository.nicknameFlow.test {
            assertEquals(null, awaitItem()) 
        }
        repository.emojiAvatar.test {
            assertEquals("👤", awaitItem())
        }
    }

    @Test
    fun testSavingAndResettingIdentity() = runTest {
        repository.saveNickname("NewName")
        repository.nicknameFlow.test {
            assertEquals("NewName", awaitItem())
        }
        
        repository.logout()
        repository.nicknameFlow.test {
            assertEquals(null, awaitItem())
        }
    }

    @Test
    fun testStealthModeToggle() = runTest {
        repository.stealthMode.test {
            assertFalse(awaitItem())
        }
        
        repository.toggleStealth(true)
        repository.stealthMode.test {
            assertTrue(awaitItem())
        }
    }

    @Test
    fun testDeviceIdGeneration() = runTest {
        val id1 = repository.getDeviceId()
        assertTrue(id1.isNotEmpty())
        
        val id2 = repository.getDeviceId()
        assertEquals(id1, id2)
    }

    @Test
    fun testBlockingUsers() = runTest {
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
