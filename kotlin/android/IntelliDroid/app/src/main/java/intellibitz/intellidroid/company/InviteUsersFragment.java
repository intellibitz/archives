package intellibitz.intellidroid.company;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import intellibitz.intellidroid.IntellibitzActivityFragment;
import intellibitz.intellidroid.content.DeviceContactContentProvider;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.db.ContactItemColumns;
import intellibitz.intellidroid.listener.ContactListener;
import intellibitz.intellidroid.listener.IntellibitzContactTopicListener;
import intellibitz.intellidroid.task.BitmapFromUrlTask;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.util.NetworkImageView;
import intellibitz.intellidroid.IntellibitzActivityFragment;
import intellibitz.intellidroid.R;
import intellibitz.intellidroid.content.DeviceContactContentProvider;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.db.ContactItemColumns;
import intellibitz.intellidroid.listener.ContactListener;
import intellibitz.intellidroid.listener.IntellibitzContactTopicListener;
import intellibitz.intellidroid.task.BitmapFromUrlTask;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.util.NetworkImageView;
import intellibitz.intellidroid.content.DeviceContactContentProvider;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 *
 */
public class InviteUsersFragment extends
        IntellibitzActivityFragment implements
        SearchView.OnQueryTextListener,
        SearchView.OnCloseListener,
        LoaderManager.LoaderCallbacks<Cursor>,
        TabLayout.OnTabSelectedListener {
    //    View.OnClickListener,
    //        Toolbar.OnMenuItemClickListener,
//        View.OnClickListener {
//        TabLayout.OnTabSelectedListener {
    public static final String TAG = "InviteUsersFrag";
    SparseArray<ContactItem> contactItemSparseArray = new SparseArray<>();
    private ContentObserver contentObserver;
    private Intent shared;
    private View view;
    //    private Toolbar detailToolbar;
//    private Toolbar detailFilterToolbar;
//    private TabLayout detailTabLayoutFilter;
    private RecyclerView recyclerView;
    private RecyclerViewAdapter viewAdapter;
    private HashSet<ContactItem> selectedContactItems = new HashSet<>();
    private ContactItem contactItem;
    private String onQueryTextSubmit;
    private String filter;
    private ContactListener contactListener;
    private IntellibitzContactTopicListener contactTopicListener;

    public static InviteUsersFragment newInstance(ContactItem contactItem,
                                                  ContactItem user, ContactListener contactListener) {
        InviteUsersFragment fragment = new InviteUsersFragment();
        fragment.setContactListener(contactListener);
        if (contactListener instanceof IntellibitzContactTopicListener) {
            fragment.setContactTopicListener((IntellibitzContactTopicListener) contactListener);
        }
        fragment.setUser(user);
        fragment.setContactItem(contactItem);
        Bundle args = new Bundle();
        args.putParcelable(ContactItem.USER_CONTACT, user);
        args.putParcelable(ContactItem.TAG, contactItem);
        fragment.setArguments(args);
        return fragment;
    }

    public ContactItem getContactItem() {
        return contactItem;
    }

    public void setContactItem(ContactItem contactItem) {
        this.contactItem = contactItem;
    }

    public void setContactListener(ContactListener contactListener) {
        this.contactListener = contactListener;
    }

    public void setContactTopicListener(IntellibitzContactTopicListener contactTopicListener) {
        this.contactTopicListener = contactTopicListener;
    }

    public void onNewMenuClicked() {

    }

    @Override
    public void onDestroy() {
        getActivity().getContentResolver().unregisterContentObserver(contentObserver);
        super.onDestroy();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        contentObserver = new ContentObserver(new Handler()) {
            @Override
            public boolean deliverSelfNotifications() {
                return super.deliverSelfNotifications();
            }

            @Override
            public void onChange(boolean selfChange) {
                onChange(selfChange, null);
            }

            @Override
            public void onChange(boolean selfChange, Uri uri) {
                restartLoader();
            }
        };
        getActivity().getContentResolver().registerContentObserver(
                DeviceContactContentProvider.CONTENT_URI, false,
                contentObserver);

    }

/*
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_search, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        if (searchItem != null) {
            SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
            searchView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.getRootView().findViewById(R.id.toolbar);
//                    tvToolbarTitle.setVisibility(View.GONE);
//                    tvToolbarSubTitle.setVisibility(View.GONE);
//                    tbView.setVisibility(View.GONE);
                }
            });
            searchView.setOnSearchClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    tvToolbarTitle.setVisibility(View.GONE);
//                    tvToolbarSubTitle.setVisibility(View.GONE);
//                    tbView.setVisibility(View.GONE);
//                    hideBottomToolbar();
                }
            });
            searchView.setOnCloseListener(new SearchView.OnCloseListener() {
                @Override
                public boolean onClose() {
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
//                    onQuerySubmit(query);
//                    hideBottomToolbar();
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    // use this method for auto complete search process
//                    onQueryChange(newText);
//                    hideBottomToolbar();
                    return false;
                }
            });
//            searchView.setOnQueryTextListener(messagesContentFragment);
//            searchView.setOnCloseListener(messagesContentFragment);
        }
    }
*/

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(ContactItem.USER_CONTACT, user);
        outState.putParcelable(ContactItem.TAG, contactItem);
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_inviteusers, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (null == savedInstanceState) {
            user = getArguments().getParcelable(ContactItem.USER_CONTACT);
            contactItem = getArguments().getParcelable(ContactItem.TAG);
        } else {
            user = savedInstanceState.getParcelable(ContactItem.USER_CONTACT);
            contactItem = savedInstanceState.getParcelable(ContactItem.TAG);
        }
        if (null == contactItem) contactItem = new ContactItem("InviteUsersFragment");
        setupAppBar();
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        setupSwipe(view);
//        detailFilterToolbar = (Toolbar) view.findViewById(R.id.toolbar5);
//        detailTabLayoutFilter = (TabLayout) view.findViewById(R.id.tablayout5);
//        setupDetailFilterToolbar();
        restartLoader();
    }

    private void restartLoader(String query) {
        this.filter = query;
        restartLoader();
    }

    private void restartLoader() {
        getActivity().getSupportLoaderManager().restartLoader(
                MainApplicationSingleton.CONTACTSELECT_LOADERID, null, this);
    }


    public void createRecycleAdapter() {
        if (null == recyclerView && view != null)
            recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        if (null == recyclerView) return;
        if (null == contactItemSparseArray || 0 == contactItemSparseArray.size()) return;
        List<ContactItem> values = MainApplicationSingleton.asList(contactItemSparseArray);
        viewAdapter = new RecyclerViewAdapter(values);
        viewAdapter.setHasStableIds(true);
        recyclerView.swapAdapter(viewAdapter, true);
//        recyclerView.scrollToPosition(viewAdapter.getItemCount() - 1);
    }

    public void setupSwipe(View view) {
        final SwipeRefreshLayout refreshLayout = (SwipeRefreshLayout)
                view.findViewById(R.id.swiperefresh);
        if (refreshLayout != null) {
            refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                private final Handler mHandler = new Handler();

                @Override
                public void onRefresh() {
                    // Post a delayed runnable to reset the refreshing state in 2 seconds
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            refreshLayout.setRefreshing(false);
                        }
                    }, 2000);
                }
            });
        }
    }

