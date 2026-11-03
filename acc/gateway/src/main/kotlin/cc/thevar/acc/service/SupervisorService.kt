package cc.thevar.acc.service

import cc.thevar.acc.protocol.WorkerState
import cc.thevar.acc.protocol.WorkerStatus
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.slf4j.LoggerFactory
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.seconds

class SupervisorService(private val projectRoot: File) : AutoCloseable {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val workers = ConcurrentHashMap<String, ManagedWorker>()
    
    private val _workerStates = MutableStateFlow<List<WorkerState>>(emptyList())
    val workerStates = _workerStates.asStateFlow()

    init {
        // No more Python bridges
        registerWorker("FRONTEND_BUILDER", listOf("./gradlew", ":frontend:web:wasmJsBrowserDistribution", "--quiet"), restartPolicy = false)
        
        // Start monitoring loop
        scope.launch {
            while (isActive) {
                checkWorkers()
                delay(2.seconds)
            }
        }
    }

    fun registerWorker(name: String, command: List<String>, restartPolicy: Boolean, env: Map<String, String> = emptyMap()) {
        workers[name] = ManagedWorker(name, command, restartPolicy, env)
    }

    fun spawnAgent(agentName: String, model: String, apiBase: String? = null) {
        logger.info("Spawning Kotlin-native agent: {} for model: {} (API: {})", agentName, model, apiBase ?: "Default")
        // In this new architecture, agents are services, not separate processes.
        // We can track them here if needed, or just let the AgentService handle requests.
    }

    fun startEngine(provider: String) {
        if (provider != "ollama") {
            logger.warn("Only 'ollama' engine provider is supported in sandbox mode for now.")
            return
        }

        scope.launch {
            try {
                logger.info("Starting engine provider: {}", provider)
                // Check if already running
                if (isOllamaRunning()) {
                    logger.info("Ollama is already running.")
                    return@launch
                }

                val pb = ProcessBuilder("ollama", "serve")
                    .directory(projectRoot)
                    .redirectErrorStream(true)
                
                pb.environment()["OLLAMA_MODELS"] = File(projectRoot, "data/ollama").absolutePath
                File(projectRoot, "data/ollama").mkdirs()

                pb.start()
                registerWorker("ENGINE_OLLAMA", listOf("ollama", "serve"), restartPolicy = true)
                // In this local mode, we'll wrap it in a ManagedWorker if we want status tracking
                // For now, let's just start it and let ManagedWorker handle it if registered
                
                logger.info("Engine provider {} started.", provider)
            } catch (e: Exception) {
                logger.error("Failed to start engine {}: {}", provider, e.message, e)
            }
        }
    }

    private fun isOllamaRunning(): Boolean {
        return ProcessHandle.allProcesses().anyMatch { ph ->
            ph.info().commandLine().map { it.contains("ollama serve") }.orElse(false)
        }
    }

    fun startWorker(name: String) {
        workers[name]?.start()
    }

    fun stopAll() {
        workers.values.forEach { it.stop() }
    }

    override fun close() {
        logger.info("Closing SupervisorService, stopping all workers...")
        stopAll()
        scope.cancel("SupervisorService closing")
    }

    private fun checkWorkers() {
        val states = workers.values.map { it.updateState() }
        _workerStates.value = states
        
        // Auto-heal
        workers.values.forEach { worker ->
            if (worker.shouldRestart()) {
                logger.warn("Worker {} crashed. Restarting...", worker.name)
                worker.start()
            }
        }
    }

    private inner class ManagedWorker(
        val name: String,
        val command: List<String>,
        val restartPolicy: Boolean,
        val env: Map<String, String> = emptyMap()
    ) {
        private var process: Process? = null
        var status = WorkerStatus.STOPPED
        var restarts = 0
        var lastMsg = ""
        var lastError: String? = null
        private var logJob: Job? = null

        fun start() {
            if (status == WorkerStatus.RUNNING || status == WorkerStatus.STARTING) return
            
            status = WorkerStatus.STARTING
            lastError = null
            
            try {
                val pb = ProcessBuilder(command).directory(projectRoot).redirectErrorStream(true)
                pb.environment().putAll(env)
                process = pb.start()
                status = WorkerStatus.RUNNING
                
                // Capture logs
                logJob?.cancel()
                logJob = scope.launch {
                    process?.inputStream?.bufferedReader()?.useLines { lines ->
                        lines.forEach { line ->
                            lastMsg = line.trim()
                            if (lastMsg.length > 100) lastMsg = lastMsg.take(97) + "..."
                        }
                    }
                }
            } catch (e: Exception) {
                status = WorkerStatus.CRASHED
                lastError = e.message
            }
        }

        fun stop() {
            logJob?.cancel()
            process?.let {
                logger.info("Stopping worker {}: Sending SIGTERM...", name)
                it.destroy()
                if (!it.waitFor(5, TimeUnit.SECONDS)) {
                    logger.warn("Worker {} failed to stop gracefully. Sending SIGKILL...", name)
                    it.destroyForcibly()
                }
            }
            status = WorkerStatus.STOPPED
        }

        fun shouldRestart(): Boolean {
            return restartPolicy && status == WorkerStatus.CRASHED
        }

        fun updateState(): WorkerState {
            val p = process
            if (p != null && !p.isAlive) {
                val exitCode = p.exitValue()
                if (exitCode == 0) {
                    status = WorkerStatus.COMPLETED
                } else {
                    if (status != WorkerStatus.STOPPED && status != WorkerStatus.COMPLETED) {
                        status = WorkerStatus.CRASHED
                        restarts++
                        lastError = "Exit Code $exitCode"
                    }
                }
            }
            
            return WorkerState(
                name = name,
                status = status,
                pid = try { p?.pid()?.toInt() } catch (e: Exception) { null },
                restarts = restarts,
                lastMsg = if (status == WorkerStatus.RUNNING) lastMsg else (lastError ?: ""),
                error = lastError
            )
        }
    }
}
