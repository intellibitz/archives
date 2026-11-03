package cc.thevar.acc.service

import cc.thevar.acc.protocol.ConsoleLine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File

class CommandHandler(
    private val projectRoot: File,
    private val provisioningService: ProvisioningService
) {
    fun handleCommand(commandLine: String): Flow<ConsoleLine> = flow {
        val parts = commandLine.trim().split(" ")
        val baseCommand = parts.firstOrNull() ?: return@flow

        emit(ConsoleLine("$ acc $commandLine", "COMMAND"))

        when (baseCommand) {
            "prune" -> {
                provisioningService.pruneFleet()
                emit(ConsoleLine("Fleet pruning initiated.", "INFO"))
                emit(ConsoleLine("Task Finished", "SUCCESS"))
            }
            "backup" -> {
                provisioningService.backupConfig()
                emit(ConsoleLine("Configuration backup initiated.", "INFO"))
                emit(ConsoleLine("Task Finished", "SUCCESS"))
            }
            "auto-scale" -> {
                provisioningService.autoScale()
                emit(ConsoleLine("Hardware-aware auto-scaling initiated.", "INFO"))
                emit(ConsoleLine("Task Finished", "SUCCESS"))
            }
            "sync" -> {
                provisioningService.provisionAll()
                emit(ConsoleLine("Fleet synchronization started.", "INFO"))
                emit(ConsoleLine("Task Finished", "SUCCESS"))
            }
            else -> {
                emit(ConsoleLine("Error: Command '$baseCommand' is not allowed or unrecognized. External commands are disabled in Kotlin-native mode.", "ERROR"))
            }
        }
    }.flowOn(Dispatchers.IO)
}
