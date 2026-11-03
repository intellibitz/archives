package cc.thevar.acc.routing

import cc.thevar.acc.service.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import java.io.File

fun Application.configureRouting() {
    val projectRoot by inject<File>()
    val fleetManager by inject<FleetManager>()
    val provisioningService by inject<ProvisioningService>()
    val supervisorService by inject<SupervisorService>()
    val monitoringService by inject<MonitoringService>()
    val commandHandler by inject<CommandHandler>()
    val sessionManager by inject<SessionManager>()

    routing {
        authenticate("auth-basic") {
            statusRoutes(supervisorService)
            provisioningRoutes(fleetManager, provisioningService)
            agentRoutes(supervisorService)
            webSocketRoutes(sessionManager, provisioningService, monitoringService, commandHandler)
        }

        staticRoutes(projectRoot, supervisorService, monitoringService)
    }
}
