package intellibitz.intellidroid.widget

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import intellibitz.intellidroid.MainActivity
import intellibitz.intellidroid.R
import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.util.MainApplicationSingleton

class LockPasswordFragment : BottomSheetDialogFragment() {

    private var user: ContactItem? = null
    private var contentView: View? = null
    private var snackView: View? = null

    private var etOtp1: EditText? = null
    private var etOtp2: EditText? = null
    private var etOtp3: EditText? = null
    private var etOtp4: EditText? = null
    private var mListener: OnLockPasswordFragmentListener? = null

    fun addLockPasswordFragmentListener(listener: OnLockPasswordFragmentListener?) {
        mListener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.fragment_lockpwd, null)
        contentView = view

        val arguments = arguments
        if (arguments != null) {
            user = arguments.getParcelable(ContactItem.USER_CONTACT)
        }

        val otp1 = view.findViewById<EditText>(R.id.et_otp_1)
        val otp2 = view.findViewById<EditText>(R.id.et_otp_2)
        val otp3 = view.findViewById<EditText>(R.id.et_otp_3)
        val otp4 = view.findViewById<EditText>(R.id.et_otp_4)

        etOtp1 = otp1
        etOtp2 = otp2
        etOtp3 = otp3
        etOtp4 = otp4

        otp1?.setOnKeyListener { _, _, keyEvent ->
            if (KeyEvent.ACTION_UP == keyEvent.action) {
                if (otp1.text.isNotEmpty()) otp2?.requestFocus()
            }
            false
        }
        otp2?.setOnKeyListener { _, _, keyEvent ->
            if (KeyEvent.ACTION_UP == keyEvent.action) {
                if (otp2.text.isNotEmpty()) otp3?.requestFocus()
            }
            false
        }
        otp3?.setOnKeyListener { _, _, keyEvent ->
            if (KeyEvent.ACTION_UP == keyEvent.action) {
                if (otp3.text.isNotEmpty()) otp4?.requestFocus()
            }
            false
        }
        otp4?.setOnKeyListener { _, _, _ ->
            false
        }
        otp4?.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                return@setOnEditorActionListener true
            }
            false
        }

        unPackUser()
        otp1?.requestFocus()

        val builder = AlertDialog.Builder(requireContext())
        builder.setView(view)
            .setPositiveButton(R.string.ok) { _, _ ->
                performOkTask()
            }
            .setNegativeButton(R.string.cancel) { _, _ ->
                mListener?.onDialogNegativeClick(this@LockPasswordFragment)
            }
        return builder.create()
    }

    private fun unPackUser() {
        val ctx = context ?: return
        val pwd = MainApplicationSingleton.getInstance(ctx).getStringValueSP(
            MainApplicationSingleton.LOCKPWD_PARAM
        )
        if (!TextUtils.isEmpty(pwd) && pwd.length == 4) {
            etOtp1?.setText(pwd.substring(0, 1))
            etOtp2?.setText(pwd.substring(1, 2))
            etOtp3?.setText(pwd.substring(2, 3))
            etOtp4?.setText(pwd.substring(3, 4))
        }
    }

    fun getSnackView(): View? {
        if (null == snackView) {
            snackView = contentView?.findViewById(R.id.mr_art)
        }
        return snackView
    }

    fun invalidOTPAlert(valStr: String?) {
        etOtp4?.error = valStr
    }

    fun performOkTask() {
        invalidOTPAlert(null)

        val t1 = etOtp1?.editableText?.toString() ?: ""
        if (TextUtils.isEmpty(t1)) showOtpError()
        val t2 = etOtp2?.editableText?.toString() ?: ""
        if (TextUtils.isEmpty(t2)) showOtpError()
        val t3 = etOtp3?.editableText?.toString() ?: ""
        if (TextUtils.isEmpty(t3)) showOtpError()
        val t4 = etOtp4?.editableText?.toString() ?: ""
        if (TextUtils.isEmpty(t4)) showOtpError()

        val pwd = t1 + t2 + t3 + t4
        if (TextUtils.isEmpty(pwd) || pwd.length < 4) {
            showOtpError()
            return
        }

        val ctx = context
        if (ctx != null) {
            MainApplicationSingleton.getInstance(ctx).putStringValueSP(
                MainApplicationSingleton.LOCKPWD_PARAM, pwd
            )
        }
        mListener?.onDialogPositiveClick(this)
    }

    fun showOtpError() {
        etOtp1?.requestFocus()
    }

    fun setError(text: String?) {
        etOtp4?.error = text
    }

    private fun startProfileInfoActivity() {
        val intent = Intent(activity, MainActivity::class.java)
        intent.putExtra(ContactItem.USER_CONTACT, user)
        startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_PROFILEINFO_RQ_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (MainApplicationSingleton.ACTIVITY_PROFILEINFO_RQ_CODE == requestCode) {
            val act = activity ?: return
            if (Activity.RESULT_OK == resultCode) {
                if (data != null) {
                    val item = data.getParcelableExtra<ContactItem>(ContactItem.USER_CONTACT)
                    val intent = act.intent
                    intent.putExtra(ContactItem.USER_CONTACT, item)
                    act.setResult(Activity.RESULT_OK, intent)
                    act.finish()
                }
            } else if (Activity.RESULT_CANCELED == resultCode) {
                val intent = act.intent
                intent.putExtra(ContactItem.USER_CONTACT, user)
                act.setResult(Activity.RESULT_CANCELED, intent)
                act.finish()
                Log.e(TAG, "onActivityResult: 0 Contacts selected - ")
            }
        }
    }

    interface OnLockPasswordFragmentListener {
        fun onDialogPositiveClick(dialog: DialogFragment)
        fun onDialogNegativeClick(dialog: DialogFragment)
    }

    companion object {
        private const val TAG = "LockPwdFrag"

        @JvmStatic
        fun newInstance(user: ContactItem?, listener: OnLockPasswordFragmentListener?): LockPasswordFragment {
            val fragment = LockPasswordFragment()
            fragment.addLockPasswordFragmentListener(listener)
            val args = Bundle()
            args.putParcelable(ContactItem.USER_CONTACT, user)
            fragment.arguments = args
            return fragment
        }
    }
}
