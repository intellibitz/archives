package com.retailwave.fce.shared.domain;
/**
 * $Id: User.java 5 2010-06-03 11:07:35Z muthu $
 * $HeadURL: svn://10.10.200.111:3691/Finance/tags/framework-snapshot1/fce/src/main/java/com/retailwave/fce/shared/domain/User.java $
 */

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "USER")
public class User implements Serializable{

    /**
     *  The serialization runtime associates with each serializable class a version number, called a serialVersionUID,
     * which is used during deserialization to verify that the sender and receiver of a serialized object have loaded
     * classes for that object that are compatible with respect to serialization. If the receiver has loaded a class for
     * the object that has a different serialVersionUID than that of the corresponding sender's class, then
     * deserialization will result in an InvalidClassException. A serializable class can declare its own serialVersionUID
     * explicitly by declaring a field named "serialVersionUID" that must be static, final, and of type long:

     ANY-ACCESS-MODIFIER static final long serialVersionUID = 42L;


If a serializable class does not explicitly declare a serialVersionUID, then the serialization runtime will calculate a
     default serialVersionUID value for that class based on various aspects of the class, as described in the Java(TM)
     Object Serialization Specification. However, it is strongly recommended that all serializable classes explicitly
     declare serialVersionUID values, since the default serialVersionUID computation is highly sensitive to class details
     that may vary depending on compiler implementations, and can thus result in unexpected InvalidClassExceptions
     during deserialization. Therefore, to guarantee a consistent serialVersionUID value across different java compiler
     implementations, a serializable class must declare an explicit serialVersionUID value. It is also strongly advised
     that explicit serialVersionUID declarations use the private modifier where possible, since such declarations apply
     only to the immediately declaring class--serialVersionUID fields are not useful as inherited members. Array classes
     cannot declare an explicit serialVersionUID, so they always have the default computed value, but the requirement
     for matching serialVersionUID values is waived for array classes.
     */
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name="user_id")
    private long userId;

    @Column(name="user_name", nullable = false, length=30)
    private String name;

    @Column(name="user_fullName", nullable = false, length=50)
    private String fullName;

    @Column(name="email", length=50)
    private String emailAddress;

    private String externalId;
    private String role;
    private String program;
    private String country;
    private boolean active;
    private String activeSearch;
    private String typeSearch;
    private boolean partnerUser;
    private Long partnerId;
    private String partnerName;
    private boolean wildcard = true;

    public User() {
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public Long getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(Long partnerId) {
        this.partnerId = partnerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getActiveSearch() {
        return activeSearch;
    }

    public void setActiveSearch(String activeSearch) {
        this.activeSearch = activeSearch;
    }

    public String getTypeSearch() {
        return typeSearch;
    }

    public void setTypeSearch(String typeSearch) {
        this.typeSearch = typeSearch;
    }

    public boolean isPartnerUser() {
        return partnerUser;
    }

    public void setPartnerUser(boolean partnerUser) {
        this.partnerUser = partnerUser;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getProgram() {
        return program;
    }

    public void setProgram(String program) {
        this.program = program;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public boolean isWildcard() {
        return wildcard;
    }

    public void setWildcard(boolean wildcard) {
        this.wildcard = wildcard;
    }

    public void setPartnerName(String name) {
        partnerName = name;
    }

    public String getPartnerName() {
        return partnerName;
    }

    @Override
    public String toString() {
        return "UserDTO{" +
                "userId='" + userId + '\'' +
                ", name='" + name + '\'' +
                ", fullName='" + fullName + '\'' +
                ", emailAddress='" + emailAddress + '\'' +
                ", role='" + role + '\'' +
                ", program='" + program + '\'' +
                ", country='" + country + '\'' +
                ", active=" + active +
                ", partnerUser=" + partnerUser +
                '}';
    }

}