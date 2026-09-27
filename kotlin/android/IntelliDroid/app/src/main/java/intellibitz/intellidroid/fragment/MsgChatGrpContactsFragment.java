package intellibitz.intellidroid.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.text.util.Rfc822Token;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import intellibitz.intellidroid.IntellibitzActivityFragment;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.db.ContactItemColumns;
import intellibitz.intellidroid.graphics.ColorGenerator;
import intellibitz.intellidroid.graphics.TextDrawable;
import intellibitz.intellidroid.listener.ContactListener;
import intellibitz.intellidroid.listener.ContactsTopicListener;
import intellibitz.intellidroid.task.BitmapFromUrlTask;
import intellibitz.intellidroid.task.CreateGroupTask;
import intellibitz.intellidroid.task.GetGroupsTask;
import intellibitz.intellidroid.task.GroupDetailsTask;
import intellibitz.intellidroid.task.GroupsAddUsersTask;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.util.NetworkImageView;
import intellibitz.intellidroid.IntellibitzActivityFragment;
import intellibitz.intellidroid.R;
import intellibitz.intellidroid.activity.MsgChatGrpContactsDetailActivity;
import intellibitz.intellidroid.content.MsgChatGrpContactsContentProvider;
import intellibitz.intellidroid.content.task.GroupsSaveToDBTask;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.db.ContactItemColumns;
import intellibitz.intellidroid.graphics.ColorGenerator;
import intellibitz.intellidroid.graphics.TextDrawable;
import intellibitz.intellidroid.listener.ContactListener;
import intellibitz.intellidroid.listener.ContactsTopicListener;
import intellibitz.intellidroid.service.ContactService;
import intellibitz.intellidroid.task.BitmapFromUrlTask;
import intellibitz.intellidroid.task.CreateGroupTask;
import intellibitz.intellidroid.task.GetGroupsTask;
import intellibitz.intellidroid.task.GroupDetailsTask;
import intellibitz.intellidroid.task.GroupsAddUsersTask;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.util.NetworkImageView;
import intellibitz.intellidroid.widget.EditGroupDialogFragment;

import intellibitz.intellidroid.content.MsgChatGrpContactsContentProvider;
import intellibitz.intellidroid.service.ContactService;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.NavUtils;
import androidx.fragment.app.DialogFragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

/**
 *
 */
