/**
 *
 */
package com.androidrocks.bex.server.servlet.twitter;

import com.androidrocks.bex.server.manager.TwitterFactory;
import com.androidrocks.bex.server.persistent.UserNotFoundException;
import com.androidrocks.bex.server.servlet.ServletHelper;
import twitter4j.TwitterException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * @author muthu
 */
public class PublishBook extends HttpServlet {
    private static final Logger log = Logger.getLogger(PublishBook.class
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
        String name = req.getHeader("id");
        String token = req.getHeader("token");
        String book = req.getParameter("book");


        if (null == name || null == token || null == book
                || EMPTY.equals(name) || EMPTY.equals(token) || EMPTY.equals(book)) {
            ServletHelper.onRequestFailure(req, resp, new NullPointerException("User/Book cannot be NULL or EMPTY"));
        }

        log.info("Got name + token: " + name);

        try {
            TwitterFactory.publishBook(name, token, book);
// prepare the output
            resp.getWriter().print("OK");
        } catch (UserNotFoundException e) {
            log.severe(e.getMessage());
            ServletHelper.onRequestFailure(req, resp, e);
        } catch (TwitterException e) {
            log.severe(e.getMessage());
            ServletHelper.onRequestFailure(req, resp, e);
        }
    }

}