package intellibitz.intellidroid.widget.advrecyclerview.draggable

import android.util.Log
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import intellibitz.intellidroid.widget.advrecyclerview.swipeable.BaseSwipeableItemAdapter
import intellibitz.intellidroid.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager
import intellibitz.intellidroid.widget.advrecyclerview.swipeable.SwipeableItemAdapter
import intellibitz.intellidroid.widget.advrecyclerview.swipeable.SwipeableItemInternalUtils
import intellibitz.intellidroid.widget.advrecyclerview.swipeable.action.SwipeResultAction
import intellibitz.intellidroid.widget.advrecyclerview.swipeable.action.SwipeResultActionDefault
import intellibitz.intellidroid.widget.advrecyclerview.utils.BaseWrapperAdapter
import intellibitz.intellidroid.widget.advrecyclerview.utils.WrapperAdapterUtils

internal class DraggableItemWrapperAdapter<VH : RecyclerView.ViewHolder>(
    manager: RecyclerViewDragDropManager?,
    adapter: RecyclerView.Adapter<VH>
) : BaseWrapperAdapter<VH>(adapter), SwipeableItemAdapter<VH> {

    private var mDragDropManager: RecyclerViewDragDropManager? = manager
    private var mDraggableItemAdapter: DraggableItemAdapter<RecyclerView.ViewHolder>?
    private var mDraggingItemViewHolder: RecyclerView.ViewHolder? = null
    private var mDraggingItemInfo: DraggingItemInfo? = null
    private var mDraggableRange: ItemDraggableRange? = null
    private var mDraggingItemInitialPosition = RecyclerView.NO_POSITION
    private var mDraggingItemCurrentPosition = RecyclerView.NO_POSITION

    init {
        @Suppress("UNCHECKED_CAST")
        val draggableAdapter = getDraggableItemAdapter(adapter) as DraggableItemAdapter<RecyclerView.ViewHolder>?
        requireNotNull(draggableAdapter) { "adapter does not implement DraggableItemAdapter" }
        requireNotNull(manager) { "manager cannot be null" }

        mDraggableItemAdapter = draggableAdapter
    }

    companion object {
        private const val TAG = "ARVDraggableWrapper"
        private const val STATE_FLAG_INITIAL_VALUE = -1
        private const val LOCAL_LOGV = false
        private const val LOCAL_LOGD = false
        private const val LOCAL_LOGI = true
        private const val DEBUG_BYPASS_MOVE_OPERATION_MODE = false

        @JvmStatic
        protected fun convertToOriginalPosition(position: Int, dragInitial: Int, dragCurrent: Int): Int {
            if (dragInitial < 0 || dragCurrent < 0) {
                return position
            } else {
                return if ((dragInitial == dragCurrent) ||
                    ((position < dragInitial) && (position < dragCurrent)) ||
                    (position > dragInitial) && (position > dragCurrent)
                ) {
                    position
                } else if (dragCurrent < dragInitial) {
                    if (position == dragCurrent) {
                        dragInitial
                    } else {
                        position - 1
                    }
                } else {
                    if (position == dragCurrent) {
                        dragInitial
                    } else {
                        position + 1
                    }
                }
            }
        }

        private fun safeUpdateFlags(holder: RecyclerView.ViewHolder, flags: Int) {
            if (holder !is DraggableItemViewHolder) {
                return
            }

            var newFlags = flags
            val curFlags = holder.dragStateFlags
            val mask = DraggableItemConstants.STATE_FLAG_IS_UPDATED.inv()

            if ((curFlags == STATE_FLAG_INITIAL_VALUE) || (((curFlags xor newFlags) and mask) != 0)) {
                newFlags = newFlags or DraggableItemConstants.STATE_FLAG_IS_UPDATED
            }

            holder.dragStateFlags = newFlags
        }

        private fun getDraggableItemAdapter(adapter: RecyclerView.Adapter<*>): DraggableItemAdapter<*>? {
            return WrapperAdapterUtils.findWrappedAdapter(adapter, DraggableItemAdapter::class.java)
        }
    }

    override fun onRelease() {
        super.onRelease()
        mDraggingItemViewHolder = null
        mDraggableItemAdapter = null
        mDragDropManager = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val holder = super.onCreateViewHolder(parent, viewType)
        if (holder is DraggableItemViewHolder) {
            holder.dragStateFlags = STATE_FLAG_INITIAL_VALUE
        }
        return holder
    }

    override fun onBindViewHolder(holder: VH, position: Int, payloads: List<Any>) {
        if (isDragging) {
            val draggingItemId = mDraggingItemInfo!!.id
            val itemId = holder.itemId

            val origPosition = convertToOriginalPosition(
                position, mDraggingItemInitialPosition, mDraggingItemCurrentPosition
            )

            if (itemId == draggingItemId && holder !== mDraggingItemViewHolder) {
                if (mDraggingItemViewHolder != null) {
                    onDraggingItemRecycled()
                }

                if (LOCAL_LOGI) {
                    Log.i(TAG, "a new view holder object for the currently dragging item is assigned")
                }

                mDraggingItemViewHolder = holder
                mDragDropManager?.onNewDraggingItemViewBound(holder)
            }

            var flags = DraggableItemConstants.STATE_FLAG_DRAGGING

            if (itemId == draggingItemId) {
                flags = flags or DraggableItemConstants.STATE_FLAG_IS_ACTIVE
            }
            if (mDraggableRange?.checkInRange(position) == true) {
                flags = flags or DraggableItemConstants.STATE_FLAG_IS_IN_RANGE
            }

            safeUpdateFlags(holder, flags)
            super.onBindViewHolder(holder, origPosition, payloads)
        } else {
            safeUpdateFlags(holder, 0)
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    override fun getItemId(position: Int): Long {
        return if (isDragging) {
            val origPosition = convertToOriginalPosition(
                position, mDraggingItemInitialPosition, mDraggingItemCurrentPosition
            )
            super.getItemId(origPosition)
        } else {
            super.getItemId(position)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (isDragging) {
            val origPosition = convertToOriginalPosition(
                position, mDraggingItemInitialPosition, mDraggingItemCurrentPosition
            )
            super.getItemViewType(origPosition)
        } else {
            super.getItemViewType(position)
        }
    }

    override fun onHandleWrappedAdapterChanged() {
        if (shouldCancelDragOnDataUpdated()) {
            cancelDrag()
        } else {
            super.onHandleWrappedAdapterChanged()
        }
    }

    override fun onHandleWrappedAdapterItemRangeChanged(positionStart: Int, itemCount: Int) {
        if (shouldCancelDragOnDataUpdated()) {
            cancelDrag()
        } else {
            super.onHandleWrappedAdapterItemRangeChanged(positionStart, itemCount)
        }
    }

    override fun onHandleWrappedAdapterItemRangeInserted(positionStart: Int, itemCount: Int) {
        if (shouldCancelDragOnDataUpdated()) {
            cancelDrag()
        } else {
            super.onHandleWrappedAdapterItemRangeInserted(positionStart, itemCount)
        }
    }

    override fun onHandleWrappedAdapterItemRangeRemoved(positionStart: Int, itemCount: Int) {
        if (shouldCancelDragOnDataUpdated()) {
            cancelDrag()
        } else {
            super.onHandleWrappedAdapterItemRangeRemoved(positionStart, itemCount)
        }
    }

    override fun onHandleWrappedAdapterRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
        if (shouldCancelDragOnDataUpdated()) {
            cancelDrag()
        } else {
            super.onHandleWrappedAdapterRangeMoved(fromPosition, toPosition, itemCount)
        }
    }

    private fun shouldCancelDragOnDataUpdated(): Boolean {
        if (DEBUG_BYPASS_MOVE_OPERATION_MODE) {
            return false
        }
        return isDragging
    }

    private fun cancelDrag() {
        mDragDropManager?.cancelDrag()
    }

    internal fun onDragItemStarted(draggingItemInfo: DraggingItemInfo, holder: RecyclerView.ViewHolder, range: ItemDraggableRange?) {
        if (LOCAL_LOGD) {
            Log.d(TAG, "onDragItemStarted(holder = $holder)")
        }

        if (DEBUG_BYPASS_MOVE_OPERATION_MODE) {
            return
        }

        check(holder.itemId != RecyclerView.NO_ID) { "dragging target must provides valid ID" }

        val pos = holder.adapterPosition
        mDraggingItemInitialPosition = pos
        mDraggingItemCurrentPosition = pos
        mDraggingItemInfo = draggingItemInfo
        mDraggingItemViewHolder = holder
        mDraggableRange = range

        notifyDataSetChanged()
    }

    internal fun onDragItemFinished(result: Boolean) {
        if (LOCAL_LOGD) {
            Log.d(TAG, "onDragItemFinished(result = $result)")
        }

        if (DEBUG_BYPASS_MOVE_OPERATION_MODE) {
            return
        }

        if (result && (mDraggingItemCurrentPosition != mDraggingItemInitialPosition)) {
            val adapter = WrapperAdapterUtils.findWrappedAdapter(
                wrappedAdapter, DraggableItemAdapter::class.java
            )
            adapter?.onMoveItem(mDraggingItemInitialPosition, mDraggingItemCurrentPosition)
        }

        mDraggingItemInitialPosition = RecyclerView.NO_POSITION
        mDraggingItemCurrentPosition = RecyclerView.NO_POSITION
        mDraggableRange = null
        mDraggingItemInfo = null
        mDraggingItemViewHolder = null

        notifyDataSetChanged()
    }

    override fun onViewRecycled(holder: VH) {
        if (isDragging) {
            if (holder === mDraggingItemViewHolder) {
                onDraggingItemRecycled()
            }
        }
        super.onViewRecycled(holder)
    }

    private fun onDraggingItemRecycled() {
        if (LOCAL_LOGI) {
            Log.i(TAG, "a view holder object which is bound to currently dragging item is recycled")
        }
        mDraggingItemViewHolder = null
        mDragDropManager?.onDraggingItemViewRecycled()
    }

    internal fun canStartDrag(holder: RecyclerView.ViewHolder, position: Int, x: Int, y: Int): Boolean {
        if (LOCAL_LOGV) {
            Log.v(TAG, "canStartDrag(holder = $holder, position = $position, x = $x, y = $y)")
        }
        return mDraggableItemAdapter?.onCheckCanStartDrag(holder, position, x, y) ?: false
    }

    internal fun canDropItems(draggingPosition: Int, dropPosition: Int): Boolean {
        if (LOCAL_LOGV) {
            Log.v(TAG, "canDropItems(draggingPosition = $draggingPosition, dropPosition = $dropPosition)")
        }
        return mDraggableItemAdapter?.onCheckCanDrop(draggingPosition, dropPosition) ?: false
    }

    internal fun getItemDraggableRange(holder: RecyclerView.ViewHolder, position: Int): ItemDraggableRange? {
        if (LOCAL_LOGV) {
            Log.v(TAG, "getItemDraggableRange(holder = $holder, position = $position)")
        }
        return mDraggableItemAdapter?.onGetItemDraggableRange(holder, position)
    }

    internal fun moveItem(fromPosition: Int, toPosition: Int) {
        if (LOCAL_LOGD) {
            Log.d(TAG, "onMoveItem(fromPosition = $fromPosition, toPosition = $toPosition)")
        }

        if (DEBUG_BYPASS_MOVE_OPERATION_MODE) {
            mDraggableItemAdapter?.onMoveItem(fromPosition, toPosition)
            return
        }

        val origFromPosition = convertToOriginalPosition(
            fromPosition, mDraggingItemInitialPosition, mDraggingItemCurrentPosition
        )

        check(origFromPosition == mDraggingItemInitialPosition) {
            "onMoveItem() - may be a bug or has duplicate IDs --- " +
                    "mDraggingItemInitialPosition = $mDraggingItemInitialPosition, " +
                    "mDraggingItemCurrentPosition = $mDraggingItemCurrentPosition, " +
                    "origFromPosition = $origFromPosition, " +
                    "fromPosition = $fromPosition, " +
                    "toPosition = $toPosition"
        }

        mDraggingItemCurrentPosition = toPosition
        notifyItemMoved(fromPosition, toPosition)
    }

    protected val isDragging: Boolean
        get() = mDraggingItemInfo != null

    internal fun getDraggingItemInitialPosition(): Int {
        return mDraggingItemInitialPosition
    }

    internal fun getDraggingItemCurrentPosition(): Int {
        return mDraggingItemCurrentPosition
    }

    private fun getOriginalPosition(position: Int): Int {
        return if (isDragging) {
            convertToOriginalPosition(position, mDraggingItemInitialPosition, mDraggingItemCurrentPosition)
        } else {
            position
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun onGetSwipeReactionType(holder: VH, position: Int, x: Int, y: Int): Int {
        val adapter = wrappedAdapter
        if (adapter !is BaseSwipeableItemAdapter<*>) {
            return RecyclerViewSwipeManager.AFTER_SWIPE_REACTION_DEFAULT
        }

        val correctedPosition = getOriginalPosition(position)
        val swipeableItemAdapter = adapter as BaseSwipeableItemAdapter<VH>
        return swipeableItemAdapter.onGetSwipeReactionType(holder, correctedPosition, x, y)
    }

    @Suppress("UNCHECKED_CAST")
    override fun onSetSwipeBackground(holder: VH, position: Int, type: Int) {
        val adapter = wrappedAdapter
        if (adapter !is BaseSwipeableItemAdapter<*>) {
            return
        }

        val correctedPosition = getOriginalPosition(position)
        val swipeableItemAdapter = adapter as BaseSwipeableItemAdapter<VH>
        swipeableItemAdapter.onSetSwipeBackground(holder, correctedPosition, type)
    }

    @Suppress("UNCHECKED_CAST")
    override fun onSwipeItem(holder: VH, position: Int, result: Int): SwipeResultAction? {
        val adapter = wrappedAdapter
        if (adapter !is BaseSwipeableItemAdapter<*>) {
            return SwipeResultActionDefault()
        }

        val correctedPosition = getOriginalPosition(position)
        return SwipeableItemInternalUtils.invokeOnSwipeItem(
            adapter as BaseSwipeableItemAdapter<RecyclerView.ViewHolder>,
            holder,
            correctedPosition,
            result
        )
    }
}
