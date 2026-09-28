package intellibitz.sted.ui

import intellibitz.sted.util.Resources
import intellibitz.sted.widgets.AboutDialog

class AboutSTED private constructor() : AboutDialog(Resources.getResource(Resources.TITLE_ABOUT_STED), AboutText.instance!!) {
    companion object {
        @JvmStatic
        var instance: AboutSTED? = null
            get() {
                if (field == null) {
                    field = AboutSTED()
                }
                return field
            }
            private set
    }
}
