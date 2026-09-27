package intellibitz.intellidroid.account;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.R;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.data.ContactItem;

/**
 *
 */
public class NewPasswordFragment extends
        BottomSheetDialogFragment {
    private static final String TAG = "NewPasswordFrag";

    private View snackView;

    private EditText tvPwd1;
    private EditText tvPwd2;
    private OnNewPasswordFragmentListener mListener;
    private ContactItem user;
    private int mode = -1;

    public NewPasswordFragment() {
        super();
    }

    public static NewPasswordFragment newInstance(ContactItem user) {
        NewPasswordFragment fragment = new NewPasswordFragment();
        Bundle args = new Bundle();
        args.putParcelable(ContactItem.USER_CONTACT, user);
        fragment.setArguments(args);
        return fragment;
    }

    public ContactItem getUser() {
        return user;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public void addNewPasswordFragmentListener(OnNewPasswordFragmentListener listener) {
        mListener = listener;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnNewPasswordFragmentListener)
            addNewPasswordFragmentListener((OnNewPasswordFragmentListener) context);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View view = inflater.inflate(R.layout.fragment_newpassword, (ViewGroup) getView());
        if (null == savedInstanceState) {
            user = getArguments().getParcelable(ContactItem.USER_CONTACT);
        } else {
            user = savedInstanceState.getParcelable(ContactItem.USER_CONTACT);
        }

        tvPwd1 = (EditText) view.findViewById(R.id.tv_password1);
        tvPwd2 = (EditText) view.findViewById(R.id.tv_password2);
//        unPackUser();
        tvPwd1.requestFocus();

        View rlLogin = view.findViewById(R.id.rl_login);
        View btnLogin = view.findViewById(R.id.btn_login);
        View ibtnLogin = view.findViewById(R.id.ibtn_login);
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performOkTask();
            }
        };
        rlLogin.setOnClickListener(onClickListener);
        btnLogin.setOnClickListener(onClickListener);
        ibtnLogin.setOnClickListener(onClickListener);

//        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.LaunchTheme);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//        builder.setTitle(R.string.new_password);
        builder.setView(view);
/*
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
                            mListener.onNewPasswordDialogNegativeClick(NewPasswordFragment.this);
                    }
                });
*/
        return builder.create();
    }

    public void performOkTask() {
        String pwd = tvPwd1.getText().toString();
        if (!TextUtils.isEmpty(pwd) &&
                pwd.length() >= 6 &&
                pwd.equals(tvPwd2.getText().toString())) {
            user.setPwd(pwd);
            if (mListener != null)
                mListener.onNewPasswordDialogPositiveClick(NewPasswordFragment.this);
//            okActivity();
        } else {
            setError("Passwords does not match - min 6 char");
        }
    }

    public View getSnackView() {
        if (null == snackView) {
            View view = getView();
            if (view != null)
                snackView = view.findViewById(R.id.username);
        }
        return snackView;
    }

    public void setError(String text) {
        //        clears the error message and the icon
        if (tvPwd2 != null)
            tvPwd2.setError(text);
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

    public interface OnNewPasswordFragmentListener {
        void onNewPasswordDialogPositiveClick(DialogFragment dialog);

        void onNewPasswordDialogNegativeClick(DialogFragment dialog);
    }

}
