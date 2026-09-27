package intellibitz.intellidroid.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;

import intellibitz.intellidroid.IntellibitzActivityFragment;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.listener.ProfileListener;
import intellibitz.intellidroid.listener.ProfileTopicListener;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.IntellibitzActivityFragment;
import intellibitz.intellidroid.R;
import intellibitz.intellidroid.activity.ProfileInfoActivity;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.listener.ProfileListener;
import intellibitz.intellidroid.listener.ProfileTopicListener;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.widget.LockPasswordFragment;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

/**
 *
 */
public class MyAccountFragment extends
        IntellibitzActivityFragment implements LockPasswordFragment.OnLockPasswordFragmentListener {

    public static final String TAG = "MyAccountFrag";
    private View snackView;
    private ProfileTopicListener profileTopicListener;
    private ImageView imageView;

    public MyAccountFragment() {
        // Required empty public constructor
        super();
    }

    public static MyAccountFragment newInstance(ProfileListener listener, ContactItem user) {
        MyAccountFragment fragment = new MyAccountFragment();
        if (listener instanceof ProfileTopicListener)
            fragment.setProfileTopicListener((ProfileTopicListener) listener);
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
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        profileTopicListener = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(ContactItem.USER_CONTACT, user);
        View view = getView();
        if (view != null) {
            Switch startLock = (Switch) view.findViewById(R.id.sw_startuplock);
            MainApplicationSingleton.getInstance(getContext()).putBooleanValueSP(
                    MainApplicationSingleton.STARTUPLOCK_PARAM, startLock.isChecked()
            );
            Switch inappLock = (Switch) view.findViewById(R.id.sw_inapplock);
            MainApplicationSingleton.getInstance(getContext()).putBooleanValueSP(
                    MainApplicationSingleton.INAPPLOCK_PARAM, inappLock.isChecked()
            );
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        final boolean flag1 = MainApplicationSingleton.getInstance(getContext()).getBooleanValueSP(
                MainApplicationSingleton.STARTUPLOCK_PARAM
        );
        final boolean flag2 = MainApplicationSingleton.getInstance(getContext()).getBooleanValueSP(
                MainApplicationSingleton.INAPPLOCK_PARAM
        );

        View view = getView();
        if (view != null) {
            Switch startLock = (Switch) view.findViewById(R.id.sw_startuplock);
            startLock.setChecked(flag1);
            Switch inappLock = (Switch) view.findViewById(R.id.sw_inapplock);
            inappLock.setChecked(flag2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_myaccount, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        snackView = view.findViewById(R.id.cl);
        if (null == savedInstanceState) {
            user = getArguments().getParcelable(ContactItem.USER_CONTACT);
        } else {
            user = savedInstanceState.getParcelable(ContactItem.USER_CONTACT);
        }
        View setpwd = view.findViewById(R.id.ll_setpwd);
        setpwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLockPwdDialog();
            }
        });
    }

    public void showLockPwdDialog() {
        LockPasswordFragment.newInstance(user, this).show(
                getAppCompatActivity().getSupportFragmentManager(), "LockPasswordDialog");
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        if (dialog instanceof LockPasswordFragment) {
            LockPasswordFragment lockPasswordFragment = (LockPasswordFragment) dialog;
            final String s = MainApplicationSingleton.getInstance(getContext()).getStringValueSP(
                    MainApplicationSingleton.LOCKPWD_PARAM
            );
            Log.d(TAG, "onDialogPositiveClick" + s);
        }
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        if (dialog instanceof LockPasswordFragment) {
            LockPasswordFragment lockPasswordFragment = (LockPasswordFragment) dialog;
            final String s = MainApplicationSingleton.getInstance(getContext()).getStringValueSP(
                    MainApplicationSingleton.LOCKPWD_PARAM
            );
            Log.d(TAG, "onDialogNegativeClick" + s);
        }

    }
}
