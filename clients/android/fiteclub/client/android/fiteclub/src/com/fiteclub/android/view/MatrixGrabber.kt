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

import javax.microedition.khronos.opengles.GL10

class MatrixGrabber {
    @JvmField
    var mModelView: FloatArray
    @JvmField
    var mProjection: FloatArray

    init {
        mModelView = FloatArray(16)
        mProjection = FloatArray(16)
    }

    /**
     * Record the current modelView and projection matrix state.
     * Has the side effect of setting the current matrix state to GL_MODELVIEW
     * @param gl
     */
    fun getCurrentState(gl: GL10) {
        getCurrentProjection(gl)
        getCurrentModelView(gl)
    }

    /**
     * Record the current modelView matrix state. Has the side effect of
     * setting the current matrix state to GL_MODELVIEW
     * @param gl
     */
    fun getCurrentModelView(gl: GL10) {
        getMatrix(gl, GL10.GL_MODELVIEW, mModelView)
    }

    /**
     * Record the current projection matrix state. Has the side effect of
     * setting the current matrix state to GL_PROJECTION
     * @param gl
     */
    fun getCurrentProjection(gl: GL10) {
        getMatrix(gl, GL10.GL_PROJECTION, mProjection)
    }

    private fun getMatrix(gl: GL10, mode: Int, mat: FloatArray) {
        val gl2 = gl as MatrixTrackingGL
        gl2.glMatrixMode(mode)
        gl2.getMatrix(mat, 0)
    }
}
