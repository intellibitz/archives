package com.androidrocks.bex.server.manager;

import com.androidrocks.bex.client.json.Book;
import com.androidrocks.bex.client.json.Friend;
import com.androidrocks.bex.client.json.Match;
import com.androidrocks.bex.server.persistent.BEXFriend;
import com.androidrocks.bex.server.persistent.TradeBook;
import com.androidrocks.bex.server.persistent.TradeMatch;
import com.androidrocks.bex.server.persistent.TwitFriend;
import com.androidrocks.bex.server.persistent.WishBook;
import com.androidrocks.bex.server.persistent.WishMatch;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import twitter4j.User;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.logging.Logger;

public final class TypeFactory {

    private static final Logger log = Logger.getLogger(TypeFactory.class
            .getName());

    public static final Type TWITTER4J_USER_LIST_TYPE = new TypeToken<List<User>>() {
    }.getType();

    public static final Type MATCH_LIST_TYPE = new TypeToken<List<Match>>() {
    }.getType();

    public static final Type FRIEND_LIST_TYPE = new TypeToken<List<Friend>>() {
    }.getType();

    public static final Type BOOK_LIST_TYPE = new TypeToken<List<Book>>() {
    }.getType();

    public static final Type BEXFRIEND_LIST_DATA_TYPE =
            new TypeToken<List<BEXFriend>>() {
    }.getType();

    public static final Type TWITFRIEND_LIST_DATA_TYPE =
            new TypeToken<List<TwitFriend>>() {
    }.getType();

    public static final Type WISH_LIST_DATA_TYPE =
            new TypeToken<List<WishBook>>() {
    }.getType();

    public static final Type TRADE_LIST_DATA_TYPE =
            new TypeToken<List<TradeBook>>() {
    }.getType();

    static final String KEY_PREFIX = "T";

    public static String prefixKeyId(String id) {
        return KEY_PREFIX + id;
    }

    public static String prefixKeyId(long id) {
        return KEY_PREFIX + id;
    }

    private TypeFactory() {
    }

    public static List<com.androidrocks.bex.client.json.Friend> bexFriendsToJsonFriends(List<BEXFriend> users) {
        return transformList(users, BEXFRIEND_LIST_DATA_TYPE, FRIEND_LIST_TYPE);
    }

    public static List<com.androidrocks.bex.client.json.Friend> twitFriendsToJsonFriends(List<TwitFriend> users) {
        return transformList(users, TWITFRIEND_LIST_DATA_TYPE, FRIEND_LIST_TYPE);
    }

    public static Map<Key, Entity> jUsersToTwitFriendEntities(List<User> users) {
        Map<Key, Entity> entities = new HashMap<Key, Entity>(users.size());
        for (User user : users) {
            Entity entity = jUserToTwitFriendEntity (user);
            entities.put(entity.getKey(), entity);
        }
        log.info("jUsersToTwitFriendEntities: "+entities.size());
        return entities;
    }

    public static List<TwitFriend> twitter4jUsersToFriends(List<User> users) {
// this doesn't work on server
        /*
        * Uncaught exception from servlet
java.lang.SecurityException: java.lang.IllegalAccessException: Reflection is not allowed on static final long java.text.SimpleDateFormat.serialVersionUID
*/
//        return transformList(users, TWITTER4J_USER_LIST_TYPE, TWITTER_USER_LIST_TYPE);

// do the manual copy to avoid gae limitation
        List<TwitFriend> friends = new ArrayList<TwitFriend>(users.size());
        for (User user : users) {
            TwitFriend friend = jUserToTwitFriend(user);
            friends.add(friend);
        }
        log.info("twitter4jUsersToFriends: "+friends.size());
        return friends;
    }

    public static Entity jUserToTwitFriendEntity(User user) {
        Entity entity = new Entity(TwitFriend.class.getSimpleName(), prefixKeyId(user.getId()));
        entity.setProperty("id", user.getId());
        entity.setProperty("screenName", user.getScreenName());
        entity.setProperty("name", user.getName());
        entity.setProperty("description", user.getDescription());
        entity.setProperty("followersCount", user.getFollowersCount());
        entity.setProperty("location", user.getLocation());
        entity.setProperty("profileImageUrl", user.getProfileImageURL().toString());
        entity.setProperty("isProtected", user.isProtected());
        return entity;
    }

