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
import android.content.res.Resources;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.media.MediaPlayer;
import android.util.Log;
import com.fiteclub.android.R;

/**
 * The Game Thread
 * <p> Updates the Fighters based on the Gamer's Input
 */
public class FightThread extends Thread {

    /*
     * State-tracking constants
     */
    public static final int STATE_LOSE = 1;
    public static final int STATE_PAUSE = 2;
    public static final int STATE_READY = 3;
    public static final int STATE_RUNNING = 4;
    public static final int STATE_WIN = 5;

    /**
     * The state of the game. One of READY, RUNNING, PAUSE, LOSE, or WIN
     */
    private int mState;

    /*
     * Member (state) fields
     */
    /**
     * Current height of the surface/canvas.
     *
     * @see #setSurfaceSize
     */
    private int mCanvasHeight = 1;

    /**
     * Current width of the surface/canvas.
     *
     * @see #setSurfaceSize
     */
    private int mCanvasWidth = 1;

    /**
     * Message handler used by thread to interact with TextView
     */
    private Handler mHandler;

    /**
     * Indicate whether the surface has been created & is ready to draw
     */
    private boolean mSurfaceReady = false;

    /**
     * Handle to the surface manager object we interact with
     */
    private SurfaceHolder mSurfaceHolder;

    /**
     * The drawable to use as the background of the animation canvas
     */
    private Bitmap mBackgroundImage;
//    private Drawable mBackgroundDrawable;

    /**
     * The drawable to use as the background of the animation canvas
     */
    private Bitmap mTouchButtonImage;

    private Bitmap[] myStanceFrames = new Bitmap[2];
    private Bitmap[] myHButtFrames = new Bitmap[2];

    private Drawable[] mUCutFrames = new Drawable[4];
    private Drawable[] mStanceFrames = new Drawable[3];
    private Drawable[] mDuckFrames = new Drawable[2];
    private Drawable[] mRHookFrames = new Drawable[1];

    private int STAND = 0;
    private int STANCEL = 1;
    private int STANCER = 2;

    private Drawable mFighter;
    private Bitmap myFighter;
    
    private Context mContext;
    MediaPlayer mBackgroundTrack;
    private int mPanBackground = 0;

    FightThread(SurfaceHolder surfaceHolder, Context context, Handler handler) {
        // get handles to some important objects
        mSurfaceHolder = surfaceHolder;
        mHandler = handler;
        mContext = context;

        Resources res = context.getResources();
        // cache handles to our key sprites & other drawables

// load my stance
        myStanceFrames[0] = BitmapFactory.decodeResource(res, R.drawable.stanceleft);
        myStanceFrames[1] = BitmapFactory.decodeResource(res, R.drawable.stanceright);

        myHButtFrames[0] = BitmapFactory.decodeResource(res, R.drawable.headbutt1);
        myHButtFrames[1] = BitmapFactory.decodeResource(res, R.drawable.headbutt2);

// load the opponent stance
        mStanceFrames[0] = context.getResources().getDrawable(R.drawable.stand);
        mStanceFrames[1] = context.getResources().getDrawable(R.drawable.stancel);
        mStanceFrames[2] = context.getResources().getDrawable(R.drawable.stancer);
        // Use the regular lander image as the model size for all sprites
        mCanvasWidth = mStanceFrames[0].getIntrinsicWidth();
        mCanvasHeight = mStanceFrames[0].getIntrinsicHeight();


// load the ducking
        mDuckFrames[0] = context.getResources().getDrawable(R.drawable.duck1);
        mDuckFrames[1] = context.getResources().getDrawable(R.drawable.duck2);


// load the upper cuts
        mUCutFrames[0] = context.getResources().getDrawable(R.drawable.ucut1);
        mUCutFrames[1] = context.getResources().getDrawable(R.drawable.ucut2);
        mUCutFrames[2] = context.getResources().getDrawable(R.drawable.ucut3);
        mUCutFrames[3] = context.getResources().getDrawable(R.drawable.ucut4);

        mRHookFrames[0] = context.getResources().getDrawable(R.drawable.rhook3);

        // load background image as a Bitmap instead of a Drawable b/c
        // we don't need to transform it and it's faster to draw this way
//        mBackgroundImage = BitmapFactory.decodeResource(res, R.drawable.crowd);
        mBackgroundImage = BitmapFactory.decodeResource(res, R.drawable.basement_background);
//        mBackgroundDrawable = context.getResources().getDrawable(R.drawable.basement_background);
        mTouchButtonImage = BitmapFactory.decodeResource(res, R.drawable.touch_button2);
//        mBackgroundTrack = MediaPlayer.create(mContext, R.raw.ruiner);

// initialize the first set of fighters with the default pose       
        myFighter = myStanceFrames[1];
        mFighter = mStanceFrames[0];
    }

