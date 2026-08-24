package cc.thevar.blukit.data.local

import android.content.Context
import android.util.Log
import cc.thevar.blukit.data.crypto.CryptoManager
import cc.thevar.blukit.domain.model.MessagePayload
import cc.thevar.blukit.domain.model.VibeGroup
import cc.thevar.blukit.data.local.entities.ContactEntity
import cc.thevar.blukit.data.local.entities.PeerEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream

/**
 * BLUKIT ENERGY STORE.
 */
class VibeStore(
    private val context: Context,
    private val cryptoManager: CryptoManager,
    private val ioDispatcher: kotlinx.coroutines.CoroutineDispatcher = Dispatchers.IO
) {
    private val messagesLogFile = File(context.filesDir, "vibes_log.bin")
    private val groupsFile = File(context.filesDir, "groups.bin")
    private val peersFile = File(context.filesDir, "peers.bin")
    private val contactsFile = File(context.filesDir, "contacts.bin")

    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)

    private val _messages = MutableStateFlow<List<MessagePayload>>(emptyList())
    val messages: StateFlow<List<MessagePayload>> = _messages.asStateFlow()

    private val _groups = MutableStateFlow<Map<String, VibeGroup>>(emptyMap())
    val activeGroups: StateFlow<List<VibeGroup>> = _groups
        .map { it.values.filter { !it.isArchived }.toList() }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    val archivedGroups: StateFlow<List<VibeGroup>> = _groups
        .map { it.values.filter { it.isArchived }.toList() }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    val groups: StateFlow<List<VibeGroup>> = _groups
        .map { it.values.toList() }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    private val _peers = MutableStateFlow<Map<String, PeerEntity>>(emptyMap())
    private val _contacts = MutableStateFlow<Map<String, ContactEntity>>(emptyMap())

    init {
        loadData()
        // Ensure default ties exist immediately
        if (!_groups.value.containsKey(VibeGroup.ID_AIR)) {
            _groups.update { it + (VibeGroup.ID_AIR to VibeGroup(id = VibeGroup.ID_AIR, name = "THE AIR", scope = VibeGroup.SCOPE_PUBLIC)) }
        }
        if (!_groups.value.containsKey(VibeGroup.ID_SILENCE)) {
            _groups.update { it + (VibeGroup.ID_SILENCE to VibeGroup(id = VibeGroup.ID_SILENCE, name = "SILENCE", scope = VibeGroup.SCOPE_LOCAL)) }
        }
        saveData()
        scope.launch {
            compactMessages()
        }
        scope.launch {
            autoArchiveAirs()
        }
    }

    private fun loadData() {
        if (messagesLogFile.exists()) {
            try {
                val messageMap = mutableMapOf<String, MessagePayload>()
                messagesLogFile.inputStream().use { fis ->
                    val dis = DataInputStream(fis)
                    while (fis.available() > 0) {
                        val length = dis.readInt()
                        val encrypted = ByteArray(length)
                        dis.readFully(encrypted)
                        val decrypted = cryptoManager.decryptLocal(encrypted)
                        val json = decrypted.decodeToString()
                        val message = Json.decodeFromString<MessagePayload>(json)
                        messageMap[message.messageId] = message
                    }
                }
                _messages.value = messageMap.values.toList().sortedBy { it.timestamp }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (groupsFile.exists()) {
            try {
                val encrypted = groupsFile.readBytes()
                val decrypted = cryptoManager.decryptLocal(encrypted)
                val json = decrypted.decodeToString()
                _groups.value = Json.decodeFromString(json)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (peersFile.exists()) {
            try {
                val encrypted = peersFile.readBytes()
                val decrypted = cryptoManager.decryptLocal(encrypted)
                val json = decrypted.decodeToString()
                _peers.value = Json.decodeFromString(json)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (contactsFile.exists()) {
            try {
                val encrypted = contactsFile.readBytes()
                val decrypted = cryptoManager.decryptLocal(encrypted)
                val json = decrypted.decodeToString()
                _contacts.value = Json.decodeFromString(json)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun appendMessageToLog(message: MessagePayload) {
        scope.launch {
            try {
                val json = Json.encodeToString(message)
                val encrypted = cryptoManager.encryptLocal(json.encodeToByteArray())
                FileOutputStream(messagesLogFile, true).use { fos ->
                    val dos = DataOutputStream(fos)
                    dos.writeInt(encrypted.size)
                    dos.write(encrypted)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun compactMessages() {
        val currentMessages = _messages.value

        val tempFile = File(context.filesDir, "vibes_log.tmp")
        try {
            tempFile.outputStream().use { fos ->
                val dos = DataOutputStream(fos)
                currentMessages.forEach { message ->
                    val json = Json.encodeToString(message)
                    val encrypted = cryptoManager.encryptLocal(json.encodeToByteArray())
                    dos.writeInt(encrypted.size)
                    dos.write(encrypted)
                }
            }
            if (tempFile.exists()) {
                tempFile.renameTo(messagesLogFile)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveData() {
        scope.launch {
            try {
                val groupsJson = Json.encodeToString(_groups.value)
                val encryptedGroups = cryptoManager.encryptLocal(groupsJson.encodeToByteArray())
                groupsFile.writeBytes(encryptedGroups)

                val peersJson = Json.encodeToString(_peers.value)
                val encryptedPeers = cryptoManager.encryptLocal(peersJson.encodeToByteArray())
                peersFile.writeBytes(encryptedPeers)

                val contactsJson = Json.encodeToString(_contacts.value)
                val encryptedContacts = cryptoManager.encryptLocal(contactsJson.encodeToByteArray())
                contactsFile.writeBytes(encryptedContacts)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Message Operations
    fun getAllMessages() = messages

    suspend fun insertMessage(message: MessagePayload) {
        upsertMessage(message)
    }

    suspend fun upsertMessage(message: MessagePayload) {
        _messages.update { current ->
            val exists = current.any { it.messageId == message.messageId }
            appendMessageToLog(message)
            if (exists) {
                current.map { if (it.messageId == message.messageId) message else it }
            } else {
                current + message
            }
        }
    }

    suspend fun updateMessageStatus(messageId: String, status: Int) {
        var updated: MessagePayload? = null
        _messages.update { list ->
            list.map {
                if (it.messageId == messageId) {
                    val m = it.copy(status = status)
                    updated = m
                    m
                } else it
            }
        }
        updated?.let { appendMessageToLog(it) }
    }

    suspend fun updateVibeScope(messageId: String, vibeType: Int) {
        var updated: MessagePayload? = null
        _messages.update { list ->
            list.map {
                if (it.messageId == messageId) {
                    val m = it.copy(vibeType = vibeType)
                    updated = m
                    m
                } else it
            }
        }
        updated?.let { 
            appendMessageToLog(it)
            compactMessages()
        }
    }

    suspend fun clearAllMessages() {
        _messages.value = emptyList()
        if (messagesLogFile.exists()) messagesLogFile.delete()
    }

    suspend fun deleteMessage(messageId: String) {
        val message = _messages.value.find { it.messageId == messageId }
        if (message?.type == MessagePayload.TYPE_IMAGE || message?.type == MessagePayload.TYPE_FILE) {
            message.content.let { path ->
                try {
                    val file = File(path)
                    if (file.exists() && file.absolutePath.contains(context.filesDir.absolutePath)) {
                        file.delete()
                    }
                } catch (ignored: Exception) {}
            }
        }
        _messages.update { it.filter { m -> m.messageId != messageId } }
        compactMessages()
    }

    // Vault & Archiving
    fun autoArchiveAirs() {
        val now = System.currentTimeMillis()
        _groups.update { current ->
            current.mapValues { (id, group) ->
                val isDefault = id == VibeGroup.ID_AIR || id == VibeGroup.ID_SILENCE
                if (!isDefault && !group.isArchived && (now - group.lastVibeTimestamp) > VibeGroup.ARCHIVE_THRESHOLD_MS) {
                    group.copy(isArchived = true)
                } else {
                    group
                }
            }
        }
        saveData()
    }

    fun restoreFromVault(groupId: String) {
        _groups.update { current ->
            current[groupId]?.let { 
                current + (groupId to it.copy(isArchived = false, lastVibeTimestamp = System.currentTimeMillis()))
            } ?: current
        }
        saveData()
    }

    suspend fun pruneMedia(thresholdMs: Long) {
        val now = System.currentTimeMillis()
        val allGroups = _groups.value.values
        val allPinnedVibes = allGroups.flatMap { it.pinnedVibeIds }.toSet()

        _messages.value.forEach { message ->
            if ((now - message.timestamp) > thresholdMs && message.messageId !in allPinnedVibes) {
                if (message.type == MessagePayload.TYPE_IMAGE || message.type == MessagePayload.TYPE_FILE) {
                    message.content.let { path ->
                        try {
                            val file = File(path)
                            if (file.exists() && file.absolutePath.contains(context.filesDir.absolutePath)) {
                                file.delete()
                                Log.i("VibeStore", "Pruned media: ${message.messageId}")
                            }
                        } catch (ignored: Exception) {}
                    }
                }
            }
        }
    }

    // Group Operations
    fun getAllGroups() = groups

    suspend fun insertGroup(group: VibeGroup) {
        _groups.update { it + (group.id to group) }
        saveData()
    }

    suspend fun getGroup(id: String) = _groups.value[id]

    suspend fun updateGroupMembers(groupId: String, memberIds: Set<String>) {
        _groups.update { current ->
            current[groupId]?.let { 
                current + (groupId to it.copy(memberIds = memberIds))
            } ?: current
        }
        saveData()
    }

    suspend fun updateGroupScope(groupId: String, scope: Int) {
        _groups.update { current ->
            current[groupId]?.let { 
                current + (groupId to it.copy(scope = scope))
            } ?: current
        }
        saveData()
    }

    suspend fun updateGroupLastVibe(groupId: String, timestamp: Long) {
        _groups.update { current ->
            current[groupId]?.let { 
                current + (groupId to it.copy(lastVibeTimestamp = timestamp))
            } ?: current
        }
        saveData()
    }

    suspend fun deleteGroup(id: String) {
        _groups.update { it - id }
        saveData()
    }

    suspend fun pinVibe(groupId: String, messageId: String) {
        _groups.update { current ->
            current[groupId]?.let { group ->
                current + (groupId to group.copy(pinnedVibeIds = group.pinnedVibeIds + messageId))
            } ?: current
        }
        saveData()
    }

    suspend fun unpinVibe(groupId: String, messageId: String) {
        _groups.update { current ->
            current[groupId]?.let { group ->
                current + (groupId to group.copy(pinnedVibeIds = group.pinnedVibeIds - messageId))
            } ?: current
        }
        saveData()
    }

    suspend fun updateGroupProjection(groupId: String, emoji: String?) {
        _groups.update { current ->
            current[groupId]?.let { group ->
                current + (groupId to group.copy(projectionEmoji = emoji))
            } ?: current
        }
        saveData()
    }

    suspend fun addAirSchedule(groupId: String, schedule: cc.thevar.blukit.domain.model.AirSchedule) {
        _groups.update { current ->
            current[groupId]?.let { group ->
                current + (groupId to group.copy(schedules = group.schedules + schedule))
            } ?: current
        }
        saveData()
    }

    // Peer Operations
    suspend fun getPeer(id: String): PeerEntity? {
        return _peers.value[id]
    }

    suspend fun insertPeer(peer: PeerEntity) {
        _peers.update { it + (peer.endpointId to peer) }
        saveData()
    }

    suspend fun clearPeers() {
        _peers.value = emptyMap()
        saveData()
    }

    // Contact Operations
    fun getAllContacts() = _contacts.asStateFlow().map { it.values.toList() }

    suspend fun getContact(id: String) = _contacts.value[id]

    suspend fun insertContact(contact: ContactEntity) {
        _contacts.update { it + (contact.id to contact) }
        saveData()
    }

    suspend fun deleteContact(id: String) {
        _contacts.update { it - id }
        saveData()
    }

    suspend fun deleteAllContacts() {
        _contacts.value = emptyMap()
        saveData()
    }
}
