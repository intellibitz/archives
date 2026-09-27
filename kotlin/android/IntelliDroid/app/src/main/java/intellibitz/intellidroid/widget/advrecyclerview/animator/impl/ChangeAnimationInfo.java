

package intellibitz.intellidroid.widget.advrecyclerview.animator.impl;

import androidx.recyclerview.widget.RecyclerView;

public class ChangeAnimationInfo extends ItemAnimationInfo {
    public RecyclerView.ViewHolder newHolder, oldHolder;
    public int fromX, fromY, toX, toY;

    public ChangeAnimationInfo(RecyclerView.ViewHolder oldHolder, RecyclerView.ViewHolder newHolder,
                               int fromX, int fromY, int toX, int toY) {
        this.oldHolder = oldHolder;
        this.newHolder = newHolder;
        this.fromX = fromX;
        this.fromY = fromY;
        this.toX = toX;
        this.toY = toY;
    }

    @Override
    public RecyclerView.ViewHolder getAvailableViewHolder() {
        return (oldHolder != null) ? oldHolder : newHolder;
    }

    @Override
    public void clear(RecyclerView.ViewHolder item) {
        if (oldHolder == item) {
            oldHolder = null;
        }
        if (newHolder == item) {
            newHolder = null;
        }
        if (oldHolder == null && newHolder == null) {
            fromX = 0;
            fromY = 0;
            toX = 0;
            toY = 0;
        }
    }

    @Override
    public String toString() {
        return "ChangeInfo{" +
                ", oldHolder=" + oldHolder +
                ", newHolder=" + newHolder +
                ", fromX=" + fromX +
                ", fromY=" + fromY +
                ", toX=" + toX +
                ", toY=" + toY +
                '}';
    }
}
