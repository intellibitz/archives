package intellibitz.intellidroid.listener;

/**
 */
public interface ClutterMessageListener extends
        ClutterListener {
    void onEmailMessageTyping(String text);

    void onEmailMessageTypingStopped(String text);
}
