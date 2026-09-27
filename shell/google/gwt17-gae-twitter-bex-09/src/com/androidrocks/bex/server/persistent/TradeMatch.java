/**
 *
 */
package com.androidrocks.bex.server.persistent;

import com.google.appengine.api.datastore.Key;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import java.util.HashSet;
import java.util.Set;


/**
 * @author muthu
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class TradeMatch {
    
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;
    @Persistent
    private Key book;
    @Persistent
    private Set<Key> friends;
    @Persistent
    private Set<Key> users;

    public Key getKey() {
        return key;
    }

    public void setKey(Key key) {
        this.key = key;
    }

    public Key getBook() {
        return book;
    }

    public void setBook(Key book) {
        this.book = book;
    }

    public Set<Key> getFriends() {
        if (null == friends){
            friends = new HashSet<Key>();
        }
        return friends;
    }

    public void setFriends(Set<Key> friends) {
        this.friends = friends;
    }

    public Set<Key> getUsers() {
        if (null == users){
            users = new HashSet<Key>();
        }
        return users;
    }

    public void setUsers(Set<Key> users) {
        this.users = users;
    }

}