package intellibitz.sted.ui

import org.junit.Before
import org.junit.Test

/**
 * Created by IntelliJ IDEA. User: sara Date: May 9, 2007 Time: 3:46:32 PM To
 * change this template use File | Settings | File Templates.
 */
class FontMapperDesktopFrameTest {
    private var stedWindow: STEDWindow? = null

    @Before
    fun testSTEDWindow() {
//        stedWindow = STEDWindow()
//        stedWindow.load()
    }

    @Test
    fun testFontMapperDesktopFrame() {
        val desktopFrame = DesktopFrame()
        desktopFrame.init()
    }
}
