package intellibitz.intellidroid.listener

import intellibitz.intellidroid.data.MessageItem

interface PeopleHeaderListener : PeopleListener {
    fun onPeopleHeaderChanged(messageItem: MessageItem?)
}
