package intellibitz.intellidroid.listener

import intellibitz.intellidroid.data.MessageItem

interface ClutterMessageHeaderListener : ClutterListener {
    fun onEmailMessageHeaderChanged(messageItem: MessageItem?)
}
