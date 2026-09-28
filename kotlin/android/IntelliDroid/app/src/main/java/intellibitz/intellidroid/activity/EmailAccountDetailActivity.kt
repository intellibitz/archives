package intellibitz.intellidroid.activity

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.CollapsingToolbarLayout
import intellibitz.intellidroid.IntellibitzTwoPaneUserActivity
import intellibitz.intellidroid.R
import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.domain.account.EmailAccountListActivity
import intellibitz.intellidroid.fragment.EmailAccountDetailFragment

/**
 * An activity representing a single EmailAccount detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a [EmailAccountListActivity].
 */
class EmailAccountDetailActivity : IntellibitzTwoPaneUserActivity() {

    private var userEmailItem: ContactItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emailaccount_detail)
//        supportFragmentManager.addOnBackStackChangedListener(this)
        if (null == savedInstanceState) {
            user = intent.getParcelableExtra(ContactItem.USER_CONTACT)
            userEmailItem = intent.getParcelableExtra(ContactItem.TAG)
            setupTwopane()
            setupAppBar()
            setupFragments()
        } else {
            user = savedInstanceState.getParcelable(ContactItem.USER_CONTACT)
            userEmailItem = savedInstanceState.getParcelable(ContactItem.TAG)
//            newLoginFragment(savedInstanceState)
        }
    }

    private fun setupAppBar() {
        //        user = intent.getParcelableExtra(ContactItem.TAG)
        val toolbar = findViewById<Toolbar>(R.id.detail_toolbar)
        setSupportActionBar(toolbar)
        // Show the Up button in the action bar.
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val appBarLayout = findViewById<CollapsingToolbarLayout>(R.id.toolbar_layout)
        if (appBarLayout != null) {
            appBarLayout.title = userEmailItem!!.name
        }
    }

    private fun setupFragments() {
        // Create the detail fragment and add it to the activity
        // using a fragment transaction.
/*
        Bundle arguments = new Bundle();
        arguments.putString(EmailAccountDetailFragment.ARG_ITEM_ID,
                getIntent().getStringExtra(EmailAccountDetailFragment.ARG_ITEM_ID));
        EmailAccountDetailFragment fragment = new EmailAccountDetailFragment();
        arguments.putParcelable(ContactItem.TAG, user);
        fragment.setArguments(arguments);
*/
        val emailAccountDetailFragment =
            EmailAccountDetailFragment.newInstance(userEmailItem, user)
        replaceContentFragment(emailAccountDetailFragment)
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
//            NavUtils.navigateUpTo(this, new Intent(this, MainActivity.class));
//            NavUtils.navigateUpTo(this, new Intent(this, MainActivity.class));
//            NavUtils.navigateUpTo(this, NavUtils.getParentActivityIntent(this));
//            NavUtils.navigateUpFromSameTask(this);
/*
            Intent intent = new Intent(this, EmailAccountListActivity.class);
            intent.putExtra(ContactItem.TAG, (Parcelable) user);
            NavUtils.navigateUpTo(this, intent);
*/
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        private const val TAG = "EmailDetailActivity"
    }
}
