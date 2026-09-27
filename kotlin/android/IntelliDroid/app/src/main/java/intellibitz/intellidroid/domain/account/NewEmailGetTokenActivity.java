package intellibitz.intellidroid.domain.account;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import intellibitz.intellidroid.content.UserEmailContentProvider;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.task.GetTokenTask;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.R;
import intellibitz.intellidroid.content.UserEmailContentProvider;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.task.GetTokenTask;
import intellibitz.intellidroid.util.MainApplicationSingleton;

import intellibitz.intellidroid.content.UserEmailContentProvider;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class NewEmailGetTokenActivity extends AppCompatActivity implements GetTokenTask.GetTokenTaskListener {
    private static final String TAG = "GETToken";
    // UI references.
    private View mProgressView;
    private ContactItem user;
    private GetTokenTask emailGoogleTokenTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_token);
        user = getIntent().getParcelableExtra(ContactItem.USER_CONTACT);
//        Bundle extras = getIntent().getExtras();
//        String code = extras.getString(MainApplicationSingleton.CODE_PARAM);
//        String email = extras.getString(MainApplicationSingleton.EMAIL_PARAM);
        mProgressView = findViewById(R.id.token_progress);
        if (MainApplicationSingleton.CheckNetworkConnection.isNetworkConnectionAvailable(this)) {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            emailGoogleTokenTask = new GetTokenTask(user.getEmail(), user.getEmailCode(),
                    user.getDataId(), user.getToken(), user.getDevice(), user.getDeviceRef(),
                    MainApplicationSingleton.AUTH_EMAIL_GOOGLE_TOKEN);
            emailGoogleTokenTask.setGetTokenTaskListener(this);
            emailGoogleTokenTask.execute();
        } else {
//            // TODO: 06-02-2016
//            handle no network condition
        }
    }


    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void addEmailAccount() {
        try {
//                    ContactItem userItem = new ContactItem();
//                    userItem.set_id(MainApplicationSingleton.getInstance().getLongValueSP(
//                            MainApplicationSingleton.ID_PARAM));
//            adds the current presentation carried email, to the collection.. time to save
            Log.d(TAG, user.toString());
            String email = user.getEmail();
            user.addEmail(email, user.getEmailCode());
            ContentValues values = new ContentValues();
            values.put(ContactItem.USER_CONTACT, MainApplicationSingleton.Serializer.serialize(user));
            ContentResolver contentResolver = getContentResolver();
            Uri uri = contentResolver.insert(Uri.withAppendedPath(
                    UserEmailContentProvider.CONTENT_URI, email), values);
            Log.e(TAG, "SUCCESS - User: " + user);
            Log.e(TAG, "SUCCESS - Email insert: " + uri);
        } catch (IOException e) {
            e.printStackTrace();
        }
/*
        MainApplicationSingleton mainApplication = MainApplicationSingleton.getInstance();
*/
/*
        mainApplication.putGlobalVariable(MainApplication.TOKEN_PARAM, token);
        mainApplication.putGlobalVariable(MainApplication.UID_PARAM, uid);
*//*

        mainApplication.putGlobalVariable(MainApplicationSingleton.EMAIL_PARAM, email);
        mainApplication.addStringSetValueSP(MainApplicationSingleton.EMAIL_PARAM, email);

//        // TODO: 10-03-2016
//                remove this hack.. do in async, or a service
        HandlerThread mHandlerThread = new HandlerThread("HandlerThread");
        mHandlerThread.start();
        Handler mHandler = new Handler(mHandlerThread.getLooper());
        mHandler.post(new Runnable() {
            @Override
            public void run() {
            }
        });

*/
    }

    @Override
    public void onPostGetTokenTaskExecute(JSONObject response) {
        try {
            int status = response.getInt(MainApplicationSingleton.STATUS_PARAM);
            if (99 == status) {
//                ERROR
//                    bundle.putString(MainApplicationSingleton.EMAIL_PARAM, user.getEmail());
//// TODO: 11-02-2016
//                    get back to application flow.. set password, home et al
/*
                    Intent intent = new Intent(NewEmailGetTokenActivity.this, NewEmailAccountActivity.class);
                    intent.putExtra(ContactItem.TAG, (Parcelable) user);
                    intent.putExtra("error", response.toString());
                    startActivity(intent);
*/
                Intent intent = getIntent();
                intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
                setResult(Activity.RESULT_CANCELED, intent);
                finish();
            } else if (1 == status) {
//                    SUCCESS
                //// TODO: 05-02-2016
//                    token ok, password to be set for user
//                    String url = "https://www.google.com";
//                    Bundle bundle = new Bundle();
//                    bundle.putString(MainApplicationSingleton.UID_PARAM, uid);
//                    bundle.putString(MainApplicationSingleton.TOKEN_PARAM, token);
                String email = response.getString("email");
                if (!TextUtils.isEmpty(email)) {
                    user.setEmail(email.trim());
//                        // TODO: 26-02-2016
//                        alert user of the email mismatch - happens when the user selects a different
//                        email for signing in than the one he provided in the form
                }

//                    bundle.putString(MainApplicationSingleton.EMAIL_PARAM, mEmail);
                addEmailAccount();
//                    mainApplication.putStringValueSP(MainApplication.UID_PARAM, uid);
//                    mainApplication.putStringValueSP(MainApplication.CODE_PARAM, mCode);
//                    mainApplication.putStringValueSP(MainApplication.TOKEN_PARAM, token);
//                    mainApplication.putStringValueSP(MainApplication.DEVICE_PARAM, mDevice);
//                    Notify the Main Activity of the account completion, so the socket service can be restarted
                Intent registrationComplete = new Intent(
                        MainApplicationSingleton.BROADCAST_EMAIL_ACCOUNT_ADDED);
                registrationComplete.putExtra(ContactItem.USER_CONTACT,
                        (Parcelable) user);
                LocalBroadcastManager.getInstance(
                        NewEmailGetTokenActivity.this).sendBroadcast(registrationComplete);
//// TODO: 11-02-2016
//                    get back to application flow.. set password, home et al
//                    Intent intent = new Intent(NewEmailGetTokenActivity.this, IntellibitzActivity.class);
//                    intent.putExtra(ContactItem.TAG, (Parcelable) user);
//                    intent.putExtras(bundle);
//                    startActivity(intent);
//                    startActivityForResult(intent, 1, bundle);
                Intent intent = getIntent();
                intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
                setResult(Activity.RESULT_OK, intent);
                finish();

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        finish();
        showProgress(false);
    }

    @Override
    public void onPostGetTokenTaskExecuteFail(JSONObject response) {
        Intent intent = getIntent();
        intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
        setResult(Activity.RESULT_CANCELED, intent);
        finish();
        showProgress(false);

    }

    @Override
    public void setGetTokenTaskToNull() {
        emailGoogleTokenTask = null;
    }
}