/*
    private void setupToolbar() {
        if (getActivity() != null) {
//            Toolbar toolbar = getActivity().getToolbar();
//            toolbar.setTitle(R.string.toolbar_workcontacts_title);
            setupDetailToolbar();
            setupDetailFilterToolbar();
        }
    }

    private void setupDetailToolbar() {
        if (detailToolbar != null) {
            detailToolbar.setNavigationIcon(R.drawable.ic_dialog_close_light);
            detailToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    performNavigationClose();
                }
            });
            setupDetailTitle(detailToolbar);
        }
    }

    private void setupDetailTitle(Toolbar toolbar) {
        String title = "Select Contact";
        String subTitle = "Groups";
        toolbar.setTitle(title);
        toolbar.setSubtitle(subTitle);
    }
*/

    private void setSubTitle(int count) {
        Toolbar toolbar = getToolbar();
        if (toolbar != null) {
//        toolbar.setTitle(R.string.new_group);
//            TextView tvTitle = (TextView) view.findViewById(R.id.tv_subtitle);
            TextView tvSubTitle = (TextView) toolbar.findViewById(R.id.tv_subtitle);
            tvSubTitle.setText(count + " " + getString(R.string.selected));
        }
    }

    private void setupAppBar() {
        Toolbar toolbar = getToolbar();
        if (toolbar != null) {
            setSubTitle(0);
//        toolbar.setTitle(R.string.new_group);
//            toolbar.setSubtitle(R.string.selected);
            TextView tvOk = (TextView) toolbar.findViewById(R.id.btn_ok);
            tvOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onOkPressed();
                }
            });
            TextView tvClose = (TextView) toolbar.findViewById(R.id.tv_close);
            tvClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onCancel();
                }
            });
            getAppCompatActivity().setSupportActionBar(toolbar);
            // Show the Up button in the action bar.
