package intellibitz.intellidroid.widget.advrecyclerview.expandable

import androidx.recyclerview.widget.RecyclerView
import intellibitz.intellidroid.widget.advrecyclerview.draggable.ItemDraggableRange

interface ExpandableDraggableItemAdapter<GVH : RecyclerView.ViewHolder, CVH : RecyclerView.ViewHolder> {
    /**
     * Called when user is attempt to drag the group item.
     *
     * @param holder The group ViewHolder which is associated to item user is attempt to start dragging.
     * @param groupPosition Group position.
     * @param x Touched X position. Relative from the itemView's top-left.
     * @param y Touched Y position. Relative from the itemView's top-left.
     * @return Whether can start dragging.
     */
    fun onCheckGroupCanStartDrag(holder: GVH, groupPosition: Int, x: Int, y: Int): Boolean

    /**
     * Called when user is attempt to drag the child item.
     *
     * @param holder The child ViewHolder which is associated to item user is attempt to start dragging.
     * @param groupPosition Group position.
     * @param childPosition Child position.
     * @param x Touched X position. Relative from the itemView's top-left.
     * @param y Touched Y position. Relative from the itemView's top-left.
     * @return Whether can start dragging.
     */
    fun onCheckChildCanStartDrag(holder: CVH, groupPosition: Int, childPosition: Int, x: Int, y: Int): Boolean

    /**
     * Called after the [onCheckGroupCanStartDrag] method returned true.
     *
     * @param holder The ViewHolder which is associated to item user is attempt to start dragging.
     * @param groupPosition Group position.
     * @return null: no constraints (= new ItemDraggableRange(0, getGroupCount() - 1)),
     * otherwise: the range specified item can be drag-sortable.
     */
    fun onGetGroupItemDraggableRange(holder: GVH, groupPosition: Int): ItemDraggableRange?

    /**
     * Called after the [onCheckChildCanStartDrag] method returned true.
     *
     * @param holder The ViewHolder which is associated to item user is attempt to start dragging.
     * @param groupPosition Group position.
     * @param childPosition Child position.
     * @return null: no constraints (= new ItemDraggableRange(0, getGroupCount() - 1)),
     * otherwise: the range specified item can be drag-sortable.
     */
    fun onGetChildItemDraggableRange(holder: CVH, groupPosition: Int, childPosition: Int): ItemDraggableRange?

    /**
     * Called when group item is moved. Should apply the move operation result to data set.
     *
     * @param fromGroupPosition Previous group position of the item.
     * @param toGroupPosition New group position of the item.
     */
    fun onMoveGroupItem(fromGroupPosition: Int, toGroupPosition: Int)

    /**
     * Called when child item is moved. Should apply the move operation result to data set.
     *
     * @param fromGroupPosition Previous group position of the item.
     * @param fromChildPosition Previous child position of the item.
     * @param toGroupPosition New group position of the item.
     * @param toChildPosition New child position of the item.
     */
    fun onMoveChildItem(fromGroupPosition: Int, fromChildPosition: Int, toGroupPosition: Int, toChildPosition: Int)

    /**
     * Called while dragging in order to check whether the dragging item can be dropped to the specified position.
     *
     * @param draggingGroupPosition The position of the currently dragging group item.
     * @param dropGroupPosition The position of a group item whether to check can be dropped or not.
     * @return Whether can be dropped to the specified position.
     */
    fun onCheckGroupCanDrop(draggingGroupPosition: Int, dropGroupPosition: Int): Boolean

    /**
     * Called while dragging in order to check whether the dragging item can be dropped to the specified position.
     *
     * @param draggingGroupPosition The group position of the currently dragging item.
     * @param draggingChildPosition The child position of the currently dragging item.
     * @param dropGroupPosition The group position to check whether the dragging item can be dropped or not.
     * @param dropChildPosition The child position to check whether the dragging item can be dropped or not.
     * @return Whether can be dropped to the specified position.
     */
    fun onCheckChildCanDrop(
        draggingGroupPosition: Int,
        draggingChildPosition: Int,
        dropGroupPosition: Int,
        dropChildPosition: Int
    ): Boolean
}
