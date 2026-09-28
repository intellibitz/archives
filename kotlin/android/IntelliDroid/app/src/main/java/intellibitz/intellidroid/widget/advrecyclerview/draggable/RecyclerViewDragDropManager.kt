package intellibitz.intellidroid.widget.advrecyclerview.draggable

import android.graphics.Rect
import android.graphics.drawable.NinePatchDrawable
import android.os.Build
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator
import androidx.core.view.MotionEventCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import intellibitz.intellidroid.widget.advrecyclerview.utils.CustomRecyclerViewUtils
import intellibitz.intellidroid.widget.advrecyclerview.utils.WrapperAdapterUtils
import java.lang.ref.WeakReference
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign

open class RecyclerViewDragDropManager : DraggableItemConstants {

    companion object {
        @JvmField
        val DEFAULT_SWAP_TARGET_TRANSITION_INTERPOLATOR: Interpolator = BasicSwapTargetTranslationInterpolator()
        @JvmField
        val DEFAULT_ITEM_SETTLE_BACK_INTO_PLACE_ANIMATION_INTERPOLATOR: Interpolator = DecelerateInterpolator()
        private const val TAG = "ARVDragDropManager"

        private const val SCROLL_DIR_NONE = 0
        private const val SCROLL_DIR_UP = (1 shl 0)
        private const val SCROLL_DIR_DOWN = (1 shl 1)
        private const val SCROLL_DIR_LEFT = (1 shl 2)
        private const val SCROLL_DIR_RIGHT = (1 shl 3)
        private const val LOCAL_LOGV = false
        private const val LOCAL_LOGD = false
        private const val SCROLL_THRESHOLD = 0.3f
        private const val SCROLL_AMOUNT_COEFF = 25f
        private const val SCROLL_TOUCH_SLOP_MULTIPLY = 1.5f

        private fun getItemViewOrigin(itemView: View?, vertical: Boolean): Int? {
            return if (itemView != null) (if (vertical) itemView.top else itemView.left) else null
        }

        private fun getDraggableItemWrapperAdapter(rv: RecyclerView): DraggableItemWrapperAdapter<*>? {
            return WrapperAdapterUtils.findWrappedAdapter(rv.adapter, DraggableItemWrapperAdapter::class.java)
        }

        private fun supportsEdgeEffect(): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH
        }

