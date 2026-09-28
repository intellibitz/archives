/**
 *
 */
package com.androidrocks.bex.server.persistent

import com.google.appengine.api.datastore.Key
import java.util.ArrayList
import java.util.Date
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
class WishBook {

    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    var key: Key? = null

    @Persistent
    private var users: MutableSet<Key>? = null

    @Persistent
    var id: String? = null

    @Persistent
    var store: String? = null

    @Persistent
    var isbn: String? = null

    @Persistent
    var ean: String? = null

    @Persistent
    var pages: Int = 0

    @Persistent
    var title: String? = null

    @Persistent
    var detailsUrl: String? = null

    @Persistent
    var publisher: String? = null

    @Persistent
    var description: String? = null

    @Persistent
    var publicationDate: Date? = null

    @Persistent
    var lastModified: Date? = null

    @Persistent
    var authors: MutableList<String> = ArrayList(1)

    @Persistent
    var image: String? = null

    @Persistent
    private var isWishList: Boolean = true

    fun setWishList(wishList: Boolean) {
        isWishList = wishList
    }

    fun isWishList(): Boolean {
        return isWishList
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
