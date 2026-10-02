package intellibitz.intellidroid.widget.advrecyclerview.expandable

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

interface ExpandableItemAdapter<GVH : RecyclerView.ViewHolder, CVH : RecyclerView.ViewHolder> {

    fun getGroupCount(): Int

    /**
     * Gets the number of children in a specified group.
     *
     * @param groupPosition the position of the group for which the children count should be returned
     * @return the number of children
     */
    fun getChildCount(groupPosition: Int): Int

    /**
     * Gets the ID for the group at the given position. This group ID must be unique across groups.
     *
     * @param groupPosition the position of the group for which the ID is wanted
     * @return the ID associated with the group
     */
    fun getGroupId(groupPosition: Int): Long

    /**
     * Gets the ID for the given child within the given group.
     *
     * @param groupPosition the position of the group that contains the child
     * @param childPosition the position of the child within the group for which the ID is wanted
     * @return the ID associated with the child
     */
    fun getChildId(groupPosition: Int, childPosition: Int): Long

    /**
     * Gets the view type of the specified group.
     *
     * @param groupPosition the position of the group for which the view type is wanted
     * @return integer value identifying the type of the view needed to represent the group item at position.
     */
    fun getGroupItemViewType(groupPosition: Int): Int

    /**
     * Gets the view type of the specified child.
     *
     * @param groupPosition the position of the group that contains the child
     * @param childPosition the position of the child within the group for which the view type is wanted
     * @return integer value identifying the type of the view needed to represent the group item at position.
     */
    fun getChildItemViewType(groupPosition: Int, childPosition: Int): Int

    /**
     * Called when RecyclerView needs a new [GVH] of the given type to represent a group item.
     *
     * @param parent The ViewGroup into which the new View will be added after it is bound to an adapter position
     * @param viewType The view type of the new View
     * @return A new group ViewHolder that holds a View of the given view type
     */
    fun onCreateGroupViewHolder(parent: ViewGroup, viewType: Int): GVH

    /**
     * Called when RecyclerView needs a new [CVH] of the given type to represent a child item.
     *
     * @param parent The ViewGroup into which the new View will be added after it is bound to an adapter position
     * @param viewType The view type of the new View
     * @return A new child ViewHolder that holds a View of the given view type
     */
    fun onCreateChildViewHolder(parent: ViewGroup, viewType: Int): CVH

    /**
     * Called by RecyclerView to display the group data at the specified position.
     *
     * @param holder The ViewHolder which should be updated
     * @param groupPosition The position of the group item within the adapter's data set
     * @param viewType The view type code
     */
    fun onBindGroupViewHolder(holder: GVH, groupPosition: Int, viewType: Int)

    /**
     * Called by RecyclerView to display the group data at the specified position.
     *
     * @param holder The ViewHolder which should be updated
     * @param groupPosition The position of the group item within the adapter's data set
     * @param viewType The view type code
     * @param payloads A non-null list of merged payloads.
     */
    fun onBindGroupViewHolder(holder: GVH, groupPosition: Int, viewType: Int, payloads: List<Any>)

    /**
     * Called by RecyclerView to display the child data at the specified position.
     *
     * @param holder The ViewHolder which should be updated
     * @param groupPosition The position of the group item within the adapter's data set
     * @param childPosition The position of the child item within the group
     * @param viewType The view type code
     * @param payloads A non-null list of merged payloads.
     */
    fun onBindChildViewHolder(holder: CVH, groupPosition: Int, childPosition: Int, viewType: Int, payloads: List<Any>)

    /**
     * Called by RecyclerView to display the child data at the specified position.
     *
     * @param holder The ViewHolder which should be updated
     * @param groupPosition The position of the group item within the adapter's data set
     * @param childPosition The position of the child item within the group
     * @param viewType The view type code
     */
    fun onBindChildViewHolder(holder: CVH, groupPosition: Int, childPosition: Int, viewType: Int)

    /**
     * Called when a user attempt to expand/collapse a group item by tapping.
     *
     * @param holder The ViewHolder associated to group item
     * @param groupPosition Group position
     * @param x Touched X position
     * @param y Touched Y position
     * @param expand true: expand, false: collapse
     * @return Whether to perform expand/collapse operation.
     */
    fun onCheckCanExpandOrCollapseGroup(holder: GVH, groupPosition: Int, x: Int, y: Int, expand: Boolean): Boolean

    /**
     * Called when a group attempt to expand by user operation.
     *
     * @param groupPosition The position of the group item
     * @param fromUser Whether the expand request is issued by a user operation
     * @return Whether the group can be expanded.
     */
    fun onHookGroupExpand(groupPosition: Int, fromUser: Boolean): Boolean

    /**
     * Called when a group attempt to collapse by user operation.
     *
     * @param groupPosition The position of the group item
     * @param fromUser Whether the collapse request is issued by a user operation
     * @return Whether the group can be collapsed.
     */
    fun onHookGroupCollapse(groupPosition: Int, fromUser: Boolean): Boolean
}
