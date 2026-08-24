package cc.thevar.blukit.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import cc.thevar.blukit.data.local.VibeStore
import cc.thevar.blukit.data.repository.ContactRepository
import cc.thevar.blukit.data.repository.IdentityRepository
import cc.thevar.blukit.data.system.RadioStateManager
import cc.thevar.blukit.network.p2p.P2PController
import cc.thevar.blukit.ui.theme.BlukitTheme
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.androidx.viewmodel.dsl.viewModel
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class NavigationTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val repository: IdentityRepository = mockk(relaxed = true)
    private val contactRepository: ContactRepository = mockk(relaxed = true)
    private val vibeStore: VibeStore = mockk(relaxed = true)
    private val radioStateManager: RadioStateManager = mockk(relaxed = true)
    private val p2pController: P2PController = mockk(relaxed = true)
    private val permissionManager: cc.thevar.blukit.data.system.SpreadPermissionManager = mockk(relaxed = true)
    private val spm: cc.thevar.blukit.data.power.SupremePowerManager = mockk(relaxed = true)
    private val connectivityUseCase: cc.thevar.blukit.domain.usecase.ConnectivityUseCase = mockk(relaxed = true)

    @Before
    fun setUp() {
        stopKoin()
        
        every { repository.nicknameFlow } returns MutableStateFlow("vibe")
        every { repository.emojiAvatar } returns MutableStateFlow("👤")
        every { repository.stealthMode } returns MutableStateFlow(false)
        every { repository.lowPowerMode } returns MutableStateFlow(false)
        every { repository.blockedUsers } returns MutableStateFlow(emptySet())
        every { repository.getDeviceId() } returns "test-device-id"
        every { repository.getCurrentNickname() } returns "vibe"
        every { repository.vibedPeers } returns MutableStateFlow(emptySet())
        
        every { p2pController.scannedDevices } returns MutableStateFlow(emptyList())
        every { p2pController.connectedLinks } returns MutableStateFlow(emptySet())
        every { p2pController.incomingLinkRequests } returns MutableStateFlow(emptySet())
        every { p2pController.outgoingLinkRequests } returns MutableStateFlow(emptySet())
        every { p2pController.isDiscovering } returns MutableStateFlow(false)
        every { p2pController.isAdvertising } returns MutableStateFlow(false)
        every { p2pController.errors } returns MutableStateFlow(null)
        every { p2pController.isConnected } returns MutableStateFlow(false)
        every { p2pController.messages } returns MutableStateFlow(emptyList())
        every { p2pController.discoveredAirs } returns MutableSharedFlow()
        
        every { radioStateManager.radioStates } returns MutableStateFlow(cc.thevar.blukit.data.system.RadioStates(false, false, false))
        
        every { vibeStore.activeGroups } returns MutableStateFlow(listOf(cc.thevar.blukit.domain.model.VibeGroup(id = cc.thevar.blukit.domain.model.VibeGroup.ID_AIR, name = "STADIUM")))
        every { vibeStore.archivedGroups } returns MutableStateFlow(emptyList())
        every { vibeStore.getAllMessages() } returns MutableStateFlow(emptyList())

        every { permissionManager.requiredPermissions } returns emptyList()
        every { permissionManager.essentialPermissions } returns emptyList()
        every { permissionManager.permissionsGranted } returns MutableStateFlow(true)

        every { spm.report } returns MutableStateFlow(cc.thevar.blukit.domain.power.SupremePowerReport())
        
        every { connectivityUseCase.manualConnectionStatus } returns MutableStateFlow(null)

        startKoin {
            modules(module {
                single { repository }
                single { vibeStore }
                single { radioStateManager }
                single { p2pController }
                single { permissionManager }
                single { spm }
                single { connectivityUseCase }
                viewModel { cc.thevar.blukit.ui.viewmodels.MainViewModel(get(), get()) }
                viewModel { cc.thevar.blukit.ui.viewmodels.BluetoothViewModel(get(), get(), get(), get(), get(), get()) }
                viewModel { cc.thevar.blukit.ui.viewmodels.SupremePowerViewModel(get()) }
            })
        }
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun testBottomNavigation() {
        startApp()
        composeTestRule.waitForIdle()
        
        // Use tags from BlukitHarmonyTopBar / BlukitVibeHub
        // In the current implementation, navigation is handled by backStack.add()
    }

    private fun startApp() {
        composeTestRule.setContent {
            BlukitTheme {
                BlukitApp(
                    onEnterPip = {}
                )
            }
        }
    }
}
