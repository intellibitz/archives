package intellibitz.intellidroid.account

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import intellibitz.intellidroid.R
import intellibitz.intellidroid.data.ContactItem

class UserSignupFragment : BottomSheetDialogFragment() {

    private var viewLayout: View? = null
    private var snackView: View? = null
    private var mListener: OnUserSignupFragmentListener? = null
    var user: ContactItem? = null

    fun addUserSignupFragmentListener(listener: OnUserSignupFragmentListener?) {
        mListener = listener
    }

    @NonNull
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.fragment_usersignup, this.view as? ViewGroup)
        viewLayout = view

        val arguments = arguments
        if (arguments != null) {
            user = arguments.getParcelable(ContactItem.USER_CONTACT)
        }

        val signup1 = view.findViewById<View>(R.id.rl_signup)
        val signup2 = view.findViewById<View>(R.id.btn_signup)
        val signup3 = view.findViewById<View>(R.id.ibtn_signup)
        val signupOnClickListener = View.OnClickListener {
            mListener?.onUserSignupDialogPositiveClick(this)
        }
        signup1?.setOnClickListener(signupOnClickListener)
        signup2?.setOnClickListener(signupOnClickListener)
        signup3?.setOnClickListener(signupOnClickListener)

        val tryagain1 = view.findViewById<View>(R.id.rl_tryagain)
        val tryagain2 = view.findViewById<View>(R.id.btn_tryagain)
        val tryagain3 = view.findViewById<View>(R.id.ibtn_tryagain)
        val cancelOnClickListener = View.OnClickListener {
            mListener?.onUserSignupDialogNegativeClick(this)
        }
        tryagain1?.setOnClickListener(cancelOnClickListener)
        tryagain2?.setOnClickListener(cancelOnClickListener)
        tryagain3?.setOnClickListener(cancelOnClickListener)

        val builder = AlertDialog.Builder(requireContext())
        builder.setView(view)
        return builder.create()
    }

    fun getSnackView(): View? {
        if (null == snackView) {
            val v = viewLayout ?: view
            snackView = v?.findViewById(R.id.username)
        }
        return snackView
    }

    protected fun okActivity() {
        val act = activity ?: return
        val intent = act.intent
        intent.putExtra(ContactItem.USER_CONTACT, user as? Parcelable)
        act.setResult(Activity.RESULT_OK, intent)
        act.finish()
    }

    protected fun cancelActivity() {
        val act = activity ?: return
        val intent = act.intent
        intent.putExtra(ContactItem.USER_CONTACT, user as? Parcelable)
        act.setResult(Activity.RESULT_CANCELED, intent)
        act.finish()
    }

    interface OnUserSignupFragmentListener {
        fun onUserSignupDialogPositiveClick(dialog: DialogFragment?)
        fun onUserSignupDialogNegativeClick(dialog: DialogFragment?)
    }

    companion object {
        private const val TAG = "UserSignupFrag"

        @JvmStatic
        fun newInstance(user: ContactItem?, listener: OnUserSignupFragmentListener?): UserSignupFragment {
            val fragment = UserSignupFragment()
            fragment.addUserSignupFragmentListener(listener)
            val args = Bundle()
            args.putParcelable(ContactItem.USER_CONTACT, user)
            fragment.arguments = args
            return fragment
        }
    }
}
