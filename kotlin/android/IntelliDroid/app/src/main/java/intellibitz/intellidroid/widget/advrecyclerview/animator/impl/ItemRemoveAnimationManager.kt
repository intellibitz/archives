package intellibitz.intellidroid.widget.advrecyclerview.animator.impl

import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import intellibitz.intellidroid.widget.advrecyclerview.animator.BaseItemAnimator

abstract class ItemRemoveAnimationManager(itemAnimator: BaseItemAnimator) :
    BaseItemAnimationManager<RemoveAnimationInfo>(itemAnimator) {

    companion object {
        private const val TAG = "ARVItemRemoveAnimMgr"
    }

    override fun getDuration(): Long {
        return mItemAnimator.removeDuration
    }

    override fun setDuration(duration: Long) {
        mItemAnimator.removeDuration = duration
    }

    override fun dispatchStarting(info: RemoveAnimationInfo, item: RecyclerView.ViewHolder?) {
        if (debugLogEnabled()) {
            Log.d(TAG, "dispatchRemoveStarting($item)")
        }
        if (item != null) {
            mItemAnimator.dispatchRemoveStarting(item)
        }
    }

    override fun dispatchFinished(info: RemoveAnimationInfo, item: RecyclerView.ViewHolder?) {
        if (debugLogEnabled()) {
            Log.d(TAG, "dispatchRemoveFinished($item)")
        }
        if (item != null) {
            mItemAnimator.dispatchRemoveFinished(item)
        }
    }

    override fun endNotStartedAnimation(info: RemoveAnimationInfo, item: RecyclerView.ViewHolder?): Boolean {
        return if (info.holder != null && (item == null || info.holder == item)) {
            onAnimationEndedBeforeStarted(info, info.holder)
            dispatchFinished(info, info.holder)
            info.clear(info.holder)
            true
        } else {
            false
        }
    }

    abstract fun addPendingAnimation(holder: RecyclerView.ViewHolder): Boolean
}
