package intellibitz.intellidroid.widget.advrecyclerview.animator

import android.util.Log
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import intellibitz.intellidroid.widget.advrecyclerview.animator.impl.ItemAddAnimationManager
import intellibitz.intellidroid.widget.advrecyclerview.animator.impl.ItemChangeAnimationManager
import intellibitz.intellidroid.widget.advrecyclerview.animator.impl.ItemMoveAnimationManager
import intellibitz.intellidroid.widget.advrecyclerview.animator.impl.ItemRemoveAnimationManager
import kotlin.math.max

abstract class GeneralItemAnimator protected constructor() : BaseItemAnimator() {

    private var mDebug = false
    private var mRemoveAnimationManager: ItemRemoveAnimationManager? = null
    private var mAddAnimationsManager: ItemAddAnimationManager? = null
    private var mChangeAnimationsManager: ItemChangeAnimationManager? = null
    private var mMoveAnimationsManager: ItemMoveAnimationManager? = null

    init {
        setup()
    }

    private fun setup() {
        onSetup()

        check(
            !(mRemoveAnimationManager == null ||
                    mAddAnimationsManager == null ||
                    mChangeAnimationsManager == null ||
                    mMoveAnimationsManager == null)
        ) {
            "setup incomplete"
        }
    }

    protected abstract fun onSetup()

    override fun runPendingAnimations() {
        if (!hasPendingAnimations()) {
            return
        }
        onSchedulePendingAnimations()
    }

    override fun animateRemove(holder: RecyclerView.ViewHolder): Boolean {
        if (mDebug) {
            Log.d(TAG, "animateRemove(id = ${holder.itemId}, position = ${holder.layoutPosition})")
        }
        return mRemoveAnimationManager!!.addPendingAnimation(holder)
    }

    override fun animateAdd(holder: RecyclerView.ViewHolder): Boolean {
        if (mDebug) {
            Log.d(TAG, "animateAdd(id = ${holder.itemId}, position = ${holder.layoutPosition})")
        }
        return mAddAnimationsManager!!.addPendingAnimation(holder)
    }

    override fun animateMove(holder: RecyclerView.ViewHolder, fromX: Int, fromY: Int, toX: Int, toY: Int): Boolean {
        if (mDebug) {
            Log.d(TAG, "animateMove(id = ${holder.itemId}, position = ${holder.layoutPosition}, fromX = $fromX, fromY = $fromY, toX = $toX, toY = $toY)")
        }
        return mMoveAnimationsManager!!.addPendingAnimation(holder, fromX, fromY, toX, toY)
    }

    override fun animateChange(
        oldHolder: RecyclerView.ViewHolder,
        newHolder: RecyclerView.ViewHolder?,
        fromX: Int,
        fromY: Int,
        toX: Int,
        toY: Int
    ): Boolean {
        if (oldHolder === newHolder) {
            return mMoveAnimationsManager!!.addPendingAnimation(oldHolder, fromX, fromY, toX, toY)
        }

        if (mDebug) {
            val oldId = oldHolder.itemId.toString()
            val oldPosition = oldHolder.layoutPosition.toString()
            val newId = newHolder?.itemId?.toString() ?: "-"
            val newPosition = newHolder?.layoutPosition?.toString() ?: "-"

            Log.d(
                TAG,
                "animateChange(old.id = $oldId, old.position = $oldPosition, new.id = $newId, new.position = $newPosition, fromX = $fromX, fromY = $fromY, toX = $toX, toY = $toY)"
            )
        }

        return mChangeAnimationsManager!!.addPendingAnimation(oldHolder, newHolder, fromX, fromY, toX, toY)
    }

    protected open fun cancelAnimations(item: RecyclerView.ViewHolder) {
        ViewCompat.animate(item.itemView).cancel()
    }

    override fun endAnimation(item: RecyclerView.ViewHolder) {
        cancelAnimations(item)

        mMoveAnimationsManager!!.endPendingAnimations(item)
        mChangeAnimationsManager!!.endPendingAnimations(item)
        mRemoveAnimationManager!!.endPendingAnimations(item)
        mAddAnimationsManager!!.endPendingAnimations(item)

        mMoveAnimationsManager!!.endDeferredReadyAnimations(item)
        mChangeAnimationsManager!!.endDeferredReadyAnimations(item)
        mRemoveAnimationManager!!.endDeferredReadyAnimations(item)
        mAddAnimationsManager!!.endDeferredReadyAnimations(item)

        if (mRemoveAnimationManager!!.removeFromActive(item) && mDebug) {
            throw IllegalStateException("after animation is cancelled, item should not be in the active animation list [remove]")
        }

        if (mAddAnimationsManager!!.removeFromActive(item) && mDebug) {
            throw IllegalStateException("after animation is cancelled, item should not be in the active animation list [add]")
        }

        if (mChangeAnimationsManager!!.removeFromActive(item) && mDebug) {
            throw IllegalStateException("after animation is cancelled, item should not be in the active animation list [change]")
        }

        if (mMoveAnimationsManager!!.removeFromActive(item) && mDebug) {
            throw IllegalStateException("after animation is cancelled, item should not be in the active animation list [move]")
        }

        dispatchFinishedWhenDone()
    }

