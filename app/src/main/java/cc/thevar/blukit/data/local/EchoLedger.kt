/**
 * BLUKIT DATA: ECHO LEDGER
 *
 * The secure, offline persistence engine for Echoes on the mesh.
 * Orchestrates local Spheres and existence history.
 */
package cc.thevar.blukit.data.local

import android.content.Context
import android.util.Log
import cc.thevar.blukit.data.crypto.CryptoManager
import cc.thevar.blukit.data.local.db.MessageDatabase
import cc.thevar.blukit.data.local.entities.ContactEntity
import cc.thevar.blukit.data.local.entities.PeerEntity
import cc.thevar.blukit.domain.model.Echo
import cc.thevar.blukit.domain.model.Sphere
import cc.thevar.blukit.domain.model.SphereEvent
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
 * Manages secure storage and reactive state of all resonance interactions.
 * 
 * @param historyRetentionLimit The maximum number of Echoes to keep before triggering eviction.
 */
class EchoLedger(
    private val context: Context,
    private val cryptoManager: CryptoManager,
    ioDispatcher: kotlinx.coroutines.CoroutineDispatcher = Dispatchers.IO,
    private val historyRetentionLimit: Int = 1000,
) {
    // --- Persistence Anchors ---
    private val echoesLogFile = File(context.filesDir, "echoes_log.bin")
    private val spheresFile = File(context.filesDir, "spheres.bin")
    private val sourcesFile = File(context.filesDir, "sources.bin")
    private val contactsFile = File(context.filesDir, "contacts.bin")

    private val database = MessageDatabase(context)
    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)

    // --- Reactive State Flows ---
    private val _echoes = MutableStateFlow<List<Echo>>(emptyList())
    /** The complete chronological life stream of Echoes. */
    val echoes: StateFlow<List<Echo>> = _echoes.asStateFlow()

    private val _spheres = MutableStateFlow<Map<String, Sphere>>(emptyMap())
    
    /** Public frequencies and active private channels. */
    val activeSpheres: StateFlow<List<Sphere>> = _spheres
        .map { sphereMap -> sphereMap.values.filter { !it.isArchived }.toList() }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList(),
        )

    /** Spheres that haven't pulsed in 30 days. */
    val archivedSpheres: StateFlow<List<Sphere>> = _spheres
        .map { sphereMap -> sphereMap.values.filter { it.isArchived && !it.isVaulted }.toList() }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList(),
        )

    /** Explicitly preserved contexts. */
    val vaultedSpheres: StateFlow<List<Sphere>> = _spheres
        .map { sphereMap -> sphereMap.values.filter { it.isVaulted }.toList() }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList(),
        )

    val spheres: StateFlow<List<Sphere>> = _spheres
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
        // Ensure default spheres exist immediately (GLOBAL / SILENCE)
        if (!_spheres.value.containsKey(Sphere.ID_GLOBAL)) {
            _spheres.update { it + (Sphere.ID_GLOBAL to Sphere(id = Sphere.ID_GLOBAL, name = "GLOBAL SPHERE", scope = Sphere.SCOPE_PUBLIC)) }
        }
        if (!_spheres.value.containsKey(Sphere.ID_SILENCE)) {
            _spheres.update { it + (Sphere.ID_SILENCE to Sphere(id = Sphere.ID_SILENCE, name = "SILENCE", scope = Sphere.SCOPE_LOCAL)) }
        }
        saveData()
        scope.launch {
            autoArchiveSpheres()
        }
    }

    /** Interrogates local binary storage and raw SQLite to decrypt the resonance state. */
    private fun loadData() {
        if (echoesLogFile.exists()) {
            try {
                echoesLogFile.inputStream().use { fis ->
                    val dis = DataInputStream(fis)
                    while (fis.available() > 0) {
                        val length = dis.readInt()
                        val encrypted = ByteArray(length)
                        dis.readFully(encrypted)
                        try {
                            val decrypted = cryptoManager.decryptLocal(encrypted)
                            val echo = Json.decodeFromString<Echo>(decrypted.decodeToString())
                            saveEchoToDb(echo, encrypted) // Pass encrypted to avoid double encryption in saveEchoToDb if called this way
                        } catch (_: Exception) {}
                    }
                }
                echoesLogFile.delete()
            } catch (e: Exception) { e.printStackTrace() }
        }

        try {
            val rawEchoes = database.getAllRawMessages()
            val echoMap = mutableMapOf<String, Echo>()
            rawEchoes.forEach { encrypted ->
                try {
                    val decrypted = cryptoManager.decryptLocal(encrypted)
                    val echo = Json.decodeFromString<Echo>(decrypted.decodeToString())
                    echoMap[echo.messageId] = echo
                } catch (_: Exception) {}
            }
            _echoes.value = echoMap.values.asSequence().sortedBy { it.timestamp }.toList()
        } catch (e: Exception) { e.printStackTrace() }
        if (spheresFile.exists()) {
            try {
                val encrypted = spheresFile.readBytes()
                val decrypted = cryptoManager.decryptLocal(encrypted)
                val json = decrypted.decodeToString()
                _spheres.value = Json.decodeFromString(json)
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

    private fun saveEchoToDb(echo: Echo, preEncrypted: ByteArray? = null) {
        scope.launch {
            try {
                val encrypted = if (preEncrypted != null) preEncrypted else {
                    val json = Json.encodeToString(echo)
                    cryptoManager.encryptLocal(json.encodeToByteArray())
                }
                database.insertMessage(echo, encrypted) // Keeping database method name for now, or update it later
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun saveData() {
        scope.launch {
            try {
                val spheresJson = Json.encodeToString(_spheres.value)
                val encryptedSpheres = cryptoManager.encryptLocal(spheresJson.encodeToByteArray())
                spheresFile.writeBytes(encryptedSpheres)

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

    // --- Echo Operations ---
    fun getAllEchoes() = echoes

    /** Returns raw encrypted Echoes since a specific timestamp for sync. */
    fun getRawEchoesSince(timestamp: Long): List<ByteArray> = database.getRawMessagesSince(timestamp)

    /** Returns the latest Echo ID in the local database. */
    fun getLatestEchoId(): String? = database.getLatestMessageId()

    /** 
     * Incorporates a new Echo into the stream.
     * Uses LWW-CRDT logic for shared items to ensure deterministic state across the resonance.
     */
    fun upsertEcho(echo: Echo) {
        if (echo.content.isBlank()) return

        if (echo.type == Echo.TYPE_CONSENSUS_VOTE) {
            handleConsensusVote(echo)
            return
        }

        _echoes.update { current ->
            val existingIndex = current.indexOfFirst { it.messageId == echo.messageId }
            
            if (existingIndex != -1) {
                val existing = current[existingIndex]
                
                // LWW CRDT for shared item mutation
                val isMutableType = (echo.type == Echo.TYPE_NOTE_UPDATE) || (echo.type == Echo.TYPE_ASSIGNMENT_TASK)
                
                if (isMutableType) {
                    if ((echo.noteVersion > existing.noteVersion) || 
                        ((echo.noteVersion == existing.noteVersion) && (echo.timestamp > existing.timestamp))) {
                        saveEchoToDb(echo)
                        current.toMutableList().apply { set(existingIndex, echo) }
                    } else {
                        current
                    }
                } else {
                    current
                }
            } else {
                saveEchoToDb(echo)
                (current + echo).sortedBy { it.timestamp }
            }
        }
        
        // AUTO-PRUNE: Maintain history retention limits per context
        val targetGid = echo.groupId
        if (targetGid != null) {
            scope.launch {
                pruneHistory(targetGid)
            }
        }
    }

    /**
     * Social Logic: Processes a consensus vote to adjust the weight of a target Echo.
     */
    private fun handleConsensusVote(voteEcho: Echo) {
        val targetEchoId = voteEcho.parentMessageId ?: return
        val weight = try { voteEcho.content.toInt() } catch (_: Exception) { 0 }
        
        _echoes.update { current ->
            current.map { 
                if (it.messageId == targetEchoId) {
                    val newWeight = it.resonanceWeight + weight
                    database.updateWeight(targetEchoId, newWeight)
                    it.copy(resonanceWeight = newWeight)
                } else it
            }
        }
    }

    /**
     * Retrieves high-priority Echoes for the Sphere header.
     */
    fun getHighResonanceEchoes(groupId: String, limit: Int = 3): StateFlow<List<Echo>> {
        return echoes.map { list ->
            list.asSequence()
                .filter { (it.groupId == groupId) && (it.resonanceWeight > 0) }
                .sortedByDescending { it.resonanceWeight }
                .take(limit)
                .toList()
        }.stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    /** Evicts low-priority Echoes once a context exceeds retention limits. */
    private fun pruneHistory(groupId: String) {
        val sphere = getSphere(groupId) ?: return
        val currentEchoes = _echoes.value.filter { it.groupId == groupId }
        
        val maxLimit = if (sphere.scope == Sphere.SCOPE_PRIVATE) 2000 else historyRetentionLimit
        
        if (currentEchoes.size > maxLimit) {
            val toRemoveCount = currentEchoes.size - maxLimit
            
            val echoesToDelete = currentEchoes.asSequence()
                .filter { it.messageId !in sphere.pinnedMessageIds }
                .filter { it.type != Echo.TYPE_ASSIGNMENT_TASK }
                .filter { !it.isMeta }
                .sortedBy { it.timestamp }
                .take(toRemoveCount)
                .toList()

            echoesToDelete.forEach { deleteEcho(it.messageId) }
            Log.i("EchoLedger", "Pruned ${echoesToDelete.size} Echoes in $groupId.")
        }
    }

    fun updateEchoStatus(messageId: String, status: Int) {
        var updated: Echo? = null
        _echoes.update { list ->
            list.map {
                if (it.messageId == messageId) {
                    val e = it.copy(status = status)
                    updated = e
                    e
                } else it
            }
        }
        updated?.let { saveEchoToDb(it) }
    }

    fun clearAllEchoes() {
        _echoes.value = emptyList()
        if (echoesLogFile.exists()) echoesLogFile.delete()
    }

    fun deleteEcho(messageId: String) {
        val echo = _echoes.value.find { it.messageId == messageId }
        if (echo?.type == Echo.TYPE_IMAGE || echo?.type == Echo.TYPE_FILE) {
            echo.content.let { path ->
                try {
                    val file = File(path)
                    if (file.exists() && file.absolutePath.contains(context.filesDir.absolutePath)) {
                        file.delete()
                    }
                } catch (_: Exception) {}
            }
        }
        database.deleteMessage(messageId)
        _echoes.update { it.filter { e -> e.messageId != messageId } }
    }

    /**
     * Increments the anchoring status of a record.
     * High anchoring count indicates strong decentralized persistence.
     */
    fun incrementAnchoredCount(messageId: String) {
        var updated: Echo? = null
        _echoes.update { list ->
            list.map {
                if (it.messageId == messageId) {
                    val e = it.copy(anchoredCount = (it.anchoredCount + 1).coerceAtMost(10))
                    updated = e
                    e
                } else it
            }
        }
        updated?.let { saveEchoToDb(it) }
    }

    // --- Archive ---

    /** Protocols automatically move inactive Spheres into archive. */
    fun autoArchiveSpheres() {
        val now = System.currentTimeMillis()
        _spheres.update { current ->
            current.mapValues { (id, sphere) ->
                val isDefault = id == Sphere.ID_GLOBAL || id == Sphere.ID_SILENCE
                if (!isDefault && !sphere.isArchived && !sphere.isVaulted && (now - sphere.lastMessageTimestamp) > Sphere.ARCHIVE_THRESHOLD_MS) {
                    sphere.copy(isArchived = true)
                } else {
                    sphere
                }
            }
        }
        saveData()
    }

    fun vaultSphere(groupId: String, isVaulted: Boolean) {
        _spheres.update { current ->
            current[groupId]?.let { 
                current + (groupId to it.copy(isVaulted = isVaulted, vaultTimestamp = if (isVaulted) System.currentTimeMillis() else null))
            } ?: current
        }
        saveData()
    }

    fun seniorVaultSphere(groupId: String, isSeniorVault: Boolean) {
        _spheres.update { current ->
            current[groupId]?.let { 
                current + (groupId to it.copy(isSeniorVault = isSeniorVault))
            } ?: current
        }
        saveData()
    }

    fun restoreFromVault(groupId: String) {
        _spheres.update { current ->
            current[groupId]?.let { 
                current + (groupId to it.copy(isArchived = false, isVaulted = false, lastMessageTimestamp = System.currentTimeMillis()))
            } ?: current
        }
        saveData()
    }

    /** Prunes media older than 90 days. */
    fun pruneMedia(thresholdMs: Long) {
        val now = System.currentTimeMillis()
        val allSpheres = _spheres.value.values
        val allPinnedMessages = allSpheres.asSequence().flatMap { it.pinnedMessageIds }.toSet()
        val vaultedSphereIds = allSpheres.asSequence().filter { it.isVaulted }.map { it.id }.toSet()
        val seniorVaultIds = allSpheres.asSequence().filter { it.isSeniorVault }.map { it.id }.toSet()

        _echoes.value.forEach { echo ->
            val isFromVaultedSphere = echo.groupId in vaultedSphereIds
            val isFromSeniorVault = echo.groupId in seniorVaultIds
            val isPermanentMemory = echo.type == Echo.TYPE_MEMORY
            
            if ((now - echo.timestamp) > thresholdMs && echo.messageId !in allPinnedMessages && !isFromVaultedSphere && !isFromSeniorVault && !isPermanentMemory) {
                if (echo.type == Echo.TYPE_IMAGE || echo.type == Echo.TYPE_FILE) {
                    echo.content.let { path ->
                        try {
                            val file = File(path)
                            if (file.exists() && file.absolutePath.contains(context.filesDir.absolutePath)) {
                                file.delete()
                                Log.i("EchoLedger", "Pruned media: ${echo.messageId}")
                            }
                        } catch (_: Exception) {}
                    }
                }
            }
        }
    }

    // --- Sphere Operations ---
    fun insertSphere(sphere: Sphere) {
        _spheres.update { it + (sphere.id to sphere) }
        saveData()
    }

    /** Adds a Source to a specific Sphere. */
    fun joinSphere(groupId: String, userId: String) {
        _spheres.update { current ->
            current[groupId]?.let { sphere ->
                current + (groupId to sphere.copy(memberIds = sphere.memberIds + userId))
            } ?: current
        }
        saveData()
    }

    /**
     * Verifies if a Source has participation rights in a Sphere.
     */
    fun isMember(groupId: String, userId: String): Boolean {
        val sphere = _spheres.value[groupId] ?: return false
        if (sphere.scope == Sphere.SCOPE_PUBLIC) return true
        return sphere.isDefaultSphere || userId in sphere.allMemberIds
    }

    fun getSphere(id: String) = _spheres.value[id]

    /** Orchestrates member partitioning for scalability. */
    fun updateSphereMembers(groupId: String, memberIds: Set<String>) {
        _spheres.update { current ->
            current[groupId]?.let { sphere ->
                val cappedMembers = memberIds.asSequence().take(Sphere.MAX_MEMBERS_PER_SECTION).toSet()
                
                val updatedSphere = if (cappedMembers.size > sphere.partitionThreshold) {
                    val sectionId = "section_${sphere.memberSections.size}"
                    val newSections = sphere.memberSections.toMutableMap()
                    newSections[sectionId] = cappedMembers.take(sphere.partitionThreshold).toSet()
                    sphere.copy(memberIds = emptySet(), memberSections = newSections)
                } else {
                    sphere.copy(memberIds = cappedMembers)
                }
                current + (groupId to updatedSphere)
            } ?: current
        }
        saveData()
    }

    fun updateSphereScope(groupId: String, scope: Int) {
        _spheres.update { current ->
            current[groupId]?.let { 
                current + (groupId to it.copy(scope = scope))
            } ?: current
        }
        saveData()
    }

    fun updateSphereLastEcho(groupId: String, timestamp: Long) {
        _spheres.update { current ->
            current[groupId]?.let { 
                current + (groupId to it.copy(lastMessageTimestamp = timestamp))
            } ?: current
        }
        saveData()
    }

    fun addSphereSchedule(groupId: String, schedule: SphereEvent) {
        _spheres.update { current ->
            current[groupId]?.let { sphere ->
                current + (groupId to sphere.copy(schedules = sphere.schedules + schedule))
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
