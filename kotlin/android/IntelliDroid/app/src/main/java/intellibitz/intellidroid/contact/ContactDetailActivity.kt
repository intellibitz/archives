package intellibitz.intellidroid.contact

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.core.app.NavUtils
import androidx.fragment.app.Fragment
import intellibitz.intellidroid.IntellibitzTwoPaneUserActivity
import intellibitz.intellidroid.MainActivity
import intellibitz.intellidroid.R
import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.listener.ContactListener

class ContactDetailActivity : IntellibitzTwoPaneUserActivity(), ContactListener {

    private var contactItem: ContactItem? = null

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(ContactItem.TAG, contactItem)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        contactItem = savedInstanceState.getParcelable(ContactItem.TAG)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contactselect)
        if (null == savedInstanceState) {
            user = intent.getParcelableExtra(ContactItem.USER_CONTACT)
            contactItem = intent.getParcelableExtra(ContactItem.TAG)
        } else {
            user = savedInstanceState.getParcelable(ContactItem.USER_CONTACT)
            contactItem = savedInstanceState.getParcelable(ContactItem.TAG)
        }
        setupTwopane()
        setupFragments()
    }

    private fun setupAppBar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.setTitle(R.string.select_contacts)
        toolbar.setSubtitle(R.string.app_title)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            NavUtils.navigateUpTo(this, Intent(this, MainActivity::class.java))
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupFragments() {
        val contactDetailFragment = ContactDetailFragment.newInstance(contactItem, user)
        replaceContentFragment(contactDetailFragment)
    }

    fun replaceContentFragment(fragment: Fragment?): Fragment? {
        if (null == fragment) {
            return null
        }
        itemView.visibility = View.GONE
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(
            R.id.two_pane_container, fragment,
            fragment.javaClass.simpleName
        )
        fragmentTransaction.commit()
        itemView.visibility = View.VISIBLE
        return fragment
    }

    fun replaceDetailFragment(fragment: Fragment?): Fragment? {
        if (null == fragment) return null
        itemView.visibility = View.GONE
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        if (twoPane) {
            detailView.visibility = View.GONE
            fragmentTransaction.replace(
                R.id.two_pane_empty_container, fragment,
                fragment.javaClass.simpleName
            )
            detailView.visibility = View.VISIBLE
        } else {
            fragmentTransaction.replace(
                R.id.two_pane_container, fragment,
                fragment.javaClass.simpleName
            )
        }
        fragmentTransaction.commit()
        itemView.visibility = View.VISIBLE
        return fragment
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount == 1) {
            finish()
        }
        super.onBackPressed()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        Log.d(TAG, "onTrimMemory: $level")
    }

    override fun onViewModeChanged() {}

    override fun onViewModeItem() {}

    companion object {
        private const val TAG = "ContactDetailActivity"
    }
}
