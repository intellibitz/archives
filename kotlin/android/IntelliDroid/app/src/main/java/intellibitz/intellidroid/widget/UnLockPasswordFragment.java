package intellibitz.intellidroid.widget;


import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.R;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.util.MainApplicationSingleton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import intellibitz.intellidroid.data.ContactItem;


/**
 *
 */
public class UnLockPasswordFragment extends
        BottomSheetDialogFragment {
    private static final String TAG = "UnLockPwdFrag";

    private ContactItem user;
    private ProgressDialog progressDialog;
    private View view;
    private View snackView;

    private EditText etOtp1;
    private EditText etOtp2;
    private EditText etOtp3;
    private EditText etOtp4;
    private OnUnLockPasswordFragmentListener mListener;

    public UnLockPasswordFragment() {
        super();
    }

    public static UnLockPasswordFragment newInstance(ContactItem user, OnUnLockPasswordFragmentListener listener) {
        UnLockPasswordFragment fragment = new UnLockPasswordFragment();
        fragment.addUnLockPasswordFragmentListener(listener);
        Bundle args = new Bundle();
        args.putParcelable(ContactItem.USER_CONTACT, user);
        fragment.setArguments(args);
        return fragment;
    }

    public void addUnLockPasswordFragmentListener(OnUnLockPasswordFragmentListener listener) {
        mListener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View view = inflater.inflate(R.layout.fragment_unlockpwd, (ViewGroup) getView());

        Bundle arguments = getArguments();
        user = arguments.getParcelable(ContactItem.USER_CONTACT);

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
                resendOtpCall.setEnabled(false);
                resendOtpCallTimer.start();
            }
        });
        View rlLogin = view.findViewById(R.id.rl_login);
        rlLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                    if (etOtp4.getText().length() > 0) {

                    }
                }
                return false;
            }
        });
        etOtp4.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    return true;
                }
                return false;
            }
        });
//        unPackUser();
        etOtp1.requestFocus();

//        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.LaunchTheme);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.unlock_app);
        builder.setView(view)
                // Add action buttons
                .setPositiveButton(R.string.menu_title_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        performOkTask();
                    }
                })
                .setNegativeButton(R.string.menu_title_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (mListener != null)
                            mListener.onDialogNegativeClick(UnLockPasswordFragment.this);
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                Button positiveButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        performOkTask();
                        // TODO - call 'dismiss()' only if you need it
                    }
                });
                Button negativeButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                // same for negative (and/or neutral) button if required
            }
        });

        return alertDialog;
    }

    public View getSnackView() {
        if (null == snackView)
//            snackView = view.findViewById(R.id.username);
            snackView = view.findViewById(R.id.mr_art);
        return snackView;
    }

    public void performOkTask() {
        invalidPwdAlert(null);
        final String pwd = MainApplicationSingleton.getInstance(getContext()).getStringValueSP(
                MainApplicationSingleton.LOCKPWD_PARAM
        );
        if (!TextUtils.isEmpty(pwd)) {
            String t1 = etOtp1.getEditableText().toString();
            if (TextUtils.isEmpty(t1)) showPwdError();
            String t2 = etOtp2.getEditableText().toString();
            if (TextUtils.isEmpty(t2)) showPwdError();
            String t3 = etOtp3.getEditableText().toString();
            if (TextUtils.isEmpty(t3)) showPwdError();
            String t4 = etOtp4.getEditableText().toString();
            if (TextUtils.isEmpty(t4)) showPwdError();

            String s = t1 + t2 + t3 + t4;
            if (TextUtils.isEmpty(s) || s.length() < 4) {
                invalidPwdAlert("Wrong Password - Please try again");
                showPwdError();
                return;
            }

            if (!s.equals(pwd)) {
                invalidPwdAlert("Wrong Password - Please try again");
                showPwdError();
                return;
            }
        }
        if (mListener != null) {
            mListener.onDialogPositiveClick(UnLockPasswordFragment.this);
            this.dismiss();
        }
    }

    public void invalidPwdAlert(String val) {
        etOtp1.setError(val);
    }

    public void showPwdError() {
        etOtp1.setText("");
        etOtp2.setText("");
        etOtp3.setText("");
        etOtp4.setText("");
        etOtp1.requestFocus();
        return;
    }

    public void setError(String text) {
        //        clears the error message and the icon
        if (etOtp1 != null)
            etOtp1.setError(text);
    }

    public interface OnUnLockPasswordFragmentListener {
        void onDialogPositiveClick(DialogFragment dialog);

        void onDialogNegativeClick(DialogFragment dialog);
    }

}
