package intellibitz.intellidroid.widget.advrecyclerview.swipeable

import android.view.animation.Interpolator

internal class RubberBandInterpolator(private val limit: Float) : Interpolator {
    override fun getInterpolation(input: Float): Float {
        val t = 1.0f - input
        return limit * (1.0f - t * t)
    }
}
