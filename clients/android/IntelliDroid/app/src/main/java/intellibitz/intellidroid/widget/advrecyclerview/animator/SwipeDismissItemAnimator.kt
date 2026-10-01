package intellibitz.intellidroid.widget.advrecyclerview.animator

import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Interpolator
import androidx.core.view.ViewCompat
import androidx.core.view.ViewPropertyAnimatorCompat
import androidx.recyclerview.widget.RecyclerView
import intellibitz.intellidroid.widget.advrecyclerview.animator.impl.AddAnimationInfo
import intellibitz.intellidroid.widget.advrecyclerview.animator.impl.ChangeAnimationInfo
import intellibitz.intellidroid.widget.advrecyclerview.animator.impl.ItemAddAnimationManager
import intellibitz.intellidroid.widget.advrecyclerview.animator.impl.ItemChangeAnimationManager
import intellibitz.intellidroid.widget.advrecyclerview.animator.impl.ItemMoveAnimationManager
import intellibitz.intellidroid.widget.advrecyclerview.animator.impl.ItemRemoveAnimationManager
import intellibitz.intellidroid.widget.advrecyclerview.animator.impl.MoveAnimationInfo
import intellibitz.intellidroid.widget.advrecyclerview.animator.impl.RemoveAnimationInfo
import intellibitz.intellidroid.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager
import intellibitz.intellidroid.widget.advrecyclerview.swipeable.SwipeableItemViewHolder

open class SwipeDismissItemAnimator : GeneralItemAnimator() {

    override fun onSetup() {
        setItemAddAnimationsManager(DefaultItemAddAnimationManager(this))
        setItemRemoveAnimationManager(SwipeDismissItemRemoveAnimationManager(this))
        setItemChangeAnimationsManager(SwipeDismissItemChangeAnimationManager(this))
        setItemMoveAnimationsManager(SwipeDismissItemMoveAnimationManager(this))

        removeDuration = 150
        moveDuration = 150
    }

    override fun onSchedulePendingAnimations() {
        schedulePendingAnimationsByDefaultRule()
    }

    override fun cancelAnimations(item: RecyclerView.ViewHolder) {
        super.cancelAnimations(item)
    }

    /**
     * Item Animation manager for ADD operation (Same behavior as DefaultItemAnimator class)
     */
    private class DefaultItemAddAnimationManager(itemAnimator: BaseItemAnimator) :
        ItemAddAnimationManager(itemAnimator) {

        override fun onCreateAnimation(info: AddAnimationInfo) {
            val animator = ViewCompat.animate(info.holder.itemView)
            animator.alpha(1f)
            animator.duration = duration
            startActiveItemAnimation(info, info.holder, animator)
        }

        override fun onAnimationEndedSuccessfully(info: AddAnimationInfo, item: RecyclerView.ViewHolder) {}

        override fun onAnimationEndedBeforeStarted(info: AddAnimationInfo, item: RecyclerView.ViewHolder) {
            ViewCompat.setAlpha(item.itemView, 1f)
        }

        override fun onAnimationCancel(info: AddAnimationInfo, item: RecyclerView.ViewHolder) {
            ViewCompat.setAlpha(item.itemView, 1f)
        }

        override fun addPendingAnimation(item: RecyclerView.ViewHolder): Boolean {
            endAnimation(item)
            ViewCompat.setAlpha(item.itemView, 0f)
            enqueuePendingAnimationInfo(AddAnimationInfo(item))
            return true
        }
    }

