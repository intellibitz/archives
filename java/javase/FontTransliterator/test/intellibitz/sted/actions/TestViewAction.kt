package intellibitz.sted.actions

import org.junit.Test
import javax.swing.Action

class TestViewAction {
    @Test
    fun testViewToolBar() {
        var action: Action? = null
        var action2: Action? = null
        try {
            action = Class.forName("intellibitz.sted.actions.ViewAction")
                .newInstance() as Action
            action2 = Class.forName("intellibitz.sted.actions.ViewAction\$ViewToolBar")
                .newInstance() as Action
        } catch (e: InstantiationException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        }
        assert(action != null)
        assert(action2 != null)
    }
}
