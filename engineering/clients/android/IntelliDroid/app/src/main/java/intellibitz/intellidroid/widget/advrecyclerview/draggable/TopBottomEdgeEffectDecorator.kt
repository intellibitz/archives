package intellibitz.intellidroid.widget.advrecyclerview.draggable

import androidx.recyclerview.widget.RecyclerView

internal class TopBottomEdgeEffectDecorator(recyclerView: RecyclerView) :
    BaseEdgeEffectDecorator(recyclerView) {

    override fun getEdgeDirection(no: Int): Int {
        return when (no) {
            0 -> EDGE_TOP
            1 -> EDGE_BOTTOM
            else -> throw IllegalArgumentException()
        }
    }
}
