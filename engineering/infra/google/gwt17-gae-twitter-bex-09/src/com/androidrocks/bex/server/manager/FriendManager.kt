package com.androidrocks.bex.server.manager

import com.androidrocks.bex.client.json.Friend
import com.androidrocks.bex.server.persistent.BEXFriend
import com.androidrocks.bex.server.persistent.TwitFriend
import com.androidrocks.bex.server.persistent.User
import com.google.appengine.api.datastore.DatastoreService
import com.google.appengine.api.datastore.DatastoreServiceFactory
import com.google.appengine.api.datastore.Entity
import com.google.appengine.api.datastore.EntityNotFoundException
import com.google.appengine.api.datastore.Key
import com.google.appengine.api.datastore.Query
import javax.jdo.PersistenceManager
import javax.jdo.Transaction
import java.util.ArrayList
import java.util.HashSet
import java.util.List
import java.util.Map
import java.util.Set
import java.util.logging.Logger

class FriendManager private constructor() {
    companion object {
        private val log: Logger = Logger.getLogger(FriendManager::class.java
            .getName())
        @JvmStatic
        fun saveBEXFriends(friends: List<BEXFriend>?) {

                    for (friend in friends) {
                        saveBEXFriendWithCustomKey(friend)
                    }
        }

        @JvmStatic
        fun saveTwitFriends(friends: List<TwitFriend>?) {

            // do a batch save.. sequence save will throw an exception
                    for (friend in friends) {
                        saveTwitFriendWithCustomKey(friend)
                    }
        }

        @JvmStatic
        fun saveBEXFriendWithCustomKey(friend: BEXFriend?) {

                    val pm = PMF.get().persistenceManager
                    val tx = pm.currentTransaction()
                    try {
                        tx.begin()
                        friend.setKey(TypeFactory.createBEXFriendKeyWithPrefix(friend.getId()))
                        pm.makePersistent(friend)
                        log.info("#saveBEXFriendWithCustomKey: " + friend)
                        tx.commit()
                    } finally {
                        if (tx.isActive) {
                            tx.rollback()
                        }
                        pm.close()
                    }
        }

        @JvmStatic
        fun saveTwitFriendWithCustomKey(friend: TwitFriend?) {

                    val pm = PMF.get().persistenceManager
                    val tx = pm.currentTransaction()
                    try {
                        tx.begin()
                        friend.setKey(TypeFactory.createTwitFriendKeyWithPrefix(friend.getId()))
                        pm.makePersistent(friend)
                        log.info("#saveTwitFriendWithCustomKey: " + friend)
                        tx.commit()
                    } finally {
                        if (tx.isActive) {
                            tx.rollback()
                        }
                        pm.close()
                    }
        }

        @JvmStatic
        fun fetchBEXFriends(): List<Friend>? {

                    val query = Query(BEXFriend::class.java.getSimpleName())
                    return TypeFactory.entitiesToJsonFriends(PMF.query (query))
        }

        @JvmStatic
        fun fetchFriends(keys: Set<Key>?): List<Friend>? {

                    return TypeFactory.entitiesToJsonFriends(PMF.get(keys))
        }

        @JvmStatic
        fun filterFriends(user: User?, twitFriends: Map<Key, Entity>?) {

                    val twitFriendKeys = user.getTwitFriends()
                    val userKeys = HashSet<Key>((twitFriends.size))
                    for (friend in twitFriendKeys) {
                        val key = TypeFactory.createUserKey(friend.name)
                        userKeys.add(key)
                        log.info("Adding user key: " + key)
                    }

                    val datastoreService = DatastoreServiceFactory.getDatastoreService()
                    val userEntityMap = datastoreService.get(userKeys)
                    log.info("Got System Users: "+userEntityMap.size)
                    userKeys = userEntityMap.keySet()
                    val userKeyNames = TypeFactory.getKeyNames(userKeys)
            //        List<TwitFriend> twitFriends = ArrayList()
            //        List<BEXFriend> bexFriends = ArrayList()
                    val bexFriends = ArrayList()

                    for (twitFriendKey in twitFriendKeys) {
                        log.info("for Friend: " + twitFriendKey)
            // compare the key names.. only the names can match (not the entire key, as class are different)
                        if (userKeyNames.contains(twitFriendKey.name)) {
                            log.info("User name matching Friend name: "+ twitFriendKey.name)
            //                BEXFriend bex
                            Entity bex
            // generate a new key for BEXFriend, since they are different from TwitFriend!!
                            try{
                                bex = PMF.get(TypeFactory.createBEXFriendKey(twitFriendKey.name))
            /*
                                bex = (BEXFriend) PMF.loadObjectById
                                        (BEXFriend::class.java, TypeFactory.createBEXFriendKey(twitFriendKey.name))
            */

                            } catch (EntityNotFoundException e){
            // bex twitFriendKey might not be created yet, so create a new one
            // TODO: TWIT FRIEND ENTITY IS AVAILABLE, GET IT FROM twitFriends list param coming in
            // no need for an unnecessary data hit here
                                val twit = twitFriends.get(twitFriendKey)
            //                    TwitFriend twit = (TwitFriend) PMF.loadObjectById(TwitFriend::class.java, twitFriendKey)
            //                    bex = TypeFactory.twitFriendToBEXFriend(twit)
                                bex = TypeFactory.twitEntityToBEXEntity(twit)
                            }
                            log.info("adding as BEXFriend: " + bex)
            // defer the saving to batch saving later
            //                UserManager.saveBEXFriend(user, bex)
                            bexFriends.add(bex)
                        } else {
            // NOT REQUIRED.. SINCE TWIT FRIEND RELATIONS ARE ALREADY SET
                            log.info("User name NOT matching Friend name: "+ twitFriendKey.name)
            //                TwitFriend twit = (TwitFriend) PMF.loadObjectById(TwitFriend::class.java, twitFriendKey)
            //                log.info("adding as TwitFriend: " + twit)
            // defer the saving to batch saving later
            //                UserManager.saveTwitFriend(user, twit)
            //                twitFriends.add(twit)
                        }
                    }
                    UserManager.saveBEXFriends(user, bexFriends)
            //        never have to do this, coz the twit twitFriends would have been saved already, when fetching
            //        UserManager.saveTwitFriends(user, twitFriends)
                    log.info("#fetchTwitterFriends: BEXFriends count: " + user.getBexFriends().size)
                    log.info("#fetchTwitterFriends: TwitterFriends count: " + user.getTwitFriends().size)
        }

        @JvmStatic
        fun fetchBEXFriends(userKeyNames: Set<String>?): Set<Key>? {

                    val keys = HashSet<Key>(userKeyNames.size)
                    for (userKeyName in userKeyNames) {
                        keys.add(TypeFactory.createBEXFriendKey(userKeyName))
                    }
                    val entities = PMF.get(keys)
                    return entities.keySet()
        }

    }
}
