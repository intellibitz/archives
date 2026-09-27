package intellibitz.intellidroid.fragment;

import android.app.Activity;
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
import android.graphics.drawable.NinePatchDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import intellibitz.intellidroid.IntellibitzActivityFragment;
import intellibitz.intellidroid.IntellibitzPermissionFragment;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.db.ContactItemColumns;
import intellibitz.intellidroid.listener.ContactHeaderListener;
import intellibitz.intellidroid.listener.ContactListener;
import intellibitz.intellidroid.task.BitmapFromUrlTask;
import intellibitz.intellidroid.task.CreateGroupTask;
import intellibitz.intellidroid.task.GroupModifyUserTypeTask;
import intellibitz.intellidroid.task.GroupRemoveUsersTask;
import intellibitz.intellidroid.task.GroupsAddUsersTask;
import intellibitz.intellidroid.task.LeaveGroupTask;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.util.MediaPicker;
import intellibitz.intellidroid.util.MediaPickerRequest;
import intellibitz.intellidroid.util.NetworkImageView;
import intellibitz.intellidroid.IntellibitzActivityFragment;
import intellibitz.intellidroid.IntellibitzPermissionFragment;
import intellibitz.intellidroid.R;
import intellibitz.intellidroid.activity.ContactSelectActivity;
import intellibitz.intellidroid.content.MsgChatGrpContactContentProvider;
import intellibitz.intellidroid.content.MsgChatGrpContactsContentProvider;
import intellibitz.intellidroid.content.task.GroupsSaveToDBTask;
import intellibitz.intellidroid.content.task.GroupsUpdateDBTask;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.db.ContactItemColumns;
import intellibitz.intellidroid.graphics.ColorGenerator;
import intellibitz.intellidroid.graphics.TextDrawable;
import intellibitz.intellidroid.listener.ContactHeaderListener;
import intellibitz.intellidroid.listener.ContactListener;
import intellibitz.intellidroid.task.BitmapFromUrlTask;
import intellibitz.intellidroid.task.CreateGroupTask;
import intellibitz.intellidroid.task.GroupModifyUserTypeTask;
import intellibitz.intellidroid.task.GroupRemoveUsersTask;
import intellibitz.intellidroid.task.GroupsAddUsersTask;
import intellibitz.intellidroid.task.LeaveGroupTask;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.util.MediaPicker;
import intellibitz.intellidroid.util.MediaPickerRequest;
import intellibitz.intellidroid.util.NetworkImageView;
import intellibitz.intellidroid.widget.advrecyclerview.animator.GeneralItemAnimator;
import intellibitz.intellidroid.widget.advrecyclerview.animator.SwipeDismissItemAnimator;
import intellibitz.intellidroid.widget.advrecyclerview.decoration.ItemShadowDecorator;
import intellibitz.intellidroid.widget.advrecyclerview.decoration.SimpleListDividerDecorator;
import intellibitz.intellidroid.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;
import intellibitz.intellidroid.widget.advrecyclerview.swipeable.SwipeableItemAdapter;
import intellibitz.intellidroid.widget.advrecyclerview.swipeable.SwipeableItemConstants;
import intellibitz.intellidroid.widget.advrecyclerview.swipeable.action.SwipeResultAction;
import intellibitz.intellidroid.widget.advrecyclerview.swipeable.action.SwipeResultActionDefault;
import intellibitz.intellidroid.widget.advrecyclerview.swipeable.action.SwipeResultActionMoveToSwipedDirection;
import intellibitz.intellidroid.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager;
import intellibitz.intellidroid.widget.advrecyclerview.utils.AbstractSwipeableItemViewHolder;
import intellibitz.intellidroid.widget.advrecyclerview.utils.RecyclerViewAdapterUtils;
import intellibitz.intellidroid.widget.advrecyclerview.utils.WrapperAdapterUtils;

import intellibitz.intellidroid.content.MsgChatGrpContactContentProvider;
import intellibitz.intellidroid.content.MsgChatGrpContactsContentProvider;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import static intellibitz.intellidroid.util.MainApplicationSingleton.ACTIVITY_CONTACTSELECT_RQ_CODE;

/**
 */
public class MsgChatGrpContactsDetailFragment extends
        IntellibitzActivityFragment implements
        ContactListener,
        View.OnClickListener,
//        GroupsDetailFragment.ItemPinnedEventListener,
        LoaderManager.LoaderCallbacks<Cursor>,
        GroupsUpdateDBTask.GroupsUpdateDBTaskListener,
        TabLayout.OnTabSelectedListener,
        GroupModifyUserTypeTask.GroupModifyUserTypeTaskListener,
        GroupRemoveUsersTask.GroupsRemoveUsersTaskListener,
        LeaveGroupTask.LeaveGroupTaskListener,
        CreateGroupTask.CreateGroupTaskListener,
        GroupsSaveToDBTask.GroupsSaveToDBTaskListener,
        GroupsAddUsersTask.GroupsAddUsersTaskListener {
    private static final String TAG = "MsgChatGrpCtsDetailFrag";
    private View snackView;

/*
    public interface ItemPinnedEventListener {
        void onNotifyItemPinnedDialogDismissed(int position, boolean ok);
    }

    // implements ItemPinnedMessageDialogFragment.EventListener
    @Override
    public void onNotifyItemPinnedDialogDismissed(int itemPosition, boolean ok) {
        viewAdapter.getBaseItem(itemPosition).setPinned(ok);
        viewAdapter.notifyItemChanged(itemPosition);
    }
*/


    private ContentObserver contentObserver;
    private RecyclerView recyclerView;
    private RecyclerViewAdapter viewAdapter;
    private RecyclerView.Adapter mWrappedAdapter;
    private RecyclerViewTouchActionGuardManager mRecyclerViewTouchActionGuardManager;
    private RecyclerViewSwipeManager mRecyclerViewSwipeManager;

    private boolean newGroup = false;

    //    private Toolbar detailFilterToolbar;
//    private TabLayout detailTabLayoutFilter;
    private NetworkImageView imageView;
    private ContactItem selfContactItem;
    private HandlerThread handlerThread;

    private Collection<ContactItem> contactItems = new ArrayList<>();

    //    private android.support.v7.app.ActionBar actionBar;
/*
    public void setActionBar(android.support.v7.app.ActionBar actionBar) {
        this.actionBar = actionBar;
    }
*/

    private Toolbar toolbar;
    private EditText etGroupName;
    private ContactItem contactItem;
    private String filter;
    private Cursor cursor;
    private ContactListener contactListener;
    private ContactHeaderListener contactHeaderListener;
    private GroupModifyUserTypeTask groupModifyUserTypeTask;
    private GroupRemoveUsersTask groupRemoveUsersTask;
    private GroupsSaveToDBTask groupsSaveToDBTask;
    private LeaveGroupTask leaveGroupTask;
    private GroupsAddUsersTask groupsAddUsersTask;
    private GroupsUpdateDBTask groupsUpdateDBTask;
    private CreateGroupTask createGroupTask;
    private ActionMode mActionMode;
    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu_context_groupcontact_details, menu);
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

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MsgChatGrpContactsDetailFragment() {
        super();
    }

    public static MsgChatGrpContactsDetailFragment newInstance(
            ContactItem contactItem, ContactItem user, ContactListener contactListener) {
        MsgChatGrpContactsDetailFragment fragment = new MsgChatGrpContactsDetailFragment();
        fragment.setContactListener(contactListener);
        if (contactListener instanceof ContactHeaderListener) {
            fragment.setContactHeaderListener((ContactHeaderListener) contactListener);
        }
        if (contactListener != null)
            fragment.setViewModeListener(contactListener);
        fragment.setUser(user);
        fragment.setContactItem(contactItem);
//        fragment.setActionBar(actionBar);
        Bundle args = new Bundle();
        args.putParcelable(ContactItem.USER_CONTACT, user);
        args.putParcelable(ContactItem.TAG, contactItem);
        fragment.setArguments(args);
        return fragment;
    }

    public void setContactItems(Collection<ContactItem> items) {
        this.contactItems.clear();
        if (items != null)
            this.contactItems.addAll(items);
    }

    public ContactListener getContactListener() {
        return contactListener;
    }

    public void setContactListener(ContactListener contactListener) {
        this.contactListener = contactListener;
    }

    public ContactHeaderListener getContactHeaderListener() {
        return contactHeaderListener;
    }

    public void setContactHeaderListener(ContactHeaderListener contactHeaderListener) {
        this.contactHeaderListener = contactHeaderListener;
    }

    public void setContactItem(ContactItem contactItem) {
        this.contactItem = contactItem;
    }

    @Override
    public void onDestroyView() {
        if (mRecyclerViewSwipeManager != null) {
            mRecyclerViewSwipeManager.release();
            mRecyclerViewSwipeManager = null;
        }

        if (mRecyclerViewTouchActionGuardManager != null) {
            mRecyclerViewTouchActionGuardManager.release();
            mRecyclerViewTouchActionGuardManager = null;
        }

        if (recyclerView != null) {
            recyclerView.setItemAnimator(null);
            recyclerView.setAdapter(null);
            recyclerView = null;
        }

        if (mWrappedAdapter != null) {
            WrapperAdapterUtils.releaseAll(mWrappedAdapter);
            mWrappedAdapter = null;
        }
        viewAdapter = null;
//        mLayoutManager = null;

        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        getAppCompatActivity().getContentResolver().unregisterContentObserver(contentObserver);
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
        getAppCompatActivity().getContentResolver().registerContentObserver(
                MsgChatGrpContactsContentProvider.CONTENT_URI, false,
                contentObserver);

    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            user = savedInstanceState.getParcelable(ContactItem.USER_CONTACT);
            contactItem = savedInstanceState.getParcelable(ContactItem.TAG);
        }
        super.onViewStateRestored(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(ContactItem.USER_CONTACT, user);
        outState.putParcelable(ContactItem.TAG, contactItem);
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_msgchatgrpcontactsdetail, container, false);
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
        if (contactItem != null) {
            newGroup = TextUtils.isEmpty(contactItem.getDataId());
        }

        setupAppBar();
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        snackView = view.findViewById(R.id.cl);
//        detailFilterToolbar = (Toolbar) view.findViewById(R.id.toolbar5);
//        detailTabLayoutFilter = (TabLayout) view.findViewById(R.id.tablayout5);

