package intellibitz.intellidroid.account;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import com.google.android.material.snackbar.Snackbar;
import intellibitz.intellidroid.IntellibitzPermissionFragment;
import intellibitz.intellidroid.IntellibitzUserFragment;
import intellibitz.intellidroid.content.UserContentProvider;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.listener.ProfileTopicListener;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.IntellibitzPermissionFragment;
import intellibitz.intellidroid.IntellibitzUserFragment;
import intellibitz.intellidroid.R;
import intellibitz.intellidroid.content.UserContentProvider;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.listener.ProfileTopicListener;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.IntellibitzPermissionFragment;
import intellibitz.intellidroid.IntellibitzUserFragment;
import intellibitz.intellidroid.content.UserContentProvider;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.listener.ProfileTopicListener;
import org.json.JSONException;
import org.json.JSONObject;


/**
 *
 */
public class ProfileSignupFragment extends
        IntellibitzUserFragment implements
        SignupTask.SignupTaskListener {

    private static final String TAG = "ProfileInfoFragment";
    private final static int REQUEST_CAMERA_AND_STORAGE_PERMISSION = 1;
    private Button btnContinue;
    private ProfileTopicListener profileTopicListener;
    private EditText etFirstname;
    private EditText etLastname;
    private View view;
    private View snackView;
    private Snackbar snackbar;
    private Button btnPermission;
    private View viewPermission;
    private ProgressDialog progressDialog;

    public ProfileSignupFragment() {
        super();
    }

    public static ProfileSignupFragment newInstance(ContactItem user) {
        ProfileSignupFragment fragment = new ProfileSignupFragment();
        fragment.setUser(user);
        Bundle args = new Bundle();
        args.putParcelable(ContactItem.USER_CONTACT, user);
        fragment.setArguments(args);
        return fragment;
    }

    public void setProfileTopicListener(ProfileTopicListener profileTopicListener) {
        this.profileTopicListener = profileTopicListener;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ProfileTopicListener)
            profileTopicListener = (ProfileTopicListener) context;
    }

    @Override
    protected void onContactsPermissionsGranted() {
        super.onContactsPermissionsGranted();
        if (null == user.getProfilePic()) {
        }
    }

    @Override
    public void onResume() {
        super.onResume();
//        unPackUser(getArguments());
//        if (alertReadContacts() && alertReadStorage() && alertWriteStorage()) ;
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
    protected void onReadPhoneStatePermissionsGranted() {
    }

    @Override
    protected void onPhoneReadStatePermissionsDenied() {
        snackbar = makeReadPhoneStateSnack(snackbar, getSnackView(), getActivity());
    }

    public boolean alertReadContacts() {
        if (null == snackbar) {
            Activity activity = getActivity();
            if (null == activity) return false;
            snackbar = makeReadContactsSnack(getSnackView(), activity);
        }
        if (isReadContactsPermissionGranted(getActivity())) {
            if (snackbar != null)
                snackbar.dismiss();
            viewPermission.setVisibility(View.GONE);
            return true;
        } else {
            viewPermission.setVisibility(View.VISIBLE);
            return mayRequestReadContacts(snackbar, getSnackView());
        }
    }

    public boolean alertReadStorage() {
        if (null == snackbar) {
            Activity activity = getActivity();
            if (null == activity) return false;
            snackbar = makeReadExternalStorageSnack(getSnackView(), activity);
        }
        if (isReadExternalStoragePermissionGranted(getActivity())) {
            if (snackbar != null)
                snackbar.dismiss();
            viewPermission.setVisibility(View.GONE);
            return true;
        } else {
            viewPermission.setVisibility(View.VISIBLE);
            return mayRequestReadExternalStorage(snackbar, getSnackView());
        }
    }

    public boolean alertWriteStorage() {
        if (null == snackbar) {
            Activity activity = getActivity();
            if (null == activity) return false;
            snackbar = makeWriteExternalStorageSnack(getSnackView(), activity);
        }
        if (isWriteExternalStoragePermissionGranted(getActivity())) {
            if (snackbar != null)
                snackbar.dismiss();
            viewPermission.setVisibility(View.GONE);
            return true;
        } else {
            viewPermission.setVisibility(View.VISIBLE);
            return mayRequestWriteExternalStorage(snackbar, getSnackView());
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_profilesignup, container, false);
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
//        View view2 = view.findViewById(R.id.cl);
//        mayRequestReadContacts(view2);
        etFirstname = (EditText) view.findViewById(R.id.et_firstname);
        etLastname = (EditText) view.findViewById(R.id.et_lastname);

        etLastname.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // Perform action on key press
                    performSignup();
                    return true;
                }
                return false;
            }
        });


        View.OnClickListener loginListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performSignup();
            }
        };
        View rlLogin = view.findViewById(R.id.rl_login);
        View ibtnLogin = view.findViewById(R.id.ibtn_login);

        rlLogin.setOnClickListener(loginListener);
        ibtnLogin.setOnClickListener(loginListener);
        btnContinue = (Button) view.findViewById(R.id.btn_login);
        btnContinue.setOnClickListener(loginListener);

        etFirstname.requestFocus();
        unPackUser();
        if (!IntellibitzPermissionFragment.isReadPhoneStatePermissionGranted(getContext())) {
            mayRequestReadPhoneState(snackView);
        }
    }

    private void performSignup() {
        String firstname = etFirstname.getText().toString();
        if (TextUtils.isEmpty(firstname)) {
            etFirstname.setError("First Name is required for Intellibitz");
        }
        String lastname = etLastname.getText().toString();
        if (TextUtils.isEmpty(lastname)) {
            etLastname.setError("Last Name is required for Intellibitz");
        }
        etFirstname.setError(null);
        etLastname.setError(null);
        user.setName(firstname);
        user.setFirstName(firstname);
        user.setLastName(firstname);
        user.setDisplayName(firstname + " " + lastname);
        showProgress();
        UserContentProvider.setsDeviceIdNameInfo(user, getContext());
        execSignUpTask();
    }

    private void execSignUpTask() {
        SignupTask signupTask = new SignupTask(user.getEmail(), user.getOtp(), user.getName(), user.getPwd(),
                user.getDevice(), user.getDeviceId(), user.getDeviceName(), user.getStatus(),
                MainApplicationSingleton.AUTH_ACCOUNT_SIGNUP, user, getContext());
        signupTask.setRequestTimeoutMillis(30000);
        signupTask.setSignupTaskListener(this);
        signupTask.execute();
    }

    @Override
    public void onPostSignupResponse(JSONObject response, String email, ContactItem user) {
        try {
            int status = response.optInt(MainApplicationSingleton.STATUS_PARAM, -1);
            if (99 == status || -1 == status) {
                onPostSignupErrorResponse(response);
            } else if (1 == status) {
                UserContentProvider.activateUserSignupInDB(response, user, getContext());
                hideProgress();
                okActivity();
            }
        } catch (Throwable e) {
            e.printStackTrace();
            Log.e(TAG, "Exception: " + e.getMessage());
            try {
                onPostSignupErrorResponse(
                        new JSONObject("{\"err\":\"onPostSignupResponse - failed \"" +
                                e.toString() + "}"));
            } catch (JSONException e1) {
                onPostSignupErrorResponse(null);
            }
        }
        hideProgress();
    }

    @Override
    public void onPostSignupErrorResponse(JSONObject response) {
        hideProgress();
    }

    protected void okActivity() {
        Activity activity = getActivity();
        Intent intent = activity.getIntent();
        intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
        activity.setResult(Activity.RESULT_OK, intent);
        activity.finish();
    }

    public View getSnackView() {
        if (null == snackView)
//            snackView = getActivity().findViewById(R.id.cl);
//            snackView = view.findViewById(R.id.ll);
            snackView = view.findViewById(R.id.username);
        return snackView;
    }

    private void unPackUser() {
        String firstName = user.getFirstName();
        if (!TextUtils.isEmpty(firstName))
            etFirstname.setText(firstName);
        String lastName = user.getLastName();
        if (!TextUtils.isEmpty(lastName))
            etLastname.setText(lastName);
    }

    private void packUser(ContactItem u) {
        user.setName(u.getName());
        user.setDevice(u.getDevice());
        user.setMobile(u.getMobile());
        user.setCountryName(u.getCountryName());
        user.setCountryCode(u.getCountryCode());
        user.setProfilePic(u.getProfilePic());
    }

    public ContactItem packUser() {
        String firstname = etFirstname.getText().toString();
        String lastname = etLastname.getText().toString();
        if (!TextUtils.isEmpty(firstname))
            user.setFirstName(firstname);
        if (!TextUtils.isEmpty(lastname))
            user.setLastName(lastname);
        return user;
    }

    public void setError(String text) {
        //        clears the error message and the icon
        if (etFirstname != null)
            etFirstname.setError(text);
    }

    public void showProgress() {
        progressDialog = ProgressDialog.show(getActivity(), "Profile", "Saving Profile info", true);
    }

    public void hideProgress() {
        if (progressDialog != null) {
            FragmentActivity activity = getActivity();
            if (activity != null && !activity.isFinishing()) {
                progressDialog.dismiss();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_AND_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            }
        }
    }

    @Override
    protected void onReadExternalStoragePermissionsGranted() {
        super.onReadExternalStoragePermissionsGranted();
    }

    @Override
    protected void onWriteExternalStoragePermissionsGranted() {
        super.onWriteExternalStoragePermissionsGranted();
    }

    @Override
    protected void onCameraPermissionsGranted() {
        super.onCameraPermissionsGranted();
    }

    @Override
    protected void onReadExternalStoragePermissionsDenied() {
        super.onReadExternalStoragePermissionsDenied();
    }

    @Override
    protected void onWriteExternalStoragePermissionsDenied() {
        super.onWriteExternalStoragePermissionsDenied();
    }

    @Override
    protected void onContactsPermissionsDenied() {
        super.onContactsPermissionsDenied();
    }

    @Override
    protected void onCameraPermissionsDenied() {
        super.onCameraPermissionsDenied();
    }

}
