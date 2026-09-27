package com.androidrocks.bex.server.manager;

import com.androidrocks.bex.client.json.Book;
import com.androidrocks.bex.client.json.Friend;
import com.androidrocks.bex.server.persistent.BEXFriend;
import com.androidrocks.bex.server.persistent.TradeBook;
import com.androidrocks.bex.server.persistent.TwitFriend;
import com.androidrocks.bex.server.persistent.User;
import com.androidrocks.bex.server.persistent.WishBook;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import twitter4j.TwitterException;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public final class UserManager {

    private static final Logger log = Logger.getLogger(UserManager.class
            .getName());

    private UserManager() {
    }

    public static List<Friend> fetchTwitFriends(User user) {
        Map<Key, Entity> entities = PMF.get(PMF.chopSetToPaging(user.getTwitFriends()));
        List<Friend> friends =
                TypeFactory.entitiesToJsonFriends(entities);
        return friends;
    }

    public static List<Friend> fetchBEXFriends(User user) {
        Map<Key, Entity> entities = PMF.get(PMF.chopSetToPaging(user.getBexFriends()));
        List<Friend> friends =
                TypeFactory.entitiesToJsonFriends(entities);
        return friends;
    }

    public static List<Book> fetchWishBook(User user) {
        Map<Key, Entity> entities = PMF.get(PMF.chopSetToPaging(user.getWishList()));
        List<Book> books =
                TypeFactory.entitiesToJsonBooks(entities);
        return books;
    }

    public static List<Book> fetchTradeBook(User user) {
        Map<Key, Entity> entities = PMF.get(PMF.chopSetToPaging(user.getTradeList()));
        List<Book> books =
                TypeFactory.entitiesToJsonBooks(entities);
        return books;
    }

    public static void fetchTwitterFollowers(User user)
            throws TwitterException {
// if refresh is requested.. then do this
        Map<Key, Entity> friends = TwitterFactory.getTwitFollowerEntities(user);
// save the twit friends in the system first
        List<Key> friendKeys = PMF.put(friends.values());
// now setup the relation with user and save user
        UserManager.saveTwitFriends(user, friendKeys);
// the friends need to be checked in the system, if they are registered with bex
        FriendManager.filterFriends(user, friends);
    }

    public static void fetchTwitterFriends(User user)
            throws TwitterException {
// if refresh is requested.. then do this
        Map<Key, Entity> friends = TwitterFactory.getTwitFriendEntities(user);
// save the twit friends in the system first
        List<Key> friendKeys = PMF.put(friends.values());
// now setup the relation with user and save user
        UserManager.saveTwitFriends(user, friendKeys);
// the friends need to be checked in the system, if they are registered with bex
        FriendManager.filterFriends(user, friends);
    }

    public static void saveUserWithCustomKey(User user) {
// saveUserWithCustomKey user only if brand new.. existing users should use update instead
//        if (null == PMF.loadUser(user.getScreenName(), user.getTwitterId(), user.getToken(), user.getTokenSecret())){
        PersistenceManager pm = PMF.get().getPersistenceManager();
//        pm.setDetachAllOnCommit(true);
        Transaction tx = pm.currentTransaction();
        try {
            tx.begin();
// set the key to twitter id, so retrieval is easier
            user.setKey(TypeFactory.createUserKeyWithPrefix(user.getTwitterId()));
            pm.makePersistent(user);
            log.info("#saveUserWithCustomKey: " + user);
            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            pm.close();
        }
/*
        } else {
            log.info("SKIPPED User saveUserWithCustomKey - User already in Database" + user);
        }
*/
    }

    static void addWishList(User user, WishBook book) {
        user.getWishList().add(book.getKey());
        book.getUsers().add(user.getKey());
    }

    static void removeWishList(User user, WishBook book) {
        user.getWishList().remove(book.getKey());
        book.getUsers().remove(user.getKey());
    }

    static void addTradeList(User user, TradeBook book) {
        user.getTradeList().add(book.getKey());
        book.getUsers().add(user.getKey());
    }

    static void removeTradeList(User user, TradeBook book) {
        user.getTradeList().remove(book.getKey());
        book.getUsers().remove(user.getKey());
    }

    static void addBEXFriend(User user, BEXFriend friend) {
        user.getBexFriends().add(friend.getKey());
        friend.getUsers().add(user.getKey());
    }

    static void removeBEXFriend(User user, BEXFriend friend) {
        user.getBexFriends().remove(friend.getKey());
        friend.getUsers().remove(user.getKey());
    }

    static void addTwitFriend(User user, TwitFriend friend) {
        user.getTwitFriends().add(friend.getKey());
        friend.getUsers().add(user.getKey());
    }

    static void removeTwitFriend(User user, TwitFriend friend) {
        user.getTwitFriends().remove(friend.getKey());
        friend.getUsers().remove(user.getKey());
    }

    public static void saveTwitFriend(User user, TwitFriend friend) {
/*
        user.getTwitFriends().add(friend.getKey());
        List<Key> keys = (List<Key>) PMF.get().getPersistenceManager().detachCopyAll(friend.getUsers());
        keys.add(user.getKey());
        friend.setUsers(new HashSet<Key>(keys));
*/
        addTwitFriend(user, friend);
        PMF.makePersistent(friend);
        PMF.makePersistent(user);
    }

    public static void saveBEXFriend(User user, BEXFriend friend) {
        addBEXFriend(user, friend);
        PMF.makePersistent(friend);
        PMF.makePersistent(user);
    }

    public static void saveBEXFriends(User user, List<Entity> friends) {
        List<Key> keys = PMF.put(friends);
        user.setBexFriends(new HashSet<Key>(keys));
        PMF.makePersistent(user);
    }

    public static void saveTwitFriends(User user, List<Key> friendKeys) {
        user.setTwitFriends(new HashSet<Key>(friendKeys));
        PMF.makePersistent(user);
    }

    public static void saveTradeList(User user, List<TradeBook> books) {
        for (TradeBook book : books) {
            addTradeList(user, book);
            PMF.makePersistent(book);
        }
        PMF.makePersistent(user);
    }

    public static void saveWishList(User user, List<WishBook> books) {
        for (WishBook book : books) {
            addWishList(user, book);
            PMF.makePersistent(book);
        }
        PMF.makePersistent(user);
    }

    public static User loadUser(String name) {
        User user = null;
        PersistenceManager pm = PMF.get().getPersistenceManager();
        Query query = pm.newQuery(User.class, "screenName == nameParam");
        query.declareParameters("String nameParam");
        List<User> results = (List<User>) query.execute(name);
        if (null != results && !results.isEmpty()) {
            user = results.get(0);
            log.info("#loadUser: " + user);
        }
        pm.close();
        return user;
    }

    public static User loadUser(String name, String token) {
        log.info("#loadUser: " + name);
        User user = null;
        PersistenceManager pm = PMF.get().getPersistenceManager();
        Query query = pm.newQuery(User.class, "screenName == nameParam");
        query.setFilter("token == tokenParam");
        query.declareParameters("String nameParam, String tokenParam");
        List<User> results = (List<User>) query.execute(name, token);
        if (null != results && !results.isEmpty()) {
            user = results.get(0);
            log.info("#loadUser: " + user);
        }
        pm.close();
        return user;
    }

    public static User loadUser(String name, String twitterId, String token, String tokenSecret) {
        log.info("#loadUser: " + name);
        User user = null;
        PersistenceManager pm = PMF.get().getPersistenceManager();
        Query query = pm.newQuery(User.class, "screenName == screenNameParam");
        query.setFilter("twitterId == twitterIdParam");
        query.setFilter("token == tokenParam");
        query.setFilter("tokenSecret == tokenSecretParam");
        query.declareParameters("String screenNameParam, String twitterIdParam, String tokenParam, String tokenSecretParam");
        List<User> results = (List<User>) query.executeWithArray(name, twitterId, token, tokenSecret);
        if (null != results && !results.isEmpty()) {
            user = results.get(0);
            log.info("#loadUser: " + user);
        }
        pm.close();
        return user;
    }

/*
    public static User loadUserWithTwitFriends(User user) {
        PersistenceManager pm = PMF.get().getPersistenceManager();
        Transaction tx = pm.currentTransaction();
        try {
            tx.begin();
                user = pm.getObjectById(User.class, user.getKey());
                log.info("#loadUser: TwitFriends: " + user.getTwitFriends());
                log.info("#loadUser: TwitFriends size: " + user.getTwitFriends().size());
            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            pm.close();
        }
        return user;
    }
*/

    public static User loadUserWithTwitFriends(String name, String token) {
        log.info("#loadUser: " + name);
        User user = null;
        PersistenceManager pm = PMF.get().getPersistenceManager();
        Query query = pm.newQuery(User.class, "screenName == nameParam");
        query.setFilter("token == tokenParam");
        query.declareParameters("String nameParam, String tokenParam");
        List<User> results = (List<User>) query.execute(name, token);
        if (null != results && !results.isEmpty()) {
            user = results.get(0);
            pm.retrieveAll(user.getTwitFriends());
            log.info("#loadUser: " + user);
        }
        pm.close();
        return user;
    }

    public static User loadUserWithFriends(String name, String token) {
        log.info("#loadUser: " + name);
        User user = null;
        PersistenceManager pm = PMF.get().getPersistenceManager();
        Query query = pm.newQuery(User.class, "screenName == nameParam");
        query.setFilter("token == tokenParam");
        query.declareParameters("String nameParam, String tokenParam");
        List<User> results = (List<User>) query.execute(name, token);
        if (null != results && !results.isEmpty()) {
            user = results.get(0);
            pm.retrieveAll(user.getTwitFriends());
            pm.retrieveAll(user.getBexFriends());
            log.info("#loadUser: " + user);
        }
        pm.close();
        return user;
    }

    public static User loadUserWithBEXFriends(String name, String token) {
        log.info("#loadUser: " + name);
        User user = null;
        PersistenceManager pm = PMF.get().getPersistenceManager();
        Query query = pm.newQuery(User.class, "screenName == nameParam");
        query.setFilter("token == tokenParam");
        query.declareParameters("String nameParam, String tokenParam");
        List<User> results = (List<User>) query.execute(name, token);
        if (null != results && !results.isEmpty()) {
            user = results.get(0);
            pm.retrieveAll(user.getBexFriends());
            log.info("#loadUser: " + user);
        }
        pm.close();
        return user;
    }

}