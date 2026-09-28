package intellibitz.sted.actions

import intellibitz.sted.util.MenuHandler
import intellibitz.sted.util.Resources
import java.awt.event.ItemEvent

open class ViewAction : ItemListenerAction() {

    class ViewSample : ViewAction() {
        override fun itemStateChanged(e: ItemEvent) {
            val sampleText = getSTEDWindow().desktop!!.fontMapperDesktopFrame!!.mapperPanel!!.previewPanel!!
            sampleText.isVisible = ItemEvent.SELECTED == e.stateChange
            sampleText.validate()
        }
    }

    class ViewToolBar : ViewAction() {
        override fun itemStateChanged(e: ItemEvent) {
            MenuHandler.instance!!.getToolBar(Resources.MENUBAR_STED)!!.isVisible = ItemEvent.SELECTED == e.stateChange
        }
    }

    class ViewStatus : ViewAction() {
        override fun itemStateChanged(e: ItemEvent) {
            getSTEDWindow().statusPanel!!.isVisible = ItemEvent.SELECTED == e.stateChange
        }
    }

    class ViewMapping : ViewAction() {
        override fun itemStateChanged(e: ItemEvent) {
            val splitPane = getSTEDWindow().desktop!!.fontMapperDesktopFrame!!.mapperPanel!!.mappingEntryPanel!!.splitPane!!
            splitPane.bottomComponent.isVisible = ItemEvent.SELECTED == e.stateChange
            splitPane.resetToPreferredSizes()
            splitPane.validate()
        }
    }
}
