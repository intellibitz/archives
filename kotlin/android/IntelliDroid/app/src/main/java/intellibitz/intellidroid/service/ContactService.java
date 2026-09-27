package intellibitz.intellidroid.service;

import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcelable;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;

import intellibitz.intellidroid.IntellibitzPermissionFragment;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.db.ContactItemColumns;
import intellibitz.intellidroid.db.DatabaseHelper;
import intellibitz.intellidroid.task.BulkUploadContactsTask;
import intellibitz.intellidroid.task.ContactsFetchTask;
import intellibitz.intellidroid.task.ContactsSaveToDBTask;
import intellibitz.intellidroid.task.GetBroadcastListTask;
import intellibitz.intellidroid.task.GetGroupsTask;
import intellibitz.intellidroid.task.GetWorkContactsTask;
import intellibitz.intellidroid.task.GroupDetailsTask;
import intellibitz.intellidroid.task.GroupsAddUsersTask;
import intellibitz.intellidroid.task.WorkContactsSaveToDBTask;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.IntellibitzPermissionFragment;
import intellibitz.intellidroid.content.DeviceContactContentProvider;
import intellibitz.intellidroid.content.UserContentProvider;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.db.ContactItemColumns;
import intellibitz.intellidroid.db.DatabaseHelper;
import intellibitz.intellidroid.task.BulkUploadContactsTask;
import intellibitz.intellidroid.task.ContactsFetchTask;
import intellibitz.intellidroid.task.ContactsSaveToDBTask;
import intellibitz.intellidroid.task.GetBroadcastListTask;
import intellibitz.intellidroid.task.GetGroupsTask;
import intellibitz.intellidroid.task.GetWorkContactsTask;
import intellibitz.intellidroid.task.GroupDetailsTask;
import intellibitz.intellidroid.task.GroupsAddUsersTask;
import intellibitz.intellidroid.task.WorkContactsSaveToDBTask;
import intellibitz.intellidroid.util.MainApplicationSingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import androidx.annotation.WorkerThread;

