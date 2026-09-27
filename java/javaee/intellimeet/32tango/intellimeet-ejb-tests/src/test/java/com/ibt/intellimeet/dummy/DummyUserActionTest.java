package com.ibt.intellimeet.dummy;

import org.jboss.seam.mock.BaseSeamTest;
import org.jboss.seam.mock.SeamTest;
import org.testng.annotations.Test;

import java.util.logging.Logger;

public class DummyUserActionTest
        extends SeamTest
{

    Logger log = Logger.getLogger(DummyUserActionTest.class.getName());

    public DummyUserActionTest()
    {
        super();
    }


    @Test
    public void testSeamComponents()
            throws Exception
    {
        new BaseSeamTest.FacesRequest("/dummy/registerDummyUser.xhtml")
        {
            @Override
            protected void invokeApplication()
                    throws Exception
            {
                setValue("#{dummyUser.name}", "test1");
                setValue("#{dummyUser.username}", "test1");
                setValue("#{dummyUser.password}", "test1");
                setValue("#{dummyUserAction.verify}", "test1");
                Object id = invokeAction("#{dummyUserAction.register}");
                log.info("TEST: Registred User with id: " + id);
            }
        }.run();

    }

}