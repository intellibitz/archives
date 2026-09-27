package com.androidrocks.bex.server.manager.test;

import com.androidrocks.bex.client.json.Book;
import com.androidrocks.bex.server.manager.BookManager;
import com.androidrocks.bex.server.manager.FriendManager;
import com.androidrocks.bex.server.manager.PMF;
import com.androidrocks.bex.server.manager.UserManager;
import com.androidrocks.bex.server.persistent.BEXFriend;
import com.androidrocks.bex.server.persistent.TradeBook;
import com.androidrocks.bex.server.persistent.User;
import com.androidrocks.bex.server.persistent.WishBook;
import com.androidrocks.bex.server.persistent.WishMatch;
import com.androidrocks.bex.server.persistent.TwitFriend;
import com.google.appengine.api.datastore.Key;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public final class DataMockFactory {

    private static final Logger log = Logger.getLogger(DataMockFactory.class
            .getName());


    private DataMockFactory() {
    }

    /**
     *

ID/Name screenName token tokenSecret twitterId

T14281625   mobeegal    14281625-NkNfzTWZ5yNTEP1rddHM7NHTQ1V7i6xG3ECAF9l84  UlTCA2qZKelpH9yaJCcJszDpxSBsFM5irWax4mvaE   14281625
T67034976   BooksEX     67034976-0DVfTtjPZXYWC6q02REEWF6FmGC8fFxCk52vDZuuq  7qPNMTHBgvOgCB2csvuwaeycl4InOfl1cXBsu1u30   67034976


     * @return
     */
    public static List<User> mockUsers(){

        List<User> users = new ArrayList<User>(2);
        User user = new User();
        user.setScreenName("mobeegal");
        user.setToken("14281625-NkNfzTWZ5yNTEP1rddHM7NHTQ1V7i6xG3ECAF9l84");
        user.setTokenSecret("UlTCA2qZKelpH9yaJCcJszDpxSBsFM5irWax4mvaE");
        user.setTwitterId(14281625);
        users.add (user);

        User user2 = new User();
        user2.setScreenName("BooksEX");
        user2.setToken("67034976-0DVfTtjPZXYWC6q02REEWF6FmGC8fFxCk52vDZuuq");
        user2.setTokenSecret("7qPNMTHBgvOgCB2csvuwaeycl4InOfl1cXBsu1u30");
        user2.setTwitterId(67034976);
        users.add (user2);
        return users;
    }

    private static List<TwitFriend> mockTwitFriends() {
        List<TwitFriend> users = new ArrayList<TwitFriend>(2);
        TwitFriend user = new TwitFriend();
        user.setScreenName("mobeegal");
        user.setId(14281625);
        users.add (user);

        TwitFriend user2 = new TwitFriend();
        user2.setScreenName("BooksEX");
        user2.setId(67034976);
        users.add (user2);
        return users;
    }

    public static List<BEXFriend> mockBEXFriends(){

        List<BEXFriend> users = new ArrayList<BEXFriend>(2);
        BEXFriend user = new BEXFriend();
        user.setScreenName("mobeegal");
        user.setId(14281625);
        users.add (user);

        BEXFriend user2 = new BEXFriend();
        user2.setScreenName("BooksEX");
        user2.setId(67034976);
        users.add (user2);
        return users;
    }

    public static List<Book> mockClientBooks() {
        List<Book> books = new ArrayList<Book>(2);
        Book book = new Book();
        book.setId("book1");
        List<String> authors = new ArrayList<String>(2);
        authors.add("author1");
        authors.add("author2");
        book.setAuthors(authors);
        book.setImage("test image url ");
        books.add(book);

        Book book2 = new Book();
        book2.setId("book2");
        book2.setAuthors(authors);
        book2.setImage("test image url ");
        books.add(book2);
        
        return books;
    }

    public static List<WishBook> mockWishList() {
        List<WishBook> books = new ArrayList<WishBook>(2);
        WishBook book = new WishBook();
        book.setId("book1");
        List<String> authors = new ArrayList<String>(2);
        authors.add("author1");
        authors.add("author2");
        book.setAuthors(authors);
        book.setImage("test image url ");
        books.add(book);

        return books;
    }

    public static List<TradeBook> mockTradeList() {
        List<TradeBook> books = new ArrayList<TradeBook>(2);
        TradeBook book = new TradeBook();
        book.setId("book1");
        List<String> authors = new ArrayList<String>(2);
        authors.add("author1");
        authors.add("author2");
        book.setAuthors(authors);
        book.setImage("test image url ");
        books.add(book);

        return books;
    }

    public static Book mockClientBook() {
        Book book = new Book();
        book.setId("book1");
        List<String> authors = new ArrayList<String>(2);
        authors.add("author1");
        authors.add("author2");
        book.setAuthors(authors);
        book.setImage("test image url ");
        return book;
    }

    public static List<User> saveMockUsers() {
        List<User> users = mockUsers();
        for (User user : users) {
            UserManager.saveUserWithCustomKey(user);
            log.info("Saved: " + user);
        }
        return users;
    }

    public static Set<Key> saveMockWishList(User user) {
        List<WishBook> books = mockWishList();
        BookManager.saveWishList(books);
        UserManager.saveWishList(user, books);
        return user.getWishList();
    }

    public static Set<Key> saveMockWishList() {
        List<User> users = saveMockUsers();
        User user = users.get(0);
        List<WishBook> books = mockWishList();
        BookManager.saveWishList(books);
        UserManager.saveWishList(user, books);
        return user.getWishList();
    }

    public static List<TwitFriend> saveTwitFriends() {
        List<User> users = saveMockUsers();
        User user = users.get(0);
        List<TwitFriend> friends = mockTwitFriends();
        FriendManager.saveTwitFriends (friends);
//        UserManager.saveTwitFriends(user, friends);
        return friends;
    }

    public static Set<Key> saveMockBEXFriends() {
        List<User> users = saveMockUsers();
        User user = users.get(0);
        return saveMockBEXFriends(user);
    }

    public static Set<Key> saveMockBEXFriends(User user) {
        List<BEXFriend> friends = mockBEXFriends();
        FriendManager.saveBEXFriends(friends);
//        UserManager.saveBEXFriends(user, friends);
        return user.getBexFriends();
    }

    public static WishMatch saveMockWishMatch() {
        List<User> users = saveMockUsers();
        User user = users.get(0);
        saveMockWishList(user);
        saveMockBEXFriends(user);
        return saveWishMatch(user);
    }

    public static WishMatch saveWishMatch(User user) {
        WishMatch wishMatch = new WishMatch();
        wishMatch.setBook(user.getWishList().iterator().next());
        wishMatch.getUsers().add(user.getKey());
        wishMatch.getFriends().add(user.getBexFriends().iterator().next());

        PMF.makePersistent(wishMatch);
        return wishMatch;
    }

}