

package intellibitz.intellidroid.widget.advrecyclerview.animator.impl;

import androidx.recyclerview.widget.RecyclerView;

public class MoveAnimationInfo extends ItemAnimationInfo {
    public final int fromX;
    public final int fromY;
    public final int toX;
    public final int toY;
    public RecyclerView.ViewHolder holder;

    public MoveAnimationInfo(RecyclerView.ViewHolder holder, int fromX, int fromY, int toX, int toY) {
        this.holder = holder;
        this.fromX = fromX;
        this.fromY = fromY;
        this.toX = toX;
        this.toY = toY;
    }

    @Override
    public RecyclerView.ViewHolder getAvailableViewHolder() {
        return holder;
    }

    @Override
    public void clear(RecyclerView.ViewHolder item) {
        if (holder == item) {
            holder = null;
        }
    }

    @Override
    public String toString() {
        return "MoveAnimationInfo{" +
                "holder=" + holder +
                ", fromX=" + fromX +
                ", fromY=" + fromY +
                ", toX=" + toX +
                ", toY=" + toY +
                '}';
    }
}