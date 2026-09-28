package intellibitz.intellidroid.widget.advrecyclerview.swipeable

import android.view.View

/**
 * Interface which provides required information for swiping item.
 *
 * Implement this interface on your sub-class of the [androidx.recyclerview.widget.RecyclerView.ViewHolder].
 */
interface SwipeableItemViewHolder {
    /**
     * Gets the state flags value for swiping item
     */
    fun getSwipeStateFlags(): Int

    /**
     * Sets the state flags value for swiping item
     */
    fun setSwipeStateFlags(flags: Int)

    /**
     * Gets the result code of swiping item.
     */
    fun getSwipeResult(): Int

    /**
     * Sets the result code of swiping item.
     */
    fun setSwipeResult(result: Int)

    /**
     * Gets the reaction type of after swiping item.
     */
    fun getAfterSwipeReaction(): Int

    /**
     * Sets the reaction type of after swiping item.
     */
    fun setAfterSwipeReaction(reaction: Int)

    /**
     * Gets the amount of horizontal swipe.
     */
    fun getSwipeItemHorizontalSlideAmount(): Float

    /**
     * Sets the amount of horizontal swipe.
     */
    fun setSwipeItemHorizontalSlideAmount(amount: Float)

    /**
     * Gets the amount of vertical swipe.
     */
    fun getSwipeItemVerticalSlideAmount(): Float

    /**
     * Sets the amount of vertical swipe.
     */
    fun setSwipeItemVerticalSlideAmount(amount: Float)

    /**
     * Gets the maximum item left swipe amount.
     */
    fun getMaxLeftSwipeAmount(): Float

    /**
     * Sets the maximum item left swipe amount.
     */
    fun setMaxLeftSwipeAmount(amount: Float)

    /**
     * Gets the maximum item up swipe amount.
     */
    fun getMaxUpSwipeAmount(): Float

    /**
     * Sets the maximum item up swipe amount.
     */
    fun setMaxUpSwipeAmount(amount: Float)

    /**
     * Gets the maximum item right swipe amount.
     */
    fun getMaxRightSwipeAmount(): Float

    /**
     * Sets the maximum item right swipe amount.
     */
    fun setMaxRightSwipeAmount(amount: Float)

    /**
     * Gets the maximum item down swipe amount.
     */
    fun getMaxDownSwipeAmount(): Float

    /**
     * Sets the maximum item down swipe amount.
     */
    fun setMaxDownSwipeAmount(amount: Float)

    /**
     * Gets the container view for the swipeable area.
     */
    fun getSwipeableContainerView(): View

    /**
     * Called when sets background of the swiping item.
     */
    fun onSlideAmountUpdated(horizontalAmount: Float, verticalAmount: Float, isSwiping: Boolean)
}
