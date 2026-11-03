package cc.thevar.acc.service

import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

class SystemBootstrapper(private val projectRoot: File) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun bootstrap() {
        logger.info("Bootstrapping acc environment in folder sandbox mode...")
        
        // 1. Create directory structure
        val dirs = listOf("config", "logs", "data", "data/backups", "registry", ".cache")
        dirs.forEach { dir ->
            File(projectRoot, dir).mkdirs()
        }

        // 2. Generate Keystore if missing
        val keystoreFile = File(projectRoot, "config/keystore.p12")
        if (!keystoreFile.exists()) {
            generateKeystore(keystoreFile)
        }

        // 3. Initialize default fleet if missing
        val fleetJson = File(projectRoot, "config/fleet.json")
        if (!fleetJson.exists()) {
            val defaultFleet = """
                {
                    "models": [
                        {
                            "provider": "ollama",
                            "name": "phi3",
                            "repo": "microsoft/Phi-3-mini-4k-instruct-gguf",
                            "filePattern": "*Q4_K_M.gguf",
                            "tier": "FAST",
                            "quant": "Q4_K_M",
                            "isPrivate": false
                        }
                    ]
                }
            """.trimIndent()
            fleetJson.writeText(defaultFleet)
        }

        // 4. Create sentinel
        val initSentinel = File(projectRoot, "data/.initialized")
        if (!initSentinel.exists()) {
            initSentinel.createNewFile()
        }

        // 5. System tuning (Linux only)
        if (System.getProperty("os.name").contains("Linux", ignoreCase = true)) {
            applyLinuxTuning()
        }
        
        logger.info("Environment ready.")
    }

    private fun generateKeystore(file: File) {
        logger.info("Generating secure local identity...")
        val password = System.getenv("ACC_KEYSTORE_PASSWORD") ?: "password"
        try {
            val cmd = listOf(
                "keytool", "-genkeypair", "-alias", "acc", "-keyalg", "RSA", "-keysize", "2048",
                "-storetype", "PKCS12", "-keystore", file.absolutePath, "-validity", "365",
                "-storepass", password, "-keypass", password,
                "-dname", "CN=localhost, OU=acc, O=intellibitz, L=Unknown, ST=Unknown, C=Unknown"
            )
            val process = ProcessBuilder(cmd).start()
            if (process.waitFor() != 0) {
                logger.warn("Failed to generate keystore via keytool. SSL might be unavailable.")
            }
        } catch (e: Exception) {
            logger.warn("keytool not found. Skipping keystore generation.")
        }
    }

    private fun applyLinuxTuning() {
        // This usually requires sudo, so we just log what's needed or try if we have permissions
        logger.info("Applying system optimizations...")
        try {
            // Check max_map_count (required for some LLM runtimes)
            val maxMapCountPath = Paths.get("/proc/sys/vm/max_map_count")
            if (Files.exists(maxMapCountPath)) {
                val current = Files.readString(maxMapCountPath).trim().toLong()
                if (current < 262144) {
                    logger.warn("vm.max_map_count is low ($current). Consider setting it to 262144 for better LLM performance.")
                }
            }
        } catch (e: Exception) {
            logger.debug("Failed to read system tuning parameters: {}", e.message)
        }
    }
}
