package intellibitz.intellidroid.widget.advrecyclerview.swipeable.annotation

import androidx.annotation.IntDef
import intellibitz.intellidroid.widget.advrecyclerview.swipeable.SwipeableItemConstants

@IntDef(
    flag = true,
    value = [
        SwipeableItemConstants.AFTER_SWIPE_REACTION_DEFAULT.toLong(),
        SwipeableItemConstants.AFTER_SWIPE_REACTION_MOVE_TO_SWIPED_DIRECTION.toLong(),
        SwipeableItemConstants.AFTER_SWIPE_REACTION_REMOVE_ITEM.toLong()
    ]
)
@Retention(AnnotationRetention.SOURCE)
annotation class SwipeableItemAfterReactions
