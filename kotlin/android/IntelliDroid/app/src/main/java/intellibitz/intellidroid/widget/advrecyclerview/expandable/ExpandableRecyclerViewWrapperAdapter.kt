package intellibitz.intellidroid.widget.advrecyclerview.expandable

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import intellibitz.intellidroid.widget.advrecyclerview.draggable.DraggableItemAdapter
import intellibitz.intellidroid.widget.advrecyclerview.draggable.DraggableItemConstants
import intellibitz.intellidroid.widget.advrecyclerview.draggable.DraggableItemViewHolder
import intellibitz.intellidroid.widget.advrecyclerview.draggable.ItemDraggableRange
import intellibitz.intellidroid.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager
import intellibitz.intellidroid.widget.advrecyclerview.swipeable.SwipeableItemAdapter
import intellibitz.intellidroid.widget.advrecyclerview.swipeable.action.SwipeResultAction
import intellibitz.intellidroid.widget.advrecyclerview.utils.BaseWrapperAdapter
import intellibitz.intellidroid.widget.advrecyclerview.utils.WrapperAdapterUtils
import kotlin.math.max
import kotlin.math.min

internal class ExpandableRecyclerViewWrapperAdapter(
    manager: RecyclerViewExpandableItemManager?,
    adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>,
    expandedItemsSavedState: IntArray?
) : BaseWrapperAdapter<RecyclerView.ViewHolder>(adapter),
    DraggableItemAdapter<RecyclerView.ViewHolder>,
    SwipeableItemAdapter<RecyclerView.ViewHolder> {

    private var mExpandableItemAdapter: ExpandableItemAdapter<*, *>?
    private var mExpandableListManager: RecyclerViewExpandableItemManager?
    private var mPositionTranslator: ExpandablePositionTranslator?
    private var mDraggingItemGroupRangeStart = RecyclerView.NO_POSITION
    private var mDraggingItemGroupRangeEnd = RecyclerView.NO_POSITION
    private var mDraggingItemChildRangeStart = RecyclerView.NO_POSITION
    private var mDraggingItemChildRangeEnd = RecyclerView.NO_POSITION
    private var mOnGroupExpandListener: RecyclerViewExpandableItemManager.OnGroupExpandListener? = null
    private var mOnGroupCollapseListener: RecyclerViewExpandableItemManager.OnGroupCollapseListener? = null

    init {
        val expAdapter = getExpandableItemAdapter(adapter)
        requireNotNull(expAdapter) { "adapter does not implement RecyclerViewExpandableListManager" }
        requireNotNull(manager) { "manager cannot be null" }

        mExpandableItemAdapter = expAdapter
        mExpandableListManager = manager

        mPositionTranslator = ExpandablePositionTranslator()
        mPositionTranslator!!.build(mExpandableItemAdapter!!, false)

        if (expandedItemsSavedState != null) {
            mPositionTranslator!!.restoreExpandedGroupItems(expandedItemsSavedState, null, null, null)
        }
    }

    companion object {
        private const val TAG = "ARVExpandableWrapper"
        private const val VIEW_TYPE_FLAG_IS_GROUP = ExpandableAdapterHelper.VIEW_TYPE_FLAG_IS_GROUP
        private const val STATE_FLAG_INITIAL_VALUE = -1

        private fun isGroupPositionRange(range: ItemDraggableRange): Boolean {
            return range.javaClass == GroupPositionItemDraggableRange::class.java ||
                    range.javaClass == ItemDraggableRange::class.java
        }

        private fun isChildPositionRange(range: ItemDraggableRange): Boolean {
            return range.javaClass == ChildPositionItemDraggableRange::class.java
        }

        private fun getExpandableItemAdapter(adapter: RecyclerView.Adapter<*>): ExpandableItemAdapter<*, *>? {
            return WrapperAdapterUtils.findWrappedAdapter(adapter, ExpandableItemAdapter::class.java)
        }

        private fun safeUpdateExpandStateFlags(holder: RecyclerView.ViewHolder, flags: Int) {
            if (holder !is ExpandableItemViewHolder) {
                return
            }

            var newFlags = flags
            val curFlags = holder.expandStateFlags
            val mask = ExpandableItemConstants.STATE_FLAG_IS_UPDATED.inv()

            if ((curFlags != STATE_FLAG_INITIAL_VALUE) && (((curFlags xor newFlags) and ExpandableItemConstants.STATE_FLAG_IS_EXPANDED) != 0)) {
                newFlags = newFlags or ExpandableItemConstants.STATE_FLAG_HAS_EXPANDED_STATE_CHANGED
            }

            if ((curFlags == STATE_FLAG_INITIAL_VALUE) || (((curFlags xor newFlags) and mask) != 0)) {
                newFlags = newFlags or ExpandableItemConstants.STATE_FLAG_IS_UPDATED
            }

            holder.expandStateFlags = newFlags
        }
    }

    override fun onRelease() {
        super.onRelease()
        mExpandableItemAdapter = null
        mExpandableListManager = null
        mOnGroupExpandListener = null
        mOnGroupCollapseListener = null
    }

    override fun getItemCount(): Int {
        return mPositionTranslator?.getItemCount() ?: 0
    }

    override fun getItemId(position: Int): Long {
        val adapter = mExpandableItemAdapter ?: return RecyclerView.NO_ID
        val translator = mPositionTranslator ?: return RecyclerView.NO_ID

        val expandablePosition = translator.getExpandablePosition(position)
        val groupPosition = ExpandableAdapterHelper.getPackedPositionGroup(expandablePosition)
        val childPosition = ExpandableAdapterHelper.getPackedPositionChild(expandablePosition)

        return if (childPosition == RecyclerView.NO_POSITION) {
            val groupId = adapter.getGroupId(groupPosition)
            ExpandableAdapterHelper.getCombinedGroupId(groupId)
        } else {
            val groupId = adapter.getGroupId(groupPosition)
            val childId = adapter.getChildId(groupPosition, childPosition)
            ExpandableAdapterHelper.getCombinedChildId(groupId, childId)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val adapter = mExpandableItemAdapter ?: return 0
        val translator = mPositionTranslator ?: return 0

        val expandablePosition = translator.getExpandablePosition(position)
        val groupPosition = ExpandableAdapterHelper.getPackedPositionGroup(expandablePosition)
        val childPosition = ExpandableAdapterHelper.getPackedPositionChild(expandablePosition)

        val type: Int = if (childPosition == RecyclerView.NO_POSITION) {
            adapter.getGroupItemViewType(groupPosition)
        } else {
            adapter.getChildItemViewType(groupPosition, childPosition)
        }

        check((type and VIEW_TYPE_FLAG_IS_GROUP) == 0) {
            "Illegal view type (type = " + Integer.toHexString(type) + ")"
        }

        return if (childPosition == RecyclerView.NO_POSITION) (type or VIEW_TYPE_FLAG_IS_GROUP) else type
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val adapter = mExpandableItemAdapter
            ?: throw IllegalStateException("adapter is null in onCreateViewHolder")

        val maskedViewType = viewType and VIEW_TYPE_FLAG_IS_GROUP.inv()

        val holder: RecyclerView.ViewHolder = if ((viewType and VIEW_TYPE_FLAG_IS_GROUP) != 0) {
            adapter.onCreateGroupViewHolder(parent, maskedViewType)
        } else {
            adapter.onCreateChildViewHolder(parent, maskedViewType)
        }

        if (holder is ExpandableItemViewHolder) {
            holder.expandStateFlags = STATE_FLAG_INITIAL_VALUE
        }

        return holder
    }

    @Suppress("UNCHECKED_CAST")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: List<Any>) {
        val adapter = mExpandableItemAdapter ?: return
        val translator = mPositionTranslator ?: return

        val expandablePosition = translator.getExpandablePosition(position)
        val groupPosition = ExpandableAdapterHelper.getPackedPositionGroup(expandablePosition)
        val childPosition = ExpandableAdapterHelper.getPackedPositionChild(expandablePosition)
        val viewType = holder.itemViewType and VIEW_TYPE_FLAG_IS_GROUP.inv()

        var flags = 0
        if (childPosition == RecyclerView.NO_POSITION) {
            flags = flags or ExpandableItemConstants.STATE_FLAG_IS_GROUP
        } else {
            flags = flags or ExpandableItemConstants.STATE_FLAG_IS_CHILD
        }

        if (translator.isGroupExpanded(groupPosition)) {
            flags = flags or ExpandableItemConstants.STATE_FLAG_IS_EXPANDED
        }

        safeUpdateExpandStateFlags(holder, flags)
        correctItemDragStateFlags(holder, groupPosition, childPosition)

        val typedAdapter = adapter as ExpandableItemAdapter<RecyclerView.ViewHolder, RecyclerView.ViewHolder>
        if (childPosition == RecyclerView.NO_POSITION) {
            typedAdapter.onBindGroupViewHolder(holder, groupPosition, viewType)
        } else {
            typedAdapter.onBindChildViewHolder(holder, groupPosition, childPosition, viewType)
        }
    }

    private fun rebuildPositionTranslator() {
        if (mPositionTranslator != null && mExpandableItemAdapter != null) {
            val savedState = mPositionTranslator!!.getSavedStateArray()
            mPositionTranslator!!.build(mExpandableItemAdapter!!, false)
            mPositionTranslator!!.restoreExpandedGroupItems(savedState, null, null, null)
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        if (holder is ExpandableItemViewHolder) {
            holder.expandStateFlags = STATE_FLAG_INITIAL_VALUE
        }
        super.onViewRecycled(holder)
    }

    override fun onHandleWrappedAdapterChanged() {
        rebuildPositionTranslator()
        super.onHandleWrappedAdapterChanged()
    }

    override fun onHandleWrappedAdapterItemRangeChanged(positionStart: Int, itemCount: Int) {
        super.onHandleWrappedAdapterItemRangeChanged(positionStart, itemCount)
    }

    override fun onHandleWrappedAdapterItemRangeInserted(positionStart: Int, itemCount: Int) {
        rebuildPositionTranslator()
        super.onHandleWrappedAdapterItemRangeInserted(positionStart, itemCount)
    }

    override fun onHandleWrappedAdapterItemRangeRemoved(positionStart: Int, itemCount: Int) {
        if (itemCount == 1 && mPositionTranslator != null) {
            val expandablePosition = mPositionTranslator!!.getExpandablePosition(positionStart)
            val groupPosition = ExpandableAdapterHelper.getPackedPositionGroup(expandablePosition)
            val childPosition = ExpandableAdapterHelper.getPackedPositionChild(expandablePosition)

            if (childPosition == RecyclerView.NO_POSITION) {
                mPositionTranslator!!.removeGroupItem(groupPosition)
            } else {
                mPositionTranslator!!.removeChildItem(groupPosition, childPosition)
            }
        } else {
            rebuildPositionTranslator()
        }

        super.onHandleWrappedAdapterItemRangeRemoved(positionStart, itemCount)
    }

    override fun onHandleWrappedAdapterRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
        rebuildPositionTranslator()
        super.onHandleWrappedAdapterRangeMoved(fromPosition, toPosition, itemCount)
    }

    @Suppress("UNCHECKED_CAST")
    override fun onCheckCanStartDrag(holder: RecyclerView.ViewHolder, position: Int, x: Int, y: Int): Boolean {
        if (mExpandableItemAdapter !is ExpandableDraggableItemAdapter<*, *>) {
            return false
        }

        val adapter = mExpandableItemAdapter as ExpandableDraggableItemAdapter<RecyclerView.ViewHolder, RecyclerView.ViewHolder>
        val translator = mPositionTranslator ?: return false

        val expandablePosition = translator.getExpandablePosition(position)
        val groupPosition = ExpandableAdapterHelper.getPackedPositionGroup(expandablePosition)
        val childPosition = ExpandableAdapterHelper.getPackedPositionChild(expandablePosition)

        val canStart = if (childPosition == RecyclerView.NO_POSITION) {
            adapter.onCheckGroupCanStartDrag(holder, groupPosition, x, y)
        } else {
            adapter.onCheckChildCanStartDrag(holder, groupPosition, childPosition, x, y)
        }

        mDraggingItemGroupRangeStart = RecyclerView.NO_POSITION
        mDraggingItemGroupRangeEnd = RecyclerView.NO_POSITION
        mDraggingItemChildRangeStart = RecyclerView.NO_POSITION
        mDraggingItemChildRangeEnd = RecyclerView.NO_POSITION

        return canStart
    }

    @Suppress("UNCHECKED_CAST")
    override fun onGetItemDraggableRange(holder: RecyclerView.ViewHolder, position: Int): ItemDraggableRange? {
        if (mExpandableItemAdapter !is ExpandableDraggableItemAdapter<*, *>) {
            return null
        }

        val baseAdapter = mExpandableItemAdapter ?: return null
        if (baseAdapter.getGroupCount() < 1) {
            return null
        }

        val adapter = baseAdapter as ExpandableDraggableItemAdapter<RecyclerView.ViewHolder, RecyclerView.ViewHolder>
        val translator = mPositionTranslator ?: return null

        val expandablePosition = translator.getExpandablePosition(position)
        val groupPosition = ExpandableAdapterHelper.getPackedPositionGroup(expandablePosition)
        val childPosition = ExpandableAdapterHelper.getPackedPositionChild(expandablePosition)

        if (childPosition == RecyclerView.NO_POSITION) {
            val range = adapter.onGetGroupItemDraggableRange(holder, groupPosition)
            return if (range == null) {
                val lastGroup = max(0, baseAdapter.getGroupCount() - 1)
                val start = 0
                val end = max(start, translator.getItemCount() - translator.getVisibleChildCount(lastGroup) - 1)
                ItemDraggableRange(start, end)
            } else if (isGroupPositionRange(range)) {
                val startPackedGroupPosition = ExpandableAdapterHelper.getPackedPositionForGroup(range.start)
                val endPackedGroupPosition = ExpandableAdapterHelper.getPackedPositionForGroup(range.end)
                val start = translator.getFlatPosition(startPackedGroupPosition)
                var end = translator.getFlatPosition(endPackedGroupPosition)

                if (range.end > groupPosition) {
                    end += translator.getVisibleChildCount(range.end)
                }

                mDraggingItemGroupRangeStart = range.start
                mDraggingItemGroupRangeEnd = range.end

                ItemDraggableRange(start, end)
            } else {
                throw IllegalStateException("Invalid range specified: $range")
            }
        } else {
            val range = adapter.onGetChildItemDraggableRange(holder, groupPosition, childPosition)
            return if (range == null) {
                val start = 1
                ItemDraggableRange(start, max(start, translator.getItemCount() - 1))
            } else if (isGroupPositionRange(range)) {
                val startPackedGroupPosition = ExpandableAdapterHelper.getPackedPositionForGroup(range.start)
                val endPackedGroupPosition = ExpandableAdapterHelper.getPackedPositionForGroup(range.end)
                val end = translator.getFlatPosition(endPackedGroupPosition) + translator.getVisibleChildCount(range.end)
                var start = translator.getFlatPosition(startPackedGroupPosition) + 1
                start = min(start, end)

                mDraggingItemGroupRangeStart = range.start
                mDraggingItemGroupRangeEnd = range.end

                ItemDraggableRange(start, end)
            } else if (isChildPositionRange(range)) {
                val maxChildrenPos = max(translator.getVisibleChildCount(groupPosition) - 1, 0)
                val childStart = min(range.start, maxChildrenPos)
                val childEnd = min(range.end, maxChildrenPos)
                val startPackedChildPosition = ExpandableAdapterHelper.getPackedPositionForChild(groupPosition, childStart)
                val endPackedChildPosition = ExpandableAdapterHelper.getPackedPositionForChild(groupPosition, childEnd)
                val start = translator.getFlatPosition(startPackedChildPosition)
                val end = translator.getFlatPosition(endPackedChildPosition)

                mDraggingItemChildRangeStart = childStart
                mDraggingItemChildRangeEnd = childEnd

                ItemDraggableRange(start, end)
            } else {
                throw IllegalStateException("Invalid range specified: $range")
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun onCheckCanDrop(draggingPosition: Int, dropPosition: Int): Boolean {
        if (mExpandableItemAdapter !is ExpandableDraggableItemAdapter<*, *>) {
            return true
        }

        val baseAdapter = mExpandableItemAdapter ?: return false
        if (baseAdapter.getGroupCount() < 1) {
            return false
        }

        val adapter = baseAdapter as ExpandableDraggableItemAdapter<RecyclerView.ViewHolder, RecyclerView.ViewHolder>
        val translator = mPositionTranslator ?: return false

        val draggingExpandablePosition = translator.getExpandablePosition(draggingPosition)
        val draggingGroupPosition = ExpandableAdapterHelper.getPackedPositionGroup(draggingExpandablePosition)
        val draggingChildPosition = ExpandableAdapterHelper.getPackedPositionChild(draggingExpandablePosition)

        val dropExpandablePosition = translator.getExpandablePosition(dropPosition)
        val dropGroupPosition = ExpandableAdapterHelper.getPackedPositionGroup(dropExpandablePosition)
        val dropChildPosition = ExpandableAdapterHelper.getPackedPositionChild(dropExpandablePosition)

        val draggingIsGroup = (draggingChildPosition == RecyclerView.NO_POSITION)
        val dropIsGroup = (dropChildPosition == RecyclerView.NO_POSITION)

        if (draggingIsGroup) {
            val canDrop = if (draggingGroupPosition == dropGroupPosition) {
                dropIsGroup
            } else if (draggingPosition < dropPosition) {
                val isDropGroupExpanded = translator.isGroupExpanded(dropGroupPosition)
                val dropGroupVisibleChildren = translator.getVisibleChildCount(dropGroupPosition)
                if (dropIsGroup) {
                    !isDropGroupExpanded
                } else {
                    dropChildPosition == (dropGroupVisibleChildren - 1)
                }
            } else {
                dropIsGroup
            }

            return if (canDrop) {
                adapter.onCheckGroupCanDrop(draggingGroupPosition, dropGroupPosition)
            } else {
                false
            }
        } else {
            val isDropGroupExpanded = translator.isGroupExpanded(dropGroupPosition)
            val canDrop: Boolean
            var modDropGroupPosition = dropGroupPosition
            var modDropChildPosition = dropChildPosition

            if (draggingPosition < dropPosition) {
                canDrop = true
                if (dropIsGroup) {
                    modDropChildPosition = if (isDropGroupExpanded) {
                        0
                    } else {
                        translator.getChildCount(modDropGroupPosition)
                    }
                }
            } else {
                if (dropIsGroup) {
                    if (modDropGroupPosition > 0) {
                        modDropGroupPosition -= 1
                        modDropChildPosition = translator.getChildCount(modDropGroupPosition)
                        canDrop = true
                    } else {
                        canDrop = false
                    }
                } else {
                    canDrop = true
                }
            }

            return if (canDrop) {
                adapter.onCheckChildCanDrop(draggingGroupPosition, draggingChildPosition, modDropGroupPosition, modDropChildPosition)
            } else {
                false
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun onMoveItem(fromPosition: Int, toPosition: Int) {
        if (mExpandableItemAdapter !is ExpandableDraggableItemAdapter<*, *>) {
            return
        }

        mDraggingItemGroupRangeStart = RecyclerView.NO_POSITION
        mDraggingItemGroupRangeEnd = RecyclerView.NO_POSITION
        mDraggingItemChildRangeStart = RecyclerView.NO_POSITION
        mDraggingItemChildRangeEnd = RecyclerView.NO_POSITION

        if (fromPosition == toPosition) {
            return
        }

        val adapter = mExpandableItemAdapter as ExpandableDraggableItemAdapter<RecyclerView.ViewHolder, RecyclerView.ViewHolder>
        val translator = mPositionTranslator ?: return

        val expandableFromPosition = translator.getExpandablePosition(fromPosition)
        val fromGroupPosition = ExpandableAdapterHelper.getPackedPositionGroup(expandableFromPosition)
        val fromChildPosition = ExpandableAdapterHelper.getPackedPositionChild(expandableFromPosition)

        val expandableToPosition = translator.getExpandablePosition(toPosition)
        val toGroupPosition = ExpandableAdapterHelper.getPackedPositionGroup(expandableToPosition)
        val toChildPosition = ExpandableAdapterHelper.getPackedPositionChild(expandableToPosition)

        val fromIsGroup = (fromChildPosition == RecyclerView.NO_POSITION)
        val toIsGroup = (toChildPosition == RecyclerView.NO_POSITION)

        var actualToFlatPosition = fromPosition

        if (fromIsGroup && toIsGroup) {
            adapter.onMoveGroupItem(fromGroupPosition, toGroupPosition)
            translator.moveGroupItem(fromGroupPosition, toGroupPosition)
            actualToFlatPosition = toPosition
        } else if (!fromIsGroup && !toIsGroup) {
            val modToChildPosition = if (fromGroupPosition == toGroupPosition) {
                toChildPosition
            } else {
                if (fromPosition < toPosition) {
                    toChildPosition + 1
                } else {
                    toChildPosition
                }
            }

            actualToFlatPosition = translator.getFlatPosition(
                ExpandableAdapterHelper.getPackedPositionForChild(fromGroupPosition, modToChildPosition)
            )

            adapter.onMoveChildItem(fromGroupPosition, fromChildPosition, toGroupPosition, modToChildPosition)
            translator.moveChildItem(fromGroupPosition, fromChildPosition, toGroupPosition, modToChildPosition)
        } else if (!fromIsGroup) {
            var modToGroupPosition: Int
            var modToChildPosition: Int

            if (toPosition < fromPosition) {
                if (toGroupPosition == 0) {
                    modToGroupPosition = toGroupPosition
                    modToChildPosition = 0
                } else {
                    modToGroupPosition = toGroupPosition - 1
                    modToChildPosition = translator.getChildCount(modToGroupPosition)
                }
            } else {
                if (translator.isGroupExpanded(toGroupPosition)) {
                    modToGroupPosition = toGroupPosition
                    modToChildPosition = 0
                } else {
                    modToGroupPosition = toGroupPosition
                    modToChildPosition = translator.getChildCount(modToGroupPosition)
                }
            }

            if (fromGroupPosition == modToGroupPosition) {
                val lastIndex = max(0, translator.getChildCount(modToGroupPosition) - 1)
                modToChildPosition = min(modToChildPosition, lastIndex)
            }

            if (!((fromGroupPosition == modToGroupPosition) && (fromChildPosition == modToChildPosition))) {
                actualToFlatPosition = if (translator.isGroupExpanded(toGroupPosition)) {
                    toPosition
                } else {
                    RecyclerView.NO_POSITION
                }

                adapter.onMoveChildItem(fromGroupPosition, fromChildPosition, modToGroupPosition, modToChildPosition)
                translator.moveChildItem(fromGroupPosition, fromChildPosition, modToGroupPosition, modToChildPosition)
            }
        } else {
            if (fromGroupPosition != toGroupPosition) {
                actualToFlatPosition = translator.getFlatPosition(ExpandableAdapterHelper.getPackedPositionForGroup(toGroupPosition))
                adapter.onMoveGroupItem(fromGroupPosition, toGroupPosition)
                translator.moveGroupItem(fromGroupPosition, toGroupPosition)
            }
        }

        if (actualToFlatPosition != fromPosition) {
            if (actualToFlatPosition != RecyclerView.NO_POSITION) {
                notifyItemMoved(fromPosition, actualToFlatPosition)
            } else {
                notifyItemRemoved(fromPosition)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun onGetSwipeReactionType(holder: RecyclerView.ViewHolder, position: Int, x: Int, y: Int): Int {
        if (mExpandableItemAdapter !is BaseExpandableSwipeableItemAdapter<*, *>) {
            return RecyclerViewSwipeManager.REACTION_CAN_NOT_SWIPE_ANY
        }

        val adapter = mExpandableItemAdapter as BaseExpandableSwipeableItemAdapter<RecyclerView.ViewHolder, RecyclerView.ViewHolder>
        val translator = mPositionTranslator ?: return RecyclerViewSwipeManager.REACTION_CAN_NOT_SWIPE_ANY

        val expandablePosition = translator.getExpandablePosition(position)
        val groupPosition = ExpandableAdapterHelper.getPackedPositionGroup(expandablePosition)
        val childPosition = ExpandableAdapterHelper.getPackedPositionChild(expandablePosition)

        return if (childPosition == RecyclerView.NO_POSITION) {
            adapter.onGetGroupItemSwipeReactionType(holder, groupPosition, x, y)
        } else {
            adapter.onGetChildItemSwipeReactionType(holder, groupPosition, childPosition, x, y)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun onSetSwipeBackground(holder: RecyclerView.ViewHolder, position: Int, type: Int) {
        if (mExpandableItemAdapter !is BaseExpandableSwipeableItemAdapter<*, *>) {
            return
        }

        val adapter = mExpandableItemAdapter as BaseExpandableSwipeableItemAdapter<RecyclerView.ViewHolder, RecyclerView.ViewHolder>
        val translator = mPositionTranslator ?: return

        val expandablePosition = translator.getExpandablePosition(position)
        val groupPosition = ExpandableAdapterHelper.getPackedPositionGroup(expandablePosition)
        val childPosition = ExpandableAdapterHelper.getPackedPositionChild(expandablePosition)

        if (childPosition == RecyclerView.NO_POSITION) {
            adapter.onSetGroupItemSwipeBackground(holder, groupPosition, type)
        } else {
            adapter.onSetChildItemSwipeBackground(holder, groupPosition, childPosition, type)
        }
    }

    override fun onSwipeItem(holder: RecyclerView.ViewHolder, position: Int, result: Int): SwipeResultAction? {
        if (mExpandableItemAdapter !is BaseExpandableSwipeableItemAdapter<*, *>) {
            return null
        }
        if (position == RecyclerView.NO_POSITION) {
            return null
        }

        val adapter = mExpandableItemAdapter as BaseExpandableSwipeableItemAdapter<*, *>
        val translator = mPositionTranslator ?: return null

        val expandablePosition = translator.getExpandablePosition(position)
        val groupPosition = ExpandableAdapterHelper.getPackedPositionGroup(expandablePosition)
        val childPosition = ExpandableAdapterHelper.getPackedPositionChild(expandablePosition)

        return ExpandableSwipeableItemInternalUtils.invokeOnSwipeItem(
            adapter, holder, groupPosition, childPosition, result
        )
    }

    @Suppress("UNCHECKED_CAST")
    internal fun onTapItem(holder: RecyclerView.ViewHolder, position: Int, x: Int, y: Int): Boolean {
        val adapter = mExpandableItemAdapter ?: return false
        val translator = mPositionTranslator ?: return false

        val expandablePosition = translator.getExpandablePosition(position)
        val groupPosition = ExpandableAdapterHelper.getPackedPositionGroup(expandablePosition)
        val childPosition = ExpandableAdapterHelper.getPackedPositionChild(expandablePosition)

        if (childPosition != RecyclerView.NO_POSITION) {
            return false
        }

        val expand = !translator.isGroupExpanded(groupPosition)
        val typedAdapter = adapter as ExpandableItemAdapter<RecyclerView.ViewHolder, RecyclerView.ViewHolder>
        val result = typedAdapter.onCheckCanExpandOrCollapseGroup(holder, groupPosition, x, y, expand)

        if (!result) {
            return false
        }

        if (expand) {
            expandGroup(groupPosition, true)
        } else {
            collapseGroup(groupPosition, true)
        }

        return true
    }

    internal fun expandAll() {
        val translator = mPositionTranslator ?: return
        val adapter = mExpandableItemAdapter ?: return
        if (!translator.isEmpty && !translator.isAllExpanded()) {
            translator.build(adapter, true)
            notifyDataSetChanged()
        }
    }

    internal fun collapseAll() {
        val translator = mPositionTranslator ?: return
        val adapter = mExpandableItemAdapter ?: return
        if (!translator.isEmpty && !translator.isAllCollapsed()) {
            translator.build(adapter, false)
            notifyDataSetChanged()
        }
    }

    internal fun collapseGroup(groupPosition: Int, fromUser: Boolean): Boolean {
        val translator = mPositionTranslator ?: return false
        val adapter = mExpandableItemAdapter ?: return false

        if (!translator.isGroupExpanded(groupPosition)) {
            return false
        }

        if (!adapter.onHookGroupCollapse(groupPosition, fromUser)) {
            return false
        }

        if (translator.collapseGroup(groupPosition)) {
            val packedPosition = ExpandableAdapterHelper.getPackedPositionForGroup(groupPosition)
            val flatPosition = translator.getFlatPosition(packedPosition)
            val childCount = translator.getChildCount(groupPosition)

            notifyItemRangeRemoved(flatPosition + 1, childCount)
        }

        val packedPosition = ExpandableAdapterHelper.getPackedPositionForGroup(groupPosition)
        val flatPosition = translator.getFlatPosition(packedPosition)
        notifyItemChanged(flatPosition)

        mOnGroupCollapseListener?.onGroupCollapse(groupPosition, fromUser)

        return true
    }

    internal fun expandGroup(groupPosition: Int, fromUser: Boolean): Boolean {
        val translator = mPositionTranslator ?: return false
        val adapter = mExpandableItemAdapter ?: return false

        if (translator.isGroupExpanded(groupPosition)) {
            return false
        }

        if (!adapter.onHookGroupExpand(groupPosition, fromUser)) {
            return false
        }

        if (translator.expandGroup(groupPosition)) {
            val packedPosition = ExpandableAdapterHelper.getPackedPositionForGroup(groupPosition)
            val flatPosition = translator.getFlatPosition(packedPosition)
            val childCount = translator.getChildCount(groupPosition)

            notifyItemRangeInserted(flatPosition + 1, childCount)
        }

        val packedPosition = ExpandableAdapterHelper.getPackedPositionForGroup(groupPosition)
        val flatPosition = translator.getFlatPosition(packedPosition)
        notifyItemChanged(flatPosition)

        mOnGroupExpandListener?.onGroupExpand(groupPosition, fromUser)

        return true
    }

    internal fun isGroupExpanded(groupPosition: Int): Boolean {
        return mPositionTranslator?.isGroupExpanded(groupPosition) ?: false
    }

    internal fun getExpandablePosition(flatPosition: Int): Long {
        return mPositionTranslator?.getExpandablePosition(flatPosition) ?: ExpandableAdapterHelper.NO_EXPANDABLE_POSITION
    }

    internal fun getFlatPosition(packedPosition: Long): Int {
        return mPositionTranslator?.getFlatPosition(packedPosition) ?: RecyclerView.NO_POSITION
    }

    internal fun getExpandedItemsSavedStateArray(): IntArray? {
        return mPositionTranslator?.getSavedStateArray()
    }

    internal fun setOnGroupExpandListener(listener: RecyclerViewExpandableItemManager.OnGroupExpandListener?) {
        mOnGroupExpandListener = listener
    }

    internal fun setOnGroupCollapseListener(listener: RecyclerViewExpandableItemManager.OnGroupCollapseListener?) {
        mOnGroupCollapseListener = listener
    }

    internal fun restoreState(adapterSavedState: IntArray?, callHook: Boolean, callListeners: Boolean) {
        mPositionTranslator?.restoreExpandedGroupItems(
            adapterSavedState,
            if (callHook) mExpandableItemAdapter else null,
            if (callListeners) mOnGroupExpandListener else null,
            if (callListeners) mOnGroupCollapseListener else null
        )
    }

    internal fun notifyGroupItemChanged(groupPosition: Int) {
        val translator = mPositionTranslator ?: return
        val packedPosition = ExpandableAdapterHelper.getPackedPositionForGroup(groupPosition)
        val flatPosition = translator.getFlatPosition(packedPosition)

        if (flatPosition != RecyclerView.NO_POSITION) {
            notifyItemChanged(flatPosition)
        }
    }

    internal fun notifyGroupAndChildrenItemsChanged(groupPosition: Int, payload: Any?) {
        val translator = mPositionTranslator ?: return
        val packedPosition = ExpandableAdapterHelper.getPackedPositionForGroup(groupPosition)
        val flatPosition = translator.getFlatPosition(packedPosition)
        val visibleChildCount = translator.getVisibleChildCount(groupPosition)

        if (flatPosition != RecyclerView.NO_POSITION) {
            notifyItemRangeChanged(flatPosition, 1 + visibleChildCount, payload)
        }
    }

    internal fun notifyChildrenOfGroupItemChanged(groupPosition: Int, payload: Any?) {
        val translator = mPositionTranslator ?: return
        val visibleChildCount = translator.getVisibleChildCount(groupPosition)

        if (visibleChildCount > 0) {
            val packedPosition = ExpandableAdapterHelper.getPackedPositionForChild(groupPosition, 0)
            val flatPosition = translator.getFlatPosition(packedPosition)

            if (flatPosition != RecyclerView.NO_POSITION) {
                notifyItemRangeChanged(flatPosition, visibleChildCount, payload)
            }
        }
    }

    internal fun notifyChildItemChanged(groupPosition: Int, childPosition: Int, payload: Any?) {
        notifyChildItemRangeChanged(groupPosition, childPosition, 1, payload)
    }

    internal fun notifyChildItemRangeChanged(groupPosition: Int, childPositionStart: Int, itemCount: Int, payload: Any?) {
        val translator = mPositionTranslator ?: return
        val visibleChildCount = translator.getVisibleChildCount(groupPosition)

        if (visibleChildCount > 0 && childPositionStart < visibleChildCount) {
            val packedPosition = ExpandableAdapterHelper.getPackedPositionForChild(groupPosition, 0)
            val flatPosition = translator.getFlatPosition(packedPosition)

            if (flatPosition != RecyclerView.NO_POSITION) {
                val startPosition = flatPosition + childPositionStart
                val count = min(itemCount, (visibleChildCount - childPositionStart))

                notifyItemRangeChanged(startPosition, count, payload)
            }
        }
    }

    internal fun notifyChildItemInserted(groupPosition: Int, childPosition: Int) {
        val translator = mPositionTranslator ?: return
        translator.insertChildItem(groupPosition, childPosition)

        val packedPosition = ExpandableAdapterHelper.getPackedPositionForChild(groupPosition, childPosition)
        val flatPosition = translator.getFlatPosition(packedPosition)

        if (flatPosition != RecyclerView.NO_POSITION) {
            notifyItemInserted(flatPosition)
        }
    }

    internal fun notifyChildItemRangeInserted(groupPosition: Int, childPositionStart: Int, itemCount: Int) {
        val translator = mPositionTranslator ?: return
        translator.insertChildItems(groupPosition, childPositionStart, itemCount)

        val packedPosition = ExpandableAdapterHelper.getPackedPositionForChild(groupPosition, childPositionStart)
        val flatPosition = translator.getFlatPosition(packedPosition)

        if (flatPosition != RecyclerView.NO_POSITION) {
            notifyItemRangeInserted(flatPosition, itemCount)
        }
    }

    internal fun notifyChildItemRemoved(groupPosition: Int, childPosition: Int) {
        val translator = mPositionTranslator ?: return
        val packedPosition = ExpandableAdapterHelper.getPackedPositionForChild(groupPosition, childPosition)
        val flatPosition = translator.getFlatPosition(packedPosition)

        translator.removeChildItem(groupPosition, childPosition)

        if (flatPosition != RecyclerView.NO_POSITION) {
            notifyItemRemoved(flatPosition)
        }
    }

    internal fun notifyChildItemRangeRemoved(groupPosition: Int, childPositionStart: Int, itemCount: Int) {
        val translator = mPositionTranslator ?: return
        val packedPosition = ExpandableAdapterHelper.getPackedPositionForChild(groupPosition, childPositionStart)
        val flatPosition = translator.getFlatPosition(packedPosition)

        translator.removeChildItems(groupPosition, childPositionStart, itemCount)

        if (flatPosition != RecyclerView.NO_POSITION) {
            notifyItemRangeRemoved(flatPosition, itemCount)
        }
    }

    internal fun notifyGroupItemInserted(groupPosition: Int, expanded: Boolean) {
        val translator = mPositionTranslator ?: return
        val insertedCount = translator.insertGroupItem(groupPosition, expanded)
        if (insertedCount > 0) {
            val packedPosition = ExpandableAdapterHelper.getPackedPositionForGroup(groupPosition)
            val flatPosition = translator.getFlatPosition(packedPosition)

            notifyItemInserted(flatPosition)
            raiseOnGroupExpandedSequentially(groupPosition, 1, false)
        }
    }

    internal fun notifyGroupItemRangeInserted(groupPositionStart: Int, count: Int, expanded: Boolean) {
        val translator = mPositionTranslator ?: return
        val insertedCount = translator.insertGroupItems(groupPositionStart, count, expanded)
        if (insertedCount > 0) {
            val packedPosition = ExpandableAdapterHelper.getPackedPositionForGroup(groupPositionStart)
            val flatPosition = translator.getFlatPosition(packedPosition)

            notifyItemRangeInserted(flatPosition, insertedCount)
            raiseOnGroupExpandedSequentially(groupPositionStart, count, false)
        }
    }

    private fun raiseOnGroupExpandedSequentially(groupPositionStart: Int, count: Int, fromUser: Boolean) {
        val listener = mOnGroupExpandListener ?: return
        for (i in 0 until count) {
            listener.onGroupExpand(groupPositionStart + i, fromUser)
        }
    }

    internal fun notifyGroupItemRemoved(groupPosition: Int) {
        val translator = mPositionTranslator ?: return
        val packedPosition = ExpandableAdapterHelper.getPackedPositionForGroup(groupPosition)
        val flatPosition = translator.getFlatPosition(packedPosition)

        val removedCount = translator.removeGroupItem(groupPosition)
        if (removedCount > 0) {
            notifyItemRangeRemoved(flatPosition, removedCount)
        }
    }

    internal fun notifyGroupItemRangeRemoved(groupPositionStart: Int, count: Int) {
        val translator = mPositionTranslator ?: return
        val packedPosition = ExpandableAdapterHelper.getPackedPositionForGroup(groupPositionStart)
        val flatPosition = translator.getFlatPosition(packedPosition)

        val removedCount = translator.removeGroupItems(groupPositionStart, count)
        if (removedCount > 0) {
            notifyItemRangeRemoved(flatPosition, removedCount)
        }
    }

    internal fun getGroupCount(): Int {
        return mExpandableItemAdapter?.getGroupCount() ?: 0
    }

    internal fun getChildCount(groupPosition: Int): Int {
        return mExpandableItemAdapter?.getChildCount(groupPosition) ?: 0
    }

    internal fun getExpandedGroupsCount(): Int {
        return mPositionTranslator?.getExpandedGroupsCount() ?: 0
    }

    internal fun getCollapsedGroupsCount(): Int {
        return mPositionTranslator?.getCollapsedGroupsCount() ?: 0
    }

    internal fun isAllGroupsExpanded(): Boolean {
        return mPositionTranslator?.isAllExpanded() ?: false
    }

    internal fun isAllGroupsCollapsed(): Boolean {
        return mPositionTranslator?.isAllCollapsed() ?: true
    }

    private fun correctItemDragStateFlags(holder: RecyclerView.ViewHolder, groupPosition: Int, childPosition: Int) {
        if (holder !is DraggableItemViewHolder) {
            return
        }

        val groupRangeSpecified = (mDraggingItemGroupRangeStart != RecyclerView.NO_POSITION) &&
                (mDraggingItemGroupRangeEnd != RecyclerView.NO_POSITION)
        val childRangeSpecified = (mDraggingItemChildRangeStart != RecyclerView.NO_POSITION) &&
                (mDraggingItemChildRangeEnd != RecyclerView.NO_POSITION)
        val isInGroupRange = (groupPosition >= mDraggingItemGroupRangeStart) &&
                (groupPosition <= mDraggingItemGroupRangeEnd)
        val isInChildRange = (groupPosition != RecyclerView.NO_POSITION) &&
                (childPosition >= mDraggingItemChildRangeStart) &&
                (childPosition <= mDraggingItemChildRangeEnd)

        val flags = holder.dragStateFlags
        var needCorrection = false

        if (((flags and DraggableItemConstants.STATE_FLAG_DRAGGING) != 0) &&
            ((flags and DraggableItemConstants.STATE_FLAG_IS_IN_RANGE) == 0)
        ) {
            if (!groupRangeSpecified || isInGroupRange) {
                if (!childRangeSpecified || isInChildRange) {
                    needCorrection = true
                }
            }
        }

        if (needCorrection) {
            holder.dragStateFlags = flags or DraggableItemConstants.STATE_FLAG_IS_IN_RANGE or DraggableItemConstants.STATE_FLAG_IS_UPDATED
        }
    }
}
