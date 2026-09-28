package intellibitz.intellidroid.listener

import intellibitz.intellidroid.data.ContactItem

interface DeviceContactTopicListener : ContactListener {
    fun onDeviceContactTopicClicked(item: ContactItem?, user: ContactItem?)
    fun onDeviceContactTopicsLoaded(count: Int)
}
