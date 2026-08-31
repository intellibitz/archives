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

/**
 * AIR INTELLIGENCE MODELS
 */
data class AirSynthesisHighlight(
    val title: String,
    val summary: String,
    val intensity: Float, // 0.0 to 1.0 for UI animation
    val activeTasks: List<String> = emptyList(),
    val topKeywords: List<String> = emptyList()
)

data class MinedDocumentHighlight(
    val fileName: String,
    val previewSnippet: String,
    val extractedEntities: List<String>,
    val timestamp: Long
)
