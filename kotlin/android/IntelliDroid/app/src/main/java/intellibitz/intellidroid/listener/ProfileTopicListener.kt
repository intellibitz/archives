package intellibitz.intellidroid.listener

import intellibitz.intellidroid.data.ContactItem
import java.io.File

interface ProfileTopicListener : ProfileListener {
    fun onProfilePicChanged(file: File?)
    fun onProfileTopicClicked(item: ContactItem?)
    fun onProfileTopicsLoaded(count: Int)
}
