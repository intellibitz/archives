package intellibitz.intellidroid;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;

import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.task.UpdateProfileTask;
import intellibitz.intellidroid.task.UploadProfilePicTask;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.util.MediaPickerUri;
import intellibitz.intellidroid.account.AccountLandingActivity;
import intellibitz.intellidroid.account.CheckEmailAvailableActivity;
import intellibitz.intellidroid.account.GetEmailVerificationTask;
import intellibitz.intellidroid.account.NewPasswordFragment;
import intellibitz.intellidroid.account.ProfileSignupActivity;
import intellibitz.intellidroid.account.SetPwdTask;
import intellibitz.intellidroid.account.VerifyCodeActivity;
import intellibitz.intellidroid.activity.AddEmailActivity;
import intellibitz.intellidroid.activity.LoginActivity;
import intellibitz.intellidroid.activity.OTPActivity;
import intellibitz.intellidroid.activity.ProfileInfoActivity;
import intellibitz.intellidroid.company.CompanyCreateActivity;
import intellibitz.intellidroid.company.GetInvitesActivity;
import intellibitz.intellidroid.company.GetInvitesTask;
import intellibitz.intellidroid.content.UserContentProvider;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.fragment.ProfileItemFragment;
import intellibitz.intellidroid.service.UserEmailIntentService;
import intellibitz.intellidroid.task.UpdateProfileTask;
import intellibitz.intellidroid.task.UploadProfilePicTask;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.util.MediaPickerUri;
import intellibitz.intellidroid.widget.UnLockPasswordFragment;

import intellibitz.intellidroid.activity.AddEmailActivity;
import intellibitz.intellidroid.activity.LoginActivity;
import intellibitz.intellidroid.activity.OTPActivity;
import intellibitz.intellidroid.activity.ProfileInfoActivity;
import intellibitz.intellidroid.company.GetInvitesActivity;
import intellibitz.intellidroid.company.GetInvitesTask;
import intellibitz.intellidroid.content.UserContentProvider;
import intellibitz.intellidroid.fragment.ProfileItemFragment;
import intellibitz.intellidroid.service.UserEmailIntentService;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

