/*
 * Copyright (C) 2009 Muthu Ramadoss. All rights reserved.
 *
 * Modified from Romain Guy Shelves project to suit Books-Exchange requirements.
 * Original source from Shelves - http://code.google.com/p/shelves/
 */

/*
 * Copyright (C) 2009 Romain Guy
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
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import android.view.animation.AnimationUtils
import android.widget.*
import com.androidrocks.bex.R
import com.androidrocks.bex.drawable.FastBitmapDrawable
import com.androidrocks.bex.provider.BookStoreFactory
import com.androidrocks.bex.provider.BooksManager
import com.androidrocks.bex.provider.BooksStore
import com.androidrocks.bex.util.ImageUtilities
import com.androidrocks.bex.util.TextUtilities
import com.androidrocks.bex.util.UIUtilities
import com.androidrocks.bex.util.UserTask

class AddBookActivity : Activity(), View.OnClickListener, AdapterView.OnItemClickListener {

    private var mSearchTask: SearchTask? = null
    private var mAddTask: AddTask? = null

    private lateinit var mSearchButton: View
    private lateinit var mSearchQuery: EditText
    private var mSearchPanel: View? = null
    private var mAddPanel: View? = null

    private lateinit var mBooksAdapter: SearchResultsAdapter
    private var mBookToAdd: BooksStore.Book? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.screen_add_search)
        setupViews()
    }

    private fun setupViews() {
        mSearchButton = findViewById(R.id.button_go)
        mSearchButton.setOnClickListener(this)
        mSearchButton.isEnabled = false

        mSearchQuery = findViewById(R.id.input_search_query)
        mSearchQuery.addTextChangedListener(SearchFieldWatcher())

        val cover = FastBitmapDrawable(
            ImageUtilities.createShadow(
                BitmapFactory.decodeResource(resources, R.drawable.unknown_cover_no_shadow),
                BOOK_COVER_WIDTH, BOOK_COVER_HEIGHT
            )
        )

        mBooksAdapter = SearchResultsAdapter(this, cover)

        val resultsAdapter = mBooksAdapter
        val oldAdapter = lastNonConfigurationInstance as? SearchResultsAdapter

        if (oldAdapter != null) {
            val count = oldAdapter.count
            for (i in 0 until count) {
                oldAdapter.getItem(i)?.let { resultsAdapter.add(it) }
            }
        }

        val searchResults = findViewById<ListView>(R.id.list_search_results)
        searchResults.adapter = resultsAdapter
        searchResults.onItemClickListener = this
    }

    override fun onDestroy() {
        super.onDestroy()
        onCancelAdd()
        onCancelSearch()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        restoreBookToAdd(savedInstanceState)
        restoreAddTask(savedInstanceState)
        restoreSearchTask(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (isFinishing) {
            saveBookToAdd(outState)
            saveAddTask(outState)
            saveSearchTask(outState)
        }
    }

    override fun onRetainNonConfigurationInstance(): Any {
        return mBooksAdapter
    }

    private fun saveBookToAdd(outState: Bundle) {
        mBookToAdd?.let {
            outState.putParcelable(STATE_BOOK_TO_ADD, it)
        }
    }

    private fun restoreBookToAdd(savedInstanceState: Bundle) {
        val data = savedInstanceState.getParcelable<BooksStore.Book>(STATE_BOOK_TO_ADD)
        if (data != null) {
            mBookToAdd = data
        }
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

    private fun saveSearchTask(outState: Bundle) {
        val task = mSearchTask
        if (task != null && task.status != UserTask.Status.FINISHED) {
            val bookId = task.query
            task.cancel(true)

            if (bookId != null) {
                outState.putBoolean(STATE_SEARCH_IN_PROGRESS, true)
                outState.putString(STATE_SEARCH_QUERY, bookId)
            }

            mSearchTask = null
        }
    }

    private fun restoreSearchTask(savedInstanceState: Bundle) {
        if (savedInstanceState.getBoolean(STATE_SEARCH_IN_PROGRESS)) {
            val query = savedInstanceState.getString(STATE_SEARCH_QUERY)
            if (!TextUtils.isEmpty(query)) {
                mSearchTask = SearchTask().execute(query) as SearchTask
            }
        }
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        mBookToAdd = mBooksAdapter.getItem(position)?.book
        showDialog(DIALOG_ADD)
    }

    override fun onCreateDialog(id: Int): Dialog? {
        return when (id) {
            DIALOG_ADD -> {
                val builder = AlertDialog.Builder(this)
                builder.setTitle(mBookToAdd?.title ?: " ")
                builder.setIcon(android.R.drawable.ic_dialog_alert)
                builder.setMessage(R.string.dialog_add_message)
                builder.setPositiveButton(R.string.dialog_add_ok) { _, _ ->
                    var bookId = mBookToAdd?.ean
                    if (bookId == null) bookId = mBookToAdd?.isbn
                    if (bookId == null) bookId = mBookToAdd?.internalIdNoPrefix

                    if (bookId != null) {
                        onAdd(bookId)
                    }
                    mBookToAdd = null
                }
                builder.setNegativeButton(R.string.dialog_add_cancel) { _, _ ->
                    mBookToAdd = null
                    dismissDialog(DIALOG_ADD)
                }
                builder.setOnCancelListener {
                    mBookToAdd = null
                    dismissDialog(DIALOG_ADD)
                }
                builder.setCancelable(true)
                builder.create()
            }
            else -> super.onCreateDialog(id)
        }
    }

    override fun onPrepareDialog(id: Int, dialog: Dialog) {
        super.onPrepareDialog(id, dialog)
        when (id) {
            DIALOG_ADD -> mBookToAdd?.let { dialog.setTitle(it.title) }
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.button_go -> onSearch()
        }
    }

    private fun onSearch() {
        if (mSearchTask == null || mSearchTask?.status == UserTask.Status.FINISHED) {
            mSearchTask = SearchTask().execute(mSearchQuery.text.toString()) as SearchTask
        } else {
            UIUtilities.showToast(this, R.string.error_search_in_progress)
        }
    }

    private fun onCancelSearch() {
        if (mSearchTask != null && mSearchTask?.status == UserTask.Status.RUNNING) {
            mSearchTask?.cancel(true)
            mSearchTask = null
        }
    }

    private fun onAdd(id: String) {
        if (!BooksManager.bookExists(contentResolver, id)) {
            mAddTask = AddTask().execute(id) as AddTask
        } else {
            UIUtilities.showToast(this, R.string.error_book_exists)
        }
    }

    private fun onCancelAdd() {
        if (mAddTask != null && mAddTask?.status == UserTask.Status.RUNNING) {
            mAddTask?.cancel(true)
            mAddTask = null
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

    private fun disableSearchPanel() {
        mSearchButton.isEnabled = false
        mSearchQuery.isEnabled = false
    }

    private fun enableSearchPanel() {
        mSearchButton.isEnabled = true
        mSearchQuery.isEnabled = true
    }

    private inner class AddTask : UserTask<String, Void, BooksStore.Book?>() {
        private val mLock = Any()
        var bookId: String? = null
            private set
        private var mDefaultCover: FastBitmapDrawable? = null

        override fun onPreExecute() {
            val defaultCoverBitmap = BitmapFactory.decodeResource(
                resources,
                R.drawable.unknown_cover
            )
            mDefaultCover = FastBitmapDrawable(defaultCoverBitmap)

            if (mAddPanel == null) {
                mAddPanel = (findViewById<View>(R.id.stub_add) as ViewStub).inflate()
                (mAddPanel?.findViewById<View>(R.id.progress) as ProgressBar).isIndeterminate = true

                val cancelButton = mAddPanel?.findViewById<View>(R.id.button_cancel)
                cancelButton?.setOnClickListener { onCancelAdd() }
            }

            disableSearchPanel()
            mAddPanel?.let { showPanel(it, false) }
        }

        override fun doInBackground(vararg params: String): BooksStore.Book? {
            synchronized(mLock) {
                bookId = params[0]
            }
            return BooksManager.loadAndAddBook(
                contentResolver, bookId,
                BookStoreFactory.get(this@AddBookActivity)
            )
        }

        override fun onCancelled() {
            enableSearchPanel()
            mAddPanel?.let { hidePanel(it, false) }
        }

        override fun onPostExecute(book: BooksStore.Book?) {
            enableSearchPanel()
            if (book == null) {
                UIUtilities.showToast(this@AddBookActivity, R.string.error_adding_book)
            } else {
                UIUtilities.showFormattedImageToast(
                    this@AddBookActivity, R.string.success_added,
                    ImageUtilities.getCachedCover(book.internalId, mDefaultCover),
                    book.title
                )
            }
            mAddPanel?.let { hidePanel(it, false) }
        }
    }

    private inner class SearchTask : UserTask<String, ResultBook, Void?>(), BooksStore.BookSearchListener {
        private val mLock = Any()
        var query: String? = null
            private set

        override fun onPreExecute() {
            disableSearchPanel()

            if (mSearchPanel == null) {
                mSearchPanel = (findViewById<View>(R.id.stub_search) as ViewStub).inflate()

                val progress = mSearchPanel?.findViewById<View>(R.id.progress) as ProgressBar
                progress.isIndeterminate = true

                (findViewById<View>(R.id.label_import) as TextView).setText(R.string.search_progress)

                val cancelButton = mSearchPanel?.findViewById<View>(R.id.button_cancel)
                cancelButton?.setOnClickListener { onCancelSearch() }
            }

            mBooksAdapter.clear()
            mSearchPanel?.let { showPanel(it, true) }
        }

        override fun doInBackground(vararg params: String): Void? {
            synchronized(mLock) {
                query = params[0]
            }
            BookStoreFactory.get(this@AddBookActivity).searchBooks(query, this)
            return null
        }

        override fun onProgressUpdate(vararg values: ResultBook) {
            for (book in values) {
                mBooksAdapter.add(book)
            }
        }

        override fun onPostExecute(ignore: Void?) {
            enableSearchPanel()
            UIUtilities.showFormattedToast(
                this@AddBookActivity, R.string.success_found,
                mBooksAdapter.count
            )
            mSearchPanel?.let { hidePanel(it, true) }
        }

        override fun onCancelled() {
            enableSearchPanel()
            mSearchPanel?.let { hidePanel(it, true) }
        }

        override fun onBookFound(book: BooksStore.Book?, books: java.util.ArrayList<BooksStore.Book>?) {
            if (book != null && !isCancelled) {
                publishProgress(ResultBook(book))
            }
        }
    }

    private inner class SearchFieldWatcher : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            mSearchButton.isEnabled = s.isNotEmpty()
        }
        override fun afterTextChanged(s: Editable) {}
    }

    private class SearchResultsAdapter(activity: AddBookActivity, cover: FastBitmapDrawable) :
        ArrayAdapter<ResultBook>(activity, 0) {
        
        private val mLayoutInflater: LayoutInflater = LayoutInflater.from(activity)
        private val mDefaultCover: FastBitmapDrawable = cover

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var view = convertView
            val holder: ViewHolder
            if (view == null) {
                view = mLayoutInflater.inflate(R.layout.search_result_book, parent, false)
                holder = ViewHolder()
                holder.cover = view.findViewById<View>(R.id.image_cover) as ImageView
                holder.title = view.findViewById<View>(R.id.label_title) as TextView
                holder.author = view.findViewById<View>(R.id.label_author) as TextView
                view.tag = holder
            } else {
                holder = view.tag as ViewHolder
            }

            val book = getItem(position)
            if (book != null) {
                holder.book = book.book
                holder.title?.text = book.title
                holder.author?.text = book.authors

                val hasCover = book.cover != null
                holder.cover?.setImageDrawable(if (hasCover) book.cover else mDefaultCover)
            }
            return view!!
        }
    }

    private class ViewHolder {
        var cover: ImageView? = null
        var title: TextView? = null
        var author: TextView? = null
        var book: BooksStore.Book? = null
    }

    private class ResultBook(val book: BooksStore.Book) {
        val text: String
        val title: String?
        val authors: String
        val cover: FastBitmapDrawable?

        init {
            val bitmap = ImageUtilities.createShadow(
                book.loadCover(BooksStore.ImageSize.THUMBNAIL),
                BOOK_COVER_WIDTH, BOOK_COVER_HEIGHT
            )
            if (bitmap != null) {
                cover = FastBitmapDrawable(bitmap)
            } else {
                cover = null
            }
            title = book.title
            authors = TextUtilities.join(book.authors, ", ")
            text = "$title $authors"
        }

        override fun toString(): String {
            return text
        }
    }

    companion object {
        private const val BOOK_COVER_WIDTH = 70
        private const val BOOK_COVER_HEIGHT = 70
        private const val DIALOG_ADD = 1
        private const val STATE_ADD_IN_PROGRESS = "shelves.add.inprogress"
        private const val STATE_ADD_BOOK = "shelves.add.book"
        private const val STATE_SEARCH_IN_PROGRESS = "shelves.search.inprogress"
        private const val STATE_SEARCH_QUERY = "shelves.search.book"
        private const val STATE_BOOK_TO_ADD = "shelves.add.bookToAdd"

        @JvmStatic
        fun show(context: Context) {
            val intent = Intent(context, AddBookActivity::class.java)
            context.startActivity(intent)
        }
    }
}
