package intellibitz.intellidroid.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.coordinatorlayout.widget.CoordinatorLayout;

public class ScrollingTBBehavior extends CoordinatorLayout.Behavior {
    private float scrollY = 0;

    public ScrollingTBBehavior(Context context, AttributeSet attrs) {
        super();
    }

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout,
                                       View child, View directTargetChild,
                                       View target, int nestedScrollAxes) {
        return true;
    }

    @Override
    public void onStopNestedScroll(CoordinatorLayout coordinatorLayout, View child, View target) {
        if (scrollY > 0) {
            child.setVisibility(View.GONE);
        } else {
            child.setVisibility(View.VISIBLE);
        }
        super.onStopNestedScroll(coordinatorLayout, child, target);
    }

    @Override
    public boolean onNestedFling(CoordinatorLayout coordinatorLayout, View child, View target,
                                 float velocityX, float velocityY, boolean consumed) {
        scrollY = velocityY;
        return super.onNestedFling(coordinatorLayout, child, target, velocityX, velocityY, consumed);
    }
}