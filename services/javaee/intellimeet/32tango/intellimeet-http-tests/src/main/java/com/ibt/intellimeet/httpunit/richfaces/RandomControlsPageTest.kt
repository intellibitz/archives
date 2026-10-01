package com.ibt.intellimeet.httpunit.richfaces

/*
<!--
$Id::                                                                           $: Id of last commit
$Rev::                                                                          $: Revision of last commit
$Author::                                                                       $: Author of last commit
$Date::                                                                         $: Date of last commit
$HeadURL::                                                                      $: Head URL of last commit
-->
*/

import com.meterware.httpunit.GetMethodWebRequest
import com.meterware.httpunit.WebConversation
import com.meterware.httpunit.WebRequest
import org.testng.annotations.Test

class RandomControlsPageTest {
    @Test
    @Throws(Exception::class)
    fun testRandomControlsPage() {
        val wc = WebConversation()
        val request: WebRequest = GetMethodWebRequest(
            "http://localhost:8080/intellimeet-tests/richfaces/randomControls.seam"
        )
//        WebResponse response = wc.getResponse(request);
    }
}
