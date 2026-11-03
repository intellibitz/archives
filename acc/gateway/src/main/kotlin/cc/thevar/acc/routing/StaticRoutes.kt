package cc.thevar.acc.routing

import cc.thevar.acc.protocol.WorkerStatus
import cc.thevar.acc.service.MonitoringService
import cc.thevar.acc.service.SupervisorService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File

fun Route.staticRoutes(
    projectRoot: File, 
    supervisorService: SupervisorService, 
    monitoringService: MonitoringService
) {
    val staticDir = File(projectRoot, "frontend/web/build/dist/wasmJs/productionExecutable")

    get("/") {
        val resource = this::class.java.classLoader.getResource("static/index.html")
        if (resource != null) {
            call.respondText(resource.readBytes().toString(Charsets.UTF_8), ContentType.Text.Html)
            return@get
        }

        val indexFile = File(staticDir, "index.html")
        if (indexFile.exists()) {
            call.respondFile(indexFile)
        } else {
            val workerStates = supervisorService.workerStates.value
            val builder = workerStates.find { it.name == "FRONTEND_BUILDER" }

            val bootstrapHtml = """
                <!DOCTYPE html>
                <html>
                <head>
                    <title>Acc Bootstrapping...</title>
                    <style>
                        body { background: #121212; color: #BB86FC; font-family: monospace; display: flex; flex-direction: column; align-items: center; justify-content: center; height: 100vh; margin: 0; }
                        .loader { border: 4px solid #1E1E1E; border-top: 4px solid #BB86FC; border-radius: 50%; width: 40px; height: 40px; animation: spin 1s linear infinite; margin-bottom: 20px; }
                        @keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }
                        .status { color: #03DAC6; font-size: 1.2em; }
                    </style>
                    <meta http-equiv="refresh" content="5">
                </head>
                <body>
                    <div class="loader"></div>
                    <div>AI Command Center is initializing...</div>
                    <div class="status" style="color: #03DAC6; margin-top: 10px;">${monitoringService.systemStatusMsg}</div>
                    <div style="margin-top: 20px; color: ${if (builder?.status == WorkerStatus.CRASHED) "red" else "#BB86FC"}">
                        Frontend Build: ${builder?.status ?: "WAITING"}
                    </div>
                    <div style="margin-top: 30px; color: gray; font-size: 0.7em;">(Auto-refreshing every 5s)</div>
                </body>
                </html>
            """.trimIndent()

            call.respondText(bootstrapHtml, ContentType.Text.Html)

            if (builder == null || builder.status == WorkerStatus.STOPPED) {
                supervisorService.startWorker("FRONTEND_BUILDER")
            }
        }
    }

    staticResources("/", "static") {
        contentType { file ->
            val name = file.path.lowercase()
            when {
                name.endsWith(".wasm") -> ContentType.Application.Wasm
                name.endsWith(".js") -> ContentType.Application.JavaScript
                name.endsWith(".css") -> ContentType.Text.CSS
                name.endsWith(".html") -> ContentType.Text.Html
                else -> null
            }
        }
    }

    staticFiles("/", staticDir) {
        contentType { file ->
            when (file.extension) {
                "wasm" -> ContentType.Application.Wasm
                "js" -> ContentType.Application.JavaScript
                "css" -> ContentType.Text.CSS
                "html" -> ContentType.Text.Html
                else -> null
            }
        }
    }
}
