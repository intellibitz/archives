

package intellibitz.intellidroid.widget.advrecyclerview.expandable;

import intellibitz.intellidroid.widget.advrecyclerview.draggable.ItemDraggableRange;

public class GroupPositionItemDraggableRange extends ItemDraggableRange {
    public GroupPositionItemDraggableRange(int start, int end) {
        super(start, end);
    }

    protected String getClassName() {
        return "GroupPositionItemDraggableRange";
    }
}
