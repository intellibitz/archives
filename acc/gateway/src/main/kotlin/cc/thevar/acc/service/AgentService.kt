package cc.thevar.acc.service

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.*

class AgentService(
    private val client: HttpClient,
    private val ollamaHost: String = System.getenv("OLLAMA_HOST") ?: "http://localhost:11434"
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun generateResponse(model: String, prompt: String): String {
        return try {
            val response: HttpResponse = client.post("$ollamaHost/api/generate") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("model", model)
                    put("prompt", prompt)
                    put("stream", false)
                })
            }
            if (response.status.value == 200) {
                val body = response.bodyAsText()
                json.parseToJsonElement(body).jsonObject["response"]?.jsonPrimitive?.content ?: ""
            } else "Error: ${response.status}"
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
}
