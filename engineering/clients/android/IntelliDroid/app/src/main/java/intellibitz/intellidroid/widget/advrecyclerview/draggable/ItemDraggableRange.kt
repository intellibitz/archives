package intellibitz.intellidroid.widget.advrecyclerview.draggable

open class ItemDraggableRange(val start: Int, val end: Int) {

    init {
        require(start <= end) { "end position (= $end) is smaller than start position (=$start)" }
    }

    fun checkInRange(position: Int): Boolean {
        return position in start..end
    }

    protected open fun getClassName(): String {
        return "ItemDraggableRange"
    }

    override fun toString(): String {
        return "${getClassName()}{mStart=$start, mEnd=$end}"
    }
}
