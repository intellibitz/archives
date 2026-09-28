package intellibitz.intellidroid.fragment

import android.app.Activity
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
import android.graphics.drawable.NinePatchDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Parcelable
import android.provider.MediaStore
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.tabs.TabLayout
import intellibitz.intellidroid.IntellibitzActivityFragment
import intellibitz.intellidroid.IntellibitzPermissionFragment
import intellibitz.intellidroid.R
import intellibitz.intellidroid.activity.ContactSelectActivity
import intellibitz.intellidroid.content.MsgChatGrpContactContentProvider
import intellibitz.intellidroid.content.MsgChatGrpContactsContentProvider
import intellibitz.intellidroid.content.task.GroupsSaveToDBTask
import intellibitz.intellidroid.content.task.GroupsUpdateDBTask
import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.db.ContactItemColumns
import intellibitz.intellidroid.graphics.ColorGenerator
import intellibitz.intellidroid.graphics.TextDrawable
import intellibitz.intellidroid.listener.ContactHeaderListener
import intellibitz.intellidroid.listener.ContactListener
import intellibitz.intellidroid.task.BitmapFromUrlTask
import intellibitz.intellidroid.task.CreateGroupTask
import intellibitz.intellidroid.task.GroupModifyUserTypeTask
import intellibitz.intellidroid.task.GroupRemoveUsersTask
import intellibitz.intellidroid.task.GroupsAddUsersTask
import intellibitz.intellidroid.task.LeaveGroupTask
import intellibitz.intellidroid.util.MainApplicationSingleton
import intellibitz.intellidroid.util.MediaPicker
import intellibitz.intellidroid.util.MediaPickerRequest
import intellibitz.intellidroid.util.NetworkImageView
import intellibitz.intellidroid.widget.advrecyclerview.animator.GeneralItemAnimator
import intellibitz.intellidroid.widget.advrecyclerview.animator.SwipeDismissItemAnimator
import intellibitz.intellidroid.widget.advrecyclerview.decoration.ItemShadowDecorator
import intellibitz.intellidroid.widget.advrecyclerview.decoration.SimpleListDividerDecorator
import intellibitz.intellidroid.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager
import intellibitz.intellidroid.widget.advrecyclerview.swipeable.SwipeableItemAdapter
import intellibitz.intellidroid.widget.advrecyclerview.swipeable.SwipeableItemConstants
import intellibitz.intellidroid.widget.advrecyclerview.swipeable.action.SwipeResultAction
import intellibitz.intellidroid.widget.advrecyclerview.swipeable.action.SwipeResultActionDefault
import intellibitz.intellidroid.widget.advrecyclerview.swipeable.action.SwipeResultActionMoveToSwipedDirection
import intellibitz.intellidroid.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager
import intellibitz.intellidroid.widget.advrecyclerview.utils.AbstractSwipeableItemViewHolder
import intellibitz.intellidroid.widget.advrecyclerview.utils.RecyclerViewAdapterUtils
import intellibitz.intellidroid.widget.advrecyclerview.utils.WrapperAdapterUtils
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.ArrayList
import java.util.Collection
import java.util.HashSet

/**
 */
