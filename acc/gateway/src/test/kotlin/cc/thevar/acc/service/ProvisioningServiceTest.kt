package cc.thevar.acc.service

import cc.thevar.acc.protocol.ModelManifest
import cc.thevar.acc.protocol.ProvisioningStage
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.mockk.mockk
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds

class ProvisioningServiceTest {

    private val fleetManager = mockk<FleetManager>()
    private val tempDir = File("build/tmp/test").apply { mkdirs() }

    @Test
    fun `test provisioning completed when already installed`() = runBlocking {
        val mockEngine = MockEngine { request ->
            when (request.url.encodedPath) {
                "/api/models/test/repo" -> {
                    respond(
                        content = """{"sha": "test-sha"}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                "/api/tags" -> {
                    respond(
                        content = """{"models": [{"name": "test-model"}]}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                "/api/create" -> {
                    respond(
                        content = """{"status": "success"}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                else -> respondError(HttpStatusCode.NotFound)
            }
        }

        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        val service = ProvisioningService(tempDir, fleetManager, client)
        
        // Write the local sha to avoid download
        val regDir = File(tempDir, "registry/test-model").apply { mkdirs() }
        File(regDir, "last_sync_sha").writeText("test-sha")

        val model = ModelManifest(
            name = "test-model",
            repo = "test/repo",
            filePattern = "*.gguf"
        )

        service.startProvisioning(model)

        // Wait for job to finish (since it's launched in scope)
        // In a real test we'd use a more robust way to wait
        var attempts = 0
        while (service.updates.value["test-model"]?.stage != ProvisioningStage.COMPLETED && attempts < 10) {
            delay(100.milliseconds)
            attempts++
        }

        val status = service.updates.value["test-model"]
        assertEquals(ProvisioningStage.COMPLETED, status?.stage)
        assertEquals("Already current.", status?.message)
    }
}
