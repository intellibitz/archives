package intellibitz.intellidroid.fragment;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import intellibitz.intellidroid.IntellibitzUserFragment;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.task.RemoveEmailTask;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.IntellibitzUserFragment;
import intellibitz.intellidroid.R;
import intellibitz.intellidroid.activity.AddEmailsActivity;
import intellibitz.intellidroid.content.UserEmailContentProvider;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.task.RemoveEmailTask;
import intellibitz.intellidroid.util.MainApplicationSingleton;

import intellibitz.intellidroid.activity.AddEmailsActivity;
import intellibitz.intellidroid.content.UserEmailContentProvider;
import org.json.JSONObject;

import androidx.annotation.Nullable;
import androidx.core.app.NavUtils;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

/**
 *
 */
public class EmailAccountDetailFragment extends
        IntellibitzUserFragment implements
        RemoveEmailTask.RemoveEmailTaskListener {
    public static final String TAG = "EmailAccountDetailFrag";

    private RemoveEmailTask removeEmailTask = null;
    private ContactItem userEmail;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public EmailAccountDetailFragment() {
        super();
    }

    public static EmailAccountDetailFragment newInstance(ContactItem userEmailItem, ContactItem user) {
        EmailAccountDetailFragment fragment = new EmailAccountDetailFragment();
        fragment.setUser(user);
        fragment.setUserEmail(userEmailItem);
        Bundle args = new Bundle();
        args.putParcelable(ContactItem.USER_CONTACT, user);
        args.putParcelable(ContactItem.TAG, userEmailItem);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.emailaccount_detail, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (null == savedInstanceState) {
            user = getArguments().getParcelable(ContactItem.USER_CONTACT);
            userEmail = getArguments().getParcelable(ContactItem.TAG);
        } else {
            user = savedInstanceState.getParcelable(ContactItem.USER_CONTACT);
            userEmail = savedInstanceState.getParcelable(ContactItem.TAG);
        }
        setupFAB(view);
        // Show the email content as text in a TextView.
        ((TextView) view.findViewById(R.id.tv_email)).setText(user.getEmail());
    }

    private void setupFAB(View view) {
        //        id = getIntent().getStringExtra(EmailAccountDetailFragment.ARG_ITEM_ID);

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    removeEmail();
                }
            });
        }
    }

    private void removeEmail() {
        MainApplicationSingleton.alertDialog(getContext(),
                getString(R.string.delete_email_alert),
                getString(R.string.remove_email_title),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        removeEmailTask = new RemoveEmailTask(user.getEmail(),
                                user.getDataId(), user.getToken(), user.getDevice(), user.getDeviceRef(),
                                MainApplicationSingleton.AUTH_REMOVE_EMAIL, getContext());
                        removeEmailTask.setRequestTimeoutMillis(30000);
                        removeEmailTask.setRemoveEmailTaskListener(EmailAccountDetailFragment.this);
                        removeEmailTask.execute();
                    }
                },
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
    /*
                    Snackbar.make(view, "Replace with your own detail action", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
    */

//                    MainApplicationSingleton mainApplication = MainApplicationSingleton.getInstance();
        //                userEmailItem = MainApplication.CONTACT_ITEM_MAP.get(id);
        //                String uid = mainApplication.getStringValueSP(MainApplicationSingleton.UID_PARAM);
        //                String token = mainApplication.getStringValueSP(MainApplicationSingleton.TOKEN_PARAM);
        //                String device = mainApplication.getStringValueSP(MainApplicationSingleton.DEVICE_PARAM);
        //                String email = extras.getString(MainApplication.CONTACT_PARAM);
    }

    private void removeEmailAccount() {
        String email = user.getEmail();
//        long emailId = user.getEmailId(email);
//        int result = UserEmailContentProvider.deletesUserEmail(emailId, getContext());
        int result = UserEmailContentProvider.deletesUserEmail(email, getContext());
        Log.d(TAG, "Deleted Email: " + result);
//        remove from user email collection
        user.removeEmail(email);
//        MainApplicationSingleton mainApplication = MainApplicationSingleton.getInstance();
//        mainApplication.removeStringSetValueSP(MainApplicationSingleton.EMAIL_PARAM, user.getEmail());
        Intent intent =
                new Intent(MainApplicationSingleton.BROADCAST_EMAIL_ACCOUNT_REMOVED);
        intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);

    }


    @Override
    public void onPostRemoveEmailResponse(JSONObject response) {
        showProgress(getContext(), getString(R.string.remove_email_title),
                getString(R.string.remove_email_progress), true);
        try {
            int status = response.getInt(MainApplicationSingleton.STATUS_PARAM);
            if (1 == status) {
//                    SUCCESS
                removeEmailAccount();
                //// TODO: 05-02-2016
//                    token ok, password to be set for user
//                    String url = "https://www.google.com";
/*
                    Bundle bundle = new Bundle();
                    bundle.putString(MainApplicationSingleton.UID_PARAM, user.getDataId());
                    bundle.putString(MainApplicationSingleton.TOKEN_PARAM, user.getToken());
                    bundle.putString(MainApplicationSingleton.EMAIL_PARAM, user.getEmail());
*/

                Intent intent = new Intent(getActivity(), AddEmailsActivity.class);
                intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
                NavUtils.navigateUpTo(getActivity(), intent);

//// TODO: 11-02-2016
//                    get back to application flow.. set password, home et al
/*
                    Intent intent = new Intent(NewEmailGetTokenActivity.this, IntellibitzActivity.class);
                    intent.putExtras(bundle);
                    startActivity(intent);
*/
//                    startActivityForResult(intent, 1, bundle);
            } else if (99 == status) {
//                    invalid email account..
                removeEmailAccount();
                Bundle bundle = new Bundle();
                bundle.putString("error", response.toString());
                bundle.putString(MainApplicationSingleton.EMAIL_PARAM, user.getEmail());
//// TODO: 11-02-2016
//                    get back to application flow.. set password, home et al
/*
                    Intent intent = new Intent(EmailAccountDetailActivity.this, NewEmailAccountActivity.class);
                    intent.putExtras(bundle);
                    startActivity(intent);
*/
//                    SUCCESS
            }
        } catch (Throwable e) {
            e.printStackTrace();
            Log.e(TAG, TAG + e.toString());
        }
        setEmailRemoveTaskToNull();
        finishResultActivity();
        hideProgress();
    }

    private void finishResultActivity() {
        Activity activity = getActivity();
        Intent intent = activity.getIntent();
        intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
        activity.setResult(Activity.RESULT_OK, intent);
        activity.finish();
    }

    @Override
    public void onPostRemoveEmailErrorResponse(JSONObject response) {
        setEmailRemoveTaskToNull();
    }

    @Override
    public void setEmailRemoveTaskToNull() {
        removeEmailTask = null;
    }

    public ContactItem getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(ContactItem userEmail) {
        this.userEmail = userEmail;
    }
}

