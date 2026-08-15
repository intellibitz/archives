package cc.thevar.blukit.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface Route : NavKey {
    @Serializable
    data object Crowd : Route
    
    @Serializable
    data object Energy : Route
    
    @Serializable
    data object Chat : Route

    @Serializable
    data object Vibes : Route

    @Serializable
    data object Blukit : Route
}
