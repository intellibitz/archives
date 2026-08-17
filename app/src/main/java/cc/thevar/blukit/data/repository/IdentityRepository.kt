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
 * Repository interface for managing user identity and privacy settings.
 * Handles anonymous "vibe" personas and secure storage of device-specific configuration.
 */
interface IdentityRepository {
    val nicknameFlow: StateFlow<String?>
    val emojiAvatar: StateFlow<String>
    val stealthMode: StateFlow<Boolean>
    val lowPowerMode: StateFlow<Boolean>
    val blockedUsers: StateFlow<Set<String>>
    val vibedPeers: StateFlow<Set<String>>

    fun getDeviceId(): String
    fun saveNickname(name: String)
    fun getCurrentNickname(): String
    fun saveEmoji(emoji: String)
    fun toggleStealth(enabled: Boolean)
    fun toggleLowPowerMode(enabled: Boolean)
    fun toggleVibePeer(deviceId: String)
    fun blockUser(deviceId: String)
    fun logout()
}

/**
 * Implementation of IdentityRepository using Android KeyStore-backed
 * EncryptedSharedPreferences for maximum privacy of even anonymous identifiers.
 */
class IdentityRepositoryImpl(
    context: Context
) : IdentityRepository {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val backupPrefs = context.getSharedPreferences("blukit_identity_backup", Context.MODE_PRIVATE)

    private val securePrefs = try {
        createEncryptedPrefs(context)
    } catch (e: Exception) {
        // Keystore corruption - Recovery flow
        val backupId = backupPrefs.getString(KEY_DEVICE_ID, null)
        context.deleteSharedPreferences("blukit_identity_secure")
        val newPrefs = createEncryptedPrefs(context)
        // Restore essential identity markers if possible
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
        const val KEY_VIBED_PEERS = "vibed_peers"
    }

    private val _nickname = MutableStateFlow(securePrefs.getString(KEY_NICKNAME, null))
    override val nicknameFlow: StateFlow<String?> = _nickname.asStateFlow()

    private val _emojiAvatar = MutableStateFlow(getSanitizedEmoji())
    override val emojiAvatar: StateFlow<String> = _emojiAvatar.asStateFlow()

    private fun getSanitizedEmoji(): String {
        return securePrefs.getString(KEY_EMOJI, null) ?: "👤"
    }

    private val _stealthMode = MutableStateFlow(securePrefs.getBoolean(KEY_STEALTH, true))
    override val stealthMode: StateFlow<Boolean> = _stealthMode.asStateFlow()

    private val _lowPowerMode = MutableStateFlow(securePrefs.getBoolean(KEY_LOW_POWER, true))
    override val lowPowerMode: StateFlow<Boolean> = _lowPowerMode.asStateFlow()

    private val _blockedUsers = MutableStateFlow(
        securePrefs.getStringSet(KEY_BLOCKED_USERS, emptySet()) ?: emptySet()
    )
    override val blockedUsers: StateFlow<Set<String>> = _blockedUsers.asStateFlow()

    private val _vibedPeers = MutableStateFlow(
        securePrefs.getStringSet(KEY_VIBED_PEERS, emptySet()) ?: emptySet()
    )
    override val vibedPeers: StateFlow<Set<String>> = _vibedPeers.asStateFlow()

    override fun getDeviceId(): String {
        var id = securePrefs.getString(KEY_DEVICE_ID, null)
        if (id == null) {
            // Check backup
            id = backupPrefs.getString(KEY_DEVICE_ID, null) ?: UUID.randomUUID().toString()
            securePrefs.edit { putString(KEY_DEVICE_ID, id) }
            backupPrefs.edit { putString(KEY_DEVICE_ID, id) }
        }
        return id ?: ""
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
        securePrefs.edit { putStringSet(KEY_BLOCKED_USERS, current) }
        _blockedUsers.value = current
    }

    override fun toggleVibePeer(deviceId: String) {
        val current = _vibedPeers.value.toMutableSet()
        if (current.contains(deviceId)) current.remove(deviceId) else current.add(deviceId)
        securePrefs.edit { putStringSet(KEY_VIBED_PEERS, current) }
        _vibedPeers.value = current
    }

    override fun logout() {
        securePrefs.edit { clear() }
        backupPrefs.edit { clear() }
        _nickname.value = null
        _emojiAvatar.value = "👤"
        _stealthMode.value = false
        _lowPowerMode.value = false
        _blockedUsers.value = emptySet()
        _vibedPeers.value = emptySet()
    }
}
