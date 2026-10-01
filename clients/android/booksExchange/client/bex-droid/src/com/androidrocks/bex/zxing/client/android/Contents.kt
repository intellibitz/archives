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

import android.provider.ContactsContract // Using modern contacts API since Contacts is deprecated

object Contents {
    /**
     * All the formats we know about.
     */
    object Format {
        const val UPC_A = "UPC_A"
        const val UPC_E = "UPC_E"
        const val EAN_8 = "EAN_8"
        const val EAN_13 = "EAN_13"
        const val CODE_39 = "CODE_39"
        const val CODE_128 = "CODE_128"
        const val QR_CODE = "QR_CODE"
    }

    object Type {
        /**
         * Plain text. Use Intent.putExtra(DATA, string). This can be used for URLs too, but string
         * must include "http://" or "https://".
         */
        const val TEXT = "TEXT_TYPE"

        /**
         * An email type. Use Intent.putExtra(DATA, string) where string is the email address.
         */
        const val EMAIL = "EMAIL_TYPE"

        /**
         * Use Intent.putExtra(DATA, string) where string is the phone number to call.
         */
        const val PHONE = "PHONE_TYPE"

        /**
         * An SMS type. Use Intent.putExtra(DATA, string) where string is the number to SMS.
         */
        const val SMS = "SMS_TYPE"

        /**
         * A contact. Send a request to encode it as follows:
         * <p/>
         * import android.provider.Contacts;
         * <p/>
         * Intent intent = new Intent(Intents.Encode.ACTION);
         * intent.putExtra(Intents.Encode.TYPE, CONTACT);
         * Bundle bundle = new Bundle();
         * bundle.putString(Contacts.Intents.Insert.NAME, "Jenny");
         * bundle.putString(Contacts.Intents.Insert.PHONE, "8675309");
         * bundle.putString(Contacts.Intents.Insert.EMAIL, "jenny@the80s.com");
         * bundle.putString(Contacts.Intents.Insert.POSTAL, "123 Fake St. San Francisco, CA 94102");
         * intent.putExtra(Intents.Encode.DATA, bundle);
         */
        const val CONTACT = "CONTACT_TYPE"

        /**
         * A geographic location. Use as follows:
         * Bundle bundle = new Bundle();
         * bundle.putFloat("LAT", latitude);
         * bundle.putFloat("LONG", longitude);
         * intent.putExtra(Intents.Encode.DATA, bundle);
         */
        const val LOCATION = "LOCATION_TYPE"
    }

    // These are new constants in Contacts.Intents.Insert for Android 1.1.
    // TODO: Remove these constants once we can build against the 1.1 SDK.
    private const val SECONDARY_PHONE = "secondary_phone"
    private const val TERTIARY_PHONE = "tertiary_phone"
    private const val SECONDARY_EMAIL = "secondary_email"
    private const val TERTIARY_EMAIL = "tertiary_email"


    /**
     * When using Type.CONTACT, these arrays provide the keys for adding or retrieving multiple
     * phone numbers and addresses.
     */
    @JvmField
    val PHONE_KEYS = arrayOf(
        ContactsContract.Intents.Insert.PHONE, SECONDARY_PHONE, TERTIARY_PHONE
    )

    @JvmField
    val EMAIL_KEYS = arrayOf(
        ContactsContract.Intents.Insert.EMAIL, SECONDARY_EMAIL, TERTIARY_EMAIL
    )
}
