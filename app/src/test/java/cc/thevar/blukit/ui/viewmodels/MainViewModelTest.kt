package cc.thevar.blukit.ui.viewmodels

import app.cash.turbine.test
import cc.thevar.blukit.data.local.dao.MessageDao
import cc.thevar.blukit.data.repository.IdentityRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private val repository: IdentityRepository = mockk(relaxed = true)
    private val messageDao: MessageDao = mockk(relaxed = true)
    private lateinit var viewModel: MainViewModel
    
    private val nicknameFlow = MutableStateFlow("vibe")
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { repository.nickname } returns nicknameFlow
        viewModel = MainViewModel(repository, messageDao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test isUserRegistered logic - default name is NOT registered`() = runTest {
        viewModel.isUserRegistered.test {
            assertFalse(awaitItem())
        }
    }

    @Test
    fun `test isUserRegistered logic - custom name IS registered`() = runTest {
        nicknameFlow.value = "RealUser"
        viewModel.isUserRegistered.test {
            assertTrue(awaitItem())
        }
    }

    @Test
    fun `test saveNickname calls repository`() = runTest {
        viewModel.saveNickname("NewName")
        verify { repository.setNickname("NewName") }
    }

    @Test
    fun `test clearChatHistory calls messageDao`() = runTest {
        viewModel.clearChatHistory()
        coVerify { messageDao.clearAllMessages() }
    }

    @Test
    fun `test logout clears nickname`() = runTest {
        viewModel.logout()
        verify { repository.clearNickname() }
    }
}
