package intellibitz.intellidroid.widget.advrecyclerview.expandable

import androidx.recyclerview.widget.RecyclerView
import java.util.Arrays
import kotlin.math.max
import kotlin.math.min

internal class ExpandablePositionTranslator {

    private var mCachedGroupPosInfo: LongArray? = null
    private var mCachedGroupId: IntArray? = null
    private var mGroupCount = 0
    private var mExpandedGroupCount = 0
    private var mExpandedChildCount = 0
    private var mEndOfCalculatedOffsetGroupPosition = RecyclerView.NO_POSITION
    private var mAdapter: ExpandableItemAdapter<*, *>? = null

    companion object {
        private const val ALLOCATE_UNIT = 256
        private const val FLAG_EXPANDED = 0x0000000080000000L
        private const val LOWER_31BIT_MASK = 0x000000007fffffffL
        private const val LOWER_32BIT_MASK = 0x00000000ffffffffL
        private const val UPPER_32BIT_MASK = -0x100000000L // 0xffffffff00000000L

        private fun binarySearchGroupPositionByFlatPosition(
            array: LongArray,
            endArrayPosition: Int,
            flatPosition: Int
        ): Int {
            if (endArrayPosition <= 0) {
                return 0
            }

            val v1 = (array[0] ushr 32).toInt()
            val v2 = (array[endArrayPosition] ushr 32).toInt()

            if (flatPosition <= v1) {
                return 0
            } else if (flatPosition >= v2) {
                return endArrayPosition
            }

            var lastS = 0
            var s = 0
            var e = endArrayPosition

            while (s < e) {
                val mid = (s + e) ushr 1
                val v = (array[mid] ushr 32).toInt()

                if (v < flatPosition) {
                    lastS = s
                    s = mid + 1
                } else {
                    e = mid
                }
            }

            return lastS
        }
    }

    fun build(adapter: ExpandableItemAdapter<*, *>, allExpanded: Boolean) {
        val groupCount = adapter.getGroupCount()

        enlargeArraysIfNeeded(groupCount, false)

        val info = mCachedGroupPosInfo!!
        val ids = mCachedGroupId!!
        var totalChildCount = 0
        for (i in 0 until groupCount) {
            val groupId = adapter.getGroupId(i)
            val childCount = adapter.getChildCount(i)

            if (allExpanded) {
                info[i] = (((i + totalChildCount).toLong() shl 32) or childCount.toLong()) or FLAG_EXPANDED
            } else {
                info[i] = (i.toLong() shl 32) or childCount.toLong()
            }
            ids[i] = (groupId and LOWER_32BIT_MASK).toInt()

            totalChildCount += childCount
        }

        mAdapter = adapter
        mGroupCount = groupCount
        mExpandedGroupCount = if (allExpanded) groupCount else 0
        mExpandedChildCount = if (allExpanded) totalChildCount else 0
        mEndOfCalculatedOffsetGroupPosition = max(0, groupCount - 1)
    }

    fun restoreExpandedGroupItems(
        restoreGroupIds: IntArray?,
        adapter: ExpandableItemAdapter<*, *>?,
        expandListener: RecyclerViewExpandableItemManager.OnGroupExpandListener?,
        collapseListener: RecyclerViewExpandableItemManager.OnGroupCollapseListener?
    ) {
        if (restoreGroupIds == null || restoreGroupIds.isEmpty()) {
            return
        }

        val cachedIds = mCachedGroupId ?: return
        if (mCachedGroupPosInfo == null) {
            return
        }

        // make ID + position packed array
        val idAndPos = LongArray(mGroupCount)

        for (i in 0 until mGroupCount) {
            idAndPos[i] = (cachedIds[i].toLong() shl 32) or i.toLong()
        }

        // sort both arrays
        Arrays.sort(idAndPos)

        val fromUser = false

        // find matched items & apply
        var index = 0
        for (id1 in restoreGroupIds) {
            for (j in index until idAndPos.size) {
                val id2 = (idAndPos[j] shr 32).toInt()
                val position = (idAndPos[j] and LOWER_31BIT_MASK).toInt()

                if (id2 < id1) {
                    index = j

                    if (adapter == null || adapter.onHookGroupCollapse(position, fromUser)) {
                        if (collapseGroup(position)) {
                            collapseListener?.onGroupCollapse(position, fromUser)
                        }
                    }
                } else if (id2 == id1) {
                    // matched
                    index = j + 1

                    if (adapter == null || adapter.onHookGroupExpand(position, fromUser)) {
                        if (expandGroup(position)) {
                            expandListener?.onGroupExpand(position, fromUser)
                        }
                    }
                } else { // id2 > id1
                    break
                }
            }
        }

        if (adapter != null || collapseListener != null) {
            for (i in index until idAndPos.size) {
                val position = (idAndPos[i] and LOWER_31BIT_MASK).toInt()

                if (adapter == null || adapter.onHookGroupCollapse(position, fromUser)) {
                    if (collapseGroup(position)) {
                        collapseListener?.onGroupCollapse(position, fromUser)
                    }
                }
            }
        }
    }

