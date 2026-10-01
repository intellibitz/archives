package com.ibt.intellimeet.httpunit

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
import com.meterware.httpunit.WebResponse
import org.testng.annotations.Test

class HomePageTest {
    @Test
    @Throws(Exception::class)
    fun testDisplayMainPage() {
        val wc = WebConversation()
        val request: WebRequest = GetMethodWebRequest(
            "http://localhost:8080/intellimeet-tests/home.seam"
        )
        val response: WebResponse = wc.getResponse(request)
        assert(
            response.title ==
                "Welcome to IntelliMeet - Jobs made easy!"
        )
    }
}
