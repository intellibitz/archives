package intellibitz.intellidroid.account

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Parcelable
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import com.google.android.material.snackbar.Snackbar
import com.google.i18n.phonenumbers.PhoneNumberUtil
import intellibitz.intellidroid.IntellibitzUserFragment
import intellibitz.intellidroid.R
import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.util.MainApplicationSingleton
import org.json.JSONException
import org.json.JSONObject

class VerifyCodeFragment :
    IntellibitzUserFragment(),
    GetEmailVerificationTask.GetEmailVerificationTaskListener,
    VerifyCodeTask.VerifyCodeTaskListener {

    private var viewLayout: View? = null
    private var snackView: View? = null
    private var snackbar: Snackbar? = null
    private var btnPermission: Button? = null
    private var viewPermission: View? = null

    private var mobileActivateListener: MobileActivateListener? = null
    private var etOtp1: EditText? = null
    private var etOtp2: EditText? = null
    private var etOtp3: EditText? = null
    private var etOtp4: EditText? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MobileActivateListener) {
            mobileActivateListener = context
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewLayout = inflater.inflate(R.layout.fragment_verifycode, container, false)
        return viewLayout
    }

    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (null == savedInstanceState) {
            user = arguments?.getParcelable(ContactItem.USER_CONTACT)
        } else {
            user = savedInstanceState.getParcelable(ContactItem.USER_CONTACT)
        }
        btnPermission = view.findViewById(R.id.btn_perm)
        btnPermission?.setOnClickListener {
            val act = activity ?: return@setOnClickListener
            requestReadPhoneStatePermissions(act)
        }
        viewPermission = view.findViewById(R.id.ll_perm)
        getSnackView()

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

        val resendCodeListener = View.OnClickListener {
            showProgress()
            execGetEmailVerification()
            resendOtp.isEnabled = false
            resendOtpTimer.start()
        }
        resendOtp.setOnClickListener(resendCodeListener)

        val rlLogin = view.findViewById<View>(R.id.rl_login)
        rlLogin?.setOnClickListener(resendCodeListener)

        val llContinue = view.findViewById<View>(R.id.ll_continue)
        val btnContinue = view.findViewById<View>(R.id.btn_continue)
        val continueListener = View.OnClickListener {
            performActivateTask()
        }
        llContinue?.setOnClickListener(continueListener)
        btnContinue?.setOnClickListener(continueListener)

        etOtp1 = view.findViewById(R.id.et_otp_1)
        etOtp2 = view.findViewById(R.id.et_otp_2)
        etOtp3 = view.findViewById(R.id.et_otp_3)
        etOtp4 = view.findViewById(R.id.et_otp_4)

        etOtp1?.setOnKeyListener { _, _, keyEvent ->
            if (KeyEvent.ACTION_UP == keyEvent.action) {
                if ((etOtp1?.text?.length ?: 0) > 0) {
                    etOtp2?.requestFocus()
                }
            }
            false
        }
        etOtp2?.setOnKeyListener { _, _, keyEvent ->
            if (KeyEvent.ACTION_UP == keyEvent.action) {
                if ((etOtp2?.text?.length ?: 0) > 0) {
                    etOtp3?.requestFocus()
                }
            }
            false
        }
        etOtp3?.setOnKeyListener { _, _, keyEvent ->
            if (KeyEvent.ACTION_UP == keyEvent.action) {
                if ((etOtp3?.text?.length ?: 0) > 0) {
                    etOtp4?.requestFocus()
                }
            }
            false
        }
        etOtp4?.setOnKeyListener { _, _, keyEvent ->
            if (KeyEvent.ACTION_UP == keyEvent.action) {
                if ((etOtp4?.text?.length ?: 0) > 0) {
                    performActivateTask()
                }
            }
            false
        }
        etOtp4?.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                performActivateTask()
                true
            } else {
                false
            }
        }
        unPackUser()
        etOtp1?.requestFocus()
    }

    private fun unPackUser(@Nullable savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            val u = savedInstanceState.getParcelable<ContactItem>(ContactItem.USER_CONTACT)
            if (u != null) {
                packUser(u)
                unPackUser()
            }
        }
    }

    private fun unPackUser() {
        val otp = user?.otp
        if (!TextUtils.isEmpty(otp) && 4 == otp?.length) {
            etOtp1?.setText(otp.substring(0, 1))
            etOtp2?.setText(otp.substring(1, 2))
            etOtp3?.setText(otp.substring(2, 3))
            etOtp4?.setText(otp.substring(3, 4))
        }
    }

    fun packUser(u: ContactItem): ContactItem? {
        user?.device = u.device
        user?.name = u.name
        user?.mobile = u.mobile
        user?.otp = u.otp
        user?.country = u.country
        user?.countryName = u.countryName
        user?.accountExists = u.accountExists
        user?.profilePic = u.profilePic
        return user
    }

    fun getSnackView(): View? {
        if (null == snackView) {
            snackView = viewLayout?.findViewById(R.id.username)
        }
        return snackView
    }

    fun alertReadPhoneState() {
        if (null == snackbar) {
            snackbar = makeReadPhoneStateSnack(getSnackView(), activity)
        }
        if (isReadPhoneStatePermissionGranted(context)) {
            snackbar?.dismiss()
            viewPermission?.visibility = View.GONE
        } else {
            viewPermission?.visibility = View.VISIBLE
            mayRequestReadPhoneState(snackbar, getSnackView())
        }
    }

    override fun onReadPhoneStatePermissionsGranted() {
    }

    override fun onPhoneReadStatePermissionsDenied() {
        snackbar = makeReadPhoneStateSnack(snackbar, getSnackView(), activity)
    }

    fun invalidOTPAlert(valStr: String?) {
        etOtp4?.error = valStr
    }

    fun showProgress() {
        showProgress(
            context, getString(R.string.verify_code),
            getString(R.string.note_verifycode), true
        )
    }

    fun showVerifyProgress() {
        showProgress(
            context, getString(R.string.verify_code),
            getString(R.string.please_wait), true
        )
    }

    fun performActivateTask() {
        if (MainApplicationSingleton.CheckNetworkConnection.isNetworkConnectionAvailable(context)) {
            invalidOTPAlert(null)
            val t1 = etOtp1?.editableText?.toString() ?: ""
            if (TextUtils.isEmpty(t1)) showOtpError()
            val t2 = etOtp2?.editableText?.toString() ?: ""
            if (TextUtils.isEmpty(t2)) showOtpError()
            val t3 = etOtp3?.editableText?.toString() ?: ""
            if (TextUtils.isEmpty(t3)) showOtpError()
            val t4 = etOtp4?.editableText?.toString() ?: ""
            if (TextUtils.isEmpty(t4)) showOtpError()

            val otp = t1 + t2 + t3 + t4
            if (TextUtils.isEmpty(otp) || otp.length < 4) {
                showOtpError()
                return
            }

            showVerifyProgress()
            user?.otp = otp
            execVerifyCode()
        }
    }

    fun showOtpError() {
        etOtp1?.requestFocus()
    }

    private fun normalizeUserMobile(): String? {
        user?.mobile?.let {
            user?.mobile = PhoneNumberUtil.normalizeDigitsOnly(it)
        }
        return user?.mobile
    }

    @NonNull
    private fun getUserMobileWithCC(): String {
        normalizeUserMobile()
        val dial = user?.country?.dialCode ?: ""
        val mob = user?.mobile ?: ""
        return dial + mob
    }

    private fun execVerifyCode() {
        val u = user ?: return
        val verifyCodeTask = VerifyCodeTask(
            u.email, u.otp,
            MainApplicationSingleton.AUTH_ACCOUNT_VERIFY_CODE, context
        )
        verifyCodeTask.requestTimeoutMillis = 30000
        verifyCodeTask.setVerifyCodeTaskListener(this)
        verifyCodeTask.execute()
    }

    override fun onPostVerifyCodeResponse(response: JSONObject?, email: String?, user: ContactItem?) {
        hideProgress()
        if (null == response) {
            invalidOTPAlert("Network failed - Please try again")
            return
        }
        try {
            val status = response.getInt("status")
            if (0 == status || 1 == status) {
                okActivity()
            } else if (2 == status || 99 == status) {
                onPostVerifyCodeErrorResponse(response)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
            Log.e(TAG, e.message ?: "")
        }
    }

    override fun onPostVerifyCodeErrorResponse(response: JSONObject?) {
        hideProgress()
        if (response != null) {
            Log.e(TAG, response.toString())
            invalidOTPAlert("Activation failed - Please try again")
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

    override fun onPostGetEmailVerificationResponse(response: JSONObject?, email: String?, user: ContactItem?) {
        try {
            val status = response?.getInt("status") ?: 0
            if (1 == status || 2 == status) {
            } else if (99 == status) {
                onPostGetEmailVerificationErrorResponse(response)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
            onPostGetEmailVerificationErrorResponse(response)
        }
        hideProgress()
    }

    override fun onPostGetEmailVerificationErrorResponse(response: JSONObject?) {
        Log.e(TAG, "onPostGetEmailVerificationErrorResponse: $response")
        if (response != null) {
            setError(response.toString())
        }
        hideProgress()
    }

    private fun execGetEmailVerification() {
        val u = user ?: return
        val getEmailVerificationTask = GetEmailVerificationTask(
            u.email,
            MainApplicationSingleton.AUTH_ACCOUNT_GET_EMAIL_VERIFICATION, context
        )
        getEmailVerificationTask.requestTimeoutMillis = 30000
        getEmailVerificationTask.setGetEmailVerificationTaskListener(this)
        getEmailVerificationTask.execute()
    }

    fun setError(text: String?) {
        if (etOtp4 != null) {
            etOtp4?.error = text
        }
    }

    interface MobileActivateListener {
        fun onActivateSuccess(user: ContactItem?)
    }

    companion object {
        private const val TAG = "VerifyCodeFragment"

        @JvmStatic
        fun newInstance(user: ContactItem?): VerifyCodeFragment {
            val fragment = VerifyCodeFragment()
            fragment.user = user
            val args = Bundle()
            args.putParcelable(ContactItem.USER_CONTACT, user)
            fragment.arguments = args
            return fragment
        }
    }
}
