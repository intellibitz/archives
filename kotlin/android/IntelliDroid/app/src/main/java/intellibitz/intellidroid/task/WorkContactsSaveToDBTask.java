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
public class WorkContactsSaveToDBTask extends
        IntellibitzAsyncTask {
    private static final String TAG = "WorkContactsSaveToDBTask";
    private WorkContactsSaveToDBTaskListener workContactsSaveToDBTaskListener;
    private List<ContactItem> contacts;
    private Uri result;

    public WorkContactsSaveToDBTask(Collection<ContactItem> contacts, Context context) {
        super(context);
//        needs to be an specific collection like ArrayList, so serializable exception does not occur
//        contacts provider expects a set too
        this.contacts = new ArrayList<>(contacts);
    }

    public void setWorkContactsSaveToDBTaskListener(WorkContactsSaveToDBTaskListener workContactsSaveToDBTaskListener) {
        this.workContactsSaveToDBTaskListener = workContactsSaveToDBTaskListener;
    }

    @Override
    protected Boolean doInBackground(Object... params) {
        result = DeviceContactContentProvider.savesOrUpdatesWorkContacts(contacts, getContextRef());
        return (result != null);
    }

    @Override
    protected void onPostExecute(final Object params) {
        if (success) {
            workContactsSaveToDBTaskListener.onPostWorkContactsSaveToDBExecute(result);
        } else {
            workContactsSaveToDBTaskListener.onPostWorkContactsSaveToDBExecuteFail(result);
        }
    }

    @Override
    protected void onCancelled() {
        workContactsSaveToDBTaskListener.onPostWorkContactsSaveToDBExecuteFail(result);
    }

    public interface WorkContactsSaveToDBTaskListener {
        void onPostWorkContactsSaveToDBExecute(Uri result);

        void onPostWorkContactsSaveToDBExecuteFail(Uri result);
    }

}
