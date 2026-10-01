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

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Drawable

internal open class LayerDrawable : Drawable, Drawable.Callback {
    var mLayerState: LayerState? = null

    private var mPaddingL: IntArray? = null
    private var mPaddingT: IntArray? = null
    private var mPaddingR: IntArray? = null
    private var mPaddingB: IntArray? = null

    private val mTmpRect = Rect()
    private var mParent: Drawable? = null
    private var mBlockSetBounds = false

    constructor(vararg layers: Drawable) : this(null, *layers)

    constructor(state: LayerState?, vararg layers: Drawable) : this(state) {
        val length = layers.size
        val r = arrayOfNulls<Rec>(length)

        val layerState = mLayerState!!
        for (i in 0 until length) {
            r[i] = Rec()
            r[i]?.mDrawable = layers[i]
            layers[i].callback = this
            layerState.mChildrenChangingConfigurations = layerState.mChildrenChangingConfigurations or layers[i].changingConfigurations
        }
        layerState.mNum = length
        layerState.mArray = r as Array<Rec>

        ensurePadding()
    }

    constructor(state: LayerState?) {
        val `as` = createConstantState(state)
        mLayerState = `as`
        if (`as`!!.mNum > 0) {
            ensurePadding()
        }
    }

    open fun createConstantState(state: LayerState?): LayerState? {
        return LayerState(state, this)
    }

    override fun invalidateDrawable(who: Drawable) {
        invalidateSelf()
    }

    override fun scheduleDrawable(who: Drawable, what: Runnable, `when`: Long) {
        scheduleSelf(what, `when`)
    }

    override fun unscheduleDrawable(who: Drawable, what: Runnable) {
        unscheduleSelf(what)
    }

    override fun draw(canvas: Canvas) {
        val array = mLayerState!!.mArray
        val N = mLayerState!!.mNum
        if (array != null) {
            for (i in 0 until N) {
                array[i].mDrawable?.draw(canvas)
            }
        }
    }

    override fun getChangingConfigurations(): Int {
        return super.getChangingConfigurations() or mLayerState!!.mChangingConfigurations or mLayerState!!.mChildrenChangingConfigurations
    }

    override fun getPadding(padding: Rect): Boolean {
        padding.left = 0
        padding.top = 0
        padding.right = 0
        padding.bottom = 0
        val array = mLayerState!!.mArray
        val N = mLayerState!!.mNum
        if (array != null) {
            for (i in 0 until N) {
                reapplyPadding(i, array[i])
                padding.left = Math.max(padding.left, mPaddingL!![i])
                padding.top = Math.max(padding.top, mPaddingT!![i])
                padding.right = Math.max(padding.right, mPaddingR!![i])
                padding.bottom = Math.max(padding.bottom, mPaddingB!![i])
            }
        }
        return true
    }

    override fun setVisible(visible: Boolean, restart: Boolean): Boolean {
        val changed = super.setVisible(visible, restart)
        val array = mLayerState!!.mArray
        val N = mLayerState!!.mNum
        if (array != null) {
            for (i in 0 until N) {
                array[i].mDrawable?.setVisible(visible, restart)
            }
        }
        return changed
    }

    override fun setDither(dither: Boolean) {
        val array = mLayerState!!.mArray
        val N = mLayerState!!.mNum
        if (array != null) {
            for (i in 0 until N) {
                array[i].mDrawable?.isDither = dither
            }
        }
    }

    override fun setAlpha(alpha: Int) {
        val array = mLayerState!!.mArray
        val N = mLayerState!!.mNum
        if (array != null) {
            for (i in 0 until N) {
                array[i].mDrawable?.alpha = alpha
            }
        }
    }

    override fun setColorFilter(cf: ColorFilter?) {
        val array = mLayerState!!.mArray
        val N = mLayerState!!.mNum
        if (array != null) {
            for (i in 0 until N) {
                array[i].mDrawable?.colorFilter = cf
            }
        }
    }

