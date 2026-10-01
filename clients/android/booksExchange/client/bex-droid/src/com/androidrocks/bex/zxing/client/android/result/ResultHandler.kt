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
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.provider.Contacts
import android.provider.ContactsContract
import com.androidrocks.bex.R
import com.androidrocks.bex.zxing.client.android.Contents
import com.androidrocks.bex.zxing.client.android.Intents
import com.androidrocks.bex.zxing.client.android.LocaleManager
import com.androidrocks.bex.zxing.client.android.SearchBookContentsActivity
import com.google.zxing.client.result.ParsedResult
import com.google.zxing.client.result.ParsedResultType
import java.text.DateFormat
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar

@Suppress("DEPRECATION")
abstract class ResultHandler protected constructor(
    private val mActivity: Activity,
    @JvmField protected val mResult: ParsedResult
) {

    /**
     * Indicates how many buttons the derived class wants shown.
     *
     * @return The integer button count.
     */
    abstract val buttonCount: Int

    /**
     * The text of the nth action button.
     *
     * @param index From 0 to getButtonCount() - 1
     * @return The button text as a resource ID
     */
    abstract fun getButtonText(index: Int): Int


    /**
     * Execute the action which corresponds to the nth button.
     *
     * @param index The button that was clicked.
     */
    abstract fun handleButtonPress(index: Int)

    /**
     * Create a possibly styled string for the contents of the current barcode.
     *
     * @return The text to be displayed.
     */
    val displayContents: CharSequence
        get() {
            val contents = mResult.displayResult
            return contents.replace("\r", "")
        }

    /**
     * A string describing the kind of barcode that was found, e.g. "Found contact info".
     *
     * @return The resource ID of the string.
     */
    abstract val displayTitle: Int

    /**
     * A convenience method to get the parsed type. Should not be overridden.
     *
     * @return The parsed type, e.g. URI or ISBN
     */
    val type: ParsedResultType
        get() = mResult.type

    /**
     * Sends an intent to create a new calendar event by prepopulating the Add Event UI. Older
     * versions of the system have a bug where the event title will not be filled out.
     *
     * @param summary A description of the event
     * @param start   The start time as yyyyMMdd or yyyyMMdd'T'HHmmss or yyyyMMdd'T'HHmmss'Z'
     * @param end     The end time as yyyyMMdd or yyyyMMdd'T'HHmmss or yyyyMMdd'T'HHmmss'Z'
     */
    fun addCalendarEvent(summary: String, start: String, end: String) {
        val intent = Intent(Intent.ACTION_EDIT)
        intent.setType("vnd.android.cursor.item/event")
        intent.putExtra("beginTime", calculateMilliseconds(start))
        if (start.length == 8) {
            intent.putExtra("allDay", true)
        }
        intent.putExtra("endTime", calculateMilliseconds(end))
        intent.putExtra("title", summary)
        launchIntent(intent)
    }

    fun addContact(
        names: Array<String>?, phoneNumbers: Array<String>?, emails: Array<String>?, note: String?,
        address: String?, org: String?, title: String?
    ) {
        // Only use the first name in the array, if present.
        val intent = Intent(Contacts.Intents.Insert.ACTION, Contacts.People.CONTENT_URI)
        putExtra(intent, ContactsContract.Intents.Insert.NAME, names?.get(0))

        val phoneCount = Math.min(
            phoneNumbers?.size ?: 0,
            Contents.PHONE_KEYS.size
        )
        for (x in 0 until phoneCount) {
            putExtra(intent, Contents.PHONE_KEYS[x], phoneNumbers!![x])
        }

        val emailCount = Math.min(emails?.size ?: 0, Contents.EMAIL_KEYS.size)
        for (x in 0 until emailCount) {
            putExtra(intent, Contents.EMAIL_KEYS[x], emails!![x])
        }

        putExtra(intent, ContactsContract.Intents.Insert.NOTES, note)
        putExtra(intent, ContactsContract.Intents.Insert.POSTAL, address)
        putExtra(intent, ContactsContract.Intents.Insert.COMPANY, org)
        putExtra(intent, ContactsContract.Intents.Insert.JOB_TITLE, title)
        launchIntent(intent)
    }

    fun shareByEmail(contents: String?) {
        sendEmailFromUri("mailto:", mActivity.getString(R.string.msg_share_subject_line), contents)
    }

    fun sendEmail(address: String, subject: String?, body: String?) {
        sendEmailFromUri("mailto:$address", subject, body)
    }

    // Use public Intent fields rather than private GMail app fields to specify subject and body.
    fun sendEmailFromUri(uri: String?, subject: String?, body: String?) {
        val intent = Intent(Intent.ACTION_SEND, Uri.parse(uri))
        putExtra(intent, Intent.EXTRA_SUBJECT, subject)
        putExtra(intent, Intent.EXTRA_TEXT, body)
        intent.setType("text/plain")
        launchIntent(intent)
    }

    fun shareBySMS(contents: String) {
        sendSMSFromUri(
            "smsto:", mActivity.getString(R.string.msg_share_subject_line) + ":\n" +
                    contents
        )
    }

    fun sendSMS(phoneNumber: String, body: String?) {
        sendSMSFromUri("smsto:$phoneNumber", body)
    }

    fun sendSMSFromUri(uri: String?, body: String?) {
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse(uri))
        putExtra(intent, "sms_body", body)
        // Exit the app once the SMS is sent
        intent.putExtra("compose_mode", true)
        launchIntent(intent)
    }

    fun sendMMS(phoneNumber: String, subject: String?, body: String?) {
        sendMMSFromUri("mmsto:$phoneNumber", subject, body)
    }

    fun sendMMSFromUri(uri: String?, subject: String?, body: String?) {
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse(uri))
        // The Messaging app needs to see a valid subject or else it will treat this an an SMS.
        if (subject == null || subject.length == 0) {
            putExtra(intent, "subject", mActivity.getString(R.string.msg_default_mms_subject))
        } else {
            putExtra(intent, "subject", subject)
        }
        putExtra(intent, "sms_body", body)
        intent.putExtra("compose_mode", true)
        launchIntent(intent)
    }

    fun dialPhone(phoneNumber: String) {
        launchIntent(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber")))
    }

    fun dialPhoneFromUri(uri: String?) {
        launchIntent(Intent(Intent.ACTION_DIAL, Uri.parse(uri)))
    }

    fun openMap(geoURI: String?) {
        launchIntent(Intent(Intent.ACTION_VIEW, Uri.parse(geoURI)))
    }

    /**
     * Do a geo search using the address as the query.
     *
     * @param address The address to find
     * @param title An optional title, e.g. the name of the business at this address
     */
    fun searchMap(address: String, title: String?) {
        var query = address
        if (title != null && title.length > 0) {
            query = "$query ($title)"
        }
        launchIntent(Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + Uri.encode(query))))
    }

    fun getDirections(latitude: Double, longitude: Double) {
        launchIntent(
            Intent(
                Intent.ACTION_VIEW, Uri.parse(
                    "http://maps.google." +
                            LocaleManager.countryTLD + "/maps?f=d&daddr=" + latitude + ',' + longitude
                )
            )
        )
    }

    // Uses the mobile-specific version of Product Search, which is formatted for small screens.
    fun openProductSearch(upc: String) {
        val uri = Uri.parse(
            "http://www.google." + LocaleManager.productSearchCountryTLD +
                    "/m/products?q=" + upc + "&source=zxing"
        )
        launchIntent(Intent(Intent.ACTION_VIEW, uri))
    }

    fun openBookSearch(isbn: String) {
        val uri = Uri.parse(
            "http://books.google." + LocaleManager.bookSearchCountryTLD +
                    "/books?vid=isbn" + isbn
        )
        launchIntent(Intent(Intent.ACTION_VIEW, uri))
    }

    fun searchBookContents(isbn: String?) {
        val intent = Intent(Intents.SearchBookContents.ACTION)
        intent.setClassName(mActivity, SearchBookContentsActivity::class.java.name)
        putExtra(intent, Intents.SearchBookContents.ISBN, isbn)
        launchIntent(intent)
    }

    fun openURL(url: String?) {
        launchIntent(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    fun webSearch(query: String?) {
        val intent = Intent(Intent.ACTION_WEB_SEARCH)
        intent.putExtra("query", query)
        launchIntent(intent)
    }

    private fun launchIntent(intent: Intent?) {
        if (intent != null) {
            try {
                mActivity.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                val builder = AlertDialog.Builder(mActivity)
                builder.setTitle(mActivity.getString(R.string.app_name))
                builder.setMessage(mActivity.getString(R.string.msg_intent_failed))
                builder.setPositiveButton(R.string.button_ok, null)
                builder.show()
            }
        }
    }

    companion object {
        private val DATE_FORMAT: DateFormat = SimpleDateFormat("yyyyMMdd")
        private val DATE_TIME_FORMAT: DateFormat = SimpleDateFormat("yyyyMMdd'T'HHmmss")

        const val MAX_BUTTON_COUNT = 4

        private fun calculateMilliseconds(whenString: String): Long {
            if (whenString.length == 8) {
                // Only contains year/month/day
                val date: Date?
                synchronized(DATE_FORMAT) {
                    date = DATE_FORMAT.parse(whenString, ParsePosition(0))
                }
                return date!!.time
            } else {
                // The when string can be local time, or UTC if it ends with a Z
                val date: Date?
                synchronized(DATE_TIME_FORMAT) {
                    date = DATE_TIME_FORMAT.parse(whenString.substring(0, 15), ParsePosition(0))
                }
                var milliseconds = date!!.time
                if (whenString.length == 16 && whenString[15] == 'Z') {
                    val calendar: Calendar = GregorianCalendar()
                    val offset = calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET)
                    milliseconds += offset.toLong()
                }
                return milliseconds
            }
        }

        private fun putExtra(intent: Intent, key: String, value: String?) {
            if (value != null && value.length > 0) {
                intent.putExtra(key, value)
            }
        }
    }
}
