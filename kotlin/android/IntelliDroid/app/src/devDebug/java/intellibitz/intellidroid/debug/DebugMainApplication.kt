package intellibitz.intellidroid.debug

import com.facebook.stetho.Stetho
import intellibitz.intellidroid.MainApplication

class DebugMainApplication : MainApplication() {
    override fun onCreate() {
        super.onCreate()
        Stetho.initializeWithDefaults(this)
    }
}
