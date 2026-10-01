package intellibitz.intellidroid.widget.advrecyclerview.animator

import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import intellibitz.intellidroid.widget.advrecyclerview.animator.impl.AddAnimationInfo
import intellibitz.intellidroid.widget.advrecyclerview.animator.impl.ChangeAnimationInfo
import intellibitz.intellidroid.widget.advrecyclerview.animator.impl.ItemAddAnimationManager
import intellibitz.intellidroid.widget.advrecyclerview.animator.impl.ItemChangeAnimationManager
import intellibitz.intellidroid.widget.advrecyclerview.animator.impl.ItemMoveAnimationManager
import intellibitz.intellidroid.widget.advrecyclerview.animator.impl.ItemRemoveAnimationManager
import intellibitz.intellidroid.widget.advrecyclerview.animator.impl.MoveAnimationInfo
import intellibitz.intellidroid.widget.advrecyclerview.animator.impl.RemoveAnimationInfo

open class RefactoredDefaultItemAnimator : GeneralItemAnimator() {

    override fun onSetup() {
        setItemAddAnimationsManager(DefaultItemAddAnimationManager(this))
        setItemRemoveAnimationManager(DefaultItemRemoveAnimationManager(this))
        setItemChangeAnimationsManager(DefaultItemChangeAnimationManager(this))
        setItemMoveAnimationsManager(DefaultItemMoveAnimationManager(this))
    }

    override fun onSchedulePendingAnimations() {
        schedulePendingAnimationsByDefaultRule()
    }

    /**
     * Item Animation manager for ADD operation (Same behavior as DefaultItemAnimator class)
     */
    protected open class DefaultItemAddAnimationManager(itemAnimator: BaseItemAnimator) :
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
     * Item Animation manager for REMOVE operation (Same behavior as DefaultItemAnimator class)
     */
    protected open class DefaultItemRemoveAnimationManager(itemAnimator: BaseItemAnimator) :
        ItemRemoveAnimationManager(itemAnimator) {

        override fun onCreateAnimation(info: RemoveAnimationInfo) {
            val animator = ViewCompat.animate(info.holder.itemView)
            animator.duration = duration
            animator.alpha(0f)
            startActiveItemAnimation(info, info.holder, animator)
        }

        override fun onAnimationEndedSuccessfully(info: RemoveAnimationInfo, item: RecyclerView.ViewHolder) {
            ViewCompat.setAlpha(item.itemView, 1f)
        }

        override fun onAnimationEndedBeforeStarted(info: RemoveAnimationInfo, item: RecyclerView.ViewHolder) {
            ViewCompat.setAlpha(item.itemView, 1f)
        }

        override fun onAnimationCancel(info: RemoveAnimationInfo, item: RecyclerView.ViewHolder) {}

        override fun addPendingAnimation(holder: RecyclerView.ViewHolder): Boolean {
            endAnimation(holder)
            enqueuePendingAnimationInfo(RemoveAnimationInfo(holder))
            return true
        }
    }

    /**
     * Item Animation manager for CHANGE operation (Same behavior as DefaultItemAnimator class)
     */
    protected open class DefaultItemChangeAnimationManager(itemAnimator: BaseItemAnimator) :
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
            ViewCompat.setAlpha(item.itemView, 1f)
            ViewCompat.setTranslationX(item.itemView, 0f)
            ViewCompat.setTranslationY(item.itemView, 0f)
        }

        override fun onAnimationEndedBeforeStarted(info: ChangeAnimationInfo, item: RecyclerView.ViewHolder) {
            ViewCompat.setAlpha(item.itemView, 1f)
            ViewCompat.setTranslationX(item.itemView, 0f)
            ViewCompat.setTranslationY(item.itemView, 0f)
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
     * Item Animation manager for MOVE operation (Same behavior as DefaultItemAnimator class)
     */
    protected open class DefaultItemMoveAnimationManager(itemAnimator: BaseItemAnimator) :
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
}