/*
            ActionBar actionBar = getAppCompatActivity().getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
*/
        }
    }

    public void startSearching() {
        Toolbar toolbar = getToolbar();
        if (toolbar != null) {
            TextView tvTitle = (TextView) toolbar.findViewById(R.id.tv_title);
            tvTitle.setVisibility(View.GONE);
            TextView tvSubTitle = (TextView) toolbar.findViewById(R.id.tv_subtitle);
            tvSubTitle.setVisibility(View.GONE);
            TextView tvOk = (TextView) toolbar.findViewById(R.id.btn_ok);
            tvOk.setVisibility(View.GONE);
            TextView tvClose = (TextView) toolbar.findViewById(R.id.tv_close);
            tvClose.setVisibility(View.GONE);
        }
    }

    public void stopSearching() {
        Toolbar toolbar = getToolbar();
        if (toolbar != null) {
            TextView tvTitle = (TextView) toolbar.findViewById(R.id.tv_title);
            tvTitle.setVisibility(View.VISIBLE);
            TextView tvSubTitle = (TextView) toolbar.findViewById(R.id.tv_subtitle);
            tvSubTitle.setVisibility(View.VISIBLE);
            TextView tvOk = (TextView) toolbar.findViewById(R.id.btn_ok);
            tvOk.setVisibility(View.VISIBLE);
            TextView tvClose = (TextView) toolbar.findViewById(R.id.tv_close);
            tvClose.setVisibility(View.VISIBLE);
        }
    }

    public Toolbar getToolbar() {
        View view = getView();
        if (view != null)
            return (Toolbar) view.findViewById(R.id.toolbar);
        return null;
    }

    private void setupDetailFilterToolbar() {
//        Toolbar detailFilterToolbar = clearDetailFilters();
        clearDetailFilters();
/*
        detailFilterToolbar.inflateMenu(R.menu.menu_intellibitzcontacts);
        Menu menu = detailFilterToolbar.getMenu();
        int size = menu.size();
        for (int i = 0; i < size; i++) {
            MenuItem item = menu.getItem(i);
            TabLayout.Tab tab = newTabFromMenuItem(item);
            detailTabLayoutFilter.addTab(tab, false);
        }
        detailTabLayoutFilter.addOnTabSelectedListener(this);
        detailFilterToolbar.setVisibility(View.GONE);
*/
    }

    @NonNull
    private Toolbar clearDetailFilters() {
//        Toolbar detailFilterToolbar = clearDetailFilterToolbarMenus();
        clearDetailFilterToolbarMenus();
        removeAllDetailTabLayoutFilters();
/*
        detailFilterToolbar.setVisibility(View.GONE);
        return detailFilterToolbar;
*/
        return null;
    }

    private void removeAllDetailTabLayoutFilters() {
//        detailTabLayoutFilter = getActivity().getDetailTabLayoutFilter();
/*
        detailTabLayoutFilter.removeAllTabs();
        detailTabLayoutFilter.removeOnTabSelectedListener(this);
        detailTabLayoutFilter.clearOnTabSelectedListeners();
*/
    }

    @NonNull
    private Toolbar clearDetailFilterToolbarMenus() {
//        Toolbar detailFilterToolbar = getActivity().getDetailFilterToolbar();
/*
        Menu menu = detailFilterToolbar.getMenu();
        menu.clear();
        return detailFilterToolbar;
*/
        return null;
    }