//import intellibitz.intellidroid.content.BroadcastContactsContentProvider;
//import intellibitz.intellidroid.content.MsgChatGrpContactsContentProvider;
//import intellibitz.intellidroid.content.task.BroadcastSaveToDBTask;
//import intellibitz.intellidroid.content.task.GroupsSaveToDBTask;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class ContactService extends
        Service implements
        BulkUploadContactsTask.DeviceContactsUploadTaskListener,
        ContactsFetchTask.ContactsFetchTaskListener,
        ContactsSaveToDBTask.ContactsSaveToDBTaskListener,
        GetGroupsTask.GetGroupsTaskListener,
        GroupDetailsTask.GroupDetailsTaskListener,
        GroupsAddUsersTask.GroupsAddUsersTaskListener,
        GetBroadcastListTask.GetBroadcastTaskListener,
        GetWorkContactsTask.GetWorkContactsTaskListener,
        WorkContactsSaveToDBTask.WorkContactsSaveToDBTaskListener {
    /**
     * Command to the service to register a client, receiving callbacks
     * from the service.  The Message's replyTo field must be a Messenger of
     * the client where callbacks should be sent.
     */
    public static final int MSG_REGISTER_CLIENT = 1;
    /**
     * Command to the service to unregister a client, ot stop receiving callbacks
     * from the service.  The Message's replyTo field must be a Messenger of
     * the client as previously given with MSG_REGISTER_CLIENT.
     */
    public static final int MSG_UNREGISTER_CLIENT = 2;
    /**
     * Command to service to set a new value.  This can be sent to the
     * service to supply a new value, and will be sent by the service to
     * any registered clients with the new value.
     */
    public static final int MSG_SET_VALUE = 3;
    //    on key messages
    public static final int MSG_ON_KEY = 4;
    //    on show typing from cloud
    public static final int MSG_SHOW_TYPING = 5;
    public static final String ACTION_UPDATE_CONTACT_DBEMPTY_URL =
            "intellibitz.intellidroid.service.action.ACTION_UPDATE_CONTACT_DBEMPTY_URL";
    public static final String ACTION_UPDATE_CONTACT_BYCOUNT_URL =
            "intellibitz.intellidroid.service.action.ACTION_UPDATE_CONTACT_BYCOUNT_URL";
    public static final String ACTION_UPDATE_CONTACT_BYVERSION_URL =
            "intellibitz.intellidroid.service.action.ACTION_UPDATE_CONTACT_BYVERSION_URL";
    public static final String ACTION_UPDATE_CONTACT_URL =
            "intellibitz.intellidroid.service.action.ACTION_UPDATE_CONTACT_URL";
    public static final String ACTION_UPDATE_WORK_CONTACT_URL =
            "intellibitz.intellidroid.service.action.ACTION_UPDATE_WORK_CONTACT_URL";
    public static final String ACTION_GET_GROUPS_URL =
            "intellibitz.intellidroid.service.action.ACTION_GET_GROUPS_URL";
    public static final String ACTION_GET_BROADCAST_URL =
            "intellibitz.intellidroid.service.action.ACTION_GET_BROADCAST_URL";
    public static final String ACTION_GET_GROUPS_DETAILS_URL =
            "intellibitz.intellidroid.service.action.ACTION_GET_GROUPS_DETAILS_URL";
    private static final String TAG = "ContactService";
    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(new IncomingHandler());
    private final Object lock = new Object();
    /**
     * Keeps track of all current registered clients.
     */
    ArrayList<Messenger> mClients = new ArrayList<>();
    /**
     * Holds last value set by a client.
     */
    int mValue = 0;
    private ContentObserver contentObserver;
    private boolean reconnect = false;
    private String ref;
    private ContactItem user;
    private BulkUploadContactsTask bulkUploadContactsTask;
    private GetGroupsTask getGroupsTask;
    //    private GroupsSaveToDBTask groupsSaveToDBTask;
    private GroupDetailsTask groupDetailsTask;
    private GroupsAddUsersTask groupsAddUsersTask;
    private GetBroadcastListTask getBroadcastListTask;
    //    private BroadcastSaveToDBTask broadcastSaveToDBTask;
    // Unique Identification Number for the Notification.
    // We use it on Notification start, and to cancel it.
    private int NOTIFICATION = 100;
    private volatile Looper mServiceLooper;
    private volatile ServiceHandler mServiceHandler;
    private String mName;
    private boolean mRedelivery;

    public ContactService() {
        super();
    }

    // Binder given to clients
//    private final IBinder mBinder = new LocalBinder();

    public ContactService(String name) {
        super();
        mName = name;
    }

    public static void getGroups(ContactItem user, Context context) {
        try {
//        intent service runs async in its own thread
            Intent intent = new Intent(context, ContactService.class);
            intent.setAction(ACTION_GET_GROUPS_URL);
            intent.putExtra(ContactItem.USER_CONTACT,
                    (Parcelable) UserContentProvider.getUserCloneForService(user));
            context.startService(intent);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
    }

    public static void getBroadcast(ContactItem user, Context context) {
        try {
//        intent service runs async in its own thread
            Intent intent = new Intent(context, ContactService.class);
            intent.setAction(ACTION_GET_BROADCAST_URL);
            intent.putExtra(ContactItem.USER_CONTACT,
                    (Parcelable) UserContentProvider.getUserCloneForService(user));
            context.startService(intent);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
    }

    public static void getGroupDetails(ContactItem contactItem, ContactItem user, Context context) {
        try {
//        intent service runs async in its own thread
            Intent intent = new Intent(context, ContactService.class);
            intent.setAction(ACTION_GET_GROUPS_DETAILS_URL);
            intent.putExtra(ContactItem.TAG, (Parcelable) contactItem);
            intent.putExtra(ContactItem.USER_CONTACT,
                    (Parcelable) UserContentProvider.getUserCloneForService(user));
            context.startService(intent);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
    }

    public static void asyncUpdateContacts(ContactItem user, Context context) {
        try {
//        intent service runs async in its own thread
            Intent intent = new Intent(context, ContactService.class);
            intent.setAction(ACTION_UPDATE_CONTACT_URL);
            intent.putExtra(ContactItem.USER_CONTACT,
                    (Parcelable) UserContentProvider.getUserCloneForService(user));
            context.startService(intent);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
    }

    public static void asyncUpdateWorkContacts(ContactItem user, Context context) {
        try {
//        intent service runs async in its own thread
            Intent intent = new Intent(context, ContactService.class);
            intent.setAction(ACTION_UPDATE_WORK_CONTACT_URL);
            intent.putExtra(ContactItem.USER_CONTACT,
                    (Parcelable) UserContentProvider.getUserCloneForService(user));
            context.startService(intent);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
    }

    public static void asyncUpdateContactsByCount(ContactItem user, Context context) {
        try {
//        intent service runs async in its own thread
            Intent intent = new Intent(context, ContactService.class);
            intent.setAction(ACTION_UPDATE_CONTACT_BYCOUNT_URL);
            intent.putExtra(ContactItem.USER_CONTACT,
                    (Parcelable) UserContentProvider.getUserCloneForService(user));
            context.startService(intent);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
    }

    public static void asyncUpdateContactsIfDBEmpty(ContactItem user, Context context) {
        try {
//        intent service runs async in its own thread
            Intent intent = new Intent(context, ContactService.class);
            intent.setAction(ACTION_UPDATE_CONTACT_DBEMPTY_URL);
            intent.putExtra(ContactItem.USER_CONTACT,
                    (Parcelable) UserContentProvider.getUserCloneForService(user));
            context.startService(intent);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
    }

    public static void asyncUpdateContactsByVersion(ContactItem user, Context context) {
        try {
//        intent service runs async in its own thread
            Intent intent = new Intent(context, ContactService.class);
            intent.setAction(ACTION_UPDATE_CONTACT_BYVERSION_URL);
            intent.putExtra(ContactItem.USER_CONTACT,
                    (Parcelable) UserContentProvider.getUserCloneForService(user));
            context.startService(intent);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
    }

    public static void startContactService(ContactItem user, Context context) {
        Intent intent = new Intent(context, ContactService.class);
        intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
        context.startService(intent);
    }

    public static void stopContactService(Context context) {
        // Start IntentService to register this application with GCM.
        Intent intent = new Intent(context, ContactService.class);
        context.stopService(intent);
    }

    private void sendMessageToClients() {
        sendMessageToClients(MSG_SET_VALUE, null);
    }

    private void sendMessageToClients(int msg, Object object) {
        for (int i = mClients.size() - 1; i >= 0; i--) {
            try {
                Message message = Message.obtain(null, msg, mValue, 0);
                message.obj = object;
                mClients.get(i).send(message);
            } catch (RemoteException e) {
                // The client is dead.  Remove it from the list;
                // we are going through the list from back to front
                // so this is safe to do inside the loop.
                mClients.remove(i);
            }
        }
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
/*
    public class LocalBinder extends Binder {
        ChatService getService() {
            // Return this instance of LocalService so clients can call public methods
            return ChatService.this;
        }
    }
*/
    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    /**
     * Sets intent redelivery preferences.  Usually called from the constructor
     * with your preferred semantics.
     * <p/>
     * <p>If enabled is true,
     * {@link #onStartCommand(Intent, int, int)} will return
     * {@link Service#START_REDELIVER_INTENT}, so if this process dies before
     * {@link #onHandleIntent(Intent)} returns, the process will be restarted
     * and the intent redelivered.  If multiple Intents have been sent, only
     * the most recent one is guaranteed to be redelivered.
     * <p/>
     * <p>If enabled is false (the default),
     * {@link #onStartCommand(Intent, int, int)} will return
     * {@link Service#START_NOT_STICKY}, and if the process dies, the Intent
     * dies along with it.
     */
    public void setIntentRedelivery(boolean enabled) {
        mRedelivery = enabled;
    }

    @Override
    public void onDestroy() {
        // TODO: 16-03-2016
//        quit looper as soon as the handle event is over.. in the handler
        mServiceLooper.quit();
        getContentResolver().unregisterContentObserver(contentObserver);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (null != intent) {
            user = intent.getParcelableExtra(ContactItem.USER_CONTACT);

            // TODO: It would be nice to have an option to hold a partial wakelock
            // during processing, and to have a static startService(Context, Intent)
            // method that would launch the service & hand off a wakelock.

            HandlerThread thread = new HandlerThread("IntentService[" + mName + "]");
            thread.start();

            mServiceLooper = thread.getLooper();
            mServiceHandler = new ServiceHandler(mServiceLooper);
            Message msg = mServiceHandler.obtainMessage();
            msg.arg1 = startId;
            msg.obj = intent;
            mServiceHandler.sendMessage(msg);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        contentObserver = new ContentObserver(new Handler()) {
            @Override
            public boolean deliverSelfNotifications() {
                return super.deliverSelfNotifications();
            }

            @Override
            public void onChange(boolean selfChange) {
                onChange(selfChange, null);
            }

            @Override
            public void onChange(boolean selfChange, Uri uri) {
                if (null == user) {
                    Log.e(TAG, "Contacts Content Observer: User NULL - ASYNC updates cannot start");
                } else {
                    Log.e(TAG, "Contacts Content Observer: ASYNC Updates of Contacts BEGIN");
//                    asyncUpdateContactsByVersion(user, getApplicationContext());
                    asyncUpdateContactsByCount(user, getApplicationContext());
                }
                //                does the updates here
/*
                Log.e("ContactChangeObserver", "onChange");

                // 0 Update , 1 Delete , 2 Added
                // Get count from phone contacts
                final int currentCount = contactDBOperaion.getContactsCountFromPhone();

                // Get count from your sqlite database
                int mContactCount= DbContacts.getInstance().getContactsCount();

                if (currentCount < mContactCount) {
                    // DELETE HAPPEN.
                    Log.e("Status", "Deletion");
                    contactDBOperaion.SyncContacts(1);
                } else if (currentCount == mContactCount) {
                    // UPDATE HAPPEN.
                    contactDBOperaion.SyncContacts(0);
                } else {
                    // INSERT HAPPEN.
                    Log.e("Status", "Insertion");
                    contactDBOperaion.SyncContacts(2);
                }
*/
            }
        };
        getContentResolver().registerContentObserver(
                ContactsContract.Contacts.CONTENT_URI, false,
                contentObserver);

    }

    /**
     * This method is invoked on the worker thread with a request to process.
     * Only one Intent is processed at a time, but the processing happens on a
     * worker thread that runs independently from other application logic.
     * So, if this code takes a long time, it will hold up other requests to
     * the same IntentService, but it will not hold up anything else.
     * When all requests have been handled, the IntentService stops itself,
     * so you should not call {@link #stopSelf}.
     *
     * @param intent The value passed to {@link
     *               Context#startService(Intent)}.
     */
    @WorkerThread
    protected void onHandleIntent(Intent intent) {
        synchronized (lock) {
            if (intent != null) {
                final String action = intent.getAction();
                ContactItem user = intent.getParcelableExtra(ContactItem.USER_CONTACT);
                if (ACTION_UPDATE_CONTACT_URL.equals(action)) {
                    updateContacts(user);
                    return;
                }
                if (ACTION_UPDATE_WORK_CONTACT_URL.equals(action)) {
                    execGetWorkContactsTask(user, this);
                    return;
                }
                if (ACTION_UPDATE_CONTACT_BYVERSION_URL.equals(action)) {
                    updateContactsByVersion(user);
                    return;
                }
                if (ACTION_UPDATE_CONTACT_DBEMPTY_URL.equals(action)) {
                    if (DeviceContactContentProvider.isContactsEmptyInDB(this)) {
                        updateContacts(user);
                        return;
                    }
                }
                if (ACTION_UPDATE_CONTACT_BYCOUNT_URL.equals(action)) {
                    updateContactsByCount(user);
                    return;
                }
                if (ACTION_GET_GROUPS_URL.equals(action)) {
                    getGroups(user);
                    return;
                }
                if (ACTION_GET_BROADCAST_URL.equals(action)) {
                    getBroadcast(user);
                    return;
                }
                if (ACTION_GET_GROUPS_DETAILS_URL.equals(action)) {
                    ContactItem contactItem = intent.getParcelableExtra(ContactItem.TAG);
                    getGroupDetails(contactItem, user);
                    return;
                }
            }
        }
    }

    private void updateContacts(ContactItem user) {
        if (IntellibitzPermissionFragment.isReadContactsPermissionGranted(this)) {
            ContactsFetchTask contactsFetchTask = new ContactsFetchTask(user, -1, getApplicationContext());
            contactsFetchTask.setContactsFetchTaskListener(this);
            contactsFetchTask.execute();

        }
    }

    private void updateContactsByCount(ContactItem user) {
        if (IntellibitzPermissionFragment.isReadContactsPermissionGranted(this)) {
            ContactsFetchTask contactsFetchTask = new ContactsFetchTask(user, 0, getApplicationContext());
            contactsFetchTask.setContactsFetchTaskListener(this);
            contactsFetchTask.execute();

        }
    }

    private void updateContactsByVersion(ContactItem user) {
        if (IntellibitzPermissionFragment.isReadContactsPermissionGranted(this)) {
            ContactsFetchTask contactsFetchTask = new ContactsFetchTask(user, -2, getApplicationContext());
            contactsFetchTask.setContactsFetchTaskListener(this);
            contactsFetchTask.execute();

        }
    }

    @Override
    public void onPostContactsFetchExecute(
            ContactItem user, SparseArray<ContactItem> sparseArray, int flag) {
        if (null == sparseArray || 0 == sparseArray.size()) return;
        if (-1 == flag) {
//            the first time.. full upload (only time to do a full save in local db)
            saveDeviceContactsInDB(MainApplicationSingleton.asList(sparseArray), this);
//            uploadContactsToCloud(MainApplicationSingleton.asList(sparseArray), user, this);
            execGetWorkContactsTask(user, this);
            return;
        }
        if (-2 == flag) {
            uploadContactsToCloudIfDBVersionMismatch(sparseArray, user, this);
            execGetWorkContactsTask(user, this);
            return;
        }
        if (0 == flag) {
            uploadContactsToCloudIfCountMismatch(sparseArray, user);
            execGetWorkContactsTask(user, this);
            return;
        }
    }

    @Override
    public void onPostContactsFetchExecuteFail(ContactItem user, int flag) {

    }

/*
    @Override
    public void setContactsFetchTaskToNull() {
        contactsFetchTask = null;
    }
*/

    private void uploadContactsToCloudIfCountMismatch(
            SparseArray<ContactItem> sparseArray, ContactItem user) {
        if (sparseArray != null && sparseArray.size() > 0) {
            int dbcount = DatabaseHelper.fetchRowCount(
                    DeviceContactContentProvider.RAW_CONTENT_URI,
                    DeviceContactContentProvider.TABLE_DEVICECONTACTS,
                    null,
                    this);
//            if device contacts or more than local db, a sync is required
            if (dbcount == sparseArray.size()) {
                Log.e(TAG, "uploadContactsToCloudIfCountMismatch: No Contacts SYNC require : " + dbcount);
            } else if (dbcount < sparseArray.size()) {
//                saves to local db, if the local db count is less than the contacts in the device address book
                saveDeviceContactsInDB(MainApplicationSingleton.asList(sparseArray), this);
//                uploadContactsToCloud(MainApplicationSingleton.asList(sparseArray), user, this);
                execGetWorkContactsTask(user, this);
            } else {
                Log.e(TAG, "uploadContactsToCloudIfCountMismatch: Fetch Device Contacts - SUCCESS - ");
                uploadContactsToCloudIfDBVersionMismatch(sparseArray, user, this);
            }
        }
    }

    private void uploadContactsToCloudIfDBVersionMismatch(SparseArray<ContactItem> sparseArray,
                                                          ContactItem user, Context context) {
        if (sparseArray == null || 0 == sparseArray.size()) {
            Log.e(TAG, " Contacts EMPTY - Cannot upload to cloud");
            return;
        }
        if (DeviceContactContentProvider.isContactsEmptyInDB(this)) {
//            uploadContactsToCloud(MainApplicationSingleton.asList(sparseArray), user, context);
        } else {
            HashSet<ContactItem> deviceContactItemsToUpload = new HashSet<>();
//        // TODO: 07-06-2016
//        fetch contacts from DB and check version
//        upload only mismatch version to Cloud
            Cursor cursor = getContentResolver().query(
//                    to trigger item type.. hack (see contact content provider query methods)
                    Uri.withAppendedPath(DeviceContactContentProvider.CONTENT_URI, "0"),
                    new String[]{ContactItemColumns.KEY_DEVICE_CONTACTID,
                            ContactItemColumns.KEY_VERSION},
                    null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
//                the device contacts.. some device contacts might not have been in DB yet
                SparseIntArray dbContacts = new SparseIntArray();
//                cursor can be reused.. so always move to first
                cursor.moveToFirst();
                do {
                    long id = cursor.getLong(cursor.getColumnIndex(
                            ContactItemColumns.KEY_DEVICE_CONTACTID));
                    int ver = cursor.getInt(cursor.getColumnIndex(
                            ContactItemColumns.KEY_VERSION));
                    dbContacts.put((int) id, ver);
                    ContactItem deviceContactItem = sparseArray.get((int) id);
//                    contact may not exist before - new contact
                    if (deviceContactItem != null && ver < deviceContactItem.getVersion()) {
                        deviceContactItemsToUpload.add(deviceContactItem);
                    }
                } while (cursor.moveToNext());
                cursor.close();
//                upload the remaining device contacts, which are not in db yet..
//                ContactItem[] array = longContactItemMap.values().toArray(new ContactItem[0]);
                for (int i = 0; i < sparseArray.size(); i++) {
                    int key = sparseArray.keyAt(i);
                    // get the object by the key.
                    ContactItem item = sparseArray.get(key);
                    if (0 == dbContacts.get((int) item.getDeviceContactId())) {
                        deviceContactItemsToUpload.add(item);
                    } else {
//                    if (dbContacts.containsKey(item.getDeviceContactId())) {
//                        already in db contacts, so it would be uploaded in the cursor loop
                    }
                }
            }
            if (cursor != null) cursor.close();
//            Log.e(TAG, "PARTIAL contacts upload: " + deviceContactItemsToUpload.size());
//            uploadContactsToCloud(deviceContactItemsToUpload, user, context);
            saveDeviceContactsInDB(deviceContactItemsToUpload, context);
            execGetWorkContactsTask(user, this);

/*
            if (deviceContactItemsToUpload.size() == contacts.size()) {
                Log.e(TAG, "FULL contacts upload: " + deviceContactItemsToUpload.size());
                uploadContactsToCloud(user, context);
            } else {
            }
*/
        }
    }

    private void uploadContactsToCloud(
            Collection<ContactItem> contacts, ContactItem user, Context context) {
        bulkUploadContactsTask = new BulkUploadContactsTask(
                contacts, user,
                MainApplicationSingleton.CONTACTS_BULK_UPLOAD_URL, context);
        bulkUploadContactsTask.setDeviceContactsUploadTaskListener(this);
        bulkUploadContactsTask.execute();
    }

    private void execGetWorkContactsTask(
            ContactItem user, Context context) {
        GetWorkContactsTask getWorkContactsTask = new GetWorkContactsTask(user.getCompanyId(),
                user.getDataId(), user.getToken(), user.getDevice(), user.getDeviceRef(),
                user, MainApplicationSingleton.AUTH_CONTACT_GET_WORK_CONTACTS, context);
        getWorkContactsTask.setRequestTimeoutMillis(30000);
        getWorkContactsTask.setGetWorkContactsTaskListener(this);
        getWorkContactsTask.execute();
    }

    @Override
    public void onPostGetWorkContactsResponse(JSONObject response, String companyId, ContactItem user) {
//        // TODO: 16/9/16
//        remove this hack
/*
        Collection<ContactItem> mocks = MainApplicationSingleton.mockWorkContacts(null);
        saveWorkContactsInDB(mocks, this);
*/
        try {
            int status = response.getInt(MainApplicationSingleton.STATUS_PARAM);
            if (99 == status || -1 == status) {
                onPostGetWorkContactsErrorResponse(response);
            } else if (1 == status) {
//                    SUCCESS
//                // TODO: 03-06-2016
//                should the contacts be refreshed.. upload of contacts to cloud .. does it give any new info?
                JSONArray contactsJSONArray = response.optJSONArray("contacts");
                if (contactsJSONArray != null && contactsJSONArray.length() > 0) {
                    saveWorkContactsInDB(contactsJSONArray, this);
                } else {
//            retries again..
                    Log.e(TAG, "onPostGetWorkContactsResponse: Contacts Update is EMPTY - " + response);
                    onPostGetWorkContactsErrorResponse(response);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "onPostGetWorkContactsResponse: Exception - " + e.getMessage());
            onPostGetWorkContactsErrorResponse(response);
        }
    }

    @Override
    public void onPostGetWorkContactsErrorResponse(JSONObject response) {
        Log.e(TAG, "onPostGetWorkContactsErrorResponse: - " + response);
    }

    @Override
    public void onPostDeviceContactsUploadExecute(
            JSONObject response, Collection<ContactItem> deviceContactItems, ContactItem user) {
        try {
            int status = response.getInt(MainApplicationSingleton.STATUS_PARAM);
            if (1 == status) {
//                    SUCCESS
//                // TODO: 03-06-2016
//                should the contacts be refreshed.. upload of contacts to cloud .. does it give any new info?
                JSONArray contactsJSONArray = response.optJSONArray("contacts");
                if (contactsJSONArray != null && contactsJSONArray.length() > 0) {
                    saveDeviceContactsInDB(contactsJSONArray, user, this);
                } else {
//            retries again..
                    Log.e(TAG, "onPostDeviceContactsUploadExecute: Contacts Update is EMPTY - " + response);
                }
            } else {
                onPostContactsUploadExecuteFail(response, deviceContactItems, user);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "onPostDeviceContactsUploadExecute: Exception - " + e.getMessage());
        }

    }

    @Override
    public void onPostContactsUploadExecuteFail(
            JSONObject response, Collection<ContactItem> deviceContactItems, ContactItem user) {
//                ERROR
        Log.e(TAG, "onPostContactsUploadExecuteFail: " + response);
//        MainActivity.broadcastForceLogoutIfNeg1(response, this);
    }

    @Override
    public void setContactsUploadTaskToNull() {
        bulkUploadContactsTask = null;
    }

    private void saveDeviceContactsInDB(JSONArray contactsJSONArray, ContactItem user, Context context) {
        Map<Long, ContactItem> cloudSyncedContacts =
                DeviceContactContentProvider.fillDeviceContactItemFromJSONArray(
                        contactsJSONArray, user.getDeviceRef());
//            shows contacts in UI
//        showAllContacts(user.getMsgContactItemHashMap());
        saveDeviceContactsInDB(cloudSyncedContacts.values(), context);
    }

    private void saveWorkContactsInDB(JSONArray contactsJSONArray, Context context) {
        Set<ContactItem> workContacts =
                DeviceContactContentProvider.fillWorkContactItemFromJSONArray(contactsJSONArray);
        saveWorkContactsInDB(workContacts, context);
    }

    private void saveWorkContactsInDB(Collection<ContactItem> deviceContactItems, Context context) {
        WorkContactsSaveToDBTask contactsSaveToDBTask = new WorkContactsSaveToDBTask(deviceContactItems, context);
        contactsSaveToDBTask.setWorkContactsSaveToDBTaskListener(this);
        contactsSaveToDBTask.execute();
    }

    private void saveDeviceContactsInDB(Collection<ContactItem> deviceContactItems, Context context) {
        ContactsSaveToDBTask contactsSaveToDBTask = new ContactsSaveToDBTask(deviceContactItems, context);
        contactsSaveToDBTask.setContactsSaveToDBTaskListener(this);
        contactsSaveToDBTask.execute();
    }

    @Override
    public void onPostWorkContactsSaveToDBExecute(Uri result) {
        Log.e(TAG, "onPostWorkContactsSaveToDBExecute: Save in DB - SUCCESS - " + result);
    }

    @Override
    public void onPostWorkContactsSaveToDBExecuteFail(Uri result) {
        Log.e(TAG, "onPostWorkContactsSaveToDBExecuteFail: ERROR - " + result);
    }

    @Override
    public void onPostContactsSaveToDBExecute(Uri result) {
        Log.e(TAG, "onFetchCursorTaskExecute: Save in DB - SUCCESS - " + result);
    }

    @Override
    public void onPostContactsSaveToDBExecuteFail(Uri result) {
        Log.e(TAG, "onFetchCursorTaskExecuteFail: ERROR - " + result);
    }

    private void getGroups(ContactItem user) {
//                refreshes contacts
//                gets contact from cloud logic
        getGroupsTask = new GetGroupsTask(user.getDataId(), user.getToken(),
                user.getDevice(), user.getDeviceRef(), MainApplicationSingleton.AUTH_GET_GROUPS);
        getGroupsTask.setGetGroupsTaskListener(this);
        getGroupsTask.execute();
    }

    private void getBroadcast(ContactItem user) {
//                refreshes contacts
//                gets contact from cloud logic
        getBroadcastListTask = new GetBroadcastListTask(user.getDataId(), user.getToken(),
                user.getDevice(), user.getDeviceRef(), MainApplicationSingleton.AUTH_GET_BROADCAST_LISTS);
        getBroadcastListTask.setGetBroadcastTaskListener(this);
        getBroadcastListTask.execute();
    }

    @Override
    public void onPostGetGroupsTaskExecuteFail(JSONObject response) {
//                ERROR
        Log.e(TAG, "onPostGetGroupsTaskExecuteFail: Cloud response: - " + response);
    }

    @Override
    public void setGetGroupsTaskToNull() {
        getGroupsTask = null;
    }

    @Override
    public void onPostGetGroupsTaskExecute(JSONObject response) {
//            recurses to get contacts again
//        refreshContacts();
//            savesOrUpdatesDeviceContacts(contacts);
        Log.e(TAG, "onPostGetGroupsTaskExecute: GROUPS GET SUCCESS - " + response);
        int status = 0;
        if (response != null)
            status = response.optInt("status");
        if (null == response || -1 == status || 99 == status) {
//            retries again..
            onPostGetGroupsTaskExecuteFail(response);
        } else {
            JSONArray array = response.optJSONArray("groups");
            if (null == array || 0 == array.length()) {
//            retries again..
                onPostGetGroupsTaskExecuteFail(response);
            } else {
                Log.e(TAG, " Groups get - SUCCESS - ");
//                saveGroupsInDB(array);
            }
        }
    }

    @Override
    public void onPostGetBroadcastTaskExecuteFail(JSONObject response) {
//                ERROR
        Log.e(TAG, "onPostGetBroadcastTaskExecuteFail: Cloud response: - " + response);
    }

    @Override
    public void setGetBroadcastTaskToNull() {
        getBroadcastListTask = null;
    }

    @Override
    public void onPostGetBroadcastTaskExecute(JSONObject response) {
//            recurses to get contacts again
//        refreshContacts();
//            savesOrUpdatesDeviceContacts(contacts);
        int status = 0;
        if (response != null)
            status = response.optInt("status");
        if (null == response || -1 == status || 99 == status) {
//            retries again..
            onPostGetBroadcastTaskExecuteFail(response);
        } else {
            JSONArray array = response.optJSONArray("lists");
            if (null == array || 0 == array.length()) {
//            retries again..
                onPostGetBroadcastTaskExecuteFail(response);
            } else {
                Log.e(TAG, "onPostGetBroadcastTaskExecute: Success - " + response);
//                saveBroadcastInDB(array);
            }
        }
/*
"status (1, 99)
err
lists [
 {
   ""_id"": ""e7f449a6b5e656ded58a1eaee9f3c9e9"",
   ""_rev"": ""1-c12766d3e98f404174e010a28e294cd7"",
   ""doc_type"": ""BROADCAST"",
   ""doc_owner"": ""USRMASTER_919655653929"",
   ""timestamp"": 1464344765,
   ""name"": ""test"",
   ""users"": [
       {
           ""uid"": ""USRMASTER_919840348914""
       },
       {
           ""uid"": ""USRMASTER_919600037000""
       }
   ]
 }
]"
         */
    }

/*
    private void saveGroupsInDB(JSONArray contacts) {
        Collection<ContactItem> contactItems =
                MsgChatGrpContactsContentProvider.createsContactItemsFromGetGroupsJSONArray(contacts);
        saveGroupsInDB(contactItems);
    }

    private void saveBroadcastInDB(JSONArray contacts) {
        Collection<ContactItem> contactItems =
                BroadcastContactsContentProvider.createsContactItemsFromGetGroupsJSONArray(contacts);
        saveBroadcastInDB(contactItems);
    }

    private void saveGroupsInDB(Collection<ContactItem> contacts) {
        groupsSaveToDBTask = new GroupsSaveToDBTask(contacts, this);
        groupsSaveToDBTask.setGroupsSaveToDBTaskListener(this);
        groupsSaveToDBTask.execute();
    }

    private void saveBroadcastInDB(Collection<ContactItem> contacts) {
        broadcastSaveToDBTask = new BroadcastSaveToDBTask(contacts, this);
        broadcastSaveToDBTask.setBroadcastSaveToDBTaskListener(this);
        broadcastSaveToDBTask.execute();
    }

    @Override
    public void onPostGroupsSaveToDBExecute(Uri uri, Collection<ContactItem> contacts) {
        if (contacts != null && !contacts.isEmpty()) {
            Log.e(TAG, " GROUPS Contacts - SUCCESS - ");
            for (ContactItem item : contacts) {
                Collection<ContactItem> members = item.getContactItems();
                if (null == members || members.isEmpty()) {
//                    getGroupDetails(item, user);
                } else {
                    addUsersToGroups(item);
                }
            }
        } else {
            Log.e(TAG, "Group Save has returned EMPTY contacts - PLEASE CHECK: " + uri);
        }
    }

    @Override
    public void setGroupsSaveToDBTaskToNull() {
        groupsSaveToDBTask = null;
    }

    @Override
    public void onPostBroadcastSaveToDBExecute(Uri uri, Collection<ContactItem> contacts) {
        if (contacts != null && !contacts.isEmpty()) {
            Log.e(TAG, "onPostBroadcastSaveToDBExecute: Broadcast Contacts - SUCCESS - ");
            for (ContactItem item : contacts) {
                Collection<ContactItem> members = item.getContactItems();
                if (null == members || members.isEmpty()) {
//                    getGroupDetails(item, user);
                } else {
//                    addUsersToGroups(item);
                }
            }
        } else {
            Log.e(TAG, "onPostBroadcastSaveToDBExecute: EMPTY contacts - PLEASE CHECK: " + uri);
        }
    }

    @Override
    public void setBroadcastSaveToDBTaskToNull() {
        broadcastSaveToDBTask = null;
    }
*/

    private void getGroupDetails(ContactItem contactItem, ContactItem user) {
        groupDetailsTask = new GroupDetailsTask(contactItem, user.getDataId(), user.getToken(),
                user.getDevice(), user.getDeviceRef(), MainApplicationSingleton.AUTH_GROUP_DETAILS);
        groupDetailsTask.setGroupDetailsTaskListener(this);
        groupDetailsTask.execute();
    }

    @Override
    public void onPostGroupDetailsTaskExecute(JSONObject response, ContactItem item) {
        //            // TODO: 15-07-2016
//            group already saved, by get groups
//            safe case.. to update the group again, before adding users (revisit)
/*
            Uri uri = MsgChatGrpContactsContentProvider.updateGroupDetailsInDBFromJSON(
                    response, item, this);
*/
        Uri uri = null;
        if (uri != null) {
//                    group already saved.. but members freshly retrieved from cloud
//                    save groups with members again.. joins
//                    // TODO: 20-05-2016
//                    watch out for recursive loop
            addUsersToGroups(item);
        }
    }

    @Override
    public void onPostGroupDetailsTaskExecuteFail(JSONObject response, ContactItem item) {
        Log.e(TAG, "onPostGroupDetailsTaskExecuteFail: Cloud response: - " + response);
    }

    @Override
    public void setGroupDetailsTaskToNull() {
        groupDetailsTask = null;
    }

    private void addUsersToGroups(ContactItem contactItem) {
        groupsAddUsersTask = new GroupsAddUsersTask(contactItem, user.getDataId(), user.getToken(),
                user.getDevice(), user.getDeviceRef(), MainApplicationSingleton.AUTH_GROUP_ADD_USERS);
        groupsAddUsersTask.setGroupsAddUsersTaskListener(this);
        groupsAddUsersTask.execute();
    }

    @Override
    public void onPostGroupsAddUsersExecuteFail(JSONObject response,
                                                String id, String name, String[] contacts,
                                                ContactItem contactItem) {
        Log.e(TAG, "onPostGroupsAddUsersExecuteFail: " + response);
    }

    @Override
    public void setGroupsAddUsersTaskToNull() {
        groupsAddUsersTask = null;
    }

    @Override
    public void onPostGroupsAddUsersExecute(JSONObject response,
                                            String id, String name, String[] contacts,
                                            ContactItem contactItem) {
        int status = 0;
        if (response != null)
            status = response.optInt("status");
        if (null == response || 99 == status || -1 == status) {
//            retries again..
            onPostGroupsAddUsersExecuteFail(response, id, name, contacts, contactItem);
        } else {
            Log.e(TAG, " GROUPS ADD USERS - SUCCESS - ");
        }
    }


    /**
     * Handler of incoming messages from clients.
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(final Message msg) {
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                    mClients.add(msg.replyTo);
                    break;
                case MSG_UNREGISTER_CLIENT:
                    mClients.remove(msg.replyTo);
                    break;
                case MSG_SET_VALUE:
                    mValue = msg.arg1;
                    if (null != msg.obj) {
//                        does something here.. with the recieved message
                    }
                    break;
                case MSG_ON_KEY:
                    mValue = msg.arg1;
//                        does something here.. with the recieved message
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            onHandleIntent((Intent) msg.obj);
//            stopSelf(msg.arg1);
            mServiceLooper.quit();

        }
    }

}
