package intellibitz.intellidroid.listener;

import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.data.ContactItem;

/**
 */
public interface ContactsTopicListener extends
        ContactListener {
    void onContactsTopicClicked(ContactItem item, ContactItem user);

    void onContactsTopicsLoaded(int count);
}