/*
    @NonNull
    private TabLayout.Tab newTabFromMenuItem(MenuItem item) {
        TabLayout.Tab tab = detailTabLayoutFilter.newTab();
        tab.setTag(item.getItemId());
        tab.setText(item.getTitle());
        tab.setCustomView(R.layout.tab_item);
        setSubTitle(tab, item.getTitle().toString());
*/
/*
        Drawable drawable = item.getIcon();
        drawable.setBounds(new Rect(0, 0, 20, 20));
        text.setCompoundDrawablesRelative(null, drawable, null, null);
*//*

        return tab;
    }
*/

    /**
     * contacts have been selected, go back to previous screen
     */
    private void onOkPressed() {
        //            do in background returns success.. handle them
//        this activity, is called for result with this intent.. so the parent is aware
        final Activity activity = getActivity();
        Intent intent = activity.getIntent();
//        Intent intent = new Intent();
        if (null == selectedContactItems || selectedContactItems.isEmpty()) {
            onCancel();
        } else {
            contactItem.setSelectedContacts(selectedContactItems);
            intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
            intent.putExtra(ContactItem.TAG, (Parcelable) contactItem);
            activity.setResult(Activity.RESULT_OK, intent);
            activity.finish();
        }
//        startActivity(intent);
/*
        clearDetailFilters();
        contactItem.setSelectedContacts(selectedContactItems);
//        IntellibitzActivity intellibitzActivity = removeSelf();
//        if (intellibitzActivity == null) return;
//        this.onDestroy();
//        intellibitzActivity.onBackPressed(intent);
        Intent intent = new Intent();
        intent.setAction(InviteUsersFragment.TAG);
        intent.putExtra(ContactItem.TAG, (Parcelable) contactItem);
        getActivity().onOkPressed(intent);
*/
    }

    private void onCancel() {
        final Activity activity = getActivity();
        Intent intent = activity.getIntent();
        intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
        intent.putExtra(ContactItem.TAG, (Parcelable) contactItem);
        activity.setResult(Activity.RESULT_CANCELED, intent);
        activity.finish();
    }


    public boolean onBackPressed() {
        removeSelf();
/*
        View view = getView();
        if (view != null) view.setVisibility(View.GONE);
        getActivity().removeFragment(this);
*/
//        performNavigationClose();
        return true;
    }


    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
        if (null == tab) return;
        if (null == tab.getTag()) return;
        if (R.id.micc_clear == (int) tab.getTag()) {
            return;
        }
        if (R.id.micc_select == (int) tab.getTag()) {
            return;
        }
        if (R.id.micc_done == (int) tab.getTag()) {
            return;
        }
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
        onTabSelected(tab);
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        if (null == tab) return;
        if (null == tab.getTag()) return;
        if (R.id.micc_clear == (int) tab.getTag()) {
            restartLoader();
            setSelectedCount(0);
            return;
        }
        if (R.id.micc_select == (int) tab.getTag()) {
            return;
        }
        if (R.id.micc_done == (int) tab.getTag()) {
            onOkPressed();
            return;
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
            NavUtils.navigateUpFromSameTask(getActivity());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView,
                                   HashMap<Long, ContactItem> contactItems) {
        createRecycleAdapter();
        if (contactItems.isEmpty()) {
            Snackbar.make(recyclerView,
                    "Please click refresh button to see the latest updated contacts",
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, null).show();
        }
    }

    private void showContactDetail(String id, ContactItem user) {
        ContactItem contactItem = contactItemSparseArray.get(Integer.valueOf(id));
        showContactDetail(contactItem, user);
    }

    private void showContactDetail(ContactItem contactItem, ContactItem user) {
        if (contactTopicListener != null)
            contactTopicListener.onIntellibitzContactTopicClicked(contactItem, user);
    }

    //    @Override
    public void onClick(View v) {
//        Log.d(TAG, "Clicked view: " + v);
        TextView dataId = (TextView) v.findViewById(R.id.tv_id);
        String id = dataId.getText().toString();
        showContactDetail(id, user);
/*
        if (twoPane) {
            Bundle arguments = new Bundle();
            arguments.putParcelable(ContactItem.TAG, user);
            arguments.putString(DeviceContactDetailFragment.ARG_ITEM_ID, holder.userEmailItem.getDataId());
            DeviceContactDetailFragment fragment = new DeviceContactDetailFragment();
            fragment.setArguments(arguments);
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.two_pane_empty_container, fragment)
                    .commit();
        } else {
            Context context = v.getContext();
            Intent intent = new Intent(context, ContactDetailActivity.class);
            intent.putExtra(ContactItem.TAG, (Parcelable) user);
            intent.putExtra(DeviceContactDetailFragment.ARG_ITEM_ID, holder.userEmailItem.getDataId());
            context.startActivity(intent);
        }
*/
    }

    private void setSelectedItems(ContactItem contactItem, boolean selected) {
        if (selected)
            selectedContactItems.add(contactItem);
        else
            selectedContactItems.remove(contactItem);
        final int count = selectedContactItems.size();
        setSubTitle(count);
//        setSelectedCount(count);

    }

    private void setSelectedCount(int count) {
        //        item2.setTitle(selectedContactItems.size()+" selected");
//        TabLayout.Tab tab = IntellibitzUserFragment.findTabAtByTag(detailTabLayoutFilter, R.id.micc_select);
//        setSubTitle(tab, count + " " + tab.getText());
//        detailTabLayoutFilter.requestLayout();
//        selectedItem = userEmailItem;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        restartLoader(query);
        return false;
    }

