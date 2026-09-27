package intellibitz.intellidroid.content.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.content.MsgChatGrpContactsContentProvider;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.content.MsgChatGrpContactsContentProvider;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/**
 * Represents an asynchronous login/registration task used to authenticate
 * the user.
 */
public class GroupsUpdateDBTask extends AsyncTask<Void, Void, Boolean> {
    private static final String TAG = "GroupsUpdateDBTask";
    private GroupsUpdateDBTaskListener groupsUpdateDBTaskListener;
    private Collection<ContactItem> contacts =
            Collections.synchronizedSet(new HashSet<ContactItem>(1));
    private Context context;
    private int result;

    public GroupsUpdateDBTask(ContactItem contactItem, Context context) {
        super();
        contacts.add(contactItem);
        this.context = context;
    }

    public void setGroupsUpdateDBTaskListener(GroupsUpdateDBTaskListener groupsUpdateDBTaskListener) {
        this.groupsUpdateDBTaskListener = groupsUpdateDBTaskListener;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            result = MsgChatGrpContactsContentProvider.updatesContactThreadsInDB(contacts, context);
        } catch (IOException e) {
            Log.e(TAG, " ERROR - " + e.getMessage());
            e.printStackTrace();
        }
        return (result != 0);
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        groupsUpdateDBTaskListener.setGroupsUpdateDBTaskToNull();
        if (success) {
            groupsUpdateDBTaskListener.onPostGroupsUpdateDBExecute(result, contacts);
        } else {
            groupsUpdateDBTaskListener.onPostGroupsUpdateDBExecuteFail(result, contacts);
        }
    }

    @Override
    protected void onCancelled() {
        groupsUpdateDBTaskListener.setGroupsUpdateDBTaskToNull();
    }

    public interface GroupsUpdateDBTaskListener {
        void onPostGroupsUpdateDBExecute(int uri,
                                         Collection<ContactItem> contacts);

        void onPostGroupsUpdateDBExecuteFail(int uri,
                                             Collection<ContactItem> contacts);

        void setGroupsUpdateDBTaskToNull();
    }

}
