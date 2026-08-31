package cc.thevar.blukit.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.test.platform.app.InstrumentationRegistry
import cc.thevar.blukit.data.local.MessageRepository
import cc.thevar.blukit.data.repository.ContactRepository
import cc.thevar.blukit.data.repository.IdentityRepository
import cc.thevar.blukit.data.system.HapticManager
import cc.thevar.blukit.data.system.RadioStateManager
import cc.thevar.blukit.data.system.RadioStates
import cc.thevar.blukit.data.system.SpreadPermissionManager
import cc.thevar.blukit.domain.model.Message
import cc.thevar.blukit.domain.model.Source
import cc.thevar.blukit.domain.model.Group
import cc.thevar.blukit.domain.power.HarmonyReport
import cc.thevar.blukit.domain.logic.AssistantManager
import cc.thevar.blukit.domain.usecase.ConnectivityUseCase
import cc.thevar.blukit.network.p2p.ConnectionController
import cc.thevar.blukit.network.p2p.ConnectionError
import cc.thevar.blukit.ui.theme.BlukitTheme
import cc.thevar.blukit.ui.viewmodels.ConnectionViewModel
import cc.thevar.blukit.ui.viewmodels.MainViewModel
import cc.thevar.blukit.ui.viewmodels.HarmonyViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import java.util.UUID

