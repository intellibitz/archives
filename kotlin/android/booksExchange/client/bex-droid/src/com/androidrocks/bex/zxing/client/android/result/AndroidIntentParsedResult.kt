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

import android.content.Intent
import com.google.zxing.client.result.ParsedResult
import com.google.zxing.client.result.ParsedResultType
import java.net.URISyntaxException

/**
 * A [com.google.zxing.client.result.ParsedResult] derived from a URI that encodes an Android
 * [Intent], and which should presumably trigger that intent on Android.
 */
@Suppress("DEPRECATION")
class AndroidIntentParsedResult private constructor(val intent: Intent) :
    ParsedResult(ParsedResultType.ANDROID_INTENT) {

    override fun getDisplayResult(): String {
        return intent.toString()
    }

    companion object {
        fun parse(rawText: String?): AndroidIntentParsedResult? {
            return try {
                AndroidIntentParsedResult(Intent.getIntent(rawText))
            } catch (urise: URISyntaxException) {
                null
            } catch (iae: IllegalArgumentException) {
                null
            }
        }
    }
}
