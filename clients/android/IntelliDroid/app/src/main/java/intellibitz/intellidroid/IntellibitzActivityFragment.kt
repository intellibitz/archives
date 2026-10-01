package intellibitz.intellidroid

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import intellibitz.intellidroid.listener.ViewModeListener

open class IntellibitzActivityFragment : IntellibitzUserFragment(), ViewModeListener {
    companion object {
        private const val TAG = "MainActivityFrag"
    }

    enum class VIEW_MODE {
        ITEM, DETAIL
    }

    @JvmField
    protected var mainActivity: IntellibitzActivity? = null
    @JvmField
    protected var twoPane = false
    @JvmField
    protected var viewMode = VIEW_MODE.ITEM
    @JvmField
    protected var viewModeListener: ViewModeListener? = null

    override fun onViewModeChanged() {
        viewMode = VIEW_MODE.DETAIL
    }

    override fun onViewModeItem() {
        viewMode = VIEW_MODE.DETAIL
    }

    open fun setViewModeItem() {
        viewMode = VIEW_MODE.ITEM
    }

    open fun setViewModeListener(viewModeListener: ViewModeListener?) {
        this.viewModeListener = viewModeListener
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (null == mainActivity && context is IntellibitzActivity) {
            mainActivity = context
        }
    }

    open fun getMainActivity(): IntellibitzActivity? {
        return mainActivity
    }

    open fun setMainActivity(mainActivity: IntellibitzActivity?) {
        this.mainActivity = mainActivity
    }

    protected open fun getAppCompatActivity(): AppCompatActivity? {
        var act: AppCompatActivity? = activity as? AppCompatActivity
        if (null == act) {
            act = mainActivity
        }
        return act
    }

    open fun removeSelf(): IntellibitzActivity? {
        return removeSelf(getMainActivity())
    }

    open fun removeSelf(intellibitzActivity: IntellibitzActivity?): IntellibitzActivity? {
        if (null == intellibitzActivity) {
            Log.e(TAG, "onOkPressed: FAIL - intellibitz activity is NULL")
            return null
        }
        val v = view
        if (v != null) {
            v.visibility = View.GONE
        }
        return intellibitzActivity
    }

    open fun isTwoPane(): Boolean {
        return twoPane
    }

    open fun setTwoPane(twoPane: Boolean) {
        this.twoPane = twoPane
    }

    open fun getDrawable(id: Int, theme: Resources.Theme?): Drawable? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (host != null) {
                return resources.getDrawable(id, theme)
            }
        }
        val act = getAppCompatActivity()
        return if (act != null) ContextCompat.getDrawable(act, id) else null
    }

    open fun getDrawable(id: Int): Drawable? {
        return getDrawable(id, getAppCompatActivity()?.theme)
    }

    open fun getColor(id: Int): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (host != null) {
                return resources.getColor(id, getAppCompatActivity()?.theme)
            }
        }
        val act = getAppCompatActivity()
        return if (act != null) ContextCompat.getColor(act, id) else 0
    }

    open fun setCompoundDrawablesRelative(
        textView: TextView, s: Drawable?, t: Drawable?, e: Drawable?, b: Drawable?
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            textView.setCompoundDrawablesRelative(s, t, e, b)
        }
    }

    open fun setImageDrawable(view: View?, bitmap: Bitmap?) {
        if (view is ImageView && bitmap != null) {
            val drawable = getBitmapDrawable(bitmap)
            drawable.setBounds(0, 0, 100, 100)
            view.setImageDrawable(drawable)
        }
    }

    open fun getBitmapDrawable(bitmap: Bitmap): BitmapDrawable {
        return if (host != null) {
            BitmapDrawable(resources, bitmap)
        } else {
            BitmapDrawable(bitmap)
        }
    }
}
