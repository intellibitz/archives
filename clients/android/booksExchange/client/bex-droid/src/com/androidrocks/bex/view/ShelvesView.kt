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

package com.androidrocks.bex.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.StateListDrawable
import android.util.AttributeSet
import android.view.ViewConfiguration
import android.widget.GridView
import com.androidrocks.bex.R
import com.androidrocks.bex.drawable.SpotlightDrawable
import com.androidrocks.bex.drawable.TransitionDrawable

class ShelvesView : GridView {
    private var mShelfBackground: Bitmap? = null
    private var mShelfWidth = 0
    private var mShelfHeight = 0

    private var mWebLeft: Bitmap? = null
    private var mWebRight: Bitmap? = null
    private var mWebRightWidth = 0

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        load(context, attrs, 0)
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        load(context, attrs, defStyle)
        init(context)
    }

    private fun load(context: Context, attrs: AttributeSet?, defStyle: Int) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.ShelvesView, defStyle, 0)

        val resources = resources
        val background = a.getResourceId(R.styleable.ShelvesView_shelfBackground, 0)
        val shelfBackground = BitmapFactory.decodeResource(resources, background)
        if (shelfBackground != null) {
            mShelfWidth = shelfBackground.width
            mShelfHeight = shelfBackground.height
            mShelfBackground = shelfBackground
        }

        mWebLeft = BitmapFactory.decodeResource(resources, R.drawable.web_left)

        val webRight = BitmapFactory.decodeResource(resources, R.drawable.web_right)
        mWebRightWidth = webRight.width
        mWebRight = webRight

        a.recycle()
    }

    private fun init(context: Context) {
        val drawable = StateListDrawable()

        val start = SpotlightDrawable(context, this)
        start.disableOffset()
        val end = SpotlightDrawable(context, this, R.drawable.spotlight_blue)
        end.disableOffset()
        val transition = TransitionDrawable(start, end)
        drawable.addState(
            intArrayOf(android.R.attr.state_pressed),
            transition
        )

        val normal = SpotlightDrawable(context, this)
        drawable.addState(intArrayOf(), normal)

        normal.parent = drawable
        transition.parent = drawable

        selector = drawable
        setDrawSelectorOnTop(false)
    }

    override fun dispatchDraw(canvas: Canvas) {
        val count = childCount
        val top = if (count > 0) getChildAt(0).top else 0
        val shelfWidth = mShelfWidth
        val shelfHeight = mShelfHeight
        val width = width
        val height = height
        val background = mShelfBackground

        var x = 0
        while (x < width) {
            var y = top
            while (y < height) {
                canvas.drawBitmap(background!!, x.toFloat(), y.toFloat(), null)
                y += shelfHeight
            }
            x += shelfWidth
        }

        if (count == 0) {
            canvas.drawBitmap(mWebLeft!!, 0.0f, (top + 1).toFloat(), null)
            canvas.drawBitmap(
                mWebRight!!,
                (width - mWebRightWidth).toFloat(),
                (top + shelfHeight + 1).toFloat(),
                null
            )
        }

        super.dispatchDraw(canvas)
    }

    override fun setPressed(pressed: Boolean) {
        super.setPressed(pressed)

        val current = selector.current
        if (current is TransitionDrawable) {
            if (pressed) {
                current.startTransition(
                    ViewConfiguration.getLongPressTimeout()
                )
            } else {
                current.resetTransition()
            }
        }
    }
}
