

package intellibitz.intellidroid.widget.advrecyclerview.swipeable.annotation;

import intellibitz.intellidroid.widget.advrecyclerview.swipeable.SwipeableItemConstants;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;
import intellibitz.intellidroid.widget.advrecyclerview.swipeable.SwipeableItemConstants;

@IntDef(flag = true, value = {
        SwipeableItemConstants.STATE_FLAG_SWIPING,
        SwipeableItemConstants.STATE_FLAG_IS_ACTIVE,
        SwipeableItemConstants.STATE_FLAG_IS_UPDATED,
})
@Retention(RetentionPolicy.SOURCE)
public @interface SwipeableItemStateFlags {
}
