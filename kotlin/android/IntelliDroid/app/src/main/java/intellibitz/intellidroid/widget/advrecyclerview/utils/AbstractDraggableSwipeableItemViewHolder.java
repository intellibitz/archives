

package intellibitz.intellidroid.widget.advrecyclerview.utils;

import android.view.View;

import intellibitz.intellidroid.widget.advrecyclerview.draggable.DraggableItemViewHolder;
import intellibitz.intellidroid.widget.advrecyclerview.draggable.annotation.DraggableItemStateFlags;
import intellibitz.intellidroid.widget.advrecyclerview.draggable.DraggableItemViewHolder;
import intellibitz.intellidroid.widget.advrecyclerview.draggable.annotation.DraggableItemStateFlags;
import intellibitz.intellidroid.widget.advrecyclerview.draggable.DraggableItemViewHolder;

public abstract class AbstractDraggableSwipeableItemViewHolder extends AbstractSwipeableItemViewHolder implements DraggableItemViewHolder {
    @DraggableItemStateFlags
    private int mDragStateFlags;

    public AbstractDraggableSwipeableItemViewHolder(View itemView) {
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
