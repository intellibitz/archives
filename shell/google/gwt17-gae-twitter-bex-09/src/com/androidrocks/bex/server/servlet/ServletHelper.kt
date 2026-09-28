/**
 *
 */
package com.androidrocks.bex.server.servlet

import com.androidrocks.bex.client.json.RequestFailure
import java.io.IOException
import java.util.logging.Logger
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * @author muthu
 */
class ServletHelper {
    companion object {
        private val log = Logger.getLogger(ServletHelper::class.java.name)
        const val EMPTY = ""

        @JvmStatic
        @Throws(IOException::class)
        fun onRequestFailure(req: HttpServletRequest, resp: HttpServletResponse, e: Exception) {
            val fail = RequestFailure()
            fail.reason = "Exception: " + e.message
            req.session.setAttribute("requestFailure", fail)
            resp.sendRedirect("/_APP_ERROR")
        }
    }
}
