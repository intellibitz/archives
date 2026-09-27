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

import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import com.fiteclub.android.R;
import com.fiteclub.android.view.FightSurface;
import com.fiteclub.android.view.FightThread;

public class Fight extends FiteClubActivity {

    /**
     * A handle to the View in which the game is running.
     */
    private FightSurface mFightView;
    /**
     * A handle to the thread that's actually running the animation.
     */
    private FightThread mFightThread;


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Hide the window title.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.fight);
        
        // get handles to the LunarView from XML, and its LunarThread
        mFightView = (FightSurface) findViewById(R.id.fightview);
        mFightThread = mFightView.getThread();

        // give the LunarView a handle to the TextView used for messages
        mFightView.setTextView((TextView) findViewById(R.id.text));

        if (savedInstanceState == null) {
            // we were just launched: set up a new game
            mFightThread.setState(FightThread.STATE_READY);
        } else {
            // we are being restored: resume a previous game
            mFightThread.restoreState(savedInstanceState);
        }
    }

    /**
     * Invoked when the Activity loses user focus.
     */
    @Override
    protected void onPause() {
        super.onPause();
        mFightView.getThread().pause(); // pause game when Activity pauses
    }

    /**
     * Notification that something is about to happen, to give the Activity a
     * chance to save state.
     *
     * @param outState a Bundle into which this Activity should save its state
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // just have the View's thread save its state into our Bundle
        super.onSaveInstanceState(outState);
        mFightThread.saveState(outState);
    }

}
