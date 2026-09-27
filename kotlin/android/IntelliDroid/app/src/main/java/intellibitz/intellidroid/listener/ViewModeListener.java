package intellibitz.intellidroid.listener;

/**
 */
public interface ViewModeListener extends
        IntellibitzFragmentListener {

    /**
     * lets the parent fragment, know about the child fragment workflow
     * useful for handling hard press back button
     */
    void onViewModeChanged();

    void onViewModeItem();
}
