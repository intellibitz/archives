/*
 * Copyright (C) 2009 Muthu Ramadoss. All rights reserved.
 *
 * Modified from Romain Guy Shelves project to suit Books-Exchange requirements.
 * Original source from Shelves - http://code.google.com/p/shelves/
 */

/*
 * Copyright (C) 2008 The Android Open Source Project, Romain Guy
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

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.SystemClock

class CrossFadeDrawable(var start: Bitmap, var end: Bitmap?) : Drawable() {

    private var mTransitionState = TRANSITION_NONE

    var isCrossFadeEnabled = false
    private var mReverse = false
    private var mStartTimeMillis: Long = 0
    private var mFrom = 0
    private var mTo = 0
    private var mDuration = 0
    private var mOriginalDuration = 0
    private var mAlpha = 0

    private val mStartPaint = Paint(Paint.FILTER_BITMAP_FLAG)
    private val mEndPaint = Paint(Paint.FILTER_BITMAP_FLAG)

    private var mStartX = 0f
    private var mStartY = 0f
    private var mEndX = 0f
    private var mEndY = 0f

    private val mHandler = Handler()
    private val mInvalidater = Runnable { invalidateSelf() }

    /**
     * Begin the second layer on top of the first layer.
     *
     * @param durationMillis The length of the transition in milliseconds
     */
    fun startTransition(durationMillis: Int) {
        mFrom = 0
        mTo = 255
        mAlpha = 0
        mDuration = durationMillis
        mOriginalDuration = mDuration
        mReverse = false
        mTransitionState = if (start !== end) TRANSITION_STARTING else TRANSITION_NONE
        invalidateSelf()
    }

    /**
     * Show only the first layer.
     */
    fun resetTransition() {
        mAlpha = 0
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

        if (time - mStartTimeMillis > mOriginalDuration) {
            if (mAlpha == 0) {
                mFrom = 0
                mTo = 255
                mAlpha = 0
                mReverse = false
            } else {
                mFrom = 255
                mTo = 0
                mAlpha = 255
                mReverse = true
            }
            mOriginalDuration = duration
            mDuration = mOriginalDuration
            mTransitionState = TRANSITION_STARTING
            mHandler.post(mInvalidater)
            return
        }

        mReverse = !mReverse
        mFrom = mAlpha
        mTo = if (mReverse) 0 else 255
        mDuration = (if (mReverse) time - mStartTimeMillis else mOriginalDuration - (time - mStartTimeMillis)).toInt()
        mTransitionState = TRANSITION_STARTING
    }

    override fun draw(canvas: Canvas) {
        var done = true

        when (mTransitionState) {
            TRANSITION_STARTING -> {
                mStartTimeMillis = SystemClock.uptimeMillis()
                done = false
                mTransitionState = TRANSITION_RUNNING
            }
            TRANSITION_RUNNING -> if (mStartTimeMillis >= 0) {
                val normalized = (SystemClock.uptimeMillis() - mStartTimeMillis).toFloat() / mDuration
                done = normalized >= 1.0f
                mAlpha = (mFrom + (mTo - mFrom) * Math.min(normalized, 1.0f)).toInt()

                if (done) {
                    mTransitionState = TRANSITION_NONE
                    mHandler.post(mInvalidater)
                }
            }
        }

        val alpha = mAlpha
        val crossFade = isCrossFadeEnabled

        var bitmap = start
        var paint = mStartPaint

        if (!crossFade || 255 - alpha > 0) {
            if (crossFade) {
                paint.alpha = 255 - alpha
            }
            canvas.drawBitmap(bitmap, mStartX, mStartY, paint)
            if (crossFade) {
                paint.alpha = 0xFF
            }
        }

        if (alpha > 0 && end != null) {
            bitmap = end!!
            paint = mEndPaint
            paint.alpha = alpha
            canvas.drawBitmap(bitmap, mEndX, mEndY, paint)
            paint.alpha = 0xFF
        }

        if (!done) {
            mHandler.post(mInvalidater)
        }
    }

    override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
        super.setBounds(left, top, right, bottom)

        val width = right - left
        val height = bottom - top

        mStartX = (width - start.width) / 2.0f
        mStartY = (height - start.height).toFloat()

        if (end != null) {
            mEndX = (width - end!!.width) / 2.0f
            mEndY = (height - end!!.height).toFloat()
        }
    }

    override fun getIntrinsicWidth(): Int {
        return if (end != null) Math.max(start.width, end!!.width) else start.width
    }

    override fun getIntrinsicHeight(): Int {
        return if (end != null) Math.max(start.height, end!!.height) else start.height
    }

    override fun getMinimumWidth(): Int {
        return if (end != null) Math.max(start.width, end!!.width) else start.width
    }

    override fun getMinimumHeight(): Int {
        return if (end != null) Math.max(start.height, end!!.height) else start.height
    }

    override fun setDither(dither: Boolean) {
        mStartPaint.isDither = true
        mEndPaint.isDither = true
    }

    override fun setFilterBitmap(filter: Boolean) {
        mStartPaint.isFilterBitmap = true
        mEndPaint.isFilterBitmap = true
    }

    override fun setAlpha(alpha: Int) {}

    override fun setColorFilter(cf: ColorFilter?) {
        mStartPaint.colorFilter = cf
        mEndPaint.colorFilter = cf
    }

    @Deprecated("Deprecated in Java", ReplaceWith("PixelFormat.TRANSLUCENT", "android.graphics.PixelFormat"))
    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    companion object {
        private const val TRANSITION_STARTING = 0
        private const val TRANSITION_RUNNING = 1
        private const val TRANSITION_NONE = 2
    }
}
