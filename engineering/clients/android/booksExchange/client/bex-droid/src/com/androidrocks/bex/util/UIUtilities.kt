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

package com.androidrocks.bex.util

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.androidrocks.bex.R

object UIUtilities {

    @JvmStatic
    fun showImageToast(context: Context, id: Int, drawable: Drawable?) {
        val view = LayoutInflater.from(context).inflate(R.layout.book_notification, null)
        (view.findViewById<TextView>(R.id.message)).setText(id)
        (view.findViewById<ImageView>(R.id.cover)).setImageDrawable(drawable)

        val toast = Toast(context)
        toast.duration = Toast.LENGTH_LONG
        toast.view = view

        toast.show()
    }

    @JvmStatic
    fun showToast(context: Context?, id: Int) {
        showToast(context, id, false)
    }

    @JvmStatic
    fun showToast(context: Context?, id: Int, longToast: Boolean) {
        Toast.makeText(context, id, if (longToast) Toast.LENGTH_LONG else Toast.LENGTH_SHORT).show()
    }

    @JvmStatic
    fun showFormattedImageToast(
        context: Context, id: Int, drawable: Drawable?,
        vararg args: Any?
    ) {
        val view = LayoutInflater.from(context).inflate(R.layout.book_notification, null)
        (view.findViewById<TextView>(R.id.message)).text = String.format(context.getText(id).toString(), *args)
        (view.findViewById<ImageView>(R.id.cover)).setImageDrawable(drawable)

        val toast = Toast(context)
        toast.duration = Toast.LENGTH_LONG
        toast.view = view

        toast.show()
    }

    @JvmStatic
    fun showFormattedToast(context: Context, id: Int, vararg args: Any?) {
        Toast.makeText(
            context, String.format(context.getText(id).toString(), *args),
            Toast.LENGTH_LONG
        ).show()
    }
}
