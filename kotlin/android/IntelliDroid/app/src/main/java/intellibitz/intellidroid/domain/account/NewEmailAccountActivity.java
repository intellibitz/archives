package intellibitz.intellidroid.domain.account;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.task.AddEmailTask;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.R;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.task.AddEmailTask;
import intellibitz.intellidroid.util.MainApplicationSingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class NewEmailAccountActivity extends
        AppCompatActivity implements
        AddEmailTask.AddEmailTaskListener {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 21;
    private static final int GET_CODE_RESULT = 11;
    private static final int GET_TOKEN_RESULT = 12;

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private AddEmailTask addEmailTask = null;

    // UI references.
    private AutoCompleteTextView tvEmail;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private ContactItem user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        user = getIntent().getParcelableExtra(ContactItem.USER_CONTACT);
        // Set up the login form.
        tvEmail = (AutoCompleteTextView) findViewById(R.id.email);
//        populates the autocomplete text box, with contacts from device
        populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        if (mEmailSignInButton != null) {
            mEmailSignInButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    attemptLogin();
                }
            });
        }

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        Bundle bundle = getIntent().getExtras();
        if (null != bundle) {
            tvEmail.setText(bundle.getString("email"));
//            mPasswordView.setError(bundle.getString("error"));
            setError(bundle.getString("error"), "Action");
        }

        ImageButton btnBack = (ImageButton) findViewById(R.id.btn_back);
        btnBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
//        setUpToolbar();
    }

    private void setError(String title, String action) {
        Snackbar.make(mLoginFormView, title, Snackbar.LENGTH_LONG)
                .setAction(action, null).show();
    }

