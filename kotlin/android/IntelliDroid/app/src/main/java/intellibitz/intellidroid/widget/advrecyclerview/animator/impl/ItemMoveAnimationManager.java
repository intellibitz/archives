

package intellibitz.intellidroid.widget.advrecyclerview.animator.impl;

import android.util.Log;

import intellibitz.intellidroid.widget.advrecyclerview.animator.BaseItemAnimator;

import androidx.recyclerview.widget.RecyclerView;
import intellibitz.intellidroid.widget.advrecyclerview.animator.BaseItemAnimator;

public abstract class ItemMoveAnimationManager extends BaseItemAnimationManager<MoveAnimationInfo> {
    public static final String TAG = "ARVItemMoveAnimMgr";

    public ItemMoveAnimationManager(BaseItemAnimator itemAnimator) {
        super(itemAnimator);
    }

    @Override
    public long getDuration() {
        return mItemAnimator.getMoveDuration();
    }

    @Override
    public void setDuration(long duration) {
        mItemAnimator.setMoveDuration(duration);
    }

    @Override
    public void dispatchStarting(MoveAnimationInfo info, RecyclerView.ViewHolder item) {
        if (debugLogEnabled()) {
            Log.d(TAG, "dispatchMoveStarting(" + item + ")");
        }
        mItemAnimator.dispatchMoveStarting(item);
    }

    @Override
    public void dispatchFinished(MoveAnimationInfo info, RecyclerView.ViewHolder item) {
        if (debugLogEnabled()) {
            Log.d(TAG, "dispatchMoveFinished(" + item + ")");
        }
        mItemAnimator.dispatchMoveFinished(item);
    }

    @Override
    protected boolean endNotStartedAnimation(MoveAnimationInfo info, RecyclerView.ViewHolder item) {
        if ((info.holder != null) && ((item == null) || (info.holder == item))) {
            onAnimationEndedBeforeStarted(info, info.holder);
            dispatchFinished(info, info.holder);
            info.clear(info.holder);
            return true;
        } else {
            return false;
        }
    }

    public abstract boolean addPendingAnimation(RecyclerView.ViewHolder item, int fromX, int fromY, int toX, int toY);
}
