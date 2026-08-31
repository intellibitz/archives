package cc.thevar.blukit.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface Route : NavKey {
    @Serializable
    data object Sensing : Route
    
    @Serializable
    data object Timeline : Route

    @Serializable
    data object LiveFeed : Route

    @Serializable
    data class SphereField(val roomId: String) : Route

    @Serializable
    data class EchoField(val messageId: String) : Route
}
