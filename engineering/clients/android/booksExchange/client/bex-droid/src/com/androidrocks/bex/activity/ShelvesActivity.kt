/*
 * Copyright (C) 2009 Muthu Ramadoss. All rights reserved.
 *
 * Modified from Romain Guy Shelves project to suit Books-Exchange requirements.
 * Original source from Shelves - http://code.google.com/p/shelves/
 */

/*
 * Copyright (C) 2008 Romain Guy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.androidrocks.bex.activity

import android.app.Activity
import android.app.SearchManager
import android.content.ActivityNotFoundException
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Config
import android.view.ContextMenu
import android.view.Gravity
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import android.view.animation.AnimationUtils
import android.widget.AbsListView
import android.widget.AdapterView
import android.widget.PopupWindow
import android.widget.ProgressBar
import android.widget.TextView
import com.androidrocks.bex.R
import com.androidrocks.bex.drawable.CrossFadeDrawable
import com.androidrocks.bex.drawable.FastBitmapDrawable
import com.androidrocks.bex.provider.BookStoreFactory
import com.androidrocks.bex.provider.BooksManager
import com.androidrocks.bex.provider.BooksStore
import com.androidrocks.bex.provider.BooksUpdater
import com.androidrocks.bex.scan.ScanIntent
import com.androidrocks.bex.util.ImageUtilities
import com.androidrocks.bex.util.ImportUtilities
import com.androidrocks.bex.util.UIUtilities
import com.androidrocks.bex.util.UserTask
import com.androidrocks.bex.view.ShelvesView
import java.io.IOException
import java.util.ArrayList
import java.util.concurrent.atomic.AtomicInteger

class ShelvesActivity : Activity() {

    private var mImportTask: ImportTask? = null
    private var mAddTask: AddTask? = null

    private var mBooksUpdater: BooksUpdater? = null

    private val mScrollHandler = ScrollHandler()
    var scrollState = ShelvesScrollManager.SCROLL_STATE_IDLE
        private set
    var isPendingCoversUpdate = false
        private set
    private var mFingerUp = true
    private var mPopup: PopupWindow? = null

    var defaultCover: FastBitmapDrawable? = null
        private set

    private var mGridPosition: View? = null
    private var mGridPositionText: TextView? = null

    private var mImportProgress: ProgressBar? = null
    private var mImportPanel: View? = null
    private var mAddPanel: View? = null
    private lateinit var mGrid: ShelvesView

    private var mSavedState: Bundle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.screen_shelves)
        window.setBackgroundDrawable(null)

        mBooksUpdater = BooksUpdater(this)

        setupViews()
        handleSearchQuery(intent)
    }

    private fun handleSearchQuery(queryIntent: Intent) {
        val queryAction = queryIntent.action
        if (Intent.ACTION_SEARCH == queryAction) {
            onSearch(queryIntent)
        } else if (Intent.ACTION_VIEW == queryAction) {
            val viewIntent = Intent(Intent.ACTION_VIEW, queryIntent.data)
            startActivity(viewIntent)
        }
    }

    private fun onSearch(intent: Intent) {
        val queryString = intent.getStringExtra(SearchManager.QUERY)
        mGrid.setFilterText(queryString)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        val action = intent.action
        if (Intent.ACTION_SEARCH == action) {
            onSearch(intent)
        } else if (Intent.ACTION_VIEW == action) {
            val viewIntent = Intent(Intent.ACTION_VIEW, intent.data)
            startActivity(viewIntent)
        } else if (ACTION_IMPORT == action) {
            onImport()
        }
    }

    private fun setupViews() {
        val adapter = BooksAdapter(this)
        defaultCover = adapter.defaultCover

        mGrid = findViewById<View>(R.id.grid_shelves) as ShelvesView

        val grid = mGrid
        grid.isTextFilterEnabled = true
        grid.adapter = adapter
        grid.setOnScrollListener(ShelvesScrollManager())
        grid.setOnTouchListener(FingerTracker())
        grid.onItemSelectedListener = SelectionTracker()
        grid.onItemClickListener = BookViewer()

        registerForContextMenu(grid)

        mGridPosition = layoutInflater.inflate(R.layout.grid_position, null)
        mGridPositionText = mGridPosition?.findViewById<View>(R.id.text) as TextView
    }

    override fun onResume() {
        super.onResume()
        mBooksUpdater?.start()
        mSavedState?.let { restoreLocalState(it) }
    }

    override fun onPause() {
        super.onPause()
        stopBooksUpdater()
    }

    override fun onStop() {
        super.onStop()
        stopBooksUpdater()
    }

    override fun onDestroy() {
        super.onDestroy()

        dismissPopup()

        stopBooksUpdater()

        onCancelAdd()
        onCancelImport()

        ImageUtilities.cleanupCache()
    }

    private fun stopBooksUpdater() {
        val booksUpdater = mBooksUpdater
        booksUpdater?.clear()
        booksUpdater?.stop()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        restoreLocalState(savedInstanceState)
        mSavedState = null
    }

    private fun restoreLocalState(savedInstanceState: Bundle) {
        restoreAddTask(savedInstanceState)
        restoreImportTask(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        saveAddTask(outState)
        saveImportTask(outState)
        mSavedState = outState
    }

    private fun saveAddTask(outState: Bundle) {
        val task = mAddTask
        if (task != null && task.status != UserTask.Status.FINISHED) {
            val bookId = task.bookId
            task.cancel(true)

            if (bookId != null) {
                outState.putBoolean(STATE_ADD_IN_PROGRESS, true)
                outState.putString(STATE_ADD_BOOK, bookId)
            }

            mAddTask = null
        }
    }

    private fun restoreAddTask(savedInstanceState: Bundle) {
        if (savedInstanceState.getBoolean(STATE_ADD_IN_PROGRESS)) {
            val id = savedInstanceState.getString(STATE_ADD_BOOK)
            if (id != null && !BooksManager.bookExists(contentResolver, id)) {
                mAddTask = AddTask().execute(id) as AddTask
            }
        }
    }

    private fun saveImportTask(outState: Bundle) {
        val task = mImportTask
        if (task != null && task.status != UserTask.Status.FINISHED) {
            task.cancel(true)

            outState.putBoolean(STATE_IMPORT_IN_PROGRESS, true)
            outState.putStringArrayList(STATE_IMPORT_BOOKS, task.mBooks)
            outState.putInt(STATE_IMPORT_INDEX, task.mImportCount.get())

            mImportTask = null
        }
    }

    private fun restoreImportTask(savedInstanceState: Bundle) {
        if (savedInstanceState.getBoolean(STATE_IMPORT_IN_PROGRESS)) {
            val books = savedInstanceState.getStringArrayList(STATE_IMPORT_BOOKS)
            val index = savedInstanceState.getInt(STATE_IMPORT_INDEX)

            if (books != null) {
                if (index < books.size) {
                    mImportTask = ImportTask(books, index).execute() as ImportTask
                }
            } else {
                mImportTask = ImportTask().execute() as ImportTask
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.shelves, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onMenuItemSelected(featureId: Int, item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_item_add_search -> {
                onAddSearch()
                return true
            }
            R.id.menu_item_add -> {
                onAdd()
                return true
            }
            R.id.menu_item_check -> {
                onCheck()
                return true
            }
            R.id.menu_item_search -> {
                onSearchRequested()
                return true
            }
            R.id.menu_item_settings -> {
                onSettings()
                return true
            }
        }

        return super.onMenuItemSelected(featureId, item)
    }

    private fun onSettings() {
        SettingsActivity.show(this)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val scanAvailable = ScanIntent.isInstalled(this)

        var item = menu.findItem(R.id.menu_item_add)
        item.isEnabled = scanAvailable && (mAddTask == null || mAddTask?.status == UserTask.Status.FINISHED)

        item = menu.findItem(R.id.menu_item_check)
        item.isEnabled = scanAvailable

        return super.onPrepareOptionsMenu(menu)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_SEARCH) {
            return onSearchRequested()
        }
        return super.onKeyUp(keyCode, event)
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo)

        val info = menuInfo as AdapterView.AdapterContextMenuInfo
        menu.setHeaderTitle((info.targetView as TextView).text)

        menuInflater.inflate(R.menu.book, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val info = item.menuInfo as AdapterView.AdapterContextMenuInfo
        val holder = info.targetView.tag as BookViewHolder

        when (item.itemId) {
            R.id.context_menu_item_view -> {
                onView(holder.bookId)
                return true
            }
            R.id.context_menu_item_buy -> {
                onBuy(BooksManager.findBook(contentResolver, holder.bookId))
                return true
            }
            R.id.context_menu_item_delete -> {
                onDelete(holder.bookId)
                return true
            }
        }

        return super.onContextItemSelected(item)
    }

    private fun onView(bookId: String?) {
        BookDetailsActivity.show(this, bookId)
    }

    private fun onBuy(book: BooksStore.Book?) {
        book?.let {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it.detailsUrl))
            startActivity(intent)
        }
    }

    private fun onDelete(bookId: String?) {
        if (bookId != null && BooksManager.deleteBook(contentResolver, bookId)) {
            UIUtilities.showToast(this, R.string.success_book_deleted)
        }
    }

    private fun startScan(code: Int) {
        try {
            val intent = Intent(ScanIntent.INTENT_ACTION_SCAN)
            intent.putExtra(ScanIntent.INTENT_EXTRA_SCAN_MODE, ScanIntent.INTENT_EXTRA_PRODUCT_MODE)
            startActivityForResult(intent, code)
        } catch (e: ActivityNotFoundException) {
            UIUtilities.showToast(this, R.string.error_missing_barcode_scanner, true)
        }
    }

    private fun onAddSearch() {
        AddBookActivity.show(this)
    }

    private fun onAdd() {
        startScan(REQUEST_SCAN_FOR_ADD)
    }

    private fun onCheck() {
        startScan(REQUEST_SCAN_FOR_CHECK)
    }

    private fun onImport() {
        if (mImportTask == null || mImportTask?.status == ImportTask.Status.FINISHED) {
            mImportTask = ImportTask().execute() as ImportTask
        } else {
            UIUtilities.showToast(this, R.string.error_import_in_progress)
        }
    }

    private fun onCancelAdd() {
        if (mAddTask != null && mAddTask?.status == UserTask.Status.RUNNING) {
            mAddTask?.cancel(true)
            mAddTask = null
        }
    }

    private fun onCancelImport() {
        if (mImportTask != null && mImportTask?.status == UserTask.Status.RUNNING) {
            mImportTask?.cancel(true)
            mImportTask = null
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK && data != null) {
            when (requestCode) {
                REQUEST_SCAN_FOR_ADD -> onScanAdd(data)
                REQUEST_SCAN_FOR_CHECK -> onScanCheck(data)
            }
        }
    }

    private fun onScanAdd(data: Intent) {
        val bundle = data.extras
        if (bundle != null && ScanIntent.FORMAT_EAN_13 == bundle.getString(ScanIntent.SCAN_RESULT_FORMAT)) {
            val id = bundle.getString(ScanIntent.SCAN_RESULT)
            if (id != null && !BooksManager.bookExists(contentResolver, id)) {
                mAddTask = AddTask().execute(id) as AddTask
            } else {
                UIUtilities.showToast(this, R.string.error_book_exists)
            }
        }
    }

    private fun onScanCheck(data: Intent) {
        val bundle = data.extras
        if (bundle != null && ScanIntent.FORMAT_EAN_13 == bundle.getString(ScanIntent.SCAN_RESULT_FORMAT)) {
            val id = bundle.getString(ScanIntent.SCAN_RESULT)
            val bookId = BooksManager.findBookId(contentResolver, id)
            if (bookId == null) {
                UIUtilities.showImageToast(
                    this, R.string.success_book_not_found,
                    resources.getDrawable(R.drawable.unknown_book)
                )
            } else {
                UIUtilities.showImageToast(
                    this, R.string.error_book_exists,
                    ImageUtilities.getCachedCover(bookId, defaultCover)
                )
            }
        }
    }

    private fun showPanel(panel: View, slideUp: Boolean) {
        panel.startAnimation(
            AnimationUtils.loadAnimation(
                this,
                if (slideUp) R.anim.slide_in else R.anim.slide_out_top
            )
        )
        panel.visibility = View.VISIBLE
    }

    private fun hidePanel(panel: View, slideDown: Boolean) {
        panel.startAnimation(
            AnimationUtils.loadAnimation(
                this,
                if (slideDown) R.anim.slide_out else R.anim.slide_in_top
            )
        )
        panel.visibility = View.GONE
    }

    fun updateBookCovers() {
        isPendingCoversUpdate = false

        val grid = mGrid
        val cover = defaultCover
        val count = grid.childCount

        for (i in 0 until count) {
            val view = grid.getChildAt(i)
            val holder = view.tag as BookViewHolder
            if (holder.queryCover) {
                val bookId = holder.bookId

                val cached = ImageUtilities.getCachedCover(bookId, cover)
                val d = holder.transition
                d?.setEnd(cached.bitmap)
                holder.title?.setCompoundDrawablesWithIntrinsicBounds(null, null, null, d)
                d?.startTransition(COVER_TRANSITION_DURATION)
                holder.queryCover = false
            }
        }

        grid.invalidate()
    }

    private fun postUpdateBookCovers() {
        val handler = mScrollHandler
        val message = handler.obtainMessage(MESSAGE_UPDATE_BOOK_COVERS, this)
        handler.removeMessages(MESSAGE_UPDATE_BOOK_COVERS)
        isPendingCoversUpdate = true
        handler.sendMessage(message)
    }

    private fun dismissPopup() {
        mPopup?.dismiss()
    }

    private fun showPopup() {
        if (mPopup == null) {
            val p = PopupWindow(this)
            p.isFocusable = false
            p.contentView = mGridPosition
            p.width = ViewGroup.LayoutParams.FILL_PARENT
            p.height = ViewGroup.LayoutParams.WRAP_CONTENT
            p.setBackgroundDrawable(null)
            p.animationStyle = R.style.PopupAnimation
            mPopup = p
        }

        if (mGrid.windowVisibility == View.VISIBLE) {
            mPopup?.showAtLocation(mGrid, Gravity.CENTER, 0, 0)
        }
    }

    private inner class AddTask : UserTask<String, Void, BooksStore.Book?>() {
        private val mLock = Any()
        var bookId: String? = null
            private set

        override fun onPreExecute() {
            if (mAddPanel == null) {
                mAddPanel = (findViewById<View>(R.id.stub_add) as ViewStub).inflate()
                (mAddPanel?.findViewById<View>(R.id.progress) as ProgressBar).isIndeterminate = true
                (mAddPanel?.findViewById<View>(R.id.label_import) as TextView).text = getText(R.string.add_label)

                val cancelButton = mAddPanel?.findViewById<View>(R.id.button_cancel)
                cancelButton?.setOnClickListener { onCancelAdd() }
            }

            mAddPanel?.let { showPanel(it, false) }
        }

        override fun doInBackground(vararg params: String): BooksStore.Book? {
            synchronized(mLock) {
                bookId = params[0]
            }
            return BooksManager.loadAndAddBook(
                contentResolver, bookId,
                BookStoreFactory.get(this@ShelvesActivity)
            )
        }

        override fun onCancelled() {
            mAddPanel?.let { hidePanel(it, false) }
        }

        override fun onPostExecute(book: BooksStore.Book?) {
            if (book == null) {
                UIUtilities.showToast(this@ShelvesActivity, R.string.error_adding_book)
            } else {
                UIUtilities.showFormattedImageToast(
                    this@ShelvesActivity, R.string.success_added,
                    ImageUtilities.getCachedCover(book.internalId, defaultCover),
                    book.title
                )
            }
            mAddPanel?.let { hidePanel(it, false) }
        }
    }

    private inner class ImportTask : UserTask<Void, Int, Int> {
        private var mResolver: ContentResolver? = null

        val mImportCount = AtomicInteger()
        var mBooks: ArrayList<String>? = null

        constructor()

        constructor(books: ArrayList<String>, index: Int) {
            mBooks = books
            mImportCount.set(index)
        }

        override fun onPreExecute() {
            if (mImportPanel == null) {
                mImportPanel = (findViewById<View>(R.id.stub_import) as ViewStub).inflate()
                mImportProgress = mImportPanel?.findViewById<View>(R.id.progress) as ProgressBar

                val cancelButton = mImportPanel?.findViewById<View>(R.id.button_cancel)
                cancelButton?.setOnClickListener { onCancelImport() }
            }

            mResolver = contentResolver
            mImportProgress?.progress = 0

            mImportPanel?.let { showPanel(it, true) }
        }

        override fun doInBackground(vararg params: Void): Int? {
            var imported = 0

            try {
                if (mBooks == null) mBooks = ImportUtilities.loadItems()

                val list = mBooks
                val booksStore = BookStoreFactory.get(this@ShelvesActivity)
                val count = list?.size ?: 0
                val resolver = mResolver
                val importCount = mImportCount

                for (i in importCount.get() until count) {
                    publishProgress(i, count)
                    if (isCancelled) return null
                    val id = list!![i]
                    if (!BooksManager.bookExists(mResolver, id)) {
                        if (isCancelled) return null
                        val book = BooksManager.loadAndAddBook(resolver, id, booksStore)
                        if (book != null) {
                            if (Config.LOGD) {
                                android.util.Log.d(LOG_TAG, book.toString())
                            }
                            imported++
                        }
                    }
                    importCount.incrementAndGet()
                }
            } catch (e: IOException) {
                return null
            }

            return imported
        }

        override fun onProgressUpdate(vararg values: Int?) {
            val progress = mImportProgress
            progress?.max = values[1] ?: 0
            progress?.progress = values[0] ?: 0
        }

        override fun onCancelled() {
            mImportPanel?.let { hidePanel(it, true) }
        }

        override fun onPostExecute(countImport: Int?) {
            if (countImport == null) {
                UIUtilities.showToast(this@ShelvesActivity, R.string.error_missing_import_file)
            } else {
                UIUtilities.showFormattedToast(this@ShelvesActivity, R.string.success_imported, countImport)
            }
            mImportPanel?.let { hidePanel(it, true) }
        }
    }

    private inner class ShelvesScrollManager : AbsListView.OnScrollListener {
        private var mPreviousPrefix: String? = null
        private var mPopupWillShow = false
        private val mShowPopup = Runnable { showPopup() }
        private val mDismissPopup = Runnable {
            mScrollHandler.removeCallbacks(mShowPopup)
            mPopupWillShow = false
            dismissPopup()
        }

        override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {
            if (this@ShelvesActivity.scrollState == SCROLL_STATE_FLING && scrollState != SCROLL_STATE_FLING) {
                val handler = mScrollHandler
                val message = handler.obtainMessage(MESSAGE_UPDATE_BOOK_COVERS, this@ShelvesActivity)
                handler.removeMessages(MESSAGE_UPDATE_BOOK_COVERS)
                handler.sendMessageDelayed(message, if (mFingerUp) 0 else DELAY_SHOW_BOOK_COVERS.toLong())
                isPendingCoversUpdate = true
            } else if (scrollState == SCROLL_STATE_FLING) {
                isPendingCoversUpdate = false
                mScrollHandler.removeMessages(MESSAGE_UPDATE_BOOK_COVERS)
            }

            if (scrollState == SCROLL_STATE_IDLE) {
                mScrollHandler.removeCallbacks(mShowPopup)

                val booksUpdater = mBooksUpdater
                val count = view.childCount
                for (i in 0 until count) {
                    booksUpdater?.offer((view.getChildAt(i).tag as BookViewHolder).bookId)
                }
            } else {
                mBooksUpdater?.clear()
            }

            this@ShelvesActivity.scrollState = scrollState
        }

        override fun onScroll(view: AbsListView, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
            if (this@ShelvesActivity.scrollState != SCROLL_STATE_FLING) return

            val count = view.childCount
            if (count == 0) return

            val buffer = java.lang.StringBuilder(7)

            var title = (view.getChildAt(0).tag as BookViewHolder).sortTitle
            if (title != null) {
                title = title.substring(0, Math.min(title.length, 2))
                if (title.length == 2) {
                    buffer.append(Character.toUpperCase(title[0]))
                    buffer.append(title[1])
                } else {
                    buffer.append(title.uppercase())
                }
            }

            if (count > 1) {
                buffer.append(" - ")

                val lastChild = count - 1
                title = (view.getChildAt(lastChild).tag as BookViewHolder).sortTitle
                if (title != null) {
                    title = title.substring(0, Math.min(title.length, 2))

                    if (title.length == 2) {
                        buffer.append(Character.toUpperCase(title[0]))
                        buffer.append(title[1])
                    } else {
                        buffer.append(title.uppercase())
                    }
                }
            }

            val prefix = buffer.toString()
            val scrollHandler = mScrollHandler

            if (!mPopupWillShow && (mPopup == null || !mPopup!!.isShowing) && prefix != mPreviousPrefix) {
                mPopupWillShow = true
                val showPopup = mShowPopup
                scrollHandler.removeCallbacks(showPopup)
                scrollHandler.postDelayed(showPopup, WINDOW_SHOW_DELAY.toLong())
            }

            mGridPositionText?.text = prefix
            mPreviousPrefix = prefix

            val dismissPopup = mDismissPopup
            scrollHandler.removeCallbacks(dismissPopup)
            scrollHandler.postDelayed(dismissPopup, WINDOW_DISMISS_DELAY.toLong())
        }
    }

    private class ScrollHandler : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MESSAGE_UPDATE_BOOK_COVERS -> (msg.obj as ShelvesActivity).updateBookCovers()
            }
        }
    }

    private inner class FingerTracker : View.OnTouchListener {
        override fun onTouch(view: View, event: MotionEvent): Boolean {
            val action = event.action
            mFingerUp = action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL
            if (mFingerUp && scrollState != ShelvesScrollManager.SCROLL_STATE_FLING) {
                postUpdateBookCovers()
            }
            return false
        }
    }

    private inner class SelectionTracker : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(adapterView: AdapterView<*>?, view: View, position: Int, id: Long) {
            if (scrollState != ShelvesScrollManager.SCROLL_STATE_IDLE) {
                scrollState = ShelvesScrollManager.SCROLL_STATE_IDLE
                postUpdateBookCovers()
            }
        }

        override fun onNothingSelected(adapterView: AdapterView<*>?) {}
    }

    private inner class BookViewer : AdapterView.OnItemClickListener {
        override fun onItemClick(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
            onView((view.tag as BookViewHolder).bookId)
        }
    }

    companion object {
        private const val LOG_TAG = "Shelves"
        private const val REQUEST_SCAN_FOR_ADD = 1
        private const val REQUEST_SCAN_FOR_CHECK = 2
        private const val COVER_TRANSITION_DURATION = 175
        private const val MESSAGE_UPDATE_BOOK_COVERS = 1
        private const val DELAY_SHOW_BOOK_COVERS = 550
        private const val WINDOW_DISMISS_DELAY = 600
        private const val WINDOW_SHOW_DELAY = 600
        private const val ACTION_IMPORT = "shelves.intent.action.ACTION_IMPORT"
        private const val STATE_IMPORT_IN_PROGRESS = "shelves.import.inprogress"
        private const val STATE_IMPORT_BOOKS = "shelves.import.books"
        private const val STATE_IMPORT_INDEX = "shelves.import.index"
        private const val STATE_ADD_IN_PROGRESS = "shelves.add.inprogress"
        private const val STATE_ADD_BOOK = "shelves.add.book"
    }
}
