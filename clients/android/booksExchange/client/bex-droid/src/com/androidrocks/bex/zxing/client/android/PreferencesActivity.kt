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

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.preference.CheckBoxPreference
import android.preference.PreferenceActivity
import com.androidrocks.bex.R

class PreferencesActivity : PreferenceActivity(), OnSharedPreferenceChangeListener {
    var mDecode1D: CheckBoxPreference? = null
    var mDecodeQR: CheckBoxPreference? = null

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        addPreferencesFromResource(R.xml.preferences)

        val preferences = preferenceScreen
        preferences.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        mDecode1D = preferences.findPreference(KEY_DECODE_1D) as CheckBoxPreference
        mDecodeQR = preferences.findPreference(KEY_DECODE_QR) as CheckBoxPreference
    }

    // Prevent the user from turning off both decode options
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (key == KEY_DECODE_1D) {
            mDecodeQR!!.isEnabled = mDecode1D!!.isChecked
            mDecodeQR!!.isChecked = true
        } else if (key == KEY_DECODE_QR) {
            mDecode1D!!.isEnabled = mDecodeQR!!.isChecked
            mDecode1D!!.isChecked = true
        }
    }

    companion object {
        const val KEY_DECODE_1D = "preferences_decode_1D"
        const val KEY_DECODE_QR = "preferences_decode_QR"
        const val KEY_CUSTOM_PRODUCT_SEARCH = "preferences_custom_product_search"

        const val KEY_PLAY_BEEP = "preferences_play_beep"
        const val KEY_VIBRATE = "preferences_vibrate"
        const val KEY_COPY_TO_CLIPBOARD = "preferences_copy_to_clipboard"

        const val KEY_HELP_VERSION_SHOWN = "preferences_help_version_shown"
    }
}
