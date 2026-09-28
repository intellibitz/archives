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
import android.hardware.SensorListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.TextView

/**
 * The SurfaceView used for the Game
 */
class FightSurface : SurfaceView, SurfaceHolder.Callback {

    /**
     * Pointer to the text view to display "Paused.." etc.
     */
    private var mStatusText: TextView? = null
    /**
     * The thread that actually draws the animation
     */
    private lateinit var mThread: FightThread

    // sensor manager used to control the accelerometer sensor.
    private var mSensorManager: SensorManager? = null
    // http://code.google.com/android/reference/android/hardware/SensorManager.html#SENSOR_ACCELEROMETER
    // for an explanation on the values reported by SENSOR_ACCELEROMETER.
    private val mSensorAccelerometer: SensorListener = object : SensorListener {
        // method called whenever new sensor values are reported.
        override fun onSensorChanged(sensor: Int, values: FloatArray) {
            mThread.onSensorChanged(sensor, values)
        }

        override fun onAccuracyChanged(sensor: Int, accuracy: Int) {
            // currently not used
        }
    }

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attributeSet: AttributeSet?) : super(context, attributeSet) {
        init(context)
    }

    constructor(context: Context, attributeSet: AttributeSet?, i: Int) : super(context, attributeSet, i) {
        init(context)
    }

    private fun init(context: Context) {
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        // register our interest in hearing about changes to our surface
        val holder = holder
        holder.addCallback(this)

        // create thread only; it's started in surfaceCreated()
        mThread = FightThread(holder, context, object : Handler() {
            override fun handleMessage(m: Message) {
                mStatusText!!.visibility = m.data.getInt("viz")
                mStatusText!!.text = m.data.getString("text")
            }
        })

        isFocusable = true // make sure we get key events

        // setup accelerometer sensor manager.
        mSensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        // register our accelerometer so we can receive values.
        // SENSOR_DELAY_GAME is the recommended rate for games
        mSensorManager!!.registerListener(
            mSensorAccelerometer, SensorManager.SENSOR_ACCELEROMETER,
            SensorManager.SENSOR_DELAY_GAME
        )
    }

    override fun onTouchEvent(motionEvent: MotionEvent): Boolean {
        return mThread.onTouchEvent(motionEvent)
    }

    override fun onTrackballEvent(motionEvent: MotionEvent): Boolean {
        return mThread.onTrackballEvent(motionEvent)
    }

    /**
     * Standard override to get key-press events.
     */
    override fun onKeyDown(keyCode: Int, msg: KeyEvent): Boolean {
        // quit application if user presses the back key.
        if (keyCode == KeyEvent.KEYCODE_BACK)
            unregisterListener()
//        return thread.doKeyDown(keyCode, msg);
        return super.onKeyDown(keyCode, msg)
    }

    /**
     * Standard window-focus override. Notice focus lost so we can pause on
     * focus lost. e.g. user switches to take a call.
     */
    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        if (!hasWindowFocus) mThread.pause()
    }

    /* Callback invoked when the surface dimensions change. */
    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        mThread.setSurfaceSize(width, height)
    }

    /*
     * Callback invoked when the Surface has been created and is ready to be
     * used.
     */
    override fun surfaceCreated(holder: SurfaceHolder) {
        // start the thread here so that we don't busy-wait in run()
        // waiting for the surface to be created
        mThread.setSurfaceReady(true)
        mThread.start()
    }

    /*
     * Callback invoked when the Surface has been destroyed and must no longer
     * be touched. WARNING: after this method returns, the Surface/Canvas must
     * never be touched again!
     */
    override fun surfaceDestroyed(holder: SurfaceHolder) {
        // we have to tell thread to shut down & wait for it to finish, or else
        // it might touch the Surface after we return and explode
        var retry = true
        mThread.setSurfaceReady(false)
        while (retry) {
            try {
                mThread.join()
                retry = false
            } catch (e: InterruptedException) {
                // nothing to do here
            }
        }
        // remove the sensor listener
        unregisterListener()
    }

    /**
     * Register the accelerometer sensor so we can use it in-game.
     */
    fun registerListener() {
        mSensorManager!!.registerListener(
            mSensorAccelerometer, SensorManager.SENSOR_ACCELEROMETER,
            SensorManager.SENSOR_DELAY_GAME
        )
    }

    /**
     * Unregister the accelerometer sensor otherwise it will continue to operate
     * and report values.
     */
    fun unregisterListener() {
        mSensorManager!!.unregisterListener(mSensorAccelerometer)
    }

    /**
     * Installs a pointer to the text view used for messages.
     * @param textView the view to write status text
     */
    fun setTextView(textView: TextView?) {
        mStatusText = textView
    }

    /**
     * Fetches the animation thread corresponding to this LunarView.
     *
     * @return the animation thread
     */
    val thread: FightThread
        get() = mThread
}
