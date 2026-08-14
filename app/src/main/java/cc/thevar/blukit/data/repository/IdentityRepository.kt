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
    val blockedUsers: StateFlow<Set<String>>
    
    fun getDeviceId(): String
    fun saveNickname(name: String)
    fun getCurrentNickname(): String
    fun saveEmoji(emoji: String)
    fun toggleStealth(enabled: Boolean)
    fun blockUser(deviceId: String)
    fun logout()
}

/**
 * Implementation of IdentityRepository using Android KeyStore-backed 
 * EncryptedSharedPreferences to ensure sensitive user data remains protected at rest.
 */
class IdentityRepositoryImpl(context: Context) : IdentityRepository {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val securePrefs = try {
        createEncryptedPrefs(context)
    } catch (e: Exception) {
        // Keystore corruption or signature mismatch - Purge and Recreate
        context.deleteSharedPreferences("blukit_identity_secure")
        createEncryptedPrefs(context)
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
        const val KEY_DEVICE_ID = "device_id"
        const val KEY_BLOCKED_USERS = "blocked_users"
    }

    private val _nickname = MutableStateFlow(securePrefs.getString(KEY_NICKNAME, null))
    override val nicknameFlow: StateFlow<String?> = _nickname.asStateFlow()

    private val _emojiAvatar = MutableStateFlow(getSanitizedEmoji())
    override val emojiAvatar: StateFlow<String> = _emojiAvatar.asStateFlow()

    private fun getSanitizedEmoji(): String {
        return "👤" // Default Person (Your Vibe)
    }

    private val _stealthMode = MutableStateFlow(securePrefs.getBoolean(KEY_STEALTH, true))
    override val stealthMode: StateFlow<Boolean> = _stealthMode.asStateFlow()

    private val _blockedUsers = MutableStateFlow(
        securePrefs.getStringSet(KEY_BLOCKED_USERS, emptySet()) ?: emptySet()
    )
    override val blockedUsers: StateFlow<Set<String>> = _blockedUsers.asStateFlow()

    override fun getDeviceId(): String {
        var id = securePrefs.getString(KEY_DEVICE_ID, null)
        if (id == null) {
            id = UUID.randomUUID().toString()
            securePrefs.edit { putString(KEY_DEVICE_ID, id) }
        }
        return id
    }

    override fun saveNickname(name: String) {
        securePrefs.edit { putString(KEY_NICKNAME, name) }
        _nickname.value = name
    }

    override fun getCurrentNickname(): String = securePrefs.getString(KEY_NICKNAME, null) ?: "vibe"

    override fun saveEmoji(emoji: String) {
        // No-op: Emojis thrashed, everyone is a person
    }

    override fun toggleStealth(enabled: Boolean) {
        securePrefs.edit { putBoolean(KEY_STEALTH, enabled) }
        _stealthMode.value = enabled
    }

    override fun blockUser(deviceId: String) {
        val current = _blockedUsers.value.toMutableSet()
        current.add(deviceId)
        securePrefs.edit { putStringSet(KEY_BLOCKED_USERS, current) }
        _blockedUsers.value = current
    }

    override fun logout() {
        securePrefs.edit { clear() }
        _nickname.value = null
        _emojiAvatar.value = "👤"
        _stealthMode.value = false
        _blockedUsers.value = emptySet()
    }
}
