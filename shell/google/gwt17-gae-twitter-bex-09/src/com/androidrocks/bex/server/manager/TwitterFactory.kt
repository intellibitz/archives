package com.androidrocks.bex.server.manager

import com.androidrocks.bex.server.persistent.User
import com.androidrocks.bex.server.persistent.UserNotFoundException
import com.google.appengine.api.datastore.Entity
import com.google.appengine.api.datastore.Key
import twitter4j.Twitter
import twitter4j.TwitterException
import twitter4j.http.AccessToken
import java.util.List
import java.util.Map
import java.util.logging.Logger

class TwitterFactory private constructor() {
    companion object {
        private val log: Logger = Logger.getLogger(TwitterFactory::class.java
            .getName())
        @JvmStatic
        fun getTwitter(): Twitter? {

                    val twitter = Twitter()
                    twitter.setOAuthConsumer(KEY, SECRET)
                    return twitter
        }

        @Throws(UserNotFoundException::class)
        @JvmStatic
        fun getTwitter(name: String?, token: String?): Twitter? {

                    val twitter = getTwitter()
                    twitter.setOAuthAccessToken(loadAccessToken(name, token))
                    return twitter
        }

        @JvmStatic
        fun getTwitter(user: User?): Twitter? {

                    val twitter = getTwitter()
                    twitter.setOAuthAccessToken(loadAccessToken(user))
                    return twitter
        }

        @Throws(TwitterException::class)
        @JvmStatic
        fun getTwitFriendEntities(user: User?): Map<Key,Entity>? {

                    val twitter = getTwitter(user)
            // get following
                    val users = twitter.getFriendsStatuses()
            // get followers
                    users.addAll(twitter.getFollowersStatuses())
                    log.info("#getFriendsStatuses: "+users.size)
                    return TypeFactory.jUsersToTwitFriendEntities(users)
        }

        @Throws(TwitterException::class)
        @JvmStatic
        fun getTwitFollowerEntities(user: User?): Map<Key, Entity>? {

                    val twitter = getTwitter(user)
            // get followers
                    val users = twitter.getFollowersStatuses()
                    log.info("#getFollowerStatuses: "+users.size)
                    return TypeFactory.jUsersToTwitFriendEntities(users)
        }

        @JvmStatic
        fun loadAccessToken(user: User?): AccessToken? {

                    return AccessToken(user.getToken(), user.getTokenSecret())
        }

        @Throws(UserNotFoundException::class)
        @JvmStatic
        fun loadAccessToken(name: String?, token: String?): AccessToken? {

                    val user = UserManager.loadUser(name, token)
                    if (null == user){
                        throw UserNotFoundException("User not found: "+name)
                    }
                    return AccessToken(token, user.getTokenSecret())
        }

        @Throws(UserNotFoundException::class, TwitterException::class)
        @JvmStatic
        fun directMessage(name: String?, token: String?, friend: String?, msg: String?) {

                    val twitter = getTwitter(name, token)
                    twitter.sendDirectMessage(friend, msg)
        }

        @Throws(UserNotFoundException::class, TwitterException::class)
        @JvmStatic
        fun updateStatus(name: String?, token: String?, msg: String?) {

                    val twitter = getTwitter(name, token)
                    twitter.updateStatus(msg)
        }

        @Throws(UserNotFoundException::class, TwitterException::class)
        @JvmStatic
        fun inviteFriend(name: String?, token: String?, friend: String?) {

                    val twitter = getTwitter(name, token)
                    if (twitter.existsFriendship(name, friend)){
                        twitter.sendDirectMessage(friend, INVITE_MESSAGE)
                    } else {
            // follow first.. then invite
                        twitter.createFriendship(friend, true)
                        twitter.sendDirectMessage(friend, INVITE_MESSAGE)
                    }
        }

        @Throws(UserNotFoundException::class, TwitterException::class)
        @JvmStatic
        fun createFriend(name: String?, token: String?, friend: String?): Boolean? {

                    val twitter = getTwitter(name, token)
                    if (twitter.existsFriendship(name, friend)){
            // todo: notify that the friendship exists
                        return false
                    } else {
                        twitter.createFriendship(friend, true)
                        return true
                    }
        }

        @Throws(TwitterException::class, UserNotFoundException::class)
        @JvmStatic
        fun sendDirectMessage(name: String?, token: String?, friend: String?, book: String?, msg: String?) {

                    directMessage(name, token, friend, msg+" #BookMatch: "+book +MATCH_MESSAGE_SUFFIX)
        }

        @Throws(TwitterException::class, UserNotFoundException::class)
        @JvmStatic
        fun publishBook(name: String?, token: String?, book: String?) {

                    updateStatus(name, token, PUBLISH_BOOK_MESSAGE+book+PUBLISH_MESSAGE_SUFFIX)
        }

        @Throws(TwitterException::class, UserNotFoundException::class)
        @JvmStatic
        fun follow(name: String?, token: String?, friend: String?): Boolean? {

                    return createFriend(name, token, friend)
        }

    }
}
