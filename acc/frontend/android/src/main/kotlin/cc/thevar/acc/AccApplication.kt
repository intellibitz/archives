package cc.thevar.acc

import android.app.Application
import cc.thevar.acc.di.commonModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class AccApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@AccApplication)
            modules(commonModule())
        }
    }
}
