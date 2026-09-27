package intellibitz.sted.actions;

import org.junit.Test;

import javax.swing.*;

public class TestViewAction {

    public TestViewAction() {
    }


    @Test
    public void testViewToolBar() {
        Action action = null;
        Action action2 = null;
        try {
            action = (Action) Class
                    .forName("intellibitz.sted.actions.ViewAction")
                    .newInstance();
            action2 = (Action) Class
                    .forName("intellibitz.sted.actions.ViewAction$ViewToolBar")
                    .newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        assert action != null;
        assert action2 != null;
    }

}
