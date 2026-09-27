/*
* Copyright (C) 2013 The Android Open Source Project
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
package intellibitz.intellidroid

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests for Instrumentation.
 */
@RunWith(AndroidJUnit4::class)
class SampleActivityInstrumentationTestCase2 :
    ActivityTestRule<MainActivity>(
        MainActivity::class.java) {
    private var mTestActivity: MainActivity? = null

    @Before
    fun setUp() {
        // Injecting the Instrumentation instance is required
        // for your test to run with AndroidJUnitRunner.
//        injectInstrumentation(InstrumentationRegistry.getInstrumentation());
        // Starts the activity under test using the default Intent with:
        // action = {@link Intent#ACTION_MAIN}
        // flags = {@link Intent#FLAG_ACTIVITY_NEW_TASK}
        // All other fields are null or empty.
//        super.setUp();
        mTestActivity = activity
    }

    /**
     * Add more tests below.
     */
    @Test
    fun addition_isCorrect() {
        Assert.assertEquals(4, 2 + 2.toLong())
        //Try to add a message to add context to your assertions. These messages will be shown if
        //a tests fails and make it easy to understand why a test failed
        mTestActivity = activity
        Assert.assertNotNull("mTestActivity is null", mTestActivity)
    }

    @Test
    fun testMe() {
        Assert.assertTrue(true)
    }
}