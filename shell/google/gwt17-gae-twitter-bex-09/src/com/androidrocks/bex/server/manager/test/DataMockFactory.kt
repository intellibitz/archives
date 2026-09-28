package com.androidrocks.bex.server.manager.test

import com.androidrocks.bex.client.json.Book
import com.androidrocks.bex.server.manager.BookManager
import com.androidrocks.bex.server.manager.FriendManager
import com.androidrocks.bex.server.manager.PMF
import com.androidrocks.bex.server.manager.UserManager
import com.androidrocks.bex.server.persistent.BEXFriend
import com.androidrocks.bex.server.persistent.TradeBook
import com.androidrocks.bex.server.persistent.User
import com.androidrocks.bex.server.persistent.WishBook
import com.androidrocks.bex.server.persistent.WishMatch
import com.androidrocks.bex.server.persistent.TwitFriend
import com.google.appengine.api.datastore.Key
import java.util.ArrayList
import java.util.List
import java.util.Set
import java.util.logging.Logger

class DataMockFactory private constructor() {
    companion object {
        private val log: Logger = Logger.getLogger(DataMockFactory::class.java
            .getName())
        @JvmStatic
        fun mockUsers(): List<User>? {


                    val users = ArrayList<User>(2)
                    val user = User()
                    user.setScreenName("mobeegal")
                    user.setToken("14281625-NkNfzTWZ5yNTEP1rddHM7NHTQ1V7i6xG3ECAF9l84")
                    user.setTokenSecret("UlTCA2qZKelpH9yaJCcJszDpxSBsFM5irWax4mvaE")
                    user.setTwitterId(14281625)
                    users.add (user)

                    val user2 = User()
                    user2.setScreenName("BooksEX")
                    user2.setToken("67034976-0DVfTtjPZXYWC6q02REEWF6FmGC8fFxCk52vDZuuq")
                    user2.setTokenSecret("7qPNMTHBgvOgCB2csvuwaeycl4InOfl1cXBsu1u30")
                    user2.setTwitterId(67034976)
                    users.add (user2)
                    return users
        }

        @JvmStatic
        fun mockBEXFriends(): List<BEXFriend>? {


                    val users = ArrayList<BEXFriend>(2)
                    val user = BEXFriend()
                    user.setScreenName("mobeegal")
                    user.setId(14281625)
                    users.add (user)

                    val user2 = BEXFriend()
                    user2.setScreenName("BooksEX")
                    user2.setId(67034976)
                    users.add (user2)
                    return users
        }

        @JvmStatic
        fun mockClientBooks(): List<Book>? {

                    val books = ArrayList<Book>(2)
                    val book = Book()
                    book.setId("book1")
                    val authors = ArrayList<String>(2)
                    authors.add("author1")
                    authors.add("author2")
                    book.setAuthors(authors)
                    book.setImage("test image url ")
                    books.add(book)

                    val book2 = Book()
                    book2.setId("book2")
                    book2.setAuthors(authors)
                    book2.setImage("test image url ")
                    books.add(book2)

                    return books
        }

        @JvmStatic
        fun mockWishList(): List<WishBook>? {

                    val books = ArrayList<WishBook>(2)
                    val book = WishBook()
                    book.setId("book1")
                    val authors = ArrayList<String>(2)
                    authors.add("author1")
                    authors.add("author2")
                    book.setAuthors(authors)
                    book.setImage("test image url ")
                    books.add(book)

                    return books
        }

        @JvmStatic
        fun mockTradeList(): List<TradeBook>? {

                    val books = ArrayList<TradeBook>(2)
                    val book = TradeBook()
                    book.setId("book1")
                    val authors = ArrayList<String>(2)
                    authors.add("author1")
                    authors.add("author2")
                    book.setAuthors(authors)
                    book.setImage("test image url ")
                    books.add(book)

                    return books
        }

        @JvmStatic
        fun mockClientBook(): Book? {

                    val book = Book()
                    book.setId("book1")
                    val authors = ArrayList<String>(2)
                    authors.add("author1")
                    authors.add("author2")
                    book.setAuthors(authors)
                    book.setImage("test image url ")
                    return book
        }

        @JvmStatic
        fun saveMockUsers(): List<User>? {

                    val users = mockUsers()
                    for (user in users) {
                        UserManager.saveUserWithCustomKey(user)
                        log.info("Saved: " + user)
                    }
                    return users
        }

        @JvmStatic
        fun saveMockWishList(user: User?): Set<Key>? {

                    val books = mockWishList()
                    BookManager.saveWishList(books)
                    UserManager.saveWishList(user, books)
                    return user.getWishList()
        }

        @JvmStatic
        fun saveMockWishList(): Set<Key>? {

                    val users = saveMockUsers()
                    val user = users.get(0)
                    val books = mockWishList()
                    BookManager.saveWishList(books)
                    UserManager.saveWishList(user, books)
                    return user.getWishList()
        }

        @JvmStatic
        fun saveTwitFriends(): List<TwitFriend>? {

                    val users = saveMockUsers()
                    val user = users.get(0)
                    val friends = mockTwitFriends()
                    FriendManager.saveTwitFriends (friends)
            //        UserManager.saveTwitFriends(user, friends)
                    return friends
        }

        @JvmStatic
        fun saveMockBEXFriends(): Set<Key>? {

                    val users = saveMockUsers()
                    val user = users.get(0)
                    return saveMockBEXFriends(user)
        }

        @JvmStatic
        fun saveMockBEXFriends(user: User?): Set<Key>? {

                    val friends = mockBEXFriends()
                    FriendManager.saveBEXFriends(friends)
            //        UserManager.saveBEXFriends(user, friends)
                    return user.getBexFriends()
        }

        @JvmStatic
        fun saveMockWishMatch(): WishMatch? {

                    val users = saveMockUsers()
                    val user = users.get(0)
                    saveMockWishList(user)
                    saveMockBEXFriends(user)
                    return saveWishMatch(user)
        }

        @JvmStatic
        fun saveWishMatch(user: User?): WishMatch? {

                    val wishMatch = WishMatch()
                    wishMatch.setBook(user.getWishList().iterator().next())
                    wishMatch.getUsers().add(user.getKey())
                    wishMatch.getFriends().add(user.getBexFriends().iterator().next())

                    PMF.makePersistent(wishMatch)
                    return wishMatch
        }

    }
}
