package intellibitz.intellidroid.listener

import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.data.MessageItem

interface ClutterTopicListener : ClutterListener {
    fun onClutterTopicClicked(item: MessageItem?, user: ContactItem?)
    fun onClutterTopicsLoaded(count: Int)
}
