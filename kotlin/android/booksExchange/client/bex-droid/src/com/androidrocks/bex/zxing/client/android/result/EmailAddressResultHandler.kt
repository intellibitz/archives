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
import com.androidrocks.bex.R
import com.google.zxing.client.result.EmailAddressParsedResult
import com.google.zxing.client.result.ParsedResult

class EmailAddressResultHandler(activity: Activity?, result: ParsedResult?) :
    ResultHandler(activity!!, result!!) {

    override val buttonCount: Int
        get() = mButtons.size

    override fun getButtonText(index: Int): Int {
        return mButtons[index]
    }

    override fun handleButtonPress(index: Int) {
        val emailResult = mResult as EmailAddressParsedResult
        when (index) {
            0 -> sendEmailFromUri(emailResult.mailtoURI, null, null)
            1 -> {
                val addresses = arrayOfNulls<String>(1)
                addresses[0] = emailResult.emailAddress
                addContact(null, null, addresses as Array<String>, null, null, null, null)
            }
        }
    }

    override val displayTitle: Int
        get() = R.string.result_email_address

    companion object {
        private val mButtons = intArrayOf(
            R.string.button_email,
            R.string.button_add_contact
        )
    }
}
