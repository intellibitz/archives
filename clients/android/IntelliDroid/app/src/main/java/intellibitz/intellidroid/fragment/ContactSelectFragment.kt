package intellibitz.intellidroid.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.database.ContentObserver
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.app.NavUtils
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import intellibitz.intellidroid.IntellibitzActivityFragment
import intellibitz.intellidroid.R
import intellibitz.intellidroid.content.DeviceContactContentProvider
import intellibitz.intellidroid.content.IntellibitzContactContentProvider
import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.db.ContactItemColumns
import intellibitz.intellidroid.listener.ContactListener
import intellibitz.intellidroid.listener.IntellibitzContactTopicListener
import intellibitz.intellidroid.task.BitmapFromUrlTask
import intellibitz.intellidroid.util.MainApplicationSingleton
import intellibitz.intellidroid.util.NetworkImageView
import java.io.IOException
import java.util.ArrayList
import java.util.HashMap
import java.util.HashSet
import java.util.List

/**
 *
 */
class ContactSelectFragment : IntellibitzActivityFragment(),
    SearchView.OnQueryTextListener,
    SearchView.OnCloseListener,
    LoaderManager.LoaderCallbacks<Cursor>,
    TabLayout.OnTabSelectedListener {

    companion object {
        const val TAG = "ContactSelectFrag"

        fun newInstance(contactItem: ContactItem?, user: ContactItem?): ContactSelectFragment {
            val fragment = ContactSelectFragment()
            fragment.user = user
            fragment.contactItem = contactItem
            val args = Bundle()
            args.putParcelable(ContactItem.USER_CONTACT, user)
            args.putParcelable(ContactItem.TAG, contactItem)
            fragment.arguments = args
            return fragment
        }
    }

    private val intellibitzContactItemHashMap: HashMap<Long, ContactItem> = HashMap()
    private var contentObserver: ContentObserver? = null
    private var shared: Intent? = null
    private var view: View? = null
    private var recyclerView: RecyclerView? = null
    private var viewAdapter: RecyclerViewAdapter? = null
    private val selectedContactItems: HashSet<ContactItem> = HashSet()
    private var contactItem: ContactItem? = null
    private var onQueryTextSubmit: String? = null
    private var filter: String? = null
    private var contactListener: ContactListener? = null
    private var contactTopicListener: IntellibitzContactTopicListener? = null

    var contactItem: ContactItem?
        get() = this.contactItem
        set(contactItem) {
            this.contactItem = contactItem
        }

    fun setIntellibitzContactItemHashMap(items: HashMap<Long, ContactItem>?) {
        intellibitzContactItemHashMap.clear()
        if (items != null) intellibitzContactItemHashMap.putAll(items)
    }

    fun setContactListener(contactListener: ContactListener?) {
        this.contactListener = contactListener
    }

    fun setContactTopicListener(contactTopicListener: IntellibitzContactTopicListener?) {
        this.contactTopicListener = contactTopicListener
    }

    fun onNewMenuClicked() {}

    override fun onDestroy() {
        activity?.contentResolver?.unregisterContentObserver(contentObserver)
        super.onDestroy()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ContactListener) {
            setContactListener(context)
        }
        if (context is IntellibitzContactTopicListener) {
            setContactTopicListener(context)
        }
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
        outState.putParcelable(ContactItem.TAG, contactItem)
        super.onSaveInstanceState(outState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        view = inflater.inflate(R.layout.fragment_contactselect, container, false)
        return view
    }

    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (null == savedInstanceState) {
            user = arguments?.getParcelable(ContactItem.USER_CONTACT)
            contactItem = arguments?.getParcelable(ContactItem.TAG)
        } else {
            user = savedInstanceState.getParcelable(ContactItem.USER_CONTACT)
            contactItem = arguments?.getParcelable(ContactItem.TAG)
        }
        setupAppBar()
        recyclerView = view.findViewById(R.id.recyclerview)
        setupSwipe(view)
        restartLoader()
    }

    private fun restartLoader(query: String?) {
        this.filter = query
        restartLoader()
    }

    private fun restartLoader() {
        activity?.supportLoaderManager?.restartLoader(
            MainApplicationSingleton.CONTACTSELECT_LOADERID, null, this
        )
    }

    fun createRecycleAdapter() {
        if (null == recyclerView && view != null)
            recyclerView = view?.findViewById(R.id.recyclerview)
        if (null == recyclerView) return
        if (null == intellibitzContactItemHashMap || intellibitzContactItemHashMap.isEmpty())
            return
        val values: MutableList<ContactItem> = ArrayList()
        values.addAll(intellibitzContactItemHashMap.values)
        viewAdapter = RecyclerViewAdapter(values)
        viewAdapter?.setHasStableIds(true)
        recyclerView?.swapAdapter(viewAdapter, true)
    }

    fun setupSwipe(view: View) {
        val refreshLayout = view.findViewById<SwipeRefreshLayout>(R.id.swiperefresh)
        if (refreshLayout != null) {
            refreshLayout.setOnRefreshListener {
                // Post a delayed runnable to reset the refreshing state in 2 seconds
                Handler().postDelayed({
                    refreshLayout.isRefreshing = false
                }, 2000)
            }
        }
    }

    private fun setSubTitle(count: Int) {
        val view = view
        if (view != null) {
            val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
            val tvSubTitle = view.findViewById<TextView>(R.id.tv_subtitle)
            tvSubTitle.text = "$count ${getString(R.string.selected)}"
        }
    }

    private fun setupAppBar() {
        val view = view
        if (view != null) {
            val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
            setSubTitle(0)
            val tvOk = view.findViewById<TextView>(R.id.btn_ok)
            tvOk.setOnClickListener { onOkPressed() }
            val tvClose = view.findViewById<TextView>(R.id.tv_close)
            tvClose.setOnClickListener { onCancel() }
            appCompatActivity?.setSupportActionBar(toolbar)
        }
    }

    private fun setupDetailFilterToolbar() {
        clearDetailFilters()
    }

    @NonNull
    private fun clearDetailFilters(): Toolbar? {
        clearDetailFilterToolbarMenus()
        removeAllDetailTabLayoutFilters()
        return null
    }

    private fun removeAllDetailTabLayoutFilters() {}

    @NonNull
    private fun clearDetailFilterToolbarMenus(): Toolbar? {
        return null
    }

    /**
     * contacts have been selected, go back to previous screen
     */
    private fun onOkPressed() {
        //            do in background returns success.. handle them
//        this activity, is called for result with this intent.. so the parent is aware
        val activity = activity
        val intent = activity?.intent
        if (null == selectedContactItems || selectedContactItems.isEmpty()) {
            onCancel()
        } else {
            contactItem?.selectedContacts = selectedContactItems
            intent?.putExtra(ContactItem.USER_CONTACT, user as Parcelable)
            intent?.putExtra(ContactItem.TAG, contactItem as Parcelable)
            activity?.setResult(Activity.RESULT_OK, intent)
            activity?.finish()
        }
    }

    private fun onCancel() {
        val activity = activity
        val intent = activity?.intent
        intent?.putExtra(ContactItem.USER_CONTACT, user as Parcelable)
        intent?.putExtra(ContactItem.TAG, contactItem as Parcelable)
        activity?.setResult(Activity.RESULT_CANCELED, intent)
        activity?.finish()
    }

    fun onBackPressed(): Boolean {
        removeSelf()
        return true
    }

    override fun onTabUnselected(tab: TabLayout.Tab) {
        if (null == tab) return
        if (null == tab.tag) return
        if (R.id.micc_clear == tab.tag as Int) {
            return
        }
        if (R.id.micc_select == tab.tag as Int) {
            return
        }
        if (R.id.micc_done == tab.tag as Int) {
            return
        }
    }

    override fun onTabReselected(tab: TabLayout.Tab) {
        onTabSelected(tab)
    }

    override fun onTabSelected(tab: TabLayout.Tab) {
        if (null == tab) return
        if (null == tab.tag) return
        if (R.id.micc_clear == tab.tag as Int) {
            restartLoader()
            setSelectedCount(0)
            return
        }
        if (R.id.micc_select == tab.tag as Int) {
            return
        }
        if (R.id.micc_done == tab.tag as Int) {
            onOkPressed()
            return
        }
    }

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

    private fun setupRecyclerView(@NonNull recyclerView: RecyclerView, contactItems: HashMap<Long, ContactItem>) {
        createRecycleAdapter()
        if (contactItems.isEmpty()) {
            Snackbar.make(recyclerView,
                "Please click refresh button to see the latest updated contacts",
                Snackbar.LENGTH_INDEFINITE)
                .setAction(android.R.string.ok, null).show()
        }
    }

    private fun showContactDetail(id: String, user: ContactItem?) {
        val contactItem = intellibitzContactItemHashMap[id.toLong()]
        showContactDetail(contactItem, user)
    }

    private fun showContactDetail(contactItem: ContactItem?, user: ContactItem?) {
        if (contactTopicListener != null)
            contactTopicListener?.onIntellibitzContactTopicClicked(contactItem, user)
    }

    //    @Override
    fun onClick(v: View) {
        val dataId = v.findViewById<TextView>(R.id.tv_id)
        val id = dataId.text.toString()
        showContactDetail(id, user)
    }

    private fun setSelectedItems(intellibitzContactItem: ContactItem, selected: Boolean) {
        val contactItem = ContactItem(intellibitzContactItem)
        if (selected)
            selectedContactItems.add(contactItem)
        else
            selectedContactItems.remove(contactItem)
        val count = selectedContactItems.size
        setSubTitle(count)
    }

    private fun setSelectedCount(count: Int) {}

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
        if (MainApplicationSingleton.CONTACTSELECT_LOADERID == id) {
            // Now createFileInES and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            val selection = " ( name IS NULL OR name like ? ) AND" +
                    " id <> ? AND" +
                    " ( is_cloud = 1 ) AND " +
                    " ( work_contact = 1 ) AND " +
                    " ( is_email = 0 or is_email IS NULL ) AND " +
                    " ( is_group = 0 or is_group IS NULL ) "
            val selArgs: Array<String>
            if (filter != null && !filter!!.isEmpty()) {
                selArgs = arrayOf("%$filter%", user?.dataId)
            } else {
                selArgs = arrayOf("%%", user?.dataId)
            }
            return CursorLoader(activity,
                IntellibitzContactContentProvider.CONTENT_URI,
                null,
                selection, selArgs,
                ContactItemColumns.KEY_NAME +
                        " ASC")
        }
        //        returns an empty dummy cursor.. for the cursor loader to play game
        return CursorLoader(activity,
            IntellibitzContactContentProvider.CONTENT_URI,
            null,
            ContactItemColumns.KEY_DATA_ID + " = ? ",
            arrayOf("0"), null)
    }

    override fun onLoadFinished(loader: Loader<Cursor>, cursor: Cursor?) {
        if (null == cursor) {
            if (null == intellibitzContactItemHashMap)
                intellibitzContactItemHashMap.clear()
            createRecycleAdapter()
            return
        }
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        if (MainApplicationSingleton.CONTACTSELECT_LOADERID == loader.id) {
            val count = cursor.count
            if (count > 0 && 0 == cursor.position) {
                if (null == intellibitzContactItemHashMap)
                    intellibitzContactItemHashMap.clear()
                fillItemsFromCursor(cursor)
                cursor.close()
                createRecycleAdapter()
            }
        }
    }

    fun fillItemsFromCursor(cursor: Cursor) {
        IntellibitzContactContentProvider.fillIntellibitzContactFromCursor(intellibitzContactItemHashMap, cursor)
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        if (MainApplicationSingleton.CONTACTSELECT_LOADERID == loader.id) {
            //            refreshes view, for every data change.. this might not be required since adapter is
//            loaded on finish
            if (null == intellibitzContactItemHashMap)
                intellibitzContactItemHashMap.clear()
            createRecycleAdapter()
        }
    }

    inner class RecyclerViewAdapter(private val viewItems: List<ContactItem>) :
        RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>(),
        BitmapFromUrlTask.BitmapFromUrlTaskListener {

        private var bitmapFromUrlTask: BitmapFromUrlTask? = null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_contactselect_rv, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val contactItem = viewItems[position]
            holder.mItem = contactItem
            //            resets selection
            holder.ctv.isChecked = false
            holder.tvId.text = holder.mItem?._id.toString()
            val name = contactItem.name
            //            sets the contact image
            holder.tvName.setCompoundDrawables(null, null, null, null)
            val drawable = getDrawable(R.drawable.default_profile_thread, activity?.theme)
            drawable?.setBounds(Rect(0, 0, 100, 100))
            try {
                val pic = holder.mItem?.profilePic
                if (null == pic || pic.isEmpty()) {
                    if (name != null && name.length > 0) {
                        holder.tvName.setCompoundDrawables(
                            drawable, null, null, null
                        )
                    }
                } else if (pic.startsWith("http")) {
                    //                    cloud pic
                    bitmapFromUrlTask = BitmapFromUrlTask(
                        holder.tvName, pic, activity
                    )
                    bitmapFromUrlTask?.setBitmapFromUrlTaskListener(this)
                    bitmapFromUrlTask?.execute()
                } else {
                    //                    db content pic
                    val bitmap = MainApplicationSingleton.getBitmapDecodeAnyUri(pic, context)
                    if (null == bitmap) {
                        holder.tvName.setCompoundDrawables(drawable, null, null, null)
                    } else {
                        val croppedBitmap = NetworkImageView.getCroppedBitmap(bitmap, 100)
                        val bitmapDrawable = BitmapDrawable(resources, croppedBitmap)
                        bitmapDrawable.setBounds(Rect(0, 0, 100, 100))
                        holder.tvName.setCompoundDrawables(bitmapDrawable, null, null, null)
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            holder.tvName.text = name
            val rootView = holder.mView.rootView as ViewGroup
            val linearLayout = rootView.findViewById<LinearLayout>(R.id.list_item_contactinfo)
            //            clears old views
            linearLayout.removeAllViews()
        }

        override fun getItemCount(): Int {
            return viewItems.size
        }

        override fun getItemId(position: Int): Long {
            return viewItems[position]._id
        }

        override fun onPostBitmapFromUrlExecute(bitmap: Bitmap, textView: View, context: Context?) {
            var context = context
            if (null == context) {
                context = context
            }
            if (null == context) return
            val resources = context.resources
            if (null == resources) return
            val roundedBitmap = NetworkImageView.getCroppedBitmap(bitmap, 100)
            val drawable = BitmapDrawable(resources, roundedBitmap)
            drawable.setBounds(Rect(0, 0, 100, 100))
            (textView as TextView).setCompoundDrawables(drawable, null, null, null)
        }

        override fun onPostBitmapFromUrlExecuteFail(bitmap: Bitmap) {
            Log.e(TAG, "On Post Bitmap From URL Exec ERROR: $bitmap")
        }

        override fun setBitmapFromUrlTaskToNull() {
            bitmapFromUrlTask = null
        }

        inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView),
            View.OnLongClickListener, View.OnClickListener {
            //            the views
            val tvName: TextView = mView.findViewById(R.id.tv_name)
            val tvId: TextView = mView.findViewById(R.id.tv_id)
            val ctv: CheckBox = mView.findViewById(R.id.ctv_1)
            //            the data
            var mItem: ContactItem? = null

            init {
                mView.setOnLongClickListener(this)
                mView.setOnClickListener(this)
                ctv.setOnClickListener(this)
            }

            override fun toString(): String {
                return super.toString() + " '" + tvName.text + "'"
            }

            override fun onLongClick(v: View): Boolean {
                if (v is CheckBox) {
                } else {
                    v.isSelected = !v.isSelected
                    ctv.isChecked = !ctv.isChecked
                    setSelectedItems(mItem!!, ctv.isChecked)
                    return false
                }
                setSelectedItems(mItem!!, ctv.isChecked)
                return false
            }

            override fun onClick(v: View) {
                onLongClick(v)
            }
        }
    }
}
