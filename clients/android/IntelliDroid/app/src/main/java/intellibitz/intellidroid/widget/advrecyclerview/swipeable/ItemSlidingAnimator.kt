package intellibitz.intellidroid.widget.advrecyclerview.swipeable

import android.annotation.SuppressLint
import android.graphics.Rect
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.Interpolator
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import androidx.core.view.ViewPropertyAnimatorCompat
import androidx.core.view.ViewPropertyAnimatorListener
import androidx.core.view.ViewPropertyAnimatorUpdateListener
import androidx.recyclerview.widget.RecyclerView
import intellibitz.intellidroid.widget.advrecyclerview.swipeable.action.SwipeResultAction
import java.lang.ref.WeakReference
import java.util.ArrayList

class ItemSlidingAnimator(private val mAdapter: SwipeableItemWrapperAdapter<RecyclerView.ViewHolder>) {

    private val mSlideToDefaultPositionAnimationInterpolator: Interpolator = AccelerateDecelerateInterpolator()
    private val mSlideToOutsideOfWindowAnimationInterpolator: Interpolator = AccelerateInterpolator(0.8f)
    private val mActive: MutableList<RecyclerView.ViewHolder> = ArrayList()
    private val mDeferredProcesses: MutableList<WeakReference<ViewHolderDeferredProcess>> = ArrayList()
    private val mTmpLocation = IntArray(2)
    private val mTmpRect = Rect()
    var immediatelySetTranslationThreshold: Int = 0

    fun slideToDefaultPosition(holder: RecyclerView.ViewHolder, horizontal: Boolean, shouldAnimate: Boolean, duration: Long) {
        cancelDeferredProcess(holder)
        slideToSpecifiedPositionInternal(holder, 0f, horizontal, shouldAnimate, duration, null)
    }

    fun slideToOutsideOfWindow(holder: RecyclerView.ViewHolder, dir: Int, shouldAnimate: Boolean, duration: Long) {
        cancelDeferredProcess(holder)
        slideToOutsideOfWindowInternal(holder, dir, shouldAnimate, duration, null)
    }

    fun slideToSpecifiedPosition(holder: RecyclerView.ViewHolder, position: Float, horizontal: Boolean) {
        cancelDeferredProcess(holder)
        slideToSpecifiedPositionInternal(holder, position, horizontal, false, 0, null)
    }

    fun finishSwipeSlideToDefaultPosition(
        holder: RecyclerView.ViewHolder, horizontal: Boolean,
        shouldAnimate: Boolean, duration: Long,
        itemPosition: Int, resultAction: SwipeResultAction
    ): Boolean {
        cancelDeferredProcess(holder)
        return slideToSpecifiedPositionInternal(
            holder, 0f, horizontal, shouldAnimate, duration,
            SwipeFinishInfo(itemPosition, resultAction)
        )
    }

    fun finishSwipeSlideToOutsideOfWindow(
        holder: RecyclerView.ViewHolder, dir: Int,
        shouldAnimate: Boolean, duration: Long,
        itemPosition: Int, resultAction: SwipeResultAction
    ): Boolean {
        cancelDeferredProcess(holder)
        return slideToOutsideOfWindowInternal(
            holder, dir, shouldAnimate, duration,
            SwipeFinishInfo(itemPosition, resultAction)
        )
    }

    private fun cancelDeferredProcess(holder: RecyclerView.ViewHolder) {
        val n = mDeferredProcesses.size
        for (i in n - 1 downTo 0) {
            val dp = mDeferredProcesses[i].get()
            if (dp != null && dp.hasTargetViewHolder(holder)) {
                holder.itemView.removeCallbacks(dp)
                mDeferredProcesses.removeAt(i)
            } else if (dp == null || dp.lostReference(holder)) {
                mDeferredProcesses.removeAt(i)
            }
        }
    }

    private fun scheduleViewHolderDeferredSlideProcess(holder: RecyclerView.ViewHolder, deferredProcess: ViewHolderDeferredProcess) {
        mDeferredProcesses.add(WeakReference(deferredProcess))
        holder.itemView.post(deferredProcess)
    }

