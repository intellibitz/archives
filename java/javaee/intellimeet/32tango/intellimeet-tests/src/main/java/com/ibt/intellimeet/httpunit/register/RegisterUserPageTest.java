package com.ibt.intellimeet.httpunit.register;

/*
<!--
$Id:: RegisterUserAction.java 41 2008-01-30 09:45:48Z muthu.ramadoss            $: Id of last commit
$Rev:: 41                                                                       $: Revision of last commit
$Author:: muthu.ramadoss                                                        $: Author of last commit
$Date:: 2008-01-30 15:15:48 +0530 (Wed, 30 Jan 2008)                            $: Date of last commit
$HeadURL:: https://intellimeet.googlecode.com/svn/trunk/32tango/intellimeet-ejb#$: Head URL of last commit
-->
*/

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import org.testng.annotations.Test;

public class RegisterUserPageTest
{
    @Test
    public void testRegisterUserPage()
            throws Exception
    {
        WebConversation wc = new WebConversation();
        WebRequest request = new GetMethodWebRequest
                ("http://localhost:8080/intellimeet/register/registerUser.seam");
        WebResponse response = wc.getResponse(request);
        assert (response.getTitle().equals
                ("IntelliMeet User Registration"));
    }
}