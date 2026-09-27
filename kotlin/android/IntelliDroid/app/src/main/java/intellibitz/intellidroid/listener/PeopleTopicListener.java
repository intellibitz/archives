package intellibitz.intellidroid.listener;


import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.data.MessageItem;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.data.MessageItem;

/**
 */
public interface PeopleTopicListener extends
        PeopleListener {
    void onPeopleTopicClicked(MessageItem item, ContactItem user);

    void onPeopleTopicsLoaded(int count);
}
