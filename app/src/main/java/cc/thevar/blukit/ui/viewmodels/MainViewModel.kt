/**
 * BLUKIT VIEWMODEL: MAIN ORCHESTRATOR
 *
 * Manages high-level app state, user identity, and social privacy settings.
 * Bridges the UI layer with the Identity Repository and Message Store.
 */
package cc.thevar.blukit.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cc.thevar.blukit.data.local.MessageStore
import cc.thevar.blukit.data.repository.IdentityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Orchestrates global app configuration and user persona state.
 */
class MainViewModel(
    private val repository: IdentityRepository,
    private val messageStore: MessageStore,
) : ViewModel() {

    /** Social nickname currently projecting to the mesh. */
    val nickname: Flow<String?> = repository.nicknameFlow
    /** The emoji avatar projecting the user's visual identity. */
    val emojiAvatar: Flow<String> = repository.emojiAvatar
    /** Stealth Mode: Distinguishes private interactions in Stealth Rose. */
    val isStealthMode: Flow<Boolean> = repository.stealthMode
    /** Low Power Mode: preserves hardware energy by throttling mesh activity. */
    val lowPowerMode: Flow<Boolean> = repository.lowPowerMode
    
    private val _deviceId = MutableStateFlow(repository.getDeviceId())
    /** The deterministic hardware anchor for this device. */
    val deviceId: StateFlow<String> = _deviceId.asStateFlow()

    fun saveNickname(name: String) {
        viewModelScope.launch { repository.saveNickname(name) }
    }

    /** Updates the user's visual persona. */
    fun saveEmoji(emoji: String) {
        viewModelScope.launch { repository.saveEmoji(emoji) }
    }

    fun toggleStealth(enabled: Boolean) {
        viewModelScope.launch { repository.toggleStealth(enabled) }
    }

    fun toggleLowPowerMode(enabled: Boolean) {
        viewModelScope.launch { repository.toggleLowPowerMode(enabled) }
    }

    fun togglePulsePeer(deviceId: String) {
        viewModelScope.launch { repository.togglePulsePeer(deviceId) }
    }

    fun clearPulsedPeers() {
        viewModelScope.launch { repository.clearPulsedPeers() }
    }

    fun blockUser(userId: String) {
        viewModelScope.launch { repository.blockUser(userId) }
    }

    fun unblockUser(userId: String) {
        viewModelScope.launch { repository.unblockUser(userId) }
    }

    /** Deletes an atomic message locally. */
    fun deletePulse(messageId: String) {
        viewModelScope.launch { messageStore.deleteMessage(messageId) }
    }

    /** Purges the entire local history. */
    fun clearChatHistory() {
        viewModelScope.launch { messageStore.clearAllMessages() }
    }

    /** Identity reset: Clears nickname and emoji. */
    fun resetProfile() {
        viewModelScope.launch { repository.resetProfile() }
    }

    /** Full cryptographic reset of all identity markers and anchors. */
    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _deviceId.value = repository.getDeviceId()
        }
    }
}
