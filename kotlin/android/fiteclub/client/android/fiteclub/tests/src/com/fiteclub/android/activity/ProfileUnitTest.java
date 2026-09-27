package com.fiteclub.android.activity;

/*
<!--
    Copyright (C) 2008 http://mobeegal.in

       All Rights Reserved.

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
-->
*/
/*
<!--
$Id::                                                                           $: Id of last commit
$Rev::                                                                          $: Revision of last commit
$Author::                                                                       $: Author of last commit
$Date::                                                                         $: Date of last commit
-->
*/

import android.test.ActivityUnitTestCase;
import android.util.Log;
import android.os.Bundle;
import android.content.Intent;
import com.fiteclub.android.R;

/**
 * This is a simple framework for a test of an Application.  See
 * {@link android.test.ApplicationTestCase ApplicationTestCase} for more information on
 * how to write and extend Application tests.
 * <p/>
 * To run this test, you can type:
 * adb shell am instrument -w \
 * -e class com.fiteclub.android.FiteClubApplicationTest \
 * com.fiteclub.android.tests/android.test.InstrumentationTestRunner
 */
public class ProfileUnitTest extends ActivityUnitTestCase<Profile> {

    public ProfileUnitTest() {
        super(Profile.class);
    }

    public void testProfile (){
        Log.i("ProfileUnitTest", "testProfile");
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setType("vnd.fiteclub.cursor.dir/vnd.fiteclub.profile");
        startActivity(intent, null, null);
    }
}