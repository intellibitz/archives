package cc.thevar.blukit.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
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
            preferences[deviceIdKey] ?: run {
                val newId = UUID.randomUUID().toString()
                runBlocking {
                    setDeviceId(newId)
                }
                newId
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

    fun getNickname(): String? = runBlocking {
        context.dataStore.data.map { it[nicknameKey] }.first()
    }

    fun getDeviceId(): String = runBlocking {
        deviceId.first()
    }

    suspend fun clearNickname() {
        context.dataStore.edit { preferences ->
            preferences.remove(nicknameKey)
        }
    }
}
