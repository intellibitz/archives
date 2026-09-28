package com.androidrocks.bex.server.manager

import com.androidrocks.bex.client.json.Book
import com.androidrocks.bex.client.json.Friend
import com.androidrocks.bex.client.json.Match
import com.androidrocks.bex.server.persistent.BEXFriend
import com.androidrocks.bex.server.persistent.TradeBook
import com.androidrocks.bex.server.persistent.TradeMatch
import com.androidrocks.bex.server.persistent.TwitFriend
import com.androidrocks.bex.server.persistent.WishBook
import com.androidrocks.bex.server.persistent.WishMatch
import com.google.appengine.api.datastore.Entity
import com.google.appengine.api.datastore.Key
import com.google.appengine.api.datastore.KeyFactory
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import twitter4j.User
import java.lang.reflect.Type
import java.util.ArrayList
import java.util.Collection
import java.util.Date
import java.util.HashSet
import java.util.List
import java.util.Map
import java.util.Set
import java.util.HashMap
import java.util.logging.Logger

class TypeFactory private constructor() {
    companion object {
        private val log: Logger = Logger.getLogger(TypeFactory::class.java
            .getName())
        private val TWITTER4J_USER_LIST_TYPE: Type = TypeToken<List<User>>() {
    }.getType()
        private val MATCH_LIST_TYPE: Type = TypeToken<List<Match>>() {
    }.getType()
        private val FRIEND_LIST_TYPE: Type = TypeToken<List<Friend>>() {
    }.getType()
        private val BOOK_LIST_TYPE: Type = TypeToken<List<Book>>() {
    }.getType()
        private val BEXFRIEND_LIST_DATA_TYPE: Type = TypeToken<List<BEXFriend>>() {
    }.getType()
        private val TWITFRIEND_LIST_DATA_TYPE: Type = TypeToken<List<TwitFriend>>() {
    }.getType()
        private val WISH_LIST_DATA_TYPE: Type = TypeToken<List<WishBook>>() {
    }.getType()
        private val TRADE_LIST_DATA_TYPE: Type = TypeToken<List<TradeBook>>() {
    }.getType()
        @JvmStatic
        fun prefixKeyId(id: String?): String? {

                    return KEY_PREFIX + id
        }

        @JvmStatic
        fun prefixKeyId(id: Long?): String? {

                    return KEY_PREFIX + id
        }

        @JvmStatic
        fun bexFriendsToJsonFriends(users: List<BEXFriend>?): List<com.androidrocks.bex.client.json.Friend>? {

                    return transformList(users, BEXFRIEND_LIST_DATA_TYPE, FRIEND_LIST_TYPE)
        }

        @JvmStatic
        fun twitFriendsToJsonFriends(users: List<TwitFriend>?): List<com.androidrocks.bex.client.json.Friend>? {

                    return transformList(users, TWITFRIEND_LIST_DATA_TYPE, FRIEND_LIST_TYPE)
        }

        @JvmStatic
        fun jUsersToTwitFriendEntities(users: List<User>?): Map<Key, Entity>? {

                    val entities = HashMap<Key, Entity>(users.size)
                    for (user in users) {
                        val entity = jUserToTwitFriendEntity (user)
                        entities.put(entity.getKey(), entity)
                    }
                    log.info("jUsersToTwitFriendEntities: "+entities.size)
                    return entities
        }

        @JvmStatic
        fun twitter4jUsersToFriends(users: List<User>?): List<TwitFriend>? {

            // this doesn't work on server
                    /*
                    * Uncaught exception from servlet
            java.lang.SecurityException: java.lang.IllegalAccessException: Reflection is not allowed on static final long java.text.SimpleDateFormat.serialVersionUID
            */
            //        return transformList(users, TWITTER4J_USER_LIST_TYPE, TWITTER_USER_LIST_TYPE)

            // do the manual copy to avoid gae limitation
                    val friends = ArrayList<TwitFriend>(users.size)
                    for (user in users) {
                        val friend = jUserToTwitFriend(user)
                        friends.add(friend)
                    }
                    log.info("twitter4jUsersToFriends: "+friends.size)
                    return friends
        }

        @JvmStatic
        fun jUserToTwitFriendEntity(user: User?): Entity? {

                    val entity = Entity(TwitFriend::class.java.getSimpleName(), prefixKeyId(user.getId()))
                    entity.setProperty("id", user.getId())
                    entity.setProperty("screenName", user.getScreenName())
                    entity.setProperty("name", user.name)
                    entity.setProperty("description", user.getDescription())
                    entity.setProperty("followersCount", user.getFollowersCount())
                    entity.setProperty("location", user.getLocation())
                    entity.setProperty("profileImageUrl", user.getProfileImageURL().toString())
                    entity.setProperty("isProtected", user.isProtected())
                    return entity
        }

        @JvmStatic
        fun twitEntityToBEXEntity(twit: Entity?): Entity? {

                    val entity = Entity(BEXFriend::class.java.getSimpleName(), twit.getKey().name)
                    entity.setProperty("id", twit.getProperty("id"))
                    entity.setProperty("screenName", twit.getProperty("screenName"))
                    entity.setProperty("name", twit.getProperty("name"))
                    entity.setProperty("description", twit.getProperty("description"))
                    entity.setProperty("followersCount", twit.getProperty("followersCount"))
                    entity.setProperty("location", twit.getProperty("location"))
                    entity.setProperty("profileImageUrl", twit.getProperty("profileImageUrl"))
                    entity.setProperty("isProtected", twit.getProperty("isProtected"))
                    return entity
        }

        @JvmStatic
        fun entityToJsonFriend(entity: Entity?): Friend? {

                    val friend = Friend()
                    friend.setDescription((String) entity.getProperty("description"))
                    friend.setId(String.valueOf(entity.getProperty("id")))
                    friend.setLocation((String) entity.getProperty("location"))
                    friend.setName((String) entity.getProperty("name"))
                    friend.setProfileImageUrl((String) entity.getProperty("profileImageUrl"))
                    friend.setScreenName((String) entity.getProperty("screenName"))
                    return friend
        }

        @JvmStatic
        fun twitFriendToBEXFriend(friend: TwitFriend?): BEXFriend? {

                    log.info("#twitFriendToBEXFriend: "+friend.getKey())
            // the TwitFriend can be brand new or existing.. handle both cases
                    val bexFriend = BEXFriend()
                    if (null != friend.getKey()){
            // todo: is this the right thing to do? What if BEXFriend does not exist yet.. REVISIT!!
                        bexFriend.setKey(createBEXFriendKey(friend.getKey().name))
                    }
                    bexFriend.setDescription(friend.getDescription())
                    bexFriend.setFollowersCount(friend.getFollowersCount())
                    bexFriend.setId(friend.getId())
                    bexFriend.setLocation(friend.getLocation())
                    bexFriend.setName(friend.name)
                    bexFriend.setProfileImageUrl(friend.getProfileImageUrl())
                    bexFriend.setProtected(friend.isProtected())
                    bexFriend.setScreenName(friend.getScreenName())
                    bexFriend.setUrl(friend.getUrl())
                    bexFriend.setUsers(HashSet<Key>(friend.getUsers()))
                    return bexFriend
        }

        @JvmStatic
        fun jUserToTwitFriend(user: User?): TwitFriend? {

                    val friend = TwitFriend()
                    friend.setId(user.getId())
                    friend.setScreenName(user.getScreenName())
                    friend.setName(user.name)
                    friend.setDescription(user.getDescription())
                    friend.setFollowersCount(user.getFollowersCount())
                    friend.setLocation(user.getLocation())
                    friend.setProfileImageUrl(user.getProfileImageURL().toString())
                    friend.setProtected(user.isProtected())
                    return friend
        }

        @JvmStatic
        fun entitiesToJsonFriends(entities: Map<Key, Entity>?): List<Friend>? {

                    val entityCollection = entities.values()
                    return entitiesToJsonFriends(entityCollection)
        }

        @JvmStatic
        fun entitiesToJsonFriends(entityCollection: Collection<Entity>?): List<Friend>? {

                    val friends = ArrayList<Friend>(entityCollection.size)
                    for (entity in entityCollection) {
                        val friend = entityToJsonFriend(entity)
                        friends.add(friend)
                    }
                    return friends
        }

        @JvmStatic
        fun entitiesToJsonBooks(entities: Map<Key, Entity>?): List<Book>? {

                    val books = ArrayList<Book>(entities.size)
                    for (entity in entities.values()) {
                        val book = entityToJsonBook(entity)
                        books.add(book)
                    }
                    return books
        }

        @JvmStatic
        fun entityToJsonBook(entity: Entity?): Book? {

                    val book = Book()
                    book.setId((String) entity.getProperty("id"))
                    book.setAuthors(ArrayList<String>((Collection)entity.getProperty("authors")))
                    book.setDescription((String) entity.getProperty("description"))
                    book.setDetailsUrl((String) entity.getProperty("detailsUrl"))
                    book.setEan((String) entity.getProperty("ean"))
                    book.setImage((String) entity.getProperty("image"))
                    book.setIsbn((String) entity.getProperty("isbn"))
                    book.setLastModified((Date) entity.getProperty("lastModified"))
                    book.setPages(Integer.valueOf(entity.getProperty("pages").toString()))
                    book.setPublicationDate((Date) entity.getProperty("publicationDate"))
                    book.setPublisher((String) entity.getProperty("publisher"))
                    book.setStore((String) entity.getProperty("store"))
                    book.setTitle((String) entity.getProperty("title"))
                    return book
        }

        @JvmStatic
        fun createWishBookKey(keyname: String?): Key? {

                    return KeyFactory.createKey(WishBook::class.java.getSimpleName(), keyname)
        }

        @JvmStatic
        fun createTradeBookKey(keyname: String?): Key? {

                    return KeyFactory.createKey(TradeBook::class.java.getSimpleName(), keyname)
        }

        @JvmStatic
        fun createBEXFriendKey(keyname: String?): Key? {

                    return KeyFactory.createKey(BEXFriend::class.java.getSimpleName(), keyname)
        }

        @JvmStatic
        fun createTwitFriendKey(keyname: String?): Key? {

                    return KeyFactory.createKey(TwitFriend::class.java.getSimpleName(), keyname)
        }

        @JvmStatic
        fun createUserKey(keyname: String?): Key? {

                    return KeyFactory.createKey(User::class.java.getSimpleName(), keyname)
        }

        @JvmStatic
        fun createWishMatchKey(keyname: String?): Key? {

                    return KeyFactory.createKey(WishMatch::class.java.getSimpleName(), keyname)
        }

        @JvmStatic
        fun createTradeMatchKey(keyname: String?): Key? {

                    return KeyFactory.createKey(TradeMatch::class.java.getSimpleName(), keyname)
        }

        @JvmStatic
        fun transformList(src: List?, srcType: Type?, destType: Type?): List? {

                    return fromJson(toJson(src, srcType), destType)
        }

        @JvmStatic
        fun toJson(src: List?, srcType: Type?): String? {

                    return Gson().toJson(src, srcType)
        }

        @JvmStatic
        fun fromJson(src: String?, srcType: Type?): List? {

                    return Gson().fromJson(src, srcType)
        }

        @JvmStatic
        fun wishMatchJson(matches: List<WishMatch>?): List<Match>? {

                    val jsonMatches = ArrayList<Match>(matches.size)
                    for (match in matches) {
                        val keys = match.getFriends()
                        val friends = FriendManager.fetchFriends(keys)
                        val key = match.getBook()
                        val book = (TradeBook) PMF.loadObjectById(TradeBook::class.java, key)
                        for (friend in friends) {
                            val m = Match()
                            m.setId(book.getId())
                            m.setFriendId(friend.getId())
                            m.setTitle(book.getTitle())
                            m.setDescription(book.getDescription())
                            m.setImage(book.getImage())
                            m.setLocation(friend.getLocation())
                            m.setName(friend.name)
                            m.setProfileImageUrl(friend.getProfileImageUrl())
                            m.setPublisher(book.getPublisher())
                            m.setScreenName(friend.getScreenName())
                            m.setTitle(book.getTitle())
                            jsonMatches.add(m)
                            log.info("Adding Match: "+m)
                        }
                    }
                    return jsonMatches
        }

        @JvmStatic
        fun tradeMatchJson(matches: List<TradeMatch>?): List<Match>? {

                    val jsonMatches = ArrayList<Match>(matches.size)
                    for (match in matches) {
                        val keys = match.getFriends()
                        val friends = FriendManager.fetchFriends(keys)
                        val key = match.getBook()
                        val book = (WishBook) PMF.loadObjectById(WishBook::class.java, key)
                        for (friend in friends) {
                            val m = Match()
                            m.setId(book.getId())
                            m.setFriendId(friend.getId())
                            m.setTitle(book.getTitle())
                            m.setDescription(book.getDescription())
                            m.setImage(book.getImage())
                            m.setLocation(friend.getLocation())
                            m.setName(friend.name)
                            m.setProfileImageUrl(friend.getProfileImageUrl())
                            m.setPublisher(book.getPublisher())
                            m.setScreenName(friend.getScreenName())
                            m.setTitle(book.getTitle())
                            jsonMatches.add(m)
                            log.info("Adding Match: "+m)
                        }
                    }
                    return jsonMatches
        }

        @JvmStatic
        fun getKeyNames(keys: Set<Key>?): Set<String>? {

                    val keyNames = HashSet<String>(keys.size)
                    for (key in keys) {
                        keyNames.add(key.name)
                    }
                    return keyNames
        }

        @JvmStatic
        fun createBEXFriendKeyWithPrefix(id: Long?): Key? {

                    return createBEXFriendKey(prefixKeyId(id))
        }

        @JvmStatic
        fun createTwitFriendKeyWithPrefix(id: Long?): Key? {

                    return createTwitFriendKey(prefixKeyId(id))
        }

        @JvmStatic
        fun createWishBookKeyWithPrefix(id: String?): Key? {

                    return createWishBookKey(prefixKeyId(id))
        }

        @JvmStatic
        fun createTradeBookKeyWithPrefix(id: String?): Key? {

                    return createTradeBookKey(prefixKeyId(id))
        }

        @JvmStatic
        fun createUserKeyWithPrefix(twitterId: String?): Key? {

                    return createUserKey(prefixKeyId(twitterId))
        }

    }
}
