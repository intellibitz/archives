package com.androidrocks.bex.server.manager

import com.androidrocks.bex.client.json.Book
import com.androidrocks.bex.client.json.Friend
import com.androidrocks.bex.server.persistent.BEXFriend
import com.androidrocks.bex.server.persistent.TradeBook
import com.androidrocks.bex.server.persistent.TwitFriend
import com.androidrocks.bex.server.persistent.User
import com.androidrocks.bex.server.persistent.WishBook
import com.google.appengine.api.datastore.Entity
import com.google.appengine.api.datastore.Key
import twitter4j.TwitterException
import javax.jdo.PersistenceManager
import javax.jdo.Query
import javax.jdo.Transaction
import java.util.HashSet
import java.util.List
import java.util.Map
import java.util.logging.Logger

class UserManager private constructor() {
    companion object {
        private val log: Logger = Logger.getLogger(UserManager::class.java
            .getName())
        @JvmStatic
        fun fetchTwitFriends(user: User?): List<Friend>? {

                    val entities = PMF.get(PMF.chopSetToPaging(user.getTwitFriends()))
                    List<Friend> friends =
                            TypeFactory.entitiesToJsonFriends(entities)
                    return friends
        }

        @JvmStatic
        fun fetchBEXFriends(user: User?): List<Friend>? {

                    val entities = PMF.get(PMF.chopSetToPaging(user.getBexFriends()))
                    List<Friend> friends =
                            TypeFactory.entitiesToJsonFriends(entities)
                    return friends
        }

        @JvmStatic
        fun fetchWishBook(user: User?): List<Book>? {

                    val entities = PMF.get(PMF.chopSetToPaging(user.getWishList()))
                    List<Book> books =
                            TypeFactory.entitiesToJsonBooks(entities)
                    return books
        }

        @JvmStatic
        fun fetchTradeBook(user: User?): List<Book>? {

                    val entities = PMF.get(PMF.chopSetToPaging(user.getTradeList()))
                    List<Book> books =
                            TypeFactory.entitiesToJsonBooks(entities)
                    return books
        }

        @Throws(TwitterException::class)
        @JvmStatic
        fun fetchTwitterFollowers(user: User?) {

            // if refresh is requested.. then do this
                    val friends = TwitterFactory.getTwitFollowerEntities(user)
            // save the twit friends in the system first
                    val friendKeys = PMF.put(friends.values())
            // now setup the relation with user and save user
                    UserManager.saveTwitFriends(user, friendKeys)
            // the friends need to be checked in the system, if they are registered with bex
                    FriendManager.filterFriends(user, friends)
        }

        @Throws(TwitterException::class)
        @JvmStatic
        fun fetchTwitterFriends(user: User?) {

            // if refresh is requested.. then do this
                    val friends = TwitterFactory.getTwitFriendEntities(user)
            // save the twit friends in the system first
                    val friendKeys = PMF.put(friends.values())
            // now setup the relation with user and save user
                    UserManager.saveTwitFriends(user, friendKeys)
            // the friends need to be checked in the system, if they are registered with bex
                    FriendManager.filterFriends(user, friends)
        }

        @JvmStatic
        fun saveUserWithCustomKey(user: User?) {

            // saveUserWithCustomKey user only if brand new.. existing users should use update instead
            //        if (null == PMF.loadUser(user.getScreenName(), user.getTwitterId(), user.getToken(), user.getTokenSecret())){
                    val pm = PMF.get().persistenceManager
            //        pm.setDetachAllOnCommit(true)
                    val tx = pm.currentTransaction()
                    try {
                        tx.begin()
            // set the key to twitter id, so retrieval is easier
                        user.setKey(TypeFactory.createUserKeyWithPrefix(user.getTwitterId()))
                        pm.makePersistent(user)
                        log.info("#saveUserWithCustomKey: " + user)
                        tx.commit()
                    } finally {
                        if (tx.isActive) {
                            tx.rollback()
                        }
                        pm.close()
                    }
            /*
                    } else {
                        log.info("SKIPPED User saveUserWithCustomKey - User already in Database" + user)
                    }
            */
        }

        @JvmStatic
        fun saveTwitFriend(user: User?, friend: TwitFriend?) {

            /*
                    user.getTwitFriends().add(friend.getKey())
                    val keys = (PMF as List<Key>).get().persistenceManager.detachCopyAll(friend.getUsers())
                    keys.add(user.getKey())
                    friend.setUsers(HashSet<Key>(keys))
            */
                    addTwitFriend(user, friend)
                    PMF.makePersistent(friend)
                    PMF.makePersistent(user)
        }

        @JvmStatic
        fun saveBEXFriend(user: User?, friend: BEXFriend?) {

                    addBEXFriend(user, friend)
                    PMF.makePersistent(friend)
                    PMF.makePersistent(user)
        }

        @JvmStatic
        fun saveBEXFriends(user: User?, friends: List<Entity>?) {

                    val keys = PMF.put(friends)
                    user.setBexFriends(HashSet<Key>(keys))
                    PMF.makePersistent(user)
        }

        @JvmStatic
        fun saveTwitFriends(user: User?, friendKeys: List<Key>?) {

                    user.setTwitFriends(HashSet<Key>(friendKeys))
                    PMF.makePersistent(user)
        }

        @JvmStatic
        fun saveTradeList(user: User?, books: List<TradeBook>?) {

                    for (book in books) {
                        addTradeList(user, book)
                        PMF.makePersistent(book)
                    }
                    PMF.makePersistent(user)
        }

        @JvmStatic
        fun saveWishList(user: User?, books: List<WishBook>?) {

                    for (book in books) {
                        addWishList(user, book)
                        PMF.makePersistent(book)
                    }
                    PMF.makePersistent(user)
        }

        @JvmStatic
        fun loadUser(name: String?): User? {

                    val user = null
                    val pm = PMF.get().persistenceManager
                    val query = pm.newQuery(User::class.java, "screenName == nameParam")
                    query.declareParameters("String nameParam")
                    val results = (query as List<User>).execute(name)
                    if (null != results && !results.isEmpty()) {
                        user = results.get(0)
                        log.info("#loadUser: " + user)
                    }
                    pm.close()
                    return user
        }

        @JvmStatic
        fun loadUser(name: String?, token: String?): User? {

                    log.info("#loadUser: " + name)
                    val user = null
                    val pm = PMF.get().persistenceManager
                    val query = pm.newQuery(User::class.java, "screenName == nameParam")
                    query.setFilter("token == tokenParam")
                    query.declareParameters("String nameParam, String tokenParam")
                    val results = (query as List<User>).execute(name, token)
                    if (null != results && !results.isEmpty()) {
                        user = results.get(0)
                        log.info("#loadUser: " + user)
                    }
                    pm.close()
                    return user
        }

        @JvmStatic
        fun loadUser(name: String?, twitterId: String?, token: String?, tokenSecret: String?): User? {

                    log.info("#loadUser: " + name)
                    val user = null
                    val pm = PMF.get().persistenceManager
                    val query = pm.newQuery(User::class.java, "screenName == screenNameParam")
                    query.setFilter("twitterId == twitterIdParam")
                    query.setFilter("token == tokenParam")
                    query.setFilter("tokenSecret == tokenSecretParam")
                    query.declareParameters("String screenNameParam, String twitterIdParam, String tokenParam, String tokenSecretParam")
                    val results = (query as List<User>).executeWithArray(name, twitterId, token, tokenSecret)
                    if (null != results && !results.isEmpty()) {
                        user = results.get(0)
                        log.info("#loadUser: " + user)
                    }
                    pm.close()
                    return user
        }

        @JvmStatic
        fun loadUserWithTwitFriends(user: User?): User? {

                    val pm = PMF.get().persistenceManager
                    val tx = pm.currentTransaction()
                    try {
                        tx.begin()
                            user = pm.getObjectById(User::class.java, user.getKey())
                            log.info("#loadUser: TwitFriends: " + user.getTwitFriends())
                            log.info("#loadUser: TwitFriends size: " + user.getTwitFriends().size)
                        tx.commit()
                    } finally {
                        if (tx.isActive) {
                            tx.rollback()
                        }
                        pm.close()
                    }
                    return user
        }

        @JvmStatic
        fun loadUserWithTwitFriends(name: String?, token: String?): User? {

                    log.info("#loadUser: " + name)
                    val user = null
                    val pm = PMF.get().persistenceManager
                    val query = pm.newQuery(User::class.java, "screenName == nameParam")
                    query.setFilter("token == tokenParam")
                    query.declareParameters("String nameParam, String tokenParam")
                    val results = (query as List<User>).execute(name, token)
                    if (null != results && !results.isEmpty()) {
                        user = results.get(0)
                        pm.retrieveAll(user.getTwitFriends())
                        log.info("#loadUser: " + user)
                    }
                    pm.close()
                    return user
        }

        @JvmStatic
        fun loadUserWithFriends(name: String?, token: String?): User? {

                    log.info("#loadUser: " + name)
                    val user = null
                    val pm = PMF.get().persistenceManager
                    val query = pm.newQuery(User::class.java, "screenName == nameParam")
                    query.setFilter("token == tokenParam")
                    query.declareParameters("String nameParam, String tokenParam")
                    val results = (query as List<User>).execute(name, token)
                    if (null != results && !results.isEmpty()) {
                        user = results.get(0)
                        pm.retrieveAll(user.getTwitFriends())
                        pm.retrieveAll(user.getBexFriends())
                        log.info("#loadUser: " + user)
                    }
                    pm.close()
                    return user
        }

        @JvmStatic
        fun loadUserWithBEXFriends(name: String?, token: String?): User? {

                    log.info("#loadUser: " + name)
                    val user = null
                    val pm = PMF.get().persistenceManager
                    val query = pm.newQuery(User::class.java, "screenName == nameParam")
                    query.setFilter("token == tokenParam")
                    query.declareParameters("String nameParam, String tokenParam")
                    val results = (query as List<User>).execute(name, token)
                    if (null != results && !results.isEmpty()) {
                        user = results.get(0)
                        pm.retrieveAll(user.getBexFriends())
                        log.info("#loadUser: " + user)
                    }
                    pm.close()
                    return user
        }

    }
}
