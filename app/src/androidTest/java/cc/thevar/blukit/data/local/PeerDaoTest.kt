package cc.thevar.blukit.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import cc.thevar.blukit.data.local.dao.PeerDao
import cc.thevar.blukit.data.local.entities.PeerEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class PeerDaoTest {

    private lateinit var database: ChatDatabase
    private lateinit var peerDao: PeerDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, ChatDatabase::class.java).build()
        peerDao = database.peerDao
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun insertAndGetPeer() = runTest {
        val peer = PeerEntity("id1", "Name 1", "key1", 1000L)
        peerDao.insertPeer(peer)
        
        val retrieved = peerDao.getPeer("id1")
        assertEquals("Name 1", retrieved?.name)
        assertEquals("key1", retrieved?.publicKey)
    }

    @Test
    fun clearPeers() = runTest {
        val peer = PeerEntity("id1", "Name 1", "key1", 1000L)
        peerDao.insertPeer(peer)
        peerDao.clearPeers()
        
        val retrieved = peerDao.getPeer("id1")
        assertNull(retrieved)
    }
}
