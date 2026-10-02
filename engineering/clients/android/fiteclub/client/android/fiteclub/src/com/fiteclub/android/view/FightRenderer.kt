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

import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.opengl.GLU
import android.opengl.GLUtils
import android.os.SystemClock
import com.fiteclub.android.R

import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.opengles.GL10

class FightRenderer : GLFightSurface.Renderer {

    constructor(context: Context) {
        mContext = context
        mTriangle = Triangle()
        mProjector = Projector()
        mLabelPaint = Paint()
        mLabelPaint.textSize = 32f
        mLabelPaint.isAntiAlias = true
        mLabelPaint.setARGB(0xff, 0x00, 0x00, 0x00)
    }

    override fun getConfigSpec(): IntArray {
        // We don't need a depth buffer, and don't care about our
        // color depth.
        val configSpec = intArrayOf(EGL10.EGL_DEPTH_SIZE, 0,
                EGL10.EGL_NONE)
        return configSpec
    }

    override fun surfaceCreated(gl: GL10) {
        /*
         * By default, OpenGL enables features that improve quality
         * but reduce performance. One might want to tweak that
         * especially on software renderer.
         */
        gl.glDisable(GL10.GL_DITHER)

        /*
         * Some one-time OpenGL initialization can be made here
         * probably based on features of this particular context
         */
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT,
                GL10.GL_FASTEST)

        gl.glClearColor(.5f, .5f, .5f, 1)
        gl.glShadeModel(GL10.GL_SMOOTH)
        gl.glEnable(GL10.GL_DEPTH_TEST)
        gl.glEnable(GL10.GL_TEXTURE_2D)

        /*
         * Create our texture. This has to be done each time the
         * surface is created.
         */

        val textures = IntArray(1)
        gl.glGenTextures(1, textures, 0)

        mTextureID = textures[0]
        gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureID)

        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,
                GL10.GL_NEAREST)
        gl.glTexParameterf(GL10.GL_TEXTURE_2D,
                GL10.GL_TEXTURE_MAG_FILTER,
                GL10.GL_LINEAR)

        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S,
                GL10.GL_CLAMP_TO_EDGE)
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T,
                GL10.GL_CLAMP_TO_EDGE)

        gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE,
                GL10.GL_REPLACE)

        val inputStream = mContext.resources
                .openRawResource(R.drawable.tex)
        var bitmap: Bitmap? = null
        try {
            bitmap = BitmapFactory.decodeStream(inputStream)
        } finally {
            try {
                inputStream.close()
            } catch (e: IOException) {
                // Ignore.
            }
        }

        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0)
        bitmap!!.recycle()

        if (mLabels != null) {
            mLabels!!.shutdown(gl)
        } else {
            mLabels = LabelMaker(true, 256, 64)
        }
        mLabels!!.initialize(gl)
        mLabels!!.beginAdding(gl)
        mLabelA = mLabels!!.add(gl, "A", mLabelPaint)
        mLabelB = mLabels!!.add(gl, "B", mLabelPaint)
        mLabelC = mLabels!!.add(gl, "C", mLabelPaint)
        mLabelMsPF = mLabels!!.add(gl, "ms/f", mLabelPaint)
        mLabels!!.endAdding(gl)

        if (mNumericSprite != null) {
            mNumericSprite!!.shutdown(gl)
        } else {
            mNumericSprite = NumericSprite()
        }
        mNumericSprite!!.initialize(gl, mLabelPaint)
    }

    override fun drawFrame(gl: GL10) {
        /*
         * By default, OpenGL enables features that improve quality
         * but reduce performance. One might want to tweak that
         * especially on software renderer.
         */
        gl.glDisable(GL10.GL_DITHER)

        gl.glTexEnvx(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE,
                GL10.GL_MODULATE)

        /*
         * Usually, the first thing one might want to do is to clear
         * the screen. The most efficient way of doing this is to use
         * glClear().
         */

        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT)

        /*
         * Now we're ready to draw some 3D objects
         */

        gl.glMatrixMode(GL10.GL_MODELVIEW)
        gl.glLoadIdentity()

        GLU.gluLookAt(gl, 0.0f, 0.0f, -2.5f,
                0.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 0.0f)

        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY)
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY)

        gl.glActiveTexture(GL10.GL_TEXTURE0)
        gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureID)
        gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S,
                GL10.GL_REPEAT)
        gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T,
                GL10.GL_REPEAT)

        var time: Long = SystemClock.uptimeMillis() % 4000L
        val angle: Float = 0.090f * time.toInt()

        gl.glRotatef(angle, 0, 0, 1.0f)
        gl.glScalef(2.0f, 2.0f, 2.0f)

        mTriangle.draw(gl)

        mProjector.getCurrentModelView(gl)
        mLabels!!.beginDrawing(gl, mWidth, mHeight)
        drawLabel(gl, 0, mLabelA)
        drawLabel(gl, 1, mLabelB)
        drawLabel(gl, 2, mLabelC)
        val msPFX: Float = mWidth - mLabels!!.getWidth(mLabelMsPF) - 1
        mLabels!!.draw(gl, msPFX, 0, mLabelMsPF)
        mLabels!!.endDrawing(gl)

        drawMsPF(gl, msPFX)
    }

    private fun drawMsPF(gl: GL10, rightMargin: Float) {
        val time: Long = SystemClock.uptimeMillis()
        if (mStartTime == 0L) {
            mStartTime = time
        }
        if (mFrames++ == SAMPLE_PERIOD_FRAMES) {
            mFrames = 0
            val delta: Long = time - mStartTime
            mStartTime = time
            mMsPerFrame = (delta * SAMPLE_FACTOR).toInt()
        }
        if (mMsPerFrame > 0) {
            mNumericSprite!!.setValue(mMsPerFrame)
            val numWidth: Float = mNumericSprite!!.width()
            val x: Float = rightMargin - numWidth
            mNumericSprite!!.draw(gl, x, 0, mWidth, mHeight)
        }
    }

    private fun drawLabel(gl: GL10, triangleVertex: Int, labelId: Int) {
        val x: Float = mTriangle.getX(triangleVertex)
        val y: Float = mTriangle.getY(triangleVertex)
        mScratch[0] = x
        mScratch[1] = y
        mScratch[2] = 0.0f
        mScratch[3] = 1.0f
        mProjector.project(mScratch, 0, mScratch, 4)
        val sx: Float = mScratch[4]
        val sy: Float = mScratch[5]
        val height: Float = mLabels!!.getHeight(labelId)
        val width: Float = mLabels!!.getWidth(labelId)
        val tx: Float = sx - width * 0.5f
        val ty: Float = sy - height * 0.5f
        mLabels!!.draw(gl, tx, ty, labelId)
    }

    override fun sizeChanged(gl: GL10, w: Int, h: Int) {
        mWidth = w
        mHeight = h
        gl.glViewport(0, 0, w, h)
        mProjector.setCurrentView(0, 0, w, h)

        /*
        * Set our projection matrix. This doesn't have to be done
        * each time we draw, but usually a projection needs to
        * be set when the viewport is resized.
        */

        val ratio: Float = w.toFloat() / h
        gl.glMatrixMode(GL10.GL_PROJECTION)
        gl.glLoadIdentity()
        gl.glFrustumf(-ratio, ratio, -1, 1, 1, 10)
        mProjector.getCurrentProjection(gl)
    }

    private var mWidth: Int = 0
    private var mHeight: Int = 0
    private lateinit var mContext: Context
    private lateinit var mTriangle: Triangle
    private var mTextureID: Int = 0
    private var mFrames: Int = 0
    private var mMsPerFrame: Int = 0
    companion object {
        private const val SAMPLE_PERIOD_FRAMES = 12
        private const val SAMPLE_FACTOR = 1.0f / SAMPLE_PERIOD_FRAMES
    }
    private var mStartTime: Long = 0L
    private var mLabels: LabelMaker? = null
    private lateinit var mLabelPaint: Paint
    private var mLabelA: Int = 0
    private var mLabelB: Int = 0
    private var mLabelC: Int = 0
    private var mLabelMsPF: Int = 0
    private lateinit var mProjector: Projector
    private var mNumericSprite: NumericSprite? = null
    private var mScratch: FloatArray = FloatArray(8)
}

