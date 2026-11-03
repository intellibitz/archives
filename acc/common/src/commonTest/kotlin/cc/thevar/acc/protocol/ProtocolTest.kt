package cc.thevar.acc.protocol

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProtocolTest {
    @Test
    fun testSystemStateSerialization() {
        val stats = SystemStats(
            cpuUtilization = 10.5f,
            ramUsed = 8.0f,
            ramTotal = 16.0f,
            gpu = GpuStats(0f, 0f, 0f, 0f, 0f, false),
            diskUsage = "10G"
        )
        val state = SystemState(
            stats = stats,
            workers = listOf(WorkerState("test", WorkerStatus.RUNNING)),
            fleet = emptyList(),
            provisioning = emptyList(),
            partialDownloads = emptyList(),
            proxyOnline = true,
            statusMsg = "Testing"
        )
        
        val json = Json.encodeToString(state)
        assertTrue(json.contains("\"cpuUtilization\":10.5"))
        assertTrue(json.contains("\"statusMsg\":\"Testing\""))
        
        val decoded = Json.decodeFromString<SystemState>(json)
        assertEquals(state, decoded)
    }

    @Test
    fun testAgentMessageSerialization() {
        val message = AgentMessage(
            agentName = "Acc",
            status = AgentStatus.THINKING,
            content = "Testing...",
            thoughts = listOf(AgentThought("Summary", "Detail"))
        )
        
        val json = Json.encodeToString(message)
        assertTrue(json.contains("\"agentName\":\"Acc\""))
        
        val decoded = Json.decodeFromString<AgentMessage>(json)
        assertEquals(message, decoded)
    }
}
