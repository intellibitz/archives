package intellibitz.intellidroid.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ScrollingFABBehavior(context: Context, attrs: AttributeSet?) : FloatingActionButton.Behavior() {
    private val toolbarHeight: Int = Utils.getToolbarHeight(context)

    override fun layoutDependsOn(parent: CoordinatorLayout, fab: FloatingActionButton, dependency: View): Boolean {
        return super.layoutDependsOn(parent, fab, dependency) || (dependency is AppBarLayout)
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout, fab: FloatingActionButton, dependency: View): Boolean {
        val returnValue = super.onDependentViewChanged(parent, fab, dependency)
        if (dependency is AppBarLayout) {
            val lp = fab.layoutParams as CoordinatorLayout.LayoutParams
            val fabBottomMargin = lp.bottomMargin
            val distanceToScroll = fab.height + fabBottomMargin
            val ratio = dependency.y / toolbarHeight.toFloat()
            fab.translationY = -distanceToScroll * ratio
        }
        return returnValue
    }
}
