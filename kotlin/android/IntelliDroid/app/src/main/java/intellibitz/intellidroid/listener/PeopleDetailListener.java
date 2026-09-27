package intellibitz.intellidroid.listener;

/**
 */
public interface PeopleDetailListener extends
        PeopleListener {
    void onPeopleTyping(String text);

    void onPeopleTypingStopped(String text);
}
