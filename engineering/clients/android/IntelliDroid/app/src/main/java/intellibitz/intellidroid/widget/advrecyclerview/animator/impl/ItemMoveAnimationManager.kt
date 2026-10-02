package intellibitz.intellidroid.widget.advrecyclerview.animator.impl

import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import intellibitz.intellidroid.widget.advrecyclerview.animator.BaseItemAnimator

abstract class ItemMoveAnimationManager(itemAnimator: BaseItemAnimator) :
    BaseItemAnimationManager<MoveAnimationInfo>(itemAnimator) {

    companion object {
        const val TAG = "ARVItemMoveAnimMgr"
    }

    override fun getDuration(): Long {
        return mItemAnimator.moveDuration
    }

    override fun setDuration(duration: Long) {
        mItemAnimator.moveDuration = duration
    }

    override fun dispatchStarting(info: MoveAnimationInfo, item: RecyclerView.ViewHolder?) {
        if (debugLogEnabled()) {
            Log.d(TAG, "dispatchMoveStarting($item)")
        }
        if (item != null) {
            mItemAnimator.dispatchMoveStarting(item)
        }
    }

    override fun dispatchFinished(info: MoveAnimationInfo, item: RecyclerView.ViewHolder?) {
        if (debugLogEnabled()) {
            Log.d(TAG, "dispatchMoveFinished($item)")
        }
        if (item != null) {
            mItemAnimator.dispatchMoveFinished(item)
        }
    }

    override fun endNotStartedAnimation(info: MoveAnimationInfo, item: RecyclerView.ViewHolder?): Boolean {
        return if (info.holder != null && (item == null || info.holder == item)) {
            onAnimationEndedBeforeStarted(info, info.holder)
            dispatchFinished(info, info.holder)
            info.clear(info.holder)
            true
        } else {
            false
        }
    }

    abstract fun addPendingAnimation(
        item: RecyclerView.ViewHolder,
        fromX: Int,
        fromY: Int,
        toX: Int,
        toY: Int
    ): Boolean
}
