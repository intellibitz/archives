package cc.thevar.blukit.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cc.thevar.blukit.data.IdentityRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.rememberNavBackStack

class MainViewModel(
    private val repository: IdentityRepository
) : ViewModel() {

    val nickname = repository.nickname
    val emojiAvatar = repository.emojiAvatar
    val isStealthMode = repository.stealthMode
    val deviceId = repository.deviceId

    val isUserRegistered: StateFlow<Boolean> = repository.nickname
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

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
}
