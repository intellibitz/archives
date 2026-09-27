

package intellibitz.intellidroid.widget.advrecyclerview.utils;

import android.view.View;

import intellibitz.intellidroid.widget.advrecyclerview.draggable.DraggableItemViewHolder;
import intellibitz.intellidroid.widget.advrecyclerview.draggable.annotation.DraggableItemStateFlags;
import intellibitz.intellidroid.widget.advrecyclerview.draggable.DraggableItemViewHolder;
import intellibitz.intellidroid.widget.advrecyclerview.draggable.annotation.DraggableItemStateFlags;

import androidx.recyclerview.widget.RecyclerView;
import intellibitz.intellidroid.widget.advrecyclerview.draggable.DraggableItemViewHolder;

public abstract class AbstractDraggableItemViewHolder extends RecyclerView.ViewHolder implements DraggableItemViewHolder {
    @DraggableItemStateFlags
    private int mDragStateFlags;

    public AbstractDraggableItemViewHolder(View itemView) {
        super(itemView);
    }

    @Override
    @DraggableItemStateFlags
    public int getDragStateFlags() {
        return mDragStateFlags;
    }

    @Override
    public void setDragStateFlags(@DraggableItemStateFlags int flags) {
        mDragStateFlags = flags;
    }
}
