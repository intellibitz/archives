package cc.thevar.blukit.data.repository

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import java.util.UUID

/**
 * Supreme Senior Architect Implementation:
 * Identity Repository with 100% Hardware-Backed Encrypted Storage for ALL profile data.
 * Migrated from standard DataStore to EncryptedSharedPreferences for military-grade persistence.
 */
class IdentityRepository(private val context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val securePrefs = EncryptedSharedPreferences.create(
        context,
        "blukit_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    /**
     * Reactive stream for Nickname updates.
     */
    val nickname: Flow<String?> = observeKey(KEY_NICKNAME)

    /**
     * Reactive stream for Emoji Avatar updates.
     */
    val emojiAvatar: Flow<String> = observeKey(KEY_EMOJI) { it ?: "👤" }

    /**
     * Reactive stream for Stealth Mode updates.
     */
    val stealthMode: Flow<Boolean> = observeKey(KEY_STEALTH) { it?.toBoolean() ?: false }

    /**
     * Reactive stream for Blocked Users set.
     */
    val blockedUsers: Flow<Set<String>> = observeKey(KEY_BLOCKED) { it?.split(",")?.toSet() ?: emptySet() }

    /**
     * Reactive stream for Device ID. Automatically generates if missing.
     */
    val deviceId: Flow<String> = observeKey(KEY_DEVICE_ID) {
        it ?: run {
            val newId = UUID.randomUUID().toString()
            securePrefs.edit().putString(KEY_DEVICE_ID, newId).apply()
            newId
        }
    }

    suspend fun setNickname(name: String) {
        securePrefs.edit().putString(KEY_NICKNAME, name).apply()
    }

    suspend fun setEmojiAvatar(emoji: String) {
        securePrefs.edit().putString(KEY_EMOJI, emoji).apply()
    }

    suspend fun setStealthMode(enabled: Boolean) {
        securePrefs.edit().putString(KEY_STEALTH, enabled.toString()).apply()
    }

    suspend fun blockUser(userId: String) {
        val current = blockedUsers.first()
        val updated = (current + userId).joinToString(",")
        securePrefs.edit().putString(KEY_BLOCKED, updated).apply()
    }

    suspend fun getNickname(): String? = nickname.first()

    suspend fun getDeviceId(): String = deviceId.first()

    suspend fun clearNickname() {
        securePrefs.edit().remove(KEY_NICKNAME).apply()
    }

    private fun <T> observeKey(key: String, transform: (String?) -> T = { it as T }): Flow<T> = callbackFlow {
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { prefs, changedKey ->
            if (key == changedKey) {
                trySend(transform(prefs.getString(key, null)))
            }
        }
        securePrefs.registerOnSharedPreferenceChangeListener(listener)
        trySend(transform(securePrefs.getString(key, null)))
        awaitClose { securePrefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }.onStart { emit(transform(securePrefs.getString(key, null))) }.flowOn(Dispatchers.IO)

    companion object {
        private const val KEY_NICKNAME = "nickname"
        private const val KEY_EMOJI = "emoji"
        private const val KEY_STEALTH = "stealth_mode"
        private const val KEY_DEVICE_ID = "device_id"
        private const val KEY_BLOCKED = "blocked_users"
    }
}
