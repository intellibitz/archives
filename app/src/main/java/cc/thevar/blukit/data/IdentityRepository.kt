package cc.thevar.blukit.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class IdentityRepository(private val context: Context) {

    private val nicknameKey = stringPreferencesKey("nickname")
    private val emojiKey = stringPreferencesKey("emoji")
    private val deviceIdKey = stringPreferencesKey("device_id")
    private val stealthModeKey = booleanPreferencesKey("stealth_mode")
    private val blockedUsersKey = stringSetPreferencesKey("blocked_users")

    val nickname: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[nicknameKey]
        }

    val emojiAvatar: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[emojiKey] ?: "👤"
        }

    val stealthMode: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[stealthModeKey] ?: false
        }

    val blockedUsers: Flow<Set<String>> = context.dataStore.data
        .map { preferences ->
            preferences[blockedUsersKey] ?: emptySet()
        }

    val deviceId: Flow<String> = context.dataStore.data
        .map { preferences ->
            val id = preferences[deviceIdKey]
            if (id == null) {
                val newId = UUID.randomUUID().toString()
                // Side effect in map is generally discouraged, but here it's for initialization
                // A better way would be an initialize() function
                newId
            } else {
                id
            }
        }

    suspend fun setNickname(name: String) {
        context.dataStore.edit { preferences ->
            preferences[nicknameKey] = name
        }
    }

    suspend fun setEmojiAvatar(emoji: String) {
        context.dataStore.edit { preferences ->
            preferences[emojiKey] = emoji
        }
    }

    suspend fun setStealthMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[stealthModeKey] = enabled
        }
    }

    suspend fun blockUser(userId: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[blockedUsersKey] ?: emptySet()
            preferences[blockedUsersKey] = current + userId
        }
    }

    private suspend fun setDeviceId(id: String) {
        context.dataStore.edit { preferences ->
            preferences[deviceIdKey] = id
        }
    }

    suspend fun getNickname(): String? {
        return nickname.first()
    }

    suspend fun getDeviceId(): String {
        val current = deviceId.first()
        // Ensure it's persisted if it was just generated
        context.dataStore.edit { preferences ->
            if (preferences[deviceIdKey] == null) {
                preferences[deviceIdKey] = current
            }
        }
        return current
    }

    suspend fun clearNickname() {
        context.dataStore.edit { preferences ->
            preferences.remove(nicknameKey)
        }
    }
}