    /**
     * Item Animation manager for REMOVE operation
     */
    private class SwipeDismissItemRemoveAnimationManager(itemAnimator: BaseItemAnimator) :
        ItemRemoveAnimationManager(itemAnimator) {

        companion object {
            private val DEFAULT_INTERPOLATOR: Interpolator = AccelerateDecelerateInterpolator()
        }

        private fun isSwipeDismissed(item: RecyclerView.ViewHolder?): Boolean {
            if (item !is SwipeableItemViewHolder) {
                return false
            }

            val item2 = item as SwipeableItemViewHolder
            val result = item2.swipeResult
            val reaction = item2.afterSwipeReaction

            return ((result == RecyclerViewSwipeManager.RESULT_SWIPED_LEFT ||
                    result == RecyclerViewSwipeManager.RESULT_SWIPED_UP ||
                    result == RecyclerViewSwipeManager.RESULT_SWIPED_RIGHT ||
                    result == RecyclerViewSwipeManager.RESULT_SWIPED_DOWN) &&
                    reaction == RecyclerViewSwipeManager.AFTER_SWIPE_REACTION_REMOVE_ITEM)
        }

        private fun isSwipeDismissed(info: RemoveAnimationInfo?): Boolean {
            return info is SwipeDismissRemoveAnimationInfo
        }

        override fun onCreateAnimation(info: RemoveAnimationInfo) {
            val animator: ViewPropertyAnimatorCompat

            if (isSwipeDismissed(info.holder)) {
                val view = info.holder.itemView
                animator = ViewCompat.animate(view)
                animator.duration = duration
            } else {
                val view = info.holder.itemView
                animator = ViewCompat.animate(view)
                animator.duration = duration
                animator.setInterpolator(DEFAULT_INTERPOLATOR)
                animator.alpha(0f)
            }

            startActiveItemAnimation(info, info.holder, animator)
        }

        override fun onAnimationEndedSuccessfully(info: RemoveAnimationInfo, item: RecyclerView.ViewHolder) {
            val view = item.itemView
            if (isSwipeDismissed(info)) {
                ViewCompat.setTranslationX(view, 0f)
                ViewCompat.setTranslationY(view, 0f)
            } else {
                ViewCompat.setAlpha(view, 1f)
            }
        }

        override fun onAnimationEndedBeforeStarted(info: RemoveAnimationInfo, item: RecyclerView.ViewHolder) {
            val view = item.itemView
            if (isSwipeDismissed(info)) {
                ViewCompat.setTranslationX(view, 0f)
                ViewCompat.setTranslationY(view, 0f)
            } else {
                ViewCompat.setAlpha(view, 1f)
            }
        }

        override fun onAnimationCancel(info: RemoveAnimationInfo, item: RecyclerView.ViewHolder) {}

        override fun addPendingAnimation(holder: RecyclerView.ViewHolder): Boolean {
            if (isSwipeDismissed(holder)) {
                val itemView = holder.itemView
                val prevItemX = (ViewCompat.getTranslationX(itemView) + 0.5f).toInt()
                val prevItemY = (ViewCompat.getTranslationY(itemView) + 0.5f).toInt()

                endAnimation(holder)

                ViewCompat.setTranslationX(itemView, prevItemX.toFloat())
                ViewCompat.setTranslationY(itemView, prevItemY.toFloat())

                enqueuePendingAnimationInfo(SwipeDismissRemoveAnimationInfo(holder))
                return true
            } else {
                endAnimation(holder)
                enqueuePendingAnimationInfo(RemoveAnimationInfo(holder))
                return true
            }
        }
    }

    private class SwipeDismissRemoveAnimationInfo(holder: RecyclerView.ViewHolder) : RemoveAnimationInfo(holder)

    /**
     * Item Animation manager for CHANGE operation
     */
    private class SwipeDismissItemChangeAnimationManager(itemAnimator: BaseItemAnimator) :
        ItemChangeAnimationManager(itemAnimator) {

        override fun onCreateChangeAnimationForOldItem(info: ChangeAnimationInfo) {
            val animator = ViewCompat.animate(info.oldHolder.itemView)
            animator.duration = duration
            animator.translationX((info.toX - info.fromX).toFloat())
            animator.translationY((info.toY - info.fromY).toFloat())
            animator.alpha(0f)
            startActiveItemAnimation(info, info.oldHolder, animator)
        }

        override fun onCreateChangeAnimationForNewItem(info: ChangeAnimationInfo) {
            val animator = ViewCompat.animate(info.newHolder.itemView)
            animator.translationX(0f)
            animator.translationY(0f)
            animator.duration = duration
            animator.alpha(1f)
            startActiveItemAnimation(info, info.newHolder, animator)
        }

        override fun onAnimationEndedSuccessfully(info: ChangeAnimationInfo, item: RecyclerView.ViewHolder) {
            val view = item.itemView
            ViewCompat.setAlpha(view, 1f)
            ViewCompat.setTranslationX(view, 0f)
            ViewCompat.setTranslationY(view, 0f)
        }

        override fun onAnimationEndedBeforeStarted(info: ChangeAnimationInfo, item: RecyclerView.ViewHolder) {
            val view = item.itemView
            ViewCompat.setAlpha(view, 1f)
            ViewCompat.setTranslationX(view, 0f)
            ViewCompat.setTranslationY(view, 0f)
        }

        override fun onAnimationCancel(info: ChangeAnimationInfo, item: RecyclerView.ViewHolder) {}

        override fun addPendingAnimation(
            oldHolder: RecyclerView.ViewHolder,
            newHolder: RecyclerView.ViewHolder?,
            fromX: Int,
            fromY: Int,
            toX: Int,
            toY: Int
        ): Boolean {
            val prevTranslationX = ViewCompat.getTranslationX(oldHolder.itemView)
            val prevTranslationY = ViewCompat.getTranslationY(oldHolder.itemView)
            val prevAlpha = ViewCompat.getAlpha(oldHolder.itemView)

            endAnimation(oldHolder)

            val deltaX = (toX - fromX - prevTranslationX).toInt()
            val deltaY = (toY - fromY - prevTranslationY).toInt()

            // recover prev translation state after ending animation
            ViewCompat.setTranslationX(oldHolder.itemView, prevTranslationX)
            ViewCompat.setTranslationY(oldHolder.itemView, prevTranslationY)
            ViewCompat.setAlpha(oldHolder.itemView, prevAlpha)

            if (newHolder != null) {
                // carry over translation values
                endAnimation(newHolder)
                ViewCompat.setTranslationX(newHolder.itemView, -deltaX.toFloat())
                ViewCompat.setTranslationY(newHolder.itemView, -deltaY.toFloat())
                ViewCompat.setAlpha(newHolder.itemView, 0f)
            }

            enqueuePendingAnimationInfo(
                ChangeAnimationInfo(
                    oldHolder,
                    newHolder,
                    fromX,
                    fromY,
                    toX,
                    toY
                )
            )

            return true
        }
    }

