package intellibitz.intellidroid.widget.advrecyclerview.utils

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import intellibitz.intellidroid.widget.advrecyclerview.expandable.ExpandableItemViewHolder
import intellibitz.intellidroid.widget.advrecyclerview.expandable.annotation.ExpandableItemStateFlags

abstract class AbstractExpandableItemViewHolder(itemView: View) :
    RecyclerView.ViewHolder(itemView), ExpandableItemViewHolder {

    @ExpandableItemStateFlags
    private var mExpandStateFlags = 0

    @ExpandableItemStateFlags
    override fun getExpandStateFlags(): Int {
        return mExpandStateFlags
    }

    override fun setExpandStateFlags(@ExpandableItemStateFlags flags: Int) {
        mExpandStateFlags = flags
    }
}
