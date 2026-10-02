package intellibitz.intellidroid.widget.advrecyclerview.animator.impl

import androidx.recyclerview.widget.RecyclerView

class ChangeAnimationInfo(
    @JvmField var oldHolder: RecyclerView.ViewHolder?,
    @JvmField var newHolder: RecyclerView.ViewHolder?,
    @JvmField var fromX: Int,
    @JvmField var fromY: Int,
    @JvmField var toX: Int,
    @JvmField var toY: Int
) : ItemAnimationInfo() {

    override val availableViewHolder: RecyclerView.ViewHolder?
        get() = oldHolder ?: newHolder

    override fun clear(holder: RecyclerView.ViewHolder?) {
        if (oldHolder == holder) {
            oldHolder = null
        }
        if (newHolder == holder) {
            newHolder = null
        }
        if (oldHolder == null && newHolder == null) {
            fromX = 0
            fromY = 0
            toX = 0
            toY = 0
        }
    }

    override fun toString(): String {
        return "ChangeAnimationInfo{oldHolder=$oldHolder, newHolder=$newHolder, fromX=$fromX, fromY=$fromY, toX=$toX, toY=$toY}"
    }
}
