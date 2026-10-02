package intellibitz.intellidroid.widget.advrecyclerview.swipeable

import android.graphics.Rect
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import androidx.core.view.MotionEventCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import intellibitz.intellidroid.widget.advrecyclerview.animator.SwipeDismissItemAnimator
import intellibitz.intellidroid.widget.advrecyclerview.swipeable.action.SwipeResultAction
import intellibitz.intellidroid.widget.advrecyclerview.swipeable.action.SwipeResultActionDefault
import intellibitz.intellidroid.widget.advrecyclerview.utils.CustomRecyclerViewUtils
import intellibitz.intellidroid.widget.advrecyclerview.utils.WrapperAdapterUtils

/**
 * Provides item swipe operation for [RecyclerView]
 */
class RecyclerViewSwipeManager : SwipeableItemConstants {

    private val mSwipingItemMargins = Rect()
    private var mInternalUseOnItemTouchListener: RecyclerView.OnItemTouchListener?
    private var mRecyclerView: RecyclerView? = null

    private var mReturnToDefaultPositionAnimationDuration: Long = 300
    private var mMoveToOutsideWindowAnimationDuration: Long = 200

    private var mTouchSlop: Int = 0
    private var mMinFlingVelocity: Int = 0
    private var mMaxFlingVelocity: Int = 0
    private var mInitialTouchX: Int = 0
    private var mInitialTouchY: Int = 0
    private var mCheckingTouchSlop: Long = RecyclerView.NO_ID
    private var mSwipeHorizontal: Boolean = false

    private var mItemSlideAnimator: ItemSlidingAnimator? = null
    private var mAdapter: SwipeableItemWrapperAdapter<RecyclerView.ViewHolder>? = null
    private var mSwipingItem: RecyclerView.ViewHolder? = null
    private var mSwipingItemPosition: Int = RecyclerView.NO_POSITION
    private var mSwipingItemId: Long = RecyclerView.NO_ID
    private var mTouchedItemOffsetX: Int = 0
    private var mTouchedItemOffsetY: Int = 0
    private var mLastTouchX: Int = 0
    private var mLastTouchY: Int = 0
    private var mSwipingItemReactionType: Int = 0
    private var mVelocityTracker: VelocityTracker?
    private var mSwipingItemOperator: SwipingItemOperator? = null
    private var mItemSwipeEventListener: OnItemSwipeEventListener? = null
    private var mHandler: InternalHandler? = null
    private var mLongPressTimeout: Int = 0

