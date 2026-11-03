package cc.thevar.blukit

import android.app.Application

class TestBlukitApplication : Application() {
    override fun onCreate() {
        // Do not initialize WorkManager or other real hardware services here
    }
}
