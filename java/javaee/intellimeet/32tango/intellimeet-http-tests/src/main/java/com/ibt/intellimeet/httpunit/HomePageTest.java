package com.ibt.intellimeet.httpunit;

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
import com.meterware.httpunit.WebResponse;
import org.testng.annotations.Test;

public class HomePageTest
{
    @Test
    public void testDisplayMainPage()
            throws Exception
    {
        WebConversation wc = new WebConversation();
        WebRequest request = new GetMethodWebRequest
                ("http://localhost:8080/intellimeet-tests/home.seam");
        WebResponse response = wc.getResponse(request);
        assert (response.getTitle().equals
                ("Welcome to IntelliMeet - Jobs made easy!"));
    }
}
