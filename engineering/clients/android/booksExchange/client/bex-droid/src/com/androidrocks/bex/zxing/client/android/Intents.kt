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

object Intents {

    object Scan {
        /**
         * Send this intent to open the Barcodes app in scanning mode, find a barcode, and return
         * the results.
         */
        const val ACTION = "com.androidrocks.bex.zxing.client.android.SCAN"

        // For compatibility only - do not use in new code, this will go away!
        const val DEPRECATED_ACTION = "com.android.barcodes.SCAN"

        /**
         * By default, sending Scan.ACTION will decode all barcodes that we understand. However it
         * may be useful to limit scanning to certain formats. Use Intent.putExtra(MODE, value) with
         * one of the values below (optional).
         */
        const val MODE = "SCAN_MODE"

        /**
         * Decode only UPC and EAN barcodes. This is the right choice for shopping apps which get
         * prices, reviews, etc. for products.
         */
        const val PRODUCT_MODE = "PRODUCT_MODE"

        /**
         * Decode only 1D barcodes (currently UPC, EAN, Code 39, and Code 128).
         */
        const val ONE_D_MODE = "ONE_D_MODE"

        /**
         * Decode only QR codes.
         */
        const val QR_CODE_MODE = "QR_CODE_MODE"

        /**
         * If a barcode is found, Barcodes returns RESULT_OK to onActivityResult() of the app which
         * requested the scan via startSubActivity(). The barcodes contents can be retrieved with
         * intent.getStringExtra(RESULT). If the user presses Back, the result code will be
         * RESULT_CANCELED.
         */
        const val RESULT = "SCAN_RESULT"

        /**
         * Call intent.getStringExtra(RESULT_FORMAT) to determine which barcode format was found.
         * See Contents.Format for possible values.
         */
        const val RESULT_FORMAT = "SCAN_RESULT_FORMAT"
    }

    object Encode {
        /**
         * Send this intent to encode a piece of data as a QR code and display it full screen, so
         * that another person can scan the barcode from your screen.
         */
        const val ACTION = "com.androidrocks.bex.zxing.client.android.ENCODE"

        // For compatibility only - do not use in new code, this will go away!
        const val DEPRECATED_ACTION = "com.android.barcodes.ENCODE"

        /**
         * The data to encode. Use Intent.putExtra(DATA, data) where data is either a String or a
         * Bundle, depending on the type and format specified. Non-QR Code formats should
         * just use a String here. For QR Code, see Contents for details.
         */
        const val DATA = "ENCODE_DATA"

        /**
         * The type of data being supplied if the format is QR Code. Use
         * Intent.putExtra(TYPE, type) with one of Contents.Type.
         */
        const val TYPE = "ENCODE_TYPE"

        /**
         * The barcode format to be displayed. If this isn't specified or is blank,
         * it defaults to QR Code. Use Intent.putExtra(FORMAT, format), where
         * format is one of Contents.Format.
         */
        const val FORMAT = "com.androidrocks.bex.zxing.client.android.ENCODE_FORMAT"
    }

    object SearchBookContents {
        /**
         * Use Google Book Search to search the contents of the book provided.
         */
        const val ACTION = "com.androidrocks.bex.zxing.client.android.SEARCH_BOOK_CONTENTS"

        // For compatibility only - do not use in new code, this will go away!
        const val DEPRECATED_ACTION = "com.android.barcodes.SEARCH_BOOK_CONTENTS"

        /**
         * The book to search, identified by ISBN number.
         */
        const val ISBN = "ISBN"

        /**
         * An optional field which is the text to search for.
         */
        const val QUERY = "QUERY"
    }

    object Share {
        /**
         * Give the user a choice of items to encode as a barcode, then render it as a QR Code and
         * display onscreen for a friend to scan with their phone.
         */
        const val ACTION = "com.androidrocks.bex.zxing.client.android.SHARE"
    }
}