//        setupToolbars();
        setupSwipe(view);

        etGroupName = (EditText) view.findViewById(R.id.et);
        imageView = (NetworkImageView) view.findViewById(R.id.iv_group);
        if (newGroup) {
            imageView.setVisibility(View.VISIBLE);
            imageView.setImageDrawable(getDrawable(
                    R.drawable.ic_account_circle_black_24dp, getContext().getTheme()));
            setupMediaChooserOnPermissions(imageView);
            etGroupName.setVisibility(View.VISIBLE);
            etGroupName.setText(contactItem.getName());
            etGroupName.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    contactItem.setName(etGroupName.getText().toString());
//                    setupDetailTitle(contactItem, detailToolbar);
                    setupDetailTitle(contactItem, toolbar);
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
            etGroupName.requestFocus();
//            shows details filters
//            getAppCompatActivity().showDetailFilter();
        } else {
            etGroupName.setVisibility(View.GONE);
            imageView.setVisibility(View.GONE);
        }
        restartLoader();
    }

    private void setupAppBar() {
        View view = getView();
        if (view != null) {
            toolbar = (Toolbar) view.findViewById(R.id.toolbar);
            setupDetailToolbar(contactItem);
//        toolbar.setTitle(R.string.new_group);
//        toolbar.setSubtitle(R.string.app_title);
            TextView tvOk = (TextView) view.findViewById(R.id.btn_ok);
            tvOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onOkPressed();
                }
            });
            TextView tvClose = (TextView) view.findViewById(R.id.tv_close);
            tvClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onCancelPressed();
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

    public void setupToolbars() {
        setupDetailFilterToolbar();
//        getAppCompatActivity().showDetailToolbar();
//            hides item filters
//        getAppCompatActivity().hideFilters();
    }

    private void setupMediaChooserOnPermissions(View view) {
        if (IntellibitzPermissionFragment.isCameraPermissionGranted(getContext()) &&
                IntellibitzPermissionFragment.isReadPhoneStatePermissionGranted(getContext()) &&
                IntellibitzPermissionFragment.isWriteExternalStoragePermissionGranted(getContext())) {
            setupMediaChooser(view);
        } else {
            mayRequestCamera(snackView);
            mayRequestReadExternalStorage(snackView);
            mayRequestWriteExternalStorage(snackView);
        }
    }

    private void setupMediaChooser(View view) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaPicker.openMediaChooser(MsgChatGrpContactsDetailFragment.this,
                        "Choose now", new MediaPicker.OnError() {
                            @Override
                            public void onError(IOException e) {

                                Log.e("MediaPicker", "Open chooser error.", e);

                            }
                        });
            }
        });
    }

    private void displayImage(String file) {
        if (file != null && !file.isEmpty()) {
            Log.d(TAG, "Profile image change: " + file);
            imageView.setImageUrl(file,
                    MainApplicationSingleton.getInstance(getActivity()).getImageLoader());
/*
            if (file.contains("http")){

            } else {
                imageView.setImageBitmap(BitmapFactory.decodeFile(file));
            }
*/
        }
    }

    private void setupDetailFilterToolbar() {
//        Toolbar detailFilterToolbar = clearDetailFilters();
/*
        clearDetailFilters();
        Menu menu;
        detailFilterToolbar.inflateMenu(R.menu.menu_contactthread_detail);
        menu = detailFilterToolbar.getMenu();
        int size = menu.size();
        for (int i = 0; i < size; i++) {
            MenuItem item = menu.getItem(i);
            TabLayout.Tab tab = newTabFromMenuItem(item);
            detailTabLayoutFilter.addTab(tab, false);
        }
        detailTabLayoutFilter.removeOnTabSelectedListener(this);
        detailTabLayoutFilter.clearOnTabSelectedListeners();
        detailTabLayoutFilter.addOnTabSelectedListener(this);
        detailFilterToolbar.setVisibility(View.GONE);
*/
    }

    private void createRecyclerAdapter() {
        View view = getView();

        if (null == recyclerView && view != null) {
            recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        }

        if (null == recyclerView) return;

        // touch guard manager  (this class is required to suppress scrolling while swipe-dismiss animation is running)
        mRecyclerViewTouchActionGuardManager = new RecyclerViewTouchActionGuardManager();
        mRecyclerViewTouchActionGuardManager.setInterceptVerticalScrollingWhileAnimationRunning(true);
        mRecyclerViewTouchActionGuardManager.setEnabled(true);

        // swipe manager
        mRecyclerViewSwipeManager = new RecyclerViewSwipeManager();

        selfContactItem = contactItem.getContactItem(user.getDataId());

        viewAdapter = new RecyclerViewAdapter(contactItems);
        viewAdapter.setHasStableIds(true);
        viewAdapter.setEventListener(new EventListener() {
            @Override
            public void onItemPinned(int position) {
//                // TODO: 04-06-2016
//                getAppCompatActivity().onItemPinned(position);
            }

            @Override
            public void onItemViewClicked(View v) {
                handleOnItemViewClicked(v);
            }

            @Override
            public void onUnderSwipeableViewButtonClicked(View parent, View view) {
                handleOnUnderSwipeableViewButtonClicked(parent, view);
            }
        });

//        mAdapter = myItemAdapter;

        // wrap for swiping
        mWrappedAdapter = mRecyclerViewSwipeManager.createWrappedAdapter(viewAdapter);

        final GeneralItemAnimator animator = new SwipeDismissItemAnimator();

        // Change animations are enabled by default since support-v7-recyclerview v22.
        // Disable the change animation in order to make turning back animation of swiped item works properly.
        animator.setSupportsChangeAnimations(false);


        recyclerView.swapAdapter(mWrappedAdapter, true);
        recyclerView.setItemAnimator(animator);

        // additional decorations
        //noinspection StatementWithEmptyBody
        if (supportsViewElevation()) {
            // Lollipop or later has native drop shadow feature. ItemShadowDecorator is not required.
        } else {
            recyclerView.addItemDecoration(new ItemShadowDecorator(
                    (NinePatchDrawable) ContextCompat.getDrawable(
//                        // TODO: 04-06-2016
                            getContext(), R.drawable.material_shadow_z1)));
//                            getContext(), R.drawable.item_gray_bg_layer)));
        }
        recyclerView.addItemDecoration(new SimpleListDividerDecorator(
                ContextCompat.getDrawable(getContext(),
//                        // TODO: 04-06-2016
                        R.drawable.list_divider_h), true));
//                        R.drawable.item_gray_bg_layer), true));

        // NOTE:
        // The initialization order is very important! This order determines the priority of touch event handling.
        //
        // priority: TouchActionGuard > Swipe > DragAndDrop
        mRecyclerViewTouchActionGuardManager.attachRecyclerView(recyclerView);
        mRecyclerViewSwipeManager.attachRecyclerView(recyclerView);

        // for debugging
//        animator.setDebug(true);
//        animator.setMoveDuration(2000);
//        mRecyclerViewSwipeManager.setMoveToOutsideWindowAnimationDuration(2000);
//        mRecyclerViewSwipeManager.setReturnToDefaultPositionAnimationDuration(2000);


        recyclerView.scrollToPosition(mWrappedAdapter.getItemCount() - 1);
    }

    private boolean supportsViewElevation() {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);
    }

    private void handleOnItemViewClicked(View v) {
        int position = recyclerView.getChildAdapterPosition(v);
        if (position != RecyclerView.NO_POSITION) {
//            // TODO: 04-06-2016
//            getAppCompatActivity().onItemClicked(position);
            ContactItem data = viewAdapter.getItem(position);

            if (data.isPinned()) {
                // unpin if tapped the pinned item
                data.setPinned(false);
                viewAdapter.notifyItemChanged(position);
            }
        }
    }

    /**
     * handles button clicks, under the swipe view
     *
     * @param parent the view that was clicked, the relative layout in this case
     * @param view
     */
    private void handleOnUnderSwipeableViewButtonClicked(View parent, View view) {
//        swipe does not apply for new group, yet to be saved in cloud and db
        if (newGroup) return;
//        Log.d(TAG, "View: "+parent);
        int position = recyclerView.getChildAdapterPosition(parent);
        if (position != RecyclerView.NO_POSITION) {
            ContactItem contactItem = viewAdapter.getItem(position);

//            String text = "";
            if (R.id.btn_admin == view.getId()) {
                Button button = (Button) view;
//                text += button.getText();
                if (getResources().getString(R.string.make_admin).equalsIgnoreCase(
                        button.getText().toString())) {
                    makeAdmin(contactItem);
                } else if (getResources().getString(R.string.remove_admin).equalsIgnoreCase(
                        button.getText().toString())) {
                    removeAdmin(contactItem);
                }
            } else if (R.id.btn_delete == view.getId()) {
                Button button = (Button) view;
                if (getResources().getString(R.string.delete).equalsIgnoreCase(
                        button.getText().toString())) {
                    removeUser(contactItem);
                } else if (getResources().getString(R.string.exit_group).equalsIgnoreCase(
                        button.getText().toString())) {
                    performLeaveGroup();
                }
            }
//            // TODO: 04-06-2016
//            getAppCompatActivity().onItemButtonClicked(position);
//            text += " pos: " + contactItem.getDataId();

/*
            Snackbar snackbar = Snackbar.make(
                    getView().findViewById(R.id.container),
                    text,
                    Snackbar.LENGTH_SHORT);

            snackbar.show();
*/
        }
    }

    @Override
    public void onPostGroupModifyUserTypeExecute(
            JSONObject response, ContactItem item,
            final ContactItem contactItem, int flag) {
        int status = 0;
        if (response != null)
            status = response.optInt("status");
        if (null == response || -1 == status || 99 == status) {
//            retries again..
            onPostGroupModifyUserTypeExecuteFail(response, item, contactItem, flag);
        } else {
            Log.e(TAG, "SUCCESS - " + response);
            handlerThread = MainApplicationSingleton.performOnHandlerThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            MsgChatGrpContactContentProvider.updatesTypeInDB(contactItem, getContext());
                            MsgChatGrpContactsDetailFragment.this.quitHandlerThread();
                        }
                    });
        }

    }

    private void quitHandlerThread() {
        if (handlerThread != null)
            handlerThread.quit();
    }

    @Override
    public void onPostGroupModifyUserTypeExecuteFail(
            JSONObject response, ContactItem item, ContactItem contactItem, int flag) {
        Log.e(TAG, "FAIL - " + response);
    }

    @Override
    public void setGroupModifyUserTypeTaskToNull() {
        groupModifyUserTypeTask = null;
    }

    private void makeAdmin(ContactItem contactItem) {
        if (newGroup) {

        } else {
            contactItem.setType("admin");
            modifyUserType(contactItem);
        }
    }

    private void removeAdmin(ContactItem contactItem) {
        if (newGroup) {

        } else {
            contactItem.setType("user");
            modifyUserType(contactItem);
        }
    }

    private void modifyUserType(ContactItem contactItem) {
        groupModifyUserTypeTask = new GroupModifyUserTypeTask(
                this.contactItem, contactItem, -1, user.getDataId(), user.getToken(),
                user.getDevice(), user.getDeviceRef(),
                MainApplicationSingleton.AUTH_GROUP_MODIFY_USER_TYPE);
        groupModifyUserTypeTask.setGroupModifyUserTypeTaskListener(this);
        groupModifyUserTypeTask.execute();
    }

    private void removeUser(ContactItem contactItem) {
        groupRemoveUsersTask = new GroupRemoveUsersTask(
                this.contactItem, contactItem, user.getDataId(), user.getToken(),
                user.getDevice(), user.getDeviceRef(),
                MainApplicationSingleton.AUTH_GROUP_REMOVE_USERS);
        groupRemoveUsersTask.setGroupsRemoveUsersTaskListener(this);
        groupRemoveUsersTask.execute();
    }

    private void leaveGroup(ContactItem contactItem) {
        leaveGroupTask = new LeaveGroupTask(
                this.contactItem, contactItem, user.getDataId(), user.getToken(),
                user.getDevice(), user.getDeviceRef(),
                MainApplicationSingleton.AUTH_LEAVE_GROUP);
        leaveGroupTask.setLeaveGroupTaskListener(this);
        leaveGroupTask.execute();
    }

    @Override
    public void onPostLeaveGroupExecute(
            final JSONObject response, final String id, final String name,
            final ContactItem contactItem,
            final ContactItem contactThreadItem) {
        int status = 0;
        if (response != null)
            status = response.optInt("status");
        if (null == response || -1 == status || 99 == status) {
//            retries again..
            onPostLeaveGroupExecuteFail(
                    response, id, name, contactItem, contactThreadItem);
        } else {
            Log.e(TAG, "SUCCESS - " + response);
            handlerThread = MainApplicationSingleton.performOnHandlerThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            int row = MsgChatGrpContactsContentProvider.deleteContact(
                                    contactItem, contactThreadItem, getContext());
                            if (0 == row) {
                                onPostLeaveGroupExecuteFail(
                                        response, id, name,
                                        contactItem, contactThreadItem);
                            } else {
                                contactThreadItem.getContactItems().remove(contactItem);
                                contactItems.remove(contactItem);
//                                // TODO: 10-06-2016
//                                if exit group, then perform close
                            }
                            MsgChatGrpContactsDetailFragment.this.quitHandlerThread();
                        }
                    });
        }


    }

    @Override
    public void onPostLeaveGroupExecuteFail(
            JSONObject response, String id, String name,
            ContactItem contactItem, ContactItem contactThreadItem) {
//                ERROR
        Log.e(TAG, "ERROR: Exit Group - " + response + " User: " + contactItem);
    }

    @Override
    public void setLeaveGroupTaskToNull() {
        leaveGroupTask = null;
    }

    @Override
    public void onPostGroupsRemoveUsersExecute(
            final JSONObject response, final String id, final String name,
            final String[] contacts, final ContactItem contactItem,
            final ContactItem contactThreadItem) {
        int status = 0;
        if (response != null)
            status = response.optInt("status");
        if (null == response || -1 == status || 99 == status) {
//            retries again..
            onPostGroupsRemoveUsersExecuteFail(
                    response, id, name, contacts, contactItem, contactThreadItem);
        } else {
            Log.e(TAG, "SUCCESS - " + response);
            handlerThread = MainApplicationSingleton.performOnHandlerThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            int row = MsgChatGrpContactsContentProvider.deleteContact(
                                    contactItem, contactThreadItem, getContext());
                            if (0 == row) {
                                onPostGroupsRemoveUsersExecuteFail(
                                        response, id, name, contacts,
                                        contactItem, contactThreadItem);
                            } else {
                                contactThreadItem.getContactItems().remove(contactItem);
                                contactItems.remove(contactItem);
//                                // TODO: 10-06-2016
//                                if exit group, then perform close
                            }
                            MsgChatGrpContactsDetailFragment.this.quitHandlerThread();
                        }
                    });
        }

    }

    @Override
    public void onPostGroupsRemoveUsersExecuteFail(
            JSONObject response, String id, String name, String[] contacts,
            ContactItem contactItem, ContactItem contactThreadItem) {
//                ERROR
        Log.e(TAG, "ERROR: Remove User from Group - " + response + " User: " + contactItem);
    }

    @Override
    public void setGroupsRemoveUsersTaskToNull() {
        groupRemoveUsersTask = null;
    }

    private void performLeaveGroup() {
        if (newGroup) {
            performOk();
        } else {
            if (selfContactItem != null)
                leaveGroup(selfContactItem);
        }
    }

    private void performExitContact() {
        if (newGroup) {
            performOk();
        } else {
            if (selfContactItem != null)
                removeUser(selfContactItem);
        }
    }

    private void performAddContact() {
//        Log.e(TAG, "To add contact...");
        if (contactHeaderListener != null) {
            contactHeaderListener.onContactHeaderNew();
        }
        if (viewModeListener != null) {
            viewModeListener.onViewModeChanged();
        }
        startContactSelectActivity();
//        getAppCompatActivity().setBackIntent(intent);
/*
        IntellibitzContactSelectItemFragment intellibitzContactSelectItemFragment =
                ContactSelectFragment.newInstance(
                        this, getAppCompatActivity(), twoPane, user, contactItem);
*/
//        hideFilterToolbar();
//        getAppCompatActivity().showDetailToolbar();
//        getAppCompatActivity().replaceDetailFragment(intellibitzContactSelectItemFragment);
//        getAppCompatActivity().replaceChildDetailFragment(intellibitzContactSelectItemFragment, this);

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
    private void startContactSelectActivity() {
        Intent intent = new Intent(getAppCompatActivity(), ContactSelectActivity.class);
        intent.setAction(ContactSelectFragment.TAG);
        intent.putExtra(ContactItem.TAG, (Parcelable) contactItem);
        intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
        startActivityForResult(intent, MainApplicationSingleton.ACTIVITY_CONTACTSELECT_RQ_CODE);
    }

    private void createNewGroup(ContactItem contactItem) {
        createGroupTask = new CreateGroupTask(contactItem, user.getDataId(), user.getToken(),
                user.getDevice(), user.getDeviceRef(), MainApplicationSingleton.AUTH_CREATE_GROUP);
        createGroupTask.setCreateGroupTaskListener(this);
        createGroupTask.execute();
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
//        IntellibitzActivity.alertDialog(getAppCompatActivity(),"Please save group again", "Groups Save");
        showError("Groups save failed - try again");
    }

    private void showError(String s) {
        if (etGroupName != null)
            etGroupName.setError(s);
    }

    @Override
    public void onPostCreateGroupExecute(JSONObject response,
                                         String name, File file, String[] contacts,
                                         ContactItem contactItem) {
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
                    contactItem.setDataId(id);
                    contactItem.setIntellibitzId(id);
                    contactItem.setTypeId(id);
                    contactItem.setGroupId(id);
                    contactItem.setType("GROUP");
                    contactItem.setGroup(true);
                    contactItem.setEmailItem(false);
                    savesGroupsInDB(contactItem, getAppCompatActivity());
                    Log.e(TAG, "onPostCreateGroupExecute: SUCCESS - " + response);
/*
                    viewItemsMap.put(contactItem.getDataId(), contactItem);
                    saveExistingGroupsInDB(viewItemsMap, getAppCompatActivity());
                    Log.e(TAG, " GROUPS Contacts - SUCCESS - ");
                    addUsersToGroups(id, name, contacts);
*/
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG, e.getMessage());
            }
        }
    }

    public void savesGroup(ContactItem contactItem) {
        contactItem.setType("GROUP");
        contactItem.setGroup(true);
        contactItem.setEmailItem(false);
/*
        contactItem.setName(name);
        if (file != null)
            contactItem.setProfilePic(file.getAbsolutePath());
*/
    }

    public void savesGroup(String name, File file, String id) {
//        ContactItem contactItem = new ContactItem();
//        data id, intellibitz id, type id and the group id are all the same id for a chat group
        contactItem.setDataId(id);
        contactItem.setIntellibitzId(id);
        contactItem.setTypeId(id);
        contactItem.setGroupId(id);
        contactItem.setType("GROUP");
        contactItem.setGroup(true);
        contactItem.setEmailItem(false);
        contactItem.setName(name);
        if (file != null)
            contactItem.setProfilePic(file.getAbsolutePath());
/*
        if (contacts != null && contacts.length > 0) {
            Collection<ContactItem> items =
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
                items.add(contactItem);
            }
            contactItem.setContactItems(items);
        }
*/
//        contactItem.setNewGroup(true);
//        contactItem.setGroup(true);
//        contactItem.setType("GROUP");
//        contactThreadItems.add(contactItem);
//        user.getMsgContactItemHashMap().put(contactItem.getDataId(), contactItem);
        savesGroupsInDB(contactItem, getAppCompatActivity());
    }

    private void savesGroupsInDB(ContactItem contactItem, Context context) {
        groupsSaveToDBTask = new GroupsSaveToDBTask(contactItem, context);
        groupsSaveToDBTask.setGroupsSaveToDBTaskListener(this);
        groupsSaveToDBTask.execute();
    }

    @Override
    public void onPostGroupsSaveToDBExecute(Uri uri, Collection<ContactItem> contactThreadItems) {
        if (contactThreadItems != null && !contactThreadItems.isEmpty()) {
            Log.e(TAG, " GROUPS Contacts - SUCCESS - ");
            for (ContactItem contactItem : contactThreadItems) {
                if (contactItem.isNewGroup()) {
                    Collection<ContactItem> members = contactItem.getContactItems();
                    if (null == members || members.isEmpty()) {
                    } else {
                        addUsersToGroups(contactItem);
                    }
                } else {
//                gets group info
                    Collection<ContactItem> contactItems = contactItem.getContactItems();
                    if (null == contactItems || contactItems.isEmpty()) {
//                        only if members are already not retrieved
//                        group info is retrieved.. avoids recursive hell
//                        getGroupDetails(item);
                    } else {
                    }
                }
            }
        } else {
            Log.e(TAG, "Group Save has returned EMPTY contacts - PLEASE CHECK: " + uri);
        }
//        IntellibitzActivity intellibitzActivity = removeSelf();
//        if (intellibitzActivity == null) return;
//        this.onDestroy();
/*
        Intent intent = new Intent();
        intent.setAction(GroupsDetailFragment.TAG);
        intent.putExtra(ContactItem.TAG, (Parcelable) contactItem);
        getAppCompatActivity().onOkPressed(intent);
*/
        performOk();
    }

    @Override
    public void setGroupsSaveToDBTaskToNull() {
        groupsSaveToDBTask = null;
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
    public void setGroupsAddUsersTaskToNull() {
        groupsAddUsersTask = null;
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
//            processNewMessageFromAction(id, name, user, getAppCompatActivity());
//            showAllGroups();
            AppCompatActivity activity = getAppCompatActivity();
            if (activity != null) {
                Intent intent = activity.getIntent();
                intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
                intent.putExtra(ContactItem.TAG, (Parcelable) contactItem);
                activity.setResult(Activity.RESULT_OK, intent);
                activity.finish();
            }
        }
    }

    /**
     * contacts have been selected, go back to previous screen
     */
    private void onOkPressed() {
        //            do in background returns success.. handle them
//        this activity, is called for result with this intent.. so the parent is aware
        final AppCompatActivity activity = getAppCompatActivity();
        Intent intent = activity.getIntent();
//        Intent intent = new Intent();
        HashSet<ContactItem> contactItems = contactItem.getContactItems();
        if (null == contactItems || contactItems.isEmpty()) {
            intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
            intent.putExtra(ContactItem.TAG, (Parcelable) contactItem);
            activity.setResult(Activity.RESULT_CANCELED, intent);
            activity.finish();
        } else {
            int count = contactItems.size();
            if (count > 1) {
//                contactItem.setName(intellibitzContactItem.getName());
                String name = contactItem.getName();
                if (null == name || name.isEmpty()) {
                    Log.e(TAG, "Name is empty - cannot create group");
                    intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
                    intent.putExtra(ContactItem.TAG, (Parcelable) contactItem);
                    activity.setResult(Activity.RESULT_CANCELED, intent);
                    activity.finish();
                } else {
                    contactItem.setGroup(true);
                    createNewGroup(contactItem);
                }
            }
        }
//        startActivity(intent);
/*
        HashSet<ContactItem> contactItems = contactItem.getContactItems();
        if (null == contactItems || contactItems.isEmpty()) {
            onCancelPressed();
            return;
        }

        clearDetailFilters();
//        creates the group here
        int count = contactItems.size();
        if (count > 1) {
//                contactItem.setName(intellibitzContactItem.getName());
            String name = contactItem.getName();
            if (null == name || name.isEmpty()) {
                Log.e(TAG, "Name is empty - cannot create group");
//                getAppCompatActivity().onBackPressed();
                return;
            }
//            getAppCompatActivity().popFragment();
//            getAppCompatActivity().onBackPressed();
            contactItem.setGroup(true);
            createNewGroup(contactItem);
            Log.e(TAG, "onOkPressed: selected contacts for Group chat: " + count);
            return;
        }
*/
    }

    /**
     * contacts have been selected, go back to previous screen
     */
    private void onOkPressed_() {
        HashSet<ContactItem> contactItems = contactItem.getContactItems();
        if (null == contactItems || contactItems.isEmpty()) {
            onCancelPressed();
            return;
        }

//        clearDetailFilters();
//        creates the group here
        int count = contactItems.size();
        if (count > 1) {
//                contactItem.setName(intellibitzContactItem.getName());
            String name = contactItem.getName();
            if (null == name || name.isEmpty()) {
                Log.e(TAG, "Name is empty - cannot create group");
//                getAppCompatActivity().onBackPressed();
                return;
            }
//            getAppCompatActivity().popFragment();
//            getAppCompatActivity().onBackPressed();
            contactItem.setGroup(true);
            createNewGroup(contactItem);
            Log.e(TAG, "onOkPressed: selected contacts for Group chat: " + count);
            return;
        }


    }

    private void onCancelPressed() {
        final AppCompatActivity activity = getAppCompatActivity();
        Intent intent = activity.getIntent();
        intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
        intent.putExtra(ContactItem.TAG, (Parcelable) contactItem);
        activity.setResult(Activity.RESULT_CANCELED, intent);
        activity.finish();
//        performOk();
    }

