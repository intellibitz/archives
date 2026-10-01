

package intellibitz.intellidroid

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.task.UpdateProfileTask
import intellibitz.intellidroid.task.UploadProfilePicTask
import intellibitz.intellidroid.util.MainApplicationSingleton
import intellibitz.intellidroid.util.MediaPickerUri
import intellibitz.intellidroid.account.AccountLandingActivity
import intellibitz.intellidroid.account.CheckEmailAvailableActivity
import intellibitz.intellidroid.account.GetEmailVerificationTask
import intellibitz.intellidroid.account.NewPasswordFragment
import intellibitz.intellidroid.account.ProfileSignupActivity
import intellibitz.intellidroid.account.SetPwdTask
import intellibitz.intellidroid.account.VerifyCodeActivity
import intellibitz.intellidroid.activity.AddEmailActivity
import intellibitz.intellidroid.activity.LoginActivity
import intellibitz.intellidroid.activity.OTPActivity
import intellibitz.intellidroid.activity.ProfileInfoActivity
import intellibitz.intellidroid.company.CompanyCreateActivity
import intellibitz.intellidroid.company.GetInvitesActivity
import intellibitz.intellidroid.company.GetInvitesTask
import intellibitz.intellidroid.content.UserContentProvider
import intellibitz.intellidroid.fragment.ProfileItemFragment
import intellibitz.intellidroid.service.UserEmailIntentService
import intellibitz.intellidroid.widget.UnLockPasswordFragment
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.HashSet
import androidx.fragment.app.DialogFragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager

