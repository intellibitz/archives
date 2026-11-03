/**
 * BLUKIT DATA: MESSAGE REPOSITORY
 *
 * The secure, offline persistence engine for Messages.
 * Orchestrates local Groups and message history using Room 3.0 and Paging 3.
 */
package cc.thevar.blukit.data.local

import android.content.Context
import android.util.Log
import cc.thevar.blukit.data.crypto.CryptoManager
import cc.thevar.blukit.data.local.dao.MessageDao
import cc.thevar.blukit.data.local.dao.GroupDao
import cc.thevar.blukit.data.local.dao.ContactDao
import cc.thevar.blukit.domain.model.Message
import cc.thevar.blukit.domain.model.Group
import cc.thevar.blukit.domain.model.GroupEvent
import cc.thevar.blukit.domain.model.ContactEntity
import cc.thevar.blukit.domain.model.PeerEntity
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Manages secure storage and reactive state of all connection interactions.
 */
class MessageRepository(
    private val context: Context,
    private val cryptoManager: CryptoManager,
    private val messageDao: MessageDao,
    private val groupDao: GroupDao,
    private val contactDao: ContactDao,
    private val ioDispatcher: kotlinx.coroutines.CoroutineDispatcher = Dispatchers.IO,
    private val historyRetentionLimit: Int = 1000,
) {
    private val repositoryScope = CoroutineScope(SupervisorJob() + ioDispatcher)

    // --- Reactive State Flows ---
    val messages: StateFlow<List<Message>> = messageDao.getAllMessages()
        .stateIn(repositoryScope, SharingStarted.Eagerly, emptyList())

    /** Public frequencies and active private channels. */
    val activeGroups: StateFlow<List<Group>> = groupDao.getAllGroups()
        .map { groups -> groups.filter { !it.isArchived } }
        .stateIn(repositoryScope, SharingStarted.Eagerly, emptyList())

    /** Groups that haven't pulsed in 30 days. */
    val archivedGroups: StateFlow<List<Group>> = groupDao.getAllGroups()
        .map { groups -> groups.filter { it.isArchived && !it.isVaulted } }
        .stateIn(repositoryScope, SharingStarted.Eagerly, emptyList())

    /** Explicitly preserved contexts. */
    val vaultedGroups: StateFlow<List<Group>> = groupDao.getAllGroups()
        .map { groups -> groups.filter { it.isVaulted } }
        .stateIn(repositoryScope, SharingStarted.Eagerly, emptyList())

    val groups: StateFlow<List<Group>> = groupDao.getAllGroups()
        .stateIn(repositoryScope, SharingStarted.Eagerly, emptyList())

    init {
        // Ensure default groups exist immediately (GLOBAL / SILENCE)
        repositoryScope.launch {
            if (groupDao.getGroupById(Group.ID_GLOBAL) == null) {
                groupDao.upsertGroup(Group(id = Group.ID_GLOBAL, name = "GLOBAL GROUP", scope = Group.SCOPE_PUBLIC))
            }
            if (groupDao.getGroupById(Group.ID_SILENCE) == null) {
                groupDao.upsertGroup(Group(id = Group.ID_SILENCE, name = "SILENCE", scope = Group.SCOPE_LOCAL))
            }
            autoArchiveGroups()
        }
    }

    // --- Paging Support ---
    fun getMessagesForGroup(groupId: String): Flow<PagingData<Message>> {
        return Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            pagingSourceFactory = { messageDao.getMessagesForGroupPaging(groupId) }
        ).flow
    }

    fun getTimelineMessages(): Flow<PagingData<Message>> {
        return Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            pagingSourceFactory = { messageDao.getTimelineMessagesPaging() }
        ).flow
    }

    fun getAllMessagesPaging(): Flow<PagingData<Message>> {
        return Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            pagingSourceFactory = { messageDao.getAllMessagesPaging() }
        ).flow
    }

    fun getChildMessages(parentId: String): Flow<PagingData<Message>> {
        return Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            pagingSourceFactory = { messageDao.getChildMessagesPaging(parentId) }
        ).flow
    }

    suspend fun getMessage(messageId: String): Message? = messageDao.getMessageById(messageId)

    // --- Message Operations ---
    suspend fun getLatestMessageId(): String? = messageDao.getLatestMessageId()

    /** 
     * Incorporates a new Message into the stream.
     * Uses LWW-CRDT logic for shared items.
     */
    fun upsertMessage(message: Message) {
        if (message.content.isBlank()) return

        repositoryScope.launch {
            messageDao.upsertMessage(message)
            
            // AUTO-PRUNE: Maintain history retention limits per context
            val targetGid = message.groupId
            if (targetGid != null) {
                pruneHistory(targetGid)
            }
        }
    }

    /** Evicts low-priority Messages once a context exceeds retention limits. */
    private suspend fun pruneHistory(groupId: String) {
        // Implementation simplified for now
    }

    fun updateMessageStatus(messageId: String, status: Int) {
        repositoryScope.launch { messageDao.updateStatus(messageId, status) }
    }

    fun clearAllMessages() {
        repositoryScope.launch { messageDao.clearAll() }
    }

    fun deleteMessage(messageId: String) {
        repositoryScope.launch { messageDao.deleteMessage(messageId) }
    }

    fun incrementAnchoredCount(messageId: String) {
        // Logic to update anchored count
    }

    // --- Group Operations ---
    fun autoArchiveGroups() {
        repositoryScope.launch {
            val now = System.currentTimeMillis()
            // We need to collect the flow once to get current value
            // In a real app we might use a one-shot query.
            // For now, let's keep it simple.
        }
    }

    fun vaultGroup(groupId: String, isVaulted: Boolean) {
        repositoryScope.launch {
            groupDao.getGroupById(groupId)?.let {
                groupDao.upsertGroup(it.copy(isVaulted = isVaulted, vaultTimestamp = if (isVaulted) System.currentTimeMillis() else null))
            }
        }
    }

    fun seniorVaultGroup(groupId: String, isSeniorVault: Boolean) {
        repositoryScope.launch {
            groupDao.getGroupById(groupId)?.let {
                groupDao.upsertGroup(it.copy(isSeniorVault = isSeniorVault))
            }
        }
    }

    fun restoreFromVault(groupId: String) {
        repositoryScope.launch {
            groupDao.getGroupById(groupId)?.let {
                groupDao.upsertGroup(it.copy(isArchived = false, isVaulted = false, lastMessageTimestamp = System.currentTimeMillis()))
            }
        }
    }

    fun insertGroup(group: Group) {
        repositoryScope.launch { groupDao.upsertGroup(group) }
    }

    fun joinGroup(groupId: String, userId: String) {
        repositoryScope.launch {
            groupDao.getGroupById(groupId)?.let { group ->
                groupDao.upsertGroup(group.copy(memberIds = group.memberIds + userId))
            }
        }
    }

    suspend fun isMember(groupId: String, userId: String): Boolean {
        val group = groupDao.getGroupById(groupId) ?: return false
        if (group.scope == Group.SCOPE_PUBLIC) return true
        return group.isDefaultGroup || userId in group.allMemberIds
    }

    suspend fun getGroup(id: String) = groupDao.getGroupById(id)

    fun updateGroupMembers(groupId: String, memberIds: Set<String>) {
        repositoryScope.launch {
            groupDao.getGroupById(groupId)?.let { group ->
                groupDao.upsertGroup(group.copy(memberIds = memberIds))
            }
        }
    }

    fun updateGroupScope(groupId: String, scopeVal: Int) {
        repositoryScope.launch {
            groupDao.getGroupById(groupId)?.let {
                groupDao.upsertGroup(it.copy(scope = scopeVal))
            }
        }
    }

    fun updateGroupLastMessage(groupId: String, timestamp: Long) {
        repositoryScope.launch {
            groupDao.getGroupById(groupId)?.let {
                groupDao.upsertGroup(it.copy(lastMessageTimestamp = timestamp))
            }
        }
    }

    fun addGroupSchedule(groupId: String, schedule: GroupEvent) {
        repositoryScope.launch {
            groupDao.getGroupById(groupId)?.let { group ->
                groupDao.upsertGroup(group.copy(schedules = group.schedules + schedule))
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
        }.stateIn(repositoryScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun getAllContacts(): Flow<List<ContactEntity>> = contactDao.getAllContacts()

    fun deleteContact(id: String) {
        repositoryScope.launch { contactDao.deleteContact(id) }
    }

    fun deleteAllContacts() {
        repositoryScope.launch { contactDao.deleteAll() }
    }
    fun pruneMedia(thresholdMs: Long) {
        // Implementation logic
    }

    suspend fun getRawMessagesSince(timestamp: Long): List<ByteArray> {
        val messages = messageDao.getMessagesSince(timestamp)
        return messages.map { message ->
            val json = Json.encodeToString(Message.serializer(), message)
            cryptoManager.encryptLocal(json.encodeToByteArray())
        }
    }
}
