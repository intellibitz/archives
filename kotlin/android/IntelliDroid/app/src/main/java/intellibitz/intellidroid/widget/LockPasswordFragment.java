package intellibitz.intellidroid.widget;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import intellibitz.intellidroid.MainActivity;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.MainActivity;
import intellibitz.intellidroid.R;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.util.MainApplicationSingleton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import intellibitz.intellidroid.MainActivity;
import intellibitz.intellidroid.data.ContactItem;

/**
 *
 */
public class LockPasswordFragment extends
        BottomSheetDialogFragment {
    private static final String TAG = "LockPwdFrag";

    private ContactItem user;
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private View view;
    private View snackView;

    private EditText etOtp1;
    private EditText etOtp2;
    private EditText etOtp3;
    private EditText etOtp4;
    private OnLockPasswordFragmentListener mListener;

    public LockPasswordFragment() {
        super();
    }

    public static LockPasswordFragment newInstance(ContactItem user, OnLockPasswordFragmentListener listener) {
        LockPasswordFragment fragment = new LockPasswordFragment();
        fragment.addLockPasswordFragmentListener(listener);
        Bundle args = new Bundle();
        args.putParcelable(ContactItem.USER_CONTACT, user);
        fragment.setArguments(args);
        return fragment;
    }

    public void addLockPasswordFragmentListener(OnLockPasswordFragmentListener listener) {
        mListener = listener;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
/*
        if (context instanceof MobileActivateListener)
            mobileActivateListener = (MobileActivateListener) context;
*/
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
//        View view = inflater.inflate(R.layout.fragment_lockpwd, (ViewGroup) getView());

        Bundle arguments = getArguments();
        user = arguments.getParcelable(ContactItem.USER_CONTACT);

//        etOtp1 = (EditText) view.findViewById(R.id.et_otp_1);
//        etOtp2 = (EditText) view.findViewById(R.id.et_otp_2);
//        etOtp3 = (EditText) view.findViewById(R.id.et_otp_3);
//        etOtp4 = (EditText) view.findViewById(R.id.et_otp_4);
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
//                        performOkTask();
                    }
                }
                return false;
            }
        });
        etOtp4.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_GO) {
//                    performOkTask();
                    return true;
                }
                return false;
            }
        });
        unPackUser();
        etOtp1.requestFocus();

//        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.LaunchTheme);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//        builder.setTitle(R.string.set_password);
        builder.setView(view)
                // Add action buttons
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        performOkTask();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (mListener != null)
                            mListener.onDialogNegativeClick(LockPasswordFragment.this);
                    }
                });
        return builder.create();
    }

    private void unPackUser() {
//        final String otp = user.getOtp();
        final String pwd = MainApplicationSingleton.getInstance(getContext()).getStringValueSP(
                MainApplicationSingleton.LOCKPWD_PARAM
        );
        if (!TextUtils.isEmpty(pwd) && 4 == pwd.length()) {
            etOtp1.setText(pwd.substring(0, 1));
            etOtp2.setText(pwd.substring(1, 2));
            etOtp3.setText(pwd.substring(2, 3));
            etOtp4.setText(pwd.substring(3, 4));
        }
    }

    public View getSnackView() {
        if (null == snackView)
//            snackView = view.findViewById(R.id.username);
            snackView = view.findViewById(R.id.mr_art);
        return snackView;
    }

    public void invalidOTPAlert(String val) {
        etOtp4.setError(val);
    }

    public void performOkTask() {
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

        String pwd = t1 + t2 + t3 + t4;
        if (TextUtils.isEmpty(pwd) || pwd.length() < 4) {
            showOtpError();
            return;
        }
        MainApplicationSingleton.getInstance(getContext()).putStringValueSP(
                MainApplicationSingleton.LOCKPWD_PARAM, pwd
        );
//            user.setOtp(otp);
        if (mListener != null)
            mListener.onDialogPositiveClick(LockPasswordFragment.this);
    }

    public void showOtpError() {
        etOtp1.requestFocus();
        return;
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
//        Intent intent = new Intent(getActivity(), ProfileInfoActivity.class);
        Intent intent = new Intent(getActivity(), MainActivity.class);
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

    public interface OnLockPasswordFragmentListener {
        void onDialogPositiveClick(DialogFragment dialog);

        void onDialogNegativeClick(DialogFragment dialog);
    }

}
