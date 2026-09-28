package com.fiteclub.android.activity

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

import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import com.fiteclub.android.R
import com.fiteclub.android.view.FightSurface
import com.fiteclub.android.view.FightThread

class Fight : FiteClubActivity() {

    /**
     * A handle to the View in which the game is running.
     */
    private var mFightView: FightSurface? = null
    /**
     * A handle to the thread that's actually running the animation.
     */
    private var mFightThread: FightThread? = null

    /**
     * Called when the activity is first created.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Hide the window title.
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.fight)

        // get handles to the LunarView from XML, and its LunarThread
        mFightView = findViewById(R.id.fightview) as FightSurface
        mFightThread = mFightView!!.thread

        // give the LunarView a handle to the TextView used for messages
        mFightView!!.setTextView(findViewById(R.id.text) as TextView)

        if (savedInstanceState == null) {
            // we were just launched: set up a new game
            mFightThread!!.setState(FightThread.STATE_READY)
        } else {
            // we are being restored: resume a previous game
            mFightThread!!.restoreState(savedInstanceState)
        }
    }

    /**
     * Invoked when the Activity loses user focus.
     */
    override fun onPause() {
        super.onPause()
        mFightView!!.thread.pause() // pause game when Activity pauses
    }

    /**
     * Notification that something is about to happen, to give the Activity a
     * chance to save state.
     *
     * @param outState a Bundle into which this Activity should save its state
     */
    override fun onSaveInstanceState(outState: Bundle) {
        // just have the View's thread save its state into our Bundle
        super.onSaveInstanceState(outState)
        mFightThread!!.saveState(outState)
    }
}
