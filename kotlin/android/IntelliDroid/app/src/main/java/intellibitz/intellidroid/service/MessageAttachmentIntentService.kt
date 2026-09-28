package intellibitz.intellidroid.service

import android.app.IntentService
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Parcelable
import android.util.Log
import intellibitz.intellidroid.content.MsgChatAttachmentContentProvider
import intellibitz.intellidroid.content.MsgEmailAttachmentContentProvider
import intellibitz.intellidroid.data.MessageItem
import intellibitz.intellidroid.util.MainApplicationSingleton
import java.io.IOException

class MessageAttachmentIntentService : IntentService(TAG) {

    companion object {
        const val ACTION_UPDATE_EMAIL_MESSAGE_ATTACHMENT_URL =
            "intellibitz.intellidroid.service.action.ACTION_UPDATE_CONTACT_DBEMPTY_URL"
        const val ACTION_UPDATE_CHAT_MESSAGE_ATTACHMENT_URL =
            "intellibitz.intellidroid.service.action.ACTION_UPDATE_CHAT_CONTACT_URL"
        const val ACTION_UPDATE_CHATGROUP_MESSAGE_ATTACHMENT_URL =
            "intellibitz.intellidroid.service.action.ACTION_UPDATE_CHATGROUP_CONTACT_URL"
        private const val TAG = "MsgAttachmentService"

        @JvmStatic
        fun asyncUpdateEmailAttachmentFilePathInDB(context: Context, attachmentItem: MessageItem?) {
            val intent = Intent(context, MessageAttachmentIntentService::class.java)
            intent.action = ACTION_UPDATE_EMAIL_MESSAGE_ATTACHMENT_URL
            intent.putExtra(MessageItem.ATTACHMENT_MESSAGE, attachmentItem as? Parcelable)
            context.startService(intent)
        }

        @JvmStatic
        fun asyncUpdateChatAttachmentFilePathInDB(context: Context, attachmentItem: MessageItem?) {
            val intent = Intent(context, MessageAttachmentIntentService::class.java)
            intent.action = ACTION_UPDATE_CHAT_MESSAGE_ATTACHMENT_URL
            intent.putExtra(MessageItem.ATTACHMENT_MESSAGE, attachmentItem as? Parcelable)
            context.startService(intent)
        }

        @JvmStatic
        fun asyncUpdateChatGroupAttachmentFilePathInDB(context: Context, attachmentItem: MessageItem?) {
            val intent = Intent(context, MessageAttachmentIntentService::class.java)
            intent.action = ACTION_UPDATE_CHATGROUP_MESSAGE_ATTACHMENT_URL
            intent.putExtra(MessageItem.ATTACHMENT_MESSAGE, attachmentItem as? Parcelable)
            context.startService(intent)
        }
    }

    override fun onHandleIntent(intent: Intent?) {
        if (intent != null) {
            val action = intent.action
            val attachmentItem: MessageItem? =
                intent.getParcelableExtra(MessageItem.ATTACHMENT_MESSAGE)
            if (ACTION_UPDATE_EMAIL_MESSAGE_ATTACHMENT_URL == action) {
                updateEmailMessageAttachmentURL(attachmentItem)
                return
            }
            if (ACTION_UPDATE_CHAT_MESSAGE_ATTACHMENT_URL == action) {
                updateChatMessageAttachmentURL(attachmentItem)
                return
            }
            if (ACTION_UPDATE_CHATGROUP_MESSAGE_ATTACHMENT_URL == action) {
                updateChatGroupMessageAttachmentURL(attachmentItem)
            }
        }
    }

    private fun updateEmailMessageAttachmentURL(attachmentItem: MessageItem?) {
        if (attachmentItem == null) return
        try {
            val values = ContentValues()
            values.put(
                MessageItem.ATTACHMENT_MESSAGE,
                MainApplicationSingleton.Serializer.serialize(attachmentItem)
            )
            contentResolver.update(
                MsgEmailAttachmentContentProvider.CONTENT_URI, values, null, null
            )
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e(TAG, e.message ?: "")
        }
    }

    private fun updateChatMessageAttachmentURL(attachmentItem: MessageItem?) {
        if (attachmentItem == null) return
        try {
            val values = ContentValues()
            values.put(
                MessageItem.ATTACHMENT_MESSAGE,
                MainApplicationSingleton.Serializer.serialize(attachmentItem)
            )
            val contentUri = Uri.withAppendedPath(
                MsgChatAttachmentContentProvider.CONTENT_URI,
                "Chat"
            )
            contentResolver.update(contentUri, values, null, null)
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e(TAG, e.message ?: "")
        }
    }

    private fun updateChatGroupMessageAttachmentURL(attachmentItem: MessageItem?) {
        if (attachmentItem == null) return
        try {
            val values = ContentValues()
            values.put(
                MessageItem.ATTACHMENT_MESSAGE,
                MainApplicationSingleton.Serializer.serialize(attachmentItem)
            )
            val contentUri = Uri.withAppendedPath(
                MsgChatAttachmentContentProvider.CONTENT_URI,
                "Chat"
            )
            contentResolver.update(contentUri, values, null, null)
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e(TAG, e.message ?: "")
        }
    }
}
