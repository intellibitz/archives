package intellibitz.intellidroid.content.task;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import intellibitz.intellidroid.IntellibitzAsyncTask;
import intellibitz.intellidroid.content.MsgChatAttachmentContentProvider;
import intellibitz.intellidroid.data.MessageItem;
import intellibitz.intellidroid.db.MessageItemColumns;
import intellibitz.intellidroid.IntellibitzAsyncTask;
import intellibitz.intellidroid.content.MsgChatAttachmentContentProvider;
import intellibitz.intellidroid.content.MsgChatGrpAttachmentContentProvider;
import intellibitz.intellidroid.content.MsgEmailAttachmentContentProvider;
import intellibitz.intellidroid.data.MessageItem;
import intellibitz.intellidroid.db.MessageItemColumns;
import intellibitz.intellidroid.content.MsgChatAttachmentContentProvider;
import intellibitz.intellidroid.content.MsgChatGrpAttachmentContentProvider;
import intellibitz.intellidroid.content.MsgEmailAttachmentContentProvider;

import java.util.ArrayList;
import java.util.Collection;

/**
 */
public class FetchAttachmentsTask extends
        IntellibitzAsyncTask {

    private static final String TAG = "FetchAttachmentsTask";
    private FetchAttachmentsTaskListener fetchAttachmentsTaskListener;
    private String filter;
    private ArrayList<MessageItem> attachmentItems = new ArrayList<>();

    public FetchAttachmentsTask(String filter, Context context) {
        super(context);
        this.filter = filter;
    }

    public void setFetchAttachmentsTaskListener(
            FetchAttachmentsTaskListener fetchAttachmentsTaskListener) {
        this.fetchAttachmentsTaskListener = fetchAttachmentsTaskListener;
    }

    @Override
    protected Boolean doInBackground(Object... params) {
        Context context = this.context.get();
        if (null == context) {
            Log.e(TAG, "Context NULL - skipping");
            return false;
        }
        String selection = " ( " +
                MessageItemColumns.KEY_NAME +
                " IS NULL OR " +
                MessageItemColumns.KEY_DESCRIPTION +
                " IS NULL OR " +
                MessageItemColumns.KEY_NAME +
                " like ? OR " +
                MessageItemColumns.KEY_DESCRIPTION +
                " like ? ) ";
        String[] selArgs;
        if (filter != null && !filter.isEmpty()) {
            selArgs = new String[]{"%" + filter + "%", "%" + filter + "%"};
        } else {
            selArgs = new String[]{"%%", "%%"};
        }
        String sortOrder =
                MessageItemColumns.KEY_TIMESTAMP + " ASC";
        Cursor cursor = context.getContentResolver().query(
                MsgChatAttachmentContentProvider.CONTENT_URI, null, selection, selArgs, sortOrder);
        Collection<MessageItem> items = MsgChatAttachmentContentProvider.fillAttachmentsFromCursor(cursor);
        if (cursor != null) cursor.close();
        if (items != null && !items.isEmpty())
            attachmentItems.addAll(items);

        selection = " ( " +
                MessageItemColumns.KEY_NAME +
                " IS NULL OR " +
                MessageItemColumns.KEY_DESCRIPTION +
                " IS NULL OR " +
                MessageItemColumns.KEY_NAME +
                " like ? OR " +
                MessageItemColumns.KEY_DESCRIPTION +
                " like ? ) ";
        if (filter != null && !filter.isEmpty()) {
            selArgs = new String[]{"%" + filter + "%", "%" + filter + "%"};
        } else {
            selArgs = new String[]{"%%", "%%"};
        }
        sortOrder =
                MessageItemColumns.KEY_TIMESTAMP + " ASC";
        cursor = context.getContentResolver().query(
                MsgChatGrpAttachmentContentProvider.CONTENT_URI, null, selection, selArgs, sortOrder);
        items = MsgChatAttachmentContentProvider.fillAttachmentsFromCursor(cursor);
        if (cursor != null) cursor.close();
        if (items != null && !items.isEmpty())
            attachmentItems.addAll(items);

        selection = " ( " +
                MessageItemColumns.KEY_NAME +
                " IS NULL OR " +
                MessageItemColumns.KEY_DESCRIPTION +
                " IS NULL OR " +
                MessageItemColumns.KEY_NAME +
                " like ? OR " +
                MessageItemColumns.KEY_DESCRIPTION +
                " like ? ) ";
        if (filter != null && !filter.isEmpty()) {
            selArgs = new String[]{"%" + filter + "%", "%" + filter + "%"};
        } else {
            selArgs = new String[]{"%%", "%%"};
        }
        sortOrder =
                MessageItemColumns.KEY_TIMESTAMP + " ASC";
        cursor = context.getContentResolver().query(
                MsgEmailAttachmentContentProvider.CONTENT_URI, null, selection, selArgs, sortOrder);
        items = MsgChatAttachmentContentProvider.fillAttachmentsFromCursor(cursor);
        if (cursor != null) cursor.close();
        if (items != null && !items.isEmpty())
            attachmentItems.addAll(items);

        if (attachmentItems != null && !attachmentItems.isEmpty()) {
            MessageItem[] items1 = attachmentItems.toArray(new MessageItem[0]);
            for (MessageItem item : items1) {
//                some quoted-printable, 7bit, text, html and invalid attachments are being stored
//                // TODO: 10/9/16
//                to clean up invalid requirements, at the entry point
                if (TextUtils.isEmpty(item.getName())) {
                    String description = item.getDescription();
                    if ((TextUtils.isEmpty(description)) || "null".equalsIgnoreCase(description)) {
                        attachmentItems.remove(item);
                    }
                }
            }
        }
        return !attachmentItems.isEmpty();
    }

    @Override
    protected void onPostExecute(final Object result) {
        super.onPostExecute(result);
        if (success) {
            fetchAttachmentsTaskListener.onFetchAttachmentsTaskExecute(attachmentItems);
        } else {
            fetchAttachmentsTaskListener.onFetchAttachmentsTaskExecuteFail(attachmentItems);
        }
        releaseContext();
    }

    @Override
    protected void onCancelled() {
        fetchAttachmentsTaskListener.onFetchAttachmentsTaskExecuteFail(attachmentItems);
        releaseContext();
    }

    public interface FetchAttachmentsTaskListener {
        void onFetchAttachmentsTaskExecute(ArrayList<MessageItem> attachmentItems);

        void onFetchAttachmentsTaskExecuteFail(ArrayList<MessageItem> attachmentItems);
    }
}
