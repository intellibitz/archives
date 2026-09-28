package intellibitz.intellidroid.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.core.app.NavUtils
import androidx.fragment.app.Fragment
import intellibitz.intellidroid.IntellibitzTwoPaneUserActivity
import intellibitz.intellidroid.MainActivity
import intellibitz.intellidroid.R
import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.data.MessageItem
import intellibitz.intellidroid.fragment.ClutterEmailsFragment
import intellibitz.intellidroid.listener.ClutterTopicListener
import intellibitz.intellidroid.listener.PeopleListener
import intellibitz.intellidroid.util.MainApplicationSingleton

class ClutterEmailsActivity : IntellibitzTwoPaneUserActivity(), PeopleListener, ClutterTopicListener {

    private var messageItem: MessageItem? = null

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(MessageItem.EMAIL_MESSAGE, messageItem)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        messageItem = savedInstanceState.getParcelable(MessageItem.EMAIL_MESSAGE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clutteremails)
        if (null == savedInstanceState) {
            user = intent.getParcelableExtra(ContactItem.USER_CONTACT)
            messageItem = intent.getParcelableExtra(MessageItem.EMAIL_MESSAGE)
        } else {
            user = savedInstanceState.getParcelable(ContactItem.USER_CONTACT)
            messageItem = savedInstanceState.getParcelable(MessageItem.EMAIL_MESSAGE)
        }
        setupTwopane()
        setupFragments()
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
        val clutterEmailsFragment =
            ClutterEmailsFragment.newInstance(messageItem, user, this)
        replaceContentFragment(clutterEmailsFragment)
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

    override fun onTrimMemory(level: Int) {
        Log.d(TAG, "onTrimMemory: $level")
    }

    override fun onViewModeChanged() {
    }

    override fun onViewModeItem() {
    }

    /**
     * You are calling startActivityForResult() from your Fragment. When you do this,
     * the requestCode is changed by the Activity that owns the Fragment.
     * If you want to get the correct resultCode in your activity try this:
     * Change:
     * startActivityForResult(intent, 1);
     * To:
     * getActivity().startActivityForResult(intent, 1);
     * Just a note: if you use startActivityForResult in a fragment and expect the result from
     * onActivityResult in that fragment, just make sure you call super.onActivityResult in the
     * host activity (in case you override that method there).
     * This is because the activity's onActivityResult seems to call the fragment's onActivityResult.
     * Also, note that the request code, when it travels through the activity's onActivityResult,
     * is altered
     * "the requestCode is changed by the Activity that owns the Fragment" - Gotta love the Android design...
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
//        NOTE: THE ABOVE CALL DELIVERS THE ACTIVITY RESULT TO THE FRAGMENT
    }

    override fun onClutterTopicsLoaded(count: Int) {
    }

    override fun onClutterTopicClicked(item: MessageItem?, user: ContactItem?) {
        startClutterEmailActivity(item, user)
    }

    fun startClutterEmailActivity(messageItem: MessageItem?, user: ContactItem?) {
        val intent = Intent(this, ClutterEmailActivity::class.java)
        intent.putExtra(MessageItem.EMAIL_MESSAGE, messageItem)
        intent.putExtra(ContactItem.USER_CONTACT, user)
        startActivityForResult(
            intent, MainApplicationSingleton.ACTIVITY_CLUTTEREMAIL_RQ_CODE
        )
    }

    companion object {
        private const val TAG = "ClutterEmailsAct"
    }
}
