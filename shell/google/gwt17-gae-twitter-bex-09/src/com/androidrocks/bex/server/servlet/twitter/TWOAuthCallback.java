/**
 *
 */
package com.androidrocks.bex.server.servlet.twitter;

import com.androidrocks.bex.client.json.RequestFailure;
import com.androidrocks.bex.client.json.TwitterId;
import com.androidrocks.bex.server.manager.TypeFactory;
import com.androidrocks.bex.server.manager.UserManager;
import com.androidrocks.bex.server.manager.FriendManager;
import com.androidrocks.bex.server.persistent.BEXFriend;
import com.androidrocks.bex.server.persistent.TwitFriend;
import com.androidrocks.bex.server.persistent.User;
import com.google.gson.Gson;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.http.AccessToken;
import twitter4j.http.RequestToken;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.logging.Logger;

/**
 * @author muthu
 */
public class TWOAuthCallback extends HttpServlet {
    private static final Logger log = Logger.getLogger(TWOAuthCallback.class
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
        twitter.setOAuthConsumer("DAWQiVMAP98j7BxAD55sw",
                "M1Ws4JjOC78YJwQLaAwhTXYCdDcMslEnVgwcnOC6w4");
        // todo: Obtain RequestToken from session
        RequestToken requestToken = (RequestToken) req.getSession()
                .getAttribute("requestToken");
        ;
        try {
            AccessToken accessToken = twitter.getOAuthAccessToken(requestToken);
            loginSuccess(req, resp, twitter, accessToken);
        } catch (TwitterException te) {
            loginFailure(req, resp, te);
        }
    }

    private void loginFailure(HttpServletRequest req, HttpServletResponse resp,
                              TwitterException te) throws IOException {
        if (401 == te.getStatusCode()) {
            log.severe("Unable to get the access token.");
            log.severe(te.getMessage());
        } else {
            log.severe(te.getMessage());
        }
        // todo: redirect to a failure page
        // intercept this page in client
        log.info("Redirecting response to: /twitter/login_failure");
        RequestFailure fail = new RequestFailure();
        fail.setReason("Unable to get Access Token");
        req.getSession().setAttribute("requestFailure", fail);
        resp.sendRedirect("/twitter/login_failure");
    }

    private void loginSuccess(HttpServletRequest req, HttpServletResponse resp,
                              Twitter twitter, AccessToken accessToken) throws TwitterException,
            IOException {
        // persist to the accessToken for future reference.
        User user = storeAccessToken(twitter.verifyCredentials(),
                accessToken);
        // todo: redirect to a success page, with accesstoken in session
        // intercept this page in client, and fetch accesstoken
        // req.setAttribute("accessToken", accessToken.getToken());
        // req.setAttribute("accessTokenSecret", accessToken.getTokenSecret());
        /*
           * //req.getRequestDispatcher(
           * "http://books-ex.appspot.com/twitter/login_success").forward(req, //
           * resp); log .info("Redirecting response to: /twitter/login_success");
           * req.getSession().setAttribute("user", user); resp
           * .sendRedirect("/twitter/login_success");
           */
        TwitterId twitterId = new TwitterId();
        if (null == user) {
            RequestFailure fail = new RequestFailure();
            fail.setReason("User not present in Session");
            req.getSession().setAttribute("requestFailure", fail);
//            resp.sendRedirect("/twitter/login_failure");
            resp.sendRedirect("android://books-ex.appspot.com/twitter/login_failure");
        } else {
            twitterId.setScreenName(user.getScreenName());
            twitterId.setToken(user.getToken());
            Gson gson = new Gson();
            req.setAttribute("userId", accessToken.getUserId());
            String tid = gson.toJson(twitterId);
            tid = URLEncoder.encode(tid, Charset.defaultCharset().name());
            req.setAttribute("twitterId", tid);
             resp.sendRedirect("android://books-ex.appspot.com/twitter/login_success?twitterId="+tid);
/*
            try {
                req.getRequestDispatcher("/twitter_login_success.jsp").forward(
                        req, resp);
            } catch (ServletException e) {
                e.printStackTrace();
                RequestFailure fail = new RequestFailure();
                fail.setReason(e.getMessage());
                req.getSession().setAttribute("requestFailure", fail);
                resp.sendRedirect("/twitter/login_failure");
            }
*/
        }
    }

    private User storeAccessToken(twitter4j.User jUser, AccessToken accessToken) {
        // store at.getToken()
        // store at.getTokenSecret()
        log.info("Got access token.");
        log.info("Access token: " + accessToken.getToken());
        log.info("Access token secret: " + accessToken.getTokenSecret());
        log.info("UserId: " + jUser);

        User user = new User();
        user.setTwitterId(jUser.getId());
        user.setScreenName(jUser.getScreenName());
        user.setToken(accessToken.getToken());
        user.setTokenSecret(accessToken.getTokenSecret());

        UserManager.saveUserWithCustomKey(user);

// the user has 3 forms.. User, BEXFriend, and TwitFriend.. create them all here.
        TwitFriend tf = TypeFactory.jUserToTwitFriend(jUser);
        BEXFriend bf = TypeFactory.twitFriendToBEXFriend(tf);

        FriendManager.saveBEXFriendWithCustomKey(bf);
        FriendManager.saveTwitFriendWithCustomKey(tf);
        
        return user;
    }

}
