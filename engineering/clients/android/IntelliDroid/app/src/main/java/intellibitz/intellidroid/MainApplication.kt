package intellibitz.intellidroid

import android.content.Context
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication

/**
 * //https://developer.android.com/studio/build/multidex
 * //If your minSdkVersion is set to 21 or higher, multidex is enabled by default and
 * //you do not need the multidex support library.
 * // Enabling multidex support. (only for sdk 20 or lower)
 */
open class MainApplication : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
    }

    override fun attachBaseContext(base: Context) {
//        if multidex super will take care of multidex install
        super.attachBaseContext(base)
        //        if the application does not extend MultiDex, then install here
        if (!MultiDexApplication::class.java.isInstance(this)) {
            MultiDex.install(this)
        }
    }
}