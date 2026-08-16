package cc.thevar.blukit

import android.app.Application
import cc.thevar.blukit.data.crypto.CryptoManager
import cc.thevar.blukit.data.local.VibeStore
import cc.thevar.blukit.data.power.SupremePowerManager
import cc.thevar.blukit.data.repository.ContactRepository
import cc.thevar.blukit.data.repository.IdentityRepository
import cc.thevar.blukit.data.repository.IdentityRepositoryImpl
import cc.thevar.blukit.data.system.HapticManager
import cc.thevar.blukit.data.system.RadioStateManager
import cc.thevar.blukit.network.p2p.NearbyP2PController
import cc.thevar.blukit.network.p2p.P2PController
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.hours

/**
 * Blukit Application class — entry point for global system initialization.
 */
class BlukitApplication : Application() {

    lateinit var identityRepository: IdentityRepository
    lateinit var contactRepository: ContactRepository
    lateinit var p2pController: P2PController
    lateinit var radioStateManager: RadioStateManager
    private lateinit var _spreadPermissionManager: cc.thevar.blukit.data.system.SpreadPermissionManager
    val spreadPermissionManager: cc.thevar.blukit.data.system.SpreadPermissionManager get() = _spreadPermissionManager
    lateinit var supremePowerManager: SupremePowerManager
    lateinit var vibeStore: VibeStore
    lateinit var hapticManager: HapticManager
    lateinit var cryptoManager: CryptoManager
    
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        
        cryptoManager = CryptoManager()
        vibeStore = VibeStore(this, cryptoManager)
        identityRepository = IdentityRepositoryImpl(this)
        contactRepository = ContactRepository(vibeStore) 
        radioStateManager = RadioStateManager(this)
        _spreadPermissionManager = cc.thevar.blukit.data.system.SpreadPermissionManager(this)
        hapticManager = HapticManager(this)
        
        p2pController = NearbyP2PController(
            context = this,
            repository = identityRepository,
            contactRepository = contactRepository,
            vibeStore = vibeStore,
            hapticManager = hapticManager,
            cryptoManager = cryptoManager,
            ioDispatcher = Dispatchers.IO
        )
        
        supremePowerManager = SupremePowerManager(
            p2pController = p2pController,
            vibeStore = vibeStore,
            hapticManager = hapticManager
        )

        start12HourPurge()
    }

    /**
     * Minimalist 12-hour purge logic using a simple coroutine loop.
     * Replaces WorkManager for ephemeral data retention.
     */
    private fun start12HourPurge() {
        applicationScope.launch {
            while (isActive) {
                val threshold = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(12)
                vibeStore.deleteOldMessages(threshold)
                delay(12.hours)
            }
        }
    }
}
