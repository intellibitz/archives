package intellibitz.intellidroid.widget.advrecyclerview.animator.impl

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.ViewPropertyAnimatorCompat
import androidx.core.view.ViewPropertyAnimatorListener
import androidx.recyclerview.widget.RecyclerView
import intellibitz.intellidroid.widget.advrecyclerview.animator.BaseItemAnimator
import java.util.ArrayList

abstract class BaseItemAnimationManager<T : ItemAnimationInfo>(
    @JvmField protected val mItemAnimator: BaseItemAnimator
) {
    @JvmField
    protected val mPending: MutableList<T> = ArrayList()
    @JvmField
    protected val mDeferredReadySets: MutableList<MutableList<T>> = ArrayList()
    @JvmField
    protected val mActive: MutableList<RecyclerView.ViewHolder> = ArrayList()

    protected fun debugLogEnabled(): Boolean {
        return mItemAnimator.debugLogEnabled()
    }

    fun hasPending(): Boolean {
        return mPending.isNotEmpty()
    }

    fun isRunning(): Boolean {
        return mPending.isNotEmpty() || mActive.isNotEmpty() || mDeferredReadySets.isNotEmpty()
    }

    fun removeFromActive(item: RecyclerView.ViewHolder): Boolean {
        return mActive.remove(item)
    }

    fun cancelAllStartedAnimations() {
        val active = mActive
        for (i in active.indices.reversed()) {
            val view = active[i].itemView
            ViewCompat.animate(view).cancel()
        }
    }

    fun runPendingAnimations(deferred: Boolean, deferredDelay: Long) {
        val ready: MutableList<T> = ArrayList(mPending)
        mPending.clear()

        if (deferred) {
            mDeferredReadySets.add(ready)
            val process = Runnable {
                for (info in ready) {
                    createAnimation(info)
                }
                ready.clear()
                mDeferredReadySets.remove(ready)
            }
            val view = ready[0].availableViewHolder!!.itemView
            ViewCompat.postOnAnimationDelayed(view, process, deferredDelay)
        } else {
            for (info in ready) {
                createAnimation(info)
            }
            ready.clear()
        }
    }

    abstract fun dispatchStarting(info: T, item: RecyclerView.ViewHolder?)
    abstract fun dispatchFinished(info: T, item: RecyclerView.ViewHolder?)
    abstract fun getDuration(): Long
    abstract fun setDuration(duration: Long)

    fun endPendingAnimations(item: RecyclerView.ViewHolder?) {
        val pending = mPending
        for (i in pending.indices.reversed()) {
            val info = pending[i]
            if (endNotStartedAnimation(info, item) && item != null) {
                pending.removeAt(i)
            }
        }
        if (item == null) {
            pending.clear()
        }
    }

    fun endAllPendingAnimations() {
        endPendingAnimations(null)
    }

    fun endDeferredReadyAnimations(item: RecyclerView.ViewHolder?) {
        for (i in mDeferredReadySets.indices.reversed()) {
            val ready = mDeferredReadySets[i]
            for (j in ready.indices.reversed()) {
                val info = ready[j]
                if (endNotStartedAnimation(info, item) && item != null) {
                    ready.removeAt(j)
                }
            }
            if (item == null) {
                ready.clear()
            }
            if (ready.isEmpty()) {
                mDeferredReadySets.removeAt(i)
            }
        }
    }

    fun endAllDeferredReadyAnimations() {
        endDeferredReadyAnimations(null)
    }

    internal fun createAnimation(info: T) {
        onCreateAnimation(info)
    }

    protected fun endAnimation(holder: RecyclerView.ViewHolder) {
        mItemAnimator.endAnimation(holder)
    }

    protected fun dispatchFinishedWhenDone() {
        mItemAnimator.dispatchFinishedWhenDone()
    }

    protected fun enqueuePendingAnimationInfo(info: T?) {
        if (info == null) {
            throw IllegalStateException("info is null")
        }
        mPending.add(info)
    }

    protected fun startActiveItemAnimation(
        info: T,
        holder: RecyclerView.ViewHolder,
        animator: ViewPropertyAnimatorCompat
    ) {
        animator.setListener(BaseAnimatorListener(this, info, holder, animator))
        addActiveAnimationTarget(holder)
        animator.start()
    }

    private fun addActiveAnimationTarget(item: RecyclerView.ViewHolder?) {
        if (item == null) {
            throw IllegalStateException("item is null")
        }
        mActive.add(item)
    }

    protected abstract fun onCreateAnimation(info: T)
    protected abstract fun onAnimationEndedSuccessfully(info: T, item: RecyclerView.ViewHolder?)
    protected abstract fun onAnimationEndedBeforeStarted(info: T, item: RecyclerView.ViewHolder?)
    protected abstract fun onAnimationCancel(info: T, item: RecyclerView.ViewHolder?)
    protected abstract fun endNotStartedAnimation(info: T, item: RecyclerView.ViewHolder?): Boolean

    protected class BaseAnimatorListener<T : ItemAnimationInfo>(
        private var mManager: BaseItemAnimationManager<T>?,
        private var mAnimationInfo: T?,
        private var mHolder: RecyclerView.ViewHolder?,
        private var mAnimator: ViewPropertyAnimatorCompat?
    ) : ViewPropertyAnimatorListener {

        override fun onAnimationStart(view: View) {
            mManager?.dispatchStarting(mAnimationInfo!!, mHolder)
        }

        override fun onAnimationEnd(view: View) {
            val manager = mManager ?: return
            val info = mAnimationInfo ?: return
            val holder = mHolder ?: return

            mAnimator?.setListener(null)
            mManager = null
            mAnimationInfo = null
            mHolder = null
            mAnimator = null

            manager.onAnimationEndedSuccessfully(info, holder)
            manager.dispatchFinished(info, holder)
            info.clear(holder)
            manager.mActive.remove(holder)
            manager.dispatchFinishedWhenDone()
        }

        override fun onAnimationCancel(view: View) {
            mManager?.onAnimationCancel(mAnimationInfo!!, mHolder)
        }
    }
}
