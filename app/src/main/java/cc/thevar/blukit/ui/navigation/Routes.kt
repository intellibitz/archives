package cc.thevar.blukit.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface Route : NavKey {
    @Serializable
    data object Blukit : Route
    
    @Serializable
    data object Energy : Route
    
    @Serializable
    data object Chat : Route

    @Serializable
    data object Vibes : Route

    @Serializable
    data object SideVibes : Route

    @Serializable
    data object Focus : Route

    @Serializable
    data class VibeDetail(val groupId: String) : Route
}
