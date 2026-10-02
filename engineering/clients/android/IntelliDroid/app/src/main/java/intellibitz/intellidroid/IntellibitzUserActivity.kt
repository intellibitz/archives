package intellibitz.intellidroid

import android.os.Bundle
import intellibitz.intellidroid.data.BaseItem
import intellibitz.intellidroid.data.ContactItem
import java.util.*
import kotlin.collections.HashSet

//import java.util.*

open class IntellibitzUserActivity : IntellibitzPermissionActivity() {
    @JvmField
    protected var user: ContactItem? = null
    private val baseItemListeners = Collections.synchronizedSet(HashSet<BaseItemListener>())
    fun setUser(user: ContactItem?) {
        this.user = user
    }

    fun addBaseItemListener(listener: BaseItemListener) {
        baseItemListeners.add(listener)
    }

    private fun notifyBaseItemListeners(value: BaseItem?) {
        for (baseItemListener in baseItemListeners) {
            baseItemListener.onBaseItemStateChanged(ContactItem.USER_CONTACT, value)
        }
    }

    fun notifyUserBaseItemListeners() {
        notifyBaseItemListeners(user)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        user = savedInstanceState.getParcelable(ContactItem.USER_CONTACT)
        notifyUserBaseItemListeners()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(ContactItem.USER_CONTACT, user)
        super.onSaveInstanceState(outState)
    }

    interface BaseItemListener {
        fun onBaseItemStateChanged(key: String?, value: BaseItem?)
    }
}