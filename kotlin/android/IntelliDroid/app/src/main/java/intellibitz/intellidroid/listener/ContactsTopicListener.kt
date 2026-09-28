package intellibitz.intellidroid.listener

import intellibitz.intellidroid.data.ContactItem

interface ContactsTopicListener : ContactListener {
    fun onContactsTopicClicked(item: ContactItem?, user: ContactItem?)
    fun onContactsTopicsLoaded(count: Int)
}
