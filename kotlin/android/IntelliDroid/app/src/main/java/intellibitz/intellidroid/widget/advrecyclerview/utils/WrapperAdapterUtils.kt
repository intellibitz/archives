package intellibitz.intellidroid.widget.advrecyclerview.utils

import androidx.recyclerview.widget.RecyclerView

object WrapperAdapterUtils {

    @JvmStatic
    fun <T> findWrappedAdapter(adapter: RecyclerView.Adapter<*>?, clazz: Class<T>): T? {
        if (adapter == null) return null
        if (clazz.isInstance(adapter)) {
            return clazz.cast(adapter)
        } else if (adapter is BaseWrapperAdapter<*>) {
            val wrappedAdapter = adapter.wrappedAdapter
            return findWrappedAdapter(wrappedAdapter, clazz)
        } else {
            return null
        }
    }

    @JvmStatic
    fun releaseAll(adapter: RecyclerView.Adapter<*>?): RecyclerView.Adapter<*>? {
        return releaseCyclically(adapter)
    }

    private fun releaseCyclically(adapter: RecyclerView.Adapter<*>?): RecyclerView.Adapter<*>? {
        if (adapter !is BaseWrapperAdapter<*>) {
            return adapter
        }

        val wrapperAdapter = adapter as BaseWrapperAdapter<*>
        val wrappedAdapter = wrapperAdapter.wrappedAdapter

        wrapperAdapter.release()

        return releaseCyclically(wrappedAdapter)
    }
}
