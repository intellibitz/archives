package intellibitz.intellidroid.widget.advrecyclerview.animator.impl

import androidx.recyclerview.widget.RecyclerView

class MoveAnimationInfo(
    @JvmField var holder: RecyclerView.ViewHolder?,
    @JvmField val fromX: Int,
    @JvmField val fromY: Int,
    @JvmField val toX: Int,
    @JvmField val toY: Int
) : ItemAnimationInfo() {

    override val availableViewHolder: RecyclerView.ViewHolder?
        get() = holder

    override fun clear(holder: RecyclerView.ViewHolder?) {
        if (this.holder == holder) {
            this.holder = null
        }
    }

    override fun toString(): String {
        return "MoveAnimationInfo{holder=$holder, fromX=$fromX, fromY=$fromY, toX=$toX, toY=$toY}"
    }
}
