package intellibitz.intellidroid.widget.advrecyclerview.utils

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import intellibitz.intellidroid.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager
import intellibitz.intellidroid.widget.advrecyclerview.swipeable.SwipeableItemViewHolder
import intellibitz.intellidroid.widget.advrecyclerview.swipeable.annotation.SwipeableItemReactions
import intellibitz.intellidroid.widget.advrecyclerview.swipeable.annotation.SwipeableItemResults
import intellibitz.intellidroid.widget.advrecyclerview.swipeable.annotation.SwipeableItemStateFlags

abstract class AbstractSwipeableItemViewHolder(itemView: View) :
    RecyclerView.ViewHolder(itemView), SwipeableItemViewHolder {

    @SwipeableItemStateFlags
    private var mSwipeStateFlags = 0
    @SwipeableItemResults
    private var mSwipeResult = RecyclerViewSwipeManager.RESULT_NONE
    @SwipeableItemReactions
    private var mAfterSwipeReaction = RecyclerViewSwipeManager.AFTER_SWIPE_REACTION_DEFAULT
    private var mHorizontalSwipeAmount = 0f
    private var mVerticalSwipeAmount = 0f
    private var mMaxLeftSwipeAmount = RecyclerViewSwipeManager.OUTSIDE_OF_THE_WINDOW_LEFT
    private var mMaxUpSwipeAmount = RecyclerViewSwipeManager.OUTSIDE_OF_THE_WINDOW_TOP
    private var mMaxRightSwipeAmount = RecyclerViewSwipeManager.OUTSIDE_OF_THE_WINDOW_RIGHT
    private var mMaxDownSwipeAmount = RecyclerViewSwipeManager.OUTSIDE_OF_THE_WINDOW_BOTTOM

    @SwipeableItemStateFlags
    override fun getSwipeStateFlags(): Int {
        return mSwipeStateFlags
    }

    override fun setSwipeStateFlags(@SwipeableItemStateFlags flags: Int) {
        mSwipeStateFlags = flags
    }

    @SwipeableItemResults
    override fun getSwipeResult(): Int {
        return mSwipeResult
    }

    override fun setSwipeResult(@SwipeableItemResults result: Int) {
        mSwipeResult = result
    }

    @SwipeableItemReactions
    override fun getAfterSwipeReaction(): Int {
        return mAfterSwipeReaction
    }

    override fun setAfterSwipeReaction(@SwipeableItemReactions reaction: Int) {
        mAfterSwipeReaction = reaction
    }

    override fun getSwipeItemVerticalSlideAmount(): Float {
        return mVerticalSwipeAmount
    }

    override fun setSwipeItemVerticalSlideAmount(amount: Float) {
        mVerticalSwipeAmount = amount
    }

    override fun getSwipeItemHorizontalSlideAmount(): Float {
        return mHorizontalSwipeAmount
    }

    override fun setSwipeItemHorizontalSlideAmount(amount: Float) {
        mHorizontalSwipeAmount = amount
    }

    abstract override fun getSwipeableContainerView(): View

    override fun getMaxLeftSwipeAmount(): Float {
        return mMaxLeftSwipeAmount
    }

    override fun setMaxLeftSwipeAmount(amount: Float) {
        mMaxLeftSwipeAmount = amount
    }

    override fun getMaxUpSwipeAmount(): Float {
        return mMaxUpSwipeAmount
    }

    override fun setMaxUpSwipeAmount(amount: Float) {
        mMaxUpSwipeAmount = amount
    }

    override fun getMaxRightSwipeAmount(): Float {
        return mMaxRightSwipeAmount
    }

    override fun setMaxRightSwipeAmount(amount: Float) {
        mMaxRightSwipeAmount = amount
    }

    override fun getMaxDownSwipeAmount(): Float {
        return mMaxDownSwipeAmount
    }

    override fun setMaxDownSwipeAmount(amount: Float) {
        mMaxDownSwipeAmount = amount
    }

    override fun onSlideAmountUpdated(horizontalAmount: Float, verticalAmount: Float, isSwiping: Boolean) {
    }
}
