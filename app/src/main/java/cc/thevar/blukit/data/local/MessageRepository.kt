/**
 * BLUKIT DATA: MESSAGE REPOSITORY
 *
 * The secure, offline persistence engine for Messages.
 * Orchestrates local Groups and message history.
 */
package cc.thevar.blukit.data.local

import android.content.Context
import android.util.Log
import cc.thevar.blukit.data.crypto.CryptoManager
import cc.thevar.blukit.data.local.db.MessageDatabase
import cc.thevar.blukit.data.local.entities.ContactEntity
import cc.thevar.blukit.data.local.entities.PeerEntity
import cc.thevar.blukit.domain.model.Message
import cc.thevar.blukit.domain.model.Group
import cc.thevar.blukit.domain.model.GroupEvent
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
 * Manages secure storage and reactive state of all connection interactions.
 * 
 * @param historyRetentionLimit The maximum number of Messages to keep before triggering eviction.
 */
class MessageRepository(
    private val context: Context,
    private val cryptoManager: CryptoManager,
    ioDispatcher: kotlinx.coroutines.CoroutineDispatcher = Dispatchers.IO,
    private val historyRetentionLimit: Int = 1000,
) {
    // --- Persistence Anchors ---
    private val messagesLogFile = File(context.filesDir, "messages_log.bin")
    private val groupsFile = File(context.filesDir, "groups.bin")
    private val sourcesFile = File(context.filesDir, "sources.bin")
    private val contactsFile = File(context.filesDir, "contacts.bin")

    private val database = MessageDatabase(context)
    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)

    // --- Reactive State Flows ---
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    /** The complete chronological life stream of Messages. */
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _groups = MutableStateFlow<Map<String, Group>>(emptyMap())
    
    /** Public frequencies and active private channels. */
    val activeGroups: StateFlow<List<Group>> = _groups
        .map { groupMap -> groupMap.values.filter { !it.isArchived }.toList() }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList(),
        )

    /** Groups that haven't pulsed in 30 days. */
    val archivedGroups: StateFlow<List<Group>> = _groups
        .map { groupMap -> groupMap.values.filter { it.isArchived && !it.isVaulted }.toList() }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList(),
        )

    /** Explicitly preserved contexts. */
    val vaultedGroups: StateFlow<List<Group>> = _groups
        .map { groupMap -> groupMap.values.filter { it.isVaulted }.toList() }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList(),
        )

    val groups: StateFlow<List<Group>> = _groups
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
        // Ensure default groups exist immediately (GLOBAL / SILENCE)
        if (!_groups.value.containsKey(Group.ID_GLOBAL)) {
            _groups.update { it + (Group.ID_GLOBAL to Group(id = Group.ID_GLOBAL, name = "GLOBAL GROUP", scope = Group.SCOPE_PUBLIC)) }
        }
        if (!_groups.value.containsKey(Group.ID_SILENCE)) {
            _groups.update { it + (Group.ID_SILENCE to Group(id = Group.ID_SILENCE, name = "SILENCE", scope = Group.SCOPE_LOCAL)) }
        }
        saveData()
        scope.launch {
            autoArchiveGroups()
        }
    }

    /** Interrogates local binary storage and raw SQLite to decrypt the connection state. */
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
                            val message = Json.decodeFromString<Message>(decrypted.decodeToString())
                            saveMessageToDb(message, encrypted) 
                        } catch (_: Exception) {}
                    }
                }
                messagesLogFile.delete()
            } catch (e: Exception) { e.printStackTrace() }
        }

        try {
            val rawMessages = database.getAllRawMessages()
            val messageMap = mutableMapOf<String, Message>()
            rawMessages.forEach { encrypted ->
                try {
                    val decrypted = cryptoManager.decryptLocal(encrypted)
                    val message = Json.decodeFromString<Message>(decrypted.decodeToString())
                    messageMap[message.messageId] = message
                } catch (_: Exception) {}
            }
            _messages.value = messageMap.values.asSequence().sortedBy { it.timestamp }.toList()
        } catch (e: Exception) { e.printStackTrace() }
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
        if (sourcesFile.exists()) {
            try {
                val encrypted = sourcesFile.readBytes()
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

    private fun saveMessageToDb(message: Message, preEncrypted: ByteArray? = null) {
        scope.launch {
            try {
                val encrypted = if (preEncrypted != null) preEncrypted else {
                    val json = Json.encodeToString(message)
                    cryptoManager.encryptLocal(json.encodeToByteArray())
                }
                database.insertMessage(message, encrypted) 
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun saveData() {
        scope.launch {
            try {
                val groupsJson = Json.encodeToString(_groups.value)
                val encryptedGroups = cryptoManager.encryptLocal(groupsJson.encodeToByteArray())
                groupsFile.writeBytes(encryptedGroups)

                val sourcesJson = Json.encodeToString(_peers.value)
                val encryptedSources = cryptoManager.encryptLocal(sourcesJson.encodeToByteArray())
                sourcesFile.writeBytes(encryptedSources)

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

    /** Returns raw encrypted Messages since a specific timestamp for sync. */
    fun getRawMessagesSince(timestamp: Long): List<ByteArray> = database.getRawMessagesSince(timestamp)

    /** Returns the latest Message ID in the local database. */
    fun getLatestMessageId(): String? = database.getLatestMessageId()

    /** 
     * Incorporates a new Message into the stream.
     * Uses LWW-CRDT logic for shared items to ensure deterministic state across the connection.
     */
    fun upsertMessage(message: Message) {
        if (message.content.isBlank()) return

        if (message.type == Message.TYPE_CONSENSUS_VOTE) {
            handleConsensusVote(message)
            return
        }

        _messages.update { current ->
            val existingIndex = current.indexOfFirst { it.messageId == message.messageId }
            
            if (existingIndex != -1) {
                val existing = current[existingIndex]
                
                // LWW CRDT for shared item mutation
                val isMutableType = (message.type == Message.TYPE_NOTE_UPDATE) || (message.type == Message.TYPE_ASSIGNMENT_TASK)
                
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
     * Social Logic: Processes a consensus vote to adjust the weight of a target Message.
     */
    private fun handleConsensusVote(voteMessage: Message) {
        val targetMessageId = voteMessage.parentMessageId ?: return
        val weight = try { voteMessage.content.toInt() } catch (_: Exception) { 0 }
        
        _messages.update { current ->
            current.map { 
                if (it.messageId == targetMessageId) {
                    val newWeight = it.connectionWeight + weight
                    database.updateWeight(targetMessageId, newWeight)
                    it.copy(connectionWeight = newWeight)
                } else it
            }
        }
    }

    /**
     * Retrieves high-priority Messages for the Group header.
     */
    fun getHighConnectionMessages(groupId: String, limit: Int = 3): StateFlow<List<Message>> {
        return messages.map { list ->
            list.asSequence()
                .filter { (it.groupId == groupId) && (it.connectionWeight > 0) }
                .sortedByDescending { it.connectionWeight }
                .take(limit)
                .toList()
        }.stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    /** Evicts low-priority Messages once a context exceeds retention limits. */
    private fun pruneHistory(groupId: String) {
        val group = getGroup(groupId) ?: return
        val currentMessages = _messages.value.filter { it.groupId == groupId }
        
        val maxLimit = if (group.scope == Group.SCOPE_PRIVATE) 2000 else historyRetentionLimit
        
        if (currentMessages.size > maxLimit) {
            val toRemoveCount = currentMessages.size - maxLimit
            
            val messagesToDelete = currentMessages.asSequence()
                .filter { it.messageId !in group.pinnedMessageIds }
                .filter { it.type != Message.TYPE_ASSIGNMENT_TASK }
                .filter { !it.isMeta }
                .sortedBy { it.timestamp }
                .take(toRemoveCount)
                .toList()

            messagesToDelete.forEach { deleteMessage(it.messageId) }
            Log.i("MessageRepository", "Pruned ${messagesToDelete.size} Messages in $groupId.")
        }
    }

    fun updateMessageStatus(messageId: String, status: Int) {
        var updated: Message? = null
        _messages.update { list ->
            list.map {
                if (it.messageId == messageId) {
                    val e = it.copy(status = status)
                    updated = e
                    e
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
        if (message?.type == Message.TYPE_IMAGE || message?.type == Message.TYPE_FILE) {
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
        _messages.update { it.filter { e -> e.messageId != messageId } }
    }

    /**
     * Increments the anchoring status of a record.
     * High anchoring count indicates strong decentralized persistence.
     */
    fun incrementAnchoredCount(messageId: String) {
        var updated: Message? = null
        _messages.update { list ->
            list.map {
                if (it.messageId == messageId) {
                    val e = it.copy(anchoredCount = (it.anchoredCount + 1).coerceAtMost(10))
                    updated = e
                    e
                } else it
            }
        }
        updated?.let { saveMessageToDb(it) }
    }

    // --- Archive ---

    /** Protocols automatically move inactive Groups into archive. */
    fun autoArchiveGroups() {
        val now = System.currentTimeMillis()
        _groups.update { current ->
            current.mapValues { (id, group) ->
                val isDefault = id == Group.ID_GLOBAL || id == Group.ID_SILENCE
                if (!isDefault && !group.isArchived && !group.isVaulted && (now - group.lastMessageTimestamp) > Group.ARCHIVE_THRESHOLD_MS) {
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
            val isPermanentMemory = message.type == Message.TYPE_MEMORY
            
            if ((now - message.timestamp) > thresholdMs && message.messageId !in allPinnedMessages && !isFromVaultedGroup && !isFromSeniorVault && !isPermanentMemory) {
                if (message.type == Message.TYPE_IMAGE || message.type == Message.TYPE_FILE) {
                    message.content.let { path ->
                        try {
                            val file = File(path)
                            if (file.exists() && file.absolutePath.contains(context.filesDir.absolutePath)) {
                                file.delete()
                                Log.i("MessageRepository", "Pruned media: ${message.messageId}")
                            }
                        } catch (_: Exception) {}
                    }
                }
            }
        }
    }

    // --- Group Operations ---
    fun insertGroup(group: Group) {
        _groups.update { it + (group.id to group) }
        saveData()
    }

    /** Adds a Source to a specific Group. */
    fun joinGroup(groupId: String, userId: String) {
        _groups.update { current ->
            current[groupId]?.let { group ->
                current + (groupId to group.copy(memberIds = group.memberIds + userId))
            } ?: current
        }
        saveData()
    }

    /**
     * Verifies if a Source has participation rights in a Group.
     */
    fun isMember(groupId: String, userId: String): Boolean {
        val group = _groups.value[groupId] ?: return false
        if (group.scope == Group.SCOPE_PUBLIC) return true
        return group.isDefaultGroup || userId in group.allMemberIds
    }

    fun getGroup(id: String) = _groups.value[id]

    /** Orchestrates member partitioning for scalability. */
    fun updateGroupMembers(groupId: String, memberIds: Set<String>) {
        _groups.update { current ->
            current[groupId]?.let { group ->
                val cappedMembers = memberIds.asSequence().take(Group.MAX_MEMBERS_PER_SECTION).toSet()
                
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

    fun updateGroupLastMessage(groupId: String, timestamp: Long) {
        _groups.update { current ->
            current[groupId]?.let { 
                current + (groupId to it.copy(lastMessageTimestamp = timestamp))
            } ?: current
        }
        saveData()
    }

    fun addGroupSchedule(groupId: String, schedule: GroupEvent) {
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
