package intellibitz.intellidroid.widget.advrecyclerview.swipeable

import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.animation.Interpolator
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import intellibitz.intellidroid.widget.advrecyclerview.utils.CustomRecyclerViewUtils
import java.lang.ref.WeakReference

internal class RemovingItemDecorator(
    rv: RecyclerView,
    swipingItem: RecyclerView.ViewHolder,
    result: Int,
    removeAnimationDuration: Long,
    moveAnimationDuration: Long
) : RecyclerView.ItemDecoration() {

    private val mSwipingItemId: Long = swipingItem.itemId
    private val mSwipingItemBounds: Rect = Rect()
    private val mRemoveAnimationDuration: Long = removeAnimationDuration + ADDITIONAL_REMOVE_DURATION
    private val mMoveAnimationDuration: Long = moveAnimationDuration
    private val mHorizontal: Boolean = result == RecyclerViewSwipeManager.RESULT_SWIPED_LEFT || result == RecyclerViewSwipeManager.RESULT_SWIPED_RIGHT
    private var mRecyclerView: RecyclerView? = rv
    private var mSwipingItem: RecyclerView.ViewHolder? = swipingItem
    private var mTranslationX: Int = (ViewCompat.getTranslationX(swipingItem.itemView) + 0.5f).toInt()
    private var mTranslationY: Int = (ViewCompat.getTranslationY(swipingItem.itemView) + 0.5f).toInt()
    private var mStartTime: Long = 0
    private var mMoveAnimationInterpolator: Interpolator? = null
    private var mSwipeBackgroundDrawable: Drawable? = null
    private var mPendingNotificationMask: Int = 0

    init {
        CustomRecyclerViewUtils.getViewBounds(swipingItem.itemView, mSwipingItemBounds)
    }

    fun setMoveAnimationInterpolator(interpolator: Interpolator?) {
        mMoveAnimationInterpolator = interpolator
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val elapsedTime = getElapsedTime(mStartTime)
        val scale = determineBackgroundScaleSwipeCompletedSuccessfully(elapsedTime)

        fillSwipingItemBackground(c, mSwipeBackgroundDrawable, scale)

        val swipingItem = mSwipingItem
        if (swipingItem != null && mSwipingItemId == swipingItem.itemId) {
            mTranslationX = (ViewCompat.getTranslationX(swipingItem.itemView) + 0.5f).toInt()
            mTranslationY = (ViewCompat.getTranslationY(swipingItem.itemView) + 0.5f).toInt()
        }

        if (requiresContinuousAnimationAfterSwipeCompletedSuccessfully(elapsedTime)) {
            postInvalidateOnAnimation()
        }
    }

    private fun requiresContinuousAnimationAfterSwipeCompletedSuccessfully(elapsedTime: Long): Boolean {
        return (elapsedTime >= mRemoveAnimationDuration) &&
                (elapsedTime < (mRemoveAnimationDuration + mMoveAnimationDuration))
    }

    private fun determineBackgroundScaleSwipeCompletedSuccessfully(elapsedTime: Long): Float {
        var heightScale = 0.0f
        if (elapsedTime < mRemoveAnimationDuration) {
            heightScale = 1.0f
        } else if (elapsedTime < (mRemoveAnimationDuration + mMoveAnimationDuration)) {
            if (mMoveAnimationDuration != 0L) {
                heightScale = 1.0f - (elapsedTime - mRemoveAnimationDuration).toFloat() / mMoveAnimationDuration.toFloat()
                mMoveAnimationInterpolator?.let {
                    heightScale = it.getInterpolation(heightScale)
                }
            }
        }
        return heightScale
    }

    private fun fillSwipingItemBackground(c: Canvas, drawable: Drawable?, scale: Float) {
        val bounds = mSwipingItemBounds
        val translationX = mTranslationX
        val translationY = mTranslationY
        val hScale = if (mHorizontal) 1.0f else scale
        val vScale = if (mHorizontal) scale else 1.0f

        val width = (hScale * bounds.width() + 0.5f).toInt()
        val height = (vScale * bounds.height() + 0.5f).toInt()

        if (height == 0 || width == 0 || drawable == null) {
            return
        }

        val savedCount = c.save()

        c.clipRect(
            bounds.left + translationX,
            bounds.top + translationY,
            bounds.left + translationX + width,
            bounds.top + translationY + height
        )

        c.translate(
            (bounds.left + translationX - (bounds.width() - width) / 2).toFloat(),
            (bounds.top + translationY - (bounds.height() - height) / 2).toFloat()
        )
        drawable.setBounds(0, 0, bounds.width(), bounds.height())
        drawable.draw(c)

        c.restoreToCount(savedCount)
    }

    private fun postInvalidateOnAnimation() {
        mRecyclerView?.let { ViewCompat.postInvalidateOnAnimation(it) }
    }

    fun start() {
        val swipingItem = mSwipingItem ?: return
        val rv = mRecyclerView ?: return
        val containerView = (swipingItem as SwipeableItemViewHolder).getSwipeableContainerView()

        ViewCompat.animate(containerView).cancel()

        rv.addItemDecoration(this)

        mStartTime = System.currentTimeMillis()
        mTranslationY = (ViewCompat.getTranslationY(swipingItem.itemView) + 0.5f).toInt()
        mSwipeBackgroundDrawable = swipingItem.itemView.background

        postInvalidateOnAnimation()
        notifyDelayed(NOTIFY_REMOVAL_EFFECT_PHASE_1, mRemoveAnimationDuration)
    }

    private fun notifyDelayed(code: Int, delay: Long) {
        val mask = 1 shl code
        if ((mPendingNotificationMask and mask) != 0) {
            return
        }

        mPendingNotificationMask = mPendingNotificationMask or mask

        val rv = mRecyclerView ?: return
        val notification = DelayedNotificationRunner(this, code)
        ViewCompat.postOnAnimationDelayed(rv, notification, delay)
    }

    internal fun onDelayedNotification(code: Int) {
        val mask = 1 shl code
        val elapsedTime = getElapsedTime(mStartTime)

        mPendingNotificationMask = mPendingNotificationMask and mask.inv()

        when (code) {
            NOTIFY_REMOVAL_EFFECT_PHASE_1 -> {
                if (elapsedTime < mRemoveAnimationDuration) {
                    notifyDelayed(NOTIFY_REMOVAL_EFFECT_PHASE_1, mRemoveAnimationDuration - elapsedTime)
                } else {
                    postInvalidateOnAnimation()
                    notifyDelayed(NOTIFY_REMOVAL_EFFECT_END, mMoveAnimationDuration)
                }
            }
            NOTIFY_REMOVAL_EFFECT_END -> {
                finish()
            }
        }
    }

    private fun finish() {
        mRecyclerView?.removeItemDecoration(this)
        postInvalidateOnAnimation()

        mRecyclerView = null
        mSwipingItem = null
        mTranslationY = 0
        mMoveAnimationInterpolator = null
    }

    private class DelayedNotificationRunner(decorator: RemovingItemDecorator, private val mCode: Int) : Runnable {
        private var mRefDecorator: WeakReference<RemovingItemDecorator>? = WeakReference(decorator)

        override fun run() {
            val decorator = mRefDecorator?.get()
            mRefDecorator?.clear()
            mRefDecorator = null

            decorator?.onDelayedNotification(mCode)
        }
    }

    companion object {
        private const val TAG = "RemovingItemDecorator"
        private const val NOTIFY_REMOVAL_EFFECT_PHASE_1 = 0
        private const val NOTIFY_REMOVAL_EFFECT_END = 1
        private const val ADDITIONAL_REMOVE_DURATION: Long = 0

        fun getElapsedTime(initialTime: Long): Long {
            val curTime = System.currentTimeMillis()
            return if (curTime >= initialTime) curTime - initialTime else Long.MAX_VALUE
        }
    }
}
