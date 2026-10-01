package intellibitz.intellidroid.account

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.core.app.NavUtils
import androidx.fragment.app.Fragment
import intellibitz.intellidroid.IntellibitzTwoPaneUserActivity
import intellibitz.intellidroid.MainActivity
import intellibitz.intellidroid.R
import intellibitz.intellidroid.data.ContactItem

class AccountLandingActivity : IntellibitzTwoPaneUserActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_accountlanding)
        if (null == savedInstanceState) {
            user = intent.getParcelableExtra(ContactItem.USER_CONTACT)
            setupTwopane()
            setupFragments()
        } else {
            user = savedInstanceState.getParcelable(ContactItem.USER_CONTACT)
        }
    }

    private fun setupFragments() {
        val accountLandingFragment = AccountLandingFragment.newInstance(user)
        replaceContentFragment(accountLandingFragment)
    }

    fun replaceContentFragment(fragment: Fragment?): Fragment? {
        if (null == fragment) {
            return null
        }
        itemView?.visibility = View.GONE
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(
            R.id.two_pane_container, fragment,
            fragment.javaClass.simpleName
        )
        fragmentTransaction.commit()
        itemView?.visibility = View.VISIBLE
        return fragment
    }

    fun replaceDetailFragment(fragment: Fragment?): Fragment? {
        if (null == fragment) return null
        itemView?.visibility = View.GONE
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        if (twoPane) {
            detailView?.visibility = View.GONE
            fragmentTransaction.replace(
                R.id.two_pane_empty_container, fragment,
                fragment.javaClass.simpleName
            )
            detailView?.visibility = View.VISIBLE
        } else {
            fragmentTransaction.replace(
                R.id.two_pane_container, fragment,
                fragment.javaClass.simpleName
            )
        }
        fragmentTransaction.commit()
        itemView?.visibility = View.VISIBLE
        return fragment
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            NavUtils.navigateUpTo(this, Intent(this, MainActivity::class.java))
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    companion object {
        private const val TAG = "AcctLandingAct"
    }
}