class MsgChatGrpContactsDetailFragment : IntellibitzActivityFragment(),
    ContactListener,
    View.OnClickListener,
    LoaderManager.LoaderCallbacks<Cursor>,
    GroupsUpdateDBTask.GroupsUpdateDBTaskListener,
    TabLayout.OnTabSelectedListener,
    GroupModifyUserTypeTask.GroupModifyUserTypeTaskListener,
    GroupRemoveUsersTask.GroupsRemoveUsersTaskListener,
    LeaveGroupTask.LeaveGroupTaskListener,
    CreateGroupTask.CreateGroupTaskListener,
    GroupsSaveToDBTask.GroupsSaveToDBTaskListener,
    GroupsAddUsersTask.GroupsAddUsersTaskListener {

    companion object {
        private const val TAG = "MsgChatGrpCtsDetailFrag"

        fun newInstance(
            contactItem: ContactItem?,
            user: ContactItem?,
            contactListener: ContactListener?
        ): MsgChatGrpContactsDetailFragment {
            val fragment = MsgChatGrpContactsDetailFragment()
            fragment.contactListener = contactListener
            if (contactListener is ContactHeaderListener) {
                fragment.contactHeaderListener = contactListener as ContactHeaderListener
            }
            if (contactListener != null) {
                fragment.viewModeListener = contactListener
            }
            fragment.user = user
            fragment.contactItem = contactItem
            val args = Bundle()
            args.putParcelable(ContactItem.USER_CONTACT, user)
            args.putParcelable(ContactItem.TAG, contactItem)
            fragment.arguments = args
            return fragment
        }
    }

    private var snackView: View? = null
    private var contentObserver: ContentObserver? = null
    private var recyclerView: RecyclerView? = null
    private var viewAdapter: RecyclerViewAdapter? = null
    private var mWrappedAdapter: RecyclerView.Adapter<*>? = null
    private var mRecyclerViewTouchActionGuardManager: RecyclerViewTouchActionGuardManager? = null
    private var mRecyclerViewSwipeManager: RecyclerViewSwipeManager? = null
    private var newGroup = false
    private var imageView: NetworkImageView? = null
    private var selfContactItem: ContactItem? = null
    private var handlerThread: HandlerThread? = null
    private val contactItems: Collection<ContactItem> = ArrayList()
    private var toolbar: Toolbar? = null
    private var etGroupName: EditText? = null
    private var contactItem: ContactItem? = null
    private var filter: String? = null
    private var cursor: Cursor? = null
    private var contactListener: ContactListener? = null
    private var contactHeaderListener: ContactHeaderListener? = null
    private var groupModifyUserTypeTask: GroupModifyUserTypeTask? = null
    private var groupRemoveUsersTask: GroupRemoveUsersTask? = null
    private var groupsSaveToDBTask: GroupsSaveToDBTask? = null
    private var leaveGroupTask: LeaveGroupTask? = null
    private var groupsAddUsersTask: GroupsAddUsersTask? = null
    private var groupsUpdateDBTask: GroupsUpdateDBTask? = null
    private var createGroupTask: CreateGroupTask? = null
    private var mActionMode: ActionMode? = null
    private val mActionModeCallback: ActionMode.Callback = object : ActionMode.Callback {

        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            val inflater: MenuInflater = mode.menuInflater
            inflater.inflate(R.menu.menu_context_groupcontact_details, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            when (item.itemId) {
                R.id.menu_delete -> {
                    deleteMessages()
                    mode.finish()
                    return true
                }
                R.id.menu_flag -> {
                    flagMessages()
                    mode.finish()
                    return true
                }
                R.id.menu_unflag -> {
                    unflagMessages()
                    mode.finish()
                    return true
                }
                else -> return false
            }
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            mActionMode = null
        }
    }

    fun setContactItems(items: Collection<ContactItem>?) {
        contactItems.clear()
        if (items != null) {
            contactItems.addAll(items)
        }
    }

    override fun onDestroyView() {
        mRecyclerViewSwipeManager?.release()
        mRecyclerViewSwipeManager = null

        mRecyclerViewTouchActionGuardManager?.release()
        mRecyclerViewTouchActionGuardManager = null

        recyclerView?.let {
            it.itemAnimator = null
            it.adapter = null
        }
        recyclerView = null

        mWrappedAdapter?.let { WrapperAdapterUtils.releaseAll(it) }
        mWrappedAdapter = null
        viewAdapter = null

        super.onDestroyView()
    }

    override fun onDestroy() {
        getAppCompatActivity().contentResolver.unregisterContentObserver(contentObserver)
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
        getAppCompatActivity().contentResolver.registerContentObserver(
            MsgChatGrpContactsContentProvider.CONTENT_URI, false,
            contentObserver
        )
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            user = savedInstanceState.getParcelable(ContactItem.USER_CONTACT)
            contactItem = savedInstanceState.getParcelable(ContactItem.TAG)
        }
        super.onViewStateRestored(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(ContactItem.USER_CONTACT, user)
        outState.putParcelable(ContactItem.TAG, contactItem)
        super.onSaveInstanceState(outState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_msgchatgrpcontactsdetail, container, false)
    }

    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (null == savedInstanceState) {
            user = arguments?.getParcelable(ContactItem.USER_CONTACT)
            contactItem = arguments?.getParcelable(ContactItem.TAG)
        } else {
            user = savedInstanceState.getParcelable(ContactItem.USER_CONTACT)
            contactItem = savedInstanceState.getParcelable(ContactItem.TAG)
        }
        if (contactItem != null) {
            newGroup = TextUtils.isEmpty(contactItem?.dataId)
        }

        setupAppBar()
        recyclerView = view.findViewById(R.id.recyclerview)
        snackView = view.findViewById(R.id.cl)

        setupSwipe(view)

        etGroupName = view.findViewById(R.id.et)
        imageView = view.findViewById(R.id.iv_group)
        if (newGroup) {
            imageView?.visibility = View.VISIBLE
            imageView?.setImageDrawable(
                getDrawable(
                    R.drawable.ic_account_circle_black_24dp,
                    context?.theme
                )
            )
            setupMediaChooserOnPermissions(imageView)
            etGroupName?.visibility = View.VISIBLE
            etGroupName?.setText(contactItem?.name)
            etGroupName?.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    contactItem?.name = etGroupName?.text.toString()
                    setupDetailTitle(contactItem, toolbar)
                }

                override fun afterTextChanged(s: Editable) {}
            })
            etGroupName?.requestFocus()
        } else {
            etGroupName?.visibility = View.GONE
            imageView?.visibility = View.GONE
        }
        restartLoader()
    }

    private fun setupAppBar() {
        val view = view
        if (view != null) {
            toolbar = view.findViewById(R.id.toolbar)
            setupDetailToolbar(contactItem)
            val tvOk = view.findViewById<TextView>(R.id.btn_ok)
            tvOk.setOnClickListener { onOkPressed() }
            val tvClose = view.findViewById<TextView>(R.id.tv_close)
            tvClose.setOnClickListener { onCancelPressed() }
            getAppCompatActivity().setSupportActionBar(toolbar)
        }
    }

    fun setupToolbars() {
        setupDetailFilterToolbar()
    }

    private fun setupMediaChooserOnPermissions(view: View?) {
        if (IntellibitzPermissionFragment.isCameraPermissionGranted(context) &&
            IntellibitzPermissionFragment.isReadPhoneStatePermissionGranted(context) &&
            IntellibitzPermissionFragment.isWriteExternalStoragePermissionGranted(context)
        ) {
            setupMediaChooser(view)
        } else {
            mayRequestCamera(snackView)
            mayRequestReadExternalStorage(snackView)
            mayRequestWriteExternalStorage(snackView)
        }
    }

    private fun setupMediaChooser(view: View?) {
        view?.setOnClickListener {
            MediaPicker.openMediaChooser(
                this@MsgChatGrpContactsDetailFragment,
                "Choose now"
            ) { e: IOException ->
                Log.e("MediaPicker", "Open chooser error.", e)
            }
        }
    }

    private fun displayImage(file: String?) {
        if (file != null && !file.isEmpty()) {
            Log.d(TAG, "Profile image change: $file")
            imageView?.setImageUrl(
                file,
                MainApplicationSingleton.getInstance(activity).imageLoader
            )
        }
    }

    private fun setupDetailFilterToolbar() {}

    private fun createRecyclerAdapter() {
        val view = view

        if (null == recyclerView && view != null) {
            recyclerView = view.findViewById(R.id.recyclerview)
        }

        if (null == recyclerView) return

        mRecyclerViewTouchActionGuardManager = RecyclerViewTouchActionGuardManager()
        mRecyclerViewTouchActionGuardManager?.isInterceptVerticalScrollingWhileAnimationRunning = true
        mRecyclerViewTouchActionGuardManager?.isEnabled = true

        mRecyclerViewSwipeManager = RecyclerViewSwipeManager()

        selfContactItem = contactItem?.getContactItem(user?.dataId)

        viewAdapter = RecyclerViewAdapter(contactItems)
        viewAdapter?.setHasStableIds(true)
        viewAdapter?.setEventListener(object : EventListener {
            override fun onItemPinned(position: Int) {}

            override fun onItemViewClicked(v: View) {
                handleOnItemViewClicked(v)
            }

            override fun onUnderSwipeableViewButtonClicked(parent: View, view: View) {
                handleOnUnderSwipeableViewButtonClicked(parent, view)
            }
        })

        mWrappedAdapter = mRecyclerViewSwipeManager?.createWrappedAdapter(viewAdapter)

        val animator = SwipeDismissItemAnimator()
        animator.supportsChangeAnimations = false

        recyclerView?.swapAdapter(mWrappedAdapter, true)
        recyclerView?.itemAnimator = animator

        if (!supportsViewElevation()) {
            recyclerView?.addItemDecoration(
                ItemShadowDecorator(
                    ContextCompat.getDrawable(
                        context!!,
                        R.drawable.material_shadow_z1
                    ) as NinePatchDrawable
                )
            )
        }
        recyclerView?.addItemDecoration(
            SimpleListDividerDecorator(
                ContextCompat.getDrawable(context!!, R.drawable.list_divider_h),
                true
            )
        )

        mRecyclerViewTouchActionGuardManager?.attachRecyclerView(recyclerView)
        mRecyclerViewSwipeManager?.attachRecyclerView(recyclerView)

        recyclerView?.scrollToPosition(mWrappedAdapter?.itemCount?.minus(1) ?: 0)
    }

    private fun supportsViewElevation(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
    }

    private fun handleOnItemViewClicked(v: View) {
        val position = recyclerView?.getChildAdapterPosition(v) ?: RecyclerView.NO_POSITION
        if (position != RecyclerView.NO_POSITION) {
            val data = viewAdapter?.getItem(position)
            if (data?.isPinned == true) {
                data.isPinned = false
                viewAdapter?.notifyItemChanged(position)
            }
        }
    }

    private fun handleOnUnderSwipeableViewButtonClicked(parent: View, view: View) {
        if (newGroup) return
        val position = recyclerView?.getChildAdapterPosition(parent) ?: RecyclerView.NO_POSITION
        if (position != RecyclerView.NO_POSITION) {
            val contactItem = viewAdapter?.getItem(position)
            if (R.id.btn_admin == view.id) {
                val button = view as Button
                if (resources.getString(R.string.make_admin).equals(button.text.toString(), ignoreCase = true)) {
                    makeAdmin(contactItem)
                } else if (resources.getString(R.string.remove_admin).equals(button.text.toString(), ignoreCase = true)) {
                    removeAdmin(contactItem)
                }
            } else if (R.id.btn_delete == view.id) {
                val button = view as Button
                if (resources.getString(R.string.delete).equals(button.text.toString(), ignoreCase = true)) {
                    removeUser(contactItem)
                } else if (resources.getString(R.string.exit_group).equals(button.text.toString(), ignoreCase = true)) {
                    performLeaveGroup()
                }
            }
        }
    }

    override fun onPostGroupModifyUserTypeExecute(
        response: JSONObject?,
        item: ContactItem?,
        contactItem: ContactItem?,
        flag: Int
    ) {
        val status = response?.optInt("status") ?: 0
        if (null == response || -1 == status || 99 == status) {
            onPostGroupModifyUserTypeExecuteFail(response, item, contactItem, flag)
        } else {
            Log.e(TAG, "SUCCESS - $response")
            handlerThread = MainApplicationSingleton.performOnHandlerThread {
                MsgChatGrpContactContentProvider.updatesTypeInDB(contactItem, context)
                quitHandlerThread()
            }
        }
    }

    private fun quitHandlerThread() {
        handlerThread?.quit()
    }

    override fun onPostGroupModifyUserTypeExecuteFail(
        response: JSONObject?,
        item: ContactItem?,
        contactItem: ContactItem?,
        flag: Int
    ) {
        Log.e(TAG, "FAIL - $response")
    }

    override fun setGroupModifyUserTypeTaskToNull() {
        groupModifyUserTypeTask = null
    }

    private fun makeAdmin(contactItem: ContactItem?) {
        if (!newGroup) {
            contactItem?.type = "admin"
            modifyUserType(contactItem)
        }
    }

    private fun removeAdmin(contactItem: ContactItem?) {
        if (!newGroup) {
            contactItem?.type = "user"
            modifyUserType(contactItem)
        }
    }

    private fun modifyUserType(contactItem: ContactItem?) {
        groupModifyUserTypeTask = GroupModifyUserTypeTask(
            this.contactItem,
            contactItem,
            -1,
            user?.dataId,
            user?.token,
            user?.device,
            user?.deviceRef,
            MainApplicationSingleton.AUTH_GROUP_MODIFY_USER_TYPE
        )
        groupModifyUserTypeTask?.setGroupModifyUserTypeTaskListener(this)
        groupModifyUserTypeTask?.execute()
    }

    private fun removeUser(contactItem: ContactItem?) {
        groupRemoveUsersTask = GroupRemoveUsersTask(
            this.contactItem,
            contactItem,
            user?.dataId,
            user?.token,
            user?.device,
            user?.deviceRef,
            MainApplicationSingleton.AUTH_GROUP_REMOVE_USERS
        )
        groupRemoveUsersTask?.setGroupsRemoveUsersTaskListener(this)
        groupRemoveUsersTask?.execute()
    }

    private fun leaveGroup(contactItem: ContactItem?) {
        leaveGroupTask = LeaveGroupTask(
            this.contactItem,
            contactItem,
            user?.dataId,
            user?.token,
            user?.device,
            user?.deviceRef,
            MainApplicationSingleton.AUTH_LEAVE_GROUP
        )
        leaveGroupTask?.setLeaveGroupTaskListener(this)
        leaveGroupTask?.execute()
    }

    override fun onPostLeaveGroupExecute(
        response: JSONObject?,
        id: String?,
        name: String?,
        contactItem: ContactItem?,
        contactThreadItem: ContactItem?
    ) {
        val status = response?.optInt("status") ?: 0
        if (null == response || -1 == status || 99 == status) {
            onPostLeaveGroupExecuteFail(response, id, name, contactItem, contactThreadItem)
        } else {
            Log.e(TAG, "SUCCESS - $response")
            handlerThread = MainApplicationSingleton.performOnHandlerThread {
                val row = MsgChatGrpContactsContentProvider.deleteContact(
                    contactItem,
                    contactThreadItem,
                    context
                )
                if (0 == row) {
                    onPostLeaveGroupExecuteFail(response, id, name, contactItem, contactThreadItem)
                } else {
                    contactThreadItem?.contactItems?.remove(contactItem)
                    contactItems.remove(contactItem)
                }
                quitHandlerThread()
            }
        }
    }

    override fun onPostLeaveGroupExecuteFail(
        response: JSONObject?,
        id: String?,
        name: String?,
        contactItem: ContactItem?,
        contactThreadItem: ContactItem?
    ) {
        Log.e(TAG, "ERROR: Exit Group - $response User: $contactItem")
    }

    override fun setLeaveGroupTaskToNull() {
        leaveGroupTask = null
    }

    override fun onPostGroupsRemoveUsersExecute(
        response: JSONObject?,
        id: String?,
        name: String?,
        contacts: Array<String>?,
        contactItem: ContactItem?,
        contactThreadItem: ContactItem?
    ) {
        val status = response?.optInt("status") ?: 0
        if (null == response || -1 == status || 99 == status) {
            onPostGroupsRemoveUsersExecuteFail(response, id, name, contacts, contactItem, contactThreadItem)
        } else {
            Log.e(TAG, "SUCCESS - $response")
            handlerThread = MainApplicationSingleton.performOnHandlerThread {
                val row = MsgChatGrpContactsContentProvider.deleteContact(
                    contactItem,
                    contactThreadItem,
                    context
                )
                if (0 == row) {
                    onPostGroupsRemoveUsersExecuteFail(response, id, name, contacts, contactItem, contactThreadItem)
                } else {
                    contactThreadItem?.contactItems?.remove(contactItem)
                    contactItems.remove(contactItem)
                }
                quitHandlerThread()
            }
        }
    }

    override fun onPostGroupsRemoveUsersExecuteFail(
        response: JSONObject?,
        id: String?,
        name: String?,
        contacts: Array<String>?,
        contactItem: ContactItem?,
        contactThreadItem: ContactItem?
    ) {
        Log.e(TAG, "ERROR: Remove User from Group - $response User: $contactItem")
    }

    override fun setGroupsRemoveUsersTaskToNull() {
        groupRemoveUsersTask = null
    }

    private fun performLeaveGroup() {
        if (newGroup) {
            performOk()
        } else {
            selfContactItem?.let { leaveGroup(it) }
        }
    }

    private fun performExitContact() {
        if (newGroup) {
            performOk()
        } else {
            selfContactItem?.let { removeUser(it) }
        }
    }

    private fun performAddContact() {
        contactHeaderListener?.onContactHeaderNew()
        viewModeListener?.onViewModeChanged()
        startContactSelectActivity()
    }

    private fun startContactSelectActivity() {
        val intent = Intent(getAppCompatActivity(), ContactSelectActivity::class.java)
        intent.action = ContactSelectFragment.TAG
        intent.putExtra(ContactItem.TAG, contactItem as Parcelable)
        intent.putExtra(ContactItem.USER_CONTACT, user as Parcelable)
        startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_CONTACTSELECT_RQ_CODE)
    }

    private fun createNewGroup(contactItem: ContactItem?) {
        createGroupTask = CreateGroupTask(
            contactItem,
            user?.dataId,
            user?.token,
            user?.device,
            user?.deviceRef,
            MainApplicationSingleton.AUTH_CREATE_GROUP
        )
        createGroupTask?.setCreateGroupTaskListener(this)
        createGroupTask?.execute()
    }

    override fun setCreateGroupTaskToNull() {
        createGroupTask = null
    }

    override fun onPostCreateGroupExecuteFail(
        response: JSONObject?,
        name: String?,
        file: File?,
        contacts: Array<String>?,
        contactItem: ContactItem?
    ) {
        Log.e(TAG, "CONTACTS GET ERROR - $response")
        showError("Groups save failed - try again")
    }

    private fun showError(s: String) {
        etGroupName?.error = s
    }

    override fun onPostCreateGroupExecute(
        response: JSONObject?,
        name: String?,
        file: File?,
        contacts: Array<String>?,
        contactItem: ContactItem?
    ) {
        val status = response?.optInt("status") ?: 0
        if (null == response || 99 == status || -1 == status) {
            onPostCreateGroupExecuteFail(response, name, file, contacts, contactItem)
        } else {
            try {
                val id = response?.getString("group_id")
                if (null == id || 0 == id.length) {
                } else {
                    contactItem?.dataId = id
                    contactItem?.intellibitzId = id
                    contactItem?.typeId = id
                    contactItem?.groupId = id
                    contactItem?.type = "GROUP"
                    contactItem?.isGroup = true
                    contactItem?.isEmailItem = false
                    savesGroupsInDB(contactItem, getAppCompatActivity())
                    Log.e(TAG, "onPostCreateGroupExecute: SUCCESS - $response")
                }
            } catch (e: JSONException) {
                e.printStackTrace()
                Log.e(TAG, e.message)
            }
        }
    }

    fun savesGroup(contactItem: ContactItem?) {
        contactItem?.type = "GROUP"
        contactItem?.isGroup = true
        contactItem?.isEmailItem = false
    }

    fun savesGroup(name: String?, file: File?, id: String?) {
        contactItem?.dataId = id
        contactItem?.intellibitzId = id
        contactItem?.typeId = id
        contactItem?.groupId = id
        contactItem?.type = "GROUP"
        contactItem?.isGroup = true
        contactItem?.isEmailItem = false
        contactItem?.name = name
        if (file != null) {
            contactItem?.profilePic = file.absolutePath
        }
    }

    private fun savesGroupsInDB(contactItem: ContactItem?, context: Context?) {
        groupsSaveToDBTask = GroupsSaveToDBTask(contactItem, context)
        groupsSaveToDBTask?.setGroupsSaveToDBTaskListener(this)
        groupsSaveToDBTask?.execute()
    }

    override fun onPostGroupsSaveToDBExecute(uri: Uri?, contactThreadItems: Collection<ContactItem>?) {
        if (contactThreadItems != null && contactThreadItems.isNotEmpty()) {
            Log.e(TAG, " GROUPS Contacts - SUCCESS - ")
            for (contactItem in contactThreadItems) {
                if (contactItem.isNewGroup) {
                    val members = contactItem.contactItems
                    if (null == members || members.isEmpty()) {
                    } else {
                        addUsersToGroups(contactItem)
                    }
                } else {
                    val contactItems = contactItem.contactItems
                    if (null == contactItems || contactItems.isEmpty()) {
                    } else {
                    }
                }
            }
        } else {
            Log.e(TAG, "Group Save has returned EMPTY contacts - PLEASE CHECK: $uri")
        }
        performOk()
    }

    override fun setGroupsSaveToDBTaskToNull() {
        groupsSaveToDBTask = null
    }

    private fun addUsersToGroups(contactItem: ContactItem?) {
        groupsAddUsersTask = GroupsAddUsersTask(
            contactItem,
            user?.dataId,
            user?.token,
            user?.device,
            user?.deviceRef,
            MainApplicationSingleton.AUTH_GROUP_ADD_USERS
        )
        groupsAddUsersTask?.setGroupsAddUsersTaskListener(this)
        groupsAddUsersTask?.execute()
    }

    override fun onPostGroupsAddUsersExecuteFail(
        response: JSONObject?,
        id: String?,
        name: String?,
        contacts: Array<String>?,
        contactItem: ContactItem?
    ) {
        Log.e(TAG, "onPostGroupsAddUsersExecuteFail: $response")
    }

    override fun setGroupsAddUsersTaskToNull() {
        groupsAddUsersTask = null
    }

    override fun onPostGroupsAddUsersExecute(
        response: JSONObject?,
        id: String?,
        name: String?,
        contacts: Array<String>?,
        contactItem: ContactItem?
    ) {
        val status = response?.optInt("status") ?: 0
        if (null == response || 99 == status || -1 == status) {
            onPostGroupsAddUsersExecuteFail(response, id, name, contacts, contactItem)
        } else {
            Log.e(TAG, " GROUPS ADD USERS - SUCCESS - ")
            val activity = getAppCompatActivity()
            if (activity != null) {
                val intent = activity.intent
                intent.putExtra(ContactItem.USER_CONTACT, user as Parcelable)
                intent.putExtra(ContactItem.TAG, contactItem as Parcelable)
                activity.setResult(Activity.RESULT_OK, intent)
                activity.finish()
            }
        }
    }

    private fun onOkPressed() {
        val activity = getAppCompatActivity()
        val intent = activity.intent
        val contactItems = contactItem?.contactItems
        if (null == contactItems || contactItems.isEmpty()) {
            intent.putExtra(ContactItem.USER_CONTACT, user as Parcelable)
            intent.putExtra(ContactItem.TAG, contactItem as Parcelable)
            activity.setResult(Activity.RESULT_CANCELED, intent)
            activity.finish()
        } else {
            val count = contactItems.size
            if (count > 1) {
                val name = contactItem?.name
                if (null == name || name.isEmpty()) {
                    Log.e(TAG, "Name is empty - cannot create group")
                    intent.putExtra(ContactItem.USER_CONTACT, user as Parcelable)
                    intent.putExtra(ContactItem.TAG, contactItem as Parcelable)
                    activity.setResult(Activity.RESULT_CANCELED, intent)
                    activity.finish()
                } else {
                    contactItem?.isGroup = true
                    createNewGroup(contactItem)
                }
            }
        }
    }

    private fun onCancelPressed() {
        val activity = getAppCompatActivity()
        val intent = activity.intent
        intent.putExtra(ContactItem.USER_CONTACT, user as Parcelable)
        intent.putExtra(ContactItem.TAG, contactItem as Parcelable)
        activity.setResult(Activity.RESULT_CANCELED, intent)
        activity.finish()
    }

    private fun performOk() {
        val activity = getAppCompatActivity()
        val intent = activity.intent
        intent.putExtra(ContactItem.USER_CONTACT, user as Parcelable)
        intent.putExtra(ContactItem.TAG, contactItem as Parcelable)
        activity.setResult(Activity.RESULT_OK, intent)
        activity.finish()
    }

    override fun onBackPressed(): Boolean {
        removeSelf()
        viewModeListener?.onViewModeItem()
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (MainApplicationSingleton.ACTIVITY_CONTACTSELECT_RQ_CODE == requestCode) {
            if (Activity.RESULT_OK == resultCode) {
                if (data != null) {
                    val item = data.getParcelableExtra<ContactItem>(ContactItem.TAG)
                    if (null == contactItem) contactItem = item
                    val size = contactItem?.selectedContacts?.size ?: 0
                    if (contactItem == item) {
                        Log.e(TAG, " Already selected contacts: $size")
                    } else {
                        contactItem?.selectedContacts = item?.selectedContacts
                    }
                    contactItem?.mergeSelectedContacts()
                    if (TextUtils.isEmpty(contactItem?.dataId)) {
                        setContactItems(contactItem?.contactItems)
                        setupDetailTitle(contactItem, toolbar)
                        createRecyclerAdapter()
                    } else {
                        updateGroupsInDB(contactItem, getAppCompatActivity())
                    }
                }
            } else if (Activity.RESULT_CANCELED == resultCode) {
                Log.e(TAG, "onActivityResult: 0 Contacts selected - ")
            }
        }

        MediaPicker.handleActivityResult(
            activity,
            requestCode,
            resultCode,
            data,
            object : MediaPicker.OnResult {
                override fun onError(e: IOException) {
                    Log.e("MediaPicker", "Got file error.", e)
                }

                override fun onSuccess(mediaFile: File, request: MediaPickerRequest) {
                    Log.e("MediaPicker", "Got file result: $mediaFile for code: $request")
                    if (request != MediaPickerRequest.REQUEST_CROP) {
                        val paramColor = ContextCompat.getColor(activity, android.R.color.black)
                        val paramWidth = 128
                        val paramHeight = 128
                        MediaPicker.startForImageCrop(
                            this@MsgChatGrpContactsDetailFragment,
                            mediaFile,
                            paramWidth,
                            paramHeight,
                            paramColor
                        ) { e: IOException ->
                            Log.e("MediaPicker", "Open cropper error.", e)
                        }
                    } else {
                        contactItem?.profilePic = mediaFile.absolutePath
                        displayImage(contactItem?.profilePic)
                        setupDetailTitle(contactItem, toolbar)
                    }
                }

                override fun onCancelled() {
                    Log.e("MediaPicker", "Got cancelled event.")
                }
            }
        )
    }

    fun onOkPressed(intent: Intent?) {
        if (null == intent) {
            Log.e(TAG, " cancelled selected contacts by pressing back: ")
            return
        } else {
            val item = intent.getParcelableExtra<ContactItem>(ContactItem.TAG)
            if (null == item) {
                Log.e(TAG, " cancelled selected contacts by pressing back: ")
                return
            }
            if (contactItem == item) {
                Log.e(TAG, " Already selected contacts: ${contactItem?.selectedContacts?.size}")
            } else {
                contactItem?.selectedContacts = item?.selectedContacts
            }
            contactItem?.mergeSelectedContacts()
            if (TextUtils.isEmpty(contactItem?.dataId)) {
            } else {
                updateGroupsInDB(contactItem, getAppCompatActivity())
            }
            Log.e(TAG, " selected contacts: ${contactItem?.selectedContacts?.size}")
        }
    }

    private fun updateGroupsInDB(contactItem: ContactItem?, context: Context?) {
        groupsUpdateDBTask = GroupsUpdateDBTask(contactItem, context)
        groupsUpdateDBTask?.setGroupsUpdateDBTaskListener(this)
        groupsUpdateDBTask?.execute()
    }

    override fun onPostGroupsUpdateDBExecute(result: Int, contacts: Collection<ContactItem>?) {
        contactItem = contacts?.iterator()?.next()
    }

    override fun onPostGroupsUpdateDBExecuteFail(result: Int, contacts: Collection<ContactItem>?) {
        Log.e(TAG, " ERROR - $result")
    }

    override fun setGroupsUpdateDBTaskToNull() {
        groupsUpdateDBTask = null
    }

    fun setupDetailToolbar() {
        setupDetailToolbar(contactItem)
    }

    private fun setupDetailToolbar(item: ContactItem?) {
        setupDetailTitle(item, toolbar)
    }

    private fun setupDetailTitle(item: ContactItem?, toolbar: Toolbar?) {
        val view = view
        if (view != null) {
            toolbar = view.findViewById(R.id.toolbar)
            if (null == toolbar) return
            var title = "New Group"
            if (item != null) {
                title = item.name ?: "New Group"
            }
            var subTitle = "Group Contact"
            val names = item?.contactsNameAsArray
            if (null == names || 0 == names.size) {
                subTitle = "0 Contacts in Group"
            } else {
                subTitle = ""
                for (name in names) {
                    if (name != null && !subTitle.isEmpty() && !subTitle.contains(name)) {
                        subTitle += ","
                    }
                    subTitle += name
                }
            }
            val pic = item?.profilePic
            if (item != null) {
                setNavigationIcon(toolbar, pic)
            }
            val tvTitle = view.findViewById<TextView>(R.id.tv_subtitle)
            val tvSubTitle = view.findViewById<TextView>(R.id.tv_subtitle)

            tvTitle.text = title
            tvSubTitle.text = subTitle
        }
    }

    private fun setNavigationIcon(toolbar: Toolbar?, pic: String?) {
        if (pic != null && !pic.isEmpty()) {
            if (pic.startsWith("http")) {
                val bitmapFromUrlTask = arrayOfNulls<BitmapFromUrlTask>(1)
                bitmapFromUrlTask[0] = BitmapFromUrlTask(
                    null,
                    pic,
                    getAppCompatActivity()
                )
                bitmapFromUrlTask[0]?.setBitmapFromUrlTaskListener(
                    object : BitmapFromUrlTask.BitmapFromUrlTaskListener {
                        override fun onPostBitmapFromUrlExecute(
                            bitmap: Bitmap?,
                            textView: View?,
                            context: Context?
                        ) {
                            val ctx = context ?: context
                            if (null == ctx) return
                            val resources = ctx.resources
                            if (null == resources) return
                            val roundedBitmap = NetworkImageView.getCroppedBitmap(bitmap, 100)
                            val drawable = BitmapDrawable(resources, roundedBitmap)
                            drawable.bounds = Rect(0, 0, 100, 100)
                            toolbar?.navigationIcon = drawable
                        }

                        override fun onPostBitmapFromUrlExecuteFail(response: Bitmap?) {}

                        override fun setBitmapFromUrlTaskToNull() {
                            bitmapFromUrlTask[0] = null
                        }
                    }
                )
                bitmapFromUrlTask[0]?.execute()
            } else {
                val bitmap = BitmapFactory.decodeFile(pic)
                val roundedBitmap = NetworkImageView.getCroppedBitmap(bitmap, 100)
                val drawable = BitmapDrawable(resources, roundedBitmap)
                drawable.bounds = Rect(0, 0, 100, 100)
                toolbar?.navigationIcon = drawable
            }
        }
    }

    fun setupSwipe(view: View) {
        val refreshLayout = view.findViewById<SwipeRefreshLayout>(R.id.swiperefresh)
        refreshLayout?.setOnRefreshListener {
            val mHandler = Handler()
            mHandler.postDelayed({
                refreshLayout.isRefreshing = false
            }, 2000)
        }
    }

    private fun restartLoader() {
        getAppCompatActivity().supportLoaderManager.restartLoader(
            MainApplicationSingleton.GROUPCONTACT_DETAIL_FRAGMENT_LOADERID,
            null,
            this
        )
    }

    override fun onClick(v: View) {}

    override fun onTabSelected(tab: TabLayout.Tab) {
        if (null == tab) return
        if (null == tab.tag) return
        if (R.id.mict_cancel == tab.tag as Int) {
            onCancelPressed()
            return
        }
        if (R.id.mict_done == tab.tag as Int) {
            onOkPressed()
            return
        }
    }

    override fun onTabUnselected(tab: TabLayout.Tab) {
        if (null == tab) return
        if (null == tab.tag) return
        if (R.id.mict_cancel == tab.tag as Int) {
            return
        }
        if (R.id.mict_done == tab.tag as Int) {
            return
        }
    }

    override fun onTabReselected(tab: TabLayout.Tab) {}

    private fun setSelectedItem(mItem: ContactItem?) {}

    private fun execDeleteMsgsTask(msgs: Array<String>?) {}

    private fun execFlagMsgsTask(msgs: Array<String>?) {}

    private fun execUnFlagMsgsTask(msgs: Array<String>?) {}

    private fun deleteMessages() {}

    private fun flagMessages() {}

    private fun unflagMessages() {}

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        val cursorLoader: CursorLoader
        if (contactItem != null && contactItem?.dataId != null &&
            MainApplicationSingleton.GROUPCONTACT_DETAIL_FRAGMENT_LOADERID == id
        ) {
            val contactThreadItemId = contactItem?.dataId
            val selection = " ( gc.name IS NULL OR gc.name like ? ) "
                    + " AND ( " +
                    " gc." + ContactItemColumns.KEY_IS_GROUP +
                    " = 1 ) AND " +
                    " ( gc." + ContactItemColumns.KEY_IS_EMAIL +
                    " = 0 OR " +
                    " gc." + ContactItemColumns.KEY_IS_EMAIL +
                    " IS NULL ) AND " +
                    " ( ak." + ContactItemColumns.KEY_IS_GROUP +
                    " = 0 OR " +
                    " ak." + ContactItemColumns.KEY_IS_GROUP +
                    " IS NULL ) " +
                    " AND  ( gc." + ContactItemColumns.KEY_DATA_ID +
                    " = ? )"
            val selArgs: Array<String>
            if (filter != null && !filter.isEmpty()) {
                selArgs = arrayOf("%$filter%", contactThreadItemId)
            } else {
                selArgs = arrayOf("%%", contactThreadItemId)
            }
            cursorLoader = CursorLoader(
                getAppCompatActivity(),
                MsgChatGrpContactsContentProvider.JOIN_CONTENT_URI,
                null,
                selection,
                selArgs,
                "gc." + ContactItemColumns.KEY_TIMESTAMP +
                        " DESC"
            )
        } else {
            cursorLoader = CursorLoader(
                getAppCompatActivity(),
                MsgChatGrpContactsContentProvider.JOIN_CONTENT_URI,
                null,
                "gc." + ContactItemColumns.KEY_DATA_ID + " = ? ",
                arrayOf("0"),
                null
            )
        }
        return cursorLoader
    }

    override fun onLoadFinished(loader: Loader<Cursor>, cursor: Cursor?) {
        this.cursor = cursor
        if (null == cursor) {
            if (null == contactItem) return
            setContactItems(contactItem?.contactItems)
            createRecyclerAdapter()
            return
        }
        if (MainApplicationSingleton.GROUPCONTACT_DETAIL_FRAGMENT_LOADERID == loader.id) {
            val count = cursor.count
            if (count > 0 && 0 == cursor.position) {
                MsgChatGrpContactsContentProvider.fillContactThreadFromJoinCursor(
                    contactItem,
                    cursor
                )
                setContactItems(contactItem?.contactItems)
            }
            createRecyclerAdapter()
        }
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        if (MainApplicationSingleton.GROUPCONTACT_DETAIL_FRAGMENT_LOADERID == loader.id) {
            cursor = null
            setContactItems(contactItem?.contactItems)
            createRecyclerAdapter()
        }
    }

    private interface Swipeable : SwipeableItemConstants

    interface EventListener {
        fun onItemPinned(position: Int)
        fun onItemViewClicked(v: View)
        fun onUnderSwipeableViewButtonClicked(parent: View, view: View)
    }

    inner class RecyclerViewAdapter(items: Collection<ContactItem>?) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>(),
        BitmapFromUrlTask.BitmapFromUrlTaskListener,
        SwipeableItemAdapter<RecyclerView.ViewHolder> {

        private var mEventListener: EventListener? = null
        private val mSwipeableViewContainerOnClickListener: View.OnClickListener
        private val mUnderSwipeableViewButtonOnClickListener: View.OnClickListener
        private val viewItems = ArrayList<ContactItem>()
        private var bitmapFromUrlTask: BitmapFromUrlTask? = null

        init {
            if (items != null) {
                viewItems.addAll(items)
            }
            mSwipeableViewContainerOnClickListener = View.OnClickListener { v ->
                onSwipeableViewContainerClick(v)
            }
            mUnderSwipeableViewButtonOnClickListener = View.OnClickListener { v ->
                onUnderSwipeableViewButtonClick(v)
            }
        }

        fun setEventListener(eventListener: EventListener?) {
            mEventListener = eventListener
        }

        override fun onSetSwipeBackground(holder: RecyclerView.ViewHolder, position: Int, type: Int) {
            val bgRes = when (type) {
                Swipeable.DRAWABLE_SWIPE_NEUTRAL_BACKGROUND -> R.drawable.bg_swipe_item_neutral
                Swipeable.DRAWABLE_SWIPE_LEFT_BACKGROUND -> R.drawable.bg_swipe_item_left
                Swipeable.DRAWABLE_SWIPE_RIGHT_BACKGROUND -> R.drawable.bg_swipe_item_right
                else -> R.drawable.bg_swipe_item_neutral
            }
            holder.itemView.setBackgroundResource(bgRes)
        }

        override fun onGetSwipeReactionType(holder: RecyclerView.ViewHolder, position: Int, x: Int, y: Int): Int {
            if (holder is AbstractSwipeableItemViewHolder) {
                val h = holder as AbstractSwipeableItemViewHolder
                if (MainApplicationSingleton.hitTest(h.swipeableContainerView, x, y)) {
                    return Swipeable.REACTION_CAN_SWIPE_BOTH_H
                } else {
                    return Swipeable.REACTION_CAN_NOT_SWIPE_BOTH_H
                }
            } else {
                return Swipeable.REACTION_CAN_NOT_SWIPE_BOTH_H
            }
        }

        override fun onSwipeItem(holder: RecyclerView.ViewHolder, position: Int, result: Int): SwipeResultAction? {
            return when (result) {
                Swipeable.RESULT_SWIPED_LEFT -> SwipeLeftResultAction(this, position)
                Swipeable.RESULT_SWIPED_RIGHT, Swipeable.RESULT_CANCELED -> if (position != RecyclerView.NO_POSITION) {
                    UnpinResultAction(this, position)
                } else {
                    null
                }
                else -> null
            }
        }

        private fun onSwipeableViewContainerClick(v: View) {
            mEventListener?.onItemViewClicked(
                RecyclerViewAdapterUtils.getParentViewHolderItemView(v)
            )
        }

        private fun onUnderSwipeableViewButtonClick(v: View) {
            mEventListener?.onUnderSwipeableViewButtonClicked(
                RecyclerViewAdapterUtils.getParentViewHolderItemView(v),
                v
            )
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val viewHolder: RecyclerView.ViewHolder
            when (viewType) {
                R.layout.listitem_groupcontactdetail -> {
                    val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.listitem_groupcontactdetail, parent, false)
                    viewHolder = ItemViewHolder(view)
                }
                R.layout.listitem_groupcontactdetail_info -> {
                    val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.listitem_groupcontactdetail_info, parent, false)
                    viewHolder = HeaderViewHolder(view)
                }
                R.layout.list_item_groupinfo -> {
                    val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.listitem_groupcontactdetail_info, parent, false)
                    viewHolder = FooterViewHolder(view)
                }
                else -> {
                    val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.listitem_groupcontactdetail, parent, false)
                    viewHolder = ItemViewHolder(view)
                }
            }
            return viewHolder
        }

        override fun getItemCount(): Int {
            return viewItems.size + 2
        }

        fun getItem(position: Int): ContactItem? {
            return if (position in 1 until viewItems.size + 1) {
                viewItems[position - 1]
            } else {
                null
            }
        }

        override fun getItemId(position: Int): Long {
            return when {
                position == 0 -> 0
                position == viewItems.size + 1 -> position.toLong()
                else -> viewItems[position - 1]._id
            }
        }

        override fun getItemViewType(position: Int): Int {
            return when (position) {
                0 -> R.layout.listitem_groupcontactdetail_info
                viewItems.size + 1 -> R.layout.list_item_groupinfo
                else -> R.layout.listitem_groupcontactdetail
            }
        }

        override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
            when (viewHolder) {
                is HeaderViewHolder -> {
                    val holder = viewHolder
                    holder.tvHeader.text = "Add another contact"
                    val drawable = getDrawable(
                        R.drawable.ic_add_circle_outline_black_18dp,
                        getAppCompatActivity().theme
                    )
                    drawable?.bounds = Rect(0, 0, 100, 100)
                    holder.tvHeader.setCompoundDrawables(drawable, null, null, null)
                    holder.tvHeader.setOnClickListener { performAddContact() }
                }
                is FooterViewHolder -> {
                    val holder = viewHolder
                    holder.tvFooter.text = "Exit Group"
                    val drawable = getDrawable(
                        R.drawable.ic_dialog_close_light,
                        getAppCompatActivity().theme
                    )
                    drawable?.bounds = Rect(0, 0, 100, 100)
                    holder.tvFooter.setCompoundDrawables(drawable, null, null, null)
                    holder.tvFooter.setOnClickListener { performLeaveGroup() }
                    if (contactItem?.isNewGroup == true || contactItem?.isEmpty == true) {
                        holder.mView.visibility = View.GONE
                    }
                    if (newGroup) {
                        holder.tvFooter.visibility = View.GONE
                    }
                }
                is ItemViewHolder -> {
                    val holder = viewHolder
                    val item = getItem(position)
                    holder.mContainer.setOnClickListener(mSwipeableViewContainerOnClickListener)
                    holder.btnDelete.setOnClickListener(mUnderSwipeableViewButtonOnClickListener)
                    holder.btnAdmin.setOnClickListener(mUnderSwipeableViewButtonOnClickListener)

                    val swipeState = holder.swipeStateFlags

                    if ((swipeState and Swipeable.STATE_FLAG_IS_UPDATED) != 0) {
                        val bgResId = when {
                            (swipeState and Swipeable.STATE_FLAG_IS_ACTIVE) != 0 -> R.drawable.bg_item_swiping_active_state
                            (swipeState and Swipeable.STATE_FLAG_SWIPING) != 0 -> R.drawable.bg_item_swiping_state
                            else -> R.drawable.bg_item_normal_state
                        }
                        holder.mContainer.setBackgroundResource(bgResId)
                    }

                    holder.maxLeftSwipeAmount = -0.5f
                    holder.maxRightSwipeAmount = 0f
                    holder.swipeItemHorizontalSlideAmount = if (item?.isPinned == true) -0.5f else 0f

                    holder.mItem = item
                    holder.tv_Id.text = holder.mItem?._id.toString()

                    holder.btnDelete.visibility = View.GONE
                    holder.btnAdmin.visibility = View.GONE

                    if (selfContactItem != null) {
                        when {
                            "superadmin".equals(selfContactItem?.type, ignoreCase = true) -> {
                                holder.btnDelete.visibility = View.VISIBLE
                                holder.btnAdmin.visibility = View.VISIBLE
                                if ("admin".equals(holder.mItem?.type, ignoreCase = true)) {
                                    holder.btnAdmin.setText(R.string.remove_admin)
                                }
                            }
                            "admin".equals(selfContactItem?.type, ignoreCase = true) -> {
                                holder.btnDelete.visibility = View.VISIBLE
                                holder.btnAdmin.visibility = View.VISIBLE
                                if ("admin".equals(holder.mItem?.type, ignoreCase = true)) {
                                    holder.btnAdmin.setText(R.string.remove_admin)
                                }
                            }
                            "user".equals(selfContactItem?.type, ignoreCase = true) -> {
                                holder.btnDelete.visibility = View.GONE
                                holder.btnAdmin.visibility = View.GONE
                            }
                        }
                    }

                    var name = item?.name
                    if (user?.dataId == holder.mItem?.dataId) {
                        name += " (You)"
                        holder.btnDelete.visibility = View.VISIBLE
                        holder.btnDelete.setText(R.string.exit_group)
                        holder.btnAdmin.visibility = View.GONE
                    }
                    holder.tvName.text = name

                    try {
                        val pic = holder.mItem?.profilePic
                        if (null == pic || pic.isEmpty()) {
                            if (name != null && name.length > 0) {
                                val textDrawable = ColorGenerator.getTextDrawable(name)
                                textDrawable.bounds = Rect(0, 0, 100, 100)
                                holder.tvName.setCompoundDrawables(textDrawable, null, null, null)
                            }
                        } else if (pic.startsWith("http")) {
                            bitmapFromUrlTask = BitmapFromUrlTask(
                                holder.tvName,
                                pic,
                                getAppCompatActivity()
                            )
                            bitmapFromUrlTask?.setBitmapFromUrlTaskListener(this)
                            bitmapFromUrlTask?.execute()
                        } else if (pic.startsWith("/")) {
                            val bm = BitmapFactory.decodeFile(pic)
                            val croppedBitmap = NetworkImageView.getCroppedBitmap(bm, 100)
                            val drawable = BitmapDrawable(resources, croppedBitmap)
                            drawable.bounds = Rect(0, 0, 100, 100)
                            holder.tvName.setCompoundDrawables(drawable, null, null, null)
                        } else if (pic.startsWith("content")) {
                            val bm = MediaStore.Images.Media.getBitmap(
                                getAppCompatActivity().contentResolver,
                                Uri.parse(pic)
                            )
                            val croppedBitmap = NetworkImageView.getCroppedBitmap(bm, 100)
                            val drawable = BitmapDrawable(resources, croppedBitmap)
                            drawable.bounds = Rect(0, 0, 100, 100)
                            holder.tvName.setCompoundDrawables(drawable, null, null, null)
                        } else {
                            val bm = MediaStore.Images.Media.getBitmap(
                                getAppCompatActivity().contentResolver,
                                Uri.parse(pic)
                            )
                            val croppedBitmap = NetworkImageView.getCroppedBitmap(bm, 100)
                            val drawable = BitmapDrawable(resources, croppedBitmap)
                            drawable.bounds = Rect(0, 0, 100, 100)
                            holder.tvName.setCompoundDrawables(drawable, null, null, null)
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                    val rootView = holder.mView.rootView as ViewGroup
                    val linearLayout = holder.mView.findViewById<LinearLayout>(R.id.list_item_groupinfo)
                    linearLayout.removeAllViews()

                    if ("superadmin".equals(holder.mItem?.type, ignoreCase = true)) {
                        val view = LayoutInflater.from(holder.mView.context).inflate(
                            R.layout.listitem_groupcontactdetail_info,
                            rootView,
                            false
                        )
                        val tv = view.findViewById<TextView>(R.id.tv_info)
                        tv.text = holder.mItem?.type
                        linearLayout.addView(view)
                    } else if ("admin".equals(holder.mItem?.type, ignoreCase = true)) {
                        val view = LayoutInflater.from(holder.mView.context).inflate(
                            R.layout.listitem_groupcontactdetail_info,
                            rootView,
                            false
                        )
                        val tv = view.findViewById<TextView>(R.id.tv_info)
                        tv.text = holder.mItem?.type
                        linearLayout.addView(view)
                    }
                }
            }
        }

        override fun onPostBitmapFromUrlExecute(bitmap: Bitmap?, textView: View?, context: Context?) {
            val ctx = context ?: context
            if (null == ctx) return
            val resources = ctx.resources
            if (null == resources) return
            val roundedBitmap = NetworkImageView.getCroppedBitmap(bitmap, 100)
            val drawable = BitmapDrawable(resources, roundedBitmap)
            drawable.bounds = Rect(0, 0, 100, 100)
            (textView as? TextView)?.setCompoundDrawables(drawable, null, null, null)
        }

        override fun onPostBitmapFromUrlExecuteFail(bitmap: Bitmap?) {
            Log.e(TAG, "On Post Bitmap From URL Exec ERROR: $bitmap")
        }

        override fun setBitmapFromUrlTaskToNull() {
            bitmapFromUrlTask = null
        }

        private inner class SwipeLeftResultAction(
            private val mAdapter: RecyclerViewAdapter,
            private val mPosition: Int
        ) : SwipeResultActionMoveToSwipedDirection() {
            private var mSetPinned = false

            override fun onPerformAction() {
                super.onPerformAction()
                val item = mAdapter.getItem(mPosition)
                if (item?.isPinned == false) {
                    item.isPinned = true
                    mAdapter.notifyItemChanged(mPosition)
                    mSetPinned = true
                }
            }

            override fun onSlideAnimationEnd() {
                super.onSlideAnimationEnd()
                if (mSetPinned && mAdapter.mEventListener != null) {
                    mAdapter.mEventListener?.onItemPinned(mPosition)
                }
            }

            override fun onCleanUp() {
                super.onCleanUp()
            }
        }

        private inner class UnpinResultAction(
            private val mAdapter: RecyclerViewAdapter,
            private val mPosition: Int
        ) : SwipeResultActionDefault() {

            override fun onPerformAction() {
                super.onPerformAction()
                val item = mAdapter.getItem(mPosition)
                if (item?.isPinned == true) {
                    item.isPinned = false
                    mAdapter.notifyItemChanged(mPosition)
                }
            }

            override fun onCleanUp() {
                super.onCleanUp()
            }
        }

        inner class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val mView: View = view
            val tvHeader: TextView = view.findViewById(R.id.tv_info)
        }

        inner class FooterViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val mView: View = view
            val tvFooter: TextView = view.findViewById(R.id.tv_info)
        }

        inner class ItemViewHolder(view: View) : AbstractSwipeableItemViewHolder(view),
            View.OnLongClickListener {
            val mView: View = view
            val tvName: TextView = view.findViewById(R.id.tv_name)
            val tv_Id: TextView = view.findViewById(R.id.tv_id)
            val mContainer: FrameLayout = view.findViewById(R.id.container)
            var mItem: ContactItem? = null
            val btnDelete: Button = view.findViewById(R.id.btn_delete)
            val btnAdmin: Button = view.findViewById(R.id.btn_admin)

            init {
                mView.setOnLongClickListener(this)
                mView.setOnClickListener(this@MsgChatGrpContactsDetailFragment)
            }

            override fun getSwipeableContainerView(): View {
                return mContainer
            }

            override fun toString(): String {
                return super.toString() + " '" + tvName.text + "'"
            }

            override fun onLongClick(v: View): Boolean {
                setSelectedItem(mItem)
                if (null == mActionMode) {
                    mActionMode = getAppCompatActivity().startSupportActionMode(mActionModeCallback)
                    v.isSelected = !v.isSelected
                    return true
                }
                v.isSelected = !v.isSelected
                return false
            }
        }
    }
}
