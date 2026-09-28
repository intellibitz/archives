package intellibitz.intellidroid.widget.advrecyclerview.swipeable.annotation

import androidx.annotation.IntDef
import intellibitz.intellidroid.widget.advrecyclerview.swipeable.SwipeableItemConstants

@IntDef(
    flag = false,
    value = [
        SwipeableItemConstants.DRAWABLE_SWIPE_NEUTRAL_BACKGROUND.toLong(),
        SwipeableItemConstants.DRAWABLE_SWIPE_LEFT_BACKGROUND.toLong(),
        SwipeableItemConstants.DRAWABLE_SWIPE_UP_BACKGROUND.toLong(),
        SwipeableItemConstants.DRAWABLE_SWIPE_RIGHT_BACKGROUND.toLong(),
        SwipeableItemConstants.DRAWABLE_SWIPE_DOWN_BACKGROUND.toLong()
    ]
)
@Retention(AnnotationRetention.SOURCE)
annotation class SwipeableItemDrawableTypes
