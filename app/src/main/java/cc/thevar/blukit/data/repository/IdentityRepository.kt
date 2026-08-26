/**
 * BLUKIT DATA: IDENTITY REPOSITORY
 *
 * Manages the user's Event Persona and mesh privacy settings.
 * Orchestrates hardware-encrypted storage for anonymous identifiers and tactical toggles.
 * 
 * Features:
 * - Anonymous Personas: Map deterministic device IDs to ephemeral nicknames and emojis.
 * - Hardware Recovery: Self-healing EncryptedSharedPreferences to handle KeyStore corruption.
 * - Tactical Toggles: Global control for Stealth Mode and Low Power mesh operations.
 */
package cc.thevar.blukit.data.repository

import android.content.Context
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * Repository interface for user identity and mesh configuration.
 */
interface IdentityRepository {
    /** The user's current tactical nickname. */
    val nicknameFlow: StateFlow<String?>
    /** The emoji projecting the user's identity on the Discovery Radar. */
    val emojiAvatar: StateFlow<String>
    /** Stealth Mode: DISTINGUISHES private chains in Stealth Rose. */
    val stealthMode: StateFlow<Boolean>
    /** Low Power Mode: Throttles radio frequency to preserve hardware energy. */
    val lowPowerMode: StateFlow<Boolean>
    /** Set of blocked peer hardware IDs. */
    val blockedUsers: StateFlow<Set<String>>
    /** Set of identifiers for peers with active secure ties. */
    val pulsedPeers: StateFlow<Set<String>>

    /** Retrieves or generates a permanent anonymous hardware anchor. */
    fun getDeviceId(): String
    fun saveNickname(name: String)
    fun getCurrentNickname(): String
    fun saveEmoji(emoji: String)
    fun toggleStealth(enabled: Boolean)
    fun toggleLowPowerMode(enabled: Boolean)
    /** Toggles a peer's status in the user's secure orbit. */
    fun togglePulsePeer(deviceId: String)
    fun clearPulsedPeers()
    fun blockUser(deviceId: String)
    fun unblockUser(deviceId: String)
    /** Clears nickname/emoji while preserving the device anchor. */
    fun resetProfile()
    /** Full cryptographic reset of all identity markers. */
    fun logout()
}

/**
 * Implementation using Android KeyStore-backed EncryptedSharedPreferences.
 */
