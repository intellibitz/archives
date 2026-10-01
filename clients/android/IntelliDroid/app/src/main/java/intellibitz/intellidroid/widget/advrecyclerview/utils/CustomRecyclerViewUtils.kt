package intellibitz.intellidroid.widget.advrecyclerview.utils

import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

class CustomRecyclerViewUtils {
    companion object {
        const val ORIENTATION_UNKNOWN = -1
        const val ORIENTATION_HORIZONTAL = OrientationHelper.HORIZONTAL // = 0
        const val ORIENTATION_VERTICAL = OrientationHelper.VERTICAL // = 1

        const val LAYOUT_TYPE_UNKNOWN = -1
        const val LAYOUT_TYPE_LINEAR_HORIZONTAL = 0
        const val LAYOUT_TYPE_LINEAR_VERTICAL = 1
        const val LAYOUT_TYPE_GRID_HORIZONTAL = 2
        const val LAYOUT_TYPE_GRID_VERTICAL = 3
        const val LAYOUT_TYPE_STAGGERED_GRID_HORIZONTAL = 4
        const val LAYOUT_TYPE_STAGGERED_GRID_VERTICAL = 5

        const val INVALID_SPAN_ID = -1
        const val INVALID_SPAN_COUNT = -1

        @JvmStatic
        fun findChildViewHolderUnderWithoutTranslation(rv: RecyclerView, x: Float, y: Float): RecyclerView.ViewHolder? {
            val child = findChildViewUnderWithoutTranslation(rv, x, y)
            return if (child != null) rv.getChildViewHolder(child) else null
        }

        @JvmStatic
        fun getLayoutType(rv: RecyclerView): Int {
            return getLayoutType(rv.layoutManager)
        }

        @JvmStatic
        fun extractOrientation(layoutType: Int): Int {
            return when (layoutType) {
                LAYOUT_TYPE_UNKNOWN -> ORIENTATION_UNKNOWN
                LAYOUT_TYPE_LINEAR_HORIZONTAL,
                LAYOUT_TYPE_GRID_HORIZONTAL,
                LAYOUT_TYPE_STAGGERED_GRID_HORIZONTAL -> ORIENTATION_HORIZONTAL
                LAYOUT_TYPE_LINEAR_VERTICAL,
                LAYOUT_TYPE_GRID_VERTICAL,
                LAYOUT_TYPE_STAGGERED_GRID_VERTICAL -> ORIENTATION_VERTICAL
                else -> throw IllegalArgumentException("Unknown layout type (= $layoutType)")
            }
        }

        @JvmStatic
        fun getLayoutType(layoutManager: RecyclerView.LayoutManager?): Int {
            return if (layoutManager is GridLayoutManager) {
                if (layoutManager.orientation == GridLayoutManager.HORIZONTAL) {
                    LAYOUT_TYPE_GRID_HORIZONTAL
                } else {
                    LAYOUT_TYPE_GRID_VERTICAL
                }
            } else if (layoutManager is LinearLayoutManager) {
                if (layoutManager.orientation == LinearLayoutManager.HORIZONTAL) {
                    LAYOUT_TYPE_LINEAR_HORIZONTAL
                } else {
                    LAYOUT_TYPE_LINEAR_VERTICAL
                }
            } else if (layoutManager is StaggeredGridLayoutManager) {
                if (layoutManager.orientation == StaggeredGridLayoutManager.HORIZONTAL) {
                    LAYOUT_TYPE_STAGGERED_GRID_HORIZONTAL
                } else {
                    LAYOUT_TYPE_STAGGERED_GRID_VERTICAL
                }
            } else {
                LAYOUT_TYPE_UNKNOWN
            }
        }

        private fun findChildViewUnderWithoutTranslation(parent: ViewGroup, x: Float, y: Float): View? {
            val count = parent.childCount
            for (i in count - 1 downTo 0) {
                val child = parent.getChildAt(i)
                if (x >= child.left && x <= child.right && y >= child.top && y <= child.bottom) {
                    return child
                }
            }
            return null
        }

        @JvmStatic
        fun findChildViewHolderUnderWithTranslation(rv: RecyclerView, x: Float, y: Float): RecyclerView.ViewHolder? {
            val child = rv.findChildViewUnder(x, y)
            return if (child != null) rv.getChildViewHolder(child) else null
        }

        @JvmStatic
        fun getLayoutMargins(v: View, outMargins: Rect): Rect {
            val layoutParams = v.layoutParams
            if (layoutParams is ViewGroup.MarginLayoutParams) {
                outMargins.left = layoutParams.leftMargin
                outMargins.right = layoutParams.rightMargin
                outMargins.top = layoutParams.topMargin
                outMargins.bottom = layoutParams.bottomMargin
            } else {
                outMargins.bottom = 0
                outMargins.top = 0
                outMargins.right = 0
                outMargins.left = 0
            }
            return outMargins
        }

        @JvmStatic
        fun getDecorationOffsets(layoutManager: RecyclerView.LayoutManager, view: View, outDecorations: Rect): Rect {
            outDecorations.left = layoutManager.getLeftDecorationWidth(view)
            outDecorations.right = layoutManager.getRightDecorationWidth(view)
            outDecorations.top = layoutManager.getTopDecorationHeight(view)
            outDecorations.bottom = layoutManager.getBottomDecorationHeight(view)
            return outDecorations
        }

        @JvmStatic
        fun getViewBounds(v: View, outBounds: Rect): Rect {
            outBounds.left = v.left
            outBounds.right = v.right
            outBounds.top = v.top
            outBounds.bottom = v.bottom
            return outBounds
        }

        @JvmStatic
        fun findFirstVisibleItemPosition(rv: RecyclerView, includesPadding: Boolean): Int {
            val layoutManager = rv.layoutManager
            return if (layoutManager is LinearLayoutManager) {
                if (includesPadding) {
                    findFirstVisibleItemPositionIncludesPadding(layoutManager)
                } else {
                    layoutManager.findFirstVisibleItemPosition()
                }
            } else {
                RecyclerView.NO_POSITION
            }
        }

        @JvmStatic
        fun findLastVisibleItemPosition(rv: RecyclerView, includesPadding: Boolean): Int {
            val layoutManager = rv.layoutManager
            return if (layoutManager is LinearLayoutManager) {
                if (includesPadding) {
                    findLastVisibleItemPositionIncludesPadding(layoutManager)
                } else {
                    layoutManager.findLastVisibleItemPosition()
                }
            } else {
                RecyclerView.NO_POSITION
            }
        }

        @JvmStatic
        fun findFirstCompletelyVisibleItemPosition(rv: RecyclerView): Int {
            val layoutManager = rv.layoutManager
            return if (layoutManager is LinearLayoutManager) {
                layoutManager.findFirstCompletelyVisibleItemPosition()
            } else {
                RecyclerView.NO_POSITION
            }
        }

        @JvmStatic
        fun findLastCompletelyVisibleItemPosition(rv: RecyclerView): Int {
            val layoutManager = rv.layoutManager
            return if (layoutManager is LinearLayoutManager) {
                layoutManager.findLastCompletelyVisibleItemPosition()
            } else {
                RecyclerView.NO_POSITION
            }
        }

        @JvmStatic
        fun getSynchronizedPosition(holder: RecyclerView.ViewHolder): Int {
            val pos1 = holder.layoutPosition
            val pos2 = holder.adapterPosition
            return if (pos1 == pos2) {
                pos1
            } else {
                RecyclerView.NO_POSITION
            }
        }

        @JvmStatic
        fun getSpanCount(rv: RecyclerView): Int {
            val layoutManager = rv.layoutManager
            return if (layoutManager is GridLayoutManager) {
                layoutManager.spanCount
            } else if (layoutManager is StaggeredGridLayoutManager) {
                layoutManager.spanCount
            } else {
                1
            }
        }

        @JvmStatic
        fun getOrientation(rv: RecyclerView): Int {
            return getOrientation(rv.layoutManager)
        }

        @JvmStatic
        fun getOrientation(layoutManager: RecyclerView.LayoutManager?): Int {
            return if (layoutManager is GridLayoutManager) {
                layoutManager.orientation
            } else if (layoutManager is LinearLayoutManager) {
                layoutManager.orientation
            } else if (layoutManager is StaggeredGridLayoutManager) {
                layoutManager.orientation
            } else {
                ORIENTATION_UNKNOWN
            }
        }

        private fun findFirstVisibleItemPositionIncludesPadding(lm: LinearLayoutManager): Int {
            val child = findOneVisibleChildIncludesPadding(lm, 0, lm.childCount, false, true)
            return if (child == null) RecyclerView.NO_POSITION else lm.getPosition(child)
        }

        private fun findLastVisibleItemPositionIncludesPadding(lm: LinearLayoutManager): Int {
            val child = findOneVisibleChildIncludesPadding(lm, lm.childCount - 1, -1, false, true)
            return if (child == null) RecyclerView.NO_POSITION else lm.getPosition(child)
        }

        private fun findOneVisibleChildIncludesPadding(
            lm: LinearLayoutManager, fromIndex: Int, toIndex: Int,
            completelyVisible: Boolean, acceptPartiallyVisible: Boolean
        ): View? {
            val isVertical = (lm.orientation == LinearLayoutManager.VERTICAL)
            val start = 0
            val end = if (isVertical) lm.height else lm.width
            val next = if (toIndex > fromIndex) 1 else -1
            var partiallyVisible: View? = null
            var i = fromIndex
            while (i != toIndex) {
                val child = lm.getChildAt(i)
                if (child != null) {
                    val childStart = if (isVertical) child.top else child.left
                    val childEnd = if (isVertical) child.bottom else child.right
                    if (childStart < end && childEnd > start) {
                        if (completelyVisible) {
                            if (childStart >= start && childEnd <= end) {
                                return child
                            } else if (acceptPartiallyVisible && partiallyVisible == null) {
                                partiallyVisible = child
                            }
                        } else {
                            return child
                        }
                    }
                }
                i += next
            }
            return partiallyVisible
        }

        @JvmStatic
        fun safeGetAdapterPosition(holder: RecyclerView.ViewHolder?): Int {
            return holder?.adapterPosition ?: RecyclerView.NO_POSITION
        }

        @JvmStatic
        fun safeGetLayoutPosition(holder: RecyclerView.ViewHolder?): Int {
            return holder?.layoutPosition ?: RecyclerView.NO_POSITION
        }

        @JvmStatic
        fun findViewByPosition(layoutManager: RecyclerView.LayoutManager, position: Int): View? {
            return if (position != RecyclerView.NO_POSITION) layoutManager.findViewByPosition(position) else null
        }

        @JvmStatic
        fun getSpanIndex(holder: RecyclerView.ViewHolder?): Int {
            val itemView = getLaidOutItemView(holder) ?: return INVALID_SPAN_ID
            val lp = itemView.layoutParams
            return when (lp) {
                is StaggeredGridLayoutManager.LayoutParams -> lp.spanIndex
                is GridLayoutManager.LayoutParams -> lp.spanIndex
                is RecyclerView.LayoutParams -> 0
                else -> INVALID_SPAN_ID
            }
        }

        @JvmStatic
        fun getSpanSize(holder: RecyclerView.ViewHolder?): Int {
            val itemView = getLaidOutItemView(holder) ?: return INVALID_SPAN_COUNT
            val lp = itemView.layoutParams
            return when (lp) {
                is StaggeredGridLayoutManager.LayoutParams -> {
                    val isFullSpan = lp.isFullSpan
                    if (isFullSpan) {
                        val rv = itemView.parent as RecyclerView
                        getSpanCount(rv)
                    } else {
                        1
                    }
                }
                is GridLayoutManager.LayoutParams -> lp.spanSize
                is RecyclerView.LayoutParams -> 1
                else -> INVALID_SPAN_COUNT
            }
        }

        @JvmStatic
        fun isFullSpan(holder: RecyclerView.ViewHolder?): Boolean {
            val itemView = getLaidOutItemView(holder) ?: return true
            val lp = itemView.layoutParams
            return when (lp) {
                is StaggeredGridLayoutManager.LayoutParams -> lp.isFullSpan
                is GridLayoutManager.LayoutParams -> {
                    val rv = itemView.parent as RecyclerView
                    val spanCount = getSpanCount(rv)
                    val spanSize = lp.spanSize
                    spanCount == spanSize
                }
                is RecyclerView.LayoutParams -> true
                else -> true
            }
        }

        private fun getLaidOutItemView(holder: RecyclerView.ViewHolder?): View? {
            if (holder == null) {
                return null
            }
            val itemView = holder.itemView
            return if (!ViewCompat.isLaidOut(itemView)) {
                null
            } else itemView
        }

        @JvmStatic
        fun isLinearLayout(layoutType: Int): Boolean {
            return layoutType == LAYOUT_TYPE_LINEAR_VERTICAL || layoutType == LAYOUT_TYPE_LINEAR_HORIZONTAL
        }

        @JvmStatic
        fun isGridLayout(layoutType: Int): Boolean {
            return layoutType == LAYOUT_TYPE_GRID_VERTICAL || layoutType == LAYOUT_TYPE_GRID_HORIZONTAL
        }

        @JvmStatic
        fun isStaggeredGridLayout(layoutType: Int): Boolean {
            return layoutType == LAYOUT_TYPE_STAGGERED_GRID_VERTICAL || layoutType == LAYOUT_TYPE_STAGGERED_GRID_HORIZONTAL
        }
    }
}
