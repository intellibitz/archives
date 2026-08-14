package cc.thevar.blukit.network.p2p

import cc.thevar.blukit.domain.model.ConnectionStatus
import cc.thevar.blukit.domain.model.MessagePayload
import cc.thevar.blukit.domain.model.P2PDevice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Composite P2P Controller:
 * Orchestrates multiple P2P engines (Nearby Connections + BLE Fallback).
 * Implements "Best of Both Worlds" logic.
 */
class CompositeP2PController(
    private val nearbyController: NearbyP2PController,
    private val bleController: BleFallbackController
) : P2PController {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override val scannedDevices: StateFlow<List<P2PDevice>> = combine(
        nearbyController.scannedDevices,
        bleController.scannedDevices
    ) { nearby, ble ->
        (nearby + ble).distinctBy { it.id }
    }.stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    override val isConnected: StateFlow<Boolean> = combine(
        nearbyController.isConnected,
        bleController.isConnected
    ) { nearby, ble -> nearby || ble }.stateIn(scope, SharingStarted.WhileSubscribed(5000), false)

    override val connectedLinks: StateFlow<Set<String>> = combine(
        nearbyController.connectedLinks,
        bleController.connectedLinks
    ) { nearby, ble -> nearby + ble }.stateIn(scope, SharingStarted.WhileSubscribed(5000), emptySet())

    override val incomingLinkRequests: StateFlow<Set<P2PDevice>> = combine(
        nearbyController.incomingLinkRequests,
        bleController.incomingLinkRequests
    ) { nearby, ble ->
        (nearby + ble).distinctBy { it.id }.toSet()
    }.stateIn(scope, SharingStarted.WhileSubscribed(5000), emptySet())

    override val isDiscovering: StateFlow<Boolean> = combine(
        nearbyController.isDiscovering,
        bleController.isDiscovering
    ) { nearby, ble -> nearby || ble }.stateIn(scope, SharingStarted.WhileSubscribed(5000), false)

    override val isAdvertising: StateFlow<Boolean> = combine(
        nearbyController.isAdvertising,
        bleController.isAdvertising
    ) { nearby, ble -> nearby || ble }.stateIn(scope, SharingStarted.WhileSubscribed(5000), false)

    override val errors: StateFlow<String> = combine(
        nearbyController.errors,
        bleController.errors
    ) { nearby, ble ->
        nearby.takeIf { it.isNotEmpty() } ?: ble
    }.stateIn(scope, SharingStarted.WhileSubscribed(5000), "")

    override val messages: StateFlow<List<MessagePayload>> = nearbyController.messages

    override fun startDiscovery() {
        nearbyController.startDiscovery()
        bleController.startDiscovery()
    }

    override fun stopDiscovery() {
        nearbyController.stopDiscovery()
        bleController.stopDiscovery()
    }

    override fun startAdvertising() {
        nearbyController.startAdvertising()
        bleController.startAdvertising()
    }

    override fun stopAdvertising() {
        nearbyController.stopAdvertising()
        bleController.stopAdvertising()
    }

    override fun connectToDevice(device: P2PDevice): SharedFlow<ConnectionStatus> {
        return if (device.id.startsWith("nearby:")) { // Example tagging
            nearbyController.connectToDevice(device)
        } else {
            bleController.connectToDevice(device)
        }
    }

    override suspend fun sendMessage(content: String, receiverId: String?): MessagePayload? {
        return nearbyController.sendMessage(content, receiverId) ?: bleController.sendMessage(content, receiverId)
    }

    override suspend fun broadcastMessage(content: String): MessagePayload? {
        val nearby = nearbyController.broadcastMessage(content)
        val ble = bleController.broadcastMessage(content)
        return nearby ?: ble
    }

    override fun requestLink(device: P2PDevice) {
        if (device.id.startsWith("nearby:")) {
            nearbyController.requestLink(device)
        } else {
            bleController.requestLink(device)
        }
    }

    override fun acceptLink(device: P2PDevice) {
        if (device.id.startsWith("nearby:")) {
            nearbyController.acceptLink(device)
        } else {
            bleController.acceptLink(device)
        }
    }

    override fun denyLink(device: P2PDevice) {
        if (device.id.startsWith("nearby:")) {
            nearbyController.denyLink(device)
        } else {
            bleController.denyLink(device)
        }
    }

    override fun closeConnection() {
        nearbyController.closeConnection()
        bleController.closeConnection()
    }

    override fun release() {
        nearbyController.release()
        bleController.release()
    }
}
