package intellibitz.intellidroid.widget.advrecyclerview.swipeable

import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import intellibitz.intellidroid.widget.advrecyclerview.swipeable.action.SwipeResultAction
import intellibitz.intellidroid.widget.advrecyclerview.utils.BaseWrapperAdapter
import intellibitz.intellidroid.widget.advrecyclerview.utils.WrapperAdapterUtils

internal class SwipeableItemWrapperAdapter<VH : RecyclerView.ViewHolder>(
    manager: RecyclerViewSwipeManager?,
    adapter: RecyclerView.Adapter<VH>
) : BaseWrapperAdapter<VH>(adapter) {

    private var mSwipeableItemAdapter: BaseSwipeableItemAdapter<RecyclerView.ViewHolder>? = null
    private var mSwipeManager: RecyclerViewSwipeManager? = null
    private var mSwipingItemId: Long = RecyclerView.NO_ID

    init {
        val swipeableAdapter = getSwipeableItemAdapter(adapter)
            ?: throw IllegalArgumentException("adapter does not implement SwipeableItemAdapter")
        mSwipeableItemAdapter = swipeableAdapter

        if (manager == null) {
            throw IllegalArgumentException("manager cannot be null")
        }
        mSwipeManager = manager
    }

    override fun onRelease() {
        super.onRelease()
        mSwipeableItemAdapter = null
        mSwipeManager = null
        mSwipingItemId = RecyclerView.NO_ID
    }

    override fun onViewRecycled(holder: VH) {
        super.onViewRecycled(holder)

        if (mSwipingItemId != RecyclerView.NO_ID && mSwipingItemId == holder.itemId) {
            mSwipeManager?.cancelSwipe()
        }

        // reset SwipeableItemViewHolder state
        if (holder is SwipeableItemViewHolder) {
            mSwipeManager?.cancelPendingAnimations(holder)

            val swipeableHolder = holder as SwipeableItemViewHolder
            swipeableHolder.setSwipeItemHorizontalSlideAmount(0f)
            swipeableHolder.setSwipeItemVerticalSlideAmount(0f)

            val containerView: View? = swipeableHolder.getSwipeableContainerView()
            if (containerView != null) {
                ViewCompat.animate(containerView).cancel()
                ViewCompat.setTranslationX(containerView, 0.0f)
                ViewCompat.setTranslationY(containerView, 0.0f)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val holder = super.onCreateViewHolder(parent, viewType)
        if (holder is SwipeableItemViewHolder) {
            holder.setSwipeStateFlags(STATE_FLAG_INITIAL_VALUE)
        }
        return holder
    }

    override fun onBindViewHolder(holder: VH, position: Int, payloads: MutableList<Any>) {
        var prevSwipeItemSlideAmount = 0f
        if (holder is SwipeableItemViewHolder) {
            prevSwipeItemSlideAmount = getSwipeItemSlideAmount(holder, swipeHorizontal())
        }

        if (isSwiping()) {
            var flags = SwipeableItemConstants.STATE_FLAG_SWIPING
            if (holder.itemId == mSwipingItemId) {
                flags = flags or SwipeableItemConstants.STATE_FLAG_IS_ACTIVE
            }
            safeUpdateFlags(holder, flags)
            super.onBindViewHolder(holder, position, payloads)
        } else {
            safeUpdateFlags(holder, 0)
            super.onBindViewHolder(holder, position, payloads)
        }

        if (holder is SwipeableItemViewHolder) {
            val swipeItemSlideAmount = getSwipeItemSlideAmount(holder, swipeHorizontal())
            val swipeManager = mSwipeManager
            if (swipeManager != null) {
                val isSwiping = swipeManager.isSwiping()
                val isAnimationRunning = swipeManager.isAnimationRunning(holder)
                if (prevSwipeItemSlideAmount != swipeItemSlideAmount || !(isSwiping || isAnimationRunning)) {
                    swipeManager.applySlideItem(
                        holder, position,
                        prevSwipeItemSlideAmount, swipeItemSlideAmount, swipeHorizontal(),
                        true, isSwiping
                    )
                }
            }
        }
    }

    override fun onHandleWrappedAdapterChanged() {
        if (isSwiping()) {
            cancelSwipe()
        } else {
            super.onHandleWrappedAdapterChanged()
        }
    }

    override fun onHandleWrappedAdapterItemRangeChanged(positionStart: Int, itemCount: Int) {
        if (isSwiping()) {
            cancelSwipe()
        } else {
            super.onHandleWrappedAdapterItemRangeChanged(positionStart, itemCount)
        }
    }

    override fun onHandleWrappedAdapterItemRangeInserted(positionStart: Int, itemCount: Int) {
        if (isSwiping()) {
            cancelSwipe()
        } else {
            super.onHandleWrappedAdapterItemRangeInserted(positionStart, itemCount)
        }
    }

    override fun onHandleWrappedAdapterItemRangeRemoved(positionStart: Int, itemCount: Int) {
        if (isSwiping()) {
            cancelSwipe()
        } else {
            super.onHandleWrappedAdapterItemRangeRemoved(positionStart, itemCount)
        }
    }

    override fun onHandleWrappedAdapterRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
        if (isSwiping()) {
            cancelSwipe()
        } else {
            super.onHandleWrappedAdapterRangeMoved(fromPosition, toPosition, itemCount)
        }
    }

    private fun cancelSwipe() {
        mSwipeManager?.cancelSwipe()
    }

    // NOTE: This method is called from RecyclerViewSwipeManager
    internal fun getSwipeReactionType(holder: RecyclerView.ViewHolder, position: Int, x: Int, y: Int): Int {
        if (LOCAL_LOGV) {
            Log.v(TAG, "getSwipeReactionType(holder = $holder, position = $position, x = $x, y = $y)")
        }
        return mSwipeableItemAdapter?.onGetSwipeReactionType(holder, position, x, y) ?: 0
    }

    // NOTE: This method is called from RecyclerViewSwipeManager
    internal fun onUpdateSlideAmount(
        holder: RecyclerView.ViewHolder, position: Int,
        horizontal: Boolean, amount: Float, isSwiping: Boolean, type: Int
    ) {
        if (LOCAL_LOGV) {
            Log.v(TAG, "onUpdateSlideAmount(holder = $holder, position = $position, horizontal = $horizontal, amount = $amount, isSwiping = $isSwiping, type = $type)")
        }
        mSwipeableItemAdapter?.onSetSwipeBackground(holder, position, type)
        (holder as? SwipeableItemViewHolder)?.onSlideAmountUpdated(
            if (horizontal) amount else 0.0f,
            if (horizontal) 0.0f else amount,
            isSwiping
        )
    }

    // NOTE: This method is called from ItemSlidingAnimator
    internal fun onUpdateSlideAmount(
        holder: RecyclerView.ViewHolder, position: Int,
        horizontal: Boolean, amount: Float, isSwiping: Boolean
    ) {
        if (LOCAL_LOGV) {
            Log.v(TAG, "onUpdateSlideAmount(holder = $holder, position = $position, horizontal = $horizontal, amount = $amount, isSwiping = $isSwiping)")
        }
        (holder as? SwipeableItemViewHolder)?.onSlideAmountUpdated(
            if (horizontal) amount else 0.0f,
            if (horizontal) 0.0f else amount,
            isSwiping
        )
    }

    // NOTE: This method is called from RecyclerViewSwipeManager
    internal fun onSwipeItemStarted(manager: RecyclerViewSwipeManager, holder: RecyclerView.ViewHolder, id: Long) {
        if (LOCAL_LOGD) {
            Log.d(TAG, "onSwipeItemStarted(holder = $holder, id = $id)")
        }
        mSwipingItemId = id
        notifyDataSetChanged()
    }

    // NOTE: This method is called from RecyclerViewSwipeManager
    internal fun onSwipeItemFinished(holder: RecyclerView.ViewHolder, position: Int, result: Int): SwipeResultAction? {
        if (LOCAL_LOGD) {
            Log.d(TAG, "onSwipeItemFinished(holder = $holder, position = $position, result = $result)")
        }
        mSwipingItemId = RecyclerView.NO_ID
        return SwipeableItemInternalUtils.invokeOnSwipeItem(mSwipeableItemAdapter, holder, position, result)
    }

    internal fun onSwipeItemFinished2(
        holder: RecyclerView.ViewHolder, position: Int,
        result: Int, afterReaction: Int, resultAction: SwipeResultAction
    ) {
        if (holder is SwipeableItemViewHolder) {
            holder.setSwipeResult(result)
            holder.setAfterSwipeReaction(afterReaction)
            setSwipeItemSlideAmount(
                holder,
                getSwipeAmountFromAfterReaction(result, afterReaction),
                swipeHorizontal()
            )
        }
        resultAction.performAction()
        notifyDataSetChanged()
    }

    protected fun isSwiping(): Boolean {
        return mSwipingItemId != RecyclerView.NO_ID
    }

    private fun swipeHorizontal(): Boolean {
        return mSwipeManager?.swipeHorizontal() ?: false
    }

    companion object {
        private const val TAG = "ARVSwipeableWrapper"
        private const val STATE_FLAG_INITIAL_VALUE = -1
        private const val LOCAL_LOGV = false
        private const val LOCAL_LOGD = false

        private fun getSwipeItemSlideAmount(holder: SwipeableItemViewHolder, horizontal: Boolean): Float {
            return if (horizontal) {
                holder.getSwipeItemHorizontalSlideAmount()
            } else {
                holder.getSwipeItemVerticalSlideAmount()
            }
        }

        private fun setSwipeItemSlideAmount(holder: SwipeableItemViewHolder, amount: Float, horizontal: Boolean) {
            if (horizontal) {
                holder.setSwipeItemHorizontalSlideAmount(amount)
            } else {
                holder.setSwipeItemVerticalSlideAmount(amount)
            }
        }

        private fun getSwipeAmountFromAfterReaction(result: Int, afterReaction: Int): Float {
            return when (afterReaction) {
                RecyclerViewSwipeManager.AFTER_SWIPE_REACTION_DEFAULT -> 0.0f
                RecyclerViewSwipeManager.AFTER_SWIPE_REACTION_MOVE_TO_SWIPED_DIRECTION,
                RecyclerViewSwipeManager.AFTER_SWIPE_REACTION_REMOVE_ITEM -> when (result) {
                    RecyclerViewSwipeManager.RESULT_SWIPED_LEFT -> RecyclerViewSwipeManager.OUTSIDE_OF_THE_WINDOW_LEFT
                    RecyclerViewSwipeManager.RESULT_SWIPED_RIGHT -> RecyclerViewSwipeManager.OUTSIDE_OF_THE_WINDOW_RIGHT
                    RecyclerViewSwipeManager.RESULT_SWIPED_UP -> RecyclerViewSwipeManager.OUTSIDE_OF_THE_WINDOW_TOP
                    RecyclerViewSwipeManager.RESULT_SWIPED_DOWN -> RecyclerViewSwipeManager.OUTSIDE_OF_THE_WINDOW_BOTTOM
                    else -> 0.0f
                }
                else -> 0.0f
            }
        }

        private fun safeUpdateFlags(holder: RecyclerView.ViewHolder, flagsParam: Int) {
            if (holder !is SwipeableItemViewHolder) {
                return
            }

            var flags = flagsParam
            val curFlags = holder.getSwipeStateFlags()
            val mask = SwipeableItemConstants.STATE_FLAG_IS_UPDATED.inv()

            // append UPDATED flag
            if (curFlags == STATE_FLAG_INITIAL_VALUE || ((curFlags xor flags) and mask) != 0) {
                flags = flags or SwipeableItemConstants.STATE_FLAG_IS_UPDATED
            }

            holder.setSwipeStateFlags(flags)
        }

        @Suppress("UNCHECKED_CAST")
        private fun getSwipeableItemAdapter(adapter: RecyclerView.Adapter<*>): BaseSwipeableItemAdapter<RecyclerView.ViewHolder>? {
            return WrapperAdapterUtils.findWrappedAdapter(adapter, BaseSwipeableItemAdapter::class.java) as? BaseSwipeableItemAdapter<RecyclerView.ViewHolder>
        }
    }
}
