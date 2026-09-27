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
import com.fiteclub.android.view.*;

import javax.microedition.khronos.opengles.GL;

public class GLFight extends FiteClubActivity {

    /**
     * A handle to the View in which the game is running.
     */
    private GLFightSurface mFightView;


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
        setContentView(R.layout.glfight);

        // get handles to the LunarView from XML, and its LunarThread
        mFightView = (GLFightSurface) findViewById(R.id.fightview);
        mFightView.setGLWrapper(new GLFightSurface.GLWrapper() {
            public GL wrap(GL gl) {
                return new MatrixTrackingGL(gl);
            }});
        mFightView.setRenderer(new FightRenderer(this));
        mFightView.requestFocus();

    }

    @Override
    protected void onPause() {
        super.onPause();
        mFightView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFightView.onResume();
    }


}