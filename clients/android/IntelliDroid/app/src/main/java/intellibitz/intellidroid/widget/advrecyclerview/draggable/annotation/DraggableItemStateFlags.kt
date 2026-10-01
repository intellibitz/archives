package intellibitz.intellidroid.widget.advrecyclerview.draggable.annotation

import androidx.annotation.IntDef
import intellibitz.intellidroid.widget.advrecyclerview.draggable.DraggableItemConstants

@IntDef(
    flag = true,
    value = [
        DraggableItemConstants.STATE_FLAG_DRAGGING.toLong(),
        DraggableItemConstants.STATE_FLAG_IS_ACTIVE.toLong(),
        DraggableItemConstants.STATE_FLAG_IS_IN_RANGE.toLong(),
        DraggableItemConstants.STATE_FLAG_IS_UPDATED.toLong()
    ]
)
@Retention(AnnotationRetention.SOURCE)
annotation class DraggableItemStateFlags