/*
    public void onBackPressed(Intent intent) {
        onBackPressed();
*/
/*
        if (null == intent) {
            Log.e(TAG, " cancelled selected contacts by pressing back: ");
            return;
        } else {
            ContactItem item = intent.getParcelableExtra(ContactItem.TAG);
            if (null == item) {
                Log.e(TAG, " cancelled selected contacts by pressing back: ");
//                onBackPressed();
//                getAppCompatActivity().onBackPressed();
                return;
            }
            if (contactItem == item) {
                Log.e(TAG, " Already selected contacts: " + contactItem.getSelectedContacts().size());
            } else {
                contactItem.setSelectedContacts(item.getSelectedContacts());
            }
//            merges selected contacts into group contacts and clears selected contacts
            contactItem.mergeSelectedContacts();
            if (TextUtils.isEmpty(contactItem.getDataId())) {
// NEW group
            } else {
//                saves updated contacts in db for existing
                updateGroupsInDB(contactItem, getAppCompatActivity());
            }
            Log.e(TAG, " selected contacts: " + contactItem.getSelectedContacts().size());
        }
*//*

    }
*/

    private void performOk() {
        final AppCompatActivity activity = getAppCompatActivity();
        Intent intent = activity.getIntent();
        intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
        intent.putExtra(ContactItem.TAG, (Parcelable) contactItem);
        activity.setResult(Activity.RESULT_OK, intent);
        activity.finish();
//        getAppCompatActivity().removeFragment(this);
//        getAppCompatActivity().removeChildFragment(this);
//        onBackPressed();
//        removeSelf();
//        clearDetailFilters();
//        clears previous back intents, set by child
//        getAppCompatActivity().setBackIntent(null);
//        getAppCompatActivity().onBackPressed();
/*
        IntellibitzActivity intellibitzActivity = getAppCompatActivity();
        if (null == intellibitzActivity){
            Log.e(TAG, "onOkPressed: FAIL - intellibitz activity is NULL");
            return;
        }
        intellibitzActivity.hideDetailFilters();
//        getAppCompatActivity().removeChildFragment(this);
        Intent intent = new Intent();
        intent.setAction(GroupsDetailFragment.TAG);
//        intent.putExtra("test", "test");
        View view = getView();
        if (view != null) view.setVisibility(View.GONE);
//        intellibitzActivity.popFragment();
        intellibitzActivity.removeFragment(this);
*/
//        this.onDestroy();
//        if (viewModeListener != null) viewModeListener.onViewModeItem();
//        intellibitzActivity.onBackPressed(intent);
    }

    public boolean onBackPressed() {
        removeSelf();
//            onViewModeItem();
        if (viewModeListener != null) {
            viewModeListener.onViewModeItem();
        }
        return true;
/*
        if (null == intellibitzContactSelectItemFragment) {
        }
        if (intellibitzContactSelectItemFragment.onBackPressed()){
//            return true;
        };
*/
//        intellibitzContactSelectItemFragment = null;
//        return false;
//        performOk();
/*
        if (null == intellibitzContactSelectItemFragment) return false;
        intellibitzContactSelectItemFragment.onBackPressed();
        View view = getView();
        if (view != null) view.setVisibility(View.GONE);
        getAppCompatActivity().removeFragment(intellibitzContactSelectItemFragment);
        intellibitzContactSelectItemFragment = null;
        if (viewModeListener != null) viewModeListener.onViewModeItem();
*/
/*
        if (null == intellibitzContactSelectItemFragment) {
            IntellibitzActivity intellibitzActivity = getAppCompatActivity();
            if (null == intellibitzActivity) {
                Log.e(TAG, "onOkPressed: FAIL - intellibitz activity is NULL");
                return false;
            }
            intellibitzActivity.hideDetailFilters();
//        getAppCompatActivity().removeChildFragment(this);
            Intent intent = new Intent();
            intent.setAction(GroupsDetailFragment.TAG);
//        intent.putExtra("test", "test");
            View view = getView();
            if (view != null) view.setVisibility(View.GONE);
//        intellibitzActivity.popFragment();
            intellibitzActivity.removeFragment(this);
            return true;
        }
        View view = getView();
        if (view != null) view.setVisibility(View.GONE);
        getAppCompatActivity().removeFragment(intellibitzContactSelectItemFragment);
        intellibitzContactSelectItemFragment = null;
*/
//        this.onDestroy();
//        if (viewModeListener != null) viewModeListener.onViewModeItem();
//        intellibitzActivity.onBackPressed(intent);
//        performOk();
//        return true;
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
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
        if (MainApplicationSingleton.ACTIVITY_CONTACTSELECT_RQ_CODE == requestCode) {
            if (Activity.RESULT_OK == resultCode) {
                if (data != null) {
//                    contacts selected for chat
//                    Log.e(TAG, " selected contacts: " + contactItem.getSelectedContacts().size());
                    ContactItem item = data.getParcelableExtra(ContactItem.TAG);
                    if (null == contactItem) contactItem = item;
                    final int size = contactItem.getSelectedContacts().size();
                    if (contactItem == item) {
                        Log.e(TAG, " Already selected contacts: " + size);
                    } else {
                        contactItem.setSelectedContacts(item.getSelectedContacts());
                    }
//            merges selected contacts into group contacts and clears selected contacts
                    contactItem.mergeSelectedContacts();
                    if (TextUtils.isEmpty(contactItem.getDataId())) {
// NEW group
                        setContactItems(contactItem.getContactItems());
                        setupDetailTitle(contactItem, toolbar);
                        createRecyclerAdapter();
                    } else {
//                saves updated contacts in db for existing
                        updateGroupsInDB(contactItem, getAppCompatActivity());
                    }
//                    Log.e(TAG, " selected contacts: " + contactItem.getSelectedContacts().size());
                }
            } else if (Activity.RESULT_CANCELED == resultCode) {
                Log.e(TAG, "onActivityResult: 0 Contacts selected - ");
            }
        }

        MediaPicker.handleActivityResult(getActivity(), requestCode, resultCode, data,
                new MediaPicker.OnResult() {
                    @Override
                    public void onError(IOException e) {
                        Log.e("MediaPicker", "Got file error.", e);
                    }

                    @Override
                    public void onSuccess(File mediaFile, MediaPickerRequest request) {
                        Log.e("MediaPicker", "Got file result: " + mediaFile + " for code: " + request);
                        if (request != MediaPickerRequest.REQUEST_CROP) {
                            final int paramColor = ContextCompat.getColor(getActivity(), android.R.color.black);
                            final int paramWidth = 128;
                            final int paramHeight = 128;
                            MediaPicker.startForImageCrop(MsgChatGrpContactsDetailFragment.this, mediaFile, paramWidth,
                                    paramHeight, paramColor, new MediaPicker.OnError() {
                                        @Override
                                        public void onError(IOException e) {
                                            Log.e("MediaPicker", "Open cropper error.", e);
                                        }
                                    });
                        } else {
                            //                                final String dataUrl = MediaPickerEncoder.toDataUrl(mediaFile);
//                                Log.d(TAG, dataUrl);
                            contactItem.setProfilePic(mediaFile.getAbsolutePath());
                            // When we are done cropping, display it in the ImageView.
                            displayImage(contactItem.getProfilePic());
                            setupDetailTitle(contactItem, toolbar);
//                                notify the listener, the parent activity in this case
//                                syncs to the cloud
//                                profileImageChanged(mediaFile);
                        }
                    }

                    @Override
                    public void onCancelled() {
                        Log.e("MediaPicker", "Got cancelled event.");
                    }
                });
    }

    public void onOkPressed(Intent intent) {
        if (null == intent) {
            Log.e(TAG, " cancelled selected contacts by pressing back: ");
            return;
        } else {
            ContactItem item = intent.getParcelableExtra(ContactItem.TAG);
            if (null == item) {
                Log.e(TAG, " cancelled selected contacts by pressing back: ");
//                onBackPressed();
//                getAppCompatActivity().onBackPressed();
                return;
            }
            if (contactItem == item) {
                Log.e(TAG, " Already selected contacts: " + contactItem.getSelectedContacts().size());
            } else {
                contactItem.setSelectedContacts(item.getSelectedContacts());
            }
//            merges selected contacts into group contacts and clears selected contacts
            contactItem.mergeSelectedContacts();
            if (TextUtils.isEmpty(contactItem.getDataId())) {
// NEW group
            } else {
//                saves updated contacts in db for existing
                updateGroupsInDB(contactItem, getAppCompatActivity());
            }
            Log.e(TAG, " selected contacts: " + contactItem.getSelectedContacts().size());
        }
    }

    private void updateGroupsInDB(ContactItem contactItem, Context context) {
        groupsUpdateDBTask = new GroupsUpdateDBTask(contactItem, context);
        groupsUpdateDBTask.setGroupsUpdateDBTaskListener(this);
        groupsUpdateDBTask.execute();
    }

    @Override
    public void onPostGroupsUpdateDBExecute(int result, Collection<ContactItem> contacts) {
        contactItem = contacts.iterator().next();
    }

    @Override
    public void onPostGroupsUpdateDBExecuteFail(int result, Collection<ContactItem> contacts) {
        Log.e(TAG, " ERROR - " + result);
    }

    @Override
    public void setGroupsUpdateDBTaskToNull() {
        groupsUpdateDBTask = null;
    }

    public void setupDetailToolbar() {
        setupDetailToolbar(contactItem);
    }

    private void setupDetailToolbar(ContactItem item) {
//        detailToolbar = getAppCompatActivity().getDetailToolbar();
/*
        if (detailToolbar != null) {
//            detailToolbar.setNavigationIcon(R.drawable.ic_dialog_close_light);
            detailToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    performOk();
                }
            });
        }
*/
        setupDetailTitle(item, toolbar);
    }

    private void setupDetailTitle(ContactItem item, Toolbar toolbar) {
        View view = getView();
        if (view != null) {
            toolbar = (Toolbar) view.findViewById(R.id.toolbar);
            if (null == toolbar) return;
            String title = "New Group";
            if (item != null)
                title = item.getName();
            if (null == title || title.isEmpty()) {
                title = "New Group";
            }
            String subTitle = "Group Contact";
            String[] names = null;
            if (item != null)
                names = item.getContactsNameAsArray();
            if (null == names || 0 == names.length) {
                subTitle = "0 Contacts in Group";
            } else {
                subTitle = "";
                for (String name : names) {
                    if (name != null && !subTitle.isEmpty() && !subTitle.contains(name)) {
                        subTitle += ",";
                    }
                    subTitle += name;
                }
            }
            String pic = null;
            if (item != null) {
                pic = item.getProfilePic();
                setNavigationIcon(toolbar, pic);
            }
            TextView tvTitle = (TextView) view.findViewById(R.id.tv_subtitle);
            TextView tvSubTitle = (TextView) view.findViewById(R.id.tv_subtitle);

            tvTitle.setText(title);
            tvSubTitle.setText(subTitle);
        }
    }

    private void setNavigationIcon(final Toolbar toolbar, String pic) {
        if (pic != null && !pic.isEmpty()) {
            if (pic.startsWith("http")) {
                final BitmapFromUrlTask[] bitmapFromUrlTask = {new BitmapFromUrlTask(
                        null, pic, getAppCompatActivity())};
                bitmapFromUrlTask[0].setBitmapFromUrlTaskListener(
                        new BitmapFromUrlTask.BitmapFromUrlTaskListener() {
                            @Override
                            public void onPostBitmapFromUrlExecute(Bitmap bitmap,
                                                                   View textView, Context context) {
                                if (null == context) {
                                    context = getContext();
                                }
                                if (null == context) return;
                                Resources resources = context.getResources();
                                if (null == resources) return;
                                Bitmap roundedBitmap = NetworkImageView.getCroppedBitmap(bitmap, 100);
                                BitmapDrawable drawable = new BitmapDrawable(resources, roundedBitmap);
                                drawable.setBounds(new Rect(0, 0, 100, 100));
//                                toolbar.setIcon(drawable);
                                toolbar.setNavigationIcon(drawable);
                            }

                            @Override
                            public void onPostBitmapFromUrlExecuteFail(Bitmap response) {

                            }

                            @Override
                            public void setBitmapFromUrlTaskToNull() {
                                bitmapFromUrlTask[0] = null;
                            }
                        });
                bitmapFromUrlTask[0].execute();
            } else {
//            Drawable bitmap = BitmapDrawable.createFromPath(pic);
                Bitmap bitmap = BitmapFactory.decodeFile(pic);
                Bitmap roundedBitmap = NetworkImageView.getCroppedBitmap(bitmap, 100);
                BitmapDrawable drawable = new BitmapDrawable(getResources(), roundedBitmap);
                drawable.setBounds(new Rect(0, 0, 100, 100));
                toolbar.setNavigationIcon(drawable);
            }
        }
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

    private void restartLoader() {
        getAppCompatActivity().getSupportLoaderManager().restartLoader(
                MainApplicationSingleton.GROUPCONTACT_DETAIL_FRAGMENT_LOADERID, null, this);
//        first time init
/*
        if (null == viewAdapter) {
            getAppCompatActivity().getSupportLoaderManager().initLoader(
                    MainApplicationSingleton.GROUPCONTACT_DETAIL_FRAGMENT_LOADERID, null, this);

        } else {
            getAppCompatActivity().getSupportLoaderManager().restartLoader(
                    MainApplicationSingleton.GROUPCONTACT_DETAIL_FRAGMENT_LOADERID, null, this);
        }
*/
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        if (null == tab) return;
        if (null == tab.getTag()) return;
        if (R.id.mict_cancel == (int) tab.getTag()) {
            onCancelPressed();
            return;
        }
        if (R.id.mict_done == (int) tab.getTag()) {
            onOkPressed();
            return;
        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
        if (null == tab) return;
        if (null == tab.getTag()) return;
        if (R.id.mict_cancel == (int) tab.getTag()) {
            return;
        }
        if (R.id.mict_done == (int) tab.getTag()) {
            return;
        }

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    private void setSelectedItem(ContactItem mItem) {
//        selectedItem = userEmailItem;
    }

    private void execDeleteMsgsTask(String[] msgs) {
    }

    private void execFlagMsgsTask(String[] msgs) {
    }

    private void execUnFlagMsgsTask(String[] msgs) {
    }

    private void deleteMessages() {
//        execDeleteMsgsTask(new String[]{selectedItem.getDataId()});
    }

    private void flagMessages() {
//        execFlagMsgsTask(new String[]{selectedItem.getDataId()});
    }

    private void unflagMessages() {
//        execUnFlagMsgsTask(new String[]{selectedItem.getDataId()});
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This is called when a new Loader needs to be created.  This
        // sample only has one Loader, so we don't care about the ID.
        // First, pick the base URI to use depending on whether we are// currently filtering.
        CursorLoader cursorLoader;
        if (contactItem != null && contactItem.getDataId() != null &&
                MainApplicationSingleton.GROUPCONTACT_DETAIL_FRAGMENT_LOADERID == id) {
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
/*
//===========================================================================
//            IMPORTANT
//  JOIN QUERY - SO REMOVE AMBIGUITY BY SPECIFYING JOIN TABLE NAME
// GC AS PREFIX IS REQUIRED FOR THE QUERY TO WORK CORRECTLY
//===========================================================================
             */
            String contactThreadItemId = contactItem.getDataId();
/*
                    String selection = " ( gc.name IS NULL OR gc.name like ? ) AND " +
                            " gc." + MsgChatGrpContactsContentProvider.ContactItemColumns.KEY_IS_GROUP +
                            " = 1 AND " +
                            " ( ak." + MsgChatGrpContactsContentProvider.ContactItemColumns.KEY_IS_GROUP +
                            " = 0 OR " +
                            " ak." + MsgChatGrpContactsContentProvider.ContactItemColumns.KEY_IS_GROUP +
                            " IS NULL ) " +
                            "AND  ( gc." +
                            MsgChatGrpContactsContentProvider.ContactItemColumns.KEY_DATA_ID +
                            " = ? )";
*/
            String selection = " ( gc.name IS NULL OR gc.name like ? ) "
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
/*
                            " AND " +
                            " ( ak." + MsgChatGrpContactsContentProvider.ContactItemColumns.KEY_IS_GROUP +
                            " = 1 OR " +
                            " ak." + MsgChatGrpContactsContentProvider.ContactItemColumns.KEY_IS_GROUP +
                            " IS NULL ) " +
*/
                    " AND  ( gc." + ContactItemColumns.KEY_DATA_ID +
                    " = ? )";
            String[] selArgs;
            if (filter != null && !filter.isEmpty()) {
                selArgs = new String[]{"%" + filter + "%", contactThreadItemId};
            } else {
                selArgs = new String[]{"%%", contactThreadItemId};
            }
//            Log.d(TAG, "FILTER: " + filter + " " + selArgs + " " + selection);
            cursorLoader = new CursorLoader(getAppCompatActivity(),
                    MsgChatGrpContactsContentProvider.JOIN_CONTENT_URI,
                    null,
                    selection, selArgs,
                    "gc." + ContactItemColumns.KEY_TIMESTAMP +
                            " DESC");
        } else {
//        returns an empty dummy cursor.. for the cursor loader to play game
            cursorLoader = new CursorLoader(getAppCompatActivity(),
                    MsgChatGrpContactsContentProvider.JOIN_CONTENT_URI,
                    null,
                    "gc." + ContactItemColumns.KEY_DATA_ID + " = ? ",
                    new String[]{"0"}, null);
        }
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        this.cursor = cursor;
        if (null == cursor) {
            if (null == contactItem) return;
//            handles.. fresh new group logic.. the entity is not in db yet.. so cursor null
//            contactItem.fillMsgContactItemHashMap();
            setContactItems(contactItem.getContactItems());
            createRecyclerAdapter();
            return;
        }
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        if (MainApplicationSingleton.GROUPCONTACT_DETAIL_FRAGMENT_LOADERID == loader.getId()) {
            int count = cursor.getCount();
/*
            if (messageTopicListener != null) {
                messageTopicListener.onMessageTopicsLoaded(count);
            }
*/
//            if (0 == count)
//                showEmpty("Please add Email Accounts to see conversations. You can select a Contact to send chat");
            if (count > 0 && 0 == cursor.getPosition()) {
//                hideEmpty();
//                    contactItems.clear();
//                ArrayList<DeviceContactItem> viewItemsMap = new ArrayList<>(count);
//                    DeviceContactItem contactItem = new DeviceContactItem();
//                contactItem.getContactItems().clear();
                MsgChatGrpContactsContentProvider.fillContactThreadFromJoinCursor(
                        contactItem, cursor);
//                contactItem.fillMsgContactItemHashMap();
                setContactItems(contactItem.getContactItems());
//                cursor.close();
            }
            createRecyclerAdapter();
        }
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        if (MainApplicationSingleton.GROUPCONTACT_DETAIL_FRAGMENT_LOADERID == loader.getId()) {
//            set cursor to null, to avoid memory leak
            cursor = null;
//            refreshes view, for every data change.. this might not be required since adapter is
//            loaded on finish
//            handles.. fresh new group logic.. the entity is not in db yet.. so cursor null
//            contactItem.fillMsgContactItemHashMap();
            setContactItems(contactItem.getContactItems());
            createRecyclerAdapter();
        }
    }

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

    // NOTE: Make accessible with short name
    private interface Swipeable extends SwipeableItemConstants {
    }

    public interface EventListener {
        void onItemPinned(int position);

        void onItemViewClicked(View v);

        void onUnderSwipeableViewButtonClicked(View parent, View view);
    }

    public class RecyclerViewAdapter extends
            RecyclerView.Adapter<RecyclerView.ViewHolder> implements
            BitmapFromUrlTask.BitmapFromUrlTaskListener,
            SwipeableItemAdapter<RecyclerView.ViewHolder> {
        private EventListener mEventListener;
        private View.OnClickListener mSwipeableViewContainerOnClickListener;
        private View.OnClickListener mUnderSwipeableViewButtonOnClickListener;
        private ArrayList<ContactItem> viewItems = new ArrayList<>();
        private BitmapFromUrlTask bitmapFromUrlTask;

        public RecyclerViewAdapter(Collection<ContactItem> items) {
            if (items != null) {
                this.viewItems.addAll(items);
            }
            mSwipeableViewContainerOnClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onSwipeableViewContainerClick(v);
                }
            };
            mUnderSwipeableViewButtonOnClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onUnderSwipeableViewButtonClick(v);
                }
            };
        }

        public void setEventListener(EventListener eventListener) {
            mEventListener = eventListener;
        }

        @Override
        public void onSetSwipeBackground(RecyclerView.ViewHolder holder, int position, int type) {
            int bgRes = 0;
            switch (type) {
                case Swipeable.DRAWABLE_SWIPE_NEUTRAL_BACKGROUND:
                    bgRes = R.drawable.bg_swipe_item_neutral;
//                    bgRes = R.drawable.item_gray_bg_layer;
                    break;
                case Swipeable.DRAWABLE_SWIPE_LEFT_BACKGROUND:
                    bgRes = R.drawable.bg_swipe_item_left;
//                    bgRes = R.drawable.item_gray_bg_layer;
                    break;
                case Swipeable.DRAWABLE_SWIPE_RIGHT_BACKGROUND:
                    bgRes = R.drawable.bg_swipe_item_right;
//                    bgRes = R.drawable.item_gray_bg_layer;
                    break;
            }

            holder.itemView.setBackgroundResource(bgRes);
        }

        @Override
        public int onGetSwipeReactionType(RecyclerView.ViewHolder holder, int position, int x, int y) {
            if (holder instanceof AbstractSwipeableItemViewHolder) {
                AbstractSwipeableItemViewHolder h = (AbstractSwipeableItemViewHolder) holder;
                if (MainApplicationSingleton.hitTest(h.getSwipeableContainerView(), x, y)) {
                    return Swipeable.REACTION_CAN_SWIPE_BOTH_H;
                } else {
                    return Swipeable.REACTION_CAN_NOT_SWIPE_BOTH_H;
                }
            } else {
                return Swipeable.REACTION_CAN_NOT_SWIPE_BOTH_H;
            }
        }

        @Override
        public SwipeResultAction onSwipeItem(
                RecyclerView.ViewHolder holder, int position, int result) {
//            Log.d(TAG, "onSwipeItem(position = " + position + ", result = " + result + ")");

            switch (result) {
                // swipe left --- pin
                case Swipeable.RESULT_SWIPED_LEFT:
                    return new SwipeLeftResultAction(this, position);
                // other --- do nothing
                case Swipeable.RESULT_SWIPED_RIGHT:
                case Swipeable.RESULT_CANCELED:
                default:
                    if (position != RecyclerView.NO_POSITION) {
                        return new UnpinResultAction(this, position);
                    } else {
                        return null;
                    }
            }
        }

        private void onSwipeableViewContainerClick(View v) {
            if (mEventListener != null) {
                mEventListener.onItemViewClicked(
                        RecyclerViewAdapterUtils.getParentViewHolderItemView(v));
            }
        }

        private void onUnderSwipeableViewButtonClick(View v) {
            if (mEventListener != null) {
                mEventListener.onUnderSwipeableViewButtonClicked(
                        RecyclerViewAdapterUtils.getParentViewHolderItemView(v), v);
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            RecyclerView.ViewHolder viewHolder;
            if (R.layout.listitem_groupcontactdetail == viewType) {
//                ROW ITEMS
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.listitem_groupcontactdetail, parent, false);
                viewHolder = new ItemViewHolder(view);
            } else if (R.layout.listitem_groupcontactdetail_info == viewType) {
//                HEADER
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.listitem_groupcontactdetail_info, parent, false);
                viewHolder = new HeaderViewHolder(view);
            } else if (R.layout.list_item_groupinfo == viewType) {
//                FOOTER
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.listitem_groupcontactdetail_info, parent, false);
                viewHolder = new FooterViewHolder(view);
            } else {
//                DEFAULT
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.listitem_groupcontactdetail, parent, false);
                viewHolder = new ItemViewHolder(view);
            }
            return viewHolder;
        }

        @Override
        public int getItemCount() {
//            adds the header & the footer
            return viewItems.size() + 2;
        }

        public ContactItem getItem(int position) {
//            deducts the header & the footer
            return viewItems.get(position - 1);
        }

        @Override
        public long getItemId(int position) {
//            deducts the header & footer
            return (0 == position ? 0 :
                    (viewItems.size() + 1 == position ? position :
                            viewItems.get(position - 1).get_id()));
        }

        @Override
        public int getItemViewType(int position) {
            if (0 == position) {
//            the first item.. header
//                NOTE: the layout used below is only for an ID.. can be any unique id
                return R.layout.listitem_groupcontactdetail_info;
            }
            if (viewItems.size() + 1 == position) {
//            the last item.. footer
//                NOTE: the layout used below is only for an ID.. can be any unique id
                return R.layout.list_item_groupinfo;
            }
            return R.layout.listitem_groupcontactdetail;
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int position) {
            if (viewHolder instanceof HeaderViewHolder) {
                HeaderViewHolder holder = ((HeaderViewHolder) viewHolder);
                holder.tvHeader.setText("Add another contact");
                Drawable drawable = getDrawable(R.drawable.ic_add_circle_outline_black_18dp,
                        getAppCompatActivity().getTheme());
                drawable.setBounds(new Rect(0, 0, 100, 100));
                holder.tvHeader.setCompoundDrawables(drawable, null, null, null);
                holder.tvHeader.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        performAddContact();
                    }
                });
            } else if (viewHolder instanceof FooterViewHolder) {
                FooterViewHolder holder = ((FooterViewHolder) viewHolder);
                holder.tvFooter.setText("Exit Group");
                Drawable drawable = getDrawable(R.drawable.ic_dialog_close_light,
                        getAppCompatActivity().getTheme());
                drawable.setBounds(new Rect(0, 0, 100, 100));
                holder.tvFooter.setCompoundDrawables(drawable, null, null, null);
                holder.tvFooter.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        performLeaveGroup();
                    }
                });
                if (contactItem.isNewGroup() || contactItem.isEmpty()) {
                    holder.mView.setVisibility(View.GONE);
                }
                if (newGroup)
                    holder.tvFooter.setVisibility(View.GONE);
            } else if (viewHolder instanceof ItemViewHolder) {
                ItemViewHolder holder = ((ItemViewHolder) viewHolder);
//                deduct the header.. hence position -1
                final ContactItem item = getItem(position);

                // set listeners
                // (if the item is *pinned*, click event comes to the mContainer)
                holder.mContainer.setOnClickListener(mSwipeableViewContainerOnClickListener);
                holder.btnDelete.setOnClickListener(mUnderSwipeableViewButtonOnClickListener);
                holder.btnAdmin.setOnClickListener(mUnderSwipeableViewButtonOnClickListener);

                // set text
//                holder.mTextView.setText(item.getText());

                // set background resource (target view ID: container)
                final int swipeState = holder.getSwipeStateFlags();

                if ((swipeState & Swipeable.STATE_FLAG_IS_UPDATED) != 0) {
                    int bgResId;
//// TODO: 04-06-2016
                    if ((swipeState & Swipeable.STATE_FLAG_IS_ACTIVE) != 0) {
                        bgResId = R.drawable.bg_item_swiping_active_state;
//                        bgResId = R.drawable.item_gray_bg_layer;
                    } else if ((swipeState & Swipeable.STATE_FLAG_SWIPING) != 0) {
//                        bgResId = R.drawable.item_gray_bg_layer;
                        bgResId = R.drawable.bg_item_swiping_state;
                    } else {
                        bgResId = R.drawable.bg_item_normal_state;
//                        bgResId = R.drawable.item_gray_bg_layer;
                    }

                    holder.mContainer.setBackgroundResource(bgResId);
                }

                // set swiping properties
                holder.setMaxLeftSwipeAmount(-0.5f);
                holder.setMaxRightSwipeAmount(0);
                holder.setSwipeItemHorizontalSlideAmount(
                        item.isPinned() ? -0.5f : 0);


                holder.mItem = item;
                holder.tv_Id.setText(String.valueOf(holder.mItem.get_id()));

                holder.btnDelete.setVisibility(View.GONE);
                holder.btnAdmin.setVisibility(View.GONE);

//                checks user access roles
                if (selfContactItem != null) {
                    if ("superadmin".equalsIgnoreCase(selfContactItem.getType())) {
                        holder.btnDelete.setVisibility(View.VISIBLE);
                        holder.btnAdmin.setVisibility(View.VISIBLE);
                        if ("admin".equalsIgnoreCase(holder.mItem.getType())) {
                            holder.btnAdmin.setText(R.string.remove_admin);
                        }
                    } else if ("admin".equalsIgnoreCase(selfContactItem.getType())) {
                        holder.btnDelete.setVisibility(View.VISIBLE);
                        holder.btnAdmin.setVisibility(View.VISIBLE);
                        if ("admin".equalsIgnoreCase(holder.mItem.getType())) {
                            holder.btnAdmin.setText(R.string.remove_admin);
                        }
                    } else if ("user".equalsIgnoreCase(selfContactItem.getType())) {
                        holder.btnDelete.setVisibility(View.GONE);
                        holder.btnAdmin.setVisibility(View.GONE);

                    } else {

                    }
                }

                String name = item.getName();
//                checks self, to identify self actions
                if (user.getDataId().equals(holder.mItem.getDataId())) {
                    name += " (You)";
                    holder.btnDelete.setVisibility(View.VISIBLE);
                    holder.btnDelete.setText(R.string.exit_group);
                    holder.btnAdmin.setVisibility(View.GONE);
                }
                holder.tvName.setText(name);

//            HashSet<IntellibitzContactItem> contactItems = contactItem.getContactItems();
//                Set<MobileItem> mobiles = new HashSet<>();
//                Set<EmailItem> emails = new HashSet<>();
/*
            if (null == name && !mobiles.isEmpty()) {
                name = mobiles.iterator().next().getMobile();
            }
            if (null == name && !emails.isEmpty()) {
                name = emails.iterator().next().getEmail();
            }
*/
//            sets the contact image
                try {
                    String pic = holder.mItem.getProfilePic();
                    if (null == pic || pic.isEmpty()) {
                        if (name != null && name.length() > 0) {
                            TextDrawable textDrawable = ColorGenerator.getTextDrawable(name);
                            textDrawable.setBounds(new Rect(0, 0, 100, 100));
                            holder.tvName.setCompoundDrawables(textDrawable, null, null, null);
                        }
                    } else if (pic.startsWith("http")) {
//                    cloud pic
                        bitmapFromUrlTask = new BitmapFromUrlTask(
                                holder.tvName, pic, getAppCompatActivity());
                        bitmapFromUrlTask.setBitmapFromUrlTaskListener(this);
                        bitmapFromUrlTask.execute();
                    } else if (pic.startsWith("/")) {
//                    storage pic
                        Bitmap bm = BitmapFactory.decodeFile(pic);
                        Bitmap croppedBitmap = NetworkImageView.getCroppedBitmap(bm, 100);
                        Drawable drawable = new BitmapDrawable(getResources(), croppedBitmap);
                        drawable.setBounds(new Rect(0, 0, 100, 100));
                        holder.tvName.setCompoundDrawables(drawable, null, null, null);
                    } else if (pic.startsWith("content")) {
//                    db content pic
                        Bitmap bm = MediaStore.Images.Media.getBitmap(
                                getAppCompatActivity().getContentResolver(), Uri.parse(pic));
                        Bitmap croppedBitmap = NetworkImageView.getCroppedBitmap(bm, 100);
                        BitmapDrawable drawable = new BitmapDrawable(getResources(), croppedBitmap);
                        drawable.setBounds(new Rect(0, 0, 100, 100));
                        holder.tvName.setCompoundDrawables(drawable, null, null, null);
                    } else {
                        Bitmap bm = MediaStore.Images.Media.getBitmap(
                                getAppCompatActivity().getContentResolver(), Uri.parse(pic));
                        Bitmap croppedBitmap = NetworkImageView.getCroppedBitmap(bm, 100);
                        BitmapDrawable drawable = new BitmapDrawable(getResources(), croppedBitmap);
                        drawable.setBounds(new Rect(0, 0, 100, 100));
                        holder.tvName.setCompoundDrawables(drawable, null, null, null);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ViewGroup rootView = (ViewGroup) holder.mView.getRootView();
                LinearLayout linearLayout = (LinearLayout)
                        holder.mView.findViewById(R.id.list_item_groupinfo);
//            clears old views
                linearLayout.removeAllViews();

                if ("superadmin".equalsIgnoreCase(holder.mItem.getType())) {
                    View view = LayoutInflater.from(holder.mView.getContext()).inflate(
                            R.layout.listitem_groupcontactdetail_info,
                            rootView, false);
                    TextView tv = (TextView) view.findViewById(R.id.tv_info);
                    tv.setText(holder.mItem.getType());
//                adds the dynamic view to the dynamic layout
                    linearLayout.addView(view);

                } else if ("admin".equalsIgnoreCase(holder.mItem.getType())) {
                    View view = LayoutInflater.from(holder.mView.getContext()).inflate(
                            R.layout.listitem_groupcontactdetail_info,
                            rootView, false);
                    TextView tv = (TextView) view.findViewById(R.id.tv_info);
                    tv.setText(holder.mItem.getType());
//                adds the dynamic view to the dynamic layout
                    linearLayout.addView(view);

                }


/*
            for (final IntellibitzContactItem item : contactItems) {
                final MobileItem mobileItem = item.getMobileItem();
                View view = LayoutInflater.from(holder.mView.getContext()).inflate(
                        R.layout.listitem_groupcontactdetail_info,
                        rootView, false);
                TextView tv = (TextView) view.findViewById(R.id.tv_contactinfo);
                tv.setText(item.getName() + ":" + item.getIntellibitzId());
*/
/*
                tv.setText(mobileItem.getMobile());
*//*

*/
/*
                ImageView iv = (ImageView) view.findViewById(R.id.iv_contactinfo);
                iv.setImageDrawable(getDrawable(
                        R.drawable.ic_chat_black_24dp, getTheme()));
                Bitmap bm = MediaStore.Images.Media.getBitmap(getContentResolver(),
                        Uri.parse(pic));
*//*


                Drawable drawable = getDrawable(R.drawable.ic_chat_black_24dp,
                        getAppCompatActivity().getTheme());
                assert drawable != null;
                drawable.setBounds(new Rect(0, 0, 40, 40));
                tv.setCompoundDrawablesRelative(null, null, drawable, null);
                tv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "Clicked view: " + v);
//                        contactItem.setSelectedId(item.getIntellibitzId());
//                        contactItem.setMobileItem(item);
                        Intent intent =
                                new Intent(MainApplicationSingleton.BROADCAST_CONTACT_PHONE_SELECTED);
                        intent.putExtra(MainApplicationSingleton.MOBILE_PARAM, mobileItem.getMobile());
                        intent.putExtra(ContactItem.TAG, (Parcelable) user);
                        intent.putExtra(ContactItem.DEVICE_CONTACT, (Parcelable) contactItem);
                        if (shared != null && shared.getExtras() != null) {
                            intent.putExtras(shared.getExtras());
                        }
                        LocalBroadcastManager.getInstance(
                                getAppCompatActivity()).sendBroadcast(intent);
//                        finishes.. so doesn't show up in back stack
//                        finish();
                    }
                });
                linearLayout.addView(view);
            }
*/

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
                        getAppCompatActivity().getTheme());
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
                                getAppCompatActivity()).sendBroadcast(intent);
//                        finishes.. so doesn't show up in back stack
//                        finish();
                    }
                });
                linearLayout.addView(view);
            }
