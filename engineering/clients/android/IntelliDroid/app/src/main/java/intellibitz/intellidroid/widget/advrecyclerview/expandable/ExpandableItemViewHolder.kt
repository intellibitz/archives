package intellibitz.intellidroid.widget.advrecyclerview.expandable

import intellibitz.intellidroid.widget.advrecyclerview.expandable.annotation.ExpandableItemStateFlags

/**
 * Interface which provides required information for expanding item.
 * Implement this interface on your sub-class of the [androidx.recyclerview.widget.RecyclerView.ViewHolder].
 */
interface ExpandableItemViewHolder {
    /**
     * Gets the state flags value for expanding item.
     * Sets the state flags value for expanding item.
     */
    @get:ExpandableItemStateFlags
    @setparam:ExpandableItemStateFlags
    var expandStateFlags: Int
}