public class MsgChatGrpContactsFragment extends
        IntellibitzActivityFragment implements
        SearchView.OnQueryTextListener,
        SearchView.OnCloseListener,
        CreateGroupTask.CreateGroupTaskListener,
        GroupsAddUsersTask.GroupsAddUsersTaskListener,
        GroupsSaveToDBTask.GroupsSaveToDBTaskListener,
        View.OnClickListener,
        ContactListener,
        ContactsTopicListener,
        EditGroupDialogFragment.OnEditGroupDialogFragmentListener,
        LoaderManager.LoaderCallbacks<Cursor>,
        GroupDetailsTask.GroupDetailsTaskListener {
    //        Toolbar.OnMenuItemClickListener,
//        View.OnClickListener {
//        TabLayout.OnTabSelectedListener {
    public static final String TAG = "MsgChatGrpContactsFrag";

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;
    private ContentObserver contentObserver;

    private RecyclerView recyclerView;
    private Intent shared;

    private View view;
    private String onQueryTextSubmit;
    private ContactListener contactListFragment;
    private RecyclerViewAdapter viewAdapter;

    private FloatingActionButton fab;
    private Cursor cursor;
    private String filter;
    private ContactItem selectedItem;

    private List<ContactItem> contactItems;

    private GetGroupsTask getGroupsTask;
    private GroupDetailsTask groupDetailsTask;
    private CreateGroupTask createGroupTask;
    private GroupsAddUsersTask groupsAddUsersTask;
    private GroupsSaveToDBTask groupsSaveToDBTask;


    private ContactsTopicListener contactTopicListener;

    public static MsgChatGrpContactsFragment newInstance(ContactItem user, ContactListener contactListener) {
        MsgChatGrpContactsFragment fragment = new MsgChatGrpContactsFragment();
        Bundle args = new Bundle();
        args.putParcelable(ContactItem.USER_CONTACT, user);
        fragment.setContactListFragment(contactListener);
        if (contactListener instanceof ContactsTopicListener)
            fragment.setContactTopicListener((ContactsTopicListener) contactListener);
        fragment.setViewModeListener(contactListener);
        fragment.setUser(user);
        fragment.setArguments(args);
        return fragment;
    }

/*
    public void setIntellibitzActivity(IntellibitzActivity getActivity()) {
        super.setIntellibitzActivity(getActivity());
        shared = getActivity().getSharedIntent();
    }
*/

    public void setContactTopicListener(ContactsTopicListener contactTopicListener) {
        this.contactTopicListener = contactTopicListener;
    }

    @Override
    public void onViewModeChanged() {
        if (contactListFragment != null) {
            contactListFragment.onViewModeChanged();
        }
        super.onViewModeChanged();
    }

    @Override
    public void onViewModeItem() {
        if (viewModeListener != null) {
            viewModeListener.onViewModeItem();
        }
        super.onViewModeItem();
    }

    @Override
    public void setCreateGroupTaskToNull() {
        createGroupTask = null;
    }

    @Override
    public void onPostCreateGroupExecuteFail(JSONObject response,
                                             String name, File file, String[] contacts,
                                             ContactItem contactItem) {
//                ERROR
        Log.e(TAG, "CONTACTS GET ERROR - " + response);

    }

    @Override
    public void onPostCreateGroupExecute(JSONObject response,
                                         String name, File file, String[] contacts,
                                         ContactItem contactItem) {
        Log.e(TAG, "GROUPS GET SUCCESS - " + response);
        int status = 0;
        if (response != null)
            status = response.optInt("status");
        if (null == response || 99 == status || -1 == status) {
//            retries again..
            onPostCreateGroupExecuteFail(response, name, file, contacts, contactItem);
        } else {
            try {
                String id = response.getString("group_id");
                if (null == id || 0 == id.length()) {
//            retries again..
                } else {
                    savesGroup(name, file, contacts, id);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void setGroupsSaveToDBTaskToNull() {
        groupsSaveToDBTask = null;
    }

    @Override
    public void setGroupsAddUsersTaskToNull() {
        groupsAddUsersTask = null;
    }

    private void createNewGroup(ContactItem contactItem) {
        contactItem.setGroup(true);
        createGroupTask = new CreateGroupTask(contactItem, user.getDataId(), user.getToken(),
                user.getDevice(), user.getDeviceRef(), MainApplicationSingleton.AUTH_CREATE_GROUP);
        createGroupTask.setCreateGroupTaskListener(this);
        createGroupTask.execute();
    }

    private void createGroups(String[] contacts, String name) {
        createGroupTask = new CreateGroupTask(name, null, contacts, user.getDataId(), user.getToken(),
                user.getDevice(), user.getDeviceRef(), MainApplicationSingleton.AUTH_CREATE_GROUP);
        createGroupTask.setCreateGroupTaskListener(this);
        createGroupTask.execute();
    }

    public void savesGroup(String name, File file, String[] contacts, String id) {
        ContactItem contactThreadItem = new ContactItem();
//        data id, intellibitz id, type id and the group id are all the same id for a chat group
        contactThreadItem.setDataId(id);
        contactThreadItem.setIntellibitzId(id);
        contactThreadItem.setTypeId(id);
        contactThreadItem.setGroupId(id);
        contactThreadItem.setGroup(true);
        contactThreadItem.setEmailItem(false);
        contactThreadItem.setName(name);
        if (file != null)
            contactThreadItem.setProfilePic(file.getAbsolutePath());
        if (contacts != null && contacts.length > 0) {
            Collection<ContactItem> contactItems =
                    Collections.synchronizedSet(
                            new HashSet<ContactItem>(contacts.length));
            for (String contact : contacts) {
//                MobileItem mobileItem = new MobileItem(contact, contact, contact, "USER");
//                    // TODO: 20-06-2016
//                    to fetch mobile
//                MobileItem mobileItem = new MobileItem();
                ContactItem contactItem = new ContactItem();
                contactItem.setIntellibitzId(contact);
                contactItem.setDataId(contact);
//                IntellibitzContactItem intellibitzContactItem = new IntellibitzContactItem(contact);
//                intellibitzContactItem.setIntellibitzId(contact);
                contactItem.setGroup(true);
                contactItem.setType("GROUP");
                contactItem.setEmailItem(false);
                contactItems.add(contactItem);
            }
            contactThreadItem.setContactItems(contactItems);
        }
        contactThreadItem.setNewGroup(true);
        contactThreadItem.setGroup(true);
        contactThreadItem.setType("GROUP");
        contactItems.add(contactThreadItem);
//        user.getMsgContactItemHashMap().put(contactItem.getDataId(), contactItem);
        saveNewGroupsInDB(contactThreadItem, getActivity().getApplicationContext());
    }

    private void saveNewGroupsInDB(ContactItem contactItem, Context context) {
        groupsSaveToDBTask = new GroupsSaveToDBTask(contactItem, context);
        groupsSaveToDBTask.setGroupsSaveToDBTaskListener(this);
        groupsSaveToDBTask.execute();
    }

    private void saveExistingGroupInDB(ContactItem contactItem, Context context) {
        groupsSaveToDBTask = new GroupsSaveToDBTask(contactItem, context);
        groupsSaveToDBTask.setGroupsSaveToDBTaskListener(this);
        groupsSaveToDBTask.execute();
    }

    private void saveExistingGroupsInDB(Collection<ContactItem> contacts, Context context) {
        groupsSaveToDBTask = new GroupsSaveToDBTask(contacts, context);
        groupsSaveToDBTask.setGroupsSaveToDBTaskListener(this);
        groupsSaveToDBTask.execute();
    }

    @Override
    public void onPostGroupsSaveToDBExecute(Uri uri, Collection<ContactItem> contacts) {
        if (contacts != null && !contacts.isEmpty()) {
            Log.e(TAG, " GROUPS Contacts - SUCCESS - ");
            for (ContactItem item : contacts) {
                if (item.isNewGroup()) {
                    Collection<ContactItem> members = item.getContactItems();
                    if (null == members || members.isEmpty()) {
                    } else {
                        addUsersToGroups(item);
                    }
                } else {
//                gets group info
                    Collection<ContactItem> members = item.getContactItems();
                    if (null == members || members.isEmpty()) {
//                        only if members are already not retrieved
//                        group info is retrieved.. avoids recursive hell
                        getGroupDetails(item);
                    } else {
                    }
                }
            }
        } else {
            Log.e(TAG, "Group Save has returned EMPTY contacts - PLEASE CHECK: " + uri);
        }
    }

    private void getGroupDetails(ContactItem contactItem) {
        groupDetailsTask = new GroupDetailsTask(contactItem, user.getDataId(), user.getToken(),
                user.getDevice(), user.getDeviceRef(), MainApplicationSingleton.AUTH_GROUP_DETAILS);
        groupDetailsTask.setGroupDetailsTaskListener(this);
        groupDetailsTask.execute();
    }

    private void getGroupDetailsForNameUpdate(ContactItem contactItem) {
//        sets flag -1, to indicate only name update.. will be useful to handle post execute 
//        the flag will be returned in post execute
//        // TODO: 09-06-2016
        groupDetailsTask = new GroupDetailsTask(contactItem, -1, user.getDataId(), user.getToken(),
                user.getDevice(), user.getDeviceRef(), MainApplicationSingleton.AUTH_GROUP_DETAILS);
        groupDetailsTask.setGroupDetailsTaskListener(this);
        groupDetailsTask.execute();
    }

    @Override
    public void onPostGroupDetailsTaskExecute(JSONObject response, ContactItem item) {
        try {
            Uri uri = MsgChatGrpContactsContentProvider.updateGroupDetailsInDBFromJSON(
                    response, item, getActivity());
            if (uri != null) {
//                    group already saved.. but members freshly retrieved from cloud
//                    save groups with members again.. joins
//                    // TODO: 20-05-2016
//                    watch out for recursive loop
                saveExistingGroupInDB(item, getActivity());
/*
                long id = ContentUris.parseId(uri);
                if (0 == id) {
                }
*/
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onPostGroupDetailsTaskExecuteFail(JSONObject response, ContactItem item) {

    }

    @Override
    public void setGroupDetailsTaskToNull() {
        groupDetailsTask = null;
    }


    private void addUsersToGroups(ContactItem contactItem) {
        groupsAddUsersTask = new GroupsAddUsersTask(contactItem, user.getDataId(), user.getToken(),
                user.getDevice(), user.getDeviceRef(), MainApplicationSingleton.AUTH_GROUP_ADD_USERS);
        groupsAddUsersTask.setGroupsAddUsersTaskListener(this);
        groupsAddUsersTask.execute();
    }

    @Override
    public void onPostGroupsAddUsersExecuteFail(JSONObject response,
                                                String id, String name, String[] contacts,
                                                ContactItem contactItem) {
        Log.e(TAG, "onPostGroupsAddUsersExecuteFail: " + response);
    }

    @Override
    public void onPostGroupsAddUsersExecute(JSONObject response,
                                            String id, String name, String[] contacts,
                                            ContactItem contactItem) {
        int status = 0;
        if (response != null)
            status = response.optInt("status");
        if (null == response || 99 == status || -1 == status) {
//            retries again..
            onPostGroupsAddUsersExecuteFail(response, id, name, contacts, contactItem);
        } else {
            Log.e(TAG, " GROUPS ADD USERS - SUCCESS - ");
//            processNewMessageFromAction(id, name, user, getActivity().getApplicationContext());
            showAllGroups();
        }
    }

    private void showAllGroups() {
        if (selectedItem != null && selectedItem.isNewGroup()) {
            Log.e(TAG, " New Group: " + selectedItem);
            selectedItem = null;
            getActivity().onBackPressed();
        }
        createRecyclerAdapter();
    }


    public void showNewGroupDialog() {
        // Create an instance of the dialog fragment and show it
//        // TODO: 25-04-2016
//        move this to the respective item fragment
//        2 is for chat
        EditGroupDialogFragment.newMessageDialog(this, 2, user).show(
                getActivity().getSupportFragmentManager(), "NewMessageDialog");
    }


    public void onNewMenuClicked() {
//        showNewGroupDialog();
/*
        NewEmailDialogFragment.newMessageDialog(this, 0, user).show(
                getActivity().getSupportFragmentManager(), "NewMessageDialog");
*/
        selectedItem = new ContactItem();
        selectedItem.setNewGroup(true);
//        showContactThreadDetail();
        startMsgChatGrpContactsDetailActivity();
    }

    public void showContactThreadDetail() {
        startMsgChatGrpContactsDetailActivity();
/*
        GroupsDetailFragment msgChatGrpContactsDetailFragment =
                GroupsDetailFragment.newInstance(this, getActivity(), twoPane,
                        selectedItem, user);
//        msgChatGrpContactsDetailFragment.onOkPressed(null);
        getActivity().replaceDetailFragment(msgChatGrpContactsDetailFragment);
        return msgChatGrpContactsDetailFragment;
*/
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
    public void startMsgChatGrpContactsDetailActivity() {
        Intent intent = new Intent(getActivity(), MsgChatGrpContactsDetailActivity.class);
        intent.putExtra(ContactItem.TAG, (Parcelable) selectedItem);
        intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
        startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_MSGCHATGRPCONTACTS_RQ_CODE);
/*
        GroupsDetailFragment msgChatGrpContactsDetailFragment =
                GroupsDetailFragment.newInstance(this, getActivity(), twoPane,
                        selectedItem, user);
//        msgChatGrpContactsDetailFragment.onOkPressed(null);
        getActivity().replaceDetailFragment(msgChatGrpContactsDetailFragment);
        return msgChatGrpContactsDetailFragment;
*/
    }

    private void setupFAB(View view) {
        fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onNewMenuClicked();
            }
        });
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
                MsgChatGrpContactsContentProvider.CONTENT_URI, false,
                contentObserver);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(ContactItem.USER_CONTACT, user);
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_msgchatgrpcontacts, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        if (null == savedInstanceState) {
            user = getArguments().getParcelable(ContactItem.USER_CONTACT);
//            setupToolbar();
            setupSwipe(view);
            setupFAB(view);
        } else {
            user = savedInstanceState.getParcelable(ContactItem.USER_CONTACT);
        }
        if (MsgChatGrpContactsContentProvider.isGroupsEmptyInDB(getActivity())) {
//            getGroupsFromCloud();
            ContactService.getGroups(user, getActivity());
        }
        restartLoader();
    }

    private void restartLoader(String query) {
        filter = query;
        restartLoader();
    }

    private void restartLoader() {
        getActivity().getSupportLoaderManager().restartLoader(
                MainApplicationSingleton.GROUP_CONTACTITEM_FRAGMENT_LOADERID, null, this);
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
            Toolbar toolbar = getActivity().getToolbar();
            assert toolbar != null;
            toolbar.setTitle(R.string.toolbar_workcontacts_title);
        }
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
            NavUtils.navigateUpFromSameTask(getActivity());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void createRecyclerAdapter() {
        if (null == recyclerView && view != null)
            recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        if (null == recyclerView) return;
        if (null == contactItems) return;
        viewAdapter = new RecyclerViewAdapter(contactItems);
        viewAdapter.setHasStableIds(true);
        recyclerView.swapAdapter(viewAdapter, true);
//        recyclerView.scrollToPosition(viewAdapter.getItemCount() - 1);
        // TODO: 20-05-2016
//        comment this out
//        checksGroupInfo(contactItems);
//        checksGroupName();
    }

    public void checksGroupName() {
        if (null == contactItems || contactItems.isEmpty()) return;
        ContactItem[] items = contactItems.toArray(new ContactItem[0]);
        for (ContactItem item : items) {
            if (TextUtils.isEmpty(item.getName())) {
//                gets group info
                getGroupDetailsForNameUpdate(item);
            }
        }
    }

    public void checksGroupInfo(Collection<ContactItem> contactItems) {
        if (contactItems != null && !contactItems.isEmpty()) {
            for (ContactItem item : contactItems) {
                if (!item.isNewGroup()) {
//                gets group info
                    Collection<ContactItem> members = item.getContactItems();
                    if (null == members || members.isEmpty()) {
//                        only if members are already not retrieved
//                        group info is retrieved.. avoids recursive hell
                        getGroupDetails(item);
                    }
                }
            }
        }
    }

    public ContactListener getContactListFragment() {
        return contactListFragment;
    }

    public void setContactListFragment(ContactListener contactListFragment) {
        this.contactListFragment = contactListFragment;
    }

    private void showGroupChat(String id, ContactItem user) {
        ContactItem contactItem = MainApplicationSingleton.getBaseItem(id, contactItems);
        String name = contactItem.getName();
//        if name is not known yet, or failed to save in db before.. do it here
        if (null == name || name.isEmpty()) {
//            // TODO: 09-06-2016
//            do only name update in DB.. not the entire group
            try {
                getGroupDetailsForNameUpdate((ContactItem) contactItem.clone());
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }
        showGroupChat(contactItem, user);
    }

    private void showContactDetail(String id, ContactItem user) {
        ContactItem contactItem = MainApplicationSingleton.getBaseItem(
                id, contactItems);
        String name = contactItem.getName();
//        if name is not known yet, or failed to save in db before.. do it here
        if (null == name || name.isEmpty()) {
//            // TODO: 09-06-2016
//            do only name update in DB.. not the entire group
            try {
                getGroupDetailsForNameUpdate((ContactItem) contactItem.clone());
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }
        showContactDetail(contactItem, user);
    }

    private void showContactDetail(ContactItem contactItem, ContactItem user) {
        if (null == contactTopicListener) {

        } else {
            contactTopicListener.onContactsTopicClicked(contactItem, user);
        }
        selectedItem = contactItem;
        startMsgChatGrpContactsDetailActivity();
/*
        GroupsDetailFragment msgChatGrpContactsDetailFragment =
                GroupsDetailFragment.newInstance(this, getActivity(), twoPane,
                        contactItem, user);
//        hideFilterToolbar();
        getActivity().showDetailToolbar();
        getActivity().replaceDetailFragment(msgChatGrpContactsDetailFragment);
//        getActivity().replaceChildDetailFragment(msgChatGrpContactsDetailFragment, this);

*/
    }

    private void showGroupChat(ContactItem contactItem, ContactItem user) {
        if (null == contactTopicListener) {

        } else {
            contactTopicListener.onContactsTopicClicked(contactItem, user);
        }
        selectedItem = contactItem;
        Intent intent =
                new Intent(MainApplicationSingleton.BROADCAST_CONTACT_GROUP_SELECTED);
        intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
        intent.putExtra(ContactItem.TAG, (Parcelable) contactItem);
        if (shared != null && shared.getExtras() != null) {
            intent.putExtras(shared.getExtras());
        }
        LocalBroadcastManager.getInstance(
                getActivity().getApplicationContext()).sendBroadcast(intent);

    }

    public boolean onBackPressed() {
/*
        Intent back = getActivity().getBackIntent();
        if (null == back) return true;
        final String action = back.getAction();
        if (!ContactSelectFragment.TAG.equals(action)) return true;
        getActivity().setBackIntent(null);
//        showContactThreadDetail();
        startMsgChatGrpContactsDetailActivity();
*/
        return false;
/*
//        return onBackPressed(null);
        if (null == contactThreadDetailFragment) {
            return true;
        }
//            getActivity().replaceDetailFragment(contactThreadDetailFragment);
        if (contactThreadDetailFragment.onBackPressed()) {
            return true;
        }
        getActivity().replaceDetailFragment(contactThreadDetailFragment);
*/
    }

    public void onOkPressed(Intent intent) {
        if (null == intent) {
            Log.e(TAG, " cancelled selected contacts by pressing back: ");
            getActivity().onBackPressed();
            return;
        }
        ContactItem contactItem = intent.getParcelableExtra(
                ContactItem.TAG);
        String source = intent.getAction();
        if (null == contactItem) {
            Log.e(TAG, " cancelled selected contacts by pressing back: ");
            if (ContactSelectFragment.TAG.equals(source)) {
                if (selectedItem.isNewGroup()) {
                    startMsgChatGrpContactsDetailActivity();
/*
                    GroupsDetailFragment msgChatGrpContactsDetailFragment =
                            GroupsDetailFragment.newInstance(this, getActivity(), twoPane,
                                    selectedItem, user);
//                    getActivity().removeFragment(intellibitzContactItemFragment);
                    getActivity().removeFragment(msgChatGrpContactsDetailFragment);
//                    msgChatGrpContactsDetailFragment.onOkPressed(intent);
                    getActivity().replaceDetailFragment(msgChatGrpContactsDetailFragment);
*/
                    return;
                }
            }
            Log.e(TAG, " cancelled group by pressing back: ");
//            getActivity().onBackPressed();
            return;
        }
//        back button press.. will recreate state.. so the previously selected item , need to be
//        restored back from the back pressed intent from the clicked fragment
        if (null == selectedItem) selectedItem = contactItem;
        if (selectedItem == contactItem) {
            Log.e(TAG, " Already selected contacts: " +
                    selectedItem.getSelectedContacts().size());
        } else {
            selectedItem.setSelectedContacts(contactItem.getSelectedContacts());
        }
//            merges selected contacts into group contacts and clears selected contacts
        selectedItem.mergeSelectedContacts();
        int count = selectedItem.getContactItems().size();
//        checks group workflow first.. so the selection can continue for the group
        if (ContactSelectFragment.TAG.equals(source)) {
            if (selectedItem.isNewGroup()) {
                startMsgChatGrpContactsDetailActivity();
/*
                GroupsDetailFragment msgChatGrpContactsDetailFragment =
                        GroupsDetailFragment.newInstance(this, getActivity(), twoPane,
                                selectedItem, user);
//                getActivity().removeFragment(intellibitzContactItemFragment);
                getActivity().removeFragment(msgChatGrpContactsDetailFragment);
//                msgChatGrpContactsDetailFragment.onOkPressed(intent);
                getActivity().replaceDetailFragment(msgChatGrpContactsDetailFragment);
*/
                return;
            } else {
                if (count <= 1) {
//                Not sufficient contacts.. close the detail, and let the user choose again
//                    getActivity().onBackPressed();
                    return;
/*
            ContactItem msgContactItem =
                    selectedItem.getContactItems().iterator().next();
            IntellibitzContactItem intellibitzContactItem = msgContactItem.getIntellibitzContactItem();
            selectedItem.setDataId(intellibitzContactItem.getIntellibitzId());
            selectedItem.setName(intellibitzContactItem.getName());
//                createNewChat(contactItem, user);

*/
                }
            }
        }

/*
        if (count > 1) {
//                contactItem.setName(intellibitzContactItem.getName());
            String name = selectedItem.getName();
            if (null == name || name.isEmpty()) {
                Log.e(TAG, "Name is empty - cannot create group");
                getActivity().onBackPressed();
                return;
            }
//            getActivity().popFragment();
//            getActivity().onBackPressed();
            selectedItem.setGroup(true);
            createNewGroup(selectedItem);
            Log.e(TAG, " selected contacts for Group chat: " + count);
            return;
        }
*/
/*
        if (count <= 1) {
//                Not sufficient contacts.. close the detail, and let the user choose again
//            getActivity().onBackPressed();
            return;
*/
/*
            ContactItem msgContactItem =
                    selectedItem.getContactItems().iterator().next();
            IntellibitzContactItem intellibitzContactItem = msgContactItem.getIntellibitzContactItem();
            selectedItem.setDataId(intellibitzContactItem.getIntellibitzId());
            selectedItem.setName(intellibitzContactItem.getName());
//                createNewChat(contactItem, user);
*//*

        }
*/
//        Log.e(TAG, " selected contacts: " + selectedItem.getSelectedContacts().size());
//        getActivity().onBackPressed();
        restartLoader();
        return;
/*
        if (null == intent) {
//            if new group, on back pressed from anywhere with a null intent.. must remain in item
            if (null == selectedItem || selectedItem.isNewGroup()) {
                getActivity().onBackPressed();
                return;
            }
            Log.e(TAG, " cancelled selected contacts by pressing back: ");
//            getActivity().onBackPressed();
            contactThreadDetailFragment =
                    GroupsDetailFragment.newInstance(this, getActivity(), twoPane,
                            selectedItem, user);
            contactThreadDetailFragment.onOkPressed(intent);
            getActivity().replaceDetailFragment(contactThreadDetailFragment);
            return;
        }
*/
    }

/*
    public void onBackPressed(Intent intent) {
        onOkPressed(intent);
    }
*/

    @Override
    public void onClick(View v) {
//        Log.d(TAG, "Clicked view: " + v);
        TextView dataId = (TextView) v.findViewById(R.id.tv_id);
        String id = dataId.getText().toString();
//        edits group
//        showContactDetail(id, user);
//        group chat
        showGroupChat(id, user);

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

    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the NoticeDialogFragment.NoticeDialogListener interface
    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        // User touched the dialog's positive button
//        // TODO: 16-03-2016
//        user = dialog.getArguments().getParcelable(ContactItem.TAG);
//        getActivity().notifyUserBaseItemListeners();

//        user must be signed into atleast one email account

        EditGroupDialogFragment editGroupDialogFragment = (EditGroupDialogFragment) dialog;
        String subject = editGroupDialogFragment.getSubject();
        if (subject != null && subject.length() > 0) {

            Rfc822Token[] to = editGroupDialogFragment.getTo();
            int i = 0;
            String[] sto = new String[0];
            if (to != null && to.length > 0) {
                sto = new String[to.length];
                for (Rfc822Token s : to) {
                    String address = s.getAddress();
                    sto[i++] = address;
                }
            }
            Log.d(TAG, Arrays.toString(sto));
            if (1 == sto.length) {
//                processNewMessageFromAction(sto[0], subject, user, getActivity().getApplicationContext());
            } else if (sto.length > 1) {
                createGroups(sto, subject);
            }

/*
            ContactItem groupContactItem = new ContactItem();
            groupContactItem.setName(subject);
            createGroups(groupContactItem);
*/

/*
            MessageItem messageThreadItem = new MessageItem();
//        new message.. sets id to a constant.. global new message id
            messageThreadItem.setDataId(MessageItem.TAG);
            messageThreadItem.setDocType("THREAD");
            messageThreadItem.setDataRev("1");
            messageThreadItem.setFrom(user.getName());
            messageThreadItem.setSubject(subject);
            Rfc822Token[] to = editGroupDialogFragment.getTo();
            Rfc822Token[] cc = editGroupDialogFragment.getCc();
            Rfc822Token[] bcc = editGroupDialogFragment.getBcc();
*/
/*
        String to = newEmailDialogFragment.getTo();
        String cc = newEmailDialogFragment.getCc();
        String bcc = newEmailDialogFragment.getBcc();
*//*

            MessageItem.setMessageThreadEmailAddress(messageThreadItem, to, cc, bcc);
            messageThreadItem.setDocOwner(user.getDocOwner());
            messageThreadItem.setDocSender(user.getName());
            messageThreadItem.setDocOwnerEmail(user.getEmail());
            messageThreadItem.setDocSenderEmail(user.getEmail());
            messageThreadItem.setTimestamp(System.currentTimeMillis());
            Intent intent = new Intent();
            if (editGroupDialogFragment.isChatMode()) {
                intent.setAction(MainApplicationSingleton.BROADCAST_NEW_CHAT_DIALOG_OK);
            } else {
                intent.setAction(MainApplicationSingleton.BROADCAST_NEW_EMAIL_DIALOG_OK);
            }
            intent.putExtra(ContactItem.TAG, (Parcelable) user);
            intent.putExtra(MessageItem.TAG, (Parcelable) messageThreadItem);
            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
*/
        }
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        // User touched the dialog's negative button
    }

    @Override
    public void onContactsTopicClicked(ContactItem item, ContactItem user) {

    }

    @Override
    public void onContactsTopicsLoaded(int count) {

    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        restartLoader(query);
        return false;
    }

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
        if (MainApplicationSingleton.GROUP_CONTACTITEM_FRAGMENT_LOADERID == id) {
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
/*
//===========================================================================
//            IMPORTANT
//  JOIN QUERY - SO REMOVE AMBIGUITY BY SPECIFYING JOIN TABLE NAME
// GC AS PREFIX IS REQUIRED FOR THE QUERY TO WORK CORRECTLY
//===========================================================================
             */
            String selection = " ( name IS NULL OR name like ? ) "
                    + " AND ( " +
                    ContactItemColumns.KEY_IS_GROUP +
                    " = 1 ) AND ( " +
                    ContactItemColumns.KEY_IS_EMAIL +
                    " = 0 OR " +
                    ContactItemColumns.KEY_IS_EMAIL +
                    " IS NULL ) ";
/*
                    +
                    " AND ( ak." + ContactContentProvider.ContactItemColumns.KEY_IS_GROUP +
                    " = 0 OR " +
                    " ak." + ContactContentProvider.ContactItemColumns.KEY_IS_GROUP +
                    " IS NULL ) "
*/
/*
                    +
                    " AND ( ak." + MsgChatGrpContactsContentProvider.ContactItemColumns.KEY_IS_GROUP +
                    " = 1 OR " +
                    " ak." + MsgChatGrpContactsContentProvider.ContactItemColumns.KEY_IS_GROUP +
                    " IS NULL ) "
*/
            String[] selArgs;
            if (filter != null && !filter.isEmpty()) {
                selArgs = new String[]{"%" + filter + "%"};
            } else {
                selArgs = new String[]{"%%"};
            }
//            Log.d(TAG, "FILTER: " + filter + " " + selArgs + " " + selection);
            return new CursorLoader(getActivity(),
                    MsgChatGrpContactsContentProvider.CONTENT_URI,
                    null,
                    selection, selArgs,
                    ContactItemColumns.KEY_TIMESTAMP + " DESC");
        }
//        returns an empty dummy cursor.. for the cursor loader to play game
        return new CursorLoader(getActivity(),
                MsgChatGrpContactsContentProvider.CONTENT_URI,
                null,
                ContactItemColumns.KEY_DATA_ID + " = ? ",
                new String[]{"0"}, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        if (MainApplicationSingleton.GROUP_CONTACTITEM_FRAGMENT_LOADERID == loader.getId()) {
            if (null == cursor) {
                contactItems = null;
                createRecyclerAdapter();
                return;
            }
            int count = cursor.getCount();
/*
            if (messageTopicListener != null) {
                messageTopicListener.onMessageTopicsLoaded(count);
            }
*/
//            if (0 == count)
//                showEmpty("Please add Email Accounts to see conversations. You can select a Contact to send chat");
            if (0 == count) {
                contactItems = null;
                createRecyclerAdapter();
                return;
            }
            if (count > 0 && 0 == cursor.getPosition()) {
                contactItems = null;
                fillItemsFromCursor(cursor);
                cursor.close();
                createRecyclerAdapter();
            }
        }
    }

    public void fillItemsFromCursor(Cursor cursor) {
        this.contactItems =
                MsgChatGrpContactsContentProvider.createsContactsFromCursor(cursor);
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        if (MainApplicationSingleton.GROUP_CONTACTITEM_FRAGMENT_LOADERID == loader.getId()) {
            contactItems = null;
//            refreshes view, for every data change.. this might not be required since adapter is
//            loaded on finish
//            setIntellibitzContactItemHashMap(user.getMsgContactItemHashMap());
            createRecyclerAdapter();
        }
    }

    public void onTrimMemory(int level) {
        Log.d(TAG, "onTrimMemory: " + level);

    }

    public class RecyclerViewAdapter
            extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> implements
            BitmapFromUrlTask.BitmapFromUrlTaskListener {

        private List<ContactItem> viewItems;
        private BitmapFromUrlTask bitmapFromUrlTask;

        public RecyclerViewAdapter(List<ContactItem> items) {
            this.viewItems = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.group_item_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            final ContactItem contactItem = viewItems.get(position);
            holder.mItem = contactItem;
//            the collection is based on dataid.. so stores cloud id
            holder.tvId.setText(String.valueOf(holder.mItem.getDataId()));

            holder.tvName.setCompoundDrawablesRelative(null, null, null, null);
            HashSet<ContactItem> contactItems = contactItem.getContactItems();
//            Set<MobileItem> mobiles = new HashSet<>();
//            Set<EmailItem> emails = new HashSet<>();
/*
            if (null == name && !mobiles.isEmpty()) {
                name = mobiles.iterator().next().getMobile();
            }
            if (null == name && !emails.isEmpty()) {
                name = emails.iterator().next().getEmail();
            }
*/
            Drawable chatDrawable = getDrawable(R.drawable.ic_chat_black_24dp,
                    getActivity().getTheme());
            if (chatDrawable != null) {
                chatDrawable.setBounds(new Rect(0, 0, 100, 100));
            }
//            sets the contact image
            holder.tvName.setCompoundDrawables(null, null, null, null);
            String name = contactItem.getName();
            if (TextUtils.isEmpty(name)) name = "Group Name (not set)";
            try {
                String pic = holder.mItem.getProfilePic();
                if (null == pic || pic.isEmpty()) {
                    if (name.length() > 0) {
                        TextDrawable textDrawable = ColorGenerator.getTextDrawable(name);
                        textDrawable.setBounds(new Rect(0, 0, 100, 100));
                        holder.tvName.setCompoundDrawablesRelative(
                                textDrawable, null, chatDrawable, null);
                    }
                } else if (pic.startsWith("http")) {
//                    cloud pic
                    bitmapFromUrlTask = new BitmapFromUrlTask(
                            holder.tvName, pic, getActivity().getApplicationContext());
                    bitmapFromUrlTask.setBitmapFromUrlTaskListener(this);
                    bitmapFromUrlTask.execute();
                } else if (pic.startsWith("/")) {
//                    storage pic
                    Bitmap bm = BitmapFactory.decodeFile(pic);
                    Bitmap croppedBitmap = NetworkImageView.getCroppedBitmap(bm, 100);
                    Drawable drawable = new BitmapDrawable(getResources(), croppedBitmap);
                    drawable.setBounds(new Rect(0, 0, 100, 100));
                    holder.tvName.setCompoundDrawablesRelative(drawable, null, chatDrawable, null);
                } else if (pic.startsWith("content")) {
//                    db content pic
                    Bitmap bm = MediaStore.Images.Media.getBitmap(
                            getActivity().getContentResolver(), Uri.parse(pic));
                    Bitmap croppedBitmap = NetworkImageView.getCroppedBitmap(bm, 100);
                    BitmapDrawable drawable = new BitmapDrawable(getResources(), croppedBitmap);
                    drawable.setBounds(new Rect(0, 0, 100, 100));
                    holder.tvName.setCompoundDrawablesRelative(drawable, null, chatDrawable, null);
                } else {
                    Bitmap bm = MediaStore.Images.Media.getBitmap(
                            getActivity().getContentResolver(), Uri.parse(pic));
                    Bitmap croppedBitmap = NetworkImageView.getCroppedBitmap(bm, 100);
                    BitmapDrawable drawable = new BitmapDrawable(getResources(), croppedBitmap);
                    drawable.setBounds(new Rect(0, 0, 100, 100));
                    holder.tvName.setCompoundDrawablesRelative(drawable, null, chatDrawable, null);
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, e.getMessage());
            }
            holder.tvName.setText(name);
            ViewGroup rootView = (ViewGroup) holder.mView.getRootView();
            LinearLayout linearLayout = (LinearLayout)
                    rootView.findViewById(R.id.list_item_groupinfo);
//            clears old views
            linearLayout.removeAllViews();
            for (final ContactItem item : contactItems) {
                View view = LayoutInflater.from(holder.mView.getContext()).inflate(
                        R.layout.list_item_contactinfo,
                        rootView, false);
                TextView tv = (TextView) view.findViewById(R.id.tv_contactinfo);
                tv.setText(item.getName() + ":" + item.getTypeId());

                String pic = item.getProfilePic();
                BitmapDrawable picDrawable = null;
                try {
                    Bitmap bitmap = MainApplicationSingleton.getBitmapDecodeAnyUri(pic, getContext());
                    if (null == bitmap) {
/*
                        TextDrawable drawable = ColorGenerator.getTextDrawable(name);
                        drawable.setBounds(new Rect(0, 0, 100, 100));
                        holder.tvTo.setCompoundDrawablesRelative(drawable, null, null, null);
*/
                    } else {
                        Bitmap croppedBitmap = NetworkImageView.getCroppedBitmap(bitmap, 100);
                        picDrawable = new BitmapDrawable(getResources(), croppedBitmap);
                        picDrawable.setBounds(new Rect(0, 0, 100, 100));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, e.getMessage());
                }

                /*
                tv.setText(mobileItem.getMobile());
*/
/*
                ImageView iv = (ImageView) view.findViewById(R.id.iv_contactinfo);
                iv.setImageDrawable(getDrawable(
                        R.drawable.ic_chat_black_24dp, getTheme()));
                Bitmap bm = MediaStore.Images.Media.getBitmap(getContentResolver(),
                        Uri.parse(pic));
*/

                Drawable drawable = getDrawable(R.drawable.ic_chat_black_24dp,
                        getActivity().getTheme());
                assert drawable != null;
                drawable.setBounds(new Rect(0, 0, 40, 40));
//                tv.setCompoundDrawablesRelative(null, null, drawable, null);
                setCompoundDrawablesRelative(tv, picDrawable, null, drawable, null);
                tv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        Log.d(TAG, "Clicked view: " + v);
//                        contactItem.setSelectedId(item.getIntellibitzId());
//                        contactItem.setMobileItem(item);
                        Intent intent =
                                new Intent(MainApplicationSingleton.BROADCAST_CONTACT_PHONE_SELECTED);
                        intent.putExtra(MainApplicationSingleton.MOBILE_PARAM, (Parcelable) item);
                        intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
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

/*
            for (final IntellibitzContactItem item : contactItems) {
                final EmailItem emailItem = item.getEmailItem();
                View view = LayoutInflater.from(holder.mView.getContext()).inflate(
                        R.layout.list_item_contactinfo,
                        rootView, false);
                TextView tv = (TextView) view.findViewById(R.id.tv_contactinfo);
//                tv.setText(emailItem.getEmail());
*/
/*
                ImageView iv = (ImageView) view.findViewById(R.id.iv_contactinfo);
                iv.setImageDrawable(getDrawable(
                        R.drawable.ic_email_black_24dp, getTheme()));
*//*


                Drawable drawable = getDrawable(R.drawable.ic_email_black_24dp,
                        getActivity().getTheme());
                assert drawable != null;
                drawable.setBounds(new Rect(0, 0, 40, 40));
                tv.setCompoundDrawablesRelative(null, null, drawable, null);
                tv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "Clicked view: " + v);
                        Intent intent = new Intent(
                                MainApplicationSingleton.BROADCAST_CONTACT_EMAIL_SELECTED);
                        intent.putExtra(MainApplicationSingleton.EMAIL_PARAM, emailItem.getEmail());
                        intent.putExtra(ContactItem.DEVICE_CONTACT, (Parcelable) contactItem);
                        if (shared != null && shared.getExtras() != null) {
                            intent.putExtras(shared.getExtras());
                        }
                        LocalBroadcastManager.getInstance(
                                getActivity()).sendBroadcast(intent);
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

            Drawable chatDrawable = getDrawable(R.drawable.ic_chat_black_24dp,
                    getActivity().getTheme());
            if (chatDrawable != null) {
                chatDrawable.setBounds(new Rect(0, 0, 100, 100));
            }
            ((TextView) textView).setCompoundDrawablesRelative(drawable, null, chatDrawable, null);
        }

        @Override
        public void onPostBitmapFromUrlExecuteFail(Bitmap bitmap) {
            Log.e(TAG, "On Post Bitmap From URL Exec ERROR: " + bitmap);
        }

        @Override
        public void setBitmapFromUrlTaskToNull() {
            bitmapFromUrlTask = null;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            //            the views
            public final View mView;
            public final TextView tvName;
            public final TextView tvId;
            //            the data
            public ContactItem mItem;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mView.setOnClickListener(MsgChatGrpContactsFragment.this);
                tvId = (TextView) view.findViewById(R.id.tv_id);
                tvName = (TextView) view.findViewById(R.id.tv_name);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + tvName.getText() + "'";
            }
        }
    }


}