    private fun slideToSpecifiedPositionInternal(
        holder: RecyclerView.ViewHolder, position: Float,
        horizontal: Boolean, shouldAnimate: Boolean, duration: Long,
        swipeFinish: SwipeFinishInfo?
    ): Boolean {
        val defaultInterpolator = mSlideToDefaultPositionAnimationInterpolator
        val animDuration = if (shouldAnimate) duration else 0L

        if (position != 0.0f) {
            val containerView = (holder as SwipeableItemViewHolder).getSwipeableContainerView()
            val width = containerView.width
            val height = containerView.height

            if (horizontal && width != 0) {
                val translationX = (width * position + 0.5f).toInt()
                return animateSlideInternalCompat(
                    holder, horizontal, translationX, 0, animDuration, defaultInterpolator, swipeFinish
                )
            } else if (!horizontal && height != 0) {
                val translationY = (height * position + 0.5f).toInt()
                return animateSlideInternalCompat(
                    holder, horizontal, 0, translationY, animDuration, defaultInterpolator, swipeFinish
                )
            } else {
                if (swipeFinish != null) {
                    throw IllegalStateException("Unexpected state in slideToSpecifiedPositionInternal (swipeFinish == null)")
                }
                scheduleViewHolderDeferredSlideProcess(
                    holder, DeferredSlideProcess(holder, position, horizontal)
                )
                return false
            }
        } else {
            return animateSlideInternalCompat(
                holder, horizontal, 0, 0, animDuration, defaultInterpolator, swipeFinish
            )
        }
    }

    private fun slideToOutsideOfWindowInternal(
        holder: RecyclerView.ViewHolder, dir: Int, shouldAnimateParam: Boolean, duration: Long,
        swipeFinish: SwipeFinishInfo?
    ): Boolean {
        if (holder !is SwipeableItemViewHolder) {
            return false
        }

        var shouldAnimate = shouldAnimateParam
        val containerView = holder.getSwipeableContainerView()
        val parent = containerView.parent as? ViewGroup ?: return false

        val left = containerView.left
        val right = containerView.right
        val top = containerView.top
        val bottom = containerView.bottom
        val width = right - left
        val height = bottom - top
        val parentIsShown = parent.isShown

        parent.getWindowVisibleDisplayFrame(mTmpRect)
        val windowWidth = mTmpRect.width()
        val windowHeight = mTmpRect.height()

        var translateX = 0
        var translateY = 0

        if (width == 0 || height == 0 || !parentIsShown) {
            when (dir) {
                DIR_LEFT -> translateX = -windowWidth
                DIR_UP -> translateY = -windowHeight
                DIR_RIGHT -> translateX = windowWidth
                DIR_DOWN -> translateY = windowHeight
            }
            shouldAnimate = false
        } else {
            parent.getLocationInWindow(mTmpLocation)
            val x = mTmpLocation[0]
            val y = mTmpLocation[1]

            when (dir) {
                DIR_LEFT -> translateX = -(x + width)
                DIR_UP -> translateY = -(y + height)
                DIR_RIGHT -> translateX = windowWidth - (x - left)
                DIR_DOWN -> translateY = windowHeight - (y - top)
            }
        }

        if (shouldAnimate) {
            shouldAnimate = containerView.isShown
        }

        val animDuration = if (shouldAnimate) duration else 0L
        val horizontal = dir == DIR_LEFT || dir == DIR_RIGHT
        return animateSlideInternalCompat(
            holder, horizontal,
            translateX, translateY, animDuration, mSlideToOutsideOfWindowAnimationInterpolator,
            swipeFinish
        )
    }

    private fun animateSlideInternalCompat(
        holder: RecyclerView.ViewHolder,
        horizontal: Boolean, translationX: Int, translationY: Int, duration: Long, interpolator: Interpolator,
        swipeFinish: SwipeFinishInfo?
    ): Boolean {
        return if (supportsViewPropertyAnimator()) {
            animateSlideInternal(holder, horizontal, translationX, translationY, duration, interpolator, swipeFinish)
        } else {
            slideInternalPreHoneycomb(holder, horizontal, translationX, translationY)
        }
    }

    private fun animateSlideInternal(
        holder: RecyclerView.ViewHolder, horizontal: Boolean,
        translationX: Int, translationY: Int, duration: Long, interpolator: Interpolator,
        swipeFinish: SwipeFinishInfo?
    ): Boolean {
        if (holder !is SwipeableItemViewHolder) {
            return false
        }

        val containerView = holder.getSwipeableContainerView()
        val prevTranslationX = (ViewCompat.getTranslationX(containerView) + 0.5f).toInt()
        val prevTranslationY = (ViewCompat.getTranslationY(containerView) + 0.5f).toInt()

        endAnimation(holder)

        val curTranslationX = (ViewCompat.getTranslationX(containerView) + 0.5f).toInt()
        val curTranslationY = (ViewCompat.getTranslationY(containerView) + 0.5f).toInt()
        val toX = translationX
        val toY = translationY

        if (duration == 0L ||
            (curTranslationX == toX && curTranslationY == toY) ||
            (Math.max(Math.abs(toX - prevTranslationX), Math.abs(toY - prevTranslationY)) <= immediatelySetTranslationThreshold)
        ) {
            ViewCompat.setTranslationX(containerView, toX.toFloat())
            ViewCompat.setTranslationY(containerView, toY.toFloat())
            return false
        }

        ViewCompat.setTranslationX(containerView, prevTranslationX.toFloat())
        ViewCompat.setTranslationY(containerView, prevTranslationY.toFloat())

        val listener = SlidingAnimatorListenerObject(
            mAdapter, mActive, holder, toX, toY, duration, horizontal, interpolator,
            swipeFinish
        )
        listener.start()
        return true
    }

