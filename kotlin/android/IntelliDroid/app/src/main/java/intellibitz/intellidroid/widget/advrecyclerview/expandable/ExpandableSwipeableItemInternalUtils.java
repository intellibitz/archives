

package intellibitz.intellidroid.widget.advrecyclerview.expandable;

import intellibitz.intellidroid.widget.advrecyclerview.swipeable.action.SwipeResultAction;

import androidx.recyclerview.widget.RecyclerView;

class ExpandableSwipeableItemInternalUtils {
    private ExpandableSwipeableItemInternalUtils() {
    }

    @SuppressWarnings("unchecked")
    public static SwipeResultAction invokeOnSwipeItem(
            BaseExpandableSwipeableItemAdapter<?, ?> adapter, RecyclerView.ViewHolder holder,
            int groupPosition, int childPosition, int result) {

        if (childPosition == RecyclerView.NO_POSITION) {
            return ((ExpandableSwipeableItemAdapter) adapter).onSwipeGroupItem(holder, groupPosition, result);
        } else {
            return ((ExpandableSwipeableItemAdapter) adapter).onSwipeChildItem(holder, groupPosition, childPosition, result);
        }
    }
}
