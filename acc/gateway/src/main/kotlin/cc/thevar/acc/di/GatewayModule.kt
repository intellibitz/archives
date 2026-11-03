package cc.thevar.acc.di

import cc.thevar.acc.service.*
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import java.io.File

fun gatewayModule(projectRoot: File) = module {
    single { projectRoot }
    single { SessionManager() }
    single { SystemBootstrapper(get()) }
    single { SystemMetricsService(get()) }
    single {
        val proxyUrl = System.getenv("HTTP_PROXY") ?: System.getenv("http_proxy")
        HttpClient(CIO) {
            engine {
                proxyUrl?.let { proxy = ProxyBuilder.http(it) }
            }
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }
    single { FleetManager(File(get<File>(), "config")) }
    single { ProvisioningService(get(), get(), get()) }
    single { SupervisorService(get()) }
    single { AgentService(get()) }
    single { CommandHandler(get(), get()) }
    single { MonitoringService(get(), get(), get(), get(), get()) }
}