    fun endAnimation(holder: RecyclerView.ViewHolder) {
        if (holder !is SwipeableItemViewHolder) {
            return
        }

        cancelDeferredProcess(holder)
        val containerView = holder.getSwipeableContainerView()
        ViewCompat.animate(containerView).cancel()

        if (mActive.remove(holder)) {
            throw IllegalStateException("after animation is cancelled, item should not be in the active animation list [slide]")
        }
    }

    fun endAnimations() {
        for (i in mActive.size - 1 downTo 0) {
            val holder = mActive[i]
            endAnimation(holder)
        }
    }

    fun isRunning(holder: RecyclerView.ViewHolder): Boolean {
        return mActive.contains(holder)
    }

    fun isRunning(): Boolean {
        return mActive.isNotEmpty()
    }

    fun getSwipeContainerViewTranslationX(holder: RecyclerView.ViewHolder): Int {
        return if (supportsViewPropertyAnimator()) {
            val containerView = (holder as SwipeableItemViewHolder).getSwipeableContainerView()
            (ViewCompat.getTranslationX(containerView) + 0.5f).toInt()
        } else {
            getTranslationXPreHoneycomb(holder)
        }
    }

    fun getSwipeContainerViewTranslationY(holder: RecyclerView.ViewHolder): Int {
        return if (supportsViewPropertyAnimator()) {
            val containerView = (holder as SwipeableItemViewHolder).getSwipeableContainerView()
            (ViewCompat.getTranslationY(containerView) + 0.5f).toInt()
        } else {
            getTranslationYPreHoneycomb(holder)
        }
    }

    private abstract class ViewHolderDeferredProcess(holder: RecyclerView.ViewHolder) : Runnable {
        private val mRefHolder = WeakReference(holder)

        override fun run() {
            val holder = mRefHolder.get()
            if (holder != null) {
                onProcess(holder)
            }
        }

        fun lostReference(holder: RecyclerView.ViewHolder): Boolean {
            val holder2 = mRefHolder.get()
            return holder2 == null
        }

        fun hasTargetViewHolder(holder: RecyclerView.ViewHolder): Boolean {
            val holder2 = mRefHolder.get()
            return holder2 == holder
        }

        protected abstract fun onProcess(holder: RecyclerView.ViewHolder)
    }

    private class DeferredSlideProcess(
        holder: RecyclerView.ViewHolder,
        private val mPosition: Float,
        private val mHorizontal: Boolean
    ) : ViewHolderDeferredProcess(holder) {

        override fun onProcess(holder: RecyclerView.ViewHolder) {
            val containerView = (holder as SwipeableItemViewHolder).getSwipeableContainerView()
            if (mHorizontal) {
                val width = containerView.width
                val translationX = (width * mPosition + 0.5f).toInt()
                slideInternalCompat(holder, mHorizontal, translationX, 0)
            } else {
                val height = containerView.height
                val translationY = (height * mPosition + 0.5f).toInt()
                slideInternalCompat(holder, mHorizontal, 0, translationY)
            }
        }
    }

