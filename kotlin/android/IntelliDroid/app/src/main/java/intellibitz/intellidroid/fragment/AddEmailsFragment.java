package intellibitz.intellidroid.fragment;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import intellibitz.intellidroid.IntellibitzUserFragment;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.db.UserEmailJoinColumns;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.IntellibitzUserFragment;
import intellibitz.intellidroid.R;
import intellibitz.intellidroid.activity.AddEmailActivity;
import intellibitz.intellidroid.activity.EmailAccountDetailActivity;
import intellibitz.intellidroid.content.UserEmailContentProvider;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.db.UserEmailJoinColumns;
import intellibitz.intellidroid.util.MainApplicationSingleton;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import intellibitz.intellidroid.activity.AddEmailActivity;
import intellibitz.intellidroid.content.UserEmailContentProvider;


/**
 *
 */
public class AddEmailsFragment extends
        IntellibitzUserFragment {

    private static final String TAG = "AddEmailsFragment";
    private Button btnNext;
    private ImageButton btnBack;

    private View view;
    private View snackView;
    private Snackbar snackbar;
    private View progressBar;
    private RecyclerView recyclerView;
    BroadcastReceiver emailAccountRemovedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            user = intent.getParcelableExtra(ContactItem.USER_CONTACT);
            setupRecyclerView();
        }
    };
    BroadcastReceiver emailAccountAddedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            user = intent.getParcelableExtra(ContactItem.USER_CONTACT);
            setupRecyclerView();
        }
    };

    public AddEmailsFragment() {
        super();
    }

    public static AddEmailsFragment newInstance(ContactItem user) {
        AddEmailsFragment fragment = new AddEmailsFragment();
        fragment.setUser(user);
        Bundle args = new Bundle();
        args.putParcelable(ContactItem.USER_CONTACT, user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onPause() {
//        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(emailAccountAddedReceiver);
//        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(emailAccountRemovedReceiver);
        super.onPause();
    }


    @Override
    public void onResume() {
        super.onResume();
/*
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(emailAccountAddedReceiver,
                new IntentFilter(MainApplicationSingleton.BROADCAST_EMAIL_ACCOUNT_ADDED));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(emailAccountRemovedReceiver,
                new IntentFilter(MainApplicationSingleton.BROADCAST_EMAIL_ACCOUNT_REMOVED));
*/
        setupRecyclerView();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_addemails, container, false);
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
        btnBack = (ImageButton) view.findViewById(R.id.btn_back);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });
        btnNext = (Button) view.findViewById(R.id.btn_next);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startAddEmailActivity();
            }
        });
        getSnackView();

        View rlLogin = view.findViewById(R.id.rl_login);
        rlLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startAddEmailActivity();
            }
        });
        recyclerView = (RecyclerView) view.findViewById(R.id.rv_emails);
        setupRecyclerView();

    }

    public View getSnackView() {
        if (null == snackView)
//            snackView = getActivity().findViewById(R.id.cl);
//            snackView = view.findViewById(R.id.ll);
            snackView = view.findViewById(R.id.username);
        return snackView;
    }

    private void setupRecyclerView() {
        Set<ContactItem> emails = user.getContactItems();
        if (null == emails || emails.isEmpty()) {
//                EmailSyncHandler.SyncEmailsFromCloud();
//            starts the sync from cloud
/*
            Context context = AddEmailsFragment.this;
            Intent intent = new Intent(context,
                    UserEmailIntentService.class);
            intent.setAction(UserEmailIntentService.ACTION_SYNC_MESSAGE_ATTACHMENTS);
            intent.putExtra(ContactItem.TAG, (Serializable) user);
            context.startService(intent);
*/
            Snackbar.make(recyclerView,
//                    "Please click add button to add new email account",
                    getResources().getText(R.string.add_email_snack),
                    Snackbar.LENGTH_SHORT)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startAddEmailActivity();
                        }
                    }).show();
