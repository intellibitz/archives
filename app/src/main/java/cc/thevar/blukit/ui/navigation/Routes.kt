package cc.thevar.blukit.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface Route : NavKey {
    @Serializable
    data object Event : Route
    
    @Serializable
    data object Resonance : Route

    @Serializable
    data object Timeline : Route

    @Serializable
    data object LiveFeed : Route

    @Serializable
    data class GroupField(val groupId: String) : Route

    @Serializable
    data class PulseField(val messageId: String) : Route
}
