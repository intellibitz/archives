package intellibitz.intellidroid.widget.advrecyclerview.draggable

import android.graphics.Rect
import androidx.recyclerview.widget.RecyclerView
import intellibitz.intellidroid.widget.advrecyclerview.utils.CustomRecyclerViewUtils

class DraggingItemInfo(rv: RecyclerView, vh: RecyclerView.ViewHolder, touchX: Int, touchY: Int) {
    @JvmField
    val width: Int = vh.itemView.width
    @JvmField
    val height: Int = vh.itemView.height
    @JvmField
    val id: Long = vh.itemId
    @JvmField
    val initialItemLeft: Int = vh.itemView.left
    @JvmField
    val initialItemTop: Int = vh.itemView.top
    @JvmField
    val grabbedPositionX: Int = touchX - initialItemLeft
    @JvmField
    val grabbedPositionY: Int = touchY - initialItemTop
    @JvmField
    val margins: Rect = Rect()
    @JvmField
    val spanSize: Int

    init {
        CustomRecyclerViewUtils.getLayoutMargins(vh.itemView, margins)
        spanSize = CustomRecyclerViewUtils.getSpanSize(vh)
    }
}
