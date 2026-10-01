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

import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.CursorAdapter
import android.widget.FilterQueryProvider
import android.widget.TextView
import com.androidrocks.bex.R
import com.androidrocks.bex.drawable.CrossFadeDrawable
import com.androidrocks.bex.drawable.FastBitmapDrawable
import com.androidrocks.bex.provider.BooksStore
import com.androidrocks.bex.util.ImageUtilities

internal class BooksAdapter(private val mActivity: ShelvesActivity) :
    CursorAdapter(
        mActivity,
        mActivity.managedQuery(
            BooksStore.Book.CONTENT_URI,
            PROJECTION_IDS_AND_TITLE,
            null,
            null,
            BooksStore.Book.DEFAULT_SORT_ORDER
        ),
        true
    ), FilterQueryProvider {

    private val mInflater: LayoutInflater = LayoutInflater.from(mActivity)
    private val mTitleIndex: Int
    private val mSortTitleIndex: Int
    private val mInternalIdIndex: Int
    private val mSelection: String
    private val mDefaultCoverBitmap: Bitmap
    val defaultCover: FastBitmapDrawable
    private val mArguments2 = arrayOfNulls<String>(2)

    init {
        val c = cursor

        mTitleIndex = c.getColumnIndexOrThrow(BooksStore.Book.TITLE)
        mSortTitleIndex = c.getColumnIndexOrThrow(BooksStore.Book.SORT_TITLE)
        mInternalIdIndex = c.getColumnIndexOrThrow(BooksStore.Book.INTERNAL_ID)

        mDefaultCoverBitmap = BitmapFactory.decodeResource(mActivity.resources, R.drawable.unknown_cover)
        defaultCover = FastBitmapDrawable(mDefaultCoverBitmap)

        val selection = StringBuilder()
        selection.append(BooksStore.Book.TITLE)
        selection.append(" LIKE ? OR ")
        selection.append(BooksStore.Book.AUTHORS)
        selection.append(" LIKE ?")
        mSelection = selection.toString()

        filterQueryProvider = this
    }

    override fun newView(context: Context, cursor: Cursor, parent: ViewGroup): View {
        val view = mInflater.inflate(R.layout.shelf_book, parent, false) as TextView

        val holder = BookViewHolder()
        holder.title = view

        view.tag = holder

        val transition = CrossFadeDrawable(mDefaultCoverBitmap, null)
        transition.callback = view
        transition.isCrossFadeEnabled = true
        holder.transition = transition

        return view
    }

    override fun bindView(view: View, context: Context, c: Cursor) {
        val holder = view.tag as BookViewHolder
        val bookId = c.getString(mInternalIdIndex)
        holder.bookId = bookId
        holder.sortTitle = c.getString(mSortTitleIndex)

        if (mActivity.scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING || mActivity.isPendingCoversUpdate) {
            holder.title?.setCompoundDrawablesWithIntrinsicBounds(null, null, null, defaultCover)
            holder.queryCover = true
        } else {
            holder.title?.setCompoundDrawablesWithIntrinsicBounds(
                null, null, null,
                ImageUtilities.getCachedCover(bookId, defaultCover)
            )
            holder.queryCover = false
        }

        val buffer = holder.buffer
        c.copyStringToBuffer(mTitleIndex, buffer)
        val size = buffer.sizeCopied
        if (size != 0) {
            holder.title?.setText(buffer.data, 0, size)
        }
    }

    override fun changeCursor(cursor: Cursor?) {
        val oldCursor = getCursor()
        if (oldCursor != null) mActivity.stopManagingCursor(oldCursor)
        super.changeCursor(cursor)
    }

    override fun runQuery(constraint: CharSequence?): Cursor? {
        if (constraint == null || constraint.isEmpty()) {
            return mActivity.managedQuery(
                BooksStore.Book.CONTENT_URI, PROJECTION_IDS_AND_TITLE,
                null, null, BooksStore.Book.DEFAULT_SORT_ORDER
            )
        }

        val buffer = java.lang.StringBuilder()
        buffer.append('%').append(constraint).append('%')
        val pattern = buffer.toString()

        val arguments2 = mArguments2
        arguments2[0] = pattern
        arguments2[1] = pattern
        return mActivity.managedQuery(
            BooksStore.Book.CONTENT_URI, PROJECTION_IDS_AND_TITLE,
            mSelection, arguments2, BooksStore.Book.DEFAULT_SORT_ORDER
        )
    }

    companion object {
        private val PROJECTION_IDS_AND_TITLE = arrayOf(
            BooksStore.Book._ID,
            BooksStore.Book.INTERNAL_ID,
            BooksStore.Book.TITLE,
            BooksStore.Book.SORT_TITLE
        )
    }
}
