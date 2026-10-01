package intellibitz.intellidroid.widget.advrecyclerview.expandable

import androidx.recyclerview.widget.RecyclerView
import intellibitz.intellidroid.widget.advrecyclerview.swipeable.annotation.SwipeableItemDrawableTypes
import intellibitz.intellidroid.widget.advrecyclerview.swipeable.annotation.SwipeableItemReactions

interface BaseExpandableSwipeableItemAdapter<GVH : RecyclerView.ViewHolder, CVH : RecyclerView.ViewHolder> {

    @SwipeableItemReactions
    fun onGetGroupItemSwipeReactionType(holder: GVH, groupPosition: Int, x: Int, y: Int): Int

    @SwipeableItemReactions
    fun onGetChildItemSwipeReactionType(holder: CVH, groupPosition: Int, childPosition: Int, x: Int, y: Int): Int

    fun onSetGroupItemSwipeBackground(holder: GVH, groupPosition: Int, @SwipeableItemDrawableTypes type: Int)

    fun onSetChildItemSwipeBackground(holder: CVH, groupPosition: Int, childPosition: Int, @SwipeableItemDrawableTypes type: Int)
}
