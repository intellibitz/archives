package intellibitz.intellidroid.service

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.util.Log
import android.util.SparseArray
import android.util.SparseIntArray
import intellibitz.intellidroid.content.DeviceContactContentProvider
import intellibitz.intellidroid.data.ContactItem
import java.util.HashSet

class ContactFetch(private val context: Context) {
    companion object {
        @JvmField
        val PHOTO_THUMBNAIL_URI: String =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                ContactsContract.Contacts.PHOTO_THUMBNAIL_URI
            else ContactsContract.Contacts.PHOTO_ID

        @JvmField
        val PHOTO_URI: String =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                ContactsContract.CommonDataKinds.Phone.PHOTO_URI
            else ContactsContract.Contacts.PHOTO_ID

        @JvmField
        val PHOTO_FILE_ID: String =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                ContactsContract.CommonDataKinds.Phone.PHOTO_FILE_ID
            else ContactsContract.Contacts.PHOTO_ID
    }

    @JvmField
    var TAG: String = "ContactFetch"
    @JvmField
    var CONTACT_ID_URI: String = ContactsContract.Contacts._ID
    @JvmField
    var DATA_CONTACT_ID_URI: String = ContactsContract.Data.CONTACT_ID
    @JvmField
    var DATA_VERSION: String = ContactsContract.Data.DATA_VERSION
    @JvmField
    var MIMETYPE_URI: String = ContactsContract.Data.MIMETYPE
    @JvmField
    var EMAIL_URI: String = ContactsContract.CommonDataKinds.Email.DATA
    @JvmField
    var PHONE_URI: String = ContactsContract.CommonDataKinds.Phone.DATA
    @JvmField
    var FAMILY_URI: String = ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME
    @JvmField
    var GIVEN_NAME_URI: String = ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME
    @JvmField
    var DISPLAY_NAME_URI: String = ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME
    @JvmField
    var DISPLAY_NAME_PRIMARY: String =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            ContactsContract.Data.DISPLAY_NAME_PRIMARY
        else ContactsContract.Data.DISPLAY_NAME
    @JvmField
    var FAMILY_TYPE: String = ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
    @JvmField
    var MAIL_TYPE: String = ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE
    @JvmField
    var PHONE_TYPE: String = ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE

    fun getContactCursor(stringQuery: String?, sortOrder: String?): Cursor? {
        val contentUri: Uri = if (stringQuery == null)
            ContactsContract.Contacts.CONTENT_URI
        else
            Uri.withAppendedPath(
                ContactsContract.Contacts.CONTENT_FILTER_URI,
                Uri.encode(stringQuery)
            )

        val projection = arrayOf(
            CONTACT_ID_URI,
            DISPLAY_NAME_PRIMARY,
            PHOTO_THUMBNAIL_URI
        )

        val selection = "$DISPLAY_NAME_PRIMARY NOT LIKE ?"
        val selectionArgs = arrayOf("%@%")

        return context.contentResolver.query(
            contentUri, projection, selection, selectionArgs, sortOrder
        )
    }

    fun getContactDetailsCursor(): Cursor? {
        val projection = arrayOf(
            DATA_CONTACT_ID_URI,
            MIMETYPE_URI,
            EMAIL_URI,
            PHONE_URI,
            GIVEN_NAME_URI,
            FAMILY_URI,
            DISPLAY_NAME_URI,
            DATA_VERSION
        )

        val selection = "$DISPLAY_NAME_PRIMARY NOT LIKE ? AND ($MIMETYPE_URI=?  OR $MIMETYPE_URI=?  OR $MIMETYPE_URI=? )"

        val selectionArgs = arrayOf(
            "%@%",
            ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE,
            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
            ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
        )

        return context.contentResolver.query(
            ContactsContract.Data.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null
        )
    }

