package intellibitz.intellidroid.widget.advrecyclerview.animator.impl

import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import intellibitz.intellidroid.widget.advrecyclerview.animator.BaseItemAnimator

abstract class ItemChangeAnimationManager(itemAnimator: BaseItemAnimator) :
    BaseItemAnimationManager<ChangeAnimationInfo>(itemAnimator) {

    companion object {
        private const val TAG = "ARVItemChangeAnimMgr"
    }

    override fun dispatchStarting(info: ChangeAnimationInfo, item: RecyclerView.ViewHolder?) {
        if (debugLogEnabled()) {
            Log.d(TAG, "dispatchChangeStarting($item)")
        }
        if (item != null) {
            mItemAnimator.dispatchChangeStarting(item, item == info.oldHolder)
        }
    }

    override fun dispatchFinished(info: ChangeAnimationInfo, item: RecyclerView.ViewHolder?) {
        if (debugLogEnabled()) {
            Log.d(TAG, "dispatchChangeFinished($item)")
        }
        if (item != null) {
            mItemAnimator.dispatchChangeFinished(item, item == info.oldHolder)
        }
    }

    override fun getDuration(): Long {
        return mItemAnimator.changeDuration
    }

    override fun setDuration(duration: Long) {
        mItemAnimator.changeDuration = duration
    }

    override fun onCreateAnimation(info: ChangeAnimationInfo) {
        if (info.oldHolder != null && info.oldHolder!!.itemView != null) {
            onCreateChangeAnimationForOldItem(info)
        }
        if (info.newHolder != null && info.newHolder!!.itemView != null) {
            onCreateChangeAnimationForNewItem(info)
        }
    }

    override fun endNotStartedAnimation(info: ChangeAnimationInfo, item: RecyclerView.ViewHolder?): Boolean {
        if (info.oldHolder != null && (item == null || info.oldHolder == item)) {
            onAnimationEndedBeforeStarted(info, info.oldHolder)
            dispatchFinished(info, info.oldHolder)
            info.clear(info.oldHolder)
        }
        if (info.newHolder != null && (item == null || info.newHolder == item)) {
            onAnimationEndedBeforeStarted(info, info.newHolder)
            dispatchFinished(info, info.newHolder)
            info.clear(info.newHolder)
        }
        return info.oldHolder == null && info.newHolder == null
    }

    protected abstract fun onCreateChangeAnimationForNewItem(info: ChangeAnimationInfo)
    protected abstract fun onCreateChangeAnimationForOldItem(info: ChangeAnimationInfo)

    abstract fun addPendingAnimation(
        oldHolder: RecyclerView.ViewHolder?,
        newHolder: RecyclerView.ViewHolder?,
        fromX: Int,
        fromY: Int,
        toX: Int,
        toY: Int
    ): Boolean
}
