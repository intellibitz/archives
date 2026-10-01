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
import android.telephony.PhoneNumberUtils
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import com.androidrocks.bex.R
import com.google.zxing.client.result.AddressBookParsedResult
import com.google.zxing.client.result.ParsedResult
import java.text.DateFormat
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Date

class AddressBookResultHandler(activity: Activity?, result: ParsedResult?) :
    ResultHandler(activity!!, result!!) {

    private val mFields: BooleanArray
    override val buttonCount: Int

    init {
        val addressResult = result as AddressBookParsedResult
        val address = addressResult.address
        val hasAddress = address != null && address.length > 0
        val phoneNumbers = addressResult.phoneNumbers
        val hasPhoneNumber = phoneNumbers != null && phoneNumbers.size > 0
        val emails = addressResult.emails
        val hasEmailAddress = emails != null && emails.size > 0

        mFields = BooleanArray(MAX_BUTTON_COUNT)
        mFields[0] = true // Add contact is always available
        mFields[1] = hasAddress
        mFields[2] = hasPhoneNumber
        mFields[3] = hasEmailAddress

        var buttonCount = 0
        for (x in 0 until MAX_BUTTON_COUNT) {
            if (mFields[x]) {
                buttonCount++
            }
        }
        this.buttonCount = buttonCount
    }

    // This takes all the work out of figuring out which buttons/actions should be in which
    // positions, based on which fields are present in this barcode.
    private fun mapIndexToAction(index: Int): Int {
        if (index < buttonCount) {
            var count = -1
            for (x in 0 until MAX_BUTTON_COUNT) {
                if (mFields[x]) {
                    count++
                }
                if (count == index) {
                    return x
                }
            }
        }
        return -1
    }

    override fun getButtonText(index: Int): Int {
        val action = mapIndexToAction(index)
        when (action) {
            0 -> return R.string.button_add_contact
            1 -> return R.string.button_show_map
            2 -> return R.string.button_dial
            3 -> return R.string.button_email
            else -> throw ArrayIndexOutOfBoundsException()
        }
    }

    override fun handleButtonPress(index: Int) {
        val addressResult = mResult as AddressBookParsedResult
        val action = mapIndexToAction(index)
        when (action) {
            0 -> addContact(
                addressResult.names, addressResult.phoneNumbers,
                addressResult.emails, addressResult.note,
                addressResult.address, addressResult.org,
                addressResult.title
            )

            1 -> {
                val names = addressResult.names
                val title = if (names != null) names[0] else null
                searchMap(addressResult.address, title)
            }

            2 -> dialPhone(addressResult.phoneNumbers[0])
            3 -> sendEmail(addressResult.emails[0], null, null)
            else -> {}
        }
    }

    // Overriden so we can hyphenate phone numbers, format birthdays, and bold the name.
    override val displayContents: CharSequence
        get() {
            val result = mResult as AddressBookParsedResult
            val contents = StringBuffer()
            ParsedResult.maybeAppend(result.names, contents)
            val namesLength = contents.length

            val pronunciation = result.pronunciation
            if (pronunciation != null && pronunciation.length > 0) {
                contents.append("\n(")
                contents.append(pronunciation)
                contents.append(')')
            }

            ParsedResult.maybeAppend(result.title, contents)
            ParsedResult.maybeAppend(result.org, contents)
            ParsedResult.maybeAppend(result.address, contents)
            val numbers = result.phoneNumbers
            if (numbers != null) {
                for (number in numbers) {
                    ParsedResult.maybeAppend(PhoneNumberUtils.formatNumber(number), contents)
                }
            }
            ParsedResult.maybeAppend(result.emails, contents)
            ParsedResult.maybeAppend(result.url, contents)

            val birthday = result.birthday
            if (birthday != null && birthday.length > 0) {
                val date: Date?
                synchronized(DATE_FORMAT) {
                    date = DATE_FORMAT.parse(birthday, ParsePosition(0))
                }
                ParsedResult.maybeAppend(
                    DateFormat.getDateInstance().format(date!!.time),
                    contents
                )
            }
            ParsedResult.maybeAppend(result.note, contents)

            if (namesLength > 0) {
                // Bold the full name to make it stand out a bit.
                val styled: Spannable = SpannableString(contents.toString())
                styled.setSpan(StyleSpan(android.graphics.Typeface.BOLD), 0, namesLength, 0)
                return styled
            } else {
                return contents.toString()
            }
        }

    override val displayTitle: Int
        get() = R.string.result_address_book

    companion object {
        private val DATE_FORMAT: DateFormat = SimpleDateFormat("yyyyMMdd")
    }
}
