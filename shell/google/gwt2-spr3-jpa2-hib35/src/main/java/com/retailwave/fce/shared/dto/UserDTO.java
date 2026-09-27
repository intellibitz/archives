package com.retailwave.fce.shared.dto;
/**
 * $Id: UserDTO.java 5 2010-06-03 11:07:35Z muthu $
 * $HeadURL: svn://10.10.200.111:3691/Finance/tags/framework-snapshot1/fce/src/main/java/com/retailwave/fce/shared/dto/UserDTO.java $
 */

import com.google.gwt.user.client.rpc.IsSerializable;

import java.io.Serializable;

public class UserDTO implements Serializable, IsSerializable {
    private String userId;
    private String externalId;
    private String name;
    private String fullName;
    private String emailAddress;
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
    private static final long serialVersionUID = -1895382209235209986L;

    public UserDTO() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
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
