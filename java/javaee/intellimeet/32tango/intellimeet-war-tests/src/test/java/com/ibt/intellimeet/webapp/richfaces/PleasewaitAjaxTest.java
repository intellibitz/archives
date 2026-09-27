package com.ibt.intellimeet.webapp.richfaces;

/*
<!--
$Id::                                                                           $: Id of last commit
$Rev::                                                                          $: Revision of last commit
$Author::                                                                       $: Author of last commit
$Date::                                                                         $: Date of last commit
$HeadURL::                                                                      $: Head URL of last commit
-->
*/

import org.jboss.seam.mock.SeamTest;
import org.jboss.seam.mock.BaseSeamTest;
import org.testng.annotations.Test;

import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA. User: sara Date: Jan 29, 2008 Time: 4:21:28 PM To
 * change this template use File | Settings | File Templates.
 */
public class PleasewaitAjaxTest
        extends SeamTest
{

    Logger log = Logger.getLogger(PleasewaitAjaxTest.class.getName());

    public PleasewaitAjaxTest()
    {
        super();
    }


    @Test
    public void testPleasewaitAjax()
            throws Exception
    {
        new BaseSeamTest.FacesRequest(
                "/richfaces/pleasewait_ajax.xhtml")
        {
            @Override
            protected void invokeApplication()
                    throws Exception
            {
                super.invokeApplication();
                log.info("TEST: Simple invocation of the page complete ");
            }
        }.run();

    }
}
