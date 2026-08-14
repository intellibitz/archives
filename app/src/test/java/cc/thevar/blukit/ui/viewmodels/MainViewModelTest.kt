package cc.thevar.blukit.ui.viewmodels

import cc.thevar.blukit.data.local.dao.MessageDao
import cc.thevar.blukit.data.repository.IdentityRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private lateinit var repository: IdentityRepository
    private val messageDao: MessageDao = mockk(relaxed = true)
    private lateinit var viewModel: MainViewModel
    
    private val nicknameFlow = MutableStateFlow<String?>("vibe")
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        every { repository.nicknameFlow } returns nicknameFlow
        every { repository.emojiAvatar } returns MutableStateFlow("👤")
        every { repository.stealthMode } returns MutableStateFlow(false)
        every { repository.blockedUsers } returns MutableStateFlow(emptySet())
        every { repository.getDeviceId() } returns "test-id"
        
        viewModel = MainViewModel(repository, messageDao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test saveNickname calls repository`() = runTest {
        viewModel.saveNickname("NewName")
        verify { repository.saveNickname("NewName") }
    }

    @Test
    fun `test clearChatHistory calls messageDao`() = runTest {
        viewModel.clearChatHistory()
        coVerify { messageDao.clearAllMessages() }
    }

    @Test
    fun `test logout calls repository logout`() = runTest {
        viewModel.logout()
        verify { repository.logout() }
    }
}