        private fun supportsViewTranslation(): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB
        }

        private fun safeEndAnimation(rv: RecyclerView?, holder: RecyclerView.ViewHolder) {
            rv?.itemAnimator?.endAnimation(holder)
        }

        private fun safeEndAnimations(rv: RecyclerView?) {
            rv?.itemAnimator?.endAnimations()
        }

        internal fun findSwapTargetItem(
            dest: SwapTarget,
            rv: RecyclerView,
            draggingItem: RecyclerView.ViewHolder?,
            draggingItemInfo: DraggingItemInfo,
            overlayItemLeft: Int,
            overlayItemTop: Int,
            range: ItemDraggableRange?,
            checkCanSwap: Boolean,
            alternative: Boolean
        ): SwapTarget {
            var swapTargetHolder: RecyclerView.ViewHolder? = null
            var left = overlayItemLeft
            var top = overlayItemTop

            dest.clear()

            if ((draggingItem == null) || (
                        draggingItem.adapterPosition != RecyclerView.NO_POSITION &&
                                draggingItem.itemId == draggingItemInfo.id)
            ) {
                val layoutType = CustomRecyclerViewUtils.getLayoutType(rv)
                val isVerticalLayout =
                    (CustomRecyclerViewUtils.extractOrientation(layoutType) == CustomRecyclerViewUtils.ORIENTATION_VERTICAL)

                if (isVerticalLayout) {
                    left = max(left, rv.paddingLeft)
                    left = min(left, max(0, rv.width - rv.paddingRight - draggingItemInfo.width))
                } else {
                    top = max(top, rv.paddingTop)
                    top = min(top, max(0, rv.height - rv.paddingBottom - draggingItemInfo.height))
                }

                when (layoutType) {
                    CustomRecyclerViewUtils.LAYOUT_TYPE_GRID_HORIZONTAL,
                    CustomRecyclerViewUtils.LAYOUT_TYPE_GRID_VERTICAL ->
                        swapTargetHolder = findSwapTargetItemForGridLayoutManager(
                            rv, draggingItem, draggingItemInfo, left, top, isVerticalLayout, checkCanSwap, alternative
                        )
                    CustomRecyclerViewUtils.LAYOUT_TYPE_STAGGERED_GRID_HORIZONTAL,
                    CustomRecyclerViewUtils.LAYOUT_TYPE_STAGGERED_GRID_VERTICAL ->
                        swapTargetHolder = findSwapTargetItemForStaggeredGridLayoutManager(
                            rv, draggingItem, draggingItemInfo, left, top, isVerticalLayout, checkCanSwap, alternative
                        )
                    CustomRecyclerViewUtils.LAYOUT_TYPE_LINEAR_HORIZONTAL,
                    CustomRecyclerViewUtils.LAYOUT_TYPE_LINEAR_VERTICAL ->
                        swapTargetHolder = findSwapTargetItemForLinearLayoutManager(
                            rv, draggingItem, draggingItemInfo, left, top, isVerticalLayout, checkCanSwap, alternative
                        )
                }
            }

            if (swapTargetHolder === draggingItem) {
                swapTargetHolder = null
                dest.self = true
            }

            if (swapTargetHolder != null && range != null) {
                if (!range.checkInRange(swapTargetHolder.adapterPosition)) {
                    swapTargetHolder = null
                }
            }

            dest.holder = swapTargetHolder
            dest.position = CustomRecyclerViewUtils.safeGetAdapterPosition(swapTargetHolder)

            return dest
        }

        private fun findSwapTargetItemForGridLayoutManager(
            rv: RecyclerView,
            draggingItem: RecyclerView.ViewHolder?,
            draggingItemInfo: DraggingItemInfo,
            overlayItemLeft: Int,
            overlayItemTop: Int,
            vertical: Boolean,
            checkCanSwap: Boolean,
            alternative: Boolean
        ): RecyclerView.ViewHolder? {
            if (alternative) {
                return null
            }

            var swapTargetHolder = findSwapTargetItemForGridLayoutManagerInternal1(
                rv, draggingItem, draggingItemInfo, overlayItemLeft, overlayItemTop, vertical
            )

            if (swapTargetHolder == null) {
                swapTargetHolder = findSwapTargetItemForGridLayoutManagerInternal2(
                    rv, draggingItem, draggingItemInfo, overlayItemLeft, overlayItemTop, vertical
                )
            }

            return swapTargetHolder
        }

        private fun findSwapTargetItemForGridLayoutManagerInternal1(
            rv: RecyclerView,
            draggingItem: RecyclerView.ViewHolder?,
            draggingItemInfo: DraggingItemInfo,
            overlayItemLeft: Int,
            overlayItemTop: Int,
            vertical: Boolean
        ): RecyclerView.ViewHolder? {
            var cx = overlayItemLeft
            var cy = overlayItemTop

            if (vertical) {
                val ml = draggingItemInfo.margins.left
                val mr = draggingItemInfo.margins.right
                cx += ((draggingItemInfo.width + (ml + mr)) / draggingItemInfo.spanSize * 0.5f - ml).toInt()
                cy += draggingItemInfo.height / 2
            } else {
                val mt = draggingItemInfo.margins.top
                val mb = draggingItemInfo.margins.bottom
                cx += draggingItemInfo.width / 2
                cy += ((draggingItemInfo.height + (mt + mb)) / draggingItemInfo.spanSize * 0.5f - mt).toInt()
            }

            return CustomRecyclerViewUtils.findChildViewHolderUnderWithoutTranslation(rv, cx.toFloat(), cy.toFloat())
        }

        private fun findSwapTargetItemForGridLayoutManagerInternal2(
            rv: RecyclerView,
            draggingItem: RecyclerView.ViewHolder?,
            draggingItemInfo: DraggingItemInfo,
            overlayItemLeft: Int,
            overlayItemTop: Int,
            vertical: Boolean
        ): RecyclerView.ViewHolder? {
            val spanCount = CustomRecyclerViewUtils.getSpanCount(rv)
            val height = rv.height
            val width = rv.width
            val paddingLeft = if (vertical) rv.paddingLeft else 0
            val paddingTop = if (!vertical) rv.paddingTop else 0
            val paddingRight = if (vertical) rv.paddingRight else 0
            val paddingBottom = if (!vertical) rv.paddingBottom else 0
            val columnWidth = (width - paddingLeft - paddingRight) / spanCount
            val rowHeight = (height - paddingTop - paddingBottom) / spanCount

            val cx = overlayItemLeft + draggingItemInfo.width / 2
            val cy = overlayItemTop + draggingItemInfo.height / 2

            for (i in spanCount - 1 downTo 0) {
                val cx2 = if (vertical) (paddingLeft + (columnWidth * i) + (columnWidth / 2)) else cx
                val cy2 = if (!vertical) (paddingTop + (rowHeight * i) + (rowHeight / 2)) else cy
                val vh2 = CustomRecyclerViewUtils.findChildViewHolderUnderWithoutTranslation(rv, cx2.toFloat(), cy2.toFloat())

                if (vh2 != null) {
                    val itemCount = rv.layoutManager?.itemCount ?: 0
                    val pos = vh2.adapterPosition

                    if ((pos != RecyclerView.NO_POSITION) && (pos == itemCount - 1)) {
                        return vh2
                    }
                    break
                }
            }

            return null
        }

        private fun findSwapTargetItemForStaggeredGridLayoutManager(
            rv: RecyclerView,
            draggingItem: RecyclerView.ViewHolder?,
            draggingItemInfo: DraggingItemInfo,
            overlayItemLeft: Int,
            overlayItemTop: Int,
            vertical: Boolean,
            checkCanSwap: Boolean,
            alternative: Boolean
        ): RecyclerView.ViewHolder? {
            if (alternative || draggingItem == null) {
                return null
            }

            var swapTargetHolder: RecyclerView.ViewHolder? = null

            val spanCount = CustomRecyclerViewUtils.getSpanCount(rv)
            val draggingItemSpanIndex = CustomRecyclerViewUtils.getSpanIndex(draggingItem)

            val ssvh: RecyclerView.ViewHolder?
            val csvh: RecyclerView.ViewHolder?
            val esvh: RecyclerView.ViewHolder?
            val sevh: RecyclerView.ViewHolder?
            val cevh: RecyclerView.ViewHolder?
            val eevh: RecyclerView.ViewHolder?
            val sSpanIndex: Int
            val eSpanIndex: Int
            val overlayItemOrigin: Int
            val draggingItemOrigin: Int

            if (vertical) {
                val sx = overlayItemLeft + 1
                val ex = overlayItemLeft + draggingItemInfo.width - 2
                val sy = overlayItemTop + 1
                val cy = overlayItemTop + draggingItemInfo.height / 2 - 1
                val ey = overlayItemTop + draggingItemInfo.height - 2

                val sPadding = rv.paddingLeft
                val ePadding = rv.paddingRight
                val rvSize = rv.width
                val spanLength = (rvSize - sPadding - ePadding) * (1.0f / spanCount)

                sSpanIndex = min(max(((sx - draggingItemInfo.margins.left - sPadding) / spanLength).toInt(), 0), spanCount - 1)
                eSpanIndex = min(max(((ex - draggingItemInfo.margins.right - sPadding) / spanLength).toInt(), 0), spanCount - 1)

                overlayItemOrigin = overlayItemTop
                draggingItemOrigin = draggingItem.itemView.top

                ssvh = CustomRecyclerViewUtils.findChildViewHolderUnderWithoutTranslation(rv, sx.toFloat(), sy.toFloat())
                csvh = CustomRecyclerViewUtils.findChildViewHolderUnderWithoutTranslation(rv, sx.toFloat(), cy.toFloat())
                esvh = CustomRecyclerViewUtils.findChildViewHolderUnderWithoutTranslation(rv, sx.toFloat(), ey.toFloat())
                sevh = CustomRecyclerViewUtils.findChildViewHolderUnderWithoutTranslation(rv, ex.toFloat(), sy.toFloat())
                cevh = CustomRecyclerViewUtils.findChildViewHolderUnderWithoutTranslation(rv, ex.toFloat(), cy.toFloat())
                eevh = CustomRecyclerViewUtils.findChildViewHolderUnderWithoutTranslation(rv, ex.toFloat(), ey.toFloat())
            } else {
                val sx = overlayItemLeft + 1
                val cx = overlayItemLeft + draggingItemInfo.width / 2 - 1
                val ex = overlayItemLeft + draggingItemInfo.width - 2
                val sy = overlayItemTop + 1
                val ey = overlayItemTop + draggingItemInfo.height - 2

                val sPadding = rv.paddingTop
                val ePadding = rv.paddingBottom
                val rvSize = rv.height
                val spanLength = (rvSize - sPadding - ePadding) * (1.0f / spanCount)

                sSpanIndex = min(max(((sx - draggingItemInfo.margins.top - sPadding) / spanLength).toInt(), 0), spanCount - 1)
                eSpanIndex = min(max(((ex - draggingItemInfo.margins.left - sPadding) / spanLength).toInt(), 0), spanCount - 1)

                overlayItemOrigin = overlayItemLeft
                draggingItemOrigin = draggingItem.itemView.left

                ssvh = CustomRecyclerViewUtils.findChildViewHolderUnderWithoutTranslation(rv, sx.toFloat(), sy.toFloat())
                csvh = CustomRecyclerViewUtils.findChildViewHolderUnderWithoutTranslation(rv, cx.toFloat(), sy.toFloat())
                esvh = CustomRecyclerViewUtils.findChildViewHolderUnderWithoutTranslation(rv, ex.toFloat(), sy.toFloat())
                sevh = CustomRecyclerViewUtils.findChildViewHolderUnderWithoutTranslation(rv, sx.toFloat(), ey.toFloat())
                cevh = CustomRecyclerViewUtils.findChildViewHolderUnderWithoutTranslation(rv, cx.toFloat(), ey.toFloat())
                eevh = CustomRecyclerViewUtils.findChildViewHolderUnderWithoutTranslation(rv, ex.toFloat(), ey.toFloat())
            }

            var sState = 0
            var eState = 0

            if (csvh != null) {
                sState = sState or 1
                if (csvh === ssvh) {
                    sState = sState or 2
                }
                if (csvh === esvh) {
                    sState = sState or 4
                }
            }

            if (cevh != null) {
                eState = eState or 1
                if (cevh === sevh) {
                    eState = eState or 2
                }
                if (cevh === eevh) {
                    eState = eState or 4
                }
            }

            val sCount = java.lang.Integer.bitCount(sState)
            val eCount = java.lang.Integer.bitCount(eState)

            if (sSpanIndex != draggingItemSpanIndex && sSpanIndex == eSpanIndex) {
                if (sCount == 3) {
                    swapTargetHolder = csvh
                } else if (eCount == 3) {
                    swapTargetHolder = cevh
                }
            }

            if (swapTargetHolder == null) {
                if (sCount == 2 && eCount != 2) {
                    swapTargetHolder = csvh
                } else if (eCount == 2 && sCount != 2) {
                    swapTargetHolder = cevh
                }
            }

            if (swapTargetHolder != null) {
                val swapTargetItemSpanIndex = CustomRecyclerViewUtils.getSpanIndex(swapTargetHolder)

                if (draggingItemSpanIndex == swapTargetItemSpanIndex) {
                    if (overlayItemOrigin <= draggingItemOrigin) {
                        if (((sState or eState) and 2) != 0) {
                            swapTargetHolder = null
                        }
                    } else {
                        if (((sState or eState) and 4) != 0) {
                            swapTargetHolder = null
                        }
                    }
                }
            }

            return swapTargetHolder
        }

        private fun findSwapTargetItemForLinearLayoutManager(
            rv: RecyclerView,
            draggingItem: RecyclerView.ViewHolder?,
            draggingItemInfo: DraggingItemInfo,
            overlayItemLeft: Int,
            overlayItemTop: Int,
            vertical: Boolean,
            checkCanSwap: Boolean,
            alternative: Boolean
        ): RecyclerView.ViewHolder? {
            if (draggingItem == null) {
                return null
            }

            var swapTargetHolder: RecyclerView.ViewHolder? = null

            if (!checkCanSwap && !alternative) {
                val draggingItemPosition = draggingItem.adapterPosition
                val draggingViewOrigin = if (vertical) draggingItem.itemView.top else draggingItem.itemView.left
                val overlayItemOrigin = if (vertical) overlayItemTop else overlayItemLeft

                if (overlayItemOrigin < draggingViewOrigin) {
                    if (draggingItemPosition > 0) {
                        swapTargetHolder = rv.findViewHolderForAdapterPosition(draggingItemPosition - 1)
                    }
                } else if (overlayItemOrigin > draggingViewOrigin) {
                    if (draggingItemPosition < ((rv.adapter?.itemCount ?: 0) - 1)) {
                        swapTargetHolder = rv.findViewHolderForAdapterPosition(draggingItemPosition + 1)
                    }
                }
            } else {
                val gap = draggingItem.itemView.resources.displayMetrics.density * 8
                val hgap = min(draggingItemInfo.width * 0.2f, gap)
                val vgap = min(draggingItemInfo.height * 0.2f, gap)
                val cx = overlayItemLeft + draggingItemInfo.width * 0.5f
                val cy = overlayItemTop + draggingItemInfo.height * 0.5f

                val swapTargetHolder1 = CustomRecyclerViewUtils.findChildViewHolderUnderWithoutTranslation(rv, cx - hgap, cy - vgap)
                val swapTargetHolder2 = CustomRecyclerViewUtils.findChildViewHolderUnderWithoutTranslation(rv, cx + hgap, cy + vgap)

                if (swapTargetHolder1 === swapTargetHolder2) {
                    swapTargetHolder = swapTargetHolder1
                }
            }

            return swapTargetHolder
        }
    }

    private val mTmpRect1 = Rect()
    internal var mDraggingItemViewHolder: RecyclerView.ViewHolder? = null
    private var mRecyclerView: RecyclerView? = null
    private var mSwapTargetTranslationInterpolator: Interpolator? = DEFAULT_SWAP_TARGET_TRANSITION_INTERPOLATOR
    private var mScrollOnDraggingProcess: ScrollOnDraggingProcessRunnable? = null

    private var mInternalUseOnItemTouchListener: RecyclerView.OnItemTouchListener? = null
    private var mInternalUseOnScrollListener: RecyclerView.OnScrollListener? = null

    private var mEdgeEffectDecorator: BaseEdgeEffectDecorator? = null
    private var mShadowDrawable: NinePatchDrawable? = null

    private var mDisplayDensity = 0f
    private var mTouchSlop = 0
    private var mScrollTouchSlop = 0
    private var mInitialTouchX = 0
    private var mInitialTouchY = 0
    private var mInitialTouchItemId = RecyclerView.NO_ID
    private var mInitiateOnLongPress = false
    private var mInitiateOnMove = true
    private var mLongPressTimeout: Int
    private var mCheckCanDrop = false

    private var mInScrollByMethod = false
    private var mActualScrollByXAmount = 0
    private var mActualScrollByYAmount = 0
    private var mItemSettleBackIntoPlaceAnimationDuration = 200
    private var mItemSettleBackIntoPlaceAnimationInterpolator: Interpolator? = DEFAULT_ITEM_SETTLE_BACK_INTO_PLACE_ANIMATION_INTERPOLATOR

    private var mAdapter: DraggableItemWrapperAdapter<*>? = null
    private var mDraggingItemInfo: DraggingItemInfo? = null
    private var mDraggingItemDecorator: DraggingItemDecorator? = null
    private var mSwapTargetItemOperator: SwapTargetItemOperator? = null
    private var mLastTouchX = 0
    private var mLastTouchY = 0
    private var mDragStartTouchX = 0
    private var mDragStartTouchY = 0
    private var mDragMinTouchX = 0
    private var mDragMinTouchY = 0
    private var mDragMaxTouchX = 0
    private var mDragMaxTouchY = 0
    private var mDragScrollDistanceX = 0
    private var mDragScrollDistanceY = 0
    private var mScrollDirMask = SCROLL_DIR_NONE
    private var mOrigOverScrollMode = 0
    private var mDraggableRange: ItemDraggableRange? = null
    private var mHandler: InternalHandler? = null
    private var mItemDragEventListener: OnItemDragEventListener? = null
    private var mCanDragH = false
    private var mCanDragV = false
    private var mDragEdgeScrollSpeed = 1.0f
    private val mTempSwapTarget = SwapTarget()
    private val mCheckItemSwappingRunnable = Runnable {
        if (mDraggingItemViewHolder != null) {
            val rv = getRecyclerView()
            if (rv != null) {
                checkItemSwapping(rv)
            }
        }
    }

    init {
        mInternalUseOnItemTouchListener = object : RecyclerView.OnItemTouchListener {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                return this@RecyclerViewDragDropManager.onInterceptTouchEvent(rv, e)
            }

            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
                this@RecyclerViewDragDropManager.onTouchEvent(rv, e)
            }

            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
                this@RecyclerViewDragDropManager.onRequestDisallowInterceptTouchEvent(disallowIntercept)
            }
        }

        mInternalUseOnScrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                this@RecyclerViewDragDropManager.onScrollStateChanged(recyclerView, newState)
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                this@RecyclerViewDragDropManager.onScrolled(recyclerView, dx, dy)
            }
        }

        mScrollOnDraggingProcess = ScrollOnDraggingProcessRunnable(this)
        mLongPressTimeout = ViewConfiguration.getLongPressTimeout()
    }

    @Suppress("UNCHECKED_CAST")
    fun createWrappedAdapter(adapter: RecyclerView.Adapter<*>): RecyclerView.Adapter<*> {
        require(adapter.hasStableIds()) { "The passed adapter does not support stable IDs" }
        check(mAdapter == null) { "already have a wrapped adapter" }

        val wrapper = DraggableItemWrapperAdapter(this, adapter as RecyclerView.Adapter<RecyclerView.ViewHolder>)
        mAdapter = wrapper
        return mAdapter!!
    }

    val isReleased: Boolean
        get() = mInternalUseOnItemTouchListener == null

    fun isReleased(): Boolean {
        return mInternalUseOnItemTouchListener == null
    }

    fun attachRecyclerView(rv: RecyclerView) {
        check(!isReleased) { "Accessing released object" }
        check(mRecyclerView == null) { "RecyclerView instance has already been set" }
        check(mAdapter != null && getDraggableItemWrapperAdapter(rv) === mAdapter) { "adapter is not set properly" }

        mRecyclerView = rv
        mRecyclerView!!.addOnScrollListener(mInternalUseOnScrollListener!!)
        mRecyclerView!!.addOnItemTouchListener(mInternalUseOnItemTouchListener!!)

        mDisplayDensity = mRecyclerView!!.resources.displayMetrics.density
        mTouchSlop = ViewConfiguration.get(mRecyclerView!!.context).scaledTouchSlop
        mScrollTouchSlop = (mTouchSlop * SCROLL_TOUCH_SLOP_MULTIPLY + 0.5f).toInt()
        mHandler = InternalHandler(this)

        if (supportsEdgeEffect()) {
            when (CustomRecyclerViewUtils.getOrientation(mRecyclerView)) {
                CustomRecyclerViewUtils.ORIENTATION_HORIZONTAL ->
                    mEdgeEffectDecorator = LeftRightEdgeEffectDecorator(mRecyclerView!!)
                CustomRecyclerViewUtils.ORIENTATION_VERTICAL ->
                    mEdgeEffectDecorator = TopBottomEdgeEffectDecorator(mRecyclerView!!)
            }
            mEdgeEffectDecorator?.start()
        }
    }

    fun release() {
        cancelDrag(true)

        mHandler?.release()
        mHandler = null

        mEdgeEffectDecorator?.finish()
        mEdgeEffectDecorator = null

        if (mRecyclerView != null && mInternalUseOnItemTouchListener != null) {
            mRecyclerView!!.removeOnItemTouchListener(mInternalUseOnItemTouchListener!!)
        }
        mInternalUseOnItemTouchListener = null

        if (mRecyclerView != null && mInternalUseOnScrollListener != null) {
            mRecyclerView!!.removeOnScrollListener(mInternalUseOnScrollListener!!)
        }
        mInternalUseOnScrollListener = null

        mScrollOnDraggingProcess?.release()
        mScrollOnDraggingProcess = null
        mAdapter = null
        mRecyclerView = null
        mSwapTargetTranslationInterpolator = null
    }

    val isDragging: Boolean
        get() = (mDraggingItemInfo != null) && (mHandler?.isCancelDragRequested != true)

    fun isDragging(): Boolean {
        return (mDraggingItemInfo != null) && (mHandler?.isCancelDragRequested != true)
    }

    fun setDraggingItemShadowDrawable(drawable: NinePatchDrawable?) {
        mShadowDrawable = drawable
    }

    fun setSwapTargetTranslationInterpolator(interpolator: Interpolator?) {
        mSwapTargetTranslationInterpolator = interpolator
    }

    fun getSwapTargetTranslationInterpolator(): Interpolator? {
        return mSwapTargetTranslationInterpolator
    }

    fun isInitiateOnLongPressEnabled(): Boolean {
        return mInitiateOnLongPress
    }

    fun setInitiateOnLongPress(initiateOnLongPress: Boolean) {
        mInitiateOnLongPress = initiateOnLongPress
    }

    fun isInitiateOnMoveEnabled(): Boolean {
        return mInitiateOnMove
    }

    fun setInitiateOnMove(initiateOnMove: Boolean) {
        mInitiateOnMove = initiateOnMove
    }

    fun setLongPressTimeout(longPressTimeout: Int) {
        mLongPressTimeout = longPressTimeout
    }

    fun getItemDragEventListener(): OnItemDragEventListener? {
        return mItemDragEventListener
    }

    fun setOnItemDragEventListener(listener: OnItemDragEventListener?) {
        mItemDragEventListener = listener
    }

    var dragEdgeScrollSpeed: Float
        get() = mDragEdgeScrollSpeed
        set(speed) {
            mDragEdgeScrollSpeed = min(max(speed, 0.0f), 2.0f)
        }

    fun isCheckCanDropEnabled(): Boolean {
        return mCheckCanDrop
    }

    fun setCheckCanDropEnabled(enabled: Boolean) {
        mCheckCanDrop = enabled
    }

    internal fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
        val action = MotionEventCompat.getActionMasked(e)

        if (LOCAL_LOGV) {
            Log.v(TAG, "onInterceptTouchEvent() action = $action")
        }

        when (action) {
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL ->
                handleActionUpOrCancel(action, true)

            MotionEvent.ACTION_DOWN ->
                if (!isDragging) {
                    handleActionDown(rv, e)
                }

            MotionEvent.ACTION_MOVE ->
                if (isDragging) {
                    handleActionMoveWhileDragging(rv, e)
                    return true
                } else {
                    if (handleActionMoveWhileNotDragging(rv, e)) {
                        return true
                    }
                }
        }

        return false
    }

    internal fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
        val action = MotionEventCompat.getActionMasked(e)

        if (LOCAL_LOGV) {
            Log.v(TAG, "onTouchEvent() action = $action")
        }

        if (!isDragging) {
            return
        }

        when (action) {
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL ->
                handleActionUpOrCancel(action, true)

            MotionEvent.ACTION_MOVE ->
                handleActionMoveWhileDragging(rv, e)
        }
    }

    internal fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
        if (disallowIntercept) {
            cancelDrag(true)
        }
    }

    internal fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        if (LOCAL_LOGV) {
            Log.v(TAG, "onScrolled(dx = $dx, dy = $dy)")
        }

        if (mInScrollByMethod) {
            mActualScrollByXAmount = dx
            mActualScrollByYAmount = dy
        } else if (isDragging) {
            ViewCompat.postOnAnimationDelayed(mRecyclerView, mCheckItemSwappingRunnable, 500)
        }
    }

    internal fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        if (LOCAL_LOGV) {
            Log.v(TAG, "onScrollStateChanged(newState = $newState)")
        }

        if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
            cancelDrag(true)
        }
    }

    private fun handleActionDown(rv: RecyclerView, e: MotionEvent): Boolean {
        val holder = CustomRecyclerViewUtils.findChildViewHolderUnderWithoutTranslation(rv, e.x, e.y)

        if (!checkTouchedItemState(rv, holder)) {
            return false
        }

        val orientation = CustomRecyclerViewUtils.getOrientation(mRecyclerView)
        val spanCount = CustomRecyclerViewUtils.getSpanCount(mRecyclerView)

        val touchX = (e.x + 0.5f).toInt()
        val touchY = (e.y + 0.5f).toInt()
        mInitialTouchX = touchX
        mLastTouchX = touchX
        mInitialTouchY = touchY
        mLastTouchY = touchY
        mInitialTouchItemId = holder!!.itemId
        mCanDragH = (orientation == CustomRecyclerViewUtils.ORIENTATION_HORIZONTAL) ||
                ((orientation == CustomRecyclerViewUtils.ORIENTATION_VERTICAL) && (spanCount > 1))
        mCanDragV = (orientation == CustomRecyclerViewUtils.ORIENTATION_VERTICAL) ||
                ((orientation == CustomRecyclerViewUtils.ORIENTATION_HORIZONTAL) && (spanCount > 1))

        if (mInitiateOnLongPress) {
            mHandler?.startLongPressDetection(e, mLongPressTimeout)
        }

        return true
    }

    internal fun handleOnLongPress(e: MotionEvent) {
        if (mInitiateOnLongPress && mRecyclerView != null) {
            checkConditionAndStartDragging(mRecyclerView!!, e, false)
        }
    }

    private fun startDragging(rv: RecyclerView, e: MotionEvent, holder: RecyclerView.ViewHolder, range: ItemDraggableRange) {
        safeEndAnimation(rv, holder)

        mHandler?.cancelLongPressDetection()

        mDraggingItemInfo = DraggingItemInfo(rv, holder, mLastTouchX, mLastTouchY)
        mDraggingItemViewHolder = holder
        mDraggableRange = range

        mOrigOverScrollMode = ViewCompat.getOverScrollMode(rv)
        ViewCompat.setOverScrollMode(rv, ViewCompat.OVER_SCROLL_NEVER)

        mLastTouchX = (e.x + 0.5f).toInt()
        mLastTouchY = (e.y + 0.5f).toInt()

        mDragStartTouchY = mLastTouchY
        mDragMinTouchY = mLastTouchY
        mDragMaxTouchY = mLastTouchY
        mDragStartTouchX = mLastTouchX
        mDragMinTouchX = mLastTouchX
        mDragMaxTouchX = mLastTouchX
        mScrollDirMask = SCROLL_DIR_NONE

        mRecyclerView?.parent?.requestDisallowInterceptTouchEvent(true)

        startScrollOnDraggingProcess()

        mAdapter?.onDragItemStarted(mDraggingItemInfo!!, holder, mDraggableRange)
        mAdapter?.onBindViewHolder(holder, holder.layoutPosition)

        mDraggingItemDecorator = DraggingItemDecorator(mRecyclerView!!, holder, mDraggableRange)
        mDraggingItemDecorator!!.setShadowDrawable(mShadowDrawable)
        mDraggingItemDecorator!!.start(e, mDraggingItemInfo!!)

        val layoutType = CustomRecyclerViewUtils.getLayoutType(mRecyclerView)

        if (supportsViewTranslation() && !mCheckCanDrop &&
            (layoutType == CustomRecyclerViewUtils.LAYOUT_TYPE_LINEAR_VERTICAL ||
                    layoutType == CustomRecyclerViewUtils.LAYOUT_TYPE_LINEAR_HORIZONTAL)
        ) {
            mSwapTargetItemOperator = SwapTargetItemOperator(mRecyclerView!!, holder, mDraggableRange, mDraggingItemInfo!!)
            mSwapTargetItemOperator!!.setSwapTargetTranslationInterpolator(mSwapTargetTranslationInterpolator)
            mSwapTargetItemOperator!!.start()
            mSwapTargetItemOperator!!.update(
                mDraggingItemDecorator!!.getDraggingItemTranslationX(),
                mDraggingItemDecorator!!.getDraggingItemTranslationY()
            )
        }

        mEdgeEffectDecorator?.reorderToTop()

        mItemDragEventListener?.onItemDragStarted(mAdapter?.getDraggingItemInitialPosition() ?: 0)
        mItemDragEventListener?.onItemDragMoveDistanceUpdated(0, 0)
    }

    fun cancelDrag() {
        cancelDrag(false)
    }

    internal fun cancelDrag(immediately: Boolean) {
        handleActionUpOrCancel(MotionEvent.ACTION_CANCEL, false)

        if (immediately) {
            finishDragging(false)
        } else {
            if (isDragging) {
                mHandler?.requestDeferredCancelDrag()
            }
        }
    }

    private fun finishDragging(result: Boolean) {
        if (!isDragging) {
            return
        }

        mHandler?.removeDeferredCancelDragRequest()

        if (mRecyclerView != null && mDraggingItemViewHolder != null) {
            ViewCompat.setOverScrollMode(mRecyclerView, mOrigOverScrollMode)
        }

        if (mDraggingItemDecorator != null) {
            mDraggingItemDecorator!!.setReturnToDefaultPositionAnimationDuration(mItemSettleBackIntoPlaceAnimationDuration)
            mDraggingItemDecorator!!.setReturnToDefaultPositionAnimationInterpolator(mItemSettleBackIntoPlaceAnimationInterpolator)
            mDraggingItemDecorator!!.finish(true)
        }

        if (mSwapTargetItemOperator != null) {
            mSwapTargetItemOperator!!.setReturnToDefaultPositionAnimationDuration(mItemSettleBackIntoPlaceAnimationDuration)
            mDraggingItemDecorator?.setReturnToDefaultPositionAnimationInterpolator(mItemSettleBackIntoPlaceAnimationInterpolator)
            mSwapTargetItemOperator!!.finish(true)
        }

        mEdgeEffectDecorator?.releaseBothGlows()
        stopScrollOnDraggingProcess()

        mRecyclerView?.parent?.requestDisallowInterceptTouchEvent(false)
        mRecyclerView?.invalidate()

        mDraggableRange = null
        mDraggingItemDecorator = null
        mSwapTargetItemOperator = null
        mDraggingItemViewHolder = null
        mDraggingItemInfo = null

        mLastTouchX = 0
        mLastTouchY = 0
        mDragStartTouchX = 0
        mDragStartTouchY = 0
        mDragMinTouchX = 0
        mDragMinTouchY = 0
        mDragMaxTouchX = 0
        mDragMaxTouchY = 0
        mDragScrollDistanceX = 0
        mDragScrollDistanceY = 0
        mCanDragH = false
        mCanDragV = false

        var draggingItemInitialPosition = RecyclerView.NO_POSITION
        var draggingItemCurrentPosition = RecyclerView.NO_POSITION

        if (mAdapter != null) {
            draggingItemInitialPosition = mAdapter!!.getDraggingItemInitialPosition()
            draggingItemCurrentPosition = mAdapter!!.getDraggingItemCurrentPosition()
            mAdapter!!.onDragItemFinished(result)
        }

        mItemDragEventListener?.onItemDragFinished(
            draggingItemInitialPosition,
            draggingItemCurrentPosition,
            result
        )
    }

    private fun handleActionUpOrCancel(action: Int, invokeFinish: Boolean): Boolean {
        val result = (action == MotionEvent.ACTION_UP)

        mHandler?.cancelLongPressDetection()

        mInitialTouchX = 0
        mInitialTouchY = 0
        mLastTouchX = 0
        mLastTouchY = 0
        mDragStartTouchX = 0
        mDragStartTouchY = 0
        mDragMinTouchX = 0
        mDragMinTouchY = 0
        mDragMaxTouchX = 0
        mDragMaxTouchY = 0
        mDragScrollDistanceX = 0
        mDragScrollDistanceY = 0
        mInitialTouchItemId = RecyclerView.NO_ID
        mCanDragH = false
        mCanDragV = false

        if (invokeFinish && isDragging) {
            if (LOCAL_LOGD) {
                Log.d(TAG, "dragging finished --- result = $result")
            }
            finishDragging(result)
        }

        return true
    }

    private fun handleActionMoveWhileNotDragging(rv: RecyclerView, e: MotionEvent): Boolean {
        return if (mInitiateOnMove) {
            checkConditionAndStartDragging(rv, e, true)
        } else {
            false
        }
    }

    private fun checkConditionAndStartDragging(rv: RecyclerView, e: MotionEvent, checkTouchSlop: Boolean): Boolean {
        if (mDraggingItemInfo != null) {
            return false
        }

        val touchX = (e.x + 0.5f).toInt()
        val touchY = (e.y + 0.5f).toInt()

        mLastTouchX = touchX
        mLastTouchY = touchY

        if (mInitialTouchItemId == RecyclerView.NO_ID) {
            return false
        }

        if (checkTouchSlop) {
            if (!((mCanDragH && (abs(touchX - mInitialTouchX) > mTouchSlop)) ||
                        (mCanDragV && (abs(touchY - mInitialTouchY) > mTouchSlop)))
            ) {
                return false
            }
        }

        val holder = CustomRecyclerViewUtils.findChildViewHolderUnderWithoutTranslation(rv, mInitialTouchX.toFloat(), mInitialTouchY.toFloat())
            ?: return false

        val position = CustomRecyclerViewUtils.getSynchronizedPosition(holder)
        if (position == RecyclerView.NO_POSITION) {
            return false
        }

        val adapter = mAdapter ?: return false

        val view = holder.itemView
        val translateX = (ViewCompat.getTranslationX(view) + 0.5f).toInt()
        val translateY = (ViewCompat.getTranslationY(view) + 0.5f).toInt()
        val viewX = touchX - (view.left + translateX)
        val viewY = touchY - (view.top + translateY)

        if (!adapter.canStartDrag(holder, position, viewX, viewY)) {
            return false
        }

        var range = adapter.getItemDraggableRange(holder, position)
        if (range == null) {
            range = ItemDraggableRange(0, max(0, adapter.itemCount - 1))
        }

        verifyItemDraggableRange(range, holder)

        if (LOCAL_LOGD) {
            Log.d(TAG, "dragging started")
        }

        startDragging(rv, e, holder, range)
        return true
    }

    private fun verifyItemDraggableRange(range: ItemDraggableRange, holder: RecyclerView.ViewHolder) {
        val start = 0
        val end = max(0, (mAdapter?.itemCount ?: 0) - 1)

        check(range.start <= range.end) { "Invalid range specified --- start > range (range = $range)" }
        check(range.start >= start) { "Invalid range specified --- start < 0 (range = $range)" }
        check(range.end <= end) { "Invalid range specified --- end >= count (range = $range)" }
        check(range.checkInRange(holder.adapterPosition)) {
            "Invalid range specified --- does not contain drag target item (range = $range, position = ${holder.adapterPosition})"
        }
    }

    private fun handleActionMoveWhileDragging(rv: RecyclerView, e: MotionEvent) {
        mLastTouchX = (e.x + 0.5f).toInt()
        mLastTouchY = (e.y + 0.5f).toInt()

        mDragMinTouchX = min(mDragMinTouchX, mLastTouchX)
        mDragMinTouchY = min(mDragMinTouchY, mLastTouchY)
        mDragMaxTouchX = max(mDragMaxTouchX, mLastTouchX)
        mDragMaxTouchY = max(mDragMaxTouchY, mLastTouchY)

        updateDragDirectionMask()

        val updated = mDraggingItemDecorator?.update(e, false) ?: false

        if (updated) {
            if (mSwapTargetItemOperator != null) {
                mSwapTargetItemOperator!!.update(
                    mDraggingItemDecorator!!.getDraggingItemTranslationX(),
                    mDraggingItemDecorator!!.getDraggingItemTranslationY()
                )
            }

            checkItemSwapping(rv)
            onItemMoveDistanceUpdated()
        }
    }

    private fun updateDragDirectionMask() {
        when (CustomRecyclerViewUtils.getOrientation(mRecyclerView)) {
            CustomRecyclerViewUtils.ORIENTATION_VERTICAL -> {
                if (((mDragStartTouchY - mDragMinTouchY) > mScrollTouchSlop) ||
                    ((mDragMaxTouchY - mLastTouchY) > mScrollTouchSlop)
                ) {
                    mScrollDirMask = mScrollDirMask or SCROLL_DIR_UP
                }
                if (((mDragMaxTouchY - mDragStartTouchY) > mScrollTouchSlop) ||
                    ((mLastTouchY - mDragMinTouchY) > mScrollTouchSlop)
                ) {
                    mScrollDirMask = mScrollDirMask or SCROLL_DIR_DOWN
                }
            }
            CustomRecyclerViewUtils.ORIENTATION_HORIZONTAL -> {
                if (((mDragStartTouchX - mDragMinTouchX) > mScrollTouchSlop) ||
                    ((mDragMaxTouchX - mLastTouchX) > mScrollTouchSlop)
                ) {
                    mScrollDirMask = mScrollDirMask or SCROLL_DIR_LEFT
                }
                if (((mDragMaxTouchX - mDragStartTouchX) > mScrollTouchSlop) ||
                    ((mLastTouchX - mDragMinTouchX) > mScrollTouchSlop)
                ) {
                    mScrollDirMask = mScrollDirMask or SCROLL_DIR_RIGHT
                }
            }
        }
    }

    internal fun checkItemSwapping(rv: RecyclerView) {
        val draggingItem = mDraggingItemViewHolder
        val adapter = mAdapter ?: return
        val info = mDraggingItemInfo ?: return

        val overlayItemLeft = mLastTouchX - info.grabbedPositionX
        val overlayItemTop = mLastTouchY - info.grabbedPositionY
        val draggingItemInitialPosition = adapter.getDraggingItemInitialPosition()
        val draggingItemCurrentPosition = adapter.getDraggingItemCurrentPosition()
        var swapTarget: SwapTarget
        var canSwap = false

        swapTarget = findSwapTargetItem(
            mTempSwapTarget, rv, draggingItem, info, overlayItemLeft, overlayItemTop, mDraggableRange, mCheckCanDrop, false
        )

        if (swapTarget.position != RecyclerView.NO_POSITION) {
            if (!mCheckCanDrop) {
                canSwap = true
            }
            if (!canSwap) {
                canSwap = adapter.canDropItems(draggingItemInitialPosition, swapTarget.position)
            }
            if (!canSwap) {
                swapTarget = findSwapTargetItem(
                    mTempSwapTarget, rv, draggingItem, info, overlayItemLeft, overlayItemTop, mDraggableRange, mCheckCanDrop, true
                )

                if (swapTarget.position != RecyclerView.NO_POSITION) {
                    canSwap = adapter.canDropItems(draggingItemInitialPosition, swapTarget.position)
                }
            }
        }

        if (canSwap && swapTarget.holder != null) {
            swapItems(rv, draggingItemCurrentPosition, draggingItem, swapTarget.holder!!)
        }

        if (mSwapTargetItemOperator != null) {
            mSwapTargetItemOperator!!.setSwapTargetItem(if (canSwap) swapTarget.holder else null)
        }
    }

    private fun onItemMoveDistanceUpdated() {
        val listener = mItemDragEventListener ?: return
        val decorator = mDraggingItemDecorator ?: return

        val moveX = mDragScrollDistanceX + decorator.getDraggingItemMoveOffsetX()
        val moveY = mDragScrollDistanceY + decorator.getDraggingItemMoveOffsetY()

        listener.onItemDragMoveDistanceUpdated(moveX, moveY)
    }

    internal fun handleScrollOnDragging() {
        val rv = mRecyclerView ?: return

        when (CustomRecyclerViewUtils.getOrientation(rv)) {
            CustomRecyclerViewUtils.ORIENTATION_VERTICAL ->
                handleScrollOnDraggingInternal(rv, false)
            CustomRecyclerViewUtils.ORIENTATION_HORIZONTAL ->
                handleScrollOnDraggingInternal(rv, true)
        }
    }

    private fun handleScrollOnDraggingInternal(rv: RecyclerView, horizontal: Boolean) {
        val edge = if (horizontal) rv.width else rv.height
        if (edge == 0) {
            return
        }

        val invEdge = 1.0f / edge
        val normalizedTouchPos = (if (horizontal) mLastTouchX else mLastTouchY) * invEdge
        val threshold = SCROLL_THRESHOLD
        val invThreshold = 1.0f / threshold
        val centerOffset = normalizedTouchPos - 0.5f
        val absCenterOffset = abs(centerOffset)
        val acceleration = max(0.0f, threshold - (0.5f - absCenterOffset)) * invThreshold
        val mask = mScrollDirMask
        val decorator = mDraggingItemDecorator ?: return

        var scrollAmount = (sign(centerOffset) * (SCROLL_AMOUNT_COEFF * mDragEdgeScrollSpeed * mDisplayDensity * acceleration + 0.5f)).toInt()
        var actualScrolledAmount = 0

        val range = mDraggableRange ?: return

        val firstVisibleChild = CustomRecyclerViewUtils.findFirstCompletelyVisibleItemPosition(mRecyclerView)
        val lastVisibleChild = CustomRecyclerViewUtils.findLastCompletelyVisibleItemPosition(mRecyclerView)

        var reachedToFirstHardLimit = false
        var reachedToFirstSoftLimit = false
        var reachedToLastHardLimit = false
        var reachedToLastSoftLimit = false

        if (firstVisibleChild != RecyclerView.NO_POSITION) {
            if (firstVisibleChild <= range.start) {
                reachedToFirstSoftLimit = true
            }
            if (firstVisibleChild <= (range.start - 1)) {
                reachedToFirstHardLimit = true
            }
        }

        if (lastVisibleChild != RecyclerView.NO_POSITION) {
            if (lastVisibleChild >= range.end) {
                reachedToLastSoftLimit = true
            }
            if (lastVisibleChild >= (range.end + 1)) {
                reachedToLastHardLimit = true
            }
        }

        if (scrollAmount > 0) {
            if ((mask and (if (horizontal) SCROLL_DIR_RIGHT else SCROLL_DIR_DOWN)) == 0) {
                scrollAmount = 0
            }
        } else if (scrollAmount < 0) {
            if ((mask and (if (horizontal) SCROLL_DIR_LEFT else SCROLL_DIR_UP)) == 0) {
                scrollAmount = 0
            }
        }

        if ((!reachedToFirstHardLimit && (scrollAmount < 0)) ||
            (!reachedToLastHardLimit && (scrollAmount > 0))
        ) {
            safeEndAnimationsIfRequired(rv)

            actualScrolledAmount = if (horizontal) {
                scrollByXAndGetScrolledAmount(scrollAmount)
            } else {
                scrollByYAndGetScrolledAmount(scrollAmount)
            }

            if (scrollAmount < 0) {
                decorator.setIsScrolling(!reachedToFirstSoftLimit)
            } else {
                decorator.setIsScrolling(!reachedToLastSoftLimit)
            }

            decorator.refresh(true)
            mSwapTargetItemOperator?.update(
                decorator.getDraggingItemTranslationX(),
                decorator.getDraggingItemTranslationY()
            )
        } else {
            decorator.setIsScrolling(false)
        }

        val actualIsScrolling = (actualScrolledAmount != 0)

        if (mEdgeEffectDecorator != null) {
            val edgeEffectStrength = 0.005f

            val draggingItemTopLeft = if (horizontal) decorator.getTranslatedItemPositionLeft() else decorator.getTranslatedItemPositionTop()
            val draggingItemBottomRight = if (horizontal) decorator.getTranslatedItemPositionRight() else decorator.getTranslatedItemPositionBottom()
            val draggingItemCenter = (draggingItemTopLeft + draggingItemBottomRight) / 2
            val nearEdgePosition = if (firstVisibleChild == 0 && lastVisibleChild == 0) {
                if (scrollAmount < 0) draggingItemTopLeft else draggingItemBottomRight
            } else {
                if (draggingItemCenter < (edge / 2)) draggingItemTopLeft else draggingItemBottomRight
            }

            val nearEdgeOffset = (nearEdgePosition * invEdge) - 0.5f
            val absNearEdgeOffset = abs(nearEdgeOffset)
            var edgeEffectPullDistance = 0f

            if ((absNearEdgeOffset > 0.4f) && (scrollAmount != 0) && !actualIsScrolling) {
                if (nearEdgeOffset < 0) {
                    if (if (horizontal) decorator.isReachedToLeftLimit() else decorator.isReachedToTopLimit()) {
                        edgeEffectPullDistance = -mDisplayDensity * edgeEffectStrength
                    }
                } else {
                    if (if (horizontal) decorator.isReachedToRightLimit() else decorator.isReachedToBottomLimit()) {
                        edgeEffectPullDistance = mDisplayDensity * edgeEffectStrength
                    }
                }
            }

            updateEdgeEffect(edgeEffectPullDistance)
        }

        ViewCompat.postOnAnimation(mRecyclerView, mCheckItemSwappingRunnable)

        if (actualScrolledAmount != 0) {
            if (horizontal) {
                mDragScrollDistanceX += actualScrolledAmount
            } else {
                mDragScrollDistanceY += actualScrolledAmount
            }

            onItemMoveDistanceUpdated()
        }
    }

    private fun updateEdgeEffect(distance: Float) {
        if (distance != 0.0f) {
            if (distance < 0) {
                mEdgeEffectDecorator?.pullFirstEdge(distance)
            } else {
                mEdgeEffectDecorator?.pullSecondEdge(distance)
            }
        } else {
            mEdgeEffectDecorator?.releaseBothGlows()
        }
    }

    private fun scrollByYAndGetScrolledAmount(ry: Int): Int {
        val rv = mRecyclerView ?: return 0
        mActualScrollByYAmount = 0
        mInScrollByMethod = true
        rv.scrollBy(0, ry)
        mInScrollByMethod = false
        return mActualScrollByYAmount
    }

    private fun scrollByXAndGetScrolledAmount(rx: Int): Int {
        val rv = mRecyclerView ?: return 0
        mActualScrollByXAmount = 0
        mInScrollByMethod = true
        rv.scrollBy(rx, 0)
        mInScrollByMethod = false
        return mActualScrollByXAmount
    }

    internal fun getRecyclerView(): RecyclerView? {
        return mRecyclerView
    }

    private fun startScrollOnDraggingProcess() {
        mScrollOnDraggingProcess?.start()
    }

    private fun stopScrollOnDraggingProcess() {
        mScrollOnDraggingProcess?.stop()
    }

    private fun swapItems(
        rv: RecyclerView,
        draggingItemAdapterPosition: Int,
        draggingItem: RecyclerView.ViewHolder?,
        swapTargetHolder: RecyclerView.ViewHolder
    ) {
        val swapTargetMargins = CustomRecyclerViewUtils.getLayoutMargins(swapTargetHolder.itemView, mTmpRect1)
        val fromPosition = draggingItemAdapterPosition
        val toPosition = swapTargetHolder.adapterPosition
        val diffPosition = abs(fromPosition - toPosition)
        var performSwapping = false

        if (fromPosition == RecyclerView.NO_POSITION || toPosition == RecyclerView.NO_POSITION) {
            return
        }

        val actualDraggingItemId = rv.adapter?.getItemId(fromPosition)
        if (actualDraggingItemId != mDraggingItemInfo?.id) {
            if (LOCAL_LOGV) {
                Log.v(TAG, "RecyclerView state has not been synchronized to data yet")
            }
            return
        }

        val isLinearLayout = CustomRecyclerViewUtils.isLinearLayout(CustomRecyclerViewUtils.getLayoutType(rv))
        val swapNextItemSmoothlyInLinearLayout = isLinearLayout && (!supportsViewTranslation() || !mCheckCanDrop)

        if (diffPosition == 0) {
            // no-op
        } else if ((diffPosition == 1) && (draggingItem != null) && swapNextItemSmoothlyInLinearLayout) {
            val v1 = draggingItem.itemView
            val v2 = swapTargetHolder.itemView
            val m1 = mDraggingItemInfo!!.margins
            val m2 = swapTargetMargins

            if (mCanDragH) {
                val left = min(v1.left - m1.left, v2.left - m2.left)
                val right = max(v1.right + m1.right, v2.right + m2.right)

                val midPointOfTheItems = left + ((right - left) * 0.5f)
                val midPointOfTheOverlaidItem = (mLastTouchX - mDraggingItemInfo!!.grabbedPositionX) + (mDraggingItemInfo!!.width * 0.5f)

                if (toPosition < fromPosition) {
                    if (midPointOfTheOverlaidItem < midPointOfTheItems) {
                        performSwapping = true
                    }
                } else {
                    if (midPointOfTheOverlaidItem > midPointOfTheItems) {
                        performSwapping = true
                    }
                }
            }

            if (!performSwapping && mCanDragV) {
                val top = min(v1.top - m1.top, v2.top - m2.top)
                val bottom = max(v1.bottom + m1.bottom, v2.bottom + m2.bottom)

                val midPointOfTheItems = top + ((bottom - top) * 0.5f)
                val midPointOfTheOverlaidItem = (mLastTouchY - mDraggingItemInfo!!.grabbedPositionY) + (mDraggingItemInfo!!.height * 0.5f)

                if (toPosition < fromPosition) {
                    if (midPointOfTheOverlaidItem < midPointOfTheItems) {
                        performSwapping = true
                    }
                } else {
                    if (midPointOfTheOverlaidItem > midPointOfTheItems) {
                        performSwapping = true
                    }
                }
            }
        } else {
            performSwapping = true
        }

        if (performSwapping) {
            performSwapItems(rv, swapTargetHolder, swapTargetMargins, fromPosition, toPosition)
        }
    }

    private fun performSwapItems(
        rv: RecyclerView,
        swapTargetHolder: RecyclerView.ViewHolder,
        swapTargetMargins: Rect,
        fromPosition: Int,
        toPosition: Int
    ) {
        if (LOCAL_LOGD) {
            Log.d(TAG, "item swap (from: $fromPosition, to: $toPosition)")
        }

        mItemDragEventListener?.onItemDragPositionChanged(fromPosition, toPosition)

        val layoutManager = mRecyclerView?.layoutManager ?: return
        val layoutType = CustomRecyclerViewUtils.getLayoutType(mRecyclerView)
        val isVertical = (CustomRecyclerViewUtils.extractOrientation(layoutType) == CustomRecyclerViewUtils.ORIENTATION_VERTICAL)
        val firstVisible = CustomRecyclerViewUtils.findFirstVisibleItemPosition(mRecyclerView, false)
        val fromView = CustomRecyclerViewUtils.findViewByPosition(layoutManager, fromPosition)
        val toView = CustomRecyclerViewUtils.findViewByPosition(layoutManager, toPosition)
        val firstView = CustomRecyclerViewUtils.findViewByPosition(layoutManager, firstVisible)
        val fromOrigin = getItemViewOrigin(fromView, isVertical)
        val toOrigin = getItemViewOrigin(toView, isVertical)
        val firstOrigin = getItemViewOrigin(firstView, isVertical)

        mAdapter?.moveItem(fromPosition, toPosition)

        if ((firstVisible == fromPosition) && (firstOrigin != null) && (toOrigin != null)) {
            rv.scrollBy(0, -(toOrigin - firstOrigin))
            safeEndAnimations(rv)
        } else if ((firstVisible == toPosition) && (fromView != null) && (fromOrigin != null) && (fromOrigin != toOrigin)) {
            val lp = fromView.layoutParams as ViewGroup.MarginLayoutParams
            rv.scrollBy(0, -(layoutManager.getDecoratedMeasuredHeight(fromView) + lp.topMargin + lp.bottomMargin))
            safeEndAnimations(rv)
        }
    }

    private fun checkTouchedItemState(rv: RecyclerView, holder: RecyclerView.ViewHolder?): Boolean {
        if (holder !is DraggableItemViewHolder) {
            return false
        }

        val itemPosition = holder.adapterPosition
        val adapter = rv.adapter ?: return false

        if (itemPosition !in 0 until adapter.itemCount) {
            return false
        }

        if (holder.itemId != adapter.getItemId(itemPosition)) {
            return false
        }

        return true
    }

    private fun safeEndAnimationsIfRequired(rv: RecyclerView) {
        if (mSwapTargetItemOperator != null) {
            safeEndAnimations(rv)
        }
    }

    var itemSettleBackIntoPlaceAnimationDuration: Int
        get() = mItemSettleBackIntoPlaceAnimationDuration
        set(duration) {
            mItemSettleBackIntoPlaceAnimationDuration = duration
        }

    fun getItemSettleBackIntoPlaceAnimationDuration(): Int {
        return mItemSettleBackIntoPlaceAnimationDuration
    }

    var itemSettleBackIntoPlaceAnimationInterpolator: Interpolator?
        get() = mItemSettleBackIntoPlaceAnimationInterpolator
        set(interpolator) {
            mItemSettleBackIntoPlaceAnimationInterpolator = interpolator
        }

    fun getItemSettleBackIntoPlaceAnimationInterpolator(): Interpolator? {
        return mItemSettleBackIntoPlaceAnimationInterpolator
    }

    internal fun onDraggingItemViewRecycled() {
        mDraggingItemViewHolder = null
        mDraggingItemDecorator?.invalidateDraggingItem()
    }

    internal fun onNewDraggingItemViewBound(holder: RecyclerView.ViewHolder) {
        mDraggingItemViewHolder = holder
        mDraggingItemDecorator?.setDraggingItemViewHolder(holder)
    }

    interface OnItemDragEventListener {
        fun onItemDragStarted(position: Int)
        fun onItemDragPositionChanged(fromPosition: Int, toPosition: Int)
        fun onItemDragFinished(fromPosition: Int, toPosition: Int, result: Boolean)
        fun onItemDragMoveDistanceUpdated(offsetX: Int, offsetY: Int)
    }

    internal class SwapTarget {
        @JvmField
        var holder: RecyclerView.ViewHolder? = null
        @JvmField
        var position = RecyclerView.NO_POSITION
        @JvmField
        var self = false

        fun clear() {
            holder = null
            position = RecyclerView.NO_POSITION
            self = false
        }
    }

    private class ScrollOnDraggingProcessRunnable(holder: RecyclerViewDragDropManager) : Runnable {
        private val mHolderRef: WeakReference<RecyclerViewDragDropManager> = WeakReference(holder)
        private var mStarted = false

        fun start() {
            if (mStarted) {
                return
            }

            val holder = mHolderRef.get() ?: return
            val rv = holder.getRecyclerView() ?: return

            ViewCompat.postOnAnimation(rv, this)
            mStarted = true
        }

        fun stop() {
            if (!mStarted) {
                return
            }
            mStarted = false
        }

        fun release() {
            mHolderRef.clear()
            mStarted = false
        }

        override fun run() {
            val holder = mHolderRef.get() ?: return
            if (!mStarted) {
                return
            }

            holder.handleScrollOnDragging()

            val rv = holder.getRecyclerView()
            if (rv != null && mStarted) {
                ViewCompat.postOnAnimation(rv, this)
            } else {
                mStarted = false
            }
        }
    }

    private class InternalHandler(holder: RecyclerViewDragDropManager) : Handler() {
        private var mHolder: RecyclerViewDragDropManager? = holder
        private var mDownMotionEvent: MotionEvent? = null

        companion object {
            private const val MSG_LONGPRESS = 1
            private const val MSG_DEFERRED_CANCEL_DRAG = 2
        }

        fun release() {
            removeCallbacksAndMessages(null)
            mHolder = null
        }

        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_LONGPRESS -> {
                    if (mDownMotionEvent != null) {
                        mHolder?.handleOnLongPress(mDownMotionEvent!!)
                    }
                }
                MSG_DEFERRED_CANCEL_DRAG -> mHolder?.cancelDrag(true)
            }
        }

        fun startLongPressDetection(e: MotionEvent, timeout: Int) {
            cancelLongPressDetection()
            mDownMotionEvent = MotionEvent.obtain(e)
            sendEmptyMessageAtTime(MSG_LONGPRESS, e.downTime + timeout)
        }

        fun cancelLongPressDetection() {
            removeMessages(MSG_LONGPRESS)
            mDownMotionEvent?.recycle()
            mDownMotionEvent = null
        }

        fun removeDeferredCancelDragRequest() {
            removeMessages(MSG_DEFERRED_CANCEL_DRAG)
        }

        fun requestDeferredCancelDrag() {
            if (isCancelDragRequested) {
                return
            }
            sendEmptyMessage(MSG_DEFERRED_CANCEL_DRAG)
        }

        val isCancelDragRequested: Boolean
            get() = hasMessages(MSG_DEFERRED_CANCEL_DRAG)
    }
}
