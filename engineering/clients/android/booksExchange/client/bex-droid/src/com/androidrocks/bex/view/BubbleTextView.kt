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

package com.androidrocks.bex.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.TextView
import com.androidrocks.bex.R

/**
 * TextView that draws a bubble behind the text. We cannot use a LineBackgroundSpan
 * because we want to make the bubble taller than the text and TextView's clip is
 * too aggressive.
 */
class BubbleTextView : TextView {
    private val mRect = RectF()
    private var mPaint: Paint? = null
    private var mDrawableBottom: Drawable? = null

    private var mBackgroundSizeChanged = false
    private var mBackground: Drawable? = null
    private var mCornerRadius = 0f
    private var mPaddingH = 0f
    private var mPaddingV = 0f

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init()
    }

    private fun init() {
        mBackground = background
        setBackgroundDrawable(null)
        if (mBackground != null) mBackground!!.callback = this

        mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mPaint!!.color = context.resources.getColor(R.color.translucent_dark)

        val scale = context.resources.displayMetrics.density
        mCornerRadius = CORNER_RADIUS * scale
        mPaddingH = PADDING_H * scale
        //noinspection PointlessArithmeticExpression
        mPaddingV = PADDING_V * scale
    }

    override fun setFrame(left: Int, top: Int, right: Int, bottom: Int): Boolean {
        if (getLeft() != left || getRight() != right || getTop() != top || getBottom() != bottom) {
            mBackgroundSizeChanged = true
        }
        return super.setFrame(left, top, right, bottom)
    }

    override fun verifyDrawable(who: Drawable): Boolean {
        return mDrawableBottom === who || who === mBackground || super.verifyDrawable(who)
    }

    override fun drawableStateChanged() {
        val d = mBackground
        if (d != null && d.isStateful) {
            d.state = drawableState
        }
        super.drawableStateChanged()
    }

    override fun draw(canvas: Canvas) {
        val background = mBackground
        if (background != null) {
            val scrollX = scrollX
            val scrollY = scrollY

            if (mBackgroundSizeChanged) {
                background.setBounds(0, 0, width, height)
                mBackgroundSizeChanged = false
            }

            if ((scrollX or scrollY) == 0) {
                background.draw(canvas)
            } else {
                canvas.translate(scrollX.toFloat(), scrollY.toFloat())
                background.draw(canvas)
                canvas.translate(-scrollX.toFloat(), -scrollY.toFloat())
            }
        }

        val layout = layout
        val rect = mRect
        val left = compoundPaddingLeft
        val top = extendedPaddingTop

        rect[left + layout.getLineLeft(0) - mPaddingH, top + layout.getLineTop(0) - mPaddingV, Math.min(
            left + layout.getLineRight(0),
            (scrollX + width - compoundPaddingRight).toFloat()
        ) + mPaddingH] = top + layout.getLineBottom(0) + mPaddingV
        canvas.drawRoundRect(rect, mCornerRadius, mCornerRadius, mPaint!!)

        super.draw(canvas)
    }

    override fun setCompoundDrawablesWithIntrinsicBounds(
        left: Drawable?, top: Drawable?,
        right: Drawable?, bottom: Drawable?
    ) {
        super.setCompoundDrawablesWithIntrinsicBounds(left, top, right, bottom)
        mDrawableBottom = bottom
    }

    override fun invalidateDrawable(drawable: Drawable) {
        if (mDrawableBottom === drawable) {
            val dirty = drawable.bounds

            // Assume paddingLeft == paddingRight
            val drawableWidth = dirty.right - dirty.left
            val left = (width - drawableWidth) / 2 + scrollX

            // Assume we draw the bottom drawable at the bottom
            val drawableHeight = dirty.bottom - dirty.top
            val top = height - paddingBottom - drawableHeight + scrollY

            invalidate(left, top, left + drawableWidth, top + drawableHeight)
        } else {
            super.invalidateDrawable(drawable)
        }
    }

    companion object {
        private const val CORNER_RADIUS = 14.0f
        private const val PADDING_H = 10.0f
        private const val PADDING_V = 5.0f
    }
}
