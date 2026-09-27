package intellibitz.intellidroid.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import intellibitz.intellidroid.IntellibitzActivityFragment;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.db.ContactItemColumns;
import intellibitz.intellidroid.graphics.ColorGenerator;
import intellibitz.intellidroid.graphics.TextDrawable;
import intellibitz.intellidroid.listener.ContactListener;
import intellibitz.intellidroid.listener.DeviceContactTopicListener;
import intellibitz.intellidroid.task.BitmapFromUrlTask;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.util.NetworkImageView;
import intellibitz.intellidroid.IntellibitzActivityFragment;
import intellibitz.intellidroid.R;
import intellibitz.intellidroid.content.DeviceContactContentProvider;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.db.ContactItemColumns;
import intellibitz.intellidroid.graphics.ColorGenerator;
import intellibitz.intellidroid.graphics.TextDrawable;
import intellibitz.intellidroid.listener.ContactListener;
import intellibitz.intellidroid.listener.DeviceContactTopicListener;
import intellibitz.intellidroid.service.ContactService;
import intellibitz.intellidroid.task.BitmapFromUrlTask;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.util.NetworkImageView;

import intellibitz.intellidroid.content.DeviceContactContentProvider;
import intellibitz.intellidroid.service.ContactService;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.NavUtils;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

/**
 *
 */
