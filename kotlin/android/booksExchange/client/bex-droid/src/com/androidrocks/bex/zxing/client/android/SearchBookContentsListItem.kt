/*
 * Copyright (C) 2009 Muthu Ramadoss. All rights reserved.
 *
 * Modified from Zxing project to suit Books-Exchange requirements.
 * Original source from Zxing - http://code.google.com/p/zxing/
 */

/*
 * Copyright (C) 2008 ZXing authors
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

package com.androidrocks.bex.zxing.client.android

import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import com.androidrocks.bex.R

class SearchBookContentsListItem : LinearLayout {

    private var mPageNumberView: TextView? = null
    private var mSnippetView: TextView? = null

    internal constructor(context: Context?) : super(context)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    override fun onFinishInflate() {
        super.onFinishInflate()
        mPageNumberView = findViewById<TextView>(R.id.page_number_view)
        mSnippetView = findViewById<TextView>(R.id.snippet_view)
    }

    fun set(result: SearchBookContentsResult) {
        mPageNumberView!!.text = result.pageNumber
        val snippet = result.snippet
        if (snippet.length > 0) {
            if (result.validSnippet) {
                val lowerQuery = SearchBookContentsResult.query!!.toLowerCase()
                val lowerSnippet = snippet.toLowerCase()
                val styledSnippet: Spannable = SpannableString(snippet)
                val boldSpan = StyleSpan(Typeface.BOLD)
                val queryLength = lowerQuery.length
                var offset = 0
                while (true) {
                    val pos = lowerSnippet.indexOf(lowerQuery, offset)
                    if (pos < 0) {
                        break
                    }
                    styledSnippet.setSpan(boldSpan, pos, pos + queryLength, 0)
                    offset = pos + queryLength
                }
                mSnippetView!!.text = styledSnippet
            } else {
                // This may be an error message, so don't try to bold the query terms within it
                mSnippetView!!.text = snippet
            }
        } else {
            mSnippetView!!.text = ""
        }
    }
}
