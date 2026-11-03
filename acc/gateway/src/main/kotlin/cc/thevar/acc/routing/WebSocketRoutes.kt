package cc.thevar.acc.routing

import cc.thevar.acc.protocol.*
import cc.thevar.acc.service.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun Route.webSocketRoutes(
    sessionManager: SessionManager,
    provisioningService: ProvisioningService,
    monitoringService: MonitoringService,
    commandHandler: CommandHandler
) {
    webSocket("/ws/ui") {
        sessionManager.uiSessions.add(this)
        val job = launch {
            monitoringService.systemState.filterNotNull().collect { state ->
                send(Frame.Text(Json.encodeToString(state)))
            }
        }
        try {
            for (frame in incoming) { }
        } finally {
            job.cancel()
            sessionManager.uiSessions.remove(this)
        }
    }

    webSocket("/ws/system") {
        sessionManager.systemSessions.add(this)
        val job = launch {
            monitoringService.systemState.filterNotNull().collect { state ->
                send(Frame.Text(Json.encodeToString(state)))
            }
        }
        try {
            for (frame in incoming) { }
        } finally {
            job.cancel()
            sessionManager.systemSessions.remove(this)
        }
    }

    webSocket("/ws/console") {
        try {
            for (frame in incoming) {
                if (frame is Frame.Text) {
                    val commandLine = frame.readText().trim()
                    commandHandler.handleCommand(commandLine).collect { line: ConsoleLine ->
                        send(Frame.Text(Json.encodeToString(line)))
                    }
                }
            }
        } catch (e: Exception) {
            send(Frame.Text(Json.encodeToString(ConsoleLine("Error: ${e.message}", "ERROR"))))
        }
    }

    webSocket("/ws/agent") {
        try {
            for (frame in incoming) {
                if (frame is Frame.Text) {
                    val text = frame.readText()
                    sessionManager.uiSessions.forEach { session ->
                        session.launch {
                            try {
                                session.send(Frame.Text(text))
                            } catch (e: Exception) { }
                        }
                    }
                }
            }
        } finally { }
    }

    webSocket("/ws/provisioning") {
        try {
            for (frame in incoming) {
                if (frame is Frame.Text) {
                    if (frame.readText() == "UP") provisioningService.provisionAll()
                }
            }
        } finally { }
    }
}
