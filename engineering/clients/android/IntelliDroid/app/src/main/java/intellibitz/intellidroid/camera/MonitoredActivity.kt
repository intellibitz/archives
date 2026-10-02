/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package intellibitz.intellidroid.camera

import android.os.Bundle

class MonitoredActivity : NoSearchActivity() {

    private val mListeners = ArrayList<LifeCycleListener>()

    fun addLifeCycleListener(listener: LifeCycleListener) {
        if (mListeners.contains(listener)) return
        mListeners.add(listener)
    }

    fun removeLifeCycleListener(listener: LifeCycleListener) {
        mListeners.remove(listener)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        for (listener in mListeners) {
            listener.onActivityCreated(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        for (listener in mListeners) {
            listener.onActivityDestroyed(this)
        }
    }

    override fun onStart() {
        super.onStart()
        for (listener in mListeners) {
            listener.onActivityStarted(this)
        }
    }

    override fun onStop() {
        super.onStop()
        for (listener in mListeners) {
            listener.onActivityStopped(this)
        }
    }

    interface LifeCycleListener {
        fun onActivityCreated(activity: MonitoredActivity)

        fun onActivityDestroyed(activity: MonitoredActivity)

        fun onActivityStarted(activity: MonitoredActivity)

        fun onActivityStopped(activity: MonitoredActivity)
    }

    class LifeCycleAdapter : LifeCycleListener {
        override fun onActivityCreated(activity: MonitoredActivity) {
        }

        override fun onActivityDestroyed(activity: MonitoredActivity) {
        }

        override fun onActivityStarted(activity: MonitoredActivity) {
        }

        override fun onActivityStopped(activity: MonitoredActivity) {
        }
    }
}
