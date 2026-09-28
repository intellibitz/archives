/**
 *
 */
package com.androidrocks.bex.server.servlet.user

import com.androidrocks.bex.client.json.Friend
import com.androidrocks.bex.server.manager.TypeFactory
import com.androidrocks.bex.server.manager.UserManager
import com.androidrocks.bex.server.persistent.User
import com.androidrocks.bex.server.servlet.ServletHelper
import twitter4j.TwitterException

import javax.servlet.ServletException
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.io.IOException
import java.util.List
import java.util.logging.Logger

/**
 * @author muthu
 */
class TwitFriendsGet : HttpServlet() {

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

        if (null == name || null == token || EMPTY == name || EMPTY == token) {
            ServletHelper.onRequestFailure(req, resp, NullPointerException("User/Pass cannot be NULL or EMPTY"))
        }

        name = name.trim()
        token = token.trim()

        log.info("Got name + token: " + name)
        try {
            val user: User = UserManager.loadUser(name, token)
            if (null == user) {
                ServletHelper.onRequestFailure(req, resp, NullPointerException("User is NULL"))
            } else {
// todo: set refresh from client
                UserManager.fetchTwitterFriends(user)
                val friends: List<Friend> = UserManager.fetchTwitFriends(user)
// prepare the output
                resp.writer.print(TypeFactory.toJson(friends, TypeFactory.FRIEND_LIST_TYPE))
            }
        } catch (TwitterException e) {
            e.printStackTrace()
            ServletHelper.onRequestFailure(req, resp, e)
        }
    }

    companion object {
        private val log = Logger.getLogger(TwitFriendsGet::class.java.name)
        const val EMPTY = ""
    }
}