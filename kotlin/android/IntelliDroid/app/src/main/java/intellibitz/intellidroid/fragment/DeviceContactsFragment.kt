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
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import intellibitz.intellidroid.IntellibitzActivityFragment
import intellibitz.intellidroid.R
import intellibitz.intellidroid.content.DeviceContactContentProvider
import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.db.ContactItemColumns
import intellibitz.intellidroid.graphics.ColorGenerator
import intellibitz.intellidroid.graphics.TextDrawable
import intellibitz.intellidroid.listener.ContactListener
import intellibitz.intellidroid.listener.DeviceContactTopicListener
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
class DeviceContactsFragment : IntellibitzActivityFragment(),
    SearchView.OnQueryTextListener,
    SearchView.OnCloseListener,
    View.OnClickListener,
    LoaderManager.LoaderCallbacks<Cursor> {

    companion object {
        const val TAG = "DeviceContactsFrag"
        /**
         * Id to identity READ_CONTACTS permission request.
         */
        private const val REQUEST_READ_CONTACTS = 0
    }

    private var contentObserver: ContentObserver? = null
    private var shared: Intent? = null
    private var view: View? = null
    private var onQueryTextSubmit: String? = null
    private var contactListFragment: ContactListener? = null
    private var recyclerView: RecyclerView? = null
    private var viewAdapter: RecyclerViewAdapter? = null
    private var contactItems: SparseArray<ContactItem> = SparseArray()
    private var filter: String? = null
    private var deviceContactTopicListener: DeviceContactTopicListener? = null

    constructor() : super()

    fun setDeviceContactTopicListener(deviceContactTopicListener: DeviceContactTopicListener) {
        this.deviceContactTopicListener = deviceContactTopicListener
    }

    fun onNewMenuClicked() {
        // Empty implementation
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
        outState.putParcelable(ContactItem.USER_CONTACT, user)
        super.onSaveInstanceState(outState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        view = inflater.inflate(R.layout.fragment_devicecontacts, container, false)
        return view
    }

    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.recyclerview)
        if (null == savedInstanceState) {
            user = arguments?.getParcelable(ContactItem.USER_CONTACT)
            val fab = view.findViewById<FloatingActionButton>(R.id.fab)
            fab.setOnClickListener {
                ContactService.asyncUpdateContactsByCount(
                    user,
                    requireActivity().applicationContext
                )
            }
        } else {
            user = savedInstanceState.getParcelable(ContactItem.USER_CONTACT)
        }
        setupSwipe(view)
        if (DeviceContactContentProvider.isContactsEmptyInDB(requireActivity())) {
            ContactService.asyncUpdateContacts(user, requireActivity())
        }
        restartLoader()
    }

    private fun restartLoader(query: String) {
        filter = query
        restartLoader()
    }

    private fun restartLoader() {
        requireActivity().supportLoaderManager.restartLoader(
            MainApplicationSingleton.CONTACTITEM_FRAGMENT_LOADERID, null, this
        )
    }

    private fun isReadyToLoad(): Boolean {
        return (null == contactItems || 0 == contactItems.size()) && activity != null
    }

    fun createRecycleAdapter() {
        if (null == recyclerView && view != null) {
            recyclerView = view?.findViewById(R.id.recyclerview)
        }
        if (null == recyclerView) return
        viewAdapter = RecyclerViewAdapter(MainApplicationSingleton.asList(contactItems))
        viewAdapter?.setHasStableIds(true)
        recyclerView?.swapAdapter(viewAdapter, true)
        recyclerView?.scrollToPosition(viewAdapter?.itemCount?.minus(1) ?: 0)
    }

    fun setupSwipe(view: View) {
        val refreshLayout = view.findViewById<SwipeRefreshLayout>(R.id.swiperefresh)
        refreshLayout?.setOnRefreshListener {
            ContactService.asyncUpdateContactsByCount(user, context)
            // Post a delayed runnable to reset the refreshing state in 2 seconds
            Handler().postDelayed({
                refreshLayout.isRefreshing = false
            }, 2000)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(requireActivity())
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupRecyclerView(
        @NonNull recyclerView: RecyclerView,
        contactItems: HashMap<Long, ContactItem>
    ) {
        createRecycleAdapter()
        if (contactItems.isEmpty()) {
            Snackbar.make(
                recyclerView,
                "Please click refresh button to see the latest updated contacts",
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction(android.R.string.ok, null).show()
        }
    }

    fun getContactListFragment(): ContactListener? {
        return contactListFragment
    }

    fun setContactListFragment(contactListFragment: ContactListener) {
        this.contactListFragment = contactListFragment
    }

    private fun showContactDetail(id: String, user: ContactItem) {
        val deviceContactItem = contactItems.get(id.toInt())
        showContactDetail(deviceContactItem, user)
    }

    private fun showContactDetail(deviceContactItem: ContactItem?, user: ContactItem) {
        deviceContactTopicListener?.onDeviceContactTopicClicked(deviceContactItem, user)
    }

    override fun onClick(v: View) {
        val dataId = v.findViewById<TextView>(R.id.tv_id)
        val id = dataId.text.toString()
        showContactDetail(id, user)
    }

    fun onBackPressed(): Boolean {
        return false
    }

    fun onOkPressed(intent: Intent) {
        // Empty implementation
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        restartLoader(query)
        return true
    }

    override fun onQueryTextChange(newText: String): Boolean {
        restartLoader(newText)
        return true
    }

    override fun onClose(): Boolean {
        restartLoader("")
        return true
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        if (MainApplicationSingleton.CONTACTITEM_FRAGMENT_LOADERID == id) {
            val selection = " ( name  IS NULL OR name like ? AND " +
                    " emails LIKE ? " +
                    " )"
            val selArgs = if (null == filter || filter.isNullOrEmpty()) {
                arrayOf("%%", "%.com%")
            } else {
                arrayOf("%$filter%", "%.com%")
            }
            return CursorLoader(
                requireActivity(),
                DeviceContactContentProvider.CONTENT_URI,
                null,
                selection, selArgs,
                ContactItemColumns.KEY_IS_INTELLIBITZ + " DESC"
            )
        }
        return CursorLoader(
            requireActivity(),
            DeviceContactContentProvider.CONTENT_URI,
            null,
            ContactItemColumns.KEY_DATA_ID + " = ? ",
            arrayOf("0"), null
        )
    }

    override fun onLoadFinished(loader: Loader<Cursor>, cursor: Cursor?) {
        if (MainApplicationSingleton.CONTACTITEM_FRAGMENT_LOADERID == loader.id) {
            if (null == cursor) {
                if (null == contactItems) contactItems = SparseArray()
                contactItems.clear()
                createRecycleAdapter()
                return
            }
            val count = cursor.count
            if (0 == count) {
                if (null == contactItems) contactItems = SparseArray()
                contactItems.clear()
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
        this.contactItems = DeviceContactContentProvider.fillDeviceContactItemFromCursor(cursor)
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        if (MainApplicationSingleton.CONTACTITEM_FRAGMENT_LOADERID == loader.id) {
            if (null == contactItems) contactItems = SparseArray()
            contactItems.clear()
            createRecycleAdapter()
        }
    }

    fun onTrimMemory(level: Int) {
        Log.d(TAG, "onTrimMemory: $level")
    }

    inner class RecyclerViewAdapter(
        items: Collection<ContactItem>?
    ) : RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>(),
        BitmapFromUrlTask.BitmapFromUrlTaskListener {

        private val viewItems = ArrayList<ContactItem>()
        private var bitmapFromUrlTask: BitmapFromUrlTask? = null

        init {
            if (items != null) {
                viewItems.addAll(items)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_devicecontacts_rv, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val deviceContactItem = viewItems[position]
            holder.mItem = deviceContactItem
            holder.tvId.text = holder.mItem?.deviceContactId.toString()
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

            val mobiles = deviceContactItem.mobiles
            val emails = deviceContactItem.emails
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

            val picDraw = getDrawable(R.drawable.default_profile_thread, requireActivity().theme)
            holder.ivProfile.setImageDrawable(picDraw)
            try {
                val pic = holder.mItem?.profilePic
                if (null == pic || pic.isEmpty()) {
                    // Empty implementation
                } else if (pic.startsWith("http")) {
                    bitmapFromUrlTask = BitmapFromUrlTask(
                        holder.ivProfile, pic, requireActivity().applicationContext
                    )
                    bitmapFromUrlTask?.setBitmapFromUrlTaskListener(this)
                    bitmapFromUrlTask?.execute()
                } else {
                    val bitmap = MainApplicationSingleton.getBitmapDecodeAnyUri(pic, context)
                    if (null == bitmap) {
                        val drawable = ColorGenerator.getTextDrawable(name)
                        setImageDrawable(holder.ivProfile, bitmap)
                    } else {
                        val croppedBitmap = NetworkImageView.getCroppedBitmap(bitmap, 100)
                        setImageDrawable(holder.ivProfile, croppedBitmap)
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e(TAG, e.message ?: "")
            }
            holder.tvName.text = name
            holder.tvStatus.text = status
            holder.tvDate.text = date
        }

        override fun getItemCount(): Int {
            return viewItems.size
        }

        override fun getItemId(position: Int): Long {
            return viewItems[position].deviceContactId
        }

        override fun onPostBitmapFromUrlExecute(bitmap: Bitmap, view: View, context: Context) {
            if (null == context) return
            val resources = context.resources
            if (null == resources) return
            val croppedBitmap = NetworkImageView.getCroppedBitmap(bitmap, 100)
            setImageDrawable(view, croppedBitmap)
        }

        override fun onPostBitmapFromUrlExecuteFail(bitmap: Bitmap) {
            Log.e(TAG, "On Post Bitmap From URL Exec ERROR: $bitmap")
        }

        override fun setBitmapFromUrlTaskToNull() {
            bitmapFromUrlTask = null
        }

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val mView: View = view
            val ivProfile: ImageView = view.findViewById(R.id.iv_contact)
            val tvId: TextView = view.findViewById(R.id.tv_id)
            val tvName: TextView = view.findViewById(R.id.tv_name)
            val tvStatus: TextView = view.findViewById(R.id.tv_status)
            val tvLastSeen: TextView = view.findViewById(R.id.tv_lastseen)
            val tvDate: TextView = view.findViewById(R.id.tv_date)
            var mItem: ContactItem? = null

            init {
                mView.setOnClickListener(this@DeviceContactsFragment)
            }

            override fun toString(): String {
                return super.toString() + " '" + tvName.text + "'"
            }
        }
    }

    companion object {
        fun newInstance(user: ContactItem, contactListener: ContactListener): DeviceContactsFragment {
            val fragment = DeviceContactsFragment()
            val args = Bundle()
            args.putParcelable(ContactItem.USER_CONTACT, user)
            fragment.setContactListFragment(contactListener)
            if (contactListener is DeviceContactTopicListener)
                fragment.setDeviceContactTopicListener(contactListener as DeviceContactTopicListener)
            fragment.setUser(user)
            fragment.arguments = args
            return fragment
        }
    }
}
