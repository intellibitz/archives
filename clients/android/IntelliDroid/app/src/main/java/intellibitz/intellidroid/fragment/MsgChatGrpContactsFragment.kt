package intellibitz.intellidroid.fragment

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.database.ContentObserver
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Parcelable
import android.provider.MediaStore
import android.text.TextUtils
import android.text.util.Rfc822Token
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.appcompat.widget.SearchView
import androidx.core.app.NavUtils
import androidx.fragment.app.DialogFragment
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import intellibitz.intellidroid.IntellibitzActivityFragment
import intellibitz.intellidroid.R
import intellibitz.intellidroid.activity.MsgChatGrpContactsDetailActivity
import intellibitz.intellidroid.content.MsgChatGrpContactsContentProvider
import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.db.ContactItemColumns
import intellibitz.intellidroid.graphics.ColorGenerator
import intellibitz.intellidroid.graphics.TextDrawable
import intellibitz.intellidroid.listener.ContactListener
import intellibitz.intellidroid.listener.ContactsTopicListener
import intellibitz.intellidroid.service.ContactService
import intellibitz.intellidroid.task.BitmapFromUrlTask
import intellibitz.intellidroid.task.CreateGroupTask
import intellibitz.intellidroid.task.GetGroupsTask
import intellibitz.intellidroid.task.GroupDetailsTask
import intellibitz.intellidroid.task.GroupsAddUsersTask
import intellibitz.intellidroid.util.MainApplicationSingleton
import intellibitz.intellidroid.util.NetworkImageView
import intellibitz.intellidroid.widget.EditGroupDialogFragment
import intellibitz.intellidroid.content.task.GroupsSaveToDBTask
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.Arrays
import java.util.Collection
import java.util.Collections
import java.util.HashSet
import java.util.List

/**
 *
 */
