package intellibitz.intellidroid.task;

import android.content.Context;
import android.net.Uri;

import intellibitz.intellidroid.IntellibitzAsyncTask;
import intellibitz.intellidroid.content.DeviceContactContentProvider;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.IntellibitzAsyncTask;
import intellibitz.intellidroid.content.DeviceContactContentProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Represents an asynchronous login/registration task used to authenticate
 * the user.
 */
public class ContactsSaveToDBTask extends
        IntellibitzAsyncTask {
    private ContactsSaveToDBTaskListener contactsSaveToDBTaskListener;
    private List<ContactItem> contacts;
    private Uri result;

    public ContactsSaveToDBTask(Collection<ContactItem> contacts, Context context) {
        super(context);
//        needs to be an specific collection like ArrayList, so serializable exception does not occur
//        contacts provider expects a set too
        this.contacts = new ArrayList<>(contacts);
    }

    public void setContactsSaveToDBTaskListener(ContactsSaveToDBTaskListener contactsSaveToDBTaskListener) {
        this.contactsSaveToDBTaskListener = contactsSaveToDBTaskListener;
    }

    @Override
    protected Boolean doInBackground(Object... params) {
        result = DeviceContactContentProvider.savesOrUpdatesDeviceContacts(contacts, getContextRef());
        return (result != null);
    }

    @Override
    protected void onPostExecute(final Object params) {
        if (success) {
            contactsSaveToDBTaskListener.onPostContactsSaveToDBExecute(result);
        } else {
            contactsSaveToDBTaskListener.onPostContactsSaveToDBExecuteFail(result);
        }
    }

    @Override
    protected void onCancelled() {
        contactsSaveToDBTaskListener.onPostContactsSaveToDBExecuteFail(result);
    }

    public interface ContactsSaveToDBTaskListener {
        void onPostContactsSaveToDBExecute(Uri result);

        void onPostContactsSaveToDBExecuteFail(Uri result);
    }

}
