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

package com.androidrocks.bex.zxing.client.android.result

import android.app.Activity
import android.preference.PreferenceManager
import com.androidrocks.bex.R
import com.androidrocks.bex.zxing.client.android.PreferencesActivity
import com.google.zxing.client.result.ISBNParsedResult
import com.google.zxing.client.result.ParsedResult

class ISBNResultHandler(activity: Activity?, result: ParsedResult?) :
    ResultHandler(activity!!, result!!) {

    private val mCustomProductSearch: String?

    init {
        val prefs = PreferenceManager.getDefaultSharedPreferences(activity)
        mCustomProductSearch = prefs.getString(PreferencesActivity.KEY_CUSTOM_PRODUCT_SEARCH, null)
    }

    override val buttonCount: Int
        get() = if (mCustomProductSearch != null && mCustomProductSearch.length > 0) mButtons.size else mButtons.size - 1

    override fun getButtonText(index: Int): Int {
        return mButtons[index]
    }

    override fun handleButtonPress(index: Int) {
        val isbnResult = mResult as ISBNParsedResult
        when (index) {
            0 -> openProductSearch(isbnResult.isbn)
            1 -> openBookSearch(isbnResult.isbn)
            2 -> searchBookContents(isbnResult.isbn)
            3 -> {
                val url = mCustomProductSearch!!.replace("%s", isbnResult.isbn)
                openURL(url)
            }
        }
    }

    override val displayTitle: Int
        get() = R.string.result_isbn

    companion object {
        private val mButtons = intArrayOf(
            R.string.button_product_search,
            R.string.button_book_search,
            R.string.button_search_book_contents,
            R.string.button_custom_product_search,
        )
    }
}
