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
 * Supreme Senior Architect Implementation:
 * Secure Identity Repository using Android KeyStore and EncryptedSharedPreferences.
 */
class IdentityRepository(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val securePrefs = EncryptedSharedPreferences.create(
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
    val nickname: StateFlow<String?> = _nickname.asStateFlow()

    private val _emojiAvatar = MutableStateFlow(getSanitizedEmoji())
    val emojiAvatar: StateFlow<String> = _emojiAvatar.asStateFlow()

    private fun getSanitizedEmoji(): String {
        val stored = securePrefs.getString(KEY_EMOJI, "🎭") ?: "🎭"
        return if (stored == "🌬️" || stored == "👤" || stored == "💓") {
            "🎭" // Migrate old defaults to the Mask
        } else {
            stored
        }
    }

    private val _stealthMode = MutableStateFlow(securePrefs.getBoolean(KEY_STEALTH, true))
    val stealthMode: StateFlow<Boolean> = _stealthMode.asStateFlow()

    private val _blockedUsers = MutableStateFlow(
        securePrefs.getStringSet(KEY_BLOCKED_USERS, emptySet()) ?: emptySet()
    )
    val blockedUsers: StateFlow<Set<String>> = _blockedUsers.asStateFlow()

    fun getDeviceId(): String {
        var id = securePrefs.getString(KEY_DEVICE_ID, null)
        if (id == null) {
            id = UUID.randomUUID().toString()
            securePrefs.edit { putString(KEY_DEVICE_ID, id) }
        }
        return id
    }

    fun saveNickname(name: String) {
        securePrefs.edit { putString(KEY_NICKNAME, name) }
        _nickname.value = name
    }

    fun getNickname(): String = securePrefs.getString(KEY_NICKNAME, null) ?: "vibe"

    fun saveEmoji(emoji: String) {
        securePrefs.edit { putString(KEY_EMOJI, emoji) }
        _emojiAvatar.value = emoji
    }

    fun toggleStealth(enabled: Boolean) {
        securePrefs.edit { putBoolean(KEY_STEALTH, enabled) }
        _stealthMode.value = enabled
    }

    fun blockUser(deviceId: String) {
        val current = _blockedUsers.value.toMutableSet()
        current.add(deviceId)
        securePrefs.edit { putStringSet(KEY_BLOCKED_USERS, current) }
        _blockedUsers.value = current
    }

    fun logout() {
        securePrefs.edit { clear() }
        _nickname.value = null
        _emojiAvatar.value = "🎭"
        _stealthMode.value = false
        _blockedUsers.value = emptySet()
    }
}
