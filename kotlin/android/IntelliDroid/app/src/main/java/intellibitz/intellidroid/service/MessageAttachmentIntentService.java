package intellibitz.intellidroid.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcelable;
import android.util.Log;

import intellibitz.intellidroid.data.MessageItem;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.content.MsgChatAttachmentContentProvider;
import intellibitz.intellidroid.content.MsgEmailAttachmentContentProvider;
import intellibitz.intellidroid.data.MessageItem;
import intellibitz.intellidroid.util.MainApplicationSingleton;

import java.io.IOException;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class MessageAttachmentIntentService extends IntentService {
    public static final String ACTION_UPDATE_EMAIL_MESSAGE_ATTACHMENT_URL =
            "intellibitz.intellidroid.service.action.ACTION_UPDATE_CONTACT_DBEMPTY_URL";
    public static final String ACTION_UPDATE_CHAT_MESSAGE_ATTACHMENT_URL =
            "intellibitz.intellidroid.service.action.ACTION_UPDATE_CHAT_CONTACT_URL";
    public static final String ACTION_UPDATE_CHATGROUP_MESSAGE_ATTACHMENT_URL =
            "intellibitz.intellidroid.service.action.ACTION_UPDATE_CHATGROUP_CONTACT_URL";
    private static final String TAG = "MsgAttachmentService";

    public MessageAttachmentIntentService() {
        super(TAG);
    }

    public static void asyncUpdateEmailAttachmentFilePathInDB(Context context, MessageItem attachmentItem) {
//        intent service runs async in its own thread
        Intent intent = new Intent(context, MessageAttachmentIntentService.class);
        intent.setAction(ACTION_UPDATE_EMAIL_MESSAGE_ATTACHMENT_URL);
        intent.putExtra(MessageItem.ATTACHMENT_MESSAGE, (Parcelable) attachmentItem);
        context.startService(intent);
    }

    public static void asyncUpdateChatAttachmentFilePathInDB(Context context, MessageItem attachmentItem) {
//        intent service runs async in its own thread
        Intent intent = new Intent(context, MessageAttachmentIntentService.class);
        intent.setAction(ACTION_UPDATE_CHAT_MESSAGE_ATTACHMENT_URL);
        intent.putExtra(MessageItem.ATTACHMENT_MESSAGE, (Parcelable) attachmentItem);
        context.startService(intent);
    }

    public static void asyncUpdateChatGroupAttachmentFilePathInDB(Context context, MessageItem attachmentItem) {
//        intent service runs async in its own thread
        Intent intent = new Intent(context, MessageAttachmentIntentService.class);
        intent.setAction(ACTION_UPDATE_CHATGROUP_MESSAGE_ATTACHMENT_URL);
        intent.putExtra(MessageItem.ATTACHMENT_MESSAGE, (Parcelable) attachmentItem);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            MessageItem attachmentItem = intent.getParcelableExtra(
                    MessageItem.ATTACHMENT_MESSAGE);
            if (ACTION_UPDATE_EMAIL_MESSAGE_ATTACHMENT_URL.equals(action)) {
                updateEmailMessageAttachmentURL(attachmentItem);
                return;
            }
            if (ACTION_UPDATE_CHAT_MESSAGE_ATTACHMENT_URL.equals(action)) {
                updateChatMessageAttachmentURL(attachmentItem);
                return;
            }
            if (ACTION_UPDATE_CHATGROUP_MESSAGE_ATTACHMENT_URL.equals(action)) {
                updateChatGroupMessageAttachmentURL(attachmentItem);
            }
        }
    }

    private void updateEmailMessageAttachmentURL(MessageItem attachmentItem) {
        try {
            ContentValues values = new ContentValues();
            values.put(MessageItem.ATTACHMENT_MESSAGE,
                    MainApplicationSingleton.Serializer.serialize(attachmentItem));
            int rows = getContentResolver().update(
                    MsgEmailAttachmentContentProvider.CONTENT_URI, values, null, null);
/*
            Log.e(TAG, "attachment file: saved in DB: " +
                    attachmentItem.getDownloadURL()+" updateEmailMessageAttachmentURL: " + rows);
*/
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }
    }

    private void updateChatMessageAttachmentURL(MessageItem attachmentItem) {
        try {
            ContentValues values = new ContentValues();
            values.put(MessageItem.ATTACHMENT_MESSAGE,
                    MainApplicationSingleton.Serializer.serialize(attachmentItem));
            Uri contentUri = Uri.withAppendedPath(MsgChatAttachmentContentProvider.CONTENT_URI,
                    "Chat");
            int rows = getContentResolver().update(contentUri, values, null, null);
/*
            Log.e(TAG, "attachment file: saved in DB: " +
                    attachmentItem.getDownloadURL()+" updateEmailMessageAttachmentURL: " + rows);
*/
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }
    }

    private void updateChatGroupMessageAttachmentURL(MessageItem attachmentItem) {
        try {
            ContentValues values = new ContentValues();
            values.put(MessageItem.ATTACHMENT_MESSAGE,
                    MainApplicationSingleton.Serializer.serialize(attachmentItem));
//            Uri contentUri = Uri.withAppendedPath(MsgChatGrpAttachmentContentProvider.CONTENT_URI,
            Uri contentUri = Uri.withAppendedPath(MsgChatAttachmentContentProvider.CONTENT_URI,
                    "Chat");
            int rows = getContentResolver().update(contentUri, values, null, null);
/*
            Log.e(TAG, "attachment file: saved in DB: " +
                    attachmentItem.getDownloadURL()+" updateEmailMessageAttachmentURL: " + rows);
*/
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }
    }

}
