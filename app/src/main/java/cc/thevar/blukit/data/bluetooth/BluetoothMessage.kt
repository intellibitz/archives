package cc.thevar.blukit.data.bluetooth

data class BluetoothMessage(
    val message: String,
    val senderName: String,
    val isFromLocalUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
