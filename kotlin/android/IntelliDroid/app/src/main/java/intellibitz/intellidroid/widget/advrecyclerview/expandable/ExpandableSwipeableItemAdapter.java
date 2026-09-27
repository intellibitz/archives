

package intellibitz.intellidroid.widget.advrecyclerview.expandable;

import intellibitz.intellidroid.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;
import intellibitz.intellidroid.widget.advrecyclerview.swipeable.action.SwipeResultAction;

import androidx.recyclerview.widget.RecyclerView;

public interface ExpandableSwipeableItemAdapter<GVH extends RecyclerView.ViewHolder, CVH extends RecyclerView.ViewHolder>
        extends BaseExpandableSwipeableItemAdapter<GVH, CVH> {

    /**
     * Called when group item is swiped.
     * <p/>
     * *Note that do not change data set and do not call notifyDataXXX() methods inside of this method.*
     *
     * @param holder        The ViewHolder which is associated to the swiped item.
     * @param groupPosition Group position.
     * @param result        The result code of user's swipe operation.
     *                      {@link RecyclerViewSwipeManager#RESULT_CANCELED},
     *                      {@link RecyclerViewSwipeManager#RESULT_SWIPED_LEFT},
     *                      {@link RecyclerViewSwipeManager#RESULT_SWIPED_UP},
     *                      {@link RecyclerViewSwipeManager#RESULT_SWIPED_RIGHT} or
     *                      {@link RecyclerViewSwipeManager#RESULT_SWIPED_DOWN}
     * @return Reaction type of after swiping.
     * One of the {@link RecyclerViewSwipeManager#AFTER_SWIPE_REACTION_DEFAULT},
     * {@link RecyclerViewSwipeManager#AFTER_SWIPE_REACTION_MOVE_TO_SWIPED_DIRECTION} or
     * {@link RecyclerViewSwipeManager#AFTER_SWIPE_REACTION_REMOVE_ITEM}.
     */
    SwipeResultAction onSwipeGroupItem(GVH holder, int groupPosition, int result);

    /**
     * Called when child item is swiped.
     * <p/>
     * *Note that do not change data set and do not call notifyDataXXX() methods inside of this method.*
     *
     * @param holder        The ViewHolder which is associated to the swiped item.
     * @param groupPosition Group position.
     * @param childPosition Child position.
     * @param result        The result code of user's swipe operation.
     *                      {@link RecyclerViewSwipeManager#RESULT_CANCELED},
     *                      {@link RecyclerViewSwipeManager#RESULT_SWIPED_LEFT},
     *                      {@link RecyclerViewSwipeManager#RESULT_SWIPED_UP},
     *                      {@link RecyclerViewSwipeManager#RESULT_SWIPED_RIGHT} or
     *                      {@link RecyclerViewSwipeManager#RESULT_SWIPED_DOWN}
     * @return Reaction type of after swiping.
     * One of the {@link RecyclerViewSwipeManager#AFTER_SWIPE_REACTION_DEFAULT},
     * {@link RecyclerViewSwipeManager#AFTER_SWIPE_REACTION_MOVE_TO_SWIPED_DIRECTION} or
     * {@link RecyclerViewSwipeManager#AFTER_SWIPE_REACTION_REMOVE_ITEM}.
     */
    SwipeResultAction onSwipeChildItem(CVH holder, int groupPosition, int childPosition, int result);
}
