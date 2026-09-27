package intellibitz.intellidroid;

import android.os.Bundle;
import android.view.View;

import intellibitz.intellidroid.R;

public class IntellibitzTwoPaneUserActivity extends
        IntellibitzUserActivity {
    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    protected boolean twoPane;
    //    single pane item view
    protected View itemView;
    //    two pane detail view
    protected View detailView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void setupTwopane() {
        itemView = findViewById(R.id.two_pane_container);
//        mayRequestReadContacts(itemView);
        detailView = findViewById(R.id.two_pane_empty_container);
        if (detailView != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            twoPane = true;
        }
    }
}
