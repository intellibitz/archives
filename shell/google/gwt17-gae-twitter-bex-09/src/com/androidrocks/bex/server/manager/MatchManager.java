package com.androidrocks.bex.server.manager;

import com.androidrocks.bex.server.persistent.BEXFriend;
import com.androidrocks.bex.server.persistent.TradeMatch;
import com.androidrocks.bex.server.persistent.User;
import com.androidrocks.bex.server.persistent.WishMatch;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Transaction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public final class MatchManager {

    private static final Logger log = Logger.getLogger(MatchManager.class
            .getName());

    private MatchManager() {
    }

    static void addWishMatchUser (WishMatch match, User user){
        match.getUsers().add(user.getKey());
    }

    static void removeWishMatchUser (WishMatch match, User user){
        match.getUsers().remove(user.getKey());
    }

    static void addWishMatchFriend (WishMatch match, BEXFriend friend){
        match.getFriends().add(friend.getKey());
    }

    static void removeWishMatchFriend (WishMatch match, BEXFriend friend){
        match.getFriends().remove(friend.getKey());
    }

    static void addTradeMatchUser (TradeMatch match, User user){
        match.getUsers().add(user.getKey());
    }

    static void removeTradeMatchUser (TradeMatch match, User user){
        match.getUsers().remove(user.getKey());
    }

    static void addTradeMatchFriend (TradeMatch match, BEXFriend friend){
        match.getFriends().add(friend.getKey());
    }

    static void removeTradeMatchFriend (TradeMatch match, BEXFriend friend){
        match.getFriends().remove(friend.getKey());
    }

    public static List<WishMatch> matchWishList(User user) {
        Set<Key> wishes = PMF.chopSetToPaging(user.getWishList());
// create trade list keys
        Set<Key> trades = new HashSet<Key>((wishes.size()));
        for (Key wish : wishes) {
            Key key = TypeFactory.createTradeBookKey(wish.getName());
            trades.add (key);
            log.info("Adding trade key: "+key);
        }
        DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
        Map<Key, Entity> tradeEntityMap = datastoreService.get(trades);
        log.info("Got Trade Entities: "+tradeEntityMap.size());
        List<WishMatch> matches = new ArrayList<WishMatch>(tradeEntityMap.size());
        for (Entity entity : tradeEntityMap.values()) {
            WishMatch match;
            HashSet<Key> keys = new HashSet<Key>((Collection) entity.getProperty("users"));
            Set<String> userKeyNames = TypeFactory.getKeyNames(keys);
            Set<Key> friends = FriendManager.fetchBEXFriends (userKeyNames);
            try{
                match = (WishMatch) PMF.loadObjectById(WishMatch.class,
                        TypeFactory.createWishMatchKey(entity.getKey().getName()));
// existing match.. add the new owner to existing owners
                addWishMatchUser(match, user);
// todo: dont rewrite this, but hacking for now.. should ideally fetch from the match pre populated
// todo: is this a tradebook, or a whishbook?
                match.setBook(entity.getKey());
// set the providers.. these are the users providing the wishbook
// the users from the book, need to be converted to bex friends and set on match               
                match.setFriends(friends);
                PMF.makePersistent(match);
                log.info("Existing Match: Added user: "+match);
            } catch (JDOObjectNotFoundException e){
// if brand new match
                match = new WishMatch();
// todo: is this a wishbook, or a tradebook?
                match.setBook(entity.getKey());
// set the match owners
                addWishMatchUser(match, user);
// set the providers.. these are the users providing the tradebook
                match.setFriends(friends);
                saveWishMatchWithCustomKey(match);
                log.info("New Match: Added user: "+match);
            }
            matches.add(match);
        }
        return matches;
    }


    public static List<TradeMatch> matchTradeList(User user) {
        Set<Key> trades = PMF.chopSetToPaging(user.getTradeList());
        Set<Key> wishes = new HashSet<Key>((trades.size()));
        for (Key trade : trades) {
            Key key = TypeFactory.createWishBookKey(trade.getName());
            wishes.add (key);
            log.info("Adding wish key: "+key);
        }
        DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
        Map<Key, Entity> entityMap = datastoreService.get(wishes);
        log.info("Got Wish Entities: "+entityMap.size());
        List<TradeMatch> matches = new ArrayList<TradeMatch>(entityMap.size());
        for (Entity entity : entityMap.values()) {
            TradeMatch match;
            HashSet<Key> keys = new HashSet<Key>((Collection) entity.getProperty("users"));
            Set<String> userKeyNames = TypeFactory.getKeyNames(keys);
            Set<Key> friends = FriendManager.fetchBEXFriends (userKeyNames);
            try{
                match = (TradeMatch) PMF.loadObjectById(TradeMatch.class,
                        TypeFactory.createTradeMatchKey(entity.getKey().getName()));
// existing match.. add the new owner to existing owners
                addTradeMatchUser(match, user);
// todo: dont rewrite this, but hacking for now.. should ideally fetch from the match pre populated               
// todo: is this a tradebook, or a whishbook?
                match.setBook(entity.getKey());
// set the providers.. these are the users providing the wishbook
                match.setFriends(friends);
                PMF.makePersistent(match);
                log.info("Existing Match: Added user: "+match);
            } catch (JDOObjectNotFoundException e){
// if brand new match
                match = new TradeMatch();
// todo: is this a tradebook, or a whishbook?
                match.setBook(entity.getKey());
// set the match owners
                addTradeMatchUser(match, user);
// set the providers.. these are the users providing the wishbook
                match.setFriends(friends);
                saveTradeMatchWithCustomKey(match);
                log.info("New Match: Added user: "+match);
            }
            matches.add(match);
        }
        return matches;
    }

    public static void saveWishMatchWithCustomKey(WishMatch match) {
        PersistenceManager pm = PMF.get().getPersistenceManager();
        Transaction tx = pm.currentTransaction();
        try {
            tx.begin();
            match.setKey(TypeFactory.createWishMatchKey(match.getBook().getName()));
            pm.makePersistent(match);
            log.info("#saveWishMatchWithCustomKey: " + match);
            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            pm.close();
        }
    }

    public static void saveTradeMatchWithCustomKey(TradeMatch match) {
        PersistenceManager pm = PMF.get().getPersistenceManager();
        Transaction tx = pm.currentTransaction();
        try {
            tx.begin();
            match.setKey(TypeFactory.createTradeMatchKey(match.getBook().getName()));
            pm.makePersistent(match);
            log.info("#saveTradeMatchWithCustomKey: " + match);
            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            pm.close();
        }
    }

}