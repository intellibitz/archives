/**
 *
 */
package com.androidrocks.bex.server.servlet.match;

import com.androidrocks.bex.client.json.Match;
import com.androidrocks.bex.server.manager.MatchManager;
import com.androidrocks.bex.server.manager.TypeFactory;
import com.androidrocks.bex.server.manager.UserManager;
import com.androidrocks.bex.server.persistent.User;
import com.androidrocks.bex.server.persistent.WishMatch;
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
public class WishMatchGet extends HttpServlet {

    private static final Logger log = Logger.getLogger(WishMatchGet.class
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

        if (null == name || null == token || EMPTY.equals(name) || EMPTY.equals(token)) {
            ServletHelper.onRequestFailure(req, resp, new NullPointerException("User/Pass cannot be NULL or EMPTY"));
        }

        name = name.trim();
        token = token.trim();

        log.info("Got name + token: " + name);
        User user = UserManager.loadUser(name);
        if (null == user) {
            ServletHelper.onRequestFailure(req, resp, new NullPointerException("User is NULL"));
        } else {
            List<WishMatch> matches = MatchManager.matchWishList(user);
            List<Match> jsonMatches = TypeFactory.wishMatchJson (matches);
// prepare the output
                resp.getWriter().print(TypeFactory.toJson(jsonMatches, TypeFactory.MATCH_LIST_TYPE));
        }
    }

}