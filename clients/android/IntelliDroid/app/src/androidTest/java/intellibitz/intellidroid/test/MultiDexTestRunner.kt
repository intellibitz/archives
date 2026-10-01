package intellibitz.intellidroid.test

import android.os.Bundle
import androidx.multidex.MultiDex
import androidx.test.runner.AndroidJUnitRunner

//https://developer.android.com/studio/build/multidex?hl=en#kotlin
//Don't use MultiDexTestRunner, which is deprecated; use AndroidJUnitRunner instead.
class MultiDexTestRunner : AndroidJUnitRunner() {
    override fun onCreate(arguments: Bundle) {
//When you write instrumentation tests for multidex apps, no additional configuration is required
// if you use a MonitoringInstrumentation (or an AndroidJUnitRunner) instrumentation. If you use
// another Instrumentation, then you must override its onCreate() method with the following code:
        MultiDex.install(targetContext)
        super.onCreate(arguments)
    }
}