package cc.thevar.blukit.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface Route : NavKey {
    @Serializable
    data object Watch : Route
    
    @Serializable
    data object Shout : Route
    
    @Serializable
    data object Chat : Route

    @Serializable
    data object Ties : Route

    @Serializable
    data object Mask : Route
}
