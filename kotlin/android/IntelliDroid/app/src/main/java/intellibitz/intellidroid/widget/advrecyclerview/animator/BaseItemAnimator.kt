package intellibitz.intellidroid.widget.advrecyclerview.animator

import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator

abstract class BaseItemAnimator : SimpleItemAnimator() {
    private var mListener: ItemAnimatorListener? = null

    fun setListener(listener: ItemAnimatorListener?) {
        mListener = listener
    }

    final override fun onAddStarting(item: RecyclerView.ViewHolder) {
        onAddStartingImpl(item)
    }

    final override fun onAddFinished(item: RecyclerView.ViewHolder) {
        onAddFinishedImpl(item)
        mListener?.onAddFinished(item)
    }

    final override fun onChangeStarting(item: RecyclerView.ViewHolder, oldItem: Boolean) {
        onChangeStartingItem(item, oldItem)
    }

    final override fun onChangeFinished(item: RecyclerView.ViewHolder, oldItem: Boolean) {
        onChangeFinishedImpl(item, oldItem)
        mListener?.onChangeFinished(item)
    }

    final override fun onMoveStarting(item: RecyclerView.ViewHolder) {
        onMoveStartingImpl(item)
    }

    final override fun onMoveFinished(item: RecyclerView.ViewHolder) {
        onMoveFinishedImpl(item)
        mListener?.onMoveFinished(item)
    }

    final override fun onRemoveStarting(item: RecyclerView.ViewHolder) {
        onRemoveStartingImpl(item)
    }

    final override fun onRemoveFinished(item: RecyclerView.ViewHolder) {
        onRemoveFinishedImpl(item)
        mListener?.onRemoveFinished(item)
    }

    protected open fun onAddStartingImpl(item: RecyclerView.ViewHolder) {}
    protected open fun onAddFinishedImpl(item: RecyclerView.ViewHolder) {}
    protected open fun onChangeStartingItem(item: RecyclerView.ViewHolder, oldItem: Boolean) {}
    protected open fun onChangeFinishedImpl(item: RecyclerView.ViewHolder, oldItem: Boolean) {}
    protected open fun onMoveStartingImpl(item: RecyclerView.ViewHolder) {}
    protected open fun onMoveFinishedImpl(item: RecyclerView.ViewHolder) {}
    protected open fun onRemoveStartingImpl(item: RecyclerView.ViewHolder) {}
    protected open fun onRemoveFinishedImpl(item: RecyclerView.ViewHolder) {}

    open fun dispatchFinishedWhenDone(): Boolean {
        return if (!isRunning) {
            dispatchAnimationsFinished()
            true
        } else {
            false
        }
    }

    open fun debugLogEnabled(): Boolean {
        return false
    }

    interface ItemAnimatorListener {
        fun onRemoveFinished(item: RecyclerView.ViewHolder?)
        fun onAddFinished(item: RecyclerView.ViewHolder?)
        fun onMoveFinished(item: RecyclerView.ViewHolder?)
        fun onChangeFinished(item: RecyclerView.ViewHolder?)
    }
}
