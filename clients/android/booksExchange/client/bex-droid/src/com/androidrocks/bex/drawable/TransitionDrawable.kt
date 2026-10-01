/*
 * Copyright (C) 2009 Muthu Ramadoss. All rights reserved.
 *
 * Modified from Romain Guy Shelves project to suit Books-Exchange requirements.
 * Original source from Shelves - http://code.google.com/p/shelves/
 */

/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.androidrocks.bex.drawable

import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.SystemClock

class TransitionDrawable : LayerDrawable, Drawable.Callback {

    /**
     * The current state of the transition. One of [TRANSITION_STARTING],
     * [TRANSITION_RUNNING] and [TRANSITION_NONE]
     */
    private var mTransitionState = TRANSITION_NONE

    private var mReverse = false
    private var mStartTimeMillis: Long = 0
    private var mFrom = 0
    private var mTo = 0
    private var mDuration = 0
    private var mState: TransitionState? = null

    constructor(vararg layers: Drawable) : this(TransitionState(null, null), *layers)

    internal constructor() : this(TransitionState(null, null))

    private constructor(state: TransitionState) : super(state) {
        mState = state
    }

    private constructor(state: TransitionState, vararg layers: Drawable) : super(state, *layers) {
        mState = state
    }

    override fun createConstantState(state: LayerState?): LayerState {
        return TransitionState(state as TransitionState?, this)
    }

    /**
     * Begin the second layer on top of the first layer.
     *
     * @param durationMillis The length of the transition in milliseconds
     */
    fun startTransition(durationMillis: Int) {
        mFrom = 0
        mTo = 255
        mState!!.mAlpha = 0
        mDuration = durationMillis
        mState!!.mDuration = mDuration
        mReverse = false
        mTransitionState = TRANSITION_STARTING
        invalidateSelf()
    }

    /**
     * Show only the first layer.
     */
    fun resetTransition() {
        mState!!.mAlpha = 0
        mTransitionState = TRANSITION_NONE
        invalidateSelf()
    }

    /**
     * Reverses the transition, picking up where the transition currently is.
     * If the transition is not currently running, this will start the transition
     * with the specified duration. If the transition is already running, the last
     * known duration will be used.
     *
     * @param duration The duration to use if no transition is running.
     */
    fun reverseTransition(duration: Int) {
        val time = SystemClock.uptimeMillis()
        // Animation is over
        if (time - mStartTimeMillis > mState!!.mDuration) {
            if (mState!!.mAlpha == 0) {
                mFrom = 0
                mTo = 255
                mState!!.mAlpha = 0
                mReverse = false
            } else {
                mFrom = 255
                mTo = 0
                mState!!.mAlpha = 255
                mReverse = true
            }
            mState!!.mDuration = duration
            mDuration = mState!!.mDuration
            mTransitionState = TRANSITION_STARTING
            invalidateSelf()
            return
        }

        mReverse = !mReverse
        mFrom = mState!!.mAlpha
        mTo = if (mReverse) 0 else 255
        mDuration = (if (mReverse) time - mStartTimeMillis else mState!!.mDuration - (time - mStartTimeMillis)).toInt()
        mTransitionState = TRANSITION_STARTING
    }

    override fun draw(canvas: Canvas) {
        var done = true
        val state = mState

        when (mTransitionState) {
            TRANSITION_STARTING -> {
                mStartTimeMillis = SystemClock.uptimeMillis()
                done = false
                mTransitionState = TRANSITION_RUNNING
            }

            TRANSITION_RUNNING -> if (mStartTimeMillis >= 0) {
                var normalized = (SystemClock.uptimeMillis() - mStartTimeMillis).toFloat() / mDuration
                done = normalized >= 1.0f
                normalized = Math.min(normalized, 1.0f)
                state!!.mAlpha = (mFrom + (mTo - mFrom) * normalized).toInt()
            }
        }

        val alpha = state!!.mAlpha
        val crossFade = state.mCrossFade
        val array = mLayerState!!.mArray
        var d: Drawable?

        d = array!![0].mDrawable
        if (crossFade) {
            d!!.alpha = 255 - alpha
        }
        d!!.draw(canvas)
        if (crossFade) {
            d.alpha = 0xFF
        }

        if (alpha > 0) {
            d = array[1].mDrawable
            d!!.alpha = alpha
            d.draw(canvas)
            d.alpha = 0xFF
        }

        if (!done) {
            invalidateSelf()
        }
    }

    /**
     * Enables or disables the cross fade of the drawables. When cross fade
     * is disabled, the first drawable is always drawn opaque. With cross
     * fade enabled, the first drawable is drawn with the opposite alpha of
     * the second drawable.
     *
     * @param enabled True to enable cross fading, false otherwise.
     */
    var isCrossFadeEnabled: Boolean
        get() = mState!!.mCrossFade
        set(enabled) {
            mState!!.mCrossFade = enabled
        }

    internal class TransitionState(orig: TransitionState?, owner: TransitionDrawable?) : LayerState(orig, owner) {
        var mAlpha = 0
        var mDuration = 0
        var mCrossFade = false

        override fun newDrawable(): Drawable {
            return TransitionDrawable(this)
        }

        override fun getChangingConfigurations(): Int {
            return mChangingConfigurations
        }
    }

    companion object {

        /**
         * A transition is about to start.
         */
        private const val TRANSITION_STARTING = 0

        /**
         * The transition has started and the animation is in progress
         */
        private const val TRANSITION_RUNNING = 1

        /**
         * No transition will be applied
         */
        private const val TRANSITION_NONE = 2
    }
}