    fun getSavedStateArray(): IntArray {
        val expandedGroups = IntArray(mExpandedGroupCount)

        val info = mCachedGroupPosInfo ?: return expandedGroups
        val ids = mCachedGroupId ?: return expandedGroups

        var index = 0
        for (i in 0 until mGroupCount) {
            val t = info[i]
            if ((t and FLAG_EXPANDED) != 0L) {
                expandedGroups[index] = ids[i]
                index += 1
            }
        }

        check(index == mExpandedGroupCount) {
            "may be a bug (index = $index, mExpandedGroupCount = $mExpandedGroupCount)"
        }

        Arrays.sort(expandedGroups)

        return expandedGroups
    }

    fun getItemCount(): Int {
        return mGroupCount + mExpandedChildCount
    }

    fun isGroupExpanded(groupPosition: Int): Boolean {
        return (mCachedGroupPosInfo!![groupPosition] and FLAG_EXPANDED) != 0L
    }

    fun getChildCount(groupPosition: Int): Int {
        return (mCachedGroupPosInfo!![groupPosition] and LOWER_31BIT_MASK).toInt()
    }

    fun getVisibleChildCount(groupPosition: Int): Int {
        return if (isGroupExpanded(groupPosition)) {
            getChildCount(groupPosition)
        } else {
            0
        }
    }

    fun collapseGroup(groupPosition: Int): Boolean {
        val info = mCachedGroupPosInfo!!
        if ((info[groupPosition] and FLAG_EXPANDED) == 0L) {
            return false
        }

        val childCount = (info[groupPosition] and LOWER_31BIT_MASK).toInt()

        info[groupPosition] = info[groupPosition] and FLAG_EXPANDED.inv()
        mExpandedGroupCount -= 1

        mExpandedChildCount -= childCount
        mEndOfCalculatedOffsetGroupPosition = min(mEndOfCalculatedOffsetGroupPosition, groupPosition)

        return true
    }

    fun expandGroup(groupPosition: Int): Boolean {
        val info = mCachedGroupPosInfo!!
        if ((info[groupPosition] and FLAG_EXPANDED) != 0L) {
            return false
        }

        val childCount = (info[groupPosition] and LOWER_31BIT_MASK).toInt()

        info[groupPosition] = info[groupPosition] or FLAG_EXPANDED
        mExpandedGroupCount += 1

        mExpandedChildCount += childCount
        mEndOfCalculatedOffsetGroupPosition = min(mEndOfCalculatedOffsetGroupPosition, groupPosition)

        return true
    }

    fun moveGroupItem(fromGroupPosition: Int, toGroupPosition: Int) {
        if (fromGroupPosition == toGroupPosition) {
            return
        }

        val info = mCachedGroupPosInfo!!
        val ids = mCachedGroupId!!

        val tmp1 = info[fromGroupPosition]
        val tmp2 = ids[fromGroupPosition]

        if (toGroupPosition < fromGroupPosition) {
            for (i in fromGroupPosition downTo toGroupPosition + 1) {
                info[i] = info[i - 1]
                ids[i] = ids[i - 1]
            }
        } else {
            for (i in fromGroupPosition until toGroupPosition) {
                info[i] = info[i + 1]
                ids[i] = ids[i + 1]
            }
        }

        info[toGroupPosition] = tmp1
        ids[toGroupPosition] = tmp2

        val minPosition = min(fromGroupPosition, toGroupPosition)

        mEndOfCalculatedOffsetGroupPosition = if (minPosition > 0) {
            min(mEndOfCalculatedOffsetGroupPosition, minPosition - 1)
        } else {
            RecyclerView.NO_POSITION
        }
    }

