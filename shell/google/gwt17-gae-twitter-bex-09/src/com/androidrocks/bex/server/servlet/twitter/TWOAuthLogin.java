/**
 *
 */
package com.androidrocks.bex.server.servlet.twitter;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.http.RequestToken;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * @author muthu
 */
public class TWOAuthLogin extends HttpServlet {

    private static final Logger log = Logger.getLogger(TWOAuthLogin.class
            .getName());

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
        Twitter twitter = new Twitter();
        twitter.setOAuthConsumer("DAWQiVMAP98j7BxAD55sw", "M1Ws4JjOC78YJwQLaAwhTXYCdDcMslEnVgwcnOC6w4");
        RequestToken requestToken;
        try {
            requestToken = twitter.getOAuthRequestToken();
            log.info("Got request token.");
            log.info("Request token: " + requestToken.getToken());
            log.info("Request token secret: " + requestToken.getTokenSecret());
            req.getSession().setAttribute("requestToken", requestToken);
            String authURL = requestToken.getAuthorizationURL();
            resp.sendRedirect(authURL);
        } catch (TwitterException e) {
            log.severe(e.getMessage());
            resp.getWriter().write(e.getMessage());
        }
    }
}
