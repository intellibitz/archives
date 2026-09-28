package intellibitz.intellidroid.fragment

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.database.ContentObserver
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Parcelable
import android.text.TextUtils
import android.util.Log
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.appcompat.widget.SearchView
import androidx.core.app.NavUtils
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import intellibitz.intellidroid.IntellibitzActivityFragment
import intellibitz.intellidroid.R
import intellibitz.intellidroid.content.DeviceContactContentProvider
import intellibitz.intellidroid.content.IntellibitzContactContentProvider
import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.db.ContactItemColumns
import intellibitz.intellidroid.graphics.ColorGenerator
import intellibitz.intellidroid.graphics.TextDrawable
import intellibitz.intellidroid.listener.ContactListener
import intellibitz.intellidroid.listener.IntellibitzContactTopicListener
import intellibitz.intellidroid.service.ContactService
import intellibitz.intellidroid.task.BitmapFromUrlTask
import intellibitz.intellidroid.util.MainApplicationSingleton
import intellibitz.intellidroid.util.NetworkImageView
import org.json.JSONArray
import org.json.JSONException
import java.io.IOException
import java.util.ArrayList

/**
 *
 */
class IntellibitzContactsFragment : IntellibitzActivityFragment(),
    SearchView.OnQueryTextListener,
    SearchView.OnCloseListener,
    View.OnClickListener,
    LoaderManager.LoaderCallbacks<Cursor> {
    //        Toolbar.OnMenuItemClickListener,
    //        View.OnClickListener {
    //        TabLayout.OnTabSelectedListener {
    companion object {
        const val TAG = "IntellibitzContactsFrag"

        /**
         * Id to identity READ_CONTACTS permission request.
         */
        private const val REQUEST_READ_CONTACTS = 0
    }

    private var contentObserver: ContentObserver? = null

    private var recyclerView: RecyclerView? = null
    private var sharedIntent: Intent? = null

    private var view: View? = null
    private var onQueryTextSubmit: String? = null
    private var contactListFragment: ContactListener? = null
    private var viewAdapter: RecyclerViewAdapter? = null
    private var intellibitzContactItems: SparseArray<ContactItem> = SparseArray()
    private var filter: String? = null

    private var intellibitzContactTopicListener: IntellibitzContactTopicListener? = null

    constructor() : super()

    fun setIntellibitzContactTopicListener(intellibitzContactTopicListener: IntellibitzContactTopicListener?) {
        this.intellibitzContactTopicListener = intellibitzContactTopicListener
    }

    fun setSharedIntent(sharedIntent: Intent?) {
        this.sharedIntent = sharedIntent
    }

    fun onNewMenuClicked() {

    }

    override fun onDestroy() {
        activity?.contentResolver?.unregisterContentObserver(contentObserver)
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
        activity?.contentResolver?.registerContentObserver(
            DeviceContactContentProvider.CONTENT_URI, false,
            contentObserver
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(ContactItem.USER_CONTACT, user)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        view = inflater.inflate(R.layout.fragment_intellibitzcontacts, container, false)
        return view
    }

    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //                syncs only if db count and cloud count differs
        //                do this based on user event
        //        // TODO: 17/9/16
        //        comment out this hack
        //        ContactService.asyncUpdateWorkContacts(user, getContext());
        recyclerView = view.findViewById(R.id.recyclerview)
        if (null == savedInstanceState) {
            user = arguments?.getParcelable(ContactItem.USER_CONTACT)
            //            setupToolbar();
        } else {
            user = savedInstanceState.getParcelable(ContactItem.USER_CONTACT)
        }
        setupSwipe(view)
        restartLoader()
    }

    private fun restartLoader(query: String?) {
        filter = query
        restartLoader()
    }

    private fun restartLoader() {
        activity?.supportLoaderManager?.restartLoader(
            MainApplicationSingleton.FAV_CONTACTITEM_FRAGMENT_LOADERID, null, this
        )
        /*
        if (null == viewAdapter) {
            getActivity().getSupportLoaderManager().initLoader(
                    MainApplicationSingleton.FAV_CONTACTITEM_FRAGMENT_LOADERID, null, this);
        } else {

            getActivity().getSupportLoaderManager().restartLoader(
                    MainApplicationSingleton.FAV_CONTACTITEM_FRAGMENT_LOADERID, null, this);
        }
        */
        //        resets the view, only if the viewItems are null
        //        // TODO: 06-04-2016
        //        for a specific item change.. implement other methods
        /*
        if (isReadyToLoad()) {
            getActivity().getSupportLoaderManager().restartLoader(
                    MainApplicationSingleton.FAV_CONTACTITEM_FRAGMENT_LOADERID, null, this);
        } else {
            createRecyclerAdapter();
        }
        */
    }

    private fun isReadyToLoad(): Boolean {
        return (null == intellibitzContactItems || 0 == intellibitzContactItems.size()) && activity != null
    }

    fun createRecycleAdapter() {
        if (null == recyclerView && view != null)
            recyclerView = view?.findViewById(R.id.recyclerview)
        if (null == recyclerView) return
        viewAdapter = RecyclerViewAdapter(MainApplicationSingleton.asList(intellibitzContactItems))
        viewAdapter?.setHasStableIds(true)
        recyclerView?.swapAdapter(viewAdapter, true)
        recyclerView?.scrollToPosition(viewAdapter?.itemCount?.minus(1) ?: 0)
    }

    fun setupSwipe(view: View) {
        val refreshLayout = view.findViewById<SwipeRefreshLayout>(R.id.swiperefresh)
        if (refreshLayout != null) {
            refreshLayout.setOnRefreshListener {
                ContactService.asyncUpdateWorkContacts(user, context)
                // Post a delayed runnable to reset the refreshing state in 2 seconds
                Handler().postDelayed({
                    refreshLayout.isRefreshing = false
                }, 2000)
            }
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
            NavUtils.navigateUpFromSameTask(activity)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupRecyclerView(@NonNull recyclerView: RecyclerView,
                                 contactItems: HashMap<Long, ContactItem>) {
        createRecycleAdapter()
        if (contactItems.isEmpty()) {
            Snackbar.make(recyclerView,
                "Please click refresh button to see the latest updated contacts",
                Snackbar.LENGTH_INDEFINITE)
                .setAction(android.R.string.ok, null).show()
        }
    }

    fun getContactListFragment(): ContactListener? {
        return contactListFragment
    }

    fun setContactListFragment(contactListFragment: ContactListener?) {
        this.contactListFragment = contactListFragment
    }

    private fun showContactDetail(id: String, user: ContactItem?) {
        val contactItem = intellibitzContactItems.get(id.toInt())
        showContactDetail(contactItem, user)
    }

    private fun showContactDetail(contactItem: ContactItem?, user: ContactItem?) {
        intellibitzContactTopicListener?.onIntellibitzContactTopicClicked(contactItem, user)
    }

    override fun onClick(v: View) {
        if (R.id.fab == v.id) {
            ContactService.asyncUpdateContacts(
                user, activity?.applicationContext
            )
            return
        }
        //        Log.d(TAG, "Clicked view: " + v);
        val dataId = v.findViewById<TextView>(R.id.tv_id)
        val id = dataId.text.toString()
        showContactDetail(id, user)
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

    fun onBackPressed(): Boolean {
        return false
    }

    fun onOkPressed(intent: Intent?) {

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
        // This is called when a new Loader needs to be created.  This
        // sample only has one Loader, so we don't care about the ID.
        // First, pick the base URI to use depending on whether we are// currently filtering.
        if (MainApplicationSingleton.FAV_CONTACTITEM_FRAGMENT_LOADERID == id) {
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
            //===========================================================================
            //            IMPORTANT
            //  JOIN QUERY - SO REMOVE AMBIGUITY BY SPECIFYING JOIN TABLE NAME
            // CT AS PREFIX IS REQUIRED FOR THE QUERY TO WORK CORRECTLY
            //===========================================================================
            val selection = " ( name IS NULL OR name like ? ) AND " +
                    " id <> ? AND " +
                    //                    " ( is_device = 1 ) AND " +
                    " ( is_cloud = 1 ) AND " +
                    " ( work_contact = 1 ) AND " +
                    " ( is_group = 0 or is_group IS NULL ) "
            /*
                        + " AND ";
                    selection += " ( " +
                            DatabaseHelper.ContactItemColumns.KEY_IS_INTELLIBITZ +
                            " = 1 )"
            */
            val selArgs: Array<String>
            if (filter != null && !filter.isEmpty()) {
                selArgs = arrayOf("%" + filter + "%", user?.dataId ?: "")
                //                selArgs = new String[]{"%" + filter + "%"};
            } else {
                selArgs = arrayOf("%%", user?.dataId ?: "")
                //                selArgs = new String[]{"%%"};
            }
            //            Log.d(TAG, "FILTER: " + filter + " " + selArgs + " " + selection);
            return CursorLoader(activity,
                IntellibitzContactContentProvider.JOIN_CONTENT_URI,
                null,
                selection, selArgs,
                ContactItemColumns.KEY_NAME +
                        " DESC")
        }
        //        returns an empty dummy cursor.. for the cursor loader to play game
        return CursorLoader(activity,
            IntellibitzContactContentProvider.JOIN_CONTENT_URI,
            null,
            ContactItemColumns.KEY_DATA_ID + " = ? ",
            arrayOf("0"), null)
    }

    override fun onLoadFinished(loader: Loader<Cursor>, cursor: Cursor?) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        if (MainApplicationSingleton.FAV_CONTACTITEM_FRAGMENT_LOADERID == loader.id) {
            if (null == cursor) {
                if (null == intellibitzContactItems) intellibitzContactItems = SparseArray()
                intellibitzContactItems.clear()
                createRecycleAdapter()
                return
            }
            val count = cursor.count
            /*
                        if (messageTopicListener != null) {
                            messageTopicListener.onMessageTopicsLoaded(count);
                        }
            */
            if (0 == count) {
                //                showEmpty("Please add Email Accounts to see conversations.
                // You can select a Contact to send chat");
                //            cursor might be obsolete.. traversed.. already used
                if (null == intellibitzContactItems) intellibitzContactItems = SparseArray()
                intellibitzContactItems.clear()
                createRecycleAdapter()
                return
            }
            if (count > 0 && 0 == cursor.position) {
                fillItemsFromCursor(cursor)
                cursor.close()
                createRecycleAdapter()
            }
        }
    }

    fun fillItemsFromCursor(cursor: Cursor) {
        this.intellibitzContactItems =
            IntellibitzContactContentProvider.fillIntellibitzContactItemFromCursor(cursor)
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        if (MainApplicationSingleton.FAV_CONTACTITEM_FRAGMENT_LOADERID == loader.id) {
            if (null == intellibitzContactItems) intellibitzContactItems = SparseArray()
            intellibitzContactItems.clear()
            //            refreshes view, for every data change.. this might not be required since adapter is
            //            loaded on finish
            createRecycleAdapter()
        }
    }

    fun onTrimMemory(level: Int) {
        Log.d(TAG, "onTrimMemory: $level")
    }

    inner class RecyclerViewAdapter(items: Collection<ContactItem>?) :
        RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>(),
        BitmapFromUrlTask.BitmapFromUrlTaskListener {

        private val viewItems: ArrayList<ContactItem> = ArrayList()
        private var bitmapFromUrlTask: BitmapFromUrlTask? = null

        init {
            if (items != null) {
                this.viewItems.addAll(items)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_intellibitzcontacts_rv, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val workContactItem = viewItems[position]
            holder.mItem = workContactItem
            holder.tvId.text = holder.mItem?._id.toString()
            var name = ""
            var status = getString(R.string.available_in_intellibitz)
            var date = getString(R.string.today)

            if (!TextUtils.isEmpty(holder.mItem?.name)) {
                name = holder.mItem?.name ?: ""
            }
            if (MainApplicationSingleton.isEmpty(name) && !TextUtils.isEmpty(holder.mItem?.displayName))
                name = holder.mItem?.displayName ?: ""
            if (TextUtils.isEmpty(name) && !TextUtils.isEmpty(holder.mItem?.firstName)) {
                name = holder.mItem?.firstName ?: ""
                if (!TextUtils.isEmpty(holder.mItem?.lastName)) {
                    name += " " + holder.mItem?.lastName
                }
            }
            if (!TextUtils.isEmpty(holder.mItem?.status)) {
                status = holder.mItem?.status ?: ""
            }
            if (!TextUtils.isEmpty(holder.mItem?.dateTime)) {
                date = holder.mItem?.dateTime ?: ""
            }
            /*
                        Set<MobileItem> mobiles = deviceContactItem.getMobiles();
                        Set<EmailItem> emails = deviceContactItem.getEmails();
            */
            val mobiles = workContactItem.mobiles
            val emails = workContactItem.emails
            if (TextUtils.isEmpty(name) && mobiles.length() > 0) {
                try {
                    name = mobiles.getString(0)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
            if (TextUtils.isEmpty(name) && emails.length() > 0) {
                try {
                    name = emails.getString(0)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
            //            sets the contact image
            val picDraw = activity?.getDrawable(R.drawable.default_profile_thread)
            //            picDraw.setBounds(new Rect(0, 0, 100, 100));
            holder.ivProfile.setImageDrawable(picDraw)
            try {
                val pic = holder.mItem?.profilePic
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
                    bitmapFromUrlTask = BitmapFromUrlTask(
                        holder.ivProfile, pic, activity?.applicationContext
                    )
                    bitmapFromUrlTask?.setBitmapFromUrlTaskListener(this)
                    bitmapFromUrlTask?.execute()
                    /*
                                        new AsyncGettingBitmapFromUrl(
                                                holder.tvTo, pic, getActivity().getApplicationContext()).execute();
                    */
                } else {
                    //                    Uri uri = Uri.parse(pic);
                    val bitmap = MainApplicationSingleton.getBitmapDecodeAnyUri(pic, context)
                    if (null == bitmap) {
                        val drawable = ColorGenerator.getTextDrawable(name)
                        setImageDrawable(holder.ivProfile, bitmap)
                        /*
                                                drawable.setBounds(new Rect(0, 0, 100, 100));
                                                holder.tvName.setCompoundDrawablesRelative(drawable, null, null, null);
                        */
                    } else {
                        val croppedBitmap = NetworkImageView.getCroppedBitmap(bitmap, 100)
                        setImageDrawable(holder.ivProfile, croppedBitmap)
                        /*
                                                BitmapDrawable drawable = new BitmapDrawable(getResources(), croppedBitmap);
                                                drawable.setBounds(new Rect(0, 0, 100, 100));
                                                holder.tvName.setCompoundDrawablesRelative(drawable, null, null, null);
                        */
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e(TAG, e.message ?: "")
            }
            //            if (!TextUtils.isEmpty(status)) name += " ( is ) " + status;
            holder.tvName.text = name
            holder.tvStatus.text = status
            holder.tvDate.text = date
            /*
                        holder.tvName.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                broadcastContactForChat(intellibitzContactItem);
                            }
                        });
            */

            /*
                        ViewGroup rootView = (ViewGroup) holder.mView.getRootView();
                        LinearLayout linearLayout = (LinearLayout)
                                rootView.findViewById(R.id.list_item_contactinfo);
                        //            clears old views
                        linearLayout.removeAllViews();
                        //            for (final MobileItem mobileItem : mobiles)
                        {
                            View view = LayoutInflater.from(holder.mView.getContext()).inflate(
                                    R.layout.list_item_contactinfo,
                                    rootView, false);
                            TextView tv = (TextView) view.findViewById(R.id.tv_contactinfo);
                            tv.setText(intellibitzContactItem.getIntellibitzId());
                            linearLayout.addView(view);
                        }
            */
            /*
                            ImageView iv = (ImageView) view.findViewById(R.id.iv_contactinfo);
                            iv.setImageDrawable(getDrawable(
                                    R.drawable.ic_chat_black_24dp, getTheme()));
                            Bitmap bm = MediaStore.Images.Media.getBitmap(getContentResolver(),
                                    Uri.parse(pic));
            */
            /*
                            Drawable drawable = getDrawable(R.drawable.ic_chat_black_24dp,
                                    getActivity().getTheme());
                            assert drawable != null;
                            drawable.setBounds(new Rect(0, 0, 80, 80));
                            tv.setCompoundDrawablesRelative(null, null, drawable, null);
                            tv.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    broadcastContactForChat(mobileItem, intellibitzContactItem);
                                }
                            });
                        }
            */

        }

        fun broadcastContactForChat(intellibitzContactItem: ContactItem?) {
            //                        Log.d(TAG, "Clicked view: " + v);
            //                        intellibitzContactItem.setSelectedId(mobileItem.getIntellibitzId());
            //                        intellibitzContactItem.setSelectedMobile(mobileItem);
            val intent =
                Intent(MainApplicationSingleton.BROADCAST_CONTACT_PHONE_SELECTED)
            intent.putExtra(MainApplicationSingleton.MOBILE_PARAM, intellibitzContactItem?.intellibitzId)
            intent.putExtra(ContactItem.USER_CONTACT, user as Parcelable)
            intent.putExtra(ContactItem.INTELLIBITZ_CONTACT, intellibitzContactItem as Parcelable)
            if (sharedIntent != null && sharedIntent?.extras != null) {
                intent.putExtras(sharedIntent?.extras!!)
            }
            LocalBroadcastManager.getInstance(
                activity?.applicationContext!!
            ).sendBroadcast(intent)
            //                        finishes.. so doesn't show up in back stack
            //                        finish();
        }

        override fun getItemCount(): Int {
            return viewItems.size
        }

        override fun getItemId(position: Int): Long {
            return viewItems[position]._id
        }

        override fun onPostBitmapFromUrlExecute(bitmap: Bitmap?, view: View?, context: Context?) {
            if (null == context) return
            val resources = context.resources
            if (null == resources) return
            val croppedBitmap = NetworkImageView.getCroppedBitmap(bitmap, 100)
            setImageDrawable(view, croppedBitmap)
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

        override fun onPostBitmapFromUrlExecuteFail(bitmap: Bitmap?) {
            Log.e(TAG, "On Post Bitmap From URL Exec ERROR: $bitmap")
        }

        override fun setBitmapFromUrlTaskToNull() {
            bitmapFromUrlTask = null
        }

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            //            the views
            val mView: View = view
            val ivProfile: ImageView = view.findViewById(R.id.iv_contact)
            val tvId: TextView = view.findViewById(R.id.tv_id)
            val tvName: TextView = view.findViewById(R.id.tv_name)
            val tvStatus: TextView = view.findViewById(R.id.tv_status)
            val tvLastSeen: TextView = view.findViewById(R.id.tv_lastseen)
            val tvDate: TextView = view.findViewById(R.id.tv_date)
            //            the data
            var mItem: ContactItem? = null

            init {
                mView.setOnClickListener(this@IntellibitzContactsFragment)
            }

            override fun toString(): String {
                return super.toString() + " '" + tvName.text + "'"
            }
        }
    }

    companion object {
        fun newInstance(user: ContactItem?, contactListener: ContactListener?): IntellibitzContactsFragment {
            val fragment = IntellibitzContactsFragment()
            val args = Bundle()
            args.putParcelable(ContactItem.USER_CONTACT, user)
            fragment.setContactListFragment(contactListener)
            if (contactListener is IntellibitzContactTopicListener)
                fragment.setIntellibitzContactTopicListener(contactListener)
            fragment.setUser(user)
            fragment.arguments = args
            return fragment
        }
    }
}
