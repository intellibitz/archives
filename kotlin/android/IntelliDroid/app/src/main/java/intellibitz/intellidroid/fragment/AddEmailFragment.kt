package intellibitz.intellidroid.fragment

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.annotation.Nullable
import intellibitz.intellidroid.IntellibitzUserFragment
import intellibitz.intellidroid.R
import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.domain.account.NewEmailAccountActivity
import intellibitz.intellidroid.util.MainApplicationSingleton

/**
 *
 */
class AddEmailFragment : IntellibitzUserFragment() {
    private var view: View? = null
    private var snackView: View? = null
    private var progressDialog: ProgressDialog? = null

    constructor() : super()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_addemail, container, false)
        return view
    }

    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (null == savedInstanceState) {
            user = arguments!!.getParcelable(ContactItem.USER_CONTACT)
        } else {
            user = savedInstanceState.getParcelable(ContactItem.USER_CONTACT)
        }
        val onClickListener = View.OnClickListener {
//                    contacts selected for chat
//                ContactItem item = data.getParcelableExtra(ContactItem.TAG);
            val activity = activity
            val intent = activity!!.intent
            intent.putExtra(ContactItem.USER_CONTACT, user as Parcelable?)
            activity.setResult(Activity.RESULT_OK, intent)
            activity.finish()
        }
        val btnSkip = view.findViewById<View>(R.id.btn_skip) as Button
        btnSkip.setOnClickListener(onClickListener)
        val rlSkip = view.findViewById<View>(R.id.rl_skip)
        rlSkip.setOnClickListener(onClickListener)

        getSnackView()

        val llGmail = view.findViewById<View>(R.id.ll_addemail_gmail)
        llGmail.setOnClickListener { startNewEmailAccountActivity() }
        val ivGmail = view.findViewById<View>(R.id.iv_addemail_gmail)
        ivGmail.setOnClickListener {
            showProgress()
            startNewEmailAccountActivity()
            hideProgress()
        }
        val tvGmail = view.findViewById<View>(R.id.tv_addemail_gmail)
        tvGmail.setOnClickListener {
            showProgress()
            startNewEmailAccountActivity()
            hideProgress()
        }
    }

    fun showProgress() {
        progressDialog =
            ProgressDialog.show(activity, "Add Email", "Redirecting to Email provider", true)
    }

    fun hideProgress() {
        if (progressDialog != null) progressDialog!!.dismiss()
    }

    fun getSnackView(): View? {
        if (null == snackView) //            snackView = getActivity().findViewById(R.id.cl);
//            snackView = view.findViewById(R.id.ll);
            snackView = view!!.findViewById(R.id.username)
        return snackView
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
     * "the requestCode is changed by the Activity that owns the Fragment" - Gotta love the Android design... –
     */
    private fun startNewEmailAccountActivity() {
        val intent = Intent(activity, NewEmailAccountActivity::class.java)
        intent.putExtra(ContactItem.USER_CONTACT, user as Parcelable?)
//        NOTE: there is a significant difference between the following two calls

//        THIS WILL DELIVER THE RESULT, TO PARENT ACTIVITY AND THE PARENT ACTIVITY IF OVERRIDDEN
//        MUST CALL SUPER.STARTACTIVITYFORRESULT IN ITS OVERRIDDEN METHOD FOR THE RESULT
//        TO BE DELIVERED TO THIS FRAGMENT
        startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_NEWEMAILACCOUNT_RQ_CODE)

//        THIS WILL DELIVER THE RESULT, TO PARENT ACTIVITY AND THE PARENT ACTIVITY IF OVERRIDDEN
//        MUST MANUALLY INVOKE THE FRAGMENTS IN ITS OVERRIDDEN METHOD FOR THE RESULT
//        TO BE DELIVERED TO THIS FRAGMENT
//        getAppCompatActivity().startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_CONTACTSELECT_RQ_CODE);
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
     * "the requestCode is changed by the Activity that owns the Fragment" - Gotta love the Android design... –
     *
     * @param requestCode the request code with which the activity started
     * @param resultCode  the result code send back by the activity
     * @param data        the intent data with extras
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data);
        if (MainApplicationSingleton.ACTIVITY_OTP_RQ_CODE == requestCode) {
            if (Activity.RESULT_OK == resultCode) {
                if (data != null) {
//                    contacts selected for chat
                    val item = data.getParcelableExtra<ContactItem>(ContactItem.USER_CONTACT)
                    val activity = activity
                    val intent = activity!!.intent
                    intent.putExtra(ContactItem.USER_CONTACT, item as Parcelable?)
                    activity.setResult(Activity.RESULT_OK, intent)
                    activity.finish()
                }
            } else if (Activity.RESULT_CANCELED == resultCode) {
                val activity = activity
                val intent = activity!!.intent
                intent.putExtra(ContactItem.USER_CONTACT, user as Parcelable?)
                activity.setResult(Activity.RESULT_CANCELED, intent)
                activity.finish()
                Log.e(TAG, "onActivityResult: OTP cancelled - ")
            }
        }
        if (MainApplicationSingleton.ACTIVITY_NEWEMAILACCOUNT_RQ_CODE == requestCode) {
            if (Activity.RESULT_OK == resultCode) {
                if (data != null) {
//                    contacts selected for chat
                    val item = data.getParcelableExtra<ContactItem>(ContactItem.USER_CONTACT)
                    val activity = activity
                    val intent = activity!!.intent
                    intent.putExtra(ContactItem.USER_CONTACT, item as Parcelable?)
                    activity.setResult(Activity.RESULT_OK, intent)
                    activity.finish()
                }
            } else if (Activity.RESULT_CANCELED == resultCode) {
                val activity = activity
                val intent = activity!!.intent
                intent.putExtra(ContactItem.USER_CONTACT, user as Parcelable?)
                activity.setResult(Activity.RESULT_CANCELED, intent)
                activity.finish()
                Log.e(TAG, "onActivityResult: Add new email - cancelled ")
            }
        }
    }

    companion object {
        private const val TAG = "AddEmailFragment"

        fun newInstance(user: ContactItem?): AddEmailFragment {
            val fragment = AddEmailFragment()
            fragment.setUser(user)
            val args = Bundle()
            args.putParcelable(ContactItem.USER_CONTACT, user)
            fragment.arguments = args
            return fragment
        }
    }
}
