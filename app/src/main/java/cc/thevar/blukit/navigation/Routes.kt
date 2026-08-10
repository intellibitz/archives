package cc.thevar.blukit.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Route : NavKey {
    @Serializable
    data object Profile : Route
    @Serializable
    data object Discovery : Route
    
    @Serializable
    data object Chat : Route
}
