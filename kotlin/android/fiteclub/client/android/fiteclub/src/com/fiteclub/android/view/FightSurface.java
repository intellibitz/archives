package com.fiteclub.android.view;

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

import android.content.Context;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.*;
import android.widget.TextView;

/**
 * The SurfaceView used for the Game
 */
public class FightSurface extends SurfaceView implements SurfaceHolder.Callback{

    /**
     * Pointer to the text view to display "Paused.." etc.
     */
    private TextView mStatusText;
    /**
     * The thread that actually draws the animation
     */
    private FightThread thread;

    // sensor manager used to control the accelerometer sensor.
    private SensorManager mSensorManager;
    // http://code.google.com/android/reference/android/hardware/SensorManager.html#SENSOR_ACCELEROMETER
    // for an explanation on the values reported by SENSOR_ACCELEROMETER.
    private final SensorListener mSensorAccelerometer = new SensorListener() {
        // method called whenever new sensor values are reported.
        public void onSensorChanged(int sensor, float[] values) {
            thread.onSensorChanged(sensor, values);
        }

        public void onAccuracyChanged(int sensor, int accuracy) {
            // currently not used
        }
    };

    public FightSurface(Context context) {
        super(context);
        init(context);
    }

    public FightSurface(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init(context);
    }

    public FightSurface(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        init(context);
    }

    private void init(Context context) {
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        // register our interest in hearing about changes to our surface
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);

        // create thread only; it's started in surfaceCreated()
        thread = new FightThread(holder, context, new Handler() {
            @Override
            public void handleMessage(Message m) {
                mStatusText.setVisibility(m.getData().getInt("viz"));
                mStatusText.setText(m.getData().getString("text"));
            }
        });

        setFocusable(true); // make sure we get key events

        // setup accelerometer sensor manager.
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        // register our accelerometer so we can receive values.
        // SENSOR_DELAY_GAME is the recommended rate for games
        mSensorManager.registerListener(mSensorAccelerometer, SensorManager.SENSOR_ACCELEROMETER,
                SensorManager.SENSOR_DELAY_GAME);

    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        return thread.onTouchEvent(motionEvent);
    }

    @Override
    public boolean onTrackballEvent(MotionEvent motionEvent) {
        return thread.onTrackballEvent(motionEvent);
    }

    /**
     * Standard override to get key-press events.
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent msg) {
        // quit application if user presses the back key.
        if (keyCode == KeyEvent.KEYCODE_BACK)
            unregisterListener();
//        return thread.doKeyDown(keyCode, msg);
        return super.onKeyDown(keyCode, msg);
    }


    /**
     * Standard window-focus override. Notice focus lost so we can pause on
     * focus lost. e.g. user switches to take a call.
     */
    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        if (!hasWindowFocus) thread.pause();
    }

    /* Callback invoked when the surface dimensions change. */
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        thread.setSurfaceSize(width, height);
    }

    /*
     * Callback invoked when the Surface has been created and is ready to be
     * used.
     */
    public void surfaceCreated(SurfaceHolder holder) {
        // start the thread here so that we don't busy-wait in run()
        // waiting for the surface to be created
        thread.setSurfaceReady(true);
        thread.start();
    }

    /*
     * Callback invoked when the Surface has been destroyed and must no longer
     * be touched. WARNING: after this method returns, the Surface/Canvas must
     * never be touched again!
     */
    public void surfaceDestroyed(SurfaceHolder holder) {
        // we have to tell thread to shut down & wait for it to finish, or else
        // it might touch the Surface after we return and explode
        boolean retry = true;
        thread.setSurfaceReady(false);
        while (retry) {
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
                // nothing to do here
            }
        }
        // remove the sensor listener
        unregisterListener();
    }

    /**
     * Register the accelerometer sensor so we can use it in-game.
     */
    public void registerListener() {
        mSensorManager.registerListener(mSensorAccelerometer, SensorManager.SENSOR_ACCELEROMETER,
                SensorManager.SENSOR_DELAY_GAME);
    }

    /**
     * Unregister the accelerometer sensor otherwise it will continue to operate
     * and report values.
     */
    public void unregisterListener() {
        mSensorManager.unregisterListener(mSensorAccelerometer);
    }

    /**
     * Installs a pointer to the text view used for messages.
     * @param textView the view to write status text
     */
    public void setTextView(TextView textView) {
        mStatusText = textView;
    }

    /**
     * Fetches the animation thread corresponding to this LunarView.
     *
     * @return the animation thread
     */
    public FightThread getThread() {
        return thread;
    }

}

