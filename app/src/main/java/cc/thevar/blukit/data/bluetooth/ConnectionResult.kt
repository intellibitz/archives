package cc.thevar.blukit.data.bluetooth

sealed interface ConnectionResult {
    data object ConnectionEstablished : ConnectionResult
    data class TransferSucceeded(val message: BluetoothPayload) : ConnectionResult
    data class Error(val message: String) : ConnectionResult
}
