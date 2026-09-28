package intellibitz.intellidroid.fragment

import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Parcelable
import android.provider.Settings
import android.telephony.TelephonyManager
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
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.i18n.phonenumbers.PhoneNumberUtil
import intellibitz.intellidroid.IntellibitzPermissionFragment
import intellibitz.intellidroid.IntellibitzUserFragment
import intellibitz.intellidroid.R
import intellibitz.intellidroid.activity.ProfileInfoActivity
import intellibitz.intellidroid.content.UserContentProvider
import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.service.ContactService
import intellibitz.intellidroid.service.UserEmailIntentService
import intellibitz.intellidroid.task.MobileActivateTask
import intellibitz.intellidroid.task.MobileGetCodeTask
import intellibitz.intellidroid.task.OTPCallTask
import intellibitz.intellidroid.util.MainApplicationSingleton
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

/**
 * A simple [Fragment] subclass.
 */
class OTPFragment : IntellibitzUserFragment(),
    MobileGetCodeTask.MobileGetCodeTaskListener,
    MobileActivateTask.MobileActivateTaskListener,
    OTPCallTask.OTPCallTaskListener {

    companion object {
        private const val TAG = "OTPFragment"

        fun newInstance(user: ContactItem): OTPFragment {
            val fragment = OTPFragment()
            fragment.setUser(user)
            val args = Bundle()
            args.putParcelable(ContactItem.USER_CONTACT, user)
            fragment.arguments = args
            return fragment
        }
    }

    private var mobileGetCodeTask: MobileGetCodeTask? = null
    private var mobileActivateTask: MobileActivateTask? = null
    private var otpCallTask: OTPCallTask? = null
    private var view: View? = null
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
        if (context is MobileActivateListener) mobileActivateListener = context
    }

    override fun onResume() {
        super.onResume()
        alertReadPhoneState()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        view = inflater.inflate(R.layout.fragment_otp, container, false)
        return view
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
            requestReadPhoneStatePermissions(activity)
        }
        viewPermission = view.findViewById(R.id.ll_perm)
        snackView
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
            showCallProgress()
            execGetOTP()
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
            showProgress()
            execOTPCallTask()
            resendOtpCall.isEnabled = false
            resendOtpCallTimer.start()
        }
        val rlLogin = view.findViewById<View>(R.id.rl_login)
        rlLogin.setOnClickListener {
            showProgress()
            execOTPCallTask()
            resendOtpCall.isEnabled = false
            resendOtpCallTimer.start()
        }
        etOtp1 = view.findViewById(R.id.et_otp_1)
        etOtp2 = view.findViewById(R.id.et_otp_2)
        etOtp3 = view.findViewById(R.id.et_otp_3)
        etOtp4 = view.findViewById(R.id.et_otp_4)
        etOtp1?.setOnKeyListener { _, _, keyEvent ->
            if (KeyEvent.ACTION_UP == keyEvent.action) {
                if (etOtp1?.text?.length ?: 0 > 0) etOtp2?.requestFocus()
            }
            false
        }
        etOtp2?.setOnKeyListener { _, _, keyEvent ->
            if (KeyEvent.ACTION_UP == keyEvent.action) {
                if (etOtp2?.text?.length ?: 0 > 0) etOtp3?.requestFocus()
            }
            false
        }
        etOtp3?.setOnKeyListener { _, _, keyEvent ->
            if (KeyEvent.ACTION_UP == keyEvent.action) {
                if (etOtp3?.text?.length ?: 0 > 0) etOtp4?.requestFocus()
            }
            false
        }
        etOtp4?.setOnKeyListener { _, _, keyEvent ->
            if (KeyEvent.ACTION_UP == keyEvent.action) {
                if (etOtp4?.text?.length ?: 0 > 0) performActivateTask()
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
        val otp = user.otp
        if (!TextUtils.isEmpty(otp) && 4 == otp.length) {
            etOtp1?.setText(otp.substring(0, 1))
            etOtp2?.setText(otp.substring(1, 2))
            etOtp3?.setText(otp.substring(2, 3))
            etOtp4?.setText(otp.substring(3, 4))
        }
    }

    fun packUser(u: ContactItem): ContactItem {
        user.device = u.device
        user.name = u.name
        user.mobile = u.mobile
        user.otp = u.otp
        user.country = u.country
        user.countryName = u.countryName
        user.accountExists = u.accountExists
        user.profilePic = u.profilePic
        return user
    }

    val snackView: View?
        get() {
            if (null == snackView) snackView = view?.findViewById(R.id.username)
            return snackView
        }

    fun alertReadPhoneState() {
        if (null == snackbar) {
            snackbar = makeReadPhoneStateSnack(snackView, activity)
        }
        if (isReadPhoneStatePermissionGranted(context)) {
            snackbar?.dismiss()
            viewPermission?.visibility = View.GONE
        } else {
            viewPermission?.visibility = View.VISIBLE
            mayRequestReadPhoneState(snackbar, snackView)
        }
    }

    override fun onReadPhoneStatePermissionsGranted() {
    }

    override fun onPhoneReadStatePermissionsDenied() {
        snackbar = makeReadPhoneStateSnack(snackbar, snackView, activity)
    }

    fun invalidOTPAlert(val: String?) {
        etOtp4?.error = val
    }

    fun showCallProgress() {
        showProgress(
            context,
            getString(R.string.login),
            getString(R.string.request_otp_by_call),
            true
        )
    }

    fun showProgress() {
        showProgress(
            context,
            getString(R.string.login),
            getString(R.string.user_activation),
            true
        )
    }

    fun performActivateTask() {
        if (MainApplicationSingleton.CheckNetworkConnection.isNetworkConnectionAvailable(context)) {
            invalidOTPAlert(null)
            val t1 = etOtp1?.editableText?.toString()
            if (TextUtils.isEmpty(t1)) showOtpError()
            val t2 = etOtp2?.editableText?.toString()
            if (TextUtils.isEmpty(t2)) showOtpError()
            val t3 = etOtp3?.editableText?.toString()
            if (TextUtils.isEmpty(t3)) showOtpError()
            val t4 = etOtp4?.editableText?.toString()
            if (TextUtils.isEmpty(t4)) showOtpError()

            val otp = t1 + t2 + t3 + t4
            if (TextUtils.isEmpty(otp) || otp.length < 4) {
                showOtpError()
                return
            }

            showProgress()
            initUserDeviceId()
            user.otp = otp
            execMobileActivateTask()
        }
    }

    fun showOtpError() {
        etOtp1?.requestFocus()
    }

    private fun normalizeUserMobile(): String {
        user.mobile = PhoneNumberUtil.normalizeDigitsOnly(user.mobile)
        return user.mobile
    }

    @NonNull
    private fun getUserMobileWithCC(): String {
        normalizeUserMobile()
        return user.country.dialCode + user.mobile
    }

    private fun savesUserInDB(user: ContactItem) {
        Log.d(TAG, "User: $user")
        try {
            val uri = UserContentProvider.savesUserInDB(user, context)
            val id = ContentUris.parseId(uri)
            MainApplicationSingleton.getInstance(context).putLongValueSP(
                MainApplicationSingleton.ID_PARAM, id
            )
            user._id = id
            Log.e(TAG, "SUCCESS - User insert: $uri")
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e(TAG, e.message)
        }
        savesUserInSP(user)
    }

    private fun initUserDeviceId() {
        user.deviceId = Build.SERIAL
        val androidId = Settings.Secure.getString(
            context?.contentResolver, Settings.Secure.ANDROID_ID
        )
        if (null == user.deviceId) {
            if (IntellibitzPermissionFragment.isReadPhoneStatePermissionGranted(context)) {
                val deviceId = (context?.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager).deviceId
                user.deviceId = deviceId
            }
            if (null == user.deviceId) {
                user.deviceId = androidId
            }
        }
        user.deviceName = Build.MODEL
        if (null == user.deviceName) {
            user.deviceName = Build.MANUFACTURER + Build.PRODUCT
            if (null == user.deviceName) {
                user.deviceName = Build.BRAND + Build.ID
            }
        }
        if (null == user.deviceId || null == user.deviceName) {
            Log.e(TAG, "User device id or device name is NULL" +
                    user.deviceId + " " + user.deviceName)
        }
    }

    private fun savesUserInSP(user: ContactItem) {
        val mainApplication = MainApplicationSingleton.getInstance(context)
        mainApplication.setUidCurrentUser(user.dataId)
        mainApplication.putStringValueSP(
            MainApplicationSingleton.UID_USER_LOGGED_IN_PARAM,
            user.dataId, false
        )
        mainApplication.putStringValueSP(MainApplicationSingleton.UID_PARAM, user.dataId)
        mainApplication.putStringValueSP(MainApplicationSingleton.TOKEN_PARAM, user.token)
        mainApplication.putStringValueSP(MainApplicationSingleton.NAME_PARAM, user.name)
        mainApplication.putStringValueSP(MainApplicationSingleton.COUNTRY_PARAM, user.country.toString())
        mainApplication.putStringValueSP(MainApplicationSingleton.COUNTRY_CODE_PARAM, user.country.dialCode)
        mainApplication.putStringValueSP(MainApplicationSingleton.MOBILE_PARAM, user.mobile)
        mainApplication.putStringValueSP(MainApplicationSingleton.DEVICE_PARAM, user.device)
        mainApplication.putStringValueSP(MainApplicationSingleton.OTP_PARAM, user.otp)
        mainApplication.putStringValueSP(MainApplicationSingleton.DEVICE_NAME_PARAM, user.deviceName)
        mainApplication.putStringValueSP(MainApplicationSingleton.DEVICE_ID_PARAM, user.deviceId)
        mainApplication.putStringValueSP(MainApplicationSingleton.DEVICE_REF_PARAM, user.deviceRef)
    }

    private fun execOTPCallTask() {
        otpCallTask = OTPCallTask(
            user.device, getUserMobileWithCC(),
            MainApplicationSingleton.AUTH_OTP_CALL
        )
        otpCallTask?.setOtpCallTaskListener(this)
        otpCallTask?.execute()
    }

    private fun execMobileActivateTask() {
        mobileActivateTask = MobileActivateTask(
            getUserMobileWithCC(), user.otp, user.name,
            user.deviceId, user.device, user.deviceName,
            MainApplicationSingleton.AUTH_MOBILE_ACTIVATE
        )
        mobileActivateTask?.setMobileActivateTaskListener(this)
        mobileActivateTask?.execute()
    }

    override fun onPostMobileActivateExecute(response: JSONObject?) {
        hideProgress()
        if (null == response) {
            invalidOTPAlert("Network failed - Please try again")
            return
        }
        try {
            val status = response.getInt("status")
            if (1 == status) {
                activateSuccess(response)
            } else if (2 == status) {
                onPostMobileActivateExecuteFail(response)
            } else if (99 == status) {
                onPostMobileActivateExecuteFail(response)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
            Log.e(TAG, e.message)
        }
    }

    @Throws(JSONException::class)
    private fun activateSuccess(response: JSONObject) {
        val uid = response.getString(MainApplicationSingleton.UID_PARAM)
        val token = response.getString(MainApplicationSingleton.TOKEN_PARAM)
        val deviceRef = response.getString(MainApplicationSingleton.DEVICE_REF_PARAM)
        user.dataId = uid
        user.token = token
        user.deviceRef = deviceRef
        ContactService.asyncUpdateContacts(user, context)
        savesUserInDB(user)
        if (1 == user.accountExists) {
            UserEmailIntentService.asyncEmailsFromCloudAndSavesInDb(user, context)
        }
        mobileActivateListener?.onActivateSuccess(user)
        val activity = activity
        val intent = activity?.intent
        intent?.putExtra(ContactItem.USER_CONTACT, user as Parcelable)
        activity?.setResult(Activity.RESULT_OK, intent)
        activity?.finish()
    }

    override fun onPostMobileActivateExecuteFail(response: JSONObject?) {
        hideProgress()
        if (response != null) {
            Log.e(TAG, response.toString())
            invalidOTPAlert("Activation failed - Please try again")
        }
    }

    override fun setMobileActivateTaskToNull() {
        mobileActivateTask = null
    }

    override fun onPostOTPCallExecute(response: JSONObject?) {
        var status = 0
        if (response != null) status = response.optInt("status")
        if (null == response || 99 == status || -1 == status) {
            onPostOTPCallExecuteFail(response)
        }
        hideProgress()
    }

    override fun onPostOTPCallExecuteFail(response: JSONObject?) {
        hideProgress()
        Log.e(TAG, "ERROR: $response")
    }

    override fun setOTPCallTaskToNull() {
        otpCallTask = null
    }

    override fun onPostMobileGetCodeExecute(response: JSONObject?) {
        try {
            val status = response?.getInt("status") ?: 0
            if (1 == status) {
            } else if (2 == status) {
            } else if (99 == status) {
                onPostMobileGetCodeExecuteFail(response)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
            onPostMobileGetCodeExecuteFail(response)
        }
        hideProgress()
    }

    override fun onPostMobileGetCodeExecuteFail(response: JSONObject?) {
        Log.e(TAG, "onPostMobileGetCodeExecuteFail: $response")
        if (response != null) setError(response.toString())
        hideProgress()
    }

    override fun setMobileGetCodeTaskToNull() {
        mobileGetCodeTask = null
    }

    private fun execGetOTP() {
        mobileGetCodeTask = MobileGetCodeTask(
            user.device, getUserMobileWithCC(),
            MainApplicationSingleton.MOBILE_GETCODE_URL
        )
        mobileGetCodeTask?.setMobileGetCodeTaskListener(this)
        mobileGetCodeTask?.execute()
    }

    fun setError(text: String) {
        etOtp4?.error = text
    }

    private fun startProfileInfoActivity() {
        val intent = Intent(activity, ProfileInfoActivity::class.java)
        intent.putExtra(ContactItem.USER_CONTACT, user as Parcelable)
        startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_PROFILEINFO_RQ_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (MainApplicationSingleton.ACTIVITY_PROFILEINFO_RQ_CODE == requestCode) {
            if (Activity.RESULT_OK == resultCode) {
                if (data != null) {
                    val item = data.getParcelableExtra<ContactItem>(ContactItem.USER_CONTACT)
                    val activity = activity
                    val intent = activity?.intent
                    intent?.putExtra(ContactItem.USER_CONTACT, item as Parcelable)
                    activity?.setResult(Activity.RESULT_OK, intent)
                    activity?.finish()
                }
            } else if (Activity.RESULT_CANCELED == resultCode) {
                val activity = activity
                val intent = activity?.intent
                intent?.putExtra(ContactItem.USER_CONTACT, user as Parcelable)
                activity?.setResult(Activity.RESULT_CANCELED, intent)
                activity?.finish()
                Log.e(TAG, "onActivityResult: 0 Contacts selected - ")
            }
        }
    }

    interface MobileActivateListener {
        fun onActivateSuccess(user: ContactItem)
    }
}
