/**
 *
 */
package com.androidrocks.bex.server.persistent;

import com.google.appengine.api.datastore.Key;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.IdGeneratorStrategy;
import java.util.Set;
import java.util.HashSet;


/**
 * @author muthu
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class BEXFriend {

    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;
    @Persistent
    private Set<Key> users;
    @Persistent
    private long id;
    @Persistent
    private String name;
    @Persistent
    private String screenName;
    @Persistent
    private String location;
    @Persistent
    private String description;
    @Persistent
    private String profileImageUrl;
    @Persistent
    private String url;
    @Persistent
    private boolean isProtected;
    @Persistent
    private int followersCount;

    public BEXFriend() {
    }

    public Key getKey() {
        return key;
    }

    public void setKey(Key key) {
        this.key = key;
    }

    
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isProtected() {
        return isProtected;
    }

    public void setProtected(boolean aProtected) {
        isProtected = aProtected;
    }

    public int getFollowersCount() {
        return followersCount;
    }

    public void setFollowersCount(int followersCount) {
        this.followersCount = followersCount;
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