    override fun isRunning(): Boolean {
        return (mRemoveAnimationManager!!.isRunning ||
                mAddAnimationsManager!!.isRunning ||
                mChangeAnimationsManager!!.isRunning ||
                mMoveAnimationsManager!!.isRunning)
    }

    override fun endAnimations() {
        mMoveAnimationsManager!!.endAllPendingAnimations()
        mRemoveAnimationManager!!.endAllPendingAnimations()
        mAddAnimationsManager!!.endAllPendingAnimations()
        mChangeAnimationsManager!!.endAllPendingAnimations()

        if (!isRunning) {
            return
        }

        mMoveAnimationsManager!!.endAllDeferredReadyAnimations()
        mAddAnimationsManager!!.endAllDeferredReadyAnimations()
        mChangeAnimationsManager!!.endAllDeferredReadyAnimations()

        mRemoveAnimationManager!!.cancelAllStartedAnimations()
        mMoveAnimationsManager!!.cancelAllStartedAnimations()
        mAddAnimationsManager!!.cancelAllStartedAnimations()
        mChangeAnimationsManager!!.cancelAllStartedAnimations()

        dispatchAnimationsFinished()
    }

    override fun debugLogEnabled(): Boolean {
        return mDebug
    }

    override fun dispatchFinishedWhenDone(): Boolean {
        if (mDebug && !isRunning) {
            Log.d(TAG, "dispatchFinishedWhenDone()")
        }
        return super.dispatchFinishedWhenDone()
    }

    protected open fun hasPendingAnimations(): Boolean {
        return (mRemoveAnimationManager!!.hasPending() ||
                mMoveAnimationsManager!!.hasPending() ||
                mChangeAnimationsManager!!.hasPending() ||
                mAddAnimationsManager!!.hasPending())
    }

    protected open fun getRemoveAnimationManager(): ItemRemoveAnimationManager? {
        return mRemoveAnimationManager
    }

    protected open fun setItemRemoveAnimationManager(removeAnimationManager: ItemRemoveAnimationManager?) {
        mRemoveAnimationManager = removeAnimationManager
    }

    protected open fun getItemAddAnimationsManager(): ItemAddAnimationManager? {
        return mAddAnimationsManager
    }

    protected open fun setItemAddAnimationsManager(addAnimationsManager: ItemAddAnimationManager?) {
        mAddAnimationsManager = addAnimationsManager
    }

    protected open fun getItemChangeAnimationsManager(): ItemChangeAnimationManager? {
        return mChangeAnimationsManager
    }

    protected open fun setItemChangeAnimationsManager(changeAnimationsManager: ItemChangeAnimationManager?) {
        mChangeAnimationsManager = changeAnimationsManager
    }

    protected open fun getItemMoveAnimationsManager(): ItemMoveAnimationManager? {
        return mMoveAnimationsManager
    }

    protected open fun setItemMoveAnimationsManager(moveAnimationsManager: ItemMoveAnimationManager?) {
        mMoveAnimationsManager = moveAnimationsManager
    }

    open var isDebug: Boolean
        get() = mDebug
        set(debug) {
            mDebug = debug
        }

    protected open fun onSchedulePendingAnimations() {
        schedulePendingAnimationsByDefaultRule()
    }

    protected open fun schedulePendingAnimationsByDefaultRule() {
        val removalsPending = mRemoveAnimationManager!!.hasPending()
        val movesPending = mMoveAnimationsManager!!.hasPending()
        val changesPending = mChangeAnimationsManager!!.hasPending()
        val additionsPending = mAddAnimationsManager!!.hasPending()

        val removeDuration = if (removalsPending) removeDuration else 0
        val moveDuration = if (movesPending) moveDuration else 0
        val changeDuration = if (changesPending) changeDuration else 0

        if (removalsPending) {
            mRemoveAnimationManager!!.runPendingAnimations(false, 0)
        }

        if (movesPending) {
            val deferred = removalsPending
            val deferredDelay = removeDuration
            mMoveAnimationsManager!!.runPendingAnimations(deferred, deferredDelay)
        }

        if (changesPending) {
            val deferred = removalsPending
            val deferredDelay = removeDuration
            mChangeAnimationsManager!!.runPendingAnimations(deferred, deferredDelay)
        }

        if (additionsPending) {
            val deferred = (removalsPending || movesPending || changesPending)
            val totalDelay = removeDuration + max(moveDuration, changeDuration)
            val deferredDelay = if (deferred) totalDelay else 0
            mAddAnimationsManager!!.runPendingAnimations(deferred, deferredDelay)
        }
    }

    companion object {
        private const val TAG = "ARVGeneralItemAnimator"
    }
}
