package intellibitz.intellidroid.widget.advrecyclerview.event

import androidx.recyclerview.widget.RecyclerView
import java.util.ArrayList

abstract class BaseRecyclerViewEventDistributor<T> {
    @JvmField
    protected var mReleased = false
    @JvmField
    protected var mRecyclerView: RecyclerView? = null
    @JvmField
    protected var mListeners: MutableList<T>? = null
    @JvmField
    protected var mPerformingClearMethod = false

    val recyclerView: RecyclerView?
        get() = mRecyclerView

    fun release() {
        if (mReleased) {
            return
        }
        mReleased = true
        clear(true)
        onRelease()
    }

    val isReleased: Boolean
        get() = mReleased

    open fun attachRecyclerView(rv: RecyclerView?) {
        val methodName = "attachRecyclerView()"
        if (rv == null) {
            throw IllegalArgumentException("RecyclerView cannot be null")
        }
        verifyIsNotReleased(methodName)
        verifyIsNotPerformingClearMethod(methodName)
        onRecyclerViewAttached(rv)
    }

    fun add(listener: T): Boolean {
        return add(listener, -1)
    }

    fun add(listener: T, index: Int): Boolean {
        val methodName = "add()"
        verifyIsNotReleased(methodName)
        verifyIsNotPerformingClearMethod(methodName)

        if (mListeners == null) {
            mListeners = ArrayList()
        }

        if (!mListeners!!.contains(listener)) {
            if (index < 0) {
                mListeners!!.add(listener)
            } else {
                mListeners!!.add(index, listener)
            }
            if (listener is RecyclerViewEventDistributorListener) {
                listener.onAddedToEventDistributor(this)
            }
        }
        return true
    }

    fun remove(listener: T): Boolean {
        val methodName = "remove()"
        verifyIsNotPerformingClearMethod(methodName)
        verifyIsNotReleased(methodName)

        if (mListeners == null) {
            return false
        }

        val removed = mListeners!!.remove(listener)
        if (removed) {
            if (listener is RecyclerViewEventDistributorListener) {
                listener.onRemovedFromEventDistributor(this)
            }
        }
        return removed
    }

    fun clear() {
        clear(false)
    }

    protected open fun clear(calledFromRelease: Boolean) {
        val methodName = "clear()"
        if (!calledFromRelease) {
            verifyIsNotReleased(methodName)
        }
        verifyIsNotPerformingClearMethod(methodName)

        if (mListeners == null) {
            return
        }

        try {
            mPerformingClearMethod = true
            val n = mListeners!!.size
            for (i in n - 1 downTo 0) {
                val listener = mListeners!!.removeAt(i)
                if (listener is RecyclerViewEventDistributorListener) {
                    listener.onRemovedFromEventDistributor(this)
                }
            }
        } finally {
            mPerformingClearMethod = false
        }
    }

    fun size(): Int {
        return mListeners?.size ?: 0
    }

    fun contains(listener: T): Boolean {
        return mListeners?.contains(listener) ?: false
    }

    protected open fun onRelease() {
        mRecyclerView = null
        mListeners = null
        mPerformingClearMethod = false
    }

    protected open fun onRecyclerViewAttached(rv: RecyclerView) {
        mRecyclerView = rv
    }

    protected fun verifyIsNotPerformingClearMethod(methodName: String) {
        if (mPerformingClearMethod) {
            throw IllegalStateException("$methodName can not be called while performing the clear() method")
        }
    }

    protected fun verifyIsNotReleased(methodName: String) {
        if (mReleased) {
            throw IllegalStateException("$methodName can not be called after release() method called")
        }
    }
}
