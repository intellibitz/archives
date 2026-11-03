package cc.thevar.acc.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cc.thevar.acc.protocol.SystemState
import io.ktor.client.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.websocket.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import cc.thevar.acc.getPlatform

class SystemViewModel : ViewModel() {
    private val _state = MutableStateFlow<SystemState?>(null)
    val state: StateFlow<SystemState?> = _state.asStateFlow()

    private val client = HttpClient {
        install(WebSockets)
        install(Auth) {
            basic {
                credentials {
                    val creds = AuthStore.credentials.value
                    if (creds != null) {
                        BasicAuthCredentials(creds.first, creds.second)
                    } else {
                        null
                    }
                }
                sendWithoutRequest { true }
            }
        }
    }

    fun connect(host: String = getPlatform().defaultGatewayHost, port: Int = 8333) {
        viewModelScope.launch {
            try {
                // Try secure first
                try {
                    client.webSocket(host = host, port = port, path = "/ws/system", request = { url.protocol = io.ktor.http.URLProtocol.WSS }) {
                        processSession(this)
                    }
                } catch (e: Exception) {
                    client.webSocket(host = host, port = port, path = "/ws/system") {
                        processSession(this)
                    }
                }
            } catch (e: Exception) {
                println("System WebSocket failed: ${e.message}")
            }
        }
    }

    private suspend fun processSession(session: DefaultClientWebSocketSession) {
        for (frame in session.incoming) {
            if (frame is Frame.Text) {
                val text = frame.readText()
                _state.value = Json.decodeFromString<SystemState>(text)
            }
        }
    }

    fun updateSystem() {
        viewModelScope.launch {
            try {
                // We use a simple HTTP POST for the update trigger
                val host = "localhost" // Should be dynamic in real app
                val port = 8333
                client.post("https://$host:$port/system/update")
            } catch (e: Exception) {
                println("Update trigger failed: ${e.message}")
            }
        }
    }

    fun spawnAgent(name: String, model: String) {
        viewModelScope.launch {
            try {
                val host = "localhost"
                val port = 8333
                client.post("https://$host:$port/agent/spawn") {
                    url {
                        parameters.append("name", name)
                        parameters.append("model", model)
                    }
                }
            } catch (e: Exception) {
                println("Agent spawn failed: ${e.message}")
            }
        }
    }

    fun startEngine(provider: String) {
        viewModelScope.launch {
            try {
                val host = "localhost"
                val port = 8333
                client.post("https://$host:$port/engine/start") {
                    url {
                        parameters.append("provider", provider)
                    }
                }
            } catch (e: Exception) {
                println("Engine start failed: ${e.message}")
            }
        }
    }
}
