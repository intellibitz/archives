package cc.thevar.blukit.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cc.thevar.blukit.data.local.VibeStore
import cc.thevar.blukit.data.repository.IdentityRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: IdentityRepository,
    private val vibeStore: VibeStore,
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

    fun toggleVibePeer(deviceId: String) {
        viewModelScope.launch {
            repository.toggleVibePeer(deviceId)
        }
    }

    fun clearVibedPeers() {
        viewModelScope.launch {
            repository.clearVibedPeers()
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

    fun deleteVibe(messageId: String) {
        viewModelScope.launch {
            vibeStore.deleteMessage(messageId)
        }
    }

    fun clearChatHistory() {
        viewModelScope.launch {
            vibeStore.clearAllMessages()
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _deviceId.value = repository.getDeviceId()
        }
    }
}
