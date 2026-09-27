package intellibitz.intellidroid.account;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.google.android.material.snackbar.Snackbar;
import intellibitz.intellidroid.IntellibitzPermissionFragment;
import intellibitz.intellidroid.IntellibitzUserFragment;
import intellibitz.intellidroid.company.GetInvitesTask;
import intellibitz.intellidroid.content.UserContentProvider;
import intellibitz.intellidroid.content.UserEmailContentProvider;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.service.ContactService;
import intellibitz.intellidroid.task.GetEmailsTask;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.IntellibitzPermissionFragment;
import intellibitz.intellidroid.IntellibitzUserFragment;
import intellibitz.intellidroid.R;
import intellibitz.intellidroid.company.GetInvitesTask;
import intellibitz.intellidroid.content.UserContentProvider;
import intellibitz.intellidroid.content.UserEmailContentProvider;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.service.ContactService;
import intellibitz.intellidroid.task.GetEmailsTask;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.IntellibitzPermissionFragment;
import intellibitz.intellidroid.IntellibitzUserFragment;
import intellibitz.intellidroid.company.GetInvitesTask;
import intellibitz.intellidroid.content.UserContentProvider;
import intellibitz.intellidroid.content.UserEmailContentProvider;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.service.ContactService;
import intellibitz.intellidroid.task.GetEmailsTask;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;


/**
 *
 */
