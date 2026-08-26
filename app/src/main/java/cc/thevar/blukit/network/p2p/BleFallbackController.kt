/**
 * BLUKIT NETWORK: BLE FALLBACK CONTROLLER
 *
 * A native BLE engine used when Google Nearby Connections is unavailable or fails.
 * Implements standard BLE GATT (Generic Attribute Profile) for pulse exchange.
 * 
 * Logic:
 * - Advertising: Local Persona info is shared via ScanRecord service data.
 * - Discovery: Scans for SERVICE_UUID and extracts peer Persona metadata.
 * - Messaging: Writes pulses to the PULSE_CHAR_UUID characteristic on the peer's GATT server.
 * - Security: Hardware-encrypted ECDH/AES handshakes similar to the primary engine.
 */
package cc.thevar.blukit.network.p2p

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import android.os.ParcelUuid
import android.util.Log
import cc.thevar.blukit.data.crypto.CryptoManager
import cc.thevar.blukit.data.local.PulseStore
import cc.thevar.blukit.data.repository.IdentityRepository
import cc.thevar.blukit.data.system.HapticManager
import cc.thevar.blukit.domain.model.ConnectionStatus
import cc.thevar.blukit.domain.model.MessagePayload
import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.domain.model.Resonance
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import java.util.Collections
import java.util.LinkedList
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import javax.crypto.SecretKey

/**
 * Native BLE engine for tactical fallback.
 */
