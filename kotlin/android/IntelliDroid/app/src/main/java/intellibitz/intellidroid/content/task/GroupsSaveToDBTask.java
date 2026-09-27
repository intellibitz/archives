package intellibitz.intellidroid.content.task;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.content.MsgChatGrpContactsContentProvider;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.content.MsgChatGrpContactsContentProvider;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/**
 * Represents an asynchronous login/registration task used to authenticate
 * the user.
 */
public class GroupsSaveToDBTask extends AsyncTask<Void, Void, Boolean> {
    private static final String TAG = "GroupsSaveToDBTask";
    private GroupsSaveToDBTaskListener groupsSaveToDBTaskListener;
    private Collection<ContactItem> contacts =
            Collections.synchronizedSet(new HashSet<ContactItem>(1));
    private Context context;
    private Uri result;

    public GroupsSaveToDBTask(ContactItem contactItem, Context context) {
        super();
        contacts.add(contactItem);
        this.context = context;
    }

    public GroupsSaveToDBTask(Collection<ContactItem> contacts, Context context) {
        super();
        this.contacts = contacts;
        this.context = context;
    }

    public void setGroupsSaveToDBTaskListener(GroupsSaveToDBTaskListener groupsSaveToDBTaskListener) {
        this.groupsSaveToDBTaskListener = groupsSaveToDBTaskListener;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        if (null == contacts || contacts.isEmpty()) return false;
        result = MsgChatGrpContactsContentProvider.savesContactThreadsInDB(contacts, context);
        return (result != null);
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        groupsSaveToDBTaskListener.setGroupsSaveToDBTaskToNull();
        if (success) {
            groupsSaveToDBTaskListener.onPostGroupsSaveToDBExecute(result, contacts);
        } else {
            Log.e(TAG, " ERROR - " + result);
        }
    }

    @Override
    protected void onCancelled() {
        groupsSaveToDBTaskListener.setGroupsSaveToDBTaskToNull();
    }

    public interface GroupsSaveToDBTaskListener {
        void onPostGroupsSaveToDBExecute(Uri uri,
                                         Collection<ContactItem> contacts);

        void setGroupsSaveToDBTaskToNull();
    }

}
