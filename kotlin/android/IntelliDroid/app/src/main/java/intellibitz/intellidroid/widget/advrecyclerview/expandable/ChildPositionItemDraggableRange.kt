package intellibitz.intellidroid.widget.advrecyclerview.expandable

import intellibitz.intellidroid.widget.advrecyclerview.draggable.ItemDraggableRange

open class ChildPositionItemDraggableRange(start: Int, end: Int) : ItemDraggableRange(start, end) {
    override fun getClassName(): String {
        return "ChildPositionItemDraggableRange"
    }
}
