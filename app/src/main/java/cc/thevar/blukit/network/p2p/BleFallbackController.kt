package cc.thevar.blukit.network.p2p

import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.os.Build
import android.os.ParcelUuid
import android.util.Log
import cc.thevar.blukit.data.crypto.CryptoManager
import cc.thevar.blukit.data.local.dao.MessageDao
import cc.thevar.blukit.data.local.dao.PeerDao
import cc.thevar.blukit.data.local.entities.toBluetoothPayload
import cc.thevar.blukit.data.local.entities.toMessageEntity
import cc.thevar.blukit.data.repository.IdentityRepository
import cc.thevar.blukit.data.system.HapticManager
import cc.thevar.blukit.domain.model.ConnectionStatus
import cc.thevar.blukit.domain.model.MessagePayload
import cc.thevar.blukit.domain.model.P2PDevice
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.crypto.SecretKey

/**
 * Native BLE Fallback Controller.
 * Used when Google Nearby Connections is unavailable or failing.
 * Implements standard BLE GATT advertising and scanning.
 */
class BleFallbackController(
    private val context: Context,
    private val repository: IdentityRepository,
    private val messageDao: MessageDao,
    private val peerDao: PeerDao,
    private val hapticManager: HapticManager,
    private val cryptoManager: CryptoManager = CryptoManager(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    mainDispatcher: CoroutineDispatcher = Dispatchers.Main
) : P2PController {

    private val tag = "BlukitBLE"
    private val bluetoothManager = context.getSystemService(BluetoothManager::class.java)
    private val adapter = bluetoothManager?.adapter

    private val internalScope = CoroutineScope(SupervisorJob() + mainDispatcher)

    private val _scannedDevices = MutableStateFlow<List<P2PDevice>>(emptyList())
    override val scannedDevices = _scannedDevices.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    override val isConnected = _isConnected.asStateFlow()

    private val _connectedLinks = MutableStateFlow<Set<String>>(emptySet())
    override val connectedLinks = _connectedLinks.asStateFlow()

    private val _incomingLinkRequests = MutableStateFlow<Set<P2PDevice>>(emptySet())
    override val incomingLinkRequests = _incomingLinkRequests.asStateFlow()

    private val _isDiscovering = MutableStateFlow(false)
    override val isDiscovering = _isDiscovering.asStateFlow()

    private val _isAdvertising = MutableStateFlow(false)
    override val isAdvertising = _isAdvertising.asStateFlow()

    private val _errors = MutableStateFlow<P2PError?>(null)
    override val errors = _errors.asStateFlow()

    override val messages: StateFlow<List<MessagePayload>> = messageDao.getAllMessages()
        .map { entities -> entities.map { it.toBluetoothPayload() } }
        .stateIn(
            scope = internalScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val vibeKeys = ConcurrentHashMap<String, SecretKey>()
    private val activeGatts = ConcurrentHashMap<String, BluetoothGatt>()
    private var gattServer: BluetoothGattServer? = null
    private val messageIdHistory = Collections.synchronizedList(LinkedList<String>())

    companion object {
        private val SERVICE_UUID = UUID.fromString("0000fb01-0000-1000-8000-00805f9b34fb")
        private val VIBE_CHAR_UUID = UUID.fromString("0000fb02-0000-1000-8000-00805f9b34fb")
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

            val newDevice = P2PDevice(
                id = device.address,
                name = parts[1],
                emoji = parts[0],
                signalStrength = result.rssi
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
            value: ByteArray
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
            } catch (e: SecurityException) {
                reportError(P2PError.ConnectionError("Permission Denied for Service Discovery"))
            }
        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            Log.i(tag, "GATT: Disconnected from $address")
            activeGatts.remove(address)
            _connectedLinks.update { it - address }
            if (activeGatts.isEmpty()) _isConnected.value = false
        }
    }

    private fun handleGattServicesDiscovered(gatt: BluetoothGatt, status: Int) {
        val address = gatt.device.address
        if (status == BluetoothGatt.GATT_SUCCESS) {
            activeGatts[address] = gatt
            _connectedLinks.update { it + address }
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
            vibeKeys.remove(device.address)
        }
    }

    private fun handleCharacteristicWrite(
        device: BluetoothDevice,
        requestId: Int,
        characteristic: BluetoothGattCharacteristic,
        responseNeeded: Boolean,
        value: ByteArray
    ) {
        if (characteristic.uuid == VIBE_CHAR_UUID) {
            if (responseNeeded) {
                try {
                    gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null)
                } catch (e: SecurityException) {}
            }
            handleReceivedData(device.address, value)
        }
    }

    private fun handleReceivedData(address: String, data: ByteArray) {
        if (data.isNotEmpty() && data[0] == HANDSHAKE_PREFIX) {
            handleHandshake(address, data)
        } else {
            handleMessage(address, data)
        }
    }

    private fun handleHandshake(address: String, data: ByteArray) {
        try {
            val publicKeyEncoded = data.copyOfRange(1, data.size)
            val keyFactory = KeyFactory.getInstance("EC")
            val vibePublicKey = keyFactory.generatePublic(X509EncodedKeySpec(publicKeyEncoded))
            val sharedSecret = cryptoManager.deriveSharedSecret(vibePublicKey)
            vibeKeys[address] = sharedSecret
            Log.i(tag, "SECURE: Channel ready for $address (BLE)")
        } catch (e: Exception) {
            Log.e(tag, "Handshake error: ${e.message}")
        }
    }

    private fun handleMessage(address: String, data: ByteArray) {
        val secretKey = vibeKeys[address] ?: return
        try {
            val decryptedBytes = cryptoManager.decrypt(data, secretKey)
            val payload = Json.decodeFromString<MessagePayload>(decryptedBytes.decodeToString())

            when (payload.type) {
                MessagePayload.TYPE_ACK -> handleAck(payload)
                else -> {
                    if (isNewMessage(payload.messageId) && payload.senderId !in repository.blockedUsers.value) {
                        handleChatMessage(address, payload, secretKey)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Message decrypt error: ${e.message}")
            reportError(P2PError.EncryptionError("Failed to decrypt incoming vibe"))
        }
    }

    private fun handleAck(payload: MessagePayload) {
        internalScope.launch(ioDispatcher) {
            messageDao.updateMessageStatus(payload.messageId, MessagePayload.STATUS_DELIVERED)
        }
    }

    private fun handleChatMessage(address: String, payload: MessagePayload, secretKey: SecretKey) {
        internalScope.launch(ioDispatcher) {
            messageDao.insertMessage(payload.toMessageEntity(isFromLocalUser = false))
            hapticManager.triggerVibe(HapticManager.VibeType.MESSAGE)
            sendAck(address, payload, secretKey)
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
            receiverId = originalPayload.senderId
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

    private fun sendData(address: String, data: ByteArray) {
        val gatt = activeGatts[address] ?: return
        val service = gatt.getService(SERVICE_UUID) ?: return
        val characteristic = service.getCharacteristic(VIBE_CHAR_UUID) ?: return
        
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
        if (adapter == null || !adapter.isEnabled) {
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
        } catch (e: SecurityException) {
            reportError(P2PError.DiscoveryError("Permission Denied"))
            _isDiscovering.value = false
        }
    }

    override fun stopDiscovery() {
        if (!_isDiscovering.value) return
        _isDiscovering.value = false
        try {
            adapter?.bluetoothLeScanner?.stopScan(scanCallback)
        } catch (e: SecurityException) {}
        _scannedDevices.value = emptyList()
    }

    override fun startAdvertising() {
        Log.i(tag, "BLE: startAdvertising()")
        if (adapter == null || !adapter.isEnabled) {
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

        val localVibe = "${repository.emojiAvatar.value}|${repository.getCurrentNickname()}|${repository.getDeviceId()}"
        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(false)
            .addServiceUuid(ParcelUuid(SERVICE_UUID))
            .addServiceData(ParcelUuid(SERVICE_UUID), localVibe.toByteArray(StandardCharsets.UTF_8))
            .build()

        try {
            advertiser.startAdvertising(settings, data, advertiseCallback)
            startGattServer()
        } catch (e: SecurityException) {
            reportError(P2PError.AdvertisingError("Permission Denied"))
            _isAdvertising.value = false
        }
    }

    private fun startGattServer() {
        try {
            gattServer = bluetoothManager?.openGattServer(context, gattServerCallback)
            val service = BluetoothGattService(SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)
            val characteristic = BluetoothGattCharacteristic(
                VIBE_CHAR_UUID,
                BluetoothGattCharacteristic.PROPERTY_WRITE or BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
                BluetoothGattCharacteristic.PERMISSION_WRITE
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
        } catch (e: SecurityException) {}
    }

    override fun requestLink(device: P2PDevice) {
        // BLE implementation of Link Request
        sendHandshake(device.id)
    }

    override fun acceptLink(device: P2PDevice) {
        _incomingLinkRequests.update { it - device }
        _connectedLinks.update { it + device.id }
        _isConnected.value = true
    }

    override fun denyLink(device: P2PDevice) {
        _incomingLinkRequests.update { it - device }
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                bluetoothDevice.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
            } else {
                bluetoothDevice.connectGatt(context, false, gattCallback)
            }
        } catch (e: SecurityException) {
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

    override suspend fun sendMessage(content: String, receiverId: String?): MessagePayload? {
        val payload = MessagePayload(
            messageId = UUID.randomUUID().toString(),
            senderId = repository.getDeviceId(),
            senderName = repository.getCurrentNickname(),
            senderEmoji = repository.emojiAvatar.value,
            receiverId = receiverId,
            content = content,
            timestamp = System.currentTimeMillis()
        )
        val json = Json.encodeToString(MessagePayload.serializer(), payload)
        val bytes = json.toByteArray()

        try {
            if (receiverId != null) {
                val key = vibeKeys[receiverId] ?: return null
                val encrypted = cryptoManager.encrypt(bytes, key)
                sendData(receiverId, encrypted)
            } else {
                activeGatts.keys.forEach { target ->
                    vibeKeys[target]?.let { key ->
                        try {
                            sendData(target, cryptoManager.encrypt(bytes, key))
                        } catch (e: Exception) {}
                    }
                }
            }
            messageDao.insertMessage(payload.toMessageEntity(isFromLocalUser = true))
            synchronized(messageIdHistory) {
                messageIdHistory.add(payload.messageId)
                if (messageIdHistory.size > 100) messageIdHistory.removeAt(0)
            }
            return payload
        } catch (e: Exception) {
            return null
        }
    }

    private fun reportError(error: P2PError) {
        internalScope.launch { _errors.emit(error) }
    }

    override suspend fun broadcastMessage(content: String): MessagePayload? {
        return sendMessage(content, null)
    }

    override fun closeConnection() {
        activeGatts.values.forEach { 
            try { it.disconnect(); it.close() } catch (e: Exception) {}
        }
        activeGatts.clear()
        vibeKeys.clear()
        _isConnected.value = false
        _connectedLinks.value = emptySet()
    }

    override fun release() {
        stopDiscovery()
        stopAdvertising()
        closeConnection()
        internalScope.cancel()
    }
}
