

package intellibitz.intellidroid.widget.advrecyclerview.swipeable.action;

import intellibitz.intellidroid.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;
import intellibitz.intellidroid.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;

public abstract class SwipeResultActionRemoveItem extends SwipeResultAction {
    public SwipeResultActionRemoveItem() {
        super(RecyclerViewSwipeManager.AFTER_SWIPE_REACTION_REMOVE_ITEM);
    }
}
