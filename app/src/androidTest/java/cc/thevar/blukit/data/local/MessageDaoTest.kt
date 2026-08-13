package cc.thevar.blukit.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import cc.thevar.blukit.data.local.dao.MessageDao
import cc.thevar.blukit.data.local.entities.MessageEntity
import cc.thevar.blukit.domain.model.MessagePayload
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class MessageDaoTest {

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
    fun writeAndReadMessage() = runTest {
        val message = MessageEntity(
            messageId = "msg1",
            senderId = "user1",
            senderName = "User 1",
            content = "Hello Mesh",
            timestamp = 123456789L,
            type = MessagePayload.TYPE_TEXT,
            isFromLocalUser = false,
            status = MessagePayload.STATUS_SENT
        )
        messageDao.insertMessage(message)
        
        messageDao.getAllMessages().test {
            val list = awaitItem()
            assertEquals(1, list.size)
            assertEquals("Hello Mesh", list[0].content)
        }
    }

    @Test
    fun clearMessages() = runTest {
        val message = MessageEntity(
            "id1", "s1", "n1", null, null, "c1", 1L,
            MessagePayload.TYPE_TEXT, false, MessagePayload.STATUS_SENT
        )
        messageDao.insertMessage(message)
        messageDao.clearAllMessages()
        
        messageDao.getAllMessages().test {
            assertTrue(awaitItem().isEmpty())
        }
    }

    @Test
    fun updateStatus() = runTest {
        val message = MessageEntity(
            "id1", "s1", "n1", null, null, "c1", 1L,
            MessagePayload.TYPE_TEXT, false, MessagePayload.STATUS_PENDING
        )
        messageDao.insertMessage(message)
        messageDao.updateMessageStatus("id1", MessagePayload.STATUS_DELIVERED)
        
        messageDao.getAllMessages().test {
            assertEquals(MessagePayload.STATUS_DELIVERED, awaitItem()[0].status)
        }
    }
}
