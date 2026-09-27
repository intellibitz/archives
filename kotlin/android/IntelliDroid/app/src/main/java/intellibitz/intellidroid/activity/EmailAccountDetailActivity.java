package intellibitz.intellidroid.activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import intellibitz.intellidroid.IntellibitzTwoPaneUserActivity;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.fragment.EmailAccountDetailFragment;
import intellibitz.intellidroid.IntellibitzTwoPaneUserActivity;
import intellibitz.intellidroid.R;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.domain.account.EmailAccountListActivity;
import intellibitz.intellidroid.fragment.EmailAccountDetailFragment;
import intellibitz.intellidroid.fragment.EmailAccountDetailFragment;

/**
 * An activity representing a single EmailAccount detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link EmailAccountListActivity}.
 */
public class EmailAccountDetailActivity extends
        IntellibitzTwoPaneUserActivity {

    private static final String TAG = "EmailDetailActivity";
    private ContactItem userEmailItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emailaccount_detail);
//        getSupportFragmentManager().addOnBackStackChangedListener(this);
        if (null == savedInstanceState) {
            user = getIntent().getParcelableExtra(ContactItem.USER_CONTACT);
            userEmailItem = getIntent().getParcelableExtra(ContactItem.TAG);
            setupTwopane();
            setupAppBar();
            setupFragments();
        } else {
            user = savedInstanceState.getParcelable(ContactItem.USER_CONTACT);
            userEmailItem = savedInstanceState.getParcelable(ContactItem.TAG);
//            newLoginFragment(savedInstanceState);
        }
    }

    private void setupAppBar() {
        //        user = getIntent().getParcelableExtra(ContactItem.TAG);
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);
        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        if (appBarLayout != null) {
            appBarLayout.setTitle(userEmailItem.getName());
        }
    }

    private void setupFragments() {
        // Create the detail fragment and add it to the activity
        // using a fragment transaction.
/*
        Bundle arguments = new Bundle();
        arguments.putString(EmailAccountDetailFragment.ARG_ITEM_ID,
                getIntent().getStringExtra(EmailAccountDetailFragment.ARG_ITEM_ID));
        EmailAccountDetailFragment fragment = new EmailAccountDetailFragment();
        arguments.putParcelable(ContactItem.TAG, user);
        fragment.setArguments(arguments);
*/
        final EmailAccountDetailFragment emailAccountDetailFragment =
                EmailAccountDetailFragment.newInstance(userEmailItem, user);
        replaceContentFragment(emailAccountDetailFragment);
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
//            NavUtils.navigateUpTo(this, new Intent(this, MainActivity.class));
//            NavUtils.navigateUpTo(this, NavUtils.getParentActivityIntent(this));
//            NavUtils.navigateUpFromSameTask(this);
/*
            Intent intent = new Intent(this, EmailAccountListActivity.class);
            intent.putExtra(ContactItem.TAG, (Parcelable) user);
            NavUtils.navigateUpTo(this, intent);
*/
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
