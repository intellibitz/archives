package intellibitz.intellidroid

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ServiceTestRule
import intellibitz.intellidroid.service.ChatService
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeoutException

@RunWith(AndroidJUnit4::class)
class SampleServiceTestCase : ServiceTestRule() {
    @Rule
    val mServiceRule = ServiceTestRule()
    fun setUp() {
//        setContext(InstrumentationRegistry.getInstrumentation().getTargetContext());
//        super.setUp();
    }

    @Test
    @Throws(TimeoutException::class)
    fun testWithBoundService() {
        // Create the service Intent.
        val serviceIntent = Intent(
            InstrumentationRegistry.getInstrumentation().targetContext,
            service.ChatService::class.java
        )

        // Data can be passed to the service via the Intent.
        serviceIntent.putExtra("key", 42L)

//        the binder must be implemented on the service for the following to work
        // Bind the service and grab a reference to the binder.
        val binder = mServiceRule.bindService(serviceIntent)
        Assert.assertNotNull(binder)

        // Get the reference to the service, or you can call
        // public methods on the binder directly.
//        ChatService service = (binder).getService();

        // Verify that the service is working correctly.
//        assertNotNull(service);
//        assertThat(service.getRandomInt(), is(any(Integer.class)));
    }
}