package intellibitz.intellidroid.widget.advrecyclerview.draggable

import androidx.recyclerview.widget.RecyclerView

interface DraggableItemAdapter<T : RecyclerView.ViewHolder> {

    /**
     * Called when user is attempt to drag the item.
     *
     * @param holder The ViewHolder which is associated to item user is attempt to start dragging.
     * @param position The position of the item within the adapter's data set.
     * @param x Touched X position. Relative from the itemView's top-left.
     * @param y Touched Y position. Relative from the itemView's top-left.
     * @return Whether can start dragging.
     */
    fun onCheckCanStartDrag(holder: T, position: Int, x: Int, y: Int): Boolean

    /**
     * Called after the [onCheckCanStartDrag] method returned true.
     *
     * @param holder The ViewHolder which is associated to item user is attempt to start dragging.
     * @param position The position of the item within the adapter's data set.
     * @return null: no constraints (= new ItemDraggableRange(0, getItemCount() - 1)),
     * otherwise: the range specified item can be drag-sortable.
     */
    fun onGetItemDraggableRange(holder: T, position: Int): ItemDraggableRange?

    /**
     * Called when item is moved. Should apply the move operation result to data set.
     *
     * @param fromPosition Previous position of the item.
     * @param toPosition New position of the item.
     */
    fun onMoveItem(fromPosition: Int, toPosition: Int)

    /**
     * Called while dragging in order to check whether the dragging item can be dropped to the specified position.
     *
     * @param draggingPosition The position of the currently dragging item.
     * @param dropPosition The position to check whether the dragging item can be dropped or not.
     * @return Whether can be dropped to the specified position.
     */
    fun onCheckCanDrop(draggingPosition: Int, dropPosition: Int): Boolean
}
