package intellibitz.intellidroid.account;


import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
public class UserSignupFragment extends
        BottomSheetDialogFragment {
    private static final String TAG = "UserSignupFrag";

    private View view;
    private View snackView;

    private OnUserSignupFragmentListener mListener;
    private ContactItem user;

    public UserSignupFragment() {
        super();
    }

    public static UserSignupFragment newInstance(ContactItem user, OnUserSignupFragmentListener listener) {
        UserSignupFragment fragment = new UserSignupFragment();
        fragment.addUserSignupFragmentListener(listener);
        Bundle args = new Bundle();
        args.putParcelable(ContactItem.USER_CONTACT, user);
        fragment.setArguments(args);
        return fragment;
    }

    public ContactItem getUser() {
        return user;
    }

    public void addUserSignupFragmentListener(OnUserSignupFragmentListener listener) {
        mListener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View view = inflater.inflate(R.layout.fragment_usersignup, (ViewGroup) getView());

        Bundle arguments = getArguments();
        user = arguments.getParcelable(ContactItem.USER_CONTACT);


        View signup1 = view.findViewById(R.id.rl_signup);
        View signup2 = view.findViewById(R.id.btn_signup);
        View signup3 = view.findViewById(R.id.ibtn_signup);
        View.OnClickListener signupOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null)
                    mListener.onUserSignupDialogPositiveClick(UserSignupFragment.this);
            }
        };
        signup1.setOnClickListener(signupOnClickListener);
        signup2.setOnClickListener(signupOnClickListener);
        signup3.setOnClickListener(signupOnClickListener);

        View tryagain1 = view.findViewById(R.id.rl_tryagain);
        View tryagain2 = view.findViewById(R.id.btn_tryagain);
        View tryagain3 = view.findViewById(R.id.ibtn_tryagain);
        View.OnClickListener cancelOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null)
                    mListener.onUserSignupDialogNegativeClick(UserSignupFragment.this);
            }
        };
        tryagain1.setOnClickListener(cancelOnClickListener);
        tryagain2.setOnClickListener(cancelOnClickListener);
        tryagain3.setOnClickListener(cancelOnClickListener);

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

    public View getSnackView() {
        if (null == snackView)
            snackView = view.findViewById(R.id.username);
        return snackView;
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

    public interface OnUserSignupFragmentListener {
        void onUserSignupDialogPositiveClick(DialogFragment dialog);

        void onUserSignupDialogNegativeClick(DialogFragment dialog);
    }

}
