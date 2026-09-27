

package intellibitz.intellidroid.widget.advrecyclerview.animator.impl;

import androidx.recyclerview.widget.RecyclerView;

public abstract class ItemAnimationInfo {
    public abstract RecyclerView.ViewHolder getAvailableViewHolder();

    public abstract void clear(RecyclerView.ViewHolder holder);
}

