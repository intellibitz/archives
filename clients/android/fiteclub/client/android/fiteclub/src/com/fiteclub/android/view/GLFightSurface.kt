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

import android.content.Context
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import java.util.ArrayList
import java.util.concurrent.Semaphore
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGL11
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLContext
import javax.microedition.khronos.egl.EGLDisplay
import javax.microedition.khronos.egl.EGLSurface
import javax.microedition.khronos.opengles.GL
import javax.microedition.khronos.opengles.GL10

/**
 * An implementation of SurfaceView that uses the dedicated surface for
 * displaying an OpenGL animation.  This allows the animation to run in a
 * separate thread, without requiring that it be driven by the update mechanism
 * of the view hierarchy.
 *
 * The application-specific rendering code is delegated to a GLView.Renderer
 * instance.
 */
class GLFightSurface : SurfaceView, SurfaceHolder.Callback {

    companion object {
        private val sEglSemaphore = Semaphore(1)
    }

    private var mSizeChanged = true

    private lateinit var mHolder: SurfaceHolder
    private var mGLThread: GLThread? = null
    private var mGLWrapper: GLWrapper? = null

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    private fun init() {
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed
        mHolder = holder
        mHolder.addCallback(this)
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_GPU)
    }

    fun setGLWrapper(glWrapper: GLWrapper?) {
        mGLWrapper = glWrapper
    }

    fun setRenderer(renderer: Renderer) {
        mGLThread = GLThread(renderer)
        mGLThread!!.start()
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        mGLThread!!.surfaceCreated()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        // Surface will be destroyed when we return
        mGLThread!!.surfaceDestroyed()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, w: Int, h: Int) {
        // Surface size or format has changed. This should not happen in this
        // example.
        mGLThread!!.onWindowResize(w, h)
    }

    /**
     * Inform the view that the activity is paused.
     */
    fun onPause() {
        mGLThread!!.onPause()
    }

    /**
     * Inform the view that the activity is resumed.
     */
    fun onResume() {
        mGLThread!!.onResume()
    }

    /**
     * Inform the view that the window focus has changed.
     */
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        mGLThread!!.onWindowFocusChanged(hasFocus)
    }

    /**
     * Queue an "event" to be run on the GL rendering thread.
     * @param r the runnable to be run on the GL rendering thread.
     */
    fun queueEvent(r: Runnable) {
        mGLThread!!.queueEvent(r)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mGLThread!!.requestExitAndWait()
    }

    // ----------------------------------------------------------------------

    interface GLWrapper {
        fun wrap(gl: GL): GL
    }

    // ----------------------------------------------------------------------

    /**
     * A generic renderer interface.
     */
    interface Renderer {
        /**
         * @return the EGL configuration specification desired by the renderer.
         */
        fun getConfigSpec(): IntArray

        /**
         * Surface created.
         * Called when the surface is created. Called when the application
         * starts, and whenever the GPU is reinitialized. This will
         * typically happen when the device awakes after going to sleep.
         * Set your textures here.
         */
        fun surfaceCreated(gl: GL10)

        /**
         * Surface changed size.
         * Called after the surface is created and whenever
         * the OpenGL ES surface size changes. Set your viewport here.
         * @param gl
         * @param width
         * @param height
         */
        fun sizeChanged(gl: GL10, width: Int, height: Int)

        /**
         * Draw the current frame.
         * @param gl
         */
        fun drawFrame(gl: GL10)
    }

    /**
     * An EGL helper class.
     */
    private inner class EglHelper {
        var mEgl: EGL10? = null
        var mEglDisplay: EGLDisplay? = null
        var mEglSurface: EGLSurface? = null
        var mEglConfig: EGLConfig? = null
        var mEglContext: EGLContext? = null

        /**
         * Initialize EGL for a given configuration spec.
         * @param configSpec
         */
        fun start(configSpec: IntArray) {
            /*
             * Get an EGL instance
             */
            mEgl = EGLContext.getEGL() as EGL10

            /*
             * Get to the default display.
             */
            mEglDisplay = mEgl!!.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY)

            /*
             * We can now initialize EGL for that display
             */
            val version = IntArray(2)
            mEgl!!.eglInitialize(mEglDisplay, version)

            val configs = arrayOfNulls<EGLConfig>(1)
            val num_config = IntArray(1)
            mEgl!!.eglChooseConfig(
                mEglDisplay, configSpec, configs, 1,
                num_config
            )
            mEglConfig = configs[0]

            /*
            * Create an OpenGL ES context. This must be done only once, an
            * OpenGL context is a somewhat heavy object.
            */
            mEglContext = mEgl!!.eglCreateContext(
                mEglDisplay, mEglConfig,
                EGL10.EGL_NO_CONTEXT, null
            )

            mEglSurface = null
        }

        /*
         * Create and return an OpenGL surface
         */
        fun createSurface(holder: SurfaceHolder): GL {
            /*
             *  The window size has changed, so we need to create a new
             *  surface.
             */
            if (mEglSurface != null) {
                /*
                 * Unbind and destroy the old EGL surface, if
                 * there is one.
                 */
                mEgl!!.eglMakeCurrent(
                    mEglDisplay, EGL10.EGL_NO_SURFACE,
                    EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT
                )
                mEgl!!.eglDestroySurface(mEglDisplay, mEglSurface)
            }

            /*
             * Create an EGL surface we can render into.
             */
            mEglSurface = mEgl!!.eglCreateWindowSurface(
                mEglDisplay,
                mEglConfig, holder, null
            )

            /*
             * Before we can issue GL commands, we need to make sure
             * the context is current and bound to a surface.
             */
            mEgl!!.eglMakeCurrent(
                mEglDisplay, mEglSurface, mEglSurface,
                mEglContext
            )

            var gl = mEglContext!!.gl
            if (mGLWrapper != null) {
                gl = mGLWrapper!!.wrap(gl)
            }
            return gl
        }

        /**
         * Display the current render surface.
         * @return false if the context has been lost.
         */
        fun swap(): Boolean {
            mEgl!!.eglSwapBuffers(mEglDisplay, mEglSurface)

            /*
             * Always check for EGL_CONTEXT_LOST, which means the context
             * and all associated data were lost (For instance because
             * the device went to sleep). We need to sleep until we
             * get a new surface.
             */
            return mEgl!!.eglGetError() != EGL11.EGL_CONTEXT_LOST
        }

        fun finish() {
            if (mEglSurface != null) {
                mEgl!!.eglMakeCurrent(
                    mEglDisplay, EGL10.EGL_NO_SURFACE,
                    EGL10.EGL_NO_SURFACE,
                    EGL10.EGL_NO_CONTEXT
                )
                mEgl!!.eglDestroySurface(mEglDisplay, mEglSurface)
                mEglSurface = null
            }
            if (mEglContext != null) {
                mEgl!!.eglDestroyContext(mEglDisplay, mEglContext)
                mEglContext = null
            }
            if (mEglDisplay != null) {
                mEgl!!.eglTerminate(mEglDisplay)
                mEglDisplay = null
            }
        }
    }

    /**
     * A generic GL Thread. Takes care of initializing EGL and GL. Delegates
     * to a Renderer instance to do the actual drawing.
     *
     */
    inner class GLThread(renderer: Renderer) : Thread() {
        private var mDone: Boolean = false
        private var mPaused: Boolean = false
        private var mHasFocus: Boolean = false
        private var mHasSurface: Boolean = false
        private var mContextLost: Boolean = false
        private var mWidth: Int = 0
        private var mHeight: Int = 0
        private var mRenderer: Renderer = renderer
        private val mEventQueue: ArrayList<Runnable> = ArrayList()
        private var mEglHelper: EglHelper? = null

        init {
            mDone = false
            mWidth = 0
            mHeight = 0
            name = "GLThread"
        }

        override fun run() {
            /*
             * When the android framework launches a second instance of
             * an activity, the new instance's onCreate() method may be
             * called before the first instance returns from onDestroy().
             *
             * This semaphore ensures that only one instance at a time
             * accesses EGL.
             */
            try {
                try {
                    sEglSemaphore.acquire()
                } catch (e: InterruptedException) {
                    return
                }
                guardedRun()
            } catch (e: InterruptedException) {
                // fall thru and exit normally
            } finally {
                sEglSemaphore.release()
            }
        }

        @Throws(InterruptedException::class)
        private fun guardedRun() {
            mEglHelper = EglHelper()
            /*
             * Specify a configuration for our opengl session
             * and grab the first configuration that matches is
             */
            val configSpec = mRenderer.getConfigSpec()
            mEglHelper!!.start(configSpec)

            var gl: GL10? = null
            var tellRendererSurfaceCreated = true
            var tellRendererSurfaceChanged = true

            /*
             * This is our main activity thread's loop, we go until
             * asked to quit.
             */
            while (!mDone) {
                /*
                 *  Update the asynchronous state (window size)
                 */
                var w: Int
                var h: Int
                var changed: Boolean
                var needStart = false
                synchronized(this) {
                    var r: Runnable?
                    while (true) {
                        r = getEvent()
                        if (r == null) break
                        r!!.run()
                    }
                    if (mPaused) {
                        mEglHelper!!.finish()
                        needStart = true
                    }
                    if (needToWait()) {
                        while (needToWait()) {
                            (this as Object).wait()
                        }
                    }
                    if (mDone) {
                        break
                    }
                    changed = mSizeChanged
                    w = mWidth
                    h = mHeight
                    mSizeChanged = false
                }
                if (needStart) {
                    mEglHelper!!.start(configSpec)
                    tellRendererSurfaceCreated = true
                    changed = true
                }
                if (changed) {
                    gl = mEglHelper!!.createSurface(mHolder) as GL10
                    tellRendererSurfaceChanged = true
                }
                if (tellRendererSurfaceCreated) {
                    mRenderer.surfaceCreated(gl!!)
                    tellRendererSurfaceCreated = false
                }
                if (tellRendererSurfaceChanged) {
                    mRenderer.sizeChanged(gl!!, w, h)
                    tellRendererSurfaceChanged = false
                }
                if ((w > 0) && (h > 0)) {
                    /* draw a frame here */
                    mRenderer.drawFrame(gl!!)

                    /*
                     * Once we're done with GL, we need to call swapBuffers()
                     * to instruct the system to display the rendered frame
                     */
                    mEglHelper!!.swap()
                }
            }

            /*
             * clean-up everything...
             */
            mEglHelper!!.finish()
        }

        private fun needToWait(): Boolean {
            return (mPaused || (!mHasFocus) || (!mHasSurface) || mContextLost) && (!mDone)
        }

        fun surfaceCreated() {
            synchronized(this) {
                mHasSurface = true
                mContextLost = false
                (this as Object).notify()
            }
        }

        fun surfaceDestroyed() {
            synchronized(this) {
                mHasSurface = false
                (this as Object).notify()
            }
        }

        fun onPause() {
            synchronized(this) {
                mPaused = true
            }
        }

        fun onResume() {
            synchronized(this) {
                mPaused = false
                (this as Object).notify()
            }
        }

        fun onWindowFocusChanged(hasFocus: Boolean) {
            synchronized(this) {
                mHasFocus = hasFocus
                if (mHasFocus == true) {
                    (this as Object).notify()
                }
            }
        }

        fun onWindowResize(w: Int, h: Int) {
            synchronized(this) {
                mWidth = w
                mHeight = h
                mSizeChanged = true
            }
        }

        fun requestExitAndWait() {
            // don't call this from GLThread thread or it is a guaranteed
            // deadlock!
            synchronized(this) {
                mDone = true
                (this as Object).notify()
            }
            try {
                join()
            } catch (ex: InterruptedException) {
                Thread.currentThread().interrupt()
            }
        }

        /**
         * Queue an "event" to be run on the GL rendering thread.
         * @param r the runnable to be run on the GL rendering thread.
         */
        fun queueEvent(r: Runnable) {
            synchronized(this) {
                mEventQueue.add(r)
            }
        }

        private fun getEvent(): Runnable? {
            synchronized(this) {
                if (mEventQueue.size > 0) {
                    return mEventQueue.removeAt(0)
                }
            }
            return null
        }
    }
}
