package cc.thevar.blukit.data.bluetooth

import kotlinx.serialization.Serializable

@Serializable
data class BluetoothPayload(
    val messageId: String,
    val senderId: String,
    val senderName: String,
    val receiverId: String? = null,
    val content: String,
    val timestamp: Long,
    val type: Int = TYPE_TEXT
) {
    companion object {
        const val TYPE_TEXT = 1
        const val TYPE_IMAGE = 2
    }
}