    fun getDetailedContactList(queryString: String?): SparseArray<ContactItem> {
        val contactCursor = getContactCursor(queryString, DISPLAY_NAME_PRIMARY)
            ?: return SparseArray()
        if (contactCursor.count == 0) {
            contactCursor.close()
            return SparseArray()
        }

        val contactIds = HashSet<Int>()
        if (contactCursor.moveToFirst()) {
            do {
                contactIds.add(
                    contactCursor.getInt(
                        contactCursor.getColumnIndex(CONTACT_ID_URI)
                    )
                )
            } while (contactCursor.moveToNext())
        }

        val nameMap = SparseArray<String>()
        val pictureMap = SparseArray<String>()

        val idIdx = contactCursor.getColumnIndex(CONTACT_ID_URI)
        val nameIdx = contactCursor.getColumnIndex(DISPLAY_NAME_PRIMARY)
        val pictureIdx = contactCursor.getColumnIndex(PHOTO_THUMBNAIL_URI)

        if (contactCursor.moveToFirst()) {
            do {
                nameMap.put(contactCursor.getInt(idIdx), contactCursor.getString(nameIdx))
                pictureMap.put(contactCursor.getInt(idIdx), contactCursor.getString(pictureIdx))
            } while (contactCursor.moveToNext())
        }

        val detailsCursor = getContactDetailsCursor() ?: return SparseArray()
        val emailMap = SparseArray<HashSet<String>>()
        val phoneMap = SparseArray<HashSet<String>>()
        val familyMap = SparseArray<String>()
        val givenMap = SparseArray<String>()
        val displayMap = SparseArray<String>()
        val versionMap = SparseIntArray()

        val detIdIdx = detailsCursor.getColumnIndex(DATA_CONTACT_ID_URI)
        val mimeIdx = detailsCursor.getColumnIndex(MIMETYPE_URI)
        val mailIdx = detailsCursor.getColumnIndex(EMAIL_URI)
        val phoneIdx = detailsCursor.getColumnIndex(PHONE_URI)
        val familyIdx = detailsCursor.getColumnIndex(FAMILY_URI)
        val givenIdx = detailsCursor.getColumnIndex(GIVEN_NAME_URI)
        val displayIdx = detailsCursor.getColumnIndex(DISPLAY_NAME_URI)
        val versionIdx = detailsCursor.getColumnIndex(DATA_VERSION)

        var mailString: String
        var phoneString: String

        if (detailsCursor.moveToFirst()) {
            do {
                val contactIdIndex = detailsCursor.getInt(detIdIdx)
                if (!contactIds.contains(contactIdIndex)) {
                    Log.e(TAG, "Skipping - " + nameMap.get(contactIdIndex))
                    continue
                }

                val version = detailsCursor.getInt(versionIdx)
                val valVersion = versionMap.get(contactIdIndex)
                versionMap.put(contactIdIndex, version + valVersion)

                val mime = detailsCursor.getString(mimeIdx)
                if (MAIL_TYPE == mime) {
                    mailString = detailsCursor.getString(mailIdx)
                    var mailz = emailMap.get(contactIdIndex)
                    if (null == mailz) {
                        mailz = HashSet()
                        emailMap.put(contactIdIndex, mailz)
                    }
                    mailz.add(mailString)
                } else if (FAMILY_TYPE == mime) {
                    givenMap.put(contactIdIndex, detailsCursor.getString(givenIdx))
                    familyMap.put(contactIdIndex, detailsCursor.getString(familyIdx))
                    displayMap.put(contactIdIndex, detailsCursor.getString(displayIdx))
                } else if (PHONE_TYPE == mime) {
                    phoneString = detailsCursor.getString(phoneIdx)
                    var phonez = phoneMap.get(contactIdIndex)
                    if (null == phonez) {
                        phonez = HashSet()
                        phoneMap.put(contactIdIndex, phonez)
                    }
                    phonez.add(phoneString)
                }
            } while (detailsCursor.moveToNext())
        }

        contactCursor.close()
        detailsCursor.close()

        val contacts = SparseArray<ContactItem>()
        for (contactId in contactIds) {
            val ver = versionMap.get(contactId)
            val dataId = contactId.toString()
            val name = nameMap.get(contactId)
            val firstName = givenMap.get(contactId)
            val lastName = familyMap.get(contactId)
            val displayName = displayMap.get(contactId)
            val profilePic = pictureMap.get(contactId)
            val mobiles = phoneMap.get(contactId)
            val emails = emailMap.get(contactId)

            val deviceContactItem = DeviceContactContentProvider.createsDeviceContactItem(
                contactId, ver, dataId, name, firstName, lastName, displayName, profilePic, mobiles, emails
            )
            contacts.put(contactId, deviceContactItem)
        }
        return contacts
    }
}
