package intellibitz.intellidroid.widget

import android.app.Dialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.CountDownTimer
import android.text.TextUtils
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import intellibitz.intellidroid.R
import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.util.MainApplicationSingleton

class UnLockPasswordFragment : BottomSheetDialogFragment() {

    private var user: ContactItem? = null
    private var progressDialog: ProgressDialog? = null
    private var contentView: View? = null
    private var snackView: View? = null

    private var etOtp1: EditText? = null
    private var etOtp2: EditText? = null
    private var etOtp3: EditText? = null
    private var etOtp4: EditText? = null
    private var mListener: OnUnLockPasswordFragmentListener? = null

    fun addUnLockPasswordFragmentListener(listener: OnUnLockPasswordFragmentListener?) {
        mListener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.fragment_unlockpwd, null)
        contentView = view

        val arguments = arguments
        if (arguments != null) {
            user = arguments.getParcelable(ContactItem.USER_CONTACT)
        }

        val resendOtp = view.findViewById<Button>(R.id.btn_resendOTP)
        resendOtp.isEnabled = false
        val resend = resendOtp.text.toString()
        val resendOtpTimer = object : CountDownTimer(15000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                resendOtp.text = "$resend ${millisUntilFinished / 1000}"
            }

            override fun onFinish() {
                resendOtp.text = resend
                resendOtp.isEnabled = true
            }
        }.start()
        resendOtp.setOnClickListener {
            resendOtp.isEnabled = false
            resendOtpTimer.start()
        }

        val resendOtpCall = view.findViewById<Button>(R.id.btn_resendOTPCall)
        resendOtpCall.isEnabled = false
        val txt = resendOtpCall.text.toString()
        val resendOtpCallTimer = object : CountDownTimer(15000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                resendOtpCall.text = "$txt ${millisUntilFinished / 1000}"
            }

            override fun onFinish() {
                resendOtpCall.text = txt
                resendOtpCall.isEnabled = true
            }
        }.start()
        resendOtpCall.setOnClickListener {
            resendOtpCall.isEnabled = false
            resendOtpCallTimer.start()
        }

        val rlLogin = view.findViewById<View>(R.id.rl_login)
        rlLogin.setOnClickListener {
            resendOtpCall.isEnabled = false
            resendOtpCallTimer.start()
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

        otp1?.requestFocus()

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(R.string.unlock_app)
        builder.setView(view)
            .setPositiveButton(R.string.menu_title_ok) { _, _ ->
                performOkTask()
            }
            .setNegativeButton(R.string.menu_title_cancel) { _, _ ->
                mListener?.onDialogNegativeClick(this@UnLockPasswordFragment)
            }
        val alertDialog = builder.create()
        alertDialog.setOnShowListener { dialog ->
            val positiveButton = (dialog as AlertDialog).getButton(DialogInterface.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                performOkTask()
            }
        }

        return alertDialog
    }

    fun getSnackView(): View? {
        if (null == snackView) {
            snackView = contentView?.findViewById(R.id.mr_art)
        }
        return snackView
    }

    fun performOkTask() {
        invalidPwdAlert(null)
        val ctx = context ?: return
        val pwd = MainApplicationSingleton.getInstance(ctx).getStringValueSP(
            MainApplicationSingleton.LOCKPWD_PARAM
        )
        if (!TextUtils.isEmpty(pwd)) {
            val t1 = etOtp1?.editableText?.toString() ?: ""
            if (TextUtils.isEmpty(t1)) showPwdError()
            val t2 = etOtp2?.editableText?.toString() ?: ""
            if (TextUtils.isEmpty(t2)) showPwdError()
            val t3 = etOtp3?.editableText?.toString() ?: ""
            if (TextUtils.isEmpty(t3)) showPwdError()
            val t4 = etOtp4?.editableText?.toString() ?: ""
            if (TextUtils.isEmpty(t4)) showPwdError()

            val s = t1 + t2 + t3 + t4
            if (TextUtils.isEmpty(s) || s.length < 4) {
                invalidPwdAlert("Wrong Password - Please try again")
                showPwdError()
                return
            }

            if (s != pwd) {
                invalidPwdAlert("Wrong Password - Please try again")
                showPwdError()
                return
            }
        }
        mListener?.onDialogPositiveClick(this)
        dismiss()
    }

    fun invalidPwdAlert(valStr: String?) {
        etOtp1?.error = valStr
    }

    fun showPwdError() {
        etOtp1?.setText("")
        etOtp2?.setText("")
        etOtp3?.setText("")
        etOtp4?.setText("")
        etOtp1?.requestFocus()
    }

    fun setError(text: String?) {
        etOtp1?.error = text
    }

    interface OnUnLockPasswordFragmentListener {
        fun onDialogPositiveClick(dialog: DialogFragment)
        fun onDialogNegativeClick(dialog: DialogFragment)
    }

    companion object {
        private const val TAG = "UnLockPwdFrag"

        @JvmStatic
        fun newInstance(user: ContactItem?, listener: OnUnLockPasswordFragmentListener?): UnLockPasswordFragment {
            val fragment = UnLockPasswordFragment()
            fragment.addUnLockPasswordFragmentListener(listener)
            val args = Bundle()
            args.putParcelable(ContactItem.USER_CONTACT, user)
            fragment.arguments = args
            return fragment
        }
    }
}
