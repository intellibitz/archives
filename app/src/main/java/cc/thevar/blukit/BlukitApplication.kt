package cc.thevar.blukit

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import cc.thevar.blukit.data.crypto.CryptoManager
import cc.thevar.blukit.data.local.ChatDatabase
import cc.thevar.blukit.data.power.SupremePowerManager
import cc.thevar.blukit.data.repository.ContactRepository
import cc.thevar.blukit.data.repository.IdentityRepository
import cc.thevar.blukit.data.repository.IdentityRepositoryImpl
import cc.thevar.blukit.data.system.HapticManager
import cc.thevar.blukit.data.system.RadioStateManager
import cc.thevar.blukit.data.worker.PurgeWorker
import cc.thevar.blukit.network.p2p.NearbyP2PController
import cc.thevar.blukit.network.p2p.P2PController
import kotlinx.coroutines.Dispatchers
import java.util.concurrent.TimeUnit

/**
 * Blukit Application class — entry point for global system initialization.
 */
class BlukitApplication : Application() {

    lateinit var identityRepository: IdentityRepository
    lateinit var contactRepository: ContactRepository
    lateinit var p2pController: P2PController
    lateinit var radioStateManager: RadioStateManager
    lateinit var supremePowerManager: SupremePowerManager
    lateinit var database: ChatDatabase
    lateinit var hapticManager: HapticManager
    lateinit var cryptoManager: CryptoManager

    override fun onCreate() {
        super.onCreate()
        
        database = ChatDatabase.getInstance(this)
        identityRepository = IdentityRepositoryImpl(this)
        contactRepository = ContactRepository(database.contactDao)
        radioStateManager = RadioStateManager(this)
        hapticManager = HapticManager(this)
        cryptoManager = CryptoManager()
        
        p2pController = NearbyP2PController(
            context = this,
            repository = identityRepository,
            contactRepository = contactRepository,
            messageDao = database.messageDao,
            peerDao = database.peerDao,
            hapticManager = hapticManager,
            cryptoManager = cryptoManager,
            ioDispatcher = Dispatchers.IO
        )
        
        supremePowerManager = SupremePowerManager(
            p2pController = p2pController,
            messageDao = database.messageDao,
            hapticManager = hapticManager
        )

        setupPurgeWorker()
    }

    /**
     * Schedules a periodic background worker to enforce ephemeral data retention policies.
     * Chat logs are purged every 12 hours to maintain "Vibing Persistence" and ensure user privacy.
     */
    private fun setupPurgeWorker() {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .setRequiresDeviceIdle(true)
            .build()

        val purgeRequest = PeriodicWorkRequestBuilder<PurgeWorker>(
            12, TimeUnit.HOURS
        ).setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "purge_messages",
            ExistingPeriodicWorkPolicy.KEEP,
            purgeRequest
        )
    }
}
