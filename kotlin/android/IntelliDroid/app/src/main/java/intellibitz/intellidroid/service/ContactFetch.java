package intellibitz.intellidroid.service;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;

import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.content.DeviceContactContentProvider;
import intellibitz.intellidroid.data.ContactItem;

import java.util.HashSet;

/**
 */
public class ContactFetch {
    public static String PHOTO_THUMBNAIL_URI = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) ?
            ContactsContract.Contacts.PHOTO_THUMBNAIL_URI : ContactsContract.Contacts.PHOTO_ID;
    public static String PHOTO_URI = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) ?
            ContactsContract.CommonDataKinds.Phone.PHOTO_URI : ContactsContract.Contacts.PHOTO_ID;
    public static String PHOTO_FILE_ID = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) ?
            ContactsContract.CommonDataKinds.Phone.PHOTO_FILE_ID : ContactsContract.Contacts.PHOTO_ID;
    private final Context context;
    public String TAG = "ContactFetch";
    public String CONTACT_ID_URI = ContactsContract.Contacts._ID;
    public String DATA_CONTACT_ID_URI = ContactsContract.Data.CONTACT_ID;
    public String DATA_VERSION = ContactsContract.Data.DATA_VERSION;
    public String MIMETYPE_URI = ContactsContract.Data.MIMETYPE;
    public String EMAIL_URI = ContactsContract.CommonDataKinds.Email.DATA;
    public String PHONE_URI = ContactsContract.CommonDataKinds.Phone.DATA;
    public String FAMILY_URI = ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME;
    public String GIVEN_NAME_URI = ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME;
    public String DISPLAY_NAME_URI = ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME;
    public String DISPLAY_NAME_PRIMARY = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) ?
            ContactsContract.Data.DISPLAY_NAME_PRIMARY : ContactsContract.Data.DISPLAY_NAME;
    public String FAMILY_TYPE = ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE;
    public String MAIL_TYPE = ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE;
    public String PHONE_TYPE = ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE;

    public ContactFetch(Context context) {
        this.context = context;
    }

    public Cursor getContactCursor(String stringQuery, String sortOrder) {

//            Log.i(TAG, "+++++++++++++++++++++++++++++++++++++++++++++++++++");
//            Log.e(TAG, "ContactCursor search has started...");

//        Long t0 = System.currentTimeMillis();

        Uri CONTENT_URI;

        if (stringQuery == null)
            CONTENT_URI = ContactsContract.Contacts.CONTENT_URI;
        else
            CONTENT_URI = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_FILTER_URI,
                    Uri.encode(stringQuery));

        String[] PROJECTION = new String[]{
                CONTACT_ID_URI,
                DISPLAY_NAME_PRIMARY,
                PHOTO_THUMBNAIL_URI
//                ,DATA_VERSION
        };

        String SELECTION = DISPLAY_NAME_PRIMARY + " NOT LIKE ?";
        String[] SELECTION_ARGS = new String[]{"%" + "@" + "%"};

        Cursor cursor = context.getContentResolver().query(
                CONTENT_URI, PROJECTION, SELECTION, SELECTION_ARGS, sortOrder);

//            Long t1 = System.currentTimeMillis();

