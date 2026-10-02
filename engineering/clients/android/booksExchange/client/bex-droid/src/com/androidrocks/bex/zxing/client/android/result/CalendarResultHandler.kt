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
import com.google.zxing.client.result.CalendarParsedResult
import com.google.zxing.client.result.ParsedResult
import java.text.DateFormat
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar

class CalendarResultHandler(activity: Activity?, result: ParsedResult?) :
    ResultHandler(activity!!, result!!) {

    override val buttonCount: Int
        get() = mButtons.size

    override fun getButtonText(index: Int): Int {
        return mButtons[index]
    }

    override fun handleButtonPress(index: Int) {
        val calendarResult = mResult as CalendarParsedResult
        when (index) {
            0 -> addCalendarEvent(
                calendarResult.summary, calendarResult.start,
                calendarResult.end
            )
        }
    }

    override val displayContents: CharSequence
        get() {
            val calResult = mResult as CalendarParsedResult
            val result = StringBuffer()
            ParsedResult.maybeAppend(calResult.summary, result)
            appendTime(calResult.start, result)

            // The end can be null if the event has no duration, so use the start time.
            var endString = calResult.end
            if (endString == null) {
                endString = calResult.start
            }
            appendTime(endString, result)

            ParsedResult.maybeAppend(calResult.location, result)
            ParsedResult.maybeAppend(calResult.attendee, result)
            ParsedResult.maybeAppend(calResult.title, result)
            return result.toString()
        }

    override val displayTitle: Int
        get() = R.string.result_calendar

    companion object {
        private val DATE_FORMAT: DateFormat = SimpleDateFormat("yyyyMMdd")
        private val DATE_TIME_FORMAT: DateFormat = SimpleDateFormat("yyyyMMdd'T'HHmmss")

        private val mButtons = intArrayOf(
            R.string.button_add_calendar
        )

        private fun appendTime(whenString: String, result: StringBuffer) {
            if (whenString.length == 8) {
                // Show only year/month/day
                val date: Date?
                synchronized(DATE_FORMAT) {
                    date = DATE_FORMAT.parse(whenString, ParsePosition(0))
                }
                ParsedResult.maybeAppend(
                    DateFormat.getDateInstance().format(date!!.time),
                    result
                )
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
                ParsedResult.maybeAppend(
                    DateFormat.getDateTimeInstance().format(milliseconds),
                    result
                )
            }
        }
    }
}
