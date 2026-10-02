/**
 *
 */
package com.androidrocks.bex.server.servlet.twitter

import com.androidrocks.bex.client.json.RequestFailure
import com.androidrocks.bex.client.json.TwitterId
import com.androidrocks.bex.server.persistent.User
import com.google.gson.Gson

import javax.servlet.ServletException
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.io.IOException
import java.util.logging.Logger

/**
 * @author muthu
 */
class TWOAuthLoginSuccess : HttpServlet() {

    /* (non-Javadoc)
      * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
      */
    @Throws(ServletException::class, IOException::class)
    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        // TODO Auto-generated method stub
        doPost(req, resp)
    }

    /* (non-Javadoc)
      * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
      */
    @Throws(ServletException::class, IOException::class)
    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        val bexuser: User = (User) req.session.getAttribute("bexuser")
        val twitterId: TwitterId = TwitterId()
        if (null == bexuser) {
            val fail: RequestFailure = RequestFailure()
            fail.reason = "User not present in Session"
            req.session.setAttribute("requestFailure", fail)
            resp.sendRedirect("/twitter/login_failure")
        } else {
            twitterId.setScreenName(bexuser.getScreenName())
            val gson: Gson = Gson()
            resp.writer.print(gson.toJson(twitterId))
        }
    }

    companion object {
        private val log = Logger.getLogger(TWOAuthLoginSuccess::class.java.name)
    }
}