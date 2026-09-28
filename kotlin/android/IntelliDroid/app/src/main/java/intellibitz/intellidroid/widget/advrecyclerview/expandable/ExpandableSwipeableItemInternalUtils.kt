package intellibitz.intellidroid.widget.advrecyclerview.expandable

import androidx.recyclerview.widget.RecyclerView
import intellibitz.intellidroid.widget.advrecyclerview.swipeable.action.SwipeResultAction

internal object ExpandableSwipeableItemInternalUtils {

    @JvmStatic
    @Suppress("UNCHECKED_CAST")
    fun invokeOnSwipeItem(
        adapter: BaseExpandableSwipeableItemAdapter<*, *>,
        holder: RecyclerView.ViewHolder,
        groupPosition: Int,
        childPosition: Int,
        result: Int
    ): SwipeResultAction? {
        return if (childPosition == RecyclerView.NO_POSITION) {
            (adapter as ExpandableSwipeableItemAdapter<RecyclerView.ViewHolder, RecyclerView.ViewHolder>)
                .onSwipeGroupItem(holder, groupPosition, result)
        } else {
            (adapter as ExpandableSwipeableItemAdapter<RecyclerView.ViewHolder, RecyclerView.ViewHolder>)
                .onSwipeChildItem(holder, groupPosition, childPosition, result)
        }
    }
}
