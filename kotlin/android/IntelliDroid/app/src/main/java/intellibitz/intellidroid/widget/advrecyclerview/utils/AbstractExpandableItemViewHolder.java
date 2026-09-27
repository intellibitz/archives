

package intellibitz.intellidroid.widget.advrecyclerview.utils;

import android.view.View;

import intellibitz.intellidroid.widget.advrecyclerview.expandable.ExpandableItemViewHolder;
import intellibitz.intellidroid.widget.advrecyclerview.expandable.annotation.ExpandableItemStateFlags;
import intellibitz.intellidroid.widget.advrecyclerview.expandable.ExpandableItemViewHolder;
import intellibitz.intellidroid.widget.advrecyclerview.expandable.annotation.ExpandableItemStateFlags;

import androidx.recyclerview.widget.RecyclerView;
import intellibitz.intellidroid.widget.advrecyclerview.expandable.ExpandableItemViewHolder;

public abstract class AbstractExpandableItemViewHolder extends RecyclerView.ViewHolder implements ExpandableItemViewHolder {
    @ExpandableItemStateFlags
    private int mExpandStateFlags;

    public AbstractExpandableItemViewHolder(View itemView) {
        super(itemView);
    }

    @Override
    @ExpandableItemStateFlags
    public int getExpandStateFlags() {
        return mExpandStateFlags;
    }

    @Override
    public void setExpandStateFlags(@ExpandableItemStateFlags int flags) {
        mExpandStateFlags = flags;
    }
}
