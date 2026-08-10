package cc.thevar.blukit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.room.Room
import cc.thevar.blukit.data.IdentityRepository
import cc.thevar.blukit.data.system.RadioStateManager
import cc.thevar.blukit.data.local.ChatDatabase
import cc.thevar.blukit.data.networking.NearbyP2PController
import cc.thevar.blukit.ui.BlukitApp
import cc.thevar.blukit.ui.theme.BlukitTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {
    
    // Simple repository instance for the foundation
    private val repository by lazy {
        IdentityRepository(applicationContext)
    }

    private val radioStateManager by lazy {
        RadioStateManager(applicationContext)
    }

    private val database by lazy {
        Room.databaseBuilder(
            applicationContext,
            ChatDatabase::class.java,
            "chat.db"
        ).build()
    }
    
    private val p2pController by lazy {
        NearbyP2PController(applicationContext, repository, database.messageDao)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isStealthMode by repository.stealthMode.collectAsStateWithLifecycle(initialValue = false)
            
            BlukitTheme(stealthMode = isStealthMode) {
                BlukitApp(
                    repository = repository,
                    radioStateManager = radioStateManager,
                    p2pController = p2pController
                )
            }
        }
    }
}
