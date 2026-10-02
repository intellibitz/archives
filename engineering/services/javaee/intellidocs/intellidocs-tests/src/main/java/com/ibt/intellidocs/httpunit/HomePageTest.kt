package com.ibt.intellidocs.httpunit

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
            "http://localhost:8080/intellidocs/home.seam"
        )
        val response: WebResponse = wc.getResponse(request)
        assert(
            response.title ==
                "Welcome to IntelliDocs - Your Desktop made Simple"
        )
    }
}
