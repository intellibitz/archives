package intellibitz.intellidroid.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import intellibitz.intellidroid.IntellibitzTwoPaneUserActivity;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.data.MessageItem;
import intellibitz.intellidroid.fragment.IntellibitzContactsFragment;
import intellibitz.intellidroid.listener.ContactListener;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.IntellibitzTwoPaneUserActivity;
import intellibitz.intellidroid.R;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.data.MessageItem;
import intellibitz.intellidroid.fragment.IntellibitzContactsFragment;
import intellibitz.intellidroid.listener.ContactListener;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.fragment.IntellibitzContactsFragment;

public class IntellibitzContactsActivity extends
        IntellibitzTwoPaneUserActivity implements
        ContactListener {
    private static final String TAG = "IntellibitzContactsAct";
    BroadcastReceiver messageToNestReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            addToNest(intent);
        }
    };
    private MessageItem messageItem;

    private void addToNest(Intent intent) {
        Log.e(TAG, TAG + intent);
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(messageToNestReceiver);
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(MessageItem.TAG, messageItem);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        messageItem = savedInstanceState.getParcelable(MessageItem.TAG);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intellibitzcontacts);
        if (null == savedInstanceState) {
            user = getIntent().getParcelableExtra(ContactItem.USER_CONTACT);
            messageItem = getIntent().getParcelableExtra(MessageItem.TAG);
        } else {
            user = savedInstanceState.getParcelable(ContactItem.USER_CONTACT);
            messageItem = savedInstanceState.getParcelable(MessageItem.TAG);
        }
        setupTwopane();
        setupAppBar();
        setupFragments();
        LocalBroadcastManager.getInstance(this).registerReceiver(
                messageToNestReceiver,
                new IntentFilter(MainApplicationSingleton.BROADCAST_MESSAGETO_NEST));
    }

    private void setupAppBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.pref_title_contacts);
        toolbar.setSubtitle(R.string.app_title);
        setSupportActionBar(toolbar);
        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupFragments() {
        final IntellibitzContactsFragment deviceContactsFragment =
                IntellibitzContactsFragment.newInstance(user, this);
//        msgsGrpNestFragment.onMessageForward(getIntent());
//                MsgsGrpNestFragment.newInstance(messageItem, user, this);
        replaceContentFragment(deviceContactsFragment);
    }

    /**
     * @param fragment the fragment to be replaced in the two pane container
     * @return fragment the replaced fragment
     */
    public Fragment replaceContentFragment(Fragment fragment) {
        if (null == fragment) {
            return null;
        }
        itemView.setVisibility(View.GONE);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.two_pane_container, fragment,
                fragment.getClass().getSimpleName());
//        fragmentTransaction.addToBackStack(fragment.getClass().getSimpleName());
        fragmentTransaction.commit();
        itemView.setVisibility(View.VISIBLE);
        return fragment;
    }

    /**
     * @param fragment the detail fragment to be replaced in the two pane container
     * @return fragment the replaced detail fragment
     */
    public Fragment replaceDetailFragment(Fragment fragment) {
        if (null == fragment) return null;
        itemView.setVisibility(View.GONE);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if (twoPane) {
            detailView.setVisibility(View.GONE);
            fragmentTransaction.replace(R.id.two_pane_empty_container, fragment,
                    fragment.getClass().getSimpleName());
            detailView.setVisibility(View.VISIBLE);
        } else {
            fragmentTransaction.replace(R.id.two_pane_container, fragment,
                    fragment.getClass().getSimpleName());
        }
        fragmentTransaction.commit();
        itemView.setVisibility(View.VISIBLE);
        return fragment;
    }

    @Override
    public void onBackPressed() {
//        if no more fragments is left.. then its the workflow activity which is blank
//        finish the blank activity.. to go back
        if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
            finish();
        }
        super.onBackPressed();
    }

    public void onTrimMemory(int level) {
        Log.d(TAG, "onTrimMemory: " + level);

    }

/*
    @Override
    public void onViewModeChanged() {

    }

    @Override
    public void onViewModeItem() {

    }
*/

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
//            NavUtils.navigateUpTo(this, new Intent(this, MainActivity.class));
//            NavUtils.navigateUpTo(this, NavUtils.getParentActivityIntent(this));
//            NavUtils.navigateUpFromSameTask(this);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        NOTE: THE ABOVE CALL DELIVERS THE ACTIVITY RESULT TO THE FRAGMENT
    }


/*
    @Override
    public void onEmailTopicClicked(MessageItem messageItem, ContactItem user) {
//        this can be invoked from 2 instances
//        from people item, or from email item
*/
/*
        if (msgsGrpClutterFragment != null) {
            View view = msgsGrpClutterFragment.getView();
            if (view != null) {
                view.setVisibility(View.GONE);
            }
        }
        if (msgsGrpPeopleFragment != null) {
            View view = msgsGrpPeopleFragment.getView();
            if (view != null) {
                view.setVisibility(View.GONE);
            }
        }
        hideFilterToolbars();
        showClutterEmailFragment(messageItem, user);
*//*

        startClutterEmailActivity(messageItem, user);
    }

    @Override
    public void onEmailTopicsLoaded(int count) {

    }
*/

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
    public void startClutterEmailActivity(MessageItem messageItem, ContactItem user) {
//        nulls shared intent (safe case)
//        getMainActivity().nullSharedIntent();
        Intent intent = new Intent(this, ClutterEmailActivity.class);
        intent.putExtra(MessageItem.EMAIL_MESSAGE, (Parcelable) messageItem);
        intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
        startActivityForResult(
                intent, MainApplicationSingleton.ACTIVITY_CLUTTEREMAIL_RQ_CODE);
    }

    @Override
    public void onViewModeChanged() {

    }

    @Override
    public void onViewModeItem() {

    }
}
