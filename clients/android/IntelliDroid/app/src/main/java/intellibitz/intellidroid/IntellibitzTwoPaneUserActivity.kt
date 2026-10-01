package intellibitz.intellidroid

import android.os.Bundle
import android.view.View

open class IntellibitzTwoPaneUserActivity : IntellibitzUserActivity() {
    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    @JvmField
    protected var twoPane = false

    // single pane item view
    @JvmField
    protected var itemView: View? = null

    // two pane detail view
    @JvmField
    protected var detailView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    protected open fun setupTwopane() {
        itemView = findViewById(R.id.two_pane_container)
        detailView = findViewById(R.id.two_pane_empty_container)
        if (detailView != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            twoPane = true
        }
    }
}
