package intellibitz.sted.actions

import intellibitz.sted.ui.MappingEntryPanel
import java.awt.event.ActionEvent
import javax.swing.AbstractAction

class EntryClearAction : AbstractAction() {
    private var mappingPreviewPanel: MappingEntryPanel? = null

    fun setFontPreviewPanel(mappingPreviewPanel: MappingEntryPanel) {
        this.mappingPreviewPanel = mappingPreviewPanel
    }

    fun getFontPreviewPanel(): MappingEntryPanel? {
        return mappingPreviewPanel
    }

    override fun actionPerformed(e: ActionEvent) {
        mappingPreviewPanel?.clearPreviewDisplay()
        mappingPreviewPanel?.clearButton?.isEnabled = false
    }
}
