

package intellibitz.intellidroid.widget.advrecyclerview.swipeable;

import intellibitz.intellidroid.widget.advrecyclerview.swipeable.action.SwipeResultAction;
import intellibitz.intellidroid.widget.advrecyclerview.swipeable.action.SwipeResultAction;

import androidx.recyclerview.widget.RecyclerView;

public class SwipeableItemInternalUtils {
    private SwipeableItemInternalUtils() {
    }

    @SuppressWarnings("unchecked")
    public static SwipeResultAction invokeOnSwipeItem(
            BaseSwipeableItemAdapter<?> adapter, RecyclerView.ViewHolder holder, int position, int result) {
        return ((SwipeableItemAdapter) adapter).onSwipeItem(holder, position, result);
    }
}
