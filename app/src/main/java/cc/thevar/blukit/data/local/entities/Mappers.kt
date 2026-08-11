package cc.thevar.blukit.data.local.entities

import cc.thevar.blukit.domain.model.MessagePayload

fun MessagePayload.toMessageEntity(isFromLocalUser: Boolean): MessageEntity {
    return MessageEntity(
        messageId = messageId,
        senderId = senderId,
        senderName = senderName,
        receiverId = receiverId,
        content = content,
        timestamp = timestamp,
        type = type,
        isFromLocalUser = isFromLocalUser,
        status = status
    )
}

fun MessageEntity.toBluetoothPayload(): MessagePayload {
    return MessagePayload(
        messageId = messageId,
        senderId = senderId,
        senderName = senderName,
        receiverId = receiverId,
        content = content,
        timestamp = timestamp,
        type = type,
        status = status
    )
}
