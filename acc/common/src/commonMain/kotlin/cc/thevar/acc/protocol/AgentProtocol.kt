package cc.thevar.acc.protocol

import kotlinx.serialization.Serializable

/**
 * Represents the current lifecycle state of an agent.
 */
@Serializable
enum class AgentStatus {
    IDLE, 
    THINKING, 
    EXECUTING, 
    ERROR, 
    DONE
}

/**
 * Captured internal reasoning or "hidden" thoughts of the agent.
 */
@Serializable
data class AgentThought(
    val summary: String,
    val detail: String? = null,
    val timestamp: Long = 0L
)

/**
 * A specific action taken by the agent (e.g., calling a tool, editing a file).
 */
@Serializable
data class AgentAction(
    val tool: String,
    val input: String,
    val result: String? = null
)

/**
 * The primary communication packet between the Agent Brain and the Acc UI.
 */
@Serializable
data class AgentMessage(
    val agentName: String,
    val status: AgentStatus,
    val content: String,
    val thoughts: List<AgentThought> = emptyList(),
    val actions: List<AgentAction> = emptyList(),
    val metadata: Map<String, String> = emptyMap()
)
