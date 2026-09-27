package intellibitz.intellidroid.listener;

import intellibitz.intellidroid.data.MessageItem;
import intellibitz.intellidroid.data.MessageItem;

/**
 */
public interface ClutterMessageHeaderListener extends
        ClutterListener {
    void onEmailMessageHeaderChanged(MessageItem messageItem);
}
