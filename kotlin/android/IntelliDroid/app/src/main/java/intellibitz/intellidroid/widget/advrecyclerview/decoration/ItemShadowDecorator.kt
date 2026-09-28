package intellibitz.intellidroid.widget.advrecyclerview.decoration

import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.NinePatchDrawable
import android.view.View
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView

class ItemShadowDecorator(
    private val mShadowDrawable: NinePatchDrawable,
    private val mCastShadowForTransparentBackgroundItem: Boolean = true
) : RecyclerView.ItemDecoration() {

    private val mShadowPadding = Rect()

    init {
        mShadowDrawable.getPadding(mShadowPadding)
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val childCount = parent.childCount
        if (childCount == 0) {
            return
        }

        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            if (!shouldDrawDropShadow(child)) {
                continue
            }

            val tx = (ViewCompat.getTranslationX(child) + 0.5f).toInt()
            val ty = (ViewCompat.getTranslationY(child) + 0.5f).toInt()

            val left = child.left - mShadowPadding.left
            val right = child.right + mShadowPadding.right
            val top = child.top - mShadowPadding.top
            val bottom = child.bottom + mShadowPadding.bottom

            mShadowDrawable.setBounds(left + tx, top + ty, right + tx, bottom + ty)
            mShadowDrawable.draw(c)
        }
    }

    private fun shouldDrawDropShadow(child: View): Boolean {
        if (child.visibility != View.VISIBLE) {
            return false
        }
        if (ViewCompat.getAlpha(child) != 1.0f) {
            return false
        }

        val background = child.background ?: return false

        if (!mCastShadowForTransparentBackgroundItem && (background is ColorDrawable)) {
            if (background.alpha == 0) {
                return false
            }
        }

        return true
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        outRect.set(0, 0, 0, 0)
    }
}
