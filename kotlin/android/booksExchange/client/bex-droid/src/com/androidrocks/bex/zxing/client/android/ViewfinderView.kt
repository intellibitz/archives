/*
 * Copyright (C) 2009 Muthu Ramadoss. All rights reserved.
 *
 * Modified from Zxing project to suit Books-Exchange requirements.
 * Original source from Zxing - http://code.google.com/p/zxing/
 */

/*
 * Copyright (C) 2008 ZXing authors
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

package com.androidrocks.bex.zxing.client.android

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import com.androidrocks.bex.R

/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder rectangle and partial
 * transparency outside it, as well as the laser scanner animation and result points.
 */
class ViewfinderView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private val mPaint: Paint
    private val mBox: Rect
    private var mResultBitmap: Bitmap? = null
    private val mMaskColor: Int
    private val mResultColor: Int
    private val mFrameColor: Int
    private val mLaserColor: Int
    private var mScannerAlpha: Int

    init {
        // Initialize these once for performance rather than calling them every time in onDraw().
        mPaint = Paint()
        mBox = Rect()
        val resources = resources
        mMaskColor = resources.getColor(R.color.viewfinder_mask)
        mResultColor = resources.getColor(R.color.result_view)
        mFrameColor = resources.getColor(R.color.viewfinder_frame)
        mLaserColor = resources.getColor(R.color.viewfinder_laser)
        mScannerAlpha = 0
    }

    override fun onDraw(canvas: Canvas) {
        val frame = CameraManager.get()!!.framingRect ?: return
        val width = canvas.width
        val height = canvas.height

        // Draw the exterior (i.e. outside the framing rect) darkened
        mPaint.color = if (mResultBitmap != null) mResultColor else mMaskColor
        mBox[0, 0, width] = frame.top
        canvas.drawRect(mBox, mPaint)
        mBox[0, frame.top, frame.left] = frame.bottom + 1
        canvas.drawRect(mBox, mPaint)
        mBox[frame.right + 1, frame.top, width] = frame.bottom + 1
        canvas.drawRect(mBox, mPaint)
        mBox[0, frame.bottom + 1, width] = height
        canvas.drawRect(mBox, mPaint)

        if (mResultBitmap != null) {
            // Draw the opaque result bitmap over the scanning rectangle
            mPaint.alpha = 255
            canvas.drawBitmap(mResultBitmap!!, frame.left.toFloat(), frame.top.toFloat(), mPaint)
        } else {
            // Draw a two pixel solid black border inside the framing rect
            mPaint.color = mFrameColor
            mBox[frame.left, frame.top, frame.right + 1] = frame.top + 2
            canvas.drawRect(mBox, mPaint)
            mBox[frame.left, frame.top + 2, frame.left + 2] = frame.bottom - 1
            canvas.drawRect(mBox, mPaint)
            mBox[frame.right - 1, frame.top, frame.right + 1] = frame.bottom - 1
            canvas.drawRect(mBox, mPaint)
            mBox[frame.left, frame.bottom - 1, frame.right + 1] = frame.bottom + 1
            canvas.drawRect(mBox, mPaint)

            // Draw a red "laser scanner" line through the middle to show decoding is active
            mPaint.color = mLaserColor
            mPaint.alpha = SCANNER_ALPHA[mScannerAlpha]
            mScannerAlpha = (mScannerAlpha + 1) % SCANNER_ALPHA.size
            val middle = frame.height() / 2 + frame.top
            mBox[frame.left + 2, middle - 1, frame.right - 1] = middle + 2
            canvas.drawRect(mBox, mPaint)

            // Request another update at the animation interval, but only repaint the laser line,
            // not the entire viewfinder mask.
            postInvalidateDelayed(
                ANIMATION_DELAY,
                mBox.left.toLong(),
                mBox.top.toLong(),
                mBox.right.toLong(),
                mBox.bottom.toLong()
            )
        }
    }

    fun drawViewfinder() {
        mResultBitmap = null
        invalidate()
    }

    /**
     * Draw a bitmap with the result points highlighted instead of the live scanning display.
     *
     * @param barcode An image of the decoded barcode.
     */
    fun drawResultBitmap(barcode: Bitmap?) {
        mResultBitmap = barcode
        invalidate()
    }

    companion object {
        private val SCANNER_ALPHA = intArrayOf(0, 64, 128, 192, 255, 192, 128, 64)
        private const val ANIMATION_DELAY = 100L
    }
}
