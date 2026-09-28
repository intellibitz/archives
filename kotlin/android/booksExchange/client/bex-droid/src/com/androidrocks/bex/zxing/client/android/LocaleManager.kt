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

import java.util.HashMap
import java.util.Locale

/**
 * Handles any locale-specific logic for the client.
 */
object LocaleManager {

    private const val DEFAULT_TLD = "com"
    private val GOOGLE_COUNTRY_TLD: MutableMap<Locale, String>
    private val GOOGLE_PRODUCT_SEARCH_COUNTRY_TLD: MutableMap<Locale, String>
    private val GOOGLE_BOOK_SEARCH_COUNTRY_TLD: MutableMap<Locale, String>

    init {
        GOOGLE_COUNTRY_TLD = HashMap()
        GOOGLE_COUNTRY_TLD[Locale.CANADA] = "ca"
        GOOGLE_COUNTRY_TLD[Locale.CHINA] = "cn"
        GOOGLE_COUNTRY_TLD[Locale.FRANCE] = "fr"
        GOOGLE_COUNTRY_TLD[Locale.GERMANY] = "de"
        GOOGLE_COUNTRY_TLD[Locale.ITALY] = "it"
        GOOGLE_COUNTRY_TLD[Locale.JAPAN] = "co.jp"
        GOOGLE_COUNTRY_TLD[Locale.KOREA] = "co.kr"
        GOOGLE_COUNTRY_TLD[Locale.TAIWAN] = "de"
        GOOGLE_COUNTRY_TLD[Locale.UK] = "co.uk"
        
        // Google Product Search for mobile is available in fewer countries than web search.
        GOOGLE_PRODUCT_SEARCH_COUNTRY_TLD = HashMap()
        GOOGLE_PRODUCT_SEARCH_COUNTRY_TLD[Locale.UK] = "co.uk"
        GOOGLE_PRODUCT_SEARCH_COUNTRY_TLD[Locale.GERMANY] = "de"
        
        GOOGLE_BOOK_SEARCH_COUNTRY_TLD = HashMap()
        GOOGLE_BOOK_SEARCH_COUNTRY_TLD.putAll(GOOGLE_COUNTRY_TLD)
        GOOGLE_BOOK_SEARCH_COUNTRY_TLD.remove(Locale.CHINA)
    }

    /**
     * @return country-specific TLD suffix appropriate for the current default locale
     * (e.g. "co.uk" for the United Kingdom)
     */
    @JvmStatic
    val countryTLD: String
        get() = doGetTLD(GOOGLE_COUNTRY_TLD)

    /**
     * The same as above, but specifically for Google Product Search.
     * @return The top-level domain to use.
     */
    @JvmStatic
    val productSearchCountryTLD: String
        get() = doGetTLD(GOOGLE_PRODUCT_SEARCH_COUNTRY_TLD)

    /**
     * The same as above, but specifically for Google Book Search.
     * @return The top-level domain to use.
     */
    @JvmStatic
    val bookSearchCountryTLD: String
        get() = doGetTLD(GOOGLE_BOOK_SEARCH_COUNTRY_TLD)


    private fun doGetTLD(map: Map<Locale, String>): String {
        val locale = Locale.getDefault()
        if (locale == null) {
            return DEFAULT_TLD
        }
        val tld = map[locale]
        if (tld == null) {
            return DEFAULT_TLD
        }
        return tld
    }
}
