package cc.thevar.blukit.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import cc.thevar.blukit.data.local.dao.ContactDao
import cc.thevar.blukit.data.local.entities.ContactEntity
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
class ContactDaoTest {

    private lateinit var database: ChatDatabase
    private lateinit var contactDao: ContactDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, ChatDatabase::class.java).build()
        contactDao = database.contactDao
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun insertAndListContacts() = runTest {
        val contact = ContactEntity("id1", "Alice", "bt:123", 1000L, "emoji1")
        contactDao.insertContact(contact)
        
        contactDao.getAllContacts().test {
            val list = awaitItem()
            assertEquals(1, list.size)
            assertEquals("Alice", list[0].name)
        }
    }

    @Test
    fun deleteContact() = runTest {
        val contact = ContactEntity("id1", "Alice", "bt:123", 1000L, "emoji1")
        contactDao.insertContact(contact)
        contactDao.deleteContact(contact)
        
        contactDao.getAllContacts().test {
            assertTrue(awaitItem().isEmpty())
        }
    }
}
