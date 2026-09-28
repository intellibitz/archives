/**
 *
 */
package com.androidrocks.bex.server.servlet.twitter

import javax.servlet.ServletException
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.MalformedURLException
import java.net.URL
import java.util.logging.Logger

/**
 * @author muthu
 */
class TWVerifyCredentials : HttpServlet() {

    /* (non-Javadoc)
      * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
      */
    @Throws(ServletException::class, IOException::class)
    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        // TODO Auto-generated method stub
        doPost(req, resp)
    }

    /* (non-Javadoc)
      * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
      */
    @Throws(ServletException::class, IOException::class)
    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
/*		String user = req.getParameter("twuser")
		val pass: String = req.getParameter("twpass")
*/
        val user: String = req.getParameter("mobeegal")
        val pass: String = req.getParameter("pubibt06")

        try {
            val url: URL = URL("http://twitter.com/account/verify_credentials.json")
            val reader: BufferedReader = BufferedReader(InputStreamReader(url.openStream()))
            val line: String = null
            while ((line = reader.readLine()) != null) {
                resp.writer.write(line)
            }
            reader.close()

        } catch (MalformedURLException e) {
            resp.writer.print(e.message)
        } catch (IOException e) {
            resp.writer.print(e.message)
        }
    }

    companion object {
        private val log = Logger.getLogger(TWVerifyCredentials::class.java.name)
    }
}