/*
    private void setUpToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.add_email_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.app_title);
    }
*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_addemail_toolbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item != null &&
                item.getTitle().equals(getResources().getString(R.string.menu_title_cancel))) {
//            cancels adding account, exit
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void populateAutoComplete() {
        if (mayRequestContacts()) {
            new SetupEmailAutoCompleteTask().execute(null, null);
        }

/*
        if (VERSION.SDK_INT >= 14) {
            // Use ContactsContract.Profile (API 14+)
            getLoaderManager().initLoader(0, null, this);
        } else if (VERSION.SDK_INT >= 8) {
            // Use AccountManager (API 8+)
            new SetupEmailAutoCompleteTask().execute(null, null);
        }
*/
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
//            // TODO: 28-02-2016
//            to show rationale.. right now request immediately anyways
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
            Snackbar.make(tvEmail, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    }).show();
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (addEmailTask != null) {
            return;
        }

        // Reset errors.
        tvEmail.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = tvEmail.getText().toString();
        if (!TextUtils.isEmpty(email)) {
//            remove spaces at the end
            user.setEmail(email.trim());
            email = user.getEmail();
        }
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            tvEmail.setError(getString(R.string.error_field_required));
            focusView = tvEmail;
            cancel = true;
        } else if (!isEmailValid(email)) {
            tvEmail.setError(getString(R.string.error_invalid_email));
            focusView = tvEmail;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Gets the URL from the UI's text field.
            if (MainApplicationSingleton.CheckNetworkConnection.isNetworkConnectionAvailable(this)) {
/*
            ConnectivityManager connMgr = (ConnectivityManager)
                    getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
*/
                // Show a progress spinner, and kick off a background task to
                // perform the user login attempt.
                showProgress(true);
                execAddEmailTask(password);
            } else {
                tvEmail.setError("No network connection available.");
                setError("No network connection available.", "Action");
            }
        }
    }

    private void execAddEmailTask(String password) {
        addEmailTask = new AddEmailTask(user.getEmail(), password,
                user.getDataId(), user.getToken(), user.getDevice(), user.getDeviceRef(),
                MainApplicationSingleton.AUTH_ADD_EMAIL);
        addEmailTask.setAddEmailTaskListener(this);
        addEmailTask.execute((Void) null);
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
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

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

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
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(NewEmailAccountActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        tvEmail.setAdapter(adapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (GET_CODE_RESULT == requestCode) {
            if (Activity.RESULT_OK == resultCode) {
                user = data.getParcelableExtra(ContactItem.USER_CONTACT);
                //                Start the get token activity
                Intent intent = new Intent(this, NewEmailGetTokenActivity.class);
                intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
                startActivityForResult(intent, GET_TOKEN_RESULT);

            } else if (Activity.RESULT_CANCELED == resultCode) {

            }
        } else if (GET_TOKEN_RESULT == requestCode) {
            if (Activity.RESULT_OK == resultCode) {
                user = data.getParcelableExtra(ContactItem.USER_CONTACT);
//                the work flow is complete.. go back
                Intent intent = getIntent();
                intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
                setResult(Activity.RESULT_OK, intent);
                finish();
            } else if (Activity.RESULT_CANCELED == resultCode) {

            }
        }
    }

    @Override
    public void onPostAddEmailTaskExecute(JSONObject response) {
        addEmailTask = null;
        showProgress(false);

        if (response != null) {
            mPasswordView.setError(response.toString());
            setError(response.toString(), "Action");
        }
        try {
            int status = response.getInt(MainApplicationSingleton.STATUS_PARAM);
//                url returned
            if (1 == status) {
                //// TODO: 05-02-2016
//                    Google InApp browser authentication begins
                String url = response.getString(MainApplicationSingleton.URL_PARAM);
                user.setEmailURL(url);
//                    MainApplicationSingleton.getInstance().putGlobalVariable(
//                            MainApplicationSingleton.EMAIL_PARAM, user.getEmail());
//                    String url = "https://www.google.com";
                Intent intent = new Intent(NewEmailAccountActivity.this, WebViewActivity.class);
//                    intent.putExtra(MainApplicationSingleton.URL_PARAM, url);
                intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
                startActivityForResult(intent, GET_CODE_RESULT);
//                    startActivityForResult(intent, 1, bundle);
            } else if (99 == status) {
//                    // TODO: 22-02-2016
//                    handle error
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPostAddEmailTaskExecuteFail(JSONObject response) {
        addEmailTask = null;
        showProgress(false);
//                mPasswordView.setError(getString(R.string.error_incorrect_password));
        mPasswordView.requestFocus();
    }

    @Override
    public void setAddEmailTaskToNull() {

    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * Use an AsyncTask to fetch the user's email addresses on a background thread, and update
     * the email text field with results on the main UI thread.
     */
    class SetupEmailAutoCompleteTask extends AsyncTask<Void, Void, List<String>> {

        @Override
        protected List<String> doInBackground(Void... voids) {

//            // TODO: 28-02-2016

//            hack!! remove.. email check must not be done here
//            EmailSyncHandler.SyncEmailsFromCloud();
/*
            Context context = NewEmailAccountActivity.this;
            Intent intent = new Intent(context,
                    UserEmailIntentService.class);
            intent.setAction(UserEmailIntentService.ACTION_SYNC_MESSAGE_ATTACHMENTS);
            ContactItem user = new ContactItem();
            intent.putExtra(ContactItem.TAG, (Serializable) user);
            context.startService(intent);
*/

            // TODO: register the new account here.
//            investigate response and take action
            ArrayList<String> emailAddressCollection = new ArrayList<>();

            // Get all emails from the user's contacts and copy them to a list.
            ContentResolver cr = getContentResolver();
            Cursor emailCur = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                    null, null, null);
            if (emailCur != null) {
                while (emailCur.moveToNext()) {
                    String email = emailCur.getString(emailCur.getColumnIndex(ContactsContract
                            .CommonDataKinds.Email.DATA));
                    emailAddressCollection.add(email);
                }
                emailCur.close();
            }

            return emailAddressCollection;
        }

        @Override
        protected void onPostExecute(List<String> emailAddressCollection) {
            addEmailsToAutoComplete(emailAddressCollection);
        }
    }

}