    /**
     * Item Animation manager for MOVE operation
     */
    private class SwipeDismissItemMoveAnimationManager(itemAnimator: BaseItemAnimator) :
        ItemMoveAnimationManager(itemAnimator) {

        override fun onCreateAnimation(info: MoveAnimationInfo) {
            val view = info.holder.itemView
            val deltaX = info.toX - info.fromX
            val deltaY = info.toY - info.fromY

            if (deltaX != 0) {
                ViewCompat.animate(view).translationX(0f)
            }
            if (deltaY != 0) {
                ViewCompat.animate(view).translationY(0f)
            }

            val animator = ViewCompat.animate(view)
            animator.duration = duration
            animator.setInterpolator(MOVE_INTERPOLATOR)

            startActiveItemAnimation(info, info.holder, animator)
        }

        override fun onAnimationEndedSuccessfully(info: MoveAnimationInfo, item: RecyclerView.ViewHolder) {}

        override fun onAnimationEndedBeforeStarted(info: MoveAnimationInfo, item: RecyclerView.ViewHolder) {
            ViewCompat.setTranslationY(item.itemView, 0f)
            ViewCompat.setTranslationX(item.itemView, 0f)
        }

        override fun onAnimationCancel(info: MoveAnimationInfo, item: RecyclerView.ViewHolder) {
            val view = item.itemView
            val deltaX = info.toX - info.fromX
            val deltaY = info.toY - info.fromY

            if (deltaX != 0) {
                ViewCompat.animate(view).translationX(0f)
            }
            if (deltaY != 0) {
                ViewCompat.animate(view).translationY(0f)
            }

            if (deltaX != 0) {
                ViewCompat.setTranslationX(view, 0f)
            }
            if (deltaY != 0) {
                ViewCompat.setTranslationY(view, 0f)
            }
        }

        override fun addPendingAnimation(
            item: RecyclerView.ViewHolder,
            fromX: Int,
            fromY: Int,
            toX: Int,
            toY: Int
        ): Boolean {
            var startX = fromX
            var startY = fromY
            val view = item.itemView

            startX += ViewCompat.getTranslationX(item.itemView).toInt()
            startY += ViewCompat.getTranslationY(item.itemView).toInt()

            endAnimation(item)

            val deltaX = toX - startX
            val deltaY = toY - startY

            val info = MoveAnimationInfo(item, startX, startY, toX, toY)

            if (deltaX == 0 && deltaY == 0) {
                dispatchFinished(info, info.holder)
                info.clear(info.holder)
                return false
            }

            if (deltaX != 0) {
                ViewCompat.setTranslationX(view, -deltaX.toFloat())
            }
            if (deltaY != 0) {
                ViewCompat.setTranslationY(view, -deltaY.toFloat())
            }

            enqueuePendingAnimationInfo(info)

            return true
        }
    }

    companion object {
        @JvmField
        val MOVE_INTERPOLATOR: Interpolator = AccelerateDecelerateInterpolator()
    }
}
