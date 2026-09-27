package intellibitz.intellidroid.account;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
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
import com.google.android.material.snackbar.Snackbar;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import intellibitz.intellidroid.IntellibitzUserFragment;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.IntellibitzUserFragment;
import intellibitz.intellidroid.R;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.IntellibitzUserFragment;
import intellibitz.intellidroid.data.ContactItem;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 */
public class VerifyCodeFragment extends
        IntellibitzUserFragment implements
        GetEmailVerificationTask.GetEmailVerificationTaskListener,
        VerifyCodeTask.VerifyCodeTaskListener {
    private static final String TAG = "VerifyCodeFragment";
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
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

    public VerifyCodeFragment() {
        super();
    }

    public static VerifyCodeFragment newInstance(ContactItem user) {
        VerifyCodeFragment fragment = new VerifyCodeFragment();
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
//        alertReadPhoneState();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_verifycode, container, false);
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
        View.OnClickListener resendCodeListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showProgress();
                execGetEmailVerification();
                resendOtp.setEnabled(false);
                resendOtpTimer.start();
            }
        };
        resendOtp.setOnClickListener(resendCodeListener);

        View rlLogin = view.findViewById(R.id.rl_login);
        rlLogin.setOnClickListener(resendCodeListener);

        View llContinue = view.findViewById(R.id.ll_continue);
        View btnContinue = view.findViewById(R.id.btn_continue);
        View.OnClickListener continueListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performActivateTask();
            }
        };
        llContinue.setOnClickListener(continueListener);
        btnContinue.setOnClickListener(continueListener);

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

    public void showProgress() {
        showProgress(getContext(), getString(R.string.verify_code),
                getString(R.string.note_verifycode), true);
//        progressDialog = ProgressDialog.show(getActivity(), "Login", "User Activation", true);
    }

    public void showVerifyProgress() {
        showProgress(getContext(), getString(R.string.verify_code),
                getString(R.string.please_wait), true);
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

            showVerifyProgress();
            user.setOtp(otp);
            execVerifyCode();
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

    private void execVerifyCode() {
        VerifyCodeTask verifyCodeTask = new VerifyCodeTask(user.getEmail(), user.getOtp(),
                MainApplicationSingleton.AUTH_ACCOUNT_VERIFY_CODE, getContext());
        verifyCodeTask.setRequestTimeoutMillis(30000);
        verifyCodeTask.setVerifyCodeTaskListener(this);
        verifyCodeTask.execute();
    }

    @Override
    public void onPostVerifyCodeResponse(JSONObject response, String email, ContactItem user) {
        hideProgress();
        if (null == response) {
            invalidOTPAlert("Network failed - Please try again");
            return;
        }
        try {
            int status = response.getInt("status");
            if (0 == status) {
                okActivity();
            } else if (1 == status) {
                okActivity();
            } else if (2 == status) {
//                    2 is name required
                onPostVerifyCodeErrorResponse(response);
//                static otp build
//                activateSuccess(response);
            } else if (99 == status) {
                onPostVerifyCodeErrorResponse(response);
//                static otp build
//                activateSuccess(response);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }

    }

    @Override
    public void onPostVerifyCodeErrorResponse(JSONObject response) {
        hideProgress();
        if (response != null) {
            Log.e(TAG, response.toString());
//            invalidOTPAlert(response.toString());
            invalidOTPAlert("Activation failed - Please try again");
        }
        return;

    }

    protected void okActivity() {
        Activity activity = getActivity();
        Intent intent = activity.getIntent();
        intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
        activity.setResult(Activity.RESULT_OK, intent);
        activity.finish();
    }

    protected void cancelActivity() {
        Activity activity = getActivity();
        Intent intent = activity.getIntent();
        intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
        activity.setResult(Activity.RESULT_CANCELED, intent);
        activity.finish();
    }

    @Override
    public void onPostGetEmailVerificationResponse(JSONObject response, String email, ContactItem user) {
        try {
            int status = response.getInt("status");
            if (1 == status) {
//                startOTPActivity();
            } else if (2 == status) {
//                startOTPActivity();
            } else if (99 == status) {
                onPostGetEmailVerificationErrorResponse(response);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            onPostGetEmailVerificationErrorResponse(response);
        }
        hideProgress();
    }

    @Override
    public void onPostGetEmailVerificationErrorResponse(JSONObject response) {
//                    // TODO: 02-03-2016
//                    handles error
        Log.e(TAG, "onPostGetEmailVerificationErrorResponse: " + response);
        if (response != null)
            setError(response.toString());
        hideProgress();
    }

    private void execGetEmailVerification() {
        GetEmailVerificationTask getEmailVerificationTask = new GetEmailVerificationTask(user.getEmail(),
                MainApplicationSingleton.AUTH_ACCOUNT_GET_EMAIL_VERIFICATION, getContext());
        getEmailVerificationTask.setRequestTimeoutMillis(30000);
        getEmailVerificationTask.setGetEmailVerificationTaskListener(this);
        getEmailVerificationTask.execute();
    }

    public void setError(String text) {
        //        clears the error message and the icon
        if (etOtp4 != null)
            etOtp4.setError(text);
    }

    public interface MobileActivateListener {
        void onActivateSuccess(ContactItem user);
    }

}
