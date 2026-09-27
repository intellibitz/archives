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
public class User {

    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;
    @Persistent
    private String twitterId;
    @Persistent
    private String screenName;
    @Persistent
    private String token;
    @Persistent
    private String tokenSecret;
    @Persistent
    private Set<Key> wishList;
    @Persistent
    private Set<Key> tradeList;
    @Persistent
    private Set<Key> bexFriends;
    @Persistent
    private Set<Key> twitFriends;

    /**
     * @return the id
     */
    public Key getKey() {
        return key;
    }

    /**
     * @param key the id to set
     */
    public void setKey(Key key) {
        this.key = key;
    }

    /**
     * @return the twitterId
     */
    public String getTwitterId() {
        return twitterId;
    }

    /**
     * @param twitterId the twitterId to set
     */
    public void setTwitterId(int twitterId) {
        this.twitterId = String.valueOf(twitterId);
    }

    /**
     * @return the screenName
     */
    public String getScreenName() {
        return screenName;
    }

    /**
     * @param screenName the screenName to set
     */
    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    /**
     * @return the token
     */
    public String getToken() {
        return token;
    }

    /**
     * @param token the token to set
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * @return the tokenSecret
     */
    public String getTokenSecret() {
        return tokenSecret;
    }

    /**
     * @param tokenSecret the tokenSecret to set
     */
    public void setTokenSecret(String tokenSecret) {
        this.tokenSecret = tokenSecret;
    }

    public Set<Key> getWishList() {
        if (null == wishList){
            wishList = new HashSet<Key>();
        }
        return wishList;
    }

    public void setWishList(Set<Key> wishList) {
        this.wishList = wishList;
    }

    public Set<Key> getTradeList() {
        if (null == tradeList){
            tradeList = new HashSet<Key>();
        }
        return tradeList;
    }

    public void setTradeList(Set<Key> tradeList) {
        this.tradeList = tradeList;
    }

    public Set<Key> getTwitFriends() {
        if (null == twitFriends){
            twitFriends = new HashSet<Key>();
        }
        return twitFriends;
    }

    public void setTwitFriends(Set<Key> twitFriends) {
        this.twitFriends = twitFriends;
    }

    public Set<Key> getBexFriends() {
        if (null == bexFriends){
            bexFriends = new HashSet<Key>();
        }
        return bexFriends;
    }

    public void setBexFriends(Set<Key> bexFriends) {
        this.bexFriends = bexFriends;
    }

}
