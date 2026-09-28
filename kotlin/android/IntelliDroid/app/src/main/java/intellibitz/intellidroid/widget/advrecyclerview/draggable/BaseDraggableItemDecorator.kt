package intellibitz.intellidroid.widget.advrecyclerview.draggable

import android.os.Build
import android.view.View
import android.view.animation.Interpolator
import androidx.core.view.ViewCompat
import androidx.core.view.ViewPropertyAnimatorListener
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

internal abstract class BaseDraggableItemDecorator(
    protected val mRecyclerView: RecyclerView,
    protected var mDraggingItemViewHolder: RecyclerView.ViewHolder?
) : RecyclerView.ItemDecoration() {

    private val mReturnToDefaultPositionAnimateThreshold: Int
    private var mReturnToDefaultPositionDuration = 200
    private var mReturnToDefaultPositionInterpolator: Interpolator? = null

    init {
        val displayDensity = mRecyclerView.resources.displayMetrics.density
        mReturnToDefaultPositionAnimateThreshold = (RETURN_TO_DEFAULT_POS_ANIMATE_THRESHOLD_DP * displayDensity + 0.5f).toInt()
    }

    companion object {
        private const val RETURN_TO_DEFAULT_POS_ANIMATE_THRESHOLD_DP = 2
        private const val RETURN_TO_DEFAULT_POS_ANIMATE_THRESHOLD_MSEC = 20

        @JvmStatic
        protected fun setItemTranslation(rv: RecyclerView, holder: RecyclerView.ViewHolder, x: Float, y: Float) {
            val itemAnimator = rv.itemAnimator
            itemAnimator?.endAnimation(holder)
            ViewCompat.setTranslationX(holder.itemView, x)
            ViewCompat.setTranslationY(holder.itemView, y)
        }

        private fun supportsViewPropertyAnimation(): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB
        }
    }

    fun setReturnToDefaultPositionAnimationDuration(duration: Int) {
        mReturnToDefaultPositionDuration = duration
    }

    fun setReturnToDefaultPositionAnimationInterpolator(interpolator: Interpolator?) {
        mReturnToDefaultPositionInterpolator = interpolator
    }

    protected fun moveToDefaultPosition(targetView: View, animate: Boolean) {
        val curTranslationX = ViewCompat.getTranslationX(targetView).toInt()
        val curTranslationY = ViewCompat.getTranslationY(targetView).toInt()
        val halfItemWidth = targetView.width / 2
        val halfItemHeight = targetView.height / 2
        val translationProportionX = if (halfItemWidth > 0) abs(curTranslationX.toFloat() / halfItemWidth) else 0f
        val translationProportionY = if (halfItemHeight > 0) abs(curTranslationY.toFloat() / halfItemHeight) else 0f
        val tx = 1.0f - min(translationProportionX, 1.0f)
        val ty = 1.0f - min(translationProportionY, 1.0f)
        val animDurationX = (mReturnToDefaultPositionDuration * (1.0f - (tx * tx)) + 0.5f).toInt()
        val animDurationY = (mReturnToDefaultPositionDuration * (1.0f - (ty * ty)) + 0.5f).toInt()
        val animDuration = max(animDurationX, animDurationY)
        val maxAbsTranslation = max(abs(curTranslationX), abs(curTranslationY))

        if (supportsViewPropertyAnimation() && animate &&
            (animDuration > RETURN_TO_DEFAULT_POS_ANIMATE_THRESHOLD_MSEC) &&
            (maxAbsTranslation > mReturnToDefaultPositionAnimateThreshold)
        ) {
            val animator = ViewCompat.animate(targetView)
            animator.cancel()
            animator.duration = animDuration.toLong()
            animator.setInterpolator(mReturnToDefaultPositionInterpolator)
            animator.translationX(0.0f)
            animator.translationY(0.0f)
            animator.setListener(object : ViewPropertyAnimatorListener {
                override fun onAnimationStart(view: View) {}

                override fun onAnimationEnd(view: View) {
                    animator.setListener(null)
                    ViewCompat.setTranslationX(view, 0f)
                    ViewCompat.setTranslationY(view, 0f)

                    if (view.parent is RecyclerView) {
                        ViewCompat.postInvalidateOnAnimation(view.parent as RecyclerView)
                    }
                }

                override fun onAnimationCancel(view: View) {}
            })
            animator.start()
        } else {
            ViewCompat.setTranslationX(targetView, 0f)
            ViewCompat.setTranslationY(targetView, 0f)
        }
    }
}
