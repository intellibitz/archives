package cc.thevar.blukit.network.p2p

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeAdvertiser
import android.bluetooth.le.BluetoothLeScanner
import android.content.Context
import cc.thevar.blukit.data.crypto.CryptoManager
import cc.thevar.blukit.data.local.VibeStore
import cc.thevar.blukit.data.repository.IdentityRepository
import cc.thevar.blukit.data.system.HapticManager
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BleFallbackControllerTest {

    private val context: Context = mockk(relaxed = true)
    private val repository: IdentityRepository = mockk(relaxed = true)
    private val vibeStore: VibeStore = mockk(relaxed = true)
    private val hapticManager: HapticManager = mockk(relaxed = true)
    private val cryptoManager: CryptoManager = mockk(relaxed = true)
    private val bluetoothManager: BluetoothManager = mockk(relaxed = true)
    private val bluetoothAdapter: BluetoothAdapter = mockk(relaxed = true)
    private val bleScanner: BluetoothLeScanner = mockk(relaxed = true)
    private val bleAdvertiser: BluetoothLeAdvertiser = mockk(relaxed = true)

    private lateinit var controller: BleFallbackController
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        
        every { context.getSystemService(BluetoothManager::class.java) } returns bluetoothManager
        every { bluetoothManager.adapter } returns bluetoothAdapter
        every { bluetoothAdapter.isEnabled } returns true
        every { bluetoothAdapter.bluetoothLeScanner } returns bleScanner
        every { bluetoothAdapter.bluetoothLeAdvertiser } returns bleAdvertiser
        
        every { vibeStore.getAllMessages() } returns MutableStateFlow(emptyList())
        every { repository.getDeviceId() } returns "test-ble-id"
        every { repository.getCurrentNickname() } returns "BleUser"
        every { repository.emojiAvatar } returns MutableStateFlow("👤")

        controller = BleFallbackController(
            context, repository, vibeStore, hapticManager, cryptoManager, testDispatcher, testDispatcher
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `startDiscovery calls BLE scanner`() = runTest(testDispatcher) {
        controller.startDiscovery()
        runCurrent()
        verify { bleScanner.startScan(any<List<android.bluetooth.le.ScanFilter>>(), any<android.bluetooth.le.ScanSettings>(), any<android.bluetooth.le.ScanCallback>()) }
    }

    @Test
    fun `stopDiscovery calls BLE scanner stop`() = runTest(testDispatcher) {
        controller.startDiscovery()
        runCurrent()
        controller.stopDiscovery()
        runCurrent()
        verify { bleScanner.stopScan(any<android.bluetooth.le.ScanCallback>()) }
    }

    @Test
    fun `startAdvertising calls BLE advertiser`() = runTest(testDispatcher) {
        controller.startAdvertising()
        runCurrent()
        verify { bleAdvertiser.startAdvertising(any(), any(), any()) }
    }

    @Test
    fun `stopAdvertising calls BLE advertiser stop`() = runTest(testDispatcher) {
        controller.startAdvertising()
        runCurrent()
        controller.stopAdvertising()
        runCurrent()
        verify { bleAdvertiser.stopAdvertising(any<android.bluetooth.le.AdvertiseCallback>()) }
    }
}
