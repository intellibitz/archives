package intellibitz.intellidroid.widget.advrecyclerview.utils

import android.util.Log
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import java.lang.ref.WeakReference
import java.util.Collections

open class BaseWrapperAdapter<VH : RecyclerView.ViewHolder>(
    adapter: RecyclerView.Adapter<VH>
) : RecyclerView.Adapter<VH>() {

    companion object {
        @JvmField
        protected val FULLUPDATE_PAYLOADS: List<Any> = Collections.emptyList()
        private const val TAG = "ARVBaseWrapperAdapter"
        private const val LOCAL_LOGD = false
    }

    private var mWrappedAdapter: RecyclerView.Adapter<VH>? = adapter
    private var mBridgeObserver: BridgeObserver<VH>? = BridgeObserver(this)

    init {
        mWrappedAdapter?.registerAdapterDataObserver(mBridgeObserver!!)
        super.setHasStableIds(mWrappedAdapter?.hasStableIds() == true)
    }

    fun isWrappedAdapterAlive(): Boolean {
        return mWrappedAdapter != null
    }

    fun release() {
        onRelease()
        if (mWrappedAdapter != null && mBridgeObserver != null) {
            mWrappedAdapter!!.unregisterAdapterDataObserver(mBridgeObserver!!)
        }
        mWrappedAdapter = null
        mBridgeObserver = null
    }

    protected open fun onRelease() {
        // override this method if needed
    }

    val wrappedAdapter: RecyclerView.Adapter<VH>?
        get() = mWrappedAdapter

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        if (isWrappedAdapterAlive()) {
            mWrappedAdapter?.onAttachedToRecyclerView(recyclerView)
        }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        if (isWrappedAdapterAlive()) {
            mWrappedAdapter?.onDetachedFromRecyclerView(recyclerView)
        }
    }

    override fun onViewAttachedToWindow(holder: VH) {
        if (isWrappedAdapterAlive()) {
            mWrappedAdapter?.onViewAttachedToWindow(holder)
        }
    }

    override fun onViewDetachedFromWindow(holder: VH) {
        if (isWrappedAdapterAlive()) {
            mWrappedAdapter?.onViewDetachedFromWindow(holder)
        }
    }

    override fun onViewRecycled(holder: VH) {
        if (isWrappedAdapterAlive()) {
            mWrappedAdapter?.onViewRecycled(holder)
        }
    }

    override fun setHasStableIds(hasStableIds: Boolean) {
        super.setHasStableIds(hasStableIds)
        if (isWrappedAdapterAlive()) {
            mWrappedAdapter?.setHasStableIds(hasStableIds)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return mWrappedAdapter!!.onCreateViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        onBindViewHolder(holder, position, FULLUPDATE_PAYLOADS)
    }

    override fun onBindViewHolder(holder: VH, position: Int, payloads: List<Any>) {
        if (isWrappedAdapterAlive()) {
            mWrappedAdapter?.onBindViewHolder(holder, position, payloads)
        }
    }

    override fun getItemCount(): Int {
        return if (isWrappedAdapterAlive()) mWrappedAdapter!!.itemCount else 0
    }

    override fun getItemId(position: Int): Long {
        return mWrappedAdapter?.getItemId(position) ?: RecyclerView.NO_ID
    }

    override fun getItemViewType(position: Int): Int {
        return mWrappedAdapter?.getItemViewType(position) ?: 0
    }

    protected open fun onHandleWrappedAdapterChanged() {
        notifyDataSetChanged()
    }

    protected open fun onHandleWrappedAdapterItemRangeChanged(positionStart: Int, itemCount: Int) {
        notifyItemRangeChanged(positionStart, itemCount)
    }

    protected open fun onHandleWrappedAdapterItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
        notifyItemRangeChanged(positionStart, itemCount, payload)
    }

    protected open fun onHandleWrappedAdapterItemRangeInserted(positionStart: Int, itemCount: Int) {
        notifyItemRangeInserted(positionStart, itemCount)
    }

    protected open fun onHandleWrappedAdapterItemRangeRemoved(positionStart: Int, itemCount: Int) {
        notifyItemRangeRemoved(positionStart, itemCount)
    }

    protected open fun onHandleWrappedAdapterRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
        if (itemCount != 1) {
            throw IllegalStateException("itemCount should be always 1  (actual: $itemCount)")
        }
        notifyItemMoved(fromPosition, toPosition)
    }

    internal fun onWrappedAdapterChanged() {
        if (LOCAL_LOGD) {
            Log.d(TAG, "onWrappedAdapterChanged")
        }
        onHandleWrappedAdapterChanged()
    }

    internal fun onWrappedAdapterItemRangeChanged(positionStart: Int, itemCount: Int) {
        if (LOCAL_LOGD) {
            Log.d(TAG, "onWrappedAdapterItemRangeChanged(positionStart = $positionStart, itemCount = $itemCount)")
        }
        onHandleWrappedAdapterItemRangeChanged(positionStart, itemCount)
    }

    internal fun onWrappedAdapterItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
        if (LOCAL_LOGD) {
            Log.d(TAG, "onWrappedAdapterItemRangeChanged(positionStart = $positionStart, itemCount = $itemCount, payload = $payload)")
        }
        onHandleWrappedAdapterItemRangeChanged(positionStart, itemCount, payload)
    }

    internal fun onWrappedAdapterItemRangeInserted(positionStart: Int, itemCount: Int) {
        if (LOCAL_LOGD) {
            Log.d(TAG, "onWrappedAdapterItemRangeInserted(positionStart = $positionStart, itemCount = $itemCount)")
        }
        onHandleWrappedAdapterItemRangeInserted(positionStart, itemCount)
    }

    internal fun onWrappedAdapterItemRangeRemoved(positionStart: Int, itemCount: Int) {
        if (LOCAL_LOGD) {
            Log.d(TAG, "onWrappedAdapterItemRangeRemoved(positionStart = $positionStart, itemCount = $itemCount)")
        }
        onHandleWrappedAdapterItemRangeRemoved(positionStart, itemCount)
    }

    internal fun onWrappedAdapterRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
        if (LOCAL_LOGD) {
            Log.d(TAG, "onWrappedAdapterRangeMoved(fromPosition = $fromPosition, toPosition = $toPosition, itemCount = $itemCount)")
        }
        onHandleWrappedAdapterRangeMoved(fromPosition, toPosition, itemCount)
    }

    private class BridgeObserver<VH : RecyclerView.ViewHolder>(holder: BaseWrapperAdapter<VH>) :
        RecyclerView.AdapterDataObserver() {
        private val mRefHolder: WeakReference<BaseWrapperAdapter<VH>> = WeakReference(holder)

        override fun onChanged() {
            val holder = mRefHolder.get()
            holder?.onWrappedAdapterChanged()
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
            val holder = mRefHolder.get()
            holder?.onWrappedAdapterItemRangeChanged(positionStart, itemCount)
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
            val holder = mRefHolder.get()
            holder?.onWrappedAdapterItemRangeChanged(positionStart, itemCount, payload)
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            val holder = mRefHolder.get()
            holder?.onWrappedAdapterItemRangeInserted(positionStart, itemCount)
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            val holder = mRefHolder.get()
            holder?.onWrappedAdapterItemRangeRemoved(positionStart, itemCount)
        }

        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
            val holder = mRefHolder.get()
            holder?.onWrappedAdapterRangeMoved(fromPosition, toPosition, itemCount)
        }
    }
}
