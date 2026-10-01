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
import com.fiteclub.android.R
import com.fiteclub.android.view.FightRenderer
import com.fiteclub.android.view.GLFightSurface
import com.fiteclub.android.view.MatrixTrackingGL
import javax.microedition.khronos.opengles.GL

class GLFight : FiteClubActivity() {

    /**
     * A handle to the View in which the game is running.
     */
    private var mFightView: GLFightSurface? = null

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
        setContentView(R.layout.glfight)

        // get handles to the LunarView from XML, and its LunarThread
        mFightView = findViewById(R.id.fightview) as GLFightSurface
        mFightView!!.setGLWrapper(object : GLFightSurface.GLWrapper {
            override fun wrap(gl: GL): GL {
                return MatrixTrackingGL(gl)
            }
        })
        mFightView!!.setRenderer(FightRenderer(this))
        mFightView!!.requestFocus()
    }

    override fun onPause() {
        super.onPause()
        mFightView!!.onPause()
    }

    override fun onResume() {
        super.onResume()
        mFightView!!.onResume()
    }
}
