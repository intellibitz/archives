package intellibitz.intellidroid.listener;


import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.data.ContactItem;

import java.io.File;

/**
 */
public interface ProfileTopicListener extends
        ProfileListener {
    // TODO: Update argument type and nameParam
    void onProfilePicChanged(File file);

    void onProfileTopicClicked(ContactItem item);

    void onProfileTopicsLoaded(int count);
}