open class MainActivity : IntellibitzUserActivity(), UpdateProfileTask.UpdateProfileTaskListener, UploadProfilePicTask.UploadProfilePicTaskListener, UnLockPasswordFragment.OnUnLockPasswordFragmentListener, NewPasswordFragment.OnNewPasswordFragmentListener, SetPwdTask.SetPwdTaskListener, GetInvitesTask.GetInvitesTaskListener, GetEmailVerificationTask.GetEmailVerificationTaskListener {
    companion object {
        private const val TAG = "MainActivity"
        fun broadcastForceLogout(context: Context) {
            var intent: Intent = Intent(MainApplicationSingleton.BROADCAST_FORCE_LOGOUT)
            LocalBroadcastManager.getInstance(context)
        }
        fun startMainActivityForceLogout(context: Context) {
            var intent: Intent = Intent(context, MainActivity::class.java)
            intent.action = MainApplicationSingleton.BROADCAST_FORCE_LOGOUT
            context.startActivity(intent)
        }
        fun broadcastForceLogoutIfNeg1(response: JSONObject, context: Context) {
            var status: Int = 0
            if ((response != null)) {
                status = response.optInt("status")
            }
            if ((1 == status)) {
                broadcastForceLogout(context)
            }
        }
    }
    private var updateProfileTask: UpdateProfileTask? = null
    private var uploadProfilePicTask: UploadProfilePicTask? = null
    override protected fun onResume() {
        super.onResume()
    }
    override protected fun onCreate(savedInstanceState: Bundle) {
        super.onCreate(savedInstanceState)
        if ((null == savedInstanceState)) {
            user = getIntent()
        }
        else {
            user = savedInstanceState.getParcelable(ContactItem.USER_CONTACT)
        }
        if ((null == user)) {
            user = UserContentProvider.newActiveUserFromDB(this)
        }
        if ((MainApplicationSingleton.BROADCAST_FORCE_LOGOUT == getIntent())) {
            startLoginActivity(user, false)
        }
        else {
            startActivationIfUIDOrTokenNull(user)
        }
    }
    private fun startActivationIfUIDOrTokenNull(user: ContactItem): Boolean {
        var result: Boolean = true
        if ((isUIDNull() || isTokenNull())) {
            startAccountLandingActivity(user)
            result = false
        }
        else {
            var emails: HashSet<ContactItem> = user.contactItems
            if (((null == emails) || emails.empty)) {
                UserEmailIntentService.asyncEmailsFromCloudAndSavesInDb(user, this)
            }
            val flag1: Boolean = MainApplicationSingleton.getInstance(getApplicationContext())
            if (flag1) {
                showUnLockPwdDialog()
            }
            else {
                startCluttoActivity(user)
            }
        }
        return result
    }
    fun showUnLockPwdDialog() {
        UnLockPasswordFragment.newInstance(user, this)
    }
    private fun isUIDNull(): Boolean {
        return (((user == null) || (user.dataId == null)) || user.dataId)
    }
    private fun isTokenNull(): Boolean {
        return (((user == null) || (user.token == null)) || user.token)
    }
    private fun startCluttoActivityForProfile(user: ContactItem) {
        if (((user.name != null) && !user.name)) {
            updateProfileTask = UpdateProfileTask(user.dataId, user.token, user.device, user.deviceRef, user.name, user.status, MainApplicationSingleton.AUTH_UPDATE_PROFILE)
            updateProfileTask.updateProfileTaskListener = this
            updateProfileTask.execute()
        }
    }
    private fun broadcastUserUpdate() {
        var intent: Intent = Intent(MainApplicationSingleton.BROADCAST_NEW_USER_COMPLETE)
        intent.putExtra(ContactItem.USER_CONTACT, (user as Parcelable))
        LocalBroadcastManager.getInstance(this)
    }
    override fun onPostUpdateProfileExecute(response: JSONObject) {
        var intent: Intent = Intent(this, IntellibitzActivity::class.java)
        intent.action = ProfileItemFragment.TAG
        intent.putExtra(ContactItem.USER_CONTACT, (user as Parcelable))
        startActivity(intent)
        try {
            var status: Int = response.getInt(MainApplicationSingleton.STATUS_PARAM)
            if (((99 == status) || (1 == status))) {
                onPostUpdateProfileExecuteFail(response)
            }
            else {
                if ((1 == status)) {
                    var pic: String = user.profilePic
                    if (((pic != null) && !pic.empty)) {
                        uploadProfilePicTask = UploadProfilePicTask(user.dataId, user.token, user.device, user.deviceRef, MainApplicationSingleton.AUTH_UPDATE_PROFILE)
                        uploadProfilePicTask.uploadProfilePicTaskListener = this
                        var f: File = MediaPickerUri.resolveToFile(this, Uri.parse(pic))
                        uploadProfilePicTask.execute(f)
                    }
                    Log.e(TAG, ("onPostUpdateProfileExecute: SUCCESS - " + response))
                }
            }
        }
        catch (e: JSONException | IOException) {
            e.printStackTrace()
            Log.e(TAG, ("Exception: " + e.getMessage()))
        }
        finally {
            finish()
        }
    }
    override fun onPostUpdateProfileExecuteFail(response: JSONObject) {
        Log.e(TAG, ("onPostUpdateProfileExecuteFail: ERROR - " + response))
        MainActivity.broadcastForceLogoutIfNeg1(response, this)
    }
    override fun setUpdateProfileTaskToNull() {
        updateProfileTask = null
    }
    override fun onPostUploadProfilePicExecute(response: JSONObject, filename: String) {
        try {
            var status: Int = response.getInt(MainApplicationSingleton.STATUS_PARAM)
            if (((99 == status) || (1 == status))) {
                onPostUpdateProfileExecuteFail(response)
            }
            else {
                if ((1 == status)) {
                    Log.e(TAG, ((("onPostUploadProfilePicExecute: SUCCESS - " + response) + " :file: ") + filename))
                }
            }
        }
        catch (e: JSONException) {
            e.printStackTrace()
            Log.e(TAG, ("Exception: " + e.getMessage()))
        }
        finally {
            finish()
        }
    }
    override fun onPostUploadProfilePicExecuteFail(response: JSONObject, filename: String) {
        Log.e(TAG, ((("onPostUploadProfilePicExecuteFail: ERROR - " + response) + " :file: ") + filename))
        MainActivity.broadcastForceLogoutIfNeg1(response, this)
    }
    override fun setUploadProfilePicTaskToNull() {
        uploadProfilePicTask = null
    }
    private fun startLoginActivity(user: ContactItem, isRegisterAgain: Boolean) {
        var intent: Intent = Intent(this, LoginActivity::class.java)
        intent.putExtra(ContactItem.USER_CONTACT, (user as Parcelable))
        intent.putExtra("isRegisterAgain", isRegisterAgain)
        startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_LOGIN_RQ_CODE)
    }
    private fun startCheckEmailAvailableActivity(user: ContactItem) {
        var intent: Intent = Intent(this, CheckEmailAvailableActivity::class.java)
        intent.putExtra(ContactItem.USER_CONTACT, (user as Parcelable))
        startActivityForResult(intent, MainApplicationSingleton.ACT_CHK_EMAIL_AVBL_RQ_CODE)
    }
    private fun startAccountLandingActivity(user: ContactItem) {
        var intent: Intent = Intent(this, AccountLandingActivity::class.java)
        intent.putExtra(ContactItem.USER_CONTACT, (user as Parcelable))
        startActivityForResult(intent, MainApplicationSingleton.ACT_ACCT_LANDING_RQ_CODE)
    }
    private fun startVerifyCodeActivity(user: ContactItem) {
        startVerifyCodeActivity(user, MainApplicationSingleton.ACT_VERIFY_CODE_RQ_CODE)
    }
    private fun startVerifyCodeActivity(user: ContactItem, code: Int) {
        var intent: Intent = Intent(this, VerifyCodeActivity::class.java)
        intent.putExtra(ContactItem.USER_CONTACT, (user as Parcelable))
        startActivityForResult(intent, code)
    }
    private fun startProfileSignupActivity(user: ContactItem) {
        var intent: Intent = Intent(this, ProfileSignupActivity::class.java)
        intent.putExtra(ContactItem.USER_CONTACT, (user as Parcelable))
        startActivityForResult(intent, MainApplicationSingleton.ACT_PROFILE_SIGNUP_RQ_CODE)
    }
    private fun startCompanyCreateActivity(user: ContactItem) {
        var intent: Intent = Intent(this, CompanyCreateActivity::class.java)
        intent.putExtra(ContactItem.USER_CONTACT, (user as Parcelable))
        startActivityForResult(intent, MainApplicationSingleton.ACT_COMPANY_CREATE_RQ_CODE)
    }
    private fun startGetInvitesActivity(user: ContactItem) {
        var intent: Intent = Intent(this, GetInvitesActivity::class.java)
        intent.putExtra(ContactItem.USER_CONTACT, (user as Parcelable))
        startActivityForResult(intent, MainApplicationSingleton.ACT_COMPANY_GETINVITES_RQ_CODE)
    }
    private fun startLoginActivity(user: ContactItem) {
        var intent: Intent = Intent(this, CheckEmailAvailableActivity::class.java)
        intent.putExtra(ContactItem.USER_CONTACT, (user as Parcelable))
        startActivityForResult(intent, MainApplicationSingleton.ACT_CHK_EMAIL_AVBL_RQ_CODE)
    }
    private fun startCluttoActivity(user: ContactItem) {
        var intent: Intent = Intent(this, IntellibitzActivity::class.java)
        intent.putExtra(ContactItem.USER_CONTACT, (user as Parcelable))
        startActivity(intent)
        finish()
    }
    private fun startProfileInfoActivity(user: ContactItem) {
        var intent: Intent = Intent(this, ProfileInfoActivity::class.java)
        intent.putExtra(ContactItem.USER_CONTACT, (user as Parcelable))
        startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_PROFILEINFO_RQ_CODE)
    }
    private fun startAddEmailActivity(user: ContactItem) {
        var intent: Intent = Intent(this, AddEmailActivity::class.java)
        intent.putExtra(ContactItem.USER_CONTACT, (user as Parcelable))
        startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_ADDEMAIL_RQ_CODE)
    }
    private fun startOTPActivity(user: ContactItem) {
        var intent: Intent = Intent(this, OTPActivity::class.java)
        intent.putExtra(ContactItem.USER_CONTACT, (user as Parcelable))
        startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_OTP_RQ_CODE)
    }
    override protected fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent) {
        super.onActivityResult(requestCode, resultCode, intent)
        var respCode: Int = intent.getIntExtra(MainApplicationSingleton.RESP_CODE, 1)
        if ((MainApplicationSingleton.ACT_CHK_EMAIL_AVBL_RQ_CODE == requestCode)) {
            user = intent.getParcelableExtra(ContactItem.USER_CONTACT)
            if ((Activity.RESULT_OK == resultCode)) {
                execGetEmailVerification()
                startVerifyCodeActivity(user)
            }
            else {
                if ((Activity.RESULT_CANCELED == resultCode)) {
                    if ((1 == respCode)) {
                        finish()
                    }
                    else {
                        if ((111 == respCode)) {
                            execGetEmailVerification()
                            startVerifyCodeActivity(user, MainApplicationSingleton.ACT_VERIFY_CODE_FORGOT_PWD_RQ_CODE)
                        }
                        else {
                            if ((222 == respCode)) {
                                startCluttoActivity(user)
                            }
                        }
                    }
                }
            }
        }
        else {
            if ((MainApplicationSingleton.ACT_VERIFY_CODE_FORGOT_PWD_RQ_CODE == requestCode)) {
                if ((Activity.RESULT_OK == resultCode)) {
                    user = intent.getParcelableExtra(ContactItem.USER_CONTACT)
                    showForgotPwdDialog(user)
                }
                else {
                    if ((Activity.RESULT_CANCELED == resultCode)) {
                        finish()
                    }
                }
            }
            else {
                if ((MainApplicationSingleton.ACT_VERIFY_CODE_RQ_CODE == requestCode)) {
                    if ((Activity.RESULT_OK == resultCode)) {
                        user = intent.getParcelableExtra(ContactItem.USER_CONTACT)
                        NewPasswordFragment.newInstance(user)
                    }
                    else {
                        if ((Activity.RESULT_CANCELED == resultCode)) {
                            finish()
                        }
                    }
                }
                else {
                    if ((MainApplicationSingleton.ACT_PROFILE_SIGNUP_RQ_CODE == requestCode)) {
                        if ((Activity.RESULT_OK == resultCode)) {
                            user = intent.getParcelableExtra(ContactItem.USER_CONTACT)
                            execGetInvitesTask(user)
                        }
                        else {
                            if ((Activity.RESULT_CANCELED == resultCode)) {
                                finish()
                            }
                        }
                    }
                    else {
                        if ((MainApplicationSingleton.ACT_COMPANY_GETINVITES_RQ_CODE == requestCode)) {
                            if ((Activity.RESULT_OK == resultCode)) {
                                user = intent.getParcelableExtra(ContactItem.USER_CONTACT)
                                startCompanyCreateActivity(user)
                            }
                            else {
                                if ((Activity.RESULT_CANCELED == resultCode)) {
                                    startCompanyCreateActivity(user)
                                }
                            }
                        }
                        else {
                            if ((MainApplicationSingleton.ACT_COMPANY_CREATE_RQ_CODE == requestCode)) {
                                if ((Activity.RESULT_OK == resultCode)) {
                                    user = intent.getParcelableExtra(ContactItem.USER_CONTACT)
                                    startAddEmailActivity(user)
                                }
                                else {
                                    if ((Activity.RESULT_CANCELED == resultCode)) {
                                        user = intent.getParcelableExtra(ContactItem.USER_CONTACT)
                                        startAddEmailActivity(user)
                                    }
                                }
                            }
                            else {
                                if ((MainApplicationSingleton.ACTIVITY_LOGIN_RQ_CODE == requestCode)) {
                                    if ((Activity.RESULT_OK == resultCode)) {
                                        user = intent.getParcelableExtra(ContactItem.USER_CONTACT)
                                        startOTPActivity(user)
                                    }
                                    else {
                                        if ((Activity.RESULT_CANCELED == resultCode)) {
                                            startLoginActivity(user, false)
                                        }
                                    }
                                }
                                else {
                                    if ((MainApplicationSingleton.ACTIVITY_OTP_RQ_CODE == requestCode)) {
                                        if ((Activity.RESULT_OK == resultCode)) {
                                            user = intent.getParcelableExtra(ContactItem.USER_CONTACT)
                                            startProfileInfoActivity(user)
                                        }
                                        else {
                                            if ((Activity.RESULT_CANCELED == resultCode)) {
                                                startLoginActivity(user, false)
                                            }
                                        }
                                    }
                                    else {
                                        if ((MainApplicationSingleton.ACTIVITY_PROFILEINFO_RQ_CODE == requestCode)) {
                                            if ((Activity.RESULT_OK == resultCode)) {
                                                user = intent.getParcelableExtra(ContactItem.USER_CONTACT)
                                                startAddEmailActivity(user)
                                            }
                                            else {
                                                if ((Activity.RESULT_CANCELED == resultCode)) {
                                                    user = intent.getParcelableExtra(ContactItem.USER_CONTACT)
                                                    startCluttoActivityForProfile(user)
                                                    notifyUserBaseItemListeners()
                                                    broadcastUserUpdate()
                                                }
                                            }
                                        }
                                        else {
                                            if ((MainApplicationSingleton.ACTIVITY_ADDEMAIL_RQ_CODE == requestCode)) {
                                                if ((Activity.RESULT_OK == resultCode)) {
                                                    user = intent.getParcelableExtra(ContactItem.USER_CONTACT)
                                                    startCluttoActivity(user)
                                                    notifyUserBaseItemListeners()
                                                    broadcastUserUpdate()
                                                }
                                                else {
                                                    if ((Activity.RESULT_CANCELED == resultCode)) {
                                                        user = intent.getParcelableExtra(ContactItem.USER_CONTACT)
                                                        startCluttoActivity(user)
                                                        notifyUserBaseItemListeners()
                                                        broadcastUserUpdate()
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    private fun showForgotPwdDialog(user: ContactItem) {
        var newPasswordFragment: NewPasswordFragment = NewPasswordFragment.newInstance(user)
        newPasswordFragment.mode = MainApplicationSingleton.ACT_VERIFY_CODE_FORGOT_PWD_RQ_CODE
        newPasswordFragment.show(getSupportFragmentManager(), "NewPasswordDialog")
    }
    private fun retryForgotPwdDialog(user: ContactItem) {
        var newPasswordFragment: NewPasswordFragment = NewPasswordFragment.newInstance(user)
        newPasswordFragment.mode = MainApplicationSingleton.ACT_VERIFY_CODE_FORGOT_PWD_RQ_CODE
        newPasswordFragment.error = "Please try again"
        newPasswordFragment.show(getSupportFragmentManager(), "NewPasswordDialog")
    }
    override fun onDialogPositiveClick(dialog: DialogFragment) {
        startCluttoActivity(user)
    }
    override fun onDialogNegativeClick(dialog: DialogFragment) {
        finish()
    }
    override fun onNewPasswordDialogPositiveClick(dialog: DialogFragment) {
        var newPasswordFragment: NewPasswordFragment = (dialog as NewPasswordFragment)
        if ((newPasswordFragment != null)) {
            var user: ContactItem = newPasswordFragment.user
            if ((MainApplicationSingleton.ACT_VERIFY_CODE_FORGOT_PWD_RQ_CODE == newPasswordFragment.mode)) {
                execSetPwdTask(user, (dialog as NewPasswordFragment))
            }
            else {
                startProfileSignupActivity(user)
                dialog.dismiss()
            }
        }
    }
    override fun onNewPasswordDialogNegativeClick(dialog: DialogFragment) {

    }
    private fun execGetInvitesTask(user: ContactItem) {
        var getInvitesTask: GetInvitesTask = GetInvitesTask(user.dataId, user.token, user.device, user.deviceRef, user, MainApplicationSingleton.AUTH_COMPANY_GET_INVITES, this)
        getInvitesTask.requestTimeoutMillis = 30000
        getInvitesTask.getInvitesTaskListener = this
        getInvitesTask.execute()
    }
    override fun onPostGetInvitesResponse(response: JSONObject, user: ContactItem) {
        try {
            var status: Int = response.getInt("status")
            if ((1 == status)) {
                startGetInvitesActivity(user)
            }
            else {
                if ((99 == status)) {
                    startCompanyCreateActivity(user)
                }
            }
        }
        catch (e: JSONException) {
            e.printStackTrace()
            startCompanyCreateActivity(user)
        }
    }
    override fun onPostGetInvitesErrorResponse(response: JSONObject) {
        Log.e(TAG, ("onPostGetInvitesErrorResponse: " + response))
    }
    private fun execSetPwdTask(user: ContactItem, mode: Int) {
        var setPwdTask: SetPwdTask = SetPwdTask(user.email, mode, user.otp, user.pwd, user.device, user.deviceId, user.deviceName, MainApplicationSingleton.AUTH_ACCOUNT_SET_PWD, user, this)
        setPwdTask.requestTimeoutMillis = 30000
        setPwdTask.setPwdTaskListener = this
        setPwdTask.execute()
    }
    override fun onPostSetPwdResponse(response: JSONObject, email: String, user: ContactItem, mode: Int) {
        try {
            var status: Int = response.optInt(MainApplicationSingleton.STATUS_PARAM, 1)
            if (((99 == status) || (1 == status))) {
                if ((MainApplicationSingleton.ACT_VERIFY_CODE_FORGOT_PWD_RQ_CODE == mode)) {
                    retryForgotPwdDialog(user)
                    return
                }
                onPostSetPwdErrorResponse(response)
            }
            else {
                if ((1 == status)) {
                    UserContentProvider.activateUserInDB(response, user, this)
                    startCluttoActivity(user)
                }
            }
        }
        catch (e: Throwable) {
            e.printStackTrace()
            Log.e(TAG, ("Exception: " + e.getMessage()))
            try {
                onPostSetPwdErrorResponse(JSONObject((("{\"err\":\"onPostSignupResponse - failed \"" + e.toString()) + "}")))
            }
            catch (e1: JSONException) {
                onPostSetPwdErrorResponse(null)
            }
        }
    }
    override fun onPostSetPwdErrorResponse(response: JSONObject) {
        Log.e(TAG, ("onPostSignupErrorResponse: " + response))
    }
    private fun execGetEmailVerification() {
        var getEmailVerificationTask: GetEmailVerificationTask = GetEmailVerificationTask(user.email, MainApplicationSingleton.AUTH_ACCOUNT_GET_EMAIL_VERIFICATION, this)
        getEmailVerificationTask.requestTimeoutMillis = 30000
        getEmailVerificationTask.getEmailVerificationTaskListener = this
        getEmailVerificationTask.execute()
    }
    override fun onPostGetEmailVerificationResponse(response: JSONObject, email: String, user: ContactItem) {

    }
    override fun onPostGetEmailVerificationErrorResponse(response: JSONObject) {

    }
}