public class CheckEmailAvailableFragment extends
        IntellibitzUserFragment implements
        CheckEmailAvailableTask.CheckEmailAvailableTaskListener,
        UserSignupFragment.OnUserSignupFragmentListener,
        LoginTask.LoginTaskListener,
        GetProfileTask.GetProfileTaskListener,
        GetEmailsTask.GetEmailsTaskListener,
        GetInvitesTask.GetInvitesTaskListener {

    private static final String TAG = "ChkEmailAvblFrag";
    private static final int REQUEST_READ_CONTACTS = 21;
    private AutoCompleteTextView tvEmail;
    private TextView tvPassword;
    private View view;
    private View snackView;
    private Snackbar snackbar;
    private ProgressDialog progressDialog;
    private Button btnPermission;
    private View viewPermission;

    public CheckEmailAvailableFragment() {
        super();
    }

    public static CheckEmailAvailableFragment newInstance(ContactItem user) {
        CheckEmailAvailableFragment fragment = new CheckEmailAvailableFragment();
        fragment.setUser(user);
        Bundle args = new Bundle();
        args.putParcelable(ContactItem.USER_CONTACT, user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        alertReadPhoneState();
    }

    public void alertReadPhoneState() {
        if (null == snackbar) {
            snackbar = makeReadPhoneStateSnack(getSnackView(), getActivity());
        }
        if (isReadPhoneStatePermissionGranted(getContext())) {
            if (snackbar != null)
                snackbar.dismiss();
            viewPermission.setVisibility(View.GONE);
        } else {
            viewPermission.setVisibility(View.VISIBLE);
            mayRequestReadPhoneState(snackbar, getSnackView());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_checkemailavailable, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (null == savedInstanceState) {
            user = getArguments().getParcelable(ContactItem.USER_CONTACT);
        } else {
            user = savedInstanceState.getParcelable(ContactItem.USER_CONTACT);
        }

        tvEmail = (AutoCompleteTextView) view.findViewById(R.id.tv_email);

//        populates the autocomplete text box, with contacts from device
        populateAutoComplete();

        tvPassword = (TextView) view.findViewById(R.id.tv_password);
        tvEmail.setDropDownWidth(getResources().getDisplayMetrics().widthPixels);
        TextView.OnEditorActionListener onEditorActionListener = new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // Perform action on key press
                    performLoginOrSignup();
                    return true;
                }
                return false;
            }
        };
        tvEmail.setOnEditorActionListener(onEditorActionListener);
        tvEmail.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                Log.d(TAG, keyCode + " " + event);
                if (KeyEvent.ACTION_UP == event.getAction()) {
                    String text = tvEmail.getText().toString();
                    if (text.endsWith(".com") && text.contains("@")) {
                        performLoginOrSignup();
                        return true;
                    }
                }
                return false;
/*
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode != KeyEvent.KEYCODE_ENTER) {
                }
*/
            }
        });

        View rlLogin = view.findViewById(R.id.rl_login);
        rlLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performLoginOrSignup();
            }
        });
        View btnForgotPwd = view.findViewById(R.id.btn_forgotpwd);
        btnForgotPwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performForgotPwd();
            }
        });
        ImageButton btnLogin = (ImageButton) view.findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performLoginOrSignup();
            }
        });

        tvEmail.requestFocus();
        unPackUser();

        btnPermission = (Button) view.findViewById(R.id.btn_perm);
        btnPermission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Activity activity = getActivity();
                if (null == activity) return;
                requestReadPhoneStatePermissions(activity);
            }
        });
        viewPermission = view.findViewById(R.id.ll_perm);
        getSnackView();

        if (!IntellibitzPermissionFragment.isReadPhoneStatePermissionGranted(getContext())) {
            mayRequestReadPhoneState(snackView);
        }
    }

    private void unPackUser() {
        final String email = user.getEmail();
        if (!TextUtils.isEmpty(email))
            tvEmail.setText(email);
    }

    public View getSnackView() {
        if (null == snackView)
//            snackView = getActivity().findViewById(R.id.cl);
//            snackView = view.findViewById(R.id.ll);
            snackView = view.findViewById(R.id.username);
        return snackView;
    }

    public ContactItem packUser() {
        String email = tvEmail.getText().toString();
        if (!TextUtils.isEmpty(email))
            user.setEmail(email);
        String pwd = tvPassword.getText().toString();
        if (!TextUtils.isEmpty(pwd))
            user.setPwd(pwd);
        return user;
    }

    public void setError(String text) {
        //        clears the error message and the icon
        if (tvEmail != null)
            tvEmail.setError(text);
    }

    public void showProgress() {
        progressDialog = ProgressDialog.show(getActivity(), "Login", getString(R.string.verify_code), true);
    }

    public void hideProgress() {
        if (progressDialog != null)
            progressDialog.dismiss();
    }

    public void performForgotPwd() {
        packUser();
        if (TextUtils.isEmpty(user.getEmail())) {
            setError("Email is required");
        } else {
            UserContentProvider.setsDeviceIdNameInfo(user, getContext());
            forgotPwdActivity();
        }
    }

    public void performLoginOrSignup() {
        packUser();
        if (TextUtils.isEmpty(user.getPwd())) {
            checkEmailAvailable();
        } else {
            performLogin();
        }
    }

    public void checkEmailAvailable() {
        Activity activity = getActivity();
        if (null == activity) return;
        if (MainApplicationSingleton.CheckNetworkConnection.isNetworkConnectionAvailable(activity)) {
            showProgress();
            checkEmailAvailableTask();
        }
    }

    public void performLogin() {
        Activity activity = getActivity();
        if (null == activity) return;
        if (MainApplicationSingleton.CheckNetworkConnection.isNetworkConnectionAvailable(activity)) {
            showProgress();
            setError(null);
            UserContentProvider.setsDeviceIdNameInfo(user, getContext());
            loginTask();
        }
    }

    protected void okActivity(int respCode) {
        Activity activity = getActivity();
        Intent intent = activity.getIntent();
        intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
        intent.putExtra(MainApplicationSingleton.RESP_CODE, respCode);
        activity.setResult(Activity.RESULT_OK, intent);
        activity.finish();
    }

    protected void forgotPwdActivity() {
        Activity activity = getActivity();
        Intent intent = activity.getIntent();
        intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
        intent.putExtra(MainApplicationSingleton.RESP_CODE, 111);
        activity.setResult(Activity.RESULT_CANCELED, intent);
        activity.finish();
    }

    protected void loginActivity() {
        Activity activity = getActivity();
        Intent intent = activity.getIntent();
        intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
        intent.putExtra(MainApplicationSingleton.RESP_CODE, 222);
        activity.setResult(Activity.RESULT_CANCELED, intent);
        activity.finish();
    }


    @Override
    public void onPostCheckEmailAvailableResponse(JSONObject response, String email, ContactItem user) {
        try {
            Log.d(TAG, TAG + response);
            int status = response.optInt("status", -1);
            int respCode = response.optInt("resp_code", -1);
            if (1 == status) {
//                email is available
                if (101 == respCode) {
//                    new user
                    UserSignupFragment.newInstance(user, this).show(
                            getChildFragmentManager(), "UserSignupDialog");

                } else if (102 == respCode) {
//                    existing user
//                    // TODO: 13/9/16
//                    enables the password field, and login
                    tvPassword.setEnabled(true);
                    tvPassword.requestFocus();
//                    cancelActivity(respCode);

                } else {
//                    // TODO: 13/9/16
//                    to work existing email logic
                    onPostCheckEmailAvailableErrorResponse(response);
                }
            } else if (-1 == status || 99 == status) {
                onPostCheckEmailAvailableErrorResponse(response);
            }
        } catch (Throwable e) {
            e.printStackTrace();
            onPostCheckEmailAvailableErrorResponse(response);
        }
        hideProgress();
    }

    @Override
    public void onPostCheckEmailAvailableErrorResponse(JSONObject response) {
        Log.e(TAG, "onPostCheckEmailAvailableErrorResponse: " + response);
        if (null == response) {
            setError("Network fail - Please try again");
        } else {
//            setError(response.toString());
            setError("Please check email and try again");
        }
        hideProgress();

    }

    private void checkEmailAvailableTask() {
        if (TextUtils.isEmpty(user.getEmail())) {
            try {
                onPostCheckEmailAvailableErrorResponse(
                        new JSONObject("{\"err\":\"Email is EMPTY.. Please fill\"}"));
            } catch (JSONException e) {
                e.printStackTrace();
                onPostCheckEmailAvailableErrorResponse(null);
            }
        } else {
            CheckEmailAvailableTask checkEmailAvailableTask = new CheckEmailAvailableTask(
                    user.getEmail(),
                    MainApplicationSingleton.AUTH_ACCOUNT_CHECK_EMAIL_AVAILABLE, getContext());
            checkEmailAvailableTask.setCheckEmailAvailableTaskListener(this);
            checkEmailAvailableTask.setRequestTimeoutMillis(30000);
            checkEmailAvailableTask.execute();
        }
    }

    private void loginTask() {
        if (TextUtils.isEmpty(user.getEmail()) || TextUtils.isEmpty(user.getPwd())) {
            try {
                onPostCheckEmailAvailableErrorResponse(
                        new JSONObject("{\"err\":\"Email/Pwd is EMPTY.. Please fill\"}"));
            } catch (JSONException e) {
                e.printStackTrace();
                onPostCheckEmailAvailableErrorResponse(null);
            }
        } else {
            execLoginTask(user);
        }
    }

    private void execLoginTask(ContactItem user) {
        LoginTask loginTask = new LoginTask(
                user.getEmail(), user.getPwd(), user.getDevice(), user.getDeviceId(), user.getDeviceName(),
                MainApplicationSingleton.AUTH_ACCOUNT_LOGIN, user, getContext());
        loginTask.setLoginTaskListener(this);
        loginTask.setRequestTimeoutMillis(30000);
        loginTask.execute();
    }

    private void execGetProfileTask(ContactItem user) {
        GetProfileTask getProfileTask = new GetProfileTask(
                user.getDataId(), user.getToken(), user.getDevice(), user.getDeviceRef(), user,
                MainApplicationSingleton.AUTH_GET_PROFILE, getContext());
        getProfileTask.setGetProfileTaskListener(this);
        getProfileTask.setRequestTimeoutMillis(30000);
        getProfileTask.execute();
    }

    private void execGetEmailsTask(ContactItem user, int mode) {
        GetEmailsTask getEmailsTask = new GetEmailsTask(user.getDataId(), user.getToken(), user.getDevice(), user.getDeviceRef(),
                MainApplicationSingleton.AUTH_GET_EMAILS, user, getContext(), mode);
        getEmailsTask.setRequestTimeoutMillis(30000);
        getEmailsTask.setGetEmailsTaskListener(this);
        getEmailsTask.execute();
    }

    @Override
    public void onUserSignupDialogPositiveClick(DialogFragment dialog) {
//        101 response code for new user ready to signup
        okActivity(101);
    }

    @Override
    public void onUserSignupDialogNegativeClick(DialogFragment dialog) {
        dialog.dismiss();
    }

    @Override
    public void onPostLoginResponse(JSONObject response, String email, ContactItem user) {
        try {
            int status = response.optInt(MainApplicationSingleton.STATUS_PARAM, -1);
            if (99 == status || -1 == status) {
                onPostLoginErrorResponse(response);
            } else if (1 == status) {
                UserContentProvider.parseLoginResponse(response, user.getEmail(), user);
                ContactService.asyncUpdateContacts(user, getContext());
                execGetProfileTask(user);
/*
                UserContentProvider.activateUserInDB(response, user, getContext());
                hideProgress();
                loginActivity();
*/
            }
        } catch (Throwable e) {
            e.printStackTrace();
            Log.e(TAG, "Exception: " + e.getMessage());
            try {
                onPostLoginErrorResponse(
                        new JSONObject("{\"err\":\"onPostSignupResponse - failed \"" +
                                e.toString() + "}"));
            } catch (JSONException e1) {
                onPostLoginErrorResponse(null);
            }
        }
//        hideProgress();
    }

    @Override
    public void onPostLoginErrorResponse(JSONObject response) {
        setError("Please enter correct password");
        tvPassword.setEnabled(true);
        tvPassword.requestFocus();
        hideProgress();
    }

    @Override
    public void onPostGetProfileResponse(JSONObject response, ContactItem user) {
        try {
            UserContentProvider.parseGetProfileResponse(response, user);
            UserContentProvider.savesUserInDBPlusSP(user, getContext());
            execGetEmailsTask(user, -1);
        } catch (Throwable e) {
            e.printStackTrace();
            Log.e(TAG, "Exception: " + e.getMessage());
            try {
                onPostLoginErrorResponse(
                        new JSONObject("{\"err\":\"onPostSignupResponse - failed \"" +
                                e.toString() + "}"));
            } catch (JSONException e1) {
                onPostLoginErrorResponse(null);
            }
        }
//        hideProgress();

    }

    @Override
    public void onPostGetProfileErrorResponse(JSONObject response) {
        try {
            UserContentProvider.savesUserInDBPlusSP(user, getContext());
        } catch (Throwable e) {
            e.printStackTrace();
        }
        hideProgress();
        loginActivity();
    }

    @Override
    public void onPostGetEmailsResponse(JSONObject response, ContactItem user, int mode) {
        try {
            int status = response.optInt("status");
            if (1 == status) {
                JSONArray responseJSONArray = response.getJSONArray("emails");
                if (responseJSONArray != null && responseJSONArray.length() > 0) {
                    user.getContactItems().addAll(UserContentProvider.setsEmailsFromJSONArray(
                            responseJSONArray, getContext()));
                    if (!user.getContactItems().isEmpty()) {
                        Uri uri = UserEmailContentProvider.savesUserEmailsInDB(
                                user, getContext());
                    }
                }
            }
            execGetInvitesTask(user);
        } catch (Throwable e) {
            e.printStackTrace();
            onPostGetEmailsErrorResponse(response);
            Log.e(TAG, TAG + e.toString());
        }
/*
        hideProgress();
        loginActivity();
*/
    }

    @Override
    public void onPostGetEmailsErrorResponse(JSONObject response) {
        Log.e(TAG, "onPostGetEmailsErrorResponse:" + response);
        hideProgress();
        loginActivity();
    }


    private void updatesUserCompanyInDB(String invitedCompanyId, ContactItem contactItem, ContactItem user) {
        user.setCompanyId(invitedCompanyId);
        if (contactItem != null)
            user.setCompanyName(contactItem.getCompanyName());
        ContactService.asyncUpdateWorkContacts(user, getContext());
        UserContentProvider.updatesCompanyInDB(user, getContext());
    }

    private void execGetInvitesTask(ContactItem user) {
        GetInvitesTask getInvitesTask = new GetInvitesTask(user.getDataId(), user.getToken(),
                user.getDevice(), user.getDeviceRef(), user,
                MainApplicationSingleton.AUTH_COMPANY_GET_INVITES, getContext());
        getInvitesTask.setRequestTimeoutMillis(30000);
        getInvitesTask.setGetInvitesTaskListener(this);
        getInvitesTask.execute();
    }

    @Override
    public void onPostGetInvitesResponse(JSONObject response, ContactItem user) {
        int status = response.optInt("status", -1);
        if (1 == status) {
            JSONArray jsonArray = response.optJSONArray("invites");
            if (jsonArray != null) {
                int length = jsonArray.length();
                if (length > 0) {
                    HashSet<ContactItem> contactItems = new HashSet<>(length);
                    for (int i = 0; i < length; i++) {
                        JSONObject company = jsonArray.optJSONObject(i);
                        String cid = company.optString("company_id");
                        String cname = company.optString("company_name");
                        Long timestamp = company.optLong("timestamp");
                        ContactItem contactItem = new ContactItem();
                        contactItem.setDataId(cid);
                        contactItem.setCompanyId(cid);
                        contactItem.setCompanyName(cname);
                        contactItem.setTimestamp(timestamp);
                        contactItem.setDateTime(MainApplicationSingleton.getDateTimeMillis(timestamp));
                        contactItems.add(contactItem);
                    }
                    Iterator<ContactItem> iterator = contactItems.iterator();
                    ContactItem contactItem = iterator.next();
                    updatesUserCompanyInDB(contactItem.getCompanyId(), contactItem, user);
                }
            }

        } else if (99 == status || -1 == status) {
            onPostGetInvitesErrorResponse(response);
        }
        hideProgress();
        loginActivity();
    }

    @Override
    public void onPostGetInvitesErrorResponse(JSONObject response) {
        Log.e(TAG, "onPostGetInvitesErrorResponse: " + response);
    }


    private void populateAutoComplete() {
        if (mayRequestReadContacts(getSnackView())) {
            new SetupEmailAutoCompleteTask().execute(null, null);
        }

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

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(getContext(),
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        tvEmail.setAdapter(adapter);
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
            ContentResolver cr = getContext().getContentResolver();
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