/*
    private ActionMode mActionMode;
    private MenuItem item2;
    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu_context_akcontact, menu);
            MenuItem item1 = menu.getBaseItem(0);
            item2 = menu.getBaseItem(1);
            MenuItem item3 = menu.getBaseItem(2);
            return true;
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_delete:
                    deleteMessages();
//                    shareCurrentItem();
                    mode.finish(); // Action picked, so close the CAB
                    return true;
                case R.id.menu_flag:
                    flagMessages();
//                    shareCurrentItem();
                    mode.finish(); // Action picked, so close the CAB
                    return true;
                case R.id.menu_unflag:
                    unflagMessages();
//                    shareCurrentItem();
                    mode.finish(); // Action picked, so close the CAB
                    return true;
                default:
                    return false;
            }
        }

        // Called when the user exits the action mode
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
        }
    };

*/

/*
    @Override
    public void onPostUnFlagMsgsExecute(JSONObject response, String[] item) {
        try {
            int status = response.getInt(MainApplicationSingleton.STATUS_PARAM);
//                ERROR
            if (1 == status) {
                Log.e(TAG, "UnFlag Msgs SUCCESS - " + response);
            } else if (99 == status) {
                onPostUnFlagMsgsExecute(response, item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPostUnFlagMsgsExecuteFail(JSONObject response, String[] item) {
        Log.e(TAG, "UnFlag Msgs FAIL - " + response + ": " + Arrays.toString(item));
    }

    @Override
    public void setUnFlagMsgsTaskToNull() {
        unflagMsgsTask = null;
    }

    @Override
    public void onPostFlagMsgsExecute(JSONObject response, String[] item) {
        try {
            int status = response.getInt(MainApplicationSingleton.STATUS_PARAM);
//                ERROR
            if (1 == status) {
                Log.e(TAG, "Flag Msgs SUCCESS - " + response);
            } else if (99 == status) {
                onPostFlagMsgsExecuteFail(response, item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPostFlagMsgsExecuteFail(JSONObject response, String[] item) {
        Log.e(TAG, "Flag Msgs FAIL - " + response + ": " + Arrays.toString(item));
    }

    @Override
    public void setFlagMsgsTaskToNull() {
        flagMsgsTask = null;
    }

    @Override
    public void onPostDeleteMsgsResponse(JSONObject response, String[] item) {
        try {
            int status = response.getInt(MainApplicationSingleton.STATUS_PARAM);
//                ERROR
            if (1 == status) {
                Log.e(TAG, "Delete Msgs SUCCESS - " + response);
                deleteMessagesInDB(item);
            } else if (99 == status) {
                onPostDeleteMsgsErrorResponse(response, item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void deleteMessagesInDB(String[] item) {
        try {
            int count = MessageContentProvider.deleteMsgs(item, getContext());
            Log.e(TAG, "Delete in DB: " + count);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPostDeleteMsgsErrorResponse(JSONObject response, String[] item) {
        Log.e(TAG, "Delete Msgs FAIL - " + response + ": " + Arrays.toString(item));
//        deleteMessagesInDB(item);
    }

    @Override
    public void setDeleteMsgsTaskToNull() {
        deleteMsgsTask = null;
    }
*/

    @Override
    public boolean onQueryTextChange(String newText) {
        restartLoader(newText);
        return false;
    }

    @Override
    public boolean onClose() {
        restartLoader("");
        return false;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This is called when a new Loader needs to be created.  This
        // sample only has one Loader, so we don't care about the ID.
        // First, pick the base URI to use depending on whether we are// currently filtering.
        if (MainApplicationSingleton.CONTACTSELECT_LOADERID == id) {
/*
            if (messageTopicListener != null) {
                messageTopicListener.onMessageTopicsLoaded(0);
                showEmpty("Please add Email Accounts to see conversations. You can select a Contact to send chat");
            }
*/
            // Now createFileInES and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
//        // TODO: 19-02-2016
//        to set the context right, else the loader will fail from the calling activity
//        return new CursorLoader(context, messageThreadUri,
            String selection = " ( name  IS NULL OR name like ? AND " +
                    " emails LIKE ? " +
                    " )";
            String[] selArgs;
            if (null == filter || filter.isEmpty()) {
                selArgs = new String[]{"%%", "%.com%"};
            } else {
                selArgs = new String[]{"%" + filter + "%", "%.com%"};
            }
//            Log.d(TAG, "FILTER: " + filter + " " + selArgs + " " + selection);
            return new CursorLoader(getActivity(),
                    DeviceContactContentProvider.CONTENT_URI,
                    null,
                    selection, selArgs,
                    ContactItemColumns.KEY_IS_INTELLIBITZ +
                            " DESC");
        }
//        returns an empty dummy cursor.. for the cursor loader to play game
        return new CursorLoader(getActivity(),
                DeviceContactContentProvider.CONTENT_URI,
                null,
                ContactItemColumns.KEY_DATA_ID + " = ? ",
                new String[]{"0"}, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (null == cursor) {
            if (null == contactItemSparseArray) contactItemSparseArray = new SparseArray<>();
            contactItemSparseArray.clear();
            createRecycleAdapter();
            return;
        }
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        if (MainApplicationSingleton.CONTACTSELECT_LOADERID == loader.getId()) {
            int count = cursor.getCount();
/*
            if (messageTopicListener != null) {
                messageTopicListener.onMessageTopicsLoaded(count);
            }
*/
//            if (0 == count)
//                showEmpty("Please add Email Accounts to see conversations. You can select a Contact to send chat");
            if (count > 0 && 0 == cursor.getPosition()) {
                if (null == contactItemSparseArray) contactItemSparseArray = new SparseArray<>();
                contactItemSparseArray.clear();
                fillItemsFromCursor(cursor);
                cursor.close();
                createRecycleAdapter();
            }
        }
    }

    public void fillItemsFromCursor(Cursor cursor) {
//                hideEmpty();
//                ArrayList<ContactItem> viewItemsMap = new ArrayList<>(count);
//                    ContactItem contactItem = new ContactItem();
        contactItemSparseArray =
                DeviceContactContentProvider.fillsDeviceContactItemFromCursor(cursor);
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        if (MainApplicationSingleton.CONTACTSELECT_LOADERID == loader.getId()) {
//            set cursor to null, to avoid memory leak
            //            refreshes view, for every data change.. this might not be required since adapter is
//            loaded on finish
            if (null == contactItemSparseArray) contactItemSparseArray = new SparseArray<>();
            contactItemSparseArray.clear();
            createRecycleAdapter();
        }
    }

    public class RecyclerViewAdapter extends
            RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> implements
            BitmapFromUrlTask.BitmapFromUrlTaskListener {

        private List<ContactItem> viewItems = new ArrayList<>();
        private BitmapFromUrlTask bitmapFromUrlTask;

        public RecyclerViewAdapter(List<ContactItem> items) {
            this.viewItems = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_contactselect_rv, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            final ContactItem contactItem = viewItems.get(position);
            holder.mItem = contactItem;
//            resets selection
            holder.ctv.setChecked(false);
            holder.tvId.setText(String.valueOf(holder.mItem.get_id()));
            String name = contactItem.getName();
//            sets the contact image
            holder.tvName.setCompoundDrawables(null, null, null, null);
            Drawable drawable = getDrawable(R.drawable.default_profile_thread,
                    getActivity().getTheme());
            drawable.setBounds(new Rect(0, 0, 100, 100));
            try {
                String pic = holder.mItem.getProfilePic();
                if (null == pic || pic.isEmpty()) {
                    if (name != null && name.length() > 0) {
//                        TextDrawable textDrawable = ColorGenerator.getTextDrawable(name);
//                        textDrawable.setBounds(new Rect(0, 0, 100, 100));
                        holder.tvName.setCompoundDrawables(
                                drawable, null, null, null);
                    }
                } else if (pic.startsWith("http")) {
//                    cloud pic
                    bitmapFromUrlTask = new BitmapFromUrlTask(
                            holder.tvName, pic, getActivity());
                    bitmapFromUrlTask.setBitmapFromUrlTaskListener(this);
                    bitmapFromUrlTask.execute();
/*
                    new AsyncGettingBitmapFromUrl(
                            holder.tvTo, pic, getActivity().getApplicationContext()).execute();
*/
                } else {
//                    db content pic
                    Bitmap bitmap = MainApplicationSingleton.getBitmapDecodeAnyUri(pic, getContext());
                    if (null == bitmap) {
//                        TextDrawable drawable = ColorGenerator.getTextDrawable(name);
//                        drawable.setBounds(new Rect(0, 0, 100, 100));
                        holder.tvName.setCompoundDrawables(drawable, null, null, null);
                    } else {
                        Bitmap croppedBitmap = NetworkImageView.getCroppedBitmap(bitmap, 100);
                        BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), croppedBitmap);
                        bitmapDrawable.setBounds(new Rect(0, 0, 100, 100));
                        holder.tvName.setCompoundDrawables(bitmapDrawable, null, null, null);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            holder.tvName.setText(name);
            ViewGroup rootView = (ViewGroup) holder.mView.getRootView();
            LinearLayout linearLayout = (LinearLayout)
                    rootView.findViewById(R.id.list_item_contactinfo);
//            clears old views
            linearLayout.removeAllViews();
            JSONArray emails = holder.mItem.getEmails();
            for (int i = 0; i < emails.length(); i++) {
                try {
                    String email = emails.getString(i);
                    View view = LayoutInflater.from(holder.mView.getContext()).inflate(
                            R.layout.list_item_contactinfo,
                            linearLayout, false);
                    TextView tv = (TextView) view.findViewById(R.id.tv_contactinfo);
                    tv.setText(email);
                    Drawable draw = getDrawable(R.drawable.ic_email_black_24dp,
                            getActivity().getTheme());
                    draw.setBounds(new Rect(0, 0, 40, 40));
                    tv.setCompoundDrawables(null, null, draw, null);
                    linearLayout.addView(view);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
/*
            for (final MobileItem item : mobiles) {
                View view = LayoutInflater.from(holder.mView.getContext()).inflate(
                        R.layout.list_item_contactinfo,
                        rootView, false);
                TextView tv = (TextView) view.findViewById(R.id.tv_contactinfo);
                tv.setText(item.getMobile());
*/
/*
                ImageView iv = (ImageView) view.findViewById(R.id.iv_contactinfo);
                iv.setImageDrawable(getDrawable(
                        R.drawable.ic_chat_black_24dp, getTheme()));
                Bitmap bm = MediaStore.Images.Media.getBitmap(getContentResolver(),
                        Uri.parse(pic));
*//*

                Drawable drawable = getDrawable(R.drawable.ic_chat_black_24dp,
                        getActivity().getTheme());
                assert drawable != null;
                drawable.setBounds(new Rect(0, 0, 80, 80));
                tv.setCompoundDrawablesRelative(null, null, drawable, null);
                tv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "Clicked view: " + v);
                        contactItem.setSelectedId(item.getIntellibitzId());
                        contactItem.setSelectedMobile(item);
                        Intent intent =
                                new Intent(MainApplicationSingleton.BROADCAST_CONTACT_PHONE_SELECTED);
                        intent.putExtra(MainApplicationSingleton.MOBILE_PARAM, item.getMobile());
                        intent.putExtra(ContactItem.TAG, (Parcelable) user);
                        intent.putExtra(ContactItem.DEVICE_CONTACT, (Parcelable) contactItem);
                        if (shared != null && shared.getExtras() != null) {
                            intent.putExtras(shared.getExtras());
                        }
                        LocalBroadcastManager.getInstance(
                                getActivity().getApplicationContext()).sendBroadcast(intent);
//                        finishes.. so doesn't show up in back stack
//                        finish();
                    }
                });
                linearLayout.addView(view);
            }
*/

        }

        @Override
        public int getItemCount() {
            return viewItems.size();
        }

        @Override
        public long getItemId(int position) {
            return viewItems.get(position).get_id();
        }

        @Override
        public void onPostBitmapFromUrlExecute(Bitmap bitmap, View textView, Context context) {
            if (null == context) {
                context = getContext();
            }
            if (null == context) return;
            Resources resources = context.getResources();
            if (null == resources) return;
            Bitmap roundedBitmap = NetworkImageView.getCroppedBitmap(bitmap, 100);
            BitmapDrawable drawable = new BitmapDrawable(resources, roundedBitmap);
            drawable.setBounds(new Rect(0, 0, 100, 100));
            ((TextView) textView).setCompoundDrawables(drawable, null, null, null);
        }

        @Override
        public void onPostBitmapFromUrlExecuteFail(Bitmap bitmap) {
            Log.e(TAG, "On Post Bitmap From URL Exec ERROR: " + bitmap);
        }

        @Override
        public void setBitmapFromUrlTaskToNull() {
            bitmapFromUrlTask = null;
        }

        public class ViewHolder extends
                RecyclerView.ViewHolder implements
                View.OnLongClickListener, View.OnClickListener {
            //            the views
            public final View mView;
            public final TextView tvName;
            public final TextView tvId;
            public final CheckBox ctv;
            //            the data
            public ContactItem mItem;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mView.setOnLongClickListener(this);
                mView.setOnClickListener(this);
//                mView.setOnClickListener(IntellibitzInviteUsersItemFragment.this);
                tvId = (TextView) view.findViewById(R.id.tv_id);
                tvName = (TextView) view.findViewById(R.id.tv_name);
                ctv = (CheckBox) view.findViewById(R.id.ctv_1);
                ctv.setOnClickListener(this);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + tvName.getText() + "'";
            }

            @Override
            public boolean onLongClick(View v) {
                if (v instanceof CheckBox) {
/*
                    if (null == mActionMode) {
                        // Start the CAB using the ActionMode.Callback defined above
//                        mActionMode = getActivity().startSupportActionMode(mActionModeCallback);
//                view.setSelected(true);
                        v.setSelected(!v.isSelected());
                        setSelectedItems(userEmailItem, ctv.isChecked());
//                        ctv.setChecked(!ctv.isChecked());
                        return true;
                    }
*/
                } else {
/*
                    if (null == mActionMode) {
                        // Start the CAB using the ActionMode.Callback defined above
//                        mActionMode = getActivity().startSupportActionMode(mActionModeCallback);
//                view.setSelected(true);
                        v.setSelected(!v.isSelected());
                        ctv.setChecked(!ctv.isChecked());
                        setSelectedItems(userEmailItem, ctv.isChecked());
                        return true;
                    }
*/
                    v.setSelected(!v.isSelected());
//                ctv.setChecked(v.isSelected());
                    ctv.setChecked(!ctv.isChecked());
                    setSelectedItems(mItem, ctv.isChecked());
                    return false;
                }
                setSelectedItems(mItem, ctv.isChecked());
                return false;
            }

            @Override
            public void onClick(View v) {
//                if (v instanceof CheckBox) return;
                onLongClick(v);
            }
        }
    }


}
