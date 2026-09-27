
package intellibitz.intellidroid.widget.advrecyclerview.expandable.annotation;

import intellibitz.intellidroid.widget.advrecyclerview.expandable.ExpandableItemConstants;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;
import intellibitz.intellidroid.widget.advrecyclerview.expandable.ExpandableItemConstants;

@IntDef(flag = true, value = {
        ExpandableItemConstants.STATE_FLAG_IS_GROUP,
        ExpandableItemConstants.STATE_FLAG_IS_CHILD,
        ExpandableItemConstants.STATE_FLAG_IS_EXPANDED,
        ExpandableItemConstants.STATE_FLAG_HAS_EXPANDED_STATE_CHANGED,
        ExpandableItemConstants.STATE_FLAG_IS_UPDATED,
})
@Retention(RetentionPolicy.SOURCE)
public @interface ExpandableItemStateFlags {
}
