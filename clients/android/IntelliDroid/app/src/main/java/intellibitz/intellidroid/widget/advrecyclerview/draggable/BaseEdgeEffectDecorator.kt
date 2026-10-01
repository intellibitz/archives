package intellibitz.intellidroid.widget.advrecyclerview.draggable

import android.graphics.Canvas
import androidx.core.view.ViewCompat
import androidx.core.widget.EdgeEffectCompat
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.max

internal abstract class BaseEdgeEffectDecorator(
    private var mRecyclerView: RecyclerView?
) : RecyclerView.ItemDecoration() {

    private var mGlow1: EdgeEffectCompat? = null
    private var mGlow2: EdgeEffectCompat? = null
    private var mStarted = false
    private var mGlow1Dir = 0
    private var mGlow2Dir = 0

    companion object {
        const val EDGE_LEFT = 0
        const val EDGE_TOP = 1
        const val EDGE_RIGHT = 2
        const val EDGE_BOTTOM = 3

        private fun drawGlow(c: Canvas, parent: RecyclerView, dir: Int, edge: EdgeEffectCompat): Boolean {
            if (edge.isFinished) {
                return false
            }

            val restore = c.save()
            val clipToPadding = getClipToPadding(parent)

            when (dir) {
                EDGE_TOP -> {
                    if (clipToPadding) {
                        c.translate(parent.paddingLeft.toFloat(), parent.paddingTop.toFloat())
                    }
                }
                EDGE_BOTTOM -> {
                    c.rotate(180f)
                    if (clipToPadding) {
                        c.translate(
                            (-parent.width + parent.paddingRight).toFloat(),
                            (-parent.height + parent.paddingBottom).toFloat()
                        )
                    } else {
                        c.translate((-parent.width).toFloat(), (-parent.height).toFloat())
                    }
                }
                EDGE_LEFT -> {
                    c.rotate(-90f)
                    if (clipToPadding) {
                        c.translate(
                            (-parent.height + parent.paddingTop).toFloat(),
                            parent.paddingLeft.toFloat()
                        )
                    } else {
                        c.translate((-parent.height).toFloat(), 0f)
                    }
                }
                EDGE_RIGHT -> {
                    c.rotate(90f)
                    if (clipToPadding) {
                        c.translate(
                            parent.paddingTop.toFloat(),
                            (-parent.width + parent.paddingRight).toFloat()
                        )
                    } else {
                        c.translate(0f, (-parent.width).toFloat())
                    }
                }
            }

            val needsInvalidate = edge.draw(c)
            c.restoreToCount(restore)
            return needsInvalidate
        }

        private fun updateGlowSize(rv: RecyclerView, glow: EdgeEffectCompat, dir: Int) {
            var width = rv.measuredWidth
            var height = rv.measuredHeight

            if (getClipToPadding(rv)) {
                width -= rv.paddingLeft + rv.paddingRight
                height -= rv.paddingTop + rv.paddingBottom
            }

            width = max(0, width)
            height = max(0, height)

            if (dir == EDGE_LEFT || dir == EDGE_RIGHT) {
                val t = width
                width = height
                height = t
            }

            glow.setSize(width, height)
        }

        private fun getClipToPadding(rv: RecyclerView): Boolean {
            return rv.layoutManager?.clipToPadding ?: false
        }
    }

    protected abstract fun getEdgeDirection(no: Int): Int

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        var needsInvalidate = false

        if (mGlow1 != null) {
            needsInvalidate = needsInvalidate or drawGlow(c, parent, mGlow1Dir, mGlow1!!)
        }

        if (mGlow2 != null) {
            needsInvalidate = needsInvalidate or drawGlow(c, parent, mGlow2Dir, mGlow2!!)
        }

        if (needsInvalidate) {
            ViewCompat.postInvalidateOnAnimation(parent)
        }
    }

    fun start() {
        if (mStarted) {
            return
        }
        val rv = mRecyclerView ?: return
        mGlow1Dir = getEdgeDirection(0)
        mGlow2Dir = getEdgeDirection(1)
        rv.addItemDecoration(this)
        mStarted = true
    }

    fun finish() {
        if (mStarted && mRecyclerView != null) {
            mRecyclerView!!.removeItemDecoration(this)
        }
        releaseBothGlows()
        mRecyclerView = null
        mStarted = false
    }

    fun pullFirstEdge(deltaDistance: Float) {
        val rv = mRecyclerView ?: return
        ensureGlow1(rv)

        if (mGlow1!!.onPull(deltaDistance, 0.5f)) {
            ViewCompat.postInvalidateOnAnimation(rv)
        }
    }

    fun pullSecondEdge(deltaDistance: Float) {
        val rv = mRecyclerView ?: return
        ensureGlow2(rv)

        if (mGlow2!!.onPull(deltaDistance, 0.5f)) {
            ViewCompat.postInvalidateOnAnimation(rv)
        }
    }

    fun releaseBothGlows() {
        var needsInvalidate = false

        if (mGlow1 != null) {
            needsInvalidate = needsInvalidate or mGlow1!!.onRelease()
        }

        if (mGlow2 != null) {
            needsInvalidate = needsInvalidate or mGlow2!!.onRelease()
        }

        if (needsInvalidate && mRecyclerView != null) {
            ViewCompat.postInvalidateOnAnimation(mRecyclerView!!)
        }
    }

    private fun ensureGlow1(rv: RecyclerView) {
        if (mGlow1 == null) {
            mGlow1 = EdgeEffectCompat(rv.context)
        }
        updateGlowSize(rv, mGlow1!!, mGlow1Dir)
    }

    private fun ensureGlow2(rv: RecyclerView) {
        if (mGlow2 == null) {
            mGlow2 = EdgeEffectCompat(rv.context)
        }
        updateGlowSize(rv, mGlow2!!, mGlow2Dir)
    }

    fun reorderToTop() {
        if (mStarted && mRecyclerView != null) {
            mRecyclerView!!.removeItemDecoration(this)
            mRecyclerView!!.addItemDecoration(this)
        }
    }
}
