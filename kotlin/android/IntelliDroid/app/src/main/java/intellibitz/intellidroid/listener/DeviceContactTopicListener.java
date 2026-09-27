package intellibitz.intellidroid.listener;


import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.data.ContactItem;

/**
 */
public interface DeviceContactTopicListener extends
        ContactListener {
    void onDeviceContactTopicClicked(ContactItem item, ContactItem user);

    void onDeviceContactTopicsLoaded(int count);
}
