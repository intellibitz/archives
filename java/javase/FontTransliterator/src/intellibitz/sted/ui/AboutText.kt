package intellibitz.sted.ui

import intellibitz.sted.util.Resources
import javax.swing.JTextPane
import javax.swing.text.html.HTMLEditorKit

class AboutText private constructor() : JTextPane() {
    init {
        isEditable = false
        setSize(400, 400)
        editorKit = HTMLEditorKit()
        text = Resources.getResource("about.dialog.text")
    }

    companion object {
        @JvmStatic
        var instance: AboutText? = null
            get() {
                if (field == null) {
                    field = AboutText()
                }
                return field
            }
            private set
    }
}
