package intellibitz.intellidroid.widget.advrecyclerview.utils

import android.view.View
import androidx.recyclerview.widget.RecyclerView

object RecyclerViewAdapterUtils {
    /**
     * Gets parent RecyclerView instance.
     *
     * @param view Child view of the RecyclerView's item
     * @return Parent RecyclerView instance
     */
    @JvmStatic
    fun getParentRecyclerView(view: View?): RecyclerView? {
        if (view == null) {
            return null
        }
        val parent = view.parent
        return when (parent) {
            is RecyclerView -> parent
            is View -> getParentRecyclerView(parent)
            else -> null
        }
    }

    /**
     * Gets directly child of RecyclerView (== [RecyclerView.ViewHolder.itemView])
     *
     * @param view Child view of the RecyclerView's item
     * @return Item view
     */
    @JvmStatic
    fun getParentViewHolderItemView(view: View?): View? {
        val rv = getParentRecyclerView(view) ?: return null
        return rv.findContainingItemView(view!!)
    }

    /**
     * Gets [RecyclerView.ViewHolder].
     *
     * @param view Child view of the RecyclerView's item
     * @return ViewHolder
     */
    @JvmStatic
    fun getViewHolder(view: View?): RecyclerView.ViewHolder? {
        val rv = getParentRecyclerView(view) ?: return null
        return rv.findContainingViewHolder(view!!)
    }
}
