

package intellibitz.intellidroid.widget.advrecyclerview.animator.impl;

import android.util.Log;

import intellibitz.intellidroid.widget.advrecyclerview.animator.BaseItemAnimator;

import androidx.recyclerview.widget.RecyclerView;
import intellibitz.intellidroid.widget.advrecyclerview.animator.BaseItemAnimator;

public abstract class ItemChangeAnimationManager extends BaseItemAnimationManager<ChangeAnimationInfo> {
    private static final String TAG = "ARVItemChangeAnimMgr";

    public ItemChangeAnimationManager(BaseItemAnimator itemAnimator) {
        super(itemAnimator);
    }

    @Override
    public void dispatchStarting(ChangeAnimationInfo info, RecyclerView.ViewHolder item) {
        if (debugLogEnabled()) {
            Log.d(TAG, "dispatchChangeStarting(" + item + ")");
        }
        mItemAnimator.dispatchChangeStarting(item, (item == info.oldHolder));
    }

    @Override
    public void dispatchFinished(ChangeAnimationInfo info, RecyclerView.ViewHolder item) {
        if (debugLogEnabled()) {
            Log.d(TAG, "dispatchChangeFinished(" + item + ")");
        }
        mItemAnimator.dispatchChangeFinished(item, (item == info.oldHolder));
    }

    @Override
    public long getDuration() {
        return mItemAnimator.getChangeDuration();
    }

    @Override
    public void setDuration(long duration) {
        mItemAnimator.setChangeDuration(duration);
    }

    @Override
    protected void onCreateAnimation(ChangeAnimationInfo info) {
        if (info.oldHolder != null && info.oldHolder.itemView != null) {
            onCreateChangeAnimationForOldItem(info);
        }

        if (info.newHolder != null && info.newHolder.itemView != null) {
            onCreateChangeAnimationForNewItem(info);
        }
    }

    @Override
    protected boolean endNotStartedAnimation(ChangeAnimationInfo info, RecyclerView.ViewHolder item) {
        if ((info.oldHolder != null) && ((item == null) || (info.oldHolder == item))) {
            onAnimationEndedBeforeStarted(info, info.oldHolder);
            dispatchFinished(info, info.oldHolder);
            info.clear(info.oldHolder);
        }

        if ((info.newHolder != null) && ((item == null) || (info.newHolder == item))) {
            onAnimationEndedBeforeStarted(info, info.newHolder);
            dispatchFinished(info, info.newHolder);
            info.clear(info.newHolder);
        }

        return (info.oldHolder == null && info.newHolder == null);
    }

    protected abstract void onCreateChangeAnimationForNewItem(ChangeAnimationInfo info);

    protected abstract void onCreateChangeAnimationForOldItem(ChangeAnimationInfo info);

    public abstract boolean addPendingAnimation(RecyclerView.ViewHolder oldHolder, RecyclerView.ViewHolder newHolder, int fromX, int fromY, int toX, int toY);
}
