package intellibitz.intellidroid.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Switch
import androidx.annotation.Nullable
import androidx.fragment.app.DialogFragment
import intellibitz.intellidroid.IntellibitzActivityFragment
import intellibitz.intellidroid.R
import intellibitz.intellidroid.activity.ProfileInfoActivity
import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.listener.ProfileListener
import intellibitz.intellidroid.listener.ProfileTopicListener
import intellibitz.intellidroid.util.MainApplicationSingleton
import intellibitz.intellidroid.widget.LockPasswordFragment

/**
 *
 */
class MyAccountFragment : IntellibitzActivityFragment(),
    LockPasswordFragment.OnLockPasswordFragmentListener {
    private var snackView: View? = null
    private var profileTopicListener: ProfileTopicListener? = null
    private var imageView: ImageView? = null

    constructor() : super() {
        // Required empty public constructor
    }

    fun setProfileTopicListener(profileTopicListener: ProfileTopicListener?) {
        this.profileTopicListener = profileTopicListener
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onDetach() {
        super.onDetach()
        profileTopicListener = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(ContactItem.USER_CONTACT, user)
        val view = view
        if (view != null) {
            val startLock = view.findViewById<View>(R.id.sw_startuplock) as Switch
            MainApplicationSingleton.getInstance(getContext()).putBooleanValueSP(
                MainApplicationSingleton.STARTUPLOCK_PARAM, startLock.isChecked
            )
            val inappLock = view.findViewById<View>(R.id.sw_inapplock) as Switch
            MainApplicationSingleton.getInstance(getContext()).putBooleanValueSP(
                MainApplicationSingleton.INAPPLOCK_PARAM, inappLock.isChecked
            )
        }
    }

    override fun onResume() {
        super.onResume()
        val flag1 = MainApplicationSingleton.getInstance(getContext()).getBooleanValueSP(
            MainApplicationSingleton.STARTUPLOCK_PARAM
        )
        val flag2 = MainApplicationSingleton.getInstance(getContext()).getBooleanValueSP(
            MainApplicationSingleton.INAPPLOCK_PARAM
        )

        val view = view
        if (view != null) {
            val startLock = view.findViewById<View>(R.id.sw_startuplock) as Switch
            startLock.isChecked = flag1
            val inappLock = view.findViewById<View>(R.id.sw_inapplock) as Switch
            inappLock.isChecked = flag2
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_myaccount, container, false)
    }

    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        snackView = view.findViewById(R.id.cl)
        if (null == savedInstanceState) {
            user = arguments!!.getParcelable(ContactItem.USER_CONTACT)
        } else {
            user = savedInstanceState.getParcelable(ContactItem.USER_CONTACT)
        }
        val setpwd = view.findViewById<View>(R.id.ll_setpwd)
        setpwd.setOnClickListener { showLockPwdDialog() }
    }

    fun showLockPwdDialog() {
        LockPasswordFragment.newInstance(user, this).show(
            appCompatActivity.supportFragmentManager, "LockPasswordDialog"
        )
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
    private fun startProfileInfoActivity() {
        val intent = Intent(activity, ProfileInfoActivity::class.java)
        intent.putExtra(ContactItem.USER_CONTACT, user as Parcelable?)
//        NOTE: there is a significant difference between the following two calls

//        THIS WILL DELIVER THE RESULT, TO PARENT ACTIVITY AND THE PARENT ACTIVITY IF OVERRIDDEN
//        MUST CALL SUPER.STARTACTIVITYFORRESULT IN ITS OVERRIDDEN METHOD FOR THE RESULT
//        TO BE DELIVERED TO THIS FRAGMENT
        startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_PROFILEINFO_RQ_CODE)

//        THIS WILL DELIVER THE RESULT, TO PARENT ACTIVITY AND THE PARENT ACTIVITY IF OVERRIDDEN
//        MUST MANUALLY INVOKE THE FRAGMENTS IN ITS OVERRIDDEN METHOD FOR THE RESULT
//        TO BE DELIVERED TO THIS FRAGMENT
//        getAppCompatActivity().startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_CONTACTSELECT_RQ_CODE);
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDialogPositiveClick(dialog: DialogFragment?) {
        if (dialog is LockPasswordFragment) {
            val lockPasswordFragment = dialog
            val s = MainApplicationSingleton.getInstance(getContext()).getStringValueSP(
                MainApplicationSingleton.LOCKPWD_PARAM
            )
            Log.d(TAG, "onDialogPositiveClick$s")
        }
    }

    override fun onDialogNegativeClick(dialog: DialogFragment?) {
        if (dialog is LockPasswordFragment) {
            val lockPasswordFragment = dialog
            val s = MainApplicationSingleton.getInstance(getContext()).getStringValueSP(
                MainApplicationSingleton.LOCKPWD_PARAM
            )
            Log.d(TAG, "onDialogNegativeClick$s")
        }
    }

    companion object {
        const val TAG = "MyAccountFrag"

        fun newInstance(listener: ProfileListener?, user: ContactItem?): MyAccountFragment {
            val fragment = MyAccountFragment()
            if (listener is ProfileTopicListener) fragment.setProfileTopicListener(listener)
            fragment.setUser(user)
            val args = Bundle()
            args.putParcelable(ContactItem.USER_CONTACT, user)
            fragment.arguments = args
            return fragment
        }
    }
}
