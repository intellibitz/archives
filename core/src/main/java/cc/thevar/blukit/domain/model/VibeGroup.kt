package cc.thevar.blukit.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class VibeGroup(
    val id: String,
    val name: String,
    val memberIds: Set<String>,
    val type: Int, // VIBE_SIDE or VIBE_TIE
    val lastVibeTimestamp: Long = System.currentTimeMillis(),
    val isPersistent: Boolean = false
) {
    companion object {
        const val TYPE_SIDE = 1
        const val TYPE_TIE = 2
    }
}
