package intellibitz.intellidroid.widget.advrecyclerview.draggable

import androidx.recyclerview.widget.RecyclerView

internal class LeftRightEdgeEffectDecorator(recyclerView: RecyclerView) :
    BaseEdgeEffectDecorator(recyclerView) {

    override fun getEdgeDirection(no: Int): Int {
        return when (no) {
            0 -> EDGE_LEFT
            1 -> EDGE_RIGHT
            else -> throw IllegalArgumentException()
        }
    }
}
