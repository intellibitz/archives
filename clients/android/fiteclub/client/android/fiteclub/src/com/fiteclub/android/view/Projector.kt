package com.fiteclub.android.view

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

import android.opengl.Matrix
import javax.microedition.khronos.opengles.GL10

/**
 * A utility that projects
 *
 */
class Projector {
    private val mGrabber: MatrixGrabber
    private var mMVPComputed: Boolean = false
    private val mMVP: FloatArray
    private val mV: FloatArray
    private var mX: Int = 0
    private var mY: Int = 0
    private var mViewWidth: Int = 0
    private var mViewHeight: Int = 0

    init {
        mMVP = FloatArray(16)
        mV = FloatArray(4)
        mGrabber = MatrixGrabber()
    }

    fun setCurrentView(x: Int, y: Int, width: Int, height: Int) {
        mX = x
        mY = y
        mViewWidth = width
        mViewHeight = height
    }

    fun project(obj: FloatArray, objOffset: Int, win: FloatArray, winOffset: Int) {
        if (!mMVPComputed) {
            Matrix.multiplyMM(mMVP, 0, mGrabber.mProjection, 0, mGrabber.mModelView, 0)
            mMVPComputed = true
        }

        Matrix.multiplyMV(mV, 0, mMVP, 0, obj, objOffset)

        val rw = 1.0f / mV[3]

        win[winOffset] = mX + mViewWidth * (mV[0] * rw + 1.0f) * 0.5f
        win[winOffset + 1] = mY + mViewHeight * (mV[1] * rw + 1.0f) * 0.5f
        win[winOffset + 2] = (mV[2] * rw + 1.0f) * 0.5f
    }

    /**
     * Get the current projection matrix. Has the side-effect of
     * setting current matrix mode to GL_PROJECTION
     * @param gl
     */
    fun getCurrentProjection(gl: GL10) {
        mGrabber.getCurrentProjection(gl)
        mMVPComputed = false
    }

    /**
     * Get the current model view matrix. Has the side-effect of
     * setting current matrix mode to GL_MODELVIEW
     * @param gl
     */
    fun getCurrentModelView(gl: GL10) {
        mGrabber.getCurrentModelView(gl)
        mMVPComputed = false
    }
}
