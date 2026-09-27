/**
 *
 */
package com.androidrocks.bex.server.servlet.user;

import com.androidrocks.bex.client.json.Friend;
import com.androidrocks.bex.server.manager.FriendManager;
import com.androidrocks.bex.server.manager.TypeFactory;

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
public class UsersGet extends HttpServlet {

    private static final Logger log = Logger.getLogger(UsersGet.class
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
        List<Friend> friends = FriendManager.fetchBEXFriends();
        log.info("#UsersGet: users size: "+friends.size());
// prepare the output
        resp.getWriter().print(TypeFactory.toJson(friends, TypeFactory.FRIEND_LIST_TYPE));
    }

}
