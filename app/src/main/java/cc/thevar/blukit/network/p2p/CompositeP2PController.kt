package cc.thevar.blukit.network.p2p

import cc.thevar.blukit.domain.model.ConnectionStatus
import cc.thevar.blukit.domain.model.MessagePayload
import cc.thevar.blukit.domain.model.P2PDevice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*

/**
 * Composite P2P Controller:
 * Orchestrates multiple P2P engines (Nearby Connections + BLE Fallback).
 * Implements "Best of Both Worlds" logic.
 */
class CompositeP2PController(
    private val nearbyController: NearbyP2PController,
    private val bleController: BleFallbackController,
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
    ) { nearby, ble -> nearby || ble }.stateIn(
        scope,
        SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    override val connectedTies: StateFlow<Set<String>> = combine(
        nearbyController.connectedTies,
        bleController.connectedTies
    ) { nearby, ble -> nearby + ble }.stateIn(scope, SharingStarted.WhileSubscribed(5000), emptySet())

    override val incomingRadioRequests: StateFlow<Set<P2PDevice>> = combine(
        nearbyController.incomingRadioRequests,
        bleController.incomingRadioRequests
    ) { nearby, ble ->
        (nearby.asSequence() + ble.asSequence()).distinctBy { it.id }.toSet()
    }.stateIn(scope, SharingStarted.WhileSubscribed(5000), emptySet())

    override val outgoingRadioRequests: StateFlow<Set<P2PDevice>> = combine(
        nearbyController.outgoingRadioRequests,
        bleController.outgoingRadioRequests
    ) { nearby, ble ->
        (nearby.asSequence() + ble.asSequence()).distinctBy { it.id }.toSet()
    }.stateIn(scope, SharingStarted.WhileSubscribed(5000), emptySet())

    override val isDiscovering: StateFlow<Boolean> = combine(
        nearbyController.isDiscovering,
        bleController.isDiscovering
    ) { nearby, ble -> nearby || ble }.stateIn(
        scope,
        SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    override val isAdvertising: StateFlow<Boolean> = combine(
        nearbyController.isAdvertising,
        bleController.isAdvertising
    ) { nearby, ble -> nearby || ble }.stateIn(
        scope,
        SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    override val errors: StateFlow<P2PError?> = combine(
        nearbyController.errors,
        bleController.errors
    ) { nearby, ble ->
        nearby ?: ble
    }.stateIn(scope, SharingStarted.WhileSubscribed(5000), null)

    override val messages: StateFlow<List<MessagePayload>> = nearbyController.messages
    override val syncProgress: StateFlow<Float?> = nearbyController.syncProgress

    override val discoveredCrowds: SharedFlow<cc.thevar.blukit.domain.model.Resonance> = merge(
        nearbyController.discoveredCrowds,
        bleController.discoveredCrowds
    ).shareIn(scope, SharingStarted.WhileSubscribed(5000))

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

    override suspend fun sendMessage(content: String, receiverId: String?, pulseType: Int, messageId: String?, groupId: String?, groupName: String?, type: Int): MessagePayload? {
        return nearbyController.sendMessage(content, receiverId, pulseType, messageId, groupId, groupName, type) ?: bleController.sendMessage(content, receiverId, pulseType, messageId, groupId, groupName, type)
    }

    override suspend fun broadcastMessage(content: String, pulseType: Int, messageId: String?, groupId: String?, groupName: String?, type: Int): MessagePayload? {
        val nearby = nearbyController.broadcastMessage(content, pulseType, messageId, groupId, groupName, type)
        val ble = bleController.broadcastMessage(content, pulseType, messageId, groupId, groupName, type)
        return nearby ?: ble
    }

    override suspend fun broadcastIdentityUpdate(oldName: String): MessagePayload {
        val nearby = nearbyController.broadcastIdentityUpdate(oldName)
        bleController.broadcastIdentityUpdate(oldName)
        return nearby
    }

    override suspend fun sendGroupMessage(content: String, groupId: String): MessagePayload? {
        return nearbyController.sendGroupMessage(content, groupId) ?: bleController.sendGroupMessage(content, groupId)
    }

    override suspend fun sendNoteUpdate(groupId: String, content: String, messageId: String?, version: Int): MessagePayload? {
        return nearbyController.sendNoteUpdate(groupId, content, messageId, version) ?: bleController.sendNoteUpdate(groupId, content, messageId, version)
    }

    override suspend fun sendFile(fileUri: android.net.Uri, receiverId: String?, pulseType: Int, groupId: String?, groupName: String?): MessagePayload? {
        return nearbyController.sendFile(fileUri, receiverId, pulseType, groupId, groupName)
    }

    override fun startGroupPulse(name: String, members: Set<String>, type: Int, groupId: String?, parentId: String?): String {
        return nearbyController.startGroupPulse(name, members, type, groupId, parentId)
    }

    override fun updateGroupMembers(groupId: String, memberIds: Set<String>) {
        nearbyController.updateGroupMembers(groupId, memberIds)
        bleController.updateGroupMembers(groupId, memberIds)
    }

    override fun updateGroupScope(groupId: String, scope: Int) {
        nearbyController.updateGroupScope(groupId, scope)
        bleController.updateGroupScope(groupId, scope)
    }

    override fun initiateHistorySync(endpointId: String, sinceTimestamp: Long?) {
        nearbyController.initiateHistorySync(endpointId, sinceTimestamp)
    }

    override fun requestRadio(device: P2PDevice) {
        if (device.id.startsWith("nearby:")) {
            nearbyController.requestRadio(device)
        } else {
            bleController.requestRadio(device)
        }
    }

    override fun isNearbyConnected(endpointId: String): Boolean = 
        nearbyController.isNearbyConnected(endpointId) || bleController.isNearbyConnected(endpointId)
    override fun acceptRadio(device: P2PDevice) {
        if (device.id.startsWith("nearby:")) {
            nearbyController.acceptRadio(device)
        } else {
            bleController.acceptRadio(device)
        }
    }

    override fun denyRadio(device: P2PDevice) {
        if (device.id.startsWith("nearby:")) {
            nearbyController.denyRadio(device)
        } else {
            bleController.denyRadio(device)
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
