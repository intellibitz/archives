package cc.thevar.blukit.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import cc.thevar.blukit.data.local.dao.MessageDao
import cc.thevar.blukit.data.local.entities.MessageEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChatDatabaseTest {

    private lateinit var database: ChatDatabase
    private lateinit var messageDao: MessageDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, ChatDatabase::class.java).build()
        messageDao = database.messageDao
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun testInsertAndReadMessage() = runBlocking {
        val message = MessageEntity(
            messageId = "test-id",
            senderId = "sender-1",
            senderName = "Sender",
            content = "Unit Test Message",
            timestamp = System.currentTimeMillis(),
            type = 1,
            isFromLocalUser = true,
            status = 1
        )
        messageDao.insertMessage(message)
        
        messageDao.getAllMessages().test {
            val list = awaitItem()
            assertEquals(1, list.size)
            assertEquals(message.content, list[0].content)
        }
    }

    @Test
    fun testPurgeOldMessages() = runBlocking {
        val now = System.currentTimeMillis()
        val oldMessage = MessageEntity(
            messageId = "old-id",
            senderId = "sender-1",
            senderName = "Sender",
            content = "Old Message",
            timestamp = now - (24 * 60 * 60 * 1000), // 24h ago
            type = 1,
            isFromLocalUser = true,
            status = 1
        )
        val newMessage = MessageEntity(
            messageId = "new-id",
            senderId = "sender-1",
            senderName = "Sender",
            content = "New Message",
            timestamp = now,
            type = 1,
            isFromLocalUser = true,
            status = 1
        )
        
        messageDao.insertMessage(oldMessage)
        messageDao.insertMessage(newMessage)

        val threshold = now - (12 * 60 * 60 * 1000) // 12h ago
        messageDao.deleteOldMessages(threshold)

        messageDao.getAllMessages().test {
            val list = awaitItem()
            assertEquals(1, list.size)
            assertEquals("new-id", list[0].messageId)
        }
    }
}
