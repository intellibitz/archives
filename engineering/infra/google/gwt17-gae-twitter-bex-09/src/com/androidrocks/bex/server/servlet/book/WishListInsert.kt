/**
 *
 */
package com.androidrocks.bex.server.servlet.book

import com.androidrocks.bex.client.json.Book
import com.androidrocks.bex.server.manager.BookManager
import com.androidrocks.bex.server.manager.PMF
import com.androidrocks.bex.server.manager.TypeFactory
import com.androidrocks.bex.server.manager.UserManager
import com.androidrocks.bex.server.persistent.User
import com.androidrocks.bex.server.persistent.WishBook
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
class WishListInsert : HttpServlet() {

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
        val user: User = UserManager.loadUser(name, token)
        if (null == user) {
            ServletHelper.onRequestFailure(req, resp, NullPointerException("User is NULL"))
        } else {
            val json: String = req.getParameter("content")
            if (null == json || EMPTY == json){
                ServletHelper.onRequestFailure(req, resp, NullPointerException("Post Content cannot be NULL or EMPTY"))
            }
            log.info("Post Content: "+json)

            val jsonBooks: List<Book> = TypeFactory.fromJson(json, TypeFactory.BOOK_LIST_TYPE)
            if (jsonBooks == null || jsonBooks.isEmpty()) {
// this acts like delete
                    user.getWishList().clear()
                    PMF.makePersistent(user)
            } else {
                    val books: List<WishBook> = TypeFactory.transformList
                            (jsonBooks, TypeFactory.BOOK_LIST_TYPE, TypeFactory.WISH_LIST_DATA_TYPE)
                    BookManager.saveWishList (books)
                    UserManager.saveWishList (user, books)
            }
// prepare the output
            resp.writer.print("OK")
        }

    }
    companion object {
        private val log = Logger.getLogger(WishListInsert::class.java.name)
        const val EMPTY = ""
    }
}