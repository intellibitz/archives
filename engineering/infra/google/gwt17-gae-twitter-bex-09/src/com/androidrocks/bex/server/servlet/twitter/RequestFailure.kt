/**
 *
 */
package com.androidrocks.bex.server.servlet.twitter

import com.google.gson.Gson
import java.io.IOException
import java.util.logging.Logger
import javax.servlet.ServletException
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * @author muthu
 */
class RequestFailure : HttpServlet() {

    @Throws(ServletException::class, IOException::class)
    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        // TODO Auto-generated method stub
        doPost(req, resp)
    }

    @Throws(ServletException::class, IOException::class)
    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        val fail = req.session.getAttribute(
            "requestFailure"
        ) as com.androidrocks.bex.client.json.RequestFailure?
        val gson = Gson()
        resp.writer.print(gson.toJson(fail))
    }

    companion object {
        private val log = Logger.getLogger(RequestFailure::class.java.name)
    }
}
