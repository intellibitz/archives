package intellibitz.intellidroid.account

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import intellibitz.intellidroid.IntellibitzTwoPaneUserActivity
import intellibitz.intellidroid.R
import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.util.MainApplicationSingleton

class ProfileSignupActivity : IntellibitzTwoPaneUserActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profileinfo)
        if (null == savedInstanceState) {
            user = intent.getParcelableExtra(ContactItem.USER_CONTACT)
            setupTwopane()
            setupFragments()
        } else {
            user = savedInstanceState.getParcelable(ContactItem.USER_CONTACT)
        }
    }

    private fun setupFragments() {
        val profileSignupFragment = ProfileSignupFragment.newInstance(user)
        replaceContentFragment(profileSignupFragment)
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    fun setupAlert(upAlert: Boolean, view: View?) {
        if (upAlert && view != null) {
            var text = user?.name
            if (text.isNullOrEmpty()) {
                text = "User Found"
            }
            val snack = Snackbar.make(view, text, Snackbar.LENGTH_INDEFINITE)
            snack.setAction("SWITCH") {
                val intent = Intent(
                    this@ProfileSignupActivity,
                    MainApplicationSingleton.MAIN_ACTIVITY_CLASS
                )
                intent.putExtra(ContactItem.USER_CONTACT, user as? Parcelable)
                startActivity(intent)
            }
            snack.show()
        }
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
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    companion object {
        private const val TAG = "ProfileSignupActivity"
    }
}
