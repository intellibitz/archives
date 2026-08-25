package cc.thevar.blukit.domain.logic

import cc.thevar.blukit.domain.model.MessagePayload
import cc.thevar.blukit.data.local.PulseStore
import cc.thevar.blukit.network.p2p.P2PController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Task Orchestrator: Manages collaborative assignment tracking.
 */
class TaskOrchestrator(
    private val pulseStore: PulseStore,
    private val p2pController: P2PController,
    private val scope: CoroutineScope
) {
    /**
     * Creates a new assignment task and broadcasts it to the chain.
     */
    fun createAssignment(
        groupId: String,
        title: String,
        dueDate: Long? = null,
        assigneeId: String? = null
    ) {
        val task = MessagePayload(
            messageId = UUID.randomUUID().toString(),
            senderId = "", // Filled by controller
            senderName = "", // Filled by controller
            groupId = groupId,
            content = title,
            timestamp = System.currentTimeMillis(),
            type = MessagePayload.TYPE_ASSIGNMENT_TASK,
            dueDate = dueDate,
            assigneeId = assigneeId,
            taskStatus = MessagePayload.TASK_PENDING,
            noteVersion = 1
        )
        
        scope.launch {
            p2pController.sendMessage(
                content = task.content,
                groupId = groupId,
                type = MessagePayload.TYPE_ASSIGNMENT_TASK
                // Metadata handled by controller if we extend sendMessage
            )
        }
    }

    /**
     * Updates an existing task's status with LWW resolution.
     */
    fun updateTaskStatus(
        originalTask: MessagePayload,
        newStatus: Int
    ) {
        val updatedTask = originalTask.copy(
            taskStatus = newStatus,
            noteVersion = originalTask.noteVersion + 1,
            timestamp = System.currentTimeMillis()
        )
        
        scope.launch {
            pulseStore.upsertMessage(updatedTask)
            // Broadcast update to the chain
            p2pController.sendMessage(
                content = updatedTask.content,
                groupId = updatedTask.groupId,
                messageId = updatedTask.messageId,
                type = MessagePayload.TYPE_ASSIGNMENT_TASK
            )
        }
    }
}
