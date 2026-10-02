package com.androidrocks.bex.server.manager

import com.androidrocks.bex.server.persistent.BEXFriend
import com.androidrocks.bex.server.persistent.TradeMatch
import com.androidrocks.bex.server.persistent.User
import com.androidrocks.bex.server.persistent.WishMatch
import com.google.appengine.api.datastore.DatastoreService
import com.google.appengine.api.datastore.DatastoreServiceFactory
import com.google.appengine.api.datastore.Entity
import com.google.appengine.api.datastore.Key
import javax.jdo.JDOObjectNotFoundException
import javax.jdo.PersistenceManager
import javax.jdo.Transaction
import java.util.ArrayList
import java.util.Collection
import java.util.HashSet
import java.util.List
import java.util.Map
import java.util.Set
import java.util.logging.Logger

class MatchManager private constructor() {
    companion object {
        private val log: Logger = Logger.getLogger(MatchManager::class.java
            .getName())
        @JvmStatic
        fun matchWishList(user: User?): List<WishMatch>? {

                    val wishes = PMF.chopSetToPaging(user.getWishList())
            // create trade list keys
                    val trades = HashSet<Key>((wishes.size))
                    for (wish in wishes) {
                        val key = TypeFactory.createTradeBookKey(wish.name)
                        trades.add (key)
                        log.info("Adding trade key: "+key)
                    }
                    val datastoreService = DatastoreServiceFactory.getDatastoreService()
                    val tradeEntityMap = datastoreService.get(trades)
                    log.info("Got Trade Entities: "+tradeEntityMap.size)
                    val matches = ArrayList<WishMatch>(tradeEntityMap.size)
                    for (entity in tradeEntityMap.values()) {
                        WishMatch match
                        val keys = HashSet<Key>((Collection) entity.getProperty("users"))
                        val userKeyNames = TypeFactory.getKeyNames(keys)
                        val friends = FriendManager.fetchBEXFriends (userKeyNames)
                        try{
                            match = (WishMatch) PMF.loadObjectById(WishMatch::class.java,
                                    TypeFactory.createWishMatchKey(entity.getKey().name))
            // existing match.. add the new owner to existing owners
                            addWishMatchUser(match, user)
            // todo: dont rewrite this, but hacking for now.. should ideally fetch from the match pre populated
            // todo: is this a tradebook, or a whishbook?
                            match.setBook(entity.getKey())
            // set the providers.. these are the users providing the wishbook
            // the users from the book, need to be converted to bex friends and set on match
                            match.setFriends(friends)
                            PMF.makePersistent(match)
                            log.info("Existing Match: Added user: "+match)
                        } catch (JDOObjectNotFoundException e){
            // if brand new match
                            match = WishMatch()
            // todo: is this a wishbook, or a tradebook?
                            match.setBook(entity.getKey())
            // set the match owners
                            addWishMatchUser(match, user)
            // set the providers.. these are the users providing the tradebook
                            match.setFriends(friends)
                            saveWishMatchWithCustomKey(match)
                            log.info("New Match: Added user: "+match)
                        }
                        matches.add(match)
                    }
                    return matches
        }

        @JvmStatic
        fun matchTradeList(user: User?): List<TradeMatch>? {

                    val trades = PMF.chopSetToPaging(user.getTradeList())
                    val wishes = HashSet<Key>((trades.size))
                    for (trade in trades) {
                        val key = TypeFactory.createWishBookKey(trade.name)
                        wishes.add (key)
                        log.info("Adding wish key: "+key)
                    }
                    val datastoreService = DatastoreServiceFactory.getDatastoreService()
                    val entityMap = datastoreService.get(wishes)
                    log.info("Got Wish Entities: "+entityMap.size)
                    val matches = ArrayList<TradeMatch>(entityMap.size)
                    for (entity in entityMap.values()) {
                        TradeMatch match
                        val keys = HashSet<Key>((Collection) entity.getProperty("users"))
                        val userKeyNames = TypeFactory.getKeyNames(keys)
                        val friends = FriendManager.fetchBEXFriends (userKeyNames)
                        try{
                            match = (TradeMatch) PMF.loadObjectById(TradeMatch::class.java,
                                    TypeFactory.createTradeMatchKey(entity.getKey().name))
            // existing match.. add the new owner to existing owners
                            addTradeMatchUser(match, user)
            // todo: dont rewrite this, but hacking for now.. should ideally fetch from the match pre populated
            // todo: is this a tradebook, or a whishbook?
                            match.setBook(entity.getKey())
            // set the providers.. these are the users providing the wishbook
                            match.setFriends(friends)
                            PMF.makePersistent(match)
                            log.info("Existing Match: Added user: "+match)
                        } catch (JDOObjectNotFoundException e){
            // if brand new match
                            match = TradeMatch()
            // todo: is this a tradebook, or a whishbook?
                            match.setBook(entity.getKey())
            // set the match owners
                            addTradeMatchUser(match, user)
            // set the providers.. these are the users providing the wishbook
                            match.setFriends(friends)
                            saveTradeMatchWithCustomKey(match)
                            log.info("New Match: Added user: "+match)
                        }
                        matches.add(match)
                    }
                    return matches
        }

        @JvmStatic
        fun saveWishMatchWithCustomKey(match: WishMatch?) {

                    val pm = PMF.get().persistenceManager
                    val tx = pm.currentTransaction()
                    try {
                        tx.begin()
                        match.setKey(TypeFactory.createWishMatchKey(match.getBook().name))
                        pm.makePersistent(match)
                        log.info("#saveWishMatchWithCustomKey: " + match)
                        tx.commit()
                    } finally {
                        if (tx.isActive) {
                            tx.rollback()
                        }
                        pm.close()
                    }
        }

        @JvmStatic
        fun saveTradeMatchWithCustomKey(match: TradeMatch?) {

                    val pm = PMF.get().persistenceManager
                    val tx = pm.currentTransaction()
                    try {
                        tx.begin()
                        match.setKey(TypeFactory.createTradeMatchKey(match.getBook().name))
                        pm.makePersistent(match)
                        log.info("#saveTradeMatchWithCustomKey: " + match)
                        tx.commit()
                    } finally {
                        if (tx.isActive) {
                            tx.rollback()
                        }
                        pm.close()
                    }
        }

    }
}
