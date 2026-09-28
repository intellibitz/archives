/*
 * Copyright (C) 2009 The Android Open Source Project
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

package intellibitz.intellidroid.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.os.Handler
import android.util.AttributeSet
import android.view.KeyEvent
import android.widget.ImageView

abstract class ImageViewTouchBase : ImageView {

    companion object {
        const val SCALE_RATE = 1.25F
        @Suppress("unused")
        private const val TAG = "ImageViewTouchBase"
    }

    protected val mBitmapDisplayed = RotateBitmap(null)
    private val mDisplayMatrix = Matrix()
    private val mMatrixValues = FloatArray(9)
    protected var mBaseMatrix = Matrix()
    protected var mSuppMatrix = Matrix()
    protected var mHandler = Handler()
    var mThisWidth = -1
        private set
    var mThisHeight = -1
        private set
    var mMaxZoom = 0f
        private set
    private var mRecycler: Recycler? = null
    private var mOnLayoutRunnable: Runnable? = null

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    fun setRecycler(r: Recycler?) {
        mRecycler = r
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        mThisWidth = right - left
        mThisHeight = bottom - top
        val r = mOnLayoutRunnable
        if (r != null) {
            mOnLayoutRunnable = null
            r.run()
        }
        if (mBitmapDisplayed.bitmap != null) {
            getProperBaseMatrix(mBitmapDisplayed, mBaseMatrix)
            setImageMatrix(getImageViewMatrix())
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.repeatCount == 0) {
            event.startTracking()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.isTracking && !event.isCanceled) {
            if (scale > 1.0f) {
                // If we're zoomed in, pressing Back jumps out to show the
                // entire image, otherwise Back returns the user to the gallery.
                zoomTo(1.0f)
                return true
            }
        }
        return super.onKeyUp(keyCode, event)
    }

    override fun setImageBitmap(bitmap: Bitmap?) {
        setImageBitmap(bitmap, 0)
    }

    private fun setImageBitmap(bitmap: Bitmap?, rotation: Int) {
        super.setImageBitmap(bitmap)
        val d = drawable
        if (d != null) {
            d.setDither(true)
        }

        val old = mBitmapDisplayed.bitmap
        mBitmapDisplayed.bitmap = bitmap
        mBitmapDisplayed.rotation = rotation

        if (old != null && old != bitmap && mRecycler != null) {
            mRecycler!!.recycle(old)
        }
    }

    fun clear() {
        setImageBitmapResetBase(null, true)
    }

    // This function changes bitmap, reset base matrix according to the size
    // of the bitmap, and optionally reset the supplementary matrix.
    fun setImageBitmapResetBase(bitmap: Bitmap?, resetSupp: Boolean) {
        setImageRotateBitmapResetBase(RotateBitmap(bitmap), resetSupp)
    }

    fun setImageRotateBitmapResetBase(bitmap: RotateBitmap, resetSupp: Boolean) {
        val viewWidth = width

        if (viewWidth <= 0) {
            mOnLayoutRunnable = Runnable {
                setImageRotateBitmapResetBase(bitmap, resetSupp)
            }
            return
        }

        if (bitmap.bitmap != null) {
            getProperBaseMatrix(bitmap, mBaseMatrix)
            setImageBitmap(bitmap.bitmap, bitmap.rotation)
        } else {
            mBaseMatrix.reset()
            setImageBitmap(null)
        }

        if (resetSupp) {
            mSuppMatrix.reset()
        }
        setImageMatrix(getImageViewMatrix())
        mMaxZoom = maxZoom()
    }

    // Center as much as possible in one or both axis.  Centering is
    // defined as follows:  if the image is scaled down below the
    // view's dimensions then center it (literally).  If the image
    // is scaled larger than the view and is translated out of view
    // then translate it back into view (i.e. eliminate black bars).
    protected fun center(horizontal: Boolean, vertical: Boolean) {
        if (mBitmapDisplayed.bitmap == null) {
            return
        }

        val m = getImageViewMatrix()

        val rect = RectF(0f, 0f,
                mBitmapDisplayed.bitmap!!.width.toFloat(),
                mBitmapDisplayed.bitmap!!.height.toFloat())

        m.mapRect(rect)

        val height = rect.height()
        val width = rect.width()

        var deltaX = 0f
        var deltaY = 0f

        if (vertical) {
            val viewHeight = height
            if (height < viewHeight) {
                deltaY = (viewHeight - height) / 2 - rect.top
            } else if (rect.top > 0) {
                deltaY = -rect.top
            } else if (rect.bottom < viewHeight) {
                deltaY = height - rect.bottom
            }
        }

        if (horizontal) {
            val viewWidth = width
            if (width < viewWidth) {
                deltaX = (viewWidth - width) / 2 - rect.left
            } else if (rect.left > 0) {
                deltaX = -rect.left
            } else if (rect.right < viewWidth) {
                deltaX = width - rect.right
            }
        }

        postTranslate(deltaX, deltaY)
        setImageMatrix(getImageViewMatrix())
    }

    private fun init() {
        scaleType = ScaleType.MATRIX
    }

    protected fun getValue(matrix: Matrix, whichValue: Int): Float {
        matrix.getValues(mMatrixValues)
        return mMatrixValues[whichValue]
    }

    // Get the scale factor out of the matrix.
    protected fun getScale(matrix: Matrix): Float {
        return getValue(matrix, Matrix.MSCALE_X)
    }

    protected val scale: Float
        get() = getScale(mSuppMatrix)

    // Setup the base matrix so that the image is centered and scaled properly.
    private fun getProperBaseMatrix(bitmap: RotateBitmap, matrix: Matrix) {
        val viewWidth = width.toFloat()
        val viewHeight = height.toFloat()

        val w = bitmap.width.toFloat()
        val h = bitmap.height.toFloat()
        matrix.reset()

        // We limit up-scaling to 3x otherwise the result may look bad if it's
        // a small icon.
        val widthScale = Math.min(viewWidth / w, 3.0f)
        val heightScale = Math.min(viewHeight / h, 3.0f)
        val scale = Math.min(widthScale, heightScale)

        matrix.postConcat(bitmap.rotateMatrix)
        matrix.postScale(scale, scale)

        matrix.postTranslate(
                (viewWidth - w * scale) / 2F,
                (viewHeight - h * scale) / 2F)
    }

    // Combine the base matrix and the supp matrix to make the final matrix.
    protected fun getImageViewMatrix(): Matrix {
        // The final matrix is computed as the concatentation of the base matrix
        // and the supplementary matrix.
        mDisplayMatrix.set(mBaseMatrix)
        mDisplayMatrix.postConcat(mSuppMatrix)
        return mDisplayMatrix
    }

    // Sets the maximum zoom, which is a scale relative to the base matrix. It
    // is calculated to show the image at 400% zoom regardless of screen or
    // image orientation. If in the future we decode the full 3 megapixel image,
    // rather than the current 1024x768, this should be changed down to 200%.
    protected fun maxZoom(): Float {
        if (mBitmapDisplayed.bitmap == null) {
            return 1F
        }

        val fw = mBitmapDisplayed.width.toFloat() / mThisWidth.toFloat()
        val fh = mBitmapDisplayed.height.toFloat() / mThisHeight.toFloat()
        val max = Math.max(fw, fh) * 4
        return max
    }

    protected fun zoomTo(scale: Float, centerX: Float, centerY: Float) {
        var scale = scale
        if (scale > mMaxZoom) {
            scale = mMaxZoom
        }

        val oldScale = this.scale
        val deltaScale = scale / oldScale

        mSuppMatrix.postScale(deltaScale, deltaScale, centerX, centerY)
        setImageMatrix(getImageViewMatrix())
        center(true, true)
    }

    protected fun zoomTo(scale: Float, centerX: Float, centerY: Float, durationMs: Float) {
        val incrementPerMs = (scale - this.scale) / durationMs
        val oldScale = this.scale
        val startTime = System.currentTimeMillis()

        mHandler.post(object : Runnable {
            override fun run() {
                val now = System.currentTimeMillis()
                val currentMs = Math.min(durationMs, (now - startTime).toFloat())
                val target = oldScale + incrementPerMs * currentMs
                zoomTo(target, centerX, centerY)

                if (currentMs < durationMs) {
                    mHandler.post(this)
                }
            }
        })
    }

    protected fun zoomTo(scale: Float) {
        val cx = width / 2F
        val cy = height / 2F

        zoomTo(scale, cx, cy)
    }

    protected fun zoomToPoint(scale: Float, pointX: Float, pointY: Float) {
        val cx = width / 2F
        val cy = height / 2F

        panBy(cx - pointX, cy - pointY)
        zoomTo(scale, cx, cy)
    }

    protected fun zoomIn() {
        zoomIn(SCALE_RATE)
    }

    protected fun zoomOut() {
        zoomOut(SCALE_RATE)
    }

    protected fun zoomIn(rate: Float) {
        if (scale >= mMaxZoom) {
            return     // Don't let the user zoom into the molecular level.
        }
        if (mBitmapDisplayed.bitmap == null) {
            return
        }

        val cx = width / 2F
        val cy = height / 2F

        mSuppMatrix.postScale(rate, rate, cx, cy)
        setImageMatrix(getImageViewMatrix())
    }

    protected fun zoomOut(rate: Float) {
        if (mBitmapDisplayed.bitmap == null) {
            return
        }

        val cx = width / 2F
        val cy = height / 2F

        // Zoom out to at most 1x.
        val tmp = Matrix(mSuppMatrix)
        tmp.postScale(1F / rate, 1F / rate, cx, cy)

        if (getScale(tmp) < 1F) {
            mSuppMatrix.setScale(1F, 1F, cx, cy)
        } else {
            mSuppMatrix.postScale(1F / rate, 1F / rate, cx, cy)
        }
        setImageMatrix(getImageViewMatrix())
        center(true, true)
    }

    protected fun postTranslate(dx: Float, dy: Float) {
        mSuppMatrix.postTranslate(dx, dy)
    }

    protected fun panBy(dx: Float, dy: Float) {
        postTranslate(dx, dy)
        setImageMatrix(getImageViewMatrix())
    }

    // ImageViewTouchBase will pass a Bitmap to the Recycler if it has finished
    // its use of that Bitmap.
    interface Recycler {
        fun recycle(b: Bitmap)
    }
}
