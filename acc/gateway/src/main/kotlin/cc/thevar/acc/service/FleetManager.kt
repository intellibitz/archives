package cc.thevar.acc.service

import cc.thevar.acc.protocol.ModelManifest
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
private data class FleetConfig(
    val models: List<ModelManifest> = emptyList()
)

class FleetManager(private val configDir: File) {
    private val fleetJson = File(configDir, "fleet.json")
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    fun getFleet(): List<ModelManifest> {
        if (!fleetJson.exists()) {
            migrateLegacyConfig()
        }
        return try {
            val content = fleetJson.readText()
            json.decodeFromString<FleetConfig>(content).models
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveFleet(models: List<ModelManifest>) {
        val config = FleetConfig(models)
        fleetJson.writeText(json.encodeToString(config))
    }

    private fun migrateLegacyConfig() {
        val fleetConf = File(configDir, "fleet.conf")
        val privateFleetConf = File(configDir, "private_fleet.conf")
        val models = mutableListOf<ModelManifest>()

        fun parseConf(file: File, isPrivate: Boolean) {
            if (!file.exists()) return
            val content = file.readText()
            // Brittle but better than nothing migration
            val regex = "\"([^\"]+)\"".toRegex()
            regex.findAll(content).forEach { match ->
                val parts = match.groupValues[1].split("|")
                if (parts.size >= 8) {
                    models.add(ModelManifest(
                        provider = parts[0],
                        name = parts[1],
                        repo = parts[2],
                        filePattern = parts[3],
                        tier = parts[4],
                        quant = parts[5],
                        superpower = parts[6],
                        isPrivate = isPrivate
                    ))
                }
            }
        }

        parseConf(fleetConf, false)
        parseConf(privateFleetConf, true)

        if (models.isNotEmpty()) {
            saveFleet(models)
            // Backup legacy files
            fleetConf.renameTo(File(configDir, "fleet.conf.bak"))
            privateFleetConf.renameTo(File(configDir, "private_fleet.conf.bak"))
        }
    }
}
