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
public class DirectMessage extends HttpServlet {
    private static final Logger log = Logger.getLogger(DirectMessage.class
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
        String friend = req.getParameter("friend");
        String book = req.getParameter("book");
        String msg = req.getParameter("content");


        if (null == name || null == token || null == friend
                || EMPTY.equals(name) || EMPTY.equals(token) || EMPTY.equals(friend)) {
            ServletHelper.onRequestFailure(req, resp, new NullPointerException("User/Friend cannot be NULL or EMPTY"));
        }

        log.info("Got name + token: " + name);

        try {
            TwitterFactory.sendDirectMessage(name, token, friend, book, msg);
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