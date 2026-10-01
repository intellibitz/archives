package intellibitz.intellidroid.widget.advrecyclerview.swipeable.annotation

import android.annotation.SuppressLint
import androidx.annotation.IntDef
import intellibitz.intellidroid.widget.advrecyclerview.swipeable.SwipeableItemConstants

@SuppressLint("UniqueConstants")
@IntDef(
    flag = true,
    value = [
        SwipeableItemConstants.REACTION_CAN_NOT_SWIPE_ANY.toLong(),
        SwipeableItemConstants.REACTION_CAN_NOT_SWIPE_LEFT.toLong(),
        SwipeableItemConstants.REACTION_CAN_NOT_SWIPE_LEFT_WITH_RUBBER_BAND_EFFECT.toLong(),
        SwipeableItemConstants.REACTION_CAN_SWIPE_LEFT.toLong(),
        SwipeableItemConstants.REACTION_MASK_START_SWIPE_LEFT.toLong(),
        SwipeableItemConstants.REACTION_CAN_NOT_SWIPE_UP.toLong(),
        SwipeableItemConstants.REACTION_CAN_NOT_SWIPE_UP_WITH_RUBBER_BAND_EFFECT.toLong(),
        SwipeableItemConstants.REACTION_CAN_SWIPE_UP.toLong(),
        SwipeableItemConstants.REACTION_MASK_START_SWIPE_UP.toLong(),
        SwipeableItemConstants.REACTION_CAN_NOT_SWIPE_RIGHT.toLong(),
        SwipeableItemConstants.REACTION_CAN_NOT_SWIPE_RIGHT_WITH_RUBBER_BAND_EFFECT.toLong(),
        SwipeableItemConstants.REACTION_CAN_SWIPE_RIGHT.toLong(),
        SwipeableItemConstants.REACTION_MASK_START_SWIPE_RIGHT.toLong(),
        SwipeableItemConstants.REACTION_CAN_NOT_SWIPE_DOWN.toLong(),
        SwipeableItemConstants.REACTION_CAN_NOT_SWIPE_DOWN_WITH_RUBBER_BAND_EFFECT.toLong(),
        SwipeableItemConstants.REACTION_CAN_SWIPE_DOWN.toLong(),
        SwipeableItemConstants.REACTION_MASK_START_SWIPE_DOWN.toLong(),
        SwipeableItemConstants.REACTION_CAN_NOT_SWIPE_BOTH_H.toLong(),
        SwipeableItemConstants.REACTION_CAN_NOT_SWIPE_BOTH_H_WITH_RUBBER_BAND_EFFECT.toLong(),
        SwipeableItemConstants.REACTION_CAN_SWIPE_BOTH_H.toLong(),
        SwipeableItemConstants.REACTION_CAN_NOT_SWIPE_BOTH_V.toLong(),
        SwipeableItemConstants.REACTION_CAN_NOT_SWIPE_BOTH_V_WITH_RUBBER_BAND_EFFECT.toLong(),
        SwipeableItemConstants.REACTION_CAN_SWIPE_BOTH_V.toLong(),
        SwipeableItemConstants.REACTION_CAN_NOT_SWIPE_BOTH.toLong(),
        SwipeableItemConstants.REACTION_CAN_NOT_SWIPE_BOTH_WITH_RUBBER_BAND_EFFECT.toLong(),
        SwipeableItemConstants.REACTION_CAN_SWIPE_BOTH.toLong(),
        SwipeableItemConstants.REACTION_START_SWIPE_ON_LONG_PRESS.toLong()
    ]
)
@Retention(AnnotationRetention.SOURCE)
annotation class SwipeableItemReactions
