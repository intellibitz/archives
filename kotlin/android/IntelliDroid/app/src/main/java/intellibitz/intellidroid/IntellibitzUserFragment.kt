package intellibitz.intellidroid

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import com.google.android.material.tabs.TabLayout
import intellibitz.intellidroid.data.ContactItem

/**
 * A simple Fragment subclass.
 */
open class IntellibitzUserFragment : IntellibitzPermissionFragment() {
    @JvmField
    protected var user: ContactItem? = null
    @JvmField
    protected var progressDialog: ProgressDialog? = null

    companion object {
        @JvmStatic
        fun selectTabAtByTag(tabLayout: TabLayout?, tab: Int): Boolean {
            val tabAt = findTabAtByTag(tabLayout, tab)
            if (tabAt != null && tabAt.tag is Int && tab == tabAt.tag as Int) {
                tabAt.select()
                return true
            }
            return false
        }

        @JvmStatic
        fun findTabAtByTag(tabLayout: TabLayout?, tab: Int): TabLayout.Tab? {
            if (tabLayout == null) return null
            val count = tabLayout.tabCount
            for (i in 0 until count) {
                val tabAt = tabLayout.getTabAt(i)
                if (tabAt != null && tabAt.tag is Int && tab == tabAt.tag as Int) {
                    return tabAt
                }
            }
            return null
        }

        @JvmStatic
        fun getSelectedTab(tabLayout: TabLayout?): TabLayout.Tab? {
            val position = getSelectedTabPosition(tabLayout)
            if (-1 == position || tabLayout == null) {
                return null
            }
            return tabLayout.getTabAt(position)
        }

        @JvmStatic
        fun getSelectedTabPosition(tabLayout: TabLayout?): Int {
            return tabLayout?.selectedTabPosition ?: -1
        }
    }

    open fun setUser(user: ContactItem?) {
        this.user = user
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(ContactItem.USER_CONTACT, user)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val userItem: ContactItem? = activity?.intent?.getParcelableExtra(ContactItem.USER_CONTACT)
        if (null == user) {
            user = userItem
        }
    }

    open fun showProgress(context: Context?, title: String?, msg: String?, indeterminate: Boolean) {
        progressDialog = ProgressDialog.show(context, title, msg, indeterminate)
    }

    open fun hideProgress() {
        if (progressDialog != null) {
            progressDialog!!.dismiss()
        }
    }

    protected open fun okActivity() {
        val act = activity ?: return
        val intent = act.intent
        intent.putExtra(ContactItem.USER_CONTACT, user as? Parcelable)
        act.setResult(Activity.RESULT_OK, intent)
        act.finish()
    }

    protected open fun cancelActivity() {
        val act = activity ?: return
        val intent = act.intent
        intent.putExtra(ContactItem.USER_CONTACT, user as? Parcelable)
        act.setResult(Activity.RESULT_CANCELED, intent)
        act.finish()
    }
}
