package cc.thevar.acc.routing

import cc.thevar.acc.service.FleetManager
import cc.thevar.acc.service.ProvisioningService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.provisioningRoutes(fleetManager: FleetManager, provisioningService: ProvisioningService) {
    post("/provisioning/up") {
        val modelName = call.request.queryParameters["model"]
        if (modelName != null) {
            val model = fleetManager.getFleet().find { it.name == modelName }
            if (model != null) {
                provisioningService.startProvisioning(model)
                call.respondText("Provisioning for $modelName started.")
            } else {
                call.respond(HttpStatusCode.NotFound, "Model $modelName not found in fleet.")
            }
        } else {
            provisioningService.provisionAll()
            call.respondText("Global provisioning started.")
        }
    }
}
