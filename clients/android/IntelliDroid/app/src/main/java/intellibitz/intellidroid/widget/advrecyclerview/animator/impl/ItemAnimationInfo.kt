package intellibitz.intellidroid.widget.advrecyclerview.animator.impl

import androidx.recyclerview.widget.RecyclerView

abstract class ItemAnimationInfo {
    abstract val availableViewHolder: RecyclerView.ViewHolder?
    abstract fun clear(holder: RecyclerView.ViewHolder?)
}
