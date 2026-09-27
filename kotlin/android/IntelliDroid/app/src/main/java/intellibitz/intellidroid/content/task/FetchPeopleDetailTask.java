package intellibitz.intellidroid.content.task;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;

import intellibitz.intellidroid.data.MessageItem;
import intellibitz.intellidroid.db.MessageItemColumns;
import intellibitz.intellidroid.content.MessageChatContentProvider;
import intellibitz.intellidroid.content.MessageEmailContentProvider;
import intellibitz.intellidroid.content.MessagesChatContentProvider;
import intellibitz.intellidroid.content.MessagesEmailContentProvider;
import intellibitz.intellidroid.data.MessageItem;
import intellibitz.intellidroid.db.MessageItemColumns;
import intellibitz.intellidroid.content.MessageChatContentProvider;
import intellibitz.intellidroid.content.MessageEmailContentProvider;
import intellibitz.intellidroid.content.MessagesChatContentProvider;
import intellibitz.intellidroid.content.MessagesEmailContentProvider;

/**
 */
public class FetchPeopleDetailTask extends AsyncTask<Void, Void, Boolean> {

    private static final String TAG = "FetchMessageThreadShalJoinCursorTask";
    private FetchMsgsGrpPeopleDetailTaskListener fetchMsgsGrpPeopleDetailTaskListener;
    private Context context;
    private String filter;
    private MessageItem messageItem;

    public FetchPeopleDetailTask(MessageItem messageItem, String filter, Context context) {
        super();
        this.messageItem = messageItem;
        this.filter = filter;
        this.context = context;
    }

    public void setFetchMsgsGrpPeopleDetailTaskListener(
            FetchMsgsGrpPeopleDetailTaskListener fetchMsgsGrpPeopleDetailTaskListener) {
        this.fetchMsgsGrpPeopleDetailTaskListener = fetchMsgsGrpPeopleDetailTaskListener;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        if (null == messageItem) return false;
        String chatId = messageItem.getChatId();
        if (TextUtils.isEmpty(chatId)) return false;
//                check for group contact joins..
        String selection = " ( " +
                "m." + MessageItemColumns.KEY_TEXT +
                " IS NULL OR " +
                "m." + MessageItemColumns.KEY_TEXT +
                " like ? ) AND " +
                " ( ak." + MessageItemColumns.KEY_IS_GROUP +
                " = 0 OR " +
                " ak." + MessageItemColumns.KEY_IS_GROUP +
                " IS NULL ) AND " +
                "mt." + MessageItemColumns.KEY_CHAT_ID + " = ? ";
        String[] selArgs;
        if (filter != null && !filter.isEmpty()) {
            selArgs = new String[]{"%" + filter + "%", chatId};
        } else {
            selArgs = new String[]{"%%", chatId};
        }
        String sortOrder = "m." +
                MessageItemColumns.KEY_TIMESTAMP + " ASC";
        Uri uri = Uri.withAppendedPath(
                MessagesChatContentProvider.JOIN_CONTENT_URI,
                Uri.encode(chatId));
        Cursor cursor = context.getContentResolver().query(
                uri, null, selection, selArgs, sortOrder);
        if (cursor != null && cursor.getCount() > 0) {
            try {
                MessageChatContentProvider.fillMessagesFromAllJoinCursor(
                        messageItem, cursor);
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
//            messageItems.addAll(messageItem.getMessages());
        }
        if (cursor != null) cursor.close();

        selection = " ( " +
                MessageItemColumns.KEY_SUBJECT +
                " IS NULL OR " +
                MessageItemColumns.KEY_SUBJECT +
                " like ? ) AND " +
                MessageItemColumns.KEY_TYPE +
                " = ? "
                + " AND ( " +
                MessageItemColumns.KEY_DEVICE_CONTACTID + " = ? ) "
                + " AND ( " +
                MessageItemColumns.KEY_DOC_TYPE + " = 'MSG' ) ";
        long deviceContactId = messageItem.getDeviceContactId();
        if (0 == deviceContactId) return false;
        if (deviceContactId > 0) {
            chatId = String.valueOf(deviceContactId);
            if (filter != null && !filter.isEmpty()) {
                selArgs = new String[]{"%" + filter + "%", "EMAIL", chatId};
            } else {
                selArgs = new String[]{"%%", "EMAIL", chatId};
            }
            cursor = context.getContentResolver().query(
                    MessagesEmailContentProvider.CONTENT_URI,
                    null,
                    selection, selArgs,
                    MessageItemColumns.KEY_TIMESTAMP +
                            " ASC"
            );
            if (cursor != null && cursor.getCount() > 0) {
                messageItem.getMessages().addAll(
                        MessageEmailContentProvider.fillMessageItemsFromCursor(cursor));
            }
            if (cursor != null) cursor.close();
        }

        return !messageItem.getMessages().isEmpty();
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        fetchMsgsGrpPeopleDetailTaskListener.setFetchMsgsGrpPeopleDetailTaskToNull();
        if (success) {
            fetchMsgsGrpPeopleDetailTaskListener.onFetchMsgsGrpPeopleDetailTaskExecute(messageItem);
        } else {
            fetchMsgsGrpPeopleDetailTaskListener.onFetchMsgsGrpPeopleDetailTaskExecuteFail(messageItem);
        }
    }

    @Override
    protected void onCancelled() {
        fetchMsgsGrpPeopleDetailTaskListener.setFetchMsgsGrpPeopleDetailTaskToNull();
    }

    public interface FetchMsgsGrpPeopleDetailTaskListener {
        void onFetchMsgsGrpPeopleDetailTaskExecute(MessageItem messageItem);

        void onFetchMsgsGrpPeopleDetailTaskExecuteFail(MessageItem messageItem);

        void setFetchMsgsGrpPeopleDetailTaskToNull();
    }
}
