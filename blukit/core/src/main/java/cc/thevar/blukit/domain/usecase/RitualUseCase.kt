package cc.thevar.blukit.domain.usecase

import cc.thevar.blukit.domain.model.GroupEvent
import cc.thevar.blukit.domain.model.Message
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Manages Group Rituals and coordinated events.
 */
class RitualUseCase(
    private val getGroupMembers: suspend (String) -> Set<String>,
    private val sendMessage: suspend (content: String, receiverId: String?, groupId: String?) -> Unit
) {
    suspend fun pushRitual(groupId: String, event: GroupEvent) {
        val content = Json.encodeToString(event)
        val members = getGroupMembers(groupId)
        members.forEach { memberId ->
            sendMessage(content, memberId, groupId)
        }
    }
}
