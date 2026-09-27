package intellibitz.sted.ui;

import org.junit.Before;
import org.junit.Test;

/**
 * Created by IntelliJ IDEA. User: sara Date: May 9, 2007 Time: 3:46:32 PM To
 * change this template use File | Settings | File Templates.
 */
public class FontMapperDesktopFrameTest {
    private STEDWindow stedWindow;

    public FontMapperDesktopFrameTest() {
    }

    @Before
    public void testSTEDWindow() {
//        stedWindow = new STEDWindow();
//        stedWindow.load();
    }

    @Test
    public void testFontMapperDesktopFrame() {
        DesktopFrame desktopFrame =
                new DesktopFrame();
        desktopFrame.init();
    }

}
