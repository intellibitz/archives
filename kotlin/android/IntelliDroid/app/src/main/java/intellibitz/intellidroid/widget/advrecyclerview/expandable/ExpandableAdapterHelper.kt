package intellibitz.intellidroid.widget.advrecyclerview.expandable

import androidx.recyclerview.widget.RecyclerView

internal object ExpandableAdapterHelper {
    const val NO_EXPANDABLE_POSITION = -1L // 0xffffffffffffffffL
    const val VIEW_TYPE_FLAG_IS_GROUP = 0x80000000.toInt()
    private const val LOWER_32BIT_MASK = 0x00000000ffffffffL
    private const val LOWER_31BIT_MASK = 0x000000007fffffffL

    @JvmStatic
    fun getPackedPositionForChild(groupPosition: Int, childPosition: Int): Long {
        return (childPosition.toLong() shl 32) or (groupPosition.toLong() and LOWER_32BIT_MASK)
    }

    @JvmStatic
    fun getPackedPositionForGroup(groupPosition: Int): Long {
        return (RecyclerView.NO_POSITION.toLong() shl 32) or (groupPosition.toLong() and LOWER_32BIT_MASK)
    }

    @JvmStatic
    fun getPackedPositionChild(packedPosition: Long): Int {
        return (packedPosition ushr 32).toInt()
    }

    @JvmStatic
    fun getPackedPositionGroup(packedPosition: Long): Int {
        return (packedPosition and LOWER_32BIT_MASK).toInt()
    }

    @JvmStatic
    fun getCombinedChildId(groupId: Long, childId: Long): Long {
        return ((groupId and LOWER_31BIT_MASK) shl 32) or (childId and LOWER_32BIT_MASK)
    }

    @JvmStatic
    fun getCombinedGroupId(groupId: Long): Long {
        return ((groupId and LOWER_31BIT_MASK) shl 32) or (RecyclerView.NO_ID and LOWER_32BIT_MASK)
    }

    @JvmStatic
    fun isGroupViewType(rawViewType: Int): Boolean {
        return (rawViewType and VIEW_TYPE_FLAG_IS_GROUP) != 0
    }

    @JvmStatic
    fun getGroupViewType(rawViewType: Int): Int {
        return rawViewType and VIEW_TYPE_FLAG_IS_GROUP.inv()
    }

    @JvmStatic
    fun getChildViewType(rawViewType: Int): Int {
        return rawViewType and VIEW_TYPE_FLAG_IS_GROUP.inv()
    }
}
