package intellibitz.intellidroid.widget.advrecyclerview.swipeable

import android.annotation.TargetApi
import android.os.Build
import android.view.View

internal object InternalHelperKK {
    @JvmStatic
    @TargetApi(Build.VERSION_CODES.KITKAT)
    fun clearViewPropertyAnimatorUpdateListener(view: View) {
        view.animate().setUpdateListener(null)
    }
}
