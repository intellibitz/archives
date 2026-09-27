package com.ibt.intellimeet.data;

/*
<!--
$Id::                                                                           $: Id of last commit
$Rev::                                                                          $: Revision of last commit
$Author::                                                                       $: Author of last commit
$Date::                                                                         $: Date of last commit
$HeadURL::                                                                      $: Head URL of last commit
-->
*/

import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
import static org.jboss.seam.ScopeType.SESSION;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Name("user")
@Scope(SESSION)
@Entity
@Table(name = "USERS")
public class User
        implements Serializable
{

    static final long serialVersionUID = 2973374377453022888L;

    private long id;
    private String password;
    private String email;

    /**
     * Default constructor
     */
    public User()
    {
        super ();
    }

    public User(String email, String password)
    {
        this.email = email;
        this.password = password;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "USER_ID")
    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    @NotNull
    @Length(max = 100)
    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    @NotNull
    @Length(min = 5, max = 15)
    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    @Override
    public String toString()
    {
        return "User(" + email + ")";
    }
}