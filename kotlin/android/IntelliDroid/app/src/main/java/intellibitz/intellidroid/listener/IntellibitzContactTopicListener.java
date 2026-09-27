package intellibitz.intellidroid.listener;


import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.data.ContactItem;

/**
 */
public interface IntellibitzContactTopicListener extends
        ContactListener {
    void onIntellibitzContactTopicClicked(ContactItem item, ContactItem user);

    void onIntellibitzContactTopicsLoaded(int count);
}
