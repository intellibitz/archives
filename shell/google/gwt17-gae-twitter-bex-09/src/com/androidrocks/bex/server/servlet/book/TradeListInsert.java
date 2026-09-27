/**
 *
 */
package com.androidrocks.bex.server.servlet.book;

import com.androidrocks.bex.client.json.Book;
import com.androidrocks.bex.server.manager.BookManager;
import com.androidrocks.bex.server.manager.PMF;
import com.androidrocks.bex.server.manager.TypeFactory;
import com.androidrocks.bex.server.manager.UserManager;
import com.androidrocks.bex.server.persistent.TradeBook;
import com.androidrocks.bex.server.persistent.User;
import com.androidrocks.bex.server.servlet.ServletHelper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author muthu
 */
public class TradeListInsert extends HttpServlet {

    private static final Logger log = Logger.getLogger(TradeListInsert.class
            .getName());
    static final String EMPTY = "";

    /*
      * (non-Javadoc)
      *
      * @see
      * javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest
      * , javax.servlet.http.HttpServletResponse)
      */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // TODO Auto-generated method stub
        doPost(req, resp);
    }

    /*
      * (non-Javadoc)
      *
      * @see
      * javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest
      * , javax.servlet.http.HttpServletResponse)
      */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = getServletConfig().getInitParameter("action");
        String name = req.getHeader("id");
        String token = req.getHeader("token");

        if (null == name || null == token || EMPTY.equals(name) || EMPTY.equals(token)) {
            ServletHelper.onRequestFailure(req, resp, new NullPointerException("User/Pass cannot be NULL or EMPTY"));
        }

        name = name.trim();
        token = token.trim();

        log.info("Got name + token: " + name);
        User user = UserManager.loadUser(name, token);
        if (null == user) {
            ServletHelper.onRequestFailure(req, resp, new NullPointerException("User is NULL"));
        } else {
            String json = req.getParameter("content");
            if (null == json || EMPTY.equals(json)){
                ServletHelper.onRequestFailure(req, resp, new NullPointerException("Post Content cannot be NULL or EMPTY"));
            }
            log.info("Post Content: "+json);

            List<Book> jsonBooks = TypeFactory.fromJson(json, TypeFactory.BOOK_LIST_TYPE);
            if (jsonBooks == null || jsonBooks.isEmpty()) {
// this acts like delete
                    user.getTradeList().clear();
                    PMF.makePersistent(user);
            } else {
                    List<TradeBook> books =TypeFactory.transformList
                            (jsonBooks, TypeFactory.BOOK_LIST_TYPE, TypeFactory.TRADE_LIST_DATA_TYPE);
                    BookManager.saveTradeList (books);
                    UserManager.saveTradeList (user, books);
            }
// prepare the output
            resp.getWriter().print("OK");
        }
    }
}