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
 * limitations under the License.
 */

package com.androidrocks.bex.zxing.client.android

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.androidrocks.bex.R

class SearchBookContentsAdapter(context: Context?, items: List<SearchBookContentsResult?>?) :
    ArrayAdapter<SearchBookContentsResult?>(context!!, R.layout.search_book_contents_list_item, 0, items!!) {

    override fun getView(position: Int, view: View?, viewGroup: ViewGroup): View {
        val listItem: SearchBookContentsListItem

        if (view == null) {
            val factory = LayoutInflater.from(context)
            listItem = factory.inflate(
                R.layout.search_book_contents_list_item, viewGroup, false
            ) as SearchBookContentsListItem
        } else {
            if (view is SearchBookContentsListItem) {
                listItem = view
            } else {
                return view
            }
        }

        val result = getItem(position)
        if (result != null) {
            listItem.set(result)
        }
        return listItem
    }
}
