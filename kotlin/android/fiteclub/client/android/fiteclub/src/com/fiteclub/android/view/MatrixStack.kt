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
import java.nio.FloatBuffer
import java.nio.IntBuffer

/**
 * A matrix stack, similar to OpenGL ES's internal matrix stack.
 */
class MatrixStack {
    companion object {
        private const val DEFAULT_MAX_DEPTH = 32
        private const val MATRIX_SIZE = 16
    }

    private lateinit var mMatrix: FloatArray
    private var mTop: Int = 0
    private lateinit var mTemp: FloatArray

    constructor() {
        commonInit(DEFAULT_MAX_DEPTH)
    }

    constructor(maxDepth: Int) {
        commonInit(maxDepth)
    }

    private fun commonInit(maxDepth: Int) {
        mMatrix = FloatArray(maxDepth * MATRIX_SIZE)
        mTemp = FloatArray(MATRIX_SIZE * 2)
        glLoadIdentity()
    }

    fun glFrustumf(
        left: Float, right: Float, bottom: Float, top: Float,
        near: Float, far: Float
    ) {
        Matrix.frustumM(mMatrix, mTop, left, right, bottom, top, near, far)
    }

    fun glFrustumx(
        left: Int, right: Int, bottom: Int, top: Int, near: Int,
        far: Int
    ) {
        glFrustumf(
            fixedToFloat(left), fixedToFloat(right),
            fixedToFloat(bottom), fixedToFloat(top),
            fixedToFloat(near), fixedToFloat(far)
        )
    }

    fun glLoadIdentity() {
        Matrix.setIdentityM(mMatrix, mTop)
    }

    fun glLoadMatrixf(m: FloatArray, offset: Int) {
        System.arraycopy(m, offset, mMatrix, mTop, MATRIX_SIZE)
    }

    fun glLoadMatrixf(m: FloatBuffer) {
        m.get(mMatrix, mTop, MATRIX_SIZE)
    }

    fun glLoadMatrixx(m: IntArray, offset: Int) {
        for (i in 0 until MATRIX_SIZE) {
            mMatrix[mTop + i] = fixedToFloat(m[offset + i])
        }
    }

    fun glLoadMatrixx(m: IntBuffer) {
        for (i in 0 until MATRIX_SIZE) {
            mMatrix[mTop + i] = fixedToFloat(m.get())
        }
    }

    fun glMultMatrixf(m: FloatArray, offset: Int) {
        System.arraycopy(mMatrix, mTop, mTemp, 0, MATRIX_SIZE)
        Matrix.multiplyMM(mMatrix, mTop, mTemp, 0, m, offset)
    }

    fun glMultMatrixf(m: FloatBuffer) {
        m.get(mTemp, MATRIX_SIZE, MATRIX_SIZE)
        glMultMatrixf(mTemp, MATRIX_SIZE)
    }

    fun glMultMatrixx(m: IntArray, offset: Int) {
        for (i in 0 until MATRIX_SIZE) {
            mTemp[MATRIX_SIZE + i] = fixedToFloat(m[offset + i])
        }
        glMultMatrixf(mTemp, MATRIX_SIZE)
    }

    fun glMultMatrixx(m: IntBuffer) {
        for (i in 0 until MATRIX_SIZE) {
            mTemp[MATRIX_SIZE + i] = fixedToFloat(m.get())
        }
        glMultMatrixf(mTemp, MATRIX_SIZE)
    }

    fun glOrthof(
        left: Float, right: Float, bottom: Float, top: Float,
        near: Float, far: Float
    ) {
        Matrix.orthoM(mMatrix, mTop, left, right, bottom, top, near, far)
    }

    fun glOrthox(
        left: Int, right: Int, bottom: Int, top: Int, near: Int,
        far: Int
    ) {
        glOrthof(
            fixedToFloat(left), fixedToFloat(right),
            fixedToFloat(bottom), fixedToFloat(top),
            fixedToFloat(near), fixedToFloat(far)
        )
    }

    fun glPopMatrix() {
        preflight_adjust(-1)
        adjust(-1)
    }

    fun glPushMatrix() {
        preflight_adjust(1)
        System.arraycopy(
            mMatrix, mTop, mMatrix, mTop + MATRIX_SIZE,
            MATRIX_SIZE
        )
        adjust(1)
    }

    fun glRotatef(angle: Float, x: Float, y: Float, z: Float) {
        Matrix.setRotateM(mTemp, 0, angle, x, y, z)
        System.arraycopy(mMatrix, mTop, mTemp, MATRIX_SIZE, MATRIX_SIZE)
        Matrix.multiplyMM(mMatrix, mTop, mTemp, MATRIX_SIZE, mTemp, 0)
    }

    fun glRotatex(angle: Int, x: Int, y: Int, z: Int) {
        glRotatef(angle.toFloat(), fixedToFloat(x), fixedToFloat(y), fixedToFloat(z))
    }

    fun glScalef(x: Float, y: Float, z: Float) {
        Matrix.scaleM(mMatrix, mTop, x, y, z)
    }

    fun glScalex(x: Int, y: Int, z: Int) {
        glScalef(fixedToFloat(x), fixedToFloat(y), fixedToFloat(z))
    }

    fun glTranslatef(x: Float, y: Float, z: Float) {
        Matrix.translateM(mMatrix, mTop, x, y, z)
    }

    fun glTranslatex(x: Int, y: Int, z: Int) {
        glTranslatef(fixedToFloat(x), fixedToFloat(y), fixedToFloat(z))
    }

    fun getMatrix(dest: FloatArray, offset: Int) {
        System.arraycopy(mMatrix, mTop, dest, offset, MATRIX_SIZE)
    }

    private fun fixedToFloat(x: Int): Float {
        return x * (1.0f / 65536.0f)
    }

    private fun preflight_adjust(dir: Int) {
        val newTop = mTop + dir * MATRIX_SIZE
        if (newTop < 0) {
            throw IllegalArgumentException("stack underflow")
        }
        if (newTop + MATRIX_SIZE > mMatrix.size) {
            throw IllegalArgumentException("stack overflow")
        }
    }

    private fun adjust(dir: Int) {
        mTop += dir * MATRIX_SIZE
    }
}