    fun moveChildItem(
        fromGroupPosition: Int,
        fromChildPosition: Int,
        toGroupPosition: Int,
        toChildPosition: Int
    ) {
        if (fromGroupPosition == toGroupPosition) {
            return
        }

        val info = mCachedGroupPosInfo!!
        val fromChildCount = (info[fromGroupPosition] and LOWER_31BIT_MASK).toInt()
        val toChildCount = (info[toGroupPosition] and LOWER_31BIT_MASK).toInt()

        check(fromChildCount != 0) {
            "moveChildItem(fromGroupPosition = $fromGroupPosition, fromChildPosition = $fromChildPosition, toGroupPosition = $toGroupPosition, toChildPosition = $toChildPosition) --- may be a bug."
        }

        info[fromGroupPosition] = (info[fromGroupPosition] and (UPPER_32BIT_MASK or FLAG_EXPANDED)) or (fromChildCount - 1).toLong()
        info[toGroupPosition] = (info[toGroupPosition] and (UPPER_32BIT_MASK or FLAG_EXPANDED)) or (toChildCount + 1).toLong()

        if ((info[fromGroupPosition] and FLAG_EXPANDED) != 0L) {
            mExpandedChildCount -= 1
        }
        if ((info[toGroupPosition] and FLAG_EXPANDED) != 0L) {
            mExpandedChildCount += 1
        }

        val minPosition = min(fromGroupPosition, toGroupPosition)

        mEndOfCalculatedOffsetGroupPosition = if (minPosition > 0) {
            min(mEndOfCalculatedOffsetGroupPosition, minPosition - 1)
        } else {
            RecyclerView.NO_POSITION
        }
    }

    fun getExpandablePosition(flatPosition: Int): Long {
        if (flatPosition == RecyclerView.NO_POSITION) {
            return ExpandableAdapterHelper.NO_EXPANDABLE_POSITION
        }

        val groupCount = mGroupCount
        val info = mCachedGroupPosInfo!!

        val startIndex = binarySearchGroupPositionByFlatPosition(info, mEndOfCalculatedOffsetGroupPosition, flatPosition)
        var expandablePosition = ExpandableAdapterHelper.NO_EXPANDABLE_POSITION
        var endOfCalculatedOffsetGroupPosition = mEndOfCalculatedOffsetGroupPosition
        var offset = if (startIndex == 0) 0 else (info[startIndex] ushr 32).toInt()

        for (i in startIndex until groupCount) {
            val t = info[i]

            // update offset info
            info[i] = (offset.toLong() shl 32) or (t and LOWER_32BIT_MASK)
            endOfCalculatedOffsetGroupPosition = i

            if (offset >= flatPosition) {
                // found (group item)
                expandablePosition = ExpandableAdapterHelper.getPackedPositionForGroup(i)
                break
            } else {
                offset += 1
            }

            if ((t and FLAG_EXPANDED) != 0L) {
                val childCount = (t and LOWER_31BIT_MASK).toInt()

                if ((childCount > 0) && (offset + childCount - 1) >= flatPosition) {
                    // found (child item)
                    expandablePosition = ExpandableAdapterHelper.getPackedPositionForChild(i, flatPosition - offset)
                    break
                } else {
                    offset += childCount
                }
            }
        }

        mEndOfCalculatedOffsetGroupPosition = max(mEndOfCalculatedOffsetGroupPosition, endOfCalculatedOffsetGroupPosition)

        return expandablePosition
    }

