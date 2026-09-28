package intellibitz.intellidroid.widget.advrecyclerview.draggable

interface DraggableItemConstants {
    companion object {
        /**
         * State flag for the [DraggableItemViewHolder.setDragStateFlags] and [DraggableItemViewHolder.getDragStateFlags] methods.
         * Indicates that currently performing dragging.
         */
        const val STATE_FLAG_DRAGGING = (1 shl 0)

        /**
         * State flag for the [DraggableItemViewHolder.setDragStateFlags] and [DraggableItemViewHolder.getDragStateFlags] methods.
         * Indicates that this item is being dragged.
         */
        const val STATE_FLAG_IS_ACTIVE = (1 shl 1)

        /**
         * State flag for the [DraggableItemViewHolder.setDragStateFlags] and [DraggableItemViewHolder.getDragStateFlags] methods.
         * Indicates that this item is in the range of drag-sortable items
         */
        const val STATE_FLAG_IS_IN_RANGE = (1 shl 2)

        /**
         * State flag for the [DraggableItemViewHolder.setDragStateFlags] and [DraggableItemViewHolder.getDragStateFlags] methods.
         * If this flag is set, some other flags are changed and require to apply.
         */
        const val STATE_FLAG_IS_UPDATED = (1 shl 31)
    }
}
