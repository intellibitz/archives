/**
 *
 */
package com.androidrocks.bex.server.servlet.twitter

import com.androidrocks.bex.client.json.RequestFailure
import com.androidrocks.bex.client.json.TwitterId
import com.androidrocks.bex.server.manager.FriendManager
import com.androidrocks.bex.server.manager.TypeFactory
import com.androidrocks.bex.server.manager.UserManager
import com.androidrocks.bex.server.persistent.User
import com.google.gson.Gson
import twitter4j.Twitter
import twitter4j.TwitterException
import twitter4j.http.AccessToken
import twitter4j.http.RequestToken
import java.io.IOException
import java.net.URLEncoder
import java.nio.charset.Charset
import java.util.logging.Logger
import javax.servlet.ServletException
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * @author muthu
 */
class TWOAuthCallback : HttpServlet() {

    @Throws(ServletException::class, IOException::class)
    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        // TODO Auto-generated method stub
        doPost(req, resp)
    }

    @Throws(ServletException::class, IOException::class)
    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        val twitter = Twitter()
        twitter.setOAuthConsumer(
            "DAWQiVMAP98j7BxAD55sw",
            "M1Ws4JjOC78YJwQLaAwhTXYCdDcMslEnVgwcnOC6w4"
        )
        // todo: Obtain RequestToken from session
        val requestToken = req.session
            .getAttribute("requestToken") as RequestToken?

        try {
            val accessToken = twitter.getOAuthAccessToken(requestToken)
            loginSuccess(req, resp, twitter, accessToken)
        } catch (te: TwitterException) {
            loginFailure(req, resp, te)
        }
    }

    @Throws(IOException::class)
    private fun loginFailure(
        req: HttpServletRequest, resp: HttpServletResponse,
        te: TwitterException
    ) {
        if (401 == te.statusCode) {
            log.severe("Unable to get the access token.")
            log.severe(te.message)
        } else {
            log.severe(te.message)
        }
        // todo: redirect to a failure page
        // intercept this page in client
        log.info("Redirecting response to: /twitter/login_failure")
        val fail = RequestFailure()
        fail.reason = "Unable to get Access Token"
        req.session.setAttribute("requestFailure", fail)
        resp.sendRedirect("/twitter/login_failure")
    }

    @Throws(TwitterException::class, IOException::class)
    private fun loginSuccess(
        req: HttpServletRequest, resp: HttpServletResponse,
        twitter: Twitter, accessToken: AccessToken
    ) {
        // persist to the accessToken for future reference.
        val user = storeAccessToken(
            twitter.verifyCredentials(),
            accessToken
        )
        val twitterId = TwitterId()
        if (null == user) {
            val fail = RequestFailure()
            fail.reason = "User not present in Session"
            req.session.setAttribute("requestFailure", fail)
            resp.sendRedirect("android://books-ex.appspot.com/twitter/login_failure")
        } else {
            twitterId.screenName = user.screenName
            twitterId.token = user.token
            val gson = Gson()
            req.setAttribute("userId", accessToken.userId)
            var tid = gson.toJson(twitterId)
            tid = URLEncoder.encode(tid, Charset.defaultCharset().name())
            req.setAttribute("twitterId", tid)
            resp.sendRedirect("android://books-ex.appspot.com/twitter/login_success?twitterId=$tid")
        }
    }

    private fun storeAccessToken(jUser: twitter4j.User, accessToken: AccessToken): User {
        // store at.getToken()
        // store at.getTokenSecret()
        log.info("Got access token.")
        log.info("Access token: " + accessToken.token)
        log.info("Access token secret: " + accessToken.tokenSecret)
        log.info("UserId: $jUser")

        val user = User()
        user.setTwitterId(jUser.id)
        user.screenName = jUser.screenName
        user.token = accessToken.token
        user.tokenSecret = accessToken.tokenSecret

        UserManager.saveUserWithCustomKey(user)

        // the user has 3 forms.. User, BEXFriend, and TwitFriend.. create them all here.
        val tf = TypeFactory.jUserToTwitFriend(jUser)
        val bf = TypeFactory.twitFriendToBEXFriend(tf)

        FriendManager.saveBEXFriendWithCustomKey(bf)
        FriendManager.saveTwitFriendWithCustomKey(tf)

        return user
    }

    companion object {
        private val log = Logger.getLogger(TWOAuthCallback::class.java.name)
    }
}