    @Deprecated("Deprecated in Java", ReplaceWith("PixelFormat.TRANSLUCENT", "android.graphics.PixelFormat"))
    override fun getOpacity(): Int {
        return mLayerState!!.opacity
    }

    override fun isStateful(): Boolean {
        return mLayerState!!.isStateful
    }

    override fun onStateChange(state: IntArray): Boolean {
        val array = mLayerState!!.mArray
        val N = mLayerState!!.mNum
        var paddingChanged = false
        var changed = false
        if (array != null) {
            for (i in 0 until N) {
                val r = array[i]
                if (r.mDrawable!!.setState(state)) {
                    changed = true
                }
                if (reapplyPadding(i, r)) {
                    paddingChanged = true
                }
            }
        }
        if (paddingChanged) {
            onBoundsChange(bounds)
        }
        return changed
    }

    override fun onLevelChange(level: Int): Boolean {
        val array = mLayerState!!.mArray
        val N = mLayerState!!.mNum
        var paddingChanged = false
        var changed = false
        if (array != null) {
            for (i in 0 until N) {
                val r = array[i]
                if (r.mDrawable!!.setLevel(level)) {
                    changed = true
                }
                if (reapplyPadding(i, r)) {
                    paddingChanged = true
                }
            }
        }
        if (paddingChanged) {
            onBoundsChange(bounds)
        }
        return changed
    }

    override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
        var l = left
        var r = right
        var b = bottom
        if (mBlockSetBounds) return

        val width = mLayerState!!.mArray!![0].mDrawable!!.intrinsicWidth
        l -= (width - (r - l)) / 2
        r = l + width
        b = top + intrinsicHeight
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

    override fun onBoundsChange(bounds: Rect) {
        val array = mLayerState!!.mArray
        val N = mLayerState!!.mNum
        var padL = 0
        var padT = 0
        var padR = 0
        var padB = 0
        if (array != null) {
            for (i in 0 until N) {
                val r = array[i]
                r.mDrawable!!.setBounds(
                    bounds.left + r.mInsetL + padL,
                    bounds.top + r.mInsetT + padT,
                    bounds.right - r.mInsetR - padR,
                    bounds.bottom - r.mInsetB - padB
                )
                padL += mPaddingL!![i]
                padR += mPaddingR!![i]
                padT += mPaddingT!![i]
                padB += mPaddingB!![i]
            }
        }
    }

    override fun getIntrinsicWidth(): Int {
        var width = -1
        val array = mLayerState!!.mArray
        val N = mLayerState!!.mNum
        var padL = 0
        var padR = 0
        if (array != null) {
            for (i in 0 until N) {
                val r = array[i]
                val w = r.mDrawable!!.intrinsicWidth + r.mInsetL + r.mInsetR + padL + padR
                if (w > width) {
                    width = w
                }
                padL += mPaddingL!![i]
                padR += mPaddingR!![i]
            }
        }
        return width
    }

    override fun getIntrinsicHeight(): Int {
        var height = -1
        val array = mLayerState!!.mArray
        val N = mLayerState!!.mNum
        var padT = 0
        var padB = 0
        if (array != null) {
            for (i in 0 until N) {
                val r = array[i]
                val h = r.mDrawable!!.intrinsicHeight + r.mInsetT + r.mInsetB + padT + padB
                if (h > height) {
                    height = h
                }
                padT += mPaddingT!![i]
                padB += mPaddingB!![i]
            }
        }
        return height
    }

    private fun reapplyPadding(i: Int, r: Rec): Boolean {
        val rect = mTmpRect
        r.mDrawable!!.getPadding(rect)
        if (rect.left != mPaddingL!![i] || rect.top != mPaddingT!![i] ||
            rect.right != mPaddingR!![i] || rect.bottom != mPaddingB!![i]
        ) {
            mPaddingL!![i] = rect.left
            mPaddingT!![i] = rect.top
            mPaddingR!![i] = rect.right
            mPaddingB!![i] = rect.bottom
            return true
        }
        return false
    }