    public static Entity twitEntityToBEXEntity(Entity twit) {
        Entity entity = new Entity(BEXFriend.class.getSimpleName(), twit.getKey().getName());
        entity.setProperty("id", twit.getProperty("id"));
        entity.setProperty("screenName", twit.getProperty("screenName"));
        entity.setProperty("name", twit.getProperty("name"));
        entity.setProperty("description", twit.getProperty("description"));
        entity.setProperty("followersCount", twit.getProperty("followersCount"));
        entity.setProperty("location", twit.getProperty("location"));
        entity.setProperty("profileImageUrl", twit.getProperty("profileImageUrl"));
        entity.setProperty("isProtected", twit.getProperty("isProtected"));
        return entity;
    }

    public static Friend entityToJsonFriend(Entity entity) {
        Friend friend = new Friend();
        friend.setDescription((String) entity.getProperty("description"));
        friend.setId(String.valueOf(entity.getProperty("id")));
        friend.setLocation((String) entity.getProperty("location"));
        friend.setName((String) entity.getProperty("name"));
        friend.setProfileImageUrl((String) entity.getProperty("profileImageUrl"));
        friend.setScreenName((String) entity.getProperty("screenName"));
        return friend;
    }

    public static BEXFriend twitFriendToBEXFriend (TwitFriend friend){
        log.info("#twitFriendToBEXFriend: "+friend.getKey());
// the TwitFriend can be brand new or existing.. handle both cases
        BEXFriend bexFriend = new BEXFriend();
        if (null != friend.getKey()){
// todo: is this the right thing to do? What if BEXFriend does not exist yet.. REVISIT!!
            bexFriend.setKey(createBEXFriendKey(friend.getKey().getName()));
        }
        bexFriend.setDescription(friend.getDescription());
        bexFriend.setFollowersCount(friend.getFollowersCount());
        bexFriend.setId(friend.getId());
        bexFriend.setLocation(friend.getLocation());
        bexFriend.setName(friend.getName());
        bexFriend.setProfileImageUrl(friend.getProfileImageUrl());
        bexFriend.setProtected(friend.isProtected());
        bexFriend.setScreenName(friend.getScreenName());
        bexFriend.setUrl(friend.getUrl());
        bexFriend.setUsers(new HashSet<Key>(friend.getUsers()));
        return bexFriend;
    }

    public static TwitFriend jUserToTwitFriend(User user) {
        TwitFriend friend = new TwitFriend();
        friend.setId(user.getId());
        friend.setScreenName(user.getScreenName());
        friend.setName(user.getName());
        friend.setDescription(user.getDescription());
        friend.setFollowersCount(user.getFollowersCount());
        friend.setLocation(user.getLocation());
        friend.setProfileImageUrl(user.getProfileImageURL().toString());
        friend.setProtected(user.isProtected());
        return friend;
    }

    public static List<Friend> entitiesToJsonFriends(Map<Key, Entity> entities) {
        Collection<Entity> entityCollection = entities.values();
        return entitiesToJsonFriends(entityCollection);
    }

    public static List<Friend> entitiesToJsonFriends(Collection<Entity> entityCollection) {
        List<Friend> friends = new ArrayList<Friend>(entityCollection.size());
        for (Entity entity : entityCollection) {
            Friend friend = entityToJsonFriend(entity);
            friends.add(friend);
        }
        return friends;
    }

    public static List<Book> entitiesToJsonBooks(Map<Key, Entity> entities) {
        List<Book> books = new ArrayList<Book>(entities.size());
        for (Entity entity : entities.values()) {
            Book book = entityToJsonBook(entity);
            books.add(book);
        }
        return books;
    }

    public static Book entityToJsonBook(Entity entity) {
        Book book = new Book();
        book.setId((String) entity.getProperty("id"));
        book.setAuthors(new ArrayList<String>((Collection)entity.getProperty("authors")));
        book.setDescription((String) entity.getProperty("description"));
        book.setDetailsUrl((String) entity.getProperty("detailsUrl"));
        book.setEan((String) entity.getProperty("ean"));
        book.setImage((String) entity.getProperty("image"));
        book.setIsbn((String) entity.getProperty("isbn"));
        book.setLastModified((Date) entity.getProperty("lastModified"));
        book.setPages(Integer.valueOf(entity.getProperty("pages").toString()));
        book.setPublicationDate((Date) entity.getProperty("publicationDate"));
        book.setPublisher((String) entity.getProperty("publisher"));
        book.setStore((String) entity.getProperty("store"));
        book.setTitle((String) entity.getProperty("title"));
        return book;
    }

