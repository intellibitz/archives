package cc.thevar.blukit.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cc.thevar.blukit.data.local.PulseStore
import cc.thevar.blukit.data.repository.IdentityRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: IdentityRepository,
    private val pulseStore: PulseStore,
) : ViewModel() {

    val nickname: Flow<String?> = repository.nicknameFlow
    val emojiAvatar: Flow<String> = repository.emojiAvatar
    val isStealthMode: Flow<Boolean> = repository.stealthMode
    val lowPowerMode: Flow<Boolean> = repository.lowPowerMode
    
    private val _deviceId = MutableStateFlow(repository.getDeviceId())
    val deviceId: StateFlow<String> = _deviceId.asStateFlow()

    fun saveNickname(name: String) {
        viewModelScope.launch {
            repository.saveNickname(name)
        }
    }

    /**
     * Updates the user's emoji avatar.
     * Note: This function is currently reserved for future profile editing flows.
     */
    fun saveEmoji(emoji: String) {
        viewModelScope.launch {
            repository.saveEmoji(emoji)
        }
    }

    fun toggleStealth(enabled: Boolean) {
        viewModelScope.launch {
            repository.toggleStealth(enabled)
        }
    }

    fun toggleLowPowerMode(enabled: Boolean) {
        viewModelScope.launch {
            repository.toggleLowPowerMode(enabled)
        }
    }

    fun togglePulsePeer(deviceId: String) {
        viewModelScope.launch {
            repository.togglePulsePeer(deviceId)
        }
    }

    fun clearPulsedPeers() {
        viewModelScope.launch {
            repository.clearPulsedPeers()
        }
    }

    fun blockUser(userId: String) {
        viewModelScope.launch {
            repository.blockUser(userId)
        }
    }

    fun unblockUser(userId: String) {
        viewModelScope.launch {
            repository.unblockUser(userId)
        }
    }

    fun deletePulse(messageId: String) {
        viewModelScope.launch {
            pulseStore.deleteMessage(messageId)
        }
    }

    fun clearChatHistory() {
        viewModelScope.launch {
            pulseStore.clearAllMessages()
        }
    }

    fun resetProfile() {
        viewModelScope.launch {
            repository.resetProfile()
        }
    }

    /**
     * Logs out the user and resets device identity.
     * Note: Currently unused, intended for tactical identity reset.
     */
    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _deviceId.value = repository.getDeviceId()
        }
    }
}