    private fun ensurePadding() {
        val N = mLayerState!!.mNum
        if (mPaddingL != null && mPaddingL!!.size >= N) {
            return
        }
        mPaddingL = IntArray(N)
        mPaddingT = IntArray(N)
        mPaddingR = IntArray(N)
        mPaddingB = IntArray(N)
    }

    override fun getConstantState(): ConstantState? {
        if (mLayerState!!.canConstantState()) {
            mLayerState!!.mChangingConfigurations = super.getChangingConfigurations()
            return mLayerState
        }
        return null
    }

    internal class Rec {
        var mDrawable: Drawable? = null
        var mInsetL = 0
        var mInsetT = 0
        var mInsetR = 0
        var mInsetB = 0
        var mId = 0
    }

    internal open class LayerState(orig: LayerState?, owner: LayerDrawable?) : ConstantState() {
        var mNum: Int = 0
        var mArray: Array<Rec>? = null

        var mChangingConfigurations: Int = 0
        var mChildrenChangingConfigurations: Int = 0

        private var mHaveOpacity = false
        private var mOpacity = 0

        private var mHaveStateful = false
        private var mStateful = false

        private var mCheckedConstantState = false
        private var mCanConstantState = false

        init {
            if (orig != null) {
                val origRec = orig.mArray
                val N = orig.mNum

                mNum = N
                mArray = Array(N) { Rec() }

                mChangingConfigurations = orig.mChangingConfigurations
                mChildrenChangingConfigurations = orig.mChildrenChangingConfigurations

                for (i in 0 until N) {
                    val r = mArray!![i]
                    val or = origRec!![i]
                    r.mDrawable = or.mDrawable!!.constantState!!.newDrawable()
                    r.mDrawable!!.callback = owner
                    r.mInsetL = or.mInsetL
                    r.mInsetT = or.mInsetT
                    r.mInsetR = or.mInsetR
                    r.mInsetB = or.mInsetB
                    r.mId = or.mId
                }

                mHaveOpacity = orig.mHaveOpacity
                mOpacity = orig.mOpacity
                mHaveStateful = orig.mHaveStateful
                mStateful = orig.mStateful
                mCheckedConstantState = true
                mCanConstantState = true
            } else {
                mNum = 0
                mArray = null
            }
        }

        override fun newDrawable(): Drawable {
            return LayerDrawable(this)
        }

        override fun getChangingConfigurations(): Int {
            return mChangingConfigurations
        }

        val opacity: Int
            get() {
                if (mHaveOpacity) {
                    return mOpacity
                }

                val N = mNum
                val array = mArray
                var op = if (N > 0 && array != null) array[0].mDrawable!!.opacity else PixelFormat.TRANSPARENT
                if (array != null) {
                    for (i in 1 until N) {
                        op = resolveOpacity(op, array[i].mDrawable!!.opacity)
                    }
                }
                mOpacity = op
                mHaveOpacity = true
                return op
            }

        val isStateful: Boolean
            get() {
                if (mHaveStateful) {
                    return mStateful
                }

                var stateful = false
                val N = mNum
                val array = mArray
                if (array != null) {
                    for (i in 0 until N) {
                        if (array[i].mDrawable!!.isStateful) {
                            stateful = true
                            break
                        }
                    }
                }

                mStateful = stateful
                mHaveStateful = true
                return stateful
            }

        @Synchronized
        fun canConstantState(): Boolean {
            val array = mArray
            if (!mCheckedConstantState && array != null) {
                mCanConstantState = true
                val N = mNum
                for (i in 0 until N) {
                    if (array[i].mDrawable!!.constantState == null) {
                        mCanConstantState = false
                        break
                    }
                }
                mCheckedConstantState = true
            }

            return mCanConstantState
        }
    }
}
