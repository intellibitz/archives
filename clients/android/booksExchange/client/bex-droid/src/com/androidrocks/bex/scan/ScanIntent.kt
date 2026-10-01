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

package com.androidrocks.bex.scan

import android.content.Context

object ScanIntent {
    const val INTENT_ACTION_SCAN = "com.androidrocks.bex.zxing.client.android.SCAN"
    const val INTENT_EXTRA_SCAN_MODE = "SCAN_MODE"
    const val INTENT_EXTRA_PRODUCT_MODE = "PRODUCT_MODE"
    const val SCAN_RESULT = "SCAN_RESULT"
    const val SCAN_RESULT_FORMAT = "SCAN_RESULT_FORMAT"
    const val FORMAT_EAN_13 = "EAN_13"

    @JvmStatic
    fun isInstalled(context: Context?): Boolean {
        return true
        /*        final PackageManager packageManager = context.getPackageManager();
        final Intent intent = new Intent(INTENT_ACTION_SCAN);
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
*/
    }
}
