/**
 * BLUKIT DATA: PULSE STORE
 *
 * The secure, offline persistence engine for Blukit's mesh energy.
 * Orchestrates the "Unified Pulse Frequency" by managing chronological logs and resonance states.
 * 
 * Features:
 * - Encrypted Binary Logs: Uses CryptoManager to protect local pulse history.
 * - LWW (Last-Write-Wins) CRDT: Conflict-free resolution for shared notes and tasks.
 * - Swarm Logic: Weight-based pulse prioritization for collective resonance.
 * - Pulse Decay: Automated self-cleaning logic for media and inactive crowds.
 * - Resonance Drill-Down: Hierarchical state management for active, archived, and vaulted contexts.
 */
package cc.thevar.blukit.data.local

import android.content.Context
import android.util.Log
import cc.thevar.blukit.data.crypto.CryptoManager
import cc.thevar.blukit.data.local.db.PulseDatabase
import cc.thevar.blukit.data.local.entities.ContactEntity
import cc.thevar.blukit.data.local.entities.PeerEntity
import cc.thevar.blukit.domain.model.MessagePayload
import cc.thevar.blukit.domain.model.Resonance
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
 * @param historyRetentionLimit The maximum number of pulses to keep before triggering eviction.
 */
class PulseStore(
    private val context: Context,
    private val cryptoManager: CryptoManager,
    ioDispatcher: kotlinx.coroutines.CoroutineDispatcher = Dispatchers.IO,
    private val historyRetentionLimit: Int = 1000,
) {
    // --- Persistence Anchors ---
    private val pulsesLogFile = File(context.filesDir, "pulses_log.bin")
    private val groupsFile = File(context.filesDir, "groups.bin")
    private val peersFile = File(context.filesDir, "peers.bin")
    private val contactsFile = File(context.filesDir, "contacts.bin")

    private val database = PulseDatabase(context)
    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)

    // --- Reactive State Flows ---
    private val _messages = MutableStateFlow<List<MessagePayload>>(emptyList())
    /** The complete chronological life stream of pulses. */
    val messages: StateFlow<List<MessagePayload>> = _messages.asStateFlow()

    private val _groups = MutableStateFlow<Map<String, Resonance>>(emptyMap())
    
    /** Public frequencies and active private chains. */
    val activeGroups: StateFlow<List<Resonance>> = _groups
        .map { groupMap -> groupMap.values.filter { !it.isArchived }.toList() }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList(),
        )

    /** Crowds that haven't pulsed in 30 days. */
    val archivedGroups: StateFlow<List<Resonance>> = _groups
        .map { groupMap -> groupMap.values.filter { it.isArchived && !it.isVaulted }.toList() }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList(),
        )

    /** Explicitly preserved contexts (Sunk Pulses). */
    val vaultedGroups: StateFlow<List<Resonance>> = _groups
        .map { groupMap -> groupMap.values.filter { it.isVaulted }.toList() }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList(),
        )

    val groups: StateFlow<List<Resonance>> = _groups
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
        // Ensure default ties exist immediately (THE CROWD / SILENCE)
        if (!_groups.value.containsKey(Resonance.ID_GLOBAL)) {
            _groups.update { it + (Resonance.ID_GLOBAL to Resonance(id = Resonance.ID_GLOBAL, name = "GLOBAL GROUP", scope = Resonance.SCOPE_PUBLIC)) }
        }
        if (!_groups.value.containsKey(Resonance.ID_SILENCE)) {
            _groups.update { it + (Resonance.ID_SILENCE to Resonance(id = Resonance.ID_SILENCE, name = "SILENCE", scope = Resonance.SCOPE_LOCAL)) }
        }
        saveData()
        scope.launch {
            autoArchiveCrowds()
        }
    }

    /** Interrogates local binary storage and raw SQLite DAG to decrypt the mesh state. */
    private fun loadData() {
        // MIGRATION: Move from binary log to SQLite DAG if necessary
        if (pulsesLogFile.exists()) {
            try {
                pulsesLogFile.inputStream().use { fis ->
                    val dis = DataInputStream(fis)
                    while (fis.available() > 0) {
                        val length = dis.readInt()
                        val encrypted = ByteArray(length)
                        dis.readFully(encrypted)
                        try {
                            val decrypted = cryptoManager.decryptLocal(encrypted)
                            val message = Json.decodeFromString<MessagePayload>(decrypted.decodeToString())
                            database.insertPulse(message, encrypted)
                        } catch (_: Exception) {}
                    }
                }
                pulsesLogFile.delete()
            } catch (e: Exception) { e.printStackTrace() }
        }

        try {
            val rawPulses = database.getAllRawPulses()
            val messageMap = mutableMapOf<String, MessagePayload>()
            rawPulses.forEach { encrypted ->
                try {
                    val decrypted = cryptoManager.decryptLocal(encrypted)
                    val message = Json.decodeFromString<MessagePayload>(decrypted.decodeToString())
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

    /** Persists an encrypted pulse to the SQLite DAG. */
    private fun savePulseToDb(message: MessagePayload) {
        scope.launch {
            try {
                val json = Json.encodeToString(message)
                val encrypted = cryptoManager.encryptLocal(json.encodeToByteArray())
                database.insertPulse(message, encrypted)
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

    /** Returns raw encrypted pulses since a specific timestamp for differential sync. */
    fun getRawPulsesSince(timestamp: Long): List<ByteArray> = database.getRawPulsesSince(timestamp)

    /** Returns the latest pulse ID in the local DAG. */
    fun getLatestPulseId(): String? = database.getLatestPulseId()

    /** 
     * Incorporates a new pulse into the stream.
     * Uses LWW-CRDT logic for notes and tasks to ensure deterministic state across the mesh.
     * Also handles Crowd AI consensus voting to adjust resonance weights.
     */
    fun upsertMessage(message: MessagePayload) {
        if (message.content.isBlank()) return // Validation: Ignore empty pulses

        if (message.type == MessagePayload.TYPE_CONSENSUS_VOTE) {
            handleConsensusVote(message)
            return
        }

        _messages.update { current ->
            val existingIndex = current.indexOfFirst { it.messageId == message.messageId }
            
            if (existingIndex != -1) {
                val existing = current[existingIndex]
                
                // --- GIT-STYLE DAG MERGE ---
                // If the messageId matches but hashes differ, we have a branch.
                // In Blukit's decentralized model, we prioritize the one with higher noteVersion 
                // or later timestamp (LWW).
                
                // LWW CRDT for Note & Task Mutation
                val isMutableType = (message.type == MessagePayload.TYPE_NOTE_UPDATE) || (message.type == MessagePayload.TYPE_ASSIGNMENT_TASK)
                
                if (isMutableType) {
                    if ((message.noteVersion > existing.noteVersion) || 
                        ((message.noteVersion == existing.noteVersion) && (message.timestamp > existing.timestamp))) {
                        savePulseToDb(message)
                        current.toMutableList().apply { set(existingIndex, message) }
                    } else {
                        current
                    }
                } else {
                    // For static pulses (Text/Image), we don't overwrite unless version/timestamp is newer (unlikely for same ID)
                    current
                }
            } else {
                savePulseToDb(message)
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
     * SWARM LOGIC: Processes a consensus vote to adjust the weight of a target pulse.
     */
    private fun handleConsensusVote(votePulse: MessagePayload) {
        val targetPulseId = votePulse.parentMessageId ?: return
        val weight = try { votePulse.content.toInt() } catch (_: Exception) { 0 }
        
        _messages.update { current ->
            current.map { 
                if (it.messageId == targetPulseId) {
                    val newWeight = it.resonanceWeight + weight
                    database.updateWeight(targetPulseId, newWeight)
                    it.copy(resonanceWeight = newWeight)
                } else it
            }
        }
    }

    /**
     * CROWD CANVAS: Retrieves high-priority pulses for the spatial header.
     */
    fun getHighResonancePulses(groupId: String, limit: Int = 3): StateFlow<List<MessagePayload>> {
        return messages.map { list ->
            list.asSequence()
                .filter { (it.groupId == groupId) && (it.resonanceWeight > 0) }
                .sortedByDescending { it.resonanceWeight }
                .take(limit)
                .toList()
        }.stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    /** Evicts low-priority pulses once a context exceeds retention limits. */
    private fun pruneHistory(groupId: String) {
        val group = getGroup(groupId) ?: return
        val currentMessages = _messages.value.filter { it.groupId == groupId }
        
        // DYNAMIC LIMITS: Pinned and meta-pulses are kept longer
        val maxLimit = if (group.scope == Resonance.SCOPE_PRIVATE) 2000 else historyRetentionLimit
        
        if (currentMessages.size > maxLimit) {
            val toRemoveCount = currentMessages.size - maxLimit
            
            // PRIORITY-AWARE EVICTION:
            // 1. Keep Pinned pulses
            // 2. Keep Task updates (Assignments)
            // 3. Keep meta-headers for threads
            val messagesToDelete = currentMessages.asSequence()
                .filter { it.messageId !in group.pinnedPulseIds }
                .filter { it.type != MessagePayload.TYPE_ASSIGNMENT_TASK }
                .filter { !it.isMeta }
                .sortedBy { it.timestamp }
                .take(toRemoveCount)
                .toList()

            messagesToDelete.forEach { deleteMessage(it.messageId) }
            Log.i("PulseStore", "Smarter Eviction for $groupId. Removed ${messagesToDelete.size} low-priority pulses.")
        }
    }

    fun updateMessageStatus(messageId: String, status: Int) {
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
        updated?.let { savePulseToDb(it) }
    }

    fun clearAllMessages() {
        _messages.value = emptyList()
        if (pulsesLogFile.exists()) pulsesLogFile.delete()
    }

    fun deleteMessage(messageId: String) {
        val message = _messages.value.find { it.messageId == messageId }
        if (message?.type == MessagePayload.TYPE_IMAGE || message?.type == MessagePayload.TYPE_FILE) {
            message.content.let { path ->
                try {
                    val file = File(path)
                    if (file.exists() && file.absolutePath.contains(context.filesDir.absolutePath)) {
                        file.delete()
                    }
                } catch (_: Exception) {}
            }
        }
        database.deletePulse(messageId)
        _messages.update { it.filter { m -> m.messageId != messageId } }
    }

    // --- Vault & Archiving ---

    /** Protocols automatically move inactive crowds into a "Sunk Pulse" vault. */
    fun autoArchiveCrowds() {
        val now = System.currentTimeMillis()
        _groups.update { current ->
            current.mapValues { (id, group) ->
                val isDefault = id == Resonance.ID_GLOBAL || id == Resonance.ID_SILENCE
                if (!isDefault && !group.isArchived && !group.isVaulted && (now - group.lastPulseTimestamp) > Resonance.ARCHIVE_THRESHOLD_MS) {
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
                current + (groupId to it.copy(isArchived = false, isVaulted = false, lastPulseTimestamp = System.currentTimeMillis()))
            } ?: current
        }
        saveData()
    }

    /** Prunes media older than 90 days, exempting Pinned/Senior pulses. */
    fun pruneMedia(thresholdMs: Long) {
        val now = System.currentTimeMillis()
        val allGroups = _groups.value.values
        val allPinnedPulses = allGroups.asSequence().flatMap { it.pinnedPulseIds }.toSet()
        val vaultedGroupIds = allGroups.asSequence().filter { it.isVaulted }.map { it.id }.toSet()
        val seniorVaultIds = allGroups.asSequence().filter { it.isSeniorVault }.map { it.id }.toSet()

        _messages.value.forEach { message ->
            val isFromVaultedGroup = message.groupId in vaultedGroupIds
            val isFromSeniorVault = message.groupId in seniorVaultIds
            val isPermanentMemory = message.type == MessagePayload.TYPE_MEMORY
            
            if ((now - message.timestamp) > thresholdMs && message.messageId !in allPinnedPulses && !isFromVaultedGroup && !isFromSeniorVault && !isPermanentMemory) {
                if (message.type == MessagePayload.TYPE_IMAGE || message.type == MessagePayload.TYPE_FILE) {
                    message.content.let { path ->
                        try {
                            val file = File(path)
                            if (file.exists() && file.absolutePath.contains(context.filesDir.absolutePath)) {
                                file.delete()
                                Log.i("PulseStore", "Pruned media: ${message.messageId}")
                            }
                        } catch (_: Exception) {}
                    }
                }
            }
        }
    }

    // --- Group Operations ---
    fun insertGroup(group: Resonance) {
        _groups.update { it + (group.id to group) }
        saveData()
    }

    /** Adds a user to a specific crowd or chain membership list. */
    fun joinGroup(groupId: String, userId: String) {
        _groups.update { current ->
            current[groupId]?.let { group ->
                current + (groupId to group.copy(memberIds = group.memberIds + userId))
            } ?: current
        }
        saveData()
    }

    /**
     * Verifies if a user has participation rights in a context.
     * Members of the default root crowd are always permitted.
     */
    fun isMember(groupId: String, userId: String): Boolean {
        val group = _groups.value[groupId] ?: return false
        return group.isDefaultCrowd || userId in group.allMemberIds
    }

    fun getGroup(id: String) = _groups.value[id]

    /** Orchestrates member partitioning for high-density scalability. */
    fun updateGroupMembers(groupId: String, memberIds: Set<String>) {
        _groups.update { current ->
            current[groupId]?.let { group ->
                // LIMIT ENFORCEMENT: Absolute cap on section size
                val cappedMembers = memberIds.asSequence().take(Resonance.MAX_MEMBERS_PER_SECTION).toSet()
                
                val updatedGroup = if (cappedMembers.size > group.partitionThreshold) {
                    // PARTITIONING: Split members into sections for scalability
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

    fun updateGroupLastPulse(groupId: String, timestamp: Long) {
        _groups.update { current ->
            current[groupId]?.let { 
                current + (groupId to it.copy(lastPulseTimestamp = timestamp))
            } ?: current
        }
        saveData()
    }

    fun addCrowdSchedule(groupId: String, schedule: cc.thevar.blukit.domain.model.CrowdSchedule) {
        _groups.update { current ->
            current[groupId]?.let { group ->
                current + (groupId to group.copy(schedules = group.schedules + schedule))
            } ?: current
        }
        saveData()
    }

    // --- Peer Operations ---
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