    public static Key createWishBookKey (String keyname){
        return KeyFactory.createKey(WishBook.class.getSimpleName(), keyname);
    }

    public static Key createTradeBookKey (String keyname){
        return KeyFactory.createKey(TradeBook.class.getSimpleName(), keyname);        
    }

    public static Key createBEXFriendKey (String keyname){
        return KeyFactory.createKey(BEXFriend.class.getSimpleName(), keyname);
    }

    public static Key createTwitFriendKey (String keyname){
        return KeyFactory.createKey(TwitFriend.class.getSimpleName(), keyname);
    }

    public static Key createUserKey (String keyname){
        return KeyFactory.createKey(User.class.getSimpleName(), keyname);        
    }

    public static Key createWishMatchKey (String keyname){
        return KeyFactory.createKey(WishMatch.class.getSimpleName(), keyname);
    }

    public static Key createTradeMatchKey (String keyname){
        return KeyFactory.createKey(TradeMatch.class.getSimpleName(), keyname);
    }

    public static List transformList (List src, Type srcType, Type destType){
        return fromJson(toJson(src, srcType), destType);
    }

    public static String toJson (List src, Type srcType){
        return new Gson().toJson(src, srcType);
    }

    public static List fromJson (String src, Type srcType){
        return new Gson().fromJson(src, srcType);
    }

    public static List<Match> wishMatchJson(List<WishMatch> matches) {
        List<Match> jsonMatches = new ArrayList<Match>(matches.size());
        for (WishMatch match : matches) {
            Set<Key> keys = match.getFriends();
            List<Friend> friends = FriendManager.fetchFriends(keys);
            Key key = match.getBook();
            TradeBook book = (TradeBook) PMF.loadObjectById(TradeBook.class, key);
            for (Friend friend : friends) {
                Match m = new Match();
                m.setId(book.getId());
                m.setFriendId(friend.getId());
                m.setTitle(book.getTitle());
                m.setDescription(book.getDescription());
                m.setImage(book.getImage());
                m.setLocation(friend.getLocation());
                m.setName(friend.getName());
                m.setProfileImageUrl(friend.getProfileImageUrl());
                m.setPublisher(book.getPublisher());
                m.setScreenName(friend.getScreenName());
                m.setTitle(book.getTitle());
                jsonMatches.add(m);
                log.info("Adding Match: "+m);
            }
        }
        return jsonMatches;
    }
    public static List<Match> tradeMatchJson(List<TradeMatch> matches) {
        List<Match> jsonMatches = new ArrayList<Match>(matches.size());
        for (TradeMatch match : matches) {
            Set<Key> keys = match.getFriends();
            List<Friend> friends = FriendManager.fetchFriends(keys);
            Key key = match.getBook();
            WishBook book = (WishBook) PMF.loadObjectById(WishBook.class, key);
            for (Friend friend : friends) {
                Match m = new Match();
                m.setId(book.getId());
                m.setFriendId(friend.getId());
                m.setTitle(book.getTitle());
                m.setDescription(book.getDescription());
                m.setImage(book.getImage());
                m.setLocation(friend.getLocation());
                m.setName(friend.getName());
                m.setProfileImageUrl(friend.getProfileImageUrl());
                m.setPublisher(book.getPublisher());
                m.setScreenName(friend.getScreenName());
                m.setTitle(book.getTitle());
                jsonMatches.add(m);
                log.info("Adding Match: "+m);
            }
        }
        return jsonMatches;
    }

    public static Set<String> getKeyNames(Set<Key> keys) {
        Set<String> keyNames = new HashSet<String>(keys.size());
        for (Key key : keys) {
            keyNames.add(key.getName());
        }
        return keyNames;
    }

    public static Key createBEXFriendKeyWithPrefix(long id) {
        return createBEXFriendKey(prefixKeyId(id));
    }

    public static Key createTwitFriendKeyWithPrefix(long id) {
        return createTwitFriendKey(prefixKeyId(id));
    }

    public static Key createWishBookKeyWithPrefix(String id) {
        return createWishBookKey(prefixKeyId(id));
    }

    public static Key createTradeBookKeyWithPrefix(String id) {
        return createTradeBookKey(prefixKeyId(id));
    }

    public static Key createUserKeyWithPrefix(String twitterId) {
        return createUserKey(prefixKeyId(twitterId));
    }
}