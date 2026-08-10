package cc.thevar.blukit.data.local.entities

import cc.thevar.blukit.data.bluetooth.BluetoothPayload

fun BluetoothPayload.toMessageEntity(isFromLocalUser: Boolean): MessageEntity {
    return MessageEntity(
        messageId = messageId,
        senderId = senderId,
        senderName = senderName,
        receiverId = receiverId,
        content = content,
        timestamp = timestamp,
        type = type,
        isFromLocalUser = isFromLocalUser,
        status = if (isFromLocalUser) 1 else 2 // 1: Sent, 2: Received
    )
}

fun MessageEntity.toBluetoothPayload(): BluetoothPayload {
    return BluetoothPayload(
        messageId = messageId,
        senderId = senderId,
        senderName = senderName,
        receiverId = receiverId,
        content = content,
        timestamp = timestamp,
        type = type
    )
}
