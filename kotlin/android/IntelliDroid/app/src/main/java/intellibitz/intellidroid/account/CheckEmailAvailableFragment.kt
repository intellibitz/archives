package intellibitz.intellidroid.account

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Parcelable
import android.provider.ContactsContract
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.fragment.app.DialogFragment
import com.google.android.material.snackbar.Snackbar
import intellibitz.intellidroid.IntellibitzPermissionFragment
import intellibitz.intellidroid.IntellibitzUserFragment
import intellibitz.intellidroid.R
import intellibitz.intellidroid.company.GetInvitesTask
import intellibitz.intellidroid.content.UserContentProvider
import intellibitz.intellidroid.content.UserEmailContentProvider
import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.service.ContactService
import intellibitz.intellidroid.task.GetEmailsTask
import intellibitz.intellidroid.util.MainApplicationSingleton
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.ArrayList
import java.util.HashSet

class CheckEmailAvailableFragment :
    IntellibitzUserFragment(),
    CheckEmailAvailableTask.CheckEmailAvailableTaskListener,
    UserSignupFragment.OnUserSignupFragmentListener,
    LoginTask.LoginTaskListener,
    GetProfileTask.GetProfileTaskListener,
    GetEmailsTask.GetEmailsTaskListener,
    GetInvitesTask.GetInvitesTaskListener {

    private var tvEmail: AutoCompleteTextView? = null
    private var tvPassword: TextView? = null
    private var viewLayout: View? = null
    private var snackView: View? = null
    private var snackbar: Snackbar? = null
    private var progressDialog: ProgressDialog? = null
    private var btnPermission: Button? = null
    private var viewPermission: View? = null

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewLayout = inflater.inflate(R.layout.fragment_checkemailavailable, container, false)
        return viewLayout
    }

    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (null == savedInstanceState) {
            user = arguments?.getParcelable(ContactItem.USER_CONTACT)
        } else {
            user = savedInstanceState.getParcelable(ContactItem.USER_CONTACT)
        }

        tvEmail = view.findViewById(R.id.tv_email)
        populateAutoComplete()

        tvPassword = view.findViewById(R.id.tv_password)
        tvEmail?.dropDownWidth = resources.displayMetrics.widthPixels
        val onEditorActionListener = TextView.OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                performLoginOrSignup()
                true
            } else {
                false
            }
        }
        tvEmail?.setOnEditorActionListener(onEditorActionListener)
        tvEmail?.setOnKeyListener { _, _, event ->
            if (KeyEvent.ACTION_UP == event.action) {
                val text = tvEmail?.text.toString()
                if (text.endsWith(".com") && text.contains("@")) {
                    performLoginOrSignup()
                    return@setOnKeyListener true
                }
            }
            false
        }

        val rlLogin = view.findViewById<View>(R.id.rl_login)
        rlLogin?.setOnClickListener {
            performLoginOrSignup()
        }
        val btnForgotPwd = view.findViewById<View>(R.id.btn_forgotpwd)
        btnForgotPwd?.setOnClickListener {
            performForgotPwd()
        }
        val btnLogin = view.findViewById<ImageButton>(R.id.btn_login)
        btnLogin?.setOnClickListener {
            performLoginOrSignup()
        }

        tvEmail?.requestFocus()
        unPackUser()

        btnPermission = view.findViewById(R.id.btn_perm)
        btnPermission?.setOnClickListener {
            val act = activity ?: return@setOnClickListener
            requestReadPhoneStatePermissions(act)
        }
        viewPermission = view.findViewById(R.id.ll_perm)
        getSnackView()

        if (!IntellibitzPermissionFragment.isReadPhoneStatePermissionGranted(context)) {
            mayRequestReadPhoneState(snackView)
        }
    }

    private fun unPackUser() {
        val email = user?.email
        if (!TextUtils.isEmpty(email)) {
            tvEmail?.setText(email)
        }
    }

    fun getSnackView(): View? {
        if (null == snackView) {
            snackView = viewLayout?.findViewById(R.id.username)
        }
        return snackView
    }

    fun packUser(): ContactItem? {
        val email = tvEmail?.text.toString()
        if (!TextUtils.isEmpty(email)) {
            user?.email = email
        }
        val pwd = tvPassword?.text.toString()
        if (!TextUtils.isEmpty(pwd)) {
            user?.pwd = pwd
        }
        return user
    }

    fun setError(text: String?) {
        if (tvEmail != null) {
            tvEmail?.error = text
        }
    }

    fun showProgress() {
        progressDialog = ProgressDialog.show(activity, "Login", getString(R.string.verify_code), true)
    }

    fun hideProgress() {
        progressDialog?.dismiss()
    }

    fun performForgotPwd() {
        packUser()
        if (TextUtils.isEmpty(user?.email)) {
            setError("Email is required")
        } else {
            UserContentProvider.setsDeviceIdNameInfo(user, context)
            forgotPwdActivity()
        }
    }

    fun performLoginOrSignup() {
        packUser()
        if (TextUtils.isEmpty(user?.pwd)) {
            checkEmailAvailable()
        } else {
            performLogin()
        }
    }

    fun checkEmailAvailable() {
        val act = activity ?: return
        if (MainApplicationSingleton.CheckNetworkConnection.isNetworkConnectionAvailable(act)) {
            showProgress()
            checkEmailAvailableTask()
        }
    }

    fun performLogin() {
        val act = activity ?: return
        if (MainApplicationSingleton.CheckNetworkConnection.isNetworkConnectionAvailable(act)) {
            showProgress()
            setError(null)
            UserContentProvider.setsDeviceIdNameInfo(user, context)
            loginTask()
        }
    }

    protected fun okActivity(respCode: Int) {
        val act = activity ?: return
        val intent = act.intent
        intent.putExtra(ContactItem.USER_CONTACT, user as? Parcelable)
        intent.putExtra(MainApplicationSingleton.RESP_CODE, respCode)
        act.setResult(Activity.RESULT_OK, intent)
        act.finish()
    }

    protected fun forgotPwdActivity() {
        val act = activity ?: return
        val intent = act.intent
        intent.putExtra(ContactItem.USER_CONTACT, user as? Parcelable)
        intent.putExtra(MainApplicationSingleton.RESP_CODE, 111)
        act.setResult(Activity.RESULT_CANCELED, intent)
        act.finish()
    }

    protected fun loginActivity() {
        val act = activity ?: return
        val intent = act.intent
        intent.putExtra(ContactItem.USER_CONTACT, user as? Parcelable)
        intent.putExtra(MainApplicationSingleton.RESP_CODE, 222)
        act.setResult(Activity.RESULT_CANCELED, intent)
        act.finish()
    }

    override fun onPostCheckEmailAvailableResponse(response: JSONObject?, email: String?, user: ContactItem?) {
        try {
            Log.d(TAG, TAG + response)
            val status = response?.optInt("status", -1) ?: -1
            val respCode = response?.optInt("resp_code", -1) ?: -1
            if (1 == status) {
                if (101 == respCode) {
                    UserSignupFragment.newInstance(user, this).show(
                        childFragmentManager, "UserSignupDialog"
                    )
                } else if (102 == respCode) {
                    tvPassword?.isEnabled = true
                    tvPassword?.requestFocus()
                } else {
                    onPostCheckEmailAvailableErrorResponse(response)
                }
            } else if (-1 == status || 99 == status) {
                onPostCheckEmailAvailableErrorResponse(response)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            onPostCheckEmailAvailableErrorResponse(response)
        }
        hideProgress()
    }

    override fun onPostCheckEmailAvailableErrorResponse(response: JSONObject?) {
        Log.e(TAG, "onPostCheckEmailAvailableErrorResponse: $response")
        if (null == response) {
            setError("Network fail - Please try again")
        } else {
            setError("Please check email and try again")
        }
        hideProgress()
    }

    private fun checkEmailAvailableTask() {
        if (TextUtils.isEmpty(user?.email)) {
            try {
                onPostCheckEmailAvailableErrorResponse(
                    JSONObject("{\"err\":\"Email is EMPTY.. Please fill\"}")
                )
            } catch (e: JSONException) {
                e.printStackTrace()
                onPostCheckEmailAvailableErrorResponse(null)
            }
        } else {
            val task = CheckEmailAvailableTask(
                user?.email,
                MainApplicationSingleton.AUTH_ACCOUNT_CHECK_EMAIL_AVAILABLE, context
            )
            task.setCheckEmailAvailableTaskListener(this)
            task.requestTimeoutMillis = 30000
            task.execute()
        }
    }

    private fun loginTask() {
        if (TextUtils.isEmpty(user?.email) || TextUtils.isEmpty(user?.pwd)) {
            try {
                onPostCheckEmailAvailableErrorResponse(
                    JSONObject("{\"err\":\"Email/Pwd is EMPTY.. Please fill\"}")
                )
            } catch (e: JSONException) {
                e.printStackTrace()
                onPostCheckEmailAvailableErrorResponse(null)
            }
        } else {
            execLoginTask(user!!)
        }
    }

    private fun execLoginTask(user: ContactItem) {
        val task = LoginTask(
            user.email, user.pwd, user.device, user.deviceId, user.deviceName,
            MainApplicationSingleton.AUTH_ACCOUNT_LOGIN, user, context
        )
        task.setLoginTaskListener(this)
        task.requestTimeoutMillis = 30000
        task.execute()
    }

    private fun execGetProfileTask(user: ContactItem) {
        val task = GetProfileTask(
            user.dataId, user.token, user.device, user.deviceRef, user,
            MainApplicationSingleton.AUTH_GET_PROFILE, context
        )
        task.setGetProfileTaskListener(this)
        task.requestTimeoutMillis = 30000
        task.execute()
    }

    private fun execGetEmailsTask(user: ContactItem, mode: Int) {
        val task = GetEmailsTask(
            user.dataId, user.token, user.device, user.deviceRef,
            MainApplicationSingleton.AUTH_GET_EMAILS, user, context, mode
        )
        task.requestTimeoutMillis = 30000
        task.setGetEmailsTaskListener(this)
        task.execute()
    }

    override fun onUserSignupDialogPositiveClick(dialog: DialogFragment?) {
        okActivity(101)
    }

    override fun onUserSignupDialogNegativeClick(dialog: DialogFragment?) {
        dialog?.dismiss()
    }

    override fun onPostLoginResponse(response: JSONObject?, email: String?, user: ContactItem?) {
        try {
            val status = response?.optInt(MainApplicationSingleton.STATUS_PARAM, -1) ?: -1
            if (99 == status || -1 == status) {
                onPostLoginErrorResponse(response)
            } else if (1 == status) {
                val u = user ?: this.user
                if (u != null) {
                    UserContentProvider.parseLoginResponse(response, u.email, u)
                    ContactService.asyncUpdateContacts(u, context)
                    execGetProfileTask(u)
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            Log.e(TAG, "Exception: " + e.message)
            try {
                onPostLoginErrorResponse(
                    JSONObject("{\"err\":\"onPostSignupResponse - failed \"$e}")
                )
            } catch (e1: JSONException) {
                onPostLoginErrorResponse(null)
            }
        }
    }

    override fun onPostLoginErrorResponse(response: JSONObject?) {
        setError("Please enter correct password")
        tvPassword?.isEnabled = true
        tvPassword?.requestFocus()
        hideProgress()
    }

    override fun onPostGetProfileResponse(response: JSONObject?, user: ContactItem?) {
        try {
            val u = user ?: this.user
            if (u != null) {
                UserContentProvider.parseGetProfileResponse(response, u)
                UserContentProvider.savesUserInDBPlusSP(u, context)
                execGetEmailsTask(u, -1)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            Log.e(TAG, "Exception: " + e.message)
            try {
                onPostLoginErrorResponse(
                    JSONObject("{\"err\":\"onPostSignupResponse - failed \"$e}")
                )
            } catch (e1: JSONException) {
                onPostLoginErrorResponse(null)
            }
        }
    }

    override fun onPostGetProfileErrorResponse(response: JSONObject?) {
        try {
            UserContentProvider.savesUserInDBPlusSP(user, context)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        hideProgress()
        loginActivity()
    }

    override fun onPostGetEmailsResponse(response: JSONObject?, user: ContactItem?, mode: Int) {
        try {
            val status = response?.optInt("status") ?: 0
            val u = user ?: this.user
            if (1 == status && u != null) {
                val responseJSONArray = response?.getJSONArray("emails")
                if (responseJSONArray != null && responseJSONArray.length() > 0) {
                    u.contactItems.addAll(
                        UserContentProvider.setsEmailsFromJSONArray(responseJSONArray, context)
                    )
                    if (!u.contactItems.isEmpty()) {
                        val uri: Uri? = UserEmailContentProvider.savesUserEmailsInDB(u, context)
                    }
                }
            }
            if (u != null) {
                execGetInvitesTask(u)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            onPostGetEmailsErrorResponse(response)
            Log.e(TAG, TAG + e.toString())
        }
    }

    override fun onPostGetEmailsErrorResponse(response: JSONObject?) {
        Log.e(TAG, "onPostGetEmailsErrorResponse:$response")
        hideProgress()
        loginActivity()
    }

    private fun updatesUserCompanyInDB(invitedCompanyId: String?, contactItem: ContactItem?, user: ContactItem?) {
        user?.companyId = invitedCompanyId
        if (contactItem != null) {
            user?.companyName = contactItem.companyName
        }
        ContactService.asyncUpdateWorkContacts(user, context)
        UserContentProvider.updatesCompanyInDB(user, context)
    }

    private fun execGetInvitesTask(user: ContactItem) {
        val task = GetInvitesTask(
            user.dataId, user.token, user.device, user.deviceRef, user,
            MainApplicationSingleton.AUTH_COMPANY_GET_INVITES, context
        )
        task.requestTimeoutMillis = 30000
        task.setGetInvitesTaskListener(this)
        task.execute()
    }

    override fun onPostGetInvitesResponse(response: JSONObject?, user: ContactItem?) {
        val status = response?.optInt("status", -1) ?: -1
        if (1 == status) {
            val jsonArray = response?.optJSONArray("invites")
            if (jsonArray != null) {
                val length = jsonArray.length()
                if (length > 0) {
                    val contactItems = HashSet<ContactItem>(length)
                    for (i in 0 until length) {
                        val company = jsonArray.optJSONObject(i)
                        val cid = company?.optString("company_id")
                        val cname = company?.optString("company_name")
                        val timestamp = company?.optLong("timestamp") ?: 0L
                        val contactItem = ContactItem()
                        contactItem.dataId = cid
                        contactItem.companyId = cid
                        contactItem.companyName = cname
                        contactItem.timestamp = timestamp
                        contactItem.dateTime = MainApplicationSingleton.getDateTimeMillis(timestamp)
                        contactItems.add(contactItem)
                    }
                    val iterator = contactItems.iterator()
                    val contactItem = iterator.next()
                    updatesUserCompanyInDB(contactItem.companyId, contactItem, user)
                }
            }
        } else if (99 == status || -1 == status) {
            onPostGetInvitesErrorResponse(response)
        }
        hideProgress()
        loginActivity()
    }

    override fun onPostGetInvitesErrorResponse(response: JSONObject?) {
        Log.e(TAG, "onPostGetInvitesErrorResponse: $response")
    }

    private fun populateAutoComplete() {
        if (mayRequestReadContacts(getSnackView())) {
            SetupEmailAutoCompleteTask().execute()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete()
            }
        }
    }

    private fun addEmailsToAutoComplete(emailAddressCollection: List<String>) {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line, emailAddressCollection
        )
        tvEmail?.setAdapter(adapter)
    }

    inner class SetupEmailAutoCompleteTask : AsyncTask<Void?, Void?, List<String>>() {
        override fun doInBackground(vararg voids: Void?): List<String> {
            val emailAddressCollection = ArrayList<String>()
            val ctx = context ?: return emailAddressCollection
            val cr: ContentResolver = ctx.contentResolver
            val emailCur: Cursor? = cr.query(
                ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                null, null, null
            )
            emailCur?.use { cursor ->
                val dataIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA)
                while (cursor.moveToNext()) {
                    if (dataIndex != -1) {
                        val email = cursor.getString(dataIndex)
                        if (!email.isNullOrEmpty()) {
                            emailAddressCollection.add(email)
                        }
                    }
                }
            }
            return emailAddressCollection
        }

        override fun onPostExecute(emailAddressCollection: List<String>?) {
            if (emailAddressCollection != null) {
                addEmailsToAutoComplete(emailAddressCollection)
            }
        }
    }

    companion object {
        private const val TAG = "ChkEmailAvblFrag"
        private const val REQUEST_READ_CONTACTS = 21

        @JvmStatic
        fun newInstance(user: ContactItem?): CheckEmailAvailableFragment {
            val fragment = CheckEmailAvailableFragment()
            fragment.user = user
            val args = Bundle()
            args.putParcelable(ContactItem.USER_CONTACT, user)
            fragment.arguments = args
            return fragment
        }
    }
}
