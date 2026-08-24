package cc.thevar.blukit.data.local

import android.content.Context
import app.cash.turbine.test
import cc.thevar.blukit.data.crypto.CryptoManager
import cc.thevar.blukit.domain.model.VibeGroup
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.After
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class VibeStoreTest {

    private lateinit var context: Context
    private val cryptoManager: CryptoManager = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var vibeStore: VibeStore

    @Before
    fun setUp() {
        context = mockk(relaxed = true)
        val tempDir = File.createTempFile("vibe", "store").parentFile
        every { context.filesDir } returns tempDir
        
        // Mock crypto to return input as is for local storage simulation
        every { cryptoManager.encryptLocal(any()) } answers { it.invocation.args[0] as ByteArray }
        every { cryptoManager.decryptLocal(any()) } answers { it.invocation.args[0] as ByteArray }

        vibeStore = VibeStore(context, cryptoManager, testDispatcher)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `test auto archiving logic`() = runTest(testDispatcher) {
        val oldTimestamp = System.currentTimeMillis() - (VibeGroup.ARCHIVE_THRESHOLD_MS + 1000)
        val recentTimestamp = System.currentTimeMillis()

        val oldGroup = VibeGroup(id = "old", name = "Old Group", lastVibeTimestamp = oldTimestamp)
        val recentGroup = VibeGroup(id = "recent", name = "Recent Group", lastVibeTimestamp = recentTimestamp)

        vibeStore.insertGroup(oldGroup)
        vibeStore.insertGroup(recentGroup)
        runCurrent()

        vibeStore.activeGroups.test {
            // Skip initial and after insertions
            var active = awaitItem()
            while (active.size < 4) { active = awaitItem() }
            
            vibeStore.autoArchiveAirs()
            runCurrent()
            
            active = awaitItem()
            while (active.any { it.id == "old" }) { active = awaitItem() }
            
            assertTrue(active.none { it.id == "old" })
            assertTrue(active.any { it.id == "recent" })
        }
    }

    @Test
    fun `test restore from vault`() = runTest(testDispatcher) {
        val oldGroup = VibeGroup(id = "to-archive", name = "To Archive", lastVibeTimestamp = 0)
        vibeStore.insertGroup(oldGroup)
        runCurrent()
        
        vibeStore.autoArchiveAirs()
        runCurrent()
        
        vibeStore.archivedGroups.test {
            // 1. Wait for "to-archive" to appear in archived
            var archived = awaitItem()
            while (archived.none { it.id == "to-archive" }) {
                archived = awaitItem()
            }
            
            // 2. Restore it
            vibeStore.restoreFromVault("to-archive")
            
            // 3. Wait for it to disappear
            archived = awaitItem()
            while (archived.any { it.id == "to-archive" }) {
                archived = awaitItem()
            }
            
            assertTrue("Archived should not contain restored group", archived.none { it.id == "to-archive" })
        }
    }
}
