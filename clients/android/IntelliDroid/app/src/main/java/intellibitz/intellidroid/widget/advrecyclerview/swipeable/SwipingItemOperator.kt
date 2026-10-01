package intellibitz.intellidroid.widget.advrecyclerview.swipeable

import android.view.View
import android.view.animation.Interpolator
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign

internal class SwipingItemOperator(
    manager: RecyclerViewSwipeManager,
    swipingItem: RecyclerView.ViewHolder,
    swipeReactionType: Int,
    swipeHorizontal: Boolean
) {
    private val mSwipingItemHeight: Int
    private val mSwipeHorizontal: Boolean = swipeHorizontal
    private var mSwipeManager: RecyclerViewSwipeManager? = manager
    private var mSwipingItem: RecyclerView.ViewHolder? = swipingItem
    private var mSwipingItemContainerView: View?
    private var mLeftSwipeReactionType: Int = SwipeReactionUtils.extractLeftReaction(swipeReactionType)
    private var mUpSwipeReactionType: Int = SwipeReactionUtils.extractUpReaction(swipeReactionType)
    private var mRightSwipeReactionType: Int = SwipeReactionUtils.extractRightReaction(swipeReactionType)
    private var mDownSwipeReactionType: Int = SwipeReactionUtils.extractDownReaction(swipeReactionType)
    private var mSwipingItemWidth: Int
    private var mInvSwipingItemWidth: Float
    private var mInvSwipingItemHeight: Float
    private var mSwipeDistanceX: Int = 0
    private var mSwipeDistanceY: Int = 0
    private var mPrevTranslateAmount: Float = 0f
    private var mInitialTranslateAmountX: Int = 0
    private var mInitialTranslateAmountY: Int = 0

    init {
        val containerView = (swipingItem as SwipeableItemViewHolder).getSwipeableContainerView()
        mSwipingItemContainerView = containerView
        mSwipingItemWidth = containerView.width
        mSwipingItemHeight = containerView.height
        mInvSwipingItemWidth = calcInv(mSwipingItemWidth)
        mInvSwipingItemHeight = calcInv(mSwipingItemHeight)
    }

    fun start() {
        val swipingItem = mSwipingItem ?: return
        val swipeManager = mSwipeManager ?: return

        val density = swipingItem.itemView.resources.displayMetrics.density
        val maxAmountH = max(0, mSwipingItemWidth - (density * MIN_GRABBING_AREA_SIZE).toInt())
        val maxAmountV = max(0, mSwipingItemHeight - (density * MIN_GRABBING_AREA_SIZE).toInt())

        mInitialTranslateAmountX = clip(swipeManager.getSwipeContainerViewTranslationX(swipingItem), -maxAmountH, maxAmountH)
        mInitialTranslateAmountY = clip(swipeManager.getSwipeContainerViewTranslationY(swipingItem), -maxAmountV, maxAmountV)
    }

    fun finish() {
        mSwipeManager = null
        mSwipingItem = null
        mSwipeDistanceX = 0
        mSwipeDistanceY = 0
        mSwipingItemWidth = 0
        mInvSwipingItemWidth = 0f
        mInvSwipingItemHeight = 0f
        mLeftSwipeReactionType = REACTION_CAN_NOT_SWIPE
        mUpSwipeReactionType = REACTION_CAN_NOT_SWIPE
        mRightSwipeReactionType = REACTION_CAN_NOT_SWIPE
        mDownSwipeReactionType = REACTION_CAN_NOT_SWIPE
        mPrevTranslateAmount = 0f
        mInitialTranslateAmountX = 0
        mInitialTranslateAmountY = 0
        mSwipingItemContainerView = null
    }

    fun update(itemPosition: Int, swipeDistanceX: Int, swipeDistanceY: Int) {
        val swipingItem = mSwipingItem ?: return
        val swipeManager = mSwipeManager ?: return

        if (mSwipeDistanceX == swipeDistanceX && mSwipeDistanceY == swipeDistanceY) {
            return
        }

        mSwipeDistanceX = swipeDistanceX
        mSwipeDistanceY = swipeDistanceY

        val distance = if (mSwipeHorizontal) {
            mSwipeDistanceX + mInitialTranslateAmountX
        } else {
            mSwipeDistanceY + mInitialTranslateAmountY
        }
        val itemSize = if (mSwipeHorizontal) mSwipingItemWidth else mSwipingItemHeight
        val invItemSize = if (mSwipeHorizontal) mInvSwipingItemWidth else mInvSwipingItemHeight

        val reactionType = if (mSwipeHorizontal) {
            if (distance > 0) mRightSwipeReactionType else mLeftSwipeReactionType
        } else {
            if (distance > 0) mDownSwipeReactionType else mUpSwipeReactionType
        }

        var translateAmount = 0f

        when (reactionType) {
            REACTION_CAN_NOT_SWIPE -> {
            }
            REACTION_CAN_NOT_SWIPE_WITH_RUBBER_BAND_EFFECT -> {
                val proportion = min(Math.abs(distance), itemSize) * invItemSize
                translateAmount = sign(distance.toFloat()) * RUBBER_BAND_INTERPOLATOR.getInterpolation(proportion)
            }
            REACTION_CAN_SWIPE -> {
                translateAmount = min(max(distance * invItemSize, -1.0f), 1.0f)
            }
        }

        swipeManager.applySlideItem(
            swipingItem, itemPosition,
            mPrevTranslateAmount, translateAmount, mSwipeHorizontal,
            false, true
        )

        mPrevTranslateAmount = translateAmount
    }

    companion object {
        private const val TAG = "SwipingItemOperator"
        private const val REACTION_CAN_NOT_SWIPE = InternalConstants.REACTION_CAN_NOT_SWIPE
        private const val REACTION_CAN_NOT_SWIPE_WITH_RUBBER_BAND_EFFECT = InternalConstants.REACTION_CAN_NOT_SWIPE_WITH_RUBBER_BAND_EFFECT
        private const val REACTION_CAN_SWIPE = InternalConstants.REACTION_CAN_SWIPE
        private const val RUBBER_BAND_LIMIT = 0.15f
        private const val MIN_GRABBING_AREA_SIZE = 48
        private val RUBBER_BAND_INTERPOLATOR: Interpolator = RubberBandInterpolator(RUBBER_BAND_LIMIT)

        private fun calcInv(value: Int): Float {
            return if (value != 0) 1.0f / value else 0.0f
        }

        private fun clip(v: Int, minVal: Int, maxVal: Int): Int {
            return min(max(v, minVal), maxVal)
        }
    }
}
