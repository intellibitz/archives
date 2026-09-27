package intellibitz.intellidroid

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SampleActivityUnitTestCase : ActivityTestRule<MainActivity>(
    MainActivity::class.java) {
    @Before
    fun setUp() {

        // Injecting the Instrumentation instance is required
        // for your test to run with AndroidJUnitRunner.
//        injectInstrumentation(InstrumentationRegistry.getInstrumentation());
        // Starts the activity under test using the default Intent with:
        // action = {@link Intent#ACTION_MAIN}
        // flags = {@link Intent#FLAG_ACTIVITY_NEW_TASK}
        // All other fields are null or empty.
//        mTestActivity = getActivity();
//        super.setUp();
    }

    /**
     * Add more tests below.
     */
    @Test
    fun addition_isCorrect() {
        Assert.assertEquals(4, 2 + 2.toLong())
        // Injecting the Instrumentation instance is required
        // for your test to run with AndroidJUnitRunner.
//        injectInstrumentation(InstrumentationRegistry.getInstrumentation());
        //Try to add a message to add context to your assertions. These messages will be shown if
        //a tests fails and make it easy to understand why a test failed
        Assert.assertNotNull("mTestActivity is null", InstrumentationRegistry.getInstrumentation())
    }
}