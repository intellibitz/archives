package cc.thevar.blukit.data.repository

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class IdentityRepositoryTest {

    private lateinit var repository: IdentityRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        repository = IdentityRepository(context)
    }

    @Test
    fun testNicknamePersistence() = runBlocking {
        val name = "TestUser_${System.currentTimeMillis()}"
        repository.setNickname(name)
        
        assertEquals(name, repository.getNickname())
        
        repository.nickname.test {
            assertEquals(name, awaitItem())
        }
    }

    @Test
    fun testStealthModePersistence() = runBlocking {
        repository.setStealthMode(true)
        repository.stealthMode.test {
            assertTrue(awaitItem())
        }
        
        repository.setStealthMode(false)
        repository.stealthMode.test {
            assertFalse(awaitItem())
        }
    }

    @Test
    fun testDeviceIdGeneration() = runBlocking {
        val id1 = repository.getDeviceId()
        assertNotNull(id1)
        
        val id2 = repository.getDeviceId()
        assertEquals(id1, id2) // Should remain same once generated
    }
}
