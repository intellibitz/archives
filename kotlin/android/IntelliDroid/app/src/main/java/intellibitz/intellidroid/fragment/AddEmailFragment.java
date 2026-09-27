package intellibitz.intellidroid.fragment;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import intellibitz.intellidroid.IntellibitzUserFragment;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.IntellibitzUserFragment;
import intellibitz.intellidroid.R;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.domain.account.NewEmailAccountActivity;
import intellibitz.intellidroid.util.MainApplicationSingleton;

import androidx.annotation.Nullable;


/**
 *
 */
public class AddEmailFragment extends
        IntellibitzUserFragment {

    private static final String TAG = "AddEmailFragment";
    private View view;
    private View snackView;
    private ProgressDialog progressDialog;

    public AddEmailFragment() {
        super();
    }

    public static AddEmailFragment newInstance(ContactItem user) {
        AddEmailFragment fragment = new AddEmailFragment();
        fragment.setUser(user);
        Bundle args = new Bundle();
        args.putParcelable(ContactItem.USER_CONTACT, user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_addemail, container, false);
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
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                    contacts selected for chat
//                ContactItem item = data.getParcelableExtra(ContactItem.TAG);
                Activity activity = getActivity();
                Intent intent = activity.getIntent();
                intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
                activity.setResult(Activity.RESULT_OK, intent);
                activity.finish();
            }
        };
        Button btnSkip = (Button) view.findViewById(R.id.btn_skip);
        btnSkip.setOnClickListener(onClickListener);
        View rlSkip = view.findViewById(R.id.rl_skip);
        rlSkip.setOnClickListener(onClickListener);

        getSnackView();

        View llGmail = view.findViewById(R.id.ll_addemail_gmail);
        llGmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startNewEmailAccountActivity();
            }
        });
        View ivGmail = view.findViewById(R.id.iv_addemail_gmail);
        ivGmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showProgress();
                startNewEmailAccountActivity();
                hideProgress();
            }
        });
        View tvGmail = view.findViewById(R.id.tv_addemail_gmail);
        tvGmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showProgress();
                startNewEmailAccountActivity();
                hideProgress();
            }
        });
    }

    public void showProgress() {
        progressDialog = ProgressDialog.show(getActivity(), "Add Email", "Redirecting to Email provider", true);
    }

    public void hideProgress() {
        if (progressDialog != null)
            progressDialog.dismiss();
    }

    public View getSnackView() {
        if (null == snackView)
//            snackView = getActivity().findViewById(R.id.cl);
//            snackView = view.findViewById(R.id.ll);
            snackView = view.findViewById(R.id.username);
        return snackView;
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
    private void startNewEmailAccountActivity() {
        Intent intent = new Intent(getActivity(), NewEmailAccountActivity.class);
        intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
//        NOTE: there is a significant difference between the following two calls

//        THIS WILL DELIVER THE RESULT, TO PARENT ACTIVITY AND THE PARENT ACTIVITY IF OVERRIDDEN
//        MUST CALL SUPER.STARTACTIVITYFORRESULT IN ITS OVERRIDDEN METHOD FOR THE RESULT
//        TO BE DELIVERED TO THIS FRAGMENT
        startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_NEWEMAILACCOUNT_RQ_CODE);

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
        if (MainApplicationSingleton.ACTIVITY_OTP_RQ_CODE == requestCode) {
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
                Log.e(TAG, "onActivityResult: OTP cancelled - ");
            }
        }
        if (MainApplicationSingleton.ACTIVITY_NEWEMAILACCOUNT_RQ_CODE == requestCode) {
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
                Log.e(TAG, "onActivityResult: Add new email - cancelled ");
            }
        }
    }

}
