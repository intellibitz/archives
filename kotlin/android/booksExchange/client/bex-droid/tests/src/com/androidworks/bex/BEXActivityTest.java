package com.androidrocks.bex;

import android.test.ActivityInstrumentationTestCase;

/**
 * This is a simple framework for a test of an Application.  See
 * {@link android.test.ApplicationTestCase ApplicationTestCase} for more information on
 * how to write and extend Application tests.
 * <p/>
 * To run this test, you can type:
 * adb shell am instrument -w \
 * -e class com.androidrocks.bex.BEXActivityTest \
 * com.androidrocks.bex.tests/android.test.InstrumentationTestRunner
 */
public class BEXActivityTest extends ActivityInstrumentationTestCase<BEXActivity> {

    public BEXActivityTest() {
        super("com.androidrocks.bex", BEXActivity.class);
    }

}
