package intellibitz.intellidroid.widget.advrecyclerview.draggable

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.NinePatchDrawable
import android.view.MotionEvent
import android.view.View
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import intellibitz.intellidroid.widget.advrecyclerview.utils.CustomRecyclerViewUtils
import kotlin.math.max
import kotlin.math.min

internal class DraggingItemDecorator(
    recyclerView: RecyclerView,
    draggingItem: RecyclerView.ViewHolder,
    range: ItemDraggableRange?
) : BaseDraggableItemDecorator(recyclerView, draggingItem) {

    private val mShadowPadding = Rect()
    private var mTranslationX = 0
    private var mTranslationY = 0
    private var mDraggingItemImage: Bitmap? = null
    private var mTranslationLeftLimit = 0
    private var mTranslationRightLimit = 0
    private var mTranslationTopLimit = 0
    private var mTranslationBottomLimit = 0
    private var mTouchPositionX = 0
    private var mTouchPositionY = 0
    private var mShadowDrawable: NinePatchDrawable? = null
    private var mStarted = false
    private var mIsScrolling = false
    private var mRange: ItemDraggableRange? = range
    private var mLayoutOrientation = 0
    private var mDraggingItemInfo: DraggingItemInfo? = null

    companion object {
        private const val TAG = "DraggingItemDecorator"

        private fun clip(value: Int, min: Int, max: Int): Int {
            return min(max(value, min), max)
        }

        private fun findRangeFirstItem(
            rv: RecyclerView,
            range: ItemDraggableRange?,
            firstVisiblePosition: Int,
            lastVisiblePosition: Int
        ): View? {
            if (firstVisiblePosition == RecyclerView.NO_POSITION || lastVisiblePosition == RecyclerView.NO_POSITION || range == null) {
                return null
            }

            var v: View? = null
            val childCount = rv.childCount
            for (i in 0 until childCount) {
                val v2 = rv.getChildAt(i)
                val vh = rv.getChildViewHolder(v2)

                if (vh != null) {
                    val position = vh.layoutPosition
                    if (position in firstVisiblePosition..lastVisiblePosition && range.checkInRange(position)) {
                        v = v2
                        break
                    }
                }
            }
            return v
        }

        private fun findRangeLastItem(
            rv: RecyclerView,
            range: ItemDraggableRange?,
            firstVisiblePosition: Int,
            lastVisiblePosition: Int
        ): View? {
            if (firstVisiblePosition == RecyclerView.NO_POSITION || lastVisiblePosition == RecyclerView.NO_POSITION || range == null) {
                return null
            }

            var v: View? = null
            val childCount = rv.childCount
            for (i in childCount - 1 downTo 0) {
                val v2 = rv.getChildAt(i)
                val vh = rv.getChildViewHolder(v2)

                if (vh != null) {
                    val position = vh.layoutPosition
                    if (position in firstVisiblePosition..lastVisiblePosition && range.checkInRange(position)) {
                        v = v2
                        break
                    }
                }
            }
            return v
        }

        private fun toSpanAlignedPosition(position: Int, spanCount: Int): Int {
            if (position == RecyclerView.NO_POSITION) {
                return RecyclerView.NO_POSITION
            }
            return (position / spanCount) * spanCount
        }
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val image = mDraggingItemImage
        if (image != null) {
            val left = (mTranslationX - mShadowPadding.left).toFloat()
            val top = (mTranslationY - mShadowPadding.top).toFloat()
            c.drawBitmap(image, left, top, null)
        }
    }

    fun start(e: MotionEvent, draggingItemInfo: DraggingItemInfo) {
        if (mStarted) {
            return
        }

        val holder = mDraggingItemViewHolder ?: return
        val itemView = holder.itemView

        mDraggingItemInfo = draggingItemInfo
        mDraggingItemImage = createDraggingItemImage(itemView, mShadowDrawable)

        mTranslationLeftLimit = mRecyclerView.paddingLeft
        mTranslationTopLimit = mRecyclerView.paddingTop
        mLayoutOrientation = CustomRecyclerViewUtils.getOrientation(mRecyclerView)

        itemView.visibility = View.INVISIBLE

        update(e, true)
        mRecyclerView.addItemDecoration(this)
        mStarted = true
    }

    fun finish(animate: Boolean) {
        if (mStarted) {
            mRecyclerView.removeItemDecoration(this)
        }

        val itemAnimator = mRecyclerView.itemAnimator
        itemAnimator?.endAnimations()
        mRecyclerView.stopScroll()

        updateDraggingItemPosition(mTranslationX.toFloat(), mTranslationY)
        if (mDraggingItemViewHolder != null) {
            moveToDefaultPosition(mDraggingItemViewHolder!!.itemView, animate)
            mDraggingItemViewHolder!!.itemView.visibility = View.VISIBLE
        }
        mDraggingItemViewHolder = null

        if (mDraggingItemImage != null) {
            mDraggingItemImage!!.recycle()
            mDraggingItemImage = null
        }

        mRange = null
        mTranslationX = 0
        mTranslationY = 0
        mTranslationLeftLimit = 0
        mTranslationRightLimit = 0
        mTranslationTopLimit = 0
        mTranslationBottomLimit = 0
        mTouchPositionX = 0
        mTouchPositionY = 0
        mStarted = false
    }

    fun update(e: MotionEvent, force: Boolean): Boolean {
        mTouchPositionX = (e.x + 0.5f).toInt()
        mTouchPositionY = (e.y + 0.5f).toInt()
        return refresh(force)
    }

    fun refresh(force: Boolean): Boolean {
        val prevTranslationX = mTranslationX
        val prevTranslationY = mTranslationY

        updateTranslationOffset()

        val updated = (prevTranslationX != mTranslationX) || (prevTranslationY != mTranslationY)

        if (updated || force) {
            updateDraggingItemPosition(mTranslationX.toFloat(), mTranslationY)
            ViewCompat.postInvalidateOnAnimation(mRecyclerView)
        }

        return updated
    }

    fun setShadowDrawable(shadowDrawable: NinePatchDrawable?) {
        mShadowDrawable = shadowDrawable
        mShadowDrawable?.getPadding(mShadowPadding)
    }

    fun getDraggingItemTranslationY(): Int {
        return mTranslationY
    }

    fun getDraggingItemTranslationX(): Int {
        return mTranslationX
    }

    fun getDraggingItemMoveOffsetY(): Int {
        return mTranslationY - (mDraggingItemInfo?.initialItemTop ?: 0)
    }

    fun getDraggingItemMoveOffsetX(): Int {
        return mTranslationX - (mDraggingItemInfo?.initialItemLeft ?: 0)
    }

    private fun updateTranslationOffset() {
        val rv = mRecyclerView
        val childCount = rv.childCount
        val info = mDraggingItemInfo

        if (childCount > 0 && info != null) {
            mTranslationLeftLimit = 0
            mTranslationRightLimit = rv.width - info.width

            mTranslationTopLimit = 0
            mTranslationBottomLimit = rv.height - info.height

            when (mLayoutOrientation) {
                CustomRecyclerViewUtils.ORIENTATION_VERTICAL -> {
                    mTranslationLeftLimit += rv.paddingLeft
                    mTranslationRightLimit -= rv.paddingRight
                }
                CustomRecyclerViewUtils.ORIENTATION_HORIZONTAL -> {
                    mTranslationTopLimit += rv.paddingTop
                    mTranslationBottomLimit -= rv.paddingBottom
                }
            }

            mTranslationRightLimit = max(mTranslationLeftLimit, mTranslationRightLimit)
            mTranslationBottomLimit = max(mTranslationTopLimit, mTranslationBottomLimit)

            if (!mIsScrolling) {
                val firstVisiblePosition = CustomRecyclerViewUtils.findFirstVisibleItemPosition(rv, true)
                val lastVisiblePosition = CustomRecyclerViewUtils.findLastVisibleItemPosition(rv, true)
                val firstChild = findRangeFirstItem(rv, mRange, firstVisiblePosition, lastVisiblePosition)
                val lastChild = findRangeLastItem(rv, mRange, firstVisiblePosition, lastVisiblePosition)

                when (mLayoutOrientation) {
                    CustomRecyclerViewUtils.ORIENTATION_VERTICAL -> {
                        if (firstChild != null) {
                            mTranslationTopLimit = min(mTranslationBottomLimit, firstChild.top)
                        }

                        if (lastChild != null) {
                            val limit = max(0, lastChild.bottom - info.height)
                            mTranslationBottomLimit = min(mTranslationBottomLimit, limit)
                        }
                    }
                    CustomRecyclerViewUtils.ORIENTATION_HORIZONTAL -> {
                        if (firstChild != null) {
                            mTranslationLeftLimit = min(mTranslationLeftLimit, firstChild.left)
                        }

                        if (lastChild != null) {
                            val limit = max(0, lastChild.right - info.width)
                            mTranslationRightLimit = min(mTranslationRightLimit, limit)
                        }
                    }
                }
            }
        } else {
            mTranslationLeftLimit = rv.paddingLeft
            mTranslationRightLimit = mTranslationLeftLimit
            mTranslationTopLimit = rv.paddingTop
            mTranslationBottomLimit = mTranslationTopLimit
        }

        val grabbedX = info?.grabbedPositionX ?: 0
        val grabbedY = info?.grabbedPositionY ?: 0

        mTranslationX = mTouchPositionX - grabbedX
        mTranslationY = mTouchPositionY - grabbedY

        mTranslationX = clip(mTranslationX, mTranslationLeftLimit, mTranslationRightLimit)
        mTranslationY = clip(mTranslationY, mTranslationTopLimit, mTranslationBottomLimit)
    }

    fun isReachedToTopLimit(): Boolean {
        return mTranslationY == mTranslationTopLimit
    }

    fun isReachedToBottomLimit(): Boolean {
        return mTranslationY == mTranslationBottomLimit
    }

    fun isReachedToLeftLimit(): Boolean {
        return mTranslationX == mTranslationLeftLimit
    }

    fun isReachedToRightLimit(): Boolean {
        return mTranslationX == mTranslationRightLimit
    }

    private fun createDraggingItemImage(v: View, shadow: NinePatchDrawable?): Bitmap {
        val width = v.width + mShadowPadding.left + mShadowPadding.right
        val height = v.height + mShadowPadding.top + mShadowPadding.bottom

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        if (shadow != null) {
            shadow.setBounds(0, 0, width, height)
            shadow.draw(canvas)
        }

        val savedCount = 1
        canvas.clipRect(
            mShadowPadding.left,
            mShadowPadding.top,
            width - mShadowPadding.right,
            height - mShadowPadding.bottom
        )
        canvas.translate(mShadowPadding.left.toFloat(), mShadowPadding.top.toFloat())
        v.draw(canvas)
        canvas.restoreToCount(savedCount)

        return bitmap
    }

    private fun updateDraggingItemPosition(translationX: Float, translationY: Int) {
        val holder = mDraggingItemViewHolder
        if (holder != null) {
            setItemTranslation(
                mRecyclerView, holder,
                translationX - holder.itemView.left,
                translationY.toFloat() - holder.itemView.top
            )
        }
    }

    fun setIsScrolling(isScrolling: Boolean) {
        if (mIsScrolling == isScrolling) {
            return
        }
        mIsScrolling = isScrolling
    }

    fun getTranslatedItemPositionTop(): Int {
        return mTranslationY
    }

    fun getTranslatedItemPositionBottom(): Int {
        return mTranslationY + (mDraggingItemInfo?.height ?: 0)
    }

    fun getTranslatedItemPositionLeft(): Int {
        return mTranslationX
    }

    fun getTranslatedItemPositionRight(): Int {
        return mTranslationX + (mDraggingItemInfo?.width ?: 0)
    }

    fun invalidateDraggingItem() {
        if (mDraggingItemViewHolder != null) {
            ViewCompat.setTranslationX(mDraggingItemViewHolder!!.itemView, 0f)
            ViewCompat.setTranslationY(mDraggingItemViewHolder!!.itemView, 0f)
            mDraggingItemViewHolder!!.itemView.visibility = View.VISIBLE
        }
        mDraggingItemViewHolder = null
    }

    fun setDraggingItemViewHolder(holder: RecyclerView.ViewHolder) {
        check(mDraggingItemViewHolder == null) {
            "A new view holder is attempt to be assigned before invalidating the older one"
        }
        mDraggingItemViewHolder = holder
        holder.itemView.visibility = View.INVISIBLE
    }
}
