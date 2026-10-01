package intellibitz.intellidroid.widget

import android.content.Context
import intellibitz.intellidroid.R

object Utils {
    @JvmStatic
    fun getToolbarHeight(context: Context): Int {
        val styledAttributes = context.theme.obtainStyledAttributes(intArrayOf(android.R.attr.actionBarSize))
        val toolbarHeight = styledAttributes.getDimension(0, 0f).toInt()
        styledAttributes.recycle()
        return toolbarHeight
    }

    @JvmStatic
    fun getTabsHeight(context: Context): Int {
        return context.resources.getDimension(R.dimen.tabsHeight).toInt()
    }
}
