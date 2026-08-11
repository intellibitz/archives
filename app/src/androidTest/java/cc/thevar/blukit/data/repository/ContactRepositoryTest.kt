package cc.thevar.blukit.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import cc.thevar.blukit.data.local.ChatDatabase
import cc.thevar.blukit.data.local.entities.ContactEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ContactRepositoryTest {

    private lateinit var database: ChatDatabase
    private lateinit var repository: ContactRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, ChatDatabase::class.java).build()
        repository = ContactRepository(database.contactDao)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testSaveAndGetContact() = runBlocking {
        val contact = ContactEntity(
            contactId = "peer-1",
            name = "Peer One",
            bluetoothAddress = "address-1",
            lastSeen = System.currentTimeMillis(),
            avatarUri = "🐱"
        )
        
        repository.saveContact(contact)
        
        val retrieved = repository.getContact("peer-1")
        assertEquals(contact, retrieved)
    }

    @Test
    fun testGetAllContacts() = runBlocking {
        val c1 = ContactEntity("1", "A", "addr-1", 100, "👤")
        val c2 = ContactEntity("2", "B", "addr-2", 200, "👤")
        
        repository.saveContact(c1)
        repository.saveContact(c2)
        
        repository.allContacts.test {
            val list = awaitItem()
            assertEquals(2, list.size)
        }
    }
}
