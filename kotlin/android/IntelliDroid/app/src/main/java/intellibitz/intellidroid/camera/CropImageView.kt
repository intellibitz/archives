package intellibitz.intellidroid.camera

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent

class CropImageView(context: Context, attrs: AttributeSet?) : ImageViewTouchBase(context, attrs) {
    private val mHighlightViews = ArrayList<HighlightView>()
    private var mMotionHighlightView: HighlightView? = null
    private var mLastX = 0f
    private var mLastY = 0f
    private var mMotionEdge = 0

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (mBitmapDisplayed.bitmap != null) {
            for (hv in mHighlightViews) {
                hv.matrix.set(imageMatrix)
                hv.invalidate()
                if (hv.mIsFocused) {
                    centerBasedOnHighlightView(hv)
                }
            }
        }
    }

    override fun zoomTo(scale: Float, centerX: Float, centerY: Float) {
        super.zoomTo(scale, centerX, centerY)
        for (hv in mHighlightViews) {
            hv.matrix.set(imageMatrix)
            hv.invalidate()
        }
    }

    override fun zoomIn() {
        super.zoomIn()
        for (hv in mHighlightViews) {
            hv.matrix.set(imageMatrix)
            hv.invalidate()
        }
    }

    override fun zoomOut() {
        super.zoomOut()
        for (hv in mHighlightViews) {
            hv.matrix.set(imageMatrix)
            hv.invalidate()
        }
    }

    override fun postTranslate(deltaX: Float, deltaY: Float) {
        super.postTranslate(deltaX, deltaY)
        for (i in 0 until mHighlightViews.size) {
            val hv = mHighlightViews[i]
            hv.matrix.postTranslate(deltaX, deltaY)
            hv.invalidate()
        }
    }

    // According to the event's position, change the focus to the first
    // hitting cropping rectangle.
    private fun recomputeFocus(event: MotionEvent) {
        for (i in 0 until mHighlightViews.size) {
            val hv = mHighlightViews[i]
            hv.setFocus(false)
            hv.invalidate()
        }

        for (i in 0 until mHighlightViews.size) {
            val hv = mHighlightViews[i]
            val edge = hv.getHit(event.x, event.y)
            if (edge != HighlightView.GROW_NONE) {
                if (!hv.hasFocus()) {
                    hv.setFocus(true)
                    hv.invalidate()
                }
                break
            }
        }
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val cropImage = context as CropImage
        if (cropImage.mSaving) {
            return false
        }

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (cropImage.mWaitingToPick) {
                    recomputeFocus(event)
                } else {
                    for (i in 0 until mHighlightViews.size) {
                        val hv = mHighlightViews[i]
                        val edge = hv.getHit(event.x, event.y)
                        if (edge != HighlightView.GROW_NONE) {
                            mMotionEdge = edge
                            mMotionHighlightView = hv
                            mLastX = event.x
                            mLastY = event.y
                            mMotionHighlightView?.setMode(
                                if (edge == HighlightView.MOVE)
                                    HighlightView.ModifyMode.Move
                                else
                                    HighlightView.ModifyMode.Grow
                            )
                            break
                        }
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                if (cropImage.mWaitingToPick) {
                    for (i in 0 until mHighlightViews.size) {
                        val hv = mHighlightViews[i]
                        if (hv.hasFocus()) {
                            cropImage.mCrop = hv
                            for (j in 0 until mHighlightViews.size) {
                                if (j == i) {
                                    continue
                                }
                                mHighlightViews[j].setHidden(true)
                            }
                            centerBasedOnHighlightView(hv)
                            (context as CropImage).mWaitingToPick = false
                            return true
                        }
                    }
                } else if (mMotionHighlightView != null) {
                    centerBasedOnHighlightView(mMotionHighlightView!!)
                    mMotionHighlightView?.setMode(HighlightView.ModifyMode.None)
                }
                mMotionHighlightView = null
            }
            MotionEvent.ACTION_MOVE -> {
                if (cropImage.mWaitingToPick) {
                    recomputeFocus(event)
                } else if (mMotionHighlightView != null) {
                    mMotionHighlightView?.handleMotion(
                        mMotionEdge,
                        event.x - mLastX,
                        event.y - mLastY
                    )
                    mLastX = event.x
                    mLastY = event.y

                    // This section of code is optional. It has some user
                    // benefit in that moving the crop rectangle against
                    // the edge of the screen causes scrolling but it means
                    // that the crop rectangle is no longer fixed under
                    // the user's finger.
                    ensureVisible(mMotionHighlightView!!)
                }
            }
        }

        when (event.action) {
            MotionEvent.ACTION_UP -> center(true, true)
            MotionEvent.ACTION_MOVE -> {
                // if we're not zoomed then there's no point in even allowing
                // the user to move the image around.  This call to center puts
                // it back to the normalized location (with false meaning don't
                // animate).
                if (scale == 1F) {
                    center(true, true)
                }
            }
        }

        return true
    }

    // Pan the displayed image to make sure the cropping rectangle is visible.
    private fun ensureVisible(hv: HighlightView) {
        val r = hv.mDrawRect

        val panDeltaX1 = Math.max(0, left - r.left)
        val panDeltaX2 = Math.min(0, right - r.right)

        val panDeltaY1 = Math.max(0, top - r.top)
        val panDeltaY2 = Math.min(0, bottom - r.bottom)

        val panDeltaX = if (panDeltaX1 != 0) panDeltaX1 else panDeltaX2
        val panDeltaY = if (panDeltaY1 != 0) panDeltaY1 else panDeltaY2

        if (panDeltaX != 0 || panDeltaY != 0) {
            panBy(panDeltaX, panDeltaY)
        }
    }

    // If the cropping rectangle's size changed significantly, change the
    // view's center and scale according to the cropping rectangle.
    private fun centerBasedOnHighlightView(hv: HighlightView) {
        val drawRect = hv.mDrawRect

        val width = drawRect.width().toFloat()
        val height = drawRect.height().toFloat()

        val thisWidth = width.toFloat()
        val thisHeight = height.toFloat()

        val z1 = thisWidth / width * .6F
        val z2 = thisHeight / height * .6F

        var zoom = Math.min(z1, z2)
        zoom = zoom * scale
        zoom = Math.max(1F, zoom)

        if ((Math.abs(zoom - scale) / zoom) > .1) {
            val coordinates = floatArrayOf(hv.mCropRect.centerX(), hv.mCropRect.centerY())
            imageMatrix.mapPoints(coordinates)
            zoomTo(zoom, coordinates[0], coordinates[1], 300F)
        }

        ensureVisible(hv)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (i in 0 until mHighlightViews.size) {
            mHighlightViews[i].draw(canvas)
        }
    }

    fun add(hv: HighlightView) {
        mHighlightViews.add(hv)
        invalidate()
    }
}
