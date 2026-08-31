package cc.thevar.blukit.domain.logic

import android.content.Context
import cc.thevar.blukit.data.repository.IdentityRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration.Companion.minutes

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class AutonomousManagerTest {

    private val context = mockk<Context>(relaxed = true)
    private val repository = mockk<IdentityRepository>(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    
    private val stealthMode = MutableStateFlow(true)
    private val lowPowerMode = MutableStateFlow(false)

    private lateinit var autonomousManager: AutonomousManager

    @Before
    fun setup() {
        every { repository.stealthMode } returns stealthMode
        every { repository.lowPowerMode } returns lowPowerMode
        
        autonomousManager = AutonomousManager(context, repository, testScope)
    }

    @Test
    fun `onUserActivity resurfaces user if in stealth mode`() = runTest {
        stealthMode.value = true
        
        autonomousManager.onUserActivity()
        
        verify { repository.toggleStealth(false) }
    }

    @Test
    fun `stealth mode activates automatically after timeout`() = testScope.runTest {
        stealthMode.value = false
        
        autonomousManager.onUserActivity() // Reset timer
        
        advanceTimeBy(6.minutes)
        
        verify { repository.toggleStealth(true) }
    }

    @Test
    fun `activity resets the stealth timeout`() = testScope.runTest {
        stealthMode.value = false
        
        autonomousManager.onUserActivity()
        
        advanceTimeBy(3.minutes)
        autonomousManager.onUserActivity()
        
        advanceTimeBy(3.minutes)
        
        // Should not have triggered stealth yet (total 6 mins passed, but reset at 3)
        verify(exactly = 0) { repository.toggleStealth(true) }
        
        advanceTimeBy(3.minutes)
        verify(exactly = 1) { repository.toggleStealth(true) }
    }
}
