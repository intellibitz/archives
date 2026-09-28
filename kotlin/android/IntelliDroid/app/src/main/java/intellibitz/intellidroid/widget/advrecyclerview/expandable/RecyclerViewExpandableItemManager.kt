package intellibitz.intellidroid.widget.advrecyclerview.expandable

import android.os.Parcel
import android.os.Parcelable
import android.view.MotionEvent
import android.view.ViewConfiguration
import androidx.core.view.MotionEventCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import intellibitz.intellidroid.widget.advrecyclerview.utils.CustomRecyclerViewUtils
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

open class RecyclerViewExpandableItemManager(savedState: Parcelable?) : ExpandableItemConstants {

    private var mSavedState: SavedState? = null
    private var mRecyclerView: RecyclerView? = null
    private var mAdapter: ExpandableRecyclerViewWrapperAdapter? = null
    private var mInternalUseOnItemTouchListener: RecyclerView.OnItemTouchListener? = null
    private var mOnGroupExpandListener: OnGroupExpandListener? = null
    private var mOnGroupCollapseListener: OnGroupCollapseListener? = null
    private var mTouchedItemId = RecyclerView.NO_ID
    private var mTouchSlop = 0
    private var mInitialTouchX = 0
    private var mInitialTouchY = 0

    init {
        mInternalUseOnItemTouchListener = object : RecyclerView.OnItemTouchListener {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                return this@RecyclerViewExpandableItemManager.onInterceptTouchEvent(rv, e)
            }

            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}

            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
        }

        if (savedState is SavedState) {
            mSavedState = savedState
        }
    }

    companion object {
        const val NO_EXPANDABLE_POSITION: Long = ExpandableAdapterHelper.NO_EXPANDABLE_POSITION
        private const val TAG = "ARVExpandableItemMgr"

        @JvmStatic
        fun getPackedPositionChild(packedPosition: Long): Int {
            return ExpandableAdapterHelper.getPackedPositionChild(packedPosition)
        }

        @JvmStatic
        fun getPackedPositionForChild(groupPosition: Int, childPosition: Int): Long {
            return ExpandableAdapterHelper.getPackedPositionForChild(groupPosition, childPosition)
        }

        @JvmStatic
        fun getPackedPositionForGroup(groupPosition: Int): Long {
            return ExpandableAdapterHelper.getPackedPositionForGroup(groupPosition)
        }

        @JvmStatic
        fun getPackedPositionGroup(packedPosition: Long): Int {
            return ExpandableAdapterHelper.getPackedPositionGroup(packedPosition)
        }

        @JvmStatic
        fun getCombinedChildId(groupId: Long, childId: Long): Long {
            return ExpandableAdapterHelper.getCombinedChildId(groupId, childId)
        }

        @JvmStatic
        fun getCombinedGroupId(groupId: Long): Long {
            return ExpandableAdapterHelper.getCombinedGroupId(groupId)
        }

        @JvmStatic
        fun isGroupViewType(rawViewType: Int): Boolean {
            return ExpandableAdapterHelper.isGroupViewType(rawViewType)
        }

        @JvmStatic
        fun getGroupViewType(rawViewType: Int): Int {
            return ExpandableAdapterHelper.getGroupViewType(rawViewType)
        }

        @JvmStatic
        fun getChildViewType(rawViewType: Int): Int {
            return ExpandableAdapterHelper.getChildViewType(rawViewType)
        }
    }

    val isReleased: Boolean
        get() = mInternalUseOnItemTouchListener == null

    fun isReleased(): Boolean {
        return mInternalUseOnItemTouchListener == null
    }

    fun attachRecyclerView(rv: RecyclerView) {
        check(!isReleased) { "Accessing released object" }
        check(mRecyclerView == null) { "RecyclerView instance has already been set" }

        mRecyclerView = rv
        mRecyclerView!!.addOnItemTouchListener(mInternalUseOnItemTouchListener!!)
        mTouchSlop = ViewConfiguration.get(mRecyclerView!!.context).scaledTouchSlop
    }

    fun release() {
        if (mRecyclerView != null && mInternalUseOnItemTouchListener != null) {
            mRecyclerView!!.removeOnItemTouchListener(mInternalUseOnItemTouchListener!!)
        }
        mInternalUseOnItemTouchListener = null
        mOnGroupExpandListener = null
        mOnGroupCollapseListener = null
        mRecyclerView = null
        mSavedState = null
    }

    @Suppress("UNCHECKED_CAST")
    fun createWrappedAdapter(adapter: RecyclerView.Adapter<*>): RecyclerView.Adapter<*> {
        require(adapter.hasStableIds()) { "The passed adapter does not support stable IDs" }
        check(mAdapter == null) { "already have a wrapped adapter" }

        val adapterSavedState = mSavedState?.adapterSavedState
        mSavedState = null

        val wrapper = ExpandableRecyclerViewWrapperAdapter(
            this,
            adapter as RecyclerView.Adapter<RecyclerView.ViewHolder>,
            adapterSavedState
        )
        mAdapter = wrapper

        wrapper.setOnGroupExpandListener(mOnGroupExpandListener)
        mOnGroupExpandListener = null

        wrapper.setOnGroupCollapseListener(mOnGroupCollapseListener)
        mOnGroupCollapseListener = null

        return mAdapter!!
    }

    fun getSavedState(): Parcelable {
        val adapterSavedState = mAdapter?.getExpandedItemsSavedStateArray()
        return SavedState(adapterSavedState)
    }

    internal fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
        if (mAdapter == null) {
            return false
        }

        val action = MotionEventCompat.getActionMasked(e)

        when (action) {
            MotionEvent.ACTION_DOWN -> handleActionDown(rv, e)
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (handleActionUpOrCancel(rv, e)) {
                    return false
                }
            }
        }

        return false
    }

    private fun handleActionDown(rv: RecyclerView, e: MotionEvent) {
        val holder = CustomRecyclerViewUtils.findChildViewHolderUnderWithTranslation(rv, e.x, e.y)

        mInitialTouchX = (e.x + 0.5f).toInt()
        mInitialTouchY = (e.y + 0.5f).toInt()

        mTouchedItemId = if (holder is ExpandableItemViewHolder) {
            holder.itemId
        } else {
            RecyclerView.NO_ID
        }
    }

    private fun handleActionUpOrCancel(rv: RecyclerView, e: MotionEvent): Boolean {
        val touchedItemId = mTouchedItemId
        val initialTouchX = mInitialTouchX
        val initialTouchY = mInitialTouchY

        mTouchedItemId = RecyclerView.NO_ID
        mInitialTouchX = 0
        mInitialTouchY = 0

        if (!(touchedItemId != RecyclerView.NO_ID && MotionEventCompat.getActionMasked(e) == MotionEvent.ACTION_UP)) {
            return false
        }

        val touchX = (e.x + 0.5f).toInt()
        val touchY = (e.y + 0.5f).toInt()

        val diffX = touchX - initialTouchX
        val diffY = touchY - initialTouchY

        if (!(abs(diffX) < mTouchSlop && abs(diffY) < mTouchSlop)) {
            return false
        }

        val holder = CustomRecyclerViewUtils.findChildViewHolderUnderWithTranslation(rv, e.x, e.y)

        if (holder == null || holder.itemId != touchedItemId) {
            return false
        }

        val position = CustomRecyclerViewUtils.getSynchronizedPosition(holder)

        if (position == RecyclerView.NO_POSITION) {
            return false
        }

        val view = holder.itemView
        val translateX = (ViewCompat.getTranslationX(view) + 0.5f).toInt()
        val translateY = (ViewCompat.getTranslationY(view) + 0.5f).toInt()
        val viewX = touchX - (view.left + translateX)
        val viewY = touchY - (view.top + translateY)

        return mAdapter?.onTapItem(holder, position, viewX, viewY) ?: false
    }

    fun expandAll() {
        mAdapter?.expandAll()
    }

    fun collapseAll() {
        mAdapter?.collapseAll()
    }

    fun expandGroup(groupPosition: Int): Boolean {
        return mAdapter?.expandGroup(groupPosition, false) ?: false
    }

    fun collapseGroup(groupPosition: Int): Boolean {
        return mAdapter?.collapseGroup(groupPosition, false) ?: false
    }

    fun getExpandablePosition(flatPosition: Int): Long {
        return mAdapter?.getExpandablePosition(flatPosition) ?: ExpandableAdapterHelper.NO_EXPANDABLE_POSITION
    }

    fun getFlatPosition(packedPosition: Long): Int {
        return mAdapter?.getFlatPosition(packedPosition) ?: RecyclerView.NO_POSITION
    }

    fun isGroupExpanded(groupPosition: Int): Boolean {
        return mAdapter?.isGroupExpanded(groupPosition) ?: false
    }

    fun setOnGroupExpandListener(listener: OnGroupExpandListener?) {
        if (mAdapter != null) {
            mAdapter!!.setOnGroupExpandListener(listener)
        } else {
            mOnGroupExpandListener = listener
        }
    }

    fun setOnGroupCollapseListener(listener: OnGroupCollapseListener?) {
        if (mAdapter != null) {
            mAdapter!!.setOnGroupCollapseListener(listener)
        } else {
            mOnGroupCollapseListener = listener
        }
    }

    fun restoreState(savedState: Parcelable?) {
        restoreState(savedState, false, false)
    }

    fun restoreState(savedState: Parcelable?, callHooks: Boolean, callListeners: Boolean) {
        if (savedState == null) {
            return
        }

        require(savedState is SavedState) { "Illegal saved state object passed" }
        check(mAdapter != null && mRecyclerView != null) { "RecyclerView has not been attached" }

        mAdapter!!.restoreState(savedState.adapterSavedState, callHooks, callListeners)
    }

    fun notifyGroupItemChanged(groupPosition: Int) {
        mAdapter?.notifyGroupItemChanged(groupPosition)
    }

    fun notifyGroupAndChildrenItemsChanged(groupPosition: Int) {
        mAdapter?.notifyGroupAndChildrenItemsChanged(groupPosition, null)
    }

    fun notifyGroupAndChildrenItemsChanged(groupPosition: Int, payload: Any?) {
        mAdapter?.notifyGroupAndChildrenItemsChanged(groupPosition, payload)
    }

    fun notifyChildrenOfGroupItemChanged(groupPosition: Int) {
        mAdapter?.notifyChildrenOfGroupItemChanged(groupPosition, null)
    }

    fun notifyChildrenOfGroupItemChanged(groupPosition: Int, payload: Any?) {
        mAdapter?.notifyChildrenOfGroupItemChanged(groupPosition, payload)
    }

    fun notifyChildItemChanged(groupPosition: Int, childPosition: Int) {
        mAdapter?.notifyChildItemChanged(groupPosition, childPosition, null)
    }

    fun notifyChildItemChanged(groupPosition: Int, childPosition: Int, payload: Any?) {
        mAdapter?.notifyChildItemChanged(groupPosition, childPosition, payload)
    }

    fun notifyChildItemRangeChanged(groupPosition: Int, childPositionStart: Int, itemCount: Int) {
        mAdapter?.notifyChildItemRangeChanged(groupPosition, childPositionStart, itemCount, null)
    }

    fun notifyChildItemRangeChanged(groupPosition: Int, childPositionStart: Int, itemCount: Int, payload: Any?) {
        mAdapter?.notifyChildItemRangeChanged(groupPosition, childPositionStart, itemCount, payload)
    }

    fun notifyGroupItemInserted(groupPosition: Int) {
        notifyGroupItemInserted(groupPosition, false)
    }

    fun notifyGroupItemInserted(groupPosition: Int, expanded: Boolean) {
        mAdapter?.notifyGroupItemInserted(groupPosition, expanded)
    }

    fun notifyGroupItemRangeInserted(groupPositionStart: Int, itemCount: Int) {
        notifyGroupItemRangeInserted(groupPositionStart, itemCount, false)
    }

    fun notifyGroupItemRangeInserted(groupPositionStart: Int, itemCount: Int, expanded: Boolean) {
        mAdapter?.notifyGroupItemRangeInserted(groupPositionStart, itemCount, expanded)
    }

    fun notifyChildItemInserted(groupPosition: Int, childPosition: Int) {
        mAdapter?.notifyChildItemInserted(groupPosition, childPosition)
    }

    fun notifyChildItemRangeInserted(groupPosition: Int, childPositionStart: Int, itemCount: Int) {
        mAdapter?.notifyChildItemRangeInserted(groupPosition, childPositionStart, itemCount)
    }

    fun notifyGroupItemRemoved(groupPosition: Int) {
        mAdapter?.notifyGroupItemRemoved(groupPosition)
    }

    fun notifyGroupItemRangeRemoved(groupPositionStart: Int, count: Int) {
        mAdapter?.notifyGroupItemRangeRemoved(groupPositionStart, count)
    }

    fun notifyChildItemRemoved(groupPosition: Int, childPosition: Int) {
        mAdapter?.notifyChildItemRemoved(groupPosition, childPosition)
    }

    fun notifyChildItemRangeRemoved(groupPosition: Int, childPositionStart: Int, itemCount: Int) {
        mAdapter?.notifyChildItemRangeRemoved(groupPosition, childPositionStart, itemCount)
    }

    fun getGroupCount(): Int {
        return mAdapter?.getGroupCount() ?: 0
    }

    fun getChildCount(groupPosition: Int): Int {
        return mAdapter?.getChildCount(groupPosition) ?: 0
    }

    fun scrollToGroup(groupPosition: Int, childItemHeight: Int) {
        scrollToGroup(groupPosition, childItemHeight, 0, 0)
    }

    fun scrollToGroup(groupPosition: Int, childItemHeight: Int, topMargin: Int, bottomMargin: Int) {
        val totalChildrenHeight = getChildCount(groupPosition) * childItemHeight
        scrollToGroupWithTotalChildrenHeight(groupPosition, totalChildrenHeight, topMargin, bottomMargin)
    }

    fun scrollToGroupWithTotalChildrenHeight(
        groupPosition: Int,
        totalChildrenHeight: Int,
        topMargin: Int,
        bottomMargin: Int
    ) {
        val rv = mRecyclerView ?: return
        val packedPosition = getPackedPositionForGroup(groupPosition)
        val flatPosition = getFlatPosition(packedPosition)

        val vh = rv.findViewHolderForLayoutPosition(flatPosition) ?: return

        var actualTotalChildrenHeight = totalChildrenHeight
        if (!isGroupExpanded(groupPosition)) {
            actualTotalChildrenHeight = 0
        }

        val groupItemTop = vh.itemView.top
        val groupItemBottom = vh.itemView.bottom
        val parentHeight = rv.height

        val topRoom = groupItemTop
        val bottomRoom = parentHeight - groupItemBottom

        if (topRoom <= topMargin) {
            val parentTopPadding = rv.paddingTop
            val itemTopMargin = (vh.itemView.layoutParams as RecyclerView.LayoutParams).topMargin
            val offset = topMargin - parentTopPadding - itemTopMargin

            (rv.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(flatPosition, offset)
        } else if (bottomRoom >= (actualTotalChildrenHeight + bottomMargin)) {
            // no need to scroll
        } else {
            var scrollAmount = max(0, actualTotalChildrenHeight + bottomMargin - bottomRoom)
            scrollAmount = min(topRoom - topMargin, scrollAmount)
            rv.smoothScrollBy(0, scrollAmount)
        }
    }

    fun getExpandedGroupsCount(): Int {
        return mAdapter?.getExpandedGroupsCount() ?: 0
    }

    fun getCollapsedGroupsCount(): Int {
        return mAdapter?.getCollapsedGroupsCount() ?: 0
    }

    fun isAllGroupsExpanded(): Boolean {
        return mAdapter?.isAllGroupsExpanded() ?: false
    }

    fun isAllGroupsCollapsed(): Boolean {
        return mAdapter?.isAllGroupsCollapsed() ?: true
    }

    interface OnGroupExpandListener {
        fun onGroupExpand(groupPosition: Int, fromUser: Boolean)
    }

    interface OnGroupCollapseListener {
        fun onGroupCollapse(groupPosition: Int, fromUser: Boolean)
    }

    open class SavedState : Parcelable {
        val adapterSavedState: IntArray?

        constructor(adapterSavedState: IntArray?) {
            this.adapterSavedState = adapterSavedState
        }

        protected constructor(`in`: Parcel) {
            this.adapterSavedState = `in`.createIntArray()
        }

        override fun describeContents(): Int {
            return 0
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            dest.writeIntArray(this.adapterSavedState)
        }

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(source: Parcel): SavedState {
                return SavedState(source)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls(size)
            }
        }
    }
}
