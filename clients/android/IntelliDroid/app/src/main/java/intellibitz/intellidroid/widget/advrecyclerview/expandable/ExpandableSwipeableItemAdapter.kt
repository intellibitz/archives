package intellibitz.intellidroid.widget.advrecyclerview.expandable

import androidx.recyclerview.widget.RecyclerView
import intellibitz.intellidroid.widget.advrecyclerview.swipeable.action.SwipeResultAction

interface ExpandableSwipeableItemAdapter<GVH : RecyclerView.ViewHolder, CVH : RecyclerView.ViewHolder> :
    BaseExpandableSwipeableItemAdapter<GVH, CVH> {

    /**
     * Called when group item is swiped.
     * Note: do not change data set and do not call notifyDataXXX() methods inside of this method.
     *
     * @param holder The ViewHolder which is associated to the swiped item.
     * @param groupPosition Group position.
     * @param result The result code of user's swipe operation.
     * @return Reaction type of after swiping.
     */
    fun onSwipeGroupItem(holder: GVH, groupPosition: Int, result: Int): SwipeResultAction?

    /**
     * Called when child item is swiped.
     * Note: do not change data set and do not call notifyDataXXX() methods inside of this method.
     *
     * @param holder The ViewHolder which is associated to the swiped item.
     * @param groupPosition Group position.
     * @param childPosition Child position.
     * @param result The result code of user's swipe operation.
     * @return Reaction type of after swiping.
     */
    fun onSwipeChildItem(holder: CVH, groupPosition: Int, childPosition: Int, result: Int): SwipeResultAction?
}
