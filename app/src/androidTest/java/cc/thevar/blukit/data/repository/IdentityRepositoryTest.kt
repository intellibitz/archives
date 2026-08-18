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
    fun testVibedPeersManagement() = runTest {
        repository.vibedPeers.test {
            assertTrue(awaitItem().isEmpty())
        }
        
        repository.toggleVibePeer("peer1")
        repository.vibedPeers.test {
            assertTrue(awaitItem().contains("peer1"))
        }
        
        repository.toggleVibePeer("peer1") // Toggle off
        repository.vibedPeers.test {
            assertTrue(awaitItem().isEmpty())
        }
    }

    @Test
    fun testLowPowerModeToggle() = runTest {
        repository.lowPowerMode.test {
            assertFalse(awaitItem())
        }
        
        repository.toggleLowPowerMode(true)
        repository.lowPowerMode.test {
            assertTrue(awaitItem())
        }
    }

    @Test
    fun testGetCurrentNickname() = runTest {
        assertEquals("?", repository.getCurrentNickname())
        
        repository.saveNickname("Alice")
        assertEquals("Alice", repository.getCurrentNickname())
    }

    @Test
    fun testPersistenceAcrossReinitialization() = runTest {
        repository.saveNickname("Alice")
        repository.saveEmoji("🌹")
        
        // Re-init repository
        val newRepo = IdentityRepositoryImpl(ApplicationProvider.getApplicationContext())
        assertEquals("Alice", newRepo.getCurrentNickname())
        assertEquals("🌹", newRepo.emojiAvatar.value)
    }
}
