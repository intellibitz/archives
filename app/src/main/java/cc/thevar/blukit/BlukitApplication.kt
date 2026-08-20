package cc.thevar.blukit

import android.app.Application
import cc.thevar.blukit.data.local.VibeStore
import cc.thevar.blukit.di.appModule
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.hours

/**
 * Blukit Application class — entry point for global system initialization.
 */
class BlukitApplication : Application() {

    private val vibeStore: VibeStore by inject()
    private val applicationScope: CoroutineScope by inject()

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@BlukitApplication)
            modules(appModule)
        }

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
