package intellibitz.intellidroid.widget.advrecyclerview.swipeable

import androidx.recyclerview.widget.RecyclerView
import intellibitz.intellidroid.widget.advrecyclerview.swipeable.action.SwipeResultAction
import intellibitz.intellidroid.widget.advrecyclerview.swipeable.annotation.SwipeableItemResults

interface SwipeableItemAdapter<T : RecyclerView.ViewHolder> : BaseSwipeableItemAdapter<T> {
    /**
     * Called when item is swiped.
     *
     * *Note that do not change the data set and do not call notifyDataXXX() methods inside of this method.*
     *
     * @param holder   The ViewHolder which is associated to the swiped item.
     * @param position The position of the item within the adapter's data set.
     * @param result   The result code of user's swipe operation.
     * @return Result action.
     */
    fun onSwipeItem(holder: T, position: Int, @SwipeableItemResults result: Int): SwipeResultAction?
}
