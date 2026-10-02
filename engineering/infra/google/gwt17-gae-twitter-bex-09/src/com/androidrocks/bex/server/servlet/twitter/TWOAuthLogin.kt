/**
 *
 */
package com.androidrocks.bex.server.servlet.twitter

import twitter4j.Twitter
import twitter4j.TwitterException
import twitter4j.http.RequestToken

import javax.servlet.ServletException
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.io.IOException
import java.util.logging.Logger

/**
 * @author muthu
 */
class TWOAuthLogin : HttpServlet() {

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
        val twitter: Twitter = Twitter()
        twitter.setOAuthConsumer("DAWQiVMAP98j7BxAD55sw", "M1Ws4JjOC78YJwQLaAwhTXYCdDcMslEnVgwcnOC6w4")
        RequestToken requestToken
        try {
            requestToken = twitter.getOAuthRequestToken()
            log.info("Got request token.")
            log.info("Request token: " + requestToken.getToken())
            log.info("Request token secret: " + requestToken.getTokenSecret())
            req.session.setAttribute("requestToken", requestToken)
            val authURL: String = requestToken.getAuthorizationURL()
            resp.sendRedirect(authURL)
        } catch (TwitterException e) {
            log.severe(e.message)
            resp.writer.write(e.message)
        }
    }
    companion object {
        private val log = Logger.getLogger(TWOAuthLogin::class.java.name)
    }
}