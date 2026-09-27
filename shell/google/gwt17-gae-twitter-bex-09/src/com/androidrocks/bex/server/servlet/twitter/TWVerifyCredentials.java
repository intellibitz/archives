/**
 *
 */
package com.androidrocks.bex.server.servlet.twitter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

/**
 * @author muthu
 */
public class TWVerifyCredentials extends HttpServlet {

    private static final Logger log = Logger.getLogger(TWVerifyCredentials.class
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
/*		String user = req.getParameter("twuser");
		String pass = req.getParameter("twpass");
*/
        String user = req.getParameter("mobeegal");
        String pass = req.getParameter("pubibt06");

        try {
            URL url = new URL("http://twitter.com/account/verify_credentials.json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String line = null;
            while ((line = reader.readLine()) != null) {
                resp.getWriter().write(line);
            }
            reader.close();

        } catch (MalformedURLException e) {
            resp.getWriter().print(e.getMessage());
        } catch (IOException e) {
            resp.getWriter().print(e.getMessage());
        }
    }


}
