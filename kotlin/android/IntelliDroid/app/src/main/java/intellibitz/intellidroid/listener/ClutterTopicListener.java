package intellibitz.intellidroid.listener;


import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.data.MessageItem;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.data.MessageItem;

/**
 */
public interface ClutterTopicListener extends
        ClutterListener {
    void onClutterTopicClicked(MessageItem item, ContactItem user);

    void onClutterTopicsLoaded(int count);
}
