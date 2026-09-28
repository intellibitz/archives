package intellibitz.intellidroid.widget.advrecyclerview.animator.impl

import androidx.recyclerview.widget.RecyclerView

class RemoveAnimationInfo(@JvmField var holder: RecyclerView.ViewHolder?) : ItemAnimationInfo() {

    override val availableViewHolder: RecyclerView.ViewHolder?
        get() = holder

    override fun clear(holder: RecyclerView.ViewHolder?) {
        if (this.holder == holder) {
            this.holder = null
        }
    }

    override fun toString(): String {
        return "RemoveAnimationInfo{holder=$holder}"
    }
}
