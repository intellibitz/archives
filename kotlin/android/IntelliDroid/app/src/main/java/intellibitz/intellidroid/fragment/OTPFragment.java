package intellibitz.intellidroid.fragment;


import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Parcelable;
import android.provider.Settings;
import android.telephony.TelephonyManager;
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

import com.google.android.material.snackbar.Snackbar;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import intellibitz.intellidroid.IntellibitzPermissionFragment;
import intellibitz.intellidroid.IntellibitzUserFragment;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.task.MobileActivateTask;
import intellibitz.intellidroid.task.MobileGetCodeTask;
import intellibitz.intellidroid.task.OTPCallTask;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.IntellibitzPermissionFragment;
import intellibitz.intellidroid.IntellibitzUserFragment;
import intellibitz.intellidroid.R;
import intellibitz.intellidroid.activity.ProfileInfoActivity;
import intellibitz.intellidroid.content.UserContentProvider;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.service.ContactService;
import intellibitz.intellidroid.service.UserEmailIntentService;
import intellibitz.intellidroid.task.MobileActivateTask;
import intellibitz.intellidroid.task.MobileGetCodeTask;
import intellibitz.intellidroid.task.OTPCallTask;
import intellibitz.intellidroid.util.MainApplicationSingleton;

import intellibitz.intellidroid.content.UserContentProvider;
import intellibitz.intellidroid.service.ContactService;
import intellibitz.intellidroid.service.UserEmailIntentService;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class OTPFragment extends
        IntellibitzUserFragment implements
        MobileGetCodeTask.MobileGetCodeTaskListener,
        MobileActivateTask.MobileActivateTaskListener,
        OTPCallTask.OTPCallTaskListener {
    private static final String TAG = "OTPFragment";
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private MobileGetCodeTask mobileGetCodeTask = null;
    private MobileActivateTask mobileActivateTask = null;
    private OTPCallTask otpCallTask;
    private View view;
    private View snackView;
    private Snackbar snackbar;
    private Button btnPermission;
    private View viewPermission;

    private MobileActivateListener mobileActivateListener;
    private EditText etOtp1;
    private EditText etOtp2;
    private EditText etOtp3;
    private EditText etOtp4;

    public OTPFragment() {
        super();
    }

    public static OTPFragment newInstance(ContactItem user) {
        OTPFragment fragment = new OTPFragment();
        fragment.setUser(user);
        Bundle args = new Bundle();
        args.putParcelable(ContactItem.USER_CONTACT, user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof MobileActivateListener)
            mobileActivateListener = (MobileActivateListener) context;
    }

    @Override
    public void onResume() {
        super.onResume();
//        unPackUser(getArguments());
        alertReadPhoneState();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_otp, container, false);
        return view;
    }

/*
    @Override
    public void onPause() {
        super.onPause();
        packUser();
    }
*/

/*
    @Override
    public void onStart() {
        super.onStart();
        getMobileText().setEnabled(false);
//        sets the mobile number, from the previous fragment
//        // TODO: 24-02-2016
//        do this the right way?
        unPackUser(getArguments());
        if (1 == user.getAccountExists()) {
*/
/*
            nameText.setVisibility(View.GONE);
            nameText.setFocusable(false);
            nameText.setEnabled(false);
*//*

        } else {
//            nameText.requestFocus();
            nameText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
*/
/*
                    if (keyEvent != null &&
                            (id == R.id.username || id == EditorInfo.IME_NULL) &&
                            KeyEvent.KEYCODE_ENTER == keyEvent.getKeyCode()) {
                        mobileActivateListener.onActivateKeyEvent();
                        return true;
                    }
*//*

                    return false;
                }
            });
        }
        otpText.requestFocus();
*/
/*
        if (!isReadPhoneStatePermissionGranted(getContext())) {
            showReadPhoneStateSnack(snackView, getActivity());
        }
*//*

        alertReadPhoneState();
    }
*/

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
                requestReadPhoneStatePermissions(getActivity());
            }
        });
        viewPermission = view.findViewById(R.id.ll_perm);
        getSnackView();
