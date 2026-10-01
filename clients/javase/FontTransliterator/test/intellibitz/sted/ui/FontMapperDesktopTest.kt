package intellibitz.sted.ui


import javax.swing.*

class FontMapperDesktopTest {
    private var stedWindow: STEDWindow? = null
    //    @Before public void testSTEDWindow ()
    //    {
    //        stedWindow = new STEDWindow();
    //        stedWindow.load();
    //    }
    constructor() : super()
    //    @Before public void testSTEDWindow ()
    //    {
    //        stedWindow = new STEDWindow();
    //        stedWindow.load();
    //    }
    fun testFontMapperDesktop() {
            var testFrame: JFrame = JFrame("Testing")
            testFrame.setSize(300, 300)
            var tabDesktop: TabDesktop = TabDesktop()
            tabDesktop.init()
            tabDesktop.createFontMapperDesktopFrame()
            tabDesktop.setVisible(true)
            testFrame.getContentPane().add(tabDesktop)
            testFrame.setVisible(true)
    //        tabDesktop.init();
        }

    companion object {
        @JvmStatic
                fun main(args: Array<String>) {
                var fontMapperDesktopTest: FontMapperDesktopTest =
                        FontMapperDesktopTest()
                fontMapperDesktopTest.testFontMapperDesktop()
            }
    }
}
