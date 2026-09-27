package intellibitz.intellidroid.content.task;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;

import intellibitz.intellidroid.data.MessageItem;
import intellibitz.intellidroid.db.IntellibitzItemColumns;
import intellibitz.intellidroid.db.MessageItemColumns;
import intellibitz.intellidroid.content.MessageChatContentProvider;
import intellibitz.intellidroid.data.MessageItem;
import intellibitz.intellidroid.db.MessageItemColumns;
import intellibitz.intellidroid.content.MessageChatContentProvider;

import java.util.List;

import static intellibitz.intellidroid.db.IntellibitzItemColumns.KEY_DOC_TYPE;

/**
 */
public class FetchMessageThreadShalJoinCursorTask extends AsyncTask<Void, Void, Boolean> {
    private static final String TAG = "FetchMessageThreadShalJoinCursorTask";
    private List<MessageItem> messageBeen;
    private FetchCursorTaskListener fetchCursorTaskListener;
    private Context context;
    private String filter;

    public FetchMessageThreadShalJoinCursorTask(String filter, Context context) {
        super();
//        needs to be an specific collection like ArrayList, so serializable exception does not occur
//        contacts provider expects a set too
        this.filter = filter;
        this.context = context;
    }

    public void setFetchCursorTaskListener(FetchCursorTaskListener fetchCursorTaskListener) {
        this.fetchCursorTaskListener = fetchCursorTaskListener;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        String selection = " ( " +
                "mt." + MessageItemColumns.KEY_SUBJECT +
                " IS NULL OR " +
                "mt." + MessageItemColumns.KEY_SUBJECT +
                " like ? ) "
                + " AND ( " +
                "mt." + IntellibitzItemColumns.KEY_DOC_TYPE + " = 'THREAD' ) ";
        String[] selArgs;
        if (filter != null && !filter.isEmpty()) {
            selArgs = new String[]{"%" + filter + "%"};
        } else {
            selArgs = new String[]{"%%"};
        }
//            Log.d(TAG, "FILTER: " + filter + " " + selArgs + " " + selection);
        Cursor cursor = context.getContentResolver().query(
                MessageChatContentProvider.JOIN_CONTENT_URI,
                null,
                selection, selArgs,
                "mt." + MessageItemColumns.KEY_TIMESTAMP +
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
        messageBeen =
                MessageChatContentProvider.fillMessageThreadItemsFromCursor(cursor);
        if (cursor != null) cursor.close();
        return (messageBeen != null && !messageBeen.isEmpty());
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        fetchCursorTaskListener.setFetchCursorTaskToNull();
        if (success) {
            fetchCursorTaskListener.onFetchCursorTaskExecute(messageBeen);
        } else {
            fetchCursorTaskListener.onFetchCursorTaskExecuteFail(messageBeen);
        }
    }

    @Override
    protected void onCancelled() {
        fetchCursorTaskListener.setFetchCursorTaskToNull();
    }

    public interface FetchCursorTaskListener {
        void onFetchCursorTaskExecute(List<MessageItem> result);

        void onFetchCursorTaskExecuteFail(List<MessageItem> result);

        void setFetchCursorTaskToNull();
    }
}