    /**
     * Pauses the physics update & animation.
     */
    public void pause() {
        synchronized (mSurfaceHolder) {
            if (mState == STATE_RUNNING) setState(STATE_PAUSE);
        }
    }

    /**
     * Restores game state from the indicated Bundle. Typically called when
     * the Activity is being restored after having been previously
     * destroyed.
     *
     * @param savedState Bundle containing the game state
     */
    public synchronized void restoreState(Bundle savedState) {
        synchronized (mSurfaceHolder) {
            setState(STATE_PAUSE);
        }
    }

    /**
     * Dump game state to the provided Bundle. Typically called when the
     * Activity is being suspended.
     *
     * @return Bundle with this view's state
     */
    public Bundle saveState(Bundle map) {
        synchronized (mSurfaceHolder) {
            if (map != null) {
//                    map.putDouble(KEY_FUEL, Double.valueOf(mFuel));
            }
        }
        return map;
    }

    /**
     * Used to signal the thread whether it should be running or not.
     * Passing true allows the thread to run; passing false will shut it
     * down if it's already running. Calling start() after this was most
     * recently called with false will result in an immediate shutdown.
     *
     * @param flag true to run, false to shut down
     */
    public void setSurfaceReady(boolean flag) {
        mSurfaceReady = flag;
        if (!mSurfaceReady){
            cleanup ();
        }
    }

    /**
     * Sets the game mode. That is, whether we are running, paused, in the
     * failure state, in the victory state, etc.
     *
     * @param mode one of the STATE_* constants
     */
    public void setState(int mode) {
        synchronized (mSurfaceHolder) {
            mState = mode;
        }
    }

    void setStatusText(String message) {
        Message msg = mHandler.obtainMessage();
        Bundle b = new Bundle();
        b.putString("text", message);
        b.putInt("viz", View.VISIBLE);
        msg.setData(b);
        mHandler.sendMessage(msg);
    }


    /* Callback invoked when the surface dimensions change. */
    public void setSurfaceSize(int width, int height) {
        // synchronized to make sure these all change atomically
        synchronized (mSurfaceHolder) {
            mCanvasWidth = width;
            mCanvasHeight = height;
        }
    }

    public void onSensorChanged(int sensor, float[] values) {
        synchronized (mSurfaceHolder) {
            int x = (int) values[0];
            int y = (int) values[1];
// if y is positive, left movement.. else, right movement
            if (y > 3) {
                mFighter = mStanceFrames[STANCEL];
            } else if (y < -3) {
                mFighter = mStanceFrames[STANCER];
            }
// if x is positive, up movement.. else, down movement
            else if (x > 3) {
                mFighter = mStanceFrames[STAND];
            } else if (x < -3) {
                mFighter = mDuckFrames[0];
            }

// debug
/*
            Log.i ("ACCELEROMETER VALUES <======== ", " X = "
                    + mAccelX + " Y = " + mAccelY + " Z = " + mAccelZ);
*/
//            thread.setStatusText("X = "+ (int)mAccelX + " Y = " + (int)mAccelY + " Z = " + (int)mAccelZ);

        }
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        synchronized (mSurfaceHolder){
//            switch (motionEvent.getAction()){
//                case MotionEvent.ACTION_MOVE:
                    float x = motionEvent.getX();
                    float y = motionEvent.getY();
//                    Log.i ("Touch Event : ===> ", x + " : " + y);
            if (x > 0 && x < 70 && y > 0 && y < 90){
//                    Log.i ("Touch Event 1: ===> ", x + " : " + y);
                mFighter = mUCutFrames[3];
                return true;
            }
            else if (x > 80 && x < 150 && y > 0 && y < 90){
//                    Log.i ("Touch Event 2: ===> ", x + " : " + y);
                mFighter = mRHookFrames[0];
                return true;
            }

//                default:
                    return false;
//            }
        }
    }

