package intellibitz.intellidroid.company

import android.app.Activity
import android.content.Context
import android.content.Intent
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
import android.util.SparseArray
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
import androidx.core.content.ContextCompat
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
import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.db.ContactItemColumns
import intellibitz.intellidroid.listener.ContactListener
import intellibitz.intellidroid.listener.IntellibitzContactTopicListener
import intellibitz.intellidroid.task.BitmapFromUrlTask
import intellibitz.intellidroid.util.MainApplicationSingleton
import intellibitz.intellidroid.util.NetworkImageView
import org.json.JSONArray
import org.json.JSONException
import java.io.IOException
import java.util.ArrayList
import java.util.HashMap
import java.util.HashSet

class InviteUsersFragment :
    IntellibitzActivityFragment(),
    SearchView.OnQueryTextListener,
    SearchView.OnCloseListener,
    LoaderManager.LoaderCallbacks<Cursor>,
    TabLayout.OnTabSelectedListener {

    var contactItemSparseArray: SparseArray<ContactItem>? = SparseArray()
    private var contentObserver: ContentObserver? = null
    private var shared: Intent? = null
    private var viewLayout: View? = null
    private var recyclerView: RecyclerView? = null
    private var viewAdapter: RecyclerViewAdapter? = null
    private val selectedContactItems = HashSet<ContactItem>()
    var contactItem: ContactItem? = null
    private var onQueryTextSubmit: String? = null
    private var filter: String? = null
    private var contactListener: ContactListener? = null
    private var contactTopicListener: IntellibitzContactTopicListener? = null

    fun setContactListener(contactListener: ContactListener?) {
        this.contactListener = contactListener
    }

    fun setContactTopicListener(contactTopicListener: IntellibitzContactTopicListener?) {
        this.contactTopicListener = contactTopicListener
    }

    fun onNewMenuClicked() {
    }

    override fun onDestroy() {
        contentObserver?.let {
            activity?.contentResolver?.unregisterContentObserver(it)
        }
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
            contentObserver!!
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(ContactItem.USER_CONTACT, user)
        outState.putParcelable(ContactItem.TAG, contactItem)
        super.onSaveInstanceState(outState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewLayout = inflater.inflate(R.layout.fragment_inviteusers, container, false)
        return viewLayout
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
        if (null == contactItem) contactItem = ContactItem("InviteUsersFragment")
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
        if (null == recyclerView && viewLayout != null) {
            recyclerView = viewLayout?.findViewById(R.id.recyclerview)
        }
        val rv = recyclerView ?: return
        val sparse = contactItemSparseArray ?: return
        if (0 == sparse.size()) return
        val values = MainApplicationSingleton.asList(sparse)
        viewAdapter = RecyclerViewAdapter(values)
        viewAdapter?.setHasStableIds(true)
        rv.swapAdapter(viewAdapter, true)
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

    private fun setSubTitle(count: Int) {
        val toolbar = getToolbar()
        if (toolbar != null) {
            val tvSubTitle = toolbar.findViewById<TextView>(R.id.tv_subtitle)
            tvSubTitle?.text = "$count " + getString(R.string.selected)
        }
    }

    private fun setupAppBar() {
        val toolbar = getToolbar()
        if (toolbar != null) {
            setSubTitle(0)
            val tvOk = toolbar.findViewById<TextView>(R.id.btn_ok)
            tvOk?.setOnClickListener {
                onOkPressed()
            }
            val tvClose = toolbar.findViewById<TextView>(R.id.tv_close)
            tvClose?.setOnClickListener {
                onCancel()
            }
            compatActivity?.setSupportActionBar(toolbar)
        }
    }

    fun startSearching() {
        val toolbar = getToolbar()
        if (toolbar != null) {
            toolbar.findViewById<View>(R.id.tv_title)?.visibility = View.GONE
            toolbar.findViewById<View>(R.id.tv_subtitle)?.visibility = View.GONE
            toolbar.findViewById<View>(R.id.btn_ok)?.visibility = View.GONE
            toolbar.findViewById<View>(R.id.tv_close)?.visibility = View.GONE
        }
    }

    fun stopSearching() {
        val toolbar = getToolbar()
        if (toolbar != null) {
            toolbar.findViewById<View>(R.id.tv_title)?.visibility = View.VISIBLE
            toolbar.findViewById<View>(R.id.tv_subtitle)?.visibility = View.VISIBLE
            toolbar.findViewById<View>(R.id.btn_ok)?.visibility = View.VISIBLE
            toolbar.findViewById<View>(R.id.tv_close)?.visibility = View.VISIBLE
        }
    }

    fun getToolbar(): Toolbar? {
        val v = view ?: return null
        return v.findViewById(R.id.toolbar)
    }

    private fun setupDetailFilterToolbar() {
        clearDetailFilters()
    }

    private fun clearDetailFilters(): Toolbar? {
        clearDetailFilterToolbarMenus()
        removeAllDetailTabLayoutFilters()
        return null
    }

    private fun removeAllDetailTabLayoutFilters() {
    }

    private fun clearDetailFilterToolbarMenus(): Toolbar? {
        return null
    }

    private fun onOkPressed() {
        val act = activity ?: return
        val intent = act.intent
        if (selectedContactItems.isEmpty()) {
            onCancel()
        } else {
            contactItem?.selectedContacts = selectedContactItems
            intent.putExtra(ContactItem.USER_CONTACT, user as? Parcelable)
            intent.putExtra(ContactItem.TAG, contactItem as? Parcelable)
            act.setResult(Activity.RESULT_OK, intent)
            act.finish()
        }
    }

    private fun onCancel() {
        val act = activity ?: return
        val intent = act.intent
        intent.putExtra(ContactItem.USER_CONTACT, user as? Parcelable)
        intent.putExtra(ContactItem.TAG, contactItem as? Parcelable)
        act.setResult(Activity.RESULT_CANCELED, intent)
        act.finish()
    }

    fun onBackPressed(): Boolean {
        removeSelf()
        return true
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {
        if (null == tab || null == tab.tag) return
        val tag = tab.tag as? Int ?: return
        if (R.id.micc_clear == tag || R.id.micc_select == tag || R.id.micc_done == tag) {
            return
        }
    }

    override fun onTabReselected(tab: TabLayout.Tab?) {
        onTabSelected(tab)
    }

    override fun onTabSelected(tab: TabLayout.Tab?) {
        if (null == tab || null == tab.tag) return
        val tag = tab.tag as? Int ?: return
        if (R.id.micc_clear == tag) {
            restartLoader()
            setSelectedCount(0)
            return
        }
        if (R.id.micc_select == tag) {
            return
        }
        if (R.id.micc_done == tag) {
            onOkPressed()
            return
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            activity?.let { NavUtils.navigateUpFromSameTask(it) }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupRecyclerView(@NonNull recyclerView: RecyclerView, contactItems: HashMap<Long, ContactItem>) {
        createRecycleAdapter()
        if (contactItems.isEmpty()) {
            Snackbar.make(
                recyclerView,
                "Please click refresh button to see the latest updated contacts",
                Snackbar.LENGTH_INDEFINITE
            ).setAction(android.R.string.ok, null).show()
        }
    }

    private fun showContactDetail(id: String, user: ContactItem?) {
        val item = contactItemSparseArray?.get(id.toInt())
        showContactDetail(item, user)
    }

    private fun showContactDetail(contactItem: ContactItem?, user: ContactItem?) {
        contactTopicListener?.onIntellibitzContactTopicClicked(contactItem, user)
    }

    fun onClick(v: View) {
        val dataId = v.findViewById<TextView>(R.id.tv_id)
        val id = dataId.text.toString()
        showContactDetail(id, user)
    }

    private fun setSelectedItems(contactItem: ContactItem, selected: Boolean) {
        if (selected) {
            selectedContactItems.add(contactItem)
        } else {
            selectedContactItems.remove(contactItem)
        }
        val count = selectedContactItems.size
        setSubTitle(count)
    }

    private fun setSelectedCount(count: Int) {
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        restartLoader(query)
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        restartLoader(newText)
        return false
    }

    override fun onClose(): Boolean {
        restartLoader("")
        return false
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        val act = requireActivity()
        if (MainApplicationSingleton.CONTACTSELECT_LOADERID == id) {
            val selection = " ( name  IS NULL OR name like ? AND  emails LIKE ?  )"
            val selArgs: Array<String> = if (filter == null || filter!!.isEmpty()) {
                arrayOf("%%", "%.com%")
            } else {
                arrayOf("%$filter%", "%.com%")
            }
            return CursorLoader(
                act,
                DeviceContactContentProvider.CONTENT_URI,
                null,
                selection, selArgs,
                ContactItemColumns.KEY_IS_INTELLIBITZ + " DESC"
            )
        }
        return CursorLoader(
            act,
            DeviceContactContentProvider.CONTENT_URI,
            null,
            ContactItemColumns.KEY_DATA_ID + " = ? ",
            arrayOf("0"), null
        )
    }

    override fun onLoadFinished(loader: Loader<Cursor>, cursor: Cursor?) {
        if (null == cursor) {
            if (null == contactItemSparseArray) contactItemSparseArray = SparseArray()
            contactItemSparseArray?.clear()
            createRecycleAdapter()
            return
        }
        if (MainApplicationSingleton.CONTACTSELECT_LOADERID == loader.id) {
            val count = cursor.count
            if (count > 0 && 0 == cursor.position) {
                if (null == contactItemSparseArray) contactItemSparseArray = SparseArray()
                contactItemSparseArray?.clear()
                fillItemsFromCursor(cursor)
                cursor.close()
                createRecycleAdapter()
            }
        }
    }

    fun fillItemsFromCursor(cursor: Cursor) {
        contactItemSparseArray = DeviceContactContentProvider.fillsDeviceContactItemFromCursor(cursor)
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        if (MainApplicationSingleton.CONTACTSELECT_LOADERID == loader.id) {
            if (null == contactItemSparseArray) contactItemSparseArray = SparseArray()
            contactItemSparseArray?.clear()
            createRecycleAdapter()
        }
    }

    inner class RecyclerViewAdapter(items: List<ContactItem>) :
        RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>(),
        BitmapFromUrlTask.BitmapFromUrlTaskListener {

        private var viewItems: MutableList<ContactItem> = ArrayList(items)
        private var bitmapFromUrlTask: BitmapFromUrlTask? = null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_contactselect_rv, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val contact = viewItems[position]
            holder.mItem = contact
            holder.ctv.isChecked = false
            holder.tvId.text = holder.mItem?.id.toString()
            val name = contact.name
            holder.tvName.setCompoundDrawables(null, null, null, null)
            val act = activity
            val theme = act?.theme
            val drawable = if (theme != null && act != null) {
                ContextCompat.getDrawable(act, R.drawable.default_profile_thread)
            } else null
            drawable?.setBounds(0, 0, 100, 100)
            try {
                val pic = holder.mItem?.profilePic
                if (pic.isNullOrEmpty()) {
                    if (!name.isNullOrEmpty()) {
                        holder.tvName.setCompoundDrawables(drawable, null, null, null)
                    }
                } else if (pic.startsWith("http")) {
                    bitmapFromUrlTask = BitmapFromUrlTask(holder.tvName, pic, activity)
                    bitmapFromUrlTask?.setBitmapFromUrlTaskListener(this)
                    bitmapFromUrlTask?.execute()
                } else {
                    val bitmap = MainApplicationSingleton.getBitmapDecodeAnyUri(pic, context)
                    if (null == bitmap) {
                        holder.tvName.setCompoundDrawables(drawable, null, null, null)
                    } else {
                        val croppedBitmap = NetworkImageView.getCroppedBitmap(bitmap, 100)
                        val bitmapDrawable = BitmapDrawable(resources, croppedBitmap)
                        bitmapDrawable.setBounds(0, 0, 100, 100)
                        holder.tvName.setCompoundDrawables(bitmapDrawable, null, null, null)
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            holder.tvName.text = name
            val rootView = holder.mView.rootView as? ViewGroup
            val linearLayout = rootView?.findViewById<LinearLayout>(R.id.list_item_contactinfo)
            linearLayout?.removeAllViews()
            val emails = holder.mItem?.emails
            if (emails != null) {
                for (i in 0 until emails.length()) {
                    try {
                        val email = emails.getString(i)
                        val view = LayoutInflater.from(holder.mView.context).inflate(
                            R.layout.list_item_contactinfo,
                            linearLayout, false
                        )
                        val tv = view.findViewById<TextView>(R.id.tv_contactinfo)
                        tv.text = email
                        val draw = if (act != null) ContextCompat.getDrawable(act, R.drawable.ic_email_black_24dp) else null
                        draw?.setBounds(0, 0, 40, 40)
                        tv.setCompoundDrawables(null, null, draw, null)
                        linearLayout?.addView(view)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            }
        }

        override fun getItemCount(): Int {
            return viewItems.size
        }

        override fun getItemId(position: Int): Long {
            return viewItems[position].id
        }

        override fun onPostBitmapFromUrlExecute(bitmap: Bitmap?, textView: View?, context: Context?) {
            val ctx = context ?: getContext() ?: return
            val res = ctx.resources ?: return
            val roundedBitmap = NetworkImageView.getCroppedBitmap(bitmap, 100)
            val drawable = BitmapDrawable(res, roundedBitmap)
            drawable.setBounds(0, 0, 100, 100)
            (textView as? TextView)?.setCompoundDrawables(drawable, null, null, null)
        }

        override fun onPostBitmapFromUrlExecuteFail(bitmap: Bitmap?) {
            Log.e(TAG, "On Post Bitmap From URL Exec ERROR: $bitmap")
        }

        override fun setBitmapFromUrlTaskToNull() {
            bitmapFromUrlTask = null
        }

        inner class ViewHolder(val mView: View) :
            RecyclerView.ViewHolder(mView),
            View.OnLongClickListener,
            View.OnClickListener {

            val tvName: TextView = mView.findViewById(R.id.tv_name)
            val tvId: TextView = mView.findViewById(R.id.tv_id)
            val ctv: CheckBox = mView.findViewById(R.id.ctv_1)
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
                if (v !is CheckBox) {
                    v.isSelected = !v.isSelected
                    ctv.isChecked = !ctv.isChecked
                    mItem?.let { setSelectedItems(it, ctv.isChecked) }
                    return false
                }
                mItem?.let { setSelectedItems(it, ctv.isChecked) }
                return false
            }

            override fun onClick(v: View) {
                onLongClick(v)
            }
        }
    }

    companion object {
        const val TAG = "InviteUsersFrag"

        @JvmStatic
        fun newInstance(
            contactItem: ContactItem?,
            user: ContactItem?,
            contactListener: ContactListener?
        ): InviteUsersFragment {
            val fragment = InviteUsersFragment()
            fragment.setContactListener(contactListener)
            if (contactListener is IntellibitzContactTopicListener) {
                fragment.setContactTopicListener(contactListener)
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
}
