package com.androidworks.navsys.wuffit.activity

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WuffITTrackerTest {

    @Test
    fun testActivityLaunch() {
        val scenario = ActivityScenario.launch(WuffITTracker::class.java)
        scenario.onActivity { activity ->
            assertNotNull(activity)
        }
        scenario.close()
    }
}
