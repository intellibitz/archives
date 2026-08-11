package cc.thevar.blukit.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cc.thevar.blukit.data.repository.IdentityRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: IdentityRepository,
    private val messageDao: cc.thevar.blukit.data.local.dao.MessageDao
) : ViewModel() {

    val nickname = repository.nickname
    val emojiAvatar = repository.emojiAvatar
    val isStealthMode = repository.stealthMode
    val deviceId = repository.deviceId
    val isUserRegistered: StateFlow<Boolean> = repository.nickname
        .map { it != null }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    fun saveNickname(name: String) {
        viewModelScope.launch {
            repository.setNickname(name)
        }
    }

    fun saveEmoji(emoji: String) {
        viewModelScope.launch {
            repository.setEmojiAvatar(emoji)
        }
    }

    fun toggleStealth(enabled: Boolean) {
        viewModelScope.launch {
            repository.setStealthMode(enabled)
        }
    }

    fun blockUser(userId: String) {
        viewModelScope.launch {
            repository.blockUser(userId)
        }
    }

    fun clearChatHistory() {
        viewModelScope.launch {
            messageDao.clearAllMessages()
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.clearNickname()
        }
    }
}
