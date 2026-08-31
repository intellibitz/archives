/**
 * SHARED UI STATE & MODELS
 *
 * Centralized data structures and CompositionLocals used across the 
 * UI design system.
 */
package cc.thevar.blukit.ui.screens

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

data class PersonaConnectionPoints(
    val uph: Offset? = null,
    val field: Offset? = null,
    val ticker: Offset? = null,
    val pulse: Offset? = null
)

val LocalPersonaCoordinates: ProvidableCompositionLocal<SnapshotStateMap<String, PersonaConnectionPoints>> =
    compositionLocalOf { error("No PersonaCoordinates provided") }

val LocalActiveEchoId: ProvidableCompositionLocal<MutableState<String?>> =
    compositionLocalOf { error("No ActiveEchoId provided") }

val LocalUserEmoji: ProvidableCompositionLocal<String> =
    androidx.compose.runtime.staticCompositionLocalOf { "👤" }

data class BubbleData(
    val senderId: String,
    val content: String,
    val timestamp: Long,
    val messageId: String,
    val isPrivate: Boolean = false
)

data class RelayEvent(
    val id: String,
    val start: Offset,
    val end: Offset,
    val startTime: Long,
    val color: Color = Color.Transparent
)

/**
 * Animated rings signaling network activity.
 */
data class EchoRipple(
    val id: String,
    val center: Offset,
    val startTime: Long,
    val color: Color
)
