package intellibitz.intellidroid.listener

import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.data.MessageItem

interface PeopleTopicListener : PeopleListener {
    fun onPeopleTopicClicked(item: MessageItem?, user: ContactItem?)
    fun onPeopleTopicsLoaded(count: Int)
}