    public boolean onTrackballEvent(MotionEvent motionEvent) {
        synchronized (mSurfaceHolder) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    float x = motionEvent.getX();
                    float y = motionEvent.getY();
                    mPanBackground = (int) (y * 82) + 82;
//                        setStatusText("mLeft: " + x +"mTop: "+ y);
// if y is positive, right movement.. else, left movement
                    if (y > 0) {
                        mFighter = mStanceFrames[STANCER];
                        myFighter = myStanceFrames[1];
                    } else if (y == 0) {
// if x is positive, up movement.. else, down movement
                        if (x > 0) {
                            mFighter = mStanceFrames[STAND];
                            myFighter = myHButtFrames[1];
// if down is pressed
                        } else {
                            mFighter = mDuckFrames[0];
                            myFighter = myHButtFrames[0];
                        }
//                        mPanBackground = 0;
                    } else {
                        mFighter = mStanceFrames[STANCEL];
                        myFighter = myStanceFrames[0];
                    }
                    return true;
                case MotionEvent.ACTION_DOWN:
                    mFighter = mDuckFrames[0];
                    return true;
                case MotionEvent.ACTION_UP:
                    mFighter = mStanceFrames[STAND];
                    return true;
                default:
                    return false;
            }
        }
    }

    /**
     * The main loop of a game is the part that "ticks" sub systems in a specific order and
     * usually as many times per second as possible.  Your main loop will need to run on
     * its own thread.  The reason for this is that Android has a main UI thread and if you
     * don't run your own thread, the UI thread will be blocked by your game which will
     * cause the Android OS to not be able to handle any of its normal update tasks.
     * The order of execution is usually as follows:
     * State, Input, AI, Physics, Animation, Sound and Video.
     * <p/>
     * The Game loop
     * <p/>
     * Example 1:
     * public void run() {
     * while (isRunning) {
     * while (isPaused && isRunning) {
     * sleep(100);
     * }
     * update();
     * }
     * }
     * <p/>
     */
    @Override
    public void run() {
        int TICKS_PER_SECOND = 25;
        int SKIP_TICKS = 1000 / TICKS_PER_SECOND;
        int MAX_FRAMESKIP = 5;
        long next_game_tick = System.currentTimeMillis();
        int loops;
        float interpolation = 0f;

        while (mSurfaceReady) {
            Canvas canvas = null;
            try {
                canvas = mSurfaceHolder.lockCanvas(null);
                synchronized (mSurfaceHolder) {
// play the game, only when the game is in running state                   
                    if (mState == STATE_RUNNING) {
                        loops = 0;
                        while (System.currentTimeMillis() > next_game_tick && loops < MAX_FRAMESKIP) {
// update the game state here
                            update();
                            next_game_tick += SKIP_TICKS;
                            loops++;
                        }
                        interpolation = (System.currentTimeMillis() + SKIP_TICKS - next_game_tick)
                                / SKIP_TICKS;
                    }
                    display(interpolation, canvas);
//                            display (canvas);
                }
            } finally {
                // do this in a finally so that if an exception is thrown during the above,
                // we don't leave the Surface in an inconsistent state
                if (canvas != null) {
                    mSurfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
        }
    }

    private void update() {
        // consult the game fsm, to figure out the current state
        updateState();
        // get the user input, and send it to the queue
        updateInput();
        // get the opponent input, and send it to the queue
        updateAI();
        // update the game logic
        updatePhysics();
        // set up the frames right, don't do the actual draw
        updateAnimations();
        // set up the sounds right
        updateSound();
    }

    private void display(float interpolation, Canvas canvas) {
        // final draw.. this is the time to update the screen
        updateVideo(canvas);
    }

    /**
     * Updating State means to manage state transitions, such as a game over, character select
     * or next level.  Often times you will want to wait a few seconds on a state and the
     * State management is the part that should handle this delay and setting the
     * next state after the time has passed.
     */
    private void updateState() {
    }

    /**
     * Input is any key, scroll or touch from the user.  It's important to handle this before
     * processing Physics because often times input will affect the physics so processing input
     * first will make the game more responsive.  In Android, the input events come in from the
     * main UI thread and so you must code to buffer the input so that your main loop can pick
     * it up when the time comes.  This is not a difficult task.  Defining a field for the next
     * user input and having the onKeyPressed or onTouchEvent set the next user action into that
     * field is all that will be required.  All the Input update needs to do at that point is
     * determine if it is valid input given the state of the game and let the
     * Physics side handle responding to it.
     */
    private void updateInput() {
    }

    /**
     * The AI update is analagous to a user deciding what they are going to "press" next.
     * The general idea is that the AI will press buttons just like the user does.
     * This will also be picked up and responded to by the Physics update.
     */
    private void updateAI() {
    }

    /**
     * The Physics update may or may not be actual physics.  For action games, the point of it
     * is to take into account the last time it was updated, the current time it is being
     * updated at, the user input and the AI input and determine where everything needs to be
     * and whether any collisions have occured.  For a game where you visually grab pieces and
     * slide them around, it will be the part that is sliding the piece or letting it drop
     * into place.  For a trivia game, it would be the part deciding if the answer is right
     * or wrong.  You may name yours something else, but every game has a part that is the
     * red meat of the game engine and for this article, I'm referring to it as Physics.
     * <p/>
     * Figures the lander state (x, y, fuel, ...) based on the passage of
     * realtime. Does not invalidate(). Called at the start of draw().
     * Detects the end-of-game and sets the UI to the next state.
     */
    private void updatePhysics() {
    }

    /**
     * Animations aren't as simple as just putting an animated gif into your game.
     * You will need to have the game draw each frame at the right time.
     * It's not as difficult as it sounds.  Keeping state fields like isDancing, danceFrame
     * and lastDanceFrameTime allows for the Animation update to determine if its time to
     * switch to the next frame.  That's all the animation update really does.
     * Actually displaying the change of animation is handled by the video update.
     */
    private void updateAnimations() {
    }

    /**
     * The Sound update handles triggering sounds, stopping sounds, changing volumes and
     * changing the pitch of sounds.  Normally when writing a game, the sound update would
     * actually produce a stream of bytes to be delivered to the sound buffer but Android
     * manages its own sounds so your options for games are to use SoundPool or MediaPlayer.
     * They are both a little sensitive but know that because of some low level implementation
     * details, small, low bitrate OGGs will yield the best performance
     * results and the best stability.
     */
    private void updateSound() {
        if (mBackgroundTrack != null && !mBackgroundTrack.isPlaying()){
            mBackgroundTrack.start();
        }
    }

    /**
     * The Video update takes into account the state of the game, the positions of players,
     * scores, statuses, etc and draws everything to screen.  If using a main loop, you will
     * want to use the SurfaceView and do a "push" draw.  With other views, the view itself
     * will call the draw operation and the main loop won't have to do it.
     * SurfaceView gives the highest frames per second and is the most appropriate for games
     * with animation or moving parts on screen.  All the video update should do is take the
     * state of the game and draw it for this instance in time.  Any other automation is better
     * handled by a different update task.
     *
     * @param canvas Draws the fighters, and background to the provided Canvas.
     */
    private void updateVideo(Canvas canvas) {
        // Draw the background image. Operations on the Canvas accumulate
        // so this is like clearing the screen.
// 320 x 644
// height is panoramic for background 480 - 644 = 164       
// no negatives allowed
        if (mPanBackground < 0){
            mPanBackground = 0;
        } else if (mPanBackground > 164){
            mPanBackground = 164;
        }
        canvas.drawBitmap(mBackgroundImage, 0, 0, null);
/*
        canvas.drawBitmap(Bitmap.createBitmap(mBackgroundImage, 0, mPanBackground, 320, 480),
                0, 0, null);
*/
//        canvas.clipRect(0, 82, 320, 562);

//        mBackgroundDrawable.setBounds(0, 82, 320, 562);
//        mBackgroundDrawable.draw (canvas);
        
        canvas.drawBitmap(mTouchButtonImage, 0, 0, null);

// draw opponent
        mFighter.setBounds(0, 0, mCanvasWidth, mCanvasHeight);
        mFighter.draw(canvas);
// draw my fighter
        canvas.drawBitmap(myFighter, 0, 0, null);

// test code
// todo: to be removed
/*
        Paint mLinePaint = new Paint();
        mLinePaint.setAntiAlias(true);
        mLinePaint.setARGB(255, 0, 255, 0);

        canvas.drawRoundRect(new RectF(0, 0, 70, 90), 10, 10, mLinePaint);
        canvas.drawRoundRect(new RectF(80, 0, 150, 90), 10, 10, mLinePaint);
*/

        canvas.save();
    }

    private void cleanup() {
        if (mBackgroundTrack != null && mBackgroundTrack.isPlaying()){
            mBackgroundTrack.pause();
        }
    }


}


