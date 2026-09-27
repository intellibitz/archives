package intellibitz.intellidroid.company;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import intellibitz.intellidroid.IntellibitzTwoPaneUserActivity;
import intellibitz.intellidroid.MainActivity;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.listener.ContactListener;
import intellibitz.intellidroid.IntellibitzTwoPaneUserActivity;
import intellibitz.intellidroid.MainActivity;
import intellibitz.intellidroid.R;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.listener.ContactListener;

public class InviteUsersActivity extends
        IntellibitzTwoPaneUserActivity implements
        ContactListener {
    private static final String TAG = "InviteUsersActivity";
    private ContactItem contactItem;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(ContactItem.TAG, contactItem);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        contactItem = savedInstanceState.getParcelable(ContactItem.TAG);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contactselect);
        if (null == savedInstanceState) {
            user = getIntent().getParcelableExtra(ContactItem.USER_CONTACT);
            contactItem = getIntent().getParcelableExtra(ContactItem.TAG);
        } else {
            user = savedInstanceState.getParcelable(ContactItem.USER_CONTACT);
            contactItem = savedInstanceState.getParcelable(ContactItem.TAG);
        }
        setupTwopane();
//        setupAppBar();
        setupFragments();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        if (searchItem != null) {
            SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
            searchView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ActionBar actionBar = getSupportActionBar();
                    if (actionBar != null) {
                        actionBar.getCustomView().setVisibility(View.GONE);
                    }
                    InviteUsersFragment inviteUsersFragment = (InviteUsersFragment)
                            getSupportFragmentManager().getFragments().get(0);
                    if (inviteUsersFragment != null) {
                        inviteUsersFragment.startSearching();
                    }
//                    tvToolbarTitle.setVisibility(View.GONE);
//                    tvToolbarSubTitle.setVisibility(View.GONE);
//                    tbView.setVisibility(View.GONE);
                }
            });
            searchView.setOnSearchClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    InviteUsersFragment inviteUsersFragment = (InviteUsersFragment)
                            getSupportFragmentManager().getFragments().get(0);
                    if (inviteUsersFragment != null) {
                        inviteUsersFragment.startSearching();
                    }
//                    tvToolbarTitle.setVisibility(View.GONE);
//                    tvToolbarSubTitle.setVisibility(View.GONE);
//                    tbView.setVisibility(View.GONE);
//                    hideBottomToolbar();
                }
            });
            searchView.setOnCloseListener(new SearchView.OnCloseListener() {
                @Override
                public boolean onClose() {
                    InviteUsersFragment inviteUsersFragment = (InviteUsersFragment)
                            getSupportFragmentManager().getFragments().get(0);
                    if (inviteUsersFragment != null) {
                        inviteUsersFragment.onClose();
                        inviteUsersFragment.stopSearching();
                    }
                    ActionBar actionBar = getSupportActionBar();
                    if (actionBar != null) {
//                        actionBar.getCustomView().setVisibility(View.VISIBLE);
                    }
//                    tvToolbarTitle.setVisibility(View.VISIBLE);
//                    tvToolbarSubTitle.setVisibility(View.VISIBLE);
//                    tbView.setVisibility(View.VISIBLE);
//                    onQueryClose();
//                    showBottomToolbar();
                    return false;
                }
            });
            // use this method for search process
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    InviteUsersFragment inviteUsersFragment = (InviteUsersFragment)
                            getSupportFragmentManager().getFragments().get(0);
                    if (inviteUsersFragment != null) {
                        inviteUsersFragment.onQueryTextSubmit(query);
                    }
//                    onQuerySubmit(query);
//                    hideBottomToolbar();
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    InviteUsersFragment inviteUsersFragment = (InviteUsersFragment)
                            getSupportFragmentManager().getFragments().get(0);
                    if (inviteUsersFragment != null) {
                        inviteUsersFragment.onQueryTextChange(newText);
                    }
                    return false;
                    // use this method for auto complete search process
//                    onQueryChange(newText);
//                    hideBottomToolbar();
                }
            });
//            searchView.setOnQueryTextListener(messagesContentFragment);
//            searchView.setOnCloseListener(messagesContentFragment);
        }
        return super.onCreateOptionsMenu(menu);
    }

    private void setupAppBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.select_contacts);
        toolbar.setSubtitle(R.string.app_title);
        setSupportActionBar(toolbar);
        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
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
            NavUtils.navigateUpTo(this, new Intent(this, MainActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupFragments() {
        final InviteUsersFragment inviteUsersFragment =
                InviteUsersFragment.newInstance(contactItem, user, this);
        replaceContentFragment(inviteUsersFragment);
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

    @Override
    public void onViewModeChanged() {

    }

    @Override
    public void onViewModeItem() {

    }
}
