package intellibitz.intellidroid.widget.advrecyclerview.draggable

import android.view.animation.Interpolator
import kotlin.math.abs

open class BasicSwapTargetTranslationInterpolator @JvmOverloads constructor(
    threshold: Float = 0.3f
) : Interpolator {

    private val mThreshold: Float
    private val mHalfValidRange: Float
    private val mInvValidRange: Float

    init {
        require(threshold >= 0 && threshold < 0.5f) { "Invalid threshold range: $threshold" }
        val validRange = 1.0f - 2 * threshold
        mThreshold = threshold
        mHalfValidRange = validRange * 0.5f
        mInvValidRange = 1.0f / validRange
    }

    override fun getInterpolation(input: Float): Float {
        return if (abs(input - 0.5f) < mHalfValidRange) {
            (input - mThreshold) * mInvValidRange
        } else {
            if (input < 0.5f) 0.0f else 1.0f
        }
    }
}
