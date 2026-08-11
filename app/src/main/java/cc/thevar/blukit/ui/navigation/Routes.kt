package cc.thevar.blukit.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface Route : NavKey {
    @Serializable
    data object Profile : Route
    
    @Serializable
    data object Discovery : Route
    
    @Serializable
    data object Chat : Route

    @Serializable
    data object Lobby : Route

    @Serializable
    data object Contacts : Route
}
