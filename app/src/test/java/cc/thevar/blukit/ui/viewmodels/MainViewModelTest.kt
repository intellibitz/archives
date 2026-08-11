package cc.thevar.blukit.ui.viewmodels

import app.cash.turbine.test
import cc.thevar.blukit.data.local.dao.MessageDao
import cc.thevar.blukit.data.repository.IdentityRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private val repository: IdentityRepository = mockk(relaxed = true)
    private val messageDao: MessageDao = mockk(relaxed = true)
    private lateinit var viewModel: MainViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { repository.nickname } returns flowOf("Tester")
        viewModel = MainViewModel(repository, messageDao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `isUserRegistered reflects repository nickname state`() = runTest {
        every { repository.nickname } returns flowOf(null)
        val vm = MainViewModel(repository, messageDao)
        vm.isUserRegistered.test {
            assertEquals(false, awaitItem())
        }

        every { repository.nickname } returns flowOf("User123")
        val vm2 = MainViewModel(repository, messageDao)
        vm2.isUserRegistered.test {
            assertEquals(true, awaitItem())
        }
    }

    @Test
    fun `saveNickname calls repository setNickname`() = runTest {
        viewModel.saveNickname("NewName")
        coVerify { repository.setNickname("NewName") }
    }

    @Test
    fun `clearChatHistory calls messageDao clearAllMessages`() = runTest {
        viewModel.clearChatHistory()
        coVerify { messageDao.clearAllMessages() }
    }

    @Test
    fun `logout calls repository clearNickname`() = runTest {
        viewModel.logout()
        coVerify { repository.clearNickname() }
    }
}
