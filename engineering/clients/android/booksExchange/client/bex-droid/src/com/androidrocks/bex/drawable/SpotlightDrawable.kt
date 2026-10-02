/*
 * Copyright (C) 2009 Muthu Ramadoss. All rights reserved.
 *
 * Modified from Romain Guy Shelves project to suit Books-Exchange requirements.
 * Original source from Shelves - http://code.google.com/p/shelves/
 */

/*
 * Copyright (C) 2008 Romain Guy
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

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.drawable.Drawable
import android.view.ViewGroup
import com.androidrocks.bex.R

class SpotlightDrawable(context: Context, private val mView: ViewGroup, resource: Int = R.drawable.spotlight) : Drawable() {
    private val mBitmap: Bitmap
    private val mPaint: Paint

    private var mOffsetDisabled = false
    private var mBlockSetBounds = false
    private var mParent: Drawable? = null

    init {
        mBitmap = BitmapFactory.decodeResource(context.resources, resource)

        mPaint = Paint()
        mPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SCREEN)
    }

    override fun draw(canvas: Canvas) {
        if (mView.hasWindowFocus()) {
            val bounds = bounds
            canvas.save()
            canvas.drawBitmap(mBitmap, bounds.left.toFloat(), bounds.top.toFloat(), mPaint)
            canvas.restore()
        }
    }

    override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
        var l = left
        var r = right
        var b = bottom
        if (mBlockSetBounds) return

        if (!mOffsetDisabled) {
            val width = intrinsicWidth
            val view = mView.getChildAt(0)
            if (view != null) l -= (width - view.width) / 2
            r = l + width
            b = top + intrinsicHeight
        } else {
            r = l + intrinsicWidth
            b = top + intrinsicHeight
        }

        super.setBounds(l, top, r, b)

        if (mParent != null) {
            mBlockSetBounds = true
            mParent!!.setBounds(l, top, r, b)
            mBlockSetBounds = false
        }
    }

    fun setParent(drawable: Drawable?) {
        mParent = drawable
    }

    override fun onStateChange(state: IntArray): Boolean {
        invalidateSelf()
        return super.onStateChange(state)
    }

    override fun setAlpha(alpha: Int) {
        mPaint.alpha = alpha
    }

    override fun setColorFilter(cf: ColorFilter?) {
        mPaint.colorFilter = cf
    }

    @Deprecated("Deprecated in Java", ReplaceWith("PixelFormat.TRANSLUCENT", "android.graphics.PixelFormat"))
    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    override fun getIntrinsicWidth(): Int {
        return mBitmap.width
    }

    override fun getIntrinsicHeight(): Int {
        return mBitmap.height
    }

    fun disableOffset() {
        mOffsetDisabled = true
    }
}
