package cc.thevar.blukit.wear

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

val wearModule = module {
    // Placeholder for wear dependencies
}

class BlukitWearApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@BlukitWearApplication)
            modules(wearModule)
        }
    }
}
