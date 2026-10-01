package intellibitz.intellidroid.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.core.app.NavUtils
import androidx.fragment.app.Fragment
import intellibitz.intellidroid.IntellibitzTwoPaneUserActivity
import intellibitz.intellidroid.R
import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.domain.MainSettingsActivity
import intellibitz.intellidroid.fragment.ProfileItemFragment
import intellibitz.intellidroid.listener.ProfileListener
import intellibitz.intellidroid.listener.ProfileTopicListener
import java.io.File

class ProfileActivity : IntellibitzTwoPaneUserActivity(), ProfileListener, ProfileTopicListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        if (null == savedInstanceState) {
            user = intent.getParcelableExtra(ContactItem.USER_CONTACT)
        } else {
            user = savedInstanceState.getParcelable(ContactItem.USER_CONTACT)
        }
        setupTwopane()
        setupAppBar()
        setupFragments()
    }

    private fun setupAppBar() {
        val toolbar = findViewById<Toolbar>(R.id.profile_toolbar)
        toolbar.setTitle(R.string.profile)
        toolbar.setSubtitle(R.string.app_title)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            NavUtils.navigateUpTo(this, Intent(this, MainSettingsActivity::class.java))
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupFragments() {
        val profileItemFragment = ProfileItemFragment.newInstance(this, user)
        replaceContentFragment(profileItemFragment)
    }

    /**
     * @param fragment the fragment to be replaced in the two pane container
     * @return fragment the replaced fragment
     */
    fun replaceContentFragment(fragment: Fragment?): Fragment? {
        if (null == fragment) {
            return null
        }
        itemView!!.visibility = View.GONE
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(
            R.id.two_pane_container, fragment,
            fragment.javaClass.simpleName
        )
//        fragmentTransaction.addToBackStack(fragment.javaClass.simpleName)
        fragmentTransaction.commit()
        itemView!!.visibility = View.VISIBLE
        return fragment
    }

    /**
     * @param fragment the detail fragment to be replaced in the two pane container
     * @return fragment the replaced detail fragment
     */
    fun replaceDetailFragment(fragment: Fragment?): Fragment? {
        if (null == fragment) return null
        itemView!!.visibility = View.GONE
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        if (twoPane) {
            detailView!!.visibility = View.GONE
            fragmentTransaction.replace(
                R.id.two_pane_empty_container, fragment,
                fragment.javaClass.simpleName
            )
            detailView!!.visibility = View.VISIBLE
        } else {
            fragmentTransaction.replace(
                R.id.two_pane_container, fragment,
                fragment.javaClass.simpleName
            )
        }
        fragmentTransaction.commit()
        itemView!!.visibility = View.VISIBLE
        return fragment
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount == 1) {
            finish()
        }
        super.onBackPressed()
    }

    override fun onSaveInstanceState(outState: Bundle) {
//        the activity will be destroyed, a chance to save the state
//        super.onSaveInstanceState(outState);
        @Suppress("UNUSED_VARIABLE")
        val fragment = supportFragmentManager.fragments[0] as ProfileItemFragment
        outState.putParcelable(ContactItem.USER_CONTACT, user)
    }

    override fun onProfilePicChanged(file: File?) {
    }

    override fun onProfileTopicClicked(item: ContactItem?) {
    }

    override fun onProfileTopicsLoaded(count: Int) {
    }

    override fun onTrimMemory(level: Int) {
        Log.d(TAG, "onTrimMemory: $level")
    }

    companion object {
        private const val TAG = "ProfileActivity"
    }
}
