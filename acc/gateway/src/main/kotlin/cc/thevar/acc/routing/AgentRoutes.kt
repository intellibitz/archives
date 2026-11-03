package cc.thevar.acc.routing

import cc.thevar.acc.service.SupervisorService
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.agentRoutes(supervisorService: SupervisorService) {
    post("/agent/spawn") {
        val agentName = call.request.queryParameters["name"] ?: "Architect-${System.currentTimeMillis() % 1000}"
        val model = call.request.queryParameters["model"] ?: "ollama/phi3"
        val apiBase = call.request.queryParameters["apiBase"]
        
        supervisorService.spawnAgent(agentName, model, apiBase)
        call.respondText("Agent $agentName spawned for model $model.")
    }

    post("/engine/start") {
        val provider = call.request.queryParameters["provider"] ?: "ollama"
        supervisorService.startEngine(provider)
        call.respondText("Starting engine: $provider")
    }
}
