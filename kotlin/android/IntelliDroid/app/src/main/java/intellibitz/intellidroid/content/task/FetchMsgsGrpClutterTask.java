package intellibitz.intellidroid.content.task;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import intellibitz.intellidroid.IntellibitzAsyncTask;
import intellibitz.intellidroid.data.MessageItem;
import intellibitz.intellidroid.db.MessageItemColumns;
import intellibitz.intellidroid.IntellibitzAsyncTask;
import intellibitz.intellidroid.content.MessageChatContentProvider;
import intellibitz.intellidroid.content.MsgsGrpClutterContentProvider;
import intellibitz.intellidroid.data.MessageItem;
import intellibitz.intellidroid.db.MessageItemColumns;
import intellibitz.intellidroid.content.MessageChatContentProvider;
import intellibitz.intellidroid.content.MsgsGrpClutterContentProvider;

import java.util.ArrayList;

/**
 */
public class FetchMsgsGrpClutterTask extends
        IntellibitzAsyncTask {

    public static final String TAG = "FetchMsgsGrpClutterTask";

    private FetchMsgsGrpClutterTaskListener fetchMsgsGrpClutterTaskListener;
    private String filter;
    private ArrayList<MessageItem> messageItems;

    public FetchMsgsGrpClutterTask(String filter, Context context) {
        super(context);
        this.filter = filter;
    }

    public void setFetchMsgsGrpClutterTaskListener(
            FetchMsgsGrpClutterTaskListener fetchMsgsGrpClutterTaskListener) {
        this.fetchMsgsGrpClutterTaskListener = fetchMsgsGrpClutterTaskListener;
    }

    @Override
    protected Boolean doInBackground(Object... params) {
        String selection = " ( " + MessageItemColumns.KEY_SUBJECT +
                " IS NULL OR " +
                MessageItemColumns.KEY_SUBJECT +
                " like ? ) ";
        selection += " AND ( " + MessageItemColumns.KEY_TYPE +
                " = 'EMAIL' ) ";
        selection += " AND ( " +
                MessageItemColumns.KEY_DEVICE_CONTACTID +
                " = 0 OR " +
                MessageItemColumns.KEY_DEVICE_CONTACTID +
                " IS NULL OR " +
                MessageItemColumns.KEY_IS_GROUP +
                " = 0 ) " +
                " AND ( " +
                MessageItemColumns.KEY_DOC_TYPE +
                " = 'MSG' ) ";
        String[] selArgs;
        if (filter != null && !filter.isEmpty()) {
            selArgs = new String[]{"%" + filter + "%"};
        } else {
            selArgs = new String[]{"%%"};
        }
//            Log.d(TAG, "FILTER: " + filter + " " + selArgs + " " + selection);
        Context context = this.context.get();
        if (null == context) {
            Log.e(TAG, "Context is null - stopping task");
            return false;
        }
        Cursor cursor = context.getContentResolver().query(
//                    MsgsGrpClutterContentProvider.JOIN_CONTENT_URI,
                MsgsGrpClutterContentProvider.CONTENT_URI,
                null,
                selection, selArgs,
//                    "mt." + MessageItemColumns.KEY_TIMESTAMP +
                MessageItemColumns.KEY_TIMESTAMP +
                        " DESC"
//                            " DESC LIMIT 10"
/*
// applies if message is joined.. NA in this case, since its only a shal group join.. no messages joined
                            +
                            "," +
                            "m." + DatabaseHelper.MessagesMessageJoinColumns.KEY_TIMESTAMP +
                            " ASC"
*/
        );
        if (cursor != null && cursor.getCount() > 0) {
            messageItems = MessageChatContentProvider.fillMessageThreadItemsFromCursor(cursor);
//            messageItems.addAll(messageItem.getMessages());
        }
        if (cursor != null) cursor.close();
        return messageItems != null && !messageItems.isEmpty();
    }

    @Override
    protected void onPostExecute(final Object result) {
        super.onPostExecute(result);
        if (success) {
            fetchMsgsGrpClutterTaskListener.onFetchMsgsGrpClutterTaskExecute(messageItems);
        } else {
            fetchMsgsGrpClutterTaskListener.onFetchMsgsGrpClutterTaskExecuteFail(messageItems);
        }
        releaseContext();
    }

    @Override
    protected void onCancelled() {
        fetchMsgsGrpClutterTaskListener.onFetchMsgsGrpClutterTaskExecuteFail(messageItems);
        releaseContext();
    }

    public interface FetchMsgsGrpClutterTaskListener {
        void onFetchMsgsGrpClutterTaskExecute(ArrayList<MessageItem> messageItem);

        void onFetchMsgsGrpClutterTaskExecuteFail(ArrayList<MessageItem> messageItem);
    }
}
