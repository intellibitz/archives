package intellibitz.intellidroid.widget.advrecyclerview.draggable

import intellibitz.intellidroid.widget.advrecyclerview.draggable.annotation.DraggableItemStateFlags

/**
 * Interface which provides required information for dragging item.
 * Implement this interface on your sub-class of the [androidx.recyclerview.widget.RecyclerView.ViewHolder].
 */
interface DraggableItemViewHolder {
    /**
     * Gets or sets the state flags value for dragging item.
     */
    @get:DraggableItemStateFlags
    @setparam:DraggableItemStateFlags
    var dragStateFlags: Int
}
