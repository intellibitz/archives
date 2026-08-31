package cc.thevar.blukit

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cc.thevar.blukit.data.repository.IdentityRepository
import cc.thevar.blukit.domain.logic.AutonomousManager
import cc.thevar.blukit.ui.BlukitApp
import cc.thevar.blukit.ui.theme.BlukitTheme
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val identityRepository: IdentityRepository by inject()
    private val autonomousManager: AutonomousManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        setContent {
            val isStealthMode by identityRepository.stealthMode.collectAsStateWithLifecycle(initialValue = false)
            
            BlukitTheme(stealthMode = isStealthMode) {
                BlukitApp()
            }
        }
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        autonomousManager.onUserActivity()
    }

    override fun onDestroy() {
        super.onDestroy()
        autonomousManager.stop()
    }
}
