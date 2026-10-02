package intellibitz.intellidroid.widget.advrecyclerview.touchguard

import android.util.Log
import android.view.MotionEvent
import android.view.ViewConfiguration
import androidx.core.view.MotionEventCompat
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

class RecyclerViewTouchActionGuardManager {
    companion object {
        private const val TAG = "ARVTouchActionGuardMgr"
        private const val LOCAL_LOGV = false
        private const val LOCAL_LOGD = false

        private fun isAnimationRunning(rv: RecyclerView): Boolean {
            val itemAnimator = rv.itemAnimator
            return itemAnimator != null && itemAnimator.isRunning
        }
    }

    private var mInternalUseOnItemTouchListener: RecyclerView.OnItemTouchListener? = null
    private var mRecyclerView: RecyclerView? = null
    private var mGuarding = false
    private var mInitialTouchY = 0
    private var mLastTouchY = 0
    private var mTouchSlop = 0
    private var mEnabled = false
    private var mInterceptScrollingWhileAnimationRunning = false

    init {
        mInternalUseOnItemTouchListener = object : RecyclerView.OnItemTouchListener {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                return this@RecyclerViewTouchActionGuardManager.onInterceptTouchEvent(rv, e)
            }

            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
                this@RecyclerViewTouchActionGuardManager.onTouchEvent(rv, e)
            }

            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
            }
        }
    }

    val isReleased: Boolean
        get() = mInternalUseOnItemTouchListener == null

    fun attachRecyclerView(rv: RecyclerView) {
        if (isReleased) {
            throw IllegalStateException("Accessing released object")
        }
        if (mRecyclerView != null) {
            throw IllegalStateException("RecyclerView instance has already been set")
        }
        mRecyclerView = rv
        mRecyclerView!!.addOnItemTouchListener(mInternalUseOnItemTouchListener!!)
        mTouchSlop = ViewConfiguration.get(rv.context).scaledTouchSlop
    }

    fun release() {
        if (mRecyclerView != null && mInternalUseOnItemTouchListener != null) {
            mRecyclerView!!.removeOnItemTouchListener(mInternalUseOnItemTouchListener!!)
        }
        mInternalUseOnItemTouchListener = null
        mRecyclerView = null
    }

    internal fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
        if (!mEnabled) {
            return false
        }
        val action = MotionEventCompat.getActionMasked(e)
        if (LOCAL_LOGV) {
            Log.v(TAG, "onInterceptTouchEvent() action = $action")
        }
        when (action) {
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> handleActionUpOrCancel()
            MotionEvent.ACTION_DOWN -> handleActionDown(e)
            MotionEvent.ACTION_MOVE -> if (handleActionMove(rv, e)) {
                return true
            }
        }
        return false
    }

    internal fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
        if (!mEnabled) {
            return
        }
        val action = MotionEventCompat.getActionMasked(e)
        if (LOCAL_LOGV) {
            Log.v(TAG, "onTouchEvent() action = $action")
        }
        when (action) {
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> handleActionUpOrCancel()
        }
    }

    private fun handleActionMove(rv: RecyclerView, e: MotionEvent): Boolean {
        if (!mGuarding) {
            mLastTouchY = (e.y + 0.5f).toInt()
            val distance = mLastTouchY - mInitialTouchY
            if (mInterceptScrollingWhileAnimationRunning && (abs(distance) > mTouchSlop) && isAnimationRunning(rv)) {
                mGuarding = true
            }
        }
        return mGuarding
    }

    private fun handleActionUpOrCancel() {
        mGuarding = false
        mInitialTouchY = 0
        mLastTouchY = 0
    }

    private fun handleActionDown(e: MotionEvent) {
        mLastTouchY = (e.y + 0.5f).toInt()
        mInitialTouchY = mLastTouchY
        mGuarding = false
    }

    var isEnabled: Boolean
        get() = mEnabled
        set(enabled) {
            if (mEnabled == enabled) {
                return
            }
            mEnabled = enabled
            if (!mEnabled) {
                handleActionUpOrCancel()
            }
        }

    fun isInterceptScrollingWhileAnimationRunning(): Boolean {
        return mInterceptScrollingWhileAnimationRunning
    }

    fun setInterceptVerticalScrollingWhileAnimationRunning(enabled: Boolean) {
        mInterceptScrollingWhileAnimationRunning = enabled
    }
}
