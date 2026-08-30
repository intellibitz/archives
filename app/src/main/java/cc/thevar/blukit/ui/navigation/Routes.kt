package cc.thevar.blukit.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface Route : NavKey {
    @Serializable
    data object Discovery : Route
    
    @Serializable
    data object Timeline : Route

    @Serializable
    data object LiveFeed : Route

    @Serializable
    data class RoomField(val roomId: String) : Route

    @Serializable
    data class MessageField(val messageId: String) : Route
}
