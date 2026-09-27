package com.ibt.intellimeet.app;

import org.jboss.seam.mock.BaseSeamTest;
import org.jboss.seam.mock.SeamTest;
import org.testng.annotations.Test;

import java.util.logging.Logger;

public class RegisterUserActionTest
        extends SeamTest
{

    Logger log = Logger.getLogger(RegisterUserActionTest.class.getName());

    public RegisterUserActionTest()
    {
        super();
    }


    @Test
    public void testSeamComponents()
            throws Exception
    {
        new BaseSeamTest.FacesRequest(
                "/app-ejb-tests/registerUserActionTest.xhtml")
        {
            @Override
            protected void invokeApplication()
                    throws Exception
            {
                setValue("#{user.email}", "test1@test.com");
                setValue("#{user.password}", "test1");
                setValue("#{registerUserAction.verify}", "test1");
                Object id = invokeAction("#{registerUserAction.register}");
                log.info("TEST: Registred User with id: " + id);
            }
        }.run();

    }

}