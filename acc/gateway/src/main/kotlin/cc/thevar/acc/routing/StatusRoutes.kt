package cc.thevar.acc.routing

import cc.thevar.acc.protocol.WorkerStatus
import cc.thevar.acc.service.SupervisorService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.statusRoutes(supervisorService: SupervisorService) {
    get("/health") {
        val states = supervisorService.workerStates.value
        val isHealthy = states.all { 
            it.status == WorkerStatus.RUNNING || 
            it.status == WorkerStatus.COMPLETED || 
            it.name == "FRONTEND_BUILDER" 
        }
        if (isHealthy) {
            call.respondText("Acc Gateway is Online.")
        } else {
            call.respond(HttpStatusCode.ServiceUnavailable, states)
        }
    }

    post("/system/update") {
        call.respondText("Update sequence initiated (Native).")
    }
}
