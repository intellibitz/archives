

package intellibitz.intellidroid.widget.advrecyclerview.swipeable.annotation;

import intellibitz.intellidroid.widget.advrecyclerview.swipeable.SwipeableItemConstants;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;
import intellibitz.intellidroid.widget.advrecyclerview.swipeable.SwipeableItemConstants;

@IntDef(flag = true, value = {
        SwipeableItemConstants.AFTER_SWIPE_REACTION_DEFAULT,
        SwipeableItemConstants.AFTER_SWIPE_REACTION_MOVE_TO_SWIPED_DIRECTION,
        SwipeableItemConstants.AFTER_SWIPE_REACTION_REMOVE_ITEM,
})
@Retention(RetentionPolicy.SOURCE)
public @interface SwipeableItemAfterReactions {
}