@OptIn(ExperimentalTestApi::class)
class FlowsTest : KoinTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val repository: IdentityRepository = mockk(relaxed = true)
    private val contactRepository: ContactRepository = mockk(relaxed = true)
    private val messageRepository: MessageRepository = mockk(relaxed = true)
    private val radioStateManager: RadioStateManager = mockk(relaxed = true)
    private val connectionController: ConnectionController = mockk(relaxed = true)
    private val harmonyManager: cc.thevar.blukit.data.power.HarmonyManager = mockk(relaxed = true)
    private val permissionManager: SpreadPermissionManager = mockk(relaxed = true)
    private val connectivityUseCase: ConnectivityUseCase = mockk(relaxed = true)
    private val hapticManager: HapticManager = mockk(relaxed = true)
    private val assistantManager: AssistantManager = mockk(relaxed = true)

    private val radioStatesFlow = MutableStateFlow(RadioStates(isBluetoothEnabled = true, isLocationEnabled = true, isWifiEnabled = true))
    private val scannedSourcesFlow = MutableStateFlow<List<Source>>(emptyList())
    private val connectedGroupsFlow = MutableStateFlow<Set<String>>(emptySet())
    private val incomingRequestsFlow = MutableStateFlow<Set<Source>>(emptySet())
    private val isConnectedFlow = MutableStateFlow(false)
    private val messagesFlow = MutableStateFlow<List<Message>>(emptyList())
    private val isAdvertisingFlow = MutableStateFlow(false)
    private val errorsFlow = MutableStateFlow<ConnectionError?>(null)
    private val reportFlow = MutableStateFlow(HarmonyReport())
    private val groupsFlow = MutableStateFlow<List<Group>>(emptyList())
    private val permissionsGrantedFlow = MutableStateFlow(true)

    private val testModule = module {
        single(createdAtStart = true) { repository }
        single(createdAtStart = true) { contactRepository }
        single(createdAtStart = true) { messageRepository }
        single(createdAtStart = true) { radioStateManager }
        single(createdAtStart = true) { connectionController }
        single(createdAtStart = true) { harmonyManager }
        single(createdAtStart = true) { permissionManager }
        single(createdAtStart = true) { connectivityUseCase }
        single(createdAtStart = true) { hapticManager }
        single(createdAtStart = true) { assistantManager }

        viewModelOf(::MainViewModel)
        viewModelOf(::ConnectionViewModel)
        viewModelOf(::HarmonyViewModel)
    }

    @Before
    fun setUp() {
        if (GlobalContext.getOrNull() != null) {
            stopKoin()
        }

        every { repository.nicknameFlow } returns MutableStateFlow("pulse")
        every { repository.emojiAvatar } returns MutableStateFlow("👤")
        every { repository.stealthMode } returns MutableStateFlow(false)
        every { repository.lowPowerMode } returns MutableStateFlow(false)
        every { repository.blockedUsers } returns MutableStateFlow(emptySet())
        every { repository.pulsedPeers } returns MutableStateFlow(emptySet())
        every { repository.getDeviceId() } returns "test-id"
        every { repository.getCurrentNickname() } returns "pulse"

        every { connectionController.scannedDevices } returns scannedSourcesFlow
        every { connectionController.connectedGroups } returns connectedGroupsFlow
        every { connectionController.incomingRadioRequests } returns incomingRequestsFlow
        every { connectionController.outgoingRadioRequests } returns MutableStateFlow(emptySet())
        every { connectionController.isDiscovering } returns MutableStateFlow(true)
        every { connectionController.errors } returns errorsFlow
        every { connectionController.isConnected } returns isConnectedFlow
        every { connectionController.messages } returns messagesFlow
        every { connectionController.isAdvertising } returns isAdvertisingFlow
        every { connectionController.discoveredGroups } returns MutableSharedFlow<Group>()
        every { connectionController.syncProgress } returns MutableStateFlow(null)
        
        coEvery { connectionController.sendMessage(any(), any(), any(), any(), any(), any(), any()) } answers {
            val content = firstArg<String>()
            val receiver = secondArg<String?>()
            val newMessage = Message(
                messageId = UUID.randomUUID().toString(),
                senderId = "test-id",
                senderName = "user",
                content = content,
                timestamp = System.currentTimeMillis(),
                receiverId = receiver
            )
            messagesFlow.value = messagesFlow.value + newMessage
            newMessage
        }

        every { radioStateManager.radioStates } returns radioStatesFlow
        every { harmonyManager.report } returns reportFlow
        
        every { messageRepository.groups } returns groupsFlow
        every { messageRepository.activeGroups } returns groupsFlow
        every { messageRepository.archivedGroups } returns MutableStateFlow(emptyList())
        every { messageRepository.vaultedGroups } returns MutableStateFlow(emptyList())
        every { messageRepository.messages } returns messagesFlow
        every { messageRepository.getAllMessages() } returns messagesFlow
        coEvery { messageRepository.getGroup(any()) } returns null
        every { messageRepository.autoArchiveGroups() } returns Unit
        coEvery { messageRepository.pruneMedia(any()) } returns Unit
        coEvery { messageRepository.updateGroupLastMessage(any(), any()) } returns Unit

        every { permissionManager.requiredPermissions } returns listOf(android.Manifest.permission.BLUETOOTH_SCAN)
        every { permissionManager.essentialPermissions } returns listOf(android.Manifest.permission.BLUETOOTH_SCAN)
        every { permissionManager.permissionsGranted } returns permissionsGrantedFlow

        startKoin {
            allowOverride(true)
            androidContext(InstrumentationRegistry.getInstrumentation().targetContext)
            modules(testModule)
        }
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun testNavigateToPulseAndChangeIdentity() {
        startApp()
        
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("IdentityInput"), 30000)
        composeTestRule.onNodeWithTag("IdentityInput", useUnmergedTree = true).performTextReplacement("Quantum")
        
        verify { repository.saveNickname("Quantum") }
    }

    @Test
    fun testHarmonyCheck() {
        radioStatesFlow.value = RadioStates(isBluetoothEnabled = false, isLocationEnabled = true, isWifiEnabled = true)
        
        startApp()
        
        composeTestRule.waitUntilAtLeastOneExists(hasText("AWAKE", ignoreCase = true, substring = true), 20000)
        composeTestRule.onAllNodesWithText("AWAKE", ignoreCase = true, substring = true, useUnmergedTree = true).onFirst().performClick()
    }

    private fun startApp() {
        composeTestRule.setContent {
            BlukitTheme {
                BlukitApp()
            }
        }
        composeTestRule.waitForIdle()
    }
}
