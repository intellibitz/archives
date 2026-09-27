package intellibitz.intellidroid.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import androidx.core.app.NavUtils;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import intellibitz.intellidroid.IntellibitzTwoPaneUserActivity;
import intellibitz.intellidroid.MainActivity;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.data.MessageItem;
import intellibitz.intellidroid.fragment.MessageChatGroupFragment;
import intellibitz.intellidroid.listener.PeopleListener;
import intellibitz.intellidroid.IntellibitzTwoPaneUserActivity;
import intellibitz.intellidroid.MainActivity;
import intellibitz.intellidroid.R;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.data.MessageItem;
import intellibitz.intellidroid.fragment.MessageChatGroupFragment;
import intellibitz.intellidroid.listener.PeopleListener;
import intellibitz.intellidroid.fragment.MessageChatGroupFragment;

public class MessageChatGroupActivity extends
        IntellibitzTwoPaneUserActivity implements PeopleListener {
    private static final String TAG = "MessageChatGroupAct";
    private MessageItem messageItem;
//    private Toolbar toolbar;


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
        setContentView(R.layout.activity_messagechatgroup);
        if (null == savedInstanceState) {
            user = getIntent().getParcelableExtra(ContactItem.USER_CONTACT);
            messageItem = getIntent().getParcelableExtra(MessageItem.TAG);
        } else {
            user = savedInstanceState.getParcelable(ContactItem.USER_CONTACT);
            messageItem = savedInstanceState.getParcelable(MessageItem.TAG);
        }
        setupTwopane();
//        setupAppBar();
        setupFragments();
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
            NavUtils.navigateUpTo(this, new Intent(this, MainActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupFragments() {
        final MessageChatGroupFragment messageChatGroupFragment =
                MessageChatGroupFragment.newInstance(messageItem, user, this);
        replaceContentFragment(messageChatGroupFragment);
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

    public void onTrimMemory(int level) {
        Log.d(TAG, "onTrimMemory: " + level);

    }

    @Override
    public void onViewModeChanged() {

    }

    @Override
    public void onViewModeItem() {

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


}
