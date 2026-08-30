/**
 * BLUKIT DATA: MESSAGE STORE
 *
 * The secure, offline persistence engine for messages on the mesh.
 * Orchestrates local rooms and chat history.
 */
package cc.thevar.blukit.data.local

import android.content.Context
import android.util.Log
import cc.thevar.blukit.data.crypto.CryptoManager
import cc.thevar.blukit.data.local.db.MessageDatabase
import cc.thevar.blukit.data.local.entities.ContactEntity
import cc.thevar.blukit.data.local.entities.PeerEntity
import cc.thevar.blukit.domain.model.MeshMessage
import cc.thevar.blukit.domain.model.MeshRoom
import cc.thevar.blukit.domain.model.RoomEvent
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
import kotlinx.serialization.json.Json
import java.io.DataInputStream
import java.io.File

/**
 * Manages secure storage and reactive state of all mesh interactions.
 * 
 * @param historyRetentionLimit The maximum number of messages to keep before triggering eviction.
 */
class MessageStore(
    private val context: Context,
    private val cryptoManager: CryptoManager,
    ioDispatcher: kotlinx.coroutines.CoroutineDispatcher = Dispatchers.IO,
    private val historyRetentionLimit: Int = 1000,
) {
    // --- Persistence Anchors ---
    private val messagesLogFile = File(context.filesDir, "messages_log.bin")
    private val roomsFile = File(context.filesDir, "rooms.bin")
    private val peersFile = File(context.filesDir, "peers.bin")
    private val contactsFile = File(context.filesDir, "contacts.bin")

    private val database = MessageDatabase(context)
    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)

    // --- Reactive State Flows ---
    private val _messages = MutableStateFlow<List<MeshMessage>>(emptyList())
    /** The complete chronological life stream of messages. */
    val messages: StateFlow<List<MeshMessage>> = _messages.asStateFlow()

    private val _groups = MutableStateFlow<Map<String, MeshRoom>>(emptyMap())
    
    /** Public frequencies and active private channels. */
    val activeGroups: StateFlow<List<MeshRoom>> = _groups
        .map { groupMap -> groupMap.values.filter { !it.isArchived }.toList() }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList(),
        )

    /** Rooms that haven't pulsed in 30 days. */
    val archivedGroups: StateFlow<List<MeshRoom>> = _groups
        .map { groupMap -> groupMap.values.filter { it.isArchived && !it.isVaulted }.toList() }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList(),
        )

    /** Explicitly preserved contexts. */
    val vaultedGroups: StateFlow<List<MeshRoom>> = _groups
        .map { groupMap -> groupMap.values.filter { it.isVaulted }.toList() }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList(),
        )

    val groups: StateFlow<List<MeshRoom>> = _groups
        .map { it.values.toList() }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList(),
        )

    private val _peers = MutableStateFlow<Map<String, PeerEntity>>(emptyMap())
    private val _contacts = MutableStateFlow<Map<String, ContactEntity>>(emptyMap())

    init {
        loadData()
        // Ensure default rooms exist immediately (GLOBAL / SILENCE)
        if (!_groups.value.containsKey(MeshRoom.ID_GLOBAL)) {
            _groups.update { it + (MeshRoom.ID_GLOBAL to MeshRoom(id = MeshRoom.ID_GLOBAL, name = "GLOBAL ROOM", scope = MeshRoom.SCOPE_PUBLIC)) }
        }
        if (!_groups.value.containsKey(MeshRoom.ID_SILENCE)) {
            _groups.update { it + (MeshRoom.ID_SILENCE to MeshRoom(id = MeshRoom.ID_SILENCE, name = "SILENCE", scope = MeshRoom.SCOPE_LOCAL)) }
        }
        saveData()
        scope.launch {
            autoArchiveRooms()
        }
    }

    /** Interrogates local binary storage and raw SQLite to decrypt the mesh state. */
    private fun loadData() {
        if (messagesLogFile.exists()) {
            try {
                messagesLogFile.inputStream().use { fis ->
                    val dis = DataInputStream(fis)
                    while (fis.available() > 0) {
                        val length = dis.readInt()
                        val encrypted = ByteArray(length)
                        dis.readFully(encrypted)
                        try {
                            val decrypted = cryptoManager.decryptLocal(encrypted)
                            val message = Json.decodeFromString<MeshMessage>(decrypted.decodeToString())
                            database.insertMessage(message, encrypted)
                        } catch (_: Exception) {}
                    }
                }
                messagesLogFile.delete()
            } catch (e: Exception) { e.printStackTrace() }
        }

        try {
            val rawMessages = database.getAllRawMessages()
            val messageMap = mutableMapOf<String, MeshMessage>()
            rawMessages.forEach { encrypted ->
                try {
                    val decrypted = cryptoManager.decryptLocal(encrypted)
                    val message = Json.decodeFromString<MeshMessage>(decrypted.decodeToString())
                    messageMap[message.messageId] = message
                } catch (_: Exception) {}
            }
            _messages.value = messageMap.values.asSequence().sortedBy { it.timestamp }.toList()
        } catch (e: Exception) { e.printStackTrace() }
        if (roomsFile.exists()) {
            try {
                val encrypted = roomsFile.readBytes()
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

    private fun saveMessageToDb(message: MeshMessage) {
        scope.launch {
            try {
                val json = Json.encodeToString(message)
                val encrypted = cryptoManager.encryptLocal(json.encodeToByteArray())
                database.insertMessage(message, encrypted)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun saveData() {
        scope.launch {
            try {
                val roomsJson = Json.encodeToString(_groups.value)
                val encryptedRooms = cryptoManager.encryptLocal(roomsJson.encodeToByteArray())
                roomsFile.writeBytes(encryptedRooms)

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

    // --- Message Operations ---
    fun getAllMessages() = messages

    /** Returns raw encrypted messages since a specific timestamp for sync. */
    fun getRawMessagesSince(timestamp: Long): List<ByteArray> = database.getRawMessagesSince(timestamp)

    /** Returns the latest message ID in the local database. */
    fun getLatestMessageId(): String? = database.getLatestMessageId()

    /** 
     * Incorporates a new message into the stream.
     * Uses LWW-CRDT logic for shared items to ensure deterministic state across the mesh.
     */
    fun upsertMessage(message: MeshMessage) {
        if (message.content.isBlank()) return

        if (message.type == MeshMessage.TYPE_CONSENSUS_VOTE) {
            handleConsensusVote(message)
            return
        }

        _messages.update { current ->
            val existingIndex = current.indexOfFirst { it.messageId == message.messageId }
            
            if (existingIndex != -1) {
                val existing = current[existingIndex]
                
                // LWW CRDT for shared item mutation
                val isMutableType = (message.type == MeshMessage.TYPE_NOTE_UPDATE) || (message.type == MeshMessage.TYPE_ASSIGNMENT_TASK)
                
                if (isMutableType) {
                    if ((message.noteVersion > existing.noteVersion) || 
                        ((message.noteVersion == existing.noteVersion) && (message.timestamp > existing.timestamp))) {
                        saveMessageToDb(message)
                        current.toMutableList().apply { set(existingIndex, message) }
                    } else {
                        current
                    }
                } else {
                    current
                }
            } else {
                saveMessageToDb(message)
                (current + message).sortedBy { it.timestamp }
            }
        }
        
        // AUTO-PRUNE: Maintain history retention limits per context
        val targetGid = message.groupId
        if (targetGid != null) {
            scope.launch {
                pruneHistory(targetGid)
            }
        }
    }

    /**
     * Social Logic: Processes a consensus vote to adjust the weight of a target message.
     */
    private fun handleConsensusVote(voteMessage: MeshMessage) {
        val targetMessageId = voteMessage.parentMessageId ?: return
        val weight = try { voteMessage.content.toInt() } catch (_: Exception) { 0 }
        
        _messages.update { current ->
            current.map { 
                if (it.messageId == targetMessageId) {
                    val newWeight = it.resonanceWeight + weight
                    database.updateWeight(targetMessageId, newWeight)
                    it.copy(resonanceWeight = newWeight)
                } else it
            }
        }
    }

    /**
     * Retrieves high-priority messages for the room header.
     */
    fun getHighResonanceMessages(groupId: String, limit: Int = 3): StateFlow<List<MeshMessage>> {
        return messages.map { list ->
            list.asSequence()
                .filter { (it.groupId == groupId) && (it.resonanceWeight > 0) }
                .sortedByDescending { it.resonanceWeight }
                .take(limit)
                .toList()
        }.stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    /** Evicts low-priority messages once a context exceeds retention limits. */
    private fun pruneHistory(groupId: String) {
        val group = getGroup(groupId) ?: return
        val currentMessages = _messages.value.filter { it.groupId == groupId }
        
        val maxLimit = if (group.scope == MeshRoom.SCOPE_PRIVATE) 2000 else historyRetentionLimit
        
        if (currentMessages.size > maxLimit) {
            val toRemoveCount = currentMessages.size - maxLimit
            
            val messagesToDelete = currentMessages.asSequence()
                .filter { it.messageId !in group.pinnedMessageIds }
                .filter { it.type != MeshMessage.TYPE_ASSIGNMENT_TASK }
                .filter { !it.isMeta }
                .sortedBy { it.timestamp }
                .take(toRemoveCount)
                .toList()

            messagesToDelete.forEach { deleteMessage(it.messageId) }
            Log.i("MessageStore", "Pruned ${messagesToDelete.size} messages in $groupId.")
        }
    }

    fun updateMessageStatus(messageId: String, status: Int) {
        var updated: MeshMessage? = null
        _messages.update { list ->
            list.map {
                if (it.messageId == messageId) {
                    val m = it.copy(status = status)
                    updated = m
                    m
                } else it
            }
        }
        updated?.let { saveMessageToDb(it) }
    }

    fun clearAllMessages() {
        _messages.value = emptyList()
        if (messagesLogFile.exists()) messagesLogFile.delete()
    }

    fun deleteMessage(messageId: String) {
        val message = _messages.value.find { it.messageId == messageId }
        if (message?.type == MeshMessage.TYPE_IMAGE || message?.type == MeshMessage.TYPE_FILE) {
            message.content.let { path ->
                try {
                    val file = File(path)
                    if (file.exists() && file.absolutePath.contains(context.filesDir.absolutePath)) {
                        file.delete()
                    }
                } catch (_: Exception) {}
            }
        }
        database.deleteMessage(messageId)
        _messages.update { it.filter { m -> m.messageId != messageId } }
    }

    // --- Archive ---

    /** Protocols automatically move inactive rooms into archive. */
    fun autoArchiveRooms() {
        val now = System.currentTimeMillis()
        _groups.update { current ->
            current.mapValues { (id, group) ->
                val isDefault = id == MeshRoom.ID_GLOBAL || id == MeshRoom.ID_SILENCE
                if (!isDefault && !group.isArchived && !group.isVaulted && (now - group.lastMessageTimestamp) > MeshRoom.ARCHIVE_THRESHOLD_MS) {
                    group.copy(isArchived = true)
                } else {
                    group
                }
            }
        }
        saveData()
    }

    fun vaultGroup(groupId: String, isVaulted: Boolean) {
        _groups.update { current ->
            current[groupId]?.let { 
                current + (groupId to it.copy(isVaulted = isVaulted, vaultTimestamp = if (isVaulted) System.currentTimeMillis() else null))
            } ?: current
        }
        saveData()
    }

    fun seniorVaultGroup(groupId: String, isSeniorVault: Boolean) {
        _groups.update { current ->
            current[groupId]?.let { 
                current + (groupId to it.copy(isSeniorVault = isSeniorVault))
            } ?: current
        }
        saveData()
    }

    fun restoreFromVault(groupId: String) {
        _groups.update { current ->
            current[groupId]?.let { 
                current + (groupId to it.copy(isArchived = false, isVaulted = false, lastMessageTimestamp = System.currentTimeMillis()))
            } ?: current
        }
        saveData()
    }

    /** Prunes media older than 90 days. */
    fun pruneMedia(thresholdMs: Long) {
        val now = System.currentTimeMillis()
        val allGroups = _groups.value.values
        val allPinnedMessages = allGroups.asSequence().flatMap { it.pinnedMessageIds }.toSet()
        val vaultedGroupIds = allGroups.asSequence().filter { it.isVaulted }.map { it.id }.toSet()
        val seniorVaultIds = allGroups.asSequence().filter { it.isSeniorVault }.map { it.id }.toSet()

        _messages.value.forEach { message ->
            val isFromVaultedGroup = message.groupId in vaultedGroupIds
            val isFromSeniorVault = message.groupId in seniorVaultIds
            val isPermanentMemory = message.type == MeshMessage.TYPE_MEMORY
            
            if ((now - message.timestamp) > thresholdMs && message.messageId !in allPinnedMessages && !isFromVaultedGroup && !isFromSeniorVault && !isPermanentMemory) {
                if (message.type == MeshMessage.TYPE_IMAGE || message.type == MeshMessage.TYPE_FILE) {
                    message.content.let { path ->
                        try {
                            val file = File(path)
                            if (file.exists() && file.absolutePath.contains(context.filesDir.absolutePath)) {
                                file.delete()
                                Log.i("MessageStore", "Pruned media: ${message.messageId}")
                            }
                        } catch (_: Exception) {}
                    }
                }
            }
        }
    }

    // --- Room Operations ---
    fun insertGroup(group: MeshRoom) {
        _groups.update { it + (group.id to group) }
        saveData()
    }

    /** Adds a user to a specific room. */
    fun joinGroup(groupId: String, userId: String) {
        _groups.update { current ->
            current[groupId]?.let { group ->
                current + (groupId to group.copy(memberIds = group.memberIds + userId))
            } ?: current
        }
        saveData()
    }

    /**
     * Verifies if a user has participation rights in a room.
     */
    fun isMember(groupId: String, userId: String): Boolean {
        val group = _groups.value[groupId] ?: return false
        if (group.scope == MeshRoom.SCOPE_PUBLIC) return true
        return group.isDefaultRoom || userId in group.allMemberIds
    }

    fun getGroup(id: String) = _groups.value[id]

    /** Orchestrates member partitioning for scalability. */
    fun updateGroupMembers(groupId: String, memberIds: Set<String>) {
        _groups.update { current ->
            current[groupId]?.let { group ->
                val cappedMembers = memberIds.asSequence().take(MeshRoom.MAX_MEMBERS_PER_SECTION).toSet()
                
                val updatedGroup = if (cappedMembers.size > group.partitionThreshold) {
                    val sectionId = "section_${group.memberSections.size}"
                    val newSections = group.memberSections.toMutableMap()
                    newSections[sectionId] = cappedMembers.take(group.partitionThreshold).toSet()
                    group.copy(memberIds = emptySet(), memberSections = newSections)
                } else {
                    group.copy(memberIds = cappedMembers)
                }
                current + (groupId to updatedGroup)
            } ?: current
        }
        saveData()
    }

    fun updateGroupScope(groupId: String, scope: Int) {
        _groups.update { current ->
            current[groupId]?.let { 
                current + (groupId to it.copy(scope = scope))
            } ?: current
        }
        saveData()
    }

    fun updateRoomLastMessage(groupId: String, timestamp: Long) {
        _groups.update { current ->
            current[groupId]?.let { 
                current + (groupId to it.copy(lastMessageTimestamp = timestamp))
            } ?: current
        }
        saveData()
    }

    fun addRoomSchedule(groupId: String, schedule: RoomEvent) {
        _groups.update { current ->
            current[groupId]?.let { group ->
                current + (groupId to group.copy(schedules = group.schedules + schedule))
            } ?: current
        }
        saveData()
    }

    // --- Contact Operations ---
    fun getAllContacts() = _contacts.asStateFlow().map { it.values.toList() }

    fun getContact(id: String) = _contacts.value[id]

    fun insertContact(contact: ContactEntity) {
        _contacts.update { it + (contact.id to contact) }
        saveData()
    }

    fun deleteContact(id: String) {
        _contacts.update { it - id }
        saveData()
    }

    fun deleteAllContacts() {
        _contacts.value = emptyMap()
        saveData()
    }
}
