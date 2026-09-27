/**
 *
 */
package com.androidrocks.bex.server.servlet.twitter;

import com.androidrocks.bex.client.json.RequestFailure;
import com.androidrocks.bex.client.json.TwitterId;
import com.androidrocks.bex.server.persistent.User;
import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * @author muthu
 */
public class TWOAuthLoginSuccess extends HttpServlet {

    private static final Logger log = Logger.getLogger(TWOAuthLoginSuccess.class
            .getName());

    /* (non-Javadoc)
      * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
      */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // TODO Auto-generated method stub
        doPost(req, resp);
    }

    /* (non-Javadoc)
      * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
      */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User bexuser = (User) req.getSession().getAttribute("bexuser");
        TwitterId twitterId = new TwitterId();
        if (null == bexuser) {
            RequestFailure fail = new RequestFailure();
            fail.setReason("User not present in Session");
            req.getSession().setAttribute("requestFailure", fail);
            resp.sendRedirect("/twitter/login_failure");
        } else {
            twitterId.setScreenName(bexuser.getScreenName());
            Gson gson = new Gson();
            resp.getWriter().print(gson.toJson(twitterId));
        }
    }


}
