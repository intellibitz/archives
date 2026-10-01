/**
 *
 */
package com.androidrocks.bex.server.servlet.match

import com.androidrocks.bex.client.json.Match
import com.androidrocks.bex.server.manager.MatchManager
import com.androidrocks.bex.server.manager.TypeFactory
import com.androidrocks.bex.server.manager.UserManager
import com.androidrocks.bex.server.persistent.User
import com.androidrocks.bex.server.persistent.WishMatch
import com.androidrocks.bex.server.servlet.ServletHelper

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
class WishMatchGet : HttpServlet() {

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
        val user: User = UserManager.loadUser(name)
        if (null == user) {
            ServletHelper.onRequestFailure(req, resp, NullPointerException("User is NULL"))
        } else {
            val matches: List<WishMatch> = MatchManager.matchWishList(user)
            val jsonMatches: List<Match> = TypeFactory.wishMatchJson (matches)
// prepare the output
                resp.writer.print(TypeFactory.toJson(jsonMatches, TypeFactory.MATCH_LIST_TYPE))
        }
    }

    companion object {
        private val log = Logger.getLogger(WishMatchGet::class.java.name)
        const val EMPTY = ""
    }
}