    fun getFlatPosition(packedPosition: Long): Int {
        if (packedPosition == ExpandableAdapterHelper.NO_EXPANDABLE_POSITION) {
            return RecyclerView.NO_POSITION
        }

        val groupPosition = ExpandableAdapterHelper.getPackedPositionGroup(packedPosition)
        val childPosition = ExpandableAdapterHelper.getPackedPositionChild(packedPosition)
        val groupCount = mGroupCount

        if (groupPosition !in 0 until groupCount) {
            return RecyclerView.NO_POSITION
        }

        if (childPosition != RecyclerView.NO_POSITION) {
            if (!isGroupExpanded(groupPosition)) {
                return RecyclerView.NO_POSITION
            }
        }

        val info = mCachedGroupPosInfo!!
        val startIndex = max(0, min(groupPosition, mEndOfCalculatedOffsetGroupPosition))
        var endOfCalculatedOffsetGroupPosition = mEndOfCalculatedOffsetGroupPosition
        var offset = (info[startIndex] ushr 32).toInt()
        var flatPosition = RecyclerView.NO_POSITION

        for (i in startIndex until groupCount) {
            val t = info[i]

            // update offset info
            info[i] = (offset.toLong() shl 32) or (t and LOWER_32BIT_MASK)
            endOfCalculatedOffsetGroupPosition = i

            val childCount = (t and LOWER_31BIT_MASK).toInt()

            if (i == groupPosition) {
                flatPosition = if (childPosition == RecyclerView.NO_POSITION) {
                    offset
                } else if (childPosition < childCount) {
                    (offset + 1) + childPosition
                } else {
                    RecyclerView.NO_POSITION
                }
                break
            } else {
                offset += 1

                if ((t and FLAG_EXPANDED) != 0L) {
                    offset += childCount
                }
            }
        }

        mEndOfCalculatedOffsetGroupPosition = max(mEndOfCalculatedOffsetGroupPosition, endOfCalculatedOffsetGroupPosition)

        return flatPosition
    }

    fun removeChildItem(groupPosition: Int, childPosition: Int) {
        removeChildItems(groupPosition, childPosition, 1)
    }

    fun removeChildItems(groupPosition: Int, childPositionStart: Int, count: Int) {
        val info = mCachedGroupPosInfo!!
        val t = info[groupPosition]
        val curCount = (t and LOWER_31BIT_MASK).toInt()

        check(childPositionStart >= 0 && (childPositionStart + count) <= curCount) {
            "Invalid child position removeChildItems(groupPosition = $groupPosition, childPosition = $childPositionStart, count = $count)"
        }

        if ((t and FLAG_EXPANDED) != 0L) {
            mExpandedChildCount -= count
        }

        info[groupPosition] = (t and (UPPER_32BIT_MASK or FLAG_EXPANDED)) or (curCount - count).toLong()
        mEndOfCalculatedOffsetGroupPosition = min(mEndOfCalculatedOffsetGroupPosition, groupPosition - 1)
    }

    fun insertChildItem(groupPosition: Int, childPosition: Int) {
        insertChildItems(groupPosition, childPosition, 1)
    }

    fun insertChildItems(groupPosition: Int, childPositionStart: Int, count: Int) {
        val info = mCachedGroupPosInfo!!
        val t = info[groupPosition]
        val curCount = (t and LOWER_31BIT_MASK).toInt()

        check(childPositionStart >= 0 && childPositionStart <= curCount) {
            "Invalid child position insertChildItems(groupPosition = $groupPosition, childPositionStart = $childPositionStart, count = $count)"
        }

        if ((t and FLAG_EXPANDED) != 0L) {
            mExpandedChildCount += count
        }

        info[groupPosition] = (t and (UPPER_32BIT_MASK or FLAG_EXPANDED)) or (curCount + count).toLong()
        mEndOfCalculatedOffsetGroupPosition = min(mEndOfCalculatedOffsetGroupPosition, groupPosition)
    }

