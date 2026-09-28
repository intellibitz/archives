package intellibitz.intellidroid.widget.advrecyclerview.expandable.annotation

import androidx.annotation.IntDef
import intellibitz.intellidroid.widget.advrecyclerview.expandable.ExpandableItemConstants

@IntDef(
    flag = true,
    value = [
        ExpandableItemConstants.STATE_FLAG_IS_GROUP.toLong(),
        ExpandableItemConstants.STATE_FLAG_IS_CHILD.toLong(),
        ExpandableItemConstants.STATE_FLAG_IS_EXPANDED.toLong(),
        ExpandableItemConstants.STATE_FLAG_HAS_EXPANDED_STATE_CHANGED.toLong(),
        ExpandableItemConstants.STATE_FLAG_IS_UPDATED.toLong()
    ]
)
@Retention(AnnotationRetention.SOURCE)
annotation class ExpandableItemStateFlags
