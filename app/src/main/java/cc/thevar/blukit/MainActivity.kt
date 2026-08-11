package cc.thevar.blukit

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cc.thevar.blukit.data.repository.IdentityRepository
import cc.thevar.blukit.data.repository.ContactRepository
import cc.thevar.blukit.data.system.HapticManager
import cc.thevar.blukit.data.system.RadioStateManager
import cc.thevar.blukit.data.local.ChatDatabase
import cc.thevar.blukit.network.p2p.NearbyP2PController
import cc.thevar.blukit.ui.BlukitApp
import cc.thevar.blukit.ui.theme.BlukitTheme

class MainActivity : ComponentActivity() {
    
    private val repository by lazy {
        IdentityRepository(applicationContext)
    }

    private val radioStateManager by lazy {
        RadioStateManager(applicationContext)
    }

    private val hapticManager by lazy {
        HapticManager(applicationContext)
    }

    private val database by lazy {
        ChatDatabase.getInstance(applicationContext)
    }

    private val contactRepository by lazy {
        ContactRepository(database.contactDao)
    }
    
    private val p2pController by lazy {
        NearbyP2PController(
            applicationContext,
            repository,
            contactRepository,
            database.messageDao,
            database.peerDao,
            hapticManager
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        setContent {
            val isStealthMode by repository.stealthMode.collectAsStateWithLifecycle(initialValue = false)
            
            BlukitTheme(stealthMode = isStealthMode) {
                BlukitApp(
                    repository = repository,
                    contactRepository = contactRepository,
                    messageDao = database.messageDao,
                    radioStateManager = radioStateManager,
                    p2pController = p2pController,
                    onEnterPip = {
                        enterPictureInPictureMode(
                            android.app.PictureInPictureParams.Builder().build()
                        )
                    }
                )
            }
        }
    }
}
