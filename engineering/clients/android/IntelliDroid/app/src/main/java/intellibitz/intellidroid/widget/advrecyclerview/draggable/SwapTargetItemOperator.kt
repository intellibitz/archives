package intellibitz.intellidroid.widget.advrecyclerview.draggable

import android.graphics.Canvas
import android.graphics.Rect
import android.view.animation.Interpolator
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import intellibitz.intellidroid.widget.advrecyclerview.utils.CustomRecyclerViewUtils
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

internal class SwapTargetItemOperator(
    recyclerView: RecyclerView,
    draggingItem: RecyclerView.ViewHolder,
    range: ItemDraggableRange?,
    draggingItemInfo: DraggingItemInfo
) : BaseDraggableItemDecorator(recyclerView, draggingItem) {

    private val mSwapTargetDecorationOffsets = Rect()
    private val mSwapTargetItemMargins = Rect()
    private val mDraggingItemDecorationOffsets = Rect()
    private var mSwapTargetItem: RecyclerView.ViewHolder? = null
    private var mSwapTargetTranslationInterpolator: Interpolator? = null
    private var mTranslationX = 0
    private var mTranslationY = 0
    private var mStarted = false
    private var mReqTranslationPhase = 0f
    private var mCurTranslationPhase = 0f
    private var mDraggingItemInfo: DraggingItemInfo? = draggingItemInfo
    private var mRange: ItemDraggableRange? = range
    private var mSwapTargetItemChanged = false

    init {
        CustomRecyclerViewUtils.getDecorationOffsets(
            mRecyclerView.layoutManager, mDraggingItemViewHolder!!.itemView, mDraggingItemDecorationOffsets
        )
    }

    companion object {
        private const val TAG = "SwapTargetItemOperator"

        private fun calculateCurrentTranslationPhase(cur: Float, req: Float): Float {
            val a = 0.3f
            val b = 0.01f
            val tmp = (cur * (1.0f - a)) + (req * a)
            return if (abs(tmp - req) < b) req else tmp
        }
    }

    fun setSwapTargetTranslationInterpolator(interpolator: Interpolator?) {
        mSwapTargetTranslationInterpolator = interpolator
    }

    fun setSwapTargetItem(swapTargetItem: RecyclerView.ViewHolder?) {
        if (mSwapTargetItem === swapTargetItem) {
            return
        }

        if (mSwapTargetItem != null) {
            ViewCompat.animate(mSwapTargetItem!!.itemView).translationX(0f).translationY(0f).setDuration(10).start()
        }

        mSwapTargetItem = swapTargetItem
        mSwapTargetItemChanged = true
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val draggingItem = mDraggingItemViewHolder
        val swapTargetItem = mSwapTargetItem
        val info = mDraggingItemInfo

        if (draggingItem == null || swapTargetItem == null || info == null || draggingItem.itemId != info.id) {
            return
        }

        mReqTranslationPhase = calculateTranslationPhase(draggingItem, swapTargetItem)

        if (mSwapTargetItemChanged) {
            mSwapTargetItemChanged = false
            mCurTranslationPhase = mReqTranslationPhase
        } else {
            mCurTranslationPhase = calculateCurrentTranslationPhase(mCurTranslationPhase, mReqTranslationPhase)
        }

        updateSwapTargetTranslation(draggingItem, swapTargetItem, mCurTranslationPhase)
    }

    private fun calculateTranslationPhase(draggingItem: RecyclerView.ViewHolder, swapTargetItem: RecyclerView.ViewHolder): Float {
        val swapItemView = swapTargetItem.itemView

        val pos1 = draggingItem.layoutPosition
        val pos2 = swapTargetItem.layoutPosition

        CustomRecyclerViewUtils.getDecorationOffsets(
            mRecyclerView.layoutManager, swapItemView, mSwapTargetDecorationOffsets
        )
        CustomRecyclerViewUtils.getLayoutMargins(swapItemView, mSwapTargetItemMargins)

        val m2 = mSwapTargetItemMargins
        val d2 = mSwapTargetDecorationOffsets
        val h2 = swapItemView.height + m2.top + m2.bottom + d2.top + d2.bottom
        val w2 = swapItemView.width + m2.left + m2.right + d2.left + d2.right

        val offsetXPx = draggingItem.itemView.left - mTranslationX
        val phaseX = if (w2 != 0) (offsetXPx.toFloat() / w2) else 0.0f
        val offsetYPx = draggingItem.itemView.top - mTranslationY
        val phaseY = if (h2 != 0) (offsetYPx.toFloat() / h2) else 0.0f

        var translationPhase = 0.0f
        val orientation = CustomRecyclerViewUtils.getOrientation(mRecyclerView)

        if (orientation == CustomRecyclerViewUtils.ORIENTATION_VERTICAL) {
            translationPhase = if (pos1 > pos2) {
                phaseY
            } else {
                1.0f + phaseY
            }
        } else if (orientation == CustomRecyclerViewUtils.ORIENTATION_HORIZONTAL) {
            translationPhase = if (pos1 > pos2) {
                phaseX
            } else {
                1.0f + phaseX
            }
        }

        return min(max(translationPhase, 0.0f), 1.0f)
    }

    private fun updateSwapTargetTranslation(
        draggingItem: RecyclerView.ViewHolder,
        swapTargetItem: RecyclerView.ViewHolder,
        translationPhase: Float
    ) {
        val swapItemView = swapTargetItem.itemView

        val pos1 = draggingItem.layoutPosition
        val pos2 = swapTargetItem.layoutPosition

        val info = mDraggingItemInfo ?: return
        val m1 = info.margins
        val d1 = mDraggingItemDecorationOffsets
        val h1 = info.height + m1.top + m1.bottom + d1.top + d1.bottom
        val w1 = info.width + m1.left + m1.right + d1.left + d1.right

        var phase = translationPhase
        if (mSwapTargetTranslationInterpolator != null) {
            phase = mSwapTargetTranslationInterpolator!!.getInterpolation(phase)
        }

        when (CustomRecyclerViewUtils.getOrientation(mRecyclerView)) {
            CustomRecyclerViewUtils.ORIENTATION_VERTICAL -> {
                if (pos1 > pos2) {
                    ViewCompat.setTranslationY(swapItemView, phase * h1)
                } else {
                    ViewCompat.setTranslationY(swapItemView, (phase - 1.0f) * h1)
                }
            }
            CustomRecyclerViewUtils.ORIENTATION_HORIZONTAL -> {
                if (pos1 > pos2) {
                    ViewCompat.setTranslationX(swapItemView, phase * w1)
                } else {
                    ViewCompat.setTranslationX(swapItemView, (phase - 1.0f) * w1)
                }
            }
        }
    }

    fun start() {
        if (mStarted) {
            return
        }

        mRecyclerView.addItemDecoration(this, 0)
        mStarted = true
    }

    fun finish(animate: Boolean) {
        if (mStarted) {
            mRecyclerView.removeItemDecoration(this)
        }

        val itemAnimator = mRecyclerView.itemAnimator
        itemAnimator?.endAnimations()
        mRecyclerView.stopScroll()

        if (mSwapTargetItem != null) {
            if (mDraggingItemViewHolder != null) {
                updateSwapTargetTranslation(mDraggingItemViewHolder!!, mSwapTargetItem!!, mCurTranslationPhase)
            }
            moveToDefaultPosition(mSwapTargetItem!!.itemView, animate)
            mSwapTargetItem = null
        }

        mRange = null
        mDraggingItemViewHolder = null
        mTranslationX = 0
        mTranslationY = 0
        mCurTranslationPhase = 0.0f
        mReqTranslationPhase = 0.0f
        mStarted = false
        mDraggingItemInfo = null
    }

    fun update(translationX: Int, translationY: Int) {
        mTranslationX = translationX
        mTranslationY = translationY
    }
}