//            startNewAccountActivity();
//            startAddEmailActivity();
        } else {
/*
            items = new ArrayList<>(emails.size());
            for (String email : emails) {
                items.add(new ContactItem(email, email, email));
            }
*/
        }
        recyclerView.setAdapter(new RecyclerViewAdapter(user.getContactItems()));
    }

    private void packUserEmailsFromDB() {
        Cursor cursor = getActivity().getContentResolver().query(
                ContentUris.withAppendedId(UserEmailContentProvider.CONTENT_URI, user.get_id()),
                null, null, null, null);
        if (null == cursor) return;
        if (cursor.getCount() > 0) {
            packUserEmailsFromCursor(cursor);
            cursor.close();
        }
    }

    private void packUserEmailsFromCursor(Cursor cursor) {
        do {
            String email = cursor.getString(
                    cursor.getColumnIndex(
                            UserEmailJoinColumns.KEY_EMAIL));
            if (email != null) {
                long emid = cursor.getLong(cursor.getColumnIndex("emid"));
                String name = cursor.getString(cursor.getColumnIndex("emname"));
                ContactItem item = new ContactItem(email, name, email);
                item.set_id(emid);
                user.addEmail(item);
            }
        } while (cursor.moveToNext());
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
    private void startAddEmailActivity() {
        Intent intent = new Intent(getActivity(), AddEmailActivity.class);
        intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
//        NOTE: there is a significant difference between the following two calls

//        THIS WILL DELIVER THE RESULT, TO PARENT ACTIVITY AND THE PARENT ACTIVITY IF OVERRIDDEN
//        MUST CALL SUPER.STARTACTIVITYFORRESULT IN ITS OVERRIDDEN METHOD FOR THE RESULT
//        TO BE DELIVERED TO THIS FRAGMENT
        startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_ADDEMAIL_RQ_CODE);

//        THIS WILL DELIVER THE RESULT, TO PARENT ACTIVITY AND THE PARENT ACTIVITY IF OVERRIDDEN
//        MUST MANUALLY INVOKE THE FRAGMENTS IN ITS OVERRIDDEN METHOD FOR THE RESULT
//        TO BE DELIVERED TO THIS FRAGMENT
//        getAppCompatActivity().startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_CONTACTSELECT_RQ_CODE);
    }

    private void startEmailAccountDetailActivity(ContactItem user, ContactItem userEmailItem) {
        Intent intent = new Intent(getActivity(), EmailAccountDetailActivity.class);
        intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
        intent.putExtra(ContactItem.TAG, (Parcelable) userEmailItem);
        startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_EMAILACCOUNTDETAIL_RQ_CODE);
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
        if (MainApplicationSingleton.ACTIVITY_ADDEMAIL_RQ_CODE == requestCode) {
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
        if (MainApplicationSingleton.ACTIVITY_EMAILACCOUNTDETAIL_RQ_CODE == requestCode) {
            if (Activity.RESULT_OK == resultCode) {
                if (data != null) {
//                    contacts selected for chat
                    user = data.getParcelableExtra(ContactItem.USER_CONTACT);
                    setupRecyclerView();
                }
            } else if (Activity.RESULT_CANCELED == resultCode) {
                Log.e(TAG, "onActivityResult: cancelled - ");
            }
        }
    }

    public class RecyclerViewAdapter
            extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

        private final List<ContactItem> items = new ArrayList<>();

        public RecyclerViewAdapter(Collection<ContactItem> items) {
            this.items.addAll(items);
        }

        @Override
        public RecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_addemails_rv, parent, false);
            return new AddEmailsFragment.RecyclerViewAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final RecyclerViewAdapter.ViewHolder holder, int position) {
            holder.userEmailItem = items.get(position);
//            holder.tvFrom.setText(items.get(position).id);
//            holder.mContentView.setText(items.get(position).getName());
            ViewGroup rootView = (ViewGroup) holder.mView.getRootView();
            LinearLayout linearLayout = (LinearLayout)
                    rootView.findViewById(R.id.ll_addemails_rv);
//            clears old views
            linearLayout.removeAllViews();
            View view = LayoutInflater.from(holder.mView.getContext()).inflate(
                    R.layout.fragment_addemails_rv_dyn,
                    (ViewGroup) holder.mView.getRootView(), false);
            TextView tv = (TextView) view.findViewById(R.id.tv_addemails_rv_dyn);
            tv.setText(holder.userEmailItem.getName());
            linearLayout.addView(view);


            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    user.setEmail(holder.userEmailItem.getEmail());
                    startEmailAccountDetailActivity(user, holder.userEmailItem);
/*
                    if (mTwoPane) {
                        Bundle arguments = new Bundle();
                        arguments.putString(EmailAccountDetailFragment.ARG_ITEM_ID, holder.userEmailItem.getDataId());
                        user.setEmailItem(holder.userEmailItem.getEmail());
                        EmailAccountDetailFragment fragment = new EmailAccountDetailFragment();
                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.emailaccount_detail_container, fragment)
                                .commit();
                    } else {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, EmailAccountDetailActivity.class);
                        user.setEmailItem(holder.userEmailItem.getEmail());
                        intent.putExtra(ContactItem.TAG, (Parcelable) user);
                        intent.putExtra(EmailAccountDetailFragment.ARG_ITEM_ID, holder.userEmailItem.getDataId());

                        context.startActivity(intent);
                    }
*/
                }
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public ContactItem userEmailItem;

            public ViewHolder(View view) {
                super(view);
                mView = view;
            }

            @Override
            public String toString() {
                return super.toString() + " '" + userEmailItem + "'";
            }
        }
    }

}
