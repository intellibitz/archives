/**
 *
 */
package com.androidrocks.bex.server.servlet.test;

import com.androidrocks.bex.client.json.Book;
import com.androidrocks.bex.client.json.Friend;
import com.androidrocks.bex.client.json.TwitterId;
import com.androidrocks.bex.server.manager.PMF;
import com.androidrocks.bex.server.manager.TypeFactory;
import com.androidrocks.bex.server.manager.UserManager;
import com.androidrocks.bex.server.manager.test.DataMockFactory;
import com.androidrocks.bex.server.persistent.BEXFriend;
import com.androidrocks.bex.server.persistent.TradeBook;
import com.androidrocks.bex.server.persistent.TwitFriend;
import com.androidrocks.bex.server.persistent.User;
import com.androidrocks.bex.server.persistent.WishBook;
import com.androidrocks.bex.server.persistent.WishMatch;
import com.androidrocks.bex.server.servlet.ServletHelper;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.gson.Gson;
import twitter4j.TwitterException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * @author muthu
 */
public class TestServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(TestServlet.class
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

        String action = getServletConfig().getInitParameter("action");
        String name = "mobeegal";
        String token = "14281625-NkNfzTWZ5yNTEP1rddHM7NHTQ1V7i6xG3ECAF9l84";
        if ("user_insert".equalsIgnoreCase(action)) {
            List<User> users = DataMockFactory.saveMockUsers();
            resp.getWriter().print(new Gson().toJson(users));
        } else if ("twit_friends".equalsIgnoreCase(action)) {
            List<TwitFriend> friends = DataMockFactory.saveTwitFriends();
            assert PMF.loadObjectById(TwitFriend.class, friends.get(0).getKey()) != null;
        } else if ("friends_update".equalsIgnoreCase(action)) {
// dealing with friends.. so retrieve the friends collection in advance
            User user = UserManager.loadUserWithFriends(name, token);
                try {
                    UserManager.fetchTwitterFriends(user);
//                    UserManager.loadUserWithTwitFriends(user);
/*
                    Set<Key> keys1 = user.getTwitFriends();
                    for (Key key : keys1) {
                        log.info("Twit Friends: "+key);
                        log.info(PMF.get().getPersistenceManager().getObjectById(TwitFriend.class, key).toString());
                    }
                    Extent extent = PMF.get().getPersistenceManager().getExtent(TwitFriend.class);
                    for (Object o : extent) {
                        log.info("Twit Friends by Extent: "+((TwitFriend)o).getKey());
                        log.info(PMF.get().getPersistenceManager().getObjectById(TwitFriend.class, ((TwitFriend)o).getKey()).toString());
                    }
                    extent.closeAll();
*/
                    Map<Key, Entity> entities = PMF.get(user.getTwitFriends());
//                    List<Friend> jsonFriends = FriendManager.fetchTwitFriends (user.getTwitFriends());
// prepare the output
                    List<com.androidrocks.bex.client.json.Friend> jsonFriends =
                            TypeFactory.entitiesToJsonFriends(entities);
                    resp.getWriter().print(TypeFactory.toJson(jsonFriends, TypeFactory.FRIEND_LIST_TYPE));
                } catch (TwitterException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    ServletHelper.onRequestFailure(req, resp, e);
                }
        } else if ("friends".equalsIgnoreCase(action)) {
            User user = UserManager.loadUserWithTwitFriends(name, token);
            User user2 = UserManager.loadUserWithBEXFriends(name, token);
//            UserManager.loadUserFriends(user2);
            Map<Key, Entity> entities = PMF.get(user.getTwitFriends());
            List<com.androidrocks.bex.client.json.Friend> jsonFriends =
                    TypeFactory.entitiesToJsonFriends(entities);
            resp.getWriter().print(TypeFactory.toJson(jsonFriends, TypeFactory.FRIEND_LIST_TYPE));

            List<Friend> friends2 = UserManager.fetchBEXFriends (user);
            resp.getWriter().print(TypeFactory.toJson(friends2, TypeFactory.FRIEND_LIST_TYPE));

            resp.getWriter().println("");
            resp.getWriter().println("========================================================");

//            resp.getWriter().print(TypeFactory.toJson(user2.getTwitFriends(), TypeFactory.FRIEND_LIST_DATA_TYPE));
        } else if ("friends_delete".equalsIgnoreCase(action)) {
            User user = UserManager.loadUser(name, token);
            if (null == user) {
                ServletHelper.onRequestFailure(req, resp, new NullPointerException("User is NULL"));
            } else {
                    user.getBexFriends().clear();
                    user.getTwitFriends().clear();
//                    user.getFriends().clear();
                    PMF.makePersistent(user);
                resp.getWriter().print(user);
            }
        } else if ("bexfriends_insert".equalsIgnoreCase(action)) {
                Set<Key> friends = DataMockFactory.saveMockBEXFriends();
                for (Key friend : friends) {
                   resp.getWriter().println("Friend: "+PMF.get().getPersistenceManager().getObjectById(BEXFriend.class, friend));
                }
// prepare the output
//                resp.getWriter().print(books);
        } else if ("wishlist_insert".equalsIgnoreCase(action)) {
                Set<Key> books = DataMockFactory.saveMockWishList();
                for (Key book : books) {
                   resp.getWriter().println("Book: "+PMF.get().getPersistenceManager().getObjectById(WishBook.class, book));
                }
// prepare the output
//                resp.getWriter().print(books);
        } else if ("wishmatch_insert".equalsIgnoreCase(action)) {
            WishMatch wishMatch = DataMockFactory.saveMockWishMatch();
            
// prepare the output
                resp.getWriter().print(wishMatch);
        } else if ("wishmatch_delete".equalsIgnoreCase(action)) {
            WishMatch wishMatch = DataMockFactory.saveMockWishMatch();
            PMF.delete (wishMatch);

// prepare the output
                resp.getWriter().print(wishMatch);
        } else if ("tradelist_insert".equalsIgnoreCase(action)) {
            User user = UserManager.loadUser(name, token);
            if (null == user) {
                ServletHelper.onRequestFailure(req, resp, new NullPointerException("User is NULL"));
            } else {
                List<TradeBook> books = DataMockFactory.mockTradeList();
//                user.setTradeList(books);
                PMF.makePersistent(user);
//                books = user.getTradeList();
// prepare the output
                resp.getWriter().print(books);
            }
        } else {
            Gson gson = new Gson();

            TwitterId twitterId = new TwitterId();
            twitterId.setScreenName("Testing TwitterId Screename with GSON");

            String json = gson.toJson(twitterId);
            resp.getWriter().print(json);
            twitterId = gson.fromJson(json, TwitterId.class);
            json = gson.toJson(twitterId);
            resp.getWriter().print(json);

            Book book = DataMockFactory.mockClientBook();

            json = gson.toJson(book, Book.class);
            log.info("Book Json: " + json);
            resp.getWriter().print(json);
            book = gson.fromJson(json, Book.class);
            json = gson.toJson(book, Book.class);
            resp.getWriter().print(json);

            com.androidrocks.bex.client.json.Friend friend = new com.androidrocks.bex.client.json.Friend();
            json = gson.toJson(friend, com.androidrocks.bex.client.json.Friend.class);
        }
    }


}
