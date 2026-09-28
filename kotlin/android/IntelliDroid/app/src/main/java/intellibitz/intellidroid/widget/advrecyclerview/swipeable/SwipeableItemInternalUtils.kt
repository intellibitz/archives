package intellibitz.intellidroid.widget.advrecyclerview.swipeable

import androidx.recyclerview.widget.RecyclerView
import intellibitz.intellidroid.widget.advrecyclerview.swipeable.action.SwipeResultAction

object SwipeableItemInternalUtils {
    @JvmStatic
    @Suppress("UNCHECKED_CAST")
    fun invokeOnSwipeItem(
        adapter: BaseSwipeableItemAdapter<*>?,
        holder: RecyclerView.ViewHolder,
        position: Int,
        result: Int
    ): SwipeResultAction? {
        return (adapter as? SwipeableItemAdapter<RecyclerView.ViewHolder>)?.onSwipeItem(holder, position, result)
    }
}
