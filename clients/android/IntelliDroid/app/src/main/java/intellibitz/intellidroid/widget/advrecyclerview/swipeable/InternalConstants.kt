package intellibitz.intellidroid.widget.advrecyclerview.swipeable

internal object InternalConstants {
    // bit: 0-5   : LEFT
    // bit: 6-11  : UP
    // bit: 12-17 : RIGHT
    // bit: 18-23 : DOWN
    // bit: 24    : REACTION_START_SWIPE_ON_LONG_PRESS
    const val BIT_SHIFT_AMOUNT_LEFT = 0
    const val BIT_SHIFT_AMOUNT_UP = 6
    const val BIT_SHIFT_AMOUNT_RIGHT = 12
    const val BIT_SHIFT_AMOUNT_DOWN = 18

    const val REACTION_CAN_NOT_SWIPE = 0
    const val REACTION_CAN_NOT_SWIPE_WITH_RUBBER_BAND_EFFECT = 1
    const val REACTION_CAN_SWIPE = 2
    const val REACTION_MASK_START_SWIPE = 8

    const val REACTION_START_SWIPE_ON_LONG_PRESS = 1 shl 24

    const val REACTION_CAPABILITY_MASK = 0x3
}
