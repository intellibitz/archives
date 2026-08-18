package cc.thevar.blukit.data.local

import android.content.Context
import cc.thevar.blukit.data.crypto.CryptoManager
import cc.thevar.blukit.domain.model.MessagePayload
import cc.thevar.blukit.domain.model.VibeGroup
import cc.thevar.blukit.data.local.entities.ContactEntity
import cc.thevar.blukit.data.local.entities.PeerEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File

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
        scope.launch {
            compactMessages()
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
                java.io.FileOutputStream(messagesLogFile, true).use { fos ->
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
        val now = System.currentTimeMillis()
        val twelveHoursAgo = now - 12 * 3600 * 1000
        
        _messages.update { it.filter { m -> m.timestamp >= twelveHoursAgo } }
        val currentMessages = _messages.value

        // Also purge ephemeral groups (Side Vibes) that haven't been active for 12 hours
        _groups.update { current ->
            current.filter { (_, group) ->
                group.isPersistent || group.lastVibeTimestamp >= twelveHoursAgo
            }
        }

        saveData() // Persist updated groups

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
        _messages.update { current ->
            if (current.any { it.messageId == message.messageId }) {
                current
            } else {
                appendMessageToLog(message)
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

    suspend fun deleteOldMessages(threshold: Long) {
        compactMessages()
    }

    suspend fun clearAllMessages() {
        _messages.value = emptyList()
        if (messagesLogFile.exists()) messagesLogFile.delete()
    }

    suspend fun deleteMessage(messageId: String) {
        _messages.update { it.filter { m -> m.messageId != messageId } }
        compactMessages() // Rewrite log without this message
    }

    // Group Operations
    fun getAllGroups() = groups

    suspend fun insertGroup(group: VibeGroup) {
        _groups.update { it + (group.id to group) }
        saveData()
    }

    suspend fun getGroup(id: String) = _groups.value[id]

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