class Triangle {
    constructor() {

        // Buffers to be passed to gl*Pointer() functions
        // must be direct, i.e., they must be placed on the
        // native heap where the garbage collector cannot
        // move them.
        //
        // Buffers with multi-byte datatypes (e.g., short, int, float)
        // must have their byte order set to native order

        var vbb: ByteBuffer = ByteBuffer.allocateDirect(VERTS * 3 * 4)
        vbb.order(ByteOrder.nativeOrder())
        mFVertexBuffer = vbb.asFloatBuffer()

        var tbb: ByteBuffer = ByteBuffer.allocateDirect(VERTS * 2 * 4)
        tbb.order(ByteOrder.nativeOrder())
        mTexBuffer = tbb.asFloatBuffer()

        var ibb: ByteBuffer = ByteBuffer.allocateDirect(VERTS * 2)
        ibb.order(ByteOrder.nativeOrder())
        mIndexBuffer = ibb.asShortBuffer()

        for (i in 0 until VERTS) {
            for (j in 0 until 3) {
                mFVertexBuffer.put(sCoords[i*3+j])
            }
        }

        for (i in 0 until VERTS) {
            for (j in 0 until 2) {
                mTexBuffer.put(sCoords[i*3+j] * 2.0f + 0.5f)
            }
        }

        for (i in 0 until VERTS) {
            mIndexBuffer.put(i.toShort())
        }

        mFVertexBuffer.position(0)
        mTexBuffer.position(0)
        mIndexBuffer.position(0)
    }

    fun draw(gl: GL10) {
        gl.glFrontFace(GL10.GL_CCW)
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mFVertexBuffer)
        gl.glEnable(GL10.GL_TEXTURE_2D)
        gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mTexBuffer)
        gl.glDrawElements(
            GL10.GL_TRIANGLE_STRIP, VERTS,
            GL10.GL_UNSIGNED_SHORT, mIndexBuffer
        )
    }

    fun getX(vertex: Int): Float {
        return sCoords[3 * vertex]
    }

    fun getY(vertex: Int): Float {
        return sCoords[3 * vertex + 1]
    }

    companion object {
        private const val VERTS = 3

        // A unit-sided equalateral triangle centered on the origin.
        private val sCoords = floatArrayOf(
            // X, Y, Z
            -0.5f, -0.25f, 0f,
            0.5f, -0.25f, 0f,
            0.0f, 0.559016994f, 0f
        )
    }

    private lateinit var mFVertexBuffer: FloatBuffer
    private lateinit var mTexBuffer: FloatBuffer
    private lateinit var mIndexBuffer: ShortBuffer
}
