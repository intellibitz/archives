package cc.thevar.blukit.data.local

import android.content.Context
import cc.thevar.blukit.data.crypto.CryptoManager
import cc.thevar.blukit.domain.model.MessagePayload
import cc.thevar.blukit.data.local.entities.ContactEntity
import cc.thevar.blukit.data.local.entities.PeerEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

/**
 * VibeStore: A minimalist, in-memory vibe repository with encrypted persistence.
 * Replaces Room to de-bloat the framework stack.
 */
class VibeStore(
    private val context: Context,
    private val cryptoManager: CryptoManager
) {
    private val _messages = MutableStateFlow<List<MessagePayload>>(emptyList())
    val messages: StateFlow<List<MessagePayload>> = _messages.asStateFlow()

    private val _peers = MutableStateFlow<Map<String, PeerEntity>>(emptyMap())
    private val _contacts = MutableStateFlow<Map<String, ContactEntity>>(emptyMap())

    private val messagesFile = File(context.filesDir, "vibes.bin")
    private val peersFile = File(context.filesDir, "peers.bin")
    private val contactsFile = File(context.filesDir, "contacts.bin")

    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        loadData()
    }

    private fun loadData() {
        if (messagesFile.exists()) {
            try {
                val encrypted = messagesFile.readBytes()
                val decrypted = cryptoManager.decryptLocal(encrypted)
                val json = decrypted.decodeToString()
                _messages.value = Json.decodeFromString(json)
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

    private fun saveData() {
        scope.launch {
            try {
                val messagesJson = Json.encodeToString(_messages.value)
                val encryptedMessages = cryptoManager.encryptLocal(messagesJson.encodeToByteArray())
                messagesFile.writeBytes(encryptedMessages)

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
        _messages.update { it + message }
        saveData()
    }

    suspend fun updateMessageStatus(messageId: String, status: Int) {
        _messages.update { list ->
            list.map { if (it.messageId == messageId) it.copy(status = status) else it }
        }
        saveData()
    }

    suspend fun deleteOldMessages(threshold: Long) {
        _messages.update { list ->
            list.filter { it.timestamp >= threshold }
        }
        saveData()
    }

    suspend fun clearAllMessages() {
        _messages.value = emptyList()
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
