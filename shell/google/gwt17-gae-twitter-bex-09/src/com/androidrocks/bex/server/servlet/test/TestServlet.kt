/**
 *
 */
package com.androidrocks.bex.server.servlet.test

import com.androidrocks.bex.client.json.Book
import com.androidrocks.bex.client.json.Friend
import com.androidrocks.bex.client.json.TwitterId
import com.androidrocks.bex.server.manager.PMF
import com.androidrocks.bex.server.manager.TypeFactory
import com.androidrocks.bex.server.manager.UserManager
import com.androidrocks.bex.server.manager.test.DataMockFactory
import com.androidrocks.bex.server.persistent.BEXFriend
import com.androidrocks.bex.server.persistent.TradeBook
import com.androidrocks.bex.server.persistent.TwitFriend
import com.androidrocks.bex.server.persistent.User
import com.androidrocks.bex.server.persistent.WishBook
import com.androidrocks.bex.server.persistent.WishMatch
import com.androidrocks.bex.server.servlet.ServletHelper
import com.google.appengine.api.datastore.Entity
import com.google.appengine.api.datastore.Key
import com.google.gson.Gson
import twitter4j.TwitterException
import java.io.IOException
import java.util.logging.Logger
import javax.servlet.ServletException
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * @author muthu
 */
class TestServlet : HttpServlet() {

    @Throws(ServletException::class, IOException::class)
    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        // TODO Auto-generated method stub
        doPost(req, resp)
    }

    @Throws(ServletException::class, IOException::class)
    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {

        val action = servletConfig.getInitParameter("action")
        val name = "mobeegal"
        val token = "14281625-NkNfzTWZ5yNTEP1rddHM7NHTQ1V7i6xG3ECAF9l84"
        if ("user_insert".equals(action, ignoreCase = true)) {
            val users = DataMockFactory.saveMockUsers()
            resp.writer.print(Gson().toJson(users))
        } else if ("twit_friends".equals(action, ignoreCase = true)) {
            val friends = DataMockFactory.saveTwitFriends()
            assert(PMF.loadObjectById(TwitFriend::class.java, friends[0].key) != null)
        } else if ("friends_update".equals(action, ignoreCase = true)) {
            val user = UserManager.loadUserWithFriends(name, token)
            try {
                UserManager.fetchTwitterFriends(user)
                val entities = PMF.get(user.getTwitFriends())
                val jsonFriends =
                    TypeFactory.entitiesToJsonFriends(entities)
                resp.writer.print(TypeFactory.toJson(jsonFriends, TypeFactory.FRIEND_LIST_TYPE))
            } catch (e: TwitterException) {
                e.printStackTrace()
                ServletHelper.onRequestFailure(req, resp, e)
            }
        } else if ("friends".equals(action, ignoreCase = true)) {
            val user = UserManager.loadUserWithTwitFriends(name, token)
            val user2 = UserManager.loadUserWithBEXFriends(name, token)
            val entities = PMF.get(user.getTwitFriends())
            val jsonFriends =
                TypeFactory.entitiesToJsonFriends(entities)
            resp.writer.print(TypeFactory.toJson(jsonFriends, TypeFactory.FRIEND_LIST_TYPE))

            val friends2 = UserManager.fetchBEXFriends(user)
            resp.writer.print(TypeFactory.toJson(friends2, TypeFactory.FRIEND_LIST_TYPE))

            resp.writer.println("")
            resp.writer.println("========================================================")
        } else if ("friends_delete".equals(action, ignoreCase = true)) {
            val user = UserManager.loadUser(name, token)
            if (null == user) {
                ServletHelper.onRequestFailure(req, resp, NullPointerException("User is NULL"))
            } else {
                user.getBexFriends().clear()
                user.getTwitFriends().clear()
                PMF.makePersistent(user)
                resp.writer.print(user)
            }
        } else if ("bexfriends_insert".equals(action, ignoreCase = true)) {
            val friends = DataMockFactory.saveMockBEXFriends()
            for (friend in friends) {
                resp.writer.println(
                    "Friend: " + PMF.get().persistenceManager.getObjectById(
                        BEXFriend::class.java,
                        friend
                    )
                )
            }
        } else if ("wishlist_insert".equals(action, ignoreCase = true)) {
            val books = DataMockFactory.saveMockWishList()
            for (book in books) {
                resp.writer.println(
                    "Book: " + PMF.get().persistenceManager.getObjectById(
                        WishBook::class.java,
                        book
                    )
                )
            }
        } else if ("wishmatch_insert".equals(action, ignoreCase = true)) {
            val wishMatch = DataMockFactory.saveMockWishMatch()
            resp.writer.print(wishMatch)
        } else if ("wishmatch_delete".equals(action, ignoreCase = true)) {
            val wishMatch = DataMockFactory.saveMockWishMatch()
            PMF.delete(wishMatch)
            resp.writer.print(wishMatch)
        } else if ("tradelist_insert".equals(action, ignoreCase = true)) {
            val user = UserManager.loadUser(name, token)
            if (null == user) {
                ServletHelper.onRequestFailure(req, resp, NullPointerException("User is NULL"))
            } else {
                val books = DataMockFactory.mockTradeList()
                PMF.makePersistent(user)
                resp.writer.print(books)
            }
        } else {
            val gson = Gson()

            var twitterId = TwitterId()
            twitterId.screenName = "Testing TwitterId Screename with GSON"

            var json = gson.toJson(twitterId)
            resp.writer.print(json)
            twitterId = gson.fromJson(json, TwitterId::class.java)
            json = gson.toJson(twitterId)
            resp.writer.print(json)

            var book = DataMockFactory.mockClientBook()

            json = gson.toJson(book, Book::class.java)
            log.info("Book Json: $json")
            resp.writer.print(json)
            book = gson.fromJson(json, Book::class.java)
            json = gson.toJson(book, Book::class.java)
            resp.writer.print(json)

            val friend = Friend()
            json = gson.toJson(friend, Friend::class.java)
        }
    }

    companion object {
        private val log = Logger.getLogger(TestServlet::class.java.name)
    }
}
