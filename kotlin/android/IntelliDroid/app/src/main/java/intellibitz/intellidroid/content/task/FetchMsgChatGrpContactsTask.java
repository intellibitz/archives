package intellibitz.intellidroid.content.task;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import intellibitz.intellidroid.IntellibitzAsyncTask;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.db.ContactItemColumns;
import intellibitz.intellidroid.IntellibitzAsyncTask;
import intellibitz.intellidroid.content.MsgChatGrpContactsContentProvider;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.db.ContactItemColumns;
import intellibitz.intellidroid.content.MsgChatGrpContactsContentProvider;

import java.util.ArrayList;

/**
 */
public class FetchMsgChatGrpContactsTask extends
        IntellibitzAsyncTask {

    public static final String TAG = "FtchMsgChatGrpCtctsTask";

    private FetchMsgChatGrpContactsTaskListener fetchMsgChatGrpContactsTaskListener;
    private String filter;
    private ArrayList<ContactItem> contactItems;

    public FetchMsgChatGrpContactsTask(String filter, Context context) {
        super(context);
        this.filter = filter;
    }

    public void setFetchMsgChatGrpContactsTaskListener(
            FetchMsgChatGrpContactsTaskListener fetchMsgChatGrpContactsTaskListener) {
        this.fetchMsgChatGrpContactsTaskListener = fetchMsgChatGrpContactsTaskListener;
    }

    @Override
    protected Boolean doInBackground(Object... params) {
/*
//===========================================================================
//            IMPORTANT
//  JOIN QUERY - SO REMOVE AMBIGUITY BY SPECIFYING JOIN TABLE NAME
// GC AS PREFIX IS REQUIRED FOR THE QUERY TO WORK CORRECTLY
//===========================================================================
             */
        String selection = " ( name IS NULL OR name like ? ) "
                + " AND ( " +
                ContactItemColumns.KEY_IS_GROUP +
                " = 1 ) AND ( " +
                ContactItemColumns.KEY_IS_EMAIL +
                " = 0 OR " +
                ContactItemColumns.KEY_IS_EMAIL +
                " IS NULL ) ";
/*
                    +
                    " AND ( ak." + ContactContentProvider.ContactItemColumns.KEY_IS_GROUP +
                    " = 0 OR " +
                    " ak." + ContactContentProvider.ContactItemColumns.KEY_IS_GROUP +
                    " IS NULL ) "
*/
/*
                    +
                    " AND ( ak." + MsgChatGrpContactsContentProvider.ContactItemColumns.KEY_IS_GROUP +
                    " = 1 OR " +
                    " ak." + MsgChatGrpContactsContentProvider.ContactItemColumns.KEY_IS_GROUP +
                    " IS NULL ) "
*/
        String[] selArgs;
        if (filter != null && !filter.isEmpty()) {
            selArgs = new String[]{"%" + filter + "%"};
        } else {
            selArgs = new String[]{"%%"};
        }
//            Log.d(TAG, "FILTER: " + filter + " " + selArgs + " " + selection);
/*
        return new CursorLoader(getActivity(),
                MsgChatGrpContactsContentProvider.CONTENT_URI,
                null,
                selection, selArgs,
                MsgChatGrpContactsContentProvider.ContactItemColumns.KEY_TIMESTAMP + " DESC");
*/
//            Log.d(TAG, "FILTER: " + filter + " " + selArgs + " " + selection);
        Context context = this.context.get();
        if (null == context) {
            Log.e(TAG, "Context is null - stopping task");
            return false;
        }
        Cursor cursor = context.getContentResolver().query(
//                    MsgChatGrpContactsContentProvider.JOIN_CONTENT_URI,
                MsgChatGrpContactsContentProvider.CONTENT_URI,
                null,
                selection, selArgs,
//                    "mt." + MsgChatGrpContactsContentProvider.MsgChatGrpContactsItemColumns.KEY_TIMESTAMP +
                ContactItemColumns.KEY_TIMESTAMP + " DESC");
//                            " DESC LIMIT 10"
/*
// applies if message is joined.. NA in this case, since its only a shal group join.. no messages joined
                            +
                            "," +
                            "m." + DatabaseHelper.MessagesMessageJoinColumns.KEY_TIMESTAMP +
                            " ASC"
*/
        if (cursor != null && cursor.getCount() > 0) {
            this.contactItems =
                    MsgChatGrpContactsContentProvider.createsContactsFromCursor(cursor);
//            contactItems.addAll(messageItem.getMessages());
        }
        if (cursor != null) cursor.close();
        return contactItems != null && !contactItems.isEmpty();
    }

    @Override
    protected void onPostExecute(final Object result) {
        super.onPostExecute(result);
        if (success) {
            fetchMsgChatGrpContactsTaskListener.onFetchMsgChatGrpContactsTaskExecute(contactItems);
        } else {
            fetchMsgChatGrpContactsTaskListener.onFetchMsgChatGrpContactsTaskExecuteFail(contactItems);
        }
        releaseContext();
    }

    @Override
    protected void onCancelled() {
        fetchMsgChatGrpContactsTaskListener.onFetchMsgChatGrpContactsTaskExecuteFail(contactItems);
        releaseContext();
    }

    public interface FetchMsgChatGrpContactsTaskListener {
        void onFetchMsgChatGrpContactsTaskExecute(ArrayList<ContactItem> contactItems);

        void onFetchMsgChatGrpContactsTaskExecuteFail(ArrayList<ContactItem> contactItems);
    }
}