/*
        otpText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    performActivateTask();
                    return true;
                }
                return false;
            }
        });
*/

        final Button resendOtp = (Button) view.findViewById(R.id.btn_resendOTP);
        resendOtp.setEnabled(false);
        final String resend = resendOtp.getText().toString();
        final CountDownTimer resendOtpTimer = new CountDownTimer(15000, 1000) {

            public void onTick(long millisUntilFinished) {
                resendOtp.setText(resend + " " + millisUntilFinished / 1000);
            }

            public void onFinish() {
                resendOtp.setText(resend);
                resendOtp.setEnabled(true);
            }
        }.start();
        resendOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCallProgress();
                execGetOTP();
                resendOtp.setEnabled(false);
                resendOtpTimer.start();
            }
        });

        final Button resendOtpCall = (Button) view.findViewById(R.id.btn_resendOTPCall);
        resendOtpCall.setEnabled(false);
        final String txt = resendOtpCall.getText().toString();
        final CountDownTimer resendOtpCallTimer = new CountDownTimer(15000, 1000) {

            public void onTick(long millisUntilFinished) {
                resendOtpCall.setText(txt + " " + millisUntilFinished / 1000);
            }

            public void onFinish() {
                resendOtpCall.setText(txt);
                resendOtpCall.setEnabled(true);
            }
        }.start();
        resendOtpCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgress();
                execOTPCallTask();
                resendOtpCall.setEnabled(false);
                resendOtpCallTimer.start();
            }
        });
        View rlLogin = view.findViewById(R.id.rl_login);
        rlLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showProgress();
                execOTPCallTask();
                resendOtpCall.setEnabled(false);
                resendOtpCallTimer.start();
            }
        });
        etOtp1 = (EditText) view.findViewById(R.id.et_otp_1);
        etOtp2 = (EditText) view.findViewById(R.id.et_otp_2);
        etOtp3 = (EditText) view.findViewById(R.id.et_otp_3);
        etOtp4 = (EditText) view.findViewById(R.id.et_otp_4);
        etOtp1.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (KeyEvent.ACTION_UP == keyEvent.getAction()) {
                    if (etOtp1.getText().length() > 0)
                        etOtp2.requestFocus();
                }
                return false;
            }
        });
        etOtp2.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (KeyEvent.ACTION_UP == keyEvent.getAction()) {
                    if (etOtp2.getText().length() > 0)
                        etOtp3.requestFocus();
                }
                return false;
            }
        });
        etOtp3.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (KeyEvent.ACTION_UP == keyEvent.getAction()) {
                    if (etOtp3.getText().length() > 0)
                        etOtp4.requestFocus();
                }
                return false;
            }
        });
        etOtp4.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (KeyEvent.ACTION_UP == keyEvent.getAction()) {
                    if (etOtp4.getText().length() > 0)
                        performActivateTask();
                }
                return false;
            }
        });
        etOtp4.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    performActivateTask();
                    return true;
                }
                return false;
            }
        });
        unPackUser();
        etOtp1.requestFocus();
    }

    private void unPackUser(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            ContactItem u = savedInstanceState.getParcelable(ContactItem.USER_CONTACT);
            if (u != null) {
                packUser(u);
                unPackUser();
            }
        }
    }

    private void unPackUser() {
        final String otp = user.getOtp();
        if (!TextUtils.isEmpty(otp) && 4 == otp.length()) {
            etOtp1.setText(otp.substring(0, 1));
            etOtp2.setText(otp.substring(1, 2));
            etOtp3.setText(otp.substring(2, 3));
            etOtp4.setText(otp.substring(3, 4));
        }
//        nameText.setText(user.getName());
/*
        final String mobile = user.getMobile();
        if (!TextUtils.isEmpty(mobile))
        mobileText.setText(mobile);
*/
//        countryButton.setText(user.getCountryName());
//        otpText.setText(user.getOtp());
    }

    public ContactItem packUser(ContactItem u) {
        user.setDevice(u.getDevice());
        user.setName(u.getName());
        user.setMobile(u.getMobile());
        user.setOtp(u.getOtp());
        user.setCountry(u.getCountry());
        user.setCountryName(u.getCountryName());
        user.setAccountExists(u.getAccountExists());
        user.setProfilePic(u.getProfilePic());
        return user;
    }

    public View getSnackView() {
        if (null == snackView)
            snackView = view.findViewById(R.id.username);
        return snackView;
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

    public void invalidOTPAlert(String val) {
        etOtp4.setError(val);
    }

    public void showCallProgress() {
        showProgress(getContext(), getString(R.string.login),
                getString(R.string.request_otp_by_call), true);
    }

    public void showProgress() {
        showProgress(getContext(), getString(R.string.login),
                getString(R.string.user_activation), true);
//        progressDialog = ProgressDialog.show(getActivity(), "Login", "User Activation", true);
    }

    public void performActivateTask() {
        if (MainApplicationSingleton.CheckNetworkConnection.isNetworkConnectionAvailable(getContext())) {
            //                // TODO: 21-02-2016
//                implement isActivityShowingActivateFragment logic
//        clears any previous errors, if any
            invalidOTPAlert(null);
//        save country state, for restoring if activity restart
//            packUser();
            String t1 = etOtp1.getEditableText().toString();
            if (TextUtils.isEmpty(t1)) showOtpError();
            String t2 = etOtp2.getEditableText().toString();
            if (TextUtils.isEmpty(t2)) showOtpError();
            String t3 = etOtp3.getEditableText().toString();
            if (TextUtils.isEmpty(t3)) showOtpError();
            String t4 = etOtp4.getEditableText().toString();
            if (TextUtils.isEmpty(t4)) showOtpError();

            String otp = t1 + t2 + t3 + t4;
            if (TextUtils.isEmpty(otp) || otp.length() < 4) {
                showOtpError();
                return;
            }

            showProgress();
            initUserDeviceId();
            user.setOtp(otp);
            execMobileActivateTask();
        } else {
        }
    }

    public void showOtpError() {
        etOtp1.requestFocus();
        return;
    }

    private String normalizeUserMobile() {
        //            keeps only the digits in a phone number
        user.setMobile(PhoneNumberUtil.normalizeDigitsOnly(user.getMobile()));
        return user.getMobile();
    }

    @NonNull
    private String getUserMobileWithCC() {
        normalizeUserMobile();
        return user.getCountry().getDialCode() + user.getMobile();
    }

    private void savesUserInDB(ContactItem user) {
        Log.d(TAG, "User: " + user);
//                    saves user to local db
        try {
            Uri uri = UserContentProvider.savesUserInDB(user, getContext());
            long id = ContentUris.parseId(uri);
            MainApplicationSingleton.getInstance(getContext()).putLongValueSP(
                    MainApplicationSingleton.ID_PARAM, id);
//                set the db id
            user.set_id(id);
            Log.e(TAG, "SUCCESS - User insert: " + uri);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }
        savesUserInSP(user);


//                // TODO: 05-03-2016
//                remove this hack.. do in async, or a service
/*
            HandlerThread mHandlerThread = new HandlerThread("HandlerThread");
            mHandlerThread.start();
            Handler mHandler = new Handler(mHandlerThread.getLooper());
            mHandler.post(new Runnable() {
                @Override
                public void run() {
*/
/*
            if (null == user.getName() || user.getName().isEmpty()) {
//                    fetch user from cloud, if not available local

                HashMap<String, String> data = new HashMap<>();
                MainApplicationSingleton mainApplication =
                        MainApplicationSingleton.getInstance();
                data.put(MainApplicationSingleton.DEVICE_PARAM, user.getDevice());
                data.put(MainApplicationSingleton.UID_PARAM, user.getDataId());
                data.put(MainApplicationSingleton.TOKEN_PARAM, user.getToken());
                // params comes from the execute() call: params[0] is the url.
                JSONObject response = HttpUrlConnectionParser.postHTTP(
                        MainApplicationSingleton.AUTH_GET_PROFILE, data);
                if (response != null) {
                    try {
//                                    // TODO: 07-03-2016
//                                    handle error
//                                String err = response.getString("err");
//                                String mob = response.getString("mobile");
                        user.setName(response.getString("name"));
                        user.setStatus(response.getString("status"));
                        user.setProfilePic(response.getString("profile_pic"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                mainApplication.putStringValueSP(MainApplicationSingleton.NAME_PARAM,
                        user.getName());
                mainApplication.putStringValueSP(MainApplicationSingleton.STATUS_PARAM,
                        user.getStatus());
                mainApplication.putStringValueSP(MainApplicationSingleton.PROFILE_PIC_PARAM,
                        user.getProfilePic());
*/

//                        // TODO: 07-03-2016
//                        The user restore from cloud, to be handled clean
//                        sync previous email accounts from cloud
//                        EmailSyncHandler.SyncEmailsFromCloud();

//                    }
    }

    private void initUserDeviceId() {
        user.setDeviceId(Build.SERIAL);
        final String androidId = Settings.Secure.getString(
                getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        if (null == user.getDeviceId()) {
            if (IntellibitzPermissionFragment.isReadPhoneStatePermissionGranted(getContext())) {
                final String deviceId = ((TelephonyManager) getContext().getSystemService(
                        Context.TELEPHONY_SERVICE)).getDeviceId();
                user.setDeviceId(deviceId);
            }
            if (null == user.getDeviceId()) {
                user.setDeviceId(androidId);
            }
        }
        user.setDeviceName(Build.MODEL);
        if (null == user.getDeviceName()) {
            user.setDeviceName(Build.MANUFACTURER + Build.PRODUCT);
            if (null == user.getDeviceName()) {
                user.setDeviceName(Build.BRAND + Build.ID);
            }
        }
        if (null == user.getDeviceId() || null == user.getDeviceName()) {
            Log.e(TAG, "User device id or device name is NULL" +
                    user.getDeviceId() + " " + user.getDeviceName());
        }
    }

    private void savesUserInSP(ContactItem user) {
        //            stores user in preferences
//            // TODO: 18-03-2016
//            to store all info in db, except for the active user id
        final MainApplicationSingleton mainApplication =
                MainApplicationSingleton.getInstance(getContext());
        mainApplication.setUidCurrentUser(user.getDataId());

//            set the user id, multi profiles can be supported.. do this first
        mainApplication.putStringValueSP(
                MainApplicationSingleton.UID_USER_LOGGED_IN_PARAM,
                user.getDataId(), false);
        mainApplication.putStringValueSP(MainApplicationSingleton.UID_PARAM,
                user.getDataId());
        mainApplication.putStringValueSP(MainApplicationSingleton.TOKEN_PARAM,
                user.getToken());
        mainApplication.putStringValueSP(MainApplicationSingleton.NAME_PARAM,
                user.getName());
        mainApplication.putStringValueSP(MainApplicationSingleton.COUNTRY_PARAM,
                user.getCountry().toString());
        mainApplication.putStringValueSP(MainApplicationSingleton.COUNTRY_CODE_PARAM,
                user.getCountry().getDialCode());
        mainApplication.putStringValueSP(MainApplicationSingleton.MOBILE_PARAM,
                user.getMobile());
        mainApplication.putStringValueSP(MainApplicationSingleton.DEVICE_PARAM,
                user.getDevice());
        mainApplication.putStringValueSP(MainApplicationSingleton.OTP_PARAM,
                user.getOtp());
        mainApplication.putStringValueSP(MainApplicationSingleton.DEVICE_NAME_PARAM,
                user.getDeviceName());
        mainApplication.putStringValueSP(MainApplicationSingleton.DEVICE_ID_PARAM,
                user.getDeviceId());
        mainApplication.putStringValueSP(MainApplicationSingleton.DEVICE_REF_PARAM,
                user.getDeviceRef());
    }

    private void execOTPCallTask() {
        otpCallTask = new OTPCallTask(user.getDevice(), getUserMobileWithCC(),
                MainApplicationSingleton.AUTH_OTP_CALL);
        otpCallTask.setOtpCallTaskListener(this);
        otpCallTask.execute();
    }

    private void execMobileActivateTask() {
        mobileActivateTask = new MobileActivateTask(
                getUserMobileWithCC(), user.getOtp(), user.getName(),
                user.getDeviceId(), user.getDevice(), user.getDeviceName(),
                MainApplicationSingleton.AUTH_MOBILE_ACTIVATE);
        mobileActivateTask.setMobileActivateTaskListener(this);
        mobileActivateTask.execute();
    }

    @Override
    public void onPostMobileActivateExecute(JSONObject response) {
        hideProgress();
        if (null == response) {
            invalidOTPAlert("Network failed - Please try again");
            return;
        }
        try {
            int status = response.getInt("status");
            if (1 == status) {
                activateSuccess(response);
            } else if (2 == status) {
//                    2 is name required
                onPostMobileActivateExecuteFail(response);
//                static otp build
//                activateSuccess(response);
            } else if (99 == status) {
                onPostMobileActivateExecuteFail(response);
//                static otp build
//                activateSuccess(response);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }
    }

    private void activateSuccess(JSONObject response) throws JSONException {
        String uid = response.getString(MainApplicationSingleton.UID_PARAM);
        String token = response.getString(MainApplicationSingleton.TOKEN_PARAM);
        String deviceRef = response.getString(MainApplicationSingleton.DEVICE_REF_PARAM);
        user.setDataId(uid);
        user.setToken(token);
        user.setDeviceRef(deviceRef);
//                    syncs contacts from the device to the cloud.. right away
        ContactService.asyncUpdateContacts(user, getContext());
//                    stores user in db..
        savesUserInDB(user);
//                    user already exists, if email exists in cloud.. get them
        if (1 == user.getAccountExists()) {
//                    fetch emails async from cloud
            UserEmailIntentService.asyncEmailsFromCloudAndSavesInDb(user, getContext());
        }
        if (mobileActivateListener != null)
            mobileActivateListener.onActivateSuccess(user);
//                startProfileInfoActivity();
        Activity activity = getActivity();
        Intent intent = activity.getIntent();
        intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
        activity.setResult(Activity.RESULT_OK, intent);
        activity.finish();
    }

    @Override
    public void onPostMobileActivateExecuteFail(JSONObject response) {
        hideProgress();
        if (response != null) {
            Log.e(TAG, response.toString());
//            invalidOTPAlert(response.toString());
            invalidOTPAlert("Activation failed - Please try again");
        }
        return;
    }

    @Override
    public void setMobileActivateTaskToNull() {
        mobileActivateTask = null;
    }

    @Override
    public void onPostOTPCallExecute(JSONObject response) {
        int status = 0;
        if (response != null)
            status = response.optInt("status");
        if (null == response || 99 == status || -1 == status) {
            onPostOTPCallExecuteFail(response);
        }
        hideProgress();
    }

    @Override
    public void onPostOTPCallExecuteFail(JSONObject response) {
        hideProgress();
//            // TODO: 08-06-2016
//            ERROR
        Log.e(TAG, "ERROR: " + response);
    }

    @Override
    public void setOTPCallTaskToNull() {
        otpCallTask = null;
    }

    @Override
    public void onPostMobileGetCodeExecute(JSONObject response) {
        try {
            int status = response.getInt("status");
            if (1 == status) {
//                startOTPActivity();
            } else if (2 == status) {
//                startOTPActivity();
            } else if (99 == status) {
                onPostMobileGetCodeExecuteFail(response);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            onPostMobileGetCodeExecuteFail(response);
        }
        hideProgress();
    }

    @Override
    public void onPostMobileGetCodeExecuteFail(JSONObject response) {
//                    // TODO: 02-03-2016
//                    handles error
        Log.e(TAG, "onPostMobileGetCodeExecuteFail: " + response);
        if (response != null)
            setError(response.toString());
        hideProgress();
    }

    @Override
    public void setMobileGetCodeTaskToNull() {
        mobileGetCodeTask = null;
    }

    private void execGetOTP() {
        mobileGetCodeTask = new MobileGetCodeTask(user.getDevice(), getUserMobileWithCC(),
                MainApplicationSingleton.MOBILE_GETCODE_URL);
        mobileGetCodeTask.setMobileGetCodeTaskListener(this);
        mobileGetCodeTask.execute();
    }

    public void setError(String text) {
        //        clears the error message and the icon
        if (etOtp4 != null)
            etOtp4.setError(text);
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
    private void startProfileInfoActivity() {
        Intent intent = new Intent(getActivity(), ProfileInfoActivity.class);
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
     *
     * @param requestCode the request code with which the activity started
     * @param resultCode  the result code send back by the activity
     * @param data        the intent data with extras
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
        if (MainApplicationSingleton.ACTIVITY_PROFILEINFO_RQ_CODE == requestCode) {
            if (Activity.RESULT_OK == resultCode) {
                if (data != null) {
//                    contacts selected for chat
                    ContactItem item = data.getParcelableExtra(ContactItem.USER_CONTACT);
                    Activity activity = getActivity();
                    Intent intent = activity.getIntent();
                    intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) item);
                    activity.setResult(Activity.RESULT_OK, intent);
                    activity.finish();
                }
            } else if (Activity.RESULT_CANCELED == resultCode) {
                Activity activity = getActivity();
                Intent intent = activity.getIntent();
                intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
                activity.setResult(Activity.RESULT_CANCELED, intent);
                activity.finish();
                Log.e(TAG, "onActivityResult: 0 Contacts selected - ");
            }
        }
    }

    public interface MobileActivateListener {
        void onActivateSuccess(ContactItem user);
    }

}
