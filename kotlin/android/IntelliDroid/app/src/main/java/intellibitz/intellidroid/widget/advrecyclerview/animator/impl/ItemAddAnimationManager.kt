package intellibitz.intellidroid.widget.advrecyclerview.animator.impl

import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import intellibitz.intellidroid.widget.advrecyclerview.animator.BaseItemAnimator

abstract class ItemAddAnimationManager(itemAnimator: BaseItemAnimator) :
    BaseItemAnimationManager<AddAnimationInfo>(itemAnimator) {

    companion object {
        private const val TAG = "ARVItemAddAnimMgr"
    }

    override fun getDuration(): Long {
        return mItemAnimator.addDuration
    }

    override fun setDuration(duration: Long) {
        mItemAnimator.addDuration = duration
    }

    override fun dispatchStarting(info: AddAnimationInfo, item: RecyclerView.ViewHolder?) {
        if (debugLogEnabled()) {
            Log.d(TAG, "dispatchAddStarting($item)")
        }
        if (item != null) {
            mItemAnimator.dispatchAddStarting(item)
        }
    }

    override fun dispatchFinished(info: AddAnimationInfo, item: RecyclerView.ViewHolder?) {
        if (debugLogEnabled()) {
            Log.d(TAG, "dispatchAddFinished($item)")
        }
        if (item != null) {
            mItemAnimator.dispatchAddFinished(item)
        }
    }

    override fun endNotStartedAnimation(info: AddAnimationInfo, item: RecyclerView.ViewHolder?): Boolean {
        return if (info.holder != null && (item == null || info.holder == item)) {
            onAnimationEndedBeforeStarted(info, info.holder)
            dispatchFinished(info, info.holder)
            info.clear(info.holder)
            true
        } else {
            false
        }
    }

    abstract fun addPendingAnimation(item: RecyclerView.ViewHolder): Boolean
}
