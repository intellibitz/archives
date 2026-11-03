package cc.thevar.acc.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cc.thevar.acc.protocol.AgentMessage
import io.ktor.client.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import cc.thevar.acc.getPlatform

class AgentViewModel : ViewModel() {
    private val _messages = MutableStateFlow<List<AgentMessage>>(emptyList())
    val messages: StateFlow<List<AgentMessage>> = _messages.asStateFlow()

    private val client = HttpClient {
        install(WebSockets) {
            contentConverter = KotlinxWebsocketSerializationConverter(Json)
        }
        install(ContentNegotiation) {
            json()
        }
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
                    client.webSocket(host = host, port = port, path = "/ws/ui", request = { url.protocol = io.ktor.http.URLProtocol.WSS }) {
                        println("Connected to Secure Gateway WebSocket")
                        processSession(this)
                    }
                } catch (e: Exception) {
                    client.webSocket(host = host, port = port, path = "/ws/ui") {
                        println("Connected to Gateway WebSocket")
                        processSession(this)
                    }
                }
            } catch (e: Exception) {
                println("WebSocket connection failed: ${e.message}")
            }
        }
    }

    private suspend fun processSession(session: DefaultClientWebSocketSession) {
        while (true) {
            try {
                val message = session.receiveDeserialized<AgentMessage>()
                _messages.update { it + message }
            } catch (e: Exception) {
                println("Error receiving message: ${e.message}")
                break
            }
        }
    }

    override fun onCleared() {
        client.close()
        super.onCleared()
    }
}
