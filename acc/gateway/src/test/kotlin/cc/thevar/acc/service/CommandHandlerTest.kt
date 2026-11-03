package cc.thevar.acc.service

import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CommandHandlerTest {
    private val projectRoot = File(".")
    private val provisioningService = mockk<ProvisioningService>(relaxed = true)
    private val handler = CommandHandler(projectRoot, provisioningService)

    @Test
    fun `test internal sync command`() = runBlocking {
        val results = handler.handleCommand("sync").toList()
        
        verify { provisioningService.provisionAll() }
        assertTrue(results.any { it.text.contains("Fleet synchronization started") })
        assertEquals("SUCCESS", results.last().type)
    }

    @Test
    fun `test disallowed command`() = runBlocking {
        val results = handler.handleCommand("rm -rf /").toList()
        
        assertTrue(results.any { it.type == "ERROR" && it.text.contains("not allowed") })
    }

    @Test
    fun `test internal prune command`() = runBlocking {
        val results = handler.handleCommand("prune").toList()
        
        verify { provisioningService.pruneFleet() }
        assertEquals("SUCCESS", results.last().type)
    }
}
