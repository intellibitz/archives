package cc.thevar.acc.service

import cc.thevar.acc.protocol.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import org.slf4j.LoggerFactory
import java.io.File
import kotlin.time.Duration.Companion.seconds

class MonitoringService(
    private val supervisorService: SupervisorService,
    private val provisioningService: ProvisioningService,
    private val fleetManager: FleetManager,
    private val systemMetricsService: SystemMetricsService,
    private val client: HttpClient,
    private val ollamaHost: String = System.getenv("OLLAMA_HOST") ?: "http://localhost:11434"
) : AutoCloseable {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val json = Json { ignoreUnknownKeys = true }
    var systemStatusMsg = "Initializing Acc..."

    private val _systemState = MutableStateFlow<SystemState?>(null)
    val systemState = _systemState.asStateFlow()

    fun start(applicationScope: CoroutineScope) {
        systemStatusMsg = "Acc Ready."

        // Metrics and Status loop
        applicationScope.launch(Dispatchers.IO) {
            while (isActive) {
                try {
                    val stats = systemMetricsService.getSystemStats()
                    
                    val (installed, running) = fetchOllamaStatus()

                    val fleet = fleetManager.getFleet().map { model ->
                        ModelStatus(
                            name = model.name,
                            provider = model.provider,
                            isInstalled = model.provider != "ollama" || installed.contains(model.name),
                            isRunning = model.provider != "ollama" || running.contains(model.name),
                            type = if (model.isPrivate) "PRIV" else "COMM"
                        )
                    }

                    _systemState.value = SystemState(
                        stats = stats,
                        fleet = fleet,
                        workers = supervisorService.workerStates.value,
                        provisioning = provisioningService.updates.value.values.toList(),
                        proxyOnline = false,
                        engineOnline = installed.isNotEmpty() || running.isNotEmpty(),
                        statusMsg = systemStatusMsg
                    )
                } catch (e: Exception) {
                    logger.debug("Monitoring loop error: {}", e.message)
                }
                delay(2.seconds)
            }
        }
    }

    private suspend fun fetchOllamaStatus(): Pair<Set<String>, Set<String>> {
        return try {
            coroutineScope {
                val installedDeferred = async {
                    val response = client.get("$ollamaHost/api/tags")
                    if (response.status.value == 200) {
                        json.parseToJsonElement(response.bodyAsText()).jsonObject["models"]?.jsonArray
                            ?.map { it.jsonObject["name"]?.jsonPrimitive?.content?.split(":")?.first() ?: "" }
                            ?.toSet() ?: emptySet()
                    } else emptySet()
                }
                val runningDeferred = async {
                    val response = client.get("$ollamaHost/api/ps")
                    if (response.status.value == 200) {
                        json.parseToJsonElement(response.bodyAsText()).jsonObject["models"]?.jsonArray
                            ?.map { it.jsonObject["name"]?.jsonPrimitive?.content?.split(":")?.first() ?: "" }
                            ?.toSet() ?: emptySet()
                    } else emptySet()
                }
                installedDeferred.await() to runningDeferred.await()
            }
        } catch (e: Exception) {
            emptySet<String>() to emptySet()
        }
    }

    override fun close() {
        logger.info("Closing MonitoringService...")
        scope.cancel("MonitoringService closing")
    }
}
