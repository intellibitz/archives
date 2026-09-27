package intellibitz.intellidroid.domain.account;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import intellibitz.intellidroid.activity.EmailAccountDetailActivity;
import intellibitz.intellidroid.content.UserEmailContentProvider;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.db.UserEmailJoinColumns;
import intellibitz.intellidroid.fragment.EmailAccountDetailFragment;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.R;
import intellibitz.intellidroid.activity.EmailAccountDetailActivity;
import intellibitz.intellidroid.content.UserEmailContentProvider;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.db.UserEmailJoinColumns;
import intellibitz.intellidroid.fragment.EmailAccountDetailFragment;
import intellibitz.intellidroid.util.MainApplicationSingleton;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;
import intellibitz.intellidroid.activity.EmailAccountDetailActivity;
import intellibitz.intellidroid.content.UserEmailContentProvider;
import intellibitz.intellidroid.fragment.EmailAccountDetailFragment;

/**
 * An activity representing a list of EmailAccounts. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link EmailAccountDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class EmailAccountListActivity extends AppCompatActivity {

    private static final int EMAIL_ACCOUNT_RESULT = 210;
    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private RecyclerView recyclerView;
    private ContactItem user;

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

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(emailAccountAddedReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(emailAccountRemovedReceiver);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(emailAccountAddedReceiver,
                new IntentFilter(MainApplicationSingleton.BROADCAST_EMAIL_ACCOUNT_ADDED));
        LocalBroadcastManager.getInstance(this).registerReceiver(emailAccountRemovedReceiver,
                new IntentFilter(MainApplicationSingleton.BROADCAST_EMAIL_ACCOUNT_REMOVED));
        setupRecyclerView();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        user = savedInstanceState.getParcelable(ContactItem.USER_CONTACT);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(ContactItem.USER_CONTACT, user);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emailaccount_list);
        user = getIntent().getParcelableExtra(ContactItem.USER_CONTACT);
/*
        List<ContactItem> emails = getIntent().getParcelableArrayListExtra(
                ContactItem.TAG);
*/
        Toolbar toolbar = (Toolbar) findViewById(R.id.email_list_toolbar);
        if (toolbar != null) {
            toolbar.setTitle(R.string.toolbar_emailaccounts_title);
        }
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startNewAccountActivity();
    /*
                    Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
    */
                }
            });
        }
        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (findViewById(R.id.emailaccount_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        recyclerView = (RecyclerView) findViewById(R.id.emailaccount_list);
        setupRecyclerView();

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
    private void startNewAccountActivity() {
        Intent intent = new Intent(EmailAccountListActivity.this, NewEmailAccountActivity.class);
        intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
        startActivityForResult(intent, EMAIL_ACCOUNT_RESULT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (EMAIL_ACCOUNT_RESULT == requestCode) {
            if (Activity.RESULT_OK == resultCode) {
                user = data.getParcelableExtra(ContactItem.USER_CONTACT);
                setupRecyclerView();
            } else if (Activity.RESULT_CANCELED == resultCode) {

            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupRecyclerView() {
        Set<ContactItem> emails = user.getContactItems();
        if (null == emails || emails.isEmpty()) {
//                EmailSyncHandler.SyncEmailsFromCloud();
//            starts the sync from cloud
/*
            Context context = EmailAccountListActivity.this;
            Intent intent = new Intent(context,
                    UserEmailIntentService.class);
            intent.setAction(UserEmailIntentService.ACTION_SYNC_MESSAGE_ATTACHMENTS);
            intent.putExtra(ContactItem.TAG, (Serializable) user);
            context.startService(intent);
*/
            Snackbar.make(recyclerView,
                    "Please click add button to add new email account",
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, null).show();
//            startNewAccountActivity();
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
        Cursor cursor = getContentResolver().query(
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

    public class RecyclerViewAdapter
            extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

        private final List<ContactItem> items;

        public RecyclerViewAdapter(Collection<ContactItem> items) {
            this.items = new ArrayList<>(items.size());
            this.items.addAll(items);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.emailaccount_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mItem = items.get(position);
//            holder.tvFrom.setText(items.get(position).id);
            holder.mContentView.setText(items.get(position).getName());

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTwoPane) {
                        Bundle arguments = new Bundle();
//                        arguments.putString(EmailAccountDetailFragment.ARG_ITEM_ID, holder.messageItem.getDataId());
                        user.setEmail(holder.mItem.getEmail());
                        EmailAccountDetailFragment fragment = new EmailAccountDetailFragment();
                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.emailaccount_detail_container, fragment)
                                .commit();
                    } else {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, EmailAccountDetailActivity.class);
                        user.setEmail(holder.mItem.getEmail());
                        intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
//                        intent.putExtra(EmailAccountDetailFragment.ARG_ITEM_ID, holder.messageItem.getDataId());

                        context.startActivity(intent);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView mIdView;
            public final TextView mContentView;
            public ContactItem mItem;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mIdView = (TextView) view.findViewById(R.id.tv_id);
                mContentView = (TextView) view.findViewById(R.id.content);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mContentView.getText() + "'";
            }
        }
    }


}
