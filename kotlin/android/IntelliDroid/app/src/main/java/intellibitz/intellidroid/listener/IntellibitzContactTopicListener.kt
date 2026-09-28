package intellibitz.intellidroid.listener

import intellibitz.intellidroid.data.ContactItem

interface IntellibitzContactTopicListener : ContactListener {
    fun onIntellibitzContactTopicClicked(item: ContactItem?, user: ContactItem?)
    fun onIntellibitzContactTopicsLoaded(count: Int)
}