class BleFallbackController(
    private val context: Context,
    private val repository: IdentityRepository,
    private val pulseStore: PulseStore,
    private val hapticManager: HapticManager,
    private val cryptoManager: CryptoManager = CryptoManager(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
) : P2PController {

    private val tag = "BlukitBLE"
    private val bluetoothManager = context.getSystemService(BluetoothManager::class.java)
    private val adapter = bluetoothManager?.adapter

    private val internalScope = CoroutineScope(SupervisorJob() + mainDispatcher)

    // --- P2PController State Implementation ---
    private val _scannedDevices = MutableStateFlow<List<P2PDevice>>(emptyList())
    override val scannedDevices = _scannedDevices.asStateFlow()

    private val _isConnected = MutableStateFlow(value = false)
    override val isConnected = _isConnected.asStateFlow()

    private val _connectedTies = MutableStateFlow<Set<String>>(emptySet())
    override val connectedTies = _connectedTies.asStateFlow()

    private val _incomingRadioRequests = MutableStateFlow<Set<P2PDevice>>(emptySet())
    override val incomingRadioRequests = _incomingRadioRequests.asStateFlow()

    private val _outgoingRadioRequests = MutableStateFlow<Set<P2PDevice>>(emptySet())
    override val outgoingRadioRequests = _outgoingRadioRequests.asStateFlow()

    private val _isDiscovering = MutableStateFlow(value = false)
    override val isDiscovering = _isDiscovering.asStateFlow()

    private val _isAdvertising = MutableStateFlow(value = false)
    override val isAdvertising = _isAdvertising.asStateFlow()

    private val _errors = MutableStateFlow<P2PError?>(null)
    override val errors = _errors.asStateFlow()

    private val _discoveredCrowds = MutableSharedFlow<Resonance>(extraBufferCapacity = 5)
    override val discoveredCrowds = _discoveredCrowds.asSharedFlow()

    override val messages: StateFlow<List<MessagePayload>> = pulseStore.getAllMessages()
    override val syncProgress: StateFlow<Float?> = MutableStateFlow(null)

    private val pulseKeys = ConcurrentHashMap<String, SecretKey>()
    private val activeGatts = ConcurrentHashMap<String, BluetoothGatt>()
    private var gattServer: BluetoothGattServer? = null
    private val messageIdHistory = Collections.synchronizedList(LinkedList<String>())
    private val pendingRadioRequests = Collections.synchronizedSet(mutableSetOf<String>())

    companion object {
        /** Unique Service UUID for Blukit BLE mesh. */
        private val SERVICE_UUID = UUID.fromString("0000fb01-0000-1000-8000-00805f9b34fb")
        /** Characteristic UUID for writing encrypted pulses. */
        private val PULSE_CHAR_UUID = UUID.fromString("0000fb02-0000-1000-8000-00805f9b34fb")
        /** Protocol prefix for ECDH handshake packets. */
        private const val HANDSHAKE_PREFIX = 0x01.toByte()
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            val scanRecord = result.scanRecord ?: return
            val serviceData = scanRecord.serviceData[ParcelUuid(SERVICE_UUID)] ?: return
            
            // Extract peer Persona from service data (Nickname|Emoji|ID)
            val info = String(serviceData, StandardCharsets.UTF_8)
            val parts = info.split("|", limit = 3)
            if (parts.size < 3) return

            val newDevice = P2PDevice(
                id = device.address,
                name = parts[1],
                emoji = parts[0],
                signalStrength = result.rssi,
            )
            _scannedDevices.update { current ->
                current.filter { it.id != device.address } + newDevice
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e(tag, "Scan failed: $errorCode")
            _isDiscovering.value = false
        }
    }

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            Log.i(tag, "Advertise success")
        }

        override fun onStartFailure(errorCode: Int) {
            Log.e(tag, "Advertise failed: $errorCode")
            _isAdvertising.value = false
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            handleGattConnectionChange(gatt, newState)
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            handleGattServicesDiscovered(gatt, status)
        }
    }

    private val gattServerCallback = object : BluetoothGattServerCallback() {
        override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
            handleGattServerConnectionChange(device, newState)
        }

        override fun onCharacteristicWriteRequest(
            device: BluetoothDevice,
            requestId: Int,
            characteristic: BluetoothGattCharacteristic,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray,
        ) {
            handleCharacteristicWrite(device, requestId, characteristic, responseNeeded, value)
        }
    }

    /** Manages the client-side GATT connection lifecycle. */
    private fun handleGattConnectionChange(gatt: BluetoothGatt, newState: Int) {
        val address = gatt.device.address
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            Log.i(tag, "GATT: Connected to $address")
            try {
                gatt.discoverServices()
            } catch (_: SecurityException) {
                reportError(P2PError.ConnectionError("Permission Denied for Service Discovery"))
            }
        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            Log.i(tag, "GATT: Disconnected from $address")
            activeGatts.remove(address)
            pendingRadioRequests.remove(address)
            _connectedTies.update { it - address }
            if (activeGatts.isEmpty()) _isConnected.value = false
            updateScannedDevices()
        }
    }

    /** Triggers secure handshake once Blukit service is discovered on peer. */
    private fun handleGattServicesDiscovered(gatt: BluetoothGatt, status: Int) {
        val address = gatt.device.address
        if (status == BluetoothGatt.GATT_SUCCESS) {
            activeGatts[address] = gatt
            _connectedTies.update { it + address }
            _isConnected.value = true
            sendHandshake(address)
        } else {
            Log.e(tag, "Service discovery failed with status: $status")
            reportError(P2PError.ConnectionError("Service discovery failed"))
        }
    }

    private fun handleGattServerConnectionChange(device: BluetoothDevice, newState: Int) {
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            Log.i(tag, "GATT Server: Connected to ${device.address}")
        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            Log.i(tag, "GATT Server: Disconnected from ${device.address}")
            pulseKeys.remove(device.address)
        }
    }

    /** Handles incoming GATT writes (Encrypted Pulses or Handshakes). */
    private fun handleCharacteristicWrite(
        device: BluetoothDevice,
        requestId: Int,
        characteristic: BluetoothGattCharacteristic,
        responseNeeded: Boolean,
        value: ByteArray,
    ) {
        if (characteristic.uuid == PULSE_CHAR_UUID) {
            if (responseNeeded) {
                try {
                    gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null)
                } catch (_: SecurityException) {}
            }
            handleReceivedData(device.address, value)
        }
    }

    private fun handleReceivedData(address: String, data: ByteArray) {
        if (data.isNotEmpty() && (data[0] == HANDSHAKE_PREFIX)) {
            handleHandshake(address, data)
        } else {
            handleMessage(address, data)
        }
    }

    private fun handleHandshake(address: String, data: ByteArray) {
        try {
            val publicKeyEncoded = data.copyOfRange(1, data.size)
            val keyFactory = KeyFactory.getInstance("EC")
            val pulsePublicKey = keyFactory.generatePublic(X509EncodedKeySpec(publicKeyEncoded))
            val sharedSecret = cryptoManager.deriveSharedSecret(pulsePublicKey)
            pulseKeys[address] = sharedSecret
            Log.i(tag, "SECURE: Radio ready for $address (BLE)")
            updateScannedDevices()
        } catch (e: Exception) {
            Log.e(tag, "Handshake error: ${e.message}")
        }
    }

    private fun handleMessage(address: String, data: ByteArray) {
        val secretKey = pulseKeys[address] ?: return
        try {
            val decryptedBytes = cryptoManager.decrypt(data, secretKey)
            val payload = Json.decodeFromString<MessagePayload>(decryptedBytes.decodeToString())

            when (payload.type) {
                MessagePayload.TYPE_ACK -> handleAck(payload)
                else -> {
                    if (isNewMessage(payload.messageId) && (payload.senderId !in repository.blockedUsers.value)) {
                        handleChatMessage(address, payload, secretKey)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Message decrypt error: ${e.message}")
            reportError(P2PError.EncryptionError("Failed to decrypt incoming pulse"))
        }
    }

    private fun handleAck(payload: MessagePayload) {
        internalScope.launch(ioDispatcher) {
            pulseStore.updateMessageStatus(payload.messageId, MessagePayload.STATUS_DELIVERED)
        }
    }

    private fun handleChatMessage(address: String, payload: MessagePayload, secretKey: SecretKey) {
        sendAck(address, payload, secretKey)
        if (payload.receiverId.isNullOrEmpty()) {
            relayMessage(address, payload)
        }

        internalScope.launch(ioDispatcher) {
            // AUTO-DISCOVER RESONANCES
            val gid = payload.groupId
            val gName = payload.groupName
            if ((gid != null) && (gName != null)) {
                val existing = pulseStore.getGroup(gid)
                if (existing == null) {
                    val scope = when (payload.pulseType) {
                        MessagePayload.PULSE_SHOUT -> Resonance.SCOPE_PUBLIC
                        MessagePayload.PULSE_SILENCE -> Resonance.SCOPE_LOCAL
                        else -> Resonance.SCOPE_PRIVATE
                    }
                    pulseStore.insertGroup(
                        Resonance(
                            id = gid,
                            name = gName,
                            scope = scope,
                            parentId = Resonance.ID_CROWD,
                        ),
                    )
                }
            }

            pulseStore.upsertMessage(payload)
            hapticManager.triggerPulse(HapticManager.PulseType.MESSAGE)
        }
    }

    /** Propagates pulses to other connected BLE ties. */
    private fun relayMessage(sourceAddress: String, payload: MessagePayload) {
        if (payload.hopCount >= 3) return

        internalScope.launch(ioDispatcher) {
            val myId = repository.getDeviceId()
            if (payload.senderId == myId) return@launch

            val relayedPayload = payload.copy(hopCount = payload.hopCount + 1)
            val json = Json.encodeToString(MessagePayload.serializer(), relayedPayload)
            val bytes = json.encodeToByteArray()

            activeGatts.forEach { (address, _) ->
                if (address != sourceAddress) {
                    pulseKeys[address]?.let { key ->
                        try {
                            val encrypted = cryptoManager.encrypt(bytes, key)
                            sendData(address, encrypted)
                        } catch (_: Exception) {
                            Log.e(tag, "BLE RELAY FAIL to $address")
                        }
                    }
                }
            }
        }
    }

    private fun sendAck(address: String, originalPayload: MessagePayload, secretKey: SecretKey) {
        val ack = MessagePayload(
            messageId = originalPayload.messageId,
            senderId = repository.getDeviceId(),
            senderName = "",
            content = "",
            timestamp = System.currentTimeMillis(),
            type = MessagePayload.TYPE_ACK,
            receiverId = originalPayload.senderId,
        )
        internalScope.launch(ioDispatcher) {
            try {
                val json = Json.encodeToString(MessagePayload.serializer(), ack)
                val encryptedAck = cryptoManager.encrypt(json.toByteArray(), secretKey)
                sendData(address, encryptedAck)
            } catch (e: Exception) {
                Log.e(tag, "Failed to send ACK: ${e.message}")
            }
        }
    }

    private fun isNewMessage(id: String): Boolean = synchronized(messageIdHistory) {
        if (messageIdHistory.contains(id)) false else {
            messageIdHistory.add(id)
            if (messageIdHistory.size > 100) messageIdHistory.removeAt(0)
            true
        }
    }

    /** Core GATT write logic. Handles Android API version differences. */
    private fun sendData(address: String, data: ByteArray) {
        val gatt = activeGatts[address] ?: return
        val service = gatt.getService(SERVICE_UUID) ?: return
        val characteristic = service.getCharacteristic(PULSE_CHAR_UUID) ?: return
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                gatt.writeCharacteristic(characteristic, data, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
            } else {
                @Suppress("DEPRECATION")
                characteristic.value = data
                @Suppress("DEPRECATION")
                gatt.writeCharacteristic(characteristic)
            }
        } catch (e: SecurityException) {
            Log.e(tag, "Write fail: ${e.message}")
        }
    }

    override fun startDiscovery() {
        Log.i(tag, "BLE: startDiscovery()")
        if ((adapter == null) || !adapter.isEnabled) {
            reportError(P2PError.DiscoveryError("Bluetooth Disabled"))
            return
        }
        val scanner = adapter.bluetoothLeScanner ?: return
        _isDiscovering.value = true
        
        val filter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(SERVICE_UUID))
            .build()
        
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        try {
            scanner.startScan(listOf(filter), settings, scanCallback)
        } catch (_: SecurityException) {
            reportError(P2PError.DiscoveryError("Permission Denied"))
            _isDiscovering.value = false
        }
    }

    override fun stopDiscovery() {
        if (!_isDiscovering.value) return
        _isDiscovering.value = false
        try {
            adapter?.bluetoothLeScanner?.stopScan(scanCallback)
        } catch (e: SecurityException) {
            Log.w("BleFallback", "Stop advertising failed: ${e.message}")
        }
        _scannedDevices.value = emptyList()
    }

    override fun startAdvertising() {
        Log.i(tag, "BLE: startAdvertising()")
        if ((adapter == null) || (!adapter.isEnabled)) {
            reportError(P2PError.AdvertisingError("Bluetooth Disabled"))
            return
        }
        val advertiser = adapter.bluetoothLeAdvertiser ?: return
        
        _isAdvertising.value = true

        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setConnectable(true)
            .setTimeout(0)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .build()

        // Persona meta: Emoji|Nickname|DeviceID
        val localPulse = "${repository.emojiAvatar.value}|${repository.getCurrentNickname()}|${repository.getDeviceId()}"
        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(false)
            .addServiceUuid(ParcelUuid(SERVICE_UUID))
            .addServiceData(ParcelUuid(SERVICE_UUID), localPulse.toByteArray(StandardCharsets.UTF_8))
            .build()

        try {
            advertiser.startAdvertising(settings, data, advertiseCallback)
            startGattServer()
        } catch (_: SecurityException) {
            reportError(P2PError.AdvertisingError("Permission Denied"))
            _isAdvertising.value = false
        }
    }

    /** Opens the GATT server for peer connections. */
    private fun startGattServer() {
        try {
            gattServer = bluetoothManager?.openGattServer(context, gattServerCallback)
            val service = BluetoothGattService(SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)
            val characteristic = BluetoothGattCharacteristic(
                PULSE_CHAR_UUID,
                BluetoothGattCharacteristic.PROPERTY_WRITE or BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
                BluetoothGattCharacteristic.PERMISSION_WRITE,
            )
            service.addCharacteristic(characteristic)
            gattServer?.addService(service)
        } catch (e: SecurityException) {
            Log.e(tag, "GATT Server start fail: ${e.message}")
            reportError(P2PError.AdvertisingError("Permission Denied for GATT Server"))
        }
    }

    override fun stopAdvertising() {
        if (!_isAdvertising.value) return
        _isAdvertising.value = false
        try {
            adapter?.bluetoothLeAdvertiser?.stopAdvertising(advertiseCallback)
            gattServer?.close()
            gattServer = null
        } catch (e: SecurityException) {
            Log.e("BleFallback", "GATT connection change handle failed: ${e.message}")
        }
    }

    override fun requestRadio(device: P2PDevice) {
        pendingRadioRequests.add(device.id)
        updateScannedDevices()
        sendHandshake(device.id)
    }

    override fun isNearbyConnected(endpointId: String): Boolean = false
    override fun acceptRadio(device: P2PDevice) {
        pendingRadioRequests.remove(device.id)
        _incomingRadioRequests.update { it - device }
        _connectedTies.update { it + device.id }
        _isConnected.value = true
        updateScannedDevices()
    }

    override fun denyRadio(device: P2PDevice) {
        pendingRadioRequests.remove(device.id)
        _incomingRadioRequests.update { it - device }
        updateScannedDevices()
    }

    override fun connectToDevice(device: P2PDevice): SharedFlow<ConnectionStatus> {
        val flow = MutableSharedFlow<ConnectionStatus>(replay = 1)
        flow.tryEmit(ConnectionStatus.Connecting)
        
        val bluetoothDevice = adapter?.getRemoteDevice(device.id)
        if (bluetoothDevice == null) {
            flow.tryEmit(ConnectionStatus.Error("Device not found"))
            return flow.asSharedFlow()
        }

        try {
            @Suppress("DEPRECATION")
            bluetoothDevice.connectGatt(
                context,
                false,
                gattCallback,
                BluetoothDevice.TRANSPORT_LE
            )
        } catch (e: SecurityException) {
            Log.e("BleFallback", "Permission Denied: ${e.message}")
            flow.tryEmit(ConnectionStatus.Error("Permission Denied"))
            reportError(P2PError.ConnectionError("Permission Denied"))
        }

        return flow.asSharedFlow()
    }

    private fun sendHandshake(address: String) {
        val publicKeyBytes = cryptoManager.getLocalKeyPair().public.encoded
        val handshakePayload = byteArrayOf(HANDSHAKE_PREFIX) + publicKeyBytes
        internalScope.launch(ioDispatcher) {
            sendData(address, handshakePayload)
        }
    }

    override suspend fun sendMessage(content: String, receiverId: String?, pulseType: Int, messageId: String?, groupId: String?, groupName: String?, type: Int): MessagePayload? {
        val payload = MessagePayload(
            messageId = messageId ?: UUID.randomUUID().toString(),
            senderId = repository.getDeviceId(),
            senderName = repository.getCurrentNickname(),
            senderEmoji = repository.emojiAvatar.value,
            receiverId = receiverId,
            groupId = groupId,
            groupName = groupName,
            content = content,
            timestamp = System.currentTimeMillis(),
            pulseType = pulseType,
            type = type
        )
        val json = Json.encodeToString(MessagePayload.serializer(), payload)
        val bytes = json.toByteArray()

        try {
            if (receiverId != null) {
                val key = pulseKeys[receiverId] ?: return null
                val encrypted = cryptoManager.encrypt(bytes, key)
                sendData(receiverId, encrypted)
            } else {
                activeGatts.keys.forEach { target ->
                    pulseKeys[target]?.let { key ->
                        try {
                            sendData(target, cryptoManager.encrypt(bytes, key))
                        } catch (e: Exception) {}
                    }
                }
            }
            pulseStore.upsertMessage(payload)
            synchronized(messageIdHistory) {
                messageIdHistory.add(payload.messageId)
                if (messageIdHistory.size > 100) messageIdHistory.removeAt(0)
            }
            return payload
        } catch (_: Exception) {
            return null
        }
    }

    private fun updateScannedDevices() {
        _scannedDevices.update { current ->
            current.map { device ->
                device.copy(
                    isConnected = device.id in _connectedTies.value,
                    isTiePending = device.id in pendingRadioRequests
                )
            }
        }
    }

    private fun reportError(error: P2PError) {
        internalScope.launch { _errors.emit(error) }
    }

    override suspend fun broadcastMessage(content: String, pulseType: Int, messageId: String?, groupId: String?, groupName: String?, type: Int): MessagePayload? {
        return sendMessage(content, null, pulseType, messageId, groupId, groupName, type)
    }

    override suspend fun broadcastIdentityUpdate(oldName: String): MessagePayload {
        // BLE implementation does not yet support identity broadcasts.
        return MessagePayload(
            messageId = "ble-placeholder",
            senderId = "ble",
            senderName = "ble",
            content = oldName,
            timestamp = System.currentTimeMillis(),
            type = MessagePayload.TYPE_IDENTITY_UPDATE
        )
    }

    override suspend fun sendGroupMessage(content: String, groupId: String): MessagePayload? {
        return sendMessage(content, null)?.copy(groupId = groupId)
    }

    override suspend fun sendNoteUpdate(groupId: String, content: String, messageId: String?, version: Int): MessagePayload? {
        return sendMessage(content, null, MessagePayload.PULSE_WHISPER, messageId, groupId, null)?.copy(type = MessagePayload.TYPE_NOTE_UPDATE, noteVersion = version)
    }

    override suspend fun sendFile(fileUri: android.net.Uri, receiverId: String?, pulseType: Int, groupId: String?, groupName: String?): MessagePayload? {
        Log.w(tag, "File sharing not supported on BLE Fallback")
        return null
    }

    override fun startGroupPulse(name: String, members: Set<String>, type: Int, groupId: String?, parentId: String?): String {
        val gid = groupId ?: Resonance.generateId(name, type)
        internalScope.launch(ioDispatcher) {
            pulseStore.insertGroup(
                Resonance(
                    id = gid,
                    name = name,
                    memberIds = members + repository.getDeviceId(),
                    scope = type,
                    parentId = parentId,
                )
            )
        }
        return gid
    }

    override fun updateGroupMembers(groupId: String, memberIds: Set<String>) {
        internalScope.launch(ioDispatcher) {
            pulseStore.updateGroupMembers(groupId, memberIds)
        }
    }

    override fun updateGroupScope(groupId: String, scope: Int) {
        internalScope.launch(ioDispatcher) {
            pulseStore.updateGroupScope(groupId, scope)
        }
    }

    override fun initiateHistorySync(endpointId: String, sinceTimestamp: Long?) {
        Log.w(tag, "History sync not supported on BLE Fallback")
    }

    override fun closeConnection() {
        activeGatts.values.forEach { 
            try { 
                it.disconnect()
                it.close() 
            } catch (e: SecurityException) {
                Log.w(tag, "SecurityException during disconnect")
            } catch (_: Exception) {}
        }
        activeGatts.clear()
        pulseKeys.clear()
        _isConnected.value = false
        _connectedTies.value = emptySet()
    }

    override fun release() {
        stopDiscovery()
        stopAdvertising()
        closeConnection()
        internalScope.cancel()
    }
}
