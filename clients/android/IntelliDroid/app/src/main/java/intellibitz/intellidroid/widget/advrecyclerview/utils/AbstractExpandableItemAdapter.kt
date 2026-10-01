package intellibitz.intellidroid.widget.advrecyclerview.utils

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import intellibitz.intellidroid.widget.advrecyclerview.expandable.ExpandableItemAdapter

abstract class AbstractExpandableItemAdapter<GVH : RecyclerView.ViewHolder, CVH : RecyclerView.ViewHolder> :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(), ExpandableItemAdapter<GVH, CVH> {

    final override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        throw IllegalStateException("This method will not be called")
    }

    final override fun getItemId(position: Int): Long {
        return RecyclerView.NO_ID
    }

    final override fun getItemViewType(position: Int): Int {
        return 0
    }

    final override fun getItemCount(): Int {
        return 0
    }

    override fun getGroupItemViewType(groupPosition: Int): Int {
        return 0
    }

    override fun getChildItemViewType(groupPosition: Int, childPosition: Int): Int {
        return 0
    }

    final override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    }

    override fun onBindGroupViewHolder(holder: GVH, groupPosition: Int, viewType: Int, payloads: List<Any?>) {
        onBindGroupViewHolder(holder, groupPosition, viewType)
    }

    override fun onBindChildViewHolder(
        holder: CVH,
        groupPosition: Int,
        childPosition: Int,
        viewType: Int,
        payloads: List<Any?>
    ) {
        onBindChildViewHolder(holder, groupPosition, childPosition, viewType)
    }

    override fun onHookGroupExpand(groupPosition: Int, fromUser: Boolean): Boolean {
        return true
    }

    override fun onHookGroupCollapse(groupPosition: Int, fromUser: Boolean): Boolean {
        return true
    }
}
