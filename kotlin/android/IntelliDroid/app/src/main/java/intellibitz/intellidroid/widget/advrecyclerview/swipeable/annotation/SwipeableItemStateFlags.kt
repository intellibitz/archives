package intellibitz.intellidroid.widget.advrecyclerview.swipeable.annotation

import androidx.annotation.IntDef
import intellibitz.intellidroid.widget.advrecyclerview.swipeable.SwipeableItemConstants

@IntDef(
    flag = true,
    value = [
        SwipeableItemConstants.STATE_FLAG_SWIPING.toLong(),
        SwipeableItemConstants.STATE_FLAG_IS_ACTIVE.toLong(),
        SwipeableItemConstants.STATE_FLAG_IS_UPDATED.toLong()
    ]
)
@Retention(AnnotationRetention.SOURCE)
annotation class SwipeableItemStateFlags
