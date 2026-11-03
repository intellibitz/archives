package cc.thevar.acc.ui

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object AuthStore {
    private val _credentials = MutableStateFlow<Pair<String, String>?>(null)
    val credentials = _credentials.asStateFlow()

    fun setCredentials(user: String, pass: String) {
        _credentials.value = user to pass
    }

    fun clear() {
        _credentials.value = null
    }
}
