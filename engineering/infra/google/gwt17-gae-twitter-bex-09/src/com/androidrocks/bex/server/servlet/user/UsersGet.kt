/**
 *
 */
package com.androidrocks.bex.server.servlet.user

import com.androidrocks.bex.client.json.Friend
import com.androidrocks.bex.server.manager.FriendManager
import com.androidrocks.bex.server.manager.TypeFactory

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
class UsersGet : HttpServlet() {

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
        val friends: List<Friend> = FriendManager.fetchBEXFriends()
        log.info("#UsersGet: users size: "+friends.size())
// prepare the output
        resp.writer.print(TypeFactory.toJson(friends, TypeFactory.FRIEND_LIST_TYPE))
    }

    companion object {
        private val log = Logger.getLogger(UsersGet::class.java.name)
    }
}