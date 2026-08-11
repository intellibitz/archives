package cc.thevar.blukit.domain.model

sealed interface ConnectionStatus {
    data object Idle : ConnectionStatus
    data object Connecting : ConnectionStatus
    data object Connected : ConnectionStatus
    data class Error(val message: String) : ConnectionStatus
    data class ConnectionLost(val reason: String = "Disconnected") : ConnectionStatus
    data class Received(val payload: MessagePayload) : ConnectionStatus
}
