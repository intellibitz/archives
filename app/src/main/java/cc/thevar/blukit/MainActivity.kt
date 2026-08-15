package cc.thevar.blukit

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cc.thevar.blukit.ui.BlukitApp
import cc.thevar.blukit.ui.theme.BlukitTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        setContent {
            val app = application as BlukitApplication
            val isStealthMode by app.identityRepository.stealthMode.collectAsStateWithLifecycle(initialValue = false)
            
            BlukitTheme(stealthMode = isStealthMode) {
                BlukitApp(
                    repository = app.identityRepository,
                    contactRepository = app.contactRepository,
                    messageDao = app.database.messageDao,
                    radioStateManager = app.radioStateManager,
                    p2pController = app.p2pController,
                    supremePowerManager = app.supremePowerManager,
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
