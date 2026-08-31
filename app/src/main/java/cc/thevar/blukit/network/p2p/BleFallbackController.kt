/**
 * BLUKIT NETWORK: BLE FALLBACK CONTROLLER
 *
 * A native BLE engine used when Google Nearby Connections is unavailable or fails.
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
import cc.thevar.blukit.data.local.EchoLedger
import cc.thevar.blukit.data.repository.IdentityRepository
import cc.thevar.blukit.data.system.HapticManager
import cc.thevar.blukit.domain.model.ConnectionStatus
import cc.thevar.blukit.domain.model.Echo
import cc.thevar.blukit.domain.model.Source
import cc.thevar.blukit.domain.model.Sphere
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
    private val echoLedger: EchoLedger,
    private val hapticManager: HapticManager,
    private val cryptoManager: CryptoManager = CryptoManager(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
) : ResonanceController {

    private val tag = "BlukitBLE"
    private val bluetoothManager = context.getSystemService(BluetoothManager::class.java)
    private val adapter = bluetoothManager?.adapter

    private val internalScope = CoroutineScope(SupervisorJob() + mainDispatcher)

    // --- ResonanceController State Implementation ---
    private val _scannedSources = MutableStateFlow<List<Source>>(emptyList())
    override val scannedDevices = _scannedSources.asStateFlow()

    private val _isConnected = MutableStateFlow(value = false)
    override val isConnected = _isConnected.asStateFlow()

    private val _connectedSpheres = MutableStateFlow<Set<String>>(emptySet())
    override val connectedGroups = _connectedSpheres.asStateFlow()

    private val _incomingRadioRequests = MutableStateFlow<Set<Source>>(emptySet())
    override val incomingRadioRequests = _incomingRadioRequests.asStateFlow()

    private val _outgoingRadioRequests = MutableStateFlow<Set<Source>>(emptySet())
    override val outgoingRadioRequests = _outgoingRadioRequests.asStateFlow()

    private val _isSensing = MutableStateFlow(value = false)
    override val isDiscovering = _isSensing.asStateFlow()

    private val _isAdvertising = MutableStateFlow(value = false)
    override val isAdvertising = _isAdvertising.asStateFlow()

    private val _resonanceErrors = MutableStateFlow<ResonanceError?>(null)
    override val errors = _resonanceErrors.asStateFlow()

    private val _discoveredSpheres = MutableSharedFlow<Sphere>(extraBufferCapacity = 5)
    override val discoveredRooms = _discoveredSpheres.asSharedFlow()

    override val messages: StateFlow<List<Echo>> = echoLedger.echoes
    override val syncProgress: StateFlow<Float?> = MutableStateFlow(null)

    private val messageKeys = ConcurrentHashMap<String, SecretKey>()
    private val activeGatts = ConcurrentHashMap<String, BluetoothGatt>()
    private var gattServer: BluetoothGattServer? = null
    private val messageIdHistory = Collections.synchronizedList(LinkedList<String>())
    private val pendingRadioRequests = Collections.synchronizedSet(mutableSetOf<String>())

    companion object {
        private val SERVICE_UUID = UUID.fromString("0000fb01-0000-1000-8000-00805f9b34fb")
        private val MESSAGE_CHAR_UUID = UUID.fromString("0000fb02-0000-1000-8000-00805f9b34fb")
        private const val HANDSHAKE_PREFIX = 0x01.toByte()
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            val scanRecord = result.scanRecord ?: return
            val serviceData = scanRecord.serviceData[ParcelUuid(SERVICE_UUID)] ?: return
            
            val info = String(serviceData, StandardCharsets.UTF_8)
            val parts = info.split("|", limit = 3)
            if (parts.size < 3) return

            val newSource = Source(
                id = device.address,
                name = parts[1],
                emoji = parts[0],
                signalStrength = result.rssi,
            )
            _scannedSources.update { current ->
                current.filter { it.id != device.address } + newSource
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e(tag, "Sensing failed: $errorCode")
            _isSensing.value = false
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

    private fun handleGattConnectionChange(gatt: BluetoothGatt, newState: Int) {
        val address = gatt.device.address
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            Log.i(tag, "GATT: Connected to $address")
            try {
                gatt.discoverServices()
            } catch (_: SecurityException) {
                reportError(ResonanceError.ConnectionError("Permission Denied for Service Discovery"))
            }
        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            Log.i(tag, "GATT: Disconnected from $address")
            activeGatts.remove(address)
            pendingRadioRequests.remove(address)
            _connectedSpheres.update { it - address }
            if (activeGatts.isEmpty()) _isConnected.value = false
            updateScannedSources()
        }
    }

    private fun handleGattServicesDiscovered(gatt: BluetoothGatt, status: Int) {
        val address = gatt.device.address
        if (status == BluetoothGatt.GATT_SUCCESS) {
            activeGatts[address] = gatt
            _connectedSpheres.update { it + address }
            _isConnected.value = true
            sendHandshake(address)
        } else {
            Log.e(tag, "Service discovery failed with status: $status")
            reportError(ResonanceError.ConnectionError("Service discovery failed"))
        }
    }

    private fun handleGattServerConnectionChange(device: BluetoothDevice, newState: Int) {
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            Log.i(tag, "GATT Server: Connected to ${device.address}")
        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            Log.i(tag, "GATT Server: Disconnected from ${device.address}")
            messageKeys.remove(device.address)
        }
    }

    private fun handleCharacteristicWrite(
        device: BluetoothDevice,
        requestId: Int,
        characteristic: BluetoothGattCharacteristic,
        responseNeeded: Boolean,
        value: ByteArray,
    ) {
        if (characteristic.uuid == MESSAGE_CHAR_UUID) {
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
            handleEcho(address, data)
        }
    }

    private fun handleHandshake(address: String, data: ByteArray) {
        try {
            val publicKeyEncoded = data.copyOfRange(1, data.size)
            val keyFactory = KeyFactory.getInstance("EC")
            val messagePublicKey = keyFactory.generatePublic(X509EncodedKeySpec(publicKeyEncoded))
            val sharedSecret = cryptoManager.deriveSharedSecret(messagePublicKey)
            messageKeys[address] = sharedSecret
            Log.i(tag, "SECURE: Resonance ready for $address (BLE)")
            updateScannedSources()
        } catch (e: Exception) {
            Log.e(tag, "Handshake error: ${e.message}")
        }
    }

    private fun handleEcho(address: String, data: ByteArray) {
        val secretKey = messageKeys[address] ?: return
        try {
            val decryptedBytes = cryptoManager.decrypt(data, secretKey)
            val payload = Json.decodeFromString<Echo>(decryptedBytes.decodeToString())

            when (payload.type) {
                Echo.TYPE_ACK -> handleAck(payload)
                else -> {
                    if (isNewEcho(payload.messageId) && (payload.senderId !in repository.blockedUsers.value)) {
                        handleResonanceEcho(address, payload, secretKey)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Echo decrypt error: ${e.message}")
            reportError(ResonanceError.EncryptionError("Failed to decrypt incoming Echo"))
        }
    }

    private fun handleAck(payload: Echo) {
        internalScope.launch(ioDispatcher) {
            echoLedger.updateEchoStatus(payload.messageId, Echo.STATUS_DELIVERED)
        }
    }

    private fun handleResonanceEcho(address: String, payload: Echo, secretKey: SecretKey) {
        sendAck(address, payload, secretKey)
        if (payload.receiverId.isNullOrEmpty()) {
            relayEcho(address, payload)
        }

        internalScope.launch(ioDispatcher) {
            val gid = payload.groupId
            val gName = payload.groupName
            if ((gid != null) && (gName != null)) {
                val existing = echoLedger.getSphere(gid)
                if (existing == null) {
                    val scope = when (payload.messageScope) {
                        Echo.MESSAGE_SHOUT -> Sphere.SCOPE_PUBLIC
                        Echo.MESSAGE_SILENCE -> Sphere.SCOPE_LOCAL
                        else -> Sphere.SCOPE_PRIVATE
                    }
                    echoLedger.insertSphere(
                        Sphere(
                            id = gid,
                            name = gName,
                            scope = scope,
                            parentId = Sphere.ID_GLOBAL,
                        ),
                    )
                }
            }

            echoLedger.upsertEcho(payload)
            hapticManager.triggerMessage(HapticManager.MessageType.MESSAGE)
        }
    }

    private fun relayEcho(sourceAddress: String, payload: Echo) {
        if (payload.hopCount >= 3) return

        internalScope.launch(ioDispatcher) {
            val myId = repository.getDeviceId()
            if (payload.senderId == myId) return@launch

            val relayedPayload = payload.copy(hopCount = payload.hopCount + 1)
            val json = Json.encodeToString(Echo.serializer(), relayedPayload)
            val bytes = json.encodeToByteArray()

            activeGatts.forEach { (address, _) ->
                if (address != sourceAddress) {
                    messageKeys[address]?.let { key ->
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

    private fun sendAck(address: String, originalPayload: Echo, secretKey: SecretKey) {
        val ack = Echo(
            messageId = originalPayload.messageId,
            senderId = repository.getDeviceId(),
            senderName = "",
            content = "",
            timestamp = System.currentTimeMillis(),
            type = Echo.TYPE_ACK,
            receiverId = originalPayload.senderId,
        )
        internalScope.launch(ioDispatcher) {
            try {
                val json = Json.encodeToString(Echo.serializer(), ack)
                val encryptedAck = cryptoManager.encrypt(json.toByteArray(), secretKey)
                sendData(address, encryptedAck)
            } catch (e: Exception) {
                Log.e(tag, "Failed to send ACK: ${e.message}")
            }
        }
    }

    private fun isNewEcho(id: String): Boolean = synchronized(messageIdHistory) {
        if (messageIdHistory.contains(id)) false else {
            messageIdHistory.add(id)
            if (messageIdHistory.size > 100) messageIdHistory.removeAt(0)
            true
        }
    }

    private fun sendData(address: String, data: ByteArray) {
        val gatt = activeGatts[address] ?: return
        val service = gatt.getService(SERVICE_UUID) ?: return
        val characteristic = service.getCharacteristic(MESSAGE_CHAR_UUID) ?: return
        
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
        if ((adapter == null) || !adapter.isEnabled) {
            reportError(ResonanceError.SensingError("Bluetooth Disabled"))
            return
        }
        val scanner = adapter.bluetoothLeScanner ?: return
        _isSensing.value = true
        
        val filter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(SERVICE_UUID))
            .build()
        
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        try {
            scanner.startScan(listOf(filter), settings, scanCallback)
        } catch (_: SecurityException) {
            reportError(ResonanceError.SensingError("Permission Denied"))
            _isSensing.value = false
        }
    }

    override fun stopDiscovery() {
        if (!_isSensing.value) return
        _isSensing.value = false
        try {
            adapter?.bluetoothLeScanner?.stopScan(scanCallback)
        } catch (e: SecurityException) {
            Log.w("BleFallback", "Stop sensing failed: ${e.message}")
        }
        _scannedSources.value = emptyList()
    }

    override fun startAdvertising() {
        if ((adapter == null) || (!adapter.isEnabled)) {
            reportError(ResonanceError.AdvertisingError("Bluetooth Disabled"))
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

        val localMessage = "${repository.emojiAvatar.value}|${repository.getCurrentNickname()}|${repository.getDeviceId()}"
        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(false)
            .addServiceUuid(ParcelUuid(SERVICE_UUID))
            .addServiceData(ParcelUuid(SERVICE_UUID), localMessage.toByteArray(StandardCharsets.UTF_8))
            .build()

        try {
            advertiser.startAdvertising(settings, data, advertiseCallback)
            startGattServer()
        } catch (_: SecurityException) {
            reportError(ResonanceError.AdvertisingError("Permission Denied"))
            _isAdvertising.value = false
        }
    }

    private fun startGattServer() {
        try {
            gattServer = bluetoothManager?.openGattServer(context, gattServerCallback)
            val service = BluetoothGattService(SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)
            val characteristic = BluetoothGattCharacteristic(
                MESSAGE_CHAR_UUID,
                BluetoothGattCharacteristic.PROPERTY_WRITE or BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
                BluetoothGattCharacteristic.PERMISSION_WRITE,
            )
            service.addCharacteristic(characteristic)
            gattServer?.addService(service)
        } catch (e: SecurityException) {
            Log.e(tag, "GATT Server start fail: ${e.message}")
            reportError(ResonanceError.AdvertisingError("Permission Denied for GATT Server"))
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
            Log.e("BleFallback", "GATT release fail: ${e.message}")
        }
    }

    override fun requestRadio(device: Source) {
        pendingRadioRequests.add(device.id)
        updateScannedSources()
        sendHandshake(device.id)
    }

    override fun isNearbyConnected(endpointId: String): Boolean = false
    override fun acceptRadio(device: Source) {
        pendingRadioRequests.remove(device.id)
        _connectedSpheres.update { it + device.id }
        _isConnected.value = true
        updateScannedSources()
    }

    override fun denyRadio(device: Source) {
        pendingRadioRequests.remove(device.id)
        updateScannedSources()
    }

    override fun joinRoom(groupId: String) {
        echoLedger.joinSphere(groupId, repository.getDeviceId())
    }

    override fun connectToDevice(device: Source): SharedFlow<ConnectionStatus> {
        val flow = MutableSharedFlow<ConnectionStatus>(replay = 1)
        flow.tryEmit(ConnectionStatus.Connecting)
        
        val bluetoothDevice = adapter?.getRemoteDevice(device.id)
        if (bluetoothDevice == null) {
            flow.tryEmit(ConnectionStatus.Error("Source not found"))
            return flow.asSharedFlow()
        }

        try {
            bluetoothDevice.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
        } catch (e: SecurityException) {
            Log.e("BleFallback", "Permission Denied: ${e.message}")
            flow.tryEmit(ConnectionStatus.Error("Permission Denied"))
            reportError(ResonanceError.ConnectionError("Permission Denied"))
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

    override suspend fun sendMessage(content: String, receiverId: String?, messageScope: Int, messageId: String?, groupId: String?, groupName: String?, type: Int): Echo? {
        groupId?.let { gid ->
            if (!echoLedger.isMember(gid, repository.getDeviceId())) return null
        }

        val payload = Echo(
            messageId = messageId ?: UUID.randomUUID().toString(),
            senderId = repository.getDeviceId(),
            senderName = repository.getCurrentNickname(),
            senderEmoji = repository.emojiAvatar.value,
            receiverId = receiverId,
            groupId = groupId,
            groupName = groupName,
            content = content,
            timestamp = System.currentTimeMillis(),
            messageScope = messageScope,
            type = type,
        )
        val json = Json.encodeToString(Echo.serializer(), payload)
        val bytes = json.toByteArray()

        try {
            if (receiverId != null) {
                val key = messageKeys[receiverId] ?: return null
                val encrypted = cryptoManager.encrypt(bytes, key)
                sendData(receiverId, encrypted)
            } else {
                activeGatts.keys.forEach { target ->
                    messageKeys[target]?.let { key ->
                        try {
                            sendData(target, cryptoManager.encrypt(bytes, key))
                        } catch (_: Exception) {}
                    }
                }
            }
            echoLedger.upsertEcho(payload)
            synchronized(messageIdHistory) {
                messageIdHistory.add(payload.messageId)
                if (messageIdHistory.size > 100) messageIdHistory.removeAt(0)
            }
            return payload
        } catch (_: Exception) {
            return null
        }
    }

    private fun updateScannedSources() {
        _scannedSources.update { current ->
            current.map { device ->
                device.copy(
                    isConnected = device.id in _connectedSpheres.value,
                    isGroupPending = device.id in pendingRadioRequests,
                )
            }
        }
    }

    private fun reportError(error: ResonanceError) {
        internalScope.launch { _resonanceErrors.emit(error) }
    }

    override suspend fun broadcastMessage(content: String, messageScope: Int, messageId: String?, groupId: String?, groupName: String?, type: Int): Echo? {
        return sendMessage(content, null, messageScope, messageId, groupId, groupName, type)
    }

    override suspend fun broadcastIdentityUpdate(oldName: String): Echo {
        return Echo(
            messageId = "ble-placeholder",
            senderId = "ble",
            senderName = "ble",
            content = oldName,
            timestamp = System.currentTimeMillis(),
            type = Echo.TYPE_IDENTITY_UPDATE
        )
    }

    override suspend fun sendGroupMessage(content: String, groupId: String): Echo? {
        return sendMessage(content, null)?.copy(groupId = groupId)
    }

    override suspend fun sendNoteUpdate(groupId: String, content: String, messageId: String?, version: Int): Echo? {
        return sendMessage(content, null, Echo.MESSAGE_WHISPER, messageId, groupId, null)?.copy(type = Echo.TYPE_NOTE_UPDATE, noteVersion = version)
    }

    override suspend fun sendFile(fileUri: android.net.Uri, receiverId: String?, messageScope: Int, groupId: String?, groupName: String?): Echo? {
        Log.w(tag, "File sharing not supported on BLE Fallback")
        return null
    }

    override fun startGroupRoom(name: String, members: Set<String>, type: Int, groupId: String?, parentId: String?, anchoredPublicSphereId: String?): String {
        val gid = groupId ?: Sphere.generateId(name, type)
        internalScope.launch(ioDispatcher) {
            echoLedger.insertSphere(
                Sphere(
                    id = gid,
                    name = name,
                    memberIds = members + repository.getDeviceId(),
                    scope = type,
                    parentId = parentId,
                    ownerId = repository.getDeviceId(),
                    anchoredPublicSphereId = anchoredPublicSphereId
                )
            )
        }
        return gid
    }

    override fun updateGroupMembers(groupId: String, memberIds: Set<String>) {
        internalScope.launch(ioDispatcher) {
            echoLedger.updateSphereMembers(groupId, memberIds)
        }
    }

    override fun updateGroupScope(groupId: String, scope: Int) {
        internalScope.launch(ioDispatcher) {
            echoLedger.updateSphereScope(groupId, scope)
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
            } catch (_: SecurityException) {
                Log.w(tag, "SecurityException during disconnect")
            } catch (_: Exception) {}
        }
        activeGatts.clear()
        messageKeys.clear()
        _isConnected.value = false
        _connectedSpheres.value = emptySet()
    }

    override fun release() {
        stopDiscovery()
        stopAdvertising()
        closeConnection()
        internalScope.cancel()
    }
}
