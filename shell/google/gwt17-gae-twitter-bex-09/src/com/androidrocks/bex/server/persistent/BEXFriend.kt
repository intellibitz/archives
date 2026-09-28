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
class BEXFriend {

    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    var key: Key? = null

    @Persistent
    private var users: MutableSet<Key>? = null

    @Persistent
    var id: Long = 0

    @Persistent
    var name: String? = null

    @Persistent
    var screenName: String? = null

    @Persistent
    var location: String? = null

    @Persistent
    var description: String? = null

    @Persistent
    var profileImageUrl: String? = null

    @Persistent
    var url: String? = null

    @Persistent
    private var isProtected: Boolean = false

    @Persistent
    var followersCount: Int = 0

    fun isProtected(): Boolean {
        return isProtected
    }

    fun setProtected(aProtected: Boolean) {
        isProtected = aProtected
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