class MsgChatGrpContactsFragment : IntellibitzActivityFragment(),
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

    companion object {
        const val TAG = "MsgChatGrpContactsFrag"

        /**
         * Id to identity READ_CONTACTS permission request.
         */
        private const val REQUEST_READ_CONTACTS = 0

        fun newInstance(user: ContactItem?, contactListener: ContactListener?): MsgChatGrpContactsFragment {
            val fragment = MsgChatGrpContactsFragment()
            val args = Bundle()
            args.putParcelable(ContactItem.USER_CONTACT, user)
            fragment.setContactListFragment(contactListener)
            if (contactListener is ContactsTopicListener) {
                fragment.setContactTopicListener(contactListener)
            }
            fragment.setViewModeListener(contactListener)
            fragment.setUser(user)
            fragment.arguments = args
            return fragment
        }
    }

    private var contentObserver: ContentObserver? = null
    private var recyclerView: RecyclerView? = null
    private var shared: Intent? = null
    private var view: View? = null
    private var onQueryTextSubmit: String? = null
    private var contactListFragment: ContactListener? = null
    private var viewAdapter: RecyclerViewAdapter? = null
    private var fab: FloatingActionButton? = null
    private var cursor: Cursor? = null
    private var filter: String? = null
    private var selectedItem: ContactItem? = null
    private var contactItems: List<ContactItem>? = null
    private var getGroupsTask: GetGroupsTask? = null
    private var groupDetailsTask: GroupDetailsTask? = null
    private var createGroupTask: CreateGroupTask? = null
    private var groupsAddUsersTask: GroupsAddUsersTask? = null
    private var groupsSaveToDBTask: GroupsSaveToDBTask? = null
    private var contactTopicListener: ContactsTopicListener? = null

    fun setContactTopicListener(contactTopicListener: ContactsTopicListener?) {
        this.contactTopicListener = contactTopicListener
    }

    override fun onViewModeChanged() {
        contactListFragment?.onViewModeChanged()
        super.onViewModeChanged()
    }

    override fun onViewModeItem() {
        viewModeListener?.onViewModeItem()
        super.onViewModeItem()
    }

    override fun setCreateGroupTaskToNull() {
        createGroupTask = null
    }

    override fun onPostCreateGroupExecuteFail(response: JSONObject?,
                                              name: String?, file: File?, contacts: Array<String>?,
                                              contactItem: ContactItem?) {
        // ERROR
        Log.e(TAG, "CONTACTS GET ERROR - $response")
    }

    override fun onPostCreateGroupExecute(response: JSONObject?,
                                         name: String?, file: File?, contacts: Array<String>?,
                                         contactItem: ContactItem?) {
        Log.e(TAG, "GROUPS GET SUCCESS - $response")
        var status = 0
        if (response != null) {
            status = response.optInt("status")
        }
        if (response == null || 99 == status || -1 == status) {
            // retries again..
            onPostCreateGroupExecuteFail(response, name, file, contacts, contactItem)
        } else {
            try {
                val id = response.getString("group_id")
                if (id == null || id.isEmpty()) {
                    // retries again..
                } else {
                    savesGroup(name, file, contacts, id)
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }

    override fun setGroupsSaveToDBTaskToNull() {
        groupsSaveToDBTask = null
    }

    override fun setGroupsAddUsersTaskToNull() {
        groupsAddUsersTask = null
    }

    private fun createNewGroup(contactItem: ContactItem) {
        contactItem.isGroup = true
        createGroupTask = CreateGroupTask(contactItem, user.dataId, user.token,
            user.device, user.deviceRef, MainApplicationSingleton.AUTH_CREATE_GROUP)
        createGroupTask?.setCreateGroupTaskListener(this)
        createGroupTask?.execute()
    }

    private fun createGroups(contacts: Array<String>?, name: String?) {
        createGroupTask = CreateGroupTask(name, null, contacts, user.dataId, user.token,
            user.device, user.deviceRef, MainApplicationSingleton.AUTH_CREATE_GROUP)
        createGroupTask?.setCreateGroupTaskListener(this)
        createGroupTask?.execute()
    }

    fun savesGroup(name: String?, file: File?, contacts: Array<String>?, id: String) {
        val contactThreadItem = ContactItem()
        // data id, intellibitz id, type id and the group id are all the same id for a chat group
        contactThreadItem.dataId = id
        contactThreadItem.intellibitzId = id
        contactThreadItem.typeId = id
        contactThreadItem.groupId = id
        contactThreadItem.isGroup = true
        contactThreadItem.isEmailItem = false
        contactThreadItem.name = name
        if (file != null) {
            contactThreadItem.profilePic = file.absolutePath
        }
        if (contacts != null && contacts.isNotEmpty()) {
            val contactItems: Collection<ContactItem> =
                Collections.synchronizedSet(HashSet<ContactItem>(contacts.size))
            for (contact in contacts) {
                // MobileItem mobileItem = new MobileItem(contact, contact, contact, "USER");
                // // TODO: 20-06-2016
                // to fetch mobile
                // MobileItem mobileItem = new MobileItem();
                val contactItem = ContactItem()
                contactItem.intellibitzId = contact
                contactItem.dataId = contact
                // IntellibitzContactItem intellibitzContactItem = new IntellibitzContactItem(contact);
                // intellibitzContactItem.setIntellibitzId(contact);
                contactItem.isGroup = true
                contactItem.type = "GROUP"
                contactItem.isEmailItem = false
                contactItems.add(contactItem)
            }
            contactThreadItem.contactItems = contactItems
        }
        contactThreadItem.isNewGroup = true
        contactThreadItem.isGroup = true
        contactThreadItem.type = "GROUP"
        contactItems?.add(contactThreadItem)
        // user.getMsgContactItemHashMap().put(contactItem.getDataId(), contactItem);
        saveNewGroupsInDB(contactThreadItem, requireActivity().applicationContext)
    }

    private fun saveNewGroupsInDB(contactItem: ContactItem, context: Context) {
        groupsSaveToDBTask = GroupsSaveToDBTask(contactItem, context)
        groupsSaveToDBTask?.setGroupsSaveToDBTaskListener(this)
        groupsSaveToDBTask?.execute()
    }

    private fun saveExistingGroupInDB(contactItem: ContactItem, context: Context) {
        groupsSaveToDBTask = GroupsSaveToDBTask(contactItem, context)
        groupsSaveToDBTask?.setGroupsSaveToDBTaskListener(this)
        groupsSaveToDBTask?.execute()
    }

    private fun saveExistingGroupsInDB(contacts: Collection<ContactItem>, context: Context) {
        groupsSaveToDBTask = GroupsSaveToDBTask(contacts, context)
        groupsSaveToDBTask?.setGroupsSaveToDBTaskListener(this)
        groupsSaveToDBTask?.execute()
    }

    override fun onPostGroupsSaveToDBExecute(uri: Uri?, contacts: Collection<ContactItem>?) {
        if (contacts != null && contacts.isNotEmpty()) {
            Log.e(TAG, " GROUPS Contacts - SUCCESS - ")
            for (item in contacts) {
                if (item.isNewGroup) {
                    val members = item.contactItems
                    if (members == null || members.isEmpty()) {
                    } else {
                        addUsersToGroups(item)
                    }
                } else {
                    // gets group info
                    val members = item.contactItems
                    if (members == null || members.isEmpty()) {
                        // only if members are already not retrieved
                        // group info is retrieved.. avoids recursive hell
                        getGroupDetails(item)
                    } else {
                    }
                }
            }
        } else {
            Log.e(TAG, "Group Save has returned EMPTY contacts - PLEASE CHECK: $uri")
        }
    }

    private fun getGroupDetails(contactItem: ContactItem) {
        groupDetailsTask = GroupDetailsTask(contactItem, user.dataId, user.token,
            user.device, user.deviceRef, MainApplicationSingleton.AUTH_GROUP_DETAILS)
        groupDetailsTask?.setGroupDetailsTaskListener(this)
        groupDetailsTask?.execute()
    }

    private fun getGroupDetailsForNameUpdate(contactItem: ContactItem) {
        // sets flag -1, to indicate only name update.. will be useful to handle post execute
        // the flag will be returned in post execute
        // // TODO: 09-06-2016
        groupDetailsTask = GroupDetailsTask(contactItem, -1, user.dataId, user.token,
            user.device, user.deviceRef, MainApplicationSingleton.AUTH_GROUP_DETAILS)
        groupDetailsTask?.setGroupDetailsTaskListener(this)
        groupDetailsTask?.execute()
    }

    override fun onPostGroupDetailsTaskExecute(response: JSONObject?, item: ContactItem) {
        try {
            val uri = MsgChatGrpContactsContentProvider.updateGroupDetailsInDBFromJSON(
                response, item, requireActivity())
            if (uri != null) {
                // group already saved.. but members freshly retrieved from cloud
                // save groups with members again.. joins
                // // TODO: 20-05-2016
                // watch out for recursive loop
                saveExistingGroupInDB(item, requireActivity())
                /*
                long id = ContentUris.parseId(uri);
                if (0 == id) {
                }
                */
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    override fun onPostGroupDetailsTaskExecuteFail(response: JSONObject?, item: ContactItem) {
    }

    override fun setGroupDetailsTaskToNull() {
        groupDetailsTask = null
    }

    private fun addUsersToGroups(contactItem: ContactItem) {
        groupsAddUsersTask = GroupsAddUsersTask(contactItem, user.dataId, user.token,
            user.device, user.deviceRef, MainApplicationSingleton.AUTH_GROUP_ADD_USERS)
        groupsAddUsersTask?.setGroupsAddUsersTaskListener(this)
        groupsAddUsersTask?.execute()
    }

    override fun onPostGroupsAddUsersExecuteFail(response: JSONObject?,
                                                 id: String?, name: String?, contacts: Array<String>?,
                                                 contactItem: ContactItem?) {
        Log.e(TAG, "onPostGroupsAddUsersExecuteFail: $response")
    }

    override fun onPostGroupsAddUsersExecute(response: JSONObject?,
                                            id: String?, name: String?, contacts: Array<String>?,
                                            contactItem: ContactItem?) {
        var status = 0
        if (response != null) {
            status = response.optInt("status")
        }
        if (response == null || 99 == status || -1 == status) {
            // retries again..
            onPostGroupsAddUsersExecuteFail(response, id, name, contacts, contactItem)
        } else {
            Log.e(TAG, " GROUPS ADD USERS - SUCCESS - ")
            // processNewMessageFromAction(id, name, user, getActivity().getApplicationContext());
            showAllGroups()
        }
    }

    private fun showAllGroups() {
        if (selectedItem != null && selectedItem!!.isNewGroup) {
            Log.e(TAG, " New Group: $selectedItem")
            selectedItem = null
            requireActivity().onBackPressed()
        }
        createRecyclerAdapter()
    }

    fun showNewGroupDialog() {
        // Create an instance of the dialog fragment and show it
        // // TODO: 25-04-2016
        // move this to the respective item fragment
        // 2 is for chat
        EditGroupDialogFragment.newMessageDialog(this, 2, user).show(
            requireActivity().supportFragmentManager, "NewMessageDialog")
    }

    fun onNewMenuClicked() {
        // showNewGroupDialog();
        /*
        NewEmailDialogFragment.newMessageDialog(this, 0, user).show(
                getActivity().getSupportFragmentManager(), "NewMessageDialog");
        */
        selectedItem = ContactItem()
        selectedItem!!.isNewGroup = true
        // showContactThreadDetail();
        startMsgChatGrpContactsDetailActivity()
    }

    fun showContactThreadDetail() {
        startMsgChatGrpContactsDetailActivity()
        /*
        GroupsDetailFragment msgChatGrpContactsDetailFragment =
                GroupsDetailFragment.newInstance(this, getActivity(), twoPane,
                        selectedItem, user);
        // msgChatGrpContactsDetailFragment.onOkPressed(null);
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
    fun startMsgChatGrpContactsDetailActivity() {
        val intent = Intent(requireActivity(), MsgChatGrpContactsDetailActivity::class.java)
        intent.putExtra(ContactItem.TAG, selectedItem as Parcelable)
        intent.putExtra(ContactItem.USER_CONTACT, user as Parcelable)
        startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_MSGCHATGRPCONTACTS_RQ_CODE)
        /*
        GroupsDetailFragment msgChatGrpContactsDetailFragment =
                GroupsDetailFragment.newInstance(this, getActivity(), twoPane,
                        selectedItem, user);
        // msgChatGrpContactsDetailFragment.onOkPressed(null);
        getActivity().replaceDetailFragment(msgChatGrpContactsDetailFragment);
        return msgChatGrpContactsDetailFragment;
        */
    }

    private fun setupFAB(view: View) {
        fab = view.findViewById(R.id.fab)
        fab?.setOnClickListener { onNewMenuClicked() }
    }

    override fun onDestroy() {
        requireActivity().contentResolver.unregisterContentObserver(contentObserver)
        super.onDestroy()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        contentObserver = object : ContentObserver(Handler()) {
            override fun deliverSelfNotifications(): Boolean {
                return super.deliverSelfNotifications()
            }

            override fun onChange(selfChange: Boolean) {
                onChange(selfChange, null)
            }

            override fun onChange(selfChange: Boolean, uri: Uri?) {
                restartLoader()
            }
        }
        requireActivity().contentResolver.registerContentObserver(
            MsgChatGrpContactsContentProvider.CONTENT_URI, false,
            contentObserver)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(ContactItem.USER_CONTACT, user)
        super.onSaveInstanceState(outState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                             savedInstanceState: Bundle?): View? {
        view = inflater.inflate(R.layout.fragment_msgchatgrpcontacts, container, false)
        return view
    }

    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.recyclerview)
        if (savedInstanceState == null) {
            user = requireArguments().getParcelable(ContactItem.USER_CONTACT)
            // setupToolbar();
            setupSwipe(view)
            setupFAB(view)
        } else {
            user = savedInstanceState.getParcelable(ContactItem.USER_CONTACT)
        }
        if (MsgChatGrpContactsContentProvider.isGroupsEmptyInDB(requireActivity())) {
            // getGroupsFromCloud();
            ContactService.getGroups(user, requireActivity())
        }
        restartLoader()
    }

    private fun restartLoader(query: String) {
        filter = query
        restartLoader()
    }

    private fun restartLoader() {
        requireActivity().supportLoaderManager.restartLoader(
            MainApplicationSingleton.GROUP_CONTACTITEM_FRAGMENT_LOADERID, null, this)
    }

    fun setupSwipe(view: View) {
        val refreshLayout = view.findViewById<SwipeRefreshLayout>(R.id.swiperefresh)
        refreshLayout?.setOnRefreshListener {
            // Post a delayed runnable to reset the refreshing state in 2 seconds
            Handler().postDelayed({
                refreshLayout.isRefreshing = false
            }, 2000)
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            NavUtils.navigateUpFromSameTask(requireActivity())
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    fun createRecyclerAdapter() {
        if (recyclerView == null && view != null) {
            recyclerView = view?.findViewById(R.id.recyclerview)
        }
        if (recyclerView == null) return
        if (contactItems == null) return
        viewAdapter = RecyclerViewAdapter(contactItems!!)
        viewAdapter?.setHasStableIds(true)
        recyclerView?.swapAdapter(viewAdapter, true)
        // recyclerView.scrollToPosition(viewAdapter.getItemCount() - 1);
        // // TODO: 20-05-2016
        // comment this out
        // checksGroupInfo(contactItems);
        // checksGroupName();
    }

    fun checksGroupName() {
        if (contactItems == null || contactItems!!.isEmpty()) return
        val items = contactItems!!.toTypedArray()
        for (item in items) {
            if (TextUtils.isEmpty(item.name)) {
                // gets group info
                getGroupDetailsForNameUpdate(item)
            }
        }
    }

    fun checksGroupInfo(contactItems: Collection<ContactItem>) {
        if (contactItems.isNotEmpty()) {
            for (item in contactItems) {
                if (!item.isNewGroup) {
                    // gets group info
                    val members = item.contactItems
                    if (members == null || members.isEmpty()) {
                        // only if members are already not retrieved
                        // group info is retrieved.. avoids recursive hell
                        getGroupDetails(item)
                    }
                }
            }
        }
    }

    fun getContactListFragment(): ContactListener? {
        return contactListFragment
    }

    fun setContactListFragment(contactListFragment: ContactListener?) {
        this.contactListFragment = contactListFragment
    }

    private fun showGroupChat(id: String, user: ContactItem) {
        val contactItem = MainApplicationSingleton.getBaseItem(id, contactItems)
        var name = contactItem.name
        // if name is not known yet, or failed to save in db before.. do it here
        if (name == null || name.isEmpty()) {
            // // TODO: 09-06-2016
            // do only name update in DB.. not the entire group
            try {
                getGroupDetailsForNameUpdate(contactItem.clone() as ContactItem)
            } catch (e: CloneNotSupportedException) {
                e.printStackTrace()
            }
        }
        showGroupChat(contactItem, user)
    }

    private fun showContactDetail(id: String, user: ContactItem) {
        val contactItem = MainApplicationSingleton.getBaseItem(
            id, contactItems)
        var name = contactItem.name
        // if name is not known yet, or failed to save in db before.. do it here
        if (name == null || name.isEmpty()) {
            // // TODO: 09-06-2016
            // do only name update in DB.. not the entire group
            try {
                getGroupDetailsForNameUpdate(contactItem.clone() as ContactItem)
            } catch (e: CloneNotSupportedException) {
                e.printStackTrace()
            }
        }
        showContactDetail(contactItem, user)
    }

    private fun showContactDetail(contactItem: ContactItem, user: ContactItem) {
        if (contactTopicListener == null) {
        } else {
            contactTopicListener?.onContactsTopicClicked(contactItem, user)
        }
        selectedItem = contactItem
        startMsgChatGrpContactsDetailActivity()
        /*
        GroupsDetailFragment msgChatGrpContactsDetailFragment =
                GroupsDetailFragment.newInstance(this, getActivity(), twoPane,
                        contactItem, user);
        // hideFilterToolbar();
        getActivity().showDetailToolbar();
        getActivity().replaceDetailFragment(msgChatGrpContactsDetailFragment);
        // getActivity().replaceChildDetailFragment(msgChatGrpContactsDetailFragment, this);
        */
    }

    private fun showGroupChat(contactItem: ContactItem, user: ContactItem) {
        if (contactTopicListener == null) {
        } else {
            contactTopicListener?.onContactsTopicClicked(contactItem, user)
        }
        selectedItem = contactItem
        val intent =
            Intent(MainApplicationSingleton.BROADCAST_CONTACT_GROUP_SELECTED)
        intent.putExtra(ContactItem.USER_CONTACT, user as Parcelable)
        intent.putExtra(ContactItem.TAG, contactItem as Parcelable)
        if (shared != null && shared!!.extras != null) {
            intent.putExtras(shared!!.extras!!)
        }
        LocalBroadcastManager.getInstance(
            requireActivity().applicationContext).sendBroadcast(intent)
    }

    fun onBackPressed(): Boolean {
        /*
        Intent back = getActivity().getBackIntent();
        if (null == back) return true;
        final String action = back.getAction();
        if (!ContactSelectFragment.TAG.equals(action)) return true;
        getActivity().setBackIntent(null);
        // showContactThreadDetail();
        startMsgChatGrpContactsDetailActivity();
        */
        return false
        /*
        // return onBackPressed(null);
        if (null == contactThreadDetailFragment) {
            return true;
        }
        // getActivity().replaceDetailFragment(contactThreadDetailFragment);
        if (contactThreadDetailFragment.onBackPressed()) {
            return true;
        }
        getActivity().replaceDetailFragment(contactThreadDetailFragment);
        */
    }

    fun onOkPressed(intent: Intent?) {
        if (intent == null) {
            Log.e(TAG, " cancelled selected contacts by pressing back: ")
            requireActivity().onBackPressed()
            return
        }
        val contactItem = intent.getParcelableExtra<ContactItem>(
            ContactItem.TAG)
        val source = intent.action
        if (contactItem == null) {
            Log.e(TAG, " cancelled selected contacts by pressing back: ")
            if (ContactSelectFragment.TAG == source) {
                if (selectedItem!!.isNewGroup) {
                    startMsgChatGrpContactsDetailActivity()
                    /*
                    GroupsDetailFragment msgChatGrpContactsDetailFragment =
                            GroupsDetailFragment.newInstance(this, getActivity(), twoPane,
                                    selectedItem, user);
                    // getActivity().removeFragment(intellibitzContactItemFragment);
                    getActivity().removeFragment(msgChatGrpContactsDetailFragment);
                    // msgChatGrpContactsDetailFragment.onOkPressed(intent);
                    getActivity().replaceDetailFragment(msgChatGrpContactsDetailFragment);
                    */
                    return
                }
            }
            Log.e(TAG, " cancelled group by pressing back: ")
            // getActivity().onBackPressed();
            return
        }
        // back button press.. will recreate state.. so the previously selected item , need to be
        // restored back from the back pressed intent from the clicked fragment
        if (selectedItem == null) selectedItem = contactItem
        if (selectedItem == contactItem) {
            Log.e(TAG, " Already selected contacts: " +
                selectedItem!!.selectedContacts.size)
        } else {
            selectedItem!!.selectedContacts = contactItem.selectedContacts
        }
        // merges selected contacts into group contacts and clears selected contacts
        selectedItem!!.mergeSelectedContacts()
        val count = selectedItem!!.contactItems.size
        // checks group workflow first.. so the selection can continue for the group
        if (ContactSelectFragment.TAG == source) {
            if (selectedItem!!.isNewGroup) {
                startMsgChatGrpContactsDetailActivity()
                /*
                GroupsDetailFragment msgChatGrpContactsDetailFragment =
                        GroupsDetailFragment.newInstance(this, getActivity(), twoPane,
                                selectedItem, user);
                // getActivity().removeFragment(intellibitzContactItemFragment);
                getActivity().removeFragment(msgChatGrpContactsDetailFragment);
                // msgChatGrpContactsDetailFragment.onOkPressed(intent);
                getActivity().replaceDetailFragment(msgChatGrpContactsDetailFragment);
                */
                return
            } else {
                if (count <= 1) {
                    // Not sufficient contacts.. close the detail, and let the user choose again
                    // getActivity().onBackPressed();
                    return
                    /*
                    ContactItem msgContactItem =
                            selectedItem.getContactItems().iterator().next();
                    IntellibitzContactItem intellibitzContactItem = msgContactItem.getIntellibitzContactItem();
                    selectedItem.setDataId(intellibitzContactItem.getIntellibitzId());
                    selectedItem.setName(intellibitzContactItem.getName());
                    // createNewChat(contactItem, user);
                    */
                }
            }
        }

        /*
        if (count > 1) {
            // contactItem.setName(intellibitzContactItem.getName());
            String name = selectedItem.getName();
            if (null == name || name.isEmpty()) {
                Log.e(TAG, "Name is empty - cannot create group");
                getActivity().onBackPressed();
                return;
            }
            // getActivity().popFragment();
            // getActivity().onBackPressed();
            selectedItem.setGroup(true);
            createNewGroup(selectedItem);
            Log.e(TAG, " selected contacts for Group chat: " + count);
            return;
        }
        */
        /*
        if (count <= 1) {
            // Not sufficient contacts.. close the detail, and let the user choose again
            // getActivity().onBackPressed();
            return;
            /*
            ContactItem msgContactItem =
                    selectedItem.getContactItems().iterator().next();
            IntellibitzContactItem intellibitzContactItem = msgContactItem.getIntellibitzContactItem();
            selectedItem.setDataId(intellibitzContactItem.getIntellibitzId());
            selectedItem.setName(intellibitzContactItem.getName());
            // createNewChat(contactItem, user);
            */
        }
        */
        // Log.e(TAG, " selected contacts: " + selectedItem.getSelectedContacts().size());
        // getActivity().onBackPressed();
        restartLoader()
        return
        /*
        if (null == intent) {
            // if new group, on back pressed from anywhere with a null intent.. must remain in item
            if (null == selectedItem || selectedItem.isNewGroup()) {
                getActivity().onBackPressed();
                return;
            }
            Log.e(TAG, " cancelled selected contacts by pressing back: ");
            // getActivity().onBackPressed();
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

    override fun onClick(v: View) {
        // Log.d(TAG, "Clicked view: " + v);
        val dataId = v.findViewById<TextView>(R.id.tv_id)
        val id = dataId.text.toString()
        // edits group
        // showContactDetail(id, user);
        // group chat
        showGroupChat(id, user)

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
    override fun onDialogPositiveClick(dialog: DialogFragment) {
        // User touched the dialog's positive button
        // // TODO: 16-03-2016
        // user = dialog.getArguments().getParcelable(ContactItem.TAG);
        // getActivity().notifyUserBaseItemListeners();

        // user must be signed into atleast one email account

        val editGroupDialogFragment = dialog as EditGroupDialogFragment
        val subject = editGroupDialogFragment.subject
        if (subject != null && subject.isNotEmpty()) {

            val to = editGroupDialogFragment.to
            var i = 0
            var sto = arrayOf<String>()
            if (to != null && to.isNotEmpty()) {
                sto = arrayOfNulls(to.size)
                for (s in to) {
                    val address = s.address
                    sto[i++] = address
                }
            }
            Log.d(TAG, Arrays.toString(sto))
            if (1 == sto.size) {
                // processNewMessageFromAction(sto[0], subject, user, getActivity().getApplicationContext());
            } else if (sto.size > 1) {
                createGroups(sto, subject)
            }

            /*
            ContactItem groupContactItem = new ContactItem();
            groupContactItem.setName(subject);
            createGroups(groupContactItem);
            */

            /*
            MessageItem messageThreadItem = new MessageItem();
            // new message.. sets id to a constant.. global new message id
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
            */

            /*
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

    override fun onDialogNegativeClick(dialog: DialogFragment) {
        // User touched the dialog's negative button
    }

    override fun onContactsTopicClicked(item: ContactItem, user: ContactItem) {
    }

    override fun onContactsTopicsLoaded(count: Int) {
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        restartLoader(query)
        return false
    }

    override fun onQueryTextChange(newText: String): Boolean {
        restartLoader(newText)
        return false
    }

    override fun onClose(): Boolean {
        restartLoader("")
        return false
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        // This is called when a new Loader needs to be created. This
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
            // // TODO: 19-02-2016
            // to set the context right, else the loader will fail from the calling activity
            // return new CursorLoader(context, messageThreadUri,
            /*
            //===========================================================================
            // IMPORTANT
            // JOIN QUERY - SO REMOVE AMBIGUITY BY SPECIFYING JOIN TABLE NAME
            // GC AS PREFIX IS REQUIRED FOR THE QUERY TO WORK CORRECTLY
            //===========================================================================
            */
            val selection = " ( name IS NULL OR name like ? ) "
                + " AND ( " +
                ContactItemColumns.KEY_IS_GROUP +
                " = 1 ) AND ( " +
                ContactItemColumns.KEY_IS_EMAIL +
                " = 0 OR " +
                ContactItemColumns.KEY_IS_EMAIL +
                " IS NULL ) "
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
            val selArgs: Array<String> = if (filter != null && filter!!.isNotEmpty()) {
                arrayOf("%$filter%")
            } else {
                arrayOf("%%")
            }
            // Log.d(TAG, "FILTER: " + filter + " " + selArgs + " " + selection);
            return CursorLoader(requireActivity(),
                MsgChatGrpContactsContentProvider.CONTENT_URI,
                null,
                selection, selArgs,
                ContactItemColumns.KEY_TIMESTAMP + " DESC")
        }
        // returns an empty dummy cursor.. for the cursor loader to play game
        return CursorLoader(requireActivity(),
            MsgChatGrpContactsContentProvider.CONTENT_URI,
            null,
            ContactItemColumns.KEY_DATA_ID + " = ? ",
            arrayOf("0"), null)
    }

    override fun onLoadFinished(loader: Loader<Cursor>, cursor: Cursor) {
        // Swap the new cursor in. (The framework will take care of closing the
        // old cursor once we return.)
        if (MainApplicationSingleton.GROUP_CONTACTITEM_FRAGMENT_LOADERID == loader.id) {
            if (cursor == null) {
                contactItems = null
                createRecyclerAdapter()
                return
            }
            val count = cursor.count
            /*
            if (messageTopicListener != null) {
                messageTopicListener.onMessageTopicsLoaded(count);
            }
            */
            // if (0 == count)
            // showEmpty("Please add Email Accounts to see conversations. You can select a Contact to send chat");
            if (0 == count) {
                contactItems = null
                createRecyclerAdapter()
                return
            }
            if (count > 0 && 0 == cursor.position) {
                contactItems = null
                fillItemsFromCursor(cursor)
                cursor.close()
                createRecyclerAdapter()
            }
        }
    }

    fun fillItemsFromCursor(cursor: Cursor) {
        this.contactItems =
            MsgChatGrpContactsContentProvider.createsContactsFromCursor(cursor)
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed. We need to make sure we are no
        // longer using it.
        if (MainApplicationSingleton.GROUP_CONTACTITEM_FRAGMENT_LOADERID == loader.id) {
            contactItems = null
            // refreshes view, for every data change.. this might not be required since adapter is
            // loaded on finish
            // setIntellibitzContactItemHashMap(user.getMsgContactItemHashMap());
            createRecyclerAdapter()
        }
    }

    override fun onTrimMemory(level: Int) {
        Log.d(TAG, "onTrimMemory: $level")
    }

    inner class RecyclerViewAdapter(private val viewItems: List<ContactItem>) :
        RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>(),
        BitmapFromUrlTask.BitmapFromUrlTaskListener {

        private var bitmapFromUrlTask: BitmapFromUrlTask? = null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.group_item_content, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val contactItem = viewItems[position]
            holder.mItem = contactItem
            // the collection is based on dataid.. so stores cloud id
            holder.tvId.text = holder.mItem.dataId.toString()

            holder.tvName.setCompoundDrawablesRelative(null, null, null, null)
            val contactItems = contactItem.contactItems
            // Set<MobileItem> mobiles = new HashSet<>();
            // Set<EmailItem> emails = new HashSet<>();
            /*
            if (null == name && !mobiles.isEmpty()) {
                name = mobiles.iterator().next().getMobile();
            }
            if (null == name && !emails.isEmpty()) {
                name = emails.iterator().next().getEmail();
            }
            */
            val chatDrawable = getDrawable(R.drawable.ic_chat_black_24dp,
                requireActivity().theme)
            chatDrawable?.setBounds(Rect(0, 0, 100, 100))
            // sets the contact image
            holder.tvName.setCompoundDrawables(null, null, null, null)
            var name = contactItem.name
            if (TextUtils.isEmpty(name)) name = "Group Name (not set)"
            try {
                val pic = holder.mItem.profilePic
                if (pic == null || pic.isEmpty()) {
                    if (name!!.isNotEmpty()) {
                        val textDrawable = ColorGenerator.getTextDrawable(name)
                        textDrawable.setBounds(Rect(0, 0, 100, 100))
                        holder.tvName.setCompoundDrawablesRelative(
                            textDrawable, null, chatDrawable, null)
                    }
                } else if (pic.startsWith("http")) {
                    // cloud pic
                    bitmapFromUrlTask = BitmapFromUrlTask(
                        holder.tvName, pic, requireActivity().applicationContext)
                    bitmapFromUrlTask?.setBitmapFromUrlTaskListener(this)
                    bitmapFromUrlTask?.execute()
                } else if (pic.startsWith("/")) {
                    // storage pic
                    val bm = BitmapFactory.decodeFile(pic)
                    val croppedBitmap = NetworkImageView.getCroppedBitmap(bm, 100)
                    val drawable = BitmapDrawable(resources, croppedBitmap)
                    drawable.setBounds(Rect(0, 0, 100, 100))
                    holder.tvName.setCompoundDrawablesRelative(drawable, null, chatDrawable, null)
                } else if (pic.startsWith("content")) {
                    // db content pic
                    val bm = MediaStore.Images.Media.getBitmap(
                        requireActivity().contentResolver, Uri.parse(pic))
                    val croppedBitmap = NetworkImageView.getCroppedBitmap(bm, 100)
                    val drawable = BitmapDrawable(resources, croppedBitmap)
                    drawable.setBounds(Rect(0, 0, 100, 100))
                    holder.tvName.setCompoundDrawablesRelative(drawable, null, chatDrawable, null)
                } else {
                    val bm = MediaStore.Images.Media.getBitmap(
                        requireActivity().contentResolver, Uri.parse(pic))
                    val croppedBitmap = NetworkImageView.getCroppedBitmap(bm, 100)
                    val drawable = BitmapDrawable(resources, croppedBitmap)
                    drawable.setBounds(Rect(0, 0, 100, 100))
                    holder.tvName.setCompoundDrawablesRelative(drawable, null, chatDrawable, null)
                }
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e(TAG, e.message)
            }
            holder.tvName.text = name
            val rootView = holder.mView.rootView as ViewGroup
            val linearLayout = rootView.findViewById<LinearLayout>(R.id.list_item_groupinfo)
            // clears old views
            linearLayout.removeAllViews()
            for (item in contactItems) {
                val view = LayoutInflater.from(holder.mView.context).inflate(
                    R.layout.list_item_contactinfo,
                    rootView, false)
                val tv = view.findViewById<TextView>(R.id.tv_contactinfo)
                tv.text = item.name + ":" + item.typeId

                val pic = item.profilePic
                var picDrawable: BitmapDrawable? = null
                try {
                    val bitmap = MainApplicationSingleton.getBitmapDecodeAnyUri(pic, context)
                    if (bitmap == null) {
                        /*
                        TextDrawable drawable = ColorGenerator.getTextDrawable(name);
                        drawable.setBounds(new Rect(0, 0, 100, 100));
                        holder.tvTo.setCompoundDrawablesRelative(drawable, null, null, null);
                        */
                    } else {
                        val croppedBitmap = NetworkImageView.getCroppedBitmap(bitmap, 100)
                        picDrawable = BitmapDrawable(resources, croppedBitmap)
                        picDrawable.setBounds(Rect(0, 0, 100, 100))
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    Log.e(TAG, e.message)
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

                val drawable = getDrawable(R.drawable.ic_chat_black_24dp,
                    requireActivity().theme)
                drawable?.setBounds(Rect(0, 0, 40, 40))
                // tv.setCompoundDrawablesRelative(null, null, drawable, null);
                setCompoundDrawablesRelative(tv, picDrawable, null, drawable, null)
                tv.setOnClickListener {
                    // Log.d(TAG, "Clicked view: " + v);
                    // contactItem.setSelectedId(item.getIntellibitzId());
                    // contactItem.setMobileItem(item);
                    val intent =
                        Intent(MainApplicationSingleton.BROADCAST_CONTACT_PHONE_SELECTED)
                    intent.putExtra(MainApplicationSingleton.MOBILE_PARAM, item as Parcelable)
                    intent.putExtra(ContactItem.USER_CONTACT, user as Parcelable)
                    intent.putExtra(ContactItem.DEVICE_CONTACT, contactItem as Parcelable)
                    if (shared != null && shared!!.extras != null) {
                        intent.putExtras(shared!!.extras!!)
                    }
                    LocalBroadcastManager.getInstance(
                        requireActivity().applicationContext).sendBroadcast(intent)
                    // finishes.. so doesn't show up in back stack
                    // finish();
                }
                linearLayout.addView(view)
            }

            /*
            for (final IntellibitzContactItem item : contactItems) {
                final EmailItem emailItem = item.getEmailItem();
                View view = LayoutInflater.from(holder.mView.getContext()).inflate(
                        R.layout.list_item_contactinfo,
                        rootView, false);
                TextView tv = (TextView) view.findViewById(R.id.tv_contactinfo);
                // tv.setText(emailItem.getEmail());
                /*
                ImageView iv = (ImageView) view.findViewById(R.id.iv_contactinfo);
                iv.setImageDrawable(getDrawable(
                        R.drawable.ic_email_black_24dp, getTheme()));
                */

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
                        // finishes.. so doesn't show up in back stack
                        // finish();
                    }
                });
                linearLayout.addView(view);
            }
            */
        }

        override fun getItemCount(): Int {
            return viewItems.size
        }

        override fun getItemId(position: Int): Long {
            return viewItems[position]._id
        }

        override fun onPostBitmapFromUrlExecute(bitmap: Bitmap, textView: View, context: Context?) {
            var context = context
            if (context == null) {
                context = this@MsgChatGrpContactsFragment.context
            }
            if (context == null) return
            val resources = context.resources
            if (resources == null) return

            val roundedBitmap = NetworkImageView.getCroppedBitmap(bitmap, 100)
            val drawable = BitmapDrawable(resources, roundedBitmap)
            drawable.setBounds(Rect(0, 0, 100, 100))

            val chatDrawable = getDrawable(R.drawable.ic_chat_black_24dp,
                requireActivity().theme)
            chatDrawable?.setBounds(Rect(0, 0, 100, 100))
            (textView as TextView).setCompoundDrawablesRelative(drawable, null, chatDrawable, null)
        }

        override fun onPostBitmapFromUrlExecuteFail(bitmap: Bitmap?) {
            Log.e(TAG, "On Post Bitmap From URL Exec ERROR: $bitmap")
        }

        override fun setBitmapFromUrlTaskToNull() {
            bitmapFromUrlTask = null
        }

        inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
            // the views
            val tvName: TextView = mView.findViewById(R.id.tv_name)
            val tvId: TextView = mView.findViewById(R.id.tv_id)
            // the data
            var mItem: ContactItem? = null

            init {
                mView.setOnClickListener(this@MsgChatGrpContactsFragment)
            }

            override fun toString(): String {
                return super.toString() + " '" + tvName.text + "'"
            }
        }
    }
}