    private class SlidingAnimatorListenerObject(
        private var mAdapter: SwipeableItemWrapperAdapter<RecyclerView.ViewHolder>?,
        private var mActive: MutableList<RecyclerView.ViewHolder>?,
        private var mViewHolder: RecyclerView.ViewHolder?,
        private val mToX: Int,
        private val mToY: Int,
        private val mDuration: Long,
        private val mHorizontal: Boolean,
        private val mInterpolator: Interpolator?,
        private val mSwipeFinish: SwipeFinishInfo?
    ) : ViewPropertyAnimatorListener, ViewPropertyAnimatorUpdateListener {

        private var mAnimator: ViewPropertyAnimatorCompat? = null
        private var mInvSize: Float = 0f

        fun start() {
            val holder = mViewHolder ?: return
            val containerView = (holder as SwipeableItemViewHolder).getSwipeableContainerView()

            mInvSize = 1.0f / Math.max(1.0f, (if (mHorizontal) containerView.width else containerView.height).toFloat())

            val animator = ViewCompat.animate(containerView)
            animator.setDuration(mDuration)
            animator.translationX(mToX.toFloat())
            animator.translationY(mToY.toFloat())
            if (mInterpolator != null) {
                animator.setInterpolator(mInterpolator)
            }
            animator.setListener(this)
            animator.setUpdateListener(this)

            mActive?.add(holder)
            mAnimator = animator
            animator.start()
        }

        override fun onAnimationUpdate(view: View) {
            val holder = mViewHolder ?: return
            val translation = if (mHorizontal) ViewCompat.getTranslationX(view) else ViewCompat.getTranslationY(view)
            val amount = translation * mInvSize
            mAdapter?.onUpdateSlideAmount(holder, holder.layoutPosition, mHorizontal, amount, false)
        }

        override fun onAnimationStart(view: View) {}

        override fun onAnimationEnd(view: View) {
            mAnimator?.setListener(null)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                InternalHelperKK.clearViewPropertyAnimatorUpdateListener(view)
            } else {
                mAnimator?.setUpdateListener(null)
            }

            ViewCompat.setTranslationX(view, mToX.toFloat())
            ViewCompat.setTranslationY(view, mToY.toFloat())

            val holder = mViewHolder
            if (holder != null) {
                mActive?.remove(holder)
                val itemParentView: ViewParent? = holder.itemView.parent
                if (itemParentView is View) {
                    ViewCompat.postInvalidateOnAnimation(itemParentView)
                }
            }

            mSwipeFinish?.resultAction?.slideAnimationEnd()

            mActive = null
            mAnimator = null
            mViewHolder = null
            mAdapter = null
        }

        override fun onAnimationCancel(view: View) {}
    }

    private class SwipeFinishInfo(
        val itemPosition: Int,
        var resultAction: SwipeResultAction?
    ) {
        fun clear() {
            resultAction = null
        }
    }

    companion object {
        const val DIR_LEFT = 0
        const val DIR_UP = 1
        const val DIR_RIGHT = 2
        const val DIR_DOWN = 3
        private const val TAG = "ItemSlidingAnimator"

        @JvmStatic
        fun slideInternalCompat(holder: RecyclerView.ViewHolder, horizontal: Boolean, translationX: Int, translationY: Int) {
            if (supportsViewPropertyAnimator()) {
                slideInternal(holder, horizontal, translationX, translationY)
            } else {
                slideInternalPreHoneycomb(holder, horizontal, translationX, translationY)
            }
        }

        @SuppressLint("RtlHardcoded")
        private fun slideInternalPreHoneycomb(
            holder: RecyclerView.ViewHolder, horizontal: Boolean, translationX: Int, translationY: Int
        ): Boolean {
            if (holder !is SwipeableItemViewHolder) {
                return false
            }

            val containerView = holder.getSwipeableContainerView()
            val lp = containerView.layoutParams
            if (lp is ViewGroup.MarginLayoutParams) {
                lp.leftMargin = translationX
                lp.rightMargin = -translationX
                lp.topMargin = translationY
                lp.bottomMargin = -translationY

                if (lp is FrameLayout.LayoutParams) {
                    lp.gravity = Gravity.TOP or Gravity.LEFT
                }

                containerView.layoutParams = lp
            } else {
                Log.w(TAG, "should use MarginLayoutParams supported view for compatibility on Android 2.3")
            }
            return false
        }

        private fun getTranslationXPreHoneycomb(holder: RecyclerView.ViewHolder): Int {
            val containerView = (holder as SwipeableItemViewHolder).getSwipeableContainerView()
            val lp = containerView.layoutParams
            return if (lp is ViewGroup.MarginLayoutParams) {
                lp.leftMargin
            } else {
                Log.w(TAG, "should use MarginLayoutParams supported view for compatibility on Android 2.3")
                0
            }
        }

        private fun getTranslationYPreHoneycomb(holder: RecyclerView.ViewHolder): Int {
            val containerView = (holder as SwipeableItemViewHolder).getSwipeableContainerView()
            val lp = containerView.layoutParams
            return if (lp is ViewGroup.MarginLayoutParams) {
                lp.topMargin
            } else {
                Log.w(TAG, "should use MarginLayoutParams supported view for compatibility on Android 2.3")
                0
            }
        }

        private fun slideInternal(holder: RecyclerView.ViewHolder, horizontal: Boolean, translationX: Int, translationY: Int) {
            if (holder !is SwipeableItemViewHolder) {
                return
            }

            val containerView = holder.getSwipeableContainerView()
            ViewCompat.animate(containerView).cancel()
            ViewCompat.setTranslationX(containerView, translationX.toFloat())
            ViewCompat.setTranslationY(containerView, translationY.toFloat())
        }

        private fun supportsViewPropertyAnimator(): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB
        }
    }
}
