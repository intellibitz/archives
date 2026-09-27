

package intellibitz.intellidroid.widget.advrecyclerview.expandable;

import intellibitz.intellidroid.widget.advrecyclerview.draggable.ItemDraggableRange;
import intellibitz.intellidroid.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;

import androidx.recyclerview.widget.RecyclerView;

public interface ExpandableDraggableItemAdapter<GVH extends RecyclerView.ViewHolder, CVH extends RecyclerView.ViewHolder> {
    /**
     * Called when user is attempt to drag the group item.
     *
     * @param holder        The group ViewHolder which is associated to item user is attempt to start dragging.
     * @param groupPosition Group position.
     * @param x             Touched X position. Relative from the itemView's top-left.
     * @param y             Touched Y position. Relative from the itemView's top-left.
     * @return Whether can start dragging.
     */
    boolean onCheckGroupCanStartDrag(GVH holder, int groupPosition, int x, int y);

    /**
     * Called when user is attempt to drag the child item.
     *
     * @param holder        The child ViewHolder which is associated to item user is attempt to start dragging.
     * @param groupPosition Group position.
     * @param childPosition Child position.
     * @param x             Touched X position. Relative from the itemView's top-left.
     * @param y             Touched Y position. Relative from the itemView's top-left.
     * @return Whether can start dragging.
     */
    boolean onCheckChildCanStartDrag(CVH holder, int groupPosition, int childPosition, int x, int y);

    /**
     * Called after the {@link #onCheckGroupCanStartDrag(RecyclerView.ViewHolder, int, int, int)} method returned true.
     *
     * @param holder        The ViewHolder which is associated to item user is attempt to start dragging.
     * @param groupPosition Group position.
     * @return null: no constraints (= new ItemDraggableRange(0, getGroupCount() - 1)),
     * otherwise: the range specified item can be drag-sortable.
     */
    ItemDraggableRange onGetGroupItemDraggableRange(GVH holder, int groupPosition);

    /**
     * Called after the {@link #onCheckChildCanStartDrag(RecyclerView.ViewHolder, int, int, int, int)} method returned true.
     *
     * @param holder        The ViewHolder which is associated to item user is attempt to start dragging.
     * @param groupPosition Group position.
     * @param childPosition Child position.
     * @return null: no constraints (= new ItemDraggableRange(0, getGroupCount() - 1)),
     * otherwise: the range specified item can be drag-sortable.
     */
    ItemDraggableRange onGetChildItemDraggableRange(CVH holder, int groupPosition, int childPosition);

    /**
     * Called when group item is moved. Should apply the move operation result to data set.
     *
     * @param fromGroupPosition Previous group position of the item.
     * @param toGroupPosition   New group position of the item.
     */
    void onMoveGroupItem(int fromGroupPosition, int toGroupPosition);

    /**
     * Called when child item is moved. Should apply the move operation result to data set.
     *
     * @param fromGroupPosition Previous group position of the item.
     * @param fromChildPosition Previous child position of the item.
     * @param toGroupPosition   New group position of the item.
     * @param toChildPosition   New child position of the item.
     */
    void onMoveChildItem(int fromGroupPosition, int fromChildPosition, int toGroupPosition, int toChildPosition);


    /**
     * Called while dragging in order to check whether the dragging item can be dropped to the specified position.
     * <p/>
     * NOTE: This method will be called when the checkCanDrop option is enabled by {@link RecyclerViewDragDropManager#setCheckCanDropEnabled(boolean)}.
     *
     * @param draggingGroupPosition The position of the currently dragging group item.
     * @param dropGroupPosition     The position of a group item whether to check can be dropped or not.
     * @return Whether can be dropped to the specified position.
     */
    boolean onCheckGroupCanDrop(int draggingGroupPosition, int dropGroupPosition);


    /**
     * Called while dragging in order to check whether the dragging item can be dropped to the specified position.
     * <p/>
     * NOTE: This method will be called when the checkCanDrop option is enabled by {@link RecyclerViewDragDropManager#setCheckCanDropEnabled(boolean)}.
     *
     * @param draggingGroupPosition The group position of the currently dragging item.
     * @param draggingChildPosition The child position of the currently dragging item.
     * @param dropGroupPosition     The group position to check whether the dragging item can be dropped or not.
     * @param dropChildPosition     The child position to check whether the dragging item can be dropped or not.
     * @return Whether can be dropped to the specified position.
     */
    boolean onCheckChildCanDrop(int draggingGroupPosition, int draggingChildPosition, int dropGroupPosition, int dropChildPosition);
}