    init {
        mInternalUseOnItemTouchListener = object : RecyclerView.OnItemTouchListener {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                return this@RecyclerViewSwipeManager.onInterceptTouchEvent(rv, e)
            }

            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
                this@RecyclerViewSwipeManager.onTouchEvent(rv, e)
            }

            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
                this@RecyclerViewSwipeManager.onRequestDisallowInterceptTouchEvent(disallowIntercept)
            }
        }
        mVelocityTracker = VelocityTracker.obtain()
        mLongPressTimeout = ViewConfiguration.getLongPressTimeout()
    }

    @Suppress("UNCHECKED_CAST")
    fun createWrappedAdapter(adapter: RecyclerView.Adapter<*>): RecyclerView.Adapter<*> {
        require(adapter.hasStableIds()) { "The passed adapter does not support stable IDs" }
        check(mAdapter == null) { "already have a wrapped adapter" }

        val wrapper = SwipeableItemWrapperAdapter(this, adapter as RecyclerView.Adapter<RecyclerView.ViewHolder>)
        mAdapter = wrapper
        return wrapper
    }

    fun isReleased(): Boolean {
        return mInternalUseOnItemTouchListener == null
    }

    fun attachRecyclerView(rv: RecyclerView) {
        check(!isReleased()) { "Accessing released object" }
        check(mRecyclerView == null) { "RecyclerView instance has already been set" }
        check(mAdapter != null && getSwipeableItemWrapperAdapter(rv) == mAdapter) { "adapter is not set properly" }

        val layoutOrientation = CustomRecyclerViewUtils.getOrientation(rv)
        check(layoutOrientation != CustomRecyclerViewUtils.ORIENTATION_UNKNOWN) { "failed to determine layout orientation" }

        mRecyclerView = rv
        rv.addOnItemTouchListener(mInternalUseOnItemTouchListener!!)

        val vc = ViewConfiguration.get(rv.context)
        mTouchSlop = vc.scaledTouchSlop
        mMinFlingVelocity = vc.scaledMinimumFlingVelocity
        mMaxFlingVelocity = vc.scaledMaximumFlingVelocity

        val adapter = mAdapter!!
        val animator = ItemSlidingAnimator(adapter)
        animator.immediatelySetTranslationThreshold =
            (rv.resources.displayMetrics.density * SLIDE_ITEM_IMMEDIATELY_SET_TRANSLATION_THRESHOLD_DP + 0.5f).toInt()
        mItemSlideAnimator = animator

        mSwipeHorizontal = layoutOrientation == CustomRecyclerViewUtils.ORIENTATION_VERTICAL
        mHandler = InternalHandler(this)
    }

    fun release() {
        cancelSwipe(true)

        mHandler?.release()
        mHandler = null

        val rv = mRecyclerView
        val listener = mInternalUseOnItemTouchListener
        if (rv != null && listener != null) {
            rv.removeOnItemTouchListener(listener)
        }
        mInternalUseOnItemTouchListener = null

        mVelocityTracker?.recycle()
        mVelocityTracker = null

        mItemSlideAnimator?.endAnimations()
        mItemSlideAnimator = null

        mAdapter = null
        mRecyclerView = null
    }

    fun isSwiping(): Boolean {
        return mSwipingItem != null && (mHandler?.isCancelSwipeRequested() != true)
    }

    fun setLongPressTimeout(longPressTimeout: Int) {
        mLongPressTimeout = longPressTimeout
    }

    internal fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
        val action = MotionEventCompat.getActionMasked(e)

        if (LOCAL_LOGV) {
            Log.v(TAG, "onInterceptTouchEvent() action = $action")
        }

        when (action) {
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (handleActionUpOrCancel(e, true)) {
                    return true
                }
            }
            MotionEvent.ACTION_DOWN -> {
                if (!isSwiping()) {
                    handleActionDown(rv, e)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (isSwiping()) {
                    handleActionMoveWhileSwiping(e)
                    return true
                } else {
                    if (handleActionMoveWhileNotSwiping(rv, e)) {
                        return true
                    }
                }
            }
        }

        return false
    }

    internal fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
        val action = MotionEventCompat.getActionMasked(e)

        if (LOCAL_LOGV) {
            Log.v(TAG, "onTouchEvent() action = $action")
        }

        if (!isSwiping()) {
            return
        }

        when (action) {
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                handleActionUpOrCancel(e, true)
            }
            MotionEvent.ACTION_MOVE -> {
                handleActionMoveWhileSwiping(e)
            }
        }
    }

    internal fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
        if (disallowIntercept) {
            cancelSwipe(true)
        }
    }

    private fun handleActionDown(rv: RecyclerView, e: MotionEvent): Boolean {
        val adapter = rv.adapter ?: return false
        val holder = CustomRecyclerViewUtils.findChildViewHolderUnderWithTranslation(rv, e.x, e.y) ?: return false

        if (holder !is SwipeableItemViewHolder) {
            return false
        }

        val itemPosition = CustomRecyclerViewUtils.getSynchronizedPosition(holder)

        if (itemPosition !in 0 until adapter.itemCount) {
            return false
        }
        if (holder.itemId != adapter.getItemId(itemPosition)) {
            return false
        }

        val touchX = (e.x + 0.5f).toInt()
        val touchY = (e.y + 0.5f).toInt()

        val view = holder.itemView
        val translateX = (ViewCompat.getTranslationX(view) + 0.5f).toInt()
        val translateY = (ViewCompat.getTranslationY(view) + 0.5f).toInt()
        val viewX = touchX - (view.left + translateX)
        val viewY = touchY - (view.top + translateY)

        val wrapper = mAdapter ?: return false
        val reactionType = wrapper.getSwipeReactionType(holder, itemPosition, viewX, viewY)

        if (reactionType == 0) {
            return false
        }

        mInitialTouchX = touchX
        mInitialTouchY = touchY
        mCheckingTouchSlop = holder.itemId
        mSwipingItemReactionType = reactionType

        if ((reactionType and REACTION_START_SWIPE_ON_LONG_PRESS) != 0) {
            mHandler?.startLongPressDetection(e, mLongPressTimeout)
        }

        return true
    }

    private fun handleActionUpOrCancel(e: MotionEvent?, invokeFinish: Boolean): Boolean {
        var action = MotionEvent.ACTION_CANCEL

        if (e != null) {
            action = MotionEventCompat.getActionMasked(e)
            mLastTouchX = (e.x + 0.5f).toInt()
            mLastTouchY = (e.y + 0.5f).toInt()
        }

        return if (isSwiping()) {
            if (invokeFinish) {
                handleActionUpOrCancelWhileSwiping(action)
            }
            true
        } else {
            handleActionUpOrCancelWhileNotSwiping()
            false
        }
    }

    private fun handleActionUpOrCancelWhileNotSwiping() {
        mHandler?.cancelLongPressDetection()
        mCheckingTouchSlop = RecyclerView.NO_ID
        mSwipingItemReactionType = 0
    }

    private fun handleActionUpOrCancelWhileSwiping(action: Int) {
        var result = RESULT_CANCELED
        val swipingItem = mSwipingItem

        if (action == MotionEvent.ACTION_UP && swipingItem != null) {
            val horizontal = mSwipeHorizontal
            val itemView = swipingItem.itemView
            val viewSize = if (horizontal) itemView.width else itemView.height
            val distance = if (horizontal) (mLastTouchX - mInitialTouchX).toFloat() else (mLastTouchY - mInitialTouchY).toFloat()
            val absDistance = Math.abs(distance)

            val tracker = mVelocityTracker
            if (tracker != null) {
                tracker.computeCurrentVelocity(1000, mMaxFlingVelocity.toFloat())
                val velocity = if (horizontal) tracker.xVelocity else tracker.yVelocity
                val absVelocity = Math.abs(velocity)

                if (absDistance > mTouchSlop * MIN_DISTANCE_TOUCH_SLOP_MUL &&
                    distance * velocity > 0.0f &&
                    absVelocity <= mMaxFlingVelocity &&
                    (absDistance > viewSize / 2 || absVelocity >= mMinFlingVelocity)
                ) {
                    if (horizontal && distance < 0 && SwipeReactionUtils.canSwipeLeft(mSwipingItemReactionType)) {
                        result = RESULT_SWIPED_LEFT
                    } else if (!horizontal && distance < 0 && SwipeReactionUtils.canSwipeUp(mSwipingItemReactionType)) {
                        result = RESULT_SWIPED_UP
                    } else if (horizontal && distance > 0 && SwipeReactionUtils.canSwipeRight(mSwipingItemReactionType)) {
                        result = RESULT_SWIPED_RIGHT
                    } else if (!horizontal && distance > 0 && SwipeReactionUtils.canSwipeDown(mSwipingItemReactionType)) {
                        result = RESULT_SWIPED_DOWN
                    }
                }
            }
        }

        if (LOCAL_LOGD) {
            Log.d(TAG, "swiping finished  --- result = $result")
        }

        finishSwiping(result)
    }

    private fun handleActionMoveWhileNotSwiping(rv: RecyclerView, e: MotionEvent): Boolean {
        if (mCheckingTouchSlop == RecyclerView.NO_ID) {
            return false
        }

        val dx = (e.x + 0.5f).toInt() - mInitialTouchX
        val dy = (e.y + 0.5f).toInt() - mInitialTouchY

        val scrollAxisDelta = if (mSwipeHorizontal) dy else dx
        val swipeAxisDelta = if (mSwipeHorizontal) dx else dy

        if (Math.abs(scrollAxisDelta) > mTouchSlop) {
            mCheckingTouchSlop = RecyclerView.NO_ID
            return false
        }

        if (Math.abs(swipeAxisDelta) <= mTouchSlop) {
            return false
        }

        val dirMasked = if (mSwipeHorizontal) {
            if (swipeAxisDelta < 0) {
                (mSwipingItemReactionType and REACTION_MASK_START_SWIPE_LEFT) != 0
            } else {
                (mSwipingItemReactionType and REACTION_MASK_START_SWIPE_RIGHT) != 0
            }
        } else {
            if (swipeAxisDelta < 0) {
                (mSwipingItemReactionType and REACTION_MASK_START_SWIPE_UP) != 0
            } else {
                (mSwipingItemReactionType and REACTION_MASK_START_SWIPE_DOWN) != 0
            }
        }

        if (dirMasked) {
            mCheckingTouchSlop = RecyclerView.NO_ID
            return false
        }

        val holder = CustomRecyclerViewUtils.findChildViewHolderUnderWithTranslation(rv, e.x, e.y)
        if (holder == null || holder.itemId != mCheckingTouchSlop) {
            mCheckingTouchSlop = RecyclerView.NO_ID
            return false
        }

        return checkConditionAndStartSwiping(e, holder)
    }

    private fun handleActionMoveWhileSwiping(e: MotionEvent) {
        mLastTouchX = (e.x + 0.5f).toInt()
        mLastTouchY = (e.y + 0.5f).toInt()
        mVelocityTracker?.addMovement(e)

        val swipeDistanceX = mLastTouchX - mTouchedItemOffsetX
        val swipeDistanceY = mLastTouchY - mTouchedItemOffsetY
        mSwipingItemPosition = getItemPosition(mAdapter, mSwipingItemId, mSwipingItemPosition)

        mSwipingItemOperator?.update(mSwipingItemPosition, swipeDistanceX, swipeDistanceY)
    }

    private fun checkConditionAndStartSwiping(e: MotionEvent, holder: RecyclerView.ViewHolder): Boolean {
        val itemPosition = CustomRecyclerViewUtils.getSynchronizedPosition(holder)
        if (itemPosition == RecyclerView.NO_POSITION) {
            return false
        }

        startSwiping(e, holder, itemPosition)
        return true
    }

    private fun startSwiping(e: MotionEvent, holder: RecyclerView.ViewHolder, itemPosition: Int) {
        if (LOCAL_LOGD) {
            Log.d(TAG, "swiping started")
        }

        mHandler?.cancelLongPressDetection()

        mSwipingItem = holder
        mSwipingItemPosition = itemPosition
        mSwipingItemId = mAdapter?.getItemId(itemPosition) ?: RecyclerView.NO_ID
        mLastTouchX = (e.x + 0.5f).toInt()
        mLastTouchY = (e.y + 0.5f).toInt()
        mTouchedItemOffsetX = mLastTouchX
        mTouchedItemOffsetY = mLastTouchY
        mCheckingTouchSlop = RecyclerView.NO_ID
        CustomRecyclerViewUtils.getLayoutMargins(holder.itemView, mSwipingItemMargins)

        mSwipingItemOperator = SwipingItemOperator(this, holder, mSwipingItemReactionType, mSwipeHorizontal)
        mSwipingItemOperator?.start()

        mVelocityTracker?.clear()
        mVelocityTracker?.addMovement(e)

        mRecyclerView?.parent?.requestDisallowInterceptTouchEvent(true)

        mItemSwipeEventListener?.onItemSwipeStarted(itemPosition)
        mAdapter?.onSwipeItemStarted(this, holder, mSwipingItemId)
    }

    private fun finishSwiping(result: Int) {
        val swipingItem = mSwipingItem ?: return

        mHandler?.removeDeferredCancelSwipeRequest()
        mHandler?.cancelLongPressDetection()

        mRecyclerView?.parent?.requestDisallowInterceptTouchEvent(false)

        val itemPosition = getItemPosition(mAdapter, mSwipingItemId, mSwipingItemPosition)

        mVelocityTracker?.clear()

        mSwipingItem = null
        mSwipingItemPosition = RecyclerView.NO_POSITION
        mSwipingItemId = RecyclerView.NO_ID
        mLastTouchX = 0
        mLastTouchY = 0
        mInitialTouchX = 0
        mTouchedItemOffsetX = 0
        mTouchedItemOffsetY = 0
        mCheckingTouchSlop = RecyclerView.NO_ID
        mSwipingItemReactionType = 0

        mSwipingItemOperator?.finish()
        mSwipingItemOperator = null

        val slideDir = resultCodeToSlideDirection(result)
        var resultAction: SwipeResultAction? = mAdapter?.onSwipeItemFinished(swipingItem, itemPosition, result)
        if (resultAction == null) {
            resultAction = SwipeResultActionDefault()
        }

        val afterReaction = resultAction.resultActionType
        verifyAfterReaction(result, afterReaction)

        var slideAnimated = false
        val animator = mItemSlideAnimator
        if (animator != null) {
            when (afterReaction) {
                AFTER_SWIPE_REACTION_DEFAULT -> {
                    slideAnimated = animator.finishSwipeSlideToDefaultPosition(
                        swipingItem, mSwipeHorizontal, true, mReturnToDefaultPositionAnimationDuration,
                        itemPosition, resultAction
                    )
                }
                AFTER_SWIPE_REACTION_MOVE_TO_SWIPED_DIRECTION -> {
                    slideAnimated = animator.finishSwipeSlideToOutsideOfWindow(
                        swipingItem, slideDir, true, mMoveToOutsideWindowAnimationDuration,
                        itemPosition, resultAction
                    )
                }
                AFTER_SWIPE_REACTION_REMOVE_ITEM -> {
                    val itemAnimator = mRecyclerView?.itemAnimator
                    val removeAnimationDuration = itemAnimator?.removeDuration ?: 0L

                    if (supportsViewPropertyAnimator()) {
                        val moveAnimationDuration = itemAnimator?.moveDuration ?: 0L
                        val rv = mRecyclerView
                        if (rv != null) {
                            val decorator = RemovingItemDecorator(
                                rv, swipingItem, result, removeAnimationDuration, moveAnimationDuration
                            )
                            decorator.setMoveAnimationInterpolator(SwipeDismissItemAnimator.MOVE_INTERPOLATOR)
                            decorator.start()
                        }
                    }

                    slideAnimated = animator.finishSwipeSlideToOutsideOfWindow(
                        swipingItem, slideDir, true, removeAnimationDuration,
                        itemPosition, resultAction
                    )
                }
                else -> throw IllegalStateException("Unknown after reaction type: $afterReaction")
            }
        }

        mAdapter?.onSwipeItemFinished2(swipingItem, itemPosition, result, afterReaction, resultAction)
        mItemSwipeEventListener?.onItemSwipeFinished(itemPosition, result, afterReaction)

        if (!slideAnimated) {
            resultAction.slideAnimationEnd()
        }
    }

    fun cancelSwipe() {
        cancelSwipe(false)
    }

    internal fun cancelSwipe(immediately: Boolean) {
        handleActionUpOrCancel(null, false)
        if (immediately) {
            finishSwiping(RESULT_CANCELED)
        } else {
            if (isSwiping()) {
                mHandler?.requestDeferredCancelSwipe()
            }
        }
    }

    internal fun isAnimationRunning(item: RecyclerView.ViewHolder): Boolean {
        return mItemSlideAnimator?.isRunning(item) == true
    }

    private fun slideItem(holder: RecyclerView.ViewHolder, amount: Float, horizontal: Boolean, shouldAnimate: Boolean) {
        val animator = mItemSlideAnimator ?: return
        when (amount) {
            OUTSIDE_OF_THE_WINDOW_LEFT -> {
                animator.slideToOutsideOfWindow(holder, ItemSlidingAnimator.DIR_LEFT, shouldAnimate, mMoveToOutsideWindowAnimationDuration)
            }
            OUTSIDE_OF_THE_WINDOW_TOP -> {
                animator.slideToOutsideOfWindow(holder, ItemSlidingAnimator.DIR_UP, shouldAnimate, mMoveToOutsideWindowAnimationDuration)
            }
            OUTSIDE_OF_THE_WINDOW_RIGHT -> {
                animator.slideToOutsideOfWindow(holder, ItemSlidingAnimator.DIR_RIGHT, shouldAnimate, mMoveToOutsideWindowAnimationDuration)
            }
            OUTSIDE_OF_THE_WINDOW_BOTTOM -> {
                animator.slideToOutsideOfWindow(holder, ItemSlidingAnimator.DIR_DOWN, shouldAnimate, mMoveToOutsideWindowAnimationDuration)
            }
            0.0f -> {
                animator.slideToDefaultPosition(holder, horizontal, shouldAnimate, mReturnToDefaultPositionAnimationDuration)
            }
            else -> {
                animator.slideToSpecifiedPosition(holder, amount, horizontal)
            }
        }
    }

    fun getReturnToDefaultPositionAnimationDuration(): Long {
        return mReturnToDefaultPositionAnimationDuration
    }

    fun setReturnToDefaultPositionAnimationDuration(duration: Long) {
        mReturnToDefaultPositionAnimationDuration = duration
    }

    fun getMoveToOutsideWindowAnimationDuration(): Long {
        return mMoveToOutsideWindowAnimationDuration
    }

    fun setMoveToOutsideWindowAnimationDuration(duration: Long) {
        mMoveToOutsideWindowAnimationDuration = duration
    }

    fun getOnItemSwipeEventListener(): OnItemSwipeEventListener? {
        return mItemSwipeEventListener
    }

    fun setOnItemSwipeEventListener(listener: OnItemSwipeEventListener?) {
        mItemSwipeEventListener = listener
    }

    internal fun swipeHorizontal(): Boolean {
        return mSwipeHorizontal
    }

    internal fun applySlideItem(
        holder: RecyclerView.ViewHolder, itemPosition: Int,
        prevAmount: Float, amount: Float, horizontal: Boolean, shouldAnimate: Boolean, isSwiping: Boolean
    ) {
        val holder2 = holder as SwipeableItemViewHolder
        val containerView = holder2.getSwipeableContainerView() ?: return

        val reqBackgroundType = if (amount == 0.0f) {
            if (prevAmount == 0.0f) {
                DRAWABLE_SWIPE_NEUTRAL_BACKGROUND
            } else {
                determineBackgroundType(prevAmount, horizontal)
            }
        } else {
            determineBackgroundType(amount, horizontal)
        }

        if (amount == 0.0f) {
            slideItem(holder, amount, horizontal, shouldAnimate)
            mAdapter?.onUpdateSlideAmount(holder, itemPosition, horizontal, amount, isSwiping, reqBackgroundType)
        } else {
            var adjustedAmount = amount
            val minLimit = if (horizontal) holder2.getMaxLeftSwipeAmount() else holder2.getMaxUpSwipeAmount()
            val maxLimit = if (horizontal) holder2.getMaxRightSwipeAmount() else holder2.getMaxDownSwipeAmount()

            adjustedAmount = Math.max(adjustedAmount, minLimit)
            adjustedAmount = Math.min(adjustedAmount, maxLimit)

            mAdapter?.onUpdateSlideAmount(holder, itemPosition, horizontal, amount, isSwiping, reqBackgroundType)
            slideItem(holder, adjustedAmount, horizontal, shouldAnimate)
        }
    }

    internal fun cancelPendingAnimations(holder: RecyclerView.ViewHolder) {
        mItemSlideAnimator?.endAnimation(holder)
    }

    internal fun getSwipeContainerViewTranslationX(holder: RecyclerView.ViewHolder): Int {
        return mItemSlideAnimator?.getSwipeContainerViewTranslationX(holder) ?: 0
    }

    internal fun getSwipeContainerViewTranslationY(holder: RecyclerView.ViewHolder): Int {
        return mItemSlideAnimator?.getSwipeContainerViewTranslationY(holder) ?: 0
    }

    internal fun handleOnLongPress(e: MotionEvent?) {
        if (e == null) return
        val rv = mRecyclerView ?: return
        val holder = rv.findViewHolderForItemId(mCheckingTouchSlop)
        if (holder != null) {
            checkConditionAndStartSwiping(e, holder)
        }
    }

    interface OnItemSwipeEventListener {
        fun onItemSwipeStarted(position: Int)
        fun onItemSwipeFinished(position: Int, result: Int, afterSwipeReaction: Int)
    }

    private class InternalHandler(holder: RecyclerViewSwipeManager) : Handler(Looper.getMainLooper()) {
        private var mHolder: RecyclerViewSwipeManager? = holder
        private var mDownMotionEvent: MotionEvent? = null

        fun release() {
            removeCallbacksAndMessages(null)
            mHolder = null
        }

        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_LONGPRESS -> mHolder?.handleOnLongPress(mDownMotionEvent)
                MSG_DEFERRED_CANCEL_SWIPE -> mHolder?.cancelSwipe(true)
            }
        }

        fun startLongPressDetection(e: MotionEvent, timeout: Int) {
            cancelLongPressDetection()
            mDownMotionEvent = MotionEvent.obtain(e)
            sendEmptyMessageAtTime(MSG_LONGPRESS, e.downTime + timeout)
        }

        fun cancelLongPressDetection() {
            removeMessages(MSG_LONGPRESS)
            mDownMotionEvent?.recycle()
            mDownMotionEvent = null
        }

        fun removeDeferredCancelSwipeRequest() {
            removeMessages(MSG_DEFERRED_CANCEL_SWIPE)
        }

        fun requestDeferredCancelSwipe() {
            if (isCancelSwipeRequested()) {
                return
            }
            sendEmptyMessage(MSG_DEFERRED_CANCEL_SWIPE)
        }

        fun isCancelSwipeRequested(): Boolean {
            return hasMessages(MSG_DEFERRED_CANCEL_SWIPE)
        }

        companion object {
            private const val MSG_LONGPRESS = 1
            private const val MSG_DEFERRED_CANCEL_SWIPE = 2
        }
    }

    companion object {
        private const val TAG = "ARVSwipeManager"
        private const val MIN_DISTANCE_TOUCH_SLOP_MUL = 10
        private const val SLIDE_ITEM_IMMEDIATELY_SET_TRANSLATION_THRESHOLD_DP = 8
        private const val LOCAL_LOGV = false
        private const val LOCAL_LOGD = false

        const val STATE_FLAG_SWIPING = SwipeableItemConstants.STATE_FLAG_SWIPING
        const val STATE_FLAG_IS_ACTIVE = SwipeableItemConstants.STATE_FLAG_IS_ACTIVE
        const val STATE_FLAG_IS_UPDATED = SwipeableItemConstants.STATE_FLAG_IS_UPDATED

        const val RESULT_NONE = SwipeableItemConstants.RESULT_NONE
        const val RESULT_CANCELED = SwipeableItemConstants.RESULT_CANCELED
        const val RESULT_SWIPED_LEFT = SwipeableItemConstants.RESULT_SWIPED_LEFT
        const val RESULT_SWIPED_UP = SwipeableItemConstants.RESULT_SWIPED_UP
        const val RESULT_SWIPED_RIGHT = SwipeableItemConstants.RESULT_SWIPED_RIGHT
        const val RESULT_SWIPED_DOWN = SwipeableItemConstants.RESULT_SWIPED_DOWN

        const val REACTION_CAN_NOT_SWIPE_ANY = SwipeableItemConstants.REACTION_CAN_NOT_SWIPE_ANY
        const val REACTION_CAN_NOT_SWIPE_LEFT = SwipeableItemConstants.REACTION_CAN_NOT_SWIPE_LEFT
        const val REACTION_CAN_NOT_SWIPE_LEFT_WITH_RUBBER_BAND_EFFECT = SwipeableItemConstants.REACTION_CAN_NOT_SWIPE_LEFT_WITH_RUBBER_BAND_EFFECT
        const val REACTION_CAN_SWIPE_LEFT = SwipeableItemConstants.REACTION_CAN_SWIPE_LEFT
        const val REACTION_MASK_START_SWIPE_LEFT = SwipeableItemConstants.REACTION_MASK_START_SWIPE_LEFT

        const val REACTION_CAN_NOT_SWIPE_UP = SwipeableItemConstants.REACTION_CAN_NOT_SWIPE_UP
        const val REACTION_CAN_NOT_SWIPE_UP_WITH_RUBBER_BAND_EFFECT = SwipeableItemConstants.REACTION_CAN_NOT_SWIPE_UP_WITH_RUBBER_BAND_EFFECT
        const val REACTION_CAN_SWIPE_UP = SwipeableItemConstants.REACTION_CAN_SWIPE_UP
        const val REACTION_MASK_START_SWIPE_UP = SwipeableItemConstants.REACTION_MASK_START_SWIPE_UP

        const val REACTION_CAN_NOT_SWIPE_RIGHT = SwipeableItemConstants.REACTION_CAN_NOT_SWIPE_RIGHT
        const val REACTION_CAN_NOT_SWIPE_RIGHT_WITH_RUBBER_BAND_EFFECT = SwipeableItemConstants.REACTION_CAN_NOT_SWIPE_RIGHT_WITH_RUBBER_BAND_EFFECT
        const val REACTION_CAN_SWIPE_RIGHT = SwipeableItemConstants.REACTION_CAN_SWIPE_RIGHT
        const val REACTION_MASK_START_SWIPE_RIGHT = SwipeableItemConstants.REACTION_MASK_START_SWIPE_RIGHT

        const val REACTION_CAN_NOT_SWIPE_DOWN = SwipeableItemConstants.REACTION_CAN_NOT_SWIPE_DOWN
        const val REACTION_CAN_NOT_SWIPE_DOWN_WITH_RUBBER_BAND_EFFECT = SwipeableItemConstants.REACTION_CAN_NOT_SWIPE_DOWN_WITH_RUBBER_BAND_EFFECT
        const val REACTION_CAN_SWIPE_DOWN = SwipeableItemConstants.REACTION_CAN_SWIPE_DOWN
        const val REACTION_MASK_START_SWIPE_DOWN = SwipeableItemConstants.REACTION_MASK_START_SWIPE_DOWN

        const val REACTION_START_SWIPE_ON_LONG_PRESS = SwipeableItemConstants.REACTION_START_SWIPE_ON_LONG_PRESS

        const val REACTION_CAN_NOT_SWIPE_BOTH_H = SwipeableItemConstants.REACTION_CAN_NOT_SWIPE_BOTH_H
        const val REACTION_CAN_NOT_SWIPE_BOTH_H_WITH_RUBBER_BAND_EFFECT = SwipeableItemConstants.REACTION_CAN_NOT_SWIPE_BOTH_H_WITH_RUBBER_BAND_EFFECT
        const val REACTION_CAN_SWIPE_BOTH_H = SwipeableItemConstants.REACTION_CAN_SWIPE_BOTH_H

        const val REACTION_CAN_NOT_SWIPE_BOTH_V = SwipeableItemConstants.REACTION_CAN_NOT_SWIPE_BOTH_V
        const val REACTION_CAN_NOT_SWIPE_BOTH_V_WITH_RUBBER_BAND_EFFECT = SwipeableItemConstants.REACTION_CAN_NOT_SWIPE_BOTH_V_WITH_RUBBER_BAND_EFFECT
        const val REACTION_CAN_SWIPE_BOTH_V = SwipeableItemConstants.REACTION_CAN_SWIPE_BOTH_V

        const val DRAWABLE_SWIPE_NEUTRAL_BACKGROUND = SwipeableItemConstants.DRAWABLE_SWIPE_NEUTRAL_BACKGROUND
        const val DRAWABLE_SWIPE_LEFT_BACKGROUND = SwipeableItemConstants.DRAWABLE_SWIPE_LEFT_BACKGROUND
        const val DRAWABLE_SWIPE_UP_BACKGROUND = SwipeableItemConstants.DRAWABLE_SWIPE_UP_BACKGROUND
        const val DRAWABLE_SWIPE_RIGHT_BACKGROUND = SwipeableItemConstants.DRAWABLE_SWIPE_RIGHT_BACKGROUND
        const val DRAWABLE_SWIPE_DOWN_BACKGROUND = SwipeableItemConstants.DRAWABLE_SWIPE_DOWN_BACKGROUND

        const val AFTER_SWIPE_REACTION_DEFAULT = SwipeableItemConstants.AFTER_SWIPE_REACTION_DEFAULT
        const val AFTER_SWIPE_REACTION_REMOVE_ITEM = SwipeableItemConstants.AFTER_SWIPE_REACTION_REMOVE_ITEM
        const val AFTER_SWIPE_REACTION_MOVE_TO_SWIPED_DIRECTION = SwipeableItemConstants.AFTER_SWIPE_REACTION_MOVE_TO_SWIPED_DIRECTION

        const val OUTSIDE_OF_THE_WINDOW_LEFT = SwipeableItemConstants.OUTSIDE_OF_THE_WINDOW_LEFT
        const val OUTSIDE_OF_THE_WINDOW_TOP = SwipeableItemConstants.OUTSIDE_OF_THE_WINDOW_TOP
        const val OUTSIDE_OF_THE_WINDOW_RIGHT = SwipeableItemConstants.OUTSIDE_OF_THE_WINDOW_RIGHT
        const val OUTSIDE_OF_THE_WINDOW_BOTTOM = SwipeableItemConstants.OUTSIDE_OF_THE_WINDOW_BOTTOM

        private fun getSwipeableItemWrapperAdapter(rv: RecyclerView): SwipeableItemWrapperAdapter<*>? {
            return WrapperAdapterUtils.findWrappedAdapter(rv.adapter, SwipeableItemWrapperAdapter::class.java)
        }

        private fun verifyAfterReaction(result: Int, afterReaction: Int) {
            if (afterReaction == AFTER_SWIPE_REACTION_MOVE_TO_SWIPED_DIRECTION ||
                afterReaction == AFTER_SWIPE_REACTION_REMOVE_ITEM
            ) {
                when (result) {
                    RESULT_SWIPED_LEFT,
                    RESULT_SWIPED_UP,
                    RESULT_SWIPED_RIGHT,
                    RESULT_SWIPED_DOWN -> {
                    }
                    else -> throw IllegalStateException("Unexpected after reaction has been requested: result = $result, afterReaction = $afterReaction")
                }
            }
        }

        private fun resultCodeToSlideDirection(result: Int): Int {
            return when (result) {
                RESULT_SWIPED_LEFT -> ItemSlidingAnimator.DIR_LEFT
                RESULT_SWIPED_UP -> ItemSlidingAnimator.DIR_UP
                RESULT_SWIPED_RIGHT -> ItemSlidingAnimator.DIR_RIGHT
                RESULT_SWIPED_DOWN -> ItemSlidingAnimator.DIR_DOWN
                else -> ItemSlidingAnimator.DIR_LEFT
            }
        }

        private fun getItemPosition(adapter: RecyclerView.Adapter<*>?, itemId: Long, itemPositionGuess: Int): Int {
            if (adapter == null) return RecyclerView.NO_POSITION

            val itemCount = adapter.itemCount
            if (itemPositionGuess in 0 until itemCount) {
                if (adapter.getItemId(itemPositionGuess) == itemId) return itemPositionGuess
            }

            for (i in 0 until itemCount) {
                if (adapter.getItemId(i) == itemId) return i
            }

            return RecyclerView.NO_POSITION
        }

        private fun determineBackgroundType(amount: Float, horizontal: Boolean): Int {
            return if (horizontal) {
                if (amount < 0) DRAWABLE_SWIPE_LEFT_BACKGROUND else DRAWABLE_SWIPE_RIGHT_BACKGROUND
            } else {
                if (amount < 0) DRAWABLE_SWIPE_UP_BACKGROUND else DRAWABLE_SWIPE_DOWN_BACKGROUND
            }
        }

        private fun supportsViewPropertyAnimator(): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB
        }
    }
}
