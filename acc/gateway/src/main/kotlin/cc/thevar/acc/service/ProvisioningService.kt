package cc.thevar.acc.service

import cc.thevar.acc.protocol.*
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.*
import org.slf4j.LoggerFactory
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentHashMap

class ProvisioningService(
    private val projectRoot: File,
    private val fleetManager: FleetManager,
    private val client: HttpClient,
    private val ollamaHost: String = System.getenv("OLLAMA_HOST") ?: "http://localhost:11434"
) : AutoCloseable {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val _updates = MutableStateFlow<Map<String, ProvisioningUpdate>>(emptyMap())
    val updates = _updates.asStateFlow()

    private val activeJobs = ConcurrentHashMap<String, Job>()
    private val json = Json { ignoreUnknownKeys = true }

    fun provisionAll() {
        val fleet = fleetManager.getFleet()
        fleet.forEach { model ->
            startProvisioning(model)
        }
    }

    private fun updateStatus(
        name: String, 
        stage: ProvisioningStage, 
        progress: Float = 0f, 
        speed: String = "", 
        message: String = ""
    ) {
        val current = _updates.value.toMutableMap()
        current[name] = ProvisioningUpdate(name, stage, progress, speed, message)
        _updates.value = current
    }

    fun startProvisioning(model: ModelManifest) {
        if (activeJobs.containsKey(model.name)) return

        val job = scope.launch {
            try {
                updateStatus(model.name, ProvisioningStage.SCANNING, 0f, message = "Checking remote status...")
                
                val regDir = File(projectRoot, "registry/${model.name}").apply { mkdirs() }
                val shaFile = File(regDir, "last_sync_sha")
                val localSha = if (shaFile.exists()) shaFile.readText().trim() else ""
                
                val remoteSha = try {
                    val response: HttpResponse = client.get("https://huggingface.co/api/models/${model.repo}")
                    if (response.status.value == 200) {
                        val body = response.bodyAsText()
                        val jsonElement = json.parseToJsonElement(body).jsonObject
                        jsonElement["sha"]?.jsonPrimitive?.content ?: ""
                    } else ""
                } catch (e: Exception) {
                    logger.warn("Failed to fetch remote SHA for {}: {}", model.name, e.message)
                    ""
                }

                val isInstalled = try {
                    val response: HttpResponse = client.get("$ollamaHost/api/tags")
                    if (response.status.value == 200) {
                        val body = response.bodyAsText()
                        val jsonElement = json.parseToJsonElement(body).jsonObject
                        val models = jsonElement["models"]?.jsonArray ?: JsonArray(emptyList())
                        models.any { it.jsonObject["name"]?.jsonPrimitive?.content?.startsWith(model.name) ?: false }
                    } else false
                } catch (e: Exception) {
                    logger.warn("Failed to check Ollama status for {}: {}", model.name, e.message)
                    false
                }

                if (remoteSha != localSha || !isInstalled) {
                    updateStatus(model.name, ProvisioningStage.DOWNLOADING, 0.1f, message = "Downloading model...")
                    
                    val dlDir = File(projectRoot, ".cache/${model.name}").apply { mkdirs() }
                    val targetFile = File(dlDir, model.filePattern.replace("*", "model")) 
                    
                    downloadFile(model.repo, model.filePattern, targetFile) { progress, speed ->
                        updateStatus(model.name, ProvisioningStage.DOWNLOADING, progress, message = speed)
                    }

                    updateStatus(model.name, ProvisioningStage.REGISTERING, 0.9f, message = "Building Ollama model...")
                    registerInOllama(model, regDir, dlDir)
                    
                    if (remoteSha.isNotEmpty()) {
                        shaFile.writeText(remoteSha)
                    }
                    
                    updateStatus(model.name, ProvisioningStage.COMPLETED, 1.0f, message = "Ready.")
                    dlDir.deleteRecursively()
                } else {
                    updateStatus(model.name, ProvisioningStage.COMPLETED, 1.0f, message = "Already current.")
                }
            } catch (e: Exception) {
                updateStatus(model.name, ProvisioningStage.ERROR, message = e.message ?: "Unknown error")
            } finally {
                activeJobs.remove(model.name)
            }
        }
        activeJobs[model.name] = job
    }

    private suspend fun downloadFile(repo: String, filePattern: String, target: File, onProgress: (Float, String) -> Unit) {
        val fileName = filePattern.replace("*", "Q4_K_M")
        val url = "https://huggingface.co/$repo/resolve/main/$fileName"
        
        client.prepareGet(url).execute { response ->
            if (response.status.value != 200) throw Exception("HF Download failed: ${response.status}")
            
            val contentLength = response.contentLength() ?: -1L
            val channel = response.bodyAsChannel()
            target.parentFile.mkdirs()
            
            target.outputStream().use { os ->
                var totalBytes = 0L
                val buffer = ByteArray(1024 * 1024)
                while (!channel.isClosedForRead) {
                    val read = channel.readAvailable(buffer)
                    if (read > 0) {
                        os.write(buffer, 0, read)
                        totalBytes += read
                        if (contentLength > 0) {
                            onProgress(totalBytes.toFloat() / contentLength, "${totalBytes / 1024 / 1024} MB")
                        }
                    }
                }
            }
        }
    }

    private suspend fun registerInOllama(model: ModelManifest, optDir: File, dlDir: File) {
        val ggufFile = dlDir.walkTopDown().find { it.extension == "gguf" } ?: throw Exception("GGUF not found")
        val ollamaGgufPath = ggufFile.absolutePath
        val threads = Runtime.getRuntime().availableProcessors().let { if (it > 4) it - 4 else it }
        val gpuLayers = calculateGpuLayers(model.name)
        val paramFile = File(optDir, "user_params")
        val userParams = if (paramFile.exists()) paramFile.readText() else "PARAMETER temperature 0.7\nPARAMETER top_p 0.9"
        
        val modelfileContent = """
            FROM $ollamaGgufPath
            PARAMETER num_gpu $gpuLayers
            PARAMETER num_thread $threads
            PARAMETER num_ctx 32768
            $userParams
            SYSTEM "You are the Master Architect, an elite Android Lead Engineer."
        """.trimIndent()
        
        updateStatus(model.name, ProvisioningStage.REGISTERING, 0.92f, message = "Sending Modelfile to Ollama...")

        try {
            val response: HttpResponse = client.post("$ollamaHost/api/create") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("name", model.name)
                    put("modelfile", modelfileContent)
                    put("stream", true)
                })
            }

            if (response.status.value == 200) {
                response.bodyAsText().lines().forEach { line ->
                    if (line.isNotBlank()) {
                        val status = json.parseToJsonElement(line).jsonObject["status"]?.jsonPrimitive?.content
                        if (status != null) {
                            updateStatus(model.name, ProvisioningStage.REGISTERING, 0.95f, message = status)
                        }
                    }
                }
            } else {
                throw Exception("Ollama API Error: ${response.status}")
            }
        } catch (e: Exception) {
            throw Exception("Ollama registration failed: ${e.message}")
        }
    }

    private fun calculateGpuLayers(name: String): Int {
        return when {
            name.contains("70b", ignoreCase = true) -> 20
            name.contains("8x22b", ignoreCase = true) || name.contains("command-r-plus", ignoreCase = true) -> 10
            else -> 99
        }
    }

    fun pruneFleet() {
        scope.launch {
            try {
                val fleet = fleetManager.getFleet().map { it.name }.toSet()
                val response: HttpResponse = client.get("$ollamaHost/api/tags")
                if (response.status.value == 200) {
                    val body = response.bodyAsText()
                    val jsonElement = json.parseToJsonElement(body).jsonObject
                    val installed = jsonElement["models"]?.jsonArray?.map { it.jsonObject["name"]?.jsonPrimitive?.content?.split(":")?.first() ?: "" } ?: emptyList()
                    
                    installed.forEach { model ->
                        if (model.isNotEmpty() && !fleet.contains(model)) {
                            logger.info("[Prune] Removing unmanaged model: {}", model)
                            client.delete("$ollamaHost/api/delete") {
                                contentType(ContentType.Application.Json)
                                setBody(buildJsonObject { put("name", model) })
                            }
                        }
                    }
                }
                File(projectRoot, ".cache").listFiles()?.forEach { it.deleteRecursively() }
            } catch (e: Exception) {
                logger.error("[Prune] Error: {}", e.message)
            }
        }
    }

    fun backupConfig() {
        val ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        val backupDir = File(projectRoot, "data/backups/$ts").apply { mkdirs() }
        try {
            File(projectRoot, "config").listFiles()?.forEach { if (it.isFile) it.copyTo(File(backupDir, it.name)) }
            File(projectRoot, "registry").copyRecursively(File(backupDir, "registry"), overwrite = true)
        } catch (e: Exception) {
            logger.error("[Backup] Error: {}", e.message)
        }
    }

    fun autoScale() {
        logger.info("Hardware-aware auto-scaling initiated...")
        // This will eventually re-trigger provisioning with updated hardware profiles
    }

    override fun close() {
        activeJobs.values.forEach { it.cancel() }
        scope.cancel()
        client.close()
    }
}
