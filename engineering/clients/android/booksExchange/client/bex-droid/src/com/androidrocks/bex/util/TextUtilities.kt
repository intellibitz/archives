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

import java.util.Collection

object TextUtilities {

    @JvmStatic
    fun join(items: Collection<*>?, delimiter: String): String {
        if (items == null || items.isEmpty()) {
            return ""
        }

        val iter = items.iterator()
        val buffer = java.lang.StringBuilder(iter.next().toString())

        while (iter.hasNext()) {
            buffer.append(delimiter).append(iter.next())
        }

        return buffer.toString()
    }
}
