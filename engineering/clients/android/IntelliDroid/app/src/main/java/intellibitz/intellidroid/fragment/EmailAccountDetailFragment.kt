package intellibitz.intellidroid.fragment

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.core.app.NavUtils
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import intellibitz.intellidroid.IntellibitzUserFragment
import intellibitz.intellidroid.R
import intellibitz.intellidroid.activity.AddEmailsActivity
import intellibitz.intellidroid.content.UserEmailContentProvider
import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.task.RemoveEmailTask
import intellibitz.intellidroid.util.MainApplicationSingleton
import org.json.JSONObject

/**
 *
 */
class EmailAccountDetailFragment : IntellibitzUserFragment(),
    RemoveEmailTask.RemoveEmailTaskListener {
    private var removeEmailTask: RemoveEmailTask? = null
    private var userEmail: ContactItem? = null

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    constructor() : super()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.emailaccount_detail, container, false)
        return rootView
    }

    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (null == savedInstanceState) {
            user = arguments!!.getParcelable(ContactItem.USER_CONTACT)
            userEmail = arguments!!.getParcelable(ContactItem.TAG)
        } else {
            user = savedInstanceState.getParcelable(ContactItem.USER_CONTACT)
            userEmail = savedInstanceState.getParcelable(ContactItem.TAG)
        }
        setupFAB(view)
        // Show the email content as text in a TextView.
        (view.findViewById<View>(R.id.tv_email) as TextView).text = user!!.email
    }

    private fun setupFAB(view: View) {
        //        id = getIntent().getStringExtra(EmailAccountDetailFragment.ARG_ITEM_ID);

        val fab = view.findViewById<View>(R.id.fab) as FloatingActionButton?
        if (fab != null) {
            fab.setOnClickListener { removeEmail() }
        }
    }

    private fun removeEmail() {
        MainApplicationSingleton.alertDialog(
            getContext(),
            getString(R.string.delete_email_alert),
            getString(R.string.remove_email_title),
            DialogInterface.OnClickListener { dialog, which ->
                removeEmailTask = RemoveEmailTask(
                    user!!.email,
                    user!!.dataId, user!!.token, user!!.device, user!!.deviceRef,
                    MainApplicationSingleton.AUTH_REMOVE_EMAIL, getContext()
                )
                removeEmailTask!!.setRequestTimeoutMillis(30000)
                removeEmailTask!!.setRemoveEmailTaskListener(this@EmailAccountDetailFragment)
                removeEmailTask!!.execute()
            },
            DialogInterface.OnClickListener { dialog, which -> dialog.dismiss() })
        /*
                    Snackbar.make(view, "Replace with your own detail action", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
    */

//                    MainApplicationSingleton mainApplication = MainApplicationSingleton.getInstance();
        //                userEmailItem = MainApplication.CONTACT_ITEM_MAP.get(id);
        //                String uid = mainApplication.getStringValueSP(MainApplicationSingleton.UID_PARAM);
        //                String token = mainApplication.getStringValueSP(MainApplicationSingleton.TOKEN_PARAM);
        //                String device = mainApplication.getStringValueSP(MainApplicationSingleton.DEVICE_PARAM);
        //                String email = extras.getString(MainApplication.CONTACT_PARAM);
    }

    private fun removeEmailAccount() {
        val email = user!!.email
//        long emailId = user.getEmailId(email);
//        int result = UserEmailContentProvider.deletesUserEmail(emailId, getContext());
        val result = UserEmailContentProvider.deletesUserEmail(email, getContext())
        Log.d(TAG, "Deleted Email: $result")
//        remove from user email collection
        user!!.removeEmail(email)
//        MainApplicationSingleton mainApplication = MainApplicationSingleton.getInstance();
//        mainApplication.removeStringSetValueSP(MainApplicationSingleton.EMAIL_PARAM, user.getEmail());
        val intent =
            Intent(MainApplicationSingleton.BROADCAST_EMAIL_ACCOUNT_REMOVED)
        intent.putExtra(ContactItem.USER_CONTACT, user as Parcelable?)
        LocalBroadcastManager.getInstance(activity!!).sendBroadcast(intent)
    }

    override fun onPostRemoveEmailResponse(response: JSONObject?) {
        showProgress(
            getContext(), getString(R.string.remove_email_title),
            getString(R.string.remove_email_progress), true
        )
        try {
            val status = response!!.getInt(MainApplicationSingleton.STATUS_PARAM)
            if (1 == status) {
//                    SUCCESS
                removeEmailAccount()
                //// TODO: 05-02-2016
//                    token ok, password to be set for user
//                    String url = "https://www.google.com";
                /*
                    Bundle bundle = new Bundle();
                    bundle.putString(MainApplicationSingleton.UID_PARAM, user.getDataId());
                    bundle.putString(MainApplicationSingleton.TOKEN_PARAM, user.getToken());
                    bundle.putString(MainApplicationSingleton.EMAIL_PARAM, user.getEmail());
*/

                val intent = Intent(activity, AddEmailsActivity::class.java)
                intent.putExtra(ContactItem.USER_CONTACT, user as Parcelable?)
                NavUtils.navigateUpTo(activity!!, intent)

                //// TODO: 11-02-2016
//                    get back to application flow.. set password, home et al
                /*
                    Intent intent = new Intent(NewEmailGetTokenActivity.this, IntellibitzActivity.class);
                    intent.putExtras(bundle);
                    startActivity(intent);
*/
//                    startActivityForResult(intent, 1, bundle);
            } else if (99 == status) {
//                    invalid email account..
                removeEmailAccount()
                val bundle = Bundle()
                bundle.putString("error", response.toString())
                bundle.putString(MainApplicationSingleton.EMAIL_PARAM, user!!.email)
                //// TODO: 11-02-2016
//                    get back to application flow.. set password, home et al
                /*
                    Intent intent = new Intent(EmailAccountDetailActivity.this, NewEmailAccountActivity.class);
                    intent.putExtras(bundle);
                    startActivity(intent);
*/
//                    SUCCESS
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            Log.e(TAG, TAG + e.toString())
        }
        setEmailRemoveTaskToNull()
        finishResultActivity()
        hideProgress()
    }

    private fun finishResultActivity() {
        val activity = activity
        val intent = activity!!.intent
        intent.putExtra(ContactItem.USER_CONTACT, user as Parcelable?)
        activity.setResult(Activity.RESULT_OK, intent)
        activity.finish()
    }

    override fun onPostRemoveEmailErrorResponse(response: JSONObject?) {
        setEmailRemoveTaskToNull()
    }

    override fun setEmailRemoveTaskToNull() {
        removeEmailTask = null
    }

    fun getUserEmail(): ContactItem? {
        return userEmail
    }

    fun setUserEmail(userEmail: ContactItem?) {
        this.userEmail = userEmail
    }

    companion object {
        const val TAG = "EmailAccountDetailFrag"

        fun newInstance(userEmailItem: ContactItem?, user: ContactItem?): EmailAccountDetailFragment {
            val fragment = EmailAccountDetailFragment()
            fragment.setUser(user)
            fragment.setUserEmail(userEmailItem)
            val args = Bundle()
            args.putParcelable(ContactItem.USER_CONTACT, user)
            args.putParcelable(ContactItem.TAG, userEmailItem)
            fragment.arguments = args
            return fragment
        }
    }
}