*/
            }
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

        private class SwipeLeftResultAction extends SwipeResultActionMoveToSwipedDirection {
            private final int mPosition;
            private RecyclerViewAdapter mAdapter;
            private boolean mSetPinned;

            SwipeLeftResultAction(RecyclerViewAdapter adapter, int position) {
                mAdapter = adapter;
                mPosition = position;
            }

            @Override
            protected void onPerformAction() {
                super.onPerformAction();

                ContactItem item = mAdapter.getItem(mPosition);

                if (!item.isPinned()) {
                    item.setPinned(true);
                    mAdapter.notifyItemChanged(mPosition);
                    mSetPinned = true;
                }
            }

            @Override
            protected void onSlideAnimationEnd() {
                super.onSlideAnimationEnd();

                if (mSetPinned && mAdapter.mEventListener != null) {
                    mAdapter.mEventListener.onItemPinned(mPosition);
                }
            }

            @Override
            protected void onCleanUp() {
                super.onCleanUp();
                // clear the references
                mAdapter = null;
            }
        }

        private class UnpinResultAction extends SwipeResultActionDefault {
            private final int mPosition;
            private RecyclerViewAdapter mAdapter;

            UnpinResultAction(RecyclerViewAdapter adapter, int position) {
                mAdapter = adapter;
                mPosition = position;
            }

            @Override
            protected void onPerformAction() {
                super.onPerformAction();

                ContactItem item = mAdapter.getItem(mPosition);
                if (item.isPinned()) {
                    item.setPinned(false);
                    mAdapter.notifyItemChanged(mPosition);
                }
            }

            @Override
            protected void onCleanUp() {
                super.onCleanUp();
                // clear the references
                mAdapter = null;
            }
        }

        public class HeaderViewHolder extends
                RecyclerView.ViewHolder {
            //            the views
            public final View mView;
            public final TextView tvHeader;

            public HeaderViewHolder(View view) {
                super(view);
                mView = view;
//                mView.setOnClickListener(GroupsDetailFragment.this);
                tvHeader = (TextView) view.findViewById(R.id.tv_info);
            }
        }

        public class FooterViewHolder extends
                RecyclerView.ViewHolder {
            //            the views
            public final View mView;
            public final TextView tvFooter;

            public FooterViewHolder(View view) {
                super(view);
                mView = view;
//                mView.setOnClickListener(GroupsDetailFragment.this);
                tvFooter = (TextView) view.findViewById(R.id.tv_info);
            }
        }

        public class ItemViewHolder extends
                AbstractSwipeableItemViewHolder implements
                View.OnLongClickListener {
            //            the views
            public final View mView;
            public final TextView tvName;
            public final TextView tv_Id;
            public FrameLayout mContainer;
            //            the data
            public ContactItem mItem;
            public Button btnDelete;
            public Button btnAdmin;

            public ItemViewHolder(View view) {
                super(view);
                mView = view;
                mView.setOnLongClickListener(this);
                mView.setOnClickListener(MsgChatGrpContactsDetailFragment.this);
                mContainer = (FrameLayout) view.findViewById(R.id.container);
                tv_Id = (TextView) view.findViewById(R.id.tv_id);
                tvName = (TextView) view.findViewById(R.id.tv_name);
                btnDelete = (Button) view.findViewById(R.id.btn_delete);
//                btnDelete.setTag(R.id.btn_delete);
                btnAdmin = (Button) view.findViewById(R.id.btn_admin);
//                btnAdmin.setTag(R.id.btn_admin);
            }

            @Override
            public View getSwipeableContainerView() {
                return mContainer;
            }

            @Override
            public String toString() {
                return super.toString() + " '" + tvName.getText() + "'";
            }

            @Override
            public boolean onLongClick(View v) {
                setSelectedItem(mItem);
                if (null == mActionMode) {
                    // Start the CAB using the ActionMode.Callback defined above
                    mActionMode = getAppCompatActivity().startSupportActionMode(mActionModeCallback);
//                view.setSelected(true);
                    v.setSelected(!v.isSelected());
                    return true;
                }
                v.setSelected(!v.isSelected());
                return false;
            }
        }
    }


}
