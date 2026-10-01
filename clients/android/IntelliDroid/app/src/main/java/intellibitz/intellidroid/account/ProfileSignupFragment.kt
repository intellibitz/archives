package intellibitz.intellidroid.account

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
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
import intellibitz.intellidroid.IntellibitzPermissionFragment
import intellibitz.intellidroid.IntellibitzUserFragment
import intellibitz.intellidroid.R
import intellibitz.intellidroid.content.UserContentProvider
import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.listener.ProfileTopicListener
import intellibitz.intellidroid.util.MainApplicationSingleton
import org.json.JSONException
import org.json.JSONObject

class ProfileSignupFragment :
    IntellibitzUserFragment(),
    SignupTask.SignupTaskListener {

    private var btnContinue: Button? = null
    private var profileTopicListener: ProfileTopicListener? = null
    private var etFirstname: EditText? = null
    private var etLastname: EditText? = null
    private var viewLayout: View? = null
    private var snackView: View? = null
    private var snackbar: Snackbar? = null
    private var btnPermission: Button? = null
    private var viewPermission: View? = null
    private var progressDialog: ProgressDialog? = null

    fun setProfileTopicListener(profileTopicListener: ProfileTopicListener?) {
        this.profileTopicListener = profileTopicListener
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ProfileTopicListener) {
            profileTopicListener = context
        }
    }

    override fun onContactsPermissionsGranted() {
        super.onContactsPermissionsGranted()
    }

    override fun onResume() {
        super.onResume()
        alertReadPhoneState()
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

    fun alertReadContacts(): Boolean {
        if (null == snackbar) {
            val act = activity ?: return false
            snackbar = makeReadContactsSnack(getSnackView(), act)
        }
        val act = activity
        return if (act != null && isReadContactsPermissionGranted(act)) {
            snackbar?.dismiss()
            viewPermission?.visibility = View.GONE
            true
        } else {
            viewPermission?.visibility = View.VISIBLE
            mayRequestReadContacts(snackbar, getSnackView())
        }
    }

    fun alertReadStorage(): Boolean {
        if (null == snackbar) {
            val act = activity ?: return false
            snackbar = makeReadExternalStorageSnack(getSnackView(), act)
        }
        val act = activity
        return if (act != null && isReadExternalStoragePermissionGranted(act)) {
            snackbar?.dismiss()
            viewPermission?.visibility = View.GONE
            true
        } else {
            viewPermission?.visibility = View.VISIBLE
            mayRequestReadExternalStorage(snackbar, getSnackView())
        }
    }

    fun alertWriteStorage(): Boolean {
        if (null == snackbar) {
            val act = activity ?: return false
            snackbar = makeWriteExternalStorageSnack(getSnackView(), act)
        }
        val act = activity
        return if (act != null && isWriteExternalStoragePermissionGranted(act)) {
            snackbar?.dismiss()
            viewPermission?.visibility = View.GONE
            true
        } else {
            viewPermission?.visibility = View.VISIBLE
            mayRequestWriteExternalStorage(snackbar, getSnackView())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewLayout = inflater.inflate(R.layout.fragment_profilesignup, container, false)
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

        etFirstname = view.findViewById(R.id.et_firstname)
        etLastname = view.findViewById(R.id.et_lastname)

        etLastname?.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                performSignup()
                true
            } else {
                false
            }
        }

        val loginListener = View.OnClickListener {
            performSignup()
        }
        val rlLogin = view.findViewById<View>(R.id.rl_login)
        val ibtnLogin = view.findViewById<View>(R.id.ibtn_login)

        rlLogin?.setOnClickListener(loginListener)
        ibtnLogin?.setOnClickListener(loginListener)
        btnContinue = view.findViewById(R.id.btn_login)
        btnContinue?.setOnClickListener(loginListener)

        etFirstname?.requestFocus()
        unPackUser()
        if (!IntellibitzPermissionFragment.isReadPhoneStatePermissionGranted(context)) {
            mayRequestReadPhoneState(snackView)
        }
    }

    private fun performSignup() {
        val firstname = etFirstname?.text.toString()
        if (TextUtils.isEmpty(firstname)) {
            etFirstname?.error = "First Name is required for Intellibitz"
        }
        val lastname = etLastname?.text.toString()
        if (TextUtils.isEmpty(lastname)) {
            etLastname?.error = "Last Name is required for Intellibitz"
        }
        etFirstname?.error = null
        etLastname?.error = null
        user?.name = firstname
        user?.firstName = firstname
        user?.lastName = firstname
        user?.displayName = "$firstname $lastname"
        showProgress()
        UserContentProvider.setsDeviceIdNameInfo(user, context)
        execSignUpTask()
    }

    private fun execSignUpTask() {
        val u = user ?: return
        val signupTask = SignupTask(
            u.email, u.otp, u.name, u.pwd,
            u.device, u.deviceId, u.deviceName, u.status,
            MainApplicationSingleton.AUTH_ACCOUNT_SIGNUP, u, context
        )
        signupTask.requestTimeoutMillis = 30000
        signupTask.setSignupTaskListener(this)
        signupTask.execute()
    }

    override fun onPostSignupResponse(response: JSONObject?, email: String?, user: ContactItem?) {
        try {
            val status = response?.optInt(MainApplicationSingleton.STATUS_PARAM, -1) ?: -1
            if (99 == status || -1 == status) {
                onPostSignupErrorResponse(response)
            } else if (1 == status) {
                UserContentProvider.activateUserSignupInDB(response, user, context)
                hideProgress()
                okActivity()
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            Log.e(TAG, "Exception: " + e.message)
            try {
                onPostSignupErrorResponse(
                    JSONObject("{\"err\":\"onPostSignupResponse - failed \"$e}")
                )
            } catch (e1: JSONException) {
                onPostSignupErrorResponse(null)
            }
        }
        hideProgress()
    }

    override fun onPostSignupErrorResponse(response: JSONObject?) {
        hideProgress()
    }

    protected fun okActivity() {
        val act = activity ?: return
        val intent = act.intent
        intent.putExtra(ContactItem.USER_CONTACT, user as? Parcelable)
        act.setResult(Activity.RESULT_OK, intent)
        act.finish()
    }

    fun getSnackView(): View? {
        if (null == snackView) {
            snackView = viewLayout?.findViewById(R.id.username)
        }
        return snackView
    }

    private fun unPackUser() {
        val firstName = user?.firstName
        if (!TextUtils.isEmpty(firstName)) {
            etFirstname?.setText(firstName)
        }
        val lastName = user?.lastName
        if (!TextUtils.isEmpty(lastName)) {
            etLastname?.setText(lastName)
        }
    }

    private fun packUser(u: ContactItem) {
        user?.name = u.name
        user?.device = u.device
        user?.mobile = u.mobile
        user?.countryName = u.countryName
        user?.countryCode = u.countryCode
        user?.profilePic = u.profilePic
    }

    fun packUser(): ContactItem? {
        val firstname = etFirstname?.text.toString()
        val lastname = etLastname?.text.toString()
        if (!TextUtils.isEmpty(firstname)) {
            user?.firstName = firstname
        }
        if (!TextUtils.isEmpty(lastname)) {
            user?.lastName = lastname
        }
        return user
    }

    fun setError(text: String?) {
        if (etFirstname != null) {
            etFirstname?.error = text
        }
    }

    fun showProgress() {
        progressDialog = ProgressDialog.show(activity, "Profile", "Saving Profile info", true)
    }

    fun hideProgress() {
        if (progressDialog != null) {
            val act = activity
            if (act != null && !act.isFinishing) {
                progressDialog?.dismiss()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        @NonNull permissions: Array<out String>,
        @NonNull grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_AND_STORAGE_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            }
        }
    }

    override fun onReadExternalStoragePermissionsGranted() {
        super.onReadExternalStoragePermissionsGranted()
    }

    override fun onWriteExternalStoragePermissionsGranted() {
        super.onWriteExternalStoragePermissionsGranted()
    }

    override fun onCameraPermissionsGranted() {
        super.onCameraPermissionsGranted()
    }

    override fun onReadExternalStoragePermissionsDenied() {
        super.onReadExternalStoragePermissionsDenied()
    }

    override fun onWriteExternalStoragePermissionsDenied() {
        super.onWriteExternalStoragePermissionsDenied()
    }

    override fun onContactsPermissionsDenied() {
        super.onContactsPermissionsDenied()
    }

    override fun onCameraPermissionsDenied() {
        super.onCameraPermissionsDenied()
    }

    companion object {
        private const val TAG = "ProfileInfoFragment"
        private const val REQUEST_CAMERA_AND_STORAGE_PERMISSION = 1

        @JvmStatic
        fun newInstance(user: ContactItem?): ProfileSignupFragment {
            val fragment = ProfileSignupFragment()
            fragment.user = user
            val args = Bundle()
            args.putParcelable(ContactItem.USER_CONTACT, user)
            fragment.arguments = args
            return fragment
        }
    }
}
