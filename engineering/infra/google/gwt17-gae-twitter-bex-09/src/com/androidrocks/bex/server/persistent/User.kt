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
class User {

    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    var key: Key? = null

    @Persistent
    var twitterId: String? = null

    @Persistent
    var screenName: String? = null

    @Persistent
    var token: String? = null

    @Persistent
    var tokenSecret: String? = null

    @Persistent
    private var wishList: MutableSet<Key>? = null

    @Persistent
    private var tradeList: MutableSet<Key>? = null

    @Persistent
    private var bexFriends: MutableSet<Key>? = null

    @Persistent
    private var twitFriends: MutableSet<Key>? = null

    fun setTwitterId(twitterId: Int) {
        this.twitterId = twitterId.toString()
    }

    fun getWishList(): MutableSet<Key> {
        if (wishList == null) {
            wishList = HashSet()
        }
        return wishList!!
    }

    fun setWishList(wishList: MutableSet<Key>?) {
        this.wishList = wishList
    }

    fun getTradeList(): MutableSet<Key> {
        if (tradeList == null) {
            tradeList = HashSet()
        }
        return tradeList!!
    }

    fun setTradeList(tradeList: MutableSet<Key>?) {
        this.tradeList = tradeList
    }

    fun getTwitFriends(): MutableSet<Key> {
        if (twitFriends == null) {
            twitFriends = HashSet()
        }
        return twitFriends!!
    }

    fun setTwitFriends(twitFriends: MutableSet<Key>?) {
        this.twitFriends = twitFriends
    }

    fun getBexFriends(): MutableSet<Key> {
        if (bexFriends == null) {
            bexFriends = HashSet()
        }
        return bexFriends!!
    }

    fun setBexFriends(bexFriends: MutableSet<Key>?) {
        this.bexFriends = bexFriends
    }
}