public class DeviceContactsFragment extends
        IntellibitzActivityFragment implements
        SearchView.OnQueryTextListener,
        SearchView.OnCloseListener,
        View.OnClickListener,
        LoaderManager.LoaderCallbacks<Cursor> {
//        Toolbar.OnMenuItemClickListener,
//        View.OnClickListener {
//        TabLayout.OnTabSelectedListener {

    public static final String TAG = "DeviceContactsFrag";
    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;
    private ContentObserver contentObserver;

    private Intent shared;

    private View view;
    private String onQueryTextSubmit;
    private ContactListener contactListFragment;
    private RecyclerView recyclerView;
    private RecyclerViewAdapter viewAdapter;
    private SparseArray<ContactItem> contactItems = new SparseArray<>();
    private String filter;

    private DeviceContactTopicListener deviceContactTopicListener;

    public DeviceContactsFragment() {
        super();
    }

    public static DeviceContactsFragment newInstance(
            ContactItem user, ContactListener contactListener) {
        DeviceContactsFragment fragment = new DeviceContactsFragment();
        Bundle args = new Bundle();
        args.putParcelable(ContactItem.USER_CONTACT, user);
        fragment.setContactListFragment(contactListener);
        if (contactListener instanceof DeviceContactTopicListener)
            fragment.setDeviceContactTopicListener((DeviceContactTopicListener) contactListener);
        fragment.setUser(user);
        fragment.setArguments(args);
        return fragment;
    }

    public void setDeviceContactTopicListener(DeviceContactTopicListener deviceContactTopicListener) {
        this.deviceContactTopicListener = deviceContactTopicListener;
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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(ContactItem.USER_CONTACT, user);
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_devicecontacts, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        if (null == savedInstanceState) {
            user = getArguments().getParcelable(ContactItem.USER_CONTACT);
//            setupToolbar();
            FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
            assert fab != null;
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                syncs only if db count and cloud count differs
//                // TODO: 18-05-2016
//                do this based on user event
                    ContactService.asyncUpdateContactsByCount(
                            user, getActivity().getApplicationContext());
                }
            });
        } else {
            user = savedInstanceState.getParcelable(ContactItem.USER_CONTACT);
        }
        setupSwipe(view);
        if (DeviceContactContentProvider.isContactsEmptyInDB(getActivity())) {
            ContactService.asyncUpdateContacts(user, getActivity());
        }
        restartLoader();
    }

    private void restartLoader(String query) {
        filter = query;
        restartLoader();
    }

    private void restartLoader() {
        getActivity().getSupportLoaderManager().restartLoader(
                MainApplicationSingleton.CONTACTITEM_FRAGMENT_LOADERID, null, this);
/*
        if (null == viewAdapter) {
            getActivity().getSupportLoaderManager().initLoader(
                    MainApplicationSingleton.CONTACTITEM_FRAGMENT_LOADERID, null, this);
        } else {
            getActivity().getSupportLoaderManager().restartLoader(
                    MainApplicationSingleton.CONTACTITEM_FRAGMENT_LOADERID, null, this);
        }
*/
//        resets the view, only if the viewItems are null
//        // TODO: 06-04-2016
//        for a specific item change.. implement other methods
/*
        if (isReadyToLoad()) {
            getActivity().getSupportLoaderManager().restartLoader(
                    MainApplicationSingleton.CONTACTITEM_FRAGMENT_LOADERID, null, this);
        } else {
            createRecyclerAdapter();
        }
*/
    }

    private boolean isReadyToLoad() {
        return (null == contactItems || 0 == contactItems.size()) && getActivity() != null;
    }


    public void createRecycleAdapter() {
        if (null == recyclerView && view != null) {
            recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        }
        if (null == recyclerView) return;
        viewAdapter = new RecyclerViewAdapter(MainApplicationSingleton.asList(contactItems));
        viewAdapter.setHasStableIds(true);
        recyclerView.swapAdapter(viewAdapter, true);
        recyclerView.scrollToPosition(viewAdapter.getItemCount() - 1);
    }

    public void setupSwipe(View view) {
        final SwipeRefreshLayout refreshLayout = (SwipeRefreshLayout)
                view.findViewById(R.id.swiperefresh);
        if (refreshLayout != null) {
            refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                private final Handler mHandler = new Handler();

                @Override
                public void onRefresh() {
                    ContactService.asyncUpdateContactsByCount(user, getContext());
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
            toolbar.setTitle(R.string.toolbar_importcontacts_title);
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

    public ContactListener getContactListFragment() {
        return contactListFragment;
    }

    public void setContactListFragment(ContactListener contactListFragment) {
        this.contactListFragment = contactListFragment;
    }

    private void showContactDetail(String id, ContactItem user) {
        ContactItem deviceContactItem = contactItems.get(Integer.valueOf(id));
        showContactDetail(deviceContactItem, user);
    }

    private void showContactDetail(ContactItem deviceContactItem, ContactItem user) {
        deviceContactTopicListener.onDeviceContactTopicClicked(deviceContactItem, user);
    }

    @Override
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

    public boolean onBackPressed() {
        return false;
    }

/*
    public void onBackPressed(Intent intent) {

    }
*/

    public void onOkPressed(Intent intent) {

    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        restartLoader(query);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        restartLoader(newText);
        return true;
    }

    @Override
    public boolean onClose() {
        restartLoader("");
        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This is called when a new Loader needs to be created.  This
        // sample only has one Loader, so we don't care about the ID.
        // First, pick the base URI to use depending on whether we are// currently filtering.
        if (MainApplicationSingleton.CONTACTITEM_FRAGMENT_LOADERID == id) {
/*
            if (messageTopicListener != null) {
                messageTopicListener.onMessageTopicsLoaded(0);
                showEmpty("Please add Email Accounts to see conversations. You can select a Contact to send chat");
            }
*/
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
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        if (MainApplicationSingleton.CONTACTITEM_FRAGMENT_LOADERID == loader.getId()) {
            if (null == cursor) {
                if (null == contactItems) contactItems = new SparseArray<>();
                contactItems.clear();
                createRecycleAdapter();
                return;
            }
            int count = cursor.getCount();
/*
            if (messageTopicListener != null) {
                messageTopicListener.onMessageTopicsLoaded(count);
            }
*/
            if (0 == count) {
//                showEmpty("Please add Email Accounts to see conversations.
// You can select a Contact to send chat");
//            cursor might be obsolete.. traversed.. already used
                if (null == contactItems) contactItems = new SparseArray<>();
                contactItems.clear();
                createRecycleAdapter();
                return;
            }
            if (count > 0 && 0 == cursor.getPosition()) {
                fillItemsFromCursor(cursor);
                cursor.close();
                createRecycleAdapter();
            }
        }
    }

    public void fillItemsFromCursor(Cursor cursor) {
//        // TODO: 30-06-2016
        this.contactItems = DeviceContactContentProvider.fillDeviceContactItemFromCursor(cursor);
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        if (MainApplicationSingleton.CONTACTITEM_FRAGMENT_LOADERID == loader.getId()) {
            if (null == contactItems) contactItems = new SparseArray<>();
            contactItems.clear();
//            refreshes view, for every data change.. this might not be required since adapter is
//            loaded on finish
            createRecycleAdapter();
        }
    }

    public void onTrimMemory(int level) {
        Log.d(TAG, "onTrimMemory: " + level);

    }

    public class RecyclerViewAdapter
            extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> implements
            BitmapFromUrlTask.BitmapFromUrlTaskListener {

        private ArrayList<ContactItem> viewItems = new ArrayList<>();
        private BitmapFromUrlTask bitmapFromUrlTask;

        public RecyclerViewAdapter(Collection<ContactItem> items) {
            if (items != null) {
                this.viewItems.addAll(items);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_devicecontacts_rv, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            final ContactItem deviceContactItem = viewItems.get(position);
            holder.mItem = deviceContactItem;
            holder.tvId.setText(String.valueOf(holder.mItem.getDeviceContactId()));
            String name = "";
            String status = getString(R.string.available_in_intellibitz);
            String date = getString(R.string.today);

            if (!TextUtils.isEmpty(holder.mItem.getName())) {
                name = holder.mItem.getName();
            }
            if (MainApplicationSingleton.isEmpty(name) && !TextUtils.isEmpty(holder.mItem.getDisplayName()))
                name = holder.mItem.getDisplayName();
            if (TextUtils.isEmpty(name) && !TextUtils.isEmpty(holder.mItem.getFirstName())) {
                name = holder.mItem.getFirstName();
                if (!TextUtils.isEmpty(holder.mItem.getLastName())) {
                    name += " " + holder.mItem.getLastName();
                }
            }
            if (!TextUtils.isEmpty(holder.mItem.getStatus())) {
                status = holder.mItem.getStatus();
            }
            if (!TextUtils.isEmpty(holder.mItem.getDateTime())) {
                date = holder.mItem.getDateTime();
            }
/*
            Set<MobileItem> mobiles = deviceContactItem.getMobiles();
            Set<EmailItem> emails = deviceContactItem.getEmails();
*/
            JSONArray mobiles = deviceContactItem.getMobiles();
            JSONArray emails = deviceContactItem.getEmails();
            if (TextUtils.isEmpty(name) && mobiles.length() > 0) {
                try {
                    name = mobiles.getString(0);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if (TextUtils.isEmpty(name) && emails.length() > 0) {
                try {
                    name = emails.getString(0);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
//            sets the contact image
            Drawable picDraw = getDrawable(R.drawable.default_profile_thread,
                    getActivity().getTheme());
//            picDraw.setBounds(new Rect(0, 0, 100, 100));
            holder.ivProfile.setImageDrawable(picDraw);
            try {
                String pic = holder.mItem.getProfilePic();
/*
                Uri uri = Uri.parse("/test/uri");
                if (pic != null){
                    uri = Uri.parse(pic);
                }
                Log.e(TAG, uri.toString());
*/
                if (null == pic || pic.isEmpty()) {
//                    holder.tvName.setCompoundDrawables(picDraw, null, null, null);
/*
                    TextDrawable textDrawable = ColorGenerator.getTextDrawable(name);
                    textDrawable.setBounds(new Rect(0, 0, 100, 100));
                    holder.tvTo.setCompoundDrawablesRelative(
                            textDrawable, null, null, null);
*/
                } else if (pic.startsWith("http")) {
                    bitmapFromUrlTask = new BitmapFromUrlTask(
                            holder.ivProfile, pic, getActivity().getApplicationContext());
                    bitmapFromUrlTask.setBitmapFromUrlTaskListener(this);
                    bitmapFromUrlTask.execute();
/*
                    new AsyncGettingBitmapFromUrl(
                            holder.tvTo, pic, getActivity().getApplicationContext()).execute();
*/
                } else {
//                    Uri uri = Uri.parse(pic);
                    Bitmap bitmap = MainApplicationSingleton.getBitmapDecodeAnyUri(pic, getContext());
                    if (null == bitmap) {
                        TextDrawable drawable = ColorGenerator.getTextDrawable(name);
                        setImageDrawable(holder.ivProfile, bitmap);
/*
                        drawable.setBounds(new Rect(0, 0, 100, 100));
                        holder.tvName.setCompoundDrawablesRelative(drawable, null, null, null);
*/
                    } else {
                        Bitmap croppedBitmap = NetworkImageView.getCroppedBitmap(bitmap, 100);
                        setImageDrawable(holder.ivProfile, croppedBitmap);
/*
                        BitmapDrawable drawable = new BitmapDrawable(getResources(), croppedBitmap);
                        drawable.setBounds(new Rect(0, 0, 100, 100));
                        holder.tvName.setCompoundDrawablesRelative(drawable, null, null, null);
*/
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, e.getMessage());
            }
            holder.tvName.setText(name);
            holder.tvStatus.setText(status);
            holder.tvDate.setText(date);

/*
            ViewGroup rootView = (ViewGroup) holder.mView.getRootView();
            LinearLayout linearLayout = (LinearLayout)
                    rootView.findViewById(R.id.list_item_contactinfo);
//            clears old views
            linearLayout.removeAllViews();
//            for (final MobileItem item : mobiles) {
            if (mobiles != null && mobiles.length() > 0) {
                for (int i = 0; i < mobiles.length(); i++) {
                    try {
                        final String mobile = mobiles.getString(i);
                        View view = LayoutInflater.from(holder.mView.getContext()).inflate(
                                R.layout.list_item_contactinfo,
                                rootView, false);
                        TextView tv = (TextView) view.findViewById(R.id.tv_contactinfo);
                        tv.setText(mobile);
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
                        drawable.setBounds(new Rect(0, 0, 40, 40));
                        tv.setCompoundDrawablesRelative(null, null, drawable, null);
                        tv.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
//                        Log.d(TAG, "Clicked view: " + v);
//                        // TODO: 30-06-2016
//                        intellibitz contact to be clickcable
//                        non intellibitz to be invited
//                        deviceContactItem.setSelectedId(item.getIntellibitzId());
//                        deviceContactItem.setSelectedMobile(item);
                                Intent intent =
                                        new Intent(MainApplicationSingleton.BROADCAST_CONTACT_PHONE_SELECTED);
                                intent.putExtra(MainApplicationSingleton.MOBILE_PARAM, mobile);
                                intent.putExtra(ContactItem.TAG, (Parcelable) user);
                                intent.putExtra(ContactItem.DEVICE_CONTACT, (Parcelable) deviceContactItem);
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
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

//            for (final EmailItem item : emails) {
            if (emails != null && emails.length() > 0) {
                for (int i = 0; i < emails.length(); i++) {
                    try {
                        final String email = emails.getString(i);
                        View view = LayoutInflater.from(holder.mView.getContext()).inflate(
                                R.layout.list_item_contactinfo,
                                rootView, false);
                        TextView tv = (TextView) view.findViewById(R.id.tv_contactinfo);
                        tv.setText(email);
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
//                        Log.d(TAG, "Clicked view: " + v);
                                Intent intent = new Intent(
                                        MainApplicationSingleton.BROADCAST_CONTACT_EMAIL_SELECTED);
                                intent.putExtra(MainApplicationSingleton.EMAIL_PARAM, email);
                                intent.putExtra(ContactItem.DEVICE_CONTACT, (Parcelable) deviceContactItem);
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
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
*/
        }

        @Override
        public int getItemCount() {
            return viewItems.size();
        }

        @Override
        public long getItemId(int position) {
            return viewItems.get(position).getDeviceContactId();
        }

        @Override
        public void onPostBitmapFromUrlExecute(Bitmap bitmap, View view, Context context) {
            if (null == context) return;
            Resources resources = context.getResources();
            if (null == resources) return;
            Bitmap croppedBitmap = NetworkImageView.getCroppedBitmap(bitmap, 100);
            setImageDrawable(view, croppedBitmap);
/*
            BitmapDrawable drawable = getBitmapDrawable(croppedBitmap);
            if (drawable != null) {
                drawable.setBounds(new Rect(0, 0, 100, 100));
            }

            if (textView instanceof ImageView){
                ((ImageView) textView).setImageBitmap(croppedBitmap);
            }
*/
/*
            ContactItem intellibitzContactItem = (ContactItem) textView.getTag();
            Drawable chatDrawable;
            if (intellibitzContactItem != null && intellibitzContactItem.isEmailItem()) {
                chatDrawable = getDrawable(R.drawable.ic_email_black_24dp);
            } else {
                chatDrawable = getDrawable(R.drawable.ic_chat_bubble_outline_black_18dp);
            }
            if (chatDrawable != null) {
                chatDrawable.setBounds(new Rect(0, 0, 100, 100));
            }
            ((TextView) textView).setCompoundDrawablesRelative(drawable, null, chatDrawable, null);
*/
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
            public final ImageView ivProfile;
            public final TextView tvId;
            public final TextView tvName;
            public final TextView tvStatus;
            public final TextView tvLastSeen;
            public final TextView tvDate;
            //            the data
            public ContactItem mItem;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mView.setOnClickListener(DeviceContactsFragment.this);
                ivProfile = (ImageView) view.findViewById(R.id.iv_contact);
                tvId = (TextView) view.findViewById(R.id.tv_id);
                tvName = (TextView) view.findViewById(R.id.tv_name);
                tvStatus = (TextView) view.findViewById(R.id.tv_status);
                tvLastSeen = (TextView) view.findViewById(R.id.tv_lastseen);
                tvDate = (TextView) view.findViewById(R.id.tv_date);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + tvName.getText() + "'";
            }
        }
    }


}
