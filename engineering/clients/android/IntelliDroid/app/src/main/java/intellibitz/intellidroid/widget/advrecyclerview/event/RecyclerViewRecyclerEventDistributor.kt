package intellibitz.intellidroid.widget.advrecyclerview.event

import androidx.recyclerview.widget.RecyclerView
import java.lang.ref.WeakReference

class RecyclerViewRecyclerEventDistributor :
    BaseRecyclerViewEventDistributor<RecyclerView.RecyclerListener>() {

    private var mInternalRecyclerListener: InternalRecyclerListener? =
        InternalRecyclerListener(this)

    override fun onRecyclerViewAttached(rv: RecyclerView) {
        super.onRecyclerViewAttached(rv)
        rv.setRecyclerListener(mInternalRecyclerListener)
    }

    override fun onRelease() {
        super.onRelease()
        if (mInternalRecyclerListener != null) {
            mInternalRecyclerListener!!.release()
            mInternalRecyclerListener = null
        }
    }

    internal fun handleOnViewRecycled(holder: RecyclerView.ViewHolder) {
        val listeners = mListeners ?: return
        for (listener in listeners) {
            listener.onViewRecycled(holder)
        }
    }

    private class InternalRecyclerListener(distributor: RecyclerViewRecyclerEventDistributor) :
        RecyclerView.RecyclerListener {
        private val mRefDistributor: WeakReference<RecyclerViewRecyclerEventDistributor> =
            WeakReference(distributor)

        override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
            val distributor = mRefDistributor.get()
            distributor?.handleOnViewRecycled(holder)
        }

        fun release() {
            mRefDistributor.clear()
        }
    }
}
