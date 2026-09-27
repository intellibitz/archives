package intellibitz.sted.ui;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA. User: sara Date: May 9, 2007 Time: 3:46:32 PM To
 * change this template use File | Settings | File Templates.
 */
public class FontMapperDesktopTest {
    private STEDWindow stedWindow;

    public FontMapperDesktopTest() {
    }

//    @Before public void testSTEDWindow ()
//    {
//        stedWindow = new STEDWindow();
//        stedWindow.load();
//    }

    public void testFontMapperDesktop() {
        JFrame testFrame = new JFrame("Testing");
        testFrame.setSize(300, 300);
        TabDesktop tabDesktop = new TabDesktop();
        tabDesktop.init();
        tabDesktop.createFontMapperDesktopFrame();
        tabDesktop.setVisible(true);

        testFrame.getContentPane().add(tabDesktop);
        testFrame.setVisible(true);
//        tabDesktop.init();
    }

    public static void main(String[] args) {
        FontMapperDesktopTest fontMapperDesktopTest =
                new FontMapperDesktopTest();
        fontMapperDesktopTest.testFontMapperDesktop();
    }

}