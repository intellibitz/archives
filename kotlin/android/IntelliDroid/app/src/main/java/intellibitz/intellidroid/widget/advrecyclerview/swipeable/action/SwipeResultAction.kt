package intellibitz.intellidroid.widget.advrecyclerview.swipeable.action

abstract class SwipeResultAction protected constructor(private val mResultAction: Int) {

    fun getResultActionType(): Int {
        return mResultAction
    }

    fun performAction() {
        onPerformAction()
    }

    fun slideAnimationEnd() {
        onSlideAnimationEnd()
        onCleanUp()
    }

    protected open fun onPerformAction() {}

    protected open fun onSlideAnimationEnd() {}

    protected open fun onCleanUp() {}
}
