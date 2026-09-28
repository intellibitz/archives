package intellibitz.intellidroid.widget.advrecyclerview.utils

import android.view.View
import intellibitz.intellidroid.widget.advrecyclerview.draggable.DraggableItemViewHolder
import intellibitz.intellidroid.widget.advrecyclerview.draggable.annotation.DraggableItemStateFlags

abstract class AbstractDraggableSwipeableItemViewHolder(itemView: View) :
    AbstractSwipeableItemViewHolder(itemView), DraggableItemViewHolder {

    @DraggableItemStateFlags
    private var mDragStateFlags = 0

    @DraggableItemStateFlags
    override fun getDragStateFlags(): Int {
        return mDragStateFlags
    }

    override fun setDragStateFlags(@DraggableItemStateFlags flags: Int) {
        mDragStateFlags = flags
    }
}
