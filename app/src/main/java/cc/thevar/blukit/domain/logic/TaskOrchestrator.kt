/**
 * BLUKIT LOGIC: TASK ORCHESTRATOR
 *
 * Manages collaborative assignment tracking and offline task synchronization.
 * Orchestrates conflict-free task updates within private Chains.
 * 
 * Logic:
 * - Deterministic versioning: Uses LWW (Last-Write-Wins) for task status resolution.
 * - Mesh Propagation: Broadcasts assignment metadata across the peer network.
 */
package cc.thevar.blukit.domain.logic

import cc.thevar.blukit.data.local.PulseStore
import cc.thevar.blukit.domain.model.MessagePayload
import cc.thevar.blukit.network.p2p.P2PController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Orchestrates academic task assignments within the mesh.
 */
class TaskOrchestrator(
    private val pulseStore: PulseStore,
    private val p2pController: P2PController,
    private val scope: CoroutineScope
) {
    /**
     * Creates a new assignment task and broadcasts it to the resonant chain.
     */
    fun createAssignment(
        groupId: String,
        title: String,
        dueDate: Long? = null,
        assigneeId: String? = null
    ) {
        val task = MessagePayload(
            messageId = UUID.randomUUID().toString(),
            senderId = "", // Populated by mesh controller during dispatch
            senderName = "", 
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
            )
        }
    }

    /**
     * Updates an existing task's status with LWW resolution.
     * Increments the note version to ensure deterministic mesh synchronization.
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
            // Local persistence with CRDT resolution
            pulseStore.upsertMessage(updatedTask)
            
            // Broadcast the state shift to the chain
            p2pController.sendMessage(
                content = updatedTask.content,
                groupId = updatedTask.groupId,
                messageId = updatedTask.messageId,
                type = MessagePayload.TYPE_ASSIGNMENT_TASK
            )
        }
    }
}
