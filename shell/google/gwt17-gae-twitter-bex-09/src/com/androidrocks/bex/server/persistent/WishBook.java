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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * @author muthu
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class WishBook{
    
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;
    @Persistent
    private Set<Key> users;
    @Persistent
    private String id;
    @Persistent
    private String store;
    @Persistent
    private String isbn;
    @Persistent
    private String ean;
    @Persistent
    private int pages;
    @Persistent
    private String title;
    @Persistent
    private String detailsUrl;
    @Persistent
    private String publisher;
    @Persistent
    private String description;
    @Persistent
    private Date publicationDate;
    @Persistent
    private Date lastModified;
    @Persistent
    private List<String> authors = new ArrayList<String>(1);
    @Persistent
    private String image;
    @Persistent
    private boolean isWishList = true;

    public Key getKey() {
        return key;
    }

    public void setKey(Key key) {
        this.key = key;
    }

    public boolean isWishList() {
        return isWishList;
    }

    public void setWishList(boolean wishList) {
        isWishList = wishList;
    }

    public String getStore() {
        return store;
    }

    public void setStore(String store) {
        this.store = store;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getEan() {
        return ean;
    }

    public void setEan(String ean) {
        this.ean = ean;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDetailsUrl() {
        return detailsUrl;
    }

    public void setDetailsUrl(String detailsUrl) {
        this.detailsUrl = detailsUrl;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getPublicationDate() {
        return publicationDate;
    }

    public void setPublicationDate(Date publicationDate) {
        this.publicationDate = publicationDate;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
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