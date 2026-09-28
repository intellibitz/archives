package intellibitz.intellidroid.account

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.annotation.NonNull
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import intellibitz.intellidroid.R
import intellibitz.intellidroid.data.ContactItem

class NewPasswordFragment : BottomSheetDialogFragment() {

    private var snackView: View? = null
    private var tvPwd1: EditText? = null
    private var tvPwd2: EditText? = null
    private var mListener: OnNewPasswordFragmentListener? = null
    var user: ContactItem? = null
    var mode: Int = -1

    fun addNewPasswordFragmentListener(listener: OnNewPasswordFragmentListener?) {
        mListener = listener
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnNewPasswordFragmentListener) {
            addNewPasswordFragmentListener(context)
        }
    }

    @NonNull
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.fragment_newpassword, view as? ViewGroup)
        if (null == savedInstanceState) {
            user = arguments?.getParcelable(ContactItem.USER_CONTACT)
        } else {
            user = savedInstanceState.getParcelable(ContactItem.USER_CONTACT)
        }

        tvPwd1 = view.findViewById(R.id.tv_password1)
        tvPwd2 = view.findViewById(R.id.tv_password2)
        tvPwd1?.requestFocus()

        val rlLogin = view.findViewById<View>(R.id.rl_login)
        val btnLogin = view.findViewById<View>(R.id.btn_login)
        val ibtnLogin = view.findViewById<View>(R.id.ibtn_login)
        val onClickListener = View.OnClickListener {
            performOkTask()
        }
        rlLogin?.setOnClickListener(onClickListener)
        btnLogin?.setOnClickListener(onClickListener)
        ibtnLogin?.setOnClickListener(onClickListener)

        val builder = AlertDialog.Builder(requireContext())
        builder.setView(view)
        return builder.create()
    }

    fun performOkTask() {
        val pwd = tvPwd1?.text.toString()
        if (!TextUtils.isEmpty(pwd) &&
            pwd.length >= 6 &&
            pwd == tvPwd2?.text.toString()
        ) {
            user?.pwd = pwd
            mListener?.onNewPasswordDialogPositiveClick(this)
        } else {
            setError("Passwords does not match - min 6 char")
        }
    }

    fun getSnackView(): View? {
        if (null == snackView) {
            val view = view
            if (view != null) {
                snackView = view.findViewById(R.id.username)
            }
        }
        return snackView
    }

    fun setError(text: String?) {
        if (tvPwd2 != null) {
            tvPwd2?.error = text
        }
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

    interface OnNewPasswordFragmentListener {
        fun onNewPasswordDialogPositiveClick(dialog: DialogFragment?)
        fun onNewPasswordDialogNegativeClick(dialog: DialogFragment?)
    }

    companion object {
        private const val TAG = "NewPasswordFrag"

        @JvmStatic
        fun newInstance(user: ContactItem?): NewPasswordFragment {
            val fragment = NewPasswordFragment()
            val args = Bundle()
            args.putParcelable(ContactItem.USER_CONTACT, user)
            fragment.arguments = args
            return fragment
        }
    }
}
