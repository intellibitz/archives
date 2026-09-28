package intellibitz.intellidroid.widget.advrecyclerview.swipeable.annotation

import androidx.annotation.IntDef
import intellibitz.intellidroid.widget.advrecyclerview.swipeable.SwipeableItemConstants

@IntDef(
    flag = false,
    value = [
        SwipeableItemConstants.RESULT_NONE.toLong(),
        SwipeableItemConstants.RESULT_CANCELED.toLong(),
        SwipeableItemConstants.RESULT_SWIPED_LEFT.toLong(),
        SwipeableItemConstants.RESULT_SWIPED_UP.toLong(),
        SwipeableItemConstants.RESULT_SWIPED_RIGHT.toLong(),
        SwipeableItemConstants.RESULT_SWIPED_DOWN.toLong()
    ]
)
@Retention(AnnotationRetention.SOURCE)
annotation class SwipeableItemResults
