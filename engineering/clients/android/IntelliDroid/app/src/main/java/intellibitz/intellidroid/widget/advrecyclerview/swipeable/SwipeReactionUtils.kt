package intellibitz.intellidroid.widget.advrecyclerview.swipeable

internal object SwipeReactionUtils {
    @JvmStatic
    fun extractLeftReaction(type: Int): Int {
        return (type ushr InternalConstants.BIT_SHIFT_AMOUNT_LEFT) and InternalConstants.REACTION_CAPABILITY_MASK
    }

    @JvmStatic
    fun extractUpReaction(type: Int): Int {
        return (type ushr InternalConstants.BIT_SHIFT_AMOUNT_UP) and InternalConstants.REACTION_CAPABILITY_MASK
    }

    @JvmStatic
    fun extractRightReaction(type: Int): Int {
        return (type ushr InternalConstants.BIT_SHIFT_AMOUNT_RIGHT) and InternalConstants.REACTION_CAPABILITY_MASK
    }

    @JvmStatic
    fun extractDownReaction(type: Int): Int {
        return (type ushr InternalConstants.BIT_SHIFT_AMOUNT_DOWN) and InternalConstants.REACTION_CAPABILITY_MASK
    }

    @JvmStatic
    fun canSwipeLeft(reactionType: Int): Boolean {
        return extractLeftReaction(reactionType) == InternalConstants.REACTION_CAN_SWIPE
    }

    @JvmStatic
    fun canSwipeUp(reactionType: Int): Boolean {
        return extractUpReaction(reactionType) == InternalConstants.REACTION_CAN_SWIPE
    }

    @JvmStatic
    fun canSwipeRight(reactionType: Int): Boolean {
        return extractRightReaction(reactionType) == InternalConstants.REACTION_CAN_SWIPE
    }

    @JvmStatic
    fun canSwipeDown(reactionType: Int): Boolean {
        return extractDownReaction(reactionType) == InternalConstants.REACTION_CAN_SWIPE
    }
}
