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

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.BaseColumns
import android.provider.Browser
import android.provider.Contacts
import android.provider.ContactsContract
import android.text.ClipboardManager
import android.view.View
import android.widget.Button
import com.androidrocks.bex.R

@Suppress("DEPRECATION")
class ShareActivity : Activity() {

    private var mClipboardButton: Button? = null

    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        setContentView(R.layout.share)

        val mContactButton = findViewById<View>(R.id.contact_button) as Button
        mContactButton.setOnClickListener(mContactListener)
        val mBookmarkButton = findViewById<View>(R.id.bookmark_button) as Button
        mBookmarkButton.setOnClickListener(mBookmarkListener)
        mClipboardButton = findViewById<View>(R.id.clipboard_button) as Button
        mClipboardButton!!.setOnClickListener(mClipboardListener)
    }

    override fun onResume() {
        super.onResume()

        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        if (clipboard.hasText()) {
            mClipboardButton!!.isEnabled = true
            mClipboardButton!!.setText(R.string.button_share_clipboard)
        } else {
            mClipboardButton!!.isEnabled = false
            mClipboardButton!!.setText(R.string.button_clipboard_empty)
        }
    }

    private val mContactListener = View.OnClickListener {
        startActivityForResult(
            Intent(Intent.ACTION_PICK, Contacts.People.CONTENT_URI),
            PICK_CONTACT
        )
    }

    private val mBookmarkListener = View.OnClickListener {
        val intent = Intent(Intent.ACTION_PICK)
        intent.setClassName(this@ShareActivity, BookmarkPickerActivity::class.java.name)
        startActivityForResult(intent, PICK_BOOKMARK)
    }

    private val mClipboardListener = View.OnClickListener {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        // Should always be true, because we grey out the clipboard button in onResume() if it's empty
        if (clipboard.hasText()) {
            val intent = Intent(Intents.Encode.ACTION)
            intent.putExtra(Intents.Encode.TYPE, Contents.Type.TEXT)
            intent.putExtra(Intents.Encode.DATA, clipboard.text)
            intent.putExtra(Intents.Encode.FORMAT, Contents.Format.QR_CODE)
            startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (resultCode == RESULT_OK && intent != null) {
            when (requestCode) {
                PICK_BOOKMARK -> showTextAsBarcode(intent.getStringExtra(Browser.BookmarkColumns.URL))
                PICK_CONTACT ->                     // Data field is content://contacts/people/984
                    showContactAsBarcode(intent.data)
            }
        }
    }

    private fun showTextAsBarcode(text: String?) {
        val intent = Intent(Intents.Encode.ACTION)
        intent.putExtra(Intents.Encode.TYPE, Contents.Type.TEXT)
        intent.putExtra(Intents.Encode.DATA, text)
        intent.putExtra(Intents.Encode.FORMAT, Contents.Format.QR_CODE)
        startActivity(intent)
    }

    /**
     * Takes a contact Uri and does the necessary database lookups to retrieve that person's info,
     * then sends an Encode intent to render it as a QR Code.
     *
     * @param contactUri A Uri of the form content://contacts/people/17
     */
    private fun showContactAsBarcode(contactUri: Uri?) {
        val resolver = contentResolver
        val contactCursor = resolver.query(contactUri!!, null, null, null, null)
        val bundle = Bundle()
        if (contactCursor != null && contactCursor.moveToFirst()) {
            val nameColumn = contactCursor.getColumnIndex(Contacts.PeopleColumns.NAME)
            val name = contactCursor.getString(nameColumn)

            // Don't require a name to be present, this contact might be just a phone number.
            if (name != null && name.length > 0) {
                bundle.putString(ContactsContract.Intents.Insert.NAME, massageContactData(name))
            }
            contactCursor.close()

            val phonesUri = Uri.withAppendedPath(contactUri, Contacts.People.Phones.CONTENT_DIRECTORY)
            val phonesCursor = resolver.query(phonesUri, PHONES_PROJECTION, null, null, null)
            if (phonesCursor != null) {
                var foundPhone = 0
                while (phonesCursor.moveToNext()) {
                    val number = phonesCursor.getString(PHONES_NUMBER_COLUMN)
                    if (foundPhone < Contents.PHONE_KEYS.size) {
                        bundle.putString(Contents.PHONE_KEYS[foundPhone], massageContactData(number))
                        foundPhone++
                    }
                }
                phonesCursor.close()
            }

            val methodsUri = Uri.withAppendedPath(
                contactUri,
                Contacts.People.ContactMethods.CONTENT_DIRECTORY
            )
            val methodsCursor = resolver.query(methodsUri, METHODS_PROJECTION, null, null, null)
            if (methodsCursor != null) {
                var foundEmail = 0
                var foundPostal = false
                while (methodsCursor.moveToNext()) {
                    val kind = methodsCursor.getInt(METHODS_KIND_COLUMN)
                    val data = methodsCursor.getString(METHODS_DATA_COLUMN)
                    when (kind) {
                        Contacts.KIND_EMAIL -> if (foundEmail < Contents.EMAIL_KEYS.size) {
                            bundle.putString(Contents.EMAIL_KEYS[foundEmail], massageContactData(data))
                            foundEmail++
                        }

                        Contacts.KIND_POSTAL -> if (!foundPostal) {
                            bundle.putString(ContactsContract.Intents.Insert.POSTAL, massageContactData(data))
                            foundPostal = true
                        }
                    }
                }
                methodsCursor.close()
            }

            val intent = Intent(Intents.Encode.ACTION)
            intent.putExtra(Intents.Encode.TYPE, Contents.Type.CONTACT)
            intent.putExtra(Intents.Encode.DATA, bundle)
            intent.putExtra(Intents.Encode.FORMAT, Contents.Format.QR_CODE)

            startActivity(intent)
        }
    }

    companion object {
        private const val PICK_BOOKMARK = 0
        private const val PICK_CONTACT = 1

        //private static final int METHODS_ID_COLUMN = 0;
        private const val METHODS_KIND_COLUMN = 1
        private const val METHODS_DATA_COLUMN = 2

        private val METHODS_PROJECTION = arrayOf(
            BaseColumns._ID, // 0
            Contacts.ContactMethodsColumns.KIND, // 1
            Contacts.ContactMethodsColumns.DATA // 2
        )

        private const val PHONES_NUMBER_COLUMN = 1

        private val PHONES_PROJECTION = arrayOf(
            BaseColumns._ID, // 0
            Contacts.PhonesColumns.NUMBER // 1
        )

        private fun massageContactData(data: String): String {
            // For now -- make sure we don't put newlines in shared contact data. It messes up
            // any known encoding of contact data. Replace with space.
            var data = data
            if (data.indexOf('\n') >= 0) {
                data = data.replace("\n", " ")
            }
            if (data.indexOf('\r') >= 0) {
                data = data.replace("\r", " ")
            }
            return data
        }
    }
}
