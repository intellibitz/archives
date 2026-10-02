package com.uc.dca

import android.test.ActivityInstrumentationTestCase

/**
 * This is a simple framework for a test of an Application.  See
 * [android.test.ApplicationTestCase] for more information on
 * how to write and extend Application tests.
 *
 *
 * To run this test, you can type:
 * adb shell am instrument -w \
 * -e class com.uc.dca.DCAActivityTest \
 * com.uc.dca.tests/android.test.InstrumentationTestRunner
 */
class DCAActivityTest : ActivityInstrumentationTestCase<DCAActivity>("com.uc.dca", DCAActivity::class.java)
