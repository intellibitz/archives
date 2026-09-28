/**
 *
 */
package com.androidrocks.bex.server.servlet.twitter

import com.androidrocks.bex.server.manager.TwitterFactory
import com.androidrocks.bex.server.persistent.UserNotFoundException
import com.androidrocks.bex.server.servlet.ServletHelper
import twitter4j.TwitterException

import javax.servlet.ServletException
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.io.IOException
import java.util.logging.Logger

/**
 * @author muthu
 */
class Invite : HttpServlet() {

    /*
      * (non-Javadoc)
      *
      * @see
      * javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest
      * , javax.servlet.http.HttpServletResponse)
      */
    @Throws(ServletException::class, IOException::class)
    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        // TODO Auto-generated method stub
        doPost(req, resp)
    }

    /*
      * (non-Javadoc)
      *
      * @see
      * javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest
      * , javax.servlet.http.HttpServletResponse)
      */
    @Throws(ServletException::class, IOException::class)
    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        var name: String = req.getHeader("id")
        var token: String = req.getHeader("token")
        var friend: String = req.getHeader("friend")

        if (null == name || null == token || null == friend
                || EMPTY == name || EMPTY == token || EMPTY == friend) {
            ServletHelper.onRequestFailure(req, resp, NullPointerException("User/Friend cannot be NULL or EMPTY"))
        }

        name = name.trim()
        token = token.trim()
        friend = friend.trim()

        log.info("Got name + token: " + name)

        try {
            TwitterFactory.inviteFriend(name, token, friend)
// prepare the output
            resp.writer.print("OK")
        } catch (UserNotFoundException e) {
            log.severe(e.message)
            ServletHelper.onRequestFailure(req, resp, e)
        } catch (TwitterException e) {
            log.severe(e.message)
            ServletHelper.onRequestFailure(req, resp, e)
        }
    }

    companion object {
        private val log = Logger.getLogger(Invite::class.java.name)
        const val EMPTY = ""
    }
}