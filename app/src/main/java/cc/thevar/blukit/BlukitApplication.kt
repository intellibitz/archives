package cc.thevar.blukit

import android.app.Application
import cc.thevar.blukit.di.appModule
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

/**
 * Blukit Application class — entry point for global system initialization.
 */
class BlukitApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@BlukitApplication)
            modules(appModule)
        }
    }
}
