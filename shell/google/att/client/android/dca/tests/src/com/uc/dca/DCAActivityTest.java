package com.uc.dca;

import android.test.ActivityInstrumentationTestCase;

/**
 * This is a simple framework for a test of an Application.  See
 * {@link android.test.ApplicationTestCase ApplicationTestCase} for more information on
 * how to write and extend Application tests.
 * <p/>
 * To run this test, you can type:
 * adb shell am instrument -w \
 * -e class com.uc.dca.DCAActivityTest \
 * com.uc.dca.tests/android.test.InstrumentationTestRunner
 */
public class DCAActivityTest extends ActivityInstrumentationTestCase<DCAActivity> {

    public DCAActivityTest() {
        super("com.uc.dca", DCAActivity.class);
    }

}
