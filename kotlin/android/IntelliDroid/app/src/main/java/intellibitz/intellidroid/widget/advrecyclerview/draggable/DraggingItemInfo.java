

package intellibitz.intellidroid.widget.advrecyclerview.draggable;

import android.graphics.Rect;

import intellibitz.intellidroid.widget.advrecyclerview.utils.CustomRecyclerViewUtils;

import androidx.recyclerview.widget.RecyclerView;

public class DraggingItemInfo {
    public final int width;
    public final int height;
    public final long id;
    public final int initialItemLeft;
    public final int initialItemTop;
    public final int grabbedPositionX;
    public final int grabbedPositionY;
    public final Rect margins;
    public final int spanSize;

    public DraggingItemInfo(RecyclerView rv, RecyclerView.ViewHolder vh, int touchX, int touchY) {
        width = vh.itemView.getWidth();
        height = vh.itemView.getHeight();
        id = vh.getItemId();
        initialItemLeft = vh.itemView.getLeft();
        initialItemTop = vh.itemView.getTop();
        grabbedPositionX = touchX - initialItemLeft;
        grabbedPositionY = touchY - initialItemTop;
        margins = new Rect();
        CustomRecyclerViewUtils.getLayoutMargins(vh.itemView, margins);
        spanSize = CustomRecyclerViewUtils.getSpanSize(vh);
    }
}
