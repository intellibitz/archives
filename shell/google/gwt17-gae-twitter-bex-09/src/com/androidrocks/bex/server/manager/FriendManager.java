package com.androidrocks.bex.server.manager;

import com.androidrocks.bex.client.json.Friend;
import com.androidrocks.bex.server.persistent.BEXFriend;
import com.androidrocks.bex.server.persistent.TwitFriend;
import com.androidrocks.bex.server.persistent.User;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;

import javax.jdo.PersistenceManager;
import javax.jdo.Transaction;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public final class FriendManager {

    private static final Logger log = Logger.getLogger(FriendManager.class
            .getName());

    private FriendManager() {
    }

    public static void saveBEXFriends(List<BEXFriend> friends) {
        for (BEXFriend friend : friends) {
            saveBEXFriendWithCustomKey(friend);
        }
    }

    public static void saveTwitFriends(List<TwitFriend> friends) {
// do a batch save.. sequence save will throw an exception       
        for (TwitFriend friend : friends) {
            saveTwitFriendWithCustomKey(friend);
        }
    }

    public static void saveBEXFriendWithCustomKey(BEXFriend friend) {
        PersistenceManager pm = PMF.get().getPersistenceManager();
        Transaction tx = pm.currentTransaction();
        try {
            tx.begin();
            friend.setKey(TypeFactory.createBEXFriendKeyWithPrefix(friend.getId()));
            pm.makePersistent(friend);
            log.info("#saveBEXFriendWithCustomKey: " + friend);
            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            pm.close();
        }
    }

    public static void saveTwitFriendWithCustomKey(TwitFriend friend) {
        PersistenceManager pm = PMF.get().getPersistenceManager();
        Transaction tx = pm.currentTransaction();
        try {
            tx.begin();
            friend.setKey(TypeFactory.createTwitFriendKeyWithPrefix(friend.getId()));
            pm.makePersistent(friend);
            log.info("#saveTwitFriendWithCustomKey: " + friend);
            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            pm.close();
        }
    }

    public static List<Friend> fetchBEXFriends() {
        Query query = new Query(BEXFriend.class.getSimpleName());
        return TypeFactory.entitiesToJsonFriends(PMF.query (query));
    }

    public static List<Friend> fetchFriends(Set<Key> keys){
        return TypeFactory.entitiesToJsonFriends(PMF.get(keys));
    }

    /**
     * Sets up BEXFriends for user
     * NOTE: Do not set up the TwitFriends relations, since its already set
     * @param user
     * @param twitFriends
     */
    public static void filterFriends(User user, Map<Key, Entity> twitFriends) {
        Set<Key> twitFriendKeys = user.getTwitFriends();
        Set<Key> userKeys = new HashSet<Key>((twitFriends.size()));
        for (Key friend : twitFriendKeys) {
            Key key = TypeFactory.createUserKey(friend.getName());
            userKeys.add(key);
            log.info("Adding user key: " + key);
        }

        DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
        Map<Key, Entity> userEntityMap = datastoreService.get(userKeys);
        log.info("Got System Users: "+userEntityMap.size());
        userKeys = userEntityMap.keySet();
        Set<String> userKeyNames = TypeFactory.getKeyNames(userKeys);
//        List<TwitFriend> twitFriends = new ArrayList<TwitFriend>();
//        List<BEXFriend> bexFriends = new ArrayList<BEXFriend>();
        List<Entity> bexFriends = new ArrayList<Entity>();

        for (Key twitFriendKey : twitFriendKeys) {
            log.info("for Friend: " + twitFriendKey);
// compare the key names.. only the names can match (not the entire key, as class are different)
            if (userKeyNames.contains(twitFriendKey.getName())) {
                log.info("User name matching Friend name: "+ twitFriendKey.getName());
//                BEXFriend bex;
                Entity bex;
// generate a new key for BEXFriend, since they are different from TwitFriend!!
                try{
                    bex = PMF.get(TypeFactory.createBEXFriendKey(twitFriendKey.getName()));
/*
                    bex = (BEXFriend) PMF.loadObjectById
                            (BEXFriend.class, TypeFactory.createBEXFriendKey(twitFriendKey.getName()));
*/

                } catch (EntityNotFoundException e){
// bex twitFriendKey might not be created yet, so create a new one
// TODO: TWIT FRIEND ENTITY IS AVAILABLE, GET IT FROM twitFriends list param coming in
// no need for an unnecessary data hit here                   
                    Entity twit = twitFriends.get(twitFriendKey);
//                    TwitFriend twit = (TwitFriend) PMF.loadObjectById(TwitFriend.class, twitFriendKey);
//                    bex = TypeFactory.twitFriendToBEXFriend(twit);
                    bex = TypeFactory.twitEntityToBEXEntity(twit);
                }
                log.info("adding as BEXFriend: " + bex);
// defer the saving to batch saving later
//                UserManager.saveBEXFriend(user, bex);
                bexFriends.add(bex);
            } else {
// NOT REQUIRED.. SINCE TWIT FRIEND RELATIONS ARE ALREADY SET
                log.info("User name NOT matching Friend name: "+ twitFriendKey.getName());
//                TwitFriend twit = (TwitFriend) PMF.loadObjectById(TwitFriend.class, twitFriendKey);
//                log.info("adding as TwitFriend: " + twit);
// defer the saving to batch saving later
//                UserManager.saveTwitFriend(user, twit);
//                twitFriends.add(twit);
            }
        }
        UserManager.saveBEXFriends(user, bexFriends);
//        never have to do this, coz the twit twitFriends would have been saved already, when fetching
//        UserManager.saveTwitFriends(user, twitFriends);
        log.info("#fetchTwitterFriends: BEXFriends count: " + user.getBexFriends().size());
        log.info("#fetchTwitterFriends: TwitterFriends count: " + user.getTwitFriends().size());
    }

    public static Set<Key> fetchBEXFriends(Set<String> userKeyNames) {
        Set<Key> keys = new HashSet<Key>(userKeyNames.size());
        for (String userKeyName : userKeyNames) {
            keys.add(TypeFactory.createBEXFriendKey(userKeyName));
        }
        Map<Key, Entity> entities = PMF.get(keys);
        return entities.keySet();
    }
}