package com.androidworks.navsys.wuffit

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WuffITApplicationTest {

    @Test
    fun testAppContext() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertNotNull(appContext)
    }
}