//            Log.e(TAG, "ContactCursor finished in " + (t1 - t0) / 1000 + " secs");
//            if (null != cursor)
//                Log.e(TAG, "ContactCursor found " + cursor.getCount() + " contacts");
//            Log.i(TAG, "+++++++++++++++++++++++++++++++++++++++++++++++++++");

        return cursor;
    }

    public Cursor getContactDetailsCursor() {

//            Log.i(TAG, "+++++++++++++++++++++++++++++++++++++++++++++++++++");
//            Log.e(TAG, "ContactDetailsCursor search has started...");

//        Long t0 = System.currentTimeMillis();

        String[] PROJECTION = new String[]{
                DATA_CONTACT_ID_URI,
                MIMETYPE_URI,
                EMAIL_URI,
                PHONE_URI,
                GIVEN_NAME_URI,
                FAMILY_URI,
                DISPLAY_NAME_URI,
                DATA_VERSION
        };

        String SELECTION = DISPLAY_NAME_PRIMARY + " NOT LIKE ?" +
                " AND " +
                "(" + MIMETYPE_URI + "=? " +
                " OR " + MIMETYPE_URI + "=? " +
                " OR " + MIMETYPE_URI + "=? " + ")";

        String[] SELECTION_ARGS = new String[]{
                "%" + "@" + "%",
                ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE,
                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
                ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
        };

        Cursor cursor = context.getContentResolver().query(
                ContactsContract.Data.CONTENT_URI,
                PROJECTION,
                SELECTION,
                SELECTION_ARGS,
                null);

/*
        Long t1 = System.currentTimeMillis();

        Log.e(TAG, "ContactDetailsCursor finished in " + (t1 - t0) / 1000 + " secs");
        if (null != cursor)
            Log.e(TAG, "ContactDetailsCursor found " + cursor.getCount() + " contacts");
        Log.i(TAG, "+++++++++++++++++++++++++++++++++++++++++++++++++++");
*/

        return cursor;
    }

    public SparseArray<ContactItem> getDetailedContactList(String queryString) {

        /**
         * First we fetch the contacts name and picture uri in alphabetical order for
         * display purpose and store these data in HashMap.
         */

        Cursor contactCursor = getContactCursor(queryString, DISPLAY_NAME_PRIMARY);
        if (contactCursor.getCount() == 0) {
            return new SparseArray<>();
        }

        HashSet<Integer> contactIds = new HashSet<>();

        if (contactCursor.moveToFirst()) {
            do {
                contactIds.add(contactCursor.getInt(
                        contactCursor.getColumnIndex(CONTACT_ID_URI)));
            } while (contactCursor.moveToNext());
        }

        SparseArray<String> nameMap = new SparseArray<>();
        SparseArray<String> pictureMap = new SparseArray<>();

        int idIdx = contactCursor.getColumnIndex(CONTACT_ID_URI);

        int nameIdx = contactCursor.getColumnIndex(DISPLAY_NAME_PRIMARY);
        int pictureIdx = contactCursor.getColumnIndex(PHOTO_THUMBNAIL_URI);

        if (contactCursor.moveToFirst()) {
            do {
                nameMap.put(contactCursor.getInt(idIdx), contactCursor.getString(nameIdx));
                pictureMap.put(contactCursor.getInt(idIdx), contactCursor.getString(pictureIdx));
//                versionMap.put(contactCursor.getInt(idIdx), contactCursor.getInt(versionIdx));
            } while (contactCursor.moveToNext());
        }

        /**
         * Then we get the remaining contact information. Here email and phone
         */

/*
        String whereName = ContactsContract.Data.MIMETYPE + " = ? AND " + ContactsContract.CommonDataKinds.StructuredName.CONTACT_ID + " = ?";
String[] whereNameParams = new String[] { ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE, contact_ID };
Cursor nameCur = contentResolver.query(ContactsContract.Data.CONTENT_URI, null, whereName, whereNameParams, ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME);
while (nameCur.moveToNext()) {
String given = nameCur.getString(nameCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME));
String family = nameCur.getString(nameCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME));
String display = nameCur.getString(nameCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME));
}
nameCur.close();
         */
        Cursor detailsCursor = getContactDetailsCursor();
//        // TODO: 30-06-2016
        SparseArray<HashSet<String>> emailMap = new SparseArray<>();
        SparseArray<HashSet<String>> phoneMap = new SparseArray<>();
        SparseArray<String> familyMap = new SparseArray<>();
        SparseArray<String> givenMap = new SparseArray<>();
        SparseArray<String> displayMap = new SparseArray<>();
        SparseIntArray versionMap = new SparseIntArray();

        idIdx = detailsCursor.getColumnIndex(DATA_CONTACT_ID_URI);
        int mimeIdx = detailsCursor.getColumnIndex(MIMETYPE_URI);
        int mailIdx = detailsCursor.getColumnIndex(EMAIL_URI);
        int phoneIdx = detailsCursor.getColumnIndex(PHONE_URI);
        int familyIdx = detailsCursor.getColumnIndex(FAMILY_URI);
        int givenIdx = detailsCursor.getColumnIndex(GIVEN_NAME_URI);
        int displayIdx = detailsCursor.getColumnIndex(DISPLAY_NAME_URI);
        int versionIdx = detailsCursor.getColumnIndex(DATA_VERSION);

        String mailString;
        String phoneString;

        if (detailsCursor.moveToFirst()) {
            do {

                /**
                 * We forget all details which are not correlated with the contact list
                 */

                int contactIdIndex = detailsCursor.getInt(idIdx);
                if (!contactIds.contains(contactIdIndex)) {
                    Log.e(TAG, "Skipping - " + nameMap.get(contactIdIndex));
                    continue;
                }

                int version = detailsCursor.getInt(versionIdx);
                Integer val = versionMap.get(contactIdIndex);
                //                stores the consolidated version
                versionMap.put(contactIdIndex, version + val);

                if (detailsCursor.getString(mimeIdx).equals(MAIL_TYPE)) {
                    mailString = detailsCursor.getString(mailIdx);
                    HashSet<String> mailz = emailMap.get(contactIdIndex);
                    if (null == mailz) {
                        mailz = new HashSet<>();
                        emailMap.put(contactIdIndex, mailz);
                    }
//                    EmailItem emailItem = new EmailItem(mailString, mailString, mailString, mailString);
//                    // TODO: 30-06-2016  
//                    emailItem.setDevice(true);
                    mailz.add(mailString);

                    /**
                     * We remove all double contact having the same email address
                     */

/*
                    if(!emailMap.containsValue(mailString.toLowerCase()))
                        emailMap.put(detailsCursor.getInt(idIdx), mailString.toLowerCase());
*/

                } else if (detailsCursor.getString(mimeIdx).equals(FAMILY_TYPE)) {
                    givenMap.put(contactIdIndex, detailsCursor.getString(givenIdx));
                    familyMap.put(contactIdIndex, detailsCursor.getString(familyIdx));
                    displayMap.put(contactIdIndex, detailsCursor.getString(displayIdx));
                } else if (detailsCursor.getString(mimeIdx).equals(PHONE_TYPE)) {
                    phoneString = detailsCursor.getString(phoneIdx);
//                    // TODO: 30-06-2016
                    HashSet<String> phonez = phoneMap.get(contactIdIndex);
                    if (null == phonez) {
                        phonez = new HashSet<>();
                        phoneMap.put(contactIdIndex, phonez);
                    }
//                    MobileItem mobileItem = new MobileItem(phoneString, phoneString, phoneString, phoneString);
//                    mobileItem.setDevice(true);
                    phonez.add(phoneString);
//                        phoneMap.put(detailsCursor.getInt(idIdx), phoneString);
                }
            } while (detailsCursor.moveToNext());
        }

        contactCursor.close();
        detailsCursor.close();

        /**
         * Finally the contact list is build up
         */
        SparseArray<ContactItem> contacts = new SparseArray<>();
//        Set<Integer> detailsKeySet = emailMap.keySet();
        for (Integer contactId : contactIds) {
            int ver = versionMap.get(contactId);
            String dataId = String.valueOf(contactId);
            String name = nameMap.get(contactId);
            String firstName = givenMap.get(contactId);
            String lastName = familyMap.get(contactId);
            String displayName = displayMap.get(contactId);
            String profilePic = pictureMap.get(contactId);
            HashSet<String> mobiles = phoneMap.get(contactId);
            HashSet<String> emails = emailMap.get(contactId);
/*
            if (!detailsKeySet.contains(contactId) ||
                    (emailMap.get(contactId) == null &&
                            phoneMap.get(contactId) == null) &&
                            familyMap.get(contactId) == null)
                continue;
*/
            ContactItem deviceContactItem = DeviceContactContentProvider.createsDeviceContactItem(
                    contactId, ver, dataId, name, firstName, lastName, displayName, profilePic, mobiles, emails);
            contacts.put(contactId, deviceContactItem);
        }
        return contacts;
    }

}
