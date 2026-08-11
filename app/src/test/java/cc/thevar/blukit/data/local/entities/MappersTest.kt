package cc.thevar.blukit.data.local.entities

import cc.thevar.blukit.domain.model.MessagePayload
import org.junit.Assert.assertEquals
import org.junit.Test

class MappersTest {

    @Test
    fun `toMessageEntity correctly maps MessagePayload to MessageEntity`() {
        val payload = MessagePayload(
            messageId = "msg-123",
            senderId = "sender-456",
            senderName = "John Doe",
            content = "Hello there!",
            timestamp = 1628765432100,
            type = MessagePayload.TYPE_TEXT
        )

        val entity = payload.toMessageEntity(isFromLocalUser = true)

        assertEquals(payload.messageId, entity.messageId)
        assertEquals(payload.senderId, entity.senderId)
        assertEquals(payload.senderName, entity.senderName)
        assertEquals(payload.content, entity.content)
        assertEquals(payload.timestamp, entity.timestamp)
        assertEquals(payload.type, entity.type)
        assertEquals(true, entity.isFromLocalUser)
        assertEquals(1, entity.status) // Sent
    }

    @Test
    fun `toBluetoothPayload correctly maps MessageEntity to MessagePayload`() {
        val entity = MessageEntity(
            messageId = "msg-123",
            senderId = "sender-456",
            senderName = "John Doe",
            content = "Hello there!",
            timestamp = 1628765432100,
            type = MessagePayload.TYPE_TEXT,
            isFromLocalUser = false,
            status = 2 // Received
        )

        val payload = entity.toBluetoothPayload()

        assertEquals(entity.messageId, payload.messageId)
        assertEquals(entity.senderId, payload.senderId)
        assertEquals(entity.senderName, payload.senderName)
        assertEquals(entity.content, payload.content)
        assertEquals(entity.timestamp, payload.timestamp)
        assertEquals(entity.type, payload.type)
    }
}
