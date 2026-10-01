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
import android.content.Intent
import android.os.Bundle
import android.preference.ListPreference
import android.preference.Preference
import android.preference.PreferenceActivity
import com.androidrocks.bex.R
import com.androidrocks.bex.provider.BookStoreFactory
import com.androidrocks.bex.provider.BooksStore
import com.androidrocks.bex.util.Preferences

class SettingsActivity : PreferenceActivity(), Preference.OnPreferenceChangeListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        preferenceManager.sharedPreferencesName = Preferences.NAME
        addPreferencesFromResource(R.xml.preferences)

        val preference = findPreference(Preferences.KEY_BOOKSTORE) as ListPreference
        preference.onPreferenceChangeListener = this
        preference.value = BookStoreFactory.get(this).name

        setBookStorePreferenceEntries(preference)
        setBookStorePreferenceSummary(preference, null)

        val intent = findPreference(Preferences.KEY_IMPORT).intent
        intent?.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
    }

    private fun setBookStorePreferenceEntries(preference: ListPreference) {
        val stores = BookStoreFactory.getStores(this)
        val count = stores.size

        val values = arrayOfNulls<CharSequence>(count)
        val labels = arrayOfNulls<CharSequence>(count)

        for (i in 0 until count) {
            val store = stores[i]
            values[i] = store.name
            labels[i] = store.label
        }

        preference.entries = labels
        preference.entryValues = values
    }

    private fun setBookStorePreferenceSummary(preference: Preference, storeName: String?) {
        val booksStore: BooksStore = if (storeName == null) {
            BookStoreFactory.get(this)
        } else {
            BookStoreFactory.get(this, storeName)
        }

        preference.summary = getString(R.string.preferences_bookstore_summary, booksStore.label)
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        val key = preference.key

        if (Preferences.KEY_BOOKSTORE == key) {
            val storeName = newValue.toString()
            setBookStorePreferenceSummary(preference, storeName)
            (preference as ListPreference).value = storeName
        }

        return false
    }

    companion object {
        @JvmStatic
        fun show(context: Context) {
            val intent = Intent(context, SettingsActivity::class.java)
            context.startActivity(intent)
        }
    }
}
