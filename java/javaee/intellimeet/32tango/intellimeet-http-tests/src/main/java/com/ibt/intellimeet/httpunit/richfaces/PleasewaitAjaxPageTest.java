package com.ibt.intellimeet.httpunit.richfaces;

/*
<!--
$Id::                                                                           $: Id of last commit
$Rev::                                                                          $: Revision of last commit
$Author::                                                                       $: Author of last commit
$Date::                                                                         $: Date of last commit
$HeadURL::                                                                      $: Head URL of last commit
-->
*/

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import org.testng.annotations.Test;

public class PleasewaitAjaxPageTest
{
    @Test
    public void testPleasewaitAjaxPage()
            throws Exception
    {
        WebConversation wc = new WebConversation();
        WebRequest request = new GetMethodWebRequest
                ("http://localhost:8080/intellimeet-tests/richfaces/pleasewait_ajax.seam");
        // todo: need to test AJAX functionality
        //todo: investigate Ajax testing framework
/*
        WebResponse response = wc.getResponse(request);
*/
    }
}