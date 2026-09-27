package com.androidrocks.bex.server.manager;

import com.androidrocks.bex.server.persistent.User;
import com.androidrocks.bex.server.persistent.UserNotFoundException;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.http.AccessToken;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public final class TwitterFactory {

    private static final Logger log = Logger.getLogger(TwitterFactory.class
            .getName());

    static final String KEY = "DAWQiVMAP98j7BxAD55sw";
    static final String SECRET = "M1Ws4JjOC78YJwQLaAwhTXYCdDcMslEnVgwcnOC6w4";

    static final String PUBLISH_BOOK_MESSAGE = "Hey, I want to exchange Books in #BooksEX. Join me if interested. ";
    static final String INVITE_MESSAGE = "Hey, come join me in #BooksEX. Why waste Books? Exchange them! with me. http://books-ex.appspot.com #booksex";
    static final String MATCH_MESSAGE_SUFFIX = " #booksex #match";
    static final String PUBLISH_MESSAGE_SUFFIX = " #booksex #publish";

    private TwitterFactory() {
    }

    public static Twitter getTwitter() {
        Twitter twitter = new Twitter();
        twitter.setOAuthConsumer(KEY, SECRET);
        return twitter;
    }

    public static Twitter getTwitter(String name, String token) throws UserNotFoundException {
        Twitter twitter = getTwitter();
        twitter.setOAuthAccessToken(loadAccessToken(name, token));
        return twitter;
    }

    public static Twitter getTwitter(User user){
        Twitter twitter = getTwitter();
        twitter.setOAuthAccessToken(loadAccessToken(user));
        return twitter;
    }

    public static Map<Key,Entity> getTwitFriendEntities(User user) throws TwitterException {
        Twitter twitter = getTwitter(user);
// get following
        List<twitter4j.User> users = twitter.getFriendsStatuses();
// get followers
        users.addAll(twitter.getFollowersStatuses());
        log.info("#getFriendsStatuses: "+users.size());
        return TypeFactory.jUsersToTwitFriendEntities(users);
    }

    public static Map<Key, Entity> getTwitFollowerEntities(User user) throws TwitterException {
        Twitter twitter = getTwitter(user);
// get followers
        List<twitter4j.User> users = twitter.getFollowersStatuses();
        log.info("#getFollowerStatuses: "+users.size());
        return TypeFactory.jUsersToTwitFriendEntities(users);
    }

    public static AccessToken loadAccessToken(User user){
        return new AccessToken(user.getToken(), user.getTokenSecret());
    }

    public static AccessToken loadAccessToken(String name, String token) throws UserNotFoundException {
        User user = UserManager.loadUser(name, token);
        if (null == user){
            throw new UserNotFoundException("User not found: "+name);
        }
        return new AccessToken(token, user.getTokenSecret());
    }

    public static void directMessage (String name, String token, String friend, String msg) throws UserNotFoundException, TwitterException {
        Twitter twitter = getTwitter(name, token);
        twitter.sendDirectMessage(friend, msg);
    }

    public static void updateStatus (String name, String token, String msg) throws UserNotFoundException, TwitterException {
        Twitter twitter = getTwitter(name, token);
        twitter.updateStatus(msg);
    }

    public static void inviteFriend (String name, String token, String friend) throws UserNotFoundException, TwitterException {
        Twitter twitter = getTwitter(name, token);
        if (twitter.existsFriendship(name, friend)){
            twitter.sendDirectMessage(friend, INVITE_MESSAGE);
        } else {
// follow first.. then invite
            twitter.createFriendship(friend, true);
            twitter.sendDirectMessage(friend, INVITE_MESSAGE);
        }
    }

    public static boolean createFriend (String name, String token, String friend) throws UserNotFoundException, TwitterException {
        Twitter twitter = getTwitter(name, token);
        if (twitter.existsFriendship(name, friend)){
// todo: notify that the friendship exists
            return false;
        } else {
            twitter.createFriendship(friend, true);
            return true;
        }
    }

    public static void sendDirectMessage(String name, String token, String friend, String book, String msg) throws TwitterException, UserNotFoundException {
        directMessage(name, token, friend, msg+" #BookMatch: "+book +MATCH_MESSAGE_SUFFIX);
    }

    public static void publishBook(String name, String token, String book) throws TwitterException, UserNotFoundException {
        updateStatus(name, token, PUBLISH_BOOK_MESSAGE+book+PUBLISH_MESSAGE_SUFFIX);        
    }

    public static boolean follow(String name, String token, String friend) throws TwitterException, UserNotFoundException {
        return createFriend(name, token, friend);
    }

}