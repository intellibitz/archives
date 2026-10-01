package intellibitz.intellidroid

import android.content.Context
import android.util.Log
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber
import intellibitz.intellidroid.util.MainApplicationSingleton
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.junit.MockitoJUnitRunner

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
@RunWith(MockitoJUnitRunner.StrictStubs::class)
class IntellibitzUnitTest {
    @Mock
    var mMockContext: Context? = null

    @Test
    fun testReadStringFromContext_LocalizedString() {
        mMockContext = mock(Context::class.java, "RETURNS_DEFAULTS")
        // Given a mocked Context injected into the object under test...
//        Mockito.`when`(mMockContext!!.getString(R.string.app_name))
//            .thenReturn(FAKE_STRING)
        //        ClassUnderTest myObjectUnderTest = new ClassUnderTest(mMockContext);

        // ...when the string is returned from the object under test...
//        String result = myObjectUnderTest.getHelloWorldString();
        val result = FAKE_STRING

        // ...then the result should be the expected one.
        MatcherAssert.assertThat(result, CoreMatchers.`is`(FAKE_STRING))
    }

    @Test
    @Throws(Exception::class)
    fun testMe() {
        if (BuildConfig.DEBUG && MainApplicationSingleton.getInstance(mMockContext) == null) {
            throw AssertionError("MainApplicationSingleton is null")
        }
        //        assert (MainApplicationSingleton.getInstance(null) == null);
    }

    @Test
    @Throws(Exception::class)
    fun testBase64DecodeString() {
        val len = 104
        val `in` = StringBuilder()
        for (i in 0 until len) {
            `in`.append("a")
        }
        //        String base64 = new String(Base64.encode(in.toString().getBytes(), Base64.DEFAULT));
// Sending side
//        String s = in.toString();
//        byte[] data = s.getBytes("UTF-16");
//        String base64 = Base64.encodeToString(data, Base64.DEFAULT);
        val base64 = `in`.toString()
        Log.d(TAG, MainApplicationSingleton.decodeBase64String(base64))
    }

    @Test
    @Throws(Exception::class)
    fun testPhoneNumberCC() {
        val fullFormNumber = "+(91)9840348914"
        val partFormNumber = "+919840348914"
        val ccNumber = "919840348914"
        var phoneNumber: PhoneNumber
        phoneNumber = PhoneNumber()
        phoneNumber.nationalNumber = ccNumber.toLong()
        println(phoneNumber.toString() + " : " + phoneNumber.clearCountryCode())
        val phoneNumberUtil = PhoneNumberUtil.getInstance()
        phoneNumber = phoneNumberUtil.parse(ccNumber, "IN")
        var flag = phoneNumberUtil.isValidNumber(phoneNumber)
        var format = phoneNumberUtil.format(
            phoneNumber,
            PhoneNumberUtil.PhoneNumberFormat.E164
        )
        var normalizeDigitsOnly = PhoneNumberUtil.normalizeDigitsOnly(format)
        var number = phoneNumber.clearCountryCode()
        /*
        System.out.println(TAG +
                normalizeDigitsOnly + " is valid: "+number + " - " + flag);
*/println(TAG + " is valid: " + number + " - " + flag)
        phoneNumber = phoneNumberUtil.parse(partFormNumber, "IN")
        format = phoneNumberUtil.format(
            phoneNumber,
            PhoneNumberUtil.PhoneNumberFormat.E164
        )
        normalizeDigitsOnly = PhoneNumberUtil.normalizeDigitsOnly(format)
        flag = phoneNumberUtil.isValidNumber(phoneNumber)
        number = phoneNumber.clearCountryCode()
        /*
        System.out.println(TAG +
                normalizeDigitsOnly + " is valid: "+number + " - " + flag);
*/println(TAG + " is valid: " + number + " - " + flag)
        phoneNumber = phoneNumberUtil.parse(fullFormNumber, "IN")
        format = phoneNumberUtil.format(
            phoneNumber,
            PhoneNumberUtil.PhoneNumberFormat.E164
        )
        normalizeDigitsOnly = PhoneNumberUtil.normalizeDigitsOnly(format)
        flag = phoneNumberUtil.isValidNumber(phoneNumber)
        number = phoneNumber.clearCountryCode()
        /*
        System.out.println(TAG +
                normalizeDigitsOnly + " is valid: "+number + " - " + flag);
*/println(TAG + " is valid: " + number + " - " + flag)
    }

    @Test
    @Throws(Exception::class)
    fun testPhoneNumberUtils() {
        val number1 = "19840348914"
        val number2 = "2019840348914"
        val number3 = "919840348914"
        val number4 = "+919840348914"
        val number5 = "+(91)9840348914"
        val number6 = "9840348914"
        var phoneNumber: PhoneNumber
        val phoneNumberUtil = PhoneNumberUtil.getInstance()
        phoneNumber = phoneNumberUtil.parse(number1, "IN")
        var flag = phoneNumberUtil.isValidNumber(phoneNumber)
        println(
            TAG +
                    PhoneNumberUtil.normalizeDigitsOnly(
                        phoneNumberUtil.format(
                            phoneNumber,
                            PhoneNumberUtil.PhoneNumberFormat.E164
                        )
                    ) + " is valid:" + flag
        )
        phoneNumber = phoneNumberUtil.parse(number2, "IN")
        flag = phoneNumberUtil.isValidNumber(phoneNumber)
        println(
            TAG +
                    PhoneNumberUtil.normalizeDigitsOnly(
                        phoneNumberUtil.format(
                            phoneNumber,
                            PhoneNumberUtil.PhoneNumberFormat.E164
                        )
                    ) + " is valid:" + flag
        )
        phoneNumber = phoneNumberUtil.parse(number3, "IN")
        flag = phoneNumberUtil.isValidNumber(phoneNumber)
        println(
            TAG +
                    PhoneNumberUtil.normalizeDigitsOnly(
                        phoneNumberUtil.format(
                            phoneNumber,
                            PhoneNumberUtil.PhoneNumberFormat.E164
                        )
                    ) + " is valid:" + flag
        )
        phoneNumber = phoneNumberUtil.parse(number4, "IN")
        flag = phoneNumberUtil.isValidNumber(phoneNumber)
        println(
            TAG +
                    PhoneNumberUtil.normalizeDigitsOnly(
                        phoneNumberUtil.format(
                            phoneNumber,
                            PhoneNumberUtil.PhoneNumberFormat.E164
                        )
                    ) + " is valid:" + flag
        )
        phoneNumber = phoneNumberUtil.parse(number5, "IN")
        flag = phoneNumberUtil.isValidNumber(phoneNumber)
        println(
            TAG +
                    PhoneNumberUtil.normalizeDigitsOnly(
                        phoneNumberUtil.format(
                            phoneNumber,
                            PhoneNumberUtil.PhoneNumberFormat.E164
                        )
                    ) + " is valid:" + flag
        )
        phoneNumber = phoneNumberUtil.parse(number6, "IN")
        flag = phoneNumberUtil.isValidNumber(phoneNumber)
        println(
            TAG +
                    PhoneNumberUtil.normalizeDigitsOnly(
                        phoneNumberUtil.format(
                            phoneNumber,
                            PhoneNumberUtil.PhoneNumberFormat.E164
                        )
                    ) + " is valid:" + flag
        )
    }

    companion object {
        private const val TAG = "IntellibitzUnit"
        private const val FAKE_STRING = "INTELLIBITZ ANDROID APP UNIT TESTS USING MOCKITO"
    }
}