    fun insertGroupItems(groupPosition: Int, count: Int, expanded: Boolean): Int {
        if (count <= 0) {
            return 0
        }

        val n = count

        enlargeArraysIfNeeded(mGroupCount + n, true)

        val adapter = mAdapter!!
        val info = mCachedGroupPosInfo!!
        val ids = mCachedGroupId!!

        val start = mGroupCount - 1 + n
        val end = groupPosition - 1 + n
        for (i in start downTo end + 1) {
            info[i] = info[i - n]
            ids[i] = ids[i - n]
        }

        val expandedFlag = if (expanded) FLAG_EXPANDED else 0L
        var insertedChildCount = 0
        val end2 = groupPosition + n
        for (i in groupPosition until end2) {
            val groupId = adapter.getGroupId(i)
            val childCount = adapter.getChildCount(i)

            info[i] = ((i.toLong() shl 32) or childCount.toLong()) or expandedFlag
            ids[i] = (groupId and LOWER_32BIT_MASK).toInt()

            insertedChildCount += childCount
        }

        mGroupCount += n
        if (expanded) {
            mExpandedGroupCount += n
            mExpandedChildCount += insertedChildCount
        }

        val calculatedOffset = if (mGroupCount == 0) RecyclerView.NO_POSITION else (groupPosition - 1)
        mEndOfCalculatedOffsetGroupPosition = min(mEndOfCalculatedOffsetGroupPosition, calculatedOffset)

        return if (expanded) (n + insertedChildCount) else n
    }

    fun insertGroupItem(groupPosition: Int, expanded: Boolean): Int {
        return insertGroupItems(groupPosition, 1, expanded)
    }

    fun removeGroupItems(groupPosition: Int, count: Int): Int {
        if (count <= 0) {
            return 0
        }

        val n = count
        var removedVisibleItemCount = 0
        val info = mCachedGroupPosInfo!!
        val ids = mCachedGroupId!!

        for (i in 0 until n) {
            val t = info[groupPosition + i]

            if ((t and FLAG_EXPANDED) != 0L) {
                val visibleChildCount = (t and LOWER_31BIT_MASK).toInt()
                removedVisibleItemCount += visibleChildCount
                mExpandedChildCount -= visibleChildCount
                mExpandedGroupCount -= 1
            }
        }
        removedVisibleItemCount += n
        mGroupCount -= n

        // shift forward
        for (i in groupPosition until mGroupCount) {
            info[i] = info[i + n]
            ids[i] = ids[i + n]
        }

        val calculatedOffset = if (mGroupCount == 0) RecyclerView.NO_POSITION else (groupPosition - 1)
        mEndOfCalculatedOffsetGroupPosition = min(mEndOfCalculatedOffsetGroupPosition, calculatedOffset)

        return removedVisibleItemCount
    }

    fun removeGroupItem(groupPosition: Int): Int {
        return removeGroupItems(groupPosition, 1)
    }

    private fun enlargeArraysIfNeeded(size: Int, preserveData: Boolean) {
        val allocSize = (size + (2 * ALLOCATE_UNIT - 1)) and (ALLOCATE_UNIT - 1).inv()

        val curInfo = mCachedGroupPosInfo
        val curId = mCachedGroupId
        var newInfo = curInfo
        var newId = curId

        if (curInfo == null || curInfo.size < size) {
            newInfo = LongArray(allocSize)
        }
        if (curId == null || curId.size < size) {
            newId = IntArray(allocSize)
        }

        if (preserveData) {
            if (curInfo != null && curInfo !== newInfo) {
                System.arraycopy(curInfo, 0, newInfo, 0, curInfo.size)
            }
            if (curId != null && curId !== newId) {
                System.arraycopy(curId, 0, newId, 0, curId.size)
            }
        }

        mCachedGroupPosInfo = newInfo
        mCachedGroupId = newId
    }

    fun getExpandedGroupsCount(): Int {
        return mExpandedGroupCount
    }

    fun getCollapsedGroupsCount(): Int {
        return mGroupCount - mExpandedGroupCount
    }

    fun isAllExpanded(): Boolean {
        return !isEmpty && (mExpandedGroupCount == mGroupCount)
    }

    fun isAllCollapsed(): Boolean {
        return isEmpty || (mExpandedGroupCount == 0)
    }

    val isEmpty: Boolean
        get() = mGroupCount == 0

    fun isEmpty(): Boolean {
        return mGroupCount == 0
    }
}