import androidx.fragment.app.DialogFragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class MainActivity extends
        IntellibitzUserActivity implements
        UpdateProfileTask.UpdateProfileTaskListener,
        UploadProfilePicTask.UploadProfilePicTaskListener,
        UnLockPasswordFragment.OnUnLockPasswordFragmentListener,
        NewPasswordFragment.OnNewPasswordFragmentListener,
        SetPwdTask.SetPwdTaskListener,
        GetInvitesTask.GetInvitesTaskListener,
        GetEmailVerificationTask.GetEmailVerificationTaskListener {

    private static final String TAG = "MainActivity";

    // Used to load the 'native-lib' library on application startup.
    static {
//        System.loadLibrary("native-lib");
    }

    private UpdateProfileTask updateProfileTask;
    private UploadProfilePicTask uploadProfilePicTask;

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
//    public native String stringFromJNI();
    public static void broadcastForceLogout(Context context) {
        Intent intent = new Intent(
                MainApplicationSingleton.BROADCAST_FORCE_LOGOUT);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public static void startMainActivityForceLogout(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setAction(MainApplicationSingleton.BROADCAST_FORCE_LOGOUT);
        context.startActivity(intent);
    }

    public static void broadcastForceLogoutIfNeg1(JSONObject response, Context context) {
        int status = 0;
        if (response != null)
            status = response.optInt("status");
        if (-1 == status) {
            broadcastForceLogout(context);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_splash);
        if (null == savedInstanceState) {
//            check if this activity is being invoked with intent extras
            user = getIntent().getParcelableExtra(ContactItem.USER_CONTACT);
        } else {
//            get user from the saved state
            user = savedInstanceState.getParcelable(ContactItem.USER_CONTACT);
        }
        if (null == user) {
            user = UserContentProvider.newActiveUserFromDB(this);
        }
        if (MainApplicationSingleton.BROADCAST_FORCE_LOGOUT.equals(getIntent().getAction())) {
            startLoginActivity(user, false);
        } else {
            startActivationIfUIDOrTokenNull(user);
        }
    }

    private boolean startActivationIfUIDOrTokenNull(ContactItem user) {
        //        the logged in user.. get it from the db. other activities free to modify user
//        finally.. if token is still is null.. always start activiation
        boolean result = true;
        if (isUIDNull() || isTokenNull()) {
//            startLoginActivity(user, false);
//            startCheckEmailAvailableActivity(user);
            startAccountLandingActivity(user);
            result = false;
        } else {
            HashSet<ContactItem> emails = user.getContactItems();
            if (null == emails || emails.isEmpty()) {
                UserEmailIntentService.asyncEmailsFromCloudAndSavesInDb(user, this);
            }
//        starts the main activity, with a valid user
            final boolean flag1 = MainApplicationSingleton.getInstance(getApplicationContext()).getBooleanValueSP(
                    MainApplicationSingleton.STARTUPLOCK_PARAM
            );
            if (flag1) {
                showUnLockPwdDialog();
            } else {
                startCluttoActivity(user);
            }
        }
        return result;
/*
        if (isUIDNull() || isTokenNull()) {
            if (isUIDNull()) {
//            loads the first user found in db
//            // TODO: 19-05-2016
//            to check for active user.. to enable multi sign on
//            packUserEmailsJoinFromDB();
//                fetch active user
                packUserFromDB();
//            fetch emails of users
                packUserEmailsJoinFromDB();
            } else {
//            fetch emails of users
            }
        }
*/
//        // TODO: 21-03-2016
//        users email has to be loaded.. account exists must be set from db correctly
/*
        if (1 == user.getAccountExists()) {
//            sync users emails from db
            UserContentProvider.populateUserEmailsJoinById(user, this);
        }
*/
    }

    public void showUnLockPwdDialog() {
        UnLockPasswordFragment.newInstance(user, this).show(
                getSupportFragmentManager(), "UnLockPasswordDialog");
    }

    private boolean isUIDNull() {
        return user == null || user.getDataId() == null || user.getDataId().isEmpty();
    }

    private boolean isTokenNull() {
        return user == null || user.getToken() == null || user.getToken().isEmpty();
    }

    private void startCluttoActivityForProfile(ContactItem user) {
//        // TODO: 08-06-2016
//        a profile update is required for this user to be visible for chat in clutto
        if (user.getName() != null && !user.getName().isEmpty()) {
            updateProfileTask = new UpdateProfileTask(user.getDataId(), user.getToken(),
                    user.getDevice(), user.getDeviceRef(), user.getName(), user.getStatus(),
                    MainApplicationSingleton.AUTH_UPDATE_PROFILE);
            updateProfileTask.setUpdateProfileTaskListener(this);
            updateProfileTask.execute();
        }

    }

    private void broadcastUserUpdate() {
        // broadcasts user change
        Intent intent = new Intent(
                MainApplicationSingleton.BROADCAST_NEW_USER_COMPLETE);
        intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onPostUpdateProfileExecute(JSONObject response) {
// Start clutto.. no waiting on profile update pass or fail
        Intent intent = new Intent(this, IntellibitzActivity.class);
        intent.setAction(ProfileItemFragment.TAG);
        intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
        startActivity(intent);
        try {
            int status = response.getInt(MainApplicationSingleton.STATUS_PARAM);
            if (99 == status || -1 == status) {
                onPostUpdateProfileExecuteFail(response);
            } else if (1 == status) {
//                    SUCCESS
//        // TODO: 08-06-2016
//        a profile pic update is required, eager update.. if the pic is available already
                String pic = user.getProfilePic();
                if (pic != null && !pic.isEmpty()) {
                    uploadProfilePicTask = new UploadProfilePicTask(user.getDataId(), user.getToken(),
                            user.getDevice(), user.getDeviceRef(),
                            MainApplicationSingleton.AUTH_UPDATE_PROFILE);
                    uploadProfilePicTask.setUploadProfilePicTaskListener(this);
                    File f = MediaPickerUri.resolveToFile(this, Uri.parse(pic));
                    uploadProfilePicTask.execute(f);
                }
                Log.e(TAG, "onPostUpdateProfileExecute: SUCCESS - " + response);
            }
        } catch (JSONException | IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Exception: " + e.getMessage());
        } finally {
            finish();
        }
    }

    @Override
    public void onPostUpdateProfileExecuteFail(JSONObject response) {
//                ERROR
        Log.e(TAG, "onPostUpdateProfileExecuteFail: ERROR - " + response);
        MainActivity.broadcastForceLogoutIfNeg1(response, this);
    }

    @Override
    public void setUpdateProfileTaskToNull() {
        updateProfileTask = null;
    }

    @Override
    public void onPostUploadProfilePicExecute(JSONObject response, String filename) {
        try {
            int status = response.getInt(MainApplicationSingleton.STATUS_PARAM);
            if (99 == status || -1 == status) {
                onPostUpdateProfileExecuteFail(response);
            } else if (1 == status) {
//                    SUCCESS
                Log.e(TAG, "onPostUploadProfilePicExecute: SUCCESS - " + response + " :file: " + filename);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "Exception: " + e.getMessage());
        } finally {
            finish();
        }
    }

    @Override
    public void onPostUploadProfilePicExecuteFail(JSONObject response, String filename) {
//                ERROR
        Log.e(TAG, "onPostUploadProfilePicExecuteFail: ERROR - " + response + " :file: " + filename);
        MainActivity.broadcastForceLogoutIfNeg1(response, this);
    }

    @Override
    public void setUploadProfilePicTaskToNull() {
        uploadProfilePicTask = null;
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
    private void startLoginActivity(ContactItem user, boolean isRegisterAgain) {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
        intent.putExtra("isRegisterAgain", isRegisterAgain);
        startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_LOGIN_RQ_CODE);
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
    private void startCheckEmailAvailableActivity(ContactItem user) {
        Intent intent = new Intent(this, CheckEmailAvailableActivity.class);
        intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
        startActivityForResult(intent, MainApplicationSingleton.ACT_CHK_EMAIL_AVBL_RQ_CODE);
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
    private void startAccountLandingActivity(ContactItem user) {
        Intent intent = new Intent(this, AccountLandingActivity.class);
        intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
        startActivityForResult(intent, MainApplicationSingleton.ACT_ACCT_LANDING_RQ_CODE);
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
    private void startVerifyCodeActivity(ContactItem user) {
        startVerifyCodeActivity(user, MainApplicationSingleton.ACT_VERIFY_CODE_RQ_CODE);
    }

    private void startVerifyCodeActivity(ContactItem user, int code) {
        Intent intent = new Intent(this, VerifyCodeActivity.class);
        intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
//        NOTE: there is a significant difference between the following two calls

//        THIS WILL DELIVER THE RESULT, TO PARENT ACTIVITY AND THE PARENT ACTIVITY IF OVERRIDDEN
//        MUST CALL SUPER.STARTACTIVITYFORRESULT IN ITS OVERRIDDEN METHOD FOR THE RESULT
//        TO BE DELIVERED TO THIS FRAGMENT
        startActivityForResult(intent, code);

//        THIS WILL DELIVER THE RESULT, TO PARENT ACTIVITY AND THE PARENT ACTIVITY IF OVERRIDDEN
//        MUST MANUALLY INVOKE THE FRAGMENTS IN ITS OVERRIDDEN METHOD FOR THE RESULT
//        TO BE DELIVERED TO THIS FRAGMENT
//        getAppCompatActivity().startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_CONTACTSELECT_RQ_CODE);
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
    private void startProfileSignupActivity(ContactItem user) {
        Intent intent = new Intent(this, ProfileSignupActivity.class);
        intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
//        NOTE: there is a significant difference between the following two calls

//        THIS WILL DELIVER THE RESULT, TO PARENT ACTIVITY AND THE PARENT ACTIVITY IF OVERRIDDEN
//        MUST CALL SUPER.STARTACTIVITYFORRESULT IN ITS OVERRIDDEN METHOD FOR THE RESULT
//        TO BE DELIVERED TO THIS FRAGMENT
        startActivityForResult(intent, MainApplicationSingleton.ACT_PROFILE_SIGNUP_RQ_CODE);

//        THIS WILL DELIVER THE RESULT, TO PARENT ACTIVITY AND THE PARENT ACTIVITY IF OVERRIDDEN
//        MUST MANUALLY INVOKE THE FRAGMENTS IN ITS OVERRIDDEN METHOD FOR THE RESULT
//        TO BE DELIVERED TO THIS FRAGMENT
//        getAppCompatActivity().startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_CONTACTSELECT_RQ_CODE);
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
    private void startCompanyCreateActivity(ContactItem user) {
        Intent intent = new Intent(this, CompanyCreateActivity.class);
        intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
//        NOTE: there is a significant difference between the following two calls

//        THIS WILL DELIVER THE RESULT, TO PARENT ACTIVITY AND THE PARENT ACTIVITY IF OVERRIDDEN
//        MUST CALL SUPER.STARTACTIVITYFORRESULT IN ITS OVERRIDDEN METHOD FOR THE RESULT
//        TO BE DELIVERED TO THIS FRAGMENT
        startActivityForResult(intent, MainApplicationSingleton.ACT_COMPANY_CREATE_RQ_CODE);

//        THIS WILL DELIVER THE RESULT, TO PARENT ACTIVITY AND THE PARENT ACTIVITY IF OVERRIDDEN
//        MUST MANUALLY INVOKE THE FRAGMENTS IN ITS OVERRIDDEN METHOD FOR THE RESULT
//        TO BE DELIVERED TO THIS FRAGMENT
//        getAppCompatActivity().startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_CONTACTSELECT_RQ_CODE);
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
    private void startGetInvitesActivity(ContactItem user) {
        Intent intent = new Intent(this, GetInvitesActivity.class);
        intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
//        NOTE: there is a significant difference between the following two calls

//        THIS WILL DELIVER THE RESULT, TO PARENT ACTIVITY AND THE PARENT ACTIVITY IF OVERRIDDEN
//        MUST CALL SUPER.STARTACTIVITYFORRESULT IN ITS OVERRIDDEN METHOD FOR THE RESULT
//        TO BE DELIVERED TO THIS FRAGMENT
        startActivityForResult(intent, MainApplicationSingleton.ACT_COMPANY_GETINVITES_RQ_CODE);

//        THIS WILL DELIVER THE RESULT, TO PARENT ACTIVITY AND THE PARENT ACTIVITY IF OVERRIDDEN
//        MUST MANUALLY INVOKE THE FRAGMENTS IN ITS OVERRIDDEN METHOD FOR THE RESULT
//        TO BE DELIVERED TO THIS FRAGMENT
//        getAppCompatActivity().startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_CONTACTSELECT_RQ_CODE);
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
    private void startLoginActivity(ContactItem user) {
        Intent intent = new Intent(this, CheckEmailAvailableActivity.class);
        intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
//        NOTE: there is a significant difference between the following two calls

//        THIS WILL DELIVER THE RESULT, TO PARENT ACTIVITY AND THE PARENT ACTIVITY IF OVERRIDDEN
//        MUST CALL SUPER.STARTACTIVITYFORRESULT IN ITS OVERRIDDEN METHOD FOR THE RESULT
//        TO BE DELIVERED TO THIS FRAGMENT
        startActivityForResult(intent, MainApplicationSingleton.ACT_CHK_EMAIL_AVBL_RQ_CODE);

//        THIS WILL DELIVER THE RESULT, TO PARENT ACTIVITY AND THE PARENT ACTIVITY IF OVERRIDDEN
//        MUST MANUALLY INVOKE THE FRAGMENTS IN ITS OVERRIDDEN METHOD FOR THE RESULT
//        TO BE DELIVERED TO THIS FRAGMENT
//        getAppCompatActivity().startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_CONTACTSELECT_RQ_CODE);
    }

    /*
        private void startMobileGetCodeActivity(boolean isRegisterAgain) {
            Intent intent = new Intent(this, RegistrationActivity.class);
            intent.putExtra(ContactItem.TAG, (Parcelable) user);
            intent.putExtra("isRegisterAgain", isRegisterAgain);
            startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_LOGIN_RQ_CODE);
        }

    */
    private void startCluttoActivity(ContactItem user) {
        Intent intent = new Intent(this, IntellibitzActivity.class);
        intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
        startActivity(intent);
        finish();
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
    private void startProfileInfoActivity(ContactItem user) {
        Intent intent = new Intent(this, ProfileInfoActivity.class);
        intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
//        NOTE: there is a significant difference between the following two calls

//        THIS WILL DELIVER THE RESULT, TO PARENT ACTIVITY AND THE PARENT ACTIVITY IF OVERRIDDEN
//        MUST CALL SUPER.STARTACTIVITYFORRESULT IN ITS OVERRIDDEN METHOD FOR THE RESULT
//        TO BE DELIVERED TO THIS FRAGMENT
        startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_PROFILEINFO_RQ_CODE);

//        THIS WILL DELIVER THE RESULT, TO PARENT ACTIVITY AND THE PARENT ACTIVITY IF OVERRIDDEN
//        MUST MANUALLY INVOKE THE FRAGMENTS IN ITS OVERRIDDEN METHOD FOR THE RESULT
//        TO BE DELIVERED TO THIS FRAGMENT
//        getAppCompatActivity().startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_CONTACTSELECT_RQ_CODE);
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
    private void startAddEmailActivity(ContactItem user) {
        Intent intent = new Intent(this, AddEmailActivity.class);
        intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
//        NOTE: there is a significant difference between the following two calls

//        THIS WILL DELIVER THE RESULT, TO PARENT ACTIVITY AND THE PARENT ACTIVITY IF OVERRIDDEN
//        MUST CALL SUPER.STARTACTIVITYFORRESULT IN ITS OVERRIDDEN METHOD FOR THE RESULT
//        TO BE DELIVERED TO THIS FRAGMENT
        startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_ADDEMAIL_RQ_CODE);

//        THIS WILL DELIVER THE RESULT, TO PARENT ACTIVITY AND THE PARENT ACTIVITY IF OVERRIDDEN
//        MUST MANUALLY INVOKE THE FRAGMENTS IN ITS OVERRIDDEN METHOD FOR THE RESULT
//        TO BE DELIVERED TO THIS FRAGMENT
//        getAppCompatActivity().startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_CONTACTSELECT_RQ_CODE);
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
    private void startOTPActivity(ContactItem user) {
        Intent intent = new Intent(this, OTPActivity.class);
        intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
//        NOTE: there is a significant difference between the following two calls

//        THIS WILL DELIVER THE RESULT, TO PARENT ACTIVITY AND THE PARENT ACTIVITY IF OVERRIDDEN
//        MUST CALL SUPER.STARTACTIVITYFORRESULT IN ITS OVERRIDDEN METHOD FOR THE RESULT
//        TO BE DELIVERED TO THIS FRAGMENT
        startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_OTP_RQ_CODE);

//        THIS WILL DELIVER THE RESULT, TO PARENT ACTIVITY AND THE PARENT ACTIVITY IF OVERRIDDEN
//        MUST MANUALLY INVOKE THE FRAGMENTS IN ITS OVERRIDDEN METHOD FOR THE RESULT
//        TO BE DELIVERED TO THIS FRAGMENT
//        getAppCompatActivity().startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_CONTACTSELECT_RQ_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        int respCode = intent.getIntExtra(MainApplicationSingleton.RESP_CODE, -1);
        if (MainApplicationSingleton.ACT_CHK_EMAIL_AVBL_RQ_CODE == requestCode) {
            user = intent.getParcelableExtra(ContactItem.USER_CONTACT);
            if (Activity.RESULT_OK == resultCode) {
                execGetEmailVerification();
                startVerifyCodeActivity(user);
            } else if (Activity.RESULT_CANCELED == resultCode) {
                if (-1 == respCode) {
                    finish();
                } else if (111 == respCode) {
//                    forgots pwd
                    execGetEmailVerification();
                    startVerifyCodeActivity(user, MainApplicationSingleton.ACT_VERIFY_CODE_FORGOT_PWD_RQ_CODE);
                } else if (222 == respCode) {
//                to send to existing user login
                    startCluttoActivity(user);
                }
            }
        } else if (MainApplicationSingleton.ACT_VERIFY_CODE_FORGOT_PWD_RQ_CODE == requestCode) {
            if (Activity.RESULT_OK == resultCode) {
                user = intent.getParcelableExtra(ContactItem.USER_CONTACT);
                showForgotPwdDialog(user);
            } else if (Activity.RESULT_CANCELED == resultCode) {
//                to send to existing user login
                finish();
            }
        } else if (MainApplicationSingleton.ACT_VERIFY_CODE_RQ_CODE == requestCode) {
            if (Activity.RESULT_OK == resultCode) {
                user = intent.getParcelableExtra(ContactItem.USER_CONTACT);
//                // TODO: 13/9/16
//                to popup new password dialog
                NewPasswordFragment.newInstance(user).show(
                        getSupportFragmentManager(), "NewPasswordDialog");
            } else if (Activity.RESULT_CANCELED == resultCode) {
//                to send to existing user login
                finish();
            }
        } else if (MainApplicationSingleton.ACT_PROFILE_SIGNUP_RQ_CODE == requestCode) {
            if (Activity.RESULT_OK == resultCode) {
                user = intent.getParcelableExtra(ContactItem.USER_CONTACT);
//                // TODO: 13/9/16
//                to start companyName activity
                execGetInvitesTask(user);
//                startCompanyCreateActivity(user);
            } else if (Activity.RESULT_CANCELED == resultCode) {
//                to send to existing user login
                finish();
            }
        } else if (MainApplicationSingleton.ACT_COMPANY_GETINVITES_RQ_CODE == requestCode) {
            if (Activity.RESULT_OK == resultCode) {
                user = intent.getParcelableExtra(ContactItem.USER_CONTACT);
//                // TODO: 13/9/16
//                to start companyName activity
                startCompanyCreateActivity(user);
            } else if (Activity.RESULT_CANCELED == resultCode) {
//                to send to existing user login
                startCompanyCreateActivity(user);
            }
        } else if (MainApplicationSingleton.ACT_COMPANY_CREATE_RQ_CODE == requestCode) {
            if (Activity.RESULT_OK == resultCode) {
                user = intent.getParcelableExtra(ContactItem.USER_CONTACT);
                startAddEmailActivity(user);
//                notifyUserBaseItemListeners();
//                broadcastUserUpdate();
            } else if (Activity.RESULT_CANCELED == resultCode) {
//                // TODO: 19-07-2016
//                this can stuck in infinite loop.. in the login screen.. if login fails
//                startLoginActivity(user, false);
                user = intent.getParcelableExtra(ContactItem.USER_CONTACT);
//                startCluttoActivityForProfile(user);
//                notifyUserBaseItemListeners();
//                broadcastUserUpdate();
                startAddEmailActivity(user);
            }
        } else if (MainApplicationSingleton.ACTIVITY_LOGIN_RQ_CODE == requestCode) {
            if (Activity.RESULT_OK == resultCode) {
                user = intent.getParcelableExtra(ContactItem.USER_CONTACT);
                startOTPActivity(user);
//                startCluttoActivityForProfile(user);
//                notifyUserBaseItemListeners();
//                broadcastUserUpdate();
            } else if (Activity.RESULT_CANCELED == resultCode) {
//                // TODO: 19-07-2016
//                this can stuck in infinite loop.. in the login screen.. if login fails
                startLoginActivity(user, false);
            }
        } else if (MainApplicationSingleton.ACTIVITY_OTP_RQ_CODE == requestCode) {
            if (Activity.RESULT_OK == resultCode) {
                user = intent.getParcelableExtra(ContactItem.USER_CONTACT);
                startProfileInfoActivity(user);
//                notifyUserBaseItemListeners();
//                broadcastUserUpdate();
            } else if (Activity.RESULT_CANCELED == resultCode) {
//                // TODO: 19-07-2016
//                this can stuck in infinite loop.. in the login screen.. if login fails
                startLoginActivity(user, false);
            }
        } else if (MainApplicationSingleton.ACTIVITY_PROFILEINFO_RQ_CODE == requestCode) {
            if (Activity.RESULT_OK == resultCode) {
                user = intent.getParcelableExtra(ContactItem.USER_CONTACT);
                startAddEmailActivity(user);
//                notifyUserBaseItemListeners();
//                broadcastUserUpdate();
            } else if (Activity.RESULT_CANCELED == resultCode) {
//                // TODO: 19-07-2016
//                this can stuck in infinite loop.. in the login screen.. if login fails
//                startLoginActivity(user, false);
                user = intent.getParcelableExtra(ContactItem.USER_CONTACT);
                startCluttoActivityForProfile(user);
                notifyUserBaseItemListeners();
                broadcastUserUpdate();
            }
        } else if (MainApplicationSingleton.ACTIVITY_ADDEMAIL_RQ_CODE == requestCode) {
            if (Activity.RESULT_OK == resultCode) {
                user = intent.getParcelableExtra(ContactItem.USER_CONTACT);
//                startCluttoActivityForProfile(user);
                startCluttoActivity(user);
                notifyUserBaseItemListeners();
                broadcastUserUpdate();
            } else if (Activity.RESULT_CANCELED == resultCode) {
//                // TODO: 19-07-2016
//                this can stuck in infinite loop.. in the login screen.. if login fails
//                startLoginActivity(user, false);
                user = intent.getParcelableExtra(ContactItem.USER_CONTACT);
//                startCluttoActivityForProfile(user);
                startCluttoActivity(user);
                notifyUserBaseItemListeners();
                broadcastUserUpdate();
            }
        }
    }

    private void showForgotPwdDialog(ContactItem user) {
        //                // TODO: 13/9/16
//                to popup new password dialog
        NewPasswordFragment newPasswordFragment = NewPasswordFragment.newInstance(user);
        newPasswordFragment.setMode(MainApplicationSingleton.ACT_VERIFY_CODE_FORGOT_PWD_RQ_CODE);
        newPasswordFragment.show(getSupportFragmentManager(), "NewPasswordDialog");
    }

    private void retryForgotPwdDialog(ContactItem user) {
        //                // TODO: 13/9/16
//                to popup new password dialog
        NewPasswordFragment newPasswordFragment = NewPasswordFragment.newInstance(user);
        newPasswordFragment.setMode(MainApplicationSingleton.ACT_VERIFY_CODE_FORGOT_PWD_RQ_CODE);
        newPasswordFragment.setError("Please try again");
        newPasswordFragment.show(getSupportFragmentManager(), "NewPasswordDialog");
    }


    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        startCluttoActivity(user);
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        finish();
    }

    @Override
    public void onNewPasswordDialogPositiveClick(DialogFragment dialog) {
// // TODO: 13/9/16
//        to start profile signup activity
        NewPasswordFragment newPasswordFragment = ((NewPasswordFragment) dialog);
        if (newPasswordFragment != null) {
            ContactItem user = newPasswordFragment.getUser();
            if (MainApplicationSingleton.ACT_VERIFY_CODE_FORGOT_PWD_RQ_CODE == newPasswordFragment.getMode()) {
                execSetPwdTask(user, ((NewPasswordFragment) dialog).getMode());
            } else {
                startProfileSignupActivity(user);
                dialog.dismiss();
            }
        }
    }

    @Override
    public void onNewPasswordDialogNegativeClick(DialogFragment dialog) {

    }

    private void execGetInvitesTask(ContactItem user) {
        GetInvitesTask getInvitesTask = new GetInvitesTask(user.getDataId(), user.getToken(),
                user.getDevice(), user.getDeviceRef(), user,
                MainApplicationSingleton.AUTH_COMPANY_GET_INVITES, this);
        getInvitesTask.setRequestTimeoutMillis(30000);
        getInvitesTask.setGetInvitesTaskListener(this);
        getInvitesTask.execute();
    }

    @Override
    public void onPostGetInvitesResponse(JSONObject response, ContactItem user) {
        try {
            int status = response.getInt("status");
            if (1 == status) {
                startGetInvitesActivity(user);
            } else if (99 == status) {
                startCompanyCreateActivity(user);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            startCompanyCreateActivity(user);
        }
    }

    @Override
    public void onPostGetInvitesErrorResponse(JSONObject response) {
        Log.e(TAG, "onPostGetInvitesErrorResponse: " + response);
    }

    private void execSetPwdTask(ContactItem user, int mode) {
        SetPwdTask setPwdTask = new SetPwdTask(user.getEmail(), mode, user.getOtp(), user.getPwd(),
                user.getDevice(), user.getDeviceId(), user.getDeviceName(),
                MainApplicationSingleton.AUTH_ACCOUNT_SET_PWD, user, this);
        setPwdTask.setRequestTimeoutMillis(30000);
        setPwdTask.setSetPwdTaskListener(this);
        setPwdTask.execute();
    }

    @Override
    public void onPostSetPwdResponse(JSONObject response, String email, ContactItem user, int mode) {
        try {
            int status = response.optInt(MainApplicationSingleton.STATUS_PARAM, -1);
            if (99 == status || -1 == status) {
                if (MainApplicationSingleton.ACT_VERIFY_CODE_FORGOT_PWD_RQ_CODE == mode) {
                    retryForgotPwdDialog(user);
                    return;
                }
                onPostSetPwdErrorResponse(response);
            } else if (1 == status) {
                UserContentProvider.activateUserInDB(response, user, this);
                startCluttoActivity(user);
            }
        } catch (Throwable e) {
            e.printStackTrace();
            Log.e(TAG, "Exception: " + e.getMessage());
            try {
                onPostSetPwdErrorResponse(
                        new JSONObject("{\"err\":\"onPostSignupResponse - failed \"" +
                                e.toString() + "}"));
            } catch (JSONException e1) {
                onPostSetPwdErrorResponse(null);
            }
        }

    }

    @Override
    public void onPostSetPwdErrorResponse(JSONObject response) {
        Log.e(TAG, "onPostSignupErrorResponse: " + response);

    }

    private void execGetEmailVerification() {
        GetEmailVerificationTask getEmailVerificationTask = new GetEmailVerificationTask(user.getEmail(),
                MainApplicationSingleton.AUTH_ACCOUNT_GET_EMAIL_VERIFICATION, this);
        getEmailVerificationTask.setRequestTimeoutMillis(30000);
        getEmailVerificationTask.setGetEmailVerificationTaskListener(this);
        getEmailVerificationTask.execute();
    }

    @Override
    public void onPostGetEmailVerificationResponse(JSONObject response, String email, ContactItem user) {

    }

    @Override
    public void onPostGetEmailVerificationErrorResponse(JSONObject response) {

    }






/*
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        // Example of a call to a native method
        TextView tv = (TextView) findViewById(R.id.sample_text);
        tv.setText("SAMPLE TEXT");
    }
*/

/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
*/

}
