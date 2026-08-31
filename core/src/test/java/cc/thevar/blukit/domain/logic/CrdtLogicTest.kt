package cc.thevar.blukit.domain.logic

import cc.thevar.blukit.domain.model.Message
import org.junit.Assert.assertEquals
import org.junit.Test

class CrdtLogicTest {

    @Test
    fun testLwwCrdt_HigherVersionWins() {
        val original = Message(
            messageId = "1",
            senderId = "A",
            senderName = "Alice",
            content = "Old Content",
            timestamp = 1000,
            noteVersion = 1,
            type = Message.TYPE_NOTE_UPDATE
        )

        val update = original.copy(
            content = "New Content",
            noteVersion = 2,
            timestamp = 1100
        )

        // Simulating the logic from MessageRepository
        val result = if (update.noteVersion > original.noteVersion) update else original
        
        assertEquals("New Content", result.content)
        assertEquals(2, result.noteVersion)
    }

    @Test
    fun testLwwCrdt_SameVersionHigherTimestampWins() {
        val original = Message(
            messageId = "1",
            senderId = "A",
            senderName = "Alice",
            content = "Old Content",
            timestamp = 1000,
            noteVersion = 1,
            type = Message.TYPE_NOTE_UPDATE
        )

        val update = original.copy(
            content = "Concurrent Content",
            noteVersion = 1,
            timestamp = 1100
        )

        val result = if ((update.noteVersion > original.noteVersion) || 
            ((update.noteVersion == original.noteVersion) && (update.timestamp > original.timestamp))) {
            update
        } else {
            original
        }
        
        assertEquals("Concurrent Content", result.content)
    }
}
