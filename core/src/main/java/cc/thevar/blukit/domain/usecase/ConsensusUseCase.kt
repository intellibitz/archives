package cc.thevar.blukit.domain.usecase

import cc.thevar.blukit.domain.model.Message
import java.util.UUID

/**
 * Encapsulates the social swarm consensus logic.
 */
class ConsensusUseCase(
    private val upsertMessage: (Message) -> Unit,
    private val deviceIdProvider: () -> String,
    private val nicknameProvider: () -> String
) {
    fun castVote(messageId: String, groupId: String, weight: Int) {
        val voteMessage = Message(
            messageId = UUID.randomUUID().toString(),
            senderId = deviceIdProvider(),
            senderName = nicknameProvider(),
            parentMessageId = messageId,
            groupId = groupId,
            content = weight.toString(),
            timestamp = System.currentTimeMillis(),
            type = Message.TYPE_CONSENSUS_VOTE,
        )
        upsertMessage(voteMessage)
    }
}
