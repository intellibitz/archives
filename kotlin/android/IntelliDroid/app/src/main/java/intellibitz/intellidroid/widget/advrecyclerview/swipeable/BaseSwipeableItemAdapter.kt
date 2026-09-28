package intellibitz.intellidroid.widget.advrecyclerview.swipeable

import androidx.recyclerview.widget.RecyclerView
import intellibitz.intellidroid.widget.advrecyclerview.swipeable.annotation.SwipeableItemDrawableTypes
import intellibitz.intellidroid.widget.advrecyclerview.swipeable.annotation.SwipeableItemReactions

interface BaseSwipeableItemAdapter<T : RecyclerView.ViewHolder> {
    /**
     * Called when user is attempt to swipe the item.
     *
     * @param holder   The ViewHolder which is associated to item user is attempt to start swiping.
     * @param position The position of the item within the adapter's data set.
     * @param x        Touched X position. Relative from the itemView's top-left.
     * @param y        Touched Y position. Relative from the itemView's top-left.
     * @return Reaction type. Bitwise OR of flags in [SwipeableItemConstants]
     */
    @SwipeableItemReactions
    fun onGetSwipeReactionType(holder: T, position: Int, x: Int, y: Int): Int

    /**
     * Called when sets background of the swiping item.
     *
     * @param holder   The ViewHolder which is associated to the swiping item.
     * @param position The position of the item within the adapter's data set.
     * @param type     Background type. One of the DRAWABLE_SWIPE_* constants in [SwipeableItemConstants].
     */
    fun onSetSwipeBackground(holder: T, position: Int, @SwipeableItemDrawableTypes type: Int)
}
