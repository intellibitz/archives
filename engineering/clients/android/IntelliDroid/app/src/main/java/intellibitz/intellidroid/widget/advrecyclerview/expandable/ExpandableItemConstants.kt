package intellibitz.intellidroid.widget.advrecyclerview.expandable

interface ExpandableItemConstants {
    companion object {
        /**
         * State flag for the [ExpandableItemViewHolder.setExpandStateFlags] and [ExpandableItemViewHolder.getExpandStateFlags] methods.
         * Indicates that this ViewHolder is associated to group item.
         */
        const val STATE_FLAG_IS_GROUP = (1 shl 0)

        /**
         * State flag for the [ExpandableItemViewHolder.setExpandStateFlags] and [ExpandableItemViewHolder.getExpandStateFlags] methods.
         * Indicates that this ViewHolder is associated to child item.
         */
        const val STATE_FLAG_IS_CHILD = (1 shl 1)

        /**
         * State flag for the [ExpandableItemViewHolder.setExpandStateFlags] and [ExpandableItemViewHolder.getExpandStateFlags] methods.
         * Indicates that this is an expanded group item.
         */
        const val STATE_FLAG_IS_EXPANDED = (1 shl 2)

        /**
         * State flag for the [ExpandableItemViewHolder.setExpandStateFlags] and [ExpandableItemViewHolder.getExpandStateFlags] methods.
         * If this flag is set, the [STATE_FLAG_IS_EXPANDED] flag has changed.
         */
        const val STATE_FLAG_HAS_EXPANDED_STATE_CHANGED = (1 shl 3)

        /**
         * State flag for the [ExpandableItemViewHolder.setExpandStateFlags] and [ExpandableItemViewHolder.getExpandStateFlags] methods.
         * If this flag is set, some other flags are changed and require to apply.
         */
        const val STATE_FLAG_IS_UPDATED = (1 shl 31)
    }
}