@Suppress("DEPRECATION")
class IdentityRepositoryImpl(
    context: Context,
) : IdentityRepository {

    private val masterKey = MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    /** Plaintext backup for critical non-PII markers to handle hardware encryption failures. */
    private val backupPrefs = context.getSharedPreferences("blukit_identity_backup", Context.MODE_PRIVATE)

        private val securePrefs = try {
        createEncryptedPrefs(context)
    } catch (_: Exception) {
        // RECOVERY: If KeyStore is corrupted, purge secure storage and restore from backup.
        val backupId = backupPrefs.getString(KEY_DEVICE_ID, null)
        context.deleteSharedPreferences("blukit_identity_secure")
        val newPrefs = createEncryptedPrefs(context)
        if (backupId != null) {
            newPrefs.edit { putString(KEY_DEVICE_ID, backupId) }
        }
        newPrefs
    }

    private fun createEncryptedPrefs(context: Context) = EncryptedSharedPreferences.create(
        context,
        "blukit_identity_secure",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    private companion object {
        const val KEY_NICKNAME = "nickname"
        const val KEY_EMOJI = "emoji_avatar"
        const val KEY_STEALTH = "stealth_mode"
        const val KEY_LOW_POWER = "low_power_mode"
        const val KEY_DEVICE_ID = "device_id"
        const val KEY_BLOCKED_USERS = "blocked_users"
        const val KEY_PULSED_PEERS = "pulsed_peers"
    }

    private val _nickname = MutableStateFlow(securePrefs.getString(KEY_NICKNAME, null))
    override val nicknameFlow: StateFlow<String?> = _nickname.asStateFlow()

    private val _emojiAvatar = MutableStateFlow(getSanitizedEmoji())
    override val emojiAvatar: StateFlow<String> = _emojiAvatar.asStateFlow()

    private fun getSanitizedEmoji(): String = securePrefs.getString(KEY_EMOJI, null) ?: "👤"

    private val _stealthMode = MutableStateFlow(securePrefs.getBoolean(KEY_STEALTH, true))
    override val stealthMode: StateFlow<Boolean> = _stealthMode.asStateFlow()

    private val _lowPowerMode = MutableStateFlow(securePrefs.getBoolean(KEY_LOW_POWER, true))
    override val lowPowerMode: StateFlow<Boolean> = _lowPowerMode.asStateFlow()

    private val _blockedUsers = MutableStateFlow(
        securePrefs.getStringSet(KEY_BLOCKED_USERS, emptySet()) ?: emptySet()
    )
    override val blockedUsers: StateFlow<Set<String>> = _blockedUsers.asStateFlow()

    private val _pulsedPeers = MutableStateFlow(
        securePrefs.getStringSet(KEY_PULSED_PEERS, emptySet()) ?: emptySet()
    )
    override val pulsedPeers: StateFlow<Set<String>> = _pulsedPeers.asStateFlow()

    override fun getDeviceId(): String {
        val existingId = securePrefs.getString(KEY_DEVICE_ID, null)
        existingId?.let { return it }

        val id = backupPrefs.getString(KEY_DEVICE_ID, null) ?: UUID.randomUUID().toString()
        securePrefs.edit { putString(KEY_DEVICE_ID, id) }
        backupPrefs.edit { putString(KEY_DEVICE_ID, id) }
        return id
    }

    override fun saveNickname(name: String) {
        securePrefs.edit { putString(KEY_NICKNAME, name) }
        _nickname.value = name
    }

    override fun getCurrentNickname(): String = securePrefs.getString(KEY_NICKNAME, null) ?: "?"

    override fun saveEmoji(emoji: String) {
        securePrefs.edit { putString(KEY_EMOJI, emoji) }
        _emojiAvatar.value = emoji
    }

    override fun toggleStealth(enabled: Boolean) {
        securePrefs.edit { putBoolean(KEY_STEALTH, enabled) }
        _stealthMode.value = enabled
    }

    override fun toggleLowPowerMode(enabled: Boolean) {
        securePrefs.edit { putBoolean(KEY_LOW_POWER, enabled) }
        _lowPowerMode.value = enabled
    }

    override fun blockUser(deviceId: String) {
        val current = _blockedUsers.value.toMutableSet()
        current.add(deviceId)
        securePrefs.edit { putStringSet(KEY_BLOCKED_USERS, current.asSequence().filterNotNull().toSet()) }
        _blockedUsers.value = current
    }

    override fun unblockUser(deviceId: String) {
        val current = _blockedUsers.value.toMutableSet()
        current.remove(deviceId)
        securePrefs.edit { putStringSet(KEY_BLOCKED_USERS, current.filterNotNull().toSet()) }
        _blockedUsers.value = current
    }

    override fun resetProfile() {
        securePrefs.edit {
            remove(KEY_NICKNAME)
            remove(KEY_EMOJI)
        }
        _nickname.value = null
        _emojiAvatar.value = "👤"
    }

    override fun togglePulsePeer(deviceId: String) {
        val current = _pulsedPeers.value.toMutableSet()
        if (current.contains(deviceId)) current.remove(deviceId) else current.add(deviceId)
        securePrefs.edit { putStringSet(KEY_PULSED_PEERS, current) }
        _pulsedPeers.value = current
    }

    override fun clearPulsedPeers() {
        securePrefs.edit { putStringSet(KEY_PULSED_PEERS, emptySet()) }
        _pulsedPeers.value = emptySet()
    }

    override fun logout() {
        securePrefs.edit { clear() }
        backupPrefs.edit { clear() }
        _nickname.value = null
        _emojiAvatar.value = "👤"
        _stealthMode.value = true
        _lowPowerMode.value = true
        _blockedUsers.value = emptySet()
        _pulsedPeers.value = emptySet()
    }
}
