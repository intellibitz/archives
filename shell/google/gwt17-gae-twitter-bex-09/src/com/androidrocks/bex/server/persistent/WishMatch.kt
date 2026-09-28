/**
 *
 */
package com.androidrocks.bex.server.persistent

import com.google.appengine.api.datastore.Key
import java.util.HashSet
import javax.jdo.annotations.IdGeneratorStrategy
import javax.jdo.annotations.IdentityType
import javax.jdo.annotations.PersistenceCapable
import javax.jdo.annotations.Persistent
import javax.jdo.annotations.PrimaryKey

/**
 * @author muthu
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION)
class WishMatch {

    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    var key: Key? = null

    @Persistent
    var book: Key? = null

    @Persistent
    private var friends: MutableSet<Key>? = null

    @Persistent
    private var users: MutableSet<Key>? = null

    fun getFriends(): MutableSet<Key> {
        if (friends == null) {
            friends = HashSet()
        }
        return friends!!
    }

    fun setFriends(friends: MutableSet<Key>?) {
        this.friends = friends
    }

    fun getUsers(): MutableSet<Key> {
        if (users == null) {
            users = HashSet()
        }
        return users!!
    }

    fun setUsers(users: MutableSet<Key>?) {
        this.users = users
    }
}
