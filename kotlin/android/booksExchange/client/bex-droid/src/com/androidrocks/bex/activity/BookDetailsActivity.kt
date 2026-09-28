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
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.ImageView
import android.widget.TextView
import com.androidrocks.bex.R
import com.androidrocks.bex.drawable.FastBitmapDrawable
import com.androidrocks.bex.provider.BooksManager
import com.androidrocks.bex.provider.BooksStore
import com.androidrocks.bex.util.ImageUtilities
import com.androidrocks.bex.util.TextUtilities
import java.text.SimpleDateFormat
import java.util.Date

class BookDetailsActivity : Activity() {
    private var mBook: BooksStore.Book? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBook = getBook()
        if (mBook == null) {
            finish()
            return
        }

        setContentView(R.layout.screen_bookdetails)
        setupViews()
    }

    private fun getBook(): BooksStore.Book? {
        val intent = intent
        if (intent != null) {
            val action = intent.action
            if (Intent.ACTION_VIEW == action) {
                return intent.data?.let { BooksManager.findBook(contentResolver, it) }
            } else {
                val bookId = intent.getStringExtra(EXTRA_BOOK)
                if (bookId != null) {
                    return BooksManager.findBook(contentResolver, bookId)
                }
            }
        }
        return null
    }

    private fun setupViews() {
        val defaultCover = FastBitmapDrawable(
            BitmapFactory.decodeResource(resources, R.drawable.unknown_cover)
        )

        val cover = findViewById<ImageView>(R.id.image_cover)
        cover.setImageDrawable(
            ImageUtilities.getCachedCover(
                mBook!!.internalId,
                defaultCover
            )
        )

        setTextOrHide(R.id.label_title, mBook!!.title)
        setTextOrHide(R.id.label_author, TextUtilities.join(mBook!!.authors, ", "))

        val pages = mBook!!.pagesCount
        if (pages > 0) {
            (findViewById<TextView>(R.id.label_pages)).text = getString(R.string.label_pages, pages)
        } else {
            findViewById<View>(R.id.label_pages).visibility = View.GONE
        }

        val publicationDate = mBook!!.publicationDate
        if (publicationDate != null) {
            val date = SimpleDateFormat("MMMM yyyy").format(publicationDate)
            (findViewById<TextView>(R.id.label_date)).text = date
        } else {
            findViewById<View>(R.id.label_date).visibility = View.GONE
        }

        setTextOrHide(R.id.label_publisher, mBook!!.publisher)

        val details = findViewById<WebView>(R.id.html_reviews)
        details.setBackgroundColor(0)
        details.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY

        val webSettings = details.settings
        webSettings.cacheMode = WebSettings.LOAD_NO_CACHE
        webSettings.savePassword = false
        webSettings.saveFormData = false
        webSettings.javaScriptEnabled = false
        webSettings.setSupportZoom(false)
        webSettings.blockNetworkImage = true

        val descriptions = mBook!!.descriptions
        if (descriptions != null && descriptions.isNotEmpty()) {
            details.loadData(descriptions[0].toString(), "text/html", "utf-8")
        }
    }

    private fun setTextOrHide(id: Int, text: String?) {
        if (!TextUtils.isEmpty(text)) {
            (findViewById<TextView>(id)).text = text
        } else {
            findViewById<View>(id).visibility = View.GONE
        }
    }

    companion object {
        private const val EXTRA_BOOK = "shelves.extra.book_id"

        @JvmStatic
        fun show(context: Context, bookId: String?) {
            val intent = Intent(context, BookDetailsActivity::class.java)
            intent.putExtra(EXTRA_BOOK, bookId)
            context.startActivity(intent)
        }
    